package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客预约响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "访客预约响应")
public class VisitorAppointmentResponse {

    @Schema(description = "访问ID")
    private Long visitId;

    @Schema(description = "访客ID")
    private Long visitorId;

    @Schema(description = "预约编号")
    private String appointmentNo;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客手机号")
    private String visitorPhone;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "访问码")
    private String visitCode;

    @Schema(description = "二维码URL")
    private String qrCodeUrl;
}
