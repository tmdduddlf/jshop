아래 예시는 **두 가지 방식**— (1) **글로벌 예외 처리(@RestControllerAdvice + @ExceptionHandler)**, (2) **AOP(@AfterThrowing)** — 로 **예외가 발생했을 때 DynamoDB에 상세 정보를 기록**하는 예시 코드야.  
특히 요청 URL, 클래스명, 메서드명, 코드 라인, 스택트레이스까지 저장하도록 구현했어.

---

# 1) 전역 예외 처리 방식 (`@RestControllerAdvice + @ExceptionHandler`)

## (1) 구현 계획 (4단계)

1. **설정(Setup)**
    - `@RestControllerAdvice` 클래스 생성
    - HttpServletRequest 혹은 Spring WebRequest를 통해 **API URL** 등 요청 정보를 가져올 수 있게 설정
    - DynamoDB에 저장할 서비스 로직(`DynamoDbExceptionLogger`) 준비

2. **구현(Implementation)**
    - `GlobalExceptionHandler` 클래스에서 `@ExceptionHandler(Exception.class)` 작성
    - 예외 발생 시, `HttpServletRequest`로부터 **URL**을 추출, 예외 객체의 **StackTrace**를 활용해 클래스명, 메서드명, 코드 라인 등을 파싱해 DynamoDB에 저장

3. **테스트(Testing)**
    - 컨트롤러에서 임의 예외를 발생시켜 호출 (Postman, curl 등)
    - DynamoDB 테이블에서 기록된 정보 확인

4. **배포(Deployment)**
    - CI/CD 파이프라인에 포함
    - 운영 환경에서 스택트레이스/요청 URL 등이 정상적으로 기록되는지 모니터링

---

## (2) 예시 코드

> **아티팩트**: `GlobalExceptionHandler.java`, `DynamoDbExceptionLogger.java` 등의 파일로 구성

### GlobalExceptionHandler.java
```java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final DynamoDbExceptionLogger exceptionLogger;

    public GlobalExceptionHandler(DynamoDbExceptionLogger exceptionLogger) {
        this.exceptionLogger = exceptionLogger;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(
            Exception ex,
            HttpServletRequest request
    ) {
        // 1) 요청 URL
        String apiUrl = request.getRequestURI();

        // 2) 로그 저장
        exceptionLogger.logException(ex, apiUrl);

        // 3) 사용자 응답
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", ex.getMessage());
        responseBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseBody.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```
- `@RestControllerAdvice`로 모든 컨트롤러에서 발생하는 예외를 잡는다.
- `@ExceptionHandler(Exception.class)`를 통해 **모든 예외**를 처리한다.
- `HttpServletRequest request`를 통해 **API URL**(URI)을 획득할 수 있다.

### DynamoDbExceptionLogger.java
```java
package com.example.demo.exception;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DynamoDbExceptionLogger {

    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "exception_logs";

    public DynamoDbExceptionLogger(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void logException(Exception ex, String apiUrl) {
        // 1) 기본 정보
        String logId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        // 2) StackTrace에서 첫 번째 라인 가져오기 (클래스명, 메서드명, 라인번호)
        StackTraceElement firstTrace = ex.getStackTrace().length > 0 ? ex.getStackTrace()[0] : null;
        String className = firstTrace != null ? firstTrace.getClassName() : "unknown";
        String methodName = firstTrace != null ? firstTrace.getMethodName() : "unknown";
        int lineNumber = firstTrace != null ? firstTrace.getLineNumber() : -1;

        // 3) 스택트레이스 전체 문자열화
        String fullStackTrace = buildStackTraceString(ex);

        // 4) DynamoDB에 put할 아이템 구성
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("logId", AttributeValue.builder().s(logId).build());
        item.put("timestamp", AttributeValue.builder().n(String.valueOf(timestamp)).build());
        item.put("apiUrl", AttributeValue.builder().s(apiUrl).build());
        item.put("className", AttributeValue.builder().s(className).build());
        item.put("methodName", AttributeValue.builder().s(methodName).build());
        item.put("lineNumber", AttributeValue.builder().n(String.valueOf(lineNumber)).build());
        item.put("exceptionType", AttributeValue.builder().s(ex.getClass().getName()).build());
        item.put("message", AttributeValue.builder().s(ex.getMessage()).build());
        item.put("stackTrace", AttributeValue.builder().s(fullStackTrace).build());

        // 5) DynamoDB에 PutItem
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
        dynamoDbClient.putItem(putItemRequest);
    }

    private String buildStackTraceString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement ste : ex.getStackTrace()) {
            sb.append(ste.toString()).append("\n");
        }
        return sb.toString();
    }
}
```
- `className`, `methodName`, `lineNumber` 등은 **스택트레이스의 첫 번째 요소**를 활용해서 뽑는다.
- **스택트레이스 전체**를 `String`으로 변환하여 DynamoDB에 저장(아이템 크기가 커질 수 있으니 주의).
- `apiUrl`은 컨트롤러 레벨에서 인자로 받은 값.

---

# 2) AOP 방식 (`@Aspect + @AfterThrowing`)

## (1) 구현 계획 (4단계)

1. **설정(Setup)**
    - `spring-boot-starter-aop` 의존성 확인
    - AOP 클래스에 `@Aspect`와 `@Component` 달기
    - Pointcut 범위 설정(예: `com.example.demo.controller..*(..)`), 혹은 `service..*(..)` 등 원하는 계층 지정

2. **구현(Implementation)**
    - `@AfterThrowing` 어드바이스 메서드를 만들어 **해당 범위의 메서드**에서 발생한 예외를 잡는다.
    - 요청 URL이 필요하다면, **`RequestContextHolder`**를 사용해 `HttpServletRequest` 획득 → API URL 추출
    - 예외 정보(스택트레이스, 클래스/메서드명, 라인번호)는 예외 객체와 `JoinPoint`로부터 가져온다.

3. **테스트(Testing)**
    - AOP 활성화가 잘 되었는지(@EnableAspectJAutoProxy) 확인
    - 일부러 예외를 발생시켜 DynamoDB에 기록되는지 검증

4. **배포(Deployment)**
    - 운영환경에서 예외 발생 시 DynamoDB에 쌓이는지 모니터링

---

## (2) 예시 코드

### ExceptionLoggingAspect.java
```java
package com.example.demo.aop;

import com.example.demo.exception.DynamoDbExceptionLogger;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@RequiredArgsConstructor
public class ExceptionLoggingAspect {

    private final DynamoDbExceptionLogger exceptionLogger;

    @AfterThrowing(pointcut = "execution(* com.example.demo.controller..*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        // 1) RequestContextHolder에서 HttpServletRequest 가져오기
        ServletRequestAttributes sra =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String apiUrl = "unknown";
        if (sra != null) {
            HttpServletRequest request = sra.getRequest();
            apiUrl = request.getRequestURI();
        }

        // 2) Throwable -> Exception 변환 가능 시 DynamoDB 로깅
        if (ex instanceof Exception) {
            Exception exception = (Exception) ex;
            // 3) 클래스명, 메서드명 등
            String signature = joinPoint.getSignature().toShortString();
            // ex.getStackTrace()에서 라인 번호 등 상세 추출 가능 (Logger 내부에서도 처리)

            exceptionLogger.logException(exception, apiUrl, signature);
        }
        // 예외는 다시 호출자에게 전달됨 (throw)
    }
}
```
- `pointcut = "execution(* com.example.demo.controller..*(..))"` : `com.example.demo.controller` 패키지(하위 포함)의 **모든 메서드**에서 발생한 예외를 잡는다.
- `RequestContextHolder`를 써서 **HTTP 요청 정보**(URL 등)를 획득.
- `signature`에는 `ControllerName.method(..)` 형태의 간략한 시그니처가 들어간다.

### DynamoDbExceptionLogger.java (AOP 버전 예시)
```java
package com.example.demo.exception;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DynamoDbExceptionLogger {

    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "exception_logs";

    public DynamoDbExceptionLogger(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * 예시: AOP 방식에서 apiUrl, signature를 함께 로그
     */
    public void logException(Exception ex, String apiUrl, String signature) {
        String logId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        // 스택트레이스 첫 줄로 class, method, line 추출
        StackTraceElement firstTrace = ex.getStackTrace().length > 0 ? ex.getStackTrace()[0] : null;
        String className = (firstTrace != null) ? firstTrace.getClassName() : "unknown";
        String methodName = (firstTrace != null) ? firstTrace.getMethodName() : "unknown";
        int lineNumber = (firstTrace != null) ? firstTrace.getLineNumber() : -1;

        // 전체 스택트레이스
        String fullStack = buildStackTraceString(ex);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("logId", AttributeValue.builder().s(logId).build());
        item.put("timestamp", AttributeValue.builder().n(String.valueOf(timestamp)).build());
        item.put("apiUrl", AttributeValue.builder().s(apiUrl).build());
        // AOP에서 메서드 시그니처
        item.put("signature", AttributeValue.builder().s(signature).build());
        item.put("className", AttributeValue.builder().s(className).build());
        item.put("methodName", AttributeValue.builder().s(methodName).build());
        item.put("lineNumber", AttributeValue.builder().n(String.valueOf(lineNumber)).build());

        item.put("exceptionType", AttributeValue.builder().s(ex.getClass().getName()).build());
        item.put("message", AttributeValue.builder().s(ex.getMessage()).build());
        item.put("stackTrace", AttributeValue.builder().s(fullStack).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(putItemRequest);
    }

    private String buildStackTraceString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement ste : ex.getStackTrace()) {
            sb.append(ste.toString()).append("\n");
        }
        return sb.toString();
    }
}
```
- 여기서는 `signature`를 추가로 받아서 **AOP Pointcut** 관점의 메서드 정보까지 기록
- `className`, `methodName`, `lineNumber`는 스택트레이스에서 파싱
- `apiUrl` 역시 웹 요청에서 추출해 함께 저장

---

# 3) 핵심 요약

1. **전역 예외 처리(@RestControllerAdvice + @ExceptionHandler)**
    - 모든 컨트롤러 계층에서 발생한 예외가 **외부로 전파**되면 잡아낸다.
    - `HttpServletRequest`를 주입받아 **API URL** 등 요청 정보를 손쉽게 획득.
    - **비즈니스 로직** 내부에서 예외를 `catch` 후 삼키지 않고 **재-throw**해야만 로깅 가능.

2. **AOP(@AfterThrowing)**
    - 특정 **pointcut** 범위(Controller, Service, etc.)에서 발생하는 예외를 잡는다.
    - `RequestContextHolder`를 통해 **요청 정보**를 가져와 DynamoDB에 함께 로그.
    - 마찬가지로, **재-throw**되지 않은 예외(내부에서 삼켜진 예외)는 인지 불가.

3. **공통사항**
    - 예외 발생 시 **스택트레이스**를 추출해 클래스, 메서드, 라인번호를 파악
    - **DynamoDB** 테이블에 Item을 저장할 때, **I/O 비용**과 **데이터 크기**에 유의 (스택트레이스가 길면 저장 비용 증가)
    - 실제 운영 시에는 **TTL**(Time To Live) 설정 등으로 로그 관리(자동 만료)도 고려

이렇게 두 가지 방식을 모두 적용해 보면, 상황에 따라 **글로벌 예외 처리**와 **AOP 방식** 각각의 장단점을 이해하고 원하는 구조로 확장할 수 있을 거야! 필요하면 추가 질문해 줘.~~~~