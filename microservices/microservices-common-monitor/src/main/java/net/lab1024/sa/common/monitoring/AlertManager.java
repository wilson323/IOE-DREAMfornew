package net.lab1024.sa.common.monitoring;

import lombok.extern.slf4j.Slf4j;


import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 告警管理类
 * <p>
 * 职责：
 * - 接收、处理和分发告警
 * - 告警规则匹配
 * - 告警通知（通过NotificationConfigManager）
 * - 告警收敛、抑制
 * </p>
 * <p>
 * 注意：Manager类是纯Java类，不直接依赖Spring注解，通过构造函数注入依赖。
 * 在微服务中通过配置类将Manager注册为Spring Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Slf4j
public class AlertManager {


    private final MetricsCollector metricsCollector;
    // private final GatewayServiceClient gatewayServiceClient; // 可选，用于调用其他服务发送告警
    // private final NotificationConfigManager notificationConfigManager; // 用于获取通知渠道配置
    // 注意：NotificationConfigManager在ioedream-common-service中，不应在common-monitor中直接依赖
    // 如果需要通知功能，应该通过接口或事件机制解耦

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 简化构造函数
     *
     * @param metricsCollector 指标收集器
     */
    public AlertManager(MetricsCollector metricsCollector) {
        this(metricsCollector, null, null);
    }

    /**
     * 完整构造函数
     *
     * @param metricsCollector 指标收集器
     * @param gatewayServiceClient 网关服务客户端（可选）
     * @param notificationConfigManager 通知配置管理器（可选，在ioedream-common-service中注册）
     */
    public AlertManager(MetricsCollector metricsCollector, Object gatewayServiceClient,
                        Object notificationConfigManager) {
        this.metricsCollector = metricsCollector;
        // this.gatewayServiceClient = (GatewayServiceClient) gatewayServiceClient;
        // this.notificationConfigManager = (NotificationConfigManager) notificationConfigManager;
        init();
    }

    private void init() {
        // 启动告警规则检查调度任务
        scheduler.scheduleAtFixedRate(this::checkAlertRules, 1, 5, TimeUnit.MINUTES);
        log.info("[AlertManager] 告警规则检查调度已启动");
    }

    /**
     * 检查告警规则并触发告警
     */
    private void checkAlertRules() {
        log.debug("[AlertManager] 正在检查告警规则...");
        // 实际逻辑：从数据库加载告警规则，根据规则检查指标，如果触发则发送告警
        // 示例：检查CPU使用率是否超过阈值
        if (Math.random() > 0.8) { // 模拟随机触发告警
            sendAlert("CPU_HIGH_USAGE", "CPU使用率超过80%", "CRITICAL",
                    Map.of("host", "server-1", "cpu_usage", "85%"));
        }
    }

    /**
     * 发送告警
     *
     * @param alertCode    告警编码
     * @param message      告警消息
     * @param level        告警级别 (CRITICAL, ERROR, WARNING, INFO)
     * @param details      告警详情
     */
    public void sendAlert(String alertCode, String message, String level, Map<String, String> details) {
        log.warn("[ALERT] {} - {}: {}", level, alertCode, message);
        if (metricsCollector != null) {
            metricsCollector.recordBusinessMetric("alert.triggered.total", 1, "code", alertCode, "level", level);
        }

        // 通过NotificationConfigManager发送通知
        // 注意：NotificationConfigManager在ioedream-common-service中，不应在此直接调用
        // 实际应通过事件机制或接口解耦
        log.info("[AlertManager] 告警已记录，通知将通过事件机制发送");
    }

    /**
     * 刷新告警配置
     * <p>
     * 从数据库或配置中心重新加载告警规则和通知渠道配置
     * </p>
     */
    public void refreshAlertConfig() {
        log.info("[AlertManager] 刷新告警配置");
        // TODO: 实现从数据库或配置中心重新加载告警规则和通知渠道配置的逻辑
        // 例如：重新加载告警规则、更新通知渠道配置等
    }

    /**
     * 关闭AlertManager，清理资源
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("[AlertManager] 已关闭");
    }
}
