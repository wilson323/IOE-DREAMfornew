package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邀请响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "邀请响应")
public class InvitationResponse {

    @Schema(description = "邀请ID")
    private Long invitationId;

    @Schema(description = "邀请状态")
    private String invitationStatus;

    @Schema(description = "发送时间")
    private String sendTime;

    @Schema(description = "邀请消息")
    private String invitationMessage;
}
