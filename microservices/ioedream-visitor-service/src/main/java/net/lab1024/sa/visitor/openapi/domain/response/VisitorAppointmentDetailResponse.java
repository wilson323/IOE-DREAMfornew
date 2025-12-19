package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客预约详情响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "访客预约详情响应")
public class VisitorAppointmentDetailResponse {

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客手机号")
    private String visitorPhone;

    @Schema(description = "访客身份证号")
    private String idCard;

    @Schema(description = "访问公司")
    private String visitCompany;

    @Schema(description = "被访人姓名")
    private String intervieweeName;

    @Schema(description = "被访人部门")
    private String intervieweeDepartment;

    @Schema(description = "访问事由")
    private String visitReason;

    @Schema(description = "计划访问时间")
    private LocalDateTime plannedVisitTime;

    @Schema(description = "计划离开时间")
    private LocalDateTime plannedLeaveTime;

    @Schema(description = "预约状态")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    @Schema(description = "实际到达时间")
    private LocalDateTime actualArrivalTime;

    @Schema(description = "实际离开时间")
    private LocalDateTime actualLeaveTime;

    @Schema(description = "车牌号")
    private String plateNumber;

    @Schema(description = "备注")
    private String remarks;
}