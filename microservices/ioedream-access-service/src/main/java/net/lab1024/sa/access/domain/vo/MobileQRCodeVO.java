package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 移动端二维码响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端二维码响应")
public class MobileQRCodeVO {

    @Schema(description = "会话ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Schema(description = "二维码内容（URL）", example = "iot://access/auth?sid=xxx&ts=xxx&token=xxx")
    private String qrContent;

    @Schema(description = "二维码图片（Base64）")
    private String qrImage;

    @Schema(description = "过期时间", example = "2025-01-30T12:00:00")
    private LocalDateTime expireTime;

    @Schema(description = "有效期（秒）", example = "300")
    private Long validSeconds;

    @Schema(description = "状态", example = "pending")
    private String status;

    @Schema(description = "二维码类型", example = "temporary")
    private String qrCodeType;
}
