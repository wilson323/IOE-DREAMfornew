package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客详情VO
 */
@Data
@Schema(description = "访客详情VO")
public class VisitorDetailVO {

    @Schema(description = "访客ID")
    private Long visitorId;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "身份证号（别名）")
    private String idNumber;

    @Schema(description = "来访公司")
    private String company;

    @Schema(description = "访问状态")
    private String status;

    @Schema(description = "访问开始时间")
    private LocalDateTime visitStartTime;

    @Schema(description = "访问结束时间")
    private LocalDateTime visitEndTime;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "紧急程度描述")
    private String urgencyLevelDesc;
}