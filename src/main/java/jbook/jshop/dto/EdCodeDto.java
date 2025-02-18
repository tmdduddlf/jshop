package jbook.jshop.dto;
import lombok.Data;
import java.util.Date;

@Data
public class EdCodeDto {
    private Long id;           // 시퀀스 기반 PK
    private String code;       // 기능 식별자 (예: "250218_ASSRNC")
    private String parentCode; // 상위 코드 (예: "FTR_TGL")
    private String codeNm;     // 코드 이름
    private String toggleValue;// "on" 또는 "off"
    private Date validityDate; // 유효기간
}
