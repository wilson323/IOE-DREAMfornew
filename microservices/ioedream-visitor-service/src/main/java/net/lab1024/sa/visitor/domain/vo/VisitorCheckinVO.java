package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访客签到VO
 */
@Data
@Schema(description = "访客签到VO")
public class VisitorCheckinVO {

    @Schema(description = "访客ID")
    private Long visitorId;

    @Schema(description = "验证方式")
    private String verificationMethod;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "验证数据")
    private String verificationData;
}
