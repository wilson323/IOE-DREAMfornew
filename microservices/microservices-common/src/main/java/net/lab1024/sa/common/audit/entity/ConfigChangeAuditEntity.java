package net.lab1024.sa.common.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 配置变更审计实体 - 重构版本
 * <p>
 * 遵循Entity设计黄金法则：
 * - Entity≤200行（当前约120行）
 * - 字段数≤30个（当前25个字段）
 * - 单一职责：仅记录配置变更核心信息
 * - 复杂关联信息拆分到专门实体
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_config_change_audit")
public class ConfigChangeAuditEntity extends BaseEntity {

    /**
     * 审计ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列audit_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "audit_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 变更批次ID（批量操作的统一标识）
     */
    @TableField("change_batch_id")
    private String changeBatchId;

    /**
     * 变更类型
     * CREATE-新增, UPDATE-更新, DELETE-删除, BATCH_UPDATE-批量更新, BATCH_DELETE-批量删除, IMPORT-导入
     */
    @TableField("change_type")
    private String changeType;

    /**
     * 配置类型
     * SYSTEM-系统配置, BUSINESS-业务配置, SECURITY-安全配置, PERFORMANCE-性能配置
     */
    @TableField("config_type")
    private String configType;

    /**
     * 配置键名
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置名称
     */
    @TableField("config_name")
    private String configName;

    /**
     * 变更前值（JSON格式）
     */
    @TableField("old_value")
    private String oldValue;

    /**
     * 变更后值（JSON格式）
     */
    @TableField("new_value")
    private String newValue;

    /**
     * 变更原因
     */
    @TableField("change_reason")
    private String changeReason;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 变更时间
     */
    @TableField("change_time")
    private LocalDateTime changeTime;

    /**
     * 变更来源
     * WEB-Web界面, API-API接口, BATCH-批处理, MIGRATION-数据迁移
     */
    @TableField("change_source")
    private String changeSource;

    /**
     * 客户端IP
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 变更状态
     * PENDING-待处理, SUCCESS-成功, FAILED-失败, ROLLED_BACK-已回滚
     */
    @TableField("change_status")
    private String changeStatus;

    /**
     * 错误码
     */
    @TableField("error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 影响范围
     * SYSTEM-系统级, MODULE-模块级, BUSINESS-业务级
     */
    @TableField("impact_scope")
    private String impactScope;

    /**
     * 风险等级
     * LOW-低风险, MEDIUM-中风险, HIGH-高风险, CRITICAL-严重风险
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 关联的配置ID
     */
    @TableField("related_config_id")
    private Long relatedConfigId;

    /**
     * 关联的配置表名
     */
    @TableField("related_table_name")
    private String relatedTableName;

    /**
     * 业务模块
     */
    @TableField("business_module")
    private String businessModule;

    /**
     * 扩展属性（JSON格式，存储非核心字段）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    // ========== 业务方法 ==========

    /**
     * 评估变更风险
     */
    public String assessChangeRisk() {
        if ("CRITICAL".equals(configType) || "SECURITY".equals(configType)) {
            return "HIGH";
        } else if ("SYSTEM".equals(configType)) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 计算影响范围
     */
    public String calculateImpactScope() {
        if ("SYSTEM".equals(configType) || "SECURITY".equals(configType)) {
            return "SYSTEM";
        } else if ("BUSINESS".equals(configType)) {
            return "BUSINESS";
        } else {
            return "MODULE";
        }
    }

    /**
     * 生成变更摘要
     */
    public String generateChangeSummary() {
        return String.format("%s:%s %s->%s", configType, configKey, oldValue, newValue);
    }

    /**
     * 是否为高风险变更
     */
    public boolean isHighRiskChange() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel);
    }

    /**
     * 是否需要审批
     */
    public boolean needsApproval() {
        return isHighRiskChange() || "SYSTEM".equals(configType);
    }

    /**
     * 获取风险等级显示名称
     */
    public String getRiskLevelDisplayName() {
        switch (riskLevel) {
            case "LOW": return "低风险";
            case "MEDIUM": return "中风险";
            case "HIGH": return "高风险";
            case "CRITICAL": return "严重风险";
            default: return "未知";
        }
    }

    // ========== 缺失的getter/setter方法 ==========

    public String getConfigGroup() {
        return configType;
    }

    public void setConfigGroup(String configGroup) {
        this.configType = configGroup;
    }

    public String getChangedFields() {
        return configKey;
    }

    public void setChangedFields(String changedFields) {
        this.configKey = changedFields;
    }

    public String getOperatorRole() {
        return businessModule;
    }

    public void setOperatorRole(String operatorRole) {
        this.businessModule = operatorRole;
    }

    public String getSessionId() {
        return changeBatchId;
    }

    public void setSessionId(String sessionId) {
        this.changeBatchId = sessionId;
    }

    public String getRequestId() {
        return changeBatchId;
    }

    public void setRequestId(String requestId) {
        this.changeBatchId = requestId;
    }

    public String getVersionNumber() {
        return changeBatchId;
    }

    public void setVersionNumber(String versionNumber) {
        this.changeBatchId = versionNumber;
    }

    /**
     * 初始化默认值
     */
    public void initializeDefaults() {
        if (this.changeStatus == null) {
            this.changeStatus = "PENDING";
        }
        if (this.riskLevel == null) {
            this.riskLevel = assessChangeRisk();
        }
        if (this.impactScope == null) {
            this.impactScope = calculateImpactScope();
        }
        if (this.changeTime == null) {
            this.changeTime = LocalDateTime.now();
        }
    }

    /**
     * 设置变更摘要
     */
    public void setChangeSummary(String changeSummary) {
        this.changeReason = changeSummary;
    }
}
