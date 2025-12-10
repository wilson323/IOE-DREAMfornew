package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 编译统计信息
 * <p>
 * JIT编译器的详细统计信息
 * 包含编译时间、编译方法数等统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class CompilationStatistics {

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * JIT编译器名称
     */
    private String compilerName;

    /**
     * 编译总时间（毫秒）
     */
    private long totalCompilationTime;

    /**
     * 已编译方法数
     */
    private long compiledMethodCount;

    /**
     * 标准编译方法数
     */
    private long standardCompilationCount;

    /**
     * 优化编译方法数
     */
    private long osrCompilationCount;

    /**
     * 编译失败方法数
     */
    private long failedCompilationCount;

    /**
     * 编译队列大小
     */
    private int compilationQueueSize;

    /**
     * 编译器是否启用
     */
    private boolean compilationEnabled;

    /**
     * 平均编译时间（毫秒）
     */
    private double averageCompilationTime;

    /**
     * 编译效率（编译方法数/毫秒）
     */
    private double compilationEfficiency;
}