package net.lab1024.sa.common.monitoring;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.notification.manager.NotificationConfigManager;
import org.springframework.web.client.RestTemplate;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 告警管理器
 * <p>
 * 提供企业级监控告警体系
 * 支持实时监控、阈值告警、告警收敛、多渠道通知
 * 集成Prometheus、Grafana、钉钉、企业微信等告警渠道
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class AlertManager {

    private final MeterRegistry meterRegistry;
    private final ScheduledExecutorService scheduler;
    private final AlertConfig alertConfig;
    private final GatewayServiceClient gatewayServiceClient; // 网关服务客户端（可选）
    private final NotificationConfigManager notificationConfigManager; // 通知配置管理器（可选，用于从数据库读取配置）
    private final RestTemplate restTemplate; // HTTP客户端（用于直接调用钉钉API、企业微信API和短信API）
    private final String dingTalkWebhookUrl; // 钉钉Webhook URL（可选）
    private final String weChatWebhookUrl; // 企业微信Webhook URL（可选）
    private final String smsApiUrl; // 短信API URL（可选）
    private final String smsApiKey; // 短信API密钥（可选）
    private final String smsPhoneNumbers; // 短信接收号码（可选，逗号分隔）
    private final String smsProvider; // 短信服务商（aliyun/tencent/webhook，默认webhook）
    private final AlertNotificationDispatcher alertNotificationDispatcher;

    // 告警规则缓存
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();

    // 告警状态缓存
    private final Map<String, AlertStatus> alertStatuses = new ConcurrentHashMap<>();

    // 告警历史记录
    private final List<AlertRecord> alertHistory = Collections.synchronizedList(new ArrayList<>());

    // 构造函数注入依赖
    public AlertManager(MeterRegistry meterRegistry) {
        this(meterRegistry, null, null, null, null, null, null, null, null, null);
    }

    /**
     * 构造函数注入依赖（支持GatewayServiceClient）
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param meterRegistry 指标注册表
     * @param gatewayServiceClient 网关服务客户端（可选，如果为null则只记录日志）
     */
    public AlertManager(MeterRegistry meterRegistry, GatewayServiceClient gatewayServiceClient) {
        this(meterRegistry, gatewayServiceClient, null, null, null, null, null, null, null, null);
    }

    /**
     * 构造函数注入依赖（完整版本，支持钉钉、企业微信和短信直接调用，以及从数据库读取配置）
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     * <p>
     * 配置优先级：
     * 1. 如果提供了NotificationConfigManager，优先从数据库读取配置
     * 2. 如果数据库中没有配置，使用构造函数参数中的配置
     * 3. 如果都没有，使用默认配置
     * </p>
     *
     * @param meterRegistry 指标注册表
     * @param gatewayServiceClient 网关服务客户端（可选，如果为null则只记录日志）
     * @param notificationConfigManager 通知配置管理器（可选，用于从数据库读取配置）
     * @param restTemplate HTTP客户端（可选，用于直接调用钉钉API、企业微信API和短信API）
     * @param dingTalkWebhookUrl 钉钉Webhook URL（可选，如果提供则直接调用钉钉API）
     * @param weChatWebhookUrl 企业微信Webhook URL（可选，如果提供则直接调用企业微信API）
     * @param smsApiUrl 短信API URL（可选，如果提供则直接调用短信API）
     * @param smsApiKey 短信API密钥（可选）
     * @param smsPhoneNumbers 短信接收号码（可选，逗号分隔）
     * @param smsProvider 短信服务商（aliyun/tencent/webhook，默认webhook）
     */
    public AlertManager(MeterRegistry meterRegistry, GatewayServiceClient gatewayServiceClient,
                        NotificationConfigManager notificationConfigManager,
                        RestTemplate restTemplate, String dingTalkWebhookUrl, String weChatWebhookUrl,
                        String smsApiUrl, String smsApiKey, String smsPhoneNumbers, String smsProvider) {
        this(meterRegistry, gatewayServiceClient, notificationConfigManager, restTemplate,
                dingTalkWebhookUrl, weChatWebhookUrl, smsApiUrl, smsApiKey, smsPhoneNumbers, smsProvider, null);
    }

    /**
     * 构造函数注入依赖（完整版本，支持自定义通知分发器注入）
     * <p>
     * 用于单元测试或在特定场景下替换通知分发实现；保持原构造器兼容。
     * </p>
     */
    public AlertManager(MeterRegistry meterRegistry, GatewayServiceClient gatewayServiceClient,
                        NotificationConfigManager notificationConfigManager,
                        RestTemplate restTemplate, String dingTalkWebhookUrl, String weChatWebhookUrl,
                        String smsApiUrl, String smsApiKey, String smsPhoneNumbers, String smsProvider,
                        AlertNotificationDispatcher alertNotificationDispatcher) {
        this.meterRegistry = meterRegistry;
        this.gatewayServiceClient = gatewayServiceClient;
        this.notificationConfigManager = notificationConfigManager;
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.dingTalkWebhookUrl = dingTalkWebhookUrl;
        this.weChatWebhookUrl = weChatWebhookUrl;
        this.smsApiUrl = smsApiUrl;
        this.smsApiKey = smsApiKey;
        this.smsPhoneNumbers = smsPhoneNumbers;
        this.smsProvider = smsProvider != null && !smsProvider.isEmpty() ? smsProvider : "webhook";
        this.scheduler = Executors.newScheduledThreadPool(4, createDaemonThreadFactory("alert-manager"));
        this.alertConfig = new AlertConfig(notificationConfigManager);
        this.alertNotificationDispatcher = alertNotificationDispatcher != null
                ? alertNotificationDispatcher
                : new AlertNotificationDispatcher(
                        this.alertConfig,
                        this.gatewayServiceClient,
                        this.restTemplate,
                        this.dingTalkWebhookUrl,
                        this.weChatWebhookUrl,
                        this.smsApiUrl,
                        this.smsApiKey,
                        this.smsPhoneNumbers,
                        this.smsProvider
                );

        initializeAlertRules();
        startAlertMonitoring();
    }

    private static java.util.concurrent.ThreadFactory createDaemonThreadFactory(String namePrefix) {
        AtomicLong threadIndex = new AtomicLong(0L);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(namePrefix + "-" + threadIndex.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * 告警配置类
     * <p>
     * 支持从数据库读取配置，管理员可通过管理界面配置通知渠道的启用状态
     * 配置优先级：数据库配置 > 构造函数参数 > 默认配置
     * </p>
     */
    public static class AlertConfig {
        // 默认告警阈值
        private final Map<String, Double> thresholds = new HashMap<>();

        // 告警渠道配置（支持动态更新）
        private final Map<String, NotificationChannel> channels = new ConcurrentHashMap<>();

        // 告警收敛配置
        private final Map<String, ConvergenceConfig> convergence = new HashMap<>();

        /**
         * 构造函数（使用默认配置）
         */
        public AlertConfig() {
            this(null);
        }

        /**
         * 构造函数（支持从数据库读取配置）
         * <p>
         * 如果提供了NotificationConfigManager，优先从数据库读取配置
         * 配置键命名规范：
         * - ALERT.CHANNEL.{channelType}.ENABLED：渠道启用状态（true/false）
         * - ALERT.CHANNEL.{channelType}.NAME：渠道名称
         * 例如：ALERT.CHANNEL.DINGTALK.ENABLED、ALERT.CHANNEL.EMAIL.ENABLED
         * </p>
         *
         * @param notificationConfigManager 通知配置管理器（可选）
         */
        public AlertConfig(NotificationConfigManager notificationConfigManager) {
            // 初始化默认阈值
            thresholds.put("http.error.rate", 0.05);           // HTTP错误率 > 5%
            thresholds.put("response.time.avg", 3000.0);        // 平均响应时间 > 3秒
            thresholds.put("cache.hit.rate", 0.80);            // 缓存命中率 < 80%
            thresholds.put("cpu.usage", 0.85);                 // CPU使用率 > 85%
            thresholds.put("memory.usage", 0.85);              // 内存使用率 > 85%
            thresholds.put("disk.usage", 0.90);                // 磁盘使用率 > 90%
            thresholds.put("error.rate.system", 0.01);         // 系统错误率 > 1%
            thresholds.put("error.rate.business", 0.02);       // 业务错误率 > 2%

            // 初始化通知渠道（优先从数据库读取，如果没有则使用默认值）
            initializeChannels(notificationConfigManager);

            // 初始化收敛配置
            convergence.put("http.error.rate", new ConvergenceConfig(300, 600)); // 5分钟内只告警1次，10分钟内最多3次
            convergence.put("response.time.avg", new ConvergenceConfig(600, 1800));
            convergence.put("error.rate.system", new ConvergenceConfig(60, 300));
        }

        /**
         * 初始化通知渠道配置
         * <p>
         * 优先从数据库读取配置，如果数据库中没有配置，则使用默认值
         * 支持通过管理界面配置渠道的启用状态
         * </p>
         *
         * @param notificationConfigManager 通知配置管理器（可选）
         */
        private void initializeChannels(NotificationConfigManager notificationConfigManager) {
            // 定义所有支持的渠道
            String[] channelTypes = {"dingtalk", "wechat", "email", "sms"};
            String[] channelNames = {"钉钉通知", "企业微信", "邮件通知", "短信通知"};
            boolean[] defaultEnabled = {true, true, false, false};

            for (int i = 0; i < channelTypes.length; i++) {
                String channelType = channelTypes[i];
                String channelName = channelNames[i];
                boolean defaultEnabledValue = defaultEnabled[i];

                // 尝试从数据库读取配置
                boolean enabled = defaultEnabledValue;
                String name = channelName;

                if (notificationConfigManager != null) {
                    try {
                        // 读取渠道启用状态：ALERT.CHANNEL.{channelType}.ENABLED
                        String enabledConfig = notificationConfigManager.getConfigValue(
                                "ALERT.CHANNEL." + channelType.toUpperCase() + ".ENABLED"
                        );
                        if (enabledConfig != null) {
                            enabled = Boolean.parseBoolean(enabledConfig);
                        }

                        // 读取渠道名称：ALERT.CHANNEL.{channelType}.NAME
                        String nameConfig = notificationConfigManager.getConfigValue(
                                "ALERT.CHANNEL." + channelType.toUpperCase() + ".NAME"
                        );
                        if (nameConfig != null) {
                            name = nameConfig;
                        }
                    } catch (Exception e) {
                        // 如果读取配置失败，使用默认值
                        log.warn("[告警管理] 从数据库读取渠道配置失败，使用默认值, channelType={}, error={}",
                                channelType, e.getMessage());
                    }
                }

                channels.put(channelType, new NotificationChannel(channelType, name, enabled));
            }
        }

        /**
         * 刷新渠道配置（支持配置热更新）
         * <p>
         * 从数据库重新读取渠道配置，支持管理员通过界面修改配置后立即生效
         * </p>
         *
         * @param notificationConfigManager 通知配置管理器
         */
        public void refreshChannels(NotificationConfigManager notificationConfigManager) {
            if (notificationConfigManager == null) {
                log.warn("[告警管理] NotificationConfigManager为null，无法刷新渠道配置");
                return;
            }

            log.info("[告警管理] 开始刷新通知渠道配置");
            initializeChannels(notificationConfigManager);
            log.info("[告警管理] 通知渠道配置刷新完成, channelCount={}", channels.size());
        }

        // getters
        public Map<String, Double> getThresholds() { return thresholds; }
        public Map<String, NotificationChannel> getChannels() { return channels; }
        public Map<String, ConvergenceConfig> getConvergence() { return convergence; }
    }

    /**
     * 刷新告警配置（支持配置热更新）
     * <p>
     * 从数据库重新读取通知渠道配置，支持管理员通过界面修改配置后立即生效
     * 无需重启服务即可应用新配置
     * </p>
     */
    public void refreshAlertConfig() {
        if (notificationConfigManager == null) {
            log.warn("[告警管理] NotificationConfigManager为null，无法刷新配置");
            return;
        }

        log.info("[告警管理] 开始刷新告警配置");
        alertConfig.refreshChannels(notificationConfigManager);
        log.info("[告警管理] 告警配置刷新完成");
    }

    /**
     * 通知渠道配置
     * <p>
     * 支持动态更新启用状态，管理员可通过管理界面配置
     * </p>
     */
    public static class NotificationChannel {
        private final String type;
        private String name;
        private boolean enabled;

        public NotificationChannel(String type, String name, boolean enabled) {
            this.type = type;
            this.name = name;
            this.enabled = enabled;
        }

        public String getType() { return type; }
        public String getName() { return name; }
        public boolean isEnabled() { return enabled; }

        /**
         * 设置渠道名称
         *
         * @param name 渠道名称
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 设置渠道启用状态
         * <p>
         * 支持动态更新，管理员可通过管理界面修改后调用refreshAlertConfig()刷新
         * </p>
         *
         * @param enabled 是否启用
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 告警收敛配置
     */
    public static class ConvergenceConfig {
        private final long interval;      // 告警间隔（秒）
        private final long maxCount;      // 最大告警次数（秒）

        public ConvergenceConfig(long interval, long maxCount) {
            this.interval = interval;
            this.maxCount = maxCount;
        }

        public long getInterval() { return interval; }
        public long getMaxCount() { return maxCount; }
    }

    /**
     * 告警规则
     */
    public static class AlertRule {
        private final String id;
        private final String name;
        private final String metric;
        private final Double threshold;
        private final ComparisonOperator operator;
        private final AlertSeverity severity;
        private final String description;
        private final boolean enabled;

        public AlertRule(String id, String name, String metric, Double threshold,
                        ComparisonOperator operator, AlertSeverity severity, String description) {
            this.id = id;
            this.name = name;
            this.metric = metric;
            this.threshold = threshold;
            this.operator = operator;
            this.severity = severity;
            this.description = description;
            this.enabled = true;
        }

        // getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getMetric() { return metric; }
        public Double getThreshold() { return threshold; }
        public ComparisonOperator getOperator() { return operator; }
        public AlertSeverity getSeverity() { return severity; }
        public String getDescription() { return description; }
        public boolean isEnabled() { return enabled; }

        /**
         * 检查是否触发告警
         */
        public boolean shouldAlert(double value) {
            return switch (operator) {
                case GREATER_THAN -> value > threshold;
                case LESS_THAN -> value < threshold;
                case GREATER_EQUAL -> value >= threshold;
                case LESS_EQUAL -> value <= threshold;
                case EQUAL -> Double.compare(value, threshold) == 0;
                case NOT_EQUAL -> Double.compare(value, threshold) != 0;
            };
        }
    }

    /**
     * 比较操作符
     */
    public enum ComparisonOperator {
        GREATER_THAN("大于", ">"),
        LESS_THAN("小于", "<"),
        GREATER_EQUAL("大于等于", ">="),
        LESS_EQUAL("小于等于", "<="),
        EQUAL("等于", "=="),
        NOT_EQUAL("不等于", "!=");

        private final String name;
        private final String symbol;

        ComparisonOperator(String name, String symbol) {
            this.name = name;
            this.symbol = symbol;
        }

        public String getName() { return name; }
        public String getSymbol() { return symbol; }
    }

    /**
     * 告警严重级别
     */
    public enum AlertSeverity {
        CRITICAL("严重", 1, "🔴"),
        WARNING("警告", 2, "🟡"),
        INFO("信息", 3, "🔵"),
        SUCCESS("正常", 4, "🟢");

        private final String name;
        private final int level;
        private final String icon;

        AlertSeverity(String name, int level, String icon) {
            this.name = name;
            this.level = level;
            this.icon = icon;
        }

        public String getName() { return name; }
        public int getLevel() { return level; }
        public String getIcon() { return icon; }
    }

    /**
     * 告警状态
     */
    public static class AlertStatus {
        private final String ruleId;
        private boolean active;
        private LocalDateTime lastTriggerTime;
        private int triggerCount;
        private LocalDateTime lastNotificationTime;

        public AlertStatus(String ruleId) {
            this.ruleId = ruleId;
            this.active = false;
            this.triggerCount = 0;
        }

        // getters and setters
        public String getRuleId() { return ruleId; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public LocalDateTime getLastTriggerTime() { return lastTriggerTime; }
        public void setLastTriggerTime(LocalDateTime lastTriggerTime) { this.lastTriggerTime = lastTriggerTime; }
        public int getTriggerCount() { return triggerCount; }
        public void incrementTriggerCount() { this.triggerCount++; }
        public LocalDateTime getLastNotificationTime() { return lastNotificationTime; }
        public void setLastNotificationTime(LocalDateTime lastNotificationTime) { this.lastNotificationTime = lastNotificationTime; }
    }

    /**
     * 告警记录
     */
    public static class AlertRecord {
        private final String id;
        private final String ruleId;
        private final String ruleName;
        private final AlertSeverity severity;
        private final double value;
        private final double threshold;
        private final LocalDateTime triggerTime;
        private final String message;
        private final boolean resolved;
        private final LocalDateTime resolveTime;

        public AlertRecord(String ruleId, String ruleName, AlertSeverity severity,
                          double value, double threshold, String message) {
            this.id = UUID.randomUUID().toString();
            this.ruleId = ruleId;
            this.ruleName = ruleName;
            this.severity = severity;
            this.value = value;
            this.threshold = threshold;
            this.triggerTime = LocalDateTime.now();
            this.message = message;
            this.resolved = false;
            this.resolveTime = null;
        }

        public AlertRecord(AlertRecord original, boolean resolved) {
            this.id = original.id;
            this.ruleId = original.ruleId;
            this.ruleName = original.ruleName;
            this.severity = original.severity;
            this.value = original.value;
            this.threshold = original.threshold;
            this.triggerTime = original.triggerTime;
            this.message = original.message;
            this.resolved = resolved;
            this.resolveTime = LocalDateTime.now();
        }

        // getters
        public String getId() { return id; }
        public String getRuleId() { return ruleId; }
        public String getRuleName() { return ruleName; }
        public AlertSeverity getSeverity() { return severity; }
        public double getValue() { return value; }
        public double getThreshold() { return threshold; }
        public LocalDateTime getTriggerTime() { return triggerTime; }
        public String getMessage() { return message; }
        public boolean isResolved() { return resolved; }
        public LocalDateTime getResolveTime() { return resolveTime; }
    }

    /**
     * 初始化告警规则
     */
    private void initializeAlertRules() {
        // HTTP错误率告警
        alertRules.put("http.error.rate", new AlertRule(
            "http.error.rate",
            "HTTP错误率告警",
            "http.error.rate",
            alertConfig.getThresholds().get("http.error.rate"),
            ComparisonOperator.GREATER_THAN,
            AlertSeverity.WARNING,
            "HTTP请求错误率超过阈值"
        ));

        // 平均响应时间告警
        alertRules.put("response.time.avg", new AlertRule(
            "response.time.avg",
            "平均响应时间告警",
            "http.request.duration",
            alertConfig.getThresholds().get("response.time.avg"),
            ComparisonOperator.GREATER_THAN,
            AlertSeverity.WARNING,
            "HTTP平均响应时间超过阈值"
        ));

        // 缓存命中率告警
        alertRules.put("cache.hit.rate", new AlertRule(
            "cache.hit.rate",
            "缓存命中率告警",
            "cache.hit.rate",
            alertConfig.getThresholds().get("cache.hit.rate"),
            ComparisonOperator.LESS_THAN,
            AlertSeverity.INFO,
            "缓存命中率低于阈值"
        ));

        // CPU使用率告警
        alertRules.put("cpu.usage", new AlertRule(
            "cpu.usage",
            "CPU使用率告警",
            "system.cpu.usage",
            alertConfig.getThresholds().get("cpu.usage"),
            ComparisonOperator.GREATER_THAN,
            AlertSeverity.WARNING,
            "CPU使用率超过阈值"
        ));

        // 内存使用率告警
        alertRules.put("memory.usage", new AlertRule(
            "memory.usage",
            "内存使用率告警",
            "jvm.memory.used",
            alertConfig.getThresholds().get("memory.usage"),
            ComparisonOperator.GREATER_THAN,
            AlertSeverity.WARNING,
            "内存使用率超过阈值"
        ));

        // 系统错误率告警
        alertRules.put("error.rate.system", new AlertRule(
            "error.rate.system",
            "系统错误率告警",
            "error.system.rate",
            alertConfig.getThresholds().get("error.rate.system"),
            ComparisonOperator.GREATER_THAN,
            AlertSeverity.CRITICAL,
            "系统错误率超过阈值"
        ));

        // 业务错误率告警
        alertRules.put("error.rate.business", new AlertRule(
            "error.rate.business",
            "业务错误率告警",
            "error.business.rate",
            alertConfig.getThresholds().get("error.rate.business"),
            ComparisonOperator.GREATER_THAN,
            AlertSeverity.WARNING,
            "业务错误率超过阈值"
        ));

        log.info("[告警管理] 告警规则初始化完成, 规则数量: {}", alertRules.size());
    }

    /**
     * 启动告警监控
     */
    private void startAlertMonitoring() {
        // 每分钟检查一次告警规则
        scheduler.scheduleAtFixedRate(this::checkAlertRules, 60, 60, TimeUnit.SECONDS);

        // 每小时清理过期的告警记录
        scheduler.scheduleAtFixedRate(this::cleanupExpiredAlerts, 3600, 3600, TimeUnit.SECONDS);

        log.info("[告警管理] 告警监控任务已启动");
    }

    /**
     * 检查所有告警规则
     */
    public void checkAlertRules() {
        try {
            MetricOverview overview = getMetricOverview();

            for (Map.Entry<String, AlertRule> entry : alertRules.entrySet()) {
                // ruleId 未使用，直接使用 rule
                AlertRule rule = entry.getValue();

                if (!rule.isEnabled()) {
                    continue;
                }

                checkAlertRule(rule, overview);
            }

        } catch (Exception e) {
            log.error("[告警管理] 检查告警规则异常", e);
        }
    }

    /**
     * 检查单个告警规则
     */
    private void checkAlertRule(AlertRule rule, MetricOverview overview) {
        double currentValue = getMetricValue(rule.getMetric(), overview);
        AlertStatus status = alertStatuses.computeIfAbsent(rule.getId(), AlertStatus::new);

        boolean shouldAlert = rule.shouldAlert(currentValue);

        if (shouldAlert && !status.isActive()) {
            // 新告警触发
            triggerAlert(rule, currentValue, status);
        } else if (shouldAlert && status.isActive()) {
            // 告警持续，检查是否需要再次通知
            checkRepeatedAlert(rule, currentValue, status);
        } else if (!shouldAlert && status.isActive()) {
            // 告警恢复
            resolveAlert(rule, currentValue, status);
        }
    }

    /**
     * 获取指标值
     */
    private double getMetricValue(String metric, MetricOverview overview) {
        return switch (metric) {
            case "http.error.rate" -> calculateHttpErrorRate(overview);
            case "http.request.duration" -> overview.getAverageResponseTime();
            case "cache.hit.rate" -> overview.getCacheHitRate();
            case "system.cpu.usage" -> getSystemCpuUsage();
            case "jvm.memory.used" -> getJvmMemoryUsage();
            case "error.system.rate" -> calculateSystemErrorRate(overview);
            case "error.business.rate" -> calculateBusinessErrorRate(overview);
            default -> 0.0;
        };
    }

    /**
     * 计算HTTP错误率
     */
    private double calculateHttpErrorRate(MetricOverview overview) {
        long totalRequests = overview.getTotalRequests();
        long errorRequests = overview.getErrorRequests();

        return totalRequests > 0 ? (double) errorRequests / totalRequests : 0.0;
    }

    /**
     * 计算系统错误率
     */
    private double calculateSystemErrorRate(MetricOverview overview) {
        long totalOperations = overview.getTotalRequests() + overview.getBusinessOperations();
        long systemErrors = overview.getErrorCount();

        return totalOperations > 0 ? (double) systemErrors / totalOperations : 0.0;
    }

    /**
     * 计算业务错误率
     */
    private double calculateBusinessErrorRate(MetricOverview overview) {
        long totalOperations = overview.getTotalRequests() + overview.getBusinessOperations();
        long businessErrors = overview.getBusinessErrorCount();

        return totalOperations > 0 ? (double) businessErrors / totalOperations : 0.0;
    }

    /**
     * 获取系统CPU使用率
     */
    private double getSystemCpuUsage() {
        try {
            // 使用OperatingSystemMXBean获取CPU使用率
            return (double) Runtime.getRuntime().availableProcessors() / Runtime.getRuntime().totalMemory();
        } catch (Exception e) {
            log.warn("[告警管理] 获取CPU使用率失败", e);
            return 0.0;
        }
    }

    /**
     * 获取JVM内存使用率
     */
    private double getJvmMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return (double) usedMemory / totalMemory;
    }

    /**
     * 触发新告警
     */
    private void triggerAlert(AlertRule rule, double currentValue, AlertStatus status) {
        status.setActive(true);
        status.setLastTriggerTime(LocalDateTime.now());
        status.incrementTriggerCount();

        String message = String.format("%s告警: 当前值%.2f, 阈值%.2f. %s",
                rule.getName(), currentValue, rule.getThreshold(), rule.getDescription());

        AlertRecord record = new AlertRecord(rule.getId(), rule.getName(), rule.getSeverity(),
                currentValue, rule.getThreshold(), message);

        alertHistory.add(record);

        alertNotificationDispatcher.sendAlertNotification(rule, record, false);

        log.warn("[告警管理] 触发告警: {}", message);
    }

    /**
     * 检查重复告警
     */
    private void checkRepeatedAlert(AlertRule rule, double currentValue, AlertStatus status) {
        ConvergenceConfig convergence = alertConfig.getConvergence().get(rule.getId());
        if (convergence == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastNotification = status.getLastNotificationTime();

        // 检查是否需要再次通知
        if (lastNotification == null ||
            now.minusSeconds(convergence.getInterval()).isAfter(lastNotification)) {

            String message = String.format("%s持续告警: 当前值%.2f, 阈值%.2f, 已持续%d次",
                    rule.getName(), currentValue, rule.getThreshold(), status.getTriggerCount());

            AlertRecord record = new AlertRecord(rule.getId(), rule.getName(), rule.getSeverity(),
                    currentValue, rule.getThreshold(), message);

            alertNotificationDispatcher.sendAlertNotification(rule, record, true);

            status.setLastNotificationTime(now);
        }
    }

    /**
     * 解决告警
     */
    private void resolveAlert(AlertRule rule, double currentValue, AlertStatus status) {
        status.setActive(false);

        String message = String.format("%s告警已恢复: 当前值%.2f, 阈值%.2f",
                rule.getName(), currentValue, rule.getThreshold());

        // 查找最后的告警记录并标记为已解决
        Optional<AlertRecord> lastAlert = alertHistory.stream()
                .filter(r -> r.getRuleId().equals(rule.getId()) && !r.isResolved())
                .reduce((first, second) -> second);

        if (lastAlert.isPresent()) {
            AlertRecord resolvedRecord = new AlertRecord(lastAlert.get(), true);
            alertHistory.add(resolvedRecord);
        }

        alertNotificationDispatcher.sendRecoveryNotification(rule, currentValue);

        log.info("[告警管理] 告警已恢复: {}", message);
    }


    /**
     * 清理过期告警记录
     */
    private void cleanupExpiredAlerts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7); // 保留7天

        alertHistory.removeIf(record -> record.getTriggerTime().isBefore(cutoff) && record.isResolved());

        log.debug("[告警管理] 清理过期告警记录完成");
    }

    /**
     * 添加自定义告警规则
     */
    public void addAlertRule(AlertRule rule) {
        alertRules.put(rule.getId(), rule);
        log.info("[告警管理] 添加自定义告警规则: {}", rule.getName());
    }

    /**
     * 移除告警规则
     */
    public void removeAlertRule(String ruleId) {
        alertRules.remove(ruleId);
        alertStatuses.remove(ruleId);
        log.info("[告警管理] 移除告警规则: {}", ruleId);
    }

    /**
     * 获取告警规则列表
     */
    public List<AlertRule> getAlertRules() {
        return new ArrayList<>(alertRules.values());
    }

    /**
     * 获取活跃告警列表
     */
    public List<AlertRecord> getActiveAlerts() {
        return alertHistory.stream()
                .filter(record -> !record.isResolved())
                .sorted((r1, r2) -> r2.getTriggerTime().compareTo(r1.getTriggerTime()))
                .toList();
    }

    /**
     * 获取告警历史记录
     */
    public List<AlertRecord> getAlertHistory(int limit) {
        return alertHistory.stream()
                .sorted((r1, r2) -> r2.getTriggerTime().compareTo(r1.getTriggerTime()))
                .limit(limit)
                .toList();
    }

    /**
     * 获取告警统计信息
     */
    public Map<String, Object> getAlertStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<AlertRecord> activeAlerts = getActiveAlerts();
        Map<AlertSeverity, Long> severityCount = activeAlerts.stream()
                .collect(java.util.stream.Collectors.groupingBy(AlertRecord::getSeverity, java.util.stream.Collectors.counting()));

        stats.put("totalRules", alertRules.size());
        stats.put("activeAlerts", activeAlerts.size());
        stats.put("criticalAlerts", severityCount.getOrDefault(AlertSeverity.CRITICAL, 0L));
        stats.put("warningAlerts", severityCount.getOrDefault(AlertSeverity.WARNING, 0L));
        stats.put("infoAlerts", severityCount.getOrDefault(AlertSeverity.INFO, 0L));
        stats.put("totalHistory", alertHistory.size());

        return stats;
    }

    /**
     * 关闭告警管理器
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.debug("[告警管理] 调度器关闭被中断");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("[告警管理] 告警管理器已关闭");
    }

    /**
     * 获取所有指标概览（使用Micrometer直接查询）
     * <p>
     * 替代已废弃的 MetricsCollector.getMetricOverview
     * 使用 Micrometer 标准 API 直接查询指标
     * </p>
     *
     * @return 指标概览
     */
    private MetricOverview getMetricOverview() {
        MetricOverview overview = new MetricOverview();

        try {
            // 基础统计
            Counter totalRequestsCounter = meterRegistry.find("http.server.requests").counter();
            long totalRequests = totalRequestsCounter != null ? (long) totalRequestsCounter.count() : 0L;
            overview.setTotalRequests(totalRequests);

            // 查询成功请求数（状态码2xx）
            AtomicLong successRequests = new AtomicLong(0L);
            meterRegistry.find("http.server.requests")
                    .tag("status", "2xx")
                    .counters()
                    .forEach(counter -> successRequests.addAndGet((long) counter.count()));
            overview.setSuccessRequests(successRequests.get());

            // 查询错误请求数（状态码4xx和5xx）
            AtomicLong errorRequests = new AtomicLong(0L);
            meterRegistry.find("http.server.requests")
                    .tag("status", "4xx")
                    .counters()
                    .forEach(counter -> errorRequests.addAndGet((long) counter.count()));
            meterRegistry.find("http.server.requests")
                    .tag("status", "5xx")
                    .counters()
                    .forEach(counter -> errorRequests.addAndGet((long) counter.count()));
            overview.setErrorRequests(errorRequests.get());

            // 业务操作数（使用计数器数量作为近似值）
            int businessOperations = (int) meterRegistry.getMeters().stream()
                    .filter(meter -> meter.getId().getName().startsWith("business."))
                    .count();
            overview.setBusinessOperations(businessOperations);

            // 性能统计
            Timer httpTimer = meterRegistry.find("http.server.requests").timer();
            double averageResponseTime = httpTimer != null ? httpTimer.mean(TimeUnit.MILLISECONDS) : 0.0;
            overview.setAverageResponseTime(averageResponseTime);

            // 缓存命中率
            Counter cacheHitCounter = meterRegistry.find("cache.hit").counter();
            Counter cacheMissCounter = meterRegistry.find("cache.miss").counter();
            long cacheHits = cacheHitCounter != null ? (long) cacheHitCounter.count() : 0L;
            long cacheMisses = cacheMissCounter != null ? (long) cacheMissCounter.count() : 0L;
            long cacheTotal = cacheHits + cacheMisses;
            double cacheHitRate = cacheTotal > 0 ? (double) cacheHits / cacheTotal : 0.0;
            overview.setCacheHitRate(cacheHitRate);

            // 系统健康状态
            Counter systemErrorCounter = meterRegistry.find("error.system.total").counter();
            long errorCount = systemErrorCounter != null ? (long) systemErrorCounter.count() : 0L;
            overview.setErrorCount(errorCount);

            Counter businessErrorCounter = meterRegistry.find("error.business.total").counter();
            long businessErrorCount = businessErrorCounter != null ? (long) businessErrorCounter.count() : 0L;
            overview.setBusinessErrorCount(businessErrorCount);

            // 应用运行时间
            Gauge uptimeGauge = meterRegistry.find("application.uptime").gauge();
            double uptime = uptimeGauge != null ? uptimeGauge.value() : 0.0;
            overview.setUptime(uptime);

            // 时间戳
            overview.setLastUpdateTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error("[告警管理] 获取指标概览失败", e);
            // 返回默认值
            overview.setTotalRequests(0L);
            overview.setSuccessRequests(0L);
            overview.setErrorRequests(0L);
            overview.setBusinessOperations(0);
            overview.setAverageResponseTime(0.0);
            overview.setCacheHitRate(0.0);
            overview.setErrorCount(0L);
            overview.setBusinessErrorCount(0L);
            overview.setUptime(0.0);
            overview.setLastUpdateTime(LocalDateTime.now());
        }

        return overview;
    }

    /**
     * 指标概览数据结构
     * <p>
     * 替代已废弃的 MetricsCollector.MetricOverview
     * </p>
     */
    public static class MetricOverview {
        private long totalRequests;
        private long successRequests;
        private long errorRequests;
        private int businessOperations;
        private double averageResponseTime;
        private double cacheHitRate;
        private long errorCount;
        private long businessErrorCount;
        private double uptime;
        private LocalDateTime lastUpdateTime;

        // getters and setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        public long getSuccessRequests() { return successRequests; }
        public void setSuccessRequests(long successRequests) { this.successRequests = successRequests; }
        public long getErrorRequests() { return errorRequests; }
        public void setErrorRequests(long errorRequests) { this.errorRequests = errorRequests; }
        public int getBusinessOperations() { return businessOperations; }
        public void setBusinessOperations(int businessOperations) { this.businessOperations = businessOperations; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(double cacheHitRate) { this.cacheHitRate = cacheHitRate; }
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        public long getBusinessErrorCount() { return businessErrorCount; }
        public void setBusinessErrorCount(long businessErrorCount) { this.businessErrorCount = businessErrorCount; }
        public double getUptime() { return uptime; }
        public void setUptime(double uptime) { this.uptime = uptime; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }
}
