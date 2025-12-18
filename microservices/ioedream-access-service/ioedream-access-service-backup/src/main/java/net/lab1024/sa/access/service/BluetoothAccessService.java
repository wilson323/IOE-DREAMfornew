package net.lab1024.sa.access.service;

import java.util.List;

import net.lab1024.sa.access.controller.AccessMobileController.*;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 钃濈墮闂ㄧ鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵钃濈墮闂ㄧ鐩稿叧鐨勬牳蹇冨姛鑳斤細
 * - 钃濈墮璁惧鎵弿鍜岃繛鎺?
 * - 钃濈墮闂ㄧ楠岃瘉
 * - 璁惧閰嶅鍜岀鐞?
 * - 钃濈墮闂ㄧ鍗＄鐞?
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
public interface BluetoothAccessService {

    /**
     * 鎵弿闄勮繎鐨勮摑鐗欒澶?
     * <p>
     * 鎵弿鐢ㄦ埛闄勮繎鐨勮摑鐗欓棬绂佽澶囷紝鏀寔杩囨护鍜屾帓搴忥細
     * - 鎸変俊鍙峰己搴︽帓搴?
     * - 鎸夎澶囩被鍨嬭繃婊?
     * - 鏀寔鏈€澶ц澶囨暟闄愬埗
     * </p>
     *
     * @param request 鎵弿璇锋眰
     * @return 鍙戠幇鐨勮摑鐗欒澶囧垪琛?
     */
    ResponseDTO<List<BluetoothDeviceVO>> scanNearbyDevices(BluetoothScanRequest request);

    /**
     * 杩炴帴钃濈墮璁惧
     * <p>
     * 涓庢寚瀹氱殑钃濈墮璁惧寤虹珛杩炴帴锛?
     * - 鏀寔鑷姩閲嶈繛
     * - 杩炴帴鐘舵€佺洃鎺?
     * - 鍗忚鐗堟湰鍗忓晢
     * </p>
     *
     * @param request 杩炴帴璇锋眰
     * @return 杩炴帴缁撴灉
     */
    ResponseDTO<BluetoothConnectionResult> connectDevice(BluetoothConnectRequest request);

    /**
     * 鎵ц钃濈墮闂ㄧ楠岃瘉
     * <p>
     * 閫氳繃钃濈墮杩涜闂ㄧ鏉冮檺楠岃瘉锛?
     * - 璁惧韬唤楠岃瘉
     * - 鐢ㄦ埛鏉冮檺妫€鏌?
     * - 瀹夊叏鍔犲瘑閫氫俊
     * </p>
     *
     * @param request 楠岃瘉璇锋眰
     * @return 楠岃瘉缁撴灉
     */
    ResponseDTO<BluetoothAccessResult> performBluetoothAccess(BluetoothAccessRequest request);

    /**
     * 鑾峰彇宸茶繛鎺ヨ澶囩姸鎬?
     * <p>
     * 鏌ヨ鐢ㄦ埛宸茶繛鎺ョ殑钃濈墮璁惧鐘舵€侊細
     * - 杩炴帴鐘舵€?
     * - 淇″彿寮哄害
     * - 鐢垫睜鐢甸噺
     * - 浣跨敤缁熻
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 璁惧鐘舵€佸垪琛?
     */
    ResponseDTO<List<BluetoothDeviceStatusVO>> getConnectedDevicesStatus(Long userId);

    /**
     * 鏂紑钃濈墮璁惧杩炴帴
     * <p>
     * 鏂紑涓庢寚瀹氳摑鐗欒澶囩殑杩炴帴锛?
     * - 浼橀泤鏂紑杩炴帴
     * - 娓呯悊杩炴帴鐘舵€?
     * - 閲婃斁璧勬簮
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceAddress 璁惧MAC鍦板潃
     * @return 鏄惁鎴愬姛鏂紑
     */
    boolean disconnectDevice(Long userId, String deviceAddress);

    /**
     * 閰嶅钃濈墮璁惧
     * <p>
     * 涓庤摑鐗欒澶囪繘琛屽畨鍏ㄩ厤瀵癸細
     * - PIN鐮侀獙璇?
     * - 鍔犲瘑瀵嗛挜浜ゆ崲
     * - 閰嶅鐘舵€佺鐞?
     * </p>
     *
     * @param request 閰嶅璇锋眰
     * @return 閰嶅缁撴灉
     */
    ResponseDTO<BluetoothPairingResult> pairDevice(BluetoothPairingRequest request);

    /**
     * 鑾峰彇鐢ㄦ埛闂ㄧ鍗′俊鎭?
     * <p>
     * 鑾峰彇鐢ㄦ埛鐨勬墍鏈夐棬绂佸崱锛屽寘鎷摑鐗欓棬绂佸崱锛?
     * - 鐗╃悊闂ㄧ鍗?
     * - 铏氭嫙闂ㄧ鍗?
     * - 钃濈墮闂ㄧ鍗?
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 闂ㄧ鍗″垪琛?
     */
    ResponseDTO<List<UserAccessCardVO>> getUserAccessCards(Long userId);

    /**
     * 娣诲姞钃濈墮闂ㄧ鍗?
     * <p>
     * 涓虹敤鎴锋坊鍔犳柊鐨勮摑鐗欓棬绂佸崱锛?
     * - 璁惧缁戝畾
     * - 鏉冮檺閰嶇疆
     * - 鏈夋晥鏈熺鐞?
     * </p>
     *
     * @param request 娣诲姞璇锋眰
     * @return 娣诲姞缁撴灉
     */
    ResponseDTO<String> addBluetoothAccessCard(AddBluetoothCardRequest request);

    /**
     * 钃濈墮璁惧鍋ュ悍妫€鏌?
     * <p>
     * 妫€鏌ヨ摑鐗欒澶囩殑鍋ュ悍鐘舵€侊細
     * - 杩炴帴绋冲畾鎬?
     * - 鐢垫睜鐘舵€?
     * - 淇″彿璐ㄩ噺
     * - 鍥轰欢鐗堟湰
     * </p>
     *
     * @param deviceAddress 璁惧MAC鍦板潃
     * @return 鍋ュ悍鐘舵€佷俊鎭?
     */
    ResponseDTO<BluetoothDeviceHealthVO> checkDeviceHealth(String deviceAddress);

    /**
     * 鏇存柊钃濈墮璁惧鍥轰欢
     * <p>
     * 涓鸿摑鐗欒澶囪繘琛屽浐浠跺崌绾э細
     * - 鐗堟湰妫€鏌?
     * - 鍥轰欢涓嬭浇
     * - 瀹夊叏鍗囩骇
     * </p>
     *
     * @param deviceAddress 璁惧MAC鍦板潃
     * @param firmwareVersion 鐩爣鍥轰欢鐗堟湰
     * @return 鍗囩骇缁撴灉
     */
    ResponseDTO<BluetoothFirmwareUpdateResult> updateDeviceFirmware(
            String deviceAddress, String firmwareVersion);

    /**
     * 钃濈墮璁惧璇婃柇
     * <p>
     * 瀵硅摑鐗欒澶囪繘琛屽叏闈㈣瘖鏂細
     * - 杩炴帴璐ㄩ噺娴嬭瘯
     * - 鍔熻兘妯″潡娴嬭瘯
     * - 鎬ц兘鍩哄噯娴嬭瘯
     * </p>
     *
     * @param deviceAddress 璁惧MAC鍦板潃
     * @return 璇婃柇鎶ュ憡
     */
    ResponseDTO<BluetoothDeviceDiagnosticVO> diagnoseDevice(String deviceAddress);

    // ==================== 鍐呴儴鏁版嵁浼犺緭瀵硅薄 ====================

    /**
     * 钃濈墮璁惧鍋ュ悍鐘舵€?
     */
    class BluetoothDeviceHealthVO {
        private String deviceAddress;
        private String deviceName;
        private String healthStatus;        // 鍋ュ悍鐘舵€侊細EXCELLENT, GOOD, FAIR, POOR
        private Integer connectionScore;     // 杩炴帴璇勫垎锛?-100锛?
        private Integer signalQuality;       // 淇″彿璐ㄩ噺锛?-100锛?
        private Integer batteryHealth;       // 鐢垫睜鍋ュ悍搴︼紙0-100锛?
        private String lastCheckTime;        // 鏈€鍚庢鏌ユ椂闂?
        private List<String> issues;         // 鍙戠幇鐨勯棶棰?
        private List<String> recommendations; // 鏀硅繘寤鸿
    }

    /**
     * 钃濈墮鍥轰欢鍗囩骇缁撴灉
     */
    class BluetoothFirmwareUpdateResult {
        private Boolean success;
        private String deviceAddress;
        private String currentVersion;      // 褰撳墠鐗堟湰
        private String targetVersion;       // 鐩爣鐗堟湰
        private String updateStatus;        // 鍗囩骇鐘舵€?
        private Long updateDuration;        // 鍗囩骇鑰楁椂锛堟绉掞級
        private String errorMessage;        // 閿欒淇℃伅
        private Boolean requiresReboot;     // 鏄惁闇€瑕侀噸鍚?
    }

    /**
     * 钃濈墮璁惧璇婃柇鎶ュ憡
     */
    class BluetoothDeviceDiagnosticVO {
        private String deviceAddress;
        private String deviceName;
        private String diagnosticTime;      // 璇婃柇鏃堕棿
        private Integer overallScore;       // 缁煎悎璇勫垎锛?-100锛?
        private DiagnosticResult connectionTest;  // 杩炴帴娴嬭瘯缁撴灉
        private DiagnosticResult functionalityTest; // 鍔熻兘娴嬭瘯缁撴灉
        private DiagnosticResult performanceTest;   // 鎬ц兘娴嬭瘯缁撴灉
        private List<String> identifiedIssues;     // 璇嗗埆鐨勯棶棰?
        private List<String> suggestedActions;      // 寤鸿鎿嶄綔
    }

    /**
     * 璇婃柇娴嬭瘯缁撴灉
     */
    class DiagnosticResult {
        private String testName;           // 娴嬭瘯鍚嶇О
        private Boolean passed;            // 鏄惁閫氳繃
        private Integer score;             // 璇勫垎锛?-100锛?
        private String details;            // 璇︾粏淇℃伅
        private String status;             // 鐘舵€侊細PASS, FAIL, WARNING
    }
}