package net.lab1024.sa.visitor.domain.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 待审批访客记录VO
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "待审批访客记录VO")
public class PendingApprovalVO {

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "审批记录ID")
    private Long approvalId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "访客ID")
    private Long visitorId;

    @Schema(description = "访问ID")
    private Long visitId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客手机号")
    private String visitorPhone;

    @Schema(description = "访客身份证号")
    private String visitorIdCard;

    @Schema(description = "访客单位")
    private String visitorCompany;

    @Schema(description = "被访人姓名")
    private String visiteeName;

    @Schema(description = "被访人部门")
    private String visiteeDepartment;

    @Schema(description = "访问事由")
    private String visitPurpose;

    @Schema(description = "计划访问时间")
    private LocalDateTime plannedVisitTime;

    @Schema(description = "计划离开时间")
    private LocalDateTime plannedLeaveTime;

    @Schema(description = "访问状态")
    private Integer visitStatus;

    @Schema(description = "申请时间")
    private LocalDateTime applicationTime;

    @Schema(description = "申请人")
    private String applicantName;

    @Schema(description = "紧急程度")
    private Integer urgencyLevel;
}
