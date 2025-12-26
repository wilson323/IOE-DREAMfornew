package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签到响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "签到响应")
public class CheckInResponse {

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "签到结果")
    private Boolean success;

    @Schema(description = "签到消息")
    private String message;

    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    @Schema(description = "访问凭证")
    private String accessCredential;

    @Schema(description = "有效期至")
    private LocalDateTime validUntil;

    @Schema(description = "访问区域")
    private String accessArea;

    @Schema(description = "被访人姓名")
    private String intervieweeName;

    @Schema(description = "被访人部门")
    private String intervieweeDepartment;
}
