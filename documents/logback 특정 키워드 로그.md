아래는 **H2 DB** 환경에서 “특정 키워드(syiif) + JSON 데이터 → DB 적재” 구조를 **테스트용**으로 구현하는 예시야.  
**인메모리 DB** 또는 **파일 기반 DB** 모두 H2로 가능하지만, 여기서는 **인메모리** 예시를 통해 간단히 시연해볼게.

---

## 1. H2 DB 설정

### 1.1 Spring Boot 의존성 (Maven 예시)

```xml
<dependencies>
    <!-- H2 DB -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.1.214</version> <!-- 원하는 버전 -->
        <scope>runtime</scope>
    </dependency>

    <!-- Spring Boot Starter Logging (Logback 포함) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-logging</artifactId>
        <version>3.0.3</version>
    </dependency>
    
    <!-- 기타 필요 의존성 -->
</dependencies>
```

### 1.2 application.yml (인메모리 H2)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update   # 간단 테스트용 (실운영에는 권장 X)
```

- **`jdbc:h2:mem:testdb`**: 인메모리 DB
- 서버가 꺼지면 **데이터도 사라짐**. (테스트 시엔 편리)
- **`DB_CLOSE_ON_EXIT=FALSE`**는 서버 종료 시점에 DB가 바로 닫히지 않게 하는 설정(원하는 대로 조정)

---

## 2. 테이블 생성

### 2.1 간단한 방식 - JPA Entity

**테스트**만 빠르게 할 거라면, JPA Entity를 만들어 **`ddl-auto: update`**로 자동 생성시키는 방법이 가장 편리해.  
(물론 직접 `schema.sql` 넣거나 Flyway/Migration 툴 써도 됨.)

예: `SyiifLogEntity.java`
```java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TB_LOG_SYIIF")
@Getter
@Setter
public class SyiifLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String interfaceId;
    private String headerKey;
    private String name;
    private String messageText;

    @Column(length = 4000) // H2 인메모리이므로 충분히 큰 사이즈
    private String rawData;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;
}
```

- 이렇게 하면 **Spring Boot**가 런타임에 `TB_LOG_SYIIF` 테이블을 알아서 생성해줌.
- `createdAt` 필드는 직접 세팅하거나, DB Default로 `CURRENT_TIMESTAMP`를 줄 수도 있음.

---

## 3. Logback 설정 & Custom Appender

### 3.1 logback-spring.xml

```xml
<configuration>

    <!-- syiif 로그를 DB에 저장할 Appender -->
    <appender name="SYIIF_DB_APPENDER" class="com.example.demo.log.SyiifDbAppender">
        <!-- syiif 키워드 필터 -->
        <filter class="com.example.demo.log.SyiifKeywordFilter"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="SYIIF_DB_APPENDER" />
        <!-- 필요하다면 콘솔이나 파일 로그도 추가 -->
        <!-- <appender-ref ref="CONSOLE" /> -->
    </root>

</configuration>
```

> - `com.example.demo.log.SyiifKeywordFilter`와 `SyiifDbAppender`는 아래에서 구현 예시를 볼 거야.

---

### 3.2 SyiifKeywordFilter.java

```java
package com.example.demo.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * 로그 메시지에 'syiif'라는 문자열이 들어있으면 필터 통과
 */
public class SyiifKeywordFilter extends Filter<ILoggingEvent> {

    private static final String KEYWORD = "syiif";

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String msg = event.getFormattedMessage();
        if (msg.contains(KEYWORD)) {
            return FilterReply.ACCEPT; 
        }
        return FilterReply.NEUTRAL; 
    }
}
```

---

### 3.3 SyiifDbAppender.java (H2 DB + JPA Repository 예시)

실무에서도 **Appender**에서 직접 JDBC 코드를 쓰는 대신,  
**스프링 빈**(Repository) 의존성을 주입해 **save()**를 부를 수 있음.  
그러나 **Logback Appender**는 스프링 빈 라이프사이클과 얽히기 조금 까다롭다.  
테스트 용도라면, **엔티티매니저**나 **레포지토리**를 간단히 가져오는 식으로 시연해볼게.

#### A) 가장 간단히: 직접 JDBC

아래는 “H2 인메모리로 직접 연결” 방식. (엔티티 자동 생성에 의존하지 않는 버전)

```java
package com.example.demo.log;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

public class SyiifDbAppender extends AppenderBase<ILoggingEvent> {

    private ObjectMapper objectMapper = new ObjectMapper();

    // H2 in-memory DB url
    private static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    @Override
    protected void append(ILoggingEvent event) {
        String message = event.getFormattedMessage();

        try {
            // syiif 있으면 -> JSON 파싱 시도
            JsonNode root = objectMapper.readTree(message);

            // 필요한 4개 키 추출
            String interfaceId = getSafeString(root, "interfaceId");
            String headerKey = getSafeString(root, "headerKey");
            String name = getSafeString(root, "name");
            String msgValue = getSafeString(root, "message");

            // 방어 로직: 4개 중 하나라도 비어있으면 그냥 스킵
            if (interfaceId.isEmpty() && headerKey.isEmpty() && name.isEmpty() && msgValue.isEmpty()) {
                addInfo("[SyiifDbAppender] Missing required fields -> skip DB insert.");
                return;
            }

            // DB Insert
            insertToDb(interfaceId, headerKey, name, msgValue, message);

        } catch (Exception e) {
            // JSON 아니거나 기타 에러 -> 로그 남기고 무시
            addError("[SyiifDbAppender] Failed to parse/insert JSON", e);
        }
    }

    private String getSafeString(JsonNode node, String key) {
        if (!node.has(key)) {
            return "";
        }
        return node.get(key).asText("");
    }

    private void insertToDb(String interfaceId, 
                            String headerKey,
                            String name,
                            String msgValue,
                            String rawData) throws SQLException {

        String sql = "INSERT INTO TB_LOG_SYIIF (INTERFACE_ID, HEADER_KEY, NAME, MESSAGE_TEXT, RAW_DATA, CREATED_AT)"
                   + " VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, interfaceId);
            pstmt.setString(2, headerKey);
            pstmt.setString(3, name);
            pstmt.setString(4, msgValue);
            pstmt.setString(5, rawData);
            pstmt.executeUpdate();
        }
    }
}
```

> - **주의**: `TB_LOG_SYIIF` 테이블을 사전에 생성하거나 `ddl-auto`로 생성해야 함.
> - 매번 `DriverManager.getConnection` 호출은 성능에 부담이 될 수 있으니, 실제 대량 로그 시엔 **풀링** or **비동기** 접근 고려.

---

#### B) 테이블 생성 예시 (스키마.sql)

만약 **JPA**를 안 쓰고, 수동으로 테이블 만들고 싶다면 `src/main/resources/schema.sql`에:

```sql
CREATE TABLE TB_LOG_SYIIF (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    INTERFACE_ID VARCHAR(100),
    HEADER_KEY VARCHAR(100),
    NAME VARCHAR(100),
    MESSAGE_TEXT VARCHAR(2000),
    RAW_DATA CLOB,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

- 스프링 부트는 **ddl-auto=none** + `schema.sql`을 발견하면 자동 실행해 테이블 생성해 줌(H2 등).

---

## 4. 실제 로그 호출 & 결과

예시 Controller:

```java
package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test-syiif")
    public String testSyiifLog() {
        // syiif + JSON
        String jsonPart = "{\"interfaceId\":\"IF-123\",\"headerKey\":\"HDR-999\",\"name\":\"John\",\"message\":\"Hello Syiif\"}";
        log.info("syiif => " + jsonPart);

        // syiif 키워드가 없어 -> DB에 안 들어감
        log.info("This is normal log without syiif keyword");

        // syiif but not JSON
        log.info("syiif => Not a JSON string");

        return "Check the logs, maybe some DB insertion happened if JSON was correct!";
    }
}
```

- `/test-syiif` 호출 시:
    1. `"syiif => {\"interfaceId\":\"IF-123\", ... }"` → **키워드**: `syiif`
    2. 필터 통과 → **DBAppender**
    3. JSON 파싱 성공 → **TB_LOG_SYIIF** 에 Insert (interfaceId=IF-123, headerKey=HDR-999, …)
- **H2 콘솔**:
    - 스프링 부트에서 `spring.h2.console.enabled=true`로 설정하면, `/h2-console`에서 접속 확인 가능
    - DB URL: `jdbc:h2:mem:testdb`

---

## 5. 성능 및 확장 고려

1. **Log Volume**
    - H2 인메모리는 대량 로그 저장 시 **메모리 부족** 가능
    - 실제 운영: 외부 RDB(MySQL, Oracle 등) + 분산 로그 수집(ELK, Kafka)
2. **동기 I/O 문제**
    - 로그가 찍힐 때마다 **DB Insert**: 대량 트래픽 시 성능 저하
    - → `AsyncAppender` 쓰거나, 로깅 메시지를 **Queue**로 보낸 뒤, 별도 Consumer가 DB Insert (비동기)
3. **테이블 파티션 / 주기적 삭제**
    - LOG 테이블은 시간이 지나면 **엄청나게 커짐**
    - H2 인메모리에는 비추, 프로덕션 RDBMS는 **파티셔닝**·**아카이빙** 설계 필요
4. **JSON 확장성**
    - 현재는 **4개 key**만 추출, 나머지는 `RAW_DATA`에 전체 저장
    - 새로운 키가 필요해지면 **schema 변경** 없이 `RAW_DATA`에서 추후 파싱 가능

---

## 요약

- **H2 인메모리 DB**로 로깅 로직을 테스트하려면:
    1) `pom.xml` 혹은 `build.gradle`에 **H2** 의존성 추가
    2) `application.yml`에서 **jdbc:h2:mem:testdb** 설정
    3) 테이블 스키마 준비(`schema.sql` 또는 JPA Entity)
    4) **Logback Appender**(`SyiifDbAppender`)에서 `"syiif"`가 포함된 로그 메시지를 **JSON 파싱** → DB Insert
- 이렇게 하면 간단한 **PoC**나 **개발 환경**에서 “특정 키워드 로그를 DB에 기록하는” 기능을 손쉽게 시연/검증할 수 있음.
- 대규모 트래픽 대응 등 실무 적용 시에는 **비동기 처리**, **외부 DB**, **분산 로깅** 등을 꼭 고려해야 한다.~~~~




아래 예시는 **MyBatis XML Mapper** 방식을 이용해,  
**H2 DB**에 “syiif 키워드가 포함된 로그 + JSON 파싱된 4개 필드”를 저장하는  
**간단한 Spring Boot + Logback** 연동 예시야.

---

# 1. 전반적 흐름

1. **Logback**에서 **특정 키워드(syiif)**가 포함된 로그만 **Custom Appender**로 유입
2. **Appender**가 **JSON 파싱**해 4개 키( interfaceId, headerKey, name, message )를 추출
3. **MyBatis**의 **XML Mapper**를 통해 **DB Insert**
4. **H2 DB** 테스트(인메모리 또는 파일 DB)로 확인

**주의**:
- 실제 대규모 트래픽 시에는 **동기 DB Insert**보다 **비동기 처리**, **메시지 큐** 등을 고려해야 한다.
- Logback Appender → Spring Bean(Mapper) 주입 시 **생명주기** 문제가 생길 수 있으니,  
  예제처럼 **ApplicationContextAware**를 사용하거나 다른 방법(별도 LoggerService Bean)으로 우회해야 한다.

---

# 2. 의존성 (Maven 예시)

```xml
<dependencies>
    <!-- Spring Boot Starter Web (테스트용) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.0.3</version>
    </dependency>

    <!-- MyBatis Spring Boot Starter -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>3.0.0</version> <!-- 예시 버전 -->
    </dependency>

    <!-- H2 DB -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.1.214</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Logback (기본적으로 spring-boot-starter-logging에 포함) -->
    <!-- 필요 시 명시 추가: ch.qos.logback:logback-classic -->

    <!-- Jackson (스프링 부트 내부적으로 이미 포함) -->
</dependencies>
```

---

# 3. DB 설정 & 테이블

## 3.1 application.yml (간단 인메모리 H2 예시)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: none  # MyBatis, 수동 스키마로
mybatis:
  mapper-locations: classpath:mapper/*.xml
```

- **`DB_CLOSE_ON_EXIT=FALSE`**: 애플리케이션이 종료되어도 H2 세션을 당장 닫지 않음 (테스트 시 편리)
- **`mybatis.mapper-locations`**: XML Mapper 파일 경로 지정(예: `src/main/resources/mapper/`)

## 3.2 테이블 생성 (schema.sql)

`src/main/resources/schema.sql` (스프링 부트가 자동 실행)

```sql
CREATE TABLE TB_LOG_SYIIF (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    INTERFACE_ID VARCHAR(100),
    HEADER_KEY VARCHAR(100),
    NAME VARCHAR(100),
    MESSAGE_TEXT VARCHAR(1000),
    RAW_DATA CLOB,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

- **H2**에서 `AUTO_INCREMENT` 사용 가능
- `CLOB`(or `TEXT`)로 원본 JSON 보관 (확장성 위해)
- 로그가 엄청나게 쌓이면, 실무에서는 파티션·아카이빙 고려

---

# 4. MyBatis XML Mapper 구현

## 4.1 DTO (예: `SyiifLogDto`)

```java
package com.example.demo.dto;

import lombok.Data;

@Data
public class SyiifLogDto {
    private String interfaceId;
    private String headerKey;
    private String name;
    private String messageText;
    private String rawData;
}
```

## 4.2 Mapper 인터페이스 (`SyiifLogMapper.java`)

```java
package com.example.demo.mapper;

import com.example.demo.dto.SyiifLogDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SyiifLogMapper {
    int insertLog(SyiifLogDto logDto);
}
```

- `@Mapper`로 등록 → 스프링 부트가 자동으로 구현체를 만든다.

## 4.3 XML (`SyiifLogMapper.xml`)

`src/main/resources/mapper/SyiifLogMapper.xml` (경로는 위 `application.yml`에서 지정)

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
    PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.example.demo.mapper.SyiifLogMapper">

    <insert id="insertLog" parameterType="com.example.demo.dto.SyiifLogDto">
        INSERT INTO TB_LOG_SYIIF
        (INTERFACE_ID, HEADER_KEY, NAME, MESSAGE_TEXT, RAW_DATA)
        VALUES
        (#{interfaceId}, #{headerKey}, #{name}, #{messageText}, #{rawData})
    </insert>

</mapper>
```

- DB `CREATED_AT` 컬럼은 `DEFAULT CURRENT_TIMESTAMP`로 자동 세팅되므로 Insert 문에서 생략

---

# 5. Logback 설정 + Custom Appender

## 5.1 logback-spring.xml

```xml
<configuration>
    <!-- 1) syiif 키워드 필터 + DBAppender -->
    <appender name="SYIIF_DB_APPENDER" class="com.example.demo.log.SyiifDbAppender">
        <filter class="com.example.demo.log.SyiifKeywordFilter" />
    </appender>

    <!-- 2) Root logger -->
    <root level="INFO">
        <!-- syiif appender -->
        <appender-ref ref="SYIIF_DB_APPENDER"/>
        <!-- 콘솔 or 파일 appender도 원하면 추가 -->
        <!-- <appender-ref ref="CONSOLE" /> -->
    </root>
</configuration>
```

## 5.2 `SyiifKeywordFilter.java`

```java
package com.example.demo.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * syiif 키워드가 메시지에 포함될 때만 ACCEPT
 */
public class SyiifKeywordFilter extends Filter<ILoggingEvent> {

    private static final String KEYWORD = "syiif";

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message.contains(KEYWORD)) {
            return FilterReply.ACCEPT; // DBAppender로 넘어감
        }
        return FilterReply.NEUTRAL;
    }
}
```

## 5.3 `SyiifDbAppender.java` (핵심)

**문제**: Logback의 `Appender`는 Spring Bean이 아니라서,  
스프링의 `@Autowired`를 바로 쓸 수 없음.

**해결 예시**: `ApplicationContextAware`를 이용해,  
런타임에 **MyBatis Mapper**를 얻어온다(예제 용).  
실무에서는 **별도 LoggerService**를 스프링 빈으로 만들고,  
**Static + Setter**로 Appender에서 참조하도록 구성하기도 함.

```java
package com.example.demo.log;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.BeansException;

import com.example.demo.mapper.SyiifLogMapper;
import com.example.demo.dto.SyiifLogDto;

public class SyiifDbAppender extends AppenderBase<ILoggingEvent> implements ApplicationContextAware {

    private static SyiifLogMapper syiifLogMapper;  // static 보관 (예시)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // MyBatis Mapper Bean 얻기
        syiifLogMapper = applicationContext.getBean(SyiifLogMapper.class);
    }

    @Override
    protected void append(ILoggingEvent event) {
        // Filter에서 syiif 키워드 체크 -> 여기 들어옴
        String message = event.getFormattedMessage();

        try {
            // 메시지가 syiif + JSON이라고 가정 -> 파싱
            JsonNode root = objectMapper.readTree(message);

            String interfaceId = getSafe(root, "interfaceId");
            String headerKey   = getSafe(root, "headerKey");
            String name        = getSafe(root, "name");
            String msgValue    = getSafe(root, "message");

            // 방어 로직: 4개 필드가 전부 없어도 DB에는 rawData만 넣고 싶을 수도 있음
            // 여기선 최소 한 개라도 있으면 Insert한다고 가정
            if (interfaceId.isEmpty() && headerKey.isEmpty() && name.isEmpty() && msgValue.isEmpty()) {
                addInfo("[SyiifDbAppender] All 4 fields missing, skip insert. msg=" + message);
                return;
            }

            // DTO 생성
            SyiifLogDto dto = new SyiifLogDto();
            dto.setInterfaceId(interfaceId);
            dto.setHeaderKey(headerKey);
            dto.setName(name);
            dto.setMessageText(msgValue);
            dto.setRawData(message);

            // Mapper Insert
            if (syiifLogMapper != null) {
                syiifLogMapper.insertLog(dto);
            } else {
                addWarn("[SyiifDbAppender] syiifLogMapper is null, cannot insert DB");
            }

        } catch (Exception e) {
            // JSON 파싱 실패 or 기타
            addError("[SyiifDbAppender] Failed to parse/insert syiif JSON", e);
        }
    }

    private String getSafe(JsonNode node, String key) {
        if (!node.has(key)) return "";
        return node.get(key).asText("");
    }
}
```

### 동작 흐름

1. **Spring Boot** 기동 시, **SyiifDbAppender**가 초기화됨
2. `setApplicationContext(...)`를 통해 **`syiifLogMapper`** Bean 참조를 획득
3. “syiif” 포함 로그가 들어오면 → `append()` 메서드에서 JSON 파싱 + MyBatis `insertLog(dto)` 호출
4. `TB_LOG_SYIIF` 테이블에 저장

---

# 6. 테스트 Controller

```java
package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test-syiif")
    public String testSyiifLog() {
        // syiif + JSON 데이터
        String jsonData = "{\"interfaceId\":\"IF-777\",\"headerKey\":\"HDR-XYZ\",\"name\":\"Alice\",\"message\":\"Syiif Test!\"}";
        log.info("syiif => " + jsonData);

        // syiif 있지만 JSON 아님
        log.info("syiif => Not a valid JSON structure ... hello world");

        // syiif 없음
        log.info("some normal log line... no syiif");

        return "Done. Check H2 DB TB_LOG_SYIIF table.";
    }
}
```

- `/test-syiif` 호출 시:
    1) `"syiif => {\"interfaceId\":...}"` → **키워드** 통과 → JSON 파싱 → Insert 성공
    2) `"syiif => Not a valid JSON"` → 파싱 실패 → `addError(...)`로 로거 경고
    3) `"some normal log"` → 필터 미통과 → DB에 안 들어감

---

# 7. H2 콘솔에서 확인

- `src/main/resources/application.yml`에
  ```yaml
  spring:
    h2:
      console:
        enabled: true
        path: /h2-console
  ```
- 브라우저에서 `http://localhost:8080/h2-console` 접속
    - JDBC URL: `jdbc:h2:mem:testdb`
    - User: `sa`, Password: (빈값)
- `TB_LOG_SYIIF` 테이블을 조회하면, `IF-777`, `HDR-XYZ`, `Alice`, `Syiif Test!`, 전체 rawData 등이 들어간 행이 확인됨

---

## 마무리 요약

1. **MyBatis XML Mapper**로 DB Insert 구현
    - Dto + Mapper 인터페이스 + Mapper XML + (schema.sql 테이블)
2. **H2 인메모리 DB**로 테스트
    - `application.yml` + `schema.sql`
3. **Logback Appender**에서 **스프링 빈**(Mapper)을 참조하는 예시
    - `ApplicationContextAware`로 Mapper 획득
    - 실제 운영 시, **수많은 로그**를 **동기로 DB Insert**하면 성능 문제가 생길 수 있음 → **비동기** 또는 **별도 Queue** 고려
4. **방어 로직**
    - 키 미존재 시 `""`(빈 문자열)
    - JSON 파싱 실패 시 에러 로깅 후 무시
    - 필요에 따라 “파싱 실패”만 따로 저장하거나, “4개 키 전부 없어도 rawData만 저장” 등 커스텀 가능

이렇게 구성하면, **syiif** 키워드가 포함된 로그 메시지를 **JSON 파싱** 후, **4가지 필드**를  
**MyBatis XML Mapper** 통해 **H2 DB**에 간단히 저장할 수 있어!  