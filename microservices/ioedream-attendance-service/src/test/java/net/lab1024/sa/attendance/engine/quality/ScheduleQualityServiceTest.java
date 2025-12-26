package net.lab1024.sa.attendance.engine.quality;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;
import net.lab1024.sa.attendance.engine.model.ScheduleStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ScheduleQualityService 单元测试
 */
@DisplayName("质量评估服务测试")
@Slf4j
class ScheduleQualityServiceTest {

    private ScheduleQualityService scheduleQualityService;

    @BeforeEach
    void setUp() {
        scheduleQualityService = new ScheduleQualityService();
    }

    @Test
    @DisplayName("测试计算质量评分-高分场景")
    void testCalculateQualityScore_HighScore() {
        // Given
        Map<String, Object> stats = new HashMap<>();
        stats.put("workloadBalance", 0.9);
        stats.put("shiftCoverage", 0.9);
        stats.put("constraintSatisfaction", 0.9);
        stats.put("costSaving", 0.2);

        ScheduleResult result = ScheduleResult.builder()
                .planId(1L)
                .statistics(stats)
                .build();

        // When
        Double score = scheduleQualityService.calculateQualityScore(result);

        // Then
        assertNotNull(score);
        assertTrue(score >= 80.0, "高分场景应大于等于80分");
        log.info("[测试] 高分场景测试通过，评分: {}", score);
    }

    @Test
    @DisplayName("测试计算质量评分-低分场景")
    void testCalculateQualityScore_LowScore() {
        // Given
        Map<String, Object> stats = new HashMap<>();
        stats.put("workloadBalance", 0.5);
        stats.put("shiftCoverage", 0.5);
        stats.put("constraintSatisfaction", 0.5);
        stats.put("costSaving", 0.1);

        ScheduleResult result = ScheduleResult.builder()
                .planId(1L)
                .statistics(stats)
                .build();

        // When
        Double score = scheduleQualityService.calculateQualityScore(result);

        // Then
        assertNotNull(score);
        assertTrue(score < 80.0, "低分场景应小于80分");
        log.info("[测试] 低分场景测试通过，评分: {}", score);
    }

    @Test
    @DisplayName("测试检查是否需要审核-低分需审核")
    void testCheckNeedsReview_LowScore() {
        // Given
        ScheduleResult result = ScheduleResult.builder()
                .planId(1L)
                .qualityScore(70.0)
                .status("SUCCESS")
                .build();

        // When
        Boolean needsReview = scheduleQualityService.checkNeedsReview(result);

        // Then
        assertNotNull(needsReview);
        assertTrue(needsReview, "低分需要审核");
        log.info("[测试] 低分需审核测试通过");
    }

    @Test
    @DisplayName("测试检查是否需要审核-高分不需审核")
    void testCheckNeedsReview_HighScore() {
        // Given
        ScheduleResult result = ScheduleResult.builder()
                .planId(1L)
                .qualityScore(90.0)
                .status("SUCCESS")
                .build();

        // When
        Boolean needsReview = scheduleQualityService.checkNeedsReview(result);

        // Then
        assertNotNull(needsReview);
        assertFalse(needsReview, "高分不需审核");
        log.info("[测试] 高分不需审核测试通过");
    }

    @Test
    @DisplayName("测试生成改进建议-低分建议")
    void testGenerateRecommendations_LowScore() {
        // Given
        ScheduleResult result = ScheduleResult.builder()
                .planId(1L)
                .qualityScore(50.0)
                .statistics(new HashMap<>())
                .build();

        // When
        List<String> recommendations = scheduleQualityService.generateRecommendations(result);

        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty(), "应有改进建议");
        log.info("[测试] 低分建议测试通过，建议数: {}", recommendations.size());
    }

    @Test
    @DisplayName("测试生成统计信息")
    void testGenerateScheduleStatistics() {
        // Given
        Long planId = 1L;
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", 10);
        stats.put("totalShifts", 30);
        stats.put("totalAssignments", 100);

        ScheduleResult result = ScheduleResult.builder()
                .planId(planId)
                .statistics(stats)
                .qualityScore(85.0)
                .build();

        // When
        ScheduleStatistics statistics = scheduleQualityService.generateScheduleStatistics(
                planId, result
        );

        // Then
        assertNotNull(statistics);
        assertEquals(planId, statistics.getPlanId());
        assertEquals(10, statistics.getTotalEmployees());
        assertEquals(30, statistics.getTotalShifts());
        log.info("[测试] 生成统计信息测试通过");
    }

    @Test
    @DisplayName("测试生成统计信息-空结果")
    void testGenerateScheduleStatistics_NullResult() {
        // Given
        Long planId = 1L;

        // When
        ScheduleStatistics statistics = scheduleQualityService.generateScheduleStatistics(
                planId, null
        );

        // Then
        assertNotNull(statistics);
        assertEquals(planId, statistics.getPlanId());
        log.info("[测试] 空结果统计信息测试通过");
    }
}
