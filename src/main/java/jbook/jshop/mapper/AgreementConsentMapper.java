package jbook.jshop.mapper;

import jbook.jshop.dto.AgreementConsentDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgreementConsentMapper {
    void insert(AgreementConsentDto dto);
    AgreementConsentDto select(Long id);
    List<AgreementConsentDto> selectAll();
    void update(AgreementConsentDto dto);
    void delete(Long id);
}
