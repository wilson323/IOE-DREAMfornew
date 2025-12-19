package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 移动端应用更新检查请求
 * <p>
 * 封装移动端应用更新检查的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端应用更新检查请求")
public class MobileAppUpdateCheckRequest {

    /**
     * 当前版本
     */
    @NotBlank(message = "当前版本不能为空")
    @Schema(description = "当前版本", example = "2.0.0")
    private String currentVersion;

    /**
     * 平台类型
     */
    @Schema(description = "平台类型", example = "ANDROID", allowableValues = {"ANDROID", "IOS"})
    private String platform;
}


