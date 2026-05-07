package com.dev.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableMethodSecurity // Phân quyền dựa trên method
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {"/users", "/auth/login", "/auth/introspect"
            , "/permissions", "/roles", "/auth/logout", "/auth/refresh", "/auth/myInfor"
            , "/students", "/teachers"};

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
//    @Value("${jwt.sharedKey}")
//    private String sharedKey;


    /*
    mỗi 1 request nó sẽ đi qua HTTP Request
       ↓
    Security Filter Chain  ❗
       ↓
    JwtDecoder.decode()    ❗ (trong đây nó sẽ introspect Token nên mỗi request nó sẽ verifyToken
       ↓
    Controller
    */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll());    // cho phép tất cả

//                .csrf(csrf -> csrf.disable()).authorizeHttpRequests(request ->
//                        request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
//                                //Phân quyền dựa trên Endpoint
////                                .requestMatchers(HttpMethod.GET, "/users")
////                                .hasRole(Role.ADMIN.name())
//                                .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS).permitAll()
//
//                                .anyRequest().authenticated());
        http
                // 1. THÊM DÒNG NÀY: Bật CORS ở mức Security Filter
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() //fix CORS
                        .requestMatchers("/auth/**").permitAll()                // ✅ login, refresh
                        .anyRequest().authenticated());


        // chặn và cấp quyền request thông qua token
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        //authenticationEntryPoint: Xử lý exception 401 Unauthorized (bị chặn request - token ko hợp lệ)

        return http.build();
    }
//    @Bean
//     JwtDecoder jwtDecoder(){
//        SecretKeySpec secretKeySpec = new SecretKeySpec(sharedKey.getBytes(), "HS256");
//        return NimbusJwtDecoder
//                .withSecretKey(secretKeySpec)
//                .macAlgorithm(MacAlgorithm.HS256)
//                .build();
//    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:5173");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
