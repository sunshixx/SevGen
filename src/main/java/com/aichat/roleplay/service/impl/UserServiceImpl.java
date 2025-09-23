package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.mapper.UserMapper;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户服务实现类
 * 遵循SOLID原则中的单一职责原则和依赖倒置原则
 * 使用策略模式处理不同的用户操作
 */
@Service
@Transactional
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;

    /**
     * 构造函数注入，遵循依赖倒置原则
     *
     * @param userMapper 用户数据访问接口
     */
    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User registerUser(User user) {
        log.info("开始注册用户，用户名: {}", user.getUsername());

        // 验证用户数据
        validateUserRegistration(user);

        // 设置默认值
        user.setActive(true);
        user.setDeleted(0);

        // 保存用户
        int result = userMapper.insert(user);
        if (result > 0) {
            log.info("用户注册成功，用户ID: {}", user.getId());
            return user;
        } else {
            throw new RuntimeException("用户注册失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("根据用户名查找用户: {}", username);
        return userMapper.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("根据邮箱查找用户: {}", email);
        return userMapper.findByEmail(email);
    }

    @Override
    public User updateUser(User user) {
        log.info("更新用户信息，用户ID: {}", user.getId());

        // 检查用户是否存在
        if (userMapper.selectById(user.getId()) == null) {
            throw new RuntimeException("用户不存在");
        }

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("用户信息更新成功，用户ID: {}", user.getId());
            return user;
        } else {
            throw new RuntimeException("用户信息更新失败");
        }
    }

    @Override
    public void deleteUser(Long id) {
        log.info("删除用户，用户ID: {}", id);

        // 使用逻辑删除
        int result = userMapper.deleteById(id);
        if (result > 0) {
            log.info("用户删除成功，用户ID: {}", id);
        } else {
            throw new RuntimeException("用户删除失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    /**
     * 验证用户注册数据
     * 遵循模板方法模式
     *
     * @param user 用户信息
     */
    private void validateUserRegistration(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 可以添加更多验证逻辑
        validateUserData(user);
    }

    /**
     * 验证用户基础数据
     *
     * @param user 用户信息
     */
    private void validateUserData(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        // 这里可以使用更复杂的密码验证逻辑
        // 暂时使用简单的字符串比较，实际应用中应该使用加密后的密码比较
        return rawPassword != null && rawPassword.equals(encodedPassword);
    }
}