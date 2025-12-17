package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 仪表板查询请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardQueryRequest {
    /** 仪表板类型 */
    private String dashboardType;
    /** 刷新间隔(秒) */
    private Integer refreshInterval;
    /** 区域ID */
    private Long areaId;
    /** 指标列表 */
    private List<String> metrics;
}
