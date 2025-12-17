package net.lab1024.sa.common.permission.alert;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.permission.domain.dto.PermissionAuditDTO;
import net.lab1024.sa.common.permission.audit.PermissionAuditLogger;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.HashMap;

/**
 * 权限异常访问告警管理器
 * <p>
 * 企业级权限异常访问检测和告警系统，提供：
 * - 权限验证失败实时检测和统计
 * - 可疑权限访问模式识别和分析
 * - 异常访问行为自动告警通知
 * - 权限攻击行为智能防护
 * - 告警规则配置和动态管理
 * - 告警通知多渠道推送（邮件、短信、钉钉等）
 * - 告警历史记录和统计分析
 * </p>
 * <p>
 * 告警触发条件：
 * 1. 权限验证失败次数超过阈值
 * 2. 短时间内大量不同权限尝试
 * 3. 非工作时间异常权限访问
 * 4. 敏感操作权限验证失败
 * 5. 同一IP多用户权限异常尝试
 * 6. 权限提升请求异常频繁
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class PermissionAlertManager {

    @Resource
    private PermissionAuditLogger permissionAuditLogger;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 告警规则配置
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();

    // 用户失败次数计数器
    private final Map<Long, AtomicLong> userFailureCounters = new ConcurrentHashMap<>();

    // IP异常访问计数器
    private final Map<String, AtomicLong> ipAbnormalCounters = new ConcurrentHashMap<>();

    // 告警历史记录
    private final Queue<PermissionAlert> alertHistory = new ArrayDeque<>();

    // Redis键前缀
    private static final String USER_FAILURE_KEY_PREFIX = "alert:user:failure:";
    private static final String IP_ABNORMAL_KEY_PREFIX = "alert:ip:abnormal:";
    private static final String ALERT_RULE_KEY_PREFIX = "alert:rule:";
    private static final String ALERT_HISTORY_KEY_PREFIX = "alert:history:";

    /**
     * 处理权限验证审计日志，检测异常访问
     *
     * @param auditDTO 权限审计日志
     */
    @Async("alertExecutor")
    public void processPermissionAudit(PermissionAuditDTO auditDTO) {
        if (auditDTO == null) {
            return;
        }

        try {
            // 检查权限验证失败
            if ("DENIED".equals(auditDTO.getResult())) {
                handlePermissionDenied(auditDTO);
            }

            // 检查敏感操作异常
            if (auditDTO.isSensitive() && !"GRANTED".equals(auditDTO.getResult())) {
                handleSensitiveOperationDenied(auditDTO);
            }

            // 检查异常访问模式
            checkAbnormalAccessPattern(auditDTO);

            // 检查权限攻击模式
            checkPermissionAttackPattern(auditDTO);

        } catch (Exception e) {
            log.error("[权限告警] 处理权限审计日志异常", e);
        }
    }

    /**
     * 处理权限验证失败
     */
    private void handlePermissionDenied(PermissionAuditDTO auditDTO) {
        Long userId = auditDTO.getUserId();
        String clientIp = auditDTO.getClientIp();
        String operation = auditDTO.getOperation();

        // 更新用户失败次数
        long userFailureCount = incrementUserFailureCount(userId);

        // 更新IP异常访问次数
        if (clientIp != null) {
            incrementIpAbnormalCount(clientIp);
        }

        // 检查用户失败次数阈值
        AlertRule userFailureRule = alertRules.get("USER_FAILURE_THRESHOLD");
        if (userFailureRule != null && userFailureCount >= userFailureRule.getThreshold()) {
            triggerUserFailureAlert(userId, operation, userFailureCount, auditDTO);
        }

        // 检查IP异常访问阈值
        AlertRule ipAbnormalRule = alertRules.get("IP_ABNORMAL_THRESHOLD");
        if (ipAbnormalRule != null && clientIp != null) {
            long ipAbnormalCount = getIpAbnormalCount(clientIp);
            if (ipAbnormalCount >= ipAbnormalRule.getThreshold()) {
                triggerIpAbnormalAlert(clientIp, operation, ipAbnormalCount, auditDTO);
            }
        }
    }

    /**
     * 处理敏感操作失败
     */
    private void handleSensitiveOperationDenied(PermissionAuditDTO auditDTO) {
        Long userId = auditDTO.getUserId();
        String operation = auditDTO.getOperation();
        String resource = auditDTO.getResource();

        // 敏感操作失败立即告警
        PermissionAlert alert = PermissionAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .userId(userId)
            .clientIp(auditDTO.getClientIp())
            .operation(operation)
            .resource(resource)
            .alertType("SENSITIVE_OPERATION_DENIED")
            .severity("HIGH")
            .message(String.format("敏感操作权限验证失败: 用户=%d, 操作=%s, 资源=%s", userId, operation, resource))
            .createTime(LocalDateTime.now())
            .status("ACTIVE")
            .build();

        publishAlert(alert);
        log.warn("[权限告警-敏感] 敏感操作权限验证失败: {}", alert.getMessage());
    }

    /**
     * 检查异常访问模式
     */
    private void checkAbnormalAccessPattern(PermissionAuditDTO auditDTO) {
        Long userId = auditDTO.getUserId();
        String operation = auditDTO.getOperation();

        // 检查短时间内大量不同权限尝试
        if (isExcessivePermissionAttempts(userId)) {
            triggerExcessiveAttemptsAlert(userId, auditDTO);
        }

        // 检查非工作时间异常访问
        if (isAfterHoursAccess(auditDTO.getAuditTime())) {
            triggerAfterHoursAlert(userId, operation, auditDTO);
        }

        // 检查权限提升请求异常
        if (isPrivilegeEscalationAttempt(operation)) {
            triggerPrivilegeEscalationAlert(userId, operation, auditDTO);
        }
    }

    /**
     * 检查权限攻击模式
     */
    private void checkPermissionAttackPattern(PermissionAuditDTO auditDTO) {
        String clientIp = auditDTO.getClientIp();
        Long userId = auditDTO.getUserId();

        // 检查同IP多用户权限异常尝试
        if (clientIp != null && isMultiUserAbnormalAccess(clientIp)) {
            triggerMultiUserAttackAlert(clientIp, auditDTO);
        }

        // 检查暴力破解模式
        if (isBruteForceAttackPattern(userId, clientIp)) {
            triggerBruteForceAlert(userId, clientIp, auditDTO);
        }

        // 检查权限枚举攻击
        if (isPermissionEnumerationAttack(userId)) {
            triggerEnumerationAttackAlert(userId, auditDTO);
        }
    }

    /**
     * 触发用户失败次数告警
     */
    private void triggerUserFailureAlert(Long userId, String operation, long failureCount, PermissionAuditDTO auditDTO) {
        PermissionAlert alert = PermissionAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .userId(userId)
            .clientIp(auditDTO.getClientIp())
            .operation(operation)
            .alertType("USER_FAILURE_THRESHOLD")
            .severity("MEDIUM")
            .message(String.format("用户权限验证失败次数过多: 用户=%d, 失败次数=%d, 阈值=%d",
                                   userId, failureCount, alertRules.get("USER_FAILURE_THRESHOLD").getThreshold()))
            .createTime(LocalDateTime.now())
            .status("ACTIVE")
            .metadata(buildFailureMetadata(failureCount))
            .build();

        publishAlert(alert);
    }

    /**
     * 触发IP异常访问告警
     */
    private void triggerIpAbnormalAlert(String clientIp, String operation, long abnormalCount, PermissionAuditDTO auditDTO) {
        PermissionAlert alert = PermissionAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .userId(auditDTO.getUserId())
            .clientIp(clientIp)
            .operation(operation)
            .alertType("IP_ABNORMAL_THRESHOLD")
            .severity("HIGH")
            .message(String.format("IP地址异常访问频次过高: IP=%s, 异常次数=%d, 阈值=%d",
                                   clientIp, abnormalCount, alertRules.get("IP_ABNORMAL_THRESHOLD").getThreshold()))
            .createTime(LocalDateTime.now())
            .status("ACTIVE")
            .metadata(buildIpMetadata(clientIp, abnormalCount))
            .build();

        publishAlert(alert);
    }

    /**
     * 触发过度权限尝试告警
     */
    private void triggerExcessiveAttemptsAlert(Long userId, PermissionAuditDTO auditDTO) {
        PermissionAlert alert = PermissionAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .userId(userId)
            .clientIp(auditDTO.getClientIp())
            .operation("MULTIPLE_PERMISSIONS_ATTEMPT")
            .alertType("EXCESSIVE_PERMISSION_ATTEMPTS")
            .severity("MEDIUM")
            .message(String.format("用户短时间内尝试过多不同权限: 用户=%d", userId))
            .createTime(LocalDateTime.now())
            .status("ACTIVE")
            .build();

        publishAlert(alert);
    }

    /**
     * 触发非工作时间访问告警
     */
    private void triggerAfterHoursAlert(Long userId, String operation, PermissionAuditDTO auditDTO) {
        PermissionAlert alert = PermissionAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .userId(userId)
            .clientIp(auditDTO.getClientIp())
            .operation(operation)
            .alertType("AFTER_HOURS_ACCESS")
            .severity("LOW")
            .message(String.format("非工作时间权限访问: 用户=%d, 操作=%s", userId, operation))
            .createTime(LocalDateTime.now())
            .status("ACTIVE")
            .build();

        publishAlert(alert);
    }

    /**
     * 触发权限提升攻击告警
     */
    private void triggerPrivilegeEscalationAlert(Long userId, String operation, PermissionAuditDTO auditDTO) {
        PermissionAlert alert = PermissionAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .userId(userId)
            .clientIp(auditDTO.getClientIp())
            .operation(operation)
            .alertType("PRIVILEGE_ESCALATION_ATTEMPT")
            .severity("HIGH")
            .message(String.format("检测到权限提升尝试: 用户=%d, 操作=%s", userId, operation))
            .createTime(LocalDateTime.now())
            .status("ACTIVE")
            .build();

        publishAlert(alert);
    }

    /**
     * 触发多用户攻击告警
     */
    private void triggerMultiUserAttackAlert(String clientIp, PermissionAuditDTO auditDTO) {
        PermissionAlert alert = PermissionAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .clientIp(clientIp)
            .operation("MULTI_USER_ATTACK")
            .alertType("MULTI_USER_ATTACK")
            .severity("CRITICAL")
            .message(String.format("检测到同IP多用户异常访问模式: IP=%s", clientIp))
            .createTime(LocalDateTime.now())
            .status("ACTIVE")
            .build();

        publishAlert(alert);
    }

    /**
     * 触发暴力破解告警
     */
    private void triggerBruteForceAlert(Long userId, String clientIp, PermissionAuditDTO auditDTO) {
        PermissionAlert alert = PermissionAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .userId(userId)
            .clientIp(clientIp)
            .operation("BRUTE_FORCE_ATTACK")
            .alertType("BRUTE_FORCE_ATTACK")
            .severity("CRITICAL")
            .message(String.format("检测到权限暴力破解尝试: 用户=%d, IP=%s", userId, clientIp))
            .createTime(LocalDateTime.now())
            .status("ACTIVE")
            .build();

        publishAlert(alert);
    }

    /**
     * 触发权限枚举攻击告警
     */
    private void triggerEnumerationAttackAlert(Long userId, PermissionAuditDTO auditDTO) {
        PermissionAlert alert = PermissionAlert.builder()
            .alertId(UUID.randomUUID().toString())
            .userId(userId)
            .clientIp(auditDTO.getClientIp())
            .operation("PERMISSION_ENUMERATION")
            .alertType("PERMISSION_ENUMERATION_ATTACK")
            .severity("MEDIUM")
            .message(String.format("检测到权限枚举攻击尝试: 用户=%d", userId))
            .createTime(LocalDateTime.now())
            .status("ACTIVE")
            .build();

        publishAlert(alert);
    }

    /**
     * 发布告警
     */
    private void publishAlert(PermissionAlert alert) {
        try {
            // 存储到Redis
            String alertKey = ALERT_HISTORY_KEY_PREFIX + alert.getAlertId();
            redisTemplate.opsForValue().set(alertKey, alert, java.time.Duration.ofDays(7));

            // 添加到内存历史记录
            synchronized (alertHistory) {
                alertHistory.offer(alert);
                // 保持历史记录数量在合理范围
                while (alertHistory.size() > 1000) {
                    alertHistory.poll();
                }
            }

            // 发送告警通知
            sendAlertNotification(alert);

            // 更新告警统计
            updateAlertStatistics(alert);

            log.warn("[权限告警] 发布告警: type={}, severity={}, userId={}, message={}",
                     alert.getAlertType(), alert.getSeverity(), alert.getUserId(), alert.getMessage());

        } catch (Exception e) {
            log.error("[权限告警] 发布告警异常", e);
        }
    }

    /**
     * 发送告警通知
     */
    private void sendAlertNotification(PermissionAlert alert) {
        try {
            // 根据告警严重程度选择通知渠道
            switch (alert.getSeverity()) {
                case "CRITICAL":
                    // 严重告警：短信 + 邮件 + 钉钉
                    sendSmsNotification(alert);
                    sendEmailNotification(alert);
                    sendDingTalkNotification(alert);
                    break;
                case "HIGH":
                    // 高级告警：邮件 + 钉钉
                    sendEmailNotification(alert);
                    sendDingTalkNotification(alert);
                    break;
                case "MEDIUM":
                    // 中级告警：钉钉
                    sendDingTalkNotification(alert);
                    break;
                case "LOW":
                    // 低级告警：仅记录日志
                    break;
            }

        } catch (Exception e) {
            log.error("[权限告警] 发送告警通知异常", e);
        }
    }

    /**
     * 发送短信通知
     */
    private void sendSmsNotification(PermissionAlert alert) {
        // TODO: 实现短信通知逻辑
        log.info("[权限告警-短信] 发送短信告警: {}", alert.getMessage());
    }

    /**
     * 发送邮件通知
     */
    private void sendEmailNotification(PermissionAlert alert) {
        // TODO: 实现邮件通知逻辑
        log.info("[权限告警-邮件] 发送邮件告警: {}", alert.getMessage());
    }

    /**
     * 发送钉钉通知
     */
    private void sendDingTalkNotification(PermissionAlert alert) {
        // TODO: 实现钉钉通知逻辑
        log.info("[权限告警-钉钉] 发送钉钉告警: {}", alert.getMessage());
    }

    /**
     * 更新告警统计
     */
    private void updateAlertStatistics(PermissionAlert alert) {
        try {
            String statsKey = "alert:statistics";
            redisTemplate.opsForHash().increment(statsKey, alert.getAlertType(), 1);
            redisTemplate.opsForHash().increment(statsKey, alert.getSeverity(), 1);
            redisTemplate.expire(statsKey, java.time.Duration.ofDays(30));

        } catch (Exception e) {
            log.error("[权限告警] 更新告警统计异常", e);
        }
    }

    /**
     * 初始化默认告警规则
     */
    public void initializeDefaultRules() {
        // 用户失败次数阈值规则
        alertRules.put("USER_FAILURE_THRESHOLD", AlertRule.builder()
            .ruleId("USER_FAILURE_THRESHOLD")
            .ruleName("用户权限验证失败次数阈值")
            .alertType("USER_FAILURE_THRESHOLD")
            .threshold(10L) // 10次失败
            .timeWindow(300) // 5分钟内
            .severity("MEDIUM")
            .enabled(true)
            .description("用户在指定时间窗口内权限验证失败次数超过阈值时触发告警")
            .build());

        // IP异常访问阈值规则
        alertRules.put("IP_ABNORMAL_THRESHOLD", AlertRule.builder()
            .ruleId("IP_ABNORMAL_THRESHOLD")
            .ruleName("IP地址异常访问阈值")
            .alertType("IP_ABNORMAL_THRESHOLD")
            .threshold(50L) // 50次异常访问
            .timeWindow(300) // 5分钟内
            .severity("HIGH")
            .enabled(true)
            .description("IP地址在指定时间窗口内异常访问次数超过阈值时触发告警")
            .build());

        log.info("[权限告警] 默认告警规则初始化完成");
    }

    /**
     * 定期清理过期计数器
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void cleanupExpiredCounters() {
        try {
            // 清理Redis中的过期计数器
            Set<String> keys = redisTemplate.keys(USER_FAILURE_KEY_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl != null && ttl <= 0) {
                        redisTemplate.delete(key);
                    }
                }
            }

            // 清理内存中的过期计数器
            // TODO: 实现内存计数器的过期清理逻辑

        } catch (Exception e) {
            log.error("[权限告警] 清理过期计数器异常", e);
        }
    }

    // 以下是辅助方法实现...

    private long incrementUserFailureCount(Long userId) {
        if (userId == null) return 0;

        String key = USER_FAILURE_KEY_PREFIX + userId;
        AtomicLong counter = userFailureCounters.computeIfAbsent(userId, k -> new AtomicLong(0));
        long count = counter.incrementAndGet();

        // 同时更新Redis
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, java.time.Duration.ofMinutes(5));

        return count;
    }

    private long getIpAbnormalCount(String clientIp) {
        if (clientIp == null) return 0;

        String key = IP_ABNORMAL_KEY_PREFIX + clientIp;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (Long) value : 0;
    }

    private void incrementIpAbnormalCount(String clientIp) {
        if (clientIp == null) return;

        String key = IP_ABNORMAL_KEY_PREFIX + clientIp;
        redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, java.time.Duration.ofMinutes(5));
    }

    private Map<String, Object> buildFailureMetadata(long failureCount) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("failureCount", failureCount);
        metadata.put("timestamp", System.currentTimeMillis());
        return metadata;
    }

    private Map<String, Object> buildIpMetadata(String clientIp, long abnormalCount) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("clientIp", clientIp);
        metadata.put("abnormalCount", abnormalCount);
        metadata.put("timestamp", System.currentTimeMillis());
        return metadata;
    }

    private boolean isExcessivePermissionAttempts(Long userId) {
        // TODO: 实现过度权限尝试检测逻辑
        return false;
    }

    private boolean isAfterHoursAccess(LocalDateTime auditTime) {
        if (auditTime == null) return false;

        int hour = auditTime.getHour();
        return hour < 9 || hour > 18; // 非工作时间
    }

    private boolean isPrivilegeEscalationAttempt(String operation) {
        if (operation == null) return false;

        return operation.toUpperCase().contains("ADMIN") ||
               operation.toUpperCase().contains("ROOT") ||
               operation.toUpperCase().contains("SUPER");
    }

    private boolean isMultiUserAbnormalAccess(String clientIp) {
        // TODO: 实现同IP多用户异常访问检测逻辑
        return false;
    }

    private boolean isBruteForceAttackPattern(Long userId, String clientIp) {
        // TODO: 实现暴力破解攻击模式检测逻辑
        return false;
    }

    private boolean isPermissionEnumerationAttack(Long userId) {
        // TODO: 实现权限枚举攻击检测逻辑
        return false;
    }

    /**
     * 获取告警统计信息
     */
    public AlertStatistics getAlertStatistics() {
        AlertStatistics stats = new AlertStatistics();

        try {
            String statsKey = "alert:statistics";
            Map<Object, Object> statsData = redisTemplate.opsForHash().entries(statsKey);

            if (statsData != null) {
                for (Map.Entry<Object, Object> entry : statsData.entrySet()) {
                    String key = (String) entry.getKey();
                    Long value = entry.getValue() != null ? Long.valueOf(entry.getValue().toString()) : 0L;

                    if (key.startsWith("USER_FAILURE")) {
                        stats.setUserFailureAlerts(value);
                    } else if (key.startsWith("IP_ABNORMAL")) {
                        stats.setIpAbnormalAlerts(value);
                    } else if (key.equals("CRITICAL")) {
                        stats.setCriticalAlerts(value);
                    } else if (key.equals("HIGH")) {
                        stats.setHighAlerts(value);
                    } else if (key.equals("MEDIUM")) {
                        stats.setMediumAlerts(value);
                    } else if (key.equals("LOW")) {
                        stats.setLowAlerts(value);
                    }
                }
            }

            // 设置总告警数
            stats.setTotalAlerts(stats.getUserFailureAlerts() + stats.getIpAbnormalAlerts());

            // 设置当前活跃告警数
            stats.setActiveAlerts((long) alertHistory.size());

        } catch (Exception e) {
            log.error("[权限告警] 获取告警统计信息异常", e);
        }

        return stats;
    }

    /**
     * 获取告警历史记录
     */
    public List<PermissionAlert> getAlertHistory(int limit) {
        synchronized (alertHistory) {
            return alertHistory.stream()
                .limit(limit)
                .collect(Collectors.toList());
        }
    }

    /**
     * 告警规则配置
     */
    public static class AlertRule {
        private String ruleId;
        private String ruleName;
        private String alertType;
        private Long threshold;
        private Integer timeWindow;
        private String severity;
        private Boolean enabled;
        private String description;
        private Map<String, Object> parameters;

        public AlertRule() {}

        private AlertRule(Builder builder) {
            this.ruleId = builder.ruleId;
            this.ruleName = builder.ruleName;
            this.alertType = builder.alertType;
            this.threshold = builder.threshold;
            this.timeWindow = builder.timeWindow;
            this.severity = builder.severity;
            this.enabled = builder.enabled;
            this.description = builder.description;
            this.parameters = builder.parameters;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getRuleId() { return ruleId; }
        public String getRuleName() { return ruleName; }
        public String getAlertType() { return alertType; }
        public Long getThreshold() { return threshold; }
        public Integer getTimeWindow() { return timeWindow; }
        public String getSeverity() { return severity; }
        public Boolean getEnabled() { return enabled; }
        public String getDescription() { return description; }
        public Map<String, Object> getParameters() { return parameters; }

        // Setters
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public void setThreshold(Long threshold) { this.threshold = threshold; }
        public void setTimeWindow(Integer timeWindow) { this.timeWindow = timeWindow; }
        public void setSeverity(String severity) { this.severity = severity; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public void setDescription(String description) { this.description = description; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

        public static class Builder {
            private String ruleId;
            private String ruleName;
            private String alertType;
            private Long threshold;
            private Integer timeWindow;
            private String severity;
            private Boolean enabled;
            private String description;
            private Map<String, Object> parameters;

            public Builder ruleId(String ruleId) { this.ruleId = ruleId; return this; }
            public Builder ruleName(String ruleName) { this.ruleName = ruleName; return this; }
            public Builder alertType(String alertType) { this.alertType = alertType; return this; }
            public Builder threshold(Long threshold) { this.threshold = threshold; return this; }
            public Builder timeWindow(Integer timeWindow) { this.timeWindow = timeWindow; return this; }
            public Builder severity(String severity) { this.severity = severity; return this; }
            public Builder enabled(Boolean enabled) { this.enabled = enabled; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder parameters(Map<String, Object> parameters) { this.parameters = parameters; return this; }

            public AlertRule build() {
                return new AlertRule(this);
            }
        }
    }

    /**
     * 权限告警实体
     */
    public static class PermissionAlert {
        private String alertId;
        private Long userId;
        private String clientIp;
        private String operation;
        private String resource;
        private String alertType;
        private String severity;
        private String message;
        private LocalDateTime createTime;
        private String status; // ACTIVE, RESOLVED, SUPPRESSED
        private Map<String, Object> metadata;
        private LocalDateTime resolvedTime;
        private String resolvedBy;

        public PermissionAlert() {}

        private PermissionAlert(Builder builder) {
            this.alertId = builder.alertId;
            this.userId = builder.userId;
            this.clientIp = builder.clientIp;
            this.operation = builder.operation;
            this.resource = builder.resource;
            this.alertType = builder.alertType;
            this.severity = builder.severity;
            this.message = builder.message;
            this.createTime = builder.createTime;
            this.status = builder.status;
            this.metadata = builder.metadata;
            this.resolvedTime = builder.resolvedTime;
            this.resolvedBy = builder.resolvedBy;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getAlertId() { return alertId; }
        public Long getUserId() { return userId; }
        public String getClientIp() { return clientIp; }
        public String getOperation() { return operation; }
        public String getResource() { return resource; }
        public String getAlertType() { return alertType; }
        public String getSeverity() { return severity; }
        public String getMessage() { return message; }
        public LocalDateTime getCreateTime() { return createTime; }
        public String getStatus() { return status; }
        public Map<String, Object> getMetadata() { return metadata; }
        public LocalDateTime getResolvedTime() { return resolvedTime; }
        public String getResolvedBy() { return resolvedBy; }

        // Setters
        public void setAlertId(String alertId) { this.alertId = alertId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public void setClientIp(String clientIp) { this.clientIp = clientIp; }
        public void setOperation(String operation) { this.operation = operation; }
        public void setResource(String resource) { this.resource = resource; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public void setSeverity(String severity) { this.severity = severity; }
        public void setMessage(String message) { this.message = message; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        public void setStatus(String status) { this.status = status; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        public void setResolvedTime(LocalDateTime resolvedTime) { this.resolvedTime = resolvedTime; }
        public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

        public static class Builder {
            private String alertId;
            private Long userId;
            private String clientIp;
            private String operation;
            private String resource;
            private String alertType;
            private String severity;
            private String message;
            private LocalDateTime createTime;
            private String status;
            private Map<String, Object> metadata;
            private LocalDateTime resolvedTime;
            private String resolvedBy;

            public Builder alertId(String alertId) { this.alertId = alertId; return this; }
            public Builder userId(Long userId) { this.userId = userId; return this; }
            public Builder clientIp(String clientIp) { this.clientIp = clientIp; return this; }
            public Builder operation(String operation) { this.operation = operation; return this; }
            public Builder resource(String resource) { this.resource = resource; return this; }
            public Builder alertType(String alertType) { this.alertType = alertType; return this; }
            public Builder severity(String severity) { this.severity = severity; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder createTime(LocalDateTime createTime) { this.createTime = createTime; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
            public Builder resolvedTime(LocalDateTime resolvedTime) { this.resolvedTime = resolvedTime; return this; }
            public Builder resolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; return this; }

            public PermissionAlert build() {
                return new PermissionAlert(this);
            }
        }
    }

    /**
     * 告警统计信息
     */
    public static class AlertStatistics {
        private Long totalAlerts = 0L;
        private Long activeAlerts = 0L;
        private Long userFailureAlerts = 0L;
        private Long ipAbnormalAlerts = 0L;
        private Long criticalAlerts = 0L;
        private Long highAlerts = 0L;
        private Long mediumAlerts = 0L;
        private Long lowAlerts = 0L;
        private LocalDateTime lastUpdateTime = LocalDateTime.now();

        // Getters
        public Long getTotalAlerts() { return totalAlerts; }
        public Long getActiveAlerts() { return activeAlerts; }
        public Long getUserFailureAlerts() { return userFailureAlerts; }
        public Long getIpAbnormalAlerts() { return ipAbnormalAlerts; }
        public Long getCriticalAlerts() { return criticalAlerts; }
        public Long getHighAlerts() { return highAlerts; }
        public Long getMediumAlerts() { return mediumAlerts; }
        public Long getLowAlerts() { return lowAlerts; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }

        // Setters
        public void setTotalAlerts(Long totalAlerts) { this.totalAlerts = totalAlerts; }
        public void setActiveAlerts(Long activeAlerts) { this.activeAlerts = activeAlerts; }
        public void setUserFailureAlerts(Long userFailureAlerts) { this.userFailureAlerts = userFailureAlerts; }
        public void setIpAbnormalAlerts(Long ipAbnormalAlerts) { this.ipAbnormalAlerts = ipAbnormalAlerts; }
        public void setCriticalAlerts(Long criticalAlerts) { this.criticalAlerts = criticalAlerts; }
        public void setHighAlerts(Long highAlerts) { this.highAlerts = highAlerts; }
        public void setMediumAlerts(Long mediumAlerts) { this.mediumAlerts = mediumAlerts; }
        public void setLowAlerts(Long lowAlerts) { this.lowAlerts = lowAlerts; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }
}
