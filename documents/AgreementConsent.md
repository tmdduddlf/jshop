아래 예시는 **간결**하면서도 **필수/선택 약관 체크 및 저장** 기능, **저장된 데이터 조회·수정** 기능을 모두 구현한 최소 예시야.
- **테이블 설계**: `AGREEMENT_CONSENT` (시퀀스 기반 PK, `jsession`, `selected_items`, `created_at`, `updated_at`)
- **백엔드**: Spring Boot + MyBatis (DTO, Mapper + XML, Controller)
- **프론트엔드**: Vue 3 + TypeScript + Pinia

> **주의**: 실제 프로젝트에 맞춰 패키지명, 파일명, API 경로, 컬럼 타입 등을 적절히 수정해 사용하면 돼.

---

## 1. DB 테이블 생성 (Oracle / H2)

### 1-1) Oracle
```sql
CREATE SEQUENCE SEQ_AGREEMENT_CONSENT
  START WITH 1
  INCREMENT BY 1
  NOCACHE;

CREATE TABLE AGREEMENT_CONSENT (
  ID             NUMBER        NOT NULL,
  JSESSION       VARCHAR2(200),
  SELECTED_ITEMS VARCHAR2(2000),
  CREATED_AT     DATE          DEFAULT SYSDATE,
  UPDATED_AT     DATE,
  CONSTRAINT PK_AGREEMENT_CONSENT PRIMARY KEY (ID)
);
```

### 1-2) H2
```sql
CREATE SEQUENCE SEQ_AGREEMENT_CONSENT
  START WITH 1
  INCREMENT BY 1;

CREATE TABLE AGREEMENT_CONSENT (
  ID             BIGINT        NOT NULL,
  JSESSION       VARCHAR(200),
  SELECTED_ITEMS VARCHAR(2000),
  CREATED_AT     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT     TIMESTAMP,
  CONSTRAINT PK_AGREEMENT_CONSENT PRIMARY KEY (ID)
);
```

---

## 2. 백엔드

### 2-1) DTO (예: `AgreementConsentDto.java`)
```java
package com.example.dto;

import lombok.Data;

@Data
public class AgreementConsentDto {
    private Long id;
    private String jsession;
    // 선택된 항목들을 문자열(JSON 등)로 저장 (필수/선택 목록 구조 확장 가능)
    private String selectedItems;
    private String createdAt;
    private String updatedAt;
}
```

### 2-2) Mapper 인터페이스 (예: `AgreementConsentMapper.java`)
```java
package com.example.mapper;

import com.example.dto.AgreementConsentDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgreementConsentMapper {
    void insert(AgreementConsentDto dto);
    AgreementConsentDto select(Long id);
    List<AgreementConsentDto> selectAll();
    void update(AgreementConsentDto dto);
    void delete(Long id);
}
```

### 2-3) Mapper XML (예: `AgreementConsentMapper.xml`)
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
    PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.mapper.AgreementConsentMapper">

    <insert id="insert" parameterType="com.example.dto.AgreementConsentDto">
        INSERT INTO AGREEMENT_CONSENT 
        (ID, JSESSION, SELECTED_ITEMS, CREATED_AT, UPDATED_AT)
        VALUES 
        (SEQ_AGREEMENT_CONSENT.NEXTVAL, #{jsession}, #{selectedItems}, SYSDATE, NULL)
    </insert>

    <select id="select" parameterType="java.lang.Long" resultType="com.example.dto.AgreementConsentDto">
        SELECT
            ID AS id,
            JSESSION AS jsession,
            SELECTED_ITEMS AS selectedItems,
            TO_CHAR(CREATED_AT, 'YYYY-MM-DD HH24:MI:SS') AS createdAt,
            TO_CHAR(UPDATED_AT, 'YYYY-MM-DD HH24:MI:SS') AS updatedAt
        FROM AGREEMENT_CONSENT
        WHERE ID = #{id}
    </select>

    <select id="selectAll" resultType="com.example.dto.AgreementConsentDto">
        SELECT
            ID AS id,
            JSESSION AS jsession,
            SELECTED_ITEMS AS selectedItems,
            TO_CHAR(CREATED_AT, 'YYYY-MM-DD HH24:MI:SS') AS createdAt,
            TO_CHAR(UPDATED_AT, 'YYYY-MM-DD HH24:MI:SS') AS updatedAt
        FROM AGREEMENT_CONSENT
        ORDER BY ID DESC
    </select>

    <update id="update" parameterType="com.example.dto.AgreementConsentDto">
        UPDATE AGREEMENT_CONSENT
        SET 
            JSESSION = #{jsession},
            SELECTED_ITEMS = #{selectedItems},
            UPDATED_AT = SYSDATE
        WHERE ID = #{id}
    </update>

    <delete id="delete" parameterType="java.lang.Long">
        DELETE FROM AGREEMENT_CONSENT
        WHERE ID = #{id}
    </delete>

</mapper>
```

### 2-4) Service 계층 (AgreementConsentService.java)
```java

package com.example.service;

import com.example.dto.AgreementConsentDto;
import com.example.mapper.AgreementConsentMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AgreementConsentService {
    
    @Autowired
    private AgreementConsentMapper mapper;

    public void create(AgreementConsentDto dto) {
        mapper.insert(dto);
    }

    public AgreementConsentDto read(Long id) {
        return mapper.select(id);
    }

    public List<AgreementConsentDto> readAll() {
        return mapper.selectAll();
    }

    public void update(AgreementConsentDto dto) {
        mapper.update(dto);
    }

    public void delete(Long id) {
        mapper.delete(id);
    }
}

```

> **H2 사용 시**: `SYSDATE` 대신 `CURRENT_TIMESTAMP`를 사용하는 등 DB에 맞게 수정.

### 2-4) Controller (예: `AgreementConsentController.java`)
```java
package com.example.controller;

import com.example.dto.AgreementConsentDto;
import com.example.service.AgreementConsentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agreements")
public class AgreementConsentController {

   private final AgreementConsentService service;

   public AgreementConsentController(AgreementConsentService service) {
      this.service = service;
   }

   @PostMapping
   public void create(@RequestBody AgreementConsentDto dto) {
      service.create(dto);
   }

   @GetMapping("/{id}")
   public AgreementConsentDto read(@PathVariable Long id) {
      return service.read(id);
   }

   @GetMapping
   public List<AgreementConsentDto> readAll() {
      return service.readAll();
   }

   @PutMapping("/{id}")
   public void update(@PathVariable Long id, @RequestBody AgreementConsentDto dto) {
      dto.setId(id);
      service.update(dto);
   }

   @DeleteMapping("/{id}")
   public void delete(@PathVariable Long id) {
      service.delete(id);
   }
}
```

---

## 3. 프론트엔드 (Vue + TypeScript + Pinia)

### 3-1) Pinia 스토어 (예: `agreementStore.ts`)

```ts
import { defineStore } from 'pinia';
import axios from 'axios';

interface AgreementData {
  id?: number;
  jsession: string;
  selectedItems: string; // 실제론 JSON 문자열 등을 저장
  createdAt?: string;
  updatedAt?: string;
}

export const useAgreementStore = defineStore('agreement', {
  state: () => ({
    list: [] as AgreementData[],
    current: null as AgreementData | null,
  }),
  actions: {
    async fetchAll() {
      const res = await axios.get('/api/agreements');
      this.list = res.data;
    },
    async fetchOne(id: number) {
      const res = await axios.get(`/api/agreements/${id}`);
      this.current = res.data;
    },
    async createOne(data: AgreementData) {
      await axios.post('/api/agreements', data);
      await this.fetchAll();
    },
    async updateOne(id: number, data: AgreementData) {
      await axios.put(`/api/agreements/${id}`, data);
      await this.fetchAll();
    },
    async deleteOne(id: number) {
      await axios.delete(`/api/agreements/${id}`);
      await this.fetchAll();
    },
  },
});
```

### 3-2) 예시 컴포넌트 (약관 체크 + 저장) - `AgreementForm.vue`

```vue
<template>
  <div>
    <h2>약관 동의</h2>

    <!-- 전체 동의 -->
    <div>
      <input type="checkbox" :checked="allChecked" @change="toggleAll($event.target.checked)" />
      <label>전체 동의</label>
    </div>

    <hr />

    <!-- (필수) 약관 목록 -->
    <div v-for="(item, idx) in requiredAgreements" :key="'req'+idx">
      <input type="checkbox" :checked="item.checked" @change="toggleRequired(idx, $event.target.checked)" />
      <label>{{ item.label }} (필수)</label>
    </div>

    <hr />

    <!-- (선택) 약관 목록 -->
    <div v-for="(item, idx) in optionalAgreements" :key="'opt'+idx">
      <input type="checkbox" :checked="item.checked" @change="toggleOptional(idx, $event.target.checked)" />
      <label>{{ item.label }} (선택)</label>
    </div>

    <hr />

    <!-- JSESSION 값 입력 (간단히) -->
    <div>
      <label>JSESSION: </label>
      <input v-model="jsession" />
    </div>

    <!-- 확인(저장) 버튼 -->
    <button @click="onSave">확인</button>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import { useAgreementStore } from '@/stores/agreementStore';

export default defineComponent({
  setup() {
    const store = useAgreementStore();

    // (필수) 약관 목록
    const requiredAgreements = ref([
      { label: '계약진행을 위한 개인(신용)정보 동의', checked: false },
      { label: '휴대폰 인증 진행을 위한 개인(신용)정보처리 동의', checked: false },
    ]);

    // (선택) 약관 목록
    const optionalAgreements = ref([
      { label: '보험 및 금융상품 소개를 위한 개인신용정보 동의', checked: false },
    ]);

    // 전체 동의 여부
    const allChecked = computed(() => {
      const reqAll = requiredAgreements.value.every(item => item.checked);
      const optAll = optionalAgreements.value.every(item => item.checked);
      return reqAll && optAll;
    });

    // JSESSION 임시 입력
    const jsession = ref('');

    // 전체 동의 토글
    const toggleAll = (checked: boolean) => {
      requiredAgreements.value.forEach(item => item.checked = checked);
      optionalAgreements.value.forEach(item => item.checked = checked);
    };

    // 개별 토글
    const toggleRequired = (idx: number, checked: boolean) => {
      requiredAgreements.value[idx].checked = checked;
    };
    const toggleOptional = (idx: number, checked: boolean) => {
      optionalAgreements.value[idx].checked = checked;
    };

    // 저장(필수 항목 모두 체크되었는지 확인 -> 저장)
    const onSave = async () => {
      // 필수 약관 체크 검증
      const allRequiredChecked = requiredAgreements.value.every(item => item.checked);
      if (!allRequiredChecked) {
        alert('필수 약관을 모두 동의해주세요.');
        return;
      }
      // 선택된 항목 문자열(혹은 JSON)으로 구성
      const selected = {
        required: requiredAgreements.value.filter(i => i.checked).map(i => i.label),
        optional: optionalAgreements.value.filter(i => i.checked).map(i => i.label),
      };
      // Pinia store로 전송
      await store.createOne({
        jsession: jsession.value,
        selectedItems: JSON.stringify(selected),
      });
      alert('저장 완료!');
    };

    return {
      requiredAgreements,
      optionalAgreements,
      allChecked,
      jsession,
      toggleAll,
      toggleRequired,
      toggleOptional,
      onSave,
    };
  },
});
</script>

<style scoped>
/* 최소 스타일 예시 */
</style>
```

### 3-3) 조회/수정 컴포넌트 예시 - `AgreementList.vue`

```vue
<template>
  <div>
    <h2>저장된 약관 동의 목록</h2>
    <button @click="loadData">Reload</button>
    <ul>
      <li v-for="item in store.list" :key="item.id">
        [{{ item.id }}] {{ item.jsession }} / {{ item.selectedItems }}
        <button @click="edit(item.id)">Edit</button>
        <button @click="remove(item.id)">Delete</button>
      </li>
    </ul>

    <!-- 간단한 수정 폼 -->
    <div v-if="store.current">
      <h3>Edit ID: {{ store.current.id }}</h3>
      <input v-model="editJsession" placeholder="JSESSION" />
      <textarea v-model="editSelectedItems" placeholder="selectedItems"></textarea>
      <button @click="saveUpdate">Update</button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useAgreementStore } from '@/stores/agreementStore';

export default defineComponent({
  setup() {
    const store = useAgreementStore();
    const editJsession = ref('');
    const editSelectedItems = ref('');

    const loadData = async () => {
      await store.fetchAll();
    };

    const edit = async (id: number) => {
      await store.fetchOne(id);
      if (store.current) {
        editJsession.value = store.current.jsession;
        editSelectedItems.value = store.current.selectedItems;
      }
    };

    const saveUpdate = async () => {
      if (!store.current) return;
      await store.updateOne(store.current.id!, {
        jsession: editJsession.value,
        selectedItems: editSelectedItems.value,
      });
      store.current = null;
      editJsession.value = '';
      editSelectedItems.value = '';
      alert('수정 완료');
    };

    const remove = async (id: number) => {
      await store.deleteOne(id);
    };

    onMounted(() => {
      loadData();
    });

    return {
      store,
      editJsession,
      editSelectedItems,
      loadData,
      edit,
      saveUpdate,
      remove,
    };
  },
});
</script>
```

---

## 정리

1. **DB 테이블**
    - 시퀀스(`SEQ_AGREEMENT_CONSENT`)와 테이블(`AGREEMENT_CONSENT`)로 확장성 고려
    - `ID(PK)`, `JSESSION`, `SELECTED_ITEMS`, `CREATED_AT`, `UPDATED_AT` 컬럼

2. **백엔드**
    - **DTO**(`AgreementConsentDto`)로 데이터 구조 정의
    - **Mapper**(`AgreementConsentMapper` + XML)로 DB 접근
    - **Controller**에서 REST API로 CRUD 제공

3. **프론트엔드**
    - **Pinia**로 상태관리 (목록, 단건, CRUD 액션)
    - **약관 체크 컴포넌트**(`AgreementForm.vue`): 필수 항목 체크 검증 후 저장
    - **조회·수정 컴포넌트**(`AgreementList.vue`): 목록 표시, 단건 조회·수정·삭제

이렇게 구성하면 **필수·선택 약관 체크 후 저장**, **저장된 데이터 조회·수정**이 간단한 형태로 구현 가능해.  
실제 환경에 맞춰 URL, DB 타입, 에러 처리, UI/UX 등을 추가로 보강하면 돼!  