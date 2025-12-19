package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 移动端反馈提交请求
 * <p>
 * 封装移动端反馈提交的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端反馈提交请求")
public class MobileFeedbackSubmitRequest {

    /**
     * 反馈类型
     */
    @NotBlank(message = "反馈类型不能为空")
    @Schema(description = "反馈类型", example = "BUG", allowableValues = {"BUG", "SUGGESTION", "COMPLAINT", "OTHER"})
    private String feedbackType;

    /**
     * 反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 1000, message = "反馈内容长度不能超过1000个字符")
    @Schema(description = "反馈内容", example = "应用在使用过程中出现卡顿现象")
    private String content;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式", example = "13800138000")
    private String contact;

    /**
     * 附件URL列表
     */
    @Schema(description = "附件URL列表")
    private java.util.List<String> attachments;
}
