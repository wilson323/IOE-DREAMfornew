package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 考勤规则实体类
 * <p>
 * 考勤管理的规则配置，支持时间、地点、缺勤、加班等多种规则类型
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_rule")
public class AttendanceRuleEntity extends BaseEntity {

    /**
     * 规则ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long ruleId;

    /**
     * 规则编码
     */
    @TableField("rule_code")
    private String ruleCode;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则分类：TIME-时间规则 LOCATION-地点规则 ABSENCE-缺勤规则 OVERTIME-加班规则
     */
    @TableField("rule_category")
    private String ruleCategory;

    /**
     * 规则类型
     */
    @TableField("rule_type")
    private String ruleType;

    /**
     * 规则条件（JSON格式）
     */
    @TableField("rule_condition")
    private String ruleCondition;

    /**
     * 规则动作（JSON格式）
     */
    @TableField("rule_action")
    private String ruleAction;

    /**
     * 规则优先级（数字越小优先级越高）
     */
    @TableField("rule_priority")
    private Integer rulePriority;

    /**
     * 生效开始时间
     */
    @TableField("effective_start_time")
    private String effectiveStartTime;

    /**
     * 生效结束时间
     */
    @TableField("effective_end_time")
    private String effectiveEndTime;

    /**
     * 生效日期（1,2,3,4,5,6,7）
     */
    @TableField("effective_days")
    private String effectiveDays;

    /**
     * 适用部门ID列表（JSON数组）
     */
    @TableField("department_ids")
    private String departmentIds;

    /**
     * 适用用户ID列表（JSON数组）
     */
    @TableField("user_ids")
    private String userIds;

    /**
     * 规则状态：1-启用 0-禁用
     */
    @TableField("rule_status")
    private Integer ruleStatus;

    /**
     * 规则作用域：GLOBAL-全局 DEPARTMENT-部门 USER-个人
     */
    @TableField("rule_scope")
    private String ruleScope;

    /**
     * 执行顺序
     */
    @TableField("execution_order")
    private Integer executionOrder;

    /**
     * 父规则ID
     */
    @TableField("parent_rule_id")
    private Long parentRuleId;

    /**
     * 规则版本
     */
    @TableField("rule_version")
    private String ruleVersion;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
