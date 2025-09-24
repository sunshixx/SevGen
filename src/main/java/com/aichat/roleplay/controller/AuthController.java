package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.dto.LoginRequest;
import com.aichat.roleplay.dto.LoginResponse;
import com.aichat.roleplay.dto.RegisterRequest;
import com.aichat.roleplay.dto.SendVerificationCodeRequest;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IUserService;
import com.aichat.roleplay.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 负责用户注册、验证码发送、登录登出等认证相关功能
 */
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final IUserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到用户登录请求，用户名: {}", request.getUsername());

        try {
            // 用户认证
            Optional<User> userOpt = userService.authenticateUser(request.getUsername(), request.getPassword());

            if (!userOpt.isPresent()) {
                log.warn("用户登录失败：用户名或密码错误，用户名: {}", request.getUsername());
                return ApiResponse.error(401, "用户名或密码错误");
            }

            User user = userOpt.get();

            // 生成JWT token
            String token = jwtUtil.generateToken(user.getUsername());

            // 构建响应
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getAvatar(),
                    user.getActive()
            );

            LoginResponse response = new LoginResponse(token, userInfo);

            log.info("用户登录成功，用户名: {}", request.getUsername());
            return ApiResponse.success("登录成功", response);

        } catch (Exception e) {
            log.error("用户登录异常", e);
            return ApiResponse.error("登录失败，请稍后重试");
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<?> logout() {
        log.info("用户登出");

        try {
            UserContext.clear();

            log.info("用户登出成功");
            return ApiResponse.success("登出成功");

        } catch (Exception e) {
            log.error("用户登出异常", e);
            return ApiResponse.error("登出失败，请稍后重试");
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<?> getCurrentUser() {
        log.debug("获取当前用户信息");

        try {
            User currentUser = UserContext.getCurrentUser();

            if (currentUser == null) {
                log.warn("获取当前用户失败：用户未登录");
                return ApiResponse.error(401, "用户未登录");
            }
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getEmail(),
                    currentUser.getAvatar(),
                    currentUser.getActive()
            );

            return ApiResponse.success("获取用户信息成功", userInfo);

        } catch (Exception e) {
            log.error("获取当前用户异常", e);
            return ApiResponse.error("获取用户信息失败，请稍后重试");
        }
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-verification-code")
    public ApiResponse<?> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        log.info("收到发送验证码请求，邮箱: {}", request.getEmail());

        try {
            boolean success = userService.sendVerificationCode(request.getEmail());
            
            if (success) {
                log.info("验证码发送成功到邮箱: {}", request.getEmail());
                return ApiResponse.success("验证码发送成功，请查收邮件");
            } else {
                log.warn("验证码发送失败，邮箱: {}", request.getEmail());
                return ApiResponse.error("验证码发送失败，请检查邮箱地址或稍后重试");
            }
            
        } catch (Exception e) {
            log.error("发送验证码异常", e);
            return ApiResponse.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到用户注册请求，用户名: {}, 邮箱: {}", request.getUsername(), request.getEmail());

        try {
            // 验证验证码
            if (!userService.verifyCode(request.getEmail(), request.getVerificationCode())) {
                log.warn("用户注册失败：验证码错误或已过期，邮箱: {}", request.getEmail());
                return ApiResponse.badRequest("验证码错误或已过期");
            }

            // 创建用户对象
            User user = User.builder()
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .email(request.getEmail())
                    .active(true)
                    .build();

            // 注册用户
            User registeredUser = userService.registerUser(user);

            log.info("用户注册成功，用户ID: {}, 用户名: {}", registeredUser.getId(), registeredUser.getUsername());

            registeredUser.setPassword(null);
            
            return ApiResponse.success("用户注册成功", registeredUser);

        } catch (RuntimeException e) {
            log.warn("用户注册失败: {}", e.getMessage());
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("用户注册异常", e);
            return ApiResponse.error("用户注册失败，请稍后重试");
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ApiResponse<?> health() {
        return ApiResponse.success("Auth service is running");
    }
}