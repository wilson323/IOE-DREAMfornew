package net.lab1024.sa.base.common.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备状态日志实体类
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_status_log")
public class DeviceStatusLogEntity extends net.lab1024.sa.base.common.entity.BaseEntity {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 日志类型（STATUS_CHANGE, OPERATION, ERROR, HEARTBEAT）
     */
    private String logType;

    /**
     * 日志内容
     */
    private String logContent;

    /**
     * 日志级别（INFO, WARN, ERROR, DEBUG）
     */
    private String logLevel;

    /**
     * 日志时间
     */
    private LocalDateTime logTime;

    /**
     * 操作用户ID（如果适用）
     */
    private Long operateUserId;

    /**
     * 操作用户名（如果适用）
     */
    private String operateUserName;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 扩展信息（JSON格式）
     */
    private String extraInfo;
}