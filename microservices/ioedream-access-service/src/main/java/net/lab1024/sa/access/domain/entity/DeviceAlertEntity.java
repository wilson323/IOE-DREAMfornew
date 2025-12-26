package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 设备告警实体类
 * <p>
 * 用于记录设备各类告警事件：
 * - 设备离线告警
 * - 设备故障告警
 * - 设备温度异常
 * - 网络异常告警
 * - 存储空间不足
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_alert")
@Schema(description = "设备告警实体")
public class DeviceAlertEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 告警ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "告警ID")
    private Long alertId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 设备类型
     * 1-门禁 2-考勤 3-消费 4-视频 5-访客
     */
    @Schema(description = "设备类型")
    private Integer deviceType;

    // ==================== 告警信息 ====================

    /**
     * 告警类型
     * DEVICE_OFFLINE-设备离线
     * DEVICE_FAULT-设备故障
     * TEMP_HIGH-温度过高
     * TEMP_LOW-温度过低
     * NETWORK_ERROR-网络异常
     * STORAGE_LOW-存储空间不足
     * POWER_LOW-电量不足
     * AUTH_FAILED-认证失败
     * DEVICE_BLOCKED-设备被阻挡
     * OTHER-其他
     */
    @Schema(description = "告警类型")
    private String alertType;

    /**
     * 告警级别
     * 1-低 2-中 3-高 4-紧急
     */
    @Schema(description = "告警级别：1-低 2-中 3-高 4-紧急")
    private Integer alertLevel;

    /**
     * 告警标题
     */
    @Schema(description = "告警标题")
    private String alertTitle;

    /**
     * 告警详细内容
     */
    @Schema(description = "告警详细内容")
    private String alertMessage;

    /**
     * 告警相关数据（JSON格式）
     * 用于存储告警相关的动态数据
     * 示例：{"temperature": 85, "threshold": 80, "deviceIp": "192.168.1.100"}
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @Schema(description = "告警相关数据（JSON格式）")
    private String alertData;

    // ==================== 状态管理 ====================

    /**
     * 告警状态
     * 0-未确认 1-已确认 2-已处理 3-已忽略
     */
    @Schema(description = "告警状态：0-未确认 1-已确认 2-已处理 3-已忽略")
    private Integer alertStatus;

    /**
     * 确认人ID
     */
    @Schema(description = "确认人ID")
    private Long confirmedBy;

    /**
     * 确认时间
     */
    @Schema(description = "确认时间")
    private LocalDateTime confirmedTime;

    /**
     * 确认备注
     */
    @Schema(description = "确认备注")
    private String confirmedRemark;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID")
    private Long handledBy;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间")
    private LocalDateTime handledTime;

    /**
     * 处理结果
     */
    @Schema(description = "处理结果")
    private String handledResult;

    // ==================== 时间记录 ====================

    /**
     * 告警发生时间
     */
    @Schema(description = "告警发生时间")
    private LocalDateTime alertOccurredTime;
}
