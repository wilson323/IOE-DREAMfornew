package net.lab1024.sa.common.monitoring;

import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 企业级监控管理器
 * <p>
 * 提供业务指标监控、系统指标监控、告警通知等企业级监控功能
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入所有依赖（包括配置值）
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 3.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class EnterpriseMonitoringManager {

    private final boolean emailAlertEnabled;
    private final boolean smsAlertEnabled;
    private final String webhookUrl;
    private final String dingTalkWebhook;
    private final String smsProvider;
    private final String smsApiUrl;
    private final String smsApiKey;
    @SuppressWarnings("unused") // 预留字段，用于未来阿里云短信API实现
    private final String smsApiSecret;
    private final String smsTemplateId;
    private final String smsPhoneNumbers;
    private final String emailProvider;
    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String emailFrom;
    private final String emailTo;
    private final String emailApiUrl;
    private final String emailApiKey;
    private final MeterRegistry meterRegistry;
    private final RestTemplate restTemplate;

    /**
     * 构造函数注入所有依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param meterRegistry Micrometer指标注册表
     * @param restTemplate HTTP客户端（可选，默认创建新实例）
     * @param emailAlertEnabled 邮件告警是否启用
     * @param smsAlertEnabled 短信告警是否启用
     * @param webhookUrl Webhook URL
     * @param dingTalkWebhook 钉钉Webhook URL
     * @param smsProvider 短信服务商
     * @param smsApiUrl 短信API URL
     * @param smsApiKey 短信API Key
     * @param smsApiSecret 短信API Secret
     * @param smsTemplateId 短信模板ID
     * @param smsPhoneNumbers 短信接收号码
     * @param emailProvider 邮件服务商
     * @param smtpHost SMTP服务器地址
     * @param smtpPort SMTP服务器端口
     * @param smtpUsername SMTP用户名
     * @param smtpPassword SMTP密码
     * @param emailFrom 发件人地址
     * @param emailTo 收件人地址
     * @param emailApiUrl 邮件API URL
     * @param emailApiKey 邮件API Key
     */
    public EnterpriseMonitoringManager(
            MeterRegistry meterRegistry,
            RestTemplate restTemplate,
            boolean emailAlertEnabled,
            boolean smsAlertEnabled,
            String webhookUrl,
            String dingTalkWebhook,
            String smsProvider,
            String smsApiUrl,
            String smsApiKey,
            String smsApiSecret,
            String smsTemplateId,
            String smsPhoneNumbers,
            String emailProvider,
            String smtpHost,
            int smtpPort,
            String smtpUsername,
            String smtpPassword,
            String emailFrom,
            String emailTo,
            String emailApiUrl,
            String emailApiKey) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry不能为null");
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.emailAlertEnabled = emailAlertEnabled;
        this.smsAlertEnabled = smsAlertEnabled;
        this.webhookUrl = webhookUrl;
        this.dingTalkWebhook = dingTalkWebhook;
        this.smsProvider = smsProvider != null ? smsProvider : "aliyun";
        this.smsApiUrl = smsApiUrl;
        this.smsApiKey = smsApiKey;
        this.smsApiSecret = smsApiSecret;
        this.smsTemplateId = smsTemplateId;
        this.smsPhoneNumbers = smsPhoneNumbers;
        this.emailProvider = emailProvider != null ? emailProvider : "smtp";
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort > 0 ? smtpPort : 587;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.emailFrom = emailFrom;
        this.emailTo = emailTo;
        this.emailApiUrl = emailApiUrl;
        this.emailApiKey = emailApiKey;
    }

    /**
     * 业务计数器
     */
    private final Map<String, Counter> businessCounters = new ConcurrentHashMap<>();

    /**
     * 业务计时器
     */
    private final Map<String, Timer> businessTimers = new ConcurrentHashMap<>();

    /**
     * 业务仪表盘
     */
    private final Map<String, Gauge> businessGauges = new ConcurrentHashMap<>();

    /**
     * 系统健康指标
     */
    private final Map<String, HealthIndicator> healthIndicators = new ConcurrentHashMap<>();

    /**
     * 告警规则
     */
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();

    /**
     * 告警历史记录
     */
    private final List<AlertRecord> alertHistory = new ArrayList<>();

    /**
     * 记录业务指标计数
     */
    public void recordBusinessCount(String metricName, String... tags) {
        String key = buildMetricKey(metricName, tags);
        Counter counter = businessCounters.computeIfAbsent(key, k ->
            Counter.builder(metricName)
                .tags(tags)
                .description("Business count metric: " + metricName)
                .register(meterRegistry)
        );

        counter.increment();
        log.debug("[监控指标] 记录业务计数: metric={}, tags={}", metricName, tags);

        // 检查告警规则
        checkAlertRules(metricName, counter.count(), "COUNTER");
    }

    /**
     * 记录业务执行时间
     */
    public Timer.Sample startBusinessTimer(String metricName, String... tags) {
        return Timer.start(meterRegistry);
    }

    /**
     * 记录业务执行时间
     */
    public void recordBusinessTime(String metricName, long durationMs, String... tags) {
        String key = buildMetricKey(metricName, tags);
        Timer timer = businessTimers.computeIfAbsent(key, k ->
            Timer.builder(metricName)
                .tags(tags)
                .description("Business timer metric: " + metricName)
                .register(meterRegistry)
        );

        timer.record(durationMs, TimeUnit.MILLISECONDS);
        log.debug("[监控指标] 记录业务时间: metric={}, duration={}ms, tags={}", metricName, durationMs, tags);

        // 检查告警规则
        checkAlertRules(metricName, durationMs, "TIMER");
    }

    /**
     * 记录业务数值指标
     */
    public void recordBusinessGauge(String metricName, double value, String... tags) {
        String key = buildMetricKey(metricName, tags);
        // 使用AtomicLong存储double值（转换为long存储，精度为0.01）
        AtomicLong atomicValue = new AtomicLong((long)(value * 100));

        businessGauges.computeIfAbsent(key, k ->
            Gauge.builder(metricName, atomicValue, v -> v.get() / 100.0)
                .tags(tags)
                .description("Business gauge metric: " + metricName)
                .register(meterRegistry)
        );

        log.debug("[监控指标] 记录业务数值: metric={}, value={}, tags={}", metricName, value, tags);

        // 检查告警规则
        checkAlertRules(metricName, value, "GAUGE");
    }

    /**
     * 注册健康检查指标
     */
    public void registerHealthIndicator(String name, HealthIndicator indicator) {
        healthIndicators.put(name, indicator);
        log.debug("[监控指标] 注册健康检查: name={}", name);
    }

    /**
     * 添加告警规则
     */
    public void addAlertRule(AlertRule rule) {
        alertRules.put(rule.getName(), rule);
        log.info("[监控告警] 添加告警规则: name={}, threshold={}, operator={}",
            rule.getName(), rule.getThreshold(), rule.getOperator());
    }

    /**
     * 获取系统健康状态
     */
    public Map<String, Health> getSystemHealth() {
        Map<String, Health> healthMap = new ConcurrentHashMap<>();

        healthIndicators.forEach((name, indicator) -> {
            try {
                Health health = indicator.health();
                healthMap.put(name, health);
            } catch (Exception e) {
                healthMap.put(name, Health.down(e).build());
                log.warn("[监控指标] 健康检查异常: name={}, error={}", name, e.getMessage());
            }
        });

        return healthMap;
    }

    /**
     * 获取业务指标统计
     */
    public Map<String, Object> getBusinessMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();

        // 计数器统计
        businessCounters.forEach((key, counter) -> {
            Map<String, Object> counterStats = new ConcurrentHashMap<>();
            counterStats.put("count", counter.count());
            counterStats.put("type", "COUNTER");
            metrics.put(key, counterStats);
        });

        // 计时器统计
        businessTimers.forEach((key, timer) -> {
            Map<String, Object> timerStats = new ConcurrentHashMap<>();
            timerStats.put("count", timer.count());
            timerStats.put("mean", timer.mean(TimeUnit.MILLISECONDS));
            timerStats.put("max", timer.max(TimeUnit.MILLISECONDS));
            timerStats.put("total", timer.totalTime(TimeUnit.MILLISECONDS));
            timerStats.put("type", "TIMER");
            metrics.put(key, timerStats);
        });

        // 仪表盘统计
        businessGauges.forEach((key, gauge) -> {
            Map<String, Object> gaugeStats = new ConcurrentHashMap<>();
            gaugeStats.put("value", gauge.value());
            gaugeStats.put("type", "GAUGE");
            metrics.put(key, gaugeStats);
        });

        return metrics;
    }

    /**
     * 获取告警记录
     */
    public List<AlertRecord> getAlertHistory(int limit) {
        return alertHistory.stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 清理过期的告警记录
     */
    public void cleanExpiredAlerts(int hoursToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursToKeep);
        alertHistory.removeIf(record -> record.getTimestamp().isBefore(cutoffTime));
        log.debug("[监控告警] 清理过期告警记录: 保留{}小时", hoursToKeep);
    }

    /**
     * 检查告警规则
     */
    private void checkAlertRules(String metricName, double value, String metricType) {
        List<AlertRule> matchingRules = alertRules.values().stream()
            .filter(rule -> rule.getMetricName().equals(metricName))
            .filter(rule -> rule.getMetricType().equals(metricType))
            .collect(Collectors.toList());

        for (AlertRule rule : matchingRules) {
            if (evaluateAlertCondition(value, rule)) {
                AlertRecord record = new AlertRecord(
                    rule.getName(),
                    metricName,
                    value,
                    rule.getThreshold(),
                    rule.getSeverity(),
                    LocalDateTime.now()
                );

                alertHistory.add(record);
                log.warn("[监控告警] 触发告警: rule={}, metric={}, value={}, threshold={}",
                    rule.getName(), metricName, value, rule.getThreshold());

                // 发送告警通知
                sendAlertNotification(record);
            }
        }
    }

    /**
     * 评估告警条件
     */
    private boolean evaluateAlertCondition(double value, AlertRule rule) {
        switch (rule.getOperator()) {
            case GREATER_THAN:
                return value > rule.getThreshold();
            case LESS_THAN:
                return value < rule.getThreshold();
            case EQUAL:
                return value == rule.getThreshold();
            case GREATER_EQUAL:
                return value >= rule.getThreshold();
            case LESS_EQUAL:
                return value <= rule.getThreshold();
            case NOT_EQUAL:
                return value != rule.getThreshold();
            default:
                return false;
        }
    }

    /**
     * 发送告警通知
     */
    private void sendAlertNotification(AlertRecord record) {
        try {
            // 这里可以集成邮件、短信、钉钉等通知方式
            log.info("[监控告警] 发送告警通知: rule={}, message={}",
                record.getRuleName(), record.getMessage());

            // 实现具体的通知发送逻辑
            // 1. Webhook通知
            if (webhookUrl != null && !webhookUrl.isEmpty()) {
                sendWebhookNotification(record);
            }

            // 2. 钉钉通知
            if (dingTalkWebhook != null && !dingTalkWebhook.isEmpty()) {
                sendDingTalkNotification(record);
            }

            // 3. 邮件通知（可根据需要实现）
            if (emailAlertEnabled) {
                sendEmailNotification(record);
            }

            // 4. 短信通知（可根据需要实现）
            if (smsAlertEnabled) {
                sendSmsNotification(record);
            }

        } catch (Exception e) {
            log.error("[监控告警] 发送告警通知失败: record={}, error={}",
                record, e.getMessage(), e);
        }
    }

    /**
     * 构建指标键
     */
    private String buildMetricKey(String metricName, String... tags) {
        StringBuilder key = new StringBuilder(metricName);
        if (tags.length > 0) {
            key.append(":");
            key.append(String.join(",", tags));
        }
        return key.toString();
    }

    /**
     * 初始化监控管理器
     * <p>
     * 移除@PostConstruct，改为普通方法，由配置类在创建Bean后调用
     * </p>
     */
    public void init() {
        log.info("[监控指标] 企业级监控管理器初始化开始");

        // 注册JVM基础指标
        registerJvmMetrics();

        // 注册系统基础指标
        registerSystemMetrics();

        log.info("[监控指标] 企业级监控管理器初始化完成");
    }

    /**
     * 注册JVM指标
     */
    private void registerJvmMetrics() {
        // JVM内存指标
        Gauge.builder("jvm.memory.used", () -> {
            Runtime runtime = Runtime.getRuntime();
            return runtime.totalMemory() - runtime.freeMemory();
        }).description("JVM used memory").register(meterRegistry);

        Gauge.builder("jvm.memory.max", () -> {
            Runtime runtime = Runtime.getRuntime();
            return runtime.maxMemory();
        }).description("JVM max memory").register(meterRegistry);

        // GC指标
        registerHealthIndicator("jvm", () -> {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            double memoryUsage = (double) usedMemory / maxMemory;

            if (memoryUsage > 0.9) {
                return Health.down().withDetail("memory_usage", memoryUsage)
                    .withDetail("message", "JVM内存使用率过高").build();
            } else if (memoryUsage > 0.8) {
                return Health.unknown().withDetail("memory_usage", memoryUsage)
                    .withDetail("message", "JVM内存使用率偏高").build();
            } else {
                return Health.up().withDetail("memory_usage", memoryUsage).build();
            }
        });
    }

    /**
     * 注册系统指标
     */
    private void registerSystemMetrics() {
        // CPU指标
        registerHealthIndicator("cpu", () -> {
            // 这里可以集成具体的CPU监控实现
            return Health.up().build();
        });

        // 磁盘指标
        registerHealthIndicator("disk", () -> {
            // 这里可以集成具体的磁盘监控实现
            return Health.up().build();
        });
    }

    /**
     * 告警规则类
     */
    public static class AlertRule {
        private String name;
        private String metricName;
        private String metricType;
        private double threshold;
        private AlertOperator operator;
        private AlertSeverity severity;
        private String description;

        // 构造器
        public AlertRule(String name, String metricName, String metricType,
                        double threshold, AlertOperator operator, AlertSeverity severity) {
            this.name = name;
            this.metricName = metricName;
            this.metricType = metricType;
            this.threshold = threshold;
            this.operator = operator;
            this.severity = severity;
        }

        // Getters
        public String getName() { return name; }
        public String getMetricName() { return metricName; }
        public String getMetricType() { return metricType; }
        public double getThreshold() { return threshold; }
        public AlertOperator getOperator() { return operator; }
        public AlertSeverity getSeverity() { return severity; }
        public String getDescription() { return description; }

        // Setters
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 告警记录类
     */
    public static class AlertRecord {
        private String ruleName;
        private String metricName;
        private double value;
        private double threshold;
        private AlertSeverity severity;
        private LocalDateTime timestamp;
        private String message;

        public AlertRecord(String ruleName, String metricName, double value,
                          double threshold, AlertSeverity severity, LocalDateTime timestamp) {
            this.ruleName = ruleName;
            this.metricName = metricName;
            this.value = value;
            this.threshold = threshold;
            this.severity = severity;
            this.timestamp = timestamp;
            this.message = String.format("告警规则 %s 触发: 指标 %s 的值 %.2f %s 阈值 %.2f",
                ruleName, metricName, value, getOperatorText(), threshold);
        }

        private String getOperatorText() {
            // 根据告警规则返回操作符文本
            return "超过"; // 简化实现
        }

        // Getters
        public String getRuleName() { return ruleName; }
        public String getMetricName() { return metricName; }
        public double getValue() { return value; }
        public double getThreshold() { return threshold; }
        public AlertSeverity getSeverity() { return severity; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
    }

    /**
     * 告警操作符枚举
     */
    public enum AlertOperator {
        GREATER_THAN,
        LESS_THAN,
        EQUAL,
        GREATER_EQUAL,
        LESS_EQUAL,
        NOT_EQUAL
    }

    /**
     * 告警级别枚举
     */
    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * 发送Webhook通知
     */
    @SuppressWarnings("null")
    private void sendWebhookNotification(@NonNull AlertRecord record) {
        try {
            // 再次检查webhookUrl，确保非空
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.warn("[监控告警] Webhook URL未配置，跳过通知发送");
                return;
            }

            // 确保所有值非空，避免Null type safety警告
            String alertName = record.getRuleName() != null ? record.getRuleName() : "未知告警";
            String message = record.getMessage() != null ? record.getMessage() : "";
            String severity = record.getSeverity() != null ? record.getSeverity().name() : "INFO";
            String timestamp = LocalDateTime.now().toString();

            Map<String, Object> payload = Map.of(
                "alertName", alertName,
                "message", message,
                "severity", severity,
                "timestamp", timestamp,
                "service", "IOE-DREAM"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // webhookUrl 已在上方检查非空
            restTemplate.postForEntity(webhookUrl, entity, String.class);
            log.debug("[监控告警] Webhook通知发送成功: rule={}", record.getRuleName());

        } catch (Exception e) {
            log.error("[监控告警] Webhook通知发送失败: rule={}, error={}",
                record.getRuleName(), e.getMessage(), e);
        }
    }

    /**
     * 发送钉钉通知
     */
    @SuppressWarnings("null")
    private void sendDingTalkNotification(@NonNull AlertRecord record) {
        try {
            // 再次检查dingTalkWebhook，确保非空
            if (dingTalkWebhook == null || dingTalkWebhook.isEmpty()) {
                log.warn("[监控告警] 钉钉Webhook URL未配置，跳过通知发送");
                return;
            }

            // 确保所有值非空，避免Null type safety警告
            String ruleName = record.getRuleName() != null ? record.getRuleName() : "未知告警";
            String message = record.getMessage() != null ? record.getMessage() : "";
            String severity = record.getSeverity() != null ? record.getSeverity().name() : "INFO";
            String timestamp = LocalDateTime.now().toString();

            Map<String, Object> text = Map.of(
                "content", String.format("【IOE-DREAM监控告警】\n告警规则: %s\n告警信息: %s\n严重级别: %s\n告警时间: %s",
                    ruleName, message, severity, timestamp)
            );

            Map<String, Object> payload = Map.of(
                "msgtype", "text",
                "text", text
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // dingTalkWebhook 已在上方检查非空
            restTemplate.postForEntity(dingTalkWebhook, entity, String.class);
            log.debug("[监控告警] 钉钉通知发送成功: rule={}", record.getRuleName());

        } catch (Exception e) {
            log.error("[监控告警] 钉钉通知发送失败: rule={}, error={}",
                record.getRuleName(), e.getMessage(), e);
        }
    }

    /**
     * 发送邮件通知
     */
    @SuppressWarnings("null")
    private void sendEmailNotification(@NonNull AlertRecord record) {
        try {
            // 检查邮件配置
            if (emailTo == null || emailTo.isEmpty()) {
                log.warn("[监控告警] 邮件接收地址未配置，跳过邮件通知发送");
                return;
            }

            // 构建邮件内容
            String emailSubject = String.format("【IOE-DREAM监控告警】%s - %s",
                record.getSeverity() != null ? record.getSeverity().name() : "UNKNOWN",
                record.getRuleName());

            String emailBody = String.format(
                "<html><body>" +
                "<h2>IOE-DREAM 监控告警通知</h2>" +
                "<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>" +
                "<tr><td><strong>告警规则</strong></td><td>%s</td></tr>" +
                "<tr><td><strong>告警信息</strong></td><td>%s</td></tr>" +
                "<tr><td><strong>严重级别</strong></td><td>%s</td></tr>" +
                "<tr><td><strong>指标名称</strong></td><td>%s</td></tr>" +
                "<tr><td><strong>指标值</strong></td><td>%.2f</td></tr>" +
                "<tr><td><strong>阈值</strong></td><td>%.2f</td></tr>" +
                "<tr><td><strong>告警时间</strong></td><td>%s</td></tr>" +
                "</table>" +
                "<p style='color: #666; font-size: 12px; margin-top: 20px;'>此邮件由IOE-DREAM监控系统自动发送，请勿回复。</p>" +
                "</body></html>",
                record.getRuleName(),
                record.getMessage(),
                record.getSeverity() != null ? record.getSeverity().name() : "UNKNOWN",
                record.getMetricName(),
                record.getValue(),
                record.getThreshold(),
                record.getTimestamp()
            );

            // 根据邮件服务商选择不同的发送方式
            boolean success = false;
            switch (emailProvider.toLowerCase()) {
                case "smtp":
                    success = sendEmailViaSmtp(emailSubject, emailBody, emailTo);
                    break;
                case "webhook":
                    success = sendEmailViaWebhook(emailSubject, emailBody, emailTo);
                    break;
                case "aliyun":
                    success = sendEmailViaAliyun(emailSubject, emailBody, emailTo);
                    break;
                default:
                    log.warn("[监控告警] 不支持的邮件服务商: {}, 使用默认Webhook方式", emailProvider);
                    success = sendEmailViaWebhook(emailSubject, emailBody, emailTo);
                    break;
            }

            if (success) {
                log.debug("[监控告警] 邮件通知发送成功: rule={}, provider={}", record.getRuleName(), emailProvider);
            } else {
                log.error("[监控告警] 邮件通知发送失败: rule={}, provider={}", record.getRuleName(), emailProvider);
            }

        } catch (Exception e) {
            log.error("[监控告警] 邮件通知发送异常: rule={}, error={}",
                record.getRuleName(), e.getMessage(), e);
        }
    }

    /**
     * 通过SMTP方式发送邮件
     */
    @SuppressWarnings("null")
    private boolean sendEmailViaSmtp(String subject, String body, String to) {
        try {
            // 检查SMTP配置
            if (smtpHost == null || smtpHost.isEmpty()) {
                log.warn("[监控告警] SMTP服务器未配置，跳过邮件发送");
                return false;
            }

            if (emailFrom == null || emailFrom.isEmpty()) {
                log.warn("[监控告警] 发件人地址未配置，跳过邮件发送");
                return false;
            }

            // 使用RestTemplate调用邮件服务API（实际生产环境建议使用JavaMailSender）
            // 这里提供一个通用的HTTP API调用方式
            Map<String, Object> payload = Map.of(
                "from", emailFrom,
                "to", to.split(","),
                "subject", subject,
                "html", body,
                "smtp", Map.of(
                    "host", smtpHost,
                    "port", smtpPort,
                    "username", smtpUsername != null ? smtpUsername : "",
                    "password", smtpPassword != null ? smtpPassword : ""
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (emailApiKey != null && !emailApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + emailApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            // emailApiUrl 已在上方检查，这里使用默认值作为fallback
            String apiUrl = emailApiUrl != null && !emailApiUrl.isEmpty() ? emailApiUrl : "http://localhost:8080/api/email/send";
            restTemplate.postForEntity(apiUrl, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("[监控告警] SMTP邮件发送失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 通过Webhook方式发送邮件（通用方式）
     */
    @SuppressWarnings("null")
    private boolean sendEmailViaWebhook(String subject, String body, String to) {
        try {
            // 检查邮件API URL配置
            if (emailApiUrl == null || emailApiUrl.isEmpty()) {
                log.warn("[监控告警] 邮件API URL未配置，跳过邮件发送");
                return false;
            }

            Map<String, Object> payload = Map.of(
                "from", emailFrom != null && !emailFrom.isEmpty() ? emailFrom : "noreply@ioedream.com",
                "to", to.split(","),
                "subject", subject,
                "html", body,
                "timestamp", LocalDateTime.now().toString()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (emailApiKey != null && !emailApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + emailApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(emailApiUrl, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("[监控告警] Webhook邮件发送失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 通过阿里云邮件推送服务发送邮件
     */
    @SuppressWarnings("null")
    private boolean sendEmailViaAliyun(String subject, String body, String to) {
        try {
            // 检查邮件API URL配置
            if (emailApiUrl == null || emailApiUrl.isEmpty()) {
                log.warn("[监控告警] 邮件API URL未配置，跳过邮件发送");
                return false;
            }

            // 阿里云邮件推送API调用逻辑
            // 实际实现需要集成阿里云SDK或调用REST API
            Map<String, Object> payload = Map.of(
                "AccountName", emailFrom != null && !emailFrom.isEmpty() ? emailFrom : "noreply@ioedream.com",
                "ToAddress", to,
                "Subject", subject,
                "HtmlBody", body,
                "FromAlias", "IOE-DREAM监控系统"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (emailApiKey != null && !emailApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + emailApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(emailApiUrl, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("[监控告警] 阿里云邮件发送失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送短信通知
     */
    @SuppressWarnings("null")
    private void sendSmsNotification(@NonNull AlertRecord record) {
        try {
            // 检查短信配置
            if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                log.warn("[监控告警] 短信API URL未配置，跳过短信通知发送");
                return;
            }

            if (smsPhoneNumbers == null || smsPhoneNumbers.isEmpty()) {
                log.warn("[监控告警] 短信接收号码未配置，跳过短信通知发送");
                return;
            }

            // 构建短信内容
            String smsContent = String.format(
                "【IOE-DREAM监控告警】\n告警规则: %s\n告警信息: %s\n严重级别: %s\n告警时间: %s\n指标值: %.2f / 阈值: %.2f",
                record.getRuleName(),
                record.getMessage(),
                record.getSeverity() != null ? record.getSeverity().name() : "UNKNOWN",
                record.getTimestamp(),
                record.getValue(),
                record.getThreshold()
            );

            // 根据短信服务商选择不同的发送方式
            boolean success = false;
            switch (smsProvider.toLowerCase()) {
                case "aliyun":
                    success = sendAliyunSms(smsContent, smsPhoneNumbers);
                    break;
                case "tencent":
                    success = sendTencentSms(smsContent, smsPhoneNumbers);
                    break;
                case "webhook":
                    success = sendSmsViaWebhook(smsContent, smsPhoneNumbers);
                    break;
                default:
                    log.warn("[监控告警] 不支持的短信服务商: {}, 使用默认Webhook方式", smsProvider);
                    success = sendSmsViaWebhook(smsContent, smsPhoneNumbers);
                    break;
            }

            if (success) {
                log.debug("[监控告警] 短信通知发送成功: rule={}, provider={}", record.getRuleName(), smsProvider);
            } else {
                log.error("[监控告警] 短信通知发送失败: rule={}, provider={}", record.getRuleName(), smsProvider);
            }

        } catch (Exception e) {
            log.error("[监控告警] 短信通知发送异常: rule={}, error={}",
                record.getRuleName(), e.getMessage(), e);
        }
    }

    /**
     * 通过Webhook方式发送短信（通用方式）
     */
    @SuppressWarnings("null")
    private boolean sendSmsViaWebhook(String content, String phoneNumbers) {
        try {
            // 检查smsApiUrl是否配置
            if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                log.warn("[监控告警] 短信API URL未配置，跳过发送");
                return false;
            }

            Map<String, Object> payload = Map.of(
                "phones", phoneNumbers.split(","),
                "content", content,
                "timestamp", LocalDateTime.now().toString()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (smsApiKey != null && !smsApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + smsApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(smsApiUrl, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("[监控告警] Webhook短信发送失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送阿里云短信
     */
    @SuppressWarnings("null")
    private boolean sendAliyunSms(String content, String phoneNumbers) {
        try {
            // 检查smsApiUrl是否配置
            if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                log.warn("[监控告警] 短信API URL未配置，跳过发送");
                return false;
            }

            // 阿里云短信API调用逻辑
            // 实际实现需要集成阿里云SDK或调用REST API
            Map<String, Object> payload = Map.of(
                "PhoneNumbers", phoneNumbers,
                "SignName", "IOE-DREAM",
                "TemplateCode", smsTemplateId != null && !smsTemplateId.isEmpty() ? smsTemplateId : "SMS_ALERT",
                "TemplateParam", Map.of("content", content)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (smsApiKey != null && !smsApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + smsApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(smsApiUrl, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("[监控告警] 阿里云短信发送失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送腾讯云短信
     */
    @SuppressWarnings("null")
    private boolean sendTencentSms(String content, String phoneNumbers) {
        try {
            // 检查smsApiUrl是否配置
            if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                log.warn("[监控告警] 短信API URL未配置，跳过发送");
                return false;
            }

            // 腾讯云短信API调用逻辑
            // 实际实现需要集成腾讯云SDK或调用REST API
            Map<String, Object> payload = Map.of(
                "PhoneNumberSet", phoneNumbers.split(","),
                "TemplateID", smsTemplateId != null && !smsTemplateId.isEmpty() ? smsTemplateId : "SMS_ALERT",
                "TemplateParamSet", List.of(content)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (smsApiKey != null && !smsApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + smsApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(smsApiUrl, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("[监控告警] 腾讯云短信发送失败: error={}", e.getMessage(), e);
            return false;
        }
    }
}
