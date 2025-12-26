package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 智能排班结果明细实体
 * <p>
 * 存储智能排班算法生成的每个员工的排班明细：
 * - 员工信息
 * - 排班日期
 * - 班次信息
 * - 工作时间段
 * - 统计信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_schedule_result")
@Schema(description = "智能排班结果明细实体")
public class SmartScheduleResultEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "排班结果ID")
    private Long resultId;

    @Schema(description = "排班计划ID", required = true)
    private Long planId;

    // ==================== 员工信息 ====================

    @Schema(description = "员工ID", required = true)
    private Long employeeId;

    @Schema(description = "员工姓名", required = true)
    private String employeeName;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    // ==================== 排班日期 ====================

    @Schema(description = "排班日期", required = true)
    private LocalDate scheduleDate;

    @Schema(description = "星期: 1-周一 2-周二 ... 7-周日", example = "1")
    private Integer dayOfWeek;

    @Schema(description = "是否工作日", example = "true")
    private Boolean isWorkDay;

    // ==================== 班次信息 ====================

    @Schema(description = "班次ID", required = true)
    private Long shiftId;

    @Schema(description = "班次编码")
    private String shiftCode;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "班次类型: 1-固定 2-弹性 3-轮班 4-临时", example = "1")
    private Integer shiftType;

    // ==================== 工作时间 ====================

    @Schema(description = "上班时间", required = true, example = "08:00:00")
    private LocalTime workStartTime;

    @Schema(description = "下班时间", required = true, example = "17:00:00")
    private LocalTime workEndTime;

    @Schema(description = "工作时长（分钟）", example = "540")
    private Integer workDuration;

    @Schema(description = "午休开始时间", example = "12:00:00")
    private LocalTime lunchStartTime;

    @Schema(description = "午休结束时间", example = "13:00:00")
    private LocalTime lunchEndTime;

    @Schema(description = "午休时长（分钟）", example = "60")
    private Integer lunchDuration;

    // ==================== 统计信息 ====================

    @Schema(description = "当月工作天数", example = "22")
    private Integer monthlyWorkDays;

    @Schema(description = "当月工作时长（小时）", example = "176")
    private Double monthlyWorkHours;

    @Schema(description = "连续工作天数", example = "5")
    private Integer consecutiveWorkDays;

    @Schema(description = "连续休息天数", example = "2")
    private Integer consecutiveRestDays;

    // ==================== 优化信息 ====================

    @Schema(description = "适应度得分（0-1）", example = "0.92")
    private Double fitnessScore;

    @Schema(description = "是否存在冲突", example = "false")
    private Boolean hasConflict;

    @Schema(description = "冲突类型（多个用逗号分隔）")
    private String conflictTypes;

    @Schema(description = "冲突描述")
    private String conflictDescription;

    // ==================== 状态信息 ====================

    @Schema(description = "排班状态: 1-草稿 2-已确认 3-已执行 4-已取消", example = "1")
    private Integer scheduleStatus;

    @Schema(description = "确认时间")
    private java.time.LocalDateTime confirmTime;

    @Schema(description = "确认人ID")
    private Long confirmUserId;

    @Schema(description = "确认人姓名")
    private String confirmUserName;

    @Schema(description = "备注")
    private String remark;
}
