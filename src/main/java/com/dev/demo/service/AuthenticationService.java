package com.dev.demo.service;

import com.dev.demo.dto.request.AuthenticationRequest;
import com.dev.demo.dto.request.IntrospectRequest;
import com.dev.demo.dto.request.LogoutRequest;
import com.dev.demo.dto.request.RefreshRequest;
import com.dev.demo.dto.response.AuthenticationResponse;
import com.dev.demo.dto.response.IntrospectResponse;
import com.dev.demo.entity.BlackListToken;
import com.dev.demo.entity.User;
import com.dev.demo.exception.AppException;
import com.dev.demo.exception.ErrorCode;
import com.dev.demo.mapper.RoleMapper;
import com.dev.demo.repository.BlackListTokenRepository;
import com.dev.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class AuthenticationService {
    // [THAY ĐỔI QUAN TRỌNG]: Định nghĩa các hằng số
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";
    private static final long ACCESS_TOKEN_EXPIRATION = (5 * 60 * 1000); // 1 phút
    private static final long REFRESH_TOKEN_EXPIRATION = 7L * 24 * 60 * 60 * 1000L; // 7 ngày
    @Value("${jwt.sharedKey}")
    protected String sharedKey;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private BlackListTokenRepository blackListTokenRepository;

    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        String token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, TYPE_ACCESS);
        } catch (AppException e) {
            isValid = false;
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        // trả ra exception nếu nó hết hạn hoặc verify thất bại k thì trả về true(verify thành công)
        return new IntrospectResponse(isValid);
    }

    public SignedJWT verifyToken(String token, String expectedType) throws ParseException, JOSEException, TimeoutException {

        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(sharedKey.getBytes());

        boolean verified = signedJWT.verify(verifier);

        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        // 1. Kiểm tra chữ ký và hạn sử dụng (Bây giờ lấy expTime gốc là chuẩn)
        if (!(verified && expTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 2. [THAY ĐỔI QUAN TRỌNG]: Kiểm tra token_type xem có đúng loại đang cần không
        String tokenType = signedJWT.getJWTClaimsSet().getStringClaim("token_type");
        if (!expectedType.equals(tokenType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Do mỗi 1 request <-> 1 query DB => muốn tiết kiệm tài nguyên thì verifyToken trc(token fake bị loại ngay)
        // Chỉ check blacklist với REFRESH token
        if (TYPE_REFRESH.equals(tokenType)) {
            if (blackListTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }
        return signedJWT;
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException, TimeoutException {
        try {
            SignedJWT signedJWT = verifyToken(request.getToken(), TYPE_REFRESH);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();

            Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            BlackListToken blackListToken = new BlackListToken();
            blackListToken.setId(jti);
            blackListToken.setExpTime(expTime);
            blackListTokenRepository.save(blackListToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException, TimeoutException {
        SignedJWT signedJWT = verifyToken(request.getToken(), TYPE_REFRESH);

        var jti = signedJWT.getJWTClaimsSet().getJWTID();

        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        BlackListToken blackListToken = new BlackListToken();
        blackListToken.setId(jti);
        blackListToken.setExpTime(expTime);
        blackListTokenRepository.save(blackListToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // [THAY ĐỔI QUAN TRỌNG]: Cấp lại 1 cặp token mới hoàn toàn
        String newAccessToken = generateToken(user, TYPE_ACCESS, ACCESS_TOKEN_EXPIRATION);
        String newRefreshToken = generateToken(user, TYPE_REFRESH, REFRESH_TOKEN_EXPIRATION);

        return new AuthenticationResponse(newAccessToken, newRefreshToken, true);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

         /*
        matches(rawPassword, encodedPassword)
        rawPassword → password người dùng nhập (plain text)
        encodedPassword → password đã hash trong DB
        */
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);
        // [THAY ĐỔI QUAN TRỌNG]: Tạo ra 2 token riêng biệt
        String accessToken = generateToken(user, TYPE_ACCESS, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = generateToken(user, TYPE_REFRESH, REFRESH_TOKEN_EXPIRATION);

        return new AuthenticationResponse(accessToken, refreshToken, true);
    }

    private String generateToken(User user, String tokenType, long duration) {
        // Thuật toán sử dụng
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);


        // Set nội dụng phần thân
        JWTClaimsSet.Builder jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("Quản lý sv MIM")
                .issueTime(new Date())
                .expirationTime(new Date(
                        // SỬA Ở ĐÂY: Đổi thành 1 giờ (60 phút * 60 giây * 1000 ms) thay vì 1 phút để chạy thực tế
                        System.currentTimeMillis() + duration)
                )
                .jwtID(UUID.randomUUID().toString()) // (id duy nhất của mỗi token chuỗi 32 kí tự - không trùng)
                .claim("token_type", tokenType);
// [THAY ĐỔI QUAN TRỌNG]: Chỉ nhét Roles/Permissions vào Access Token để làm nhẹ Refresh Token
        if (TYPE_ACCESS.equals(tokenType)) {
            jwtClaimsSet.claim("scope", buildScope(user));
        }
        // phần nội dung
        Payload payload = new Payload(jwtClaimsSet.build().toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        // Ký
        try {
            jwsObject.sign(new MACSigner(sharedKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            System.out.println("lỗi token: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }
}