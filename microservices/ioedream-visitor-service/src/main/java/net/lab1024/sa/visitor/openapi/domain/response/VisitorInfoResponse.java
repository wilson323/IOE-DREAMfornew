package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客信息响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "访客信息响应")
public class VisitorInfoResponse {

    @Schema(description = "访客ID")
    private Long visitorId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客手机号")
    private String visitorPhone;

    @Schema(description = "访客身份证号")
    private String visitorIdCard;

    @Schema(description = "访客单位")
    private String visitorCompany;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "状态")
    private Integer status;
}