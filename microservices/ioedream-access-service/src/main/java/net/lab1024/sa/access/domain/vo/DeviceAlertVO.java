package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备告警VO
 * <p>
 * 用于返回设备告警信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备告警VO")
public class DeviceAlertVO {

    /**
     * 告警ID
     */
    @Schema(description = "告警ID", example = "1001")
    private Long alertId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "AC-001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "A栋1楼门禁")
    private String deviceName;

    /**
     * 设备类型
     * 1-门禁 2-考勤 3-消费 4-视频 5-访客
     */
    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    /**
     * 设备类型名称
     */
    @Schema(description = "设备类型名称", example = "门禁设备")
    private String deviceTypeName;

    /**
     * 告警类型
     * DEVICE_OFFLINE-设备离线
     * DEVICE_FAULT-设备故障
     * TEMP_HIGH-温度过高
     * NETWORK_ERROR-网络异常
     * STORAGE_LOW-存储空间不足
     */
    @Schema(description = "告警类型", example = "DEVICE_OFFLINE")
    private String alertType;

    /**
     * 告警类型名称
     */
    @Schema(description = "告警类型名称", example = "设备离线")
    private String alertTypeName;

    /**
     * 告警级别
     * 1-低 2-中 3-高 4-紧急
     */
    @Schema(description = "告警级别", example = "3")
    private Integer alertLevel;

    /**
     * 告警级别名称
     */
    @Schema(description = "告警级别名称", example = "高")
    private String alertLevelName;

    /**
     * 告警级别颜色（前端显示用）
     */
    @Schema(description = "告警级别颜色", example = "#FF5722")
    private String alertLevelColor;

    /**
     * 告警标题
     */
    @Schema(description = "告警标题", example = "设备离线告警：A栋1楼门禁已离线超过5分钟")
    private String alertTitle;

    /**
     * 告警详细内容
     */
    @Schema(description = "告警详细内容", example = "设备于2025-01-30 10:00:00离线，已超过5分钟未上线")
    private String alertMessage;

    /**
     * 告警相关数据（JSON格式）
     */
    @Schema(description = "告警相关数据")
    private String alertData;

    /**
     * 告警状态
     * 0-未确认 1-已确认 2-已处理 3-已忽略
     */
    @Schema(description = "告警状态", example = "0")
    private Integer alertStatus;

    /**
     * 告警状态名称
     */
    @Schema(description = "告警状态名称", example = "未确认")
    private String alertStatusName;

    /**
     * 确认人ID
     */
    @Schema(description = "确认人ID", example = "10001")
    private Long confirmedBy;

    /**
     * 确认人姓名
     */
    @Schema(description = "确认人姓名", example = "张三")
    private String confirmedByName;

    /**
     * 确认时间
     */
    @Schema(description = "确认时间", example = "2025-01-30T10:05:00")
    private LocalDateTime confirmedTime;

    /**
     * 确认备注
     */
    @Schema(description = "确认备注", example = "已现场确认，设备电源故障")
    private String confirmedRemark;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID", example = "10002")
    private Long handledBy;

    /**
     * 处理人姓名
     */
    @Schema(description = "处理人姓名", example = "李四")
    private String handledByName;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间", example = "2025-01-30T11:00:00")
    private LocalDateTime handledTime;

    /**
     * 处理结果
     */
    @Schema(description = "处理结果", example = "已更换电源，设备恢复正常")
    private String handledResult;

    /**
     * 告警发生时间
     */
    @Schema(description = "告警发生时间", example = "2025-01-30T10:00:00")
    private LocalDateTime alertOccurredTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-30T10:05:00")
    private LocalDateTime updateTime;
}
