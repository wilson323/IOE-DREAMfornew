package net.lab1024.sa.monitor.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统健康概览VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class SystemHealthVO {

    /**
     * 整体健康状态 (HEALTHY、WARNING、CRITICAL)
     */
    private String overallStatus;

    /**
     * 健康评分 (0-100)
     */
    private Integer healthScore;

    /**
     * 系统运行时间（秒）
     */
    private Long systemUptime;

    /**
     * 服务总数
     */
    private Integer totalServices;

    /**
     * 在线服务数
     */
    private Integer onlineServices;

    /**
     * 离线服务数
     */
    private Integer offlineServices;

    /**
     * 活跃告警数
     */
    private Integer activeAlerts;

    /**
     * 严重告警数
     */
    private Integer criticalAlerts;

    /**
     * 警告告警数
     */
    private Integer warningAlerts;

    /**
     * CPU使用率
     */
    private Double cpuUsage;

    /**
     * 内存使用率
     */
    private Double memoryUsage;

    /**
     * 磁盘使用率
     */
    private Double diskUsage;

    /**
     * 网络状态
     */
    private String networkStatus;

    /**
     * 数据库状态
     */
    private String databaseStatus;

    /**
     * 缓存状态
     */
    private String cacheStatus;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 组件健康状态
     */
    private List<ComponentHealthVO> componentHealthList;

    /**
     * 性能指标
     */
    private Map<String, Object> performanceMetrics;

    /**
     * 资源使用情况
     */
    private ResourceUsageVO resourceUsage;

    /**
     * 活跃告警列表
     */
    private List<AlertSummaryVO> activeAlertList;

    /**
     * 健康趋势数据
     */
    private List<Map<String, Object>> healthTrends;
}