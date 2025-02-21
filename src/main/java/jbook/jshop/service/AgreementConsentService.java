package jbook.jshop.service;

import jbook.jshop.dto.AgreementConsentDto;
import jbook.jshop.mapper.AgreementConsentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgreementConsentService {

    @Autowired
    private AgreementConsentMapper mapper;

    public void create(AgreementConsentDto dto) {
        mapper.insert(dto);
    }

    public AgreementConsentDto read(Long id) {
        return mapper.select(id);
    }

    public List<AgreementConsentDto> readAll() {
        return mapper.selectAll();
    }

    public void update(AgreementConsentDto dto) {
        mapper.update(dto);
    }

    public void delete(Long id) {
        mapper.delete(id);
    }

}
