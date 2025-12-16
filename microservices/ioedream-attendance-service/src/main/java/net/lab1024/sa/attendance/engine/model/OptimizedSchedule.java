package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 优化排班模型
 * <p>
 * 排班优化后的结果数据结构
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
public class OptimizedSchedule {

    /**
     * 优化ID
     */
    private String optimizationId;

    /**
     * 原始排班ID
     */
    private Long originalScheduleId;

    /**
     * 优化状态
     */
    private String status;

    /**
     * 优化算法
     */
    private String optimizationAlgorithm;

    /**
     * 优化目标
     */
    private String optimizationTarget;

    /**
     * 原始得分
     */
    private Double originalScore;

    /**
     * 优化后得分
     */
    private Double optimizedScore;

    /**
     * 改进率
     */
    private Double improvementRate;

    /**
     * 优化时间
     */
    private LocalDateTime optimizationTime;

    /**
     * 优化耗时（毫秒）
     */
    private Long optimizationDuration;

    /**
     * 优化记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 优化指标
     */
    private Map<String, Object> optimizationMetrics;

    /**
     * 优化建议
     */
    private List<String> optimizationSuggestions;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;
}