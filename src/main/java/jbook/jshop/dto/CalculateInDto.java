package jbook.jshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true) // toBuilder 추가
public class CalculateInDto {
    private String productType;
    private String premium;
}
