package com.aichat.roleplay.service;

import com.aichat.roleplay.model.User;
import org.springframework.web.multipart.MultipartFile;

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
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     */
    String updateUserAvatar(Long userId, MultipartFile file);

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean validatePassword(String rawPassword, String encodedPassword);

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @return 操作是否成功
     */
    boolean sendVerificationCode(String email);

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱地址
     * @param code  验证码
     * @return 验证是否通过
     */
    boolean verifyCode(String email, String code);

    /**
     * 用户登录认证
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息（如果认证成功）
     */
    Optional<User> authenticateUser(String username, String password);
}