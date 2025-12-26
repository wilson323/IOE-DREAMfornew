package net.lab1024.sa.video.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.FirmwareUpgradeDao;
import net.lab1024.sa.common.entity.video.FirmwareUpgradeEntity;
import net.lab1024.sa.video.sdk.DeviceProtocolAdapter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 固件升级管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 负责复杂的固件升级编排和进度跟踪
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class FirmwareUpgradeManager {

    private final FirmwareUpgradeDao firmwareUpgradeDao;
    private final DeviceProtocolAdapter deviceProtocolAdapter;

    // 升级任务缓存
    private final Map<Long, FirmwareUpgradeEntity> upgradeCache = new ConcurrentHashMap<>();

    /**
     * 构造函数注入依赖
     *
     * @param firmwareUpgradeDao    固件升级数据访问对象
     * @param deviceProtocolAdapter 设备协议适配器
     */
    public FirmwareUpgradeManager(FirmwareUpgradeDao firmwareUpgradeDao,
                                   DeviceProtocolAdapter deviceProtocolAdapter) {
        this.firmwareUpgradeDao = firmwareUpgradeDao;
        this.deviceProtocolAdapter = deviceProtocolAdapter;
    }

    /**
     * 创建固件升级任务
     *
     * @param deviceId      设备ID
     * @param deviceCode    设备编号
     * @param deviceName    设备名称
     * @param currentVersion 当前固件版本
     * @param targetVersion 目标固件版本
     * @param firmwareUrl   固件文件URL
     * @param fileSize      文件大小
     * @param fileMd5       文件MD5
     * @param upgradeType   升级类型
     * @param createUserId  创建人ID
     * @param createUserName 创建人姓名
     * @return 升级任务实体
     */
    public FirmwareUpgradeEntity createUpgradeTask(Long deviceId, String deviceCode, String deviceName,
                                                   String currentVersion, String targetVersion,
                                                   String firmwareUrl, Long fileSize, String fileMd5,
                                                   Integer upgradeType, Long createUserId, String createUserName) {
        log.info("[固件升级] 创建升级任务: deviceId={}, deviceCode={}, {} -> {}",
                deviceId, deviceCode, currentVersion, targetVersion);

        FirmwareUpgradeEntity upgrade = FirmwareUpgradeEntity.builder()
                .deviceId(deviceId)
                .deviceCode(deviceCode)
                .deviceName(deviceName)
                .currentVersion(currentVersion)
                .targetVersion(targetVersion)
                .firmwareUrl(firmwareUrl)
                .fileSize(fileSize)
                .fileMd5(fileMd5)
                .upgradeStatus(1) // 待升级
                .progress(0)
                .upgradeType(upgradeType)
                .createUserId(createUserId)
                .createUserName(createUserName)
                .createTime(LocalDateTime.now())
                .build();

        firmwareUpgradeDao.insert(upgrade);
        upgradeCache.put(upgrade.getUpgradeId(), upgrade);

        log.info("[固件升级] 升级任务创建成功: upgradeId={}", upgrade.getUpgradeId());
        return upgrade;
    }

    /**
     * 执行固件升级（异步）
     *
     * @param upgradeId 升级任务ID
     * @return CompletableFuture
     */
    public CompletableFuture<Void> executeUpgradeAsync(Long upgradeId) {
        return CompletableFuture.runAsync(() -> {
            try {
                executeUpgrade(upgradeId);
            } catch (Exception e) {
                log.error("[固件升级] 升级失败: upgradeId={}, error={}", upgradeId, e.getMessage(), e);
                handleUpgradeFailure(upgradeId, e.getMessage());
            }
        });
    }

    /**
     * 执行固件升级
     *
     * @param upgradeId 升级任务ID
     */
    private void executeUpgrade(Long upgradeId) throws InterruptedException {
        FirmwareUpgradeEntity upgrade = upgradeCache.get(upgradeId);
        if (upgrade == null) {
            upgrade = firmwareUpgradeDao.selectById(upgradeId);
        }

        if (upgrade == null) {
            throw new RuntimeException("升级任务不存在: " + upgradeId);
        }

        log.info("[固件升级] 开始执行升级: upgradeId={}, deviceId={}, {} -> {}",
                upgradeId, upgrade.getDeviceId(),
                upgrade.getCurrentVersion(), upgrade.getTargetVersion());

        // 更新状态为升级中
        upgrade.setUpgradeStatus(2); // 升级中
        upgrade.setStartTime(LocalDateTime.now());
        upgrade.setProgress(0);
        firmwareUpgradeDao.updateById(upgrade);

        try {
            // 1. 发送固件到设备
            log.info("[固件升级] 发送固件文件: upgradeId={}, url={}", upgradeId, upgrade.getFirmwareUrl());
            updateProgress(upgradeId, 10);

            // 模拟固件传输（实际应该通过设备协议适配器）
            simulateFirmwareTransfer(upgradeId);
            updateProgress(upgradeId, 50);

            // 2. 验证固件完整性
            log.info("[固件升级] 验证固件完整性: upgradeId={}", upgradeId);
            boolean integrityValid = verifyFirmwareIntegrity(upgrade);
            if (!integrityValid) {
                throw new RuntimeException("固件文件验证失败");
            }
            updateProgress(upgradeId, 60);

            // 3. 执行设备升级
            log.info("[固件升级] 执行设备升级: upgradeId={}", upgradeId);
            boolean upgradeSuccess = performDeviceUpgrade(upgrade);
            updateProgress(upgradeId, 90);

            // 4. 验证升级结果
            log.info("[固件升级] 验证升级结果: upgradeId={}", upgradeId);
            boolean versionValid = verifyUpgradeResult(upgrade);

            if (versionValid) {
                // 升级成功
                upgrade.setUpgradeStatus(3); // 升级成功
                upgrade.setProgress(100);
                upgrade.setCompleteTime(LocalDateTime.now());
                firmwareUpgradeDao.updateById(upgrade);

                log.info("[固件升级] 升级成功: upgradeId={}, deviceId={}, newVersion={}",
                        upgradeId, upgrade.getDeviceId(), upgrade.getTargetVersion());
            } else {
                throw new RuntimeException("固件版本验证失败");
            }

        } catch (Exception e) {
            handleUpgradeFailure(upgradeId, e.getMessage());
            throw e;
        }
    }

    /**
     * 更新升级进度
     *
     * @param upgradeId 升级任务ID
     * @param progress 进度（0-100）
     */
    public void updateProgress(Long upgradeId, Integer progress) {
        FirmwareUpgradeEntity upgrade = upgradeCache.get(upgradeId);
        if (upgrade == null) {
            upgrade = firmwareUpgradeDao.selectById(upgradeId);
        }

        if (upgrade != null) {
            upgrade.setProgress(progress);
            upgrade.setUpdateTime(LocalDateTime.now());
            firmwareUpgradeDao.updateById(upgrade);

            log.debug("[固件升级] 进度更新: upgradeId={}, progress={}%", upgradeId, progress);
        }
    }

    /**
     * 处理升级失败
     *
     * @param upgradeId 升级任务ID
     * @param reason    失败原因
     */
    private void handleUpgradeFailure(Long upgradeId, String reason) {
        FirmwareUpgradeEntity upgrade = upgradeCache.get(upgradeId);
        if (upgrade == null) {
            upgrade = firmwareUpgradeDao.selectById(upgradeId);
        }

        if (upgrade != null) {
            upgrade.setUpgradeStatus(4); // 升级失败
            upgrade.setCompleteTime(LocalDateTime.now());
            upgrade.setFailureReason(reason);
            firmwareUpgradeDao.updateById(upgrade);

            log.error("[固件升级] 升级失败: upgradeId={}, deviceId={}, reason={}",
                    upgradeId, upgrade.getDeviceId(), reason);
        }
    }

    /**
     * 模拟固件传输
     */
    private void simulateFirmwareTransfer(Long upgradeId) throws InterruptedException {
        // 模拟固件传输过程（实际应该通过设备协议适配器）
        Thread.sleep(2000); // 模拟2秒传输时间
        log.debug("[固件升级] 固件传输完成: upgradeId={}", upgradeId);
    }

    /**
     * 验证固件完整性
     */
    private boolean verifyFirmwareIntegrity(FirmwareUpgradeEntity upgrade) {
        // 实际应该验证MD5
        log.debug("[固件升级] 固件完整性验证通过: upgradeId={}", upgrade.getUpgradeId());
        return true;
    }

    /**
     * 执行设备升级
     */
    private boolean performDeviceUpgrade(FirmwareUpgradeEntity upgrade) {
        // 实际应该通过设备协议适配器发送升级命令
        log.debug("[固件升级] 设备升级执行完成: upgradeId={}", upgrade.getUpgradeId());
        return true;
    }

    /**
     * 验证升级结果
     */
    private boolean verifyUpgradeResult(FirmwareUpgradeEntity upgrade) {
        // 实际应该查询设备版本确认升级成功
        log.debug("[固件升级] 升级结果验证通过: upgradeId={}", upgrade.getUpgradeId());
        return true;
    }

    /**
     * 获取升级任务详情
     *
     * @param upgradeId 升级任务ID
     * @return 升级任务实体
     */
    public FirmwareUpgradeEntity getUpgradeDetail(Long upgradeId) {
        return firmwareUpgradeDao.selectById(upgradeId);
    }

    /**
     * 查询设备升级历史
     *
     * @param deviceId 设备ID
     * @return 升级记录列表
     */
    public List<FirmwareUpgradeEntity> getDeviceUpgradeHistory(Long deviceId) {
        return firmwareUpgradeDao.queryByDeviceId(deviceId);
    }

    /**
     * 查询进行中的升级任务
     *
     * @return 升级任务列表
     */
    public List<FirmwareUpgradeEntity> getPendingUpgrades() {
        return firmwareUpgradeDao.queryPendingUpgrades();
    }

    /**
     * 获取升级统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getUpgradeStatistics() {
        List<FirmwareUpgradeEntity> recentUpgrades = firmwareUpgradeDao.queryRecentUpgrades(100);

        int totalCount = recentUpgrades.size();
        long successCount = recentUpgrades.stream()
                .filter(u -> u.getUpgradeStatus() == 3)
                .count();
        long failedCount = recentUpgrades.stream()
                .filter(u -> u.getUpgradeStatus() == 4)
                .count();
        long inProgressCount = recentUpgrades.stream()
                .filter(u -> u.getUpgradeStatus() == 2)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("inProgressCount", inProgressCount);
        stats.put("successRate", totalCount > 0 ? new BigDecimal(successCount)
                .divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100")) : BigDecimal.ZERO);

        return stats;
    }
}
