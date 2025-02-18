## FEATURE TOGGLE

### 테이블 : ED_CODE

1) 대분류
   - code : FTR_TGL
   - code_nm : 기능 전환

2) 중분류
   - code : 250218_ASSRNC
   - code_nm : 보장분석 적용여부
   - 유효기간 : 20250220
   - value : on 

   - code : 250218_CALC
   - code_nm : 보험료계산 적용여부
   - 유효기간 : 20250220
   - value : on

요구사항
 - 기능 on/off 는 코드 테이블에 넣어서 관리할거야.
 - 예를들어 ① 보장분석, ② 보험료 계산 을 개발한다고 가정하자.
 - 그럴경우 관리자에서 메뉴에서 코드 테이블에 기능별 on/off 값을 입력할거야.
 - 유효기간이 넘어갈 경우 무조건 기능적용은 on 상태로 간주하자.
 - DB 설계해줘.
 - 각기능을 구현한 frontend 소스에 토글 적용해줘.
 - 각기능을 구현한 backend 소스에 토글 적용해줘.


[reply]

아래는 전체 구성에 대한 미니멀하고 직관적인 예시입니다.  
각 구성은 **오라클 쿼리**, **H2 쿼리**, **백엔드(Spring Boot + MyBatis)**, **프론트엔드(Vue 3 + Pinia + TypeScript)** 소스로 정리되어 있습니다.

---

## 1. DB 쿼리

### 1-1. Oracle 쿼리

```sql
-- Oracle: 시퀀스 생성
CREATE SEQUENCE ED_CODE_SEQ
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;

-- Oracle: ED_CODE 테이블 생성 (TOGGLE_VALUE 사용)
CREATE TABLE ED_CODE (
  ID             NUMBER         PRIMARY KEY,
  CODE           VARCHAR2(50)   NOT NULL,       -- 예: '250218_ASSRNC'
  PARENT_CODE    VARCHAR2(50)   NOT NULL,       -- 예: 'FTR_TGL'
  CODE_NM        VARCHAR2(100),                  -- 예: '보장분석 적용여부'
  TOGGLE_VALUE   VARCHAR2(10)   DEFAULT 'OFF',  -- 'on' 또는 'off'
  VALIDITY_DATE  DATE                           -- 유효기간 (지난 경우 무조건 on)
);

-- Oracle: 예시 데이터 삽입
INSERT INTO ED_CODE (ID, CODE, PARENT_CODE, CODE_NM, TOGGLE_VALUE, VALIDITY_DATE)
VALUES (ED_CODE_SEQ.NEXTVAL, 'FTR_TGL', 'ROOT', '기능 전환', 'ON', NULL);

INSERT INTO ED_CODE (ID, CODE, PARENT_CODE, CODE_NM, TOGGLE_VALUE, VALIDITY_DATE)
VALUES (ED_CODE_SEQ.NEXTVAL, '250218_ASSRNC', 'FTR_TGL', '보장분석 적용여부', 'ON', TO_DATE('2025-02-20','YYYY-MM-DD'));

INSERT INTO ED_CODE (ID, CODE, PARENT_CODE, CODE_NM, TOGGLE_VALUE, VALIDITY_DATE)
VALUES (ED_CODE_SEQ.NEXTVAL, '250218_CALC', 'FTR_TGL', '보험료계산 적용여부', 'ON', TO_DATE('2025-02-20','YYYY-MM-DD'));
```

### 1-2. H2 쿼리

```sql
-- H2: 시퀀스 생성
CREATE SEQUENCE ED_CODE_SEQ
  START WITH 1
  INCREMENT BY 1;

-- H2: ED_CODE 테이블 생성 (TOGGLE_VALUE 사용)
CREATE TABLE ED_CODE (
  ID            BIGINT       PRIMARY KEY,
  CODE          VARCHAR(50)  NOT NULL,
  PARENT_CODE   VARCHAR(50)  NOT NULL,
  CODE_NM       VARCHAR(100),
  TOGGLE_VALUE  VARCHAR(10)  DEFAULT 'OFF',
  VALIDITY_DATE DATE
);

-- H2: 예시 데이터 삽입
INSERT INTO ED_CODE (ID, CODE, PARENT_CODE, CODE_NM, TOGGLE_VALUE, VALIDITY_DATE)
VALUES (NEXT VALUE FOR ED_CODE_SEQ, 'FTR_TGL', 'ROOT', '기능 전환', 'ON', NULL);

INSERT INTO ED_CODE (ID, CODE, PARENT_CODE, CODE_NM, TOGGLE_VALUE, VALIDITY_DATE)
VALUES (NEXT VALUE FOR ED_CODE_SEQ, '250218_ASSRNC', 'FTR_TGL', '보장분석 적용여부', 'ON', '2025-02-20');

INSERT INTO ED_CODE (ID, CODE, PARENT_CODE, CODE_NM, TOGGLE_VALUE, VALIDITY_DATE)
VALUES (NEXT VALUE FOR ED_CODE_SEQ, '250218_CALC', 'FTR_TGL', '보험료계산 적용여부', 'ON', '2025-02-20');
```

---

## 2. 백엔드 전체 소스 (Spring Boot + MyBatis)

### 2-1. VO (EdCode.java) – Lombok 사용

```java
// src/main/java/com/example/domain/EdCode.java
package com.example.domain;

import lombok.Data;
import java.util.Date;

@Data
public class EdCode {
    private Long id;           // 시퀀스 기반 PK
    private String code;       // 기능 식별자 (예: "250218_ASSRNC")
    private String parentCode; // 상위 코드 (예: "FTR_TGL")
    private String codeNm;     // 코드 이름
    private String toggleValue;// "on" 또는 "off"
    private Date validityDate; // 유효기간
}
```

### 2-2. Mapper 인터페이스 (EdCodeMapper.java)

```java
// src/main/java/com/example/mapper/EdCodeMapper.java
package com.example.mapper;

import com.example.domain.EdCode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EdCodeMapper {
    // 기능 식별자(code)를 기준으로 조회
    EdCode findByCode(String code);
}
```

### 2-3. XML Mapper (EdCodeMapper.xml)

```xml
<!-- src/main/resources/mapper/EdCodeMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.mapper.EdCodeMapper">

    <select id="findByCode" parameterType="string" resultType="com.example.domain.EdCode">
        SELECT 
            ID,
            CODE,
            PARENT_CODE AS parentCode,
            CODE_NM     AS codeNm,
            TOGGLE_VALUE AS toggleValue,
            VALIDITY_DATE AS validityDate
        FROM ED_CODE
        WHERE CODE = #{code}
    </select>

</mapper>
```

### 2-4. Service (FeatureToggleService.java)

```java
// src/main/java/com/example/service/FeatureToggleService.java
package com.example.service;

import com.example.domain.EdCode;
import com.example.mapper.EdCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class FeatureToggleService {

    @Autowired
    private EdCodeMapper mapper;

    /**
     * 기능 토글 상태 확인
     * - 유효기간이 지난 경우 무조건 on 처리
     */
    public boolean isEnabled(String code) {
        EdCode ed = mapper.findByCode(code);
        if (ed == null) {
            return false;
        }
        if (ed.getValidityDate() != null && new Date().after(ed.getValidityDate())) {
            return true;
        }
        return "on".equalsIgnoreCase(ed.getToggleValue());
    }
}
```

### 2-5. Controller (FeatureToggleController.java)

```java
// src/main/java/com/example/controller/FeatureToggleController.java
package com.example.controller;

import com.example.service.FeatureToggleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/toggle")
public class FeatureToggleController {

    @Autowired
    private FeatureToggleService service;

    // 예: GET /api/toggle/250218_ASSRNC
    @GetMapping("/{code}")
    public boolean isFeatureOn(@PathVariable String code) {
        return service.isEnabled(code);
    }
}
```

---

## 3. 프론트엔드 전체 소스 (Vue 3 + Pinia + TypeScript)

### 3-1. Pinia Store (featureToggleStore.ts)

```ts
// src/stores/featureToggleStore.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';

export const useFeatureToggleStore = defineStore('featureToggle', () => {
  // key: 기능 코드, value: 토글 상태
  const toggles = ref<Record<string, boolean>>({});

  // 서버에서 토글 상태를 조회 후 캐싱
  async function checkToggle(code: string): Promise<boolean> {
    try {
      const res = await axios.get<boolean>(`/api/toggle/${code}`);
      toggles.value[code] = res.data;
      return res.data;
    } catch (err) {
      console.error('checkToggle error:', err);
      toggles.value[code] = false;
      return false;
    }
  }

  // 캐싱된 값 조회
  function isEnabled(code: string): boolean {
    return toggles.value[code] === true;
  }

  return { toggles, checkToggle, isEnabled };
});
```

### 3-2. 보장분석 페이지 (AssurancePage.vue)

```vue
<!-- src/pages/AssurancePage.vue -->
<template>
  <div>
    <h2>보장분석</h2>
    <div v-if="assrncOn">
      신규 보장분석 기능 적용됨!
    </div>
    <div v-else>
      이전 보장분석 로직 실행 중...
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useFeatureToggleStore } from '@/stores/featureToggleStore';

const store = useFeatureToggleStore();
const assrncOn = ref(false);

onMounted(async () => {
  assrncOn.value = await store.checkToggle('250218_ASSRNC');
});
</script>
```

### 3-3. 보험료 계산 페이지 (CalcPage.vue)

```vue
<!-- src/pages/CalcPage.vue -->
<template>
  <div>
    <h2>보험료 계산</h2>
    <div v-if="calcOn">
      최신 보험료 계산 기능 사용 중!
    </div>
    <div v-else>
      이전 보험료 계산 로직 적용됨...
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useFeatureToggleStore } from '@/stores/featureToggleStore';

const store = useFeatureToggleStore();
const calcOn = ref(false);

onMounted(async () => {
  calcOn.value = await store.checkToggle('250218_CALC');
});
</script>
```

---

## 전체 요약

1. **DB 설계**
    - **Oracle** 및 **H2** 모두 ED_CODE 테이블은 시퀀스 기반 PK(`ID`)와 기능 식별을 위한 `CODE`, `PARENT_CODE`, `CODE_NM`, **TOGGLE_VALUE**, `VALIDITY_DATE` 컬럼을 사용합니다.
    - 유효기간이 지난 경우 기능은 무조건 **on**으로 간주합니다.

2. **백엔드 (Spring Boot + MyBatis)**
    - **VO**: `EdCode.java` – Lombok 사용하여 간결하게 작성
    - **Mapper**: `EdCodeMapper.java` 인터페이스와 XML 파일(`EdCodeMapper.xml`)로 최소한의 조회 쿼리 구성
    - **Service**: `FeatureToggleService.java`에서 유효기간 체크 후 토글 상태 결정
    - **Controller**: `FeatureToggleController.java`에서 `/api/toggle/{code}` 엔드포인트로 on/off 상태 반환

3. **프론트엔드 (Vue 3 + Pinia + TypeScript)**
    - **Pinia Store**: `featureToggleStore.ts`에서 단순 캐싱 및 API 호출 함수 제공
    - **페이지 컴포넌트**: `AssurancePage.vue`와 `CalcPage.vue`는 `ref(false)`로 초기화 후, onMounted 시점에 `store.checkToggle(...)` 호출하여 상태 업데이트 후 분기 처리

이 구성은 누구나 쉽게 이해할 수 있도록 가시적이고 미니멀하게 작성되었으며, 향후 확장 시에도 일관된 방식으로 기능 토글을 관리할 수 있습니다.