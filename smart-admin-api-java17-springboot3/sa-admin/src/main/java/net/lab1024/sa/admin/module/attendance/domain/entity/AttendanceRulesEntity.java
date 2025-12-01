package net.lab1024.sa.admin.module.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDate;

/**
 * 考勤规则表实体
 *
 * 考勤规则管理的核心实体，支持多种规则类型和灵活配置
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Data
@TableName("t_attendance_rules")
public class AttendanceRulesEntity extends BaseEntity {

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则编码
     */
    @TableField("`rule_code`")
    private String ruleCode;

    /**
     * 规则名称
     */
    @TableField("`rule_name`")
    private String ruleName;

    /**
     * 规则类型: BASIC-基础规则 OVERTIME-加班规则 LEAVE-请假规则 HOLIDAY-节假日规则
     */
    @TableField("`rule_type`")
    private String ruleType;

    /**
     * 规则配置详细JSON格式
     *迟到规则示例：{"lateThreshold":5,"lateDeduction":0.5,"maxLateMinutes":30}
     *加班规则示例：{"weekdayMultiplier":1.5,"weekendMultiplier":2.0,"holidayMultiplier":3.0,"minOvertimeHours":1.0}
     */
    @TableField("`rule_config`")
    private String ruleConfig;

    /**
     * 适用范围 JSON格式
     * {"departments":[1,2,3],"positions":[1,2],"employees":[1001,1002]}
     */
    @TableField("`applicable_scope`")
    private String applicableScope;

    /**
     * 生效日期
     */
    @TableField("`effective_date`")
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    @TableField("`expiry_date`")
    private LocalDate expiryDate;

    /**
     * 优先级 1-最高 10-最低
     */
    @TableField("`priority`")
    private Integer priority;

    /**
     * 是否默认规则
     */
    @TableField("`is_default`")
    private Boolean isDefault;

    /**
     * 状态 1-启用 0-禁用
     */
    @TableField("`status`")
    private Boolean status;

    /**
     * 规则描述
     */
    @TableField("`description`")
    private String description;

    /**
     * 规则类型描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String ruleTypeDesc;

    /**
     * 适用部门名称（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String applicableDepartmentNames;

    /**
     * 适用岗位名称（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String applicablePositionNames;

    /**
     * 是否当前有效（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Boolean currentlyEffective;
}