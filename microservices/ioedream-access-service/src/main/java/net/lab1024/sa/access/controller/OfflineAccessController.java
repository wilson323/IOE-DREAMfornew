package net.lab1024.sa.access.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.service.OfflineAccessService;
import net.lab1024.sa.access.domain.vo.OfflineAccessResultVO;

/**
 * 绂荤嚎闂ㄧ鎺у埗鍣?
 * <p>
 * 涓撻棬澶勭悊绂荤嚎闂ㄧ鍔熻兘锛屽寘鎷細
 * - 绂荤嚎韬唤楠岃瘉鍜屾潈闄愭牎楠?
 * - 鏈湴鐢熺墿璇嗗埆楠岃瘉鍜屾椿浣撴娴?
 * - 绂荤嚎閫氳璁板綍缂撳瓨鍜屽悓姝?
 * - 搴旀€ラ棬绂佺瓥鐣ュ拰瀹夊叏鐩戞帶
 * - 缃戠粶鐘舵€佹劅鐭ュ拰鑷姩鍒囨崲
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/offline")
@Tag(name = "绂荤嚎闂ㄧ", description = "绂荤嚎闂ㄧ绠＄悊")
@PermissionCheck(value = "ACCESS", description = "绂荤嚎闂ㄧ绠＄悊")
public class OfflineAccessController {

    @Resource
    private OfflineAccessService offlineAccessService;

    // ==================== 绂荤嚎韬唤楠岃瘉鏍稿績鎺ュ彛 ====================

    /**
     * 鎵ц绂荤嚎闂ㄧ楠岃瘉
     * <p>
     * 鍦ㄧ綉缁滀腑鏂垨璁惧绂荤嚎鐘舵€佷笅鎵ц闂ㄧ楠岃瘉
     * </p>
     *
     * @param verificationRequest 楠岃瘉璇锋眰鏁版嵁
     * @return 绂荤嚎楠岃瘉缁撴灉Future
     */
    @Observed(name = "offline.access.verification", contextualName = "offline-access-verification")
    @PostMapping("/verify")
    @Operation(
            summary = "鎵ц绂荤嚎闂ㄧ楠岃瘉",
            description = "鍦ㄧ綉缁滀腑鏂垨璁惧绂荤嚎鐘舵€佷笅鎵ц闂ㄧ楠岃瘉"
    )
    @PermissionCheck(value = "ACCESS_USER", description = "鎵ц绂荤嚎闂ㄧ楠岃瘉")
    public ResponseEntity<ResponseDTO<OfflineAccessResultVO>> performOfflineAccessVerification(
            @Valid @RequestBody Map<String, Object> verificationRequest) {

        log.info("[绂荤嚎闂ㄧ] 鎵ц绂荤嚎闂ㄧ楠岃瘉锛宒eviceId={}, userId={}",
                verificationRequest.get("deviceId"), verificationRequest.get("userId"));

        try {
            Future<OfflineAccessResultVO> result = offlineAccessService.performOfflineAccessVerification(verificationRequest);

            log.info("[绂荤嚎闂ㄧ] 绂荤嚎闂ㄧ楠岃瘉浠诲姟鎻愪氦鎴愬姛锛宒eviceId={}",
                    verificationRequest.get("deviceId"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 绂荤嚎闂ㄧ楠岃瘉寮傚父锛宔rror={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("OFFLINE_VERIFICATION_ERROR", "绂荤嚎闂ㄧ楠岃瘉寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 绂荤嚎鐢熺墿鐗瑰緛楠岃瘉
     * <p>
     * 涓撻棬澶勭悊绂荤嚎鐜涓嬬殑鐢熺墿鐗瑰緛楠岃瘉
     * </p>
     *
     * @param verificationRequest 楠岃瘉璇锋眰锛堝寘鍚敓鐗╃壒寰佹暟鎹級
     * @return 鐢熺墿鐗瑰緛楠岃瘉缁撴灉
     */
    @Observed(name = "offline.access.biometric", contextualName = "offline-access-biometric")
    @PostMapping("/biometric/verify")
    @Operation(
            summary = "绂荤嚎鐢熺墿鐗瑰緛楠岃瘉",
            description = "涓撻棬澶勭悊绂荤嚎鐜涓嬬殑鐢熺墿鐗瑰緛楠岃瘉"
    )
    @PermissionCheck(value = "ACCESS_USER", description = "鎵ц绂荤嚎闂ㄧ楠岃瘉")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> performOfflineBiometricVerification(
            @Valid @RequestBody Map<String, Object> verificationRequest) {

        log.info("[绂荤嚎闂ㄧ] 鎵ц绂荤嚎鐢熺墿鐗瑰緛楠岃瘉锛宒eviceId={}, biometricType={}",
                verificationRequest.get("deviceId"), verificationRequest.get("biometricType"));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> biometricData = (Map<String, Object>) verificationRequest.get("biometricData");
            @SuppressWarnings("unchecked")
            Map<String, Object> deviceInfo = (Map<String, Object>) verificationRequest.get("deviceInfo");

            Map<String, Object> result = offlineAccessService.performOfflineBiometricVerification(
                    biometricData, deviceInfo);

            log.info("[绂荤嚎闂ㄧ] 绂荤嚎鐢熺墿鐗瑰緛楠岃瘉瀹屾垚锛宒eviceId={}, verified={}",
                    verificationRequest.get("deviceId"), result.get("verified"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 绂荤嚎鐢熺墿鐗瑰緛楠岃瘉寮傚父锛宔rror={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("OFFLINE_BIOMETRIC_ERROR", "绂荤嚎鐢熺墿鐗瑰緛楠岃瘉寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 澶氬洜绱犵绾胯璇?
     * <p>
     * 鎵ц澶氬洜绱犵绾胯韩浠借璇?
     * </p>
     *
     * @param authRequest 璁よ瘉璇锋眰锛堝寘鍚涓璇佸洜瀛愶級
     * @return 澶氬洜绱犺璇佺粨鏋?
     */
    @Observed(name = "offline.access.multiFactor", contextualName = "offline-access-multi-factor")
    @PostMapping("/multi-factor/verify")
    @Operation(
            summary = "澶氬洜绱犵绾胯璇?,
            description = "鎵ц澶氬洜绱犵绾胯韩浠借璇?
    )
    @PermissionCheck(value = "ACCESS_USER", description = "鎵ц绂荤嚎闂ㄧ楠岃瘉")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> performMultiFactorOfflineAuth(
            @Valid @RequestBody Map<String, Object> authRequest) {

        log.info("[绂荤嚎闂ㄧ] 鎵ц澶氬洜绱犵绾胯璇侊紝deviceId={}, factorCount={}",
                authRequest.get("deviceId"), authRequest.get("factorCount"));

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> authFactors = (List<Map<String, Object>>) authRequest.get("authFactors");
            String accessLevel = (String) authRequest.getOrDefault("accessLevel", "NORMAL");

            Map<String, Object> result = offlineAccessService.performMultiFactorOfflineAuth(authFactors, accessLevel);

            log.info("[绂荤嚎闂ㄧ] 澶氬洜绱犵绾胯璇佸畬鎴愶紝deviceId={}, authenticated={}",
                    authRequest.get("deviceId"), result.get("authenticated"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 澶氬洜绱犵绾胯璇佸紓甯革紝error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("OFFLINE_MULTI_FACTOR_ERROR", "澶氬洜绱犵绾胯璇佸紓甯革細" + e.getMessage()));
        }
    }

    /**
     * 绂荤嚎鏉冮檺瀹炴椂妫€鏌?
     * <p>
     * 妫€鏌ョ敤鎴风殑绂荤嚎璁块棶鏉冮檺
     * </p>
     *
     * @param permissionRequest 鏉冮檺妫€鏌ヨ姹?
     * @return 鏉冮檺妫€鏌ョ粨鏋?
     */
    @Observed(name = "offline.access.checkPermission", contextualName = "offline-access-check-permission")
    @PostMapping("/check-permission")
    @Operation(
            summary = "绂荤嚎鏉冮檺瀹炴椂妫€鏌?,
            description = "妫€鏌ョ敤鎴风殑绂荤嚎璁块棶鏉冮檺"
    )
    @PermissionCheck(value = "ACCESS_USER", description = "鎵ц绂荤嚎闂ㄧ楠岃瘉")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> checkOfflineAccessPermissions(
            @Valid @RequestBody Map<String, Object> permissionRequest) {

        log.info("[绂荤嚎闂ㄧ] 妫€鏌ョ绾胯闂潈闄愶紝userId={}, deviceId={}",
                permissionRequest.get("userId"), permissionRequest.get("deviceId"));

        try {
            Long userId = Long.valueOf(permissionRequest.get("userId").toString());
            String deviceId = (String) permissionRequest.get("deviceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> accessPoint = (Map<String, Object>) permissionRequest.get("accessPoint");

            Map<String, Object> result = offlineAccessService.checkOfflineAccessPermissions(userId, deviceId, accessPoint);

            log.info("[绂荤嚎闂ㄧ] 绂荤嚎鏉冮檺妫€鏌ュ畬鎴愶紝userId={}, deviceId={}, hasPermission={}",
                    userId, deviceId, result.get("hasPermission"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 绂荤嚎鏉冮檺妫€鏌ュ紓甯革紝error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("OFFLINE_PERMISSION_CHECK_ERROR", "绂荤嚎鏉冮檺妫€鏌ュ紓甯革細" + e.getMessage()));
        }
    }

    // ==================== 绂荤嚎鏁版嵁绠＄悊鎺ュ彛 ====================

    /**
     * 鍑嗗绂荤嚎璁块棶鏉冮檺鏁版嵁
     * <p>
     * 涓烘寚瀹氳澶囧噯澶囩绾胯闂墍闇€鐨勬潈闄愭暟鎹?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param userIds 鐢ㄦ埛ID鍒楄〃锛堝彲閫夛級
     * @return 绂荤嚎鏉冮檺鏁版嵁鍖?
     */
    @Observed(name = "offline.access.prepareData", contextualName = "offline-access-prepare-data")
    @PostMapping("/device/{deviceId}/prepare-data")
    @Operation(
            summary = "鍑嗗绂荤嚎璁块棶鏉冮檺鏁版嵁",
            description = "涓烘寚瀹氳澶囧噯澶囩绾胯闂墍闇€鐨勬潈闄愭暟鎹?,
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> prepareOfflineAccessData(
            @PathVariable String deviceId,
            @RequestBody(required = false) List<Long> userIds) {

        log.info("[绂荤嚎闂ㄧ] 鍑嗗绂荤嚎璁块棶鏉冮檺鏁版嵁锛宒eviceId={}, userCount={}",
                deviceId, userIds != null ? userIds.size() : 0);

        try {
            Map<String, Object> accessData = offlineAccessService.prepareOfflineAccessData(deviceId, userIds);

            log.info("[绂荤嚎闂ㄧ] 绂荤嚎璁块棶鏉冮檺鏁版嵁鍑嗗瀹屾垚锛宒eviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(accessData));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 鍑嗗绂荤嚎璁块棶鏉冮檺鏁版嵁寮傚父锛宒eviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("PREPARE_OFFLINE_DATA_ERROR", "鍑嗗绂荤嚎璁块棶鏉冮檺鏁版嵁寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 鍚屾绂荤嚎璁块棶鏁版嵁鍒拌澶?
     * <p>
     * 灏嗚闂潈闄愭暟鎹悓姝ュ埌闂ㄧ璁惧
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param syncRequest 鍚屾璇锋眰
     * @return 鍚屾缁撴灉Future
     */
    @Observed(name = "offline.access.syncData", contextualName = "offline-access-sync-data")
    @PostMapping("/device/{deviceId}/sync-data")
    @Operation(
            summary = "鍚屾绂荤嚎璁块棶鏁版嵁鍒拌澶?,
            description = "灏嗚闂潈闄愭暟鎹悓姝ュ埌闂ㄧ璁惧",
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> syncOfflineAccessDataToDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody Map<String, Object> syncRequest) {

        log.info("[绂荤嚎闂ㄧ] 鍚屾绂荤嚎璁块棶鏁版嵁鍒拌澶囷紝deviceId={}", deviceId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> accessData = (Map<String, Object>) syncRequest.get("accessData");

            Future<Map<String, Object>> result = offlineAccessService.syncOfflineAccessDataToDevice(deviceId, accessData);

            log.info("[绂荤嚎闂ㄧ] 绂荤嚎璁块棶鏁版嵁鍚屾浠诲姟鎻愪氦鎴愬姛锛宒eviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 鍚屾绂荤嚎璁块棶鏁版嵁寮傚父锛宒eviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("SYNC_OFFLINE_DATA_ERROR", "鍚屾绂荤嚎璁块棶鏁版嵁寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 楠岃瘉绂荤嚎璁块棶鏁版嵁瀹屾暣鎬?
     * <p>
     * 楠岃瘉璁惧涓婄殑绂荤嚎璁块棶鏁版嵁瀹屾暣鎬?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 鏁版嵁瀹屾暣鎬ч獙璇佺粨鏋?
     */
    @Observed(name = "offline.access.validateDataIntegrity", contextualName = "offline-access-validate-data-integrity")
    @GetMapping("/device/{deviceId}/validate-data-integrity")
    @Operation(
            summary = "楠岃瘉绂荤嚎璁块棶鏁版嵁瀹屾暣鎬?,
            description = "楠岃瘉璁惧涓婄殑绂荤嚎璁块棶鏁版嵁瀹屾暣鎬?,
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> validateOfflineAccessDataIntegrity(@PathVariable String deviceId) {
        log.info("[绂荤嚎闂ㄧ] 楠岃瘉绂荤嚎璁块棶鏁版嵁瀹屾暣鎬э紝deviceId={}", deviceId);

        try {
            Map<String, Object> validation = offlineAccessService.validateOfflineAccessDataIntegrity(deviceId);

            log.info("[绂荤嚎闂ㄧ] 绂荤嚎璁块棶鏁版嵁瀹屾暣鎬ч獙璇佸畬鎴愶紝deviceId={}, integrity={}",
                    deviceId, validation.get("integrity"));

            return ResponseEntity.ok(ResponseDTO.ok(validation));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 楠岃瘉绂荤嚎璁块棶鏁版嵁瀹屾暣鎬у紓甯革紝deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("VALIDATE_DATA_INTEGRITY_ERROR", "楠岃瘉绂荤嚎璁块棶鏁版嵁瀹屾暣鎬у紓甯革細" + e.getMessage()));
        }
    }

    // ==================== 绂荤嚎璁板綍绠＄悊鎺ュ彛 ====================

    /**
     * 缂撳瓨绂荤嚎閫氳璁板綍
     * <p>
     * 鍦ㄨ澶囩绾跨姸鎬佷笅缂撳瓨閫氳璁板綍
     * </p>
     *
     * @param recordRequest 璁板綍缂撳瓨璇锋眰
     * @return 缂撳瓨缁撴灉
     */
    @Observed(name = "offline.access.cacheRecord", contextualName = "offline-access-cache-record")
    @PostMapping("/cache-record")
    @Operation(
            summary = "缂撳瓨绂荤嚎閫氳璁板綍",
            description = "鍦ㄨ澶囩绾跨姸鎬佷笅缂撳瓨閫氳璁板綍"
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<Boolean>> cacheOfflineAccessRecord(
            @Valid @RequestBody Map<String, Object> recordRequest) {

        log.info("[绂荤嚎闂ㄧ] 缂撳瓨绂荤嚎閫氳璁板綍锛宒eviceId={}, recordId={}",
                recordRequest.get("deviceId"), recordRequest.get("recordId"));

        try {
            String deviceId = (String) recordRequest.get("deviceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> accessRecord = (Map<String, Object>) recordRequest.get("accessRecord");

            boolean success = offlineAccessService.cacheOfflineAccessRecord(deviceId, accessRecord);

            log.info("[绂荤嚎闂ㄧ] 绂荤嚎閫氳璁板綍缂撳瓨瀹屾垚锛宒eviceId={}, success={}", deviceId, success);

            return ResponseEntity.ok(ResponseDTO.ok(success));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 缂撳瓨绂荤嚎閫氳璁板綍寮傚父锛宔rror={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("CACHE_OFFLINE_RECORD_ERROR", "缂撳瓨绂荤嚎閫氳璁板綍寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 鎵归噺涓婁紶绂荤嚎閫氳璁板綍
     * <p>
     * 灏嗙紦瀛樼殑绂荤嚎璁板綍鎵归噺涓婁紶鍒颁簯绔?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param offlineRecords 绂荤嚎璁板綍鍒楄〃
     * @return 涓婁紶缁撴灉Future
     */
    @Observed(name = "offline.access.uploadRecords", contextualName = "offline-access-upload-records")
    @PostMapping("/device/{deviceId}/upload-records")
    @Operation(
            summary = "鎵归噺涓婁紶绂荤嚎閫氳璁板綍",
            description = "灏嗙紦瀛樼殑绂荤嚎璁板綍鎵归噺涓婁紶鍒颁簯绔?,
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<OfflineAccessResultVO>> batchUploadOfflineAccessRecords(
            @PathVariable String deviceId,
            @Valid @RequestBody List<Map<String, Object>> offlineRecords) {

        log.info("[绂荤嚎闂ㄧ] 鎵归噺涓婁紶绂荤嚎閫氳璁板綍锛宒eviceId={}, recordCount={}", deviceId, offlineRecords.size());

        try {
            Future<OfflineAccessResultVO> result = offlineAccessService.batchUploadOfflineAccessRecords(deviceId, offlineRecords);

            log.info("[绂荤嚎闂ㄧ] 绂荤嚎閫氳璁板綍涓婁紶浠诲姟鎻愪氦鎴愬姛锛宒eviceId={}, recordCount={}", deviceId, offlineRecords.size());

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 鎵归噺涓婁紶绂荤嚎閫氳璁板綍寮傚父锛宒eviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("UPLOAD_OFFLINE_RECORDS_ERROR", "鎵归噺涓婁紶绂荤嚎閫氳璁板綍寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 鑾峰彇绂荤嚎璁板綍缁熻淇℃伅
     * <p>
     * 鑾峰彇璁惧绂荤嚎璁板綍鐨勭粺璁′俊鎭?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 缁熻淇℃伅Map
     */
    @Observed(name = "offline.access.recordStatistics", contextualName = "offline-access-record-statistics")
    @GetMapping("/device/{deviceId}/record-statistics")
    @Operation(
            summary = "鑾峰彇绂荤嚎璁板綍缁熻淇℃伅",
            description = "鑾峰彇璁惧绂荤嚎璁板綍鐨勭粺璁′俊鎭?,
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getOfflineAccessRecordStatistics(@PathVariable String deviceId) {
        log.info("[绂荤嚎闂ㄧ] 鑾峰彇绂荤嚎璁板綍缁熻淇℃伅锛宒eviceId={}", deviceId);

        try {
            Map<String, Object> statistics = offlineAccessService.getOfflineAccessRecordStatistics(deviceId);

            log.info("[绂荤嚎闂ㄧ] 绂荤嚎璁板綍缁熻淇℃伅鑾峰彇鎴愬姛锛宒eviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(statistics));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 鑾峰彇绂荤嚎璁板綍缁熻淇℃伅寮傚父锛宒eviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("GET_RECORD_STATISTICS_ERROR", "鑾峰彇绂荤嚎璁板綍缁熻淇℃伅寮傚父锛? + e.getMessage()));
        }
    }

    // ==================== 搴旀€ラ棬绂佺瓥鐣ユ帴鍙?====================

    /**
     * 鍚敤搴旀€ラ棬绂佹ā寮?
     * <p>
     * 鍦ㄧ壒娈婃儏鍐典笅鍚敤搴旀€ラ棬绂佺瓥鐣?
     * </p>
     *
     * @param emergencyRequest 搴旀€ユā寮忓惎鐢ㄨ姹?
     * @return 搴旀€ユā寮忓惎鐢ㄧ粨鏋?
     */
    @Observed(name = "offline.access.enableEmergency", contextualName = "offline-access-enable-emergency")
    @PostMapping("/enable-emergency-mode")
    @Operation(
            summary = "鍚敤搴旀€ラ棬绂佹ā寮?,
            description = "鍦ㄧ壒娈婃儏鍐典笅鍚敤搴旀€ラ棬绂佺瓥鐣?
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> enableEmergencyAccessMode(
            @Valid @RequestBody Map<String, Object> emergencyRequest) {

        log.info("[绂荤嚎闂ㄧ] 鍚敤搴旀€ラ棬绂佹ā寮忥紝deviceId={}, emergencyType={}",
                emergencyRequest.get("deviceId"), emergencyRequest.get("emergencyType"));

        try {
            String deviceId = (String) emergencyRequest.get("deviceId");
            String emergencyType = (String) emergencyRequest.get("emergencyType");
            @SuppressWarnings("unchecked")
            List<String> authorizedRoles = (List<String>) emergencyRequest.get("authorizedRoles");

            Map<String, Object> result = offlineAccessService.enableEmergencyAccessMode(deviceId, emergencyType, authorizedRoles);

            log.info("[绂荤嚎闂ㄧ] 搴旀€ラ棬绂佹ā寮忓惎鐢ㄥ畬鎴愶紝deviceId={}, enabled={}",
                    deviceId, result.get("enabled"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 鍚敤搴旀€ラ棬绂佹ā寮忓紓甯革紝error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("ENABLE_EMERGENCY_MODE_ERROR", "鍚敤搴旀€ラ棬绂佹ā寮忓紓甯革細" + e.getMessage()));
        }
    }

    /**
     * 鎵ц搴旀€ユ潈闄愰獙璇?
     * <p>
     * 鍦ㄥ簲鎬ユā寮忎笅鎵ц鐗规畩鐨勬潈闄愰獙璇?
     * </p>
     *
     * @param verificationRequest 搴旀€ラ獙璇佽姹?
     * @return 搴旀€ラ獙璇佺粨鏋?
     */
    @Observed(name = "offline.access.emergencyVerification", contextualName = "offline-access-emergency-verification")
    @PostMapping("/emergency-verify")
    @Operation(
            summary = "鎵ц搴旀€ユ潈闄愰獙璇?,
            description = "鍦ㄥ簲鎬ユā寮忎笅鎵ц鐗规畩鐨勬潈闄愰獙璇?
    )
    @PermissionCheck(value = "ACCESS_USER", description = "鎵ц绂荤嚎闂ㄧ楠岃瘉")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> performEmergencyAccessVerification(
            @Valid @RequestBody Map<String, Object> verificationRequest) {

        log.info("[绂荤嚎闂ㄧ] 鎵ц搴旀€ユ潈闄愰獙璇侊紝deviceId={}, emergencyType={}",
                verificationRequest.get("deviceId"), verificationRequest.get("emergencyType"));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> emergencyContext = (Map<String, Object>) verificationRequest.get("emergencyContext");

            Map<String, Object> result = offlineAccessService.performEmergencyAccessVerification(verificationRequest, emergencyContext);

            log.info("[绂荤嚎闂ㄧ] 搴旀€ユ潈闄愰獙璇佸畬鎴愶紝deviceId={}, verified={}",
                    verificationRequest.get("deviceId"), result.get("verified"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 搴旀€ユ潈闄愰獙璇佸紓甯革紝error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("EMERGENCY_VERIFICATION_ERROR", "搴旀€ユ潈闄愰獙璇佸紓甯革細" + e.getMessage()));
        }
    }

    /**
     * 閫€鍑哄簲鎬ラ棬绂佹ā寮?
     * <p>
     * 浠庡簲鎬ユā寮忔仮澶嶆甯搁棬绂佹搷浣?
     * </p>
     *
     * @param exitRequest 閫€鍑哄簲鎬ユā寮忚姹?
     * @return 閫€鍑哄簲鎬ユā寮忕粨鏋?
     */
    @Observed(name = "offline.access.exitEmergency", contextualName = "offline-access-exit-emergency")
    @PostMapping("/exit-emergency-mode")
    @Operation(
            summary = "閫€鍑哄簲鎬ラ棬绂佹ā寮?,
            description = "浠庡簲鎬ユā寮忔仮澶嶆甯搁棬绂佹搷浣?
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> exitEmergencyAccessMode(
            @Valid @RequestBody Map<String, Object> exitRequest) {

        log.info("[绂荤嚎闂ㄧ] 閫€鍑哄簲鎬ラ棬绂佹ā寮忥紝deviceId={}, reason={}",
                exitRequest.get("deviceId"), exitRequest.get("exitReason"));

        try {
            String deviceId = (String) exitRequest.get("deviceId");
            String exitReason = (String) exitRequest.get("exitReason");

            Map<String, Object> result = offlineAccessService.exitEmergencyAccessMode(deviceId, exitReason);

            log.info("[绂荤嚎闂ㄧ] 搴旀€ラ棬绂佹ā寮忛€€鍑哄畬鎴愶紝deviceId={}, success={}",
                    deviceId, result.get("success"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 閫€鍑哄簲鎬ラ棬绂佹ā寮忓紓甯革紝error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("EXIT_EMERGENCY_MODE_ERROR", "閫€鍑哄簲鎬ラ棬绂佹ā寮忓紓甯革細" + e.getMessage()));
        }
    }

    // ==================== 璁惧绂荤嚎鐘舵€佺洃鎺ф帴鍙?====================

    /**
     * 妫€鏌ヨ澶囩绾跨姸鎬?
     * <p>
     * 妫€鏌ラ棬绂佽澶囩殑绂荤嚎鐘舵€佸拰鑳藉姏
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧绂荤嚎鐘舵€佷俊鎭?
     */
    @Observed(name = "offline.access.deviceStatus", contextualName = "offline-access-device-status")
    @GetMapping("/device/{deviceId}/offline-status")
    @Operation(
            summary = "妫€鏌ヨ澶囩绾跨姸鎬?,
            description = "妫€鏌ラ棬绂佽澶囩殑绂荤嚎鐘舵€佸拰鑳藉姏",
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_USER", description = "鎵ц绂荤嚎闂ㄧ楠岃瘉")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> checkDeviceOfflineStatus(@PathVariable String deviceId) {
        log.info("[绂荤嚎闂ㄧ] 妫€鏌ヨ澶囩绾跨姸鎬侊紝deviceId={}", deviceId);

        try {
            Map<String, Object> status = offlineAccessService.checkDeviceOfflineStatus(deviceId);

            log.info("[绂荤嚎闂ㄧ] 璁惧绂荤嚎鐘舵€佹鏌ュ畬鎴愶紝deviceId={}, offlineMode={}",
                    deviceId, status.get("offlineMode"));

            return ResponseEntity.ok(ResponseDTO.ok(status));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 妫€鏌ヨ澶囩绾跨姸鎬佸紓甯革紝deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("CHECK_DEVICE_STATUS_ERROR", "妫€鏌ヨ澶囩绾跨姸鎬佸紓甯革細" + e.getMessage()));
        }
    }

    /**
     * 棰勬祴璁惧绂荤嚎椋庨櫓
     * <p>
     * 棰勬祴璁惧鍙兘鍑虹幇鐨勭绾块闄?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param riskTimeRange 椋庨櫓棰勬祴鏃堕棿鑼冨洿锛堝皬鏃讹級
     * @return 椋庨櫓璇勪及鎶ュ憡
     */
    @Observed(name = "offline.access.predictRisks", contextualName = "offline-access-predict-risks")
    @GetMapping("/device/{deviceId}/predict-risks")
    @Operation(
            summary = "棰勬祴璁惧绂荤嚎椋庨櫓",
            description = "棰勬祴璁惧鍙兘鍑虹幇鐨勭绾块闄?,
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true),
                    @Parameter(name = "riskTimeRange", description = "椋庨櫓棰勬祴鏃堕棿鑼冨洿锛堝皬鏃讹級")
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> predictDeviceOfflineRisks(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "24") Integer riskTimeRange) {

        log.info("[绂荤嚎闂ㄧ] 棰勬祴璁惧绂荤嚎椋庨櫓锛宒eviceId={}, riskTimeRange={}h", deviceId, riskTimeRange);

        try {
            Map<String, Object> riskReport = offlineAccessService.predictDeviceOfflineRisks(deviceId, riskTimeRange);

            log.info("[绂荤嚎闂ㄧ] 璁惧绂荤嚎椋庨櫓棰勬祴瀹屾垚锛宒eviceId={}, riskLevel={}",
                    deviceId, riskReport.get("riskLevel"));

            return ResponseEntity.ok(ResponseDTO.ok(riskReport));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 棰勬祴璁惧绂荤嚎椋庨櫓寮傚父锛宒eviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("PREDICT_OFFLINE_RISKS_ERROR", "棰勬祴璁惧绂荤嚎椋庨櫓寮傚父锛? + e.getMessage()));
        }
    }

    /**
     * 鑾峰彇缃戠粶鍘嗗彶鐘舵€佸垎鏋?
     * <p>
     * 鍒嗘瀽璁惧鐨勫巻鍙茬綉缁滅姸鎬佹ā寮?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param analysisDays 鍒嗘瀽澶╂暟
     * @return 缃戠粶鐘舵€佸垎鏋愭姤鍛?
     */
    @Observed(name = "offline.access.networkAnalysis", contextualName = "offline-access-network-analysis")
    @GetMapping("/device/{deviceId}/network-analysis")
    @Operation(
            summary = "鑾峰彇缃戠粶鍘嗗彶鐘舵€佸垎鏋?,
            description = "鍒嗘瀽璁惧鐨勫巻鍙茬綉缁滅姸鎬佹ā寮?,
            parameters = {
                    @Parameter(name = "deviceId", description = "璁惧ID", required = true),
                    @Parameter(name = "analysisDays", description = "鍒嗘瀽澶╂暟")
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "绂荤嚎鏁版嵁绠＄悊")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getNetworkHistoryAnalysis(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "7") Integer analysisDays) {

        log.info("[绂荤嚎闂ㄧ] 鑾峰彇缃戠粶鍘嗗彶鐘舵€佸垎鏋愶紝deviceId={}, analysisDays={}d", deviceId, analysisDays);

        try {
            Map<String, Object> analysis = offlineAccessService.getNetworkHistoryAnalysis(deviceId, analysisDays);

            log.info("[绂荤嚎闂ㄧ] 缃戠粶鍘嗗彶鐘舵€佸垎鏋愬畬鎴愶紝deviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(analysis));

        } catch (Exception e) {
            log.error("[绂荤嚎闂ㄧ] 鑾峰彇缃戠粶鍘嗗彶鐘舵€佸垎鏋愬紓甯革紝deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("GET_NETWORK_ANALYSIS_ERROR", "鑾峰彇缃戠粶鍘嗗彶鐘舵€佸垎鏋愬紓甯革細" + e.getMessage()));
        }
    }
}