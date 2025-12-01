package net.lab1024.sa.admin.module.device.domain.enums;

import lombok.Getter;

/**
 * 设备健康指标代码枚举
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Getter
public enum DeviceMetricCodeEnum {

    HEARTBEAT_LATENCY("heartbeat_latency_ms", "心跳延迟", "ms", 0.20),
    CPU_USAGE("cpu_usage_pct", "CPU使用率", "%", 0.15),
    TEMPERATURE("temperature_celsius", "设备温度", "℃", 0.10),
    COMMAND_SUCCESS_RATIO("command_success_ratio", "指令成功率", "%", 0.25),
    ALARM_COUNT("alarm_count", "告警数量", "个", 0.15),
    UPTIME_HOURS("uptime_hours", "连续运行时间", "h", 0.05),
    MAINTENANCE_DELAY("maintenance_delay_days", "维护延迟", "天", 0.10);

    private final String code;
    private final String description;
    private final String unit;
    private final Double defaultWeight;

    DeviceMetricCodeEnum(String code, String description, String unit, Double defaultWeight) {
        this.code = code;
        this.description = description;
        this.unit = unit;
        this.defaultWeight = defaultWeight;
    }

    /**
     * 根据代码获取指标枚举
     */
    public static DeviceMetricCodeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (DeviceMetricCodeEnum metric : values()) {
            if (metric.code.equals(code)) {
                return metric;
            }
        }
        return null;
    }
}