// src/main/java/com/example/controller/CommonCodeController.java
package jbook.jshop.controller;

import jbook.jshop.service.CommonCodeService;
import jbook.jshop.dto.CommonCodeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/common-codes")
public class CommonCodeController {

    @Autowired
    private CommonCodeService service;

    // 전체 조회
    @GetMapping
    public List<CommonCodeDto> getAllCodes() {
        return service.getAll();
    }

    // 단건 조회
    @GetMapping("/{codeId}")
    public CommonCodeDto getOneCode(@PathVariable String codeId) {
        return service.getOne(codeId);
    }

    // 생성
    @PostMapping
    public void createCode(@RequestBody CommonCodeDto code) {
        service.create(code);
    }

    // 수정
    @PutMapping("/{codeId}")
    public void updateCode(@PathVariable String codeId, @RequestBody CommonCodeDto code) {
        code.setCodeId(codeId);
        service.update(code);
    }

    // 삭제
    @DeleteMapping("/{codeId}")
    public void deleteCode(@PathVariable String codeId) {
        service.delete(codeId);
    }
}
