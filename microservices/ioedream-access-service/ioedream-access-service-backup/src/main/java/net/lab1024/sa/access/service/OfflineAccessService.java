package net.lab1024.sa.access.service;

import java.util.Map;
import java.util.List;
import java.util.concurrent.Future;

import net.lab1024.sa.access.domain.vo.OfflineAccessResultVO;

/**
 * 绂荤嚎闂ㄧ鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵瀹屾暣鐨勭绾块棬绂佸姛鑳斤紝鍖呮嫭锛?
 * - 绂荤嚎韬唤楠岃瘉鍜屾潈闄愭牎楠?
 * - 鏈湴鐢熺墿璇嗗埆楠岃瘉
 * - 绂荤嚎閫氳璁板綍缂撳瓨鍜屽悓姝?
 * - 缃戠粶涓柇鏃剁殑搴旀€ラ棬绂佺瓥鐣?
 * - 璁惧绂荤嚎鐘舵€佺洃鎺у拰绠＄悊
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface OfflineAccessService {

    // ==================== 绂荤嚎韬唤楠岃瘉鏍稿績鎺ュ彛 ====================

    /**
     * 鎵ц绂荤嚎闂ㄧ楠岃瘉
     * <p>
     * 鍦ㄧ綉缁滀腑鏂垨璁惧绂荤嚎鐘舵€佷笅鎵ц闂ㄧ楠岃瘉锛?
     * - 鏈湴鏉冮檺鏁版嵁搴撴煡璇?
     * - 鐢熺墿鐗瑰緛绂荤嚎姣斿
     * - 澶氭ā鎬佽璇佽瀺鍚堥獙璇?
     * - 瀹炴椂椋庨櫓璇勪及鍜屽喅绛?
     * </p>
     *
     * @param verificationRequest 楠岃瘉璇锋眰鏁版嵁
     * @return 绂荤嚎楠岃瘉缁撴灉Future
     */
    Future<OfflineAccessResultVO> performOfflineAccessVerification(Map<String, Object> verificationRequest);

    /**
     * 绂荤嚎鐢熺墿鐗瑰緛楠岃瘉
     * <p>
     * 涓撻棬澶勭悊绂荤嚎鐜涓嬬殑鐢熺墿鐗瑰緛楠岃瘉锛?
     * - 鏈湴浜鸿劯鐗瑰緛搴撴瘮瀵?
     * - 鎸囩汗銆佽櫣鑶滅瓑鐢熺墿璇嗗埆
     * - 娲讳綋妫€娴嬮槻浼獙璇?
     * - 鐗瑰緛妯℃澘绠＄悊
     * </p>
     *
     * @param biometricData 鐢熺墿鐗瑰緛鏁版嵁
     * @param deviceInfo 璁惧淇℃伅
     * @return 鐢熺墿鐗瑰緛楠岃瘉缁撴灉
     */
    Map<String, Object> performOfflineBiometricVerification(Map<String, Object> biometricData,
                                                           Map<String, Object> deviceInfo);

    /**
     * 澶氬洜绱犵绾胯璇?
     * <p>
     * 鎵ц澶氬洜绱犵绾胯韩浠借璇侊細
     * - 鍗＄墖+鐢熺墿鐗瑰緛缁勫悎楠岃瘉
     * - 瀵嗙爜+鐢熺墿鐗瑰緛鍙岄噸楠岃瘉
     * - 鍔ㄦ€佷护鐗岀绾块獙璇?
     * - 璁よ瘉绛夌骇璇勪及
     * </p>
     *
     * @param authFactors 澶氫釜璁よ瘉鍥犲瓙
     * @param accessLevel 瑕佹眰鐨勮闂骇鍒?
     * @return 澶氬洜绱犺璇佺粨鏋?
     */
    Map<String, Object> performMultiFactorOfflineAuth(List<Map<String, Object>> authFactors,
                                                     String accessLevel);

    /**
     * 绂荤嚎鏉冮檺瀹炴椂妫€鏌?
     * <p>
     * 妫€鏌ョ敤鎴风殑绂荤嚎璁块棶鏉冮檺锛?
     * - 鏃堕棿娈垫潈闄愰獙璇?
     * - 鍖哄煙璁块棶鏉冮檺妫€鏌?
     * - 鐗规畩鏉冮檺鍜屼緥澶栧鐞?
     * - 鏉冮檺缂撳瓨鏈夋晥鎬ч獙璇?
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param accessPoint 璁块棶鐐逛俊鎭?
     * @return 鏉冮檺妫€鏌ョ粨鏋?
     */
    Map<String, Object> checkOfflineAccessPermissions(Long userId, String deviceId, Map<String, Object> accessPoint);

    // ==================== 绂荤嚎鏁版嵁绠＄悊鎺ュ彛 ====================

    /**
     * 鍑嗗绂荤嚎璁块棶鏉冮檺鏁版嵁
     * <p>
     * 涓烘寚瀹氳澶囧噯澶囩绾胯闂墍闇€鐨勬潈闄愭暟鎹細
     * - 鐢ㄦ埛鏉冮檺娓呭崟鍜屾湁鏁堟湡
     * - 鐢熺墿鐗瑰緛妯℃澘鏁版嵁
     * - 璁块棶绛栫暐鍜岃鍒?
     * - 搴旀€ヨ闂潈闄愰厤缃?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param userIds 鐢ㄦ埛ID鍒楄〃锛堝彲閫夛紝涓虹┖鍒欏噯澶囨墍鏈夌敤鎴凤級
     * @return 绂荤嚎鏉冮檺鏁版嵁鍖?
     */
    Map<String, Object> prepareOfflineAccessData(String deviceId, List<Long> userIds);

    /**
     * 鍚屾绂荤嚎璁块棶鏁版嵁鍒拌澶?
     * <p>
     * 灏嗚闂潈闄愭暟鎹悓姝ュ埌闂ㄧ璁惧锛?
     * - 澧為噺鏉冮檺鏇存柊
     * - 鐢熺墿鐗瑰緛妯℃澘鍚屾
     * - 璁惧瀹夊叏璁よ瘉
     * - 鍚屾瀹屾暣鎬ф牎楠?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param accessData 璁块棶鏁版嵁鍖?
     * @return 鍚屾缁撴灉Future
     */
    Future<Map<String, Object>> syncOfflineAccessDataToDevice(String deviceId,
                                                            Map<String, Object> accessData);

    /**
     * 楠岃瘉绂荤嚎璁块棶鏁版嵁瀹屾暣鎬?
     * <p>
     * 楠岃瘉璁惧涓婄殑绂荤嚎璁块棶鏁版嵁瀹屾暣鎬э細
     * - 鏁版嵁瀹屾暣鎬ф牎楠?
     * - 鏉冮檺鏁版嵁鏈夋晥鎬ф鏌?
     * - 鐢熺墿鐗瑰緛妯℃澘楠岃瘉
     * - 瀹夊叏瀵嗛挜鏈夋晥鎬ф鏌?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 鏁版嵁瀹屾暣鎬ч獙璇佺粨鏋?
     */
    Map<String, Object> validateOfflineAccessDataIntegrity(String deviceId);

    /**
     * 鏇存柊璁惧绂荤嚎缂撳瓨
     * <p>
     * 鏇存柊璁惧鐨勭绾挎潈闄愮紦瀛樻暟鎹細
     * - 鏉冮檺鍙樻洿澧為噺鏇存柊
     * - 鐢熺墿鐗瑰緛妯℃澘鏇存柊
     * - 璁块棶绛栫暐瑙勫垯鏇存柊
     * - 缂撳瓨鏈夋晥鏈熺鐞?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param updates 鏇存柊鏁版嵁鍒楄〃
     * @return 鏇存柊缁撴灉
     */
    boolean updateDeviceOfflineCache(String deviceId, List<Map<String, Object>> updates);

    // ==================== 绂荤嚎璁板綍绠＄悊鎺ュ彛 ====================

    /**
     * 缂撳瓨绂荤嚎閫氳璁板綍
     * <p>
     * 鍦ㄨ澶囩绾跨姸鎬佷笅缂撳瓨閫氳璁板綍锛?
     * - 涓存椂璁板綍瀛樺偍
     * - 鏁版嵁瀹屾暣鎬т繚鎶?
     * - 瀛樺偍绌洪棿绠＄悊
     * - 璁板綍浼樺厛绾ф帓搴?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param accessRecord 閫氳璁板綍鏁版嵁
     * @return 缂撳瓨缁撴灉
     */
    boolean cacheOfflineAccessRecord(String deviceId, Map<String, Object> accessRecord);

    /**
     * 鎵归噺涓婁紶绂荤嚎閫氳璁板綍
     * <p>
     * 灏嗙紦瀛樼殑绂荤嚎璁板綍鎵归噺涓婁紶鍒颁簯绔細
     * - 璁板綍瀹屾暣鎬ч獙璇?
     * - 閲嶅璁板綍妫€娴?
     * - 鏁版嵁鏍煎紡杞崲
     * - 澶辫触璁板綍閲嶈瘯鏈哄埗
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param offlineRecords 绂荤嚎璁板綍鍒楄〃
     * @return 涓婁紶缁撴灉Future
     */
    Future<OfflineAccessResultVO> batchUploadOfflineAccessRecords(String deviceId,
                                                               List<Map<String, Object>> offlineRecords);

    /**
     * 澶勭悊绂荤嚎璁板綍鍐茬獊
     * <p>
     * 澶勭悊绂荤嚎璁板綍涓庝簯绔暟鎹殑鍐茬獊锛?
     * - 鏃堕棿鍐茬獊妫€娴?
     * - 鏉冮檺鍐茬獊澶勭悊
     * - 鏁版嵁涓€鑷存€т繚璇?
     * - 鍐茬獊瑙ｅ喅绛栫暐搴旂敤
     * </p>
     *
     * @param conflicts 鍐茬獊璁板綍鍒楄〃
     * @return 鍐茬獊澶勭悊缁撴灉
     */
    Map<String, Object> resolveOfflineAccessRecordConflicts(List<Map<String, Object>> conflicts);

    /**
     * 鑾峰彇绂荤嚎璁板綍缁熻淇℃伅
     * <p>
     * 鑾峰彇璁惧绂荤嚎璁板綍鐨勭粺璁′俊鎭細
     * - 寰呬笂浼犺褰曟暟閲?
     * - 缂撳瓨瀛樺偍绌洪棿浣跨敤
     * - 璁板綍涓婁紶鎴愬姛鐜?
     * - 鏁版嵁鍐茬獊缁熻
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 缁熻淇℃伅Map
     */
    Map<String, Object> getOfflineAccessRecordStatistics(String deviceId);

    // ==================== 搴旀€ラ棬绂佺瓥鐣ユ帴鍙?====================

    /**
     * 鍚敤搴旀€ラ棬绂佹ā寮?
     * <p>
     * 鍦ㄧ壒娈婃儏鍐典笅鍚敤搴旀€ラ棬绂佺瓥鐣ワ細
     * - 绱ф€ヨ闂潈闄愭巿鏉?
     * - 涓存椂鏉冮檺鍙戞斁
     * - 瀹夊叏绾у埆璋冩暣
     * - 瀹¤鏃ュ織璁板綍
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param emergencyType 搴旀€ョ被鍨嬶紙FIRE/SECURITY/MAINTENANCE锛?
     * @param authorizedRoles 鎺堟潈瑙掕壊鍒楄〃
     * @return 搴旀€ユā寮忓惎鐢ㄧ粨鏋?
     */
    Map<String, Object> enableEmergencyAccessMode(String deviceId,
                                                  String emergencyType,
                                                  List<String> authorizedRoles);

    /**
     * 鎵ц搴旀€ユ潈闄愰獙璇?
     * <p>
     * 鍦ㄥ簲鎬ユā寮忎笅鎵ц鐗规畩鐨勬潈闄愰獙璇侊細
     * - 搴旀€ヨ韩浠介獙璇?
     * - 涓存椂鏉冮檺妫€鏌?
     * - 鍗遍櫓绾у埆璇勪及
     * - 璁块棶璁板綍鐗规畩鏍囪
     * </p>
     *
     * @param verificationRequest 楠岃瘉璇锋眰
     * @param emergencyContext 搴旀€ヤ笂涓嬫枃淇℃伅
     * @return 搴旀€ラ獙璇佺粨鏋?
     */
    Map<String, Object> performEmergencyAccessVerification(Map<String, Object> verificationRequest,
                                                           Map<String, Object> emergencyContext);

    /**
     * 閰嶇疆搴旀€ラ棬绂佺瓥鐣?
     * <p>
     * 閰嶇疆璁惧鐨勫叿浣撳簲鎬ラ棬绂佺瓥鐣ワ細
     * - 搴旀€ヨ闂鍒?
     * - 鏉冮檺鍗囩骇鏈哄埗
     * - 瀹夊叏鐩戞帶绛栫暐
     * - 鑷姩鎭㈠鏉′欢
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param emergencyPolicy 搴旀€ョ瓥鐣ラ厤缃?
     * @return 绛栫暐閰嶇疆缁撴灉
     */
    boolean configureEmergencyAccessPolicy(String deviceId, Map<String, Object> emergencyPolicy);

    /**
     * 閫€鍑哄簲鎬ラ棬绂佹ā寮?
     * <p>
     * 浠庡簲鎬ユā寮忔仮澶嶆甯搁棬绂佹搷浣滐細
     * - 鏉冮檺鎭㈠姝ｅ父
     * - 涓存椂鏉冮檺娓呯悊
     * - 瀹夊叏绾у埆閲嶇疆
     * - 鎿嶄綔璁板綍褰掓。
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param exitReason 閫€鍑哄師鍥?
     * @return 閫€鍑哄簲鎬ユā寮忕粨鏋?
     */
    Map<String, Object> exitEmergencyAccessMode(String deviceId, String exitReason);

    // ==================== 璁惧绂荤嚎鐘舵€佺洃鎺ф帴鍙?====================

    /**
     * 妫€鏌ヨ澶囩绾跨姸鎬?
     * <p>
     * 妫€鏌ラ棬绂佽澶囩殑绂荤嚎鐘舵€佸拰鑳藉姏锛?
     * - 缃戠粶杩炴帴鐘舵€?
     * - 绂荤嚎鍔熻兘鍙敤鎬?
     * - 鏁版嵁缂撳瓨鐘舵€?
     * - 鐢垫睜鎴栫數婧愮姸鎬?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧绂荤嚎鐘舵€佷俊鎭?
     */
    Map<String, Object> checkDeviceOfflineStatus(String deviceId);

    /**
     * 鐩戞帶璁惧绂荤嚎鎬ц兘
     * <p>
     * 鐩戞帶璁惧鍦ㄧ绾跨姸鎬佷笅鐨勬€ц兘琛ㄧ幇锛?
     * - 楠岃瘉鍝嶅簲鏃堕棿
     * - 瀛樺偍浣跨敤鎯呭喌
     * - 澶勭悊鑳藉姏鐩戞帶
     * - 閿欒鐜囩粺璁?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param monitoringPeriod 鐩戞帶鍛ㄦ湡锛堢锛?
     * @return 鎬ц兘鐩戞帶鎶ュ憡
     */
    Map<String, Object> monitorDeviceOfflinePerformance(String deviceId, Integer monitoringPeriod);

    /**
     * 棰勬祴璁惧绂荤嚎椋庨櫓
     * <p>
     * 棰勬祴璁惧鍙兘鍑虹幇鐨勭绾块闄╋細
     * - 缃戠粶杩炴帴椋庨櫓璇勪及
     * - 鐢垫簮澶辨晥椋庨櫓棰勬祴
     * - 瀛樺偍绌洪棿鑰楀敖棰勮
     * - 瀹夊叏濞佽儊椋庨櫓璇勪及
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param riskTimeRange 椋庨櫓棰勬祴鏃堕棿鑼冨洿锛堝皬鏃讹級
     * @return 椋庨櫓璇勪及鎶ュ憡
     */
    Map<String, Object> predictDeviceOfflineRisks(String deviceId, Integer riskTimeRange);

    /**
     * 鑾峰彇绂荤嚎妯″紡浼樺寲寤鸿
     * <p>
     * 鍩轰簬璁惧鐘舵€佸拰鍘嗗彶鏁版嵁鎻愪緵浼樺寲寤鸿锛?
     * - 鏁版嵁鍚屾棰戠巼浼樺寲
     * - 缂撳瓨绛栫暐璋冩暣寤鸿
     * - 搴旀€ラ厤缃紭鍖?
     * - 缁存姢淇濆吇寤鸿
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 浼樺寲寤鸿鍒楄〃
     */
    List<Map<String, Object>> getOfflineModeOptimizationSuggestions(String deviceId);

    // ==================== 缃戠粶鐘舵€佹劅鐭ユ帴鍙?====================

    /**
     * 妫€娴嬬綉缁滆繛鎺ヨ川閲?
     * <p>
     * 妫€娴嬭澶囦笌缃戠粶鐨勮繛鎺ヨ川閲忥細
     * - 寤惰繜鍜屽甫瀹芥祴璇?
     * - 杩炴帴绋冲畾鎬ц瘎浼?
     * - 鏁版嵁浼犺緭璐ㄩ噺妫€娴?
     * - 鏈€浣宠繛鎺ユ椂鏈烘帹鑽?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 缃戠粶璐ㄩ噺璇勪及缁撴灉
     */
    Map<String, Object> detectNetworkConnectionQuality(String deviceId);

    /**
     * 鑷姩鍒囨崲绂荤嚎妯″紡
     * <p>
     * 鍩轰簬缃戠粶鐘舵€佽嚜鍔ㄥ垏鎹㈠埌绂荤嚎妯″紡锛?
     * - 缃戠粶璐ㄩ噺闃堝€煎垽鏂?
     * - 骞虫粦妯″紡鍒囨崲
     * - 鏁版嵁鍚屾淇濇姢
     * - 鐢ㄦ埛閫氱煡鏈哄埗
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param networkStatus 褰撳墠缃戠粶鐘舵€?
     * @return 妯″紡鍒囨崲缁撴灉
     */
    Map<String, Object> autoSwitchToOfflineMode(String deviceId, Map<String, Object> networkStatus);

    /**
     * 閰嶇疆缃戠粶鎰熺煡绛栫暐
     * <p>
     * 閰嶇疆璁惧鐨勭綉缁滅姸鎬佹劅鐭ョ瓥鐣ワ細
     * - 缃戠粶璐ㄩ噺闃堝€艰瀹?
     * - 妯″紡鍒囨崲瑙﹀彂鏉′欢
     * - 鏁版嵁鍚屾浼樺厛绾?
     * - 闄嶇骇鏈嶅姟绛栫暐
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param networkAwarePolicy 缃戠粶鎰熺煡绛栫暐閰嶇疆
     * @return 绛栫暐閰嶇疆缁撴灉
     */
    boolean configureNetworkAwarePolicy(String deviceId, Map<String, Object> networkAwarePolicy);

    /**
     * 鑾峰彇缃戠粶鍘嗗彶鐘舵€佸垎鏋?
     * <p>
     * 鍒嗘瀽璁惧鐨勫巻鍙茬綉缁滅姸鎬佹ā寮忥細
     * - 缃戠粶涓柇棰戠巼缁熻
     * - 鏈€浣冲湪绾挎椂娈靛垎鏋?
     * - 缃戠粶璐ㄩ噺鍙樺寲瓒嬪娍
     * - 绂荤嚎鏃堕暱鍒嗗竷缁熻
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param analysisDays 鍒嗘瀽澶╂暟
     * @return 缃戠粶鐘舵€佸垎鏋愭姤鍛?
     */
    Map<String, Object> getNetworkHistoryAnalysis(String deviceId, Integer analysisDays);
}