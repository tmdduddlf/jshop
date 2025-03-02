// src/main/java/com/example/controller/DrinkStatusController.java
package jbook.jshop.controller;

import jbook.jshop.dto.DrinkStatus;
import jbook.jshop.service.DrinkStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/drink-status")
public class DrinkStatusController {

    @Autowired
    private DrinkStatusService service;

    // 기간별 기록 조회: GET /api/drink-status?startDate=yyyyMMdd hh:mm&endDate=yyyyMMdd hh:mm
    @GetMapping
    public List<DrinkStatus> getStatuses(@RequestParam("startDate") String startDate,
                                         @RequestParam("endDate") String endDate) {
        return service.getStatuses(startDate, endDate);
    }

    // 단건 상세 조회: GET /api/drink-status/{id}
    @GetMapping("/{id}")
    public DrinkStatus getStatus(@PathVariable Long id) {
        return service.getStatus(id);
    }

    // 기록 추가
    @PostMapping
    public void createStatus(@RequestBody DrinkStatus status) {
        service.addStatus(status);
    }

    // 기록 수정
    @PutMapping("/{id}")
    public void updateStatus(@PathVariable Long id, @RequestBody DrinkStatus status) {
        status.setId(id);
        service.updateStatus(status);
    }

    // 기록 삭제
    @DeleteMapping("/{id}")
    public void deleteStatus(@PathVariable Long id) {
        service.deleteStatus(id);
    }
}
