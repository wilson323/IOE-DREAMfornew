package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 设备使用查询请求
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUsageQueryRequest {
    private String startDate;
    private String endDate;
    private Long areaId;
    private String deviceType;
    private List<String> metrics;
}
