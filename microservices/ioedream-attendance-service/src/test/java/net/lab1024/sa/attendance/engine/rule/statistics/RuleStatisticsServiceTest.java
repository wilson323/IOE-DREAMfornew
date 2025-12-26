package net.lab1024.sa.attendance.engine.rule.statistics;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.RuleExecutionStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 规则统计服务测试类
 * <p>
 * P2-Batch4重构: 测试RuleStatisticsService的5个核心方法
 * 遵循Given-When-Then测试模式
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@DisplayName("规则统计服务测试")
@Slf4j
class RuleStatisticsServiceTest {

    private RuleStatisticsService ruleStatisticsService;

    @BeforeEach
    void setUp() {
        log.info("[规则统计测试] 初始化测试环境");
        ruleStatisticsService = new RuleStatisticsService();
    }

    @Test
    @DisplayName("测试获取执行统计-有数据场景")
    void testGetExecutionStatistics_WithData() {
        log.info("[规则统计测试] 测试场景: 获取执行统计-有数据");

        // Given: 准备测试数据 - 设置一些统计数据
        long startTime = System.currentTimeMillis() - 3600000; // 1小时前
        long endTime = System.currentTimeMillis();

        ruleStatisticsService.setStatisticsValue("totalExecutions", 100L);
        ruleStatisticsService.setStatisticsValue("successfulExecutions", 85L);
        ruleStatisticsService.setStatisticsValue("failedExecutions", 15L);

        // When: 获取执行统计
        RuleExecutionStatistics statistics = ruleStatisticsService.getExecutionStatistics(startTime, endTime);

        // Then: 验证统计结果
        assertNotNull(statistics, "统计结果不应为空");
        assertEquals(startTime, statistics.getStartTime(), "开始时间应该匹配");
        assertEquals(endTime, statistics.getEndTime(), "结束时间应该匹配");
        assertEquals(100L, statistics.getTotalExecutions(), "总执行次数应该匹配");
        assertEquals(85L, statistics.getSuccessfulExecutions(), "成功次数应该匹配");
        assertEquals(15L, statistics.getFailedExecutions(), "失败次数应该匹配");
        assertNotNull(statistics.getStatisticsTimestamp(), "统计时间戳不应为空");

        log.info("[规则统计测试] 测试通过: 执行统计获取成功, total={}, success={}, failed={}",
                statistics.getTotalExecutions(),
                statistics.getSuccessfulExecutions(),
                statistics.getFailedExecutions());
    }

    @Test
    @DisplayName("测试获取执行统计-空统计场景")
    void testGetExecutionStatistics_EmptyStatistics() {
        log.info("[规则统计测试] 测试场景: 获取执行统计-空统计");

        // Given: 准备测试数据 - 不设置任何统计数据
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();

        // When: 获取执行统计
        RuleExecutionStatistics statistics = ruleStatisticsService.getExecutionStatistics(startTime, endTime);

        // Then: 验证统计结果（应该全是0）
        assertNotNull(statistics, "统计结果不应为空");
        assertEquals(0L, statistics.getTotalExecutions(), "总执行次数应该是0");
        assertEquals(0L, statistics.getSuccessfulExecutions(), "成功次数应该是0");
        assertEquals(0L, statistics.getFailedExecutions(), "失败次数应该是0");

        log.info("[规则统计测试] 测试通过: 空统计获取成功, 所有统计值为0");
    }

    @Test
    @DisplayName("测试更新执行统计-成功场景")
    void testUpdateExecutionStatistics_Success() {
        log.info("[规则统计测试] 测试场景: 更新执行统计-成功");

        // Given: 准备测试数据
        ruleStatisticsService.setStatisticsValue("totalExecutions", 10L);
        ruleStatisticsService.setStatisticsValue("successfulExecutions", 8L);
        ruleStatisticsService.setStatisticsValue("failedExecutions", 2L);

        // When: 更新成功统计
        ruleStatisticsService.updateExecutionStatistics("SUCCESS");

        // Then: 验证统计已更新
        Long totalExecutions = ruleStatisticsService.getStatisticsValue("totalExecutions");
        Long successfulExecutions = ruleStatisticsService.getStatisticsValue("successfulExecutions");
        Long failedExecutions = ruleStatisticsService.getStatisticsValue("failedExecutions");

        assertEquals(11L, totalExecutions, "总执行次数应该增加1");
        assertEquals(9L, successfulExecutions, "成功次数应该增加1");
        assertEquals(2L, failedExecutions, "失败次数应该保持不变");

        log.info("[规则统计测试] 测试通过: 成功统计更新成功, total={}, success={}",
                totalExecutions, successfulExecutions);
    }

    @Test
    @DisplayName("测试更新执行统计-失败场景")
    void testUpdateExecutionStatistics_Failed() {
        log.info("[规则统计测试] 测试场景: 更新执行统计-失败");

        // Given: 准备测试数据
        ruleStatisticsService.setStatisticsValue("totalExecutions", 10L);
        ruleStatisticsService.setStatisticsValue("successfulExecutions", 8L);
        ruleStatisticsService.setStatisticsValue("failedExecutions", 2L);

        // When: 更新失败统计
        ruleStatisticsService.updateExecutionStatistics("FAILED");

        // Then: 验证统计已更新
        Long totalExecutions = ruleStatisticsService.getStatisticsValue("totalExecutions");
        Long successfulExecutions = ruleStatisticsService.getStatisticsValue("successfulExecutions");
        Long failedExecutions = ruleStatisticsService.getStatisticsValue("failedExecutions");

        assertEquals(11L, totalExecutions, "总执行次数应该增加1");
        assertEquals(8L, successfulExecutions, "成功次数应该保持不变");
        assertEquals(3L, failedExecutions, "失败次数应该增加1");

        log.info("[规则统计测试] 测试通过: 失败统计更新成功, total={}, failed={}",
                totalExecutions, failedExecutions);
    }

    @Test
    @DisplayName("测试获取和设置统计值")
    void testGetAndSetStatisticsValue() {
        log.info("[规则统计测试] 测试场景: 获取和设置统计值");

        // Given: 准备测试数据
        String key1 = "customMetric1";
        String key2 = "customMetric2";
        Long value1 = 12345L;
        Long value2 = 67890L;

        // When: 设置统计值
        ruleStatisticsService.setStatisticsValue(key1, value1);
        ruleStatisticsService.setStatisticsValue(key2, value2);

        // Then: 验证可以获取到设置的值
        Long retrievedValue1 = ruleStatisticsService.getStatisticsValue(key1);
        Long retrievedValue2 = ruleStatisticsService.getStatisticsValue(key2);

        assertEquals(value1, retrievedValue1, "统计值1应该匹配");
        assertEquals(value2, retrievedValue2, "统计值2应该匹配");

        // 验证获取不存在的键返回0
        Long nonExistentValue = ruleStatisticsService.getStatisticsValue("nonExistentKey");
        assertEquals(0L, nonExistentValue, "不存在的键应该返回0");

        log.info("[规则统计测试] 测试通过: 统计值获取和设置成功, {}={}, {}={}",
                key1, retrievedValue1, key2, retrievedValue2);
    }
}
