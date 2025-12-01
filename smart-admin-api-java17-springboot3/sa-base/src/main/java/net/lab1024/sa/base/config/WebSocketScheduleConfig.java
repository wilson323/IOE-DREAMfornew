package net.lab1024.sa.base.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * WebSocket调度配置
 *
 * 启用调度功能以支持心跳检测等定时任务
 *
 * @author SmartAdmin
 * @since 2025-11-14
 */
@Configuration
@EnableScheduling
public class WebSocketScheduleConfig {
    // 此配置类主要用于启用@Scheduled注解的支持
    // 具体的调度任务在WebSocketHeartbeatHandler中实现
}