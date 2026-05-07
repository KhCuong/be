package com.dev.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi k xác định", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_EXISTED(1002, "User đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User ko tồn tại", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED(1008, "Role ko tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "UNAUTHENTICATED", HttpStatus.UNAUTHORIZED),
    USERNAME_INVALD(1003, "username phải nhiều hơn 3 kí tự", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALD(1004, "password phải nhiều hơn 8 kí tự", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN);

            ;
    private int code;
    private String massage;
    private HttpStatusCode httpStatusCode;

    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }

    ErrorCode(int code, String massage, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.massage = massage;
        this.httpStatusCode = httpStatusCode;
    }

    public int getCode() {
        return code;
    }

    public String getMassage() {
        return massage;
    }
}
