package net.lab1024.sa.access.edge.service;

import java.util.Map;
import java.util.List;
import java.util.concurrent.Future;

import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.model.InferenceRequest;
import net.lab1024.sa.video.edge.model.InferenceResult;

/**
 * 杈圭紭瀹夊叏鏈嶅姟鎺ュ彛
 * <p>
 * 瀹氫箟杈圭紭璁惧瀹夊叏鎺ㄧ悊鏈嶅姟鐨勬牳蹇冨姛鑳芥帴鍙ｏ細
 * - 杈圭紭璁惧瀹夊叏鎺ㄧ悊绠＄悊
 * - 瀹夊叏妯″瀷鍔ㄦ€佹洿鏂?
 * - 璁惧鐘舵€佺洃鎺?
 * - 缁熻淇℃伅鏀堕泦
 * - 浜戣竟鍗忓悓鎺ㄧ悊
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface EdgeSecurityService {

    // ==================== 鏍稿績鎺ㄧ悊鎺ュ彛 ====================

    /**
     * 鎵ц瀹夊叏鎺ㄧ悊
     * <p>
     * 鍦ㄨ竟缂樿澶囦笂鎵ц瀹夊叏鐩稿叧鐨凙I鎺ㄧ悊浠诲姟
     * 鏀寔娲讳綋妫€娴嬨€佷汉鑴歌瘑鍒€佽涓哄垎鏋愮瓑
     * </p>
     *
     * @param inferenceRequest 鎺ㄧ悊璇锋眰
     * @return 鎺ㄧ悊缁撴灉Future
     */
    Future<InferenceResult> performSecurityInference(InferenceRequest inferenceRequest);

    /**
     * 鎵ц鍗忓悓瀹夊叏鎺ㄧ悊
     * <p>
     * 澶嶆潅鎺ㄧ悊浠诲姟鐨勪簯杈瑰崗鍚屽鐞?
     * 杈圭紭璁惧浼樺厛锛屼簯绔緟鍔╋紝缁撴灉铻嶅悎
     * </p>
     *
     * @param inferenceRequest 鎺ㄧ悊璇锋眰
     * @return 鍗忓悓鎺ㄧ悊缁撴灉Future
     */
    Future<InferenceResult> performCollaborativeSecurityInference(InferenceRequest inferenceRequest);

    /**
     * 鎵归噺瀹夊叏鎺ㄧ悊
     * <p>
     * 鎵归噺澶勭悊澶氫釜瀹夊叏鎺ㄧ悊浠诲姟
     * 鎻愰珮杈圭紭璁惧鍒╃敤鐜?
     * </p>
     *
     * @param inferenceRequests 鎺ㄧ悊璇锋眰Map (taskId -> InferenceRequest)
     * @return 鎵归噺鎺ㄧ悊缁撴灉Map (taskId -> Future<InferenceResult>)
     */
    Map<String, Future<InferenceResult>> performBatchSecurityInference(Map<String, InferenceRequest> inferenceRequests);

    // ==================== 璁惧绠＄悊鎺ュ彛 ====================

    /**
     * 娉ㄥ唽杈圭紭瀹夊叏璁惧
     * <p>
     * 娉ㄥ唽鏂扮殑杈圭紭瀹夊叏璁惧鍒扮郴缁熶腑
     * 寤虹珛杩炴帴骞跺悓姝ュ畨鍏ˋI妯″瀷
     * </p>
     *
     * @param edgeDevice 杈圭紭璁惧淇℃伅
     * @return 娉ㄥ唽缁撴灉
     */
    boolean registerEdgeSecurityDevice(EdgeDevice edgeDevice);

    /**
     * 娉ㄩ攢杈圭紭瀹夊叏璁惧
     * <p>
     * 浠庣郴缁熶腑娉ㄩ攢杈圭紭瀹夊叏璁惧
     * 娓呯悊杩炴帴鍜岀浉鍏宠祫婧?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 娉ㄩ攢缁撴灉
     */
    boolean unregisterEdgeSecurityDevice(String deviceId);

    /**
     * 鑾峰彇杈圭紭璁惧瀹夊叏鐘舵€?
     * <p>
     * 鏌ヨ杈圭紭瀹夊叏璁惧鐨勮繍琛岀姸鎬佸拰鎬ц兘鎸囨爣
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧鐘舵€佷俊鎭疢ap
     */
    Map<String, Object> getEdgeDeviceSecurityStatus(String deviceId);

    /**
     * 鑾峰彇鎵€鏈夎竟缂樺畨鍏ㄨ澶囩姸鎬?
     * <p>
     * 鎵归噺鏌ヨ鎵€鏈夎竟缂樺畨鍏ㄨ澶囩殑鐘舵€?
     * </p>
     *
     * @return 璁惧鐘舵€佸垪琛?
     */
    List<Map<String, Object>> getAllEdgeSecurityDeviceStatus();

    // ==================== 妯″瀷绠＄悊鎺ュ彛 ====================

    /**
     * 鏇存柊杈圭紭璁惧瀹夊叏妯″瀷
     * <p>
     * 鍔ㄦ€佹洿鏂拌竟缂樿澶囦笂鐨勫畨鍏ˋI妯″瀷
     * 鏀寔鐑洿鏂帮紝涓嶅奖鍝嶈澶囨甯歌繍琛?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param modelType 妯″瀷绫诲瀷
     * @param modelData 妯″瀷鏁版嵁
     * @return 鏇存柊缁撴灉
     */
    boolean updateEdgeSecurityModel(String deviceId, String modelType, byte[] modelData);

    /**
     * 鑾峰彇杈圭紭璁惧妯″瀷淇℃伅
     * <p>
     * 鏌ヨ杈圭紭璁惧涓婂姞杞界殑瀹夊叏妯″瀷淇℃伅
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 妯″瀷淇℃伅Map
     */
    Map<String, Object> getEdgeDeviceModels(String deviceId);

    /**
     * 鍚屾瀹夊叏妯″瀷鍒拌竟缂樿澶?
     * <p>
     * 灏嗕簯绔殑瀹夊叏妯″瀷鍚屾鍒拌竟缂樿澶?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param modelTypes 妯″瀷绫诲瀷鍒楄〃
     * @return 鍚屾缁撴灉
     */
    boolean syncSecurityModelsToDevice(String deviceId, List<String> modelTypes);

    // ==================== 缁熻淇℃伅鎺ュ彛 ====================

    /**
     * 鑾峰彇杈圭紭瀹夊叏缁熻淇℃伅
     * <p>
     * 鑾峰彇杈圭紭瀹夊叏绯荤粺鐨勮繍琛岀粺璁′俊鎭?
     * 鍖呮嫭璁惧鏁伴噺銆佹帹鐞嗘垚鍔熺巼銆佸钩鍧囧搷搴旀椂闂寸瓑
     * </p>
     *
     * @return 缁熻淇℃伅Map
     */
    Map<String, Object> getEdgeSecurityStatistics();

    /**
     * 鑾峰彇杈圭紭璁惧鎬ц兘鎸囨爣
     * <p>
     * 鑾峰彇鎸囧畾杈圭紭璁惧鐨勮缁嗘€ц兘鎸囨爣
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 鎬ц兘鎸囨爣Map
     */
    Map<String, Object> getEdgeDevicePerformanceMetrics(String deviceId);

    /**
     * 鑾峰彇鎺ㄧ悊浠诲姟缁熻淇℃伅
     * <p>
     * 鑾峰彇鎸囧畾鏃堕棿鑼冨洿鍐呯殑鎺ㄧ悊浠诲姟缁熻
     * </p>
     *
     * @param startTime 寮€濮嬫椂闂?
     * @param endTime 缁撴潫鏃堕棿
     * @param deviceId 璁惧ID锛堝彲閫夛級
     * @return 浠诲姟缁熻淇℃伅Map
     */
    Map<String, Object> getInferenceTaskStatistics(java.time.LocalDateTime startTime,
                                                   java.time.LocalDateTime endTime,
                                                   String deviceId);

    // ==================== 瀹夊叏绛栫暐鎺ュ彛 ====================

    /**
     * 閰嶇疆杈圭紭璁惧瀹夊叏绛栫暐
     * <p>
     * 涓鸿竟缂樿澶囬厤缃畨鍏ㄧ浉鍏崇殑绛栫暐鍙傛暟
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param securityPolicies 瀹夊叏绛栫暐Map
     * @return 閰嶇疆缁撴灉
     */
    boolean configureEdgeSecurityPolicies(String deviceId, Map<String, Object> securityPolicies);

    /**
     * 鑾峰彇杈圭紭璁惧瀹夊叏绛栫暐
     * <p>
     * 鏌ヨ杈圭紭璁惧褰撳墠鐨勫畨鍏ㄧ瓥鐣ラ厤缃?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 瀹夊叏绛栫暐Map
     */
    Map<String, Object> getEdgeSecurityPolicies(String deviceId);

    /**
     * 璇勪及杈圭紭璁惧瀹夊叏椋庨櫓
     * <p>
     * 鍩轰簬璁惧杩愯鐘舵€佸拰瀹夊叏绛栫暐杩涜椋庨櫓璇勪及
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 椋庨櫓璇勪及缁撴灉Map
     */
    Map<String, Object> assessEdgeSecurityRisk(String deviceId);

    // ==================== 鍛婅閫氱煡鎺ュ彛 ====================

    /**
     * 閰嶇疆杈圭紭璁惧鍛婅瑙勫垯
     * <p>
     * 涓鸿竟缂樿澶囬厤缃畨鍏ㄥ憡璀﹁鍒?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param alertRules 鍛婅瑙勫垯鍒楄〃
     * @return 閰嶇疆缁撴灉
     */
    boolean configureEdgeSecurityAlerts(String deviceId, List<Map<String, Object>> alertRules);

    /**
     * 鍙戦€佸畨鍏ㄥ憡璀﹂€氱煡
     * <p>
     * 褰撴娴嬪埌瀹夊叏濞佽儊鏃跺彂閫佸憡璀﹂€氱煡
     * </p>
     *
     * @param alertData 鍛婅鏁版嵁Map
     * @return 鍙戦€佺粨鏋?
     */
    boolean sendSecurityAlertNotification(Map<String, Object> alertData);

    /**
     * 鑾峰彇杈圭紭瀹夊叏鍛婅鍘嗗彶
     * <p>
     * 鏌ヨ杈圭紭璁惧鐨勫畨鍏ㄥ憡璀﹀巻鍙茶褰?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param startTime 寮€濮嬫椂闂?
     * @param endTime 缁撴潫鏃堕棿
     * @return 鍛婅鍘嗗彶鍒楄〃
     */
    List<Map<String, Object>> getEdgeSecurityAlertHistory(String deviceId,
                                                         java.time.LocalDateTime startTime,
                                                         java.time.LocalDateTime endTime);

    // ==================== 鏁版嵁绠＄悊鎺ュ彛 ====================

    /**
     * 澶囦唤杈圭紭瀹夊叏鏁版嵁
     * <p>
     * 澶囦唤杈圭紭璁惧涓婄殑閲嶈瀹夊叏鏁版嵁
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param dataTypes 鏁版嵁绫诲瀷鍒楄〃
     * @return 澶囦唤缁撴灉
     */
    boolean backupEdgeSecurityData(String deviceId, List<String> dataTypes);

    /**
     * 鎭㈠杈圭紭瀹夊叏鏁版嵁
     * <p>
     * 浠庝簯绔仮澶嶈竟缂樺畨鍏ㄦ暟鎹?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param backupData 澶囦唤鏁版嵁
     * @return 鎭㈠缁撴灉
     */
    boolean restoreEdgeSecurityData(String deviceId, Map<String, Object> backupData);

    /**
     * 娓呯悊杈圭紭瀹夊叏缂撳瓨
     * <p>
     * 娓呯悊杈圭紭璁惧涓婄殑瀹夊叏鐩稿叧缂撳瓨鏁版嵁
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param cacheTypes 缂撳瓨绫诲瀷鍒楄〃
     * @return 娓呯悊缁撴灉
     */
    boolean clearEdgeSecurityCache(String deviceId, List<String> cacheTypes);

    // ==================== 鍋ュ悍妫€鏌ユ帴鍙?====================

    /**
     * 鎵ц杈圭紭璁惧鍋ュ悍妫€鏌?
     * <p>
     * 瀵硅竟缂樿澶囪繘琛屽叏闈㈢殑鍋ュ悍妫€鏌?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 鍋ュ悍妫€鏌ョ粨鏋淢ap
     */
    Map<String, Object> performEdgeDeviceHealthCheck(String deviceId);

    /**
     * 鑾峰彇杈圭紭璁惧璇婃柇淇℃伅
     * <p>
     * 鑾峰彇杈圭紭璁惧鐨勮缁嗚瘖鏂俊鎭?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璇婃柇淇℃伅Map
     */
    Map<String, Object> getEdgeDeviceDiagnosticInfo(String deviceId);

    /**
     * 淇杈圭紭璁惧闂
     * <p>
     * 灏濊瘯鑷姩淇杈圭紭璁惧鍙戠幇鐨勯棶棰?
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param issueTypes 闂绫诲瀷鍒楄〃
     * @return 淇缁撴灉Map
     */
    Map<String, Object> repairEdgeDeviceIssues(String deviceId, List<String> issueTypes);
}