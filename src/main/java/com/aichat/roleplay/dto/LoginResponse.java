package com.aichat.roleplay.dto;

/**
 * 登录响应DTO
 */
public class LoginResponse {

    private String token;
    private UserInfo user;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /**
     * 用户信息内部类（不包含密码等敏感信息）
     */
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String avatar;
        private Boolean active;

        // Constructors
        public UserInfo() {}

        public UserInfo(Long id, String username, String email, String avatar, Boolean active) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.avatar = avatar;
            this.active = active;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }
}