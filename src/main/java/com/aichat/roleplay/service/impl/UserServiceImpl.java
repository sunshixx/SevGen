package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.mapper.UserMapper;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IEmailService;
import com.aichat.roleplay.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    
    // 验证码有效期（分钟）
    private static final int CODE_EXPIRY_MINUTES = 5;
    
    // 内存中存储验证码信息（实际项目中可使用Redis等缓存）
    private final Map<String, VerificationCodeInfo> codeStorage = new ConcurrentHashMap<>();

    private final UserMapper userMapper;
    private final IEmailService emailService;

    /**
     * 验证码信息内部类
     */
    private static class VerificationCodeInfo {
        private final String code;
        private final LocalDateTime expiry;

        public VerificationCodeInfo(String code, LocalDateTime expiry) {
            this.code = code;
            this.expiry = expiry;
        }

        public String getCode() {
            return code;
        }

        public LocalDateTime getExpiry() {
            return expiry;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }

    /**
     * 构造函数注入，遵循依赖倒置原则
     *
     * @param userMapper   用户数据访问接口
     * @param emailService 邮件服务接口
     */
    @Autowired
    public UserServiceImpl(UserMapper userMapper, IEmailService emailService) {
        this.userMapper = userMapper;
        this.emailService = emailService;
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

    @Override
    public boolean sendVerificationCode(String email) {
        log.info("为邮箱 {} 发送验证码", email);
        
        try {
            // 检查邮箱是否已注册
            if (existsByEmail(email)) {
                log.warn("邮箱 {} 已被注册，不能发送注册验证码", email);
                throw new RuntimeException("该邮箱已被注册");
            }
            
            // 生成6位数字验证码
            String code = generateVerificationCode();
            
            // 设置过期时间
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);
            
            // 存储验证码信息
            codeStorage.put(email, new VerificationCodeInfo(code, expiry));
            
            // 发送邮件
            boolean emailSent = emailService.sendVerificationCode(email, code);
            
            if (emailSent) {
                log.info("验证码发送成功到邮箱: {}", email);
                return true;
            } else {
                // 发送失败，移除存储的验证码
                codeStorage.remove(email);
                log.error("验证码发送失败到邮箱: {}", email);
                return false;
            }
            
        } catch (RuntimeException e) {
            // 重新抛出业务异常，让Controller处理
            throw e;
        } catch (Exception e) {
            log.error("发送验证码异常", e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        log.debug("验证邮箱 {} 的验证码", email);
        
        VerificationCodeInfo codeInfo = codeStorage.get(email);
        
        if (codeInfo == null) {
            log.warn("邮箱 {} 没有对应的验证码", email);
            return false;
        }
        
        if (codeInfo.isExpired()) {
            log.warn("邮箱 {} 的验证码已过期", email);
            codeStorage.remove(email); // 清理过期验证码
            return false;
        }
        
        boolean isValid = codeInfo.getCode().equals(code);
        
        if (isValid) {
            log.info("邮箱 {} 验证码验证成功", email);
            codeStorage.remove(email); // 验证成功后清理验证码
        } else {
            log.warn("邮箱 {} 验证码验证失败", email);
        }
        
        return isValid;
    }

    /**
     * 生成6位数字验证码
     *
     * @return 验证码字符串
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    @Override
    public Optional<User> authenticateUser(String username, String password) {
        log.info("用户登录认证，用户名: {}", username);
        
        try {
            // 根据用户名查找用户
            Optional<User> userOpt = findByUsername(username);
            if (!userOpt.isPresent()) {
                log.warn("用户登录失败：用户不存在，用户名: {}", username);
                return Optional.empty();
            }

            User user = userOpt.get();
            
            // 检查账户是否激活
            if (!user.getActive()) {
                log.warn("用户登录失败：账户未激活，用户名: {}", username);
                return Optional.empty();
            }

            // 验证密码
            if (!validatePassword(password, user.getPassword())) {
                log.warn("用户登录失败：密码错误，用户名: {}", username);
                return Optional.empty();
            }

            log.info("用户登录认证成功，用户名: {}", username);
            return Optional.of(user);
            
        } catch (Exception e) {
            log.error("用户登录认证异常", e);
            return Optional.empty();
        }
    }
}