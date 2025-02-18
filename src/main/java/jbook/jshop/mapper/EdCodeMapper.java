package jbook.jshop.mapper;
import jbook.jshop.dto.EdCodeDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EdCodeMapper {
    EdCodeDto findByCode(String code);
}
