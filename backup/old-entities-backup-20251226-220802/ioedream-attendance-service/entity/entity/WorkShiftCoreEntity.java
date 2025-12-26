package net.lab1024.sa.attendance.entity;

import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作班次核心实体类
 * <p>
 * 考勤班次管理的核心信息，包含基础的时间和班次配置
 * 严格遵循CLAUDE.md全局架构规范，Entity行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_work_shift")
public class WorkShiftCoreEntity extends BaseEntity {

    /**
     * 班次ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long shiftId;

    /**
     * 班次编码
     */
    @TableField("shift_code")
    private String shiftCode;

    /**
     * 班次名称
     */
    @TableField("shift_name")
    private String shiftName;

    /**
     * 班次类型：1-固定班次 2-弹性班次 3-轮班班次 4-临时班次
     */
    @TableField("shift_type")
    private Integer shiftType;

    /**
     * 工作日：1-周一 2-周二 3-周三 4-周四 5-周五 6-周六 7-周日（JSON数组）
     */
    @TableField("work_days")
    private String workDays;

    /**
     * 上班时间
     */
    @TableField("work_start_time")
    private LocalTime workStartTime;

    /**
     * 下班时间
     */
    @TableField("work_end_time")
    private LocalTime workEndTime;

    /**
     * 工作时长（分钟）
     */
    @TableField("work_duration")
    private Integer workDuration;

    /**
     * 班次状态：1-启用 0-禁用
     */
    @TableField("shift_status")
    private Integer shiftStatus;

    /**
     * 班次描述
     */
    @TableField("description")
    private String description;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
