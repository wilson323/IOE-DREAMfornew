package net.lab1024.sa.common.entity.visitor;

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
 * 自助登记终端实体类
 * <p>
 * 访客在自助终端进行自主登记的实体，支持身份证识别、人脸采集、访客码生成等功能
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong>
 * </p>
 * <ul>
 *   <li>自助信息录入：访客通过终端自主输入个人信息</li>
 *   <li>身份证识别：读取身份证芯片信息</li>
 *   <li>人脸采集：现场采集访客人脸照片</li>
 *   <li>访客码生成：生成唯一访客码用于通行</li>
 *   <li>访客卡打印：打印临时访客卡</li>
 *   <li>在线审批：支持被访人在线审批</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>无预约访客现场自助登记</li>
 *   <li>预约访客快速签到</li>
 *   <li>快递/外卖人员登记</li>
 *   <li>面试人员登记</li>
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
@TableName("t_visitor_self_service_registration")
@Schema(description = "自助登记终端实体")
public class SelfServiceRegistrationEntity extends BaseEntity {

    /**
     * 自助登记ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "自助登记ID")
    private Long registrationId;

    /**
     * 登记编号
     * <p>
     * 系统生成的唯一自助登记标识，格式：SSRG + 年月日 + 6位序号
     * 示例：SSRG20251226000001
     * </p>
     */
    @TableField("registration_code")
    @Schema(description = "登记编号")
    private String registrationCode;

    /**
     * 访客姓名
     * <p>
     * 从身份证读取或手动输入
     * 必须与身份证件姓名一致
     * </p>
     */
    @TableField("visitor_name")
    @Schema(description = "访客姓名", required = true, example = "张三")
    @NotBlank(message = "访客姓名不能为空")
    private String visitorName;

    /**
     * 证件类型
     * <p>
     * 1-身份证 2-护照 3-港澳通行证 4-台湾通行证 9-其他
     * </p>
     */
    @TableField("id_card_type")
    @Schema(description = "证件类型", example = "1")
    @NotNull(message = "证件类型不能为空")
    private Integer idCardType;

    /**
     * 证件号
     * <p>
     * 从身份证芯片读取或手动输入
     * 加密存储，符合隐私保护要求
     * </p>
     */
    @TableField("id_card")
    @Schema(description = "证件号", required = true, example = "110101199001011234")
    @NotBlank(message = "证件号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$",
            message = "身份证号格式不正确")
    private String idCard;

    /**
     * 手机号
     * <p>
     * 访客联系电话，用于验证和通知
     * 加密存储，符合隐私保护要求
     * </p>
     */
    @TableField("phone")
    @Schema(description = "手机号", required = true, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 访客类型
     * <p>
     * 1-普通访客 2-快递外卖 3-面试人员 4-施工人员 9-其他
     * </p>
     */
    @TableField("visitor_type")
    @Schema(description = "访客类型", required = true, example = "1")
    @NotNull(message = "访客类型不能为空")
    private Integer visitorType;

    /**
     * 访问事由
     * <p>
     * 访客来访的具体事由说明
     * 示例：商务洽谈、面试、送货、维修等
     * </p>
     */
    @TableField("visit_purpose")
    @Schema(description = "访问事由", required = true, example = "商务洽谈")
    @NotBlank(message = "访问事由不能为空")
    private String visitPurpose;

    /**
     * 被访人ID
     * <p>
     * 被访问员工的系统用户ID
     * 用于通知被访人和访问管理
     * </p>
     */
    @TableField("interviewee_id")
    @Schema(description = "被访人ID", example = "1001")
    private Long intervieweeId;

    /**
     * 被访人姓名
     * <p>
     * 被访问员工的真实姓名
     * 用于访问记录和通知
     * </p>
     */
    @TableField("interviewee_name")
    @Schema(description = "被访人姓名", required = true, example = "李四")
    @NotBlank(message = "被访人姓名不能为空")
    private String intervieweeName;

    /**
     * 被访人部门
     * <p>
     * 被访人所属部门名称
     * 用于访客指引和访问管理
     * </p>
     */
    @TableField("interviewee_department")
    @Schema(description = "被访人部门", example = "技术部")
    private String intervieweeDepartment;

    /**
     * 访问日期
     * <p>
     * 访客计划访问的日期
     * 默认为当天
     * </p>
     */
    @TableField("visit_date")
    @Schema(description = "访问日期", example = "2025-12-26")
    @NotNull(message = "访问日期不能为空")
    private LocalDate visitDate;

    /**
     * 预计进入时间
     * <p>
     * 访客预计进入园区的时间
     * </p>
     */
    @TableField("expected_enter_time")
    @Schema(description = "预计进入时间", example = "2025-12-26T09:00:00")
    private LocalDateTime expectedEnterTime;

    /**
     * 预计离开时间
     * <p>
     * 访客预计离开的时间
     * 用于访问时间管理和超时提醒
     * </p>
     */
    @TableField("expected_leave_time")
    @Schema(description = "预计离开时间", example = "2025-12-26T18:00:00")
    @NotNull(message = "预计离开时间不能为空")
    private LocalDateTime expectedLeaveTime;

    /**
     * 访客人脸照片URL
     * <p>
     * 自助终端采集的访客人脸照片
     * 用于人脸识别验证和通行
     * </p>
     */
    @TableField("face_photo_url")
    @Schema(description = "人脸照片URL")
    private String facePhotoUrl;

    /**
     * 人脸特征向量
     * <p>
     * 从人脸照片提取的512维特征向量
     * 用于人脸识别比对，Base64编码存储
     * </p>
     */
    @TableField("face_feature")
    @Schema(description = "人脸特征向量")
    private String faceFeature;

    /**
     * 身份证照片URL
     * <p>
     * 从身份证芯片读取的照片
     * 用于身份验证和记录
     * </p>
     */
    @TableField("id_card_photo_url")
    @Schema(description = "身份证照片URL")
    private String idCardPhotoUrl;

    /**
     * 访客码
     * <p>
     * 系统生成的唯一访客码，格式：VC + 年月日时分秒 + 4位随机码
     * 示例：VC202512261430001234
     * 用于访客身份识别和通行验证
     * </p>
     */
    @TableField("visitor_code")
    @Schema(description = "访客码")
    private String visitorCode;

    /**
     * 访客卡号
     * <p>
     * 发放的临时访客卡卡号
     * 用于门禁通行
     * </p>
     */
    @TableField("visitor_card")
    @Schema(description = "访客卡号")
    private String visitorCard;

    /**
     * 访客卡打印状态
     * <p>
     * 0-未打印 1-打印中 2-已打印 3-打印失败
     * </p>
     */
    @TableField("card_print_status")
    @Schema(description = "访客卡打印状态", example = "0")
    private Integer cardPrintStatus;

    /**
     * 访问区域
     * <p>
     * 允许访问的区域ID列表，JSON格式存储
     * 示例：[1, 2, 3]
     * </p>
     */
    @TableField("access_areas")
    @Schema(description = "访问区域")
    private String accessAreas;

    /**
     * 终端ID
     * <p>
     * 执行登记的自助终端设备ID
     * 用于设备管理和操作追溯
     * </p>
     */
    @TableField("terminal_id")
    @Schema(description = "终端ID")
    private String terminalId;

    /**
     * 终端位置
     * <p>
     * 自助终端所在的位置描述
     * 示例：A栋大厅、北门访客中心等
     * </p>
     */
    @TableField("terminal_location")
    @Schema(description = "终端位置", example = "A栋大厅")
    private String terminalLocation;

    /**
     * 登记状态
     * <p>
     * 0-待审批 1-审批通过 2-审批拒绝 3-已签到 4-已完成 5-已取消
     * </p>
     */
    @TableField("registration_status")
    @Schema(description = "登记状态", required = true, example = "0")
    @NotNull(message = "登记状态不能为空")
    private Integer registrationStatus;

    /**
     * 审批人ID
     * <p>
     * 审批访客登记的员工ID
     * 通常为被访人或前台管理员
     * </p>
     */
    @TableField("approver_id")
    @Schema(description = "审批人ID")
    private Long approverId;

    /**
     * 审批人姓名
     * <p>
     * 审批人的真实姓名
     * </p>
     */
    @TableField("approver_name")
    @Schema(description = "审批人姓名")
    private String approverName;

    /**
     * 审批时间
     * <p>
     * 访客登记审批通过的时间
     * </p>
     */
    @TableField("approval_time")
    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     * <p>
     * 审批人的审批意见或备注
     * </p>
     */
    @TableField("approval_comment")
    @Schema(description = "审批意见")
    private String approvalComment;

    /**
     * 签到时间
     * <p>
     * 访客实际签到进入的时间
     * </p>
     */
    @TableField("check_in_time")
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    /**
     * 签离时间
     * <p>
     * 访客签出离开的时间
     * </p>
     */
    @TableField("check_out_time")
    @Schema(description = "签离时间")
    private LocalDateTime checkOutTime;

    /**
     * 是否需要陪同
     * <p>
     * 0-否：无需陪同，可独立访问
     * 1-是：需要专人陪同访问
     * </p>
     */
    @TableField("escort_required")
    @Schema(description = "是否需要陪同", example = "0")
    private Integer escortRequired;

    /**
     * 陪同人姓名
     * <p>
     * 负责陪同的员工姓名
     * </p>
     */
    @TableField("escort_user")
    @Schema(description = "陪同人姓名")
    private String escortUser;

    /**
     * 携带物品
     * <p>
     * 访客携带的物品说明
     * JSON格式存储，示例：{"笔记本电脑": 1, "其他": "样品"}
     * </p>
     */
    @TableField("belongings")
    @Schema(description = "携带物品")
    private String belongings;

    /**
     * 车牌号
     * <p>
     * 访客车辆的车牌号
     * 用于车辆通行管理
     * </p>
     */
    @TableField("license_plate")
    @Schema(description = "车牌号", example = "京A12345")
    private String licensePlate;

    /**
     * 备注
     * <p>
     * 登记相关备注信息
     * 可记录特殊要求、注意事项等
     * </p>
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 用于存储登记相关的扩展信息
     * 示例：{"healthCode": "绿色", "temperature": "36.5"}
     * </p>
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
