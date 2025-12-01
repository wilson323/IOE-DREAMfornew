package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 账户冻结历史VO
 * 严格遵循repowiki规范：VO类用于数据传输，记录账户冻结和解冻的历史记录
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账户冻结历史")
public class AccountFreezeHistory {

    /**
     * 记录ID
     */
    @Schema(description = "记录ID", example = "1001")
    private Long recordId;

    /**
     * 账户ID
     */
    @Schema(description = "账户ID", example = "2001")
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 冻结时间
     */
    @Schema(description = "冻结时间", example = "2025-01-17T10:30:00")
    @NotNull(message = "操作时间不能为空")
    private LocalDateTime freezeTime;

    /**
     * 解冻时间
     */
    @Schema(description = "解冻时间", example = "2025-01-17T11:30:00")
    private LocalDateTime unfreezeTime;

    /**
     * 冻结原因
     */
    @Schema(description = "冻结原因", example = "异常消费行为")
    @NotNull(message = "冻结原因不能为空")
    @Size(max = 200, message = "冻结原因长度不能超过200个字符")
    private String freezeReason;

    /**
     * 操作人员ID
     */
    @Schema(description = "操作人员ID", example = "3001")
    private Long operatorId;

    /**
     * 操作类型：1-冻结，2-解冻
     */
    @Schema(description = "操作类型", example = "1", allowableValues = {"1", "2"})
    @NotNull(message = "操作类型不能为空")
    private Integer operationType;

    /**
     * 操作描述
     */
    @Schema(description = "操作描述", example = "系统自动冻结")
    private String operationDescription;

    /**
     * 风险等级
     */
    @Schema(description = "风险等级", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private String riskLevel;

    /**
     * 关联的交易订单号
     */
    @Schema(description = "关联的交易订单号", example = "TXN20250117001")
    private String relatedOrderNo;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "联系用户确认后可解冻")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remarks;

    /**
     * 客户端IP
     */
    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    /**
     * 设备信息
     */
    @Schema(description = "设备信息", example = "iPhone 14 Pro")
    private String deviceInfo;

    /**
     * 是否自动操作
     */
    @Schema(description = "是否自动操作", example = "true")
    private Boolean isAutomatic;

    /**
     * 创建冻结操作记录
     */
    public static AccountFreezeHistory freeze(Long accountId, Long userId, String freezeReason, Long operatorId) {
        return AccountFreezeHistory.builder()
                .accountId(accountId)
                .userId(userId)
                .freezeReason(freezeReason)
                .operatorId(operatorId)
                .operationType(1)
                .freezeTime(LocalDateTime.now())
                .operationDescription("账户冻结")
                .isAutomatic(false)
                .build();
    }

    /**
     * 创建解冻操作记录
     */
    public static AccountFreezeHistory unfreeze(Long accountId, Long userId, String unfreezeReason, Long operatorId) {
        return AccountFreezeHistory.builder()
                .accountId(accountId)
                .userId(userId)
                .freezeReason(unfreezeReason)
                .operatorId(operatorId)
                .operationType(2)
                .freezeTime(LocalDateTime.now())
                .operationDescription("账户解冻")
                .isAutomatic(false)
                .build();
    }

    /**
     * 创建系统自动冻结记录
     */
    public static AccountFreezeHistory autoFreeze(Long accountId, Long userId, String freezeReason, String riskLevel) {
        return AccountFreezeHistory.builder()
                .accountId(accountId)
                .userId(userId)
                .freezeReason(freezeReason)
                .operatorId(null)
                .operationType(1)
                .freezeTime(LocalDateTime.now())
                .operationDescription("系统自动冻结")
                .riskLevel(riskLevel)
                .isAutomatic(true)
                .build();
    }

    /**
     * 是否为冻结操作
     */
    public boolean isFreezeOperation() {
        return Integer.valueOf(1).equals(operationType);
    }

    /**
     * 是否为解冻操作
     */
    public boolean isUnfreezeOperation() {
        return Integer.valueOf(2).equals(operationType);
    }

    /**
     * 是否为自动操作
     */
    public boolean isAutomaticOperation() {
        return Boolean.TRUE.equals(isAutomatic);
    }

    /**
     * 获取操作类型描述
     */
    public String getOperationTypeDescription() {
        if (isFreezeOperation()) {
            return "冻结";
        } else if (isUnfreezeOperation()) {
            return "解冻";
        } else {
            return "未知操作";
        }
    }

    /**
     * 获取风险等级描述
     */
    public String getRiskLevelDescription() {
        if (riskLevel == null) {
            return "未知";
        }
        switch (riskLevel) {
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            case "CRITICAL":
                return "严重风险";
            default:
                return riskLevel;
        }
    }

    /**
     * 检查是否为今天发生的操作
     */
    public boolean isToday() {
        return freezeTime != null && freezeTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    /**
     * 检查是否已解冻（对于冻结记录）
     */
    public boolean isUnfrozen() {
        return isFreezeOperation() && unfreezeTime != null;
    }

    /**
     * 获取操作持续时间（分钟）
     */
    public Long getOperationDurationMinutes() {
        if (isFreezeOperation() && unfreezeTime != null && freezeTime != null) {
            return java.time.Duration.between(freezeTime, unfreezeTime).toMinutes();
        }
        return null;
    }

    /**
     * 获取简化的显示信息
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(freezeTime != null ? freezeTime.toString() : "未知时间");
        summary.append(" | ");
        summary.append(getOperationTypeDescription());
        summary.append(" | ");
        summary.append(freezeReason != null ? freezeReason : "无原因");

        if (operatorId != null) {
            summary.append(" | 操作人:");
            summary.append(operatorId);
        }

        if (riskLevel != null) {
            summary.append(" | 风险:");
            summary.append(getRiskLevelDescription());
        }

        return summary.toString();
    }
}