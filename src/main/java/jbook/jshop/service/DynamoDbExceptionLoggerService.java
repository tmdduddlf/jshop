package jbook.jshop.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
//import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
//import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
//import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.UUID;

@Slf4j
@Service
public class DynamoDbExceptionLoggerService {

//    private final DynamoDbClient dynamoDbClient;
    private static final String TABLE_NAME = "exception_logs";

//    public DynamoDbExceptionLogger(DynamoDbClient dynamoDbClient) {
//        this.dynamoDbClient = dynamoDbClient;
//    }

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
//        Map<String, AttributeValue> item = new HashMap<>();
//        item.put("logId", AttributeValue.builder().s(logId).build());
//        item.put("timestamp", AttributeValue.builder().n(String.valueOf(timestamp)).build());
//        item.put("apiUrl", AttributeValue.builder().s(apiUrl).build());
//        item.put("className", AttributeValue.builder().s(className).build());
//        item.put("methodName", AttributeValue.builder().s(methodName).build());
//        item.put("lineNumber", AttributeValue.builder().n(String.valueOf(lineNumber)).build());
//        item.put("exceptionType", AttributeValue.builder().s(ex.getClass().getName()).build());
//        item.put("message", AttributeValue.builder().s(ex.getMessage()).build());
//        item.put("stackTrace", AttributeValue.builder().s(fullStackTrace).build());

        // 4) DynamoDB 가 없어서 로그만
        log.info("[DynamoDB] log start");
        log.info("[DynamoDB] logId : {}", logId);
        log.info("[DynamoDB] timestamp : {}", String.valueOf(timestamp));
        log.info("[DynamoDB] apiUrl : {}", apiUrl);
        log.info("[DynamoDB] className : {}", className);
        log.info("[DynamoDB] methodName : {}", methodName);
        log.info("[DynamoDB] lineNumber : {}", String.valueOf(lineNumber));
        log.info("[DynamoDB] exceptionType : {}", ex.getClass().getName());
        log.info("[DynamoDB] message : {}", ex.getMessage());
        log.info("[DynamoDB] stackTrace : {}", fullStackTrace);
        log.info("[DynamoDB] log end");



        // 5) DynamoDB에 PutItem
//        PutItemRequest putItemRequest = PutItemRequest.builder()
//                .tableName(TABLE_NAME)
//                .item(item)
//                .build();
//        dynamoDbClient.putItem(putItemRequest);
    }

    private String buildStackTraceString(Exception ex) {
//        StringBuilder sb = new StringBuilder();
//        for (StackTraceElement ste : ex.getStackTrace()) {
//            sb.append(ste.toString()).append("\n");
//        }
//        return sb.toString();
        return ExceptionUtils.getStackTrace(ex);
    }
}
