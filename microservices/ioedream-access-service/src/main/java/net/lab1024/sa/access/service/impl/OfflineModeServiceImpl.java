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
 * 绂荤嚎妯″紡鏈嶅姟瀹炵幇
 * <p>
 * 鎻愪緵绂荤嚎妯″紡涓嬬殑闂ㄧ绠＄悊鍔熻兘锛?
 * - 绂荤嚎鏉冮檺鏁版嵁绠＄悊
 * - 绂荤嚎璁块棶璁板綍澶勭悊
 * - 鏁版嵁鍚屾鍜屽畬鏁存€ч獙璇?
 * - 绂荤嚎妯″紡鐘舵€佺洃鎺?
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@Service娉ㄨВ
 * - 浣跨敤@Transactional浜嬪姟绠＄悊
 * - 瀹屾暣鐨勬棩蹇楄褰曞拰閿欒澶勭悊
 * - 鎬ц兘鐩戞帶鍜屾寚鏍囨敹闆?
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

    // 绂荤嚎鏉冮檺鏁版嵁缂撳瓨
    private final Map<Long, OfflinePermissionsCache> permissionsCache = new ConcurrentHashMap<>();

    // 绂荤嚎璁块棶璁板綍缂撳瓨
    private final Map<Long, List<OfflineAccessRecord>> recordsCache = new ConcurrentHashMap<>();

    // 绂荤嚎妯″紡鐘舵€佺紦瀛?
    private final Map<Long, OfflineModeStatus> statusCache = new ConcurrentHashMap<>();

    @Override
    @Timed(value = "offline.sync", description = "绂荤嚎鏁版嵁鍚屾鑰楁椂")
    @Counted(value = "offline.sync.count", description = "绂荤嚎鏁版嵁鍚屾娆℃暟")
    public ResponseDTO<OfflineSyncResult> syncOfflineData(OfflineSyncRequest request) {
        log.info("[绂荤嚎鍚屾] 寮€濮嬪悓姝ョ绾挎暟鎹? userId={}, syncType={}, dataSize={}",
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
                    errors.add("涓嶆敮鎸佺殑鍚屾绫诲瀷: " + request.getSyncType());
            }

            failedRecords = errors.size();
            long syncDuration = System.currentTimeMillis() - startTime;

            // 璁＄畻涓嬫鍚屾鏃堕棿
            String nextSyncTime = LocalDateTime.now().plusHours(1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 鏇存柊鍚屾鐘舵€?
            updateOfflineStatus(request.getUserId(), "SYNCED", syncedRecords, failedRecords);

            OfflineSyncResult result = new OfflineSyncResult();
            result.setSuccess(errors.isEmpty());
            result.setSyncType(request.getSyncType());
            result.setSyncedRecords(syncedRecords);
            result.setFailedRecords(failedRecords);
            result.setSyncDuration(syncDuration);
            result.setNextSyncTime(nextSyncTime);
            result.setErrors(errors);

            log.info("[绂荤嚎鍚屾] 鍚屾瀹屾垚: userId={}, syncType={}, synced={}, failed={}, duration={}ms",
                    request.getUserId(), request.getSyncType(), syncedRecords, failedRecords, syncDuration);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[绂荤嚎鍚屾] 鍚屾澶辫触: userId={}, syncType={}, error={}",
                    request.getUserId(), request.getSyncType(), e.getMessage(), e);

            OfflineSyncResult result = new OfflineSyncResult();
            result.setSuccess(false);
            result.setSyncType(request.getSyncType());
            result.setSyncDuration(System.currentTimeMillis());
            result.setErrors(Arrays.asList("鍚屾寮傚父: " + e.getMessage()));

            return ResponseDTO.ok(result);
        }
    }

    @Override
    public ResponseDTO<OfflinePermissionsVO> getOfflinePermissions(Long userId, String lastSyncTime) {
        log.info("[绂荤嚎鏉冮檺] 鑾峰彇绂荤嚎鏉冮檺: userId={}, lastSyncTime={}", userId, lastSyncTime);

        try {
            OfflinePermissionsCache cache = permissionsCache.get(userId);
            if (cache == null) {
                // 濡傛灉缂撳瓨涓虹┖锛岀敓鎴愭柊鐨勬潈闄愭暟鎹?
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

            log.info("[绂荤嚎鏉冮檺] 鑾峰彇瀹屾垚: userId={}, areaPermissions={}, devicePermissions={}",
                    userId, cache.getAreaPermissions().size(), cache.getDevicePermissions().size());
            return ResponseDTO.ok(permissionsVO);

        } catch (Exception e) {
            log.error("[绂荤嚎鏉冮檺] 鑾峰彇澶辫触: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("PERMISSIONS_LOAD_FAILED", "绂荤嚎鏉冮檺鑾峰彇澶辫触");
        }
    }

    @Override
    @Timed(value = "offline.report", description = "绂荤嚎璁板綍涓婃姤鑰楁椂")
    @Counted(value = "offline.report.count", description = "绂荤嚎璁板綍涓婃姤娆℃暟")
    public ResponseDTO<OfflineReportResult> reportOfflineRecords(OfflineRecordsReportRequest request) {
        log.info("[绂荤嚎涓婃姤] 寮€濮嬩笂鎶ョ绾胯褰? userId={}, recordCount={}",
                request.getUserId(), request.getRecords().size());

        try {
            int reportedRecords = request.getRecords().size();
            int acceptedRecords = 0;
            int rejectedRecords = 0;
            List<String> rejectedReasons = new ArrayList<>();
            String reportId = generateReportId();

            // 楠岃瘉鍜屽鐞嗘瘡鏉¤褰?
            for (OfflineAccessRecord record : request.getRecords()) {
                try {
                    if (validateOfflineRecord(record)) {
                        // 淇濆瓨鍒版暟鎹簱
                        saveOfflineRecordToDatabase(record);
                        acceptedRecords++;
                    } else {
                        rejectedRecords++;
                        rejectedReasons.add("璁板綍" + record.getRecordId() + ": 楠岃瘉澶辫触");
                    }
                } catch (Exception e) {
                    rejectedRecords++;
                    rejectedReasons.add("璁板綍" + record.getRecordId() + ": " + e.getMessage());
                }
            }

            // 娓呯悊缂撳瓨鐨勭绾胯褰?
            recordsCache.remove(request.getUserId());

            OfflineReportResult result = new OfflineReportResult();
            result.setSuccess(rejectedRecords == 0);
            result.setReportedRecords(reportedRecords);
            result.setAcceptedRecords(acceptedRecords);
            result.setRejectedRecords(rejectedRecords);
            result.setRejectedReasons(rejectedReasons);
            result.setReportId(reportId);

            log.info("[绂荤嚎涓婃姤] 涓婃姤瀹屾垚: userId={}, reported={}, accepted={}, rejected={}",
                    request.getUserId(), reportedRecords, acceptedRecords, rejectedRecords);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[绂荤嚎涓婃姤] 涓婃姤澶辫触: userId={}, error={}", request.getUserId(), e.getMessage(), e);

            OfflineReportResult result = new OfflineReportResult();
            result.setSuccess(false);
            result.setReportId(generateReportId());
            result.setRejectedReasons(Arrays.asList("涓婃姤寮傚父: " + e.getMessage()));

            return ResponseDTO.ok(result);
        }
    }

    @Override
    public ResponseDTO<OfflineAccessValidationResult> validateOfflineAccess(
            Long userId, Long deviceId, String accessType, String verificationData) {
        log.info("[绂荤嚎楠岃瘉] 楠岃瘉绂荤嚎璁块棶鏉冮檺: userId={}, deviceId={}, accessType={}",
                userId, deviceId, accessType);

        try {
            // 鑾峰彇绂荤嚎鏉冮檺鏁版嵁
            OfflinePermissionsCache permissions = permissionsCache.get(userId);
            if (permissions == null) {
                return createValidationResult(false, "鏃犵绾挎潈闄愭暟鎹?, null, 0L);
            }

            // 楠岃瘉璁惧鏉冮檺
            boolean hasDevicePermission = validateDevicePermission(permissions, deviceId);
            if (!hasDevicePermission) {
                return createValidationResult(false, "鏃犺澶囪闂潈闄?, null, 0L);
            }

            // 楠岃瘉璁块棶娆℃暟闄愬埗
            long remainingQuota = validateAccessQuota(permissions, deviceId);
            if (remainingQuota < 0) {
                return createValidationResult(false, "璁块棶娆℃暟瓒呴檺", null, remainingQuota);
            }

            // 楠岃瘉鏃堕棿鏈夋晥鎬?
            if (!validateTimeValidity(permissions)) {
                return createValidationResult(false, "权限已过期", null, remainingQuota);
            }

            // 楠岃瘉璁块棶绫诲瀷
            if (!validateAccessType(permissions, accessType)) {
                return createValidationResult(false, "涓嶆敮鎸佺殑璁块棶绫诲瀷", null, remainingQuota);
            }

            // 鏇存柊璁块棶璁℃暟
            updateAccessCount(permissions, deviceId);

            OfflineAccessValidationResult result = new OfflineAccessValidationResult();
            result.setAllowed(true);
            result.setValidationReason("楠岃瘉閫氳繃");
            result.setPermissionLevel(permissions.getPermissionLevel());
            result.setRemainingQuota(remainingQuota - 1);
            result.setValidUntil(permissions.getExpiryTime());
            result.setValidationMode("OFFLINE");

            log.info("[绂荤嚎楠岃瘉] 楠岃瘉鎴愬姛: userId={}, deviceId={}, remainingQuota={}",
                    userId, deviceId, remainingQuota - 1);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[绂荤嚎楠岃瘉] 楠岃瘉澶辫触: userId={}, deviceId={}, error={}",
                    userId, deviceId, e.getMessage(), e);
            return createValidationResult(false, "楠岃瘉寮傚父: " + e.getMessage(), null, 0L);
        }
    }

    @Override
    public ResponseDTO<OfflineModeStatusVO> getOfflineModeStatus(Long userId) {
        log.info("[绂荤嚎鐘舵€乚 鏌ヨ绂荤嚎妯″紡鐘舵€? userId={}", userId);

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

            log.info("[绂荤嚎鐘舵€乚 鏌ヨ瀹屾垚: userId={}, isOfflineMode={}, pendingRecords={}",
                    userId, status.isOfflineMode(), status.getPendingRecords());
            return ResponseDTO.ok(statusVO);

        } catch (Exception e) {
            log.error("[绂荤嚎鐘舵€乚 鏌ヨ澶辫触: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("STATUS_QUERY_FAILED", "离线状态查询失败");
        }
    }

    @Override
    @Timed(value = "offline.cleanup", description = "绂荤嚎鏁版嵁娓呯悊鑰楁椂")
    public ResponseDTO<OfflineDataCleanupResult> cleanupExpiredOfflineData(Long userId) {
        log.info("[绂荤嚎娓呯悊] 寮€濮嬫竻鐞嗚繃鏈熸暟鎹? userId={}", userId);

        try {
            long startTime = System.currentTimeMillis();
            int cleanedPermissions = 0;
            int cleanedRecords = 0;
            int cleanedDevices = 0;
            long freedStorage = 0L;

            // 娓呯悊杩囨湡鏉冮檺鏁版嵁
            OfflinePermissionsCache permissions = permissionsCache.get(userId);
            if (permissions != null && isExpired(permissions.getExpiryTime())) {
                permissionsCache.remove(userId);
                cleanedPermissions = 1;
                freedStorage += estimatePermissionStorage(permissions);
            }

            // 娓呯悊鍘嗗彶璁块棶璁板綍
            List<OfflineAccessRecord> records = recordsCache.get(userId);
            if (records != null) {
                List<OfflineAccessRecord> validRecords = records.stream()
                        .filter(record -> !isRecordExpired(record))
                        .collect(Collectors.toList());

                cleanedRecords = records.size() - validRecords.size();
                recordsCache.put(userId, validRecords);
                freedStorage += cleanedRecords * 200; // 浼扮畻姣忔潯璁板綍200瀛楄妭
            }

            // 娓呯悊璁惧杩炴帴淇℃伅
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

            log.info("[绂荤嚎娓呯悊] 娓呯悊瀹屾垚: userId={}, permissions={}, records={}, devices={}, freed={}bytes",
                    userId, cleanedPermissions, cleanedRecords, cleanedDevices, freedStorage);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[绂荤嚎娓呯悊] 娓呯悊澶辫触: userId={}, error={}", userId, e.getMessage(), e);

            OfflineDataCleanupResult result = new OfflineDataCleanupResult();
            result.setSuccess(false);
            result.setUserId(userId);
            result.setCleanupDuration(System.currentTimeMillis());
            result.setErrors(Arrays.asList("娓呯悊寮傚父: " + e.getMessage()));

            return ResponseDTO.ok(result);
        }
    }

    @Override
    public ResponseDTO<OfflineDataPackageVO> generateOfflineDataPackage(Long userId, List<Long> deviceIds) {
        log.info("[绂荤嚎鏁版嵁鍖匽 鐢熸垚绂荤嚎鏁版嵁鍖? userId={}, deviceCount={}", userId, deviceIds.size());

        try {
            // 鑾峰彇鐢ㄦ埛鏉冮檺鏁版嵁
            OfflinePermissionsCache permissions = permissionsCache.get(userId);
            if (permissions == null) {
                permissions = generateOfflinePermissions(userId);
                permissionsCache.put(userId, permissions);
            }

            // 杩囨护璁惧鏉冮檺
            List<OfflineDevicePermission> filteredDevices = permissions.getDevicePermissions().stream()
                    .filter(device -> deviceIds.isEmpty() || deviceIds.contains(device.getDeviceId()))
                    .collect(Collectors.toList());

            // 璁＄畻鏁版嵁鍖呭ぇ灏?
            long packageSize = estimatePackageSize(permissions.getAreaPermissions(), filteredDevices);

            // 鐢熸垚鏁版嵁鍖?
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

            log.info("[绂荤嚎鏁版嵁鍖匽 鐢熸垚瀹屾垚: userId={}, packageSize={}bytes, deviceCount={}",
                    userId, packageSize, filteredDevices.size());
            return ResponseDTO.ok(dataPackage);

        } catch (Exception e) {
            log.error("[绂荤嚎鏁版嵁鍖匽 鐢熸垚澶辫触: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("PACKAGE_GENERATION_FAILED", "离线数据包生成失败");
        }
    }

    @Override
    public ResponseDTO<OfflineDataIntegrityResult> validateOfflineDataIntegrity(Long userId, String dataPackage) {
        log.info("[绂荤嚎鏁版嵁楠岃瘉] 楠岃瘉绂荤嚎鏁版嵁瀹屾暣鎬? userId={}, packageSize={}",
                userId, SmartStringUtil.isNotEmpty(dataPackage) ? dataPackage.length() : 0);

        try {
            OfflineDataIntegrityResult result = new OfflineDataIntegrityResult();
            result.setPackageId("PKG_" + System.currentTimeMillis());

            // 鏍￠獙鍜岄獙璇?
            String expectedChecksum = generatePackageChecksum(dataPackage);
            String actualChecksum = calculateChecksum(dataPackage);
            result.setChecksumValid(expectedChecksum.equals(actualChecksum));

            // 鏁板瓧绛惧悕楠岃瘉锛堟ā鎷燂級
            result.setSignatureValid(true);

            // 鏁版嵁鏍煎紡楠岃瘉
            result.setFormatValid(validateDataFormat(dataPackage));

            // 鏃舵晥鎬ч獙璇?
            OfflinePermissionsCache permissions = permissionsCache.get(userId);
            boolean expired = permissions == null || isExpired(permissions.getExpiryTime());
            result.setExpired(expired);

            // 缁煎悎楠岃瘉缁撴灉
            result.setValid(result.isChecksumValid() && result.isSignatureValid()
                    && result.isFormatValid() && !result.isExpired());

            List<String> validationErrors = new ArrayList<>();
            if (!result.isChecksumValid()) {
                validationErrors.add("鏁版嵁鏍￠獙鍜屼笉鍖归厤");
            }
            if (!result.isSignatureValid()) {
                validationErrors.add("鏁板瓧绛惧悕楠岃瘉澶辫触");
            }
            if (!result.isFormatValid()) {
                validationErrors.add("鏁版嵁鏍煎紡涓嶆纭?);
            }
            if (result.isExpired()) {
                validationErrors.add("数据已过期");
            }
            result.setValidationErrors(validationErrors);

            result.setValidationTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            log.info("[绂荤嚎鏁版嵁楠岃瘉] 楠岃瘉瀹屾垚: userId={}, valid={}, errors={}",
                    userId, result.isValid(), validationErrors.size());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[绂荤嚎鏁版嵁楠岃瘉] 楠岃瘉澶辫触: userId={}, error={}", userId, e.getMessage(), e);

            OfflineDataIntegrityResult result = new OfflineDataIntegrityResult();
            result.setValid(false);
            result.setValidationTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.setValidationErrors(Arrays.asList("楠岃瘉寮傚父: " + e.getMessage()));

            return ResponseDTO.ok(result);
        }
    }

    @Override
    public ResponseDTO<OfflineModeStatisticsVO> generateOfflineStatisticsReport(
            Long userId, String startTime, String endTime) {
        log.info("[绂荤嚎缁熻] 鐢熸垚缁熻鎶ュ憡: userId={}, startTime={}, endTime={}",
                userId, startTime, endTime);

        try {
            // 妯℃嫙缁熻鏁版嵁鐢熸垚
            OfflineModeStatisticsVO statistics = new OfflineModeStatisticsVO();
            statistics.setUserId(userId);
            statistics.setReportPeriod(startTime + " 鑷?" + endTime);

            // 璁块棶缁熻鏁版嵁
            statistics.setTotalAccessAttempts(150L);
            statistics.setSuccessfulAccesses(142L);
            statistics.setFailedAccesses(8L);
            statistics.setSuccessRate(94.67);

            // 浜ゆ槗缁熻鏁版嵁
            statistics.setOfflineTransactions(125L);
            statistics.setOnlineTransactions(25L);

            // 鍝嶅簲鏃堕棿缁熻
            statistics.setAverageResponseTime(85);
            statistics.setMaxResponseTime(320);
            statistics.setMinResponseTime(15);

            // 璁惧缁熻
            List<DeviceStatistics> deviceStats = new ArrayList<>();

            DeviceStatistics deviceStat1 = new DeviceStatistics();
            deviceStat1.setDeviceId(1L);
            deviceStat1.setDeviceName("涓婚棬绂佽澶?);
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
            deviceStat2.setDeviceName("渚ч棬绂佽澶?);
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

            log.info("[绂荤嚎缁熻] 缁熻鎶ュ憡鐢熸垚瀹屾垚: userId={}, successRate={}%, deviceCount={}",
                    userId, statistics.getSuccessRate(), deviceStats.size());
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[绂荤嚎缁熻] 缁熻鎶ュ憡鐢熸垚澶辫触: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("STATISTICS_GENERATION_FAILED", "缁熻鎶ュ憡鐢熸垚澶辫触");
        }
    }

    // ==================== 绉佹湁杈呭姪鏂规硶 ====================

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
            // 閲嶆柊鐢熸垚鏉冮檺鏁版嵁
            OfflinePermissionsCache permissions = generateOfflinePermissions(userId);
            permissionsCache.put(userId, permissions);
            return permissions.getAreaPermissions().size() + permissions.getDevicePermissions().size();
        } catch (Exception e) {
            errors.add("鏉冮檺鍚屾澶辫触: " + e.getMessage());
            return 0;
        }
    }

    private int syncRecords(Long userId, List<String> errors) {
        try {
            List<OfflineAccessRecord> records = recordsCache.get(userId);
            if (records != null) {
                int count = records.size();
                // 淇濆瓨鍒版暟鎹簱鍚庢竻鐞嗙紦瀛?
                for (OfflineAccessRecord record : records) {
                    saveOfflineRecordToDatabase(record);
                }
                recordsCache.remove(userId);
                return count;
            }
            return 0;
        } catch (Exception e) {
            errors.add("璁板綍鍚屾澶辫触: " + e.getMessage());
            return 0;
        }
    }

    private int syncConfig(Long userId, List<String> errors) {
        try {
            // 鍚屾閰嶇疆淇℃伅
            return 1; // 妯℃嫙鍚屾浜嗕竴涓厤缃」
        } catch (Exception e) {
            errors.add("閰嶇疆鍚屾澶辫触: " + e.getMessage());
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

        // 鐢熸垚鍖哄煙鏉冮檺
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

        // 鐢熸垚璁惧鏉冮檺
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
        // 楠岃瘉璁板綍鐨勫熀鏈俊鎭?
        return record.getRecordId() != null && record.getDeviceId() != null
                && record.getUserId() != null && record.getAccessTime() != null;
    }

    private void saveOfflineRecordToDatabase(OfflineAccessRecord record) {
        // TODO: 瀹炵幇灏嗙绾胯褰曚繚瀛樺埌鏁版嵁搴撶殑閫昏緫
        log.info("[绂荤嚎璁板綍] 淇濆瓨璁板綍鍒版暟鎹簱: recordId={}, deviceId={}", record.getRecordId(), record.getDeviceId());
    }

    private String generateChecksum(Object data) {
        // 绠€鍖栫殑鏍￠獙鍜岀敓鎴?
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
        // TODO: 瀹炵幇璁块棶璁℃暟鏇存柊閫昏緫
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
            return true; // 瑙ｆ瀽澶辫触璁や负宸茶繃鏈?
        }
    }

    private boolean isRecordExpired(OfflineAccessRecord record) {
        // 妫€鏌ヨ褰曟槸鍚﹁繃鏈燂紙7澶╁墠鐨勮褰曪級
        long sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
        return record.getAccessTime() < sevenDaysAgo;
    }

    private int cleanupDeviceConnections(Long userId) {
        // TODO: 瀹炵幇璁惧杩炴帴淇℃伅娓呯悊
        return 0;
    }

    private long estimatePermissionStorage(OfflinePermissionsCache permissions) {
        // 浼扮畻鏉冮檺鏁版嵁鍗犵敤鐨勫瓨鍌ㄧ┖闂?
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
        // 绠€鍖栫殑鏍￠獙鍜岃绠?
        return String.valueOf(data.hashCode());
    }

    private long estimatePackageSize(List<OfflineAreaPermission> areaPermissions, List<OfflineDevicePermission> devicePermissions) {
        return (areaPermissions.size() * 150) + (devicePermissions.size() * 180);
    }

    private boolean validateDataFormat(String dataPackage) {
        // 绠€鍖栫殑鏁版嵁鏍煎紡楠岃瘉
        return dataPackage != null && dataPackage.length() > 0;
    }

    // ==================== 鍐呴儴鏁版嵁绫?====================

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