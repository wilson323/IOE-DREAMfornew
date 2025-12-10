package net.lab1024.sa.common.audit.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置变更审计视图对象
 * <p>
 * 用于前端展示的配置变更审计信息，包含：
 * - 变更基本信息
 * - 操作信息
 * - 风险评估信息
 * - 审批信息
 * - 通知信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@Builder
@Schema(description = "配置变更审计视图对象")
public class ConfigChangeAuditVO {

    @Schema(description = "审计ID", example = "1234567890123456789")
    private Long auditId;

    @Schema(description = "变更批次ID", example = "BATCH_1701234567890")
    private String changeBatchId;

    @Schema(description = "变更类型", example = "UPDATE")
    private String changeType;

    @Schema(description = "变更类型显示名称", example = "更新")
    private String changeTypeDisplayName;

    @Schema(description = "配置类型", example = "SYSTEM_CONFIG")
    private String configType;

    @Schema(description = "配置类型显示名称", example = "系统配置")
    private String configTypeDisplayName;

    @Schema(description = "配置键", example = "system.app.name")
    private String configKey;

    @Schema(description = "配置名称", example = "应用名称")
    private String configName;

    @Schema(description = "配置分组", example = "system")
    private String configGroup;

    @Schema(description = "变更前值（JSON格式）")
    private String oldValue;

    @Schema(description = "变更后值（JSON格式）")
    private String newValue;

    @Schema(description = "变更字段列表（JSON格式）")
    private String changedFields;

    @Schema(description = "变更摘要", example = "更新系统配置 - 应用名称")
    private String changeSummary;

    @Schema(description = "变更原因", example = "修复配置错误")
    private String changeReason;

    @Schema(description = "变更来源", example = "WEB")
    private String changeSource;

    @Schema(description = "操作用户ID", example = "1001")
    private Long operatorId;

    @Schema(description = "操作用户名", example = "张三")
    private String operatorName;

    @Schema(description = "操作用户角色", example = "ADMIN")
    private String operatorRole;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "变更时间", example = "2024-01-01T10:00:00")
    private LocalDateTime changeTime;

    @Schema(description = "影响范围", example = "SYSTEM")
    private String impactScope;

    @Schema(description = "影响用户数", example = "20000")
    private Integer affectedUsers;

    @Schema(description = "影响设备数", example = "1000")
    private Integer affectedDevices;

    @Schema(description = "风险等级", example = "HIGH")
    private String riskLevel;

    @Schema(description = "风险等级显示名称", example = "高风险")
    private String riskLevelDisplayName;

    @Schema(description = "变更状态", example = "SUCCESS")
    private String changeStatus;

    @Schema(description = "变更状态显示名称", example = "执行成功")
    private String changeStatusDisplayName;

    @Schema(description = "执行时间（毫秒）", example = "1500")
    private Long executionTime;

    @Schema(description = "格式化执行时间", example = "1.50s")
    private String formattedExecutionTime;

    @Schema(description = "错误信息", example = "连接超时")
    private String errorMessage;

    @Schema(description = "是否需要审批", example = "1")
    private Integer requireApproval;

    @Schema(description = "审批人ID", example = "1002")
    private Long approverId;

    @Schema(description = "审批人姓名", example = "李四")
    private String approverName;

    @Schema(description = "审批时间", example = "2024-01-01T10:05:00")
    private LocalDateTime approvalTime;

    @Schema(description = "审批意见", example = "配置合理，同意变更")
    private String approvalComment;

    @Schema(description = "通知状态", example = "SENT")
    private String notificationStatus;

    @Schema(description = "通知时间", example = "2024-01-01T10:01:00")
    private LocalDateTime notificationTime;

    @Schema(description = "是否为高风险变更", example = "true")
    private Boolean highRisk;

    @Schema(description = "是否为敏感配置", example = "false")
    private Boolean sensitive;

    @Schema(description = "是否为批量操作", example = "false")
    private Boolean batchOperation;

    @Schema(description = "是否变更成功", example = "true")
    private Boolean successful;

    @Schema(description = "是否已回滚", example = "false")
    private Boolean rolledBack;

    @Schema(description = "时间差（相对当前时间）", example = "5分钟前")
    private String timeAgo;

    @Schema(description = "操作描述", example = "张三 在 5分钟前 更新了系统配置：应用名称")
    private String operationDescription;

    @Schema(description = "状态颜色", example = "success")
    private String statusColor;

    @Schema(description = "风险颜色", example = "error")
    private String riskColor;

    @Schema(description = "操作链接", example = "/audit/config-change/1234567890")
    private String detailUrl;

    @Schema(description = "审批链接", example = "/audit/config-change/1234567890/approve")
    private String approvalUrl;

    // ==================== 业务方法 ====================

    /**
     * 判断是否为高风险变更
     */
    public boolean isHighRisk() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel);
    }

    /**
     * 判断是否需要审批
     */
    public boolean needsApproval() {
        return requireApproval != null && requireApproval == 1;
    }

    /**
     * 判断是否为敏感配置
     */
    public boolean isSensitiveConfig() {
        return Boolean.TRUE.equals(sensitive);
    }

    /**
     * 判断是否为批量操作
     */
    public boolean isBatchOperation() {
        return Boolean.TRUE.equals(batchOperation);
    }

    /**
     * 判断是否变更成功
     */
    public boolean isChangeSuccessful() {
        return "SUCCESS".equals(changeStatus);
    }

    /**
     * 判断是否已回滚
     */
    public boolean isRolledBack() {
        return "ROLLED_BACK".equals(changeStatus);
    }

    /**
     * 获取状态颜色
     */
    public String getStatusColor() {
        return switch (changeStatus) {
            case "SUCCESS" -> "success";
            case "FAILED" -> "error";
            case "PENDING" -> "warning";
            case "EXECUTING" -> "processing";
            case "ROLLED_BACK" -> "default";
            default -> "default";
        };
    }

    /**
     * 获取风险颜色
     */
    public String getRiskColor() {
        return switch (riskLevel) {
            case "LOW" -> "success";
            case "MEDIUM" -> "warning";
            case "HIGH" -> "error";
            case "CRITICAL" -> "error";
            default -> "default";
        };
    }

    /**
     * 获取操作描述
     */
    public String getOperationDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(operatorName != null ? operatorName : "未知用户")
           .append(" 在 ")
           .append(getTimeAgo())
           .append(" ")
           .append(changeTypeDisplayName != null ? changeTypeDisplayName : "操作")
           .append("了")
           .append(configTypeDisplayName != null ? configTypeDisplayName : "配置");

        if (configName != null && !configName.trim().isEmpty()) {
            desc.append("：").append(configName);
        }

        if (isHighRisk()) {
            desc.append(" [").append(riskLevelDisplayName).append("]");
        }

        return desc.toString();
    }

    /**
     * 获取相对时间
     */
    public String getTimeAgo() {
        if (changeTime == null) {
            return "未知时间";
        }

        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(changeTime, now).getSeconds();

        if (seconds < 60) {
            return seconds + "秒前";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟前";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "小时前";
        } else if (seconds < 2592000) {
            return (seconds / 86400) + "天前";
        } else {
            return changeTime.toLocalDate().toString();
        }
    }

    /**
     * 获取详情URL
     */
    public String getDetailUrl() {
        return "/audit/config-change/" + auditId;
    }

    /**
     * 获取审批URL
     */
    public String getApprovalUrl() {
        return "/audit/config-change/" + auditId + "/approve";
    }

    /**
     * 判断是否可以审批
     */
    public boolean canApprove() {
        return needsApproval() && "PENDING".equals(changeStatus);
    }

    /**
     * 判断是否可以回滚
     */
    public boolean canRollback() {
        return isChangeSuccessful() && !"ROLLED_BACK".equals(changeStatus);
    }

    /**
     * 判断是否可以查看详情
     */
    public boolean canViewDetail() {
        return auditId != null;
    }
}
