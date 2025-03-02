// src/main/java/com/example/domain/DrinkStatus.java
package jbook.jshop.dto;

import lombok.Data;

@Data
public class DrinkStatus {
    private Long id;           // 시퀀스 기반 PK
    // [Modified: yyyyMMdd HH:mm 형태로 관리]
    private String statusDate; // 기록 날짜
    private String drank;      // 'Y' 또는 'N'
    private String notes;      // 메모 (옵션)
    private String regDate;    // 등록일
    private String updDate;    // 수정일
}
