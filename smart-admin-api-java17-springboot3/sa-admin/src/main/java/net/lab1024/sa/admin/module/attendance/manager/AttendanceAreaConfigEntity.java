package net.lab1024.sa.admin.module.attendance.manager;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤区域配置实体类
 * <p>
 * 用于存储区域级别的考勤配置参数
 * 包括工作时间、打卡规则、请假设置等
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_area_config")
public class AttendanceAreaConfigEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 区域名称（冗余字段，便于查询）
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 工作时间配置（JSON格式）
     */
    @TableField(value = "work_time_config", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private WorkTimeConfig workTimeConfig;

    /**
     * 打卡规则配置（JSON格式）
     */
    @TableField(value = "punch_rule_config", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private PunchRuleConfig punchRuleConfig;

    /**
     * 请假设置配置（JSON格式）
     */
    @TableField(value = "leave_config", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private LeaveConfig leaveConfig;

    /**
     * 加班设置配置（JSON格式）
     */
    @TableField(value = "overtime_config", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private OvertimeConfig overtimeConfig;

    /**
     * 排班设置配置（JSON格式）
     */
    @TableField(value = "schedule_config", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private ScheduleConfig scheduleConfig;

    /**
     * 配置启用状态
     * 0-禁用，1-启用
     */
    @TableField("enabled_flag")
    private Integer enabledFlag;

    /**
     * 配置生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 配置失效时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    // ==================== 内部配置类 ====================

    /**
     * 工作时间配置
     */
    @Data
    public static class WorkTimeConfig {
        /**
         * 上班时间
         */
        private String startTime;

        /**
         * 下班时间
         */
        private String endTime;

        /**
         * 午休开始时间
         */
        private String lunchStartTime;

        /**
         * 午休结束时间
         */
        private String lunchEndTime;

        /**
         * 工作日设置
         * 1-周一，2-周二，...，7-周日
         */
        private List<Integer> workDays;

        /**
         * 弹性工作时间（分钟）
         */
        private Integer flexibleMinutes;

        /**
         * 是否启用弹性工作制
         */
        private Boolean flexibleWorkEnabled;
    }

    /**
     * 打卡规则配置
     */
    @Data
    public static class PunchRuleConfig {
        /**
         * 迟到宽限时间（分钟）
         */
        private Integer lateTolerance;

        /**
         * 早退宽限时间（分钟）
         */
        private Integer earlyTolerance;

        /**
         * 必须打卡次数
         */
        private Integer requiredPunchCount;

        /**
         * 是否启用补打卡
         */
        private Boolean retroactivePunchEnabled;

        /**
         * 补打卡时限（天）
         */
        private Integer retroactivePunchDays;

        /**
         * 是否启用外勤打卡
         */
        private Boolean remotePunchEnabled;

        /**
         * 外勤打卡范围（米）
         */
        private Integer remotePunchRange;
    }

    /**
     * 请假设置配置
     */
    @Data
    public static class LeaveConfig {
        /**
         * 请假类型
         */
        private List<LeaveType> leaveTypes;

        /**
         * 是否需要审批
         */
        private Boolean approvalRequired;

        /**
         * 自动批准条件
         */
        private AutoApprovalRule autoApprovalRule;

        @Data
        public static class LeaveType {
            private String code;
            private String name;
            private Boolean paid;
            private Integer maxDays;
        }

        @Data
        public static class AutoApprovalRule {
            private Integer maxDays;
            private List<String> applicableTypes;
            private Boolean needAttachment;
        }
    }

    /**
     * 加班设置配置
     */
    @Data
    public static class OvertimeConfig {
        /**
         * 加班申请方式
         * PRE-提前申请，POST-事后补录
         */
        private String applyMethod;

        /**
         * 最小加班时长（分钟）
         */
        private Integer minDuration;

        /**
         * 加班费率
         */
        private Double overtimeRate;

        /**
         * 是否需要审批
         */
        private Boolean approvalRequired;

        /**
         * 自动计算规则
         */
        private AutoCalculateRule autoCalculateRule;

        @Data
        public static class AutoCalculateRule {
            private Boolean enabled;
            private Integer startAfterMinutes;
            private Integer endBeforeMinutes;
        }
    }

    /**
     * 排班设置配置
     */
    @Data
    public static class ScheduleConfig {
        /**
         * 排班模式
         * FIXED-固定排班，FLEXIBLE-弹性排班，SHIFT-轮班制
         */
        private String scheduleMode;

        /**
         * 班次设置
         */
        private List<Shift> shifts;

        /**
         * 排班周期
         * WEEKLY-周，MONTHLY-月
         */
        private String scheduleCycle;

        /**
         * 是否允许调班
         */
        private Boolean shiftChangeEnabled;

        @Data
        public static class Shift {
            private String shiftCode;
            private String shiftName;
            private String startTime;
            private String endTime;
            private List<Integer> workDays;
            private Boolean crossDay;
        }
    }
}