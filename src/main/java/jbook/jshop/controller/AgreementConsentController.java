package jbook.jshop.controller;

import jbook.jshop.dto.AgreementConsentDto;
import jbook.jshop.service.AgreementConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agreements")
public class AgreementConsentController {

    @Autowired
    private AgreementConsentService service;

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
