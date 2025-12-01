package net.lab1024.sa.admin.module.smart.monitor.websocket;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * WebSocket连接统计信息
 * <p>
 * 严格遵循repowiki规范：
 * - 提供WebSocket连接的详细统计
 * - 支持实时监控和性能分析
 * - 包含连接时间、数量、错误率等指标
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
public class WebSocketConnectionStats {

    /**
     * 总连接数
     */
    private Long totalConnections;

    /**
     * 当前活跃连接数
     */
    private Long activeConnections;

    /**
     * 峰值连接数
     */
    private Long peakConnections;

    /**
     * 今日连接数
     */
    private Long todayConnections;

    /**
     * 平均连接时长（秒）
     */
    private Double averageConnectionDuration;

    /**
     * 总消息发送数
     */
    private Long totalMessagesSent;

    /**
     * 总消息接收数
     */
    private Long totalMessagesReceived;

    /**
     * 今日消息发送数
     */
    private Long todayMessagesSent;

    /**
     * 今日消息接收数
     */
    private Long todayMessagesReceived;

    /**
     * 连接成功率
     */
    private Double connectionSuccessRate;

    /**
     * 消息发送成功率
     */
    private Double messageSendSuccessRate;

    /**
     * 平均响应时间（毫秒）
     */
    private Double averageResponseTime;

    /**
     * 服务器启动时间
     */
    private LocalDateTime serverStartTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 最大并发连接数
     */
    private Long maxConcurrentConnections;

    /**
     * 当前在线用户数
     */
    private Long onlineUsers;

    /**
     * 连接错误数
     */
    private Long connectionErrors;

    /**
     * 消息错误数
     */
    private Long messageErrors;

    /**
     * 内存使用量（MB）
     */
    private Double memoryUsage;

    /**
     * CPU使用率（百分比）
     */
    private Double cpuUsage;

    public WebSocketConnectionStats() {
        this.serverStartTime = LocalDateTime.now();
        this.lastUpdateTime = LocalDateTime.now();
        this.totalConnections = 0L;
        this.activeConnections = 0L;
        this.peakConnections = 0L;
        this.todayConnections = 0L;
        this.totalMessagesSent = 0L;
        this.totalMessagesReceived = 0L;
        this.todayMessagesSent = 0L;
        this.todayMessagesReceived = 0L;
        this.connectionSuccessRate = 100.0;
        this.messageSendSuccessRate = 100.0;
        this.averageResponseTime = 0.0;
        this.maxConcurrentConnections = 0L;
        this.onlineUsers = 0L;
        this.connectionErrors = 0L;
        this.messageErrors = 0L;
        this.memoryUsage = 0.0;
        this.cpuUsage = 0.0;
    }
}