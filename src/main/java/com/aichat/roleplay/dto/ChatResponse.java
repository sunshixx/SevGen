package com.aichat.roleplay.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 聊天响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户查询文本
     */
    private String query;
    
    /**
     * 角色响应列表
     */
    private List<RoleResponse> responses;
    
    /**
     * 最终响应
     */
    private String finalResponse;
    

    
    /**
     * 角色响应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleResponse {
        /**
         * 角色名称
         */
        private String roleName;
        
        /**
         * 响应内容
         */
        private String response;
        
        /**
         * 执行顺序
         */
        private int executionOrder;
    }
}