package com.aichat.roleplay.interceptor;

import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IUserService;
import com.aichat.roleplay.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * JWT拦截器
 * 处理请求中的JWT token并设置用户上下文
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 清除之前的用户上下文
        UserContext.clear();

        // 获取token
        String token = getTokenFromRequest(request);
        
        if (token != null && !token.isEmpty()) {
            try {
                // 从token中获取用户名
                String username = jwtUtil.getUsernameFromToken(token);
                
                if (username != null && jwtUtil.validateToken(token, username)) {
                    // 查找用户并设置到上下文中
                    Optional<User> userOpt = userService.findByUsername(username);
                    if (userOpt.isPresent()) {
                        UserContext.setCurrentUser(userOpt.get());
                    }
                }
            } catch (Exception e) {
                // token无效，不设置用户上下文
            }
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除用户上下文
        UserContext.clear();
    }

    /**
     * 从请求中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}