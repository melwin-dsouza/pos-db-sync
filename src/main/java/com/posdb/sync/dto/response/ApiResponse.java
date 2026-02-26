package com.posdb.sync.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ApiResponse<T> {
    private int status;
    private T data;
    private String errorMessage;

    public ApiResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    public ApiResponse(int status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }


}
