package com.uphill.entrypoint.rest.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private T data;
    private String errorCode;
    private String errorMessage;
    
    public static <T> ApiResponse<T> success(final T data) {
        final ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    public static <T> ApiResponse<T> failure(final String errorMessage) {
        final ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
    
    public static <T> ApiResponse<T> failure(final String errorCode, final String errorMessage) {
        final ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        return response;
    }
}

