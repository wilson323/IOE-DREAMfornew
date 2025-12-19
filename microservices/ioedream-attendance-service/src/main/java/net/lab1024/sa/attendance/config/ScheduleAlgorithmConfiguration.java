package net.lab1024.sa.attendance.config;

import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithm;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithmFactory;
import net.lab1024.sa.attendance.engine.algorithm.impl.*;
import net.lab1024.sa.attendance.engine.conflict.ConflictDetector;
import net.lab1024.sa.attendance.engine.conflict.ConflictResolver;
import net.lab1024.sa.attendance.engine.conflict.impl.ConflictDetectorImpl;
import net.lab1024.sa.attendance.engine.conflict.impl.ConflictResolverImpl;
import net.lab1024.sa.attendance.engine.impl.ScheduleEngineImpl;
import net.lab1024.sa.attendance.engine.optimizer.ScheduleOptimizer;
import net.lab1024.sa.attendance.engine.optimizer.impl.ScheduleOptimizerImpl;
import net.lab1024.sa.attendance.engine.prediction.SchedulePredictor;
import net.lab1024.sa.attendance.engine.prediction.impl.SchedulePredictorImpl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能排班算法配置类
 * <p>
 * 配置排班引擎的所有算法和组件
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2022025-12-16
 */
@Configuration
public class ScheduleAlgorithmConfiguration {

    /**
     * 配置排班算法工厂
     */
    @Bean
    public ScheduleAlgorithmFactory scheduleAlgorithmFactory() {
        ScheduleAlgorithmFactory factory = new ScheduleAlgorithmFactory();

        // 注册自定义算法提供者（如果需要）
        registerCustomAlgorithms(factory);

        return factory;
    }

    /**
     * 配置排班引擎（纯Java类，构造函数注入）
     */
    @Bean
    public ScheduleEngine scheduleEngine(ScheduleAlgorithmFactory algorithmFactory,
                                         ConflictDetector conflictDetector,
                                         ConflictResolver conflictResolver,
                                         ScheduleOptimizer scheduleOptimizer,
                                         SchedulePredictor schedulePredictor) {

        // 移除Spring注解，通过构造函数注入依赖
        return new ScheduleEngineImpl(
                algorithmFactory,
                conflictDetector,
                conflictResolver,
                scheduleOptimizer,
                schedulePredictor
        );
    }

    /**
     * 配置冲突检测器
     */
    @Bean
    public ConflictDetector conflictDetector() {
        return new ConflictDetectorImpl();
    }

    /**
     * 配置冲突解决器
     */
    @Bean
    public ConflictResolver conflictResolver() {
        return new ConflictResolverImpl();
    }

    /**
     * 配置排班优化器
     */
    @Bean
    public ScheduleOptimizer scheduleOptimizer() {
        return new ScheduleOptimizerImpl();
    }

    /**
     * 配置排班预测器
     */
    @Bean
    public SchedulePredictor schedulePredictor() {
        return new SchedulePredictorImpl();
    }

    /**
     * 注册自定义算法（如果需要）
     */
    private void registerCustomAlgorithms(ScheduleAlgorithmFactory factory) {
        // 可以在这里注册自定义算法实现
        // factory.registerAlgorithm("CUSTOM", new CustomAlgorithmProvider());
    }

    /**
     * 自定义算法提供者（示例）
     */
    private static class CustomAlgorithmProvider implements ScheduleAlgorithmFactory.AlgorithmProvider {
        @Override
        public ScheduleAlgorithm createAlgorithm() {
            // 返回自定义算法实例
            return new HeuristicAlgorithmImpl();
        }
    }
}
