package com.jellystudy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class NacosConfig {

    @Value("${notification.max-unread:100}")
    private int maxUnreadCount;

    @Value("${notification.retention-days:30}")
    private int retentionDays;

    @Value("${message.max-length:2000}")
    private int messageMaxLength;

    @Value("${system.maintenance-mode:false}")
    private boolean maintenanceMode;

    @Value("${ai.response-timeout:30}")
    private int aiResponseTimeout;

    public int getMaxUnreadCount() {
        return maxUnreadCount;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public int getMessageMaxLength() {
        return messageMaxLength;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public int getAiResponseTimeout() {
        return aiResponseTimeout;
    }
}