package net.lab1024.sa.attendance.engine.optimization;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.OptimizedSchedule;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;
import net.lab1024.sa.attendance.engine.optimizer.ScheduleOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ScheduleOptimizationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("排班优化服务测试")
@Slf4j
class ScheduleOptimizationServiceTest {

    @Mock
    private ScheduleOptimizer scheduleOptimizer;

    private ScheduleOptimizationService scheduleOptimizationService;

    @BeforeEach
    void setUp() {
        scheduleOptimizationService = new ScheduleOptimizationService(scheduleOptimizer);
    }

    @Test
    @DisplayName("测试优化排班-成本优化")
    void testOptimizeSchedule_Cost() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder().planId(1L).build();
        OptimizedSchedule expectedResult = OptimizedSchedule.builder()
                .optimizationSuccessful(true)
                .optimizationTarget("COST")
                .build();

        when(scheduleOptimizer.optimizeSchedule(any(), anyString()))
                .thenReturn(expectedResult);

        // When
        OptimizedSchedule result = scheduleOptimizationService.optimizeSchedule(
                scheduleData, "COST"
        );

        // Then
        assertNotNull(result);
        assertTrue(result.getOptimizationSuccessful());
        verify(scheduleOptimizer).optimizeSchedule(scheduleData, "COST");
        log.info("[测试] 成本优化测试通过");
    }

    @Test
    @DisplayName("测试应用优化-成功场景")
    void testApplyOptimization_Success() {
        // Given
        ScheduleResult result = ScheduleResult.builder().planId(1L).build();
        OptimizedSchedule optimizedSchedule = OptimizedSchedule.builder()
                .optimizationSuccessful(true)
                .build();

        // When
        ScheduleResult applied = scheduleOptimizationService.applyOptimization(
                result, optimizedSchedule
        );

        // Then
        assertNotNull(applied);
        log.info("[测试] 应用优化成功测试通过");
    }

    @Test
    @DisplayName("测试应用优化-失败场景")
    void testApplyOptimization_Failed() {
        // Given
        ScheduleResult result = ScheduleResult.builder().planId(1L).build();
        OptimizedSchedule optimizedSchedule = OptimizedSchedule.builder()
                .optimizationSuccessful(false)
                .build();

        // When
        ScheduleResult applied = scheduleOptimizationService.applyOptimization(
                result, optimizedSchedule
        );

        // Then
        assertNotNull(applied);
        assertEquals(result, applied);
        log.info("[测试] 应用优化失败测试通过");
    }

    @Test
    @DisplayName("测试优化排班-空数据")
    void testOptimizeSchedule_NullData() {
        // When
        OptimizedSchedule result = scheduleOptimizationService.optimizeSchedule(
                null, "COST"
        );

        // Then
        assertNotNull(result);
        log.info("[测试] 空数据测试通过");
    }
}
