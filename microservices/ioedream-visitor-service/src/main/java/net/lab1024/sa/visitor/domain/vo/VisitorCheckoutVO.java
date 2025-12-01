package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访客签出VO
 */
@Data
@Schema(description = "访客签出VO")
public class VisitorCheckoutVO {

    @Schema(description = "访客ID")
    private Long visitorId;

    @Schema(description = "验证方式")
    private String verificationMethod;
}
