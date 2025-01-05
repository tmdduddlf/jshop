package jbook.jshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ApiResponse {
    private int code;
    private Object data;
    private String message;
}
