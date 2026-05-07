package com.dev.demo.exception;

import com.dev.demo.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandle {
    @ExceptionHandler(Exception.class)
    // Các Exception ko thuộc các Exception ở dưới (nghĩa là trường hợp ngoại lai)
    public ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception) {
        ApiResponse response = new ApiResponse();
        response.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        response.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMassage());

        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ApiResponse response = new ApiResponse();
        response.setCode(exception.getErrorCode().getCode());
        response.setMessage(exception.getErrorCode().getMassage());

        return ResponseEntity.status(exception.getErrorCode().getHttpStatusCode()).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ApiResponse response = new ApiResponse();
        response.setCode(ErrorCode.UNAUTHORIZED.getCode());
        response.setMessage(ErrorCode.UNAUTHORIZED.getMassage());

        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getHttpStatusCode()).body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handlingValidException(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.valueOf(enumKey);
        ApiResponse response = new ApiResponse();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMassage());

        return ResponseEntity.badRequest().body(response);
    }


    // 1. Bắt lỗi File Upload quá dung lượng (Cực kỳ quan trọng cho API Import Excel)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        ApiResponse response = new ApiResponse<>();
        response.setCode(413); // 413: Payload Too Large
        response.setMessage("Dung lượng file vượt quá giới hạn cho phép. Vui lòng upload file nhỏ hơn.");

        return ResponseEntity.badRequest().body(response);
    }
}
