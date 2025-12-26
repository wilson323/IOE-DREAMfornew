package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备告警查询表单
 * <p>
 * 用于查询设备告警记录，支持多种筛选条件
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
@Schema(description = "设备告警查询表单")
public class DeviceAlertQueryForm {

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
     * 设备类型
     * 1-门禁 2-考勤 3-消费 4-视频 5-访客
     */
    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

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
     * 告警级别
     * 1-低 2-中 3-高 4-紧急
     */
    @Schema(description = "告警级别", example = "3")
    private Integer alertLevel;

    /**
     * 告警状态
     * 0-未确认 1-已确认 2-已处理 3-已忽略
     */
    @Schema(description = "告警状态", example = "0")
    private Integer alertStatus;

    /**
     * 告警开始时间
     */
    @Schema(description = "告警开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime startTime;

    /**
     * 告警结束时间
     */
    @Schema(description = "告警结束时间", example = "2025-01-31T23:59:59")
    private LocalDateTime endTime;

    /**
     * 关键字搜索（设备名称/告警标题/告警内容）
     */
    @Schema(description = "关键字搜索", example = "温度异常")
    private String keyword;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    @Schema(description = "页大小", example = "20")
    private Integer pageSize = 20;
}
