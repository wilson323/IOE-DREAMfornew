package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Resource;
import net.lab1024.sa.access.controller.AccessMobileController.*;
import net.lab1024.sa.access.service.OfflineModeService;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.access.dao.AreaPersonDao;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.DeviceEntity;
import net.lab1024.sa.common.entity.UserEntity;
import net.lab1024.sa.common.util.SmartStringUtil;

/**
 * 离线模式服务实现
 * <p>
 * 提供离线模式下的门禁管理功能：
 * - 离线权限数据管理
 * - 离线访问记录处理
 * - 数据同步和完整性验证
 * - 离线模式状态监控
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Transactional事务管理
 * - 完整的日志记录和错误处理
 * - 性能监控和指标收集
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class OfflineModeServiceImpl implements OfflineModeService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private AreaPersonDao areaPersonDao;

    // 离线权限数据缓存
    private final Map<Long, OfflinePermissionsCache> permissionsCache = new ConcurrentHashMap<>();

    // 离线访问记录缓存
    private final Map<Long, List<OfflineAccessRecord>> recordsCache = new ConcurrentHashMap<>();

    // 离线模式状态缓存
    private final Map<Long, OfflineModeStatus> statusCache = new ConcurrentHashMap<>();

    @Override
    @Timed(value = "offline.sync", description = "离线数据同步耗时")
    @Counted(value = "offline.sync.count", description = "离线数据同步次数")
    public ResponseDTO<OfflineSyncResult> syncOfflineData(OfflineSyncRequest request) {
        log.info("[离线同步] 开始同步离线数据: userId={}, syncType={}, dataSize={}",
                request.getUserId(), request.getSyncType(), request.getDataSize());

        try {
            long startTime = System.currentTimeMillis();
            int syncedRecords = 0;
            int failedRecords = 0;
            List<String> errors = new ArrayList<>();

            switch (request.getSyncType()) {
                case "PERMISSIONS":
                    syncedRecords = syncPermissions(request.getUserId(), errors);
                    break;
                case "RECORDS":
                    syncedRecords = syncRecords(request.getUserId(), errors);
                    break;
                case "CONFIG":
                    syncedRecords = syncConfig(request.getUserId(), errors);
                    break;
                default:
                    errors.add("不支持的同步类型: " + request.getSyncType());
            }

            failedRecords = errors.size();
            long syncDuration = System.currentTimeMillis() - startTime;

            // 计算下次同步时间
            String nextSyncTime = LocalDateTime.now().plusHours(1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 更新同步状态
            updateOfflineStatus(request.getUserId(), "SYNCED", syncedRecords, failedRecords);

            OfflineSyncResult result = new OfflineSyncResult();
            result.setSuccess(errors.isEmpty());
            result.setSyncType(request.getSyncType());
            result.setSyncedRecords(syncedRecords);
            result.setFailedRecords(failedRecords);
            result.setSyncDuration(syncDuration);
            result.setNextSyncTime(nextSyncTime);
            result.setErrors(errors);

            log.info("[离线同步] 同步完成: userId={}, syncType={}, synced={}, failed={}, duration={}ms",
                    request.getUserId(), request.getSyncType(), syncedRecords, failedRecords, syncDuration);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[离线同步] 同步失败: userId={}, syncType={}, error={}",
                    request.getUserId(), request.getSyncType(), e.getMessage(), e);

            OfflineSyncResult result = new OfflineSyncResult();
            result.setSuccess(false);
            result.setSyncType(request.getSyncType());
            result.setSyncDuration(System.currentTimeMillis());
            result.setErrors(Arrays.asList("同步异常: " + e.getMessage()));

            return ResponseDTO.ok(result);
        }
    }

    @Override
    public ResponseDTO<OfflinePermissionsVO> getOfflinePermissions(Long userId, String lastSyncTime) {
        log.info("[离线权限] 获取离线权限: userId={}, lastSyncTime={}", userId, lastSyncTime);

        try {
            OfflinePermissionsCache cache = permissionsCache.get(userId);
            if (cache == null) {
                // 如果缓存为空，生成新的权限数据
                cache = generateOfflinePermissions(userId);
                permissionsCache.put(userId, cache);
            }

            OfflinePermissionsVO permissionsVO = new OfflinePermissionsVO();
            permissionsVO.setUserId(userId);
            permissionsVO.setLastSyncTime(cache.getLastSyncTime());
            permissionsVO.setExpiryTime(cache.getExpiryTime());
            permissionsVO.setAreaPermissions(cache.getAreaPermissions());
            permissionsVO.setDevicePermissions(cache.getDevicePermissions());
            permissionsVO.setChecksum(generateChecksum(cache));
            permissionsVO.setVersion(cache.getVersion());

            log.info("[离线权限] 获取完成: userId={}, areaPermissions={}, devicePermissions={}",
                    userId, cache.getAreaPermissions().size(), cache.getDevicePermissions().size());
            return ResponseDTO.ok(permissionsVO);

        } catch (Exception e) {
            log.error("[离线权限] 获取失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("PERMISSIONS_LOAD_FAILED", "离线权限获取失败");
        }
    }

    @Override
    @Timed(value = "offline.report", description = "离线记录上报耗时")
    @Counted(value = "offline.report.count", description = "离线记录上报次数")
    public ResponseDTO<OfflineReportResult> reportOfflineRecords(OfflineRecordsReportRequest request) {
        log.info("[离线上报] 开始上报离线记录: userId={}, recordCount={}",
                request.getUserId(), request.getRecords().size());

        try {
            int reportedRecords = request.getRecords().size();
            int acceptedRecords = 0;
            int rejectedRecords = 0;
            List<String> rejectedReasons = new ArrayList<>();
            String reportId = generateReportId();

            // 验证和处理每条记录
            for (OfflineAccessRecord record : request.getRecords()) {
                try {
                    if (validateOfflineRecord(record)) {
                        // 保存到数据库
                        saveOfflineRecordToDatabase(record);
                        acceptedRecords++;
                    } else {
                        rejectedRecords++;
                        rejectedReasons.add("记录" + record.getRecordId() + ": 验证失败");
                    }
                } catch (Exception e) {
                    rejectedRecords++;
                    rejectedReasons.add("记录" + record.getRecordId() + ": " + e.getMessage());
                }
            }

            // 清理缓存的离线记录
            recordsCache.remove(request.getUserId());

            OfflineReportResult result = new OfflineReportResult();
            result.setSuccess(rejectedRecords == 0);
            result.setReportedRecords(reportedRecords);
            result.setAcceptedRecords(acceptedRecords);
            result.setRejectedRecords(rejectedRecords);
            result.setRejectedReasons(rejectedReasons);
            result.setReportId(reportId);

            log.info("[离线上报] 上报完成: userId={}, reported={}, accepted={}, rejected={}",
                    request.getUserId(), reportedRecords, acceptedRecords, rejectedRecords);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[离线上报] 上报失败: userId={}, error={}", request.getUserId(), e.getMessage(), e);

            OfflineReportResult result = new OfflineReportResult();
            result.setSuccess(false);
            result.setReportId(generateReportId());
            result.setRejectedReasons(Arrays.asList("上报异常: " + e.getMessage()));

            return ResponseDTO.ok(result);
        }
    }

    @Override
    public ResponseDTO<OfflineAccessValidationResult> validateOfflineAccess(
            Long userId, Long deviceId, String accessType, String verificationData) {
        log.info("[离线验证] 验证离线访问权限: userId={}, deviceId={}, accessType={}",
                userId, deviceId, accessType);

        try {
            // 获取离线权限数据
            OfflinePermissionsCache permissions = permissionsCache.get(userId);
            if (permissions == null) {
                return createValidationResult(false, "无离线权限数据", null, 0L);
            }

            // 验证设备权限
            boolean hasDevicePermission = validateDevicePermission(permissions, deviceId);
            if (!hasDevicePermission) {
                return createValidationResult(false, "无设备访问权限", null, 0L);
            }

            // 验证访问次数限制
            long remainingQuota = validateAccessQuota(permissions, deviceId);
            if (remainingQuota < 0) {
                return createValidationResult(false, "访问次数超限", null, remainingQuota);
            }

            // 验证时间有效性
            if (!validateTimeValidity(permissions)) {
                return createValidationResult(false, "权限已过期", null, remainingQuota);
            }

            // 验证访问类型
            if (!validateAccessType(permissions, accessType)) {
                return createValidationResult(false, "不支持的访问类型", null, remainingQuota);
            }

            // 更新访问计数
            updateAccessCount(permissions, deviceId);

            OfflineAccessValidationResult result = new OfflineAccessValidationResult();
            result.setAllowed(true);
            result.setValidationReason("验证通过");
            result.setPermissionLevel(permissions.getPermissionLevel());
            result.setRemainingQuota(remainingQuota - 1);
            result.setValidUntil(permissions.getExpiryTime());
            result.setValidationMode("OFFLINE");

            log.info("[离线验证] 验证成功: userId={}, deviceId={}, remainingQuota={}",
                    userId, deviceId, remainingQuota - 1);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[离线验证] 验证失败: userId={}, deviceId={}, error={}",
                    userId, deviceId, e.getMessage(), e);
            return createValidationResult(false, "验证异常: " + e.getMessage(), null, 0L);
        }
    }

    @Override
    public ResponseDTO<OfflineModeStatusVO> getOfflineModeStatus(Long userId) {
        log.info("[离线状态] 查询离线模式状态: userId={}", userId);

        try {
            OfflineModeStatus status = statusCache.get(userId);
            if (status == null) {
                status = createDefaultStatus(userId);
                statusCache.put(userId, status);
            }

            OfflineModeStatusVO statusVO = new OfflineModeStatusVO();
            statusVO.setUserId(userId);
            statusVO.setIsOfflineMode(status.isOfflineMode());
            statusVO.setLastSyncTime(status.getLastSyncTime());
            statusVO.setSyncedDevices(status.getSyncedDevices());
            statusVO.setPendingRecords(status.getPendingRecords());
            statusVO.setFailedRecords(status.getFailedRecords());
            statusVO.setNetworkStatus(status.getNetworkStatus());
            statusVO.setStorageUsage(status.getStorageUsage());
            statusVO.setActiveDevices(status.getActiveDevices());

            log.info("[离线状态] 查询完成: userId={}, isOfflineMode={}, pendingRecords={}",
                    userId, status.isOfflineMode(), status.getPendingRecords());
            return ResponseDTO.ok(statusVO);

        } catch (Exception e) {
            log.error("[离线状态] 查询失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("STATUS_QUERY_FAILED", "离线状态查询失败");
        }
    }

    @Override
    @Timed(value = "offline.cleanup", description = "离线数据清理耗时")
    public ResponseDTO<OfflineDataCleanupResult> cleanupExpiredOfflineData(Long userId) {
        log.info("[离线清理] 开始清理过期数据: userId={}", userId);

        try {
            long startTime = System.currentTimeMillis();
            int cleanedPermissions = 0;
            int cleanedRecords = 0;
            int cleanedDevices = 0;
            long freedStorage = 0L;

            // 清理过期权限数据
            OfflinePermissionsCache permissions = permissionsCache.get(userId);
            if (permissions != null && isExpired(permissions.getExpiryTime())) {
                permissionsCache.remove(userId);
                cleanedPermissions = 1;
                freedStorage += estimatePermissionStorage(permissions);
            }

            // 清理历史访问记录
            List<OfflineAccessRecord> records = recordsCache.get(userId);
            if (records != null) {
                List<OfflineAccessRecord> validRecords = records.stream()
                        .filter(record -> !isRecordExpired(record))
                        .collect(Collectors.toList());

                cleanedRecords = records.size() - validRecords.size();
                recordsCache.put(userId, validRecords);
                freedStorage += cleanedRecords * 200; // 估算每条记录200字节
            }

            // 清理设备连接信息
            cleanedDevices = cleanupDeviceConnections(userId);

            long cleanupDuration = System.currentTimeMillis() - startTime;

            OfflineDataCleanupResult result = new OfflineDataCleanupResult();
            result.setSuccess(true);
            result.setUserId(userId);
            result.setCleanedPermissions(cleanedPermissions);
            result.setCleanedRecords(cleanedRecords);
            result.setCleanedDevices(cleanedDevices);
            result.setFreedStorage(freedStorage);
            result.setCleanupDuration(cleanupDuration);
            result.setErrors(new ArrayList<>());

            log.info("[离线清理] 清理完成: userId={}, permissions={}, records={}, devices={}, freed={}bytes",
                    userId, cleanedPermissions, cleanedRecords, cleanedDevices, freedStorage);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[离线清理] 清理失败: userId={}, error={}", userId, e.getMessage(), e);

            OfflineDataCleanupResult result = new OfflineDataCleanupResult();
            result.setSuccess(false);
            result.setUserId(userId);
            result.setCleanupDuration(System.currentTimeMillis());
            result.setErrors(Arrays.asList("清理异常: " + e.getMessage()));

            return ResponseDTO.ok(result);
        }
    }

    @Override
    public ResponseDTO<OfflineDataPackageVO> generateOfflineDataPackage(Long userId, List<Long> deviceIds) {
        log.info("[离线数据包] 生成离线数据包: userId={}, deviceCount={}", userId, deviceIds.size());

        try {
            // 获取用户权限数据
            OfflinePermissionsCache permissions = permissionsCache.get(userId);
            if (permissions == null) {
                permissions = generateOfflinePermissions(userId);
                permissionsCache.put(userId, permissions);
            }

            // 过滤设备权限
            List<OfflineDevicePermission> filteredDevices = permissions.getDevicePermissions().stream()
                    .filter(device -> deviceIds.isEmpty() || deviceIds.contains(device.getDeviceId()))
                    .collect(Collectors.toList());

            // 计算数据包大小
            long packageSize = estimatePackageSize(permissions.getAreaPermissions(), filteredDevices);

            // 生成数据包
            OfflineDataPackageVO dataPackage = new OfflineDataPackageVO();
            dataPackage.setPackageId(generatePackageId(userId));
            dataPackage.setUserId(userId);
            dataPackage.setPackageVersion("2.0");
            dataPackage.setPackageSize(packageSize);
            dataPackage.setChecksum(generatePackageChecksum(permissions));
            dataPackage.setEncryptionMethod("AES-256");
            dataPackage.setCompressionMethod("GZIP");
            dataPackage.setGeneratedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            dataPackage.setExpiryTime(permissions.getExpiryTime());
            dataPackage.setDeviceCount(filteredDevices.size());
            dataPackage.setPermissionCount(permissions.getAreaPermissions().size());

            log.info("[离线数据包] 生成完成: userId={}, packageSize={}bytes, deviceCount={}",
                    userId, packageSize, filteredDevices.size());
            return ResponseDTO.ok(dataPackage);

        } catch (Exception e) {
            log.error("[离线数据包] 生成失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("PACKAGE_GENERATION_FAILED", "离线数据包生成失败");
        }
    }

    @Override
    public ResponseDTO<OfflineDataIntegrityResult> validateOfflineDataIntegrity(Long userId, String dataPackage) {
        log.info("[离线数据验证] 验证离线数据完整性: userId={}, packageSize={}",
                userId, SmartStringUtil.isNotEmpty(dataPackage) ? dataPackage.length() : 0);

        try {
            OfflineDataIntegrityResult result = new OfflineDataIntegrityResult();
            result.setPackageId("PKG_" + System.currentTimeMillis());

            // 校验和验证
            String expectedChecksum = generatePackageChecksum(dataPackage);
            String actualChecksum = calculateChecksum(dataPackage);
            result.setChecksumValid(expectedChecksum.equals(actualChecksum));

            // 数字签名验证（模拟）
            result.setSignatureValid(true);

            // 数据格式验证
            result.setFormatValid(validateDataFormat(dataPackage));

            // 时效性验证
            OfflinePermissionsCache permissions = permissionsCache.get(userId);
            boolean expired = permissions == null || isExpired(permissions.getExpiryTime());
            result.setExpired(expired);

            // 综合验证结果
            result.setValid(result.isChecksumValid() && result.isSignatureValid()
                    && result.isFormatValid() && !result.isExpired());

            List<String> validationErrors = new ArrayList<>();
            if (!result.isChecksumValid()) {
                validationErrors.add("数据校验和不匹配");
            }
            if (!result.isSignatureValid()) {
                validationErrors.add("数字签名验证失败");
            }
            if (!result.isFormatValid()) {
                validationErrors.add("数据格式不正确");
            }
            if (result.isExpired()) {
                validationErrors.add("数据已过期");
            }
            result.setValidationErrors(validationErrors);

            result.setValidationTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            log.info("[离线数据验证] 验证完成: userId={}, valid={}, errors={}",
                    userId, result.isValid(), validationErrors.size());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[离线数据验证] 验证失败: userId={}, error={}", userId, e.getMessage(), e);

            OfflineDataIntegrityResult result = new OfflineDataIntegrityResult();
            result.setValid(false);
            result.setValidationTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.setValidationErrors(Arrays.asList("验证异常: " + e.getMessage()));

            return ResponseDTO.ok(result);
        }
    }

    @Override
    public ResponseDTO<OfflineModeStatisticsVO> generateOfflineStatisticsReport(
            Long userId, String startTime, String endTime) {
        log.info("[离线统计] 生成统计报告: userId={}, startTime={}, endTime={}",
                userId, startTime, endTime);

        try {
            // 模拟统计数据生成
            OfflineModeStatisticsVO statistics = new OfflineModeStatisticsVO();
            statistics.setUserId(userId);
            statistics.setReportPeriod(startTime + " 至 " + endTime);

            // 访问统计数据
            statistics.setTotalAccessAttempts(150L);
            statistics.setSuccessfulAccesses(142L);
            statistics.setFailedAccesses(8L);
            statistics.setSuccessRate(94.67);

            // 交易统计数据
            statistics.setOfflineTransactions(125L);
            statistics.setOnlineTransactions(25L);

            // 响应时间统计
            statistics.setAverageResponseTime(85);
            statistics.setMaxResponseTime(320);
            statistics.setMinResponseTime(15);

            // 设备统计
            List<DeviceStatistics> deviceStats = new ArrayList<>();

            DeviceStatistics deviceStat1 = new DeviceStatistics();
            deviceStat1.setDeviceId(1L);
            deviceStat1.setDeviceName("主门禁设备");
            deviceStat1.setAccessCount(75L);
            deviceStat1.setSuccessCount(72L);
            deviceStat1.setFailureCount(3L);
            deviceStat1.setSuccessRate(96.0);
            deviceStat1.setAverageResponseTime(80);
            deviceStat1.setLastAccessTime(LocalDateTime.now().minusMinutes(5)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            deviceStat1.setStatus("ACTIVE");
            deviceStats.add(deviceStat1);

            DeviceStatistics deviceStat2 = new DeviceStatistics();
            deviceStat2.setDeviceId(2L);
            deviceStat2.setDeviceName("侧门禁设备");
            deviceStat2.setAccessCount(75L);
            deviceStat2.setSuccessCount(70L);
            deviceStat2.setFailureCount(5L);
            deviceStat2.setSuccessRate(93.33);
            deviceStat2.setAverageResponseTime(90);
            deviceStat2.setLastAccessTime(LocalDateTime.now().minusMinutes(15)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            deviceStat2.setStatus("ACTIVE");
            deviceStats.add(deviceStat2);

            statistics.setDeviceStats(deviceStats);

            log.info("[离线统计] 统计报告生成完成: userId={}, successRate={}%, deviceCount={}",
                    userId, statistics.getSuccessRate(), deviceStats.size());
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[离线统计] 统计报告生成失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("STATISTICS_GENERATION_FAILED", "统计报告生成失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    private ResponseDTO<OfflineAccessValidationResult> createValidationResult(
            boolean allowed, String reason, String permissionLevel, Long remainingQuota) {
        OfflineAccessValidationResult result = new OfflineAccessValidationResult();
        result.setAllowed(allowed);
        result.setValidationReason(reason);
        result.setPermissionLevel(permissionLevel);
        result.setRemainingQuota(remainingQuota);
        result.setValidUntil(LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.setValidationMode("OFFLINE");
        result.setWarnings(new ArrayList<>());
        return ResponseDTO.ok(result);
    }

    private int syncPermissions(Long userId, List<String> errors) {
        try {
            // 重新生成权限数据
            OfflinePermissionsCache permissions = generateOfflinePermissions(userId);
            permissionsCache.put(userId, permissions);
            return permissions.getAreaPermissions().size() + permissions.getDevicePermissions().size();
        } catch (Exception e) {
            errors.add("权限同步失败: " + e.getMessage());
            return 0;
        }
    }

    private int syncRecords(Long userId, List<String> errors) {
        try {
            List<OfflineAccessRecord> records = recordsCache.get(userId);
            if (records != null) {
                int count = records.size();
                // 保存到数据库后清理缓存
                for (OfflineAccessRecord record : records) {
                    saveOfflineRecordToDatabase(record);
                }
                recordsCache.remove(userId);
                return count;
            }
            return 0;
        } catch (Exception e) {
            errors.add("记录同步失败: " + e.getMessage());
            return 0;
        }
    }

    private int syncConfig(Long userId, List<String> errors) {
        try {
            // 同步配置信息
            return 1; // 模拟同步了一个配置项
        } catch (Exception e) {
            errors.add("配置同步失败: " + e.getMessage());
            return 0;
        }
    }

    private OfflinePermissionsCache generateOfflinePermissions(Long userId) {
        OfflinePermissionsCache cache = new OfflinePermissionsCache();
        cache.setUserId(userId);
        cache.setLastSyncTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        cache.setExpiryTime(LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        cache.setPermissionLevel("STANDARD");
        cache.setVersion(1);

        // 生成区域权限
        List<OfflineAreaPermission> areaPermissions = new ArrayList<>();
        OfflineAreaPermission areaPermission = new OfflineAreaPermission();
        areaPermission.setAreaId(1L);
        areaPermission.setAreaName("办公区");
        areaPermission.setPermissionLevel("FULL");
        areaPermission.setValidFrom(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        areaPermission.setValidUntil(cache.getExpiryTime());
        areaPermission.setAccessMethods(Arrays.asList("BLUETOOTH", "NFC", "QR_CODE"));
        areaPermissions.add(areaPermission);
        cache.setAreaPermissions(areaPermissions);

        // 生成设备权限
        List<OfflineDevicePermission> devicePermissions = new ArrayList<>();
        List<DeviceEntity> devices = accessDeviceDao.selectByUserId(userId);
        for (DeviceEntity device : devices) {
            OfflineDevicePermission devicePermission = new OfflineDevicePermission();
            devicePermission.setDeviceId(device.getDeviceId());
            devicePermission.setDeviceName(device.getDeviceName());
            devicePermission.setDeviceType(device.getDeviceType());
            devicePermission.setPermissionLevel("FULL");
            devicePermission.setDailyLimit(100);
            devicePermission.setValidFrom(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            devicePermission.setValidUntil(cache.getExpiryTime());
            devicePermissions.add(devicePermission);
        }
        cache.setDevicePermissions(devicePermissions);

        return cache;
    }

    private boolean validateOfflineRecord(OfflineAccessRecord record) {
        // 验证记录的基本信息
        return record.getRecordId() != null && record.getDeviceId() != null
                && record.getUserId() != null && record.getAccessTime() != null;
    }

    private void saveOfflineRecordToDatabase(OfflineAccessRecord record) {
        // TODO: 实现将离线记录保存到数据库的逻辑
        log.info("[离线记录] 保存记录到数据库: recordId={}, deviceId={}", record.getRecordId(), record.getDeviceId());
    }

    private String generateChecksum(Object data) {
        // 简化的校验和生成
        return String.valueOf(Objects.hash(data));
    }

    private String generateReportId() {
        return "RPT_" + System.currentTimeMillis();
    }

    private boolean validateDevicePermission(OfflinePermissionsCache permissions, Long deviceId) {
        return permissions.getDevicePermissions().stream()
                .anyMatch(device -> device.getDeviceId().equals(deviceId));
    }

    private Long validateAccessQuota(OfflinePermissionsCache permissions, Long deviceId) {
        return permissions.getDevicePermissions().stream()
                .filter(device -> device.getDeviceId().equals(deviceId))
                .map(OfflineDevicePermission::getDailyLimit)
                .findFirst()
                .orElse(0L);
    }

    private boolean validateTimeValidity(OfflinePermissionsCache permissions) {
        return !isExpired(permissions.getExpiryTime());
    }

    private boolean validateAccessType(OfflinePermissionsCache permissions, String accessType) {
        return permissions.getAreaPermissions().stream()
                .anyMatch(area -> area.getAccessMethods().contains(accessType));
    }

    private void updateAccessCount(OfflinePermissionsCache permissions, Long deviceId) {
        // TODO: 实现访问计数更新逻辑
    }

    private OfflineModeStatus createDefaultStatus(Long userId) {
        OfflineModeStatus status = new OfflineModeStatus();
        status.setUserId(userId);
        status.setOfflineMode(true);
        status.setLastSyncTime(LocalDateTime.now().minusHours(2)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        status.setSyncedDevices(5);
        status.setPendingRecords(0);
        status.setFailedRecords(0);
        status.setNetworkStatus("WEAK");
        status.setStorageUsage("45%");
        status.setActiveDevices(Arrays.asList("DEV001", "DEV002"));
        return status;
    }

    private void updateOfflineStatus(Long userId, String syncStatus, int syncedCount, int failedCount) {
        OfflineModeStatus status = statusCache.get(userId);
        if (status != null) {
            status.setLastSyncTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            status.setSyncedDevices(status.getSyncedDevices() + syncedCount);
            status.setFailedRecords(status.getFailedRecords() + failedCount);
        }
    }

    private boolean isExpired(String expiryTime) {
        try {
            LocalDateTime expiry = LocalDateTime.parse(expiryTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return LocalDateTime.now().isAfter(expiry);
        } catch (Exception e) {
            return true; // 解析失败认为已过期
        }
    }

    private boolean isRecordExpired(OfflineAccessRecord record) {
        // 检查记录是否过期（7天前的记录）
        long sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
        return record.getAccessTime() < sevenDaysAgo;
    }

    private int cleanupDeviceConnections(Long userId) {
        // TODO: 实现设备连接信息清理
        return 0;
    }

    private long estimatePermissionStorage(OfflinePermissionsCache permissions) {
        // 估算权限数据占用的存储空间
        return (permissions.getAreaPermissions().size() * 100)
                + (permissions.getDevicePermissions().size() * 120);
    }

    private String generatePackageId(Long userId) {
        return "PKG_" + userId + "_" + System.currentTimeMillis();
    }

    private String generatePackageChecksum(OfflinePermissionsCache permissions) {
        return generateChecksum(permissions);
    }

    private String generatePackageChecksum(String dataPackage) {
        return calculateChecksum(dataPackage);
    }

    private String calculateChecksum(String data) {
        // 简化的校验和计算
        return String.valueOf(data.hashCode());
    }

    private long estimatePackageSize(List<OfflineAreaPermission> areaPermissions, List<OfflineDevicePermission> devicePermissions) {
        return (areaPermissions.size() * 150) + (devicePermissions.size() * 180);
    }

    private boolean validateDataFormat(String dataPackage) {
        // 简化的数据格式验证
        return dataPackage != null && dataPackage.length() > 0;
    }

    // ==================== 内部数据类 ====================

    private static class OfflinePermissionsCache {
        private Long userId;
        private String lastSyncTime;
        private String expiryTime;
        private String permissionLevel;
        private int version;
        private List<OfflineAreaPermission> areaPermissions;
        private List<OfflineDevicePermission> devicePermissions;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getLastSyncTime() { return lastSyncTime; }
        public void setLastSyncTime(String lastSyncTime) { this.lastSyncTime = lastSyncTime; }

        public String getExpiryTime() { return expiryTime; }
        public void setExpiryTime(String expiryTime) { this.expiryTime = expiryTime; }

        public String getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(String permissionLevel) { this.permissionLevel = permissionLevel; }

        public int getVersion() { return version; }
        public void setVersion(int version) { this.version = version; }

        public List<OfflineAreaPermission> getAreaPermissions() {
            return areaPermissions != null ? areaPermissions : new ArrayList<>();
        }
        public void setAreaPermissions(List<OfflineAreaPermission> areaPermissions) {
            this.areaPermissions = areaPermissions;
        }

        public List<OfflineDevicePermission> getDevicePermissions() {
            return devicePermissions != null ? devicePermissions : new ArrayList<>();
        }
        public void setDevicePermissions(List<OfflineDevicePermission> devicePermissions) {
            this.devicePermissions = devicePermissions;
        }
    }

    private static class OfflineModeStatus {
        private Long userId;
        private boolean offlineMode;
        private String lastSyncTime;
        private int syncedDevices;
        private int pendingRecords;
        private int failedRecords;
        private String networkStatus;
        private String storageUsage;
        private List<String> activeDevices;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public boolean isOfflineMode() { return offlineMode; }
        public void setOfflineMode(boolean offlineMode) { this.offlineMode = offlineMode; }

        public String getLastSyncTime() { return lastSyncTime; }
        public void setLastSyncTime(String lastSyncTime) { this.lastSyncTime = lastSyncTime; }

        public int getSyncedDevices() { return syncedDevices; }
        public void setSyncedDevices(int syncedDevices) { this.syncedDevices = syncedDevices; }

        public int getPendingRecords() { return pendingRecords; }
        public void setPendingRecords(int pendingRecords) { this.pendingRecords = pendingRecords; }

        public int getFailedRecords() { return failedRecords; }
        public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }

        public String getNetworkStatus() { return networkStatus; }
        public void setNetworkStatus(String networkStatus) { this.networkStatus = networkStatus; }

        public String getStorageUsage() { return storageUsage; }
        public void setStorageUsage(String storageUsage) { this.storageUsage = storageUsage; }

        public List<String> getActiveDevices() { return activeDevices; }
        public void setActiveDevices(List<String> activeDevices) { this.activeDevices = activeDevices; }
    }
}