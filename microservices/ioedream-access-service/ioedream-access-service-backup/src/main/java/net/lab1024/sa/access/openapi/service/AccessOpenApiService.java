package net.lab1024.sa.access.openapi.service;

import net.lab1024.sa.access.openapi.domain.request.*;
import net.lab1024.sa.access.openapi.domain.response.*;

import java.util.List;

/**
 * 闂ㄧ绠＄悊寮€鏀続PI鏈嶅姟鎺ュ彛
 * 鎻愪緵闂ㄧ鎺у埗銆侀€氳璁板綍鏌ヨ銆侀棬绂佽澶囩鐞嗙瓑寮€鏀炬湇鍔?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AccessOpenApiService {

    /**
     * 闂ㄧ閫氳楠岃瘉
     *
     * @param request 楠岃瘉璇锋眰
     * @param token 璁块棶浠ょ墝
     * @param clientIp 瀹㈡埛绔疘P
     * @return 楠岃瘉鍝嶅簲
     */
    AccessVerifyResponse verifyAccess(AccessVerifyRequest request, String token, String clientIp);

    /**
     * 杩滅▼鎺у埗闂ㄧ璁惧
     *
     * @param deviceId 璁惧ID
     * @param action 鎿嶄綔绫诲瀷
     * @param reason 鎿嶄綔鍘熷洜
     * @param token 璁块棶浠ょ墝
     * @param clientIp 瀹㈡埛绔疘P
     * @return 鎺у埗鍝嶅簲
     */
    AccessControlResponse remoteControl(String deviceId, String action, String reason, String token, String clientIp);

    /**
     * 鑾峰彇閫氳璁板綍鍒楄〃
     *
     * @param request 鏌ヨ璇锋眰
     * @param token 璁块棶浠ょ墝
     * @return 鍒嗛〉閫氳璁板綍
     */
    net.lab1024.sa.common.openapi.domain.response.PageResult<AccessRecordResponse> getAccessRecords(
            AccessRecordQueryRequest request, String token);

    /**
     * 鑾峰彇閫氳璁板綍璇︽儏
     *
     * @param recordId 璁板綍ID
     * @param token 璁块棶浠ょ墝
     * @return 閫氳璁板綍璇︽儏
     */
    AccessRecordDetailResponse getAccessRecordDetail(Long recordId, String token);

    /**
     * 鑾峰彇闂ㄧ璁惧鍒楄〃
     *
     * @param areaId 鍖哄煙ID
     * @param deviceStatus 璁惧鐘舵€?
     * @param token 璁块棶浠ょ墝
     * @return 璁惧鍒楄〃
     */
    List<AccessDeviceResponse> getAccessDevices(Long areaId, Integer deviceStatus, String token);

    /**
     * 鑾峰彇闂ㄧ璁惧璇︽儏
     *
     * @param deviceId 璁惧ID
     * @param token 璁块棶浠ょ墝
     * @return 璁惧璇︽儏
     */
    AccessDeviceDetailResponse getAccessDeviceDetail(String deviceId, String token);

    /**
     * 鎺у埗闂ㄧ璁惧
     *
     * @param deviceId 璁惧ID
     * @param action 鎿嶄綔绫诲瀷
     * @param parameters 鎿嶄綔鍙傛暟
     * @param token 璁块棶浠ょ墝
     * @param clientIp 瀹㈡埛绔疘P
     * @return 鎺у埗鍝嶅簲
     */
    AccessControlResponse controlDevice(String deviceId, String action, String parameters, String token, String clientIp);

    /**
     * 鑾峰彇鐢ㄦ埛闂ㄧ鏉冮檺
     *
     * @param userId 鐢ㄦ埛ID
     * @param token 璁块棶浠ょ墝
     * @return 鐢ㄦ埛闂ㄧ鏉冮檺
     */
    UserAccessPermissionResponse getUserAccessPermissions(Long userId, String token);

    /**
     * 鎺堜簣鐢ㄦ埛闂ㄧ鏉冮檺
     *
     * @param request 鎺堟潈璇锋眰
     * @param token 璁块棶浠ょ墝
     * @param clientIp 瀹㈡埛绔疘P
     */
    void grantAccessPermission(GrantAccessPermissionRequest request, String token, String clientIp);

    /**
     * 鎾ら攢鐢ㄦ埛闂ㄧ鏉冮檺
     *
     * @param request 鎾ら攢璇锋眰
     * @param token 璁块棶浠ょ墝
     * @param clientIp 瀹㈡埛绔疘P
     */
    void revokeAccessPermission(RevokeAccessPermissionRequest request, String token, String clientIp);

    /**
     * 鑾峰彇瀹炴椂閫氳鐘舵€?
     *
     * @param areaId 鍖哄煙ID
     * @param token 璁块棶浠ょ墝
     * @return 瀹炴椂鐘舵€?
     */
    AccessRealtimeStatusResponse getRealtimeAccessStatus(Long areaId, String token);

    /**
     * 鑾峰彇闂ㄧ缁熻淇℃伅
     *
     * @param statisticsType 缁熻绫诲瀷
     * @param startDate 寮€濮嬫棩鏈?
     * @param endDate 缁撴潫鏃ユ湡
     * @param areaId 鍖哄煙ID
     * @param token 璁块棶浠ょ墝
     * @return 缁熻淇℃伅
     */
    AccessStatisticsResponse getAccessStatistics(String statisticsType, String startDate, String endDate, Long areaId, String token);
}