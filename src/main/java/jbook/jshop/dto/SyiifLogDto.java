package jbook.jshop.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyiifLogDto {
    private Long id;            // 시퀀스 or auto-increment
    private String interfaceId;
    private String headerKey;
    private String name;
    private String message;
    private String rawData;
    private String createdAt;   // Oracle DATE → String(또는 LocalDateTime)
}
