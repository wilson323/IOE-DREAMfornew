package net.lab1024.sa.admin.module.smart.access.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 门禁权限实体类
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("smart_access_permission")
public class SmartAccessPermissionEntity extends BaseEntity {

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long permissionId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员编号
     */
    private String personCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 权限类型 (NORMAL-普通, TEMPORARY-临时, VIP-贵宾, EMERGENCY-紧急)
     */
    private String permissionType;

    /**
     * 权限级别 (1-低级, 2-中级, 3-高级, 4-管理员, 5-超级管理员)
     */
    private Integer permissionLevel;

    /**
     * 设备ID列表(逗号分隔)
     */
    private String deviceIds;

    /**
     * 设备组ID列表(逗号分隔)
     */
    private String groupIds;

    /**
     * 区域ID列表(逗号分隔)
     */
    private String areaIds;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 有效时间段 (JSON格式: [{"dayOfWeek":1,"startTime":"08:00","endTime":"18:00"}])
     */
    private String timePeriods;

    /**
     * 有效星期 (1-7对应周一到周日，逗号分隔)
     */
    private String effectiveDays;

    /**
     * 访问次数限制 (0-无限制)
     */
    private Integer accessLimit;

    /**
     * 已访问次数
     */
    private Integer accessCount;

    /**
     * 权限状态 (ACTIVE-激活, INACTIVE-未激活, EXPIRED-已过期, SUSPENDED-暂停)
     */
    private String permissionStatus;

    /**
     * 审批状态 (PENDING-待审批, APPROVED-已审批, REJECTED-已拒绝)
     */
    private String approvalStatus;

    /**
     * 审批人
     */
    private Long approverId;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 审批备注
     */
    private String approvalRemark;

    /**
     * 发放方式 (CARD-卡片, FACE-人脸, FINGERPRINT-指纹, PASSWORD-密码)
     */
    private String issueType;

    /**
     * 卡片编号
     */
    private String cardNumber;

    /**
     * 人脸特征ID
     */
    private String faceFeatureId;

    /**
     * 指纹特征ID
     */
    private String fingerprintId;

    /**
     * 密码哈希
     */
    private String passwordHash;

    /**
     * 二维码数据
     */
    private String qrCodeData;

    /**
     * 是否需要生物识别 (0-不需要, 1-需要)
     */
    private Integer requireBiometric;

    /**
     * 是否需要密码 (0-不需要, 1-需要)
     */
    private Integer requirePassword;

    /**
     * 扩展权限配置(JSON格式)
     */
    private String extendConfig;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否启用 (0-禁用, 1-启用)
     */
    private Integer enabledFlag;
}