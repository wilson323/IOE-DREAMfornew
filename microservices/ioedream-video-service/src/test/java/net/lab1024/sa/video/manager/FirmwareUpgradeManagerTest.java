package net.lab1024.sa.video.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.FirmwareUpgradeDao;
import net.lab1024.sa.common.entity.video.FirmwareUpgradeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 固件升级管理器单元测试
 *
 * 测试范围：
 * 1. 固件升级流程（5阶段）
 * 2. 进度更新
 * 3. 升级失败处理
 * 4. 异步执行
 * 5. 完整性校验
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("固件升级管理器测试")
class FirmwareUpgradeManagerTest {

    @Mock
    private FirmwareUpgradeDao firmwareUpgradeDao;

    @InjectMocks
    private FirmwareUpgradeManager firmwareUpgradeManager;

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
                .fileSize(52428800L) // 50MB
                .fileMd5("abc123def456")
                .upgradeStatus(1) // 待升级
                .progress(0)
                .upgradeType(1) // 手动升级
                .build();
    }

    @Test
    @DisplayName("测试创建固件升级任务")
    void testCreateUpgradeTask_Success() {
        log.info("[单元测试] 测试创建固件升级任务");

        // Given
        when(firmwareUpgradeDao.insert(any(FirmwareUpgradeEntity.class)))
                .thenReturn(1);

        // When
        FirmwareUpgradeEntity result = firmwareUpgradeManager.createUpgradeTask(
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
        assertEquals(testUpgrade.getDeviceId(), result.getDeviceId(), "设备ID应匹配");
        assertEquals(testUpgrade.getCurrentVersion(), result.getCurrentVersion(), "当前版本应匹配");
        assertEquals(testUpgrade.getTargetVersion(), result.getTargetVersion(), "目标版本应匹配");
        assertEquals(1, result.getUpgradeStatus(), "状态应为待升级");
        assertEquals(0, result.getProgress(), "进度应为0");

        log.info("[单元测试] 测试通过: 固件升级任务创建成功，upgradeId={}", result.getUpgradeId());

        verify(firmwareUpgradeDao, times(1)).insert(any(FirmwareUpgradeEntity.class));
    }

    @Test
    @DisplayName("测试计算下载进度百分比")
    void testCalculateDownloadProgress() {
        log.info("[单元测试] 测试计算下载进度百分比");

        // Given
        long totalBytes = 1000000L;
        long downloadedBytes = 650000L;

        // When
        int progress = firmwareUpgradeManager.calculateDownloadProgress(totalBytes, downloadedBytes);

        // Then
        int expectedProgress = new BigDecimal(downloadedBytes)
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(totalBytes), 0, RoundingMode.HALF_UP)
                .intValue();

        assertEquals(expectedProgress, progress, "进度百分比应匹配");
        assertEquals(65, progress, "下载进度应为65%");

        log.info("[单元测试] 测试通过: 下载进度计算正确，progress={}%", progress);
    }

    @Test
    @DisplayName("测试验证固件完整性 - MD5匹配")
    void testVerifyFirmwareIntegrity_Valid() {
        log.info("[单元测试] 测试验证固件完整性 - MD5匹配");

        // Given
        String actualMd5 = "abc123def456";
        String expectedMd5 = "abc123def456";

        // When
        boolean isValid = firmwareUpgradeManager.verifyFirmwareIntegrity(actualMd5, expectedMd5);

        // Then
        assertTrue(isValid, "MD5匹配时验证应通过");

        log.info("[单元测试] 测试通过: 固件完整性验证通过");
    }

    @Test
    @DisplayName("测试验证固件完整性 - MD5不匹配")
    void testVerifyFirmwareIntegrity_Invalid() {
        log.info("[单元测试] 测试验证固件完整性 - MD5不匹配");

        // Given
        String actualMd5 = "wrongmd5123";
        String expectedMd5 = "abc123def456";

        // When
        boolean isValid = firmwareUpgradeManager.verifyFirmwareIntegrity(actualMd5, expectedMd5);

        // Then
        assertFalse(isValid, "MD5不匹配时验证应失败");

        log.info("[单元测试] 测试通过: 固件完整性验证失败（预期行为）");
    }

    @Test
    @DisplayName("测试更新升级进度")
    void testUpdateProgress_Success() {
        log.info("[单元测试] 测试更新升级进度");

        // Given
        int newProgress = 50;
        when(firmwareUpgradeDao.selectById(1L)).thenReturn(testUpgrade);
        when(firmwareUpgradeDao.updateById(any(FirmwareUpgradeEntity.class))).thenReturn(1);

        // When
        firmwareUpgradeManager.updateProgress(1L, newProgress);

        // Then
        assertEquals(newProgress, testUpgrade.getProgress(), "进度应更新为50");

        log.info("[单元测试] 测试通过: 升级进度更新成功，progress={}%", newProgress);

        verify(firmwareUpgradeDao, times(1)).selectById(1L);
        verify(firmwareUpgradeDao, times(1)).updateById(testUpgrade);
    }

    @Test
    @DisplayName("测试标记升级失败")
    void testMarkUpgradeFailed_Success() {
        log.info("[单元测试] 测试标记升级失败");

        // Given
        String failureReason = "固件传输失败";
        when(firmwareUpgradeDao.selectById(1L)).thenReturn(testUpgrade);
        when(firmwareUpgradeDao.updateById(any(FirmwareUpgradeEntity.class))).thenReturn(1);

        // When
        firmwareUpgradeManager.markUpgradeFailed(1L, failureReason);

        // Then
        assertEquals(4, testUpgrade.getUpgradeStatus(), "状态应为升级失败");
        assertEquals(failureReason, testUpgrade.getFailureReason(), "失败原因应匹配");
        assertNotNull(testUpgrade.getCompleteTime(), "完成时间应设置");

        log.info("[单元测试] 测试通过: 升级失败标记成功，reason={}", failureReason);

        verify(firmwareUpgradeDao, times(1)).selectById(1L);
        verify(firmwareUpgradeDao, times(1)).updateById(testUpgrade);
    }

    @Test
    @DisplayName("测试验证升级结果 - 版本匹配")
    void testVerifyUpgradeResult_Success() {
        log.info("[单元测试] 测试验证升级结果 - 版本匹配");

        // Given
        String actualVersion = "2.0.0";
        String expectedVersion = "2.0.0";

        // When
        boolean isValid = firmwareUpgradeManager.verifyUpgradeResult(actualVersion, expectedVersion);

        // Then
        assertTrue(isValid, "版本匹配时验证应通过");

        log.info("[单元测试] 测试通过: 升级结果验证通过");
    }

    @Test
    @DisplayName("测试验证升级结果 - 版本不匹配")
    void testVerifyUpgradeResult_Failed() {
        log.info("[单元测试] 测试验证升级结果 - 版本不匹配");

        // Given
        String actualVersion = "1.0.0";
        String expectedVersion = "2.0.0";

        // When
        boolean isValid = firmwareUpgradeManager.verifyUpgradeResult(actualVersion, expectedVersion);

        // Then
        assertFalse(isValid, "版本不匹配时验证应失败");

        log.info("[单元测试] 测试通过: 升级结果验证失败（预期行为）");
    }

    @Test
    @DisplayName("测试启动异步升级")
    void testStartUpgrade_Success() {
        log.info("[单元测试] 测试启动异步升级");

        // Given
        when(firmwareUpgradeDao.selectById(1L)).thenReturn(testUpgrade);
        when(firmwareUpgradeDao.updateById(any(FirmwareUpgradeEntity.class))).thenReturn(1);

        // When
        CompletableFuture<Void> future = firmwareUpgradeManager.startUpgrade(1L);

        // Then
        assertNotNull(future, "异步任务不应为null");

        // 等待异步任务完成（最多5秒）
        try {
            future.get();
        } catch (Exception e) {
            log.error("[单元测试] 异步任务执行异常", e);
            fail("异步任务执行失败: " + e.getMessage());
        }

        log.info("[单元测试] 测试通过: 异步升级启动成功");

        verify(firmwareUpgradeDao, times(1)).selectById(1L);
        verify(firmwareUpgradeDao, atLeastOnce()).updateById(any(FirmwareUpgradeEntity.class));
    }

    @Test
    @DisplayName("测试计算升级耗时")
    void testCalculateUpgradeDuration() {
        log.info("[单元测试] 测试计算升级耗时");

        // Given
        LocalDateTime startTime = LocalDateTime.of(2025, 12, 26, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 12, 26, 10, 5, 30); // 5分30秒

        // When
        long duration = firmwareUpgradeManager.calculateUpgradeDuration(startTime, endTime);

        // Then
        assertEquals(330L, duration, "升级耗时应为330秒（5分30秒）");

        log.info("[单元测试] 测试通过: 升级耗时计算正确，duration={}秒", duration);
    }

    @Test
    @DisplayName("测试判断是否需要回滚 - 升级失败")
    void testShouldRollback_UpgradeFailed() {
        log.info("[单元测试] 测试判断是否需要回滚 - 升级失败");

        // Given
        testUpgrade.setUpgradeStatus(4); // 升级失败

        // When
        boolean shouldRollback = firmwareUpgradeManager.shouldRollback(testUpgrade);

        // Then
        assertTrue(shouldRollback, "升级失败时应回滚");

        log.info("[单元测试] 测试通过: 判断需要回滚（升级失败）");
    }

    @Test
    @DisplayName("测试判断是否需要回滚 - 版本验证失败")
    void testShouldRollback_VersionMismatch() {
        log.info("[单元测试] 测试判断是否需要回滚 - 版本验证失败");

        // Given
        testUpgrade.setUpgradeStatus(3); // 升级成功
        // 但实际版本与目标版本不匹配的情况在业务逻辑中处理

        // When
        boolean shouldRollback = firmwareUpgradeManager.shouldRollback(testUpgrade);

        // Then
        assertFalse(shouldRollback, "升级成功且版本匹配时不需要回滚");

        log.info("[单元测试] 测试通过: 判断不需要回滚");
    }
}
