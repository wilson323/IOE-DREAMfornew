package net.lab1024.sa.device.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 设备健康报告视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
public class DeviceHealthReportVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 报告类型
     */
    private String reportType;

    /**
     * 报告生成时间
     */
    private LocalDateTime generateTime;

    /**
     * 报告时间范围开始
     */
    private LocalDateTime startTime;

    /**
     * 报告时间范围结束
     */
    private LocalDateTime endTime;

    /**
     * 当前健康评分
     */
    private BigDecimal currentHealthScore;

    /**
     * 平均健康评分
     */
    private BigDecimal averageHealthScore;

    /**
     * 健康等级
     */
    private Integer healthLevel;

    /**
     * 健康状态描述
     */
    private String healthStatus;

    /**
     * 健康趋势(上升/下降/稳定)
     */
    private String healthTrend;

    /**
     * 各项指标数据
     */
    private Map<String, Object> metrics;

    /**
     * 故障记录列表
     */
    private List<Map<String, Object>> faultRecords;

    /**
     * 维护记录列表
     */
    private List<Map<String, Object>> maintenanceRecords;

    /**
     * 告警记录列表
     */
    private List<Map<String, Object>> alarmRecords;

    /**
     * 维护建议列表
     */
    private List<Map<String, Object>> maintenanceSuggestions;

    /**
     * 故障预测信息
     */
    private Map<String, Object> failurePrediction;

    /**
     * 统计摘要
     */
    private Map<String, Object> summary;
}
