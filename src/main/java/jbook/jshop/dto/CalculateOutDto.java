package jbook.jshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true) // toBuilder 추가
public class CalculateOutDto {

    private CalculateInDto inputObj;
    private ArrayList resultArray;

}
