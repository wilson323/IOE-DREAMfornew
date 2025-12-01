package net.lab1024.sa.device.domain.vo;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备数据视图对象
 * <p>
 * 用于返回设备采集的数据信息
 *
 * @author IOE-DREAM Team
 * @date 2025-01-30
 */
@Data
@Schema(description = "设备数据视图对象")
public class DeviceDataVO {

    /**
     * 数据ID
     */
    @Schema(description = "数据ID", example = "1001")
    private Long dataId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "DEV001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主门禁设备")
    private String deviceName;

    /**
     * 数据类型（STATUS-状态、ALARM-告警、METRIC-指标、EVENT-事件）
     */
    @Schema(description = "数据类型", example = "STATUS")
    private String dataType;

    /**
     * 数据值
     */
    @Schema(description = "数据值", example = "ONLINE")
    private String dataValue;

    /**
     * 数值型数据值（如果数据是数值类型）
     */
    @Schema(description = "数值型数据值", example = "25.5")
    private Double numericValue;

    /**
     * 数据单位
     */
    @Schema(description = "数据单位", example = "°C")
    private String unit;

    /**
     * 数据标签（JSON格式）
     */
    @Schema(description = "数据标签（JSON格式）", example = "{\"location\": \"大厅\", \"level\": \"INFO\"}")
    private String tags;

    /**
     * 扩展数据（JSON格式）
     */
    @Schema(description = "扩展数据（JSON格式）")
    private String extendData;

    /**
     * 扩展数据（Map格式）
     */
    @Schema(description = "扩展数据（Map格式）")
    private Map<String, Object> extendMap;

    /**
     * 数据采集时间
     */
    @Schema(description = "数据采集时间", example = "2025-01-30 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectTime;

    /**
     * 数据上报时间
     */
    @Schema(description = "数据上报时间", example = "2025-01-30 10:30:05")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    /**
     * 数据质量（GOOD-良好、WARNING-警告、ERROR-错误）
     */
    @Schema(description = "数据质量", example = "GOOD")
    private String dataQuality;

    /**
     * 数据状态（NORMAL-正常、ALARM-告警、FAULT-故障）
     */
    @Schema(description = "数据状态", example = "NORMAL")
    private String dataStatus;

    /**
     * 数据描述
     */
    @Schema(description = "数据描述", example = "设备在线状态正常")
    private String description;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
