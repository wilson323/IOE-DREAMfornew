package net.lab1024.sa.visitor.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客登记实体类
 * <p>
 * 访客现场登记和访问过程管理实体，支持签到签出、访问权限控制等功能
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 *
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>现场签到登记</li>
 *   <li>访问权限管理</li>
 *   <li>访问过程跟踪</li>
 *   <li>签出管理</li>
 *   <li>陪同人员管理</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>预约访客现场签到</li>
 *   <li>临时访客登记</li>
 *   <li>访问过程监控</li>
 *   <li>异常访问处理</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 * @see net.lab1024.sa.common.visitor.service.VisitorRegistrationService 访客登记服务接口
 * @see net.lab1024.sa.common.visitor.dao.VisitorRegistrationDao 访客登记数据访问接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_registration")
public class VisitorRegistrationEntity extends BaseEntity {

    /**
     * 登记ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long registrationId;

    /**
     * 登记编号
     * <p>
     * 系统生成的唯一登记标识，格式：RG + 年月日 + 6位序号
     * 示例：RG20251216000001
     * </p>
     */
    @TableField("registration_code")
    private String registrationCode;

    /**
     * 关联预约ID
     * <p>
     * 如果是预约访客，关联对应的预约记录ID
     * 用于预约与登记的关联管理
     * </p>
     */
    @TableField("reservation_id")
    private Long reservationId;

    /**
     * 访客ID
     * <p>
     * 关联的访客信息ID
     * 用于获取访客基本信息和权限级别
     * </p>
     */
    @TableField("visitor_id")
    private Long visitorId;

    /**
     * 访客姓名
     * <p>
     * 访客真实姓名，用于身份验证和记录
     * 必须与身份证件姓名一致
     * </p>
     */
    @TableField("visitor_name")
    private String visitorName;

    /**
     * 证件号
     * <p>
     * 访客身份证件号码，用于身份验证
     * 加密存储，符合隐私保护要求
     * </p>
     */
    @TableField("id_card")
    private String idCard;

    /**
     * 手机号
     * <p>
     * 访客联系电话，用于验证和通知
     * 加密存储，符合隐私保护要求
     * </p>
     */
    @TableField("phone")
    private String phone;

    /**
     * 被访人ID
     * <p>
     * 被访问员工的系统用户ID
     * 用于通知被访人和访问管理
     * </p>
     */
    @TableField("interviewee_id")
    private Long intervieweeId;

    /**
     * 被访人姓名
     * <p>
     * 被访问员工的真实姓名
     * 用于访问记录和通知
     * </p>
     */
    @TableField("interviewee_name")
    private String intervieweeName;

    /**
     * 访客卡号
     * <p>
     * 临时访客卡的卡号
     * 用于门禁通行和权限控制
     * 访客结束后自动回收
     * </p>
     */
    @TableField("visitor_card")
    private String visitorCard;

    /**
     * 访问权限级别ID
     * <p>
     * 访客的权限级别ID
     * 决定访客可访问的区域和权限范围
     * 关联到权限级别配置表
     * </p>
     */
    @TableField("access_level_id")
    private Long accessLevelId;

    /**
     * 访问区域
     * <p>
     * 允许访问的区域ID列表，JSON格式存储
     * 示例：[1, 2, 3]
     * 用于访问权限控制和安全审计
     * </p>
     */
    @TableField("access_areas")
    private String accessAreas;

    /**
     * 预计离开时间
     * <p>
     * 访客预计离开的时间
     * 用于访问时间管理和超时提醒
     * </p>
     */
    @TableField("expected_leave_time")
    private LocalDateTime expectedLeaveTime;

    /**
     * 实际离开时间
     * <p>
     * 访客实际签出离开的时间
     * 用于访问时长统计和记录
     * </p>
     */
    @TableField("actual_leave_time")
    private LocalDateTime actualLeaveTime;

    /**
     * 登记设备
     * <p>
     * 访客签到时使用的设备编号
     * 用于设备管理和访问追踪
     * 示例：门禁闸机、前台终端等
     * </p>
     */
    @TableField("registration_device")
    private String registrationDevice;

    /**
     * 签入照片URL
     * <p>
     * 访客签到时拍摄的照片
     * 用于身份验证和安全记录
     * 支持人脸识别验证
     * </p>
     */
    @TableField("check_in_photo_url")
    private String checkInPhotoUrl;

    /**
     * 签出照片URL
     * <p>
     * 访客签出时拍摄的照片
     * 用于身份确认和访问记录
     * </p>
     */
    @TableField("check_out_photo_url")
    private String checkOutPhotoUrl;

    /**
     * 状态
     * <p>
     * ACTIVE-在场：访客已签到，正在访问
     * COMPLETED-已离开：访客已正常签出
     * TIMEOUT-超时：访客超时未签出
     * CANCELLED-已取消：登记被取消
     * </p>
     */
    @TableField("status")
    private String status;

    /**
     * 超时原因
     * <p>
     * 访客超时的具体原因说明
     * 用于异常处理和安全分析
     * </p>
     */
    @TableField("over_time_reason")
    private String overTimeReason;

    /**
     * 是否需要陪同
     * <p>
     * 0-否：无需陪同，可独立访问
     * 1-是：需要专人陪同访问
     * </p>
     */
    @TableField("escort_required")
    private Integer escortRequired;

    /**
     * 陪同人
     * <p>
     * 负责陪同的员工姓名
     * 用于陪同管理和责任追溯
     * </p>
     */
    @TableField("escort_user")
    private String escortUser;

    /**
     * 启用状态
     * <p>
     * true-启用：登记有效，可以正常访问
     * false-禁用：登记无效，禁止访问
     * </p>
     */
    @TableField("shelves_flag")
    private Boolean shelvesFlag;

    /**
     * 备注
     * <p>
     * 登记相关备注信息
     * 可记录特殊要求、异常情况等
     * </p>
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 用于存储登记相关的扩展信息
     * 示例：特殊设备、健康状态等
     * </p>
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
