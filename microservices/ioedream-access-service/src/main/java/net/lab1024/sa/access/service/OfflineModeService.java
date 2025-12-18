package net.lab1024.sa.access.service;

import java.util.List;

import net.lab1024.sa.access.controller.AccessMobileController.*;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 绂荤嚎妯″紡鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵绂荤嚎妯″紡涓嬬殑闂ㄧ绠＄悊鍔熻兘锛?
 * - 绂荤嚎鏉冮檺鏁版嵁鍚屾
 * - 绂荤嚎璁块棶璁板綍绠＄悊
 * - 鏁版嵁瀹屾暣鎬ч獙璇?
 * - 绂荤嚎妯″紡鐘舵€佺洃鎺?
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - Service鎺ュ彛瀹氫箟鍦ㄤ笟鍔℃湇鍔℃ā鍧椾腑
 * - 娓呮櫚鐨勬柟娉曟敞閲?
 * - 缁熶竴鐨勬暟鎹紶杈撳璞?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface OfflineModeService {

    /**
     * 鍚屾绂荤嚎鏁版嵁
     * <p>
     * 鍚屾绂荤嚎妯″紡鎵€闇€鐨勯棬绂佹暟鎹細
     * - 鐢ㄦ埛鏉冮檺鏁版嵁
     * - 璁惧淇℃伅鏁版嵁
     * - 閰嶇疆鍙傛暟鏁版嵁
     * - 鏀寔澧為噺鍚屾
     * </p>
     *
     * @param request 鍚屾璇锋眰
     * @return 鍚屾缁撴灉
     */
    ResponseDTO<OfflineSyncResult> syncOfflineData(OfflineSyncRequest request);

    /**
     * 鑾峰彇绂荤嚎璁块棶鏉冮檺
     * <p>
     * 鑾峰彇鐢ㄦ埛鍦ㄧ绾挎ā寮忎笅鐨勮闂潈闄愶細
     * - 鍖哄煙璁块棶鏉冮檺
     * - 璁惧璁块棶鏉冮檺
     * - 鏃堕棿鑼冨洿闄愬埗
     * - 璁块棶鏂瑰紡闄愬埗
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param lastSyncTime 涓婃鍚屾鏃堕棿
     * @return 绂荤嚎鏉冮檺鏁版嵁
     */
    ResponseDTO<OfflinePermissionsVO> getOfflinePermissions(Long userId, String lastSyncTime);

    /**
     * 涓婃姤绂荤嚎璁块棶璁板綍
     * <p>
     * 澶勭悊绂荤嚎妯″紡涓嬩骇鐢熺殑璁块棶璁板綍锛?
     * - 璁板綍楠岃瘉鍜屾牎楠?
     * - 鏁版嵁瀹屾暣鎬ф鏌?
     * - 寮傚父璁板綍鏍囪
     * - 鎵归噺鏁版嵁澶勭悊
     * </p>
     *
     * @param request 涓婃姤璇锋眰
     * @return 涓婃姤缁撴灉
     */
    ResponseDTO<OfflineReportResult> reportOfflineRecords(OfflineRecordsReportRequest request);

    /**
     * 楠岃瘉绂荤嚎璁块棶鏉冮檺
     * <p>
     * 鍦ㄧ绾挎ā寮忎笅楠岃瘉鐢ㄦ埛璁块棶鏉冮檺锛?
     * - 鏈湴鏉冮檺鏁版嵁楠岃瘉
     * - 鏃堕棿鏈夋晥鎬ф鏌?
     * - 璁块棶娆℃暟闄愬埗
     * - 瀹夊叏瑙勫垯楠岃瘉
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param accessType 璁块棶绫诲瀷
     * @param verificationData 楠岃瘉鏁版嵁
     * @return 楠岃瘉缁撴灉
     */
    ResponseDTO<OfflineAccessValidationResult> validateOfflineAccess(
            Long userId, Long deviceId, String accessType, String verificationData);

    /**
     * 鑾峰彇绂荤嚎妯″紡鐘舵€?
     * <p>
     * 鏌ヨ绂荤嚎妯″紡鐨勮繍琛岀姸鎬侊細
     * - 鏁版嵁鍚屾鐘舵€?
     * - 璁惧绂荤嚎鐘舵€?
     * - 缃戠粶杩炴帴鐘舵€?
     * - 寮傚父浜嬩欢缁熻
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 绂荤嚎妯″紡鐘舵€?
     */
    ResponseDTO<OfflineModeStatusVO> getOfflineModeStatus(Long userId);

    /**
     * 娓呯悊杩囨湡绂荤嚎鏁版嵁
     * <p>
     * 娓呯悊杩囨湡鐨勭绾挎暟鎹細
     * - 杩囨湡鏉冮檺鏁版嵁
     * - 鍘嗗彶璁块棶璁板綍
     * - 涓存椂缂撳瓨鏁版嵁
     * - 鏃犳晥璁惧淇℃伅
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 娓呯悊缁撴灉
     */
    ResponseDTO<OfflineDataCleanupResult> cleanupExpiredOfflineData(Long userId);

    /**
     * 鐢熸垚绂荤嚎鏁版嵁鍖?
     * <p>
     * 涓烘寚瀹氱敤鎴风敓鎴愬畬鏁寸殑绂荤嚎鏁版嵁鍖咃細
     * - 鏉冮檺鏁版嵁鎵撳寘
     * - 璁惧淇℃伅鎵撳寘
     * - 閰嶇疆鍙傛暟鎵撳寘
     * - 鍔犲瘑鍜屽帇缂?
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceIds 璁惧ID鍒楄〃
     * @return 绂荤嚎鏁版嵁鍖?
     */
    ResponseDTO<OfflineDataPackageVO> generateOfflineDataPackage(Long userId, List<Long> deviceIds);

    /**
     * 楠岃瘉绂荤嚎鏁版嵁瀹屾暣鎬?
     * <p>
     * 楠岃瘉绂荤嚎鏁版嵁鐨勫畬鏁存€у拰鏈夋晥鎬э細
     * - 鏁版嵁鏍￠獙鍜岄獙璇?
     * - 鏁板瓧绛惧悕楠岃瘉
     * - 鏁版嵁鏍煎紡楠岃瘉
     * - 鏃舵晥鎬ч獙璇?
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param dataPackage 鏁版嵁鍖?
     * @return 楠岃瘉缁撴灉
     */
    ResponseDTO<OfflineDataIntegrityResult> validateOfflineDataIntegrity(Long userId, String dataPackage);

    /**
     * 绂荤嚎妯″紡缁熻鎶ュ憡
     * <p>
     * 鐢熸垚绂荤嚎妯″紡鐨勭粺璁℃姤鍛婏細
     * - 璁块棶娆℃暟缁熻
     * - 鎴愬姛鐜囩粺璁?
     * - 寮傚父浜嬩欢缁熻
     * - 鎬ц兘鎸囨爣缁熻
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param startTime 寮€濮嬫椂闂?
     * @param endTime 缁撴潫鏃堕棿
     * @return 缁熻鎶ュ憡
     */
    ResponseDTO<OfflineModeStatisticsVO> generateOfflineStatisticsReport(
            Long userId, String startTime, String endTime);

    // ==================== 鍐呴儴鏁版嵁浼犺緭瀵硅薄 ====================

    /**
     * 绂荤嚎璁块棶楠岃瘉缁撴灉
     */
    class OfflineAccessValidationResult {
        private Boolean allowed;              // 鏄惁鍏佽璁块棶
        private String validationReason;     // 楠岃瘉鍘熷洜
        private String permissionLevel;      // 鏉冮檺绛夌骇
        private Long remainingQuota;         // 鍓╀綑閰嶉
        private String validUntil;           // 鏉冮檺鏈夋晥鏈?
        private String validationMode;       // 楠岃瘉妯″紡
        private List<String> warnings;       // 璀﹀憡淇℃伅
    }

    /**
     * 绂荤嚎妯″紡鐘舵€?
     */
    class OfflineModeStatusVO {
        private Long userId;
        private Boolean isOfflineMode;       // 鏄惁绂荤嚎妯″紡
        private String lastSyncTime;         // 鏈€鍚庡悓姝ユ椂闂?
        private Integer syncedDevices;       // 宸插悓姝ヨ澶囨暟
        private Integer pendingRecords;      // 寰呬笂鎶ヨ褰曟暟
        private Integer failedRecords;       // 澶辫触璁板綍鏁?
        private String networkStatus;        // 缃戠粶鐘舵€?
        private String storageUsage;         // 瀛樺偍浣跨敤鎯呭喌
        private List<String> activeDevices;  // 娲昏穬璁惧鍒楄〃
    }

    /**
     * 绂荤嚎鏁版嵁娓呯悊缁撴灉
     */
    class OfflineDataCleanupResult {
        private Boolean success;
        private Long userId;
        private Integer cleanedPermissions;  // 娓呯悊鐨勬潈闄愭暟閲?
        private Integer cleanedRecords;      // 娓呯悊鐨勮褰曟暟閲?
        private Integer cleanedDevices;      // 娓呯悊鐨勮澶囨暟閲?
        private Long freedStorage;           // 閲婃斁鐨勫瓨鍌ㄧ┖闂达紙瀛楄妭锛?
        private Long cleanupDuration;        // 娓呯悊鑰楁椂锛堟绉掞級
        private List<String> errors;         // 閿欒淇℃伅
    }

    /**
     * 绂荤嚎鏁版嵁鍖?
     */
    class OfflineDataPackageVO {
        private String packageId;            // 鏁版嵁鍖匢D
        private Long userId;
        private String packageVersion;      // 鏁版嵁鍖呯増鏈?
        private Long packageSize;            // 鏁版嵁鍖呭ぇ灏忥紙瀛楄妭锛?
        private String checksum;             // 鏍￠獙鍜?
        private String encryptionMethod;     // 鍔犲瘑鏂瑰紡
        private String compressionMethod;   // 鍘嬬缉鏂瑰紡
        private String generatedTime;        // 鐢熸垚鏃堕棿
        private String expiryTime;           // 杩囨湡鏃堕棿
        private Integer deviceCount;         // 璁惧鏁伴噺
        private Integer permissionCount;     // 鏉冮檺鏁伴噺
    }

    /**
     * 绂荤嚎鏁版嵁瀹屾暣鎬ч獙璇佺粨鏋?
     */
    class OfflineDataIntegrityResult {
        private Boolean valid;               // 鏁版嵁鏄惁鏈夋晥
        private String packageId;            // 鏁版嵁鍖匢D
        private Boolean checksumValid;       // 鏍￠獙鍜屾槸鍚︽湁鏁?
        private Boolean signatureValid;      // 绛惧悕鏄惁鏈夋晥
        private Boolean formatValid;         // 鏍煎紡鏄惁鏈夋晥
        private Boolean expired;             // 鏄惁杩囨湡
        private List<String> validationErrors; // 楠岃瘉閿欒
        private String validationTime;       // 楠岃瘉鏃堕棿
    }

    /**
     * 绂荤嚎妯″紡缁熻鎶ュ憡
     */
    class OfflineModeStatisticsVO {
        private Long userId;
        private String reportPeriod;         // 缁熻鍛ㄦ湡
        private Long totalAccessAttempts;    // 鎬昏闂皾璇曟鏁?
        private Long successfulAccesses;     // 鎴愬姛璁块棶娆℃暟
        private Long failedAccesses;         // 澶辫触璁块棶娆℃暟
        private Double successRate;          // 鎴愬姛鐜?
        private Long offlineTransactions;    // 绂荤嚎浜ゆ槗鏁?
        private Long onlineTransactions;     // 鍦ㄧ嚎浜ゆ槗鏁?
        private Integer averageResponseTime; // 骞冲潎鍝嶅簲鏃堕棿锛堟绉掞級
        private Integer maxResponseTime;     // 鏈€澶у搷搴旀椂闂达紙姣锛?
        private Integer minResponseTime;     // 鏈€灏忓搷搴旀椂闂达紙姣锛?
        private List<DeviceStatistics> deviceStats; // 璁惧缁熻
    }

    /**
     * 璁惧缁熻淇℃伅
     */
    class DeviceStatistics {
        private Long deviceId;
        private String deviceName;
        private Long accessCount;            // 璁块棶娆℃暟
        private Long successCount;           // 鎴愬姛娆℃暟
        private Long failureCount;           // 澶辫触娆℃暟
        private Double successRate;          // 鎴愬姛鐜?
        private Integer averageResponseTime; // 骞冲潎鍝嶅簲鏃堕棿
        private String lastAccessTime;       // 鏈€鍚庤闂椂闂?
        private String status;               // 璁惧鐘舵€?
    }
}