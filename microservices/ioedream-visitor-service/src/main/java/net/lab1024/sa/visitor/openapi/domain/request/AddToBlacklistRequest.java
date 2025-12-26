package net.lab1024.sa.visitor.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 添加到黑名单请求
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "添加到黑名单请求")
public class AddToBlacklistRequest {

    @NotBlank(message = "访客姓名不能为空")
    @Schema(description = "访客姓名")
    private String visitorName;

    @NotBlank(message = "访客手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "访客手机号")
    private String visitorPhone;

    @Schema(description = "访客身份证号")
    private String visitorIdCard;

    @NotBlank(message = "拉黑原因不能为空")
    @Schema(description = "拉黑原因")
    private String blacklistReason;
}
