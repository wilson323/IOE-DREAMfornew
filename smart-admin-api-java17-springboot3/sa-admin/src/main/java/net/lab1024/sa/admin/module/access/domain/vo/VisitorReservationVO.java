package net.lab1024.sa.admin.module.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 访客预约视图对象
 * <p>
 * 严格遵循repowiki规范：
 * - 完整的字段定义
 * - 清晰的注释说明
 * - 标准的命名规范
 * - Swagger文档注解
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Schema(description = "访客预约视图对象")
public class VisitorReservationVO {

    /**
     * 预约ID
     */
    @Schema(description = "预约ID")
    private Long reservationId;

    /**
     * 预约编号
     */
    @Schema(description = "预约编号")
    private String reservationCode;

    /**
     * 访客姓名
     */
    @Schema(description = "访客姓名")
    private String visitorName;

    /**
     * 访客手机号
     */
    @Schema(description = "访客手机号")
    private String visitorPhone;

    /**
     * 访客邮箱
     */
    @Schema(description = "访客邮箱")
    private String visitorEmail;

    /**
     * 访客公司
     */
    @Schema(description = "访客公司")
    private String visitorCompany;

    /**
     * 访客身份证号
     */
    @Schema(description = "访客身份证号")
    private String visitorIdCard;

    /**
     * 访客照片
     */
    @Schema(description = "访客照片")
    private String visitorPhoto;

    /**
     * 访问目的
     */
    @Schema(description = "访问目的")
    private String visitPurpose;

    /**
     * 访问日期
     */
    @Schema(description = "访问日期")
    private LocalDate visitDate;

    /**
     * 访问开始时间
     */
    @Schema(description = "访问开始时间")
    private LocalDateTime visitStartTime;

    /**
     * 访问结束时间
     */
    @Schema(description = "访问结束时间")
    private LocalDateTime visitEndTime;

    /**
     * 访问区域ID列表
     */
    @Schema(description = "访问区域ID列表")
    private String visitAreaIds;

    /**
     * 访问区域名称列表
     */
    @Schema(description = "访问区域名称列表")
    private String visitAreaNames;

    /**
     * 接待人用户ID
     */
    @Schema(description = "接待人用户ID")
    private Long hostUserId;

    /**
     * 接待人姓名
     */
    @Schema(description = "接待人姓名")
    private String hostUserName;

    /**
     * 接待部门
     */
    @Schema(description = "接待部门")
    private String hostDepartment;

    /**
     * 预约备注
     */
    @Schema(description = "预约备注")
    private String remarks;

    /**
     * 审批状态
     */
    @Schema(description = "审批状态")
    private Integer approvalStatus;

    /**
     * 审批状态名称
     */
    @Schema(description = "审批状态名称")
    private String approvalStatusName;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见")
    private String approvalComment;

    /**
     * 审批人ID
     */
    @Schema(description = "审批人ID")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @Schema(description = "审批人姓名")
    private String approverName;

    /**
     * 审批时间
     */
    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    /**
     * 取消原因
     */
    @Schema(description = "取消原因")
    private String cancelReason;

    /**
     * 取消时间
     */
    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    /**
     * 预约来源
     */
    @Schema(description = "预约来源")
    private Integer reservationSource;

    /**
     * 预约来源名称
     */
    @Schema(description = "预约来源名称")
    private String reservationSourceName;

    /**
     * 紧急程度
     */
    @Schema(description = "紧急程度")
    private Integer urgencyLevel;

    /**
     * 紧急程度名称
     */
    @Schema(description = "紧急程度名称")
    private String urgencyLevelName;

    /**
     * 是否需要陪同
     */
    @Schema(description = "是否需要陪同")
    private Integer needEscort;

    /**
     * 陪同人姓名
     */
    @Schema(description = "陪同人姓名")
    private String escortName;

    /**
     * 陪同人联系方式
     */
    @Schema(description = "陪同人联系方式")
    private String escortPhone;

    /**
     * 访客性别
     */
    @Schema(description = "访客性别")
    private Integer visitorGender;

    /**
     * 访客性别名称
     */
    @Schema(description = "访客性别名称")
    private String visitorGenderName;

    /**
     * 访客年龄
     */
    @Schema(description = "访客年龄")
    private Integer visitorAge;

    /**
     * 访客职业
     */
    @Schema(description = "访客职业")
    private String visitorOccupation;

    /**
     * 访问类型
     */
    @Schema(description = "访问类型")
    private Integer visitType;

    /**
     * 访问类型名称
     */
    @Schema(description = "访问类型名称")
    private String visitTypeName;

    /**
     * 预计停留时长（分钟）
     */
    @Schema(description = "预计停留时长（分钟）")
    private Integer expectedDuration;

    /**
     * 实际进入时间
     */
    @Schema(description = "实际进入时间")
    private LocalDateTime actualEntryTime;

    /**
     * 实际离开时间
     */
    @Schema(description = "实际离开时间")
    private LocalDateTime actualExitTime;

    /**
     * 实际停留时长（分钟）
     */
    @Schema(description = "实际停留时长（分钟）")
    private Integer actualDuration;

    /**
     * 特殊需求
     */
    @Schema(description = "特殊需求")
    private String specialRequirements;

    /**
     * 访客类型
     */
    @Schema(description = "访客类型")
    private Integer visitorType;

    /**
     * 访客类型名称
     */
    @Schema(description = "访客类型名称")
    private String visitorTypeName;

    /**
     * 通行权限级别
     */
    @Schema(description = "通行权限级别")
    private Integer accessLevel;

    /**
     * 通行权限级别名称
     */
    @Schema(description = "通行权限级别名称")
    private String accessLevelName;

    /**
     * 多次访问标识
     */
    @Schema(description = "多次访问标识")
    private Integer multipleVisitFlag;

    /**
     * 多次访问标识名称
     */
    @Schema(description = "多次访问标识名称")
    private String multipleVisitFlagName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createUserId;

    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名")
    private String createUserName;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    @Schema(description = "更新人姓名")
    private String updateUserName;
}