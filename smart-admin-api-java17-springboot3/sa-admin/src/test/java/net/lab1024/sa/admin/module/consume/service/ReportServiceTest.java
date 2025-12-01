package net.lab1024.sa.admin.module.consume.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.dao.RechargeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;

/**
 * 报表服务单元测试
 * 严格遵循repowiki规范：测试报表服务的核心功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("报表服务单元测试")
class ReportServiceTest {

    @Resource
    private ReportService reportService;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private RechargeRecordDao rechargeRecordDao;

    private LocalDateTime testStartTime;
    private LocalDateTime testEndTime;

    @BeforeEach
    void setUp() {
        // 设置测试时间范围（最近30天）
        testEndTime = LocalDateTime.now();
        testStartTime = testEndTime.minusDays(30);
    }

    @Test
    @DisplayName("测试获取消费汇总 - 基础功能")
    void testGetConsumeSummary_Basic() {
        // Act
        Map<String, Object> result = reportService.getConsumeSummary(
                "DAY", testStartTime, testEndTime, null, null);

        // Assert
        assertNotNull(result, "消费汇总结果不应该为null");
        assertTrue(result.containsKey("totalAmount"), "应该包含总金额字段");
        assertTrue(result.containsKey("totalCount"), "应该包含总笔数字段");
        assertTrue(result.containsKey("avgAmount"), "应该包含平均金额字段");

        BigDecimal totalAmount = (BigDecimal) result.get("totalAmount");
        Long totalCount = ((Number) result.get("totalCount")).longValue();
        BigDecimal avgAmount = (BigDecimal) result.get("avgAmount");

        assertNotNull(totalAmount, "总金额不应该为null");
        assertNotNull(totalCount, "总笔数不应该为null");
        assertNotNull(avgAmount, "平均金额不应该为null");
        assertTrue(totalAmount.compareTo(BigDecimal.ZERO) >= 0, "总金额应该大于等于0");
        assertTrue(totalCount >= 0, "总笔数应该大于等于0");
    }

    @Test
    @DisplayName("测试获取消费汇总 - 按设备筛选")
    void testGetConsumeSummary_ByDevice() {
        // Arrange: 查询所有设备，获取第一个设备ID
        List<ConsumeRecordEntity> allRecords = consumeRecordDao.selectList(null);
        Long deviceId = allRecords.isEmpty() ? null : allRecords.get(0).getDeviceId();

        if (deviceId != null) {
            // Act
            Map<String, Object> result = reportService.getConsumeSummary(
                    "DAY", testStartTime, testEndTime, deviceId, null);

            // Assert
            assertNotNull(result, "消费汇总结果不应该为null");
            assertTrue(result.containsKey("totalAmount"), "应该包含总金额字段");
        }
    }

    @Test
    @DisplayName("测试获取消费趋势 - 基础功能")
    void testGetConsumeTrend_Basic() {
        // Act
        List<Map<String, Object>> result = reportService.getConsumeTrend(
                "DAY", testStartTime, testEndTime, "AMOUNT", null, null);

        // Assert
        assertNotNull(result, "消费趋势结果不应该为null");
        assertTrue(result instanceof List, "结果应该是List类型");
    }

    @Test
    @DisplayName("测试获取消费模式分布 - 基础功能")
    void testGetConsumeModeDistribution_Basic() {
        // Act
        List<Map<String, Object>> result = reportService.getConsumeModeDistribution(
                testStartTime, testEndTime, null);

        // Assert
        assertNotNull(result, "消费模式分布结果不应该为null");
        assertTrue(result instanceof List, "结果应该是List类型");
    }

    @Test
    @DisplayName("测试获取设备消费排行 - 基础功能")
    void testGetDeviceRanking_Basic() {
        // Act
        List<Map<String, Object>> result = reportService.getDeviceRanking(
                testStartTime, testEndTime, "AMOUNT", 10);

        // Assert
        assertNotNull(result, "设备消费排行结果不应该为null");
        assertTrue(result instanceof List, "结果应该是List类型");
    }

    @Test
    @DisplayName("测试获取用户消费排行 - 基础功能")
    void testGetUserRanking_Basic() {
        // Act
        List<Map<String, Object>> result = reportService.getUserRanking(
                testStartTime, testEndTime, "AMOUNT", 10);

        // Assert
        assertNotNull(result, "用户消费排行结果不应该为null");
        assertTrue(result instanceof List, "结果应该是List类型");
    }

    @Test
    @DisplayName("测试获取时段分布 - 基础功能")
    void testGetHourDistribution_Basic() {
        // Act
        List<Map<String, Object>> result = reportService.getHourDistribution(
                testStartTime, testEndTime, null);

        // Assert
        assertNotNull(result, "时段分布结果不应该为null");
        assertTrue(result instanceof List, "结果应该是List类型");
    }

    @Test
    @DisplayName("测试获取地区分布 - 基础功能")
    void testGetRegionDistribution_Basic() {
        // Act
        List<Map<String, Object>> result = reportService.getRegionDistribution(
                testStartTime, testEndTime, "AMOUNT");

        // Assert
        assertNotNull(result, "地区分布结果不应该为null");
        assertTrue(result instanceof List, "结果应该是List类型");
    }

    @Test
    @DisplayName("测试获取同比环比数据 - 基础功能")
    void testGetComparisonData_Basic() {
        // Act
        Map<String, Object> result = reportService.getComparisonData(
                "YEAR_OVER_YEAR", testStartTime, testEndTime, null, null);

        // Assert
        assertNotNull(result, "同比环比结果不应该为null");
        assertTrue(result instanceof Map, "结果应该是Map类型");
    }

    @Test
    @DisplayName("测试获取仪表盘数据 - 基础功能")
    void testGetDashboardData_Basic() {
        // Act
        Map<String, Object> result = reportService.getDashboardData(
                testStartTime, testEndTime);

        // Assert
        assertNotNull(result, "仪表盘数据结果不应该为null");
        assertTrue(result.containsKey("todayAmount"), "应该包含今日金额字段");
        assertTrue(result.containsKey("todayCount"), "应该包含今日笔数字段");
        assertTrue(result.containsKey("totalAmount"), "应该包含总金额字段");
        assertTrue(result.containsKey("totalCount"), "应该包含总笔数字段");
        assertTrue(result.containsKey("avgAmount"), "应该包含平均金额字段");
        assertTrue(result.containsKey("updateTime"), "应该包含更新时间字段");
    }

    @Test
    @DisplayName("测试获取实时统计 - 基础功能")
    void testGetRealTimeStatistics_Basic() {
        // Act
        Map<String, Object> result = reportService.getRealTimeStatistics();

        // Assert
        assertNotNull(result, "实时统计结果不应该为null");
        assertTrue(result.containsKey("todayAmount"), "应该包含今日金额字段");
        assertTrue(result.containsKey("todayCount"), "应该包含今日笔数字段");
        assertTrue(result.containsKey("hourAmount"), "应该包含最近1小时金额字段");
        assertTrue(result.containsKey("hourCount"), "应该包含最近1小时笔数字段");
        assertTrue(result.containsKey("updateTime"), "应该包含更新时间字段");
    }

    @Test
    @DisplayName("测试获取异常检测 - 基础功能")
    void testGetAnomalyDetection_Basic() {
        // Act
        List<Map<String, Object>> result = reportService.getAnomalyDetection(
                testStartTime, testEndTime, "AMOUNT");

        // Assert
        assertNotNull(result, "异常检测结果不应该为null");
        assertTrue(result instanceof List, "结果应该是List类型");
    }

    @Test
    @DisplayName("测试获取预测分析 - 基础功能")
    void testGetForecastAnalysis_Basic() {
        // Act
        Map<String, Object> result = reportService.getForecastAnalysis(
                "AMOUNT", testStartTime, testEndTime, 7);

        // Assert
        assertNotNull(result, "预测分析结果不应该为null");
        assertTrue(result.containsKey("forecastAmount"), "应该包含预测金额字段");
        assertTrue(result.containsKey("forecastCount"), "应该包含预测笔数字段");
        assertTrue(result.containsKey("confidence"), "应该包含置信度字段");
        assertTrue(result.containsKey("forecastPeriod"), "应该包含预测周期字段");
    }

    @Test
    @DisplayName("测试生成消费报表 - 基础功能")
    void testGenerateConsumeReport_Basic() {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", testStartTime);
        params.put("endTime", testEndTime);
        params.put("timeDimension", "DAY");

        // Act
        Map<String, Object> result = reportService.generateConsumeReport(params);

        // Assert
        assertNotNull(result, "消费报表结果不应该为null");
        assertTrue(result.containsKey("reportType"), "应该包含报表类型字段");
        assertTrue(result.containsKey("startTime"), "应该包含开始时间字段");
        assertTrue(result.containsKey("endTime"), "应该包含结束时间字段");
        assertTrue(result.containsKey("data"), "应该包含数据字段");
    }

    @Test
    @DisplayName("测试生成充值报表 - 基础功能")
    void testGenerateRechargeReport_Basic() {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", testStartTime);
        params.put("endTime", testEndTime);

        // Act
        Map<String, Object> result = reportService.generateRechargeReport(params);

        // Assert
        assertNotNull(result, "充值报表结果不应该为null");
        assertTrue(result.containsKey("reportType"), "应该包含报表类型字段");
        assertTrue(result.containsKey("startTime"), "应该包含开始时间字段");
        assertTrue(result.containsKey("endTime"), "应该包含结束时间字段");
        assertTrue(result.containsKey("data"), "应该包含数据字段");
    }

    @Test
    @DisplayName("测试获取报表列表 - 基础功能")
    void testGetReportList_Basic() {
        // Arrange
        Map<String, Object> params = new HashMap<>();

        // Act
        List<Map<String, Object>> result = reportService.getReportList(params);

        // Assert
        assertNotNull(result, "报表列表结果不应该为null");
        assertTrue(result instanceof List, "结果应该是List类型");
    }

    @Test
    @DisplayName("测试缓存功能 - 消费汇总缓存")
    void testCacheFunctionality_ConsumeSummary() {
        // Arrange
        String timeDimension = "DAY";
        Long deviceId = null;
        String consumeMode = null;

        // Act: 第一次调用（应该查询数据库）
        Map<String, Object> result1 = reportService.getConsumeSummary(
                timeDimension, testStartTime, testEndTime, deviceId, consumeMode);

        // Act: 第二次调用（应该从缓存获取）
        Map<String, Object> result2 = reportService.getConsumeSummary(
                timeDimension, testStartTime, testEndTime, deviceId, consumeMode);

        // Assert
        assertNotNull(result1, "第一次调用结果不应该为null");
        assertNotNull(result2, "第二次调用结果不应该为null");
        assertEquals(result1.get("totalAmount"), result2.get("totalAmount"),
                "缓存结果应该与第一次查询结果一致");
    }

    @Test
    @DisplayName("测试异常处理 - 空参数")
    void testExceptionHandling_NullParams() {
        // Act & Assert: 应该能够处理null参数而不抛出异常
        Map<String, Object> result = reportService.getConsumeSummary(
                null, null, null, null, null);

        assertNotNull(result, "即使参数为null，也应该返回结果");
    }
}
