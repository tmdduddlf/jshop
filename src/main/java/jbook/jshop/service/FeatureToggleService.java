package jbook.jshop.service;

import jbook.jshop.dto.EdCodeDto;
import jbook.jshop.mapper.EdCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class FeatureToggleService {

    @Autowired
    private EdCodeMapper mapper;

    /**
     * 기능 토글 상태 확인
     * - 유효기간이 지난 경우 무조건 on 처리
     */
    public boolean isEnabled(String code) {
        EdCodeDto ed = mapper.findByCode(code);
        if (ed == null) {
            return false;
        }
        if (ed.getValidityDate() != null && new Date().after(ed.getValidityDate())) {
            return true;
        }
        return "on".equalsIgnoreCase(ed.getToggleValue());
    }
}
