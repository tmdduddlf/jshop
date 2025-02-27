package jbook.jshop.controller;

import jbook.jshop.dto.EdCodeDto;
import jbook.jshop.service.FeatureToggleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/toggle")
public class FeatureToggleController {

    @Autowired
    private FeatureToggleService service;

    @GetMapping("/findAll")
    public List<EdCodeDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/findByCode/{code}")
    public EdCodeDto findByCode(@PathVariable String code) {
        return service.findByCode(code);
    }

    @GetMapping("/{code}")
    public boolean isFeatureOn(@PathVariable String code) {
        return service.isEnabled(code);
    }
}
