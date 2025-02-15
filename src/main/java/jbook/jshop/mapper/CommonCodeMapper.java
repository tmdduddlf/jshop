// src/main/java/com/example/mapper/CommonCodeMapper.java
package jbook.jshop.mapper;

import jbook.jshop.dto.CommonCodeDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CommonCodeMapper {
    List<CommonCodeDto> findAll();
    CommonCodeDto findById(String codeId);
    void insert(CommonCodeDto code);
    void update(CommonCodeDto code);
    void delete(String codeId);
}
