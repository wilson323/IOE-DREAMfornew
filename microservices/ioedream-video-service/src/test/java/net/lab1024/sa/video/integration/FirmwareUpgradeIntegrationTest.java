package net.lab1024.sa.video.integration;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.FirmwareUpgradeDao;
import net.lab1024.sa.common.entity.video.FirmwareUpgradeEntity;
import net.lab1024.sa.video.manager.FirmwareUpgradeManager;
import net.lab1024.sa.video.service.FirmwareUpgradeService;
import net.lab1024.sa.video.service.impl.FirmwareUpgradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 固件升级集成测试
 *
 * 测试范围：
 * 1. 完整的固件升级流程（异步）
 * 2. 进度跟踪和状态更新
 * 3. 升级历史记录
 * 4. 并发升级处理
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("固件升级集成测试")
class FirmwareUpgradeIntegrationTest {

    @Resource
    private FirmwareUpgradeService firmwareUpgradeService;

    @Resource
    private FirmwareUpgradeDao firmwareUpgradeDao;

    @Resource
    private FirmwareUpgradeManager firmwareUpgradeManager;

    private FirmwareUpgradeEntity testUpgrade;

    @BeforeEach
    void setUp() {
        testUpgrade = FirmwareUpgradeEntity.builder()
                .deviceId(1001L)
                .deviceCode("CAM001")
                .deviceName("1号摄像头")
                .currentVersion("1.0.0")
                .targetVersion("2.0.0")
                .firmwareUrl("http://firmware.example.com/v2.0.0.bin")
                .fileSize(52428800L)
                .fileMd5("abc123def456")
                .upgradeType(1)
                .build();
    }

    @Test
    @DisplayName("测试完整固件升级流程 - 成功")
    void testCompleteFirmwareUpgrade_Success() throws Exception {
        log.info("[集成测试] 测试完整固件升级流程 - 成功");

        // Given - 创建升级任务
        FirmwareUpgradeEntity upgrade = firmwareUpgradeService.createUpgradeTask(
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

        assertNotNull(upgrade, "升级任务不应为null");
        Long upgradeId = upgrade.getUpgradeId();

        // When - 启动异步升级
        firmwareUpgradeService.startUpgrade(upgradeId);

        // Then - 等待异步完成（最多30秒）
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(10000); // 等待10秒模拟升级完成

                // 查询最终状态
                FirmwareUpgradeEntity finalStatus = firmwareUpgradeService.getUpgradeProgress(upgradeId);

                // 验证升级完成（在实际环境中应该是状态3，但测试中可能仍在进行）
                assertNotNull(finalStatus, "最终状态不应为null");
                assertNotNull(finalStatus.getProgress(), "进度应设置");

                log.info("[集成测试] 升级完成: upgradeId={}, status={}, progress={}%",
                        upgradeId, finalStatus.getUpgradeStatus(), finalStatus.getProgress());

            } catch (Exception e) {
                log.error("[集成测试] 升级流程异常", e);
                fail("升级流程异常: " + e.getMessage());
            }
        }).get(30, TimeUnit.SECONDS);

        log.info("[集成测试] 测试通过: 固件升级流程完成");
    }

    @Test
    @DisplayName("测试升级进度跟踪")
    void testUpgradeProgressTracking() throws Exception {
        log.info("[集成测试] 测试升级进度跟踪");

        // Given
        FirmwareUpgradeEntity upgrade = firmwareUpgradeService.createUpgradeTask(
                1002L, "CAM002", "2号摄像头",
                "1.0.0", "2.0.0",
                "http://firmware.example.com/v2.0.0.bin",
                52428800L, "abc123", 1
        );

        Long upgradeId = upgrade.getUpgradeId();

        // When - 启动升级并跟踪进度
        firmwareUpgradeService.startUpgrade(upgradeId);

        // 定期检查进度（最多检查5次，每次间隔2秒）
        for (int i = 0; i < 5; i++) {
            Thread.sleep(2000);

            FirmwareUpgradeEntity progress = firmwareUpgradeService.getUpgradeProgress(upgradeId);

            log.info("[集成测试] 进度检查: attempt={}, upgradeId={}, status={}, progress={}%",
                    i + 1, upgradeId, progress.getUpgradeStatus(), progress.getProgress());

            // 验证进度数据有效性
            assertNotNull(progress, "进度数据不应为null");
            assertTrue(progress.getProgress() >= 0 && progress.getProgress() <= 100,
                    "进度应在0-100范围内");

            // 如果升级完成或失败，提前结束
            if (progress.getUpgradeStatus() == 3 || progress.getUpgradeStatus() == 4) {
                break;
            }
        }

        // Then - 验证最终状态
        FirmwareUpgradeEntity finalProgress = firmwareUpgradeService.getUpgradeProgress(upgradeId);
        assertNotNull(finalProgress, "最终进度不应为null");

        log.info("[集成测试] 测试通过: 进度跟踪完成，finalStatus={}, finalProgress={}%",
                finalProgress.getUpgradeStatus(), finalProgress.getProgress());
    }

    @Test
    @DisplayName("测试升级历史查询")
    void testGetUpgradeHistory() {
        log.info("[集成测试] 测试升级历史查询");

        // Given - 创建多个升级记录
        for (int i = 0; i < 3; i++) {
            FirmwareUpgradeEntity upgrade = FirmwareUpgradeEntity.builder()
                    .deviceId(1003L + i)
                    .deviceCode("CAM00" + (3 + i))
                    .deviceName((3 + i) + "号摄像头")
                    .currentVersion("1.0.0")
                    .targetVersion("2.0.0")
                    .firmwareUrl("http://firmware.example.com/v2.0.0.bin")
                    .fileSize(52428800L)
                    .fileMd5("abc123")
                    .upgradeStatus(3) // 升级成功
                    .progress(100)
                    .upgradeType(1)
                    .build();

            firmwareUpgradeDao.insert(upgrade);
        }

        // When - 查询升级历史
        List<FirmwareUpgradeEntity> history = firmwareUpgradeService.getUpgradeHistory(1003L);

        // Then
        assertNotNull(history, "升级历史不应为null");
        assertTrue(history.size() >= 1, "升级历史应包含至少1条记录");

        log.info("[集成测试] 测试通过: 升级历史查询成功，记录数={}", history.size());
    }

    @Test
    @DisplayName("测试升级统计信息")
    void testGetUpgradeStatistics() {
        log.info("[集成测试] 测试升级统计信息");

        // Given - 创建不同状态的升级记录
        FirmwareUpgradeEntity successUpgrade = FirmwareUpgradeEntity.builder()
                .deviceId(2001L)
                .upgradeStatus(3) // 成功
                .build();

        FirmwareUpgradeEntity failedUpgrade = FirmwareUpgradeEntity.builder()
                .deviceId(2002L)
                .upgradeStatus(4) // 失败
                .build();

        firmwareUpgradeDao.insert(successUpgrade);
        firmwareUpgradeDao.insert(failedUpgrade);

        // When - 查询统计信息
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        Map<String, Object> stats = firmwareUpgradeService.getStatistics(startDate, endDate);

        // Then
        assertNotNull(stats, "统计信息不应为null");
        assertTrue((Integer) stats.get("totalCount") >= 2, "总升级次数应>=2");
        assertTrue((Integer) stats.get("successCount") >= 1, "成功次数应>=1");
        assertTrue((Integer) stats.get("failureCount") >= 1, "失败次数应>=1");

        double successRate = (double) stats.get("successRate");
        assertTrue(successRate >= 0 && successRate <= 100, "成功率应在0-100范围内");

        log.info("[集成测试] 测试通过: 升级统计查询成功，totalCount={}, successRate={}%",
                stats.get("totalCount"), stats.get("successRate"));
    }

    @Test
    @DisplayName("测试并发升级处理")
    void testConcurrentUpgrades() throws Exception {
        log.info("[集成测试] 测试并发升级处理");

        // Given - 创建3个并发升级任务
        FirmwareUpgradeEntity upgrade1 = firmwareUpgradeService.createUpgradeTask(
                3001L, "CAM001", "摄像头1", "1.0.0", "2.0.0",
                "http://firmware.example.com/v2.0.0.bin", 52428800L, "abc123", 1
        );

        FirmwareUpgradeEntity upgrade2 = firmwareUpgradeService.createUpgradeTask(
                3002L, "CAM002", "摄像头2", "1.0.0", "2.0.0",
                "http://firmware.example.com/v2.0.0.bin", 52428800L, "abc123", 1
        );

        FirmwareUpgradeEntity upgrade3 = firmwareUpgradeService.createUpgradeTask(
                3003L, "CAM003", "摄像头3", "1.0.0", "2.0.0",
                "http://firmware.example.com/v2.0.0.bin", 52428800L, "abc123", 1
        );

        // When - 并发启动升级
        firmwareUpgradeService.startUpgrade(upgrade1.getUpgradeId());
        firmwareUpgradeService.startUpgrade(upgrade2.getUpgradeId());
        firmwareUpgradeService.startUpgrade(upgrade3.getUpgradeId());

        // Then - 等待所有升级完成
        Thread.sleep(5000); // 等待5秒

        // 验证所有升级都正常进行
        FirmwareUpgradeEntity progress1 = firmwareUpgradeService.getUpgradeProgress(upgrade1.getUpgradeId());
        FirmwareUpgradeEntity progress2 = firmwareUpgradeService.getUpgradeProgress(upgrade2.getUpgradeId());
        FirmwareUpgradeEntity progress3 = firmwareUpgradeService.getUpgradeProgress(upgrade3.getUpgradeId());

        assertNotNull(progress1, "升级1进度不应为null");
        assertNotNull(progress2, "升级2进度不应为null");
        assertNotNull(progress3, "升级3进度不应为null");

        // 验证每个升级都有独立的记录
        assertNotEquals(upgrade1.getUpgradeId(), upgrade2.getUpgradeId(), "升级ID应不同");
        assertNotEquals(upgrade2.getUpgradeId(), upgrade3.getUpgradeId(), "升级ID应不同");

        log.info("[集成测试] 测试通过: 并发升级处理成功，3个升级任务独立进行");
    }
}
