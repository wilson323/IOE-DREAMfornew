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
 * 考勤规则配置实体类
 * <p>
 * 用于配置考勤异常判定规则：
 * - 迟到判定规则
 * - 早退判定规则
 * - 旷工判定规则
 * - 弹性时间配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_rule_config")
public class AttendanceRuleConfigEntity extends BaseEntity {

    /**
     * 规则配置ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long configId;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 适用范围
     * <p>
     * ALL - 全局适用
     * DEPARTMENT - 部门适用
     * SHIFT - 班次适用
     * EMPLOYEE - 员工适用
     * </p>
     */
    @TableField("apply_scope")
    private String applyScope;

    /**
     * 适用范围ID（部门ID、班次ID或员工ID）
     */
    @TableField("scope_id")
    private Long scopeId;

    /**
     * 规则状态
     * <p>
     * 1-启用
     * 0-禁用
     * </p>
     */
    @TableField("rule_status")
    private Integer ruleStatus;

    // ========== 迟到规则 ==========

    /**
     * 迟到判定启用
     * <p>
     * 1-启用
     * 0-禁用
     * </p>
     */
    @TableField("late_check_enabled")
    private Integer lateCheckEnabled;

    /**
     * 迟到判定分钟数
     * <p>
     * 上班打卡超过此时间判定为迟到
     * 例如：5表示允许5分钟弹性时间，超过5分钟算迟到
     * </p>
     */
    @TableField("late_minutes")
    private Integer lateMinutes;

    /**
     * 严重迟到分钟数
     * <p>
     * 超过此时间判定为严重迟到
     * 例如：30表示迟到超过30分钟为严重迟到
     * </p>
     */
    @TableField("serious_late_minutes")
    private Integer seriousLateMinutes;

    /**
     * 迟到处理方式
     * <p>
     * IGNORE - 忽略（不计迟到）
     * WARNING - 警告（记录迟到）
     * DEDUCT - 扣款（按规则扣款）
     * </p>
     */
    @TableField("late_handle_type")
    private String lateHandleType;

    /**
     * 迟到扣款规则
     * <p>
     * 格式：每小时扣款金额
     * 例如：50表示每小时扣50元
     * </p>
     */
    @TableField("late_deduct_amount")
    private java.math.BigDecimal lateDeductAmount;

    // ========== 早退规则 ==========

    /**
     * 早退判定启用
     */
    @TableField("early_check_enabled")
    private Integer earlyCheckEnabled;

    /**
     * 早退判定分钟数
     * <p>
     * 下班打卡早于此时间判定为早退
     * 例如：5表示允许5分钟弹性时间，早退超过5分钟算早退
     * </p>
     */
    @TableField("early_minutes")
    private Integer earlyMinutes;

    /**
     * 严重早退分钟数
     */
    @TableField("serious_early_minutes")
    private Integer seriousEarlyMinutes;

    /**
     * 早退处理方式
     */
    @TableField("early_handle_type")
    private String earlyHandleType;

    /**
     * 早退扣款规则
     */
    @TableField("early_deduct_amount")
    private java.math.BigDecimal earlyDeductAmount;

    // ========== 旷工规则 ==========

    /**
     * 旷工判定启用
     */
    @TableField("absent_check_enabled")
    private Integer absentCheckEnabled;

    /**
     * 旷工判定规则
     * <p>
     * MISSING_CARD - 缺卡超过一定时间判定为旷工
     * LATE_THRESHOLD - 迟到超过一定时间判定为旷工
     * NO_PUNCH - 全天无打卡记录判定为旷工
     * </p>
     */
    @TableField("absent_rule_type")
    private String absentRuleType;

    /**
     * 旷工判定分钟数
     * <p>
     * 缺卡超过此分钟数判定为旷工
     * 例如：60表示缺卡超过60分钟（1小时）判定为旷工
     * </p>
     */
    @TableField("absent_minutes")
    private Integer absentMinutes;

    /**
     * 迟到转旷工分钟数
     * <p>
     * 迟到超过此分钟数判定为旷工
     * 例如：120表示迟到超过120分钟（2小时）判定为旷工
     * </p>
     */
    @TableField("late_to_absent_minutes")
    private Integer lateToAbsentMinutes;

    /**
     * 旷工处理方式
     */
    @TableField("absent_handle_type")
    private String absentHandleType;

    /**
     * 旷工扣款规则
     * <p>
     * 日工资倍数
     * 例如：3表示旷工1天扣3倍日工资
     * </p>
     */
    @TableField("absent_deduct_rate")
    private java.math.BigDecimal absentDeductRate;

    // ========== 弹性时间配置 ==========

    /**
     * 弹性上班时间启用
     */
    @TableField("flexible_start_enabled")
    private Integer flexibleStartEnabled;

    /**
     * 弹性上班时间（分钟）
     * <p>
     * 允许员工晚到的时间
     * 例如：15表示允许15分钟弹性上班时间
     * </p>
     */
    @TableField("flexible_start_minutes")
    private Integer flexibleStartMinutes;

    /**
     * 弹性下班时间启用
     */
    @TableField("flexible_end_enabled")
    private Integer flexibleEndEnabled;

    /**
     * 弹性下班时间（分钟）
     */
    @TableField("flexible_end_minutes")
    private Integer flexibleEndMinutes;

    // ========== 缺卡规则 ==========

    /**
     * 缺卡判定启用
     */
    @TableField("missing_card_check_enabled")
    private Integer missingCardCheckEnabled;

    /**
     * 允许补卡次数
     * <p>
     * 每月允许补卡次数
     * 0表示不允许补卡
     * -1表示不限制补卡次数
     * </p>
     */
    @TableField("allowed_supplement_times")
    private Integer allowedSupplementTimes;

    /**
     * 补卡时间限制
     * <p>
     * 逾期几天不允许补卡
     * 例如：3表示考勤异常3天后不允许补卡
     * </p>
     */
    @TableField("supplement_days_limit")
    private Integer supplementDaysLimit;

    // ========== 描述和备注 ==========

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
