package com.aichat.roleplay.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 简化版JWT工具类
 * 不依赖外部JWT库，实现基本的token功能
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private int expiration; // 24小时

    /**
     * 生成简化版token
     * 格式: username:timestamp:signature
     */
    public String generateToken(String username) {
        long timestamp = System.currentTimeMillis() + (expiration * 1000L);
        String payload = username + ":" + timestamp;
        String signature = generateSignature(payload);
        return Base64.getEncoder().encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length == 3) {
                return parts[0];
            }
        } catch (Exception e) {
            // token格式错误
        }
        return null;
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, String username) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 3) {
                return false;
            }

            String tokenUsername = parts[0];
            long timestamp = Long.parseLong(parts[1]);
            String signature = parts[2];

            // 验证用户名
            if (!tokenUsername.equals(username)) {
                return false;
            }

            // 验证是否过期
            if (System.currentTimeMillis() > timestamp) {
                return false;
            }

            // 验证签名
            String expectedSignature = generateSignature(tokenUsername + ":" + timestamp);
            return signature.equals(expectedSignature);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成签名
     */
    private String generateSignature(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }
}