package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 加班申请VO
 * <p>
 * 用于返回加班申请详情
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "加班申请详情")
public class AttendanceOvertimeApplyVO {

    @Schema(description = "申请ID")
    private Long applyId;

    @Schema(description = "申请编号")
    private String applyNo;

    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "岗位ID")
    private Long positionId;

    @Schema(description = "岗位名称")
    private String positionName;

    @Schema(description = "加班类型")
    private String overtimeType;

    @Schema(description = "加班日期")
    private LocalDate overtimeDate;

    @Schema(description = "加班开始时间")
    private LocalTime startTime;

    @Schema(description = "加班结束时间")
    private LocalTime endTime;

    @Schema(description = "计划加班时长（小时）")
    private BigDecimal plannedHours;

    @Schema(description = "实际加班时长（小时）")
    private BigDecimal actualHours;

    @Schema(description = "加班原因")
    private String overtimeReason;

    @Schema(description = "加班详细说明")
    private String overtimeDescription;

    @Schema(description = "补偿方式")
    private String compensationType;

    @Schema(description = "调休日期")
    private LocalDate leaveDate;

    @Schema(description = "申请状态")
    private String applyStatus;

    @Schema(description = "当前审批层级")
    private Integer approvalLevel;

    @Schema(description = "当前审批人ID")
    private Long approverId;

    @Schema(description = "当前审批人姓名")
    private String approverName;

    @Schema(description = "最终审批人ID")
    private Long finalApproverId;

    @Schema(description = "最终审批人姓名")
    private String finalApproverName;

    @Schema(description = "最终审批时间")
    private LocalDateTime finalApprovalTime;

    @Schema(description = "最终审批意见")
    private String finalApprovalComment;

    @Schema(description = "工作流实例ID")
    private Long workflowInstanceId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "审批记录列表")
    private List<Object> approvalList;
}
