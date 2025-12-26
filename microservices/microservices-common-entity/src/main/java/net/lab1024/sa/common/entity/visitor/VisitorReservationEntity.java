package net.lab1024.sa.common.entity.visitor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客预约实体类
 * <p>
 * 访客预约信息管理实体，支持预约申请、审批、状态跟踪等功能
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 *
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>预约申请管理</li>
 *   <li>预约审批流程</li>
 *   <li>访问时间管理</li>
 *   <li>访问区域权限</li>
 *   <li>车辆登记管理</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>企业访客预约</li>
 *   <li>供应商预约</li>
 *   <li>面试人员预约</li>
 *   <li>维修服务预约</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 * @see net.lab1024.sa.common.visitor.service.VisitorReservationService 访客预约服务接口
 * @see net.lab1024.sa.common.visitor.dao.VisitorReservationDao 访客预约数据访问接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_reservation")
public class VisitorReservationEntity extends BaseEntity {

    /**
     * 预约ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long reservationId;

    /**
     * 预约编号
     * <p>
     * 系统生成的唯一预约标识，格式：R + 年月日 + 6位序号
     * 示例：R20251216000001
     * </p>
     */
    @TableField("reservation_code")
    private String reservationCode;

    /**
     * 访客ID
     * <p>
     * 关联的访客信息ID
     * 如果访客已存在系统记录，则关联现有访客
     * 如果是新访客，可先创建访客记录再创建预约
     * </p>
     */
    @TableField("visitor_id")
    private Long visitorId;

    /**
     * 访客姓名
     * <p>
     * 访客真实姓名，用于预约登记和门禁验证
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
     * 访客联系电话，用于预约确认和验证码发送
     * 加密存储，符合隐私保护要求
     * </p>
     */
    @TableField("phone")
    private String phone;

    /**
     * 被访人ID
     * <p>
     * 被访问员工的系统用户ID
     * 用于通知被访人和访问权限管理
     * </p>
     */
    @TableField("interviewee_id")
    private Long intervieweeId;

    /**
     * 被访人姓名
     * <p>
     * 被访问员工的真实姓名
     * 用于预约确认和门禁记录
     * </p>
     */
    @TableField("interviewee_name")
    private String intervieweeName;

    /**
     * 访问目的
     * <p>
     * 访问的主要原因分类
     * 示例：商务洽谈、面试、维修、配送等
     * 用于访问管理和统计分析
     * </p>
     */
    @TableField("visit_purpose")
    private String visitPurpose;

    /**
     * 访问事由详细说明
     * <p>
     * 访问的具体事由和需求说明
     * 用于审批决策和安全评估
     * </p>
     */
    @TableField("purpose_detail")
    private String purposeDetail;

    /**
     * 访问日期
     * <p>
     * 预计访问的日期
     * 用于访问权限的时间控制
     * </p>
     */
    @TableField("visit_date")
    private LocalDate visitDate;

    /**
     * 开始时间
     * <p>
     * 访问开始时间，格式：HH:mm
     * 示例：09:00
     * </p>
     */
    @TableField("start_time")
    private String startTime;

    /**
     * 结束时间
     * <p>
     * 访问结束时间，格式：HH:mm
     * 示例：17:00
     * </p>
     */
    @TableField("end_time")
    private String endTime;

    /**
     * 访问区域ID
     * <p>
     * 允许访问的区域ID
     * 关联到区域管理模块的AreaEntity
     * </p>
     */
    @TableField("visit_area_id")
    private Long visitAreaId;

    /**
     * 访客人数
     * <p>
     * 本次预约的访客总人数
     * 包括主访客和随行人员
     * 默认为1人
     * </p>
     */
    @TableField("visitor_count")
    private Integer visitorCount;

    /**
     * 车牌号
     * <p>
     * 访客车辆车牌号码
     * 用于车辆通行管理和停车安排
     * 支持多个车牌号，用逗号分隔
     * </p>
     */
    @TableField("car_number")
    private String carNumber;

    /**
     * 预约状态
     * <p>
     * PENDING-待审核：预约申请已提交，等待审批
     * APPROVED-已通过：预约已通过审批
     * REJECTED-已拒绝：预约被拒绝
     * CANCELLED-已取消：预约被取消
     * COMPLETED-已完成：访问已完成
     * EXPIRED-已过期：预约已过期
     * </p>
     */
    @TableField("status")
    private String status;

    /**
     * 审批人
     * <p>
     * 审批该预约的管理员姓名
     * 用于审批记录和责任追溯
     * </p>
     */
    @TableField("approve_user")
    private String approveUser;

    /**
     * 审批时间
     * <p>
     * 预约审批的具体时间
     * 用于审批流程跟踪和统计
     * </p>
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 审批意见
     * <p>
     * 审批人的意见和说明
     * 用于审批记录和沟通
     * </p>
     */
    @TableField("approve_remark")
    private String approveRemark;

    /**
     * 拒绝原因
     * <p>
     * 预约被拒绝的具体原因
     * 示例：时间冲突、安全考虑、权限不足等
     * </p>
     */
    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 是否已提醒
     * <p>
     * 0-否：未发送提醒通知
     * 1-是：已发送提醒通知
     * 用于预约提醒和通知管理
     * </p>
     */
    @TableField("reminder_flag")
    private Integer reminderFlag;

    /**
     * 启用状态
     * <p>
     * true-启用：预约有效，可以正常访问
     * false-禁用：预约暂停，禁止访问
     * </p>
     */
    @TableField("shelves_flag")
    private Boolean shelvesFlag;

    /**
     * 备注
     * <p>
     * 预约相关备注信息
     * 可记录特殊要求、注意事项等
     * </p>
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 用于存储预约相关的扩展信息
     * 示例：特殊设备需求、接待安排等
     * </p>
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
