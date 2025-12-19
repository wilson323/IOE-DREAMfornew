package net.lab1024.sa.common.permission.manager;

import net.lab1024.sa.common.permission.domain.dto.PermissionValidationResult;
import net.lab1024.sa.common.permission.domain.enums.PermissionCondition;
import net.lab1024.sa.common.permission.domain.enums.LogicOperator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限审计管理器
 * <p>
 * 企业级权限验证审计和监控组件，提供：
 * - 详细的权限验证日志记录
 * - 权限验证行为分析
 * - 安全事件检测和告警
 * - 审计报告生成
 * - 合规性检查
 * - 权限使用统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface PermissionAuditManager {

    /**
     * 记录权限验证审计
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @param resource 资源标识
     * @param result 验证结果
     */
    void recordPermissionValidation(Long userId, String permission, String resource, PermissionValidationResult result);

    /**
     * 记录角色验证审计
     *
     * @param userId 用户ID
     * @param role 角色标识
     * @param resource 资源标识
     * @param result 验证结果
     */
    void recordRoleValidation(Long userId, String role, String resource, PermissionValidationResult result);

    /**
     * 记录数据权限验证审计
     *
     * @param userId 用户ID
     * @param dataType 数据类型
     * @param resourceId 资源ID
     * @param result 验证结果
     */
    void recordDataScopeValidation(Long userId, String dataType, Object resourceId, PermissionValidationResult result);

    /**
     * 记录复合权限验证审计
     *
     * @param userId 用户ID
     * @param conditions 权限条件列表
     * @param logicOperator 逻辑操作符
     * @param result 验证结果
     */
    void recordCompositeValidation(Long userId, PermissionCondition[] conditions, LogicOperator logicOperator, PermissionValidationResult result);

    /**
     * 记录区域权限验证审计
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param permission 权限标识
     * @param result 验证结果
     */
    void recordAreaValidation(Long userId, Long areaId, String permission, PermissionValidationResult result);

    /**
     * 记录设备权限验证审计
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param permission 权限标识
     * @param result 验证结果
     */
    void recordDeviceValidation(Long userId, String deviceId, String permission, PermissionValidationResult result);

    /**
     * 记录权限变更审计
     *
     * @param userId 用户ID
     * @param changeType 变更类型
     * @param oldPermissions 原权限列表
     * @param newPermissions 新权限列表
     * @param operator 操作人
     */
    void recordPermissionChange(Long userId, String changeType, Set<String> oldPermissions, Set<String> newPermissions, Long operator);

    /**
     * 记录角色变更审计
     *
     * @param userId 用户ID
     * @param changeType 变更类型
     * @param oldRoles 原角色列表
     * @param newRoles 新角色列表
     * @param operator 操作人
     */
    void recordRoleChange(Long userId, String changeType, Set<String> oldRoles, Set<String> newRoles, Long operator);

    /**
     * 记录安全事件
     *
     * @param eventType 事件类型
     * @param severity 严重级别
     * @param userId 用户ID
     * @param description 事件描述
     * @param details 事件详情
     */
    void recordSecurityEvent(String eventType, String severity, Long userId, String description, Map<String, Object> details);

    /**
     * 查询用户权限验证历史
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 查询限制
     * @return 审计记录列表
     */
    List<PermissionAuditRecord> getUserPermissionHistory(Long userId, LocalDateTime startTime, LocalDateTime endTime, int limit);

    /**
     * 查询权限验证历史
     *
     * @param permission 权限标识
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 查询限制
     * @return 审计记录列表
     */
    List<PermissionAuditRecord> getPermissionValidationHistory(String permission, LocalDateTime startTime, LocalDateTime endTime, int limit);

    /**
     * 查询安全事件
     *
     * @param eventType 事件类型
     * @param severity 严重级别
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 查询限制
     * @return 安全事件列表
     */
    List<SecurityEvent> getSecurityEvents(String eventType, String severity, LocalDateTime startTime, LocalDateTime endTime, int limit);

    /**
     * 生成权限使用统计报告
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 权限使用统计报告
     */
    PermissionUsageReport generatePermissionUsageReport(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成安全事件报告
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 安全事件报告
     */
    SecurityEventReport generateSecurityEventReport(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成合规性报告
     *
     * @param complianceRules 合规性规则
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 合规性报告
     */
    ComplianceReport generateComplianceReport(Set<String> complianceRules, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检测异常权限使用模式
     *
     * @param userId 用户ID
     * @param timeWindow 时间窗口（小时）
     * @return 异常检测结果
     */
    AnomalyDetectionResult detectAnomalousPermissionUsage(Long userId, int timeWindow);

    /**
     * 检测权限滥用
     *
     * @param timeWindow 时间窗口（小时）
     * @param threshold 阈值
     * @return 权限滥用检测结果
     */
    List<PermissionAbuseResult> detectPermissionAbuse(int timeWindow, double threshold);

    /**
     * 清理过期审计数据
     *
     * @param retentionDays 保留天数
     */
    void cleanupExpiredAuditData(int retentionDays);

    /**
     * 获取审计统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计统计信息
     */
    AuditStats getAuditStats(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 权限审计记录
     */
    class PermissionAuditRecord {
        private final String auditId;
        private final LocalDateTime timestamp;
        private final Long userId;
        private final String validationType;
        private final String permission;
        private final String resource;
        private final boolean valid;
        private final Integer statusCode;
        private final String message;
        private final Long duration;
        private final String clientIp;
        private final String userAgent;
        private final String sessionId;
        private final Map<String, Object> metadata;

        public PermissionAuditRecord(String auditId, LocalDateTime timestamp, Long userId, String validationType,
                                   String permission, String resource, boolean valid, Integer statusCode,
                                   String message, Long duration, String clientIp, String userAgent,
                                   String sessionId, Map<String, Object> metadata) {
            this.auditId = auditId;
            this.timestamp = timestamp;
            this.userId = userId;
            this.validationType = validationType;
            this.permission = permission;
            this.resource = resource;
            this.valid = valid;
            this.statusCode = statusCode;
            this.message = message;
            this.duration = duration;
            this.clientIp = clientIp;
            this.userAgent = userAgent;
            this.sessionId = sessionId;
            this.metadata = metadata;
        }

        // Getters
        public String getAuditId() { return auditId; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Long getUserId() { return userId; }
        public String getValidationType() { return validationType; }
        public String getPermission() { return permission; }
        public String getResource() { return resource; }
        public boolean isValid() { return valid; }
        public Integer getStatusCode() { return statusCode; }
        public String getMessage() { return message; }
        public Long getDuration() { return duration; }
        public String getClientIp() { return clientIp; }
        public String getUserAgent() { return userAgent; }
        public String getSessionId() { return sessionId; }
        public Map<String, Object> getMetadata() { return metadata; }
    }

    /**
     * 安全事件
     */
    class SecurityEvent {
        private final String eventId;
        private final LocalDateTime timestamp;
        private final String eventType;
        private final String severity;
        private final Long userId;
        private final String description;
        private final Map<String, Object> details;
        private final boolean resolved;
        private final LocalDateTime resolvedAt;
        private final Long resolvedBy;

        public SecurityEvent(String eventId, LocalDateTime timestamp, String eventType, String severity,
                           Long userId, String description, Map<String, Object> details,
                           boolean resolved, LocalDateTime resolvedAt, Long resolvedBy) {
            this.eventId = eventId;
            this.timestamp = timestamp;
            this.eventType = eventType;
            this.severity = severity;
            this.userId = userId;
            this.description = description;
            this.details = details;
            this.resolved = resolved;
            this.resolvedAt = resolvedAt;
            this.resolvedBy = resolvedBy;
        }

        // Getters
        public String getEventId() { return eventId; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getEventType() { return eventType; }
        public String getSeverity() { return severity; }
        public Long getUserId() { return userId; }
        public String getDescription() { return description; }
        public Map<String, Object> getDetails() { return details; }
        public boolean isResolved() { return resolved; }
        public LocalDateTime getResolvedAt() { return resolvedAt; }
        public Long getResolvedBy() { return resolvedBy; }
    }

    /**
     * 权限使用统计报告
     */
    class PermissionUsageReport {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final Map<String, Long> permissionUsageCount;
        private final Map<String, Long> roleUsageCount;
        private final Map<String, Long> resourceUsageCount;
        private final Map<Long, Long> userActivityCount;
        private final long totalValidations;
        private final long successCount;
        private final long failureCount;
        private final double successRate;

        public PermissionUsageReport(LocalDateTime startTime, LocalDateTime endTime,
                                    Map<String, Long> permissionUsageCount, Map<String, Long> roleUsageCount,
                                    Map<String, Long> resourceUsageCount, Map<Long, Long> userActivityCount,
                                    long totalValidations, long successCount, long failureCount, double successRate) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.permissionUsageCount = permissionUsageCount;
            this.roleUsageCount = roleUsageCount;
            this.resourceUsageCount = resourceUsageCount;
            this.userActivityCount = userActivityCount;
            this.totalValidations = totalValidations;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.successRate = successRate;
        }

        // Getters
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public Map<String, Long> getPermissionUsageCount() { return permissionUsageCount; }
        public Map<String, Long> getRoleUsageCount() { return roleUsageCount; }
        public Map<String, Long> getResourceUsageCount() { return resourceUsageCount; }
        public Map<Long, Long> getUserActivityCount() { return userActivityCount; }
        public long getTotalValidations() { return totalValidations; }
        public long getSuccessCount() { return successCount; }
        public long getFailureCount() { return failureCount; }
        public double getSuccessRate() { return successRate; }
    }

    /**
     * 安全事件报告
     */
    class SecurityEventReport {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final Map<String, Long> eventTypeCount;
        private final Map<String, Long> severityCount;
        private final List<SecurityEvent> criticalEvents;
        private final List<SecurityEvent> unresolvedEvents;
        private final long totalEvents;
        private final long resolvedEvents;
        private final long unresolvedEventsCount;

        public SecurityEventReport(LocalDateTime startTime, LocalDateTime endTime,
                                 Map<String, Long> eventTypeCount, Map<String, Long> severityCount,
                                 List<SecurityEvent> criticalEvents, List<SecurityEvent> unresolvedEvents,
                                 long totalEvents, long resolvedEvents, long unresolvedEventsCount) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.eventTypeCount = eventTypeCount;
            this.severityCount = severityCount;
            this.criticalEvents = criticalEvents;
            this.unresolvedEvents = unresolvedEvents;
            this.totalEvents = totalEvents;
            this.resolvedEvents = resolvedEvents;
            this.unresolvedEventsCount = unresolvedEventsCount;
        }

        // Getters
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public Map<String, Long> getEventTypeCount() { return eventTypeCount; }
        public Map<String, Long> getSeverityCount() { return severityCount; }
        public List<SecurityEvent> getCriticalEvents() { return criticalEvents; }
        public List<SecurityEvent> getUnresolvedEvents() { return unresolvedEvents; }
        public long getTotalEvents() { return totalEvents; }
        public long getResolvedEvents() { return resolvedEvents; }
        public long getUnresolvedEventsCount() { return unresolvedEventsCount; }
    }

    /**
     * 合规性报告
     */
    class ComplianceReport {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final Map<String, Boolean> ruleCompliance;
        private final List<String> violations;
        private final double complianceScore;
        private final boolean overallCompliance;

        public ComplianceReport(LocalDateTime startTime, LocalDateTime endTime,
                              Map<String, Boolean> ruleCompliance, List<String> violations,
                              double complianceScore, boolean overallCompliance) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.ruleCompliance = ruleCompliance;
            this.violations = violations;
            this.complianceScore = complianceScore;
            this.overallCompliance = overallCompliance;
        }

        // Getters
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public Map<String, Boolean> getRuleCompliance() { return ruleCompliance; }
        public List<String> getViolations() { return violations; }
        public double getComplianceScore() { return complianceScore; }
        public boolean isOverallCompliance() { return overallCompliance; }
    }

    /**
     * 异常检测结果
     */
    class AnomalyDetectionResult {
        private final Long userId;
        private final LocalDateTime detectionTime;
        private final boolean hasAnomalies;
        private final List<Anomaly> anomalies;
        private final double anomalyScore;

        public AnomalyDetectionResult(Long userId, LocalDateTime detectionTime, boolean hasAnomalies,
                                    List<Anomaly> anomalies, double anomalyScore) {
            this.userId = userId;
            this.detectionTime = detectionTime;
            this.hasAnomalies = hasAnomalies;
            this.anomalies = anomalies;
            this.anomalyScore = anomalyScore;
        }

        // Getters
        public Long getUserId() { return userId; }
        public LocalDateTime getDetectionTime() { return detectionTime; }
        public boolean hasAnomalies() { return hasAnomalies; }
        public List<Anomaly> getAnomalies() { return anomalies; }
        public double getAnomalyScore() { return anomalyScore; }
    }

    /**
     * 异常
     */
    class Anomaly {
        private final String anomalyType;
        private final String description;
        private final double score;
        private final Map<String, Object> details;

        public Anomaly(String anomalyType, String description, double score, Map<String, Object> details) {
            this.anomalyType = anomalyType;
            this.description = description;
            this.score = score;
            this.details = details;
        }

        // Getters
        public String getAnomalyType() { return anomalyType; }
        public String getDescription() { return description; }
        public double getScore() { return score; }
        public Map<String, Object> getDetails() { return details; }
    }

    /**
     * 权限滥用检测结果
     */
    class PermissionAbuseResult {
        private final String permission;
        private final Long userId;
        private final LocalDateTime detectionTime;
        private final int usageCount;
        private final double abuseScore;
        private final List<Anomaly> anomalies;

        public PermissionAbuseResult(String permission, Long userId, LocalDateTime detectionTime,
                                   int usageCount, double abuseScore, List<Anomaly> anomalies) {
            this.permission = permission;
            this.userId = userId;
            this.detectionTime = detectionTime;
            this.usageCount = usageCount;
            this.abuseScore = abuseScore;
            this.anomalies = anomalies;
        }

        // Getters
        public String getPermission() { return permission; }
        public Long getUserId() { return userId; }
        public LocalDateTime getDetectionTime() { return detectionTime; }
        public int getUsageCount() { return usageCount; }
        public double getAbuseScore() { return abuseScore; }
        public List<Anomaly> getAnomalies() { return anomalies; }
    }

    /**
     * 审计统计信息
     */
    class AuditStats {
        private final long totalAuditRecords;
        private final long permissionValidations;
        private final long roleValidations;
        private final long dataScopeValidations;
        private final long securityEvents;
        private final long resolvedSecurityEvents;
        private final Map<String, Long> eventTypeDistribution;
        private final Map<String, Long> severityDistribution;

        public AuditStats(long totalAuditRecords, long permissionValidations, long roleValidations,
                          long dataScopeValidations, long securityEvents, long resolvedSecurityEvents,
                          Map<String, Long> eventTypeDistribution, Map<String, Long> severityDistribution) {
            this.totalAuditRecords = totalAuditRecords;
            this.permissionValidations = permissionValidations;
            this.roleValidations = roleValidations;
            this.dataScopeValidations = dataScopeValidations;
            this.securityEvents = securityEvents;
            this.resolvedSecurityEvents = resolvedSecurityEvents;
            this.eventTypeDistribution = eventTypeDistribution;
            this.severityDistribution = severityDistribution;
        }

        // Getters
        public long getTotalAuditRecords() { return totalAuditRecords; }
        public long getPermissionValidations() { return permissionValidations; }
        public long getRoleValidations() { return roleValidations; }
        public long getDataScopeValidations() { return dataScopeValidations; }
        public long getSecurityEvents() { return securityEvents; }
        public long getResolvedSecurityEvents() { return resolvedSecurityEvents; }
        public Map<String, Long> getEventTypeDistribution() { return eventTypeDistribution; }
        public Map<String, Long> getSeverityDistribution() { return severityDistribution; }
    }
}