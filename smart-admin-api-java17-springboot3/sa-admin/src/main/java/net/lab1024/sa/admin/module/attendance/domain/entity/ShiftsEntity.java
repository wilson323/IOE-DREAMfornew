package net.lab1024.sa.admin.module.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * 班次表实体
 *
 * 班次管理的核心实体，支持规律班次、弹性班次、轮班班次等多种类型
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Data
@TableName("t_shifts")
public class ShiftsEntity extends BaseEntity {

    /**
     * 班次ID
     */
    private Long shiftId;

    /**
     * 班次编码
     */
    @TableField("`shift_code`")
    private String shiftCode;

    /**
     * 班次名称
     */
    @TableField("`shift_name`")
    private String shiftName;

    /**
     * 班次类型: REGULAR-规律班次 FLEXIBLE-弹性班次 ROTATING-轮班班次
     */
    @TableField("`shift_type`")
    private String shiftType;

    /**
     * 工作日配置 JSON格式
     * {"weekdays":[1,2,3,4,5],"customDays":["2025-01-01","2025-02-10"]}
     */
    @TableField("`work_days`")
    private String workDays;

    /**
     * 总工作时长(小时)
     */
    @TableField("`total_work_hours`")
    private BigDecimal totalWorkHours;

    /**
     * 加班配置 JSON格式
     * {"enabled":true,"threshold":8.0,"multiplier":1.5}
     */
    @TableField("`overtime_config`")
    private String overtimeConfig;

    /**
     * 弹性配置 JSON格式
     * {"flexStart":60,"flexEnd":60,"coreHours":["09:00-17:00"]}
     */
    @TableField("`flexible_config`")
    private String flexibleConfig;

    /**
     * 轮班配置 JSON格式
     * {"cycleDays":7,"rotationOrder":["A","B","C"]}
     */
    @TableField("`rotation_config`")
    private String rotationConfig;

    /**
     * 休息规则 JSON格式
     * {"mealBreak":60,"restBreaks":[{"start":"15:00","end":15.15}]}
     */
    @TableField("`break_rules`")
    private String breakRules;

    /**
     * 迟到早退规则 JSON格式
     * {"lateThreshold":5,"earlyThreshold":5}
     */
    @TableField("`late_early_rules`")
    private String lateEarlyRules;

    /**
     * 班次描述
     */
    @TableField("`description`")
    private String description;

    /**
     * 排序
     */
    @TableField("`sort_order`")
    private Integer sortOrder;

    /**
     * 状态 1-启用 0-禁用
     */
    @TableField("`status`")
    private Boolean status;

    /**
     * 班次类型描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String shiftTypeDesc;

    /**
     * 工作日描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String workDaysDesc;

    /**
     * 时间段列表（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String timePeriodsList;

    /**
     * 是否支持弹性工作（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Boolean supportsFlexible;

    /**
     * 格式化工作时间（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String formattedWorkTime;
}