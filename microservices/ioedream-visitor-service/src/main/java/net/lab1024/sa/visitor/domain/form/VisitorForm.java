package net.lab1024.sa.visitor.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 访客表单
 */
@Data
@Schema(description = "访客表单")
public class VisitorForm {

    @Schema(description = "访客ID")
    private Long visitorId;

    @NotBlank(message = "访客姓名不能为空")
    @Schema(description = "访客姓名")
    private String visitorName;

    @NotBlank(message = "手机号码不能为空")
    @Schema(description = "手机号码")
    private String phone;

    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号")
    private String idCard;

    @NotBlank(message = "来访公司不能为空")
    @Schema(description = "来访公司")
    private String company;

    @NotBlank(message = "来访事由不能为空")
    @Schema(description = "来访事由")
    private String purpose;

    @NotNull(message = "访问开始时间不能为空")
    @Schema(description = "访问开始时间")
    private LocalDateTime visitStartTime;

    @NotNull(message = "访问结束时间不能为空")
    @Schema(description = "访问结束时间")
    private LocalDateTime visitEndTime;
}