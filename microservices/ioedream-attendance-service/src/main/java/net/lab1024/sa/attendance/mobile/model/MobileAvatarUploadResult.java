package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动端头像上传结果
 * <p>
 * 封装移动端头像上传响应结果
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
@Schema(description = "移动端头像上传结果")
public class MobileAvatarUploadResult {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "http://example.com/avatar/1001.jpg")
    private String avatarUrl;

    /**
     * 消息
     */
    @Schema(description = "消息", example = "头像上传成功")
    private String message;
}


