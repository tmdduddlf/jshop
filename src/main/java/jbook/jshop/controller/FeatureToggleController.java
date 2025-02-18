package jbook.jshop.controller;

import jbook.jshop.service.FeatureToggleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
