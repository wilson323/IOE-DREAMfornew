package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 门禁报警视图对象
 * <p>
 * 用于实时监控模块显示报警信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessAlarmVO {

    /**
     * 报警ID
     */
    private Long alarmId;

    /**
     * 报警类型
     * <p>
     * 可能的值：
     * - DEVICE_OFFLINE - 设备离线
     * - DEVICE_FAULT - 设备故障
     * - UNAUTHORIZED_ACCESS - 未授权通行
     * - UNAUTHORIZED_TIME - 非授权时间通行
     * - ANTI_PASSBACK - 反潜回违规
     * - MULTI_PERSON - 多人验证失败
     * - EMERGENCY - 紧急报警
     * </p>
     */
    private String alarmType;

    /**
     * 报警类型名称
     */
    private String alarmTypeName;

    /**
     * 报警级别
     * <p>
     * 可能的值：
     * - LOW - 低级别
     * - MEDIUM - 中级别
     * - HIGH - 高级别
     * - CRITICAL - 紧急
     * </p>
     */
    private String alarmLevel;

    /**
     * 报警级别名称
     */
    private String alarmLevelName;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 用户ID（如果相关）
     */
    private Long userId;

    /**
     * 用户名称（如果相关）
     */
    private String userName;

    /**
     * 报警内容
     */
    private String alarmContent;

    /**
     * 报警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 处理状态
     * <p>
     * 0-未处理
     * 1-处理中
     * 2-已处理
     * 3-已忽略
     * </p>
     */
    private Integer handleStatus;

    /**
     * 处理状态名称
     */
    private String handleStatusName;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人名称
     */
    private String handlerName;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
