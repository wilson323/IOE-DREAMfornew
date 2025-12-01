package net.lab1024.sa.admin.module.consume.service;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.consume.dao.ConsumeAuditLogDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeAuditLogEntity;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartVerificationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消费审计日志服务
 * 负责记录所有消费相关操作的详细日志
 * 满足企业级安全合规要求
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */
@Slf4j
@Service
public class ConsumeAuditService {

    @Resource
    private ConsumeAuditLogDao consumeAuditLogDao;

    /**
     * 记录消费操作审计日志
     *
     * @param personId 人员ID
     * @param operation 操作类型
     * @param amount 操作金额
     * @param deviceId 设备ID
     * @param orderNo 订单号
     * @param result 操作结果
     * @param details 详细信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordConsumeAudit(Long personId, String operation, BigDecimal amount,
                                  Long deviceId, String orderNo, String result,
                                  Map<String, Object> details) {
        try {
            ConsumeAuditLogEntity auditLog = new ConsumeAuditLogEntity();
            auditLog.setPersonId(personId);
            auditLog.setOperationType(operation);
            auditLog.setOperationAmount(amount);
            auditLog.setDeviceId(deviceId);
            auditLog.setOrderNo(orderNo);
            auditLog.setOperationResult(result);
            auditLog.setOperationTime(LocalDateTime.now());
            auditLog.setIpAddress(getCurrentIpAddress());
            auditLog.setUserAgent(getCurrentUserAgent());

            // 转换详细信息为JSON
            if (details != null && !details.isEmpty()) {
                auditLog.setOperationDetails(SmartBeanUtil.toJson(details));
            }

            // 安全风险评估
            auditLog.setRiskLevel(assessRiskLevel(personId, operation, amount, details));
            auditLog.setNeedReview(assessNeedReview(auditLog.getRiskLevel()));

            consumeAuditLogDao.insert(auditLog);

            log.debug("消费审计日志记录成功: personId={}, operation={}, orderNo={}",
                     personId, operation, orderNo);

        } catch (Exception e) {
            log.error("记录消费审计日志失败: personId={}, operation={}", personId, operation, e);
            // 审计日志失败不应影响主业务流程
        }
    }

    /**
     * 记录权限验证审计
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordPermissionAudit(Long personId, String permission, String result,
                                     String reason, String orderNo) {
        try {
            ConsumeAuditLogEntity auditLog = new ConsumeAuditLogEntity();
            auditLog.setPersonId(personId);
            auditLog.setOperationType("PERMISSION_CHECK");
            auditLog.setOperationResult(result);
            auditLog.setOrderNo(orderNo);
            auditLog.setOperationTime(LocalDateTime.now());
            auditLog.setIpAddress(getCurrentIpAddress());

            // 权限验证详情
            Map<String, Object> details = Map.of(
                "permission", permission,
                "reason", reason != null ? reason : "N/A"
            );
            auditLog.setOperationDetails(SmartBeanUtil.toJson(details));

            auditLog.setRiskLevel("LOW");
            auditLog.setNeedReview(false);

            consumeAuditLogDao.insert(auditLog);

        } catch (Exception e) {
            log.error("记录权限审计日志失败: personId={}, permission={}", personId, permission, e);
        }
    }

    /**
     * 记录安全事件审计
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordSecurityAudit(Long personId, String securityEvent, String severity,
                                   String description, String orderNo) {
        try {
            ConsumeAuditLogEntity auditLog = new ConsumeAuditLogEntity();
            auditLog.setPersonId(personId);
            auditLog.setOperationType("SECURITY_EVENT");
            auditLog.setOperationResult(severity);
            auditLog.setOrderNo(orderNo);
            auditLog.setOperationTime(LocalDateTime.now());
            auditLog.setIpAddress(getCurrentIpAddress());

            // 安全事件详情
            Map<String, Object> details = Map.of(
                "securityEvent", securityEvent,
                "description", description
            );
            auditLog.setOperationDetails(SmartBeanUtil.toJson(details));

            // 安全事件风险等级较高
            auditLog.setRiskLevel(mapSeverityToRiskLevel(severity));
            auditLog.setNeedReview("HIGH".equals(severity) || "CRITICAL".equals(severity));

            consumeAuditLogDao.insert(auditLog);

            // 高风险安全事件立即告警
            if (auditLog.getNeedReview()) {
                triggerSecurityAlert(personId, securityEvent, description);
            }

        } catch (Exception e) {
            log.error("记录安全审计日志失败: personId={}, event={}", personId, securityEvent, e);
        }
    }

    /**
     * 查询审计日志
     */
    public List<ConsumeAuditLogEntity> queryAuditLogs(Long personId, String operationType,
                                                     LocalDateTime startTime, LocalDateTime endTime,
                                                     Integer limit) {
        try {
            return consumeAuditLogDao.queryAuditLogs(personId, operationType, startTime, endTime, limit);
        } catch (Exception e) {
            log.error("查询审计日志失败: personId={}, operation={}", personId, operationType, e);
            return List.of();
        }
    }

    /**
     * 获取高风险操作列表
     */
    public List<ConsumeAuditLogEntity> getHighRiskOperations(LocalDateTime startTime,
                                                           LocalDateTime endTime) {
        try {
            return consumeAuditLogDao.selectHighRiskOperations(startTime, endTime);
        } catch (Exception e) {
            log.error("获取高风险操作失败", e);
            return List.of();
        }
    }

    /**
     * 评估风险等级
     */
    private String assessRiskLevel(Long personId, String operation, BigDecimal amount,
                                  Map<String, Object> details) {
        // 高金额操作
        if (amount != null && amount.compareTo(new BigDecimal("1000")) >= 0) {
            return "HIGH";
        }

        // 敏感操作
        if ("ACCOUNT_FREEZE".equals(operation) ||
            "ACCOUNT_UNFREEZE".equals(operation) ||
            "PASSWORD_RESET".equals(operation)) {
            return "HIGH";
        }

        // 异常时间操作（非工作时间）
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        if (hour < 6 || hour > 22) {
            return "MEDIUM";
        }

        return "LOW";
    }

    /**
     * 评估是否需要人工审核
     */
    private boolean assessNeedReview(String riskLevel) {
        return "HIGH".equals(riskLevel);
    }

    /**
     * 获取当前IP地址
     */
    private String getCurrentIpAddress() {
        try {
            // 从请求上下文获取IP地址
            // 这里简化实现，实际应该从RequestContextHolder获取
            return "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * 获取当前用户代理
     */
    private String getCurrentUserAgent() {
        try {
            // 从请求上下文获取User-Agent
            // 这里简化实现，实际应该从RequestContextHolder获取
            return "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * 映射严重程度到风险等级
     */
    private String mapSeverityToRiskLevel(String severity) {
        switch (severity) {
            case "CRITICAL":
                return "CRITICAL";
            case "HIGH":
                return "HIGH";
            case "MEDIUM":
                return "MEDIUM";
            default:
                return "LOW";
        }
    }

    /**
     * 触发安全告警
     */
    private void triggerSecurityAlert(Long personId, String securityEvent, String description) {
        try {
            // 这里应该集成告警系统
            log.warn("安全告警触发: personId={}, event={}, description={}",
                    personId, securityEvent, description);

            // 发送邮件/短信/钉钉告警
            // ...

        } catch (Exception e) {
            log.error("触发安全告警失败: personId={}, event={}", personId, securityEvent, e);
        }
    }
}