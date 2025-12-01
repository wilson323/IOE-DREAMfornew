package net.lab1024.sa.audit.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 审计统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class AuditStatisticsVO {

    /**
     * 总操作数
     */
    private Long totalOperations;

    /**
     * 成功操作数
     */
    private Long successOperations;

    /**
     * 失败操作数
     */
    private Long failureOperations;

    /**
     * 成功率
     */
    private Double successRate;

    /**
     * 总日志数
     */
    private Long totalLogs;

    /**
     * 今日日志数
     */
    private Long todayLogs;

    /**
     * 本周日志数
     */
    private Long weekLogs;

    /**
     * 本月日志数
     */
    private Long monthLogs;

    /**
     * 操作统计
     */
    private Map<String, Long> operationStatistics;

    /**
     * 用户统计
     */
    private Map<String, Long> userStatistics;

    /**
     * 资源统计
     */
    private Map<String, Long> resourceStatistics;

    /**
     * 风险统计（Map格式）
     */
    private Map<String, Long> riskLevelStatisticsMap;

    /**
     * 成功率统计
     */
    private Map<String, Double> successRateStatistics;

    /**
     * 操作类型统计列表
     */
    private List<OperationTypeStatisticsVO> operationTypeStatistics;

    /**
     * 模块统计列表
     */
    private List<ModuleStatisticsVO> moduleStatistics;

    /**
     * 风险等级统计列表
     */
    private List<RiskLevelStatisticsVO> riskLevelStatistics;

    /**
     * 每日统计列表
     */
    private List<DailyStatisticsVO> dailyStatistics;

    /**
     * 用户活跃度统计列表
     */
    private List<UserActivityStatisticsVO> userActivityStatistics;

    /**
     * 失败原因统计列表
     */
    private List<FailureReasonStatisticsVO> failureReasonStatistics;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 生成时间
     */
    private LocalDateTime generatedTime;
}
