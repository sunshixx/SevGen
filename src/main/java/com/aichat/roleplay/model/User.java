package com.aichat.roleplay.model;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 遵循SOLID原则中的单一职责原则
 * 使用MyBatis-Plus注解进行ORM映射
 */
@TableName("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增长
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名，唯一
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 邮箱，唯一
     */
    @TableField("email")
    private String email;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 账户是否激活
     */
    @TableField("active")
    private Boolean active;

    /**
     * 创建时间，自动填充
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // Constructors
    public User() {}

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.active = true;
        this.deleted = 0;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    // Builder pattern helper methods
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String password;
        private String email;
        private String avatar;
        private Boolean active = true;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private Integer deleted = 0;

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public UserBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public UserBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public UserBuilder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public UserBuilder deleted(Integer deleted) {
            this.deleted = deleted;
            return this;
        }

        public User build() {
            User user = new User();
            user.setId(this.id);
            user.setUsername(this.username);
            user.setPassword(this.password);
            user.setEmail(this.email);
            user.setAvatar(this.avatar);
            user.setActive(this.active);
            user.setCreateTime(this.createTime);
            user.setUpdateTime(this.updateTime);
            user.setDeleted(this.deleted);
            return user;
        }
    }
}