package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签退响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "签退响应")
public class CheckOutResponse {

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "签退结果")
    private Boolean success;

    @Schema(description = "签退消息")
    private String message;

    @Schema(description = "签退时间")
    private LocalDateTime checkOutTime;

    @Schema(description = "访问时长（分钟）")
    private Long visitDurationMinutes;

    @Schema(description = "是否超时")
    private Boolean isOvertime;

    @Schema(description = "访问状态")
    private String visitStatus;
}