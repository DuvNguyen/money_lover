package com.example.money_lover.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Quan trọng: Nếu field nào null thì bỏ qua, không trả về json làm rối
public class ApiResponse<T> {
    @Builder.Default
    private int code = 1000; // Quy ước: 1000 là thành công. Khác 1000 là lỗi.

    private String message;
    private T result;
}