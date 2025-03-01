## TabsConstiner 의 핵심

1) 스토어
```store
import axios from 'axios'
const store = useTabsContainerStore();
const { toggleList } = storeToRefs(store)
※ 함수는 storeToRefs 불가!
```

2) onMounted
 - `dataList.value = cloneDeep(toggleList.value)` pinia 기준으로 초기 데이터 세팅 
```onMounted 비동기 선언
onMounted(async () => {
  await store.findAll()
  dataList.value = cloneDeep(toggleList.value)
})
```

3) axios 결과를 데이터에 세팅시 list 조심
 - 아래 소스에서 `dataList.value = [data];` 이게 핵심! 
```랜더링 대상 data 가 배열인데, 단건일 경우 그냥 .value로 json결과를 세팅할 때가 있음. 
async function findByCode(code:string) {
  const { data } = await axios.get<[any]>('/api/toggle/findByCode/' + code);
  console.log("■ [TabsContainer.vue findByCode] data : " + JSON.stringify(data))
  dataList.value = [data];
}
```

4) 마운티드 되고 나서 랜더링 시키기
 - await nextTick();
```
// 템플릿 전체가 렌더링된 후 실행
await nextTick();
// 여기서 activeTab 컴포넌트도 모두 렌더링됨
console.log('템플릿과 동적 컴포넌트 모두 렌더링 완료!');
```

5) 부모에서 데이터 완전히 세팅된 후 자식컴포넌트 랜더링하기
 - v-if 사용해서 데이터 세팅된 후 컴포넌트 발동시키기
```
<!-- 탭 컴포넌트 -->
<div class="tab-content" v-if="dataList && dataList.length">
  <component :is="activeTab" />
</div>
```
---
