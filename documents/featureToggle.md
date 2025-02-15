아래는 **Pinia**를 사용하면서도 코드를 최소화한 **Feature Toggle** 예시입니다.  
백엔드(Spring Boot + MyBatis)는 이전과 비슷하되, **`@Autowired`** 방식을 유지하였고,  
프론트엔드(Vue 3 + Pinia + Composition API)는 **state/actions 객체 없이** 함수형 작성을 시도했습니다.

---

## 1. 계획 (3단계)

1) **설정**
    - 데이터베이스 `COMMON_CODE` 테이블에 `FEATURE_X` ON/OFF 여부 저장
    - 백엔드에서 `/api/feature`로 현재 토글 상태를 반환
    - 프론트엔드에서 Pinia로 토글 상태를 관리

2) **구현**
    - **백엔드**
        - `CommonCodeMapper` / `FeatureToggleService` / `FeatureToggleController`
        - `@Autowired`로 최소한의 의존성 주입
    - **프론트엔드**
        - Pinia 스토어 정의: `featureXEnabled` ref + `loadFeatureXStatus()`
        - 컴포넌트에서 store를 불러와 onMounted 시점에 토글 상태 로드

3) **테스트**
    - 공통코드에서 `FEATURE_X`를 ON/OFF로 바꿔가며 실제 화면이 즉시 전환되는지 확인
    - 장애 상황에서 OFF로 긴급 전환 후 문제 없는지 검증

---

## 2. 백엔드 코드 (간결 버전)

### (1) Mapper

```java
// src/main/java/com/example/mapper/CommonCodeMapper.java
package com.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommonCodeMapper {
    @Select("SELECT CODE_VALUE FROM COMMON_CODE WHERE CODE_ID = #{id}")
    String findCodeValue(@Param("id") String id);
}
```

### (2) Service

```java
// src/main/java/com/example/service/FeatureToggleService.java
package com.example.service;

import com.example.mapper.CommonCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeatureToggleService {

    @Autowired
    private CommonCodeMapper mapper;

    public boolean isFeatureXEnabled() {
        String val = mapper.findCodeValue("FEATURE_X");
        return "ON".equalsIgnoreCase(val);
    }
}
```

### (3) Controller

```java
// src/main/java/com/example/controller/FeatureToggleController.java
package com.example.controller;

import com.example.service.FeatureToggleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeatureToggleController {

    @Autowired
    private FeatureToggleService service;

    @GetMapping("/api/feature")
    public boolean getFeatureX() {
        return service.isFeatureXEnabled();
    }
}
```

> - **간결성**을 위해 모든 로직은 기본적인 조회만 수행
> - 필요 시 관리자 UI 등에서 `COMMON_CODE` 테이블을 수정할 수 있도록 확장 가능

---

## 3. 프론트엔드 코드 (Vue 3 + Pinia + Composition API)

### (1) Pinia Store (함수형)

```ts
// src/stores/featureStore.ts
import { defineStore } from 'pinia';
import axios from 'axios';
import { ref } from 'vue';

export const useFeatureStore = defineStore('featureStore', () => {
  const featureXEnabled = ref(false);

  async function loadFeatureXStatus() {
    try {
      const res = await axios.get<boolean>('/api/feature');
      featureXEnabled.value = res.data;
    } catch (err) {
      console.error('[FeatureStore] loadFeatureXStatus error:', err);
    }
  }

  return { featureXEnabled, loadFeatureXStatus };
});
```

> - Pinia **Composition Store** 방식으로 최소한의 로직만 정의
> - `featureXEnabled`를 ref로 관리, `loadFeatureXStatus()`로 서버의 `/api/feature`를 호출

### (2) 기능 토글 분기 컴포넌트

```vue
<!-- src/pages/FeatureXPage.vue -->
<template>
  <div>
    <h2>Feature X Demo</h2>
    <div v-if="store.featureXEnabled">
      <NewFeatureComponent />
    </div>
    <div v-else>
      <OldFeatureComponent />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useFeatureStore } from '@/stores/featureStore';
import NewFeatureComponent from '@/components/NewFeatureComponent.vue';
import OldFeatureComponent from '@/components/OldFeatureComponent.vue';

const store = useFeatureStore();

onMounted(() => {
  store.loadFeatureXStatus();
});
</script>
```

> - `onMounted` 시점에 `store.loadFeatureXStatus()` 호출 → 서버에서 현재 값(ON/OFF) fetch
> - `store.featureXEnabled`가 `true`면 `<NewFeatureComponent>`, `false`면 `<OldFeatureComponent>`

---

## 4. 요약 & 활용

- **배포 후**: 새 기능은 OFF 상태로 두었다가, 안정성 확인 후 ON으로 전환
- **장애 발생 시**: 즉시 OFF → 이전 기능 유지
- **확장**: 다른 기능 토글도 동일한 패턴(`FEATURE_Y`, `FEATURE_Z` 등)으로 추가

이처럼 **Pinia 스토어**를 사용하되 **간결한 구성**(ref + 하나의 로더 함수)으로 Feature Toggle을 적용할 수 있습니다.