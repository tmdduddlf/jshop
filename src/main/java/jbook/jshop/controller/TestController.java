package jbook.jshop.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test-syiif")
    public String testSyiifLog() {
        // syiif + JSON 데이터
        String jsonData = "{\"interfaceId\":\"IF-777\",\"headerKey\":\"HDR-XYZ\",\"name\":\"Alice\",\"message\":\"Syiif Test!\"}";
        log.info(jsonData);
        log.info("syiif => " + jsonData);

        // syiif 있지만 JSON 아님
        log.info("syiif => Not a valid JSON structure ... hello world");

        // syiif 없음
        log.info("some normal log line... no syiif");

        return "Done. Check H2 DB TB_LOG_SYIIF table.";
    }
}
