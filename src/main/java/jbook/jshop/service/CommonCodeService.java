package jbook.jshop.service;

import jbook.jshop.dto.CommonCodeDto;
import jbook.jshop.mapper.CommonCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommonCodeService {

    @Autowired
    private CommonCodeMapper mapper;

    public List<CommonCodeDto> getAll() {
        return mapper.findAll();
    }

    public CommonCodeDto getOne(String codeId) {
        return mapper.findById(codeId);
    }

    public void create(CommonCodeDto code) {
        mapper.insert(code);
    }

    public void update(CommonCodeDto code) {
        mapper.update(code);
    }

    public void delete(String codeId) {
        mapper.delete(codeId);
    }
}
