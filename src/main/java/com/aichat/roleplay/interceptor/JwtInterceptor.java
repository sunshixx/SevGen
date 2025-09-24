package com.aichat.roleplay.interceptor;

import com.aichat.roleplay.context.UserContext;
import com.aichat.roleplay.model.User;
import com.aichat.roleplay.service.IUserService;
import com.aichat.roleplay.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * JWT拦截器
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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
                logger.warn("请别忘了携带JWT Token进行请求，或者Token已过期");
            }
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
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