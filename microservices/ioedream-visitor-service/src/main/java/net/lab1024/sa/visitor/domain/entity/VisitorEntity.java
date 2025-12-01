package net.lab1024.sa.visitor.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客实体类
 * 存储访客的基本信息和预约状态
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor")
public class VisitorEntity extends BaseEntity {

    /**
     * 访客ID（主键）
     */
    @TableId(value = "visitor_id", type = IdType.AUTO)
    private Long visitorId;

    /**
     * 访客编号
     */
    private String visitorNo;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 手机号码（别名）
     */
    private String phoneNumber;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 身份证号（别名）
     */
    private String idNumber;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 公司名称
     */
    private String company;

    /**
     * 职位
     */
    private String position;

    /**
     * 访客类型代码
     */
    private String visitorTypeCode;

    /**
     * 访客类型名称
     */
    private String visitorTypeName;

    /**
     * 访问目的
     */
    private String visitPurpose;

    /**
     * 被访人员ID
     */
    private Long visitUserId;

    /**
     * 被访人员姓名
     */
    private String visitUserName;

    /**
     * 被访人员部门
     */
    private String visitUserDept;

    /**
     * 预约开始时间
     */
    private LocalDateTime appointmentStartTime;

    /**
     * 预约结束时间
     */
    private LocalDateTime appointmentEndTime;

    /**
     * 实际到达时间
     */
    private LocalDateTime actualArrivalTime;

    /**
     * 实际离开时间
     */
    private LocalDateTime actualDepartureTime;

    /**
     * 访问状态
     */
    private Integer status;

    /**
     * 访问状态代码
     */
    private String statusCode;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastVisitTime;

    /**
     * 访问状态名称
     */
    private String statusName;

    /**
     * 紧急程度
     */
    private String urgencyLevel;

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
     * 审批备注
     */
    private String approvalRemarks;

    /**
     * 访客照片路径
     */
    private String photoPath;

    /**
     * 证件照片路径
     */
    private String idCardPhotoPath;

    /**
     * 访客卡号
     */
    private String cardNo;

    /**
     * 访客卡生效时间
     */
    private LocalDateTime cardEffectiveTime;

    /**
     * 访客卡失效时间
     */
    private LocalDateTime cardExpiryTime;

    /**
     * 访问区域
     */
    private String visitArea;

    /**
     * 访问权限配置
     */
    private String permissionConfig;

    /**
     * 陪同人员ID
     */
    private Long escortUserId;

    /**
     * 陪同人员姓名
     */
    private String escortUserName;

    /**
     * 车辆号码
     */
    private String vehicleNumber;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 同步状态
     */
    private String syncStatus;
}