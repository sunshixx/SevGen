package com.aichat.roleplay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件配置属性
 */
@Component
@ConfigurationProperties(prefix = "spring.mail.template")
public class MailProperties {

    private String from;
    private String fromName;

    // Constructors
    public MailProperties() {}

    // Getters and Setters
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}