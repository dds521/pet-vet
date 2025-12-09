package com.petvet.rag.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应格式
 * 
 * @param <T> 响应数据类型
 * @author PetVetRAG Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 响应码（200表示成功）
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
            .code(200)
            .message("操作成功")
            .build();
    }
    
    /**
     * 成功响应（有数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code(200)
            .message("操作成功")
            .data(data)
            .build();
    }
    
    /**
     * 成功响应（有数据和消息）
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .code(200)
            .message(message)
            .data(data)
            .build();
    }
    
    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder()
            .code(500)
            .message(message)
            .build();
    }
    
    /**
     * 失败响应（带错误码）
     */
    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .build();
    }
}
