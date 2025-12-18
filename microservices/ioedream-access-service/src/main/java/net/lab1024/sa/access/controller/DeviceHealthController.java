package net.lab1024.sa.access.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpMethod;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.access.service.DeviceHealthService;
import net.lab1024.sa.access.domain.form.DeviceMonitorRequest;
import net.lab1024.sa.access.domain.form.MaintenancePredictRequest;
import net.lab1024.sa.access.domain.vo.DeviceHealthVO;
import net.lab1024.sa.access.domain.vo.DevicePerformanceAnalyticsVO;
import net.lab1024.sa.access.domain.vo.MaintenancePredictionVO;

/**
 * 璁惧鍋ュ悍鐩戞帶鎺у埗鍣?
 * <p>
 * 鎻愪緵璁惧鍋ュ悍鐘舵€佺洃鎺с€佹€ц兘鍒嗘瀽銆侀娴嬫€х淮鎶ょ瓑浼佷笟绾у姛鑳?
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@RestController娉ㄨВ
 * - 缁熶竴浣跨敤@Resource渚濊禆娉ㄥ叆
 * - 瀹屾暣鐨凙PI鏂囨。娉ㄨВ
 * - 鏉冮檺鎺у埗娉ㄨВ
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@PermissionCheck(value = "ACCESS_MANAGE", description = "璁惧鍋ュ悍鐩戞帶鏉冮檺")
@RequestMapping("/api/v1/device/health")
@Tag(name = "璁惧鍋ュ悍鐩戞帶", description = "璁惧鍋ュ悍鐘舵€佺洃鎺с€佹€ц兘鍒嗘瀽鍜岄娴嬫€х淮鎶?)
public class DeviceHealthController {

    /**
     * 璁惧鍋ュ悍鏈嶅姟
     */
    @Resource
    private DeviceHealthService deviceHealthService;

    /**
     * 缃戝叧鏈嶅姟瀹㈡埛绔?
     */
    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 璁惧鍋ュ悍鐩戞帶
     * <p>
     * 瀹炴椂鐩戞帶鎸囧畾璁惧鐨勫仴搴风姸鎬侊紝鍖呮嫭锛?
     * - 璁惧鍦ㄧ嚎鐘舵€?
     * - 缃戠粶杩炴帴璐ㄩ噺
     * - 鍝嶅簲鏃堕棿鐩戞帶
     * - 閿欒鐜囩粺璁?
     * </p>
     *
     * @param request 璁惧鐩戞帶璇锋眰
     * @return 璁惧鍋ュ悍鐘舵€佷俊鎭?
     */
    @PostMapping("/monitor")
    @Operation(summary = "璁惧鍋ュ悍鐩戞帶", description = "瀹炴椂鐩戞帶璁惧鍋ュ悍鐘舵€侊紝鍖呮嫭鍦ㄧ嚎鐘舵€併€佺綉缁滆川閲忋€佸搷搴旀椂闂寸瓑鎸囨爣")
    @PermissionCheck(value = {"ACCESS_MANAGER", "DEVICE_MONITOR"}, description = "璁惧鍋ュ悍鐩戞帶鎿嶄綔")
    public ResponseDTO<DeviceHealthVO> monitorDeviceHealth(
            @Valid @RequestBody DeviceMonitorRequest request) {
        log.info("[璁惧鍋ュ悍鐩戞帶] 寮€濮嬬洃鎺ц澶囷紝deviceId={}", request.getDeviceId());

        try {
            DeviceHealthVO result = deviceHealthService.monitorDeviceHealth(request);
            log.info("[璁惧鍋ュ悍鐩戞帶] 鐩戞帶瀹屾垚锛宒eviceId={}, healthScore={}",
                request.getDeviceId(), result.getHealthScore());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[璁惧鍋ュ悍鐩戞帶] 鐩戞帶寮傚父锛宒eviceId={}, error={}",
                request.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("DEVICE_HEALTH_MONITOR_ERROR", "璁惧鍋ュ悍鐩戞帶澶辫触锛? + e.getMessage());
        }
    }

    /**
     * 鑾峰彇璁惧鎬ц兘鍒嗘瀽
     * <p>
     * 鍒嗘瀽璁惧鐨勫巻鍙叉€ц兘鏁版嵁鍜岃秼鍔匡紝鍖呮嫭锛?
     * - 鍝嶅簲鏃堕棿瓒嬪娍
     * - 鎴愬姛鐜囧垎鏋?
     * - 璐熻浇鍒嗘瀽
     * - 鎬ц兘浼樺寲寤鸿
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧鎬ц兘鍒嗘瀽缁撴灉
     */
    @GetMapping("/analytics/{deviceId}")
    @Operation(summary = "璁惧鎬ц兘鍒嗘瀽", description = "鍒嗘瀽璁惧鎬ц兘鎸囨爣鍜岃秼鍔匡紝鎻愪緵浼樺寲寤鸿")
    @PermissionCheck(value = {"ACCESS_MANAGER", "DEVICE_ANALYTICS"}, description = "璁惧鎬ц兘鍒嗘瀽")
    public ResponseDTO<DevicePerformanceAnalyticsVO> getDevicePerformanceAnalytics(
            @Parameter(description = "璁惧ID", required = true)
            @PathVariable Long deviceId) {
        log.info("[璁惧鎬ц兘鍒嗘瀽] 寮€濮嬪垎鏋愯澶囨€ц兘锛宒eviceId={}", deviceId);

        try {
            DevicePerformanceAnalyticsVO result = deviceHealthService.getDevicePerformanceAnalytics(deviceId);
            log.info("[璁惧鎬ц兘鍒嗘瀽] 鍒嗘瀽瀹屾垚锛宒eviceId={}, avgResponseTime={}ms",
                deviceId, result.getAverageResponseTime());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[璁惧鎬ц兘鍒嗘瀽] 鍒嗘瀽寮傚父锛宒eviceId={}, error={}",
                deviceId, e.getMessage(), e);
            return ResponseDTO.error("DEVICE_PERFORMANCE_ANALYTICS_ERROR", "璁惧鎬ц兘鍒嗘瀽澶辫触锛? + e.getMessage());
        }
    }

    /**
     * 棰勬祴鎬х淮鎶ゅ垎鏋?
     * <p>
     * 鍩轰簬AI绠楁硶棰勬祴璁惧鐨勭淮鎶ら渶姹傦紝鍖呮嫭锛?
     * - 鏁呴殰棰勬祴
     * - 缁存姢鏃堕棿寤鸿
     * - 缁存姢浼樺厛绾ц瘎浼?
     * - 鎴愭湰棰勪及
     * </p>
     *
     * @param request 缁存姢棰勬祴璇锋眰
     * @return 缁存姢棰勬祴缁撴灉鍒楄〃
     */
    @PostMapping("/maintenance/predict")
    @Operation(summary = "棰勬祴鎬х淮鎶?, description = "鍩轰簬AI绠楁硶棰勬祴璁惧缁存姢闇€姹傦紝浼樺寲缁存姢璁″垝")
    @PermissionCheck(value = {"ACCESS_MANAGER", "MAINTENANCE_PLAN"}, description = "棰勬祴鎬х淮鎶よ鍒?)
    public ResponseDTO<List<MaintenancePredictionVO>> predictMaintenanceNeeds(
            @Valid @RequestBody MaintenancePredictRequest request) {
        log.info("[棰勬祴鎬х淮鎶 寮€濮嬪垎鏋愮淮鎶ら渶姹傦紝deviceId={}, predictionDays={}",
            request.getDeviceId(), request.getPredictionDays());

        try {
            List<MaintenancePredictionVO> result = deviceHealthService.predictMaintenanceNeeds(request);
            log.info("[棰勬祴鎬х淮鎶 鍒嗘瀽瀹屾垚锛宒eviceId={}, predictionCount={}",
                request.getDeviceId(), result.size());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[棰勬祴鎬х淮鎶 鍒嗘瀽寮傚父锛宒eviceId={}, error={}",
                request.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("MAINTENANCE_PREDICTION_ERROR", "棰勬祴鎬х淮鎶ゅ垎鏋愬け璐ワ細" + e.getMessage());
        }
    }

    /**
     * 鑾峰彇璁惧鍋ュ悍缁熻
     * <p>
     * 鑾峰彇鎵€鏈夎澶囩殑鍋ュ悍鐘舵€佺粺璁′俊鎭紝鍖呮嫭锛?
     * - 鍋ュ悍璁惧鏁伴噺
     * - 浜氬仴搴疯澶囨暟閲?
     * - 鏁呴殰璁惧鏁伴噺
     * - 绂荤嚎璁惧鏁伴噺
     * </p>
     *
     * @return 璁惧鍋ュ悍缁熻淇℃伅
     */
    @GetMapping("/statistics")
    @Operation(summary = "璁惧鍋ュ悍缁熻", description = "鑾峰彇鎵€鏈夎澶囩殑鍋ュ悍鐘舵€佺粺璁′俊鎭?)
    @PermissionCheck(value = {"ACCESS_MANAGER", "DEVICE_VIEW"}, description = "璁惧鍘嗗彶鏁版嵁鏌ョ湅")
    public ResponseDTO<Object> getDeviceHealthStatistics() {
        log.info("[璁惧鍋ュ悍缁熻] 寮€濮嬭幏鍙栫粺璁′俊鎭?);

        try {
            Object result = deviceHealthService.getDeviceHealthStatistics();
            log.info("[璁惧鍋ュ悍缁熻] 缁熻瀹屾垚");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[璁惧鍋ュ悍缁熻] 缁熻寮傚父锛宔rror={}", e.getMessage(), e);
            return ResponseDTO.error("DEVICE_HEALTH_STATISTICS_ERROR", "璁惧鍋ュ悍缁熻澶辫触锛? + e.getMessage());
        }
    }

    /**
     * 鑾峰彇璁惧鍋ュ悍鍘嗗彶鏁版嵁
     * <p>
     * 鑾峰彇鎸囧畾璁惧鐨勫巻鍙插仴搴锋暟鎹紝鐢ㄤ簬瓒嬪娍鍒嗘瀽
     * </p>
     *
     * @param deviceId 璁惧ID
     * @param startDate 寮€濮嬫椂闂?
     * @param endDate 缁撴潫鏃堕棿
     * @return 鍘嗗彶鍋ュ悍鏁版嵁
     */
    @GetMapping("/history/{deviceId}")
    @Operation(summary = "璁惧鍋ュ悍鍘嗗彶", description = "鑾峰彇璁惧鍘嗗彶鍋ュ悍鏁版嵁鐢ㄤ簬瓒嬪娍鍒嗘瀽")
    @PermissionCheck(value = {"ACCESS_MANAGER", "DEVICE_VIEW"}, description = "璁惧鍘嗗彶鏁版嵁鏌ョ湅")
    public ResponseDTO<PageResult<Object>> getDeviceHealthHistory(
            @Parameter(description = "璁惧ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "寮€濮嬫椂闂?)
            LocalDateTime startDate,
            @Parameter(description = "缁撴潫鏃堕棿")
            LocalDateTime endDate) {
        log.info("[璁惧鍋ュ悍鍘嗗彶] 鑾峰彇鍘嗗彶鏁版嵁锛宒eviceId={}, startDate={}, endDate={}",
            deviceId, startDate, endDate);

        try {
            PageResult<Object> result = deviceHealthService.getDeviceHealthHistory(deviceId, startDate, endDate);
            log.info("[璁惧鍋ュ悍鍘嗗彶] 鑾峰彇瀹屾垚锛宒eviceId={}, recordCount={}",
                deviceId, result.getTotal());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[璁惧鍋ュ悍鍘嗗彶] 鑾峰彇寮傚父锛宒eviceId={}, error={}",
                deviceId, e.getMessage(), e);
            return ResponseDTO.error("DEVICE_HEALTH_HISTORY_ERROR", "鑾峰彇璁惧鍋ュ悍鍘嗗彶澶辫触锛? + e.getMessage());
        }
    }
}