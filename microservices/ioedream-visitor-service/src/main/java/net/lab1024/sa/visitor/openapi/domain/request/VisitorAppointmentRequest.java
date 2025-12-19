package net.lab1024.sa.visitor.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * 访客预约请求
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "访客预约请求")
public class VisitorAppointmentRequest {

    @NotBlank(message = "访客姓名不能为空")
    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    @NotBlank(message = "访客手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "访客手机号", example = "13800138000")
    private String visitorPhone;

    @Schema(description = "访客身份证号", example = "110101199001011234")
    private String visitorIdCard;

    @Schema(description = "访客单位", example = "ABC公司")
    private String visitorCompany;

    @NotNull(message = "被访人ID不能为空")
    @Schema(description = "被访人ID", example = "1001")
    private Long visiteeId;

    @Schema(description = "被访人姓名", example = "李四")
    private String visiteeName;

    @Schema(description = "被访人部门", example = "技术部")
    private String visiteeDepartment;

    @NotBlank(message = "访问事由不能为空")
    @Schema(description = "访问事由", example = "商务洽谈")
    private String visitPurpose;

    @NotNull(message = "计划访问时间不能为空")
    @Schema(description = "计划访问时间")
    private LocalDateTime plannedVisitTime;

    @NotNull(message = "计划离开时间不能为空")
    @Schema(description = "计划离开时间")
    private LocalDateTime plannedLeaveTime;

    @Schema(description = "备注")
    private String remarks;
}