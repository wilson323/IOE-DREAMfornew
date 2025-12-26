package net.lab1024.sa.attendance.engine.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.SchedulePredictor;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithmFactory;
import net.lab1024.sa.attendance.engine.conflict.ConflictDetector;
import net.lab1024.sa.attendance.engine.conflict.ConflictResolver;
import net.lab1024.sa.attendance.engine.conflict.ScheduleConflictService;
import net.lab1024.sa.attendance.engine.execution.ScheduleExecutionService;
import net.lab1024.sa.attendance.engine.model.*;
import net.lab1024.sa.attendance.engine.optimization.ScheduleOptimizationService;
import net.lab1024.sa.attendance.engine.optimizer.ScheduleOptimizer;
import net.lab1024.sa.attendance.engine.prediction.SchedulePredictionService;
import net.lab1024.sa.attendance.engine.quality.ScheduleQualityService;
import net.lab1024.sa.attendance.entity.SmartSchedulePlanEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ScheduleEngineImpl 单元测试（P1-4创建）
 * <p>
 * 测试智能排班引擎Facade的委托功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0 (P2-Batch3)
 * @since 2025-12-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("智能排班引擎Facade测试")
@Slf4j
class ScheduleEngineImplTest {

    @Mock
    private ScheduleExecutionService scheduleExecutionService;

    @Mock
    private ScheduleConflictService scheduleConflictService;

    @Mock
    private ScheduleOptimizationService scheduleOptimizationService;

    @Mock
    private SchedulePredictionService schedulePredictionService;

    @Mock
    private ScheduleQualityService scheduleQualityService;

    private ScheduleEngine scheduleEngine;

    @BeforeEach
    void setUp() {
        log.info("[测试] 初始化智能排班引擎Facade");
        scheduleEngine = new ScheduleEngineImpl(
                scheduleExecutionService,
                scheduleConflictService,
                scheduleOptimizationService,
                schedulePredictionService,
                scheduleQualityService
        );
    }

    @Test
    @DisplayName("测试执行智能排班")
    void testExecuteIntelligentSchedule() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .planId(1L)
                .scheduleAlgorithm("SMART")
                .build();

        ScheduleResult expectedResult = ScheduleResult.builder()
                .planId(1L)
                .status("SUCCESS")
                .build();

        when(scheduleExecutionService.executeSchedule(any(ScheduleRequest.class)))
                .thenReturn(expectedResult);

        // When
        ScheduleResult result = scheduleEngine.executeIntelligentSchedule(request);

        // Then
        assertNotNull(result, "排班结果不应为null");
        assertEquals("SUCCESS", result.getStatus(), "排班状态应为SUCCESS");
        verify(scheduleExecutionService, times(1)).executeSchedule(request);
        log.info("[测试] 执行智能排班测试通过");
    }

    @Test
    @DisplayName("测试生成排班计划实体")
    void testGenerateSmartSchedulePlanEntity() {
        // Given
        Long planId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        SmartSchedulePlanEntity expectedEntity = new SmartSchedulePlanEntity();
        expectedEntity.setPlanId(planId);

        when(scheduleExecutionService.generatePlanEntity(eq(planId), eq(startDate), eq(endDate)))
                .thenReturn(expectedEntity);

        // When
        SmartSchedulePlanEntity result = scheduleEngine.generateSmartSchedulePlanEntity(
                planId, startDate, endDate
        );

        // Then
        assertNotNull(result, "排班计划实体不应为null");
        assertEquals(planId, result.getPlanId(), "计划ID应匹配");
        verify(scheduleExecutionService, times(1)).generatePlanEntity(planId, startDate, endDate);
        log.info("[测试] 生成排班计划实体测试通过");
    }

    @Test
    @DisplayName("测试验证排班冲突")
    void testValidateScheduleConflicts() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder()
                .planId(1L)
                .build();

        ConflictDetectionResult expectedResult = ConflictDetectionResult.builder()
                .hasConflicts(false)
                .build();

        when(scheduleConflictService.detectConflicts(any(ScheduleData.class)))
                .thenReturn(expectedResult);

        // When
        ConflictDetectionResult result = scheduleEngine.validateScheduleConflicts(scheduleData);

        // Then
        assertNotNull(result, "冲突检测结果不应为null");
        assertFalse(result.getHasConflicts(), "不应有冲突");
        verify(scheduleConflictService, times(1)).detectConflicts(scheduleData);
        log.info("[测试] 验证排班冲突测试通过");
    }

    @Test
    @DisplayName("测试解决排班冲突")
    void testResolveScheduleConflicts() {
        // Given
        List<ScheduleConflict> conflicts = new ArrayList<>();
        String resolutionStrategy = "AUTO";

        ConflictResolution expectedResult = ConflictResolution.builder()
                .resolutionSuccessful(true)
                .build();

        when(scheduleConflictService.resolveConflicts(anyList(), anyString()))
                .thenReturn(expectedResult);

        // When
        ConflictResolution result = scheduleEngine.resolveScheduleConflicts(
                conflicts, resolutionStrategy
        );

        // Then
        assertNotNull(result, "冲突解决结果不应为null");
        assertTrue(result.getResolutionSuccessful(), "解决应成功");
        verify(scheduleConflictService, times(1)).resolveConflicts(conflicts, resolutionStrategy);
        log.info("[测试] 解决排班冲突测试通过");
    }

    @Test
    @DisplayName("测试优化排班")
    void testOptimizeSchedule() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder()
                .planId(1L)
                .build();
        String optimizationTarget = "COST";

        OptimizedSchedule expectedResult = OptimizedSchedule.builder()
                .optimizationSuccessful(true)
                .build();

        when(scheduleOptimizationService.optimizeSchedule(any(ScheduleData.class), anyString()))
                .thenReturn(expectedResult);

        // When
        OptimizedSchedule result = scheduleEngine.optimizeSchedule(
                scheduleData, optimizationTarget
        );

        // Then
        assertNotNull(result, "优化排班结果不应为null");
        assertTrue(result.getOptimizationSuccessful(), "优化应成功");
        verify(scheduleOptimizationService, times(1)).optimizeSchedule(scheduleData, optimizationTarget);
        log.info("[测试] 优化排班测试通过");
    }

    @Test
    @DisplayName("测试预测排班效果")
    void testPredictScheduleEffect() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder()
                .planId(1L)
                .build();

        SchedulePrediction expectedResult = SchedulePrediction.builder()
                .predictionSuccessful(true)
                .build();

        when(schedulePredictionService.predictEffect(any(ScheduleData.class)))
                .thenReturn(expectedResult);

        // When
        SchedulePrediction result = scheduleEngine.predictScheduleEffect(scheduleData);

        // Then
        assertNotNull(result, "排班效果预测不应为null");
        assertTrue(result.getPredictionSuccessful(), "预测应成功");
        verify(schedulePredictionService, times(1)).predictEffect(scheduleData);
        log.info("[测试] 预测排班效果测试通过");
    }

    @Test
    @DisplayName("测试获取排班统计")
    void testGetScheduleStatistics() {
        // Given
        Long planId = 1L;

        // When
        ScheduleStatistics result = scheduleEngine.getScheduleStatistics(planId);

        // Then
        assertNotNull(result, "排班统计不应为null");
        assertEquals(planId, result.getPlanId(), "计划ID应匹配");
        log.info("[测试] 获取排班统计测试通过");
    }

    @Test
    @DisplayName("测试所有接口方法不为null")
    void testAllMethodsNotNull() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder().planId(1L).build();
        ScheduleData scheduleData = ScheduleData.builder().planId(1L).build();
        List<ScheduleConflict> conflicts = new ArrayList<>();

        ScheduleResult scheduleResult = ScheduleResult.builder()
                .planId(1L)
                .status("SUCCESS")
                .build();

        SmartSchedulePlanEntity planEntity = new SmartSchedulePlanEntity();
        planEntity.setPlanId(1L);

        ConflictDetectionResult conflictResult = ConflictDetectionResult.builder()
                .hasConflicts(false)
                .build();

        ConflictResolution resolution = ConflictResolution.builder()
                .resolutionSuccessful(true)
                .build();

        OptimizedSchedule optimizedSchedule = OptimizedSchedule.builder()
                .optimizationSuccessful(true)
                .build();

        SchedulePrediction prediction = SchedulePrediction.builder()
                .predictionSuccessful(true)
                .build();

        // Mock配置
        when(scheduleExecutionService.executeSchedule(any())).thenReturn(scheduleResult);
        when(scheduleExecutionService.generatePlanEntity(any(), any(), any())).thenReturn(planEntity);
        when(scheduleConflictService.detectConflicts(any())).thenReturn(conflictResult);
        when(scheduleConflictService.resolveConflicts(any(), any())).thenReturn(resolution);
        when(scheduleOptimizationService.optimizeSchedule(any(), any())).thenReturn(optimizedSchedule);
        when(schedulePredictionService.predictEffect(any())).thenReturn(prediction);

        // When & Then
        assertAll("所有接口方法返回不应为null",
                () -> assertNotNull(scheduleEngine.executeIntelligentSchedule(request)),
                () -> assertNotNull(scheduleEngine.generateSmartSchedulePlanEntity(1L,
                        LocalDate.now(), LocalDate.now())),
                () -> assertNotNull(scheduleEngine.validateScheduleConflicts(scheduleData)),
                () -> assertNotNull(scheduleEngine.resolveScheduleConflicts(conflicts, "AUTO")),
                () -> assertNotNull(scheduleEngine.optimizeSchedule(scheduleData, "COST")),
                () -> assertNotNull(scheduleEngine.predictScheduleEffect(scheduleData)),
                () -> assertNotNull(scheduleEngine.getScheduleStatistics(1L))
        );

        log.info("[测试] 所有接口方法不为null测试通过");
    }
}
