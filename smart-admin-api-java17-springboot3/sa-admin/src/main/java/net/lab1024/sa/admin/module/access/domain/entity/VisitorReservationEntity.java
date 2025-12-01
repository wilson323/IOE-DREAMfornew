package net.lab1024.sa.admin.module.access.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 访客预约实体
 * <p>
 * 严格遵循repowiki规范：
 * - 完整的字段定义
 * - 清晰的注释说明
 * - 标准的命名规范
 * - 审计字段完整性
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@TableName("t_visitor_reservation")
public class VisitorReservationEntity {

    /**
     * 预约ID
     */
    @TableId(type = IdType.AUTO)
    private Long reservationId;

    /**
     * 预约编号
     */
    private String reservationCode;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 访客手机号
     */
    private String visitorPhone;

    /**
     * 访客邮箱
     */
    private String visitorEmail;

    /**
     * 访客公司
     */
    private String visitorCompany;

    /**
     * 访客身份证号
     */
    private String visitorIdCard;

    /**
     * 访客照片
     */
    private String visitorPhoto;

    /**
     * 访问目的
     */
    private String visitPurpose;

    /**
     * 访问日期
     */
    private LocalDate visitDate;

    /**
     * 访问开始时间
     */
    private LocalDateTime visitStartTime;

    /**
     * 访问结束时间
     */
    private LocalDateTime visitEndTime;

    /**
     * 访问区域ID列表（JSON格式）
     */
    private String visitAreaIds;

    /**
     * 接待人用户ID
     */
    private Long hostUserId;

    /**
     * 接待人姓名
     */
    private String hostUserName;

    /**
     * 接待部门
     */
    private String hostDepartment;

    /**
     * 预约备注
     */
    private String remarks;

    /**
     * 审批状态：0-待审批，1-审批中，2-审批通过，3-已取消，4-已拒绝
     */
    private Integer approvalStatus;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 预约来源：1-前台预约，2-线上预约，3-电话预约，4-现场预约
     */
    private Integer reservationSource;

    /**
     * 紧急程度：1-普通，2-紧急，3-非常紧急
     */
    private Integer urgencyLevel;

    /**
     * 是否需要陪同：0-否，1-是
     */
    private Integer needEscort;

    /**
     * 陪同人姓名
     */
    private String escortName;

    /**
     * 陪同人联系方式
     */
    private String escortPhone;

    /**
     * 访客车辆信息（JSON格式）
     */
    private String vehicleInfo;

    /**
     * 健康码状态：0-绿码，1-黄码，2-红码
     */
    private Integer healthCodeStatus;

    /**
     * 体温检测结果
     */
    private Double bodyTemperature;

    /**
     * 状态变更历史（JSON格式）
     */
    private String statusHistory;

    /**
     * 附件信息（JSON格式）
     */
    private String attachments;

    /**
     * 通知状态：0-未通知，1-已通知访客，2-已通知接待人，3-全部已通知
     */
    private Integer notificationStatus;

    /**
     * 访客性别：1-男，2-女
     */
    private Integer visitorGender;

    /**
     * 访客年龄
     */
    private Integer visitorAge;

    /**
     * 访客职业
     */
    private String visitorOccupation;

    /**
     * 访问类型：1-商务访问，2-技术交流，3-培训学习，4-其他
     */
    private Integer visitType;

    /**
     * 预计停留时长（分钟）
     */
    private Integer expectedDuration;

    /**
     * 实际进入时间
     */
    private LocalDateTime actualEntryTime;

    /**
     * 实际离开时间
     */
    private LocalDateTime actualExitTime;

    /**
     * 实际停留时长（分钟）
     */
    private Integer actualDuration;

    /**
     * 评价等级：1-很差，2-较差，3-一般，4-满意，5-非常满意
     */
    private Integer satisfactionLevel;

    /**
     * 评价内容
     */
    private String evaluationContent;

    /**
     * 评价时间
     */
    private LocalDateTime evaluationTime;

    /**
     * 特殊需求
     */
    private String specialRequirements;

    /**
     * 安全检查状态：0-未检查，1-已通过，2-未通过
     */
    private Integer securityCheckStatus;

    /**
     * 安全检查备注
     */
    private String securityCheckRemarks;

    /**
     * 访客类型：1-普通访客，2-VIP访客，3-临时访客，4-长期访客
     */
    private Integer visitorType;

    /**
     * 通行权限级别：1-普通，2-受限，3-高级
     */
    private Integer accessLevel;

    /**
     * 多次访问标识：0-首次访问，1-多次访问
     */
    private Integer multipleVisitFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Integer deletedFlag;
}