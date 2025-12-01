/*
 * 检测性能指标
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.result;

import java.time.LocalDateTime;
import java.util.Map;


/**
 * 检测性能指标
 * 封装异常检测系统的性能指标信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */




public class DetectionPerformanceMetrics {

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 总检测次数
     */
    private long totalDetections;

    /**
     * 平均检测耗时（毫秒）
     */
    private double averageDetectionTime;

    /**
     * 检测准确率
     */
    private double accuracy;

    /**
     * 精确率
     */
    private double precision;

    /**
     * 召回率
     */
    private double recall;

    /**
     * F1分数
     */
    private double f1Score;

    /**
     * 误报率
     */
    private double falsePositiveRate;

    /**
     * 漏报率
     */
    private double falseNegativeRate;

    /**
     * 检测次数
     */
    private long detectionCount;

    /**
     * 检测规则命中率
     */
    private Map<String, Double> ruleHitRate;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    // 手动添加的getter/setter方法 (Lombok失效备用)
















}
