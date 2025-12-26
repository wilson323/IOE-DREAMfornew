package net.lab1024.sa.attendance.config;

import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.SchedulePredictor;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithmFactory;
import net.lab1024.sa.attendance.engine.conflict.ConflictDetector;
import net.lab1024.sa.attendance.engine.conflict.ConflictResolver;
import net.lab1024.sa.attendance.engine.conflict.ScheduleConflictService;
import net.lab1024.sa.attendance.engine.execution.ScheduleExecutionService;
import net.lab1024.sa.attendance.engine.optimization.ScheduleOptimizationService;
import net.lab1024.sa.attendance.engine.optimizer.ScheduleOptimizer;
import net.lab1024.sa.attendance.engine.prediction.SchedulePredictionService;
import net.lab1024.sa.attendance.engine.quality.ScheduleQualityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ScheduleEngineConfiguration 单元测试（P1-4创建）
 * <p>
 * 测试智能排班引擎配置类的Bean注册功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("智能排班引擎配置测试")
class ScheduleEngineConfigurationTest {

    @Mock
    private ScheduleAlgorithmFactory scheduleAlgorithmFactory;

    @Mock
    private ConflictDetector conflictDetector;

    @Mock
    private ConflictResolver conflictResolver;

    @Mock
    private ScheduleOptimizer scheduleOptimizer;

    @Mock
    private SchedulePredictor schedulePredictor;

    private ScheduleEngineConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new ScheduleEngineConfiguration();
    }

    @Test
    @DisplayName("测试注册排班执行服务")
    void testScheduleExecutionService() {
        // When
        ScheduleExecutionService service = configuration.scheduleExecutionService(
                scheduleAlgorithmFactory,
                conflictDetector,
                conflictResolver,
                scheduleOptimizer
        );

        // Then
        assertNotNull(service, "排班执行服务不应为null");
    }

    @Test
    @DisplayName("测试注册冲突处理服务")
    void testScheduleConflictService() {
        // When
        ScheduleConflictService service = configuration.scheduleConflictService(
                conflictDetector,
                conflictResolver
        );

        // Then
        assertNotNull(service, "冲突处理服务不应为null");
    }

    @Test
    @DisplayName("测试注册排班优化服务")
    void testScheduleOptimizationService() {
        // When
        ScheduleOptimizationService service = configuration.scheduleOptimizationService(
                scheduleOptimizer
        );

        // Then
        assertNotNull(service, "排班优化服务不应为null");
    }

    @Test
    @DisplayName("测试注册排班预测服务")
    void testSchedulePredictionService() {
        // When
        SchedulePredictionService service = configuration.schedulePredictionService(
                schedulePredictor
        );

        // Then
        assertNotNull(service, "排班预测服务不应为null");
    }

    @Test
    @DisplayName("测试注册质量评估服务")
    void testScheduleQualityService() {
        // When
        ScheduleQualityService service = configuration.scheduleQualityService();

        // Then
        assertNotNull(service, "质量评估服务不应为null");
    }

    @Test
    @DisplayName("测试注册智能排班引擎")
    void testScheduleEngine() {
        // Given
        ScheduleExecutionService executionService = configuration.scheduleExecutionService(
                scheduleAlgorithmFactory,
                conflictDetector,
                conflictResolver,
                scheduleOptimizer
        );

        ScheduleConflictService conflictService = configuration.scheduleConflictService(
                conflictDetector,
                conflictResolver
        );

        ScheduleOptimizationService optimizationService = configuration.scheduleOptimizationService(
                scheduleOptimizer
        );

        SchedulePredictionService predictionService = configuration.schedulePredictionService(
                schedulePredictor
        );

        ScheduleQualityService qualityService = configuration.scheduleQualityService();

        // When
        ScheduleEngine engine = configuration.scheduleEngine(
                executionService,
                conflictService,
                optimizationService,
                predictionService,
                qualityService
        );

        // Then
        assertNotNull(engine, "智能排班引擎不应为null");
    }

    @Test
    @DisplayName("测试所有Bean注册不为null")
    void testAllBeansNotNull() {
        // Given & When
        ScheduleExecutionService executionService = configuration.scheduleExecutionService(
                scheduleAlgorithmFactory,
                conflictDetector,
                conflictResolver,
                scheduleOptimizer
        );

        ScheduleConflictService conflictService = configuration.scheduleConflictService(
                conflictDetector,
                conflictResolver
        );

        ScheduleOptimizationService optimizationService = configuration.scheduleOptimizationService(
                scheduleOptimizer
        );

        SchedulePredictionService predictionService = configuration.schedulePredictionService(
                schedulePredictor
        );

        ScheduleQualityService qualityService = configuration.scheduleQualityService();

        ScheduleEngine engine = configuration.scheduleEngine(
                executionService,
                conflictService,
                optimizationService,
                predictionService,
                qualityService
        );

        // Then
        assertAll("所有Bean不应为null",
                () -> assertNotNull(executionService, "排班执行服务不应为null"),
                () -> assertNotNull(conflictService, "冲突处理服务不应为null"),
                () -> assertNotNull(optimizationService, "排班优化服务不应为null"),
                () -> assertNotNull(predictionService, "排班预测服务不应为null"),
                () -> assertNotNull(qualityService, "质量评估服务不应为null"),
                () -> assertNotNull(engine, "智能排班引擎不应为null")
        );
    }
}
