package net.lab1024.sa.visitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 自助签离记录实体类
 * <p>
 * 访客在自助签离终端完成签离流程的实体，记录签离过程中的关键信息
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong>
 * </p>
 * <ul>
 *   <li>访客码扫描识别</li>
 *   <li>访客卡归还管理</li>
 *   <li>签离身份验证</li>
 *   <li>访问时长统计</li>
 *   <li>携带物品确认</li>
 *   <li>访客满意度评价</li>
 *   <li>异常情况记录</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>正常签离：访客使用访客码扫描签离</li>
 *   <li>访客卡归还：归还临时访客卡</li>
 *   <li>物品确认：确认带走物品</li>
 *   <li>超时签离：处理超时访客签离</li>
 *   <li>强制签离：管理员强制执行签离</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_self_check_out")
@Schema(description = "自助签离记录实体")
public class SelfCheckOutEntity extends BaseEntity {

    /**
     * 签离记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "签离记录ID")
    private Long checkOutId;

    /**
     * 关联的自助登记ID
     * <p>
     * 关联到自助登记记录ID
     * 用于签离与登记的关联管理
     * </p>
     */
    @TableField("registration_id")
    @Schema(description = "自助登记ID")
    private Long registrationId;

    /**
     * 登记编号
     * <p>
     * 关联的自助登记编号
     * </p>
     */
    @TableField("registration_code")
    @Schema(description = "登记编号")
    private String registrationCode;

    /**
     * 访客码
     * <p>
     * 访客扫描的访客码
     * 用于识别访客身份
     * </p>
     */
    @TableField("visitor_code")
    @Schema(description = "访客码", required = true)
    @NotBlank(message = "访客码不能为空")
    private String visitorCode;

    /**
     * 访客姓名
     * <p>
     * 从登记记录中读取的访客姓名
     * </p>
     */
    @TableField("visitor_name")
    @Schema(description = "访客姓名")
    private String visitorName;

    /**
     * 证件号
     * <p>
     * 加密存储
     * </p>
     */
    @TableField("id_card")
    @Schema(description = "证件号")
    private String idCard;

    /**
     * 手机号
     * <p>
     * 加密存储
     * </p>
     */
    @TableField("phone")
    @Schema(description = "手机号")
    private String phone;

    /**
     * 被访人ID
     */
    @TableField("interviewee_id")
    @Schema(description = "被访人ID")
    private Long intervieweeId;

    /**
     * 被访人姓名
     */
    @TableField("interviewee_name")
    @Schema(description = "被访人姓名")
    private String intervieweeName;

    /**
     * 签离终端ID
     * <p>
     * 执行签离的终端设备ID
     * </p>
     */
    @TableField("terminal_id")
    @Schema(description = "签离终端ID")
    private String terminalId;

    /**
     * 签离终端位置
     * <p>
     * 签离终端所在位置
     * 示例：A栋大厅、北门访客中心等
     * </p>
     */
    @TableField("terminal_location")
    @Schema(description = "签离终端位置", example = "A栋大厅")
    private String terminalLocation;

    /**
     * 签离时间
     * <p>
     * 访客完成签离的时间
     * </p>
     */
    @TableField("check_out_time")
    @Schema(description = "签离时间")
    private LocalDateTime checkOutTime;

    /**
     * 实际访问时长（分钟）
     * <p>
     * 从签到时间到签离时间的实际时长
     * </p>
     */
    @TableField("visit_duration")
    @Schema(description = "实际访问时长（分钟）")
    private Integer visitDuration;

    /**
     * 预计离开时间
     * <p>
     * 登记时填写的预计离开时间
     * </p>
     */
    @TableField("expected_leave_time")
    @Schema(description = "预计离开时间")
    private LocalDateTime expectedLeaveTime;

    /**
     * 是否超时
     * <p>
     * 0-未超时 1-已超时
     * </p>
     */
    @TableField("is_overtime")
    @Schema(description = "是否超时", example = "0")
    private Integer isOvertime;

    /**
     * 超时时长（分钟）
     * <p>
     * 超过预计离开时间的分钟数
     * </p>
     */
    @TableField("overtime_duration")
    @Schema(description = "超时时长（分钟）")
    private Integer overtimeDuration;

    /**
     * 访客卡归还状态
     * <p>
     * 0-未归还 1-已归还 2-卡遗失
     * </p>
     */
    @TableField("card_return_status")
    @Schema(description = "访客卡归还状态", example = "1")
    @NotNull(message = "访客卡归还状态不能为空")
    private Integer cardReturnStatus;

    /**
     * 访客卡号
     * <p>
     * 归还的访客卡卡号
     * </p>
     */
    @TableField("visitor_card")
    @Schema(description = "访客卡号")
    private String visitorCard;

    /**
     * 签离照片URL
     * <p>
     * 签离时拍摄的访客照片
     * 用于身份确认和安全记录
     * </p>
     */
    @TableField("check_out_photo_url")
    @Schema(description = "签离照片URL")
    private String checkOutPhotoUrl;

    /**
     * 人脸验证结果
     * <p>
     * 签离时人脸识别验证结果
     * 0-未验证 1-验证通过 2-验证失败
     * </p>
     */
    @TableField("face_verify_result")
    @Schema(description = "人脸验证结果", example = "1")
    private Integer faceVerifyResult;

    /**
     * 携带物品确认
     * <p>
     * 签离时确认的携带物品
     * JSON格式存储，示例：{"笔记本电脑": 1, "样品": "是"}
     * </p>
     */
    @TableField("belongings_confirmation")
    @Schema(description = "携带物品确认")
    private String belongingsConfirmation;

    /**
     * 物品差异说明
     * <p>
     * 携带物品与登记时的差异说明
     * </p>
     */
    @TableField("belongings_difference")
    @Schema(description = "物品差异说明")
    private String belongingsDifference;

    /**
     * 访客满意度评分
     * <p>
     * 访客对本次访问的满意度评分
     * 1-5分：1-非常不满意，5-非常满意
     * </p>
     */
    @TableField("satisfaction_score")
    @Schema(description = "满意度评分", example = "5")
    private Integer satisfactionScore;

    /**
     * 访客反馈
     * <p>
     * 访客的反馈意见和建议
     * </p>
     */
    @TableField("visitor_feedback")
    @Schema(description = "访客反馈")
    private String visitorFeedback;

    /**
     * 签离方式
     * <p>
     * 1-自助签离 2-人工签离 3-强制签离
     * </p>
     */
    @TableField("check_out_method")
    @Schema(description = "签离方式", example = "1")
    private Integer checkOutMethod;

    /**
     * 操作人ID
     * <p>
     * 人工签离或强制签离的操作人ID
     * </p>
     */
    @TableField("operator_id")
    @Schema(description = "操作人ID")
    private Long operatorId;

    /**
     * 操作人姓名
     * <p>
     * 人工签离或强制签离的操作人姓名
     * </p>
     */
    @TableField("operator_name")
    @Schema(description = "操作人姓名")
    private String operatorName;

    /**
     * 签离状态
     * <p>
     * 0-待确认 1-已完成 2-已取消
     * </p>
     */
    @TableField("check_out_status")
    @Schema(description = "签离状态", required = true, example = "1")
    @NotNull(message = "签离状态不能为空")
    private Integer checkOutStatus;

    /**
     * 异常情况说明
     * <p>
     * 签离过程中的异常情况说明
     * 示例：访客卡损坏、物品丢失等
     * </p>
     */
    @TableField("exception_note")
    @Schema(description = "异常情况说明")
    private String exceptionNote;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 用于存储签离相关的扩展信息
     * 示例：{"returnTime": "18:00", "securityCheck": "通过"}
     * </p>
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
