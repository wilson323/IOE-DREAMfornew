package net.lab1024.sa.common.organization.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 互锁记录实体类
 * <p>
 * 对应数据库表: t_access_interlock_record
 * 用于记录门禁设备的互锁状态和锁定记录
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_interlock_record")
public class InterlockRecordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 互锁组ID
     */
    @TableField("interlock_group_id")
    private Long interlockGroupId;

    /**
     * 锁定状态常量
     */
    public static class LockStatus {
        public static final Integer UNLOCKED = 0;
        public static final Integer LOCKED = 1;
    }

    /**
     * 锁定状态: 0=未锁定, 1=已锁定
     */
    @TableField("lock_status")
    private Integer lockStatus;

    /**
     * 锁定时间
     */
    @TableField("lock_time")
    private LocalDateTime lockTime;

    /**
     * 解锁时间
     */
    @TableField("unlock_time")
    private LocalDateTime unlockTime;

    /**
     * 锁定时长(秒)
     */
    @TableField("lock_duration")
    private Integer lockDuration;
}
