package com.aichat.roleplay.context;

import com.aichat.roleplay.model.User;

/**
 * ThreadLocal用户上下文
 * 用于在当前请求线程中存储用户信息
 */
public class UserContext {
    
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();
    
    /**
     * 设置当前用户
     */
    public static void setCurrentUser(User user) {
        userHolder.set(user);
    }
    
    /**
     * 获取当前用户
     */
    public static User getCurrentUser() {
        return userHolder.get();
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        User user = userHolder.get();
        return user != null ? user.getId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        User user = userHolder.get();
        return user != null ? user.getUsername() : null;
    }
    
    /**
     * 清除当前用户信息
     */
    public static void clear() {
        userHolder.remove();
    }
}