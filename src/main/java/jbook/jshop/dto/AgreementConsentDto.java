package jbook.jshop.dto;

import lombok.Data;

@Data
public class AgreementConsentDto {
    private Long id;
    private String jsession;
    // 선택된 항목들을 문자열(JSON 등)로 저장 (필수/선택 목록 구조 확장 가능)
    private String selectedItems;
    private String createdAt;
    private String updatedAt;
}
