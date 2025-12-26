package net.lab1024.sa.video.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.FirmwareUpgradeDao;
import net.lab1024.sa.common.entity.video.FirmwareUpgradeEntity;
import net.lab1024.sa.video.manager.FirmwareUpgradeManager;
import net.lab1024.sa.video.service.impl.FirmwareUpgradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 固件升级服务单元测试
 *
 * 测试范围：
 * 1. 创建升级任务
 * 2. 启动升级
 * 3. 查询升级进度
 * 4. 查询升级历史
 * 5. 统计信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("固件升级服务测试")
class FirmwareUpgradeServiceTest {

    @Mock
    private FirmwareUpgradeDao firmwareUpgradeDao;

    @Mock
    private FirmwareUpgradeManager firmwareUpgradeManager;

    @InjectMocks
    private FirmwareUpgradeServiceImpl firmwareUpgradeService;

    private FirmwareUpgradeEntity testUpgrade;

    @BeforeEach
    void setUp() {
        testUpgrade = FirmwareUpgradeEntity.builder()
                .upgradeId(1L)
                .deviceId(1001L)
                .deviceCode("CAM001")
                .deviceName("1号摄像头")
                .currentVersion("1.0.0")
                .targetVersion("2.0.0")
                .firmwareUrl("http://firmware.example.com/v2.0.0.bin")
                .fileSize(52428800L)
                .fileMd5("abc123def456")
                .upgradeStatus(1)
                .progress(0)
                .upgradeType(1)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("测试创建升级任务 - 成功")
    void testCreateUpgradeTask_Success() {
        log.info("[单元测试] 测试创建升级任务 - 成功");

        // Given
        when(firmwareUpgradeManager.createUpgradeTask(
                anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyLong(), anyString(), anyInt()
        )).thenReturn(testUpgrade);

        // When
        FirmwareUpgradeEntity result = firmwareUpgradeService.createUpgradeTask(
                testUpgrade.getDeviceId(),
                testUpgrade.getDeviceCode(),
                testUpgrade.getDeviceName(),
                testUpgrade.getCurrentVersion(),
                testUpgrade.getTargetVersion(),
                testUpgrade.getFirmwareUrl(),
                testUpgrade.getFileSize(),
                testUpgrade.getFileMd5(),
                testUpgrade.getUpgradeType()
        );

        // Then
        assertNotNull(result, "升级任务不应为null");
        assertEquals(testUpgrade.getUpgradeId(), result.getUpgradeId(), "升级ID应匹配");

        log.info("[单元测试] 测试通过: 升级任务创建成功，upgradeId={}", result.getUpgradeId());

        verify(firmwareUpgradeManager, times(1)).createUpgradeTask(
                anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyLong(), anyString(), anyInt()
        );
    }

    @Test
    @DisplayName("测试启动升级 - 成功")
    void testStartUpgrade_Success() {
        log.info("[单元测试] 测试启动升级 - 成功");

        // Given
        Long upgradeId = 1L;
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(firmwareUpgradeManager.startUpgrade(upgradeId)).thenReturn(future);

        // When
        firmwareUpgradeService.startUpgrade(upgradeId);

        // Then
        log.info("[单元测试] 测试通过: 升级启动成功");

        verify(firmwareUpgradeManager, times(1)).startUpgrade(upgradeId);
    }

    @Test
    @DisplayName("测试查询升级进度 - 正常情况")
    void testGetUpgradeProgress_Success() {
        log.info("[单元测试] 测试查询升级进度 - 正常情况");

        // Given
        Long upgradeId = 1L;
        testUpgrade.setProgress(50);
        testUpgrade.setUpgradeStatus(2); // 升级中

        when(firmwareUpgradeDao.selectById(upgradeId)).thenReturn(testUpgrade);

        // When
        FirmwareUpgradeEntity result = firmwareUpgradeService.getUpgradeProgress(upgradeId);

        // Then
        assertNotNull(result, "升级进度不应为null");
        assertEquals(50, result.getProgress(), "进度应为50%");
        assertEquals(2, result.getUpgradeStatus(), "状态应为升级中");

        log.info("[单元测试] 测试通过: 升级进度查询成功，progress={}%", result.getProgress());

        verify(firmwareUpgradeDao, times(1)).selectById(upgradeId);
    }

    @Test
    @DisplayName("测试查询设备升级历史 - 正常情况")
    void testGetUpgradeHistory_Success() {
        log.info("[单元测试] 测试查询设备升级历史 - 正常情况");

        // Given
        Long deviceId = 1001L;

        List<FirmwareUpgradeEntity> history = Arrays.asList(
                testUpgrade,
                FirmwareUpgradeEntity.builder()
                        .upgradeId(2L)
                        .deviceId(deviceId)
                        .targetVersion("1.5.0")
                        .upgradeStatus(3) // 升级成功
                        .build()
        );

        when(firmwareUpgradeDao.selectByDeviceId(deviceId, 100)).thenReturn(history);

        // When
        List<FirmwareUpgradeEntity> result = firmwareUpgradeService.getUpgradeHistory(deviceId);

        // Then
        assertNotNull(result, "升级历史不应为null");
        assertEquals(2, result.size(), "升级记录数量应为2");

        log.info("[单元测试] 测试通过: 升级历史查询成功，记录数={}", result.size());

        verify(firmwareUpgradeDao, times(1)).selectByDeviceId(deviceId, 100);
    }

    @Test
    @DisplayName("测试获取升级统计信息 - 正常情况")
    void testGetStatistics_Success() {
        log.info("[单元测试] 测试获取升级统计信息 - 正常情况");

        // Given
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        when(firmwareUpgradeDao.countByDateRange(startDate, endDate)).thenReturn(10);
        when(firmwareUpgradeDao.countByStatus(3, startDate, endDate)).thenReturn(8); // 升级成功
        when(firmwareUpgradeDao.countByStatus(4, startDate, endDate)).thenReturn(2); // 升级失败
        when(firmwareUpgradeDao.countByStatus(2, startDate, endDate)).thenReturn(0); // 升级中

        // When
        Map<String, Object> stats = firmwareUpgradeService.getStatistics(startDate, endDate);

        // Then
        assertNotNull(stats, "统计信息不应为null");
        assertEquals(10, stats.get("totalCount"), "总升级次数应为10");
        assertEquals(8, stats.get("successCount"), "成功次数应为8");
        assertEquals(2, stats.get("failureCount"), "失败次数应为2");
        assertEquals(0, stats.get("inProgressCount"), "升级中数量应为0");

        double successRate = (double) 8 / 10 * 100;
        assertEquals(successRate, stats.get("successRate"), "成功率应匹配");

        log.info("[单元测试] 测试通过: 升级统计查询成功，totalCount={}, successRate={}%",
                stats.get("totalCount"), stats.get("successRate"));

        verify(firmwareUpgradeDao, times(1)).countByDateRange(startDate, endDate);
        verify(firmwareUpgradeDao, times(1)).countByStatus(3, startDate, endDate);
        verify(firmwareUpgradeDao, times(1)).countByStatus(4, startDate, endDate);
        verify(firmwareUpgradeDao, times(1)).countByStatus(2, startDate, endDate);
    }

    @Test
    @DisplayName("测试获取升级统计信息 - 无记录")
    void testGetStatistics_NoRecords() {
        log.info("[单元测试] 测试获取升级统计信息 - 无记录");

        // Given
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        when(firmwareUpgradeDao.countByDateRange(startDate, endDate)).thenReturn(0);
        when(firmwareUpgradeDao.countByStatus(anyInt(), any(), any())).thenReturn(0);

        // When
        Map<String, Object> stats = firmwareUpgradeService.getStatistics(startDate, endDate);

        // Then
        assertNotNull(stats, "统计信息不应为null");
        assertEquals(0, stats.get("totalCount"), "总升级次数应为0");
        assertEquals(0, stats.get("successCount"), "成功次数应为0");
        assertEquals(0, stats.get("failureCount"), "失败次数应为0");
        assertEquals(0.0, stats.get("successRate"), "成功率应为0");

        log.info("[单元测试] 测试通过: 无记录情况处理正确");

        verify(firmwareUpgradeDao, times(1)).countByDateRange(startDate, endDate);
    }
}
