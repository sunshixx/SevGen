package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.config.MailProperties;
import com.aichat.roleplay.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private MailProperties mailProperties;

    @Override
    public boolean sendVerificationCode(String to, String code) {
        try {
            log.info("正在发送验证码邮件到: {}", to);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getFrom());
            message.setTo(to);
            message.setSubject("【SevGen】邮箱验证码");
            message.setText(buildVerificationEmailContent(code));
            
            mailSender.send(message);
            
            log.info("验证码邮件发送成功到: {}", to);
            return true;
            
        } catch (Exception e) {
            log.error("发送验证码邮件失败到: {}", to, e);

            log.warn("邮件发送失败");
            log.info("=== 验证码信息 ===");
            log.info("收件人: {}", to);
            log.info("验证码: {}", code);
            log.info("===============");
            return true;
        }
    }

    /**
     * 构建验证码邮件内容
     *
     * @param code 验证码
     * @return 邮件内容
     */
    private String buildVerificationEmailContent(String code) {
        StringBuilder content = new StringBuilder();
        content.append("亲爱的用户，\n\n");
        content.append("您正在注册SevGen账户，您的验证码是：\n\n");
        content.append("验证码：").append(code).append("\n\n");
        content.append("验证码有效期为5分钟，请尽快完成验证。\n\n");
        content.append("如果这不是您的操作，请忽略此邮件。\n\n");
        content.append("祝您使用愉快！\n");
        content.append("SevGen团队");
        return content.toString();
    }
}