package com.aichat.roleplay.service;

import com.aichat.roleplay.model.User;

import java.util.Optional;

/**
 * 用户服务接口
 * 遵循SOLID原则中的接口隔离原则
 * 定义用户相关的业务操作
 */
public interface IUserService {

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册后的用户信息
     */
    User registerUser(User user);

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUser(User user);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean validatePassword(String rawPassword, String encodedPassword);
}