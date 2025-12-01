package net.lab1024.sa.visitor.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 访客记录实体类
 * 存储访客访问记录和通行日志
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visit_record")
public class VisitRecordEntity extends BaseEntity {

    /**
     * 记录编号
     */
    private String recordNo;

    /**
     * 访客ID
     */
    private Long visitorId;

    /**
     * 访客姓名
     */
    private String visitorName;

    /**
     * 预约ID
     */
    private Long appointmentId;

    /**
     * 预约编号
     */
    private String appointmentNo;

    /**
     * 被访人员ID
     */
    private Long visitUserId;

    /**
     * 被访人员姓名
     */
    private String visitUserName;

    /**
     * 访问区域
     */
    private String visitArea;

    /**
     * 访问门禁点
     */
    private String accessPoint;

    /**
     * 进入时间
     */
    private LocalDateTime entryTime;

    /**
     * 离开时间
     */
    private LocalDateTime exitTime;

    /**
     * 停留时长（分钟）
     */
    private Integer stayDuration;

    /**
     * 通行状态代码
     */
    private String passStatusCode;

    /**
     * 通行状态名称
     */
    private String passStatusName;

    /**
     * 验证方式代码
     */
    private String verificationMethodCode;

    /**
     * 验证方式名称
     */
    private String verificationMethodName;

    /**
     * 验证设备ID
     */
    private Long deviceId;

    /**
     * 验证设备名称
     */
    private String deviceName;

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
     * 车辆进入时间
     */
    private LocalDateTime vehicleEntryTime;

    /**
     * 车辆离开时间
     */
    private LocalDateTime vehicleExitTime;

    /**
     * 访问次数
     */
    private Integer visitCount;

    /**
     * 本次访问时长（小时）
     */
    private BigDecimal currentVisitDuration;

    /**
     * 累计访问时长（小时）
     */
    private BigDecimal totalVisitDuration;

    /**
     * 访客照片识别结果
     */
    private String photoRecognitionResult;

    /**
     * 人脸识别匹配度
     */
    private BigDecimal faceMatchScore;

    /**
     * 异常状态代码
     */
    private String exceptionStatusCode;

    /**
     * 异常状态描述
     */
    private String exceptionDescription;

    /**
     * 处理状态代码
     */
    private String processStatusCode;

    /**
     * 处理状态名称
     */
    private String processStatusName;

    /**
     * 处理备注
     */
    private String processRemarks;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

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