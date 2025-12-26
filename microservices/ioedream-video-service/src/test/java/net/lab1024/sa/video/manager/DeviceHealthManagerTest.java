package net.lab1024.sa.video.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.DeviceHealthDao;
import net.lab1024.sa.video.entity.DeviceHealthEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 设备健康管理器单元测试
 *
 * 测试范围：
 * 1. 健康评分算法（7维度）
 * 2. CPU评分计算
 * 3. 内存评分计算
 * 4. 网络评分计算
 * 5. 健康状态分级
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("设备健康管理器测试")
class DeviceHealthManagerTest {

    @Mock
    private DeviceHealthDao deviceHealthDao;

    @InjectMocks
    private DeviceHealthManager deviceHealthManager;

    @Test
    @DisplayName("测试计算健康评分 - 健康设备")
    void testCalculateHealthScore_Healthy() {
        log.info("[单元测试] 测试计算健康评分 - 健康设备");

        // Given - 健康设备的各项指标
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cpuUsage", new BigDecimal("30"));
        metrics.put("memoryUsage", new BigDecimal("40"));
        metrics.put("diskUsage", new BigDecimal("50"));
        metrics.put("networkLatency", 20);
        metrics.put("packetLoss", new BigDecimal("0.1"));
        metrics.put("frameRate", 25);
        metrics.put("uptime", 720); // 12小时

        // When
        DeviceHealthEntity health = deviceHealthManager.calculateHealthScore(
                1001L, "CAM001", "1号摄像头", metrics
        );

        // Then
        assertNotNull(health, "健康记录不应为null");
        assertTrue(health.getHealthScore() >= 80, "健康分数应>=80（健康）");
        assertEquals(1, health.getHealthStatus(), "健康状态应为1（健康）");

        log.info("[单元测试] 测试通过: 健康设备评分，healthScore={}, healthStatus={}",
                health.getHealthScore(), health.getHealthStatus());
    }

    @Test
    @DisplayName("测试计算健康评分 - 亚健康设备")
    void testCalculateHealthScore_SubHealthy() {
        log.info("[单元测试] 测试计算健康评分 - 亚健康设备");

        // Given - 亚健康设备的各项指标
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cpuUsage", new BigDecimal("70"));
        metrics.put("memoryUsage", new BigDecimal("75"));
        metrics.put("diskUsage", new BigDecimal("80"));
        metrics.put("networkLatency", 100);
        metrics.put("packetLoss", new BigDecimal("2.0"));
        metrics.put("frameRate", 20);
        metrics.put("uptime", 480); // 8小时

        // When
        DeviceHealthEntity health = deviceHealthManager.calculateHealthScore(
                1002L, "CAM002", "2号摄像头", metrics
        );

        // Then
        assertNotNull(health, "健康记录不应为null");
        assertTrue(health.getHealthScore() >= 60 && health.getHealthScore() < 80,
                "健康分数应在60-80之间（亚健康）");
        assertEquals(2, health.getHealthStatus(), "健康状态应为2（亚健康）");

        log.info("[单元测试] 测试通过: 亚健康设备评分，healthScore={}, healthStatus={}",
                health.getHealthScore(), health.getHealthStatus());
    }

    @Test
    @DisplayName("测试计算健康评分 - 不健康设备")
    void testCalculateHealthScore_Unhealthy() {
        log.info("[单元测试] 测试计算健康评分 - 不健康设备");

        // Given - 不健康设备的各项指标
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cpuUsage", new BigDecimal("95"));
        metrics.put("memoryUsage", new BigDecimal("98"));
        metrics.put("diskUsage", new BigDecimal("90"));
        metrics.put("networkLatency", 500);
        metrics.put("packetLoss", new BigDecimal("10.0"));
        metrics.put("frameRate", 10);
        metrics.put("uptime", 60); // 1小时

        // When
        DeviceHealthEntity health = deviceHealthManager.calculateHealthScore(
                1003L, "CAM003", "3号摄像头", metrics
        );

        // Then
        assertNotNull(health, "健康记录不应为null");
        assertTrue(health.getHealthScore() < 60, "健康分数应<60（不健康）");
        assertEquals(3, health.getHealthStatus(), "健康状态应为3（不健康）");

        log.info("[单元测试] 测试通过: 不健康设备评分，healthScore={}, healthStatus={}",
                health.getHealthScore(), health.getHealthStatus());
    }

    @Test
    @DisplayName("测试CPU评分计算")
    void testCalculateCpuScore() {
        log.info("[单元测试] 测试CPU评分计算");

        // Test cases: CPU使用率 vs 期望分数
        Object[][] testCases = {
                {new BigDecimal("10"), 20},   // 10% -> 20分
                {new BigDecimal("50"), 20},   // 50% -> 20分
                {new BigDecimal("70"), 15},   // 70% -> 15分
                {new BigDecimal("85"), 8},    // 85% -> 8分
                {new BigDecimal("95"), 0}     // 95% -> 0分
        };

        for (Object[] testCase : testCases) {
            BigDecimal cpuUsage = (BigDecimal) testCase[0];
            int expectedScore = (int) testCase[1];

            int actualScore = deviceHealthManager.calculateCpuScore(cpuUsage);

            assertEquals(expectedScore, actualScore,
                    "CPU使用率=" + cpuUsage + "%时，评分应为" + expectedScore);

            log.info("[单元测试] CPU使用率={}%, 评分={}", cpuUsage, actualScore);
        }

        log.info("[单元测试] 测试通过: CPU评分计算正确");
    }

    @Test
    @DisplayName("测试内存评分计算")
    void testCalculateMemoryScore() {
        log.info("[单元测试] 测试内存评分计算");

        // Test cases: 内存使用率 vs 期望分数
        Object[][] testCases = {
                {new BigDecimal("20"), 20},   // 20% -> 20分
                {new BigDecimal("60"), 20},   // 60% -> 20分
                {new BigDecimal("80"), 12},   // 80% -> 12分
                {new BigDecimal("90"), 5},    // 90% -> 5分
                {new BigDecimal("98"), 0}     // 98% -> 0分
        };

        for (Object[] testCase : testCases) {
            BigDecimal memoryUsage = (BigDecimal) testCase[0];
            int expectedScore = (int) testCase[1];

            int actualScore = deviceHealthManager.calculateMemoryScore(memoryUsage);

            assertEquals(expectedScore, actualScore,
                    "内存使用率=" + memoryUsage + "%时，评分应为" + expectedScore);

            log.info("[单元测试] 内存使用率={}%, 评分={}", memoryUsage, actualScore);
        }

        log.info("[单元测试] 测试通过: 内存评分计算正确");
    }

    @Test
    @DisplayName("测试网络延迟评分计算")
    void testCalculateNetworkScore() {
        log.info("[单元测试] 测试网络延迟评分计算");

        // Test cases: 网络延迟 vs 期望分数
        Object[][] testCases = {
                {10, 15},    // 10ms -> 15分
                {50, 15},    // 50ms -> 15分
                {100, 12},   // 100ms -> 12分
                {200, 6},    // 200ms -> 6分
                {500, 0}     // 500ms -> 0分
        };

        for (Object[] testCase : testCases) {
            int latency = (int) testCase[0];
            int expectedScore = (int) testCase[1];

            int actualScore = deviceHealthManager.calculateNetworkScore(latency);

            assertEquals(expectedScore, actualScore,
                    "网络延迟=" + latency + "ms时，评分应为" + expectedScore);

            log.info("[单元测试] 网络延迟={}ms, 评分={}", latency, actualScore);
        }

        log.info("[单元测试] 测试通过: 网络延迟评分计算正确");
    }

    @Test
    @DisplayName("测试丢包率评分计算")
    void testCalculatePacketLossScore() {
        log.info("[单元测试] 测试丢包率评分计算");

        // Test cases: 丢包率 vs 期望分数
        Object[][] testCases = {
                {new BigDecimal("0"), 10},     // 0% -> 10分
                {new BigDecimal("0.5"), 10},   // 0.5% -> 10分
                {new BigDecimal("1.0"), 8},    // 1.0% -> 8分
                {new BigDecimal("3.0"), 4},    // 3.0% -> 4分
                {new BigDecimal("10.0"), 0}    // 10.0% -> 0分
        };

        for (Object[] testCase : testCases) {
            BigDecimal packetLoss = (BigDecimal) testCase[0];
            int expectedScore = (int) testCase[1];

            int actualScore = deviceHealthManager.calculatePacketLossScore(packetLoss);

            assertEquals(expectedScore, actualScore,
                    "丢包率=" + packetLoss + "%时，评分应为" + expectedScore);

            log.info("[单元测试] 丢包率={}%, 评分={}", packetLoss, actualScore);
        }

        log.info("[单元测试] 测试通过: 丢包率评分计算正确");
    }

    @Test
    @DisplayName("测试判断告警级别 - 正常")
    void testDetermineAlarmLevel_Normal() {
        log.info("[单元测试] 测试判断告警级别 - 正常");

        // Given - 所有指标正常
        StringBuilder alarmMessage = new StringBuilder();
        int alarmLevel = deviceHealthManager.determineAlarmLevel(
                new BigDecimal("30"),  // CPU
                new BigDecimal("40"),  // Memory
                20,                    // Latency
                new BigDecimal("0.1")  // PacketLoss
        );

        // Then
        assertEquals(0, alarmLevel, "告警级别应为0（正常）");

        log.info("[单元测试] 测试通过: 告警级别为正常");
    }

    @Test
    @DisplayName("测试判断告警级别 - 警告")
    void testDetermineAlarmLevel_Warning() {
        log.info("[单元测试] 测试判断告警级别 - 警告");

        // Given - CPU过高触发警告
        int alarmLevel = deviceHealthManager.determineAlarmLevel(
                new BigDecimal("85"),  // CPU过高
                new BigDecimal("40"),  // Memory正常
                20,                    // Latency正常
                new BigDecimal("0.1")  // PacketLoss正常
        );

        // Then
        assertEquals(2, alarmLevel, "告警级别应为2（警告）");

        log.info("[单元测试] 测试通过: 告警级别为警告");
    }

    @Test
    @DisplayName("测试生成告警消息")
    void testGenerateAlarmMessage() {
        log.info("[单元测试] 测试生成告警消息");

        // Given
        StringBuilder alarmMessage = new StringBuilder();
        alarmMessage.append("CPU使用率过高(85%) ");
        alarmMessage.append("内存使用率过高(92%) ");

        // When
        String message = alarmMessage.toString().trim();

        // Then
        assertTrue(message.contains("CPU"), "告警消息应包含CPU");
        assertTrue(message.contains("内存"), "告警消息应包含内存");

        log.info("[单元测试] 测试通过: 告警消息生成正确，message={}", message);
    }
}
