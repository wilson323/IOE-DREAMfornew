package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动端设备注册结果
 * <p>
 * 封装移动端设备注册响应结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端设备注册结果")
public class MobileDeviceRegisterResult {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "device_123")
    private String deviceId;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 消息
     */
    @Schema(description = "消息", example = "设备注册成功")
    private String message;
}


