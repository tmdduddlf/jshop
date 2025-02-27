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
---
