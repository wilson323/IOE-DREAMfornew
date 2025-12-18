package net.lab1024.sa.access.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.AntiPassbackService;
import net.lab1024.sa.common.dto.ResponseDTO;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 闂ㄧ鍙嶆綔鍥炴帶鍒跺櫒
 * <p>
 * 鍐呭瓨浼樺寲璁捐锟?
 * - 浣跨敤寮傛澶勭悊锛屾彁楂樺苟鍙戞€ц兘
 * - 鍚堢悊鐨勫弬鏁伴獙璇侊紝閬垮厤鍐呭瓨婧㈠嚭
 * - 鐔旀柇鍣ㄤ繚鎶わ紝闃叉绾ц仈鏁呴殰
 * - 鎵归噺鎿嶄綔鏀寔锛屽噺灏戠綉缁滃紑閿€
 * </p>
 * <p>
 * 涓氬姟鍦烘櫙锟?
 * - 纭弽娼滃洖锛氶槻姝㈤噸澶嶅埛鍗¤繘锟?
 * - 杞弽娼滃洖锛氬厑璁搁噸澶嶅埛鍗′絾璁板綍寮傚父
 * - 鍖哄煙鍙嶆綔鍥烇細闃叉缁曡杩涘叆
 * - 鍏ㄥ眬鍙嶆綔鍥烇細璺ㄥ尯鍩熻仈鍔ㄦ锟?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@PermissionCheck(value = "ACCESS_MANAGE", description = "闂ㄧ鍙嶆綔鍥炴ā鍧楁潈闄?)
@RequestMapping("/api/v1/access/anti-passback")
@Tag(name = "闂ㄧ鍙嶆綔鍥?, description = "鍙嶆綔鍥炴鏌ャ€侀厤缃鐞嗐€佸紓甯稿鐞嗐€佺粺璁″垎鏋愮瓑API")
@Validated
@CircuitBreaker(name = "antiPassbackController")
public class AntiPassbackController {

    @Resource
    private AntiPassbackService antiPassbackService;

    // ==================== 鍙嶆綔鍥為獙锟?====================

    /**
     * 鎵ц鍙嶆綔鍥炴锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鎵ц鍙嶆綔鍥炴鏌?,
        description = "妫€鏌ョ敤鎴锋槸鍚﹀瓨鍦ㄦ綔鍥炶涓猴紝鏀寔澶氱鍙嶆綔鍥炴ā寮?
    )
    @PostMapping("/check")
    @PermissionCheck(value = {"ACCESS_OPERATOR", "ACCESS_MANAGER"}, description = "鎵ц鍙嶆綔鍥炴鏌?)
    public CompletableFuture<ResponseDTO<AntiPassbackService.AntiPassbackResult>> performAntiPassbackCheck(
            @Parameter(description = "鐢ㄦ埛ID", required = true, example = "1001")
            @RequestParam @NotNull Long userId,
            @Parameter(description = "璁惧ID", required = true, example = "1001")
            @RequestParam @NotNull Long deviceId,
            @Parameter(description = "鍖哄煙ID", required = true, example = "1001")
            @RequestParam @NotNull Long areaId,
            @Parameter(description = "楠岃瘉鏁版嵁", example = "{\"cardId\":\"CARD001\",\"faceData\":\"...\"}")
            @RequestParam @NotNull String verificationData
    ) {
        log.info("[鍙嶆綔鍥瀅 鎵ц鍙嶆綔鍥炴鏌? userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);
        return antiPassbackService.performAntiPassbackCheck(userId, deviceId, areaId, verificationData);
    }

    /**
     * 妫€鏌ュ尯鍩熷弽娼滃洖
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "妫€鏌ュ尯鍩熷弽娼滃洖",
        description = "妫€鏌ョ敤鎴锋槸鍚︿粠鍖哄煙A杩涘叆鍚庢湭浠庡尯鍩烞绂诲紑"
    )
    @PostMapping("/area-check")
    @PermissionCheck(value = {"ACCESS_OPERATOR", "ACCESS_MANAGER"}, description = "妫€鏌ュ尯鍩熷弽娼滃洖")
    public CompletableFuture<ResponseDTO<AntiPassbackService.AntiPassbackResult>> checkAreaAntiPassback(
            @Parameter(description = "鐢ㄦ埛ID", required = true, example = "1001")
            @RequestParam @NotNull Long userId,
            @Parameter(description = "杩涘叆鍖哄煙ID", required = true, example = "1001")
            @RequestParam @NotNull Long entryAreaId,
            @Parameter(description = "绂诲紑鍖哄煙ID", required = true, example = "1002")
            @RequestParam @NotNull Long exitAreaId,
            @Parameter(description = "杩涘嚭鏂瑰悜", required = true, example = "in")
            @RequestParam @NotNull String direction
    ) {
        log.info("[鍙嶆綔鍥瀅 妫€鏌ュ尯鍩熷弽娼滃洖, userId={}, entryAreaId={}, exitAreaId={}, direction={}",
                 userId, entryAreaId, exitAreaId, direction);
        return antiPassbackService.checkAreaAntiPassback(userId, entryAreaId, exitAreaId, direction);
    }

    // ==================== 鍙嶆綔鍥為厤缃锟?====================

    /**
     * 璁剧疆璁惧鍙嶆綔鍥炵瓥锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "璁剧疆璁惧鍙嶆綔鍥炵瓥鐣?,
        description = "涓鸿澶囬厤缃弽娼滃洖绛栫暐鍜屽弬鏁?
    )
    @PostMapping("/{deviceId}/policy")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "绠＄悊鎿嶄綔")
    public CompletableFuture<ResponseDTO<Void>> setAntiPassbackPolicy(
            @Parameter(description = "璁惧ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "鍙嶆綔鍥炵被鍨?, required = true, example = "hard")
            @RequestParam @NotNull String antiPassbackType,
            @Parameter(description = "閰嶇疆鍙傛暟", required = true)
            @RequestBody @NotNull Map<String, Object> config
    ) {
        log.info("[鍙嶆綔鍥瀅 璁剧疆璁惧鍙嶆綔鍥炵瓥鐣? deviceId={}, antiPassbackType={}, config={}",
                 deviceId, antiPassbackType, config);
        return antiPassbackService.setAntiPassbackPolicy(deviceId, antiPassbackType, config);
    }

    /**
     * 鑾峰彇璁惧鍙嶆綔鍥炵瓥锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鑾峰彇璁惧鍙嶆綔鍥炵瓥鐣?,
        description = "鏌ヨ璁惧鐨勫弽娼滃洖绛栫暐閰嶇疆"
    )
    @GetMapping("/{deviceId}/policy")
    @PermissionCheck(value = {"ACCESS_MANAGER", "ACCESS_OPERATOR"}, description = "绠＄悊鎴栨搷浣滄潈闄?)
    public CompletableFuture<ResponseDTO<Object>> getAntiPassbackPolicy(
            @Parameter(description = "璁惧ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[鍙嶆綔鍥瀅 鑾峰彇璁惧鍙嶆綔鍥炵瓥鐣? deviceId={}", deviceId);
        return antiPassbackService.getAntiPassbackPolicy(deviceId);
    }

    /**
     * 鏇存柊鍙嶆綔鍥為厤锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鏇存柊鍙嶆綔鍥為厤缃?,
        description = "鏇存柊璁惧鐨勫弽娼滃洖閰嶇疆鍙傛暟"
    )
    @PutMapping("/{deviceId}/config")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "绠＄悊鎿嶄綔")
    public CompletableFuture<ResponseDTO<Void>> updateAntiPassbackConfig(
            @Parameter(description = "璁惧ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "閰嶇疆鍙傛暟", required = true)
            @RequestBody @NotNull Map<String, Object> config
    ) {
        log.info("[鍙嶆綔鍥瀅 鏇存柊鍙嶆綔鍥為厤缃? deviceId={}, config={}", deviceId, config);
        return antiPassbackService.updateAntiPassbackConfig(deviceId, config);
    }

    // ==================== 鍙嶆綔鍥炶褰曠锟?====================

    /**
     * 璁板綍閫氳浜嬩欢
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "璁板綍閫氳浜嬩欢",
        description = "璁板綍鐢ㄦ埛閫氳浜嬩欢锛岀敤浜庡弽娼滃洖鍒嗘瀽"
    )
    @PostMapping("/record-access")
    @PreAuthorize("hasRole('ACCESS_OPERATOR') or hasRole('ACCESS_MANAGER')")
    public CompletableFuture<ResponseDTO<Void>> recordAccessEvent(
            @Parameter(description = "鐢ㄦ埛ID", required = true, example = "1001")
            @RequestParam @NotNull Long userId,
            @Parameter(description = "璁惧ID", required = true, example = "1001")
            @RequestParam @NotNull Long deviceId,
            @Parameter(description = "鍖哄煙ID", required = true, example = "1001")
            @RequestParam @NotNull Long areaId,
            @Parameter(description = "杩涘嚭鏂瑰悜", required = true, example = "in")
            @RequestParam @NotNull String direction,
            @Parameter(description = "楠岃瘉鏁版嵁", required = true)
            @RequestParam @NotNull String verificationData,
            @Parameter(description = "閫氳缁撴灉", required = true, example = "true")
            @RequestParam @NotNull Boolean result
    ) {
        log.info("[鍙嶆綔鍥瀅 璁板綍閫氳浜嬩欢, userId={}, deviceId={}, areaId={}, direction={}, result={}",
                 userId, deviceId, areaId, direction, result);
        return antiPassbackService.recordAccessEvent(userId, deviceId, areaId, direction, verificationData, result);
    }

    /**
     * 鑾峰彇鐢ㄦ埛鍙嶆綔鍥炵姸锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鑾峰彇鐢ㄦ埛鍙嶆綔鍥炵姸鎬?,
        description = "鏌ヨ鐢ㄦ埛鐨勫弽娼滃洖鐘舵€佷俊鎭?
    )
    @GetMapping("/user/{userId}/status")
    @PermissionCheck(value = {"ACCESS_MANAGER", "ACCESS_OPERATOR"}, description = "绠＄悊鎴栨搷浣滄潈闄?)
    public CompletableFuture<ResponseDTO<Object>> getUserAntiPassbackStatus(
            @Parameter(description = "鐢ㄦ埛ID", required = true, example = "1001")
            @PathVariable Long userId
    ) {
        log.info("[鍙嶆綔鍥瀅 鑾峰彇鐢ㄦ埛鍙嶆綔鍥炵姸鎬? userId={}", userId);
        return antiPassbackService.getUserAntiPassbackStatus(userId);
    }

    /**
     * 娓呯悊鐢ㄦ埛鍙嶆綔鍥炶锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "娓呯悊鐢ㄦ埛鍙嶆綔鍥炶褰?,
        description = "娓呯悊鐢ㄦ埛鐨勫弽娼滃洖璁板綍锛岄€氬父鍦ㄦ甯稿畬鎴愯繘鍑烘祦绋嬪悗璋冪敤"
    )
    @DeleteMapping("/user/{userId}/records")
    @PreAuthorize("hasRole('ACCESS_OPERATOR') or hasRole('ACCESS_MANAGER')")
    public CompletableFuture<ResponseDTO<Void>> clearUserAntiPassbackRecords(
            @Parameter(description = "鐢ㄦ埛ID", required = true, example = "1001")
            @PathVariable Long userId,
            @Parameter(description = "璁惧ID", required = true, example = "1001")
            @RequestParam @NotNull Long deviceId
    ) {
        log.info("[鍙嶆綔鍥瀅 娓呯悊鐢ㄦ埛鍙嶆綔鍥炶褰? userId={}, deviceId={}", userId, deviceId);
        return antiPassbackService.clearUserAntiPassbackRecords(userId, deviceId);
    }

    // ==================== 鍙嶆綔鍥炲紓甯稿锟?====================

    /**
     * 閲嶇疆鐢ㄦ埛鍙嶆綔鍥炵姸锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "閲嶇疆鐢ㄦ埛鍙嶆綔鍥炵姸锟?,
        description = "绠＄悊鍛樻墜鍔ㄩ噸缃敤鎴风殑鍙嶆綔鍥炵姸鎬侊紝鐢ㄤ簬瑙ｅ喅寮傚父鎯呭喌鎴栬锟?
    )
    @PostMapping("/user/{userId}/reset")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "绠＄悊鎿嶄綔")
    public CompletableFuture<ResponseDTO<Void>> resetUserAntiPassbackStatus(
            @Parameter(description = "鐢ㄦ埛ID", required = true, example = "1001")
            @PathVariable Long userId,
            @Parameter(description = "鎿嶄綔鍛業D", required = true, example = "1001")
            @RequestParam @NotNull Long operatorId,
            @Parameter(description = "閲嶇疆鍘熷洜", required = true, example = "璇姤寮傚父")
            @RequestParam @NotNull String reason
    ) {
        log.info("[鍙嶆綔鍥瀅 閲嶇疆鐢ㄦ埛鍙嶆綔鍥炵姸锟? userId={}, operatorId={}, reason={}", userId, operatorId, reason);
        return antiPassbackService.resetUserAntiPassbackStatus(userId, operatorId, reason);
    }

    /**
     * 鎵嬪姩澶勭悊鍙嶆綔鍥炲紓锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鎵嬪姩澶勭悊鍙嶆綔鍥炲紓锟?,
        description = "绠＄悊鍛樻墜鍔ㄥ鐞嗘娴嬪埌鐨勫弽娼滃洖寮傚父"
    )
    @PostMapping("/violation/handle")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "绠＄悊鎿嶄綔")
    public CompletableFuture<ResponseDTO<Void>> handleAntiPassbackViolation(
            @Parameter(description = "鐢ㄦ埛ID", required = true, example = "1001")
            @RequestParam @NotNull Long userId,
            @Parameter(description = "璁惧ID", required = true, example = "1001")
            @RequestParam @NotNull Long deviceId,
            @Parameter(description = "澶勭悊鏂瑰紡", required = true, example = "ignore")
            @RequestParam @NotNull String action,
            @Parameter(description = "澶勭悊澶囨敞")
            @RequestParam(required = false) String remarks
    ) {
        log.info("[鍙嶆綔鍥瀅 鎵嬪姩澶勭悊鍙嶆綔鍥炲紓锟? userId={}, deviceId={}, action={}", userId, deviceId, action);

        AntiPassbackService.AntiPassbackResult result = new AntiPassbackService.AntiPassbackResult();
        result.setPassed(false);
        result.setDenyReason("Manual violation handling: " + action);
        result.setViolationLevel("MANUAL");
        result.setRecommendedAction(action);

        return antiPassbackService.handleAntiPassbackViolation(userId, deviceId, result);
    }

    // ==================== 鍙嶆綔鍥炵粺璁″垎锟?====================

    /**
     * 鑾峰彇鍙嶆綔鍥炵粺璁′俊锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鑾峰彇鍙嶆綔鍥炵粺璁′俊锟?,
        description = "鑾峰彇鍙嶆綔鍥炵郴缁熺殑缁熻鏁版嵁鍜屽垎鏋愭姤锟?
    )
    @GetMapping("/statistics")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "绠＄悊鎿嶄綔")
    public CompletableFuture<ResponseDTO<Object>> getAntiPassbackStatistics(
            @Parameter(description = "寮€濮嬫椂闂?, example = "2025-01-01T00:00:00")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "缁撴潫鏃堕棿", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) String endTime
    ) {
        log.info("[鍙嶆綔鍥瀅 鑾峰彇鍙嶆綔鍥炵粺璁′俊鎭? startTime={}, endTime={}", startTime, endTime);
        return antiPassbackService.getAntiPassbackStatistics(startTime, endTime);
    }

    /**
     * 鑾峰彇鍙嶆綔鍥炲紓甯告姤锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鑾峰彇鍙嶆綔鍥炲紓甯告姤鍛?,
        description = "鏌ヨ鎸囧畾鏃堕棿娈靛唴鐨勫弽娼滃洖寮傚父鎶ュ憡"
    )
    @GetMapping("/violation-report")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "绠＄悊鎿嶄綔")
    public CompletableFuture<ResponseDTO<Object>> getAntiPassbackViolationReport(
            @Parameter(description = "璁惧ID", example = "1001")
            @RequestParam(required = false) Long deviceId,
            @Parameter(description = "寮€濮嬫椂闂?, example = "2025-01-01T00:00:00")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "缁撴潫鏃堕棿", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) String endTime
    ) {
        log.info("[鍙嶆綔鍥瀅 鑾峰彇鍙嶆綔鍥炲紓甯告姤鍛? deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime);
        return antiPassbackService.getAntiPassbackViolationReport(deviceId, startTime, endTime);
    }

    // ==================== 鎵归噺鎿嶄綔 ====================

    /**
     * 鎵归噺妫€鏌ュ弽娼滃洖鐘讹拷?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鎵归噺妫€鏌ュ弽娼滃洖鐘舵€?,
        description = "鍚屾椂妫€鏌ュ涓敤鎴风殑鍙嶆綔鍥炵姸鎬?
    )
    @PostMapping("/batch-check")
    @PermissionCheck(value = {"ACCESS_MANAGER", "ACCESS_OPERATOR"}, description = "绠＄悊鎴栨搷浣滄潈闄?)
    public CompletableFuture<ResponseDTO<Map<Long, Object>>> batchCheckAntiPassbackStatus(
            @Parameter(description = "鐢ㄦ埛ID鍒楄〃锛岀敤閫楀彿鍒嗛殧", required = true)
            @RequestParam @NotNull String userIds
    ) {
        log.info("[鍙嶆綔鍥瀅 鎵归噺妫€鏌ュ弽娼滃洖鐘舵€? userIds={}", userIds);
        return antiPassbackService.batchCheckAntiPassbackStatus(userIds);
    }

    /**
     * 鎵归噺娓呯悊鍙嶆綔鍥炶锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鎵归噺娓呯悊鍙嶆綔鍥炶褰?,
        description = "鍚屾椂娓呯悊澶氫釜鐢ㄦ埛鐨勫弽娼滃洖璁板綍"
    )
    @DeleteMapping("/batch-clear")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "绠＄悊鎿嶄綔")
    public CompletableFuture<ResponseDTO<Map<Long, Object>>> batchClearAntiPassbackRecords(
            @Parameter(description = "鐢ㄦ埛ID鍒楄〃锛岀敤閫楀彿鍒嗛殧", required = true)
            @RequestParam @NotNull String userIds
    ) {
        log.info("[鍙嶆綔鍥瀅 鎵归噺娓呯悊鍙嶆綔鍥炶褰? userIds={}", userIds);
        return antiPassbackService.batchClearAntiPassbackRecords(userIds);
    }

    // ==================== 閰嶇疆妯℃澘 ====================

    /**
     * 鑾峰彇鍙嶆綔鍥為厤缃ā锟?
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "鑾峰彇鍙嶆綔鍥為厤缃ā鏉?,
        description = "鑾峰彇鍚勭鍙嶆綔鍥炵被鍨嬬殑閰嶇疆妯℃澘"
    )
    @GetMapping("/config-templates")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "绠＄悊鎿嶄綔")
    public CompletableFuture<ResponseDTO<Map<String, Object>>> getAntiPassbackConfigTemplates() {
        log.info("[鍙嶆綔鍥瀅 鑾峰彇鍙嶆綔鍥為厤缃ā鏉?);

        Map<String, Object> templates = Map.of(
            "hard", Map.of(
                "type", "hard",
                "description", "纭弽娼滃洖锛氬畬鍏ㄧ姝㈤噸澶嶈繘锟?,
                "config", Map.of(
                    "enableDuplicateCheck", true,
                    "maxDuplicateInterval", 30,
                    "strictMode", true,
                    "autoLockDuration", 300
                )
            ),
            "soft", Map.of(
                "type", "soft",
                "description", "杞弽娼滃洖锛氬厑璁搁噸澶嶄絾璁板綍寮傚父",
                "config", Map.of(
                    "enableDuplicateCheck", true,
                    "maxDuplicateInterval", 60,
                    "strictMode", false,
                    "warningThreshold", 3
                )
            ),
            "area", Map.of(
                "type", "area",
                "description", "鍖哄煙鍙嶆綔鍥烇細鍖哄煙杩涘嚭閰嶅妫€锟?,
                "config", Map.of(
                    "enableAreaPairCheck", true,
                    "allowAreaCrossing", false,
                    "pairTimeout", 3600
                )
            ),
            "global", Map.of(
                "type", "global",
                "description", "鍏ㄥ眬鍙嶆綔鍥烇細璺ㄥ尯鍩熻仈鍔ㄦ锟?,
                "config", Map.of(
                    "enableGlobalCheck", true,
                    "globalTimeout", 7200,
                    "syncAcrossDevices", true
                )
            )
        );

        return CompletableFuture.completedFuture(ResponseDTO.ok(templates));
    }
}
