package net.lab1024.sa.device.comm.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 设备离线事件生产者
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * 负责将设备离线事件发布到RabbitMQ消息队列
 * </p>
 * <p>
 * 核心职责：
 * - 发布设备离线事件
 * - 发布设备恢复事件
 * - 消息发送失败重试
 * - 消息发送统计
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class DeviceOfflineEventProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    // 消息交换机和路由键配置
    private static final String EXCHANGE = "device.offline.exchange";
    private static final String OFFLINE_ROUTING_KEY = "device.offline.event";
    private static final String RECOVERY_ROUTING_KEY = "device.recovery.event";

    // 消息发送统计
    private volatile long totalSent = 0;
    private volatile long offlineEventSent = 0;
    private volatile long recoveryEventSent = 0;
    private volatile long sendFailed = 0;

    /**
     * 发送设备离线事件
     *
     * @param eventData 事件数据
     */
    public void sendOfflineEvent(Map<String, Object> eventData) {
        if (eventData == null || eventData.isEmpty()) {
            log.warn("[离线事件] 事件数据为空，跳过发送");
            return;
        }

        try {
            log.debug("[离线事件] 发送设备离线事件: deviceId={}, eventType={}",
                    eventData.get("deviceId"), eventData.get("eventType"));

            // 发送到RabbitMQ
            rabbitTemplate.convertAndSend(EXCHANGE, OFFLINE_ROUTING_KEY, eventData);

            // 更新统计
            totalSent++;
            offlineEventSent++;

            log.info("[离线事件] 设备离线事件发送成功: deviceId={}, deviceName={}",
                    eventData.get("deviceId"), eventData.get("deviceName"));

        } catch (Exception e) {
            sendFailed++;
            log.error("[离线事件] 发送设备离线事件失败: deviceId={}, error={}",
                    eventData.get("deviceId"), e.getMessage(), e);
        }
    }

    /**
     * 发送设备恢复事件
     *
     * @param eventData 事件数据
     */
    public void sendRecoveryEvent(Map<String, Object> eventData) {
        if (eventData == null || eventData.isEmpty()) {
            log.warn("[恢复事件] 事件数据为空，跳过发送");
            return;
        }

        try {
            log.debug("[恢复事件] 发送设备恢复事件: deviceId={}", eventData.get("deviceId"));

            // 发送到RabbitMQ
            rabbitTemplate.convertAndSend(EXCHANGE, RECOVERY_ROUTING_KEY, eventData);

            // 更新统计
            totalSent++;
            recoveryEventSent++;

            log.info("[恢复事件] 设备恢复事件发送成功: deviceId={}, deviceName={}",
                    eventData.get("deviceId"), eventData.get("deviceName"));

        } catch (Exception e) {
            sendFailed++;
            log.error("[恢复事件] 发送设备恢复事件失败: deviceId={}, error={}",
                    eventData.get("deviceId"), e.getMessage(), e);
        }
    }

    /**
     * 获取消息发送统计
     */
    public MessageStatistics getMessageStatistics() {
        MessageStatistics statistics = new MessageStatistics();
        statistics.setTotalSent(totalSent);
        statistics.setOfflineEventSent(offlineEventSent);
        statistics.setRecoveryEventSent(recoveryEventSent);
        statistics.setSendFailed(sendFailed);

        // 计算成功率
        long totalAttempts = totalSent + sendFailed;
        if (totalAttempts > 0) {
            double successRate = (totalSent * 100.0 / totalAttempts);
            statistics.setSuccessRate(Math.round(successRate * 100.0) / 100.0);
        } else {
            statistics.setSuccessRate(0.0);
        }

        return statistics;
    }

    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        log.info("[离线事件] 重置消息发送统计");
        totalSent = 0;
        offlineEventSent = 0;
        recoveryEventSent = 0;
        sendFailed = 0;
    }

    // ==================== 内部类 ====================

    /**
     * 消息发送统计
     */
    public static class MessageStatistics {
        private long totalSent;
        private long offlineEventSent;
        private long recoveryEventSent;
        private long sendFailed;
        private double successRate;

        // Getters and Setters
        public long getTotalSent() {
            return totalSent;
        }

        public void setTotalSent(long totalSent) {
            this.totalSent = totalSent;
        }

        public long getOfflineEventSent() {
            return offlineEventSent;
        }

        public void setOfflineEventSent(long offlineEventSent) {
            this.offlineEventSent = offlineEventSent;
        }

        public long getRecoveryEventSent() {
            return recoveryEventSent;
        }

        public void setRecoveryEventSent(long recoveryEventSent) {
            this.recoveryEventSent = recoveryEventSent;
        }

        public long getSendFailed() {
            return sendFailed;
        }

        public void setSendFailed(long sendFailed) {
            this.sendFailed = sendFailed;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }

        /**
         * 生成文本报告
         */
        @Override
        public String toString() {
            return String.format(
                "\n==================== 设备离线事件统计 ====================\n" +
                "总发送消息数: %d\n" +
                "离线事件消息: %d\n" +
                "恢复事件消息: %d\n" +
                "发送失败数量: %d\n" +
                "发送成功率: %.2f%%\n" +
                "========================================================\n",
                totalSent, offlineEventSent, recoveryEventSent, sendFailed, successRate
            );
        }
    }
}
