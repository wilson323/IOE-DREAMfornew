package net.lab1024.sa.visitor.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 发送邀请请求
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "发送邀请请求")
public class SendInvitationRequest {

    @NotNull(message = "访问ID不能为空")
    @Schema(description = "访问ID")
    private Long visitId;

    @Schema(description = "邀请方式")
    private String invitationMethod;

    @Schema(description = "邀请消息")
    private String invitationMessage;
}
