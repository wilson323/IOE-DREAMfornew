package net.lab1024.sa.attendance.engine.execution;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.ScheduleAlgorithm;
import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithmFactory;
import net.lab1024.sa.attendance.engine.conflict.ConflictDetector;
import net.lab1024.sa.attendance.engine.conflict.ConflictResolver;
import net.lab1024.sa.attendance.engine.model.*;
import net.lab1024.sa.attendance.engine.optimizer.ScheduleOptimizer;
import net.lab1024.sa.common.entity.attendance.SmartSchedulePlanEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ScheduleExecutionService 单元测试（P1-4扩展）
 * <p>
 * 测试排班执行服务的核心功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("排班执行服务测试")
@Slf4j
class ScheduleExecutionServiceTest {

    @Mock
    private ScheduleAlgorithmFactory scheduleAlgorithmFactory;

    @Mock
    private ConflictDetector conflictDetector;

    @Mock
    private ConflictResolver conflictResolver;

    @Mock
    private ScheduleOptimizer scheduleOptimizer;

    @Mock
    private ScheduleAlgorithm scheduleAlgorithm;

    private ScheduleExecutionService scheduleExecutionService;

    @BeforeEach
    void setUp() {
        log.info("[测试] 初始化排班执行服务");
        scheduleExecutionService = new ScheduleExecutionService(
                scheduleAlgorithmFactory,
                conflictDetector,
                conflictResolver,
                scheduleOptimizer
        );
    }

    @Test
    @DisplayName("测试执行排班-成功场景")
    void testExecuteSchedule_Success() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .planId(1L)
                .scheduleAlgorithm("SMART")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .build();

        when(scheduleAlgorithmFactory.getAlgorithm(anyString())).thenReturn(scheduleAlgorithm);
        when(scheduleAlgorithm.generateSchedule(any())).thenReturn(
                ScheduleResult.builder()
                        .planId(1L)
                        .status("SUCCESS")
                        .statistics(createDefaultStatistics())
                        .build()
        );

        // When
        ScheduleResult result = scheduleExecutionService.executeSchedule(request);

        // Then
        assertNotNull(result, "排班结果不应为null");
        assertEquals(1L, result.getPlanId(), "计划ID应匹配");
        assertEquals("SUCCESS", result.getStatus(), "排班状态应为成功");

        verify(scheduleAlgorithmFactory, times(1)).getAlgorithm("SMART");
        verify(scheduleAlgorithm, times(1)).generateSchedule(any(ScheduleData.class));

        log.info("[测试] 执行排班成功场景测试通过");
    }

    @Test
    @DisplayName("测试执行排班-空请求")
    void testExecuteSchedule_NullRequest() {
        // When
        ScheduleResult result = scheduleExecutionService.executeSchedule(null);

        // Then
        assertNotNull(result, "排班结果不应为null");
        assertEquals("FAILED", result.getStatus(), "排班状态应为失败");
        assertNotNull(result.getErrorMessage(), "错误信息不应为null");

        log.info("[测试] 空请求测试通过");
    }

    @Test
    @DisplayName("测试生成排班计划实体")
    void testGeneratePlanEntity() {
        // Given
        Long planId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        // When
        SmartSchedulePlanEntity result = scheduleExecutionService.generatePlanEntity(
                planId, startDate, endDate
        );

        // Then
        assertNotNull(result, "排班计划实体不应为null");
        assertEquals(planId, result.getPlanId(), "计划ID应匹配");

        log.info("[测试] 生成排班计划实体测试通过");
    }

    @Test
    @DisplayName("测试生成排班计划实体-空日期")
    void testGeneratePlanEntity_NullDates() {
        // Given
        Long planId = 1L;

        // When
        SmartSchedulePlanEntity result = scheduleExecutionService.generatePlanEntity(
                planId, null, null
        );

        // Then
        assertNotNull(result, "排班计划实体不应为null");
        assertEquals(planId, result.getPlanId(), "计划ID应匹配");

        log.info("[测试] 空日期测试通过");
    }

    @Test
    @DisplayName("测试验证请求-有效请求")
    void testValidateRequest_ValidRequest() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .planId(1L)
                .scheduleAlgorithm("SMART")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .build();

        // When
        boolean isValid = scheduleExecutionService.validateRequest(request);

        // Then
        assertTrue(isValid, "请求应为有效");

        log.info("[测试] 有效请求验证测试通过");
    }

    @Test
    @DisplayName("测试验证请求-无效请求")
    void testValidateRequest_InvalidRequest() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .planId(null)
                .scheduleAlgorithm(null)
                .build();

        // When
        boolean isValid = scheduleExecutionService.validateRequest(request);

        // Then
        assertFalse(isValid, "请求应为无效");

        log.info("[测试] 无效请求验证测试通过");
    }

    @Test
    @DisplayName("测试准备数据")
    void testPrepareData() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .planId(1L)
                .build();

        // When
        ScheduleData data = scheduleExecutionService.prepareData(request);

        // Then
        assertNotNull(data, "排班数据不应为null");
        assertEquals(1L, data.getPlanId(), "计划ID应匹配");

        log.info("[测试] 准备数据测试通过");
    }

    @Test
    @DisplayName("测试生成统计信息")
    void testGenerateStatistics() {
        // Given
        ScheduleData data = ScheduleData.builder()
                .planId(1L)
                .build();

        // When
        Map<String, Object> statistics = scheduleExecutionService.generateStatistics(data);

        // Then
        assertNotNull(statistics, "统计信息不应为null");
        assertFalse(statistics.isEmpty(), "统计信息不应为空");

        log.info("[测试] 生成统计信息测试通过");
    }

    /**
     * 创建默认统计信息
     */
    private Map<String, Object> createDefaultStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", 10);
        stats.put("totalShifts", 30);
        stats.put("workloadBalance", 0.85);
        stats.put("shiftCoverage", 0.90);
        return stats;
    }
}
