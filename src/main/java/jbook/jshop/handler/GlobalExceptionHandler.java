package jbook.jshop.handler;

import jbook.jshop.service.DynamoDbExceptionLoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private DynamoDbExceptionLoggerService exceptionLoggerService;

//    public GlobalExceptionHandler(DynamoDbExceptionLogger exceptionLogger) {
//        this.exceptionLogger = exceptionLogger;
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        // 1) 요청 URL
        String apiUrl = request.getRequestURI();

        // 2) 로그 저장
        exceptionLoggerService.logException(ex, apiUrl);

        // 3) 사용자 응답
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", ex.getMessage());
        responseBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseBody.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
