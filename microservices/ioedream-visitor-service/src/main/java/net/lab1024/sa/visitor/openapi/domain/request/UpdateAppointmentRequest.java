package net.lab1024.sa.visitor.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 更新访客预约请求
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "更新访客预约请求")
public class UpdateAppointmentRequest {

    @NotNull(message = "访问ID不能为空")
    @Schema(description = "访问ID")
    private Long visitId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客手机号")
    private String visitorPhone;

    @Schema(description = "被访人姓名")
    private String visiteeName;

    @Schema(description = "访问事由")
    private String visitPurpose;

    @Schema(description = "计划访问时间")
    private LocalDateTime plannedVisitTime;

    @Schema(description = "计划离开时间")
    private LocalDateTime plannedLeaveTime;

    @Schema(description = "备注")
    private String remarks;
}
