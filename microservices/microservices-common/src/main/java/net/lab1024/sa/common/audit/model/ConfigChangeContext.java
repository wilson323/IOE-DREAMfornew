package net.lab1024.sa.common.audit.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 配置变更上下文
 * <p>
 * 用于封装配置变更的完整上下文信息，包含：
 * - 变更基本信息（类型、键、值等）
 * - 操作信息（操作人、时间、来源等）
 * - 业务信息（模块、环境、版本等）
 * - 扩展信息（标签、属性等）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigChangeContext {

    /**
     * 变更批次ID（用于批量操作）
     */
    private String changeBatchId;

    /**
     * 变更类型
     */
    private String changeType;

    /**
     * 配置类型
     */
    private String configType;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置分组
     */
    private String configGroup;

    /**
     * 变更前值
     */
    private Object oldValue;

    /**
     * 变更后值
     */
    private Object newValue;

    /**
     * 变更字段列表
     */
    private List<String> changedFields;

    /**
     * 变更摘要
     */
    private String changeSummary;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 变更来源
     */
    private String changeSource;

    /**
     * 操作用户ID
     */
    private Long operatorId;

    /**
     * 操作用户名
     */
    private String operatorName;

    /**
     * 操作用户角色
     */
    private String operatorRole;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 用户代理信息
     */
    private String userAgent;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 请求ID（用于链路追踪）
     */
    private String requestId;

    /**
     * 影响范围
     */
    private String impactScope;

    /**
     * 影响用户数
     */
    private Integer affectedUsers;

    /**
     * 影响设备数
     */
    private Integer affectedDevices;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 是否需要审批
     */
    private Boolean requireApproval;

    /**
     * 关联的配置ID
     */
    private Long relatedConfigId;

    /**
     * 关联的配置表名
     */
    private String relatedTableName;

    /**
     * 变更环境
     */
    private String changeEnvironment;

    /**
     * 业务模块
     */
    private String businessModule;

    /**
     * 变更标签
     */
    private List<String> changeTags;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 版本号
     */
    private String versionNumber;

    /**
     * 是否为敏感配置
     */
    private Boolean isSensitive;

    /**
     * 预计执行时间（毫秒）
     */
    private Long estimatedExecutionTime;

    /**
     * 回滚信息
     */
    private Object rollbackInfo;

    // ==================== 静态工厂方法 ====================

    /**
     * 创建新增配置的上下文
     */
    public static ConfigChangeContext createAdd(String configType, String configKey, String configName,
                                               Object newValue, Long operatorId, String operatorName) {
        return ConfigChangeContext.builder()
                .changeType("CREATE")
                .configType(configType)
                .configKey(configKey)
                .configName(configName)
                .newValue(newValue)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .changeSource("WEB")
                .build();
    }

    /**
     * 创建更新配置的上下文
     */
    public static ConfigChangeContext createUpdate(String configType, String configKey, String configName,
                                                  Object oldValue, Object newValue, List<String> changedFields,
                                                  Long operatorId, String operatorName) {
        return ConfigChangeContext.builder()
                .changeType("UPDATE")
                .configType(configType)
                .configKey(configKey)
                .configName(configName)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedFields(changedFields)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .changeSource("WEB")
                .build();
    }

    /**
     * 创建删除配置的上下文
     */
    public static ConfigChangeContext createDelete(String configType, String configKey, String configName,
                                                  Object oldValue, Long operatorId, String operatorName) {
        return ConfigChangeContext.builder()
                .changeType("DELETE")
                .configType(configType)
                .configKey(configKey)
                .configName(configName)
                .oldValue(oldValue)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .changeSource("WEB")
                .build();
    }

    /**
     * 创建批量操作的上下文
     */
    public static ConfigChangeContext createBatch(String batchId, String changeType, String configType,
                                                 int configCount, Long operatorId, String operatorName) {
        return ConfigChangeContext.builder()
                .changeBatchId(batchId)
                .changeType(changeType)
                .configType(configType)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .changeSource("BATCH")
                .affectedUsers(configCount)
                .build();
    }

    /**
     * 创建数据导入的上下文
     */
    public static ConfigChangeContext createImport(String batchId, String configType,
                                                  int importCount, Long operatorId, String operatorName) {
        return ConfigChangeContext.builder()
                .changeBatchId(batchId)
                .changeType("IMPORT")
                .configType(configType)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .changeSource("IMPORT")
                .affectedUsers(importCount)
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否为高风险变更
     */
    public boolean isHighRiskChange() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel);
    }

    /**
     * 判断是否需要审批
     */
    public boolean needsApproval() {
        return Boolean.TRUE.equals(requireApproval);
    }

    /**
     * 判断是否为敏感配置
     */
    public boolean isSensitiveConfig() {
        return Boolean.TRUE.equals(isSensitive);
    }

    /**
     * 判断是否为批量操作
     */
    public boolean isBatchOperation() {
        return changeBatchId != null && !changeBatchId.trim().isEmpty();
    }

    /**
     * 获取变更摘要
     */
    public String getChangeSummary() {
        if (changeSummary != null && !changeSummary.trim().isEmpty()) {
            return changeSummary;
        }

        StringBuilder summary = new StringBuilder();
        summary.append(getChangeTypeDisplayName())
               .append(getConfigTypeDisplayName());

        if (configName != null && !configName.trim().isEmpty()) {
            summary.append(" - ").append(configName);
        }

        if (isHighRiskChange()) {
            summary.append(" [").append(getRiskLevelDisplayName()).append("]");
        }

        return summary.toString();
    }

    /**
     * 获取变更类型显示名称
     */
    public String getChangeTypeDisplayName() {
        return switch (changeType) {
            case "CREATE" -> "新增";
            case "UPDATE" -> "更新";
            case "DELETE" -> "删除";
            case "BATCH_UPDATE" -> "批量更新";
            case "BATCH_DELETE" -> "批量删除";
            case "IMPORT" -> "数据导入";
            default -> changeType;
        };
    }

    /**
     * 获取配置类型显示名称
     */
    public String getConfigTypeDisplayName() {
        return switch (configType) {
            case "SYSTEM_CONFIG" -> "系统配置";
            case "USER_THEME" -> "用户主题";
            case "USER_PREFERENCE" -> "用户偏好";
            case "I18N_RESOURCE" -> "国际化资源";
            case "THEME_TEMPLATE" -> "主题模板";
            case "WORKFLOW_CONFIG" -> "工作流配置";
            default -> configType;
        };
    }

    /**
     * 获取风险等级显示名称
     */
    public String getRiskLevelDisplayName() {
        return switch (riskLevel) {
            case "LOW" -> "低风险";
            case "MEDIUM" -> "中风险";
            case "HIGH" -> "高风险";
            case "CRITICAL" -> "严重风险";
            default -> "未知";
        };
    }

    /**
     * 设置为高风险变更
     */
    public void markAsHighRisk(String reason) {
        this.riskLevel = "HIGH";
        this.requireApproval = true;
        this.changeReason = (changeReason == null ? "" : changeReason + "; ") + "高风险原因: " + reason;
    }

    /**
     * 设置为敏感配置
     */
    public void markAsSensitive() {
        this.isSensitive = true;
        this.requireApproval = true;
        if (riskLevel == null || "LOW".equals(riskLevel)) {
            this.riskLevel = "MEDIUM";
        }
    }

    /**
     * 设置扩展属性
     */
    public void setExtendedAttribute(String key, Object value) {
        if (extendedAttributes == null) {
            extendedAttributes = new java.util.HashMap<>();
        }
        extendedAttributes.put(key, value);
    }

    /**
     * 获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtendedAttribute(String key, Class<T> type) {
        if (extendedAttributes == null || !extendedAttributes.containsKey(key)) {
            return null;
        }
        Object value = extendedAttributes.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 添加变更标签
     */
    public void addChangeTag(String tag) {
        if (changeTags == null) {
            changeTags = new java.util.ArrayList<>();
        }
        if (!changeTags.contains(tag)) {
            changeTags.add(tag);
        }
    }

    /**
     * 设置客户端信息
     */
    public void setClientInfo(String clientIp, String userAgent) {
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }

    /**
     * 设置请求上下文信息
     */
    public void setRequestContext(String sessionId, String requestId) {
        this.sessionId = sessionId;
        this.requestId = requestId;
    }

    /**
     * 设置影响范围
     */
    public void setImpact(String scope, Integer affectedUsers, Integer affectedDevices) {
        this.impactScope = scope;
        this.affectedUsers = affectedUsers;
        this.affectedDevices = affectedDevices;
    }

    /**
     * 验证上下文数据完整性
     */
    public boolean validateContext() {
        if (changeType == null || changeType.trim().isEmpty()) {
            return false;
        }
        if (configType == null || configType.trim().isEmpty()) {
            return false;
        }
        if (configKey == null || configKey.trim().isEmpty()) {
            return false;
        }
        if (operatorId == null) {
            return false;
        }
        if (operatorName == null || operatorName.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 复制上下文
     */
    public ConfigChangeContext copy() {
        return ConfigChangeContext.builder()
                .changeBatchId(this.changeBatchId)
                .changeType(this.changeType)
                .configType(this.configType)
                .configKey(this.configKey)
                .configName(this.configName)
                .configGroup(this.configGroup)
                .oldValue(this.oldValue)
                .newValue(this.newValue)
                .changedFields(this.changedFields != null ? new java.util.ArrayList<>(this.changedFields) : null)
                .changeSummary(this.changeSummary)
                .changeReason(this.changeReason)
                .changeSource(this.changeSource)
                .operatorId(this.operatorId)
                .operatorName(this.operatorName)
                .operatorRole(this.operatorRole)
                .clientIp(this.clientIp)
                .userAgent(this.userAgent)
                .sessionId(this.sessionId)
                .requestId(this.requestId)
                .impactScope(this.impactScope)
                .affectedUsers(this.affectedUsers)
                .affectedDevices(this.affectedDevices)
                .riskLevel(this.riskLevel)
                .requireApproval(this.requireApproval)
                .relatedConfigId(this.relatedConfigId)
                .relatedTableName(this.relatedTableName)
                .changeEnvironment(this.changeEnvironment)
                .businessModule(this.businessModule)
                .changeTags(this.changeTags != null ? new java.util.ArrayList<>(this.changeTags) : null)
                .extendedAttributes(this.extendedAttributes != null ? new java.util.HashMap<>(this.extendedAttributes) : null)
                .versionNumber(this.versionNumber)
                .isSensitive(this.isSensitive)
                .estimatedExecutionTime(this.estimatedExecutionTime)
                .rollbackInfo(this.rollbackInfo)
                .build();
    }
}