### autoc

1. 설정경로
   - File | Settings | Editor | Live Templates


```
@PostMapping
public void createCode(@RequestBody CommonCodeDto code) {
service.create(code);
}
```

### autoc

```
public String liveness() {
    String result = "success";
    try {
        return result;
    } catch (Exception e) {
        throw new RuntimeException("[in service] ", e);
    }
}
```