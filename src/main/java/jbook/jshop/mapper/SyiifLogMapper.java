package jbook.jshop.mapper;

import jbook.jshop.dto.SyiifLogDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SyiifLogMapper {
    int insertIfLog(SyiifLogDto dto);
    List<SyiifLogDto> selectIfLogs();
}