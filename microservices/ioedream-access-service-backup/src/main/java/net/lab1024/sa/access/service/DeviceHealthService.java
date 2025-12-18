package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.access.domain.form.DeviceMonitorRequest;
import net.lab1024.sa.access.domain.form.MaintenancePredictRequest;
import net.lab1024.sa.access.domain.vo.DeviceHealthVO;
import net.lab1024.sa.access.domain.vo.DevicePerformanceAnalyticsVO;
import net.lab1024.sa.access.domain.vo.MaintenancePredictionVO;

/**
 * 璁惧鍋ュ悍鐩戞帶鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵璁惧鍋ュ悍鐘舵€佺洃鎺с€佹€ц兘鍒嗘瀽銆侀娴嬫€х淮鎶ょ瓑鍔熻兘
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - Service鎺ュ彛瀹氫箟鍦ㄤ笟鍔℃湇鍔℃ā鍧椾腑
 * - 鏂规硶杩斿洖缁熶竴鐨勪笟鍔″璞?
 * - 娓呮櫚鐨勬柟娉曟敞閲?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface DeviceHealthService {

    /**
     * 鐩戞帶璁惧鍋ュ悍鐘舵€?
     * <p>
     * 瀹炴椂鐩戞帶璁惧鐨勫悇椤瑰仴搴锋寚鏍囷細
     * - 璁惧鍦ㄧ嚎鐘舵€佹鏌?
     * - 缃戠粶杩炴帴璐ㄩ噺璇勪及
     * - 鍝嶅簲鏃堕棿娴嬭瘯
     * - 閿欒鐜囩粺璁?
     * - 璧勬簮浣跨敤鐜囩洃鎺?
     * </p>
     *
     * @param request 璁惧鐩戞帶璇锋眰鍙傛暟
     * @return 璁惧鍋ュ悍鐘舵€佽缁嗕俊鎭?
     */
    DeviceHealthVO monitorDeviceHealth(DeviceMonitorRequest request);

    /**
     * 鑾峰彇璁惧鎬ц兘鍒嗘瀽
     * <p>
     * 鍩轰簬鍘嗗彶鏁版嵁鍒嗘瀽璁惧鎬ц兘瓒嬪娍锛?
     * - 鍝嶅簲鏃堕棿瓒嬪娍鍒嗘瀽
     * - 鎴愬姛鐜囧彉鍖栬秼鍔?
     * - 璐熻浇宄板€煎垎鏋?
     * - 鎬ц兘鐡堕璇嗗埆
     * - 浼樺寲寤鸿鐢熸垚
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧鎬ц兘鍒嗘瀽缁撴灉
     */
    DevicePerformanceAnalyticsVO getDevicePerformanceAnalytics(Long deviceId);

    /**
     * 棰勬祴璁惧缁存姢闇€姹?
     * <p>
     * 鍩轰簬AI绠楁硶棰勬祴璁惧鐨勭淮鎶ら渶姹傦細
     * - 鏁呴殰姒傜巼棰勬祴
     * - 缁存姢鏃堕棿绐楀彛寤鸿
     * - 缁存姢浼樺厛绾ф帓搴?
     * - 缁存姢鎴愭湰棰勪及
     * - 缁存姢鏂规鎺ㄨ崘
     * </p>
     *
     * @param request 缁存姢棰勬祴璇锋眰鍙傛暟
     * @return 缁存姢棰勬祴缁撴灉鍒楄〃
     */
    List<MaintenancePredictionVO> predictMaintenanceNeeds(MaintenancePredictRequest request);

    /**
     * 鑾峰彇璁惧鍋ュ悍缁熻淇℃伅
     * <p>
     * 缁熻鎵€鏈夎澶囩殑鍋ュ悍鐘舵€佸垎甯冿細
     * - 鍋ュ悍璁惧鏁伴噺鍜屾瘮渚?
     * - 浜氬仴搴疯澶囨暟閲忓拰姣斾緥
     * - 鏁呴殰璁惧鏁伴噺鍜屾瘮渚?
     * - 绂荤嚎璁惧鏁伴噺鍜屾瘮渚?
     * - 璁惧绫诲瀷鍋ュ悍鍒嗗竷
     * </p>
     *
     * @return 璁惧鍋ュ悍缁熻淇℃伅
     */
    Object getDeviceHealthStatistics();

    /**
     * 鑾峰彇璁惧鍋ュ悍鍘嗗彶鏁版嵁
     * <p>
     * 鑾峰彇鎸囧畾鏃堕棿鑼冨洿鍐呯殑璁惧鍋ュ悍鍘嗗彶鏁版嵁锛?
     * - 鍋ュ悍璇勫垎鍙樺寲瓒嬪娍
     * - 鍏抽敭鎸囨爣鍘嗗彶璁板綍
     * - 寮傚父浜嬩欢璁板綍
     * - 缁存姢璁板綍鍏宠仈
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param startDate 寮€濮嬫椂闂?
     * @param endDate 缁撴潫鏃堕棿
     * @return 鍒嗛〉鐨勫巻鍙插仴搴锋暟鎹?
     */
    PageResult<Object> getDeviceHealthHistory(Long deviceId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 鏇存柊璁惧鍋ュ悍鐘舵€?
     * <p>
     * 瀹氭椂浠诲姟璋冪敤鐨勫仴搴风姸鎬佹洿鏂帮細
     * - 鎵归噺鍋ュ悍妫€鏌?
     * - 鐘舵€佸彉鍖栭€氱煡
     * - 寮傚父鍛婅瑙﹀彂
     * </p>
     *
     * @param deviceIds 璁惧ID鍒楄〃锛屼负绌烘椂妫€鏌ユ墍鏈夎澶?
     */
    void updateDeviceHealthStatus(List<Long> deviceIds);
}