package net.lab1024.sa.common.monitor.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警实体
 * 整合自ioedream-monitor-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自monitor-service）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_alert")
public class AlertEntity {

    /**
     * 告警ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列alert_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "alert_id", type = IdType.AUTO)
    private Long id;

    private Long ruleId;
    private String alertTitle;
    private String alertDescription;
    private String alertMessage;
    private String alertLevel;
    private String alertType;
    private String serviceName;
    private String instanceId;
    private String status;
    private String alertSource;
    private String alertTags;
    private Double alertValue;
    private Double thresholdValue;
    private LocalDateTime alertTime;
    private LocalDateTime resolveTime;
    private LocalDateTime resolvedTime;
    private Long resolveUserId;
    private String resolution;
    private String resolutionNotes;
    private String notificationStatus;
    private Integer notificationCount;
    private LocalDateTime lastNotificationTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUserId;
    private Integer deletedFlag;
}
