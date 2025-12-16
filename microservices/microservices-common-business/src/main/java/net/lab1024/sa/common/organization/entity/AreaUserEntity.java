package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 区域人员关联实体
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯数据模型，无业务逻辑方法
 * - Entity≤200行，理想≤100行
 * - 包含数据字段、基础注解、构造方法
 * - 无static方法，无业务计算逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 (重构版)
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_area_user_relation")
@Schema(description = "区域人员关联实体")
public class AreaUserEntity extends BaseEntity {

    /**
     * 关联ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列relation_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * 注意：此实体类主键为String类型，使用雪花算法生成
     * </p>
     */
    @TableId(value = "relation_id", type = IdType.ASSIGN_ID)
    @Schema(description = "关联ID", example = "REL202512160001")
    private String id;

    /**
     * 区域ID
     */
    @NotNull
    @TableField("area_id")
    @Schema(description = "区域ID", example = "1001")
    private Long areaId;

    /**
     * 区域编码
     */
    @NotBlank
    @Size(max = 50)
    @TableField("area_code")
    @Schema(description = "区域编码", example = "BUILDING_A_F1")
    private String areaCode;

    /**
     * 用户ID
     */
    @NotNull
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户名
     */
    @NotBlank
    @Size(max = 100)
    @TableField("username")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 真实姓名
     */
    @Size(max = 100)
    @TableField("real_name")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    /**
     * 关联类型
     * 1-常驻人员 2-临时人员 3-访客 4-维护人员 5-管理人员
     */
    @NotNull
    @TableField("relation_type")
    @Schema(description = "关联类型", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    private Integer relationType;

    /**
     * 权限级别
     * 1-普通权限 2-高级权限 3-管理员权限
     */
    @TableField("permission_level")
    @Schema(description = "权限级别", example = "1", allowableValues = {"1", "2", "3"})
    private Integer permissionLevel;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    @Schema(description = "生效时间", example = "2025-01-01 00:00:00")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @TableField("expire_time")
    @Schema(description = "失效时间", example = "2025-12-31 23:59:59")
    private LocalDateTime expireTime;

    /**
     * 是否永久有效
     */
    @TableField("permanent")
    @Schema(description = "是否永久有效", example = "false")
    private Boolean permanent;

    /**
     * 允许开始时间（格式：HH:mm）
     */
    @Size(max = 5)
    @TableField("allow_start_time")
    @Schema(description = "允许开始时间", example = "08:00")
    private String allowStartTime;

    /**
     * 允许结束时间（格式：HH:mm）
     */
    @Size(max = 5)
    @TableField("allow_end_time")
    @Schema(description = "允许结束时间", example = "18:00")
    private String allowEndTime;

    /**
     * 仅工作日有效
     */
    @TableField("workday_only")
    @Schema(description = "仅工作日有效", example = "true")
    private Boolean workdayOnly;

    /**
     * 访问权限配置（JSON格式）
     * JSON格式：{"access": true, "attendance": true, "consume": false}
     */
    @TableField("access_permissions")
    @Schema(description = "访问权限配置", example = "{\"access\": true, \"attendance\": true}")
    private String accessPermissions;

    /**
     * 设备同步状态
     * 0-未下发 1-下发中 2-已同步 3-同步失败 4-已撤销
     */
    @TableField("device_sync_status")
    @Schema(description = "设备同步状态", example = "2", allowableValues = {"0", "1", "2", "3", "4"})
    private Integer deviceSyncStatus;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    /**
     * 最后同步时间
     */
    @TableField("last_sync_time")
    @Schema(description = "最后同步时间", example = "2025-12-16 10:30:00")
    private LocalDateTime lastSyncTime;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性", example = "{\"department\": \"研发部\"}")
    private String extendedAttributes;

    /**
     * 备注
     */
    @Size(max = 500)
    @TableField("remark")
    @Schema(description = "备注", example = "区域管理员权限")
    private String remark;

    // ==================== 构造方法 ====================

    /**
     * 默认构造函数
     */
    public AreaUserEntity() {
        super();
        // 设置默认值
        this.relationType = 1;
        this.permissionLevel = 1;
        this.deviceSyncStatus = 0;
        this.retryCount = 0;
        this.workdayOnly = false;
        this.permanent = false;
    }

    /**
     * 快速构造函数
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     * @param username 用户名
     * @param realName 真实姓名
     */
    public AreaUserEntity(Long areaId, Long userId, String username, String realName) {
        this();
        this.areaId = areaId;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
    }

    // ==================== 业务状态常量 ====================

    /**
     * 关联类型常量
     */
    public static class RelationType {
        public static final int PERMANENT = 1;      // 常驻人员
        public static final int TEMPORARY = 2;      // 临时人员
        public static final int VISITOR = 3;          // 访客
        public static final int MAINTENANCE = 4;     // 维护人员
        public static final int MANAGER = 5;          // 管理人员
    }

    /**
     * 权限级别常量
     */
    public static class PermissionLevel {
        public static final int NORMAL = 1;          // 普通权限
        public static final int ADVANCED = 2;        // 高级权限
        public static final int ADMIN = 3;            // 管理员权限
    }

    /**
     * 设备同步状态常量
     */
    public static class DeviceSyncStatus {
        public static final int NOT_SYNCED = 0;      // 未下发
        public static final int SYNCING = 1;         // 下发中
        public static final int SYNCED = 2;          // 已同步
        public static final int SYNC_FAILED = 3;     // 同步失败
        public static final int CANCELED = 4;        // 已撤销
    }
}