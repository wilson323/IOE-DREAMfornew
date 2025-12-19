package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 移动端设备注册请求
 * <p>
 * 封装移动端设备注册的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端设备注册请求")
public class MobileDeviceRegisterRequest {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "device_123")
    private String deviceId;

    /**
     * 设备类型
     */
    @NotBlank(message = "设备类型不能为空")
    @Schema(description = "设备类型", example = "ANDROID", allowableValues = {"ANDROID", "IOS", "H5"})
    private String deviceType;

    /**
     * 设备型号
     */
    @Schema(description = "设备型号", example = "iPhone 14")
    private String deviceModel;

    /**
     * 推送Token
     */
    @Schema(description = "推送Token", example = "push_token_123")
    private String pushToken;
}
