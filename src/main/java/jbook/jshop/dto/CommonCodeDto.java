// src/main/java/com/example/domain/CommonCode.java
package jbook.jshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeDto {
    private String codeId;
    private String codeValue;
    private String description;
}
