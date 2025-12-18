package net.lab1024.sa.access.service;

import java.util.List;

import net.lab1024.sa.access.controller.AccessMobileController.MobileAreaItem;
import net.lab1024.sa.access.controller.AccessMobileController.MobileDeviceItem;
import net.lab1024.sa.access.controller.AccessMobileController.MobileRealTimeStatus;
import net.lab1024.sa.access.controller.AccessMobileController.MobileUserPermissions;
import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.form.DeviceControlRequest;
import net.lab1024.sa.access.domain.form.AddDeviceRequest;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.access.domain.vo.MobileDeviceVO;
import net.lab1024.sa.access.domain.vo.DeviceControlResultVO;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 闂ㄧ璁惧鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵闂ㄧ璁惧绠＄悊鐩稿叧涓氬姟鍔熻兘
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - Service鎺ュ彛瀹氫箟鍦ㄤ笟鍔℃湇鍔℃ā鍧椾腑
 * - 鏂规硶杩斿洖ResponseDTO缁熶竴鏍煎紡
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessDeviceService {

    /**
     * 鑾峰彇闄勮繎璁惧
     *
     * @param userId 鐢ㄦ埛ID
     * @param latitude 绾害
     * @param longitude 缁忓害
     * @param radius 鍗婂緞锛堢背锛?     * @return 璁惧鍒楄〃
     */
    ResponseDTO<List<MobileDeviceItem>> getNearbyDevices(Long userId, Double latitude, Double longitude, Integer radius);

    /**
     * 鑾峰彇绉诲姩绔敤鎴锋潈闄?     *
     * @param userId 鐢ㄦ埛ID
     * @return 鏉冮檺淇℃伅
     */
    ResponseDTO<MobileUserPermissions> getMobileUserPermissions(Long userId);

    /**
     * 鑾峰彇绉诲姩绔疄鏃剁姸鎬?     *
     * @param deviceId 璁惧ID
     * @return 鐘舵€佷俊鎭?     */
    ResponseDTO<MobileRealTimeStatus> getMobileRealTimeStatus(Long deviceId);

    /**
     * 鍒嗛〉鏌ヨ璁惧
     *
     * @param queryForm 鏌ヨ琛ㄥ崟
     * @return 璁惧鍒嗛〉缁撴灉
     */
    ResponseDTO<PageResult<AccessDeviceVO>> queryDevices(AccessDeviceQueryForm queryForm);

    /**
     * 鏌ヨ璁惧璇︽儏
     *
     * @param deviceId 璁惧ID
     * @return 璁惧璇︽儏
     */
    ResponseDTO<AccessDeviceVO> getDeviceDetail(Long deviceId);

    /**
     * 娣诲姞璁惧
     *
     * @param addForm 娣诲姞琛ㄥ崟
     * @return 璁惧ID
     */
    ResponseDTO<Long> addDevice(AccessDeviceAddForm addForm);

    /**
     * 鏇存柊璁惧
     *
     * @param updateForm 鏇存柊琛ㄥ崟
     * @return 鏄惁鎴愬姛
     */
    ResponseDTO<Boolean> updateDevice(AccessDeviceUpdateForm updateForm);

    /**
     * 鍒犻櫎璁惧
     *
     * @param deviceId 璁惧ID
     * @return 鏄惁鎴愬姛
     */
    ResponseDTO<Boolean> deleteDevice(Long deviceId);

    /**
     * 鏇存柊璁惧鐘舵€?     *
     * @param deviceId 璁惧ID
     * @param status 璁惧鐘舵€?     * @return 鏄惁鎴愬姛
     */
    ResponseDTO<Boolean> updateDeviceStatus(Long deviceId, Integer status);

    /**
     * 鑾峰彇绉诲姩绔尯鍩熷垪琛?     * <p>
     * 鑾峰彇鐢ㄦ埛鏈夋潈闄愯闂殑鍖哄煙鍒楄〃锛屽寘鍚尯鍩熻鎯咃紙鍚嶇О銆佺被鍨嬨€佽澶囨暟閲忕瓑锛?     * </p>
     *
     * @param userId 鐢ㄦ埛ID锛堝彲閫夛紝涓嶄紶鍒欎粠Token鑾峰彇锛?     * @return 鍖哄煙鍒楄〃
     */
    ResponseDTO<List<MobileAreaItem>> getMobileAreas(Long userId);

    // ==================== 绉诲姩绔澶囩鐞嗗姛鑳?====================

    /**
     * 鑾峰彇绉诲姩绔澶囧垪琛?     * <p>
     * 鑾峰彇鐢ㄦ埛鏈夋潈闄愮鐞嗙殑璁惧鍒楄〃锛屾敮鎸佹寜绫诲瀷銆佺姸鎬併€佸尯鍩熴€佸叧閿瘝杩囨护
     * </p>
     *
     * @param userId 鐢ㄦ埛ID锛堝彲閫夛紝涓嶄紶鍒欎粠Token鑾峰彇锛?     * @param deviceType 璁惧绫诲瀷锛堝彲閫夛級
     * @param status 璁惧鐘舵€侊紙鍙€夛級
     * @param areaId 鎵€灞炲尯鍩燂紙鍙€夛級
     * @param keyword 鍏抽敭璇嶆悳绱紙鍙€夛級
     * @return 璁惧鍒楄〃
     */
    ResponseDTO<List<MobileDeviceVO>> getMobileDeviceList(Long userId, Integer deviceType,
                                                        Integer status, Long areaId, String keyword);

    /**
     * 璁惧鎺у埗鎿嶄綔
     * <p>
     * 瀵规寚瀹氳澶囨墽琛屾帶鍒舵搷浣滐紝濡傞噸鍚€佺淮鎶ゃ€佹牎鍑嗙瓑
     * </p>
     *
     * @param request 鎺у埗璇锋眰
     * @return 鎺у埗缁撴灉
     */
    ResponseDTO<DeviceControlResultVO> controlDevice(DeviceControlRequest request);

    /**
     * 娣诲姞璁惧
     * <p>
     * 绉诲姩绔坊鍔犳柊璁惧锛岃嚜鍔ㄥ垎閰嶈澶嘔D锛岃褰曟搷浣滄棩蹇?     * </p>
     *
     * @param request 娣诲姞璁惧璇锋眰
     * @return 璁惧ID
     */
    ResponseDTO<Long> addMobileDevice(AddDeviceRequest request);

    /**
     * 鍒犻櫎璁惧
     * <p>
     * 杞垹闄よ澶囷紝淇濈暀鍘嗗彶璁板綍锛岄獙璇佹潈闄?     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 鏄惁鎴愬姛
     */
    ResponseDTO<Boolean> deleteMobileDevice(Long deviceId);

    /**
     * 閲嶅惎璁惧
     * <p>
     * 杩滅▼閲嶅惎璁惧锛屾敮鎸佽蒋閲嶅惎鍜岀‖閲嶅惎
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param restartType 閲嶅惎绫诲瀷锛坰oft/hard锛?     * @param reason 閲嶅惎鍘熷洜
     * @return 閲嶅惎缁撴灉
     */
    ResponseDTO<DeviceControlResultVO> restartDevice(Long deviceId, String restartType, String reason);

    /**
     * 璁惧缁存姢妯″紡
     * <p>
     * 璁剧疆璁惧缁存姢妯″紡锛屾敮鎸佽缃淮鎶ゆ椂闀垮拰鍘熷洜
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param maintenanceDuration 缁存姢鏃堕暱锛堝皬鏃讹級
     * @param reason 缁存姢鍘熷洜
     * @return 鎿嶄綔缁撴灉
     */
    ResponseDTO<DeviceControlResultVO> setMaintenanceMode(Long deviceId, Integer maintenanceDuration, String reason);

    /**
     * 鏍″噯璁惧
     * <p>
     * 鏍″噯璁惧浼犳劅鍣ㄦ垨璇嗗埆妯″潡锛屾敮鎸佸绉嶆牎鍑嗙被鍨?     * </p>
     *
     * @param deviceId 璁惧ID
     * @param calibrationType 鏍″噯绫诲瀷
     * @param calibrationPrecision 鏍″噯绮惧害
     * @return 鏍″噯缁撴灉
     */
    ResponseDTO<DeviceControlResultVO> calibrateDevice(Long deviceId, String calibrationType, String calibrationPrecision);

    /**
     * 鑾峰彇绉诲姩绔澶囪鎯?     * <p>
     * 鑾峰彇璁惧璇︾粏淇℃伅锛屽寘鎷繍琛岀姸鎬併€佺粺璁℃暟鎹€佺淮鎶よ褰曠瓑
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧璇︽儏
     */
    ResponseDTO<MobileDeviceVO> getMobileDeviceDetail(Long deviceId);
}

