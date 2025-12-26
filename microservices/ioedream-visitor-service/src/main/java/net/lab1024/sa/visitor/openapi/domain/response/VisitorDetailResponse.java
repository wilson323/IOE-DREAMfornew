package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客详细信息响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "访客详细信息响应")
public class VisitorDetailResponse {

    @Schema(description = "访问ID")
    private Long visitId;

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

    @Schema(description = "被访人姓名")
    private String visiteeName;

    @Schema(description = "被访人部门")
    private String visiteeDepartment;

    @Schema(description = "访问事由")
    private String visitPurpose;

    @Schema(description = "实际访问时间")
    private LocalDateTime actualVisitTime;

    @Schema(description = "实际离开时间")
    private LocalDateTime actualLeaveTime;

    @Schema(description = "访问状态")
    private Integer visitStatus;

    @Schema(description = "访问时长（分钟）")
    private Long visitDuration;

    @Schema(description = "备注")
    private String remarks;
}
