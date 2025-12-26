package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 移动端性能测试结果
 * <p>
 * 封装移动端性能测试响应结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端性能测试结果")
public class MobilePerformanceTestResult {

    /**
     * 测试类型
     */
    @Schema(description = "测试类型", example = "API_RESPONSE")
    private String testType;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 响应时间（毫秒）
     */
    @Schema(description = "响应时间（毫秒）", example = "150")
    private Long responseTime;

    /**
     * 吞吐量（请求/秒）
     */
    @Schema(description = "吞吐量（请求/秒）", example = "1000")
    private Long throughput;

    /**
     * 数据库查询时间（毫秒）
     */
    @Schema(description = "数据库查询时间（毫秒）", example = "50")
    private Long dbQueryTime;

    /**
     * Redis访问时间（毫秒）
     */
    @Schema(description = "Redis访问时间（毫秒）", example = "10")
    private Long redisTime;

    /**
     * 网关调用时间（毫秒）
     */
    @Schema(description = "网关调用时间（毫秒）", example = "30")
    private Long gatewayTime;

    /**
     * 性能指标
     */
    @Schema(description = "性能指标")
    private Map<String, Object> performanceMetrics;

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;
}


