package net.lab1024.sa.attendance.engine.conflict;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.ConflictDetectionResult;
import net.lab1024.sa.attendance.engine.model.ConflictResolution;
import net.lab1024.sa.attendance.engine.model.ScheduleConflict;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ScheduleConflictService 单元测试（P1-4扩展）
 * <p>
 * 测试冲突处理服务的核心功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("冲突处理服务测试")
@Slf4j
class ScheduleConflictServiceTest {

    @Mock
    private ConflictDetector conflictDetector;

    @Mock
    private ConflictResolver conflictResolver;

    private ScheduleConflictService scheduleConflictService;

    @BeforeEach
    void setUp() {
        log.info("[测试] 初始化冲突处理服务");
        scheduleConflictService = new ScheduleConflictService(
                conflictDetector,
                conflictResolver
        );
    }

    @Test
    @DisplayName("测试检测冲突-无冲突场景")
    void testDetectConflicts_NoConflicts() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder()
                .planId(1L)
                .build();

        ConflictDetectionResult expectedResult = ConflictDetectionResult.builder()
                .hasConflicts(false)
                .conflictCount(0)
                .build();

        when(conflictDetector.detect(any(ScheduleData.class)))
                .thenReturn(expectedResult);

        // When
        ConflictDetectionResult result = scheduleConflictService.detectConflicts(scheduleData);

        // Then
        assertNotNull(result, "冲突检测结果不应为null");
        assertFalse(result.getHasConflicts(), "不应有冲突");
        assertEquals(0, result.getConflictCount(), "冲突数量应为0");

        verify(conflictDetector, times(1)).detect(scheduleData);

        log.info("[测试] 无冲突场景测试通过");
    }

    @Test
    @DisplayName("测试检测冲突-有冲突场景")
    void testDetectConflicts_HasConflicts() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder()
                .planId(1L)
                .build();

        List<ScheduleConflict> conflicts = new ArrayList<>();
        conflicts.add(ScheduleConflict.builder()
                .conflictId("C001")
                .conflictType("SKILL")
                .build());

        ConflictDetectionResult expectedResult = ConflictDetectionResult.builder()
                .hasConflicts(true)
                .conflictCount(1)
                .conflicts(conflicts)
                .build();

        when(conflictDetector.detect(any(ScheduleData.class)))
                .thenReturn(expectedResult);

        // When
        ConflictDetectionResult result = scheduleConflictService.detectConflicts(scheduleData);

        // Then
        assertNotNull(result, "冲突检测结果不应为null");
        assertTrue(result.getHasConflicts(), "应有冲突");
        assertEquals(1, result.getConflictCount(), "冲突数量应为1");
        assertNotNull(result.getConflicts(), "冲突列表不应为null");
        assertFalse(result.getConflicts().isEmpty(), "冲突列表不应为空");

        verify(conflictDetector, times(1)).detect(scheduleData);

        log.info("[测试] 有冲突场景测试通过");
    }

    @Test
    @DisplayName("测试解决冲突-自动解决策略")
    void testResolveConflicts_AutoStrategy() {
        // Given
        List<ScheduleConflict> conflicts = new ArrayList<>();
        conflicts.add(ScheduleConflict.builder()
                .conflictId("C001")
                .conflictType("SKILL")
                .build());

        ConflictResolution expectedResult = ConflictResolution.builder()
                .resolutionSuccessful(true)
                .resolutionStrategy("AUTO")
                .resolvedCount(1)
                .build();

        when(conflictResolver.resolve(anyList(), anyString()))
                .thenReturn(expectedResult);

        // When
        ConflictResolution result = scheduleConflictService.resolveConflicts(
                conflicts, "AUTO"
        );

        // Then
        assertNotNull(result, "冲突解决结果不应为null");
        assertTrue(result.getResolutionSuccessful(), "解决应成功");
        assertEquals("AUTO", result.getResolutionStrategy(), "解决策略应匹配");
        assertEquals(1, result.getResolvedCount(), "解决数量应为1");

        verify(conflictResolver, times(1)).resolve(conflicts, "AUTO");

        log.info("[测试] 自动解决策略测试通过");
    }

    @Test
    @DisplayName("测试解决冲突-手动解决策略")
    void testResolveConflicts_ManualStrategy() {
        // Given
        List<ScheduleConflict> conflicts = new ArrayList<>();

        ConflictResolution expectedResult = ConflictResolution.builder()
                .resolutionSuccessful(false)
                .resolutionStrategy("MANUAL")
                .resolvedCount(0)
                .build();

        when(conflictResolver.resolve(anyList(), anyString()))
                .thenReturn(expectedResult);

        // When
        ConflictResolution result = scheduleConflictService.resolveConflicts(
                conflicts, "MANUAL"
        );

        // Then
        assertNotNull(result, "冲突解决结果不应为null");
        assertFalse(result.getResolutionSuccessful(), "解决应失败");
        assertEquals("MANUAL", result.getResolutionStrategy(), "解决策略应匹配");
        assertEquals(0, result.getResolvedCount(), "解决数量应为0");

        verify(conflictResolver, times(1)).resolve(conflicts, "MANUAL");

        log.info("[测试] 手动解决策略测试通过");
    }

    @Test
    @DisplayName("测试解决冲突-空冲突列表")
    void testResolveConflicts_EmptyConflicts() {
        // Given
        List<ScheduleConflict> conflicts = new ArrayList<>();

        ConflictResolution expectedResult = ConflictResolution.builder()
                .resolutionSuccessful(true)
                .resolvedCount(0)
                .build();

        when(conflictResolver.resolve(anyList(), anyString()))
                .thenReturn(expectedResult);

        // When
        ConflictResolution result = scheduleConflictService.resolveConflicts(
                conflicts, "AUTO"
        );

        // Then
        assertNotNull(result, "冲突解决结果不应为null");
        assertEquals(0, result.getResolvedCount(), "解决数量应为0");

        verify(conflictResolver, times(1)).resolve(conflicts, "AUTO");

        log.info("[测试] 空冲突列表测试通过");
    }

    @Test
    @DisplayName("测试应用冲突解决-成功应用")
    void testApplyResolution_Success() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder()
                .planId(1L)
                .build();

        ConflictResolution resolution = ConflictResolution.builder()
                .resolutionSuccessful(true)
                .resolvedCount(1)
                .build();

        // When
        ScheduleData result = scheduleConflictService.applyResolution(
                scheduleData, resolution
        );

        // Then
        assertNotNull(result, "排班数据不应为null");

        log.info("[测试] 成功应用冲突解决测试通过");
    }

    @Test
    @DisplayName("测试应用冲突解决-失败场景")
    void testApplyResolution_Failed() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder()
                .planId(1L)
                .build();

        ConflictResolution resolution = ConflictResolution.builder()
                .resolutionSuccessful(false)
                .build();

        // When
        ScheduleData result = scheduleConflictService.applyResolution(
                scheduleData, resolution
        );

        // Then
        assertNotNull(result, "排班数据不应为null");
        assertEquals(scheduleData, result, "应返回原数据");

        log.info("[测试] 失败场景测试通过");
    }

    @Test
    @DisplayName("测试所有方法不为null")
    void testAllMethodsNotNull() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder()
                .planId(1L)
                .build();

        List<ScheduleConflict> conflicts = new ArrayList<>();

        ConflictDetectionResult detectionResult = ConflictDetectionResult.builder()
                .hasConflicts(false)
                .build();

        ConflictResolution resolutionResult = ConflictResolution.builder()
                .resolutionSuccessful(true)
                .build();

        // Mock配置
        when(conflictDetector.detect(any())).thenReturn(detectionResult);
        when(conflictResolver.resolve(any(), any())).thenReturn(resolutionResult);

        // When & Then
        assertAll("所有方法返回不应为null",
                () -> assertNotNull(scheduleConflictService.detectConflicts(scheduleData)),
                () -> assertNotNull(scheduleConflictService.resolveConflicts(conflicts, "AUTO")),
                () -> assertNotNull(scheduleConflictService.applyResolution(scheduleData, resolutionResult))
        );

        log.info("[测试] 所有方法不为null测试通过");
    }
}
