package com.jellystudy.controller;

import com.jellystudy.config.NacosConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class NacosConfigController {

    @Autowired
    private NacosConfig nacosConfig;

    @GetMapping("/nacos")
    public Map<String, Object> getNacosConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxUnreadCount", nacosConfig.getMaxUnreadCount());
        config.put("retentionDays", nacosConfig.getRetentionDays());
        config.put("messageMaxLength", nacosConfig.getMessageMaxLength());
        config.put("maintenanceMode", nacosConfig.isMaintenanceMode());
        config.put("aiResponseTimeout", nacosConfig.getAiResponseTimeout());
        config.put("source", "Nacos Config Center");
        config.put("timestamp", System.currentTimeMillis());
        return config;
    }
}