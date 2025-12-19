package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 移动端提醒设置请求
 * <p>
 * 封装移动端提醒设置的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端提醒设置请求")
public class MobileReminderSettingsRequest {

    /**
     * 是否启用上班提醒
     */
    @NotNull(message = "是否启用上班提醒不能为空")
    @Schema(description = "是否启用上班提醒", example = "true")
    private Boolean clockInReminderEnabled;

    /**
     * 上班提醒时间（分钟）
     */
    @Schema(description = "上班提醒时间（分钟）", example = "30")
    private Integer clockInReminderMinutes;

    /**
     * 是否启用下班提醒
     */
    @NotNull(message = "是否启用下班提醒不能为空")
    @Schema(description = "是否启用下班提醒", example = "true")
    private Boolean clockOutReminderEnabled;

    /**
     * 下班提醒时间（分钟）
     */
    @Schema(description = "下班提醒时间（分钟）", example = "30")
    private Integer clockOutReminderMinutes;
}
