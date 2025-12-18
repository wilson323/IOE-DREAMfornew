package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 互锁记录实体类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数量控制在30个以内
 * - 类行数控制在200行以内
 * - 纯数据模型，无业务逻辑方法
 * </p>
 * <p>
 * 业务场景：
 * - 记录设备的互锁状态
 * - 用于互锁验证（防止互锁门禁同时开启）
 * - 支持锁定/解锁机制
 * </p>
 * <p>
 * 数据库表：t_access_interlock_record
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_interlock_record")
@Schema(description = "互锁记录实体")
public class InterlockRecordEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1001")
    private Long id;

    /**
     * 设备ID
     */
    @NotNull
    @TableField("device_id")
    @Schema(description = "设备ID", example = "2001", required = true)
    private Long deviceId;

    /**
     * 互锁组ID
     */
    @NotNull
    @TableField("interlock_group_id")
    @Schema(description = "互锁组ID", example = "3001", required = true)
    private Long interlockGroupId;

    /**
     * 锁定状态
     * 0=未锁定
     * 1=已锁定
     */
    @NotNull
    @TableField("lock_status")
    @Schema(description = "锁定状态", example = "0", allowableValues = {"0", "1"}, required = true)
    private Integer lockStatus;

    /**
     * 锁定时间
     */
    @TableField("lock_time")
    @Schema(description = "锁定时间", example = "2025-01-30 10:00:00")
    private LocalDateTime lockTime;

    /**
     * 解锁时间
     */
    @TableField("unlock_time")
    @Schema(description = "解锁时间", example = "2025-01-30 10:05:00")
    private LocalDateTime unlockTime;

    /**
     * 锁定时长（秒）
     */
    @TableField("lock_duration")
    @Schema(description = "锁定时长（秒）", example = "300")
    private Integer lockDuration;

    // ==================== 锁定状态常量 ====================

    /**
     * 锁定状态常量类
     */
    public static class LockStatus {
        /**
         * 未锁定
         */
        public static final int UNLOCKED = 0;

        /**
         * 已锁定
         */
        public static final int LOCKED = 1;
    }
}
