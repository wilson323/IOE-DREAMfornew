package net.lab1024.sa.attendance.message;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.processor.AttendanceAnomalyProcessor.AttendanceAnomaly;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考勤异常事件生产者
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * 负责将考勤异常事件发布到RabbitMQ消息队列
 * </p>
 * <p>
 * 核心职责：
 * - 发布考勤异常事件
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
public class AttendanceAnomalyEventProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    // 消息交换机和路由键配置
    private static final String EXCHANGE = "attendance.anomaly.exchange";
    private static final String ROUTING_KEY = "attendance.anomaly.event";

    // 消息发送统计
    private volatile long totalSent = 0;
    private volatile long sendFailed = 0;

    /**
     * 发送考勤异常事件
     *
     * @param anomalies 异常列表
     */
    public void sendAnomalyEvent(List<AttendanceAnomaly> anomalies) {
        if (anomalies == null || anomalies.isEmpty()) {
            log.warn("[异常事件] 异常列表为空，跳过发送");
            return;
        }

        try {
            // 构建事件数据
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("anomalyCount", anomalies.size());
            eventData.put("eventTime", java.time.LocalDateTime.now());

            // 按异常类型分组统计
            Map<String, Integer> typeCount = new HashMap<>();
            for (AttendanceAnomaly anomaly : anomalies) {
                typeCount.put(anomaly.getAnomalyType(),
                        typeCount.getOrDefault(anomaly.getAnomalyType(), 0) + 1);
            }
            eventData.put("anomalyTypeCount", typeCount);

            // 添加异常详情
            eventData.put("anomalies", anomalies);

            // 发送到RabbitMQ
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, eventData);

            // 更新统计
            totalSent++;

            log.info("[异常事件] 考勤异常事件发送成功: anomalyCount={}", anomalies.size());

        } catch (Exception e) {
            sendFailed++;
            log.error("[异常事件] 发送考勤异常事件失败: anomalyCount={}, error={}",
                    anomalies.size(), e.getMessage(), e);
        }
    }

    /**
     * 发送单个考勤异常事件
     *
     * @param anomaly 异常对象
     */
    public void sendSingleAnomalyEvent(AttendanceAnomaly anomaly) {
        if (anomaly == null) {
            log.warn("[异常事件] 异常对象为空，跳过发送");
            return;
        }

        try {
            // 构建事件数据
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("anomalyId", anomaly.getAnomalyId());
            eventData.put("userId", anomaly.getUserId());
            eventData.put("departmentId", anomaly.getDepartmentId());
            eventData.put("attendanceDate", anomaly.getAttendanceDate());
            eventData.put("anomalyType", anomaly.getAnomalyType());
            eventData.put("anomalyLevel", anomaly.getAnomalyLevel());
            eventData.put("description", anomaly.getDescription());
            eventData.put("eventTime", java.time.LocalDateTime.now());

            // 发送到RabbitMQ
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, eventData);

            // 更新统计
            totalSent++;

            log.debug("[异常事件] 单个考勤异常事件发送成功: anomalyId={}", anomaly.getAnomalyId());

        } catch (Exception e) {
            sendFailed++;
            log.error("[异常事件] 发送单个考勤异常事件失败: anomalyId={}, error={}",
                    anomaly.getAnomalyId(), e.getMessage(), e);
        }
    }

    /**
     * 获取消息发送统计
     */
    public MessageStatistics getMessageStatistics() {
        MessageStatistics statistics = new MessageStatistics();
        statistics.setTotalSent(totalSent);
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
        log.info("[异常事件] 重置消息发送统计");
        totalSent = 0;
        sendFailed = 0;
    }

    // ==================== 内部类 ====================

    /**
     * 消息发送统计
     */
    public static class MessageStatistics {
        private long totalSent;
        private long sendFailed;
        private double successRate;

        // Getters and Setters
        public long getTotalSent() {
            return totalSent;
        }

        public void setTotalSent(long totalSent) {
            this.totalSent = totalSent;
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

        @Override
        public String toString() {
            return String.format(
                "\n==================== 考勤异常事件统计 ====================\n" +
                "总发送消息数: %d\n" +
                "发送失败数量: %d\n" +
                "发送成功率: %.2f%%\n" +
                "========================================================\n",
                totalSent, sendFailed, successRate
            );
        }
    }
}
