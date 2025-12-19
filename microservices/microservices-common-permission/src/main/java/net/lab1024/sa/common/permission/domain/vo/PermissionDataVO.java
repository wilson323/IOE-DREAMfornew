package net.lab1024.sa.common.permission.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 权限数据变更通知对象
 * <p>
 * 前后端权限数据同步的核心数据结构，支持：
 * - 权限变更实时通知（增删改操作）
 * - 变更类型识别（用户、角色、权限、菜单变更）
 * - 增量数据同步（减少网络传输）
 * - 变更影响范围分析
 * - 数据版本控制（支持冲突检测）
 * - 变更回滚机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限数据变更通知对象")
public class PermissionDataVO {

    /**
     * 变更ID
     */
    @Schema(description = "变更ID", example = "PERM_CHANGE_20251216_001")
    private String changeId;

    /**
     * 变更时间戳
     */
    @Schema(description = "变更时间戳", example = "1708044000000")
    private Long changeTimestamp;

    /**
     * 变更时间
     */
    @Schema(description = "变更时间", example = "2025-12-16T10:30:00")
    private LocalDateTime changeTime;

    /**
     * 变更类型
     */
    @Schema(description = "变更类型", example = "USER_PERMISSION_CHANGE")
    private String changeType; // USER_PERMISSION_CHANGE, ROLE_CHANGE, MENU_CHANGE, PERMISSION_CHANGE

    /**
     * 变更操作类型
     */
    @Schema(description = "变更操作类型", example = "UPDATE")
    private String operationType; // CREATE, UPDATE, DELETE, BATCH_OPERATION

    /**
     * 目标对象ID
     */
    @Schema(description = "目标对象ID", example = "1001")
    private Long targetId;

    /**
     * 目标对象类型
     */
    @Schema(description = "目标对象类型", example = "USER")
    private String targetType; // USER, ROLE, PERMISSION, MENU

    /**
     * 目标对象编码
     */
    @Schema(description = "目标对象编码", example = "USER_001")
    private String targetCode;

    /**
     * 目标对象名称
     */
    @Schema(description = "目标对象名称", example = "张三")
    private String targetName;

    /**
     * 数据版本号（变更前）
     */
    @Schema(description = "数据版本号（变更前）", example = "v1.2.3.20251215")
    private String beforeVersion;

    /**
     * 数据版本号（变更后）
     */
    @Schema(description = "数据版本号（变更后）", example = "v1.2.4.20251216")
    private String afterVersion;

    /**
     * 变更前数据（JSON格式）
     */
    @Schema(description = "变更前数据（JSON格式）", example = "{\"roles\": [\"USER\"], \"permissions\": [\"user:view\"]}")
    private String beforeData;

    /**
     * 变更后数据（JSON格式）
     */
    @Schema(description = "变更后数据（JSON格式）", example = "{\"roles\": [\"USER\", \"ADMIN\"], \"permissions\": [\"user:view\", \"user:edit\"]}")
    private String afterData;

    /**
     * 变更字段列表
     */
    @Schema(description = "变更字段列表", example = "[\"roles\", \"permissions\"]")
    private String[] changedFields;

    /**
     * 影响的用户ID列表
     */
    @Schema(description = "影响的用户ID列表", example = "[1001, 1002, 1003]")
    private Long[] affectedUserIds;

    /**
     * 影响的角色ID列表
     */
    @Schema(description = "影响的角色ID列表", example = "[1, 2, 3]")
    private Long[] affectedRoleIds;

    /**
     * 影响的权限ID列表
     */
    @Schema(description = "影响的权限ID列表", example = "[101, 102, 103]")
    private Long[] affectedPermissionIds;

    /**
     * 影响的菜单ID列表
     */
    @Schema(description = "影响的菜单ID列表", example = "[1001, 1002, 1003]")
    private Long[] affectedMenuIds;

    /**
     * 变更影响范围
     */
    @Schema(description = "变更影响范围", example = "GLOBAL")
    private String impactScope; // SINGLE-单个对象 BATCH-批量对象 DEPARTMENT-部门范围 GLOBAL-全局范围

    /**
     * 变更优先级
     */
    @Schema(description = "变更优先级", example = "HIGH")
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    /**
     * 是否需要立即同步
     */
    @Schema(description = "是否需要立即同步", example = "true")
    private Boolean requireImmediateSync;

    /**
     * 同步超时时间（秒）
     */
    @Schema(description = "同步超时时间（秒）", example = "300")
    private Integer syncTimeout;

    /**
     * 变更来源
     */
    @Schema(description = "变更来源", example = "SYSTEM_ADMIN")
    private String changeSource; // SYSTEM_ADMIN, USER_SELF, BATCH_IMPORT, SYSTEM_SYNC

    /**
     * 操作者ID
     */
    @Schema(description = "操作者ID", example = "999")
    private Long operatorId;

    /**
     * 操作者姓名
     */
    @Schema(description = "操作者姓名", example = "系统管理员")
    private String operatorName;

    /**
     * 操作IP地址
     */
    @Schema(description = "操作IP地址", example = "192.168.1.100")
    private String operatorIp;

    /**
     * 变更原因
     */
    @Schema(description = "变更原因", example = "用户角色调整")
    private String changeReason;

    /**
     * 变更描述
     */
    @Schema(description = "变更描述", example = "将用户张三的角色从USER调整为USER,ADMIN")
    private String changeDescription;

    /**
     * 关联的变更ID列表
     */
    @Schema(description = "关联的变更ID列表", example = "[\"PERM_CHANGE_20251216_002\", \"PERM_CHANGE_20251216_003\"]")
    private String[] relatedChangeIds;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性", example = "{\"rollbackAllowed\": true, \"notifyUsers\": true}")
    private Map<String, Object> extendedAttributes;

    /**
     * 变更状态
     */
    @Schema(description = "变更状态", example = "PENDING")
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, ROLLED_BACK

    /**
     * 处理开始时间
     */
    @Schema(description = "处理开始时间", example = "2025-12-16T10:31:00")
    private LocalDateTime processStartTime;

    /**
     * 处理完成时间
     */
    @Schema(description = "处理完成时间", example = "2025-12-16T10:32:00")
    private LocalDateTime processCompleteTime;

    /**
     * 处理耗时（毫秒）
     */
    @Schema(description = "处理耗时（毫秒）", example = "60000")
    private Long processDuration;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "同步超时，部分用户权限更新失败")
    private String errorMessage;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数", example = "2")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @Schema(description = "最大重试次数", example = "3")
    private Integer maxRetryCount;

    /**
     * 检查变更是否有效
     */
    public boolean isValid() {
        return changeId != null
            && changeType != null
            && operationType != null
            && targetId != null
            && targetType != null;
    }

    /**
     * 检查是否为高优先级变更
     */
    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "URGENT".equals(priority);
    }

    /**
     * 检查是否需要立即处理
     */
    public boolean requiresImmediateProcessing() {
        return Boolean.TRUE.equals(requireImmediateSync) || isHighPriority();
    }

    /**
     * 检查是否为全局影响范围
     */
    public boolean isGlobalImpact() {
        return "GLOBAL".equals(impactScope);
    }

    /**
     * 检查是否处理失败
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return isFailed() && retryCount < maxRetryCount;
    }

    /**
     * 获取变更摘要信息
     */
    public String getChangeSummary() {
        return String.format("%s %s %s[%d] at %s by %s",
                operationType, changeType, targetType, targetId, changeTime, operatorName);
    }

    /**
     * 获取影响范围描述
     */
    public String getImpactDescription() {
        StringBuilder desc = new StringBuilder();

        if (affectedUserIds != null && affectedUserIds.length > 0) {
            desc.append("影响用户: ").append(affectedUserIds.length).append("个");
        }

        if (affectedRoleIds != null && affectedRoleIds.length > 0) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("影响角色: ").append(affectedRoleIds.length).append("个");
        }

        if (affectedPermissionIds != null && affectedPermissionIds.length > 0) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("影响权限: ").append(affectedPermissionIds.length).append("个");
        }

        if (affectedMenuIds != null && affectedMenuIds.length > 0) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("影响菜单: ").append(affectedMenuIds.length).append("个");
        }

        return desc.length() > 0 ? desc.toString() : "影响范围: 仅目标对象";
    }

    /**
     * 构建数据版本差异
     */
    public String getVersionDifference() {
        if (beforeVersion == null && afterVersion != null) {
            return "新增版本: " + afterVersion;
        } else if (beforeVersion != null && afterVersion == null) {
            return "删除版本: " + beforeVersion;
        } else if (beforeVersion != null && afterVersion != null) {
            if (!beforeVersion.equals(afterVersion)) {
                return "版本更新: " + beforeVersion + " → " + afterVersion;
            } else {
                return "版本无变化: " + beforeVersion;
            }
        }
        return "版本信息未知";
    }
}