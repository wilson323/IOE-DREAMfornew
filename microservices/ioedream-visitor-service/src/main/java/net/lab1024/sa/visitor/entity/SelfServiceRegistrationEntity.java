package net.lab1024.sa.visitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 自助登记核心实体类
 * <p>
 * 访客在自助终端进行自主登记的核心信息
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * 字段数量: 20个 (符合≤30字段标准)
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_self_service_registration")
@Schema(description = "自助登记核心实体")
public class SelfServiceRegistrationEntity extends BaseEntity {

    /**
     * 自助登记ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "自助登记ID")
    private Long registrationId;

    /**
     * 登记编号
     */
    @TableField("registration_code")
    @Schema(description = "登记编号")
    private String registrationCode;

    /**
     * 访客姓名
     */
    @TableField("visitor_name")
    @Schema(description = "访客姓名", required = true, example = "张三")
    @NotBlank(message = "访客姓名不能为空")
    private String visitorName;

    /**
     * 证件类型 (1-身份证 2-护照 3-港澳通行证 4-台湾通行证 9-其他)
     */
    @TableField("id_card_type")
    @Schema(description = "证件类型", example = "1")
    @NotNull(message = "证件类型不能为空")
    private Integer idCardType;

    /**
     * 证件号
     */
    @TableField("id_card")
    @Schema(description = "证件号", required = true, example = "110101199001011234")
    @NotBlank(message = "证件号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$",
            message = "身份证号格式不正确")
    private String idCard;

    /**
     * 手机号
     */
    @TableField("phone")
    @Schema(description = "手机号", required = true, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 访客类型 (1-普通访客 2-快递外卖 3-面试人员 4-施工人员 9-其他)
     */
    @TableField("visitor_type")
    @Schema(description = "访客类型", required = true, example = "1")
    @NotNull(message = "访客类型不能为空")
    private Integer visitorType;

    /**
     * 访问事由
     */
    @TableField("visit_purpose")
    @Schema(description = "访问事由", required = true, example = "商务洽谈")
    @NotBlank(message = "访问事由不能为空")
    private String visitPurpose;

    /**
     * 被访人ID
     */
    @TableField("interviewee_id")
    @Schema(description = "被访人ID", example = "1001")
    private Long intervieweeId;

    /**
     * 被访人姓名
     */
    @TableField("interviewee_name")
    @Schema(description = "被访人姓名", required = true, example = "李四")
    @NotBlank(message = "被访人姓名不能为空")
    private String intervieweeName;

    /**
     * 被访人部门
     */
    @TableField("interviewee_department")
    @Schema(description = "被访人部门", example = "技术部")
    private String intervieweeDepartment;

    /**
     * 访问日期
     */
    @TableField("visit_date")
    @Schema(description = "访问日期", example = "2025-12-26")
    @NotNull(message = "访问日期不能为空")
    private LocalDate visitDate;

    /**
     * 预计进入时间
     */
    @TableField("expected_enter_time")
    @Schema(description = "预计进入时间", example = "2025-12-26T09:00:00")
    private LocalDateTime expectedEnterTime;

    /**
     * 预计离开时间
     */
    @TableField("expected_leave_time")
    @Schema(description = "预计离开时间", example = "2025-12-26T18:00:00")
    @NotNull(message = "预计离开时间不能为空")
    private LocalDateTime expectedLeaveTime;

    /**
     * 访客码
     */
    @TableField("visitor_code")
    @Schema(description = "访客码")
    private String visitorCode;

    /**
     * 登记状态 (0-待审批 1-审批通过 2-审批拒绝 3-已签到 4-已完成 5-已取消)
     */
    @TableField("registration_status")
    @Schema(description = "登记状态", required = true, example = "0")
    @NotNull(message = "登记状态不能为空")
    private Integer registrationStatus;

    /**
     * 访问区域 (JSON格式)
     */
    @TableField("access_areas")
    @Schema(description = "访问区域")
    private String accessAreas;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
