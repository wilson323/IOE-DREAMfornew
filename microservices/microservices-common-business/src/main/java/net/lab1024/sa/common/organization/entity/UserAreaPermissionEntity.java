package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 用户区域权限实体
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯数据模型，无业务逻辑方法
 * - Entity≤200行，理想≤100行
 * - 包含数据字段、基础注解、构造方法
 * - 无static方法，无业务计算逻辑
 * </p>
 * <p>
 * 核心职责：
 * - 存储用户对区域的访问权限配置
 * - 支持时间段权限控制
 * - 支持权限有效期管理
 * </p>
 * <p>
 * 数据库表：t_access_permission（复用现有表结构）
 * 注意：文档中提到的t_user_area_permission表，实际使用t_access_permission表
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_access_permission")
@Schema(description = "用户区域权限实体")
public class UserAreaPermissionEntity extends BaseEntity {

    /**
     * 权限ID（主键）
     */
    @TableId(value = "permission_id", type = IdType.AUTO)
    @Schema(description = "权限ID", example = "1001")
    private Long id;

    /**
     * 用户ID
     */
    @NotNull
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 区域ID
     */
    @NotNull
    @TableField("area_id")
    @Schema(description = "区域ID", example = "2001")
    private Long areaId;

    /**
     * 设备ID（可选，为空表示该区域所有设备）
     */
    @TableField("device_id")
    @Schema(description = "设备ID", example = "3001")
    private Long deviceId;

    /**
     * 权限类型
     * ALWAYS-永久权限
     * TIME_LIMITED-限时权限
     */
    @Size(max = 20)
    @TableField("permission_type")
    @Schema(description = "权限类型", example = "ALWAYS", allowableValues = {"ALWAYS", "TIME_LIMITED"})
    private String permissionType;

    /**
     * 生效开始时间
     */
    @TableField("start_time")
    @Schema(description = "生效开始时间", example = "2025-01-01 00:00:00")
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    @TableField("end_time")
    @Schema(description = "生效结束时间", example = "2025-12-31 23:59:59")
    private LocalDateTime endTime;

    /**
     * 通行时间段（JSON格式）
     * 格式：[{"startTime": "08:00", "endTime": "18:00", "days": [1,2,3,4,5]}]
     * 用于存储多个时间段配置
     */
    @Size(max = 500)
    @TableField("access_times")
    @Schema(description = "通行时间段（JSON格式）", example = "[{\"startTime\":\"08:00\",\"endTime\":\"18:00\"}]")
    private String accessTimes;

    /**
     * 权限状态
     * 1-有效
     * 2-失效
     */
    @TableField("status")
    @Schema(description = "权限状态", example = "1", allowableValues = {"1", "2"})
    private Integer status;

    // ==================== 构造方法 ====================

    /**
     * 默认构造函数
     */
    public UserAreaPermissionEntity() {
        super();
        // 设置默认值
        this.permissionType = "ALWAYS";
        this.status = 1;
    }

    /**
     * 快速构造函数
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     */
    public UserAreaPermissionEntity(Long userId, Long areaId) {
        this();
        this.userId = userId;
        this.areaId = areaId;
    }

    // ==================== 业务状态常量 ====================

    /**
     * 权限类型常量
     */
    public static class PermissionType {
        public static final String ALWAYS = "ALWAYS";           // 永久权限
        public static final String TIME_LIMITED = "TIME_LIMITED"; // 限时权限
    }

    /**
     * 权限状态常量
     */
    public static class Status {
        public static final int VALID = 1;    // 有效
        public static final int INVALID = 2;  // 失效
    }
}
