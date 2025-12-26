package net.lab1024.sa.attendance.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.SchedulePredictor;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithmFactory;
import net.lab1024.sa.attendance.engine.conflict.ConflictDetector;
import net.lab1024.sa.attendance.engine.conflict.ConflictResolver;
import net.lab1024.sa.attendance.engine.conflict.ScheduleConflictService;
import net.lab1024.sa.attendance.engine.execution.ScheduleExecutionService;
import net.lab1024.sa.attendance.engine.impl.ScheduleEngineImpl;
import net.lab1024.sa.attendance.engine.optimization.ScheduleOptimizationService;
import net.lab1024.sa.attendance.engine.optimizer.ScheduleOptimizer;
import net.lab1024.sa.attendance.engine.prediction.SchedulePredictionService;
import net.lab1024.sa.attendance.engine.quality.ScheduleQualityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能排班引擎配置类（P2-Batch3创建）
 * <p>
 * 负责注册智能排班相关的所有服务和组件为Spring Bean
 * 使用构造函数注入，确保依赖关系的正确性
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Configuration
public class ScheduleEngineConfiguration {

    /**
     * 注册排班执行服务
     * <p>
     * 负责排班执行的核心流程，包括请求验证、数据准备、排班生成、统计生成等
     * </p>
     */
    @Bean
    public ScheduleExecutionService scheduleExecutionService(
            ScheduleAlgorithmFactory scheduleAlgorithmFactory,
            ConflictDetector conflictDetector,
            ConflictResolver conflictResolver,
            ScheduleOptimizer scheduleOptimizer) {
        log.info("[排班配置] 注册排班执行服务为Spring Bean");
        return new ScheduleExecutionService(
                scheduleAlgorithmFactory,
                conflictDetector,
                conflictResolver,
                scheduleOptimizer
        );
    }

    /**
     * 注册冲突处理服务
     * <p>
     * 负责检测和解决排班冲突，包括技能冲突、工时冲突、容量冲突等
     * </p>
     */
    @Bean
    public ScheduleConflictService scheduleConflictService(
            ConflictDetector conflictDetector,
            ConflictResolver conflictResolver) {
        log.info("[排班配置] 注册冲突处理服务为Spring Bean");
        return new ScheduleConflictService(
                conflictDetector,
                conflictResolver
        );
    }

    /**
     * 注册排班优化服务
     * <p>
     * 负责优化排班结果，根据不同的优化目标生成优化后的排班方案
     * </p>
     */
    @Bean
    public ScheduleOptimizationService scheduleOptimizationService(
            ScheduleOptimizer scheduleOptimizer) {
        log.info("[排班配置] 注册排班优化服务为Spring Bean");
        return new ScheduleOptimizationService(scheduleOptimizer);
    }

    /**
     * 注册排班预测服务
     * <p>
     * 负责预测排班方案的效果，包括工作负载分布、成本预测等
     * </p>
     */
    @Bean
    public SchedulePredictionService schedulePredictionService(
            SchedulePredictor schedulePredictor) {
        log.info("[排班配置] 注册排班预测服务为Spring Bean");
        return new SchedulePredictionService(schedulePredictor);
    }

    /**
     * 注册质量评估服务
     * <p>
     * 负责评估排班质量并生成改进建议，包括质量评分、审核判断、推荐建议等
     * </p>
     */
    @Bean
    public ScheduleQualityService scheduleQualityService() {
        log.info("[排班配置] 注册质量评估服务为Spring Bean");
        return new ScheduleQualityService();
    }

    /**
     * 注册智能排班引擎
     * <p>
     * 智能排班引擎的Facade实现，委托给专业服务完成具体功能
     * 依赖5个专业服务，使用构造函数注入
     * </p>
     */
    @Bean
    public ScheduleEngine scheduleEngine(
            ScheduleExecutionService scheduleExecutionService,
            ScheduleConflictService scheduleConflictService,
            ScheduleOptimizationService scheduleOptimizationService,
            SchedulePredictionService schedulePredictionService,
            ScheduleQualityService scheduleQualityService) {
        log.info("[排班配置] 注册智能排班引擎为Spring Bean");
        log.info("[排班配置] 依赖服务: 执行服务、冲突服务、优化服务、预测服务、质量服务");
        return new ScheduleEngineImpl(
                scheduleExecutionService,
                scheduleConflictService,
                scheduleOptimizationService,
                schedulePredictionService,
                scheduleQualityService
        );
    }
}
