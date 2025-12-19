package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 用户访问报表响应
 */
@Data
public class UserAccessReportResponse {
    private Long totalUsers;
    private Long activeUsers;
    private List<Map<String, Object>> dailyData;
}
