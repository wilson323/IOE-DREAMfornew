package net.lab1024.sa.common.monitor.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统健康VO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@JsonFormat格式化时间字段
 * - 完整的字段注释
 * </p>
 * 整合自ioedream-monitor-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自monitor-service）
 */
@Data
@Schema(description = "系统健康VO")
public class SystemHealthVO {

    @Schema(description = "系统状态")
    private String status;

    @Schema(description = "整体健康状态")
    private String overallStatus;

    @Schema(description = "健康评分 (0-100)")
    private Integer healthScore;

    @Schema(description = "系统运行时间（秒）")
    private Long systemUptime;

    @Schema(description = "微服务总数")
    private Integer totalServices;

    @Schema(description = "在线服务数")
    private Integer onlineServices;

    @Schema(description = "离线服务数")
    private Integer offlineServices;

    @Schema(description = "活跃告警数")
    private Integer activeAlerts;

    @Schema(description = "严重告警数")
    private Integer criticalAlerts;

    @Schema(description = "警告告警数")
    private Integer warningAlerts;

    @Schema(description = "CPU使用率")
    private Double cpuUsage;

    @Schema(description = "内存使用率")
    private Double memoryUsage;

    @Schema(description = "磁盘使用率")
    private Double diskUsage;

    @Schema(description = "活跃线程数")
    private Integer activeThreads;

    @Schema(description = "数据库连接数")
    private Integer dbConnections;

    @Schema(description = "系统运行时间")
    private Long uptime;

    @Schema(description = "资源使用详情")
    private ResourceUsageVO resourceUsage;

    @Schema(description = "组件健康列表")
    private List<ComponentHealthVO> componentHealthList;

    @Schema(description = "性能指标")
    private Map<String, Object> performanceMetrics;

    @Schema(description = "最后检查时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCheckTime;

    @Schema(description = "健康趋势")
    private List<Map<String, Object>> healthTrends;

    @Schema(description = "活跃告警列表")
    private List<AlertSummaryVO> activeAlertList;

    /**
     * 获取总体状态
     */
    public String getOverallStatus() {
        return this.overallStatus != null ? this.overallStatus : this.status;
    }
}
