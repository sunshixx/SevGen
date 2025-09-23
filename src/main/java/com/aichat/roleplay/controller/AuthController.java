package com.aichat.roleplay.controller;

import com.aichat.roleplay.common.ApiResponse;
import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IUserService;
import com.aichat.roleplay.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        log.info("用户登录尝试: {}", username);

        try {
            // 验证用户凭据
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ApiResponse.error(401, "用户名或密码错误");
            }

            User user = userOpt.get();
            if (!userService.validatePassword(password, user.getPassword())) {
                return ApiResponse.error(401, "用户名或密码错误");
            }

            // 生成JWT token
            String token = jwtUtil.generateToken(username);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            
            log.info("用户登录成功: {}", username);
            return ApiResponse.success("登录成功", response);
            
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return ApiResponse.error("登录失败，请稍后重试");
        }
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody User user) {
        log.info("用户注册尝试: {}", user.getUsername());
        
        try {
            userService.registerUser(user);
            log.info("用户注册成功: {}", user.getUsername());
            return ApiResponse.success("用户注册成功");
        } catch (RuntimeException e) {
            log.warn("用户注册失败: {}", e.getMessage());
            return ApiResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ApiResponse<?> getCurrentUser() {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.unauthorized();
        }
        return ApiResponse.success(currentUser);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout() {
        // JWT是无状态的，客户端删除token即可
        // 这里可以添加token黑名单逻辑（如果需要的话）
        return ApiResponse.success("退出登录成功");
    }
}