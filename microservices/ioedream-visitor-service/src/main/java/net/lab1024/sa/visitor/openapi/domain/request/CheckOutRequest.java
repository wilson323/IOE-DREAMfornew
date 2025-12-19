package net.lab1024.sa.visitor.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 签退请求
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "签退请求")
public class CheckOutRequest {

    @Schema(description = "预约ID", required = true)
    private Long appointmentId;

    @Schema(description = "访客身份证号")
    private String idCard;

    @Schema(description = "签退设备ID")
    private String deviceId;

    @Schema(description = "签退位置")
    private String location;

    @Schema(description = "签退备注")
    private String remarks;
}