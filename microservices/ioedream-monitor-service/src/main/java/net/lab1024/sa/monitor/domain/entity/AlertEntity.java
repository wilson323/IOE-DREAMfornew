package net.lab1024.sa.monitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 告警实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_alert")
public class AlertEntity {

    /**
     * 告警ID
     */
    @TableId(type = IdType.AUTO)
    private Long alertId;

    /**
     * 告警规则ID
     */
    private Long ruleId;

    /**
     * 告警标题
     */
    private String alertTitle;

    /**
     * 告警描述
     */
    private String alertDescription;

    /**
     * 告警级别 (INFO、WARNING、ERROR、CRITICAL)
     */
    private String alertLevel;

    /**
     * 告警类型 (SYSTEM、SERVICE、PERFORMANCE、SECURITY)
     */
    private String alertType;

    /**
     * 关联服务
     */
    private String serviceName;

    /**
     * 关联实例
     */
    private String instanceId;

    /**
     * 告警状态 (ACTIVE、RESOLVED、SUPPRESSED)
     */
    private String status;

    /**
     * 告警源
     */
    private String alertSource;

    /**
     * 告警标签
     */
    private String alertTags;

    /**
     * 告警值
     */
    private Double alertValue;

    /**
     * 阈值
     */
    private Double thresholdValue;

    /**
     * 告警时间
     */
    private LocalDateTime alertTime;

    /**
     * 解决时间
     */
    private LocalDateTime resolveTime;

    /**
     * 解决人
     */
    private Long resolveUserId;

    /**
     * 解决说明
     */
    private String resolution;

    /**
     * 通知状态 (PENDING、SENT、FAILED)
     */
    private String notificationStatus;

    /**
     * 通知次数
     */
    private Integer notificationCount;

    /**
     * 最后通知时间
     */
    private LocalDateTime lastNotificationTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 删除标记
     */
    private Integer deletedFlag;
}