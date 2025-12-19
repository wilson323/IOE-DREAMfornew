package net.lab1024.sa.visitor.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 签到请求
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "签到请求")
public class CheckInRequest {

    @Schema(description = "预约ID", required = true)
    private Long appointmentId;

    @Schema(description = "访客身份证号", required = true)
    @NotBlank(message = "访客身份证号不能为空")
    private String idCard;

    @Schema(description = "访客手机号")
    private String phoneNumber;

    @Schema(description = "签到设备ID")
    private String deviceId;

    @Schema(description = "签到位置")
    private String location;

    @Schema(description = "人脸识别数据（Base64）")
    private String faceData;

    @Schema(description = "验证码")
    private String verificationCode;

    /**
     * 兼容历史字段名：checkinType
     *
     * <p>根据是否携带设备ID做一个可解释的推断。</p>
     *
     * @return 签到类型（DEVICE / MANUAL）
     */
    public String getCheckinType() {
        return (this.deviceId != null && !this.deviceId.isBlank()) ? "DEVICE" : "MANUAL";
    }
}