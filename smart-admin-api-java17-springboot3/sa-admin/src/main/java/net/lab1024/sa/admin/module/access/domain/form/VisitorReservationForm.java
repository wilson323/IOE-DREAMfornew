package net.lab1024.sa.admin.module.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 访客预约表单
 * <p>
 * 严格遵循repowiki规范：
 * - 完整的表单验证
 * - 清晰的字段说明
 * - 标准的命名规范
 * - Swagger文档注解
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Schema(description = "访客预约表单")
public class VisitorReservationForm {

    /**
     * 预约ID（更新时使用）
     */
    @Schema(description = "预约ID")
    private Long reservationId;

    /**
     * 访客姓名
     */
    @Schema(description = "访客姓名", required = true)
    @NotBlank(message = "访客姓名不能为空")
    @Size(max = 50, message = "访客姓名长度不能超过50个字符")
    private String visitorName;

    /**
     * 访客手机号
     */
    @Schema(description = "访客手机号", required = true)
    @NotBlank(message = "访客手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String visitorPhone;

    /**
     * 访客邮箱
     */
    @Schema(description = "访客邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String visitorEmail;

    /**
     * 访客公司
     */
    @Schema(description = "访客公司", required = true)
    @NotBlank(message = "访客公司不能为空")
    @Size(max = 100, message = "公司名称长度不能超过100个字符")
    private String visitorCompany;

    /**
     * 访客身份证号
     */
    @Schema(description = "访客身份证号")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", message = "身份证号格式不正确")
    private String visitorIdCard;

    /**
     * 访客照片
     */
    @Schema(description = "访客照片")
    private String visitorPhoto;

    /**
     * 访问目的
     */
    @Schema(description = "访问目的", required = true)
    @NotBlank(message = "访问目的不能为空")
    @Size(max = 500, message = "访问目的长度不能超过500个字符")
    private String visitPurpose;

    /**
     * 访问日期
     */
    @Schema(description = "访问日期", required = true)
    @NotNull(message = "访问日期不能为空")
    @Future(message = "访问日期必须是未来的日期")
    private LocalDate visitDate;

    /**
     * 访问开始时间
     */
    @Schema(description = "访问开始时间", required = true)
    @NotNull(message = "访问开始时间不能为空")
    private LocalDateTime visitStartTime;

    /**
     * 访问结束时间
     */
    @Schema(description = "访问结束时间", required = true)
    @NotNull(message = "访问结束时间不能为空")
    private LocalDateTime visitEndTime;

    /**
     * 访问区域ID列表（逗号分隔）
     */
    @Schema(description = "访问区域ID列表", required = true)
    @NotBlank(message = "访问区域不能为空")
    private String visitAreaIds;

    /**
     * 接待人用户ID
     */
    @Schema(description = "接待人用户ID", required = true)
    @NotNull(message = "接待人不能为空")
    private Long hostUserId;

    /**
     * 接待人姓名
     */
    @Schema(description = "接待人姓名", required = true)
    @NotBlank(message = "接待人姓名不能为空")
    @Size(max = 50, message = "接待人姓名长度不能超过50个字符")
    private String hostUserName;

    /**
     * 接待部门
     */
    @Schema(description = "接待部门", required = true)
    @NotBlank(message = "接待部门不能为空")
    @Size(max = 100, message = "接待部门长度不能超过100个字符")
    private String hostDepartment;

    /**
     * 预约备注
     */
    @Schema(description = "预约备注")
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String remarks;

    /**
     * 预约来源：1-前台预约，2-线上预约，3-电话预约，4-现场预约
     */
    @Schema(description = "预约来源")
    private Integer reservationSource = 2; // 默认线上预约

    /**
     * 紧急程度：1-普通，2-紧急，3-非常紧急
     */
    @Schema(description = "紧急程度")
    private Integer urgencyLevel = 1; // 默认普通

    /**
     * 是否需要陪同：0-否，1-是
     */
    @Schema(description = "是否需要陪同")
    private Integer needEscort = 0; // 默认不需要

    /**
     * 陪同人姓名
     */
    @Schema(description = "陪同人姓名")
    @Size(max = 50, message = "陪同人姓名长度不能超过50个字符")
    private String escortName;

    /**
     * 陪同人联系方式
     */
    @Schema(description = "陪同人联系方式")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "陪同人手机号格式不正确")
    private String escortPhone;

    /**
     * 访客性别：1-男，2-女
     */
    @Schema(description = "访客性别")
    private Integer visitorGender;

    /**
     * 访客年龄
     */
    @Schema(description = "访客年龄")
    private Integer visitorAge;

    /**
     * 访客职业
     */
    @Schema(description = "访客职业")
    @Size(max = 100, message = "访客职业长度不能超过100个字符")
    private String visitorOccupation;

    /**
     * 访问类型：1-商务访问，2-技术交流，3-培训学习，4-其他
     */
    @Schema(description = "访问类型")
    private Integer visitType = 1; // 默认商务访问

    /**
     * 预计停留时长（分钟）
     */
    @Schema(description = "预计停留时长（分钟）")
    private Integer expectedDuration;

    /**
     * 特殊需求
     */
    @Schema(description = "特殊需求")
    @Size(max = 500, message = "特殊需求长度不能超过500个字符")
    private String specialRequirements;

    /**
     * 访客类型：1-普通访客，2-VIP访客，3-临时访客，4-长期访客
     */
    @Schema(description = "访客类型")
    private Integer visitorType = 1; // 默认普通访客
}