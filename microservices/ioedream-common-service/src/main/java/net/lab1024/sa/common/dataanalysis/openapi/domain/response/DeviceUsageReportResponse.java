package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 设备使用报表响应
 */
@Data
public class DeviceUsageReportResponse {
    private Long totalDevices;
    private Long activeDevices;
    private List<Map<String, Object>> usageStats;
}
