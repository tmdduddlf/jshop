package jbook.jshop.mapper;
import jbook.jshop.dto.EdCodeDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EdCodeMapper {
    List<EdCodeDto> findAll();
    EdCodeDto findByCode(String code);
}
