package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 门禁告警实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_access_alert")
public class SmartAccessAlertEntity extends BaseEntity {

    /**
     * 告警ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long alertId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 告警类型
     */
    private String alertType;

    /**
     * 严重级别
     */
    private String severityLevel;

    /**
     * 告警标题
     */
    private String alertTitle;

    /**
     * 告警内容
     */
    private String alertContent;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 处理状态
     */
    private String processStatus;
}