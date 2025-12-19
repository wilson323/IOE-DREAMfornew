package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 移动端头像上传请求
 * <p>
 * 封装移动端头像上传的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端头像上传请求")
public class MobileAvatarUploadRequest {

    /**
     * 头像Base64数据
     */
    @NotBlank(message = "头像数据不能为空")
    @Schema(description = "头像Base64数据", example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String avatarData;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型", example = "image/jpeg")
    private String fileType;
}
