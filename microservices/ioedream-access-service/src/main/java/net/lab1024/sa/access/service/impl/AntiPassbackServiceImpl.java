package net.lab1024.sa.access.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.access.service.AntiPassbackService;
import net.lab1024.sa.common.access.entity.AccessRecordEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.http.HttpMethod;

/**
 * 闂ㄧ鍙嶆綔鍥炴湇鍔″疄锟?
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锟?
 * - 浣跨敤@Service娉ㄨВ鏍囪瘑鏈嶅姟锟?
 * - 浣跨敤@Transactional绠＄悊浜嬪姟
 * - 瀹炵幇@CircuitBreaker鐔旀柇鍜孈Retry閲嶈瘯鏈哄埗
 * - 浣跨敤Redis缂撳瓨鎻愬崌鎬ц兘
 * - 鏀寔鍥涚鍙嶆綔鍥炵畻娉曪細纭弽娼滃洖銆佽蒋鍙嶆綔鍥炪€佸尯鍩熷弽娼滃洖銆佸叏灞€鍙嶆綔锟?
 * </p>
 * <p>
 * P0绾у畨鍏ㄥ姛鑳斤細闃叉鍚屼竴涓汉鍦ㄧ煭鏃堕棿鍐呭湪澶氫釜闂ㄧ鐐归噸澶嶈繘锟?
 * 浼佷笟绾у疄鐜帮細楂樺苟鍙戙€侀珮鎬ц兘銆侀珮鍙敤
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AntiPassbackServiceImpl implements AntiPassbackService {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    // Redis缂撳瓨閿墠缂€
    private static final String ANTI_PASSBACK_PREFIX = "anti_passback:";
    private static final String USER_LAST_ACCESS_PREFIX = "user_last_access:";
    private static final String AREA_USER_COUNT_PREFIX = "area_user_count:";
    private static final String GLOBAL_USER_ACCESS_PREFIX = "global_user_access:";

    // 鍙嶆綔鍥炴椂闂寸獥鍙ｏ紙鍒嗛挓锟?
    private static final int ANTI_PASSBACK_TIME_WINDOW = 5;
    private static final int AREA_ANTI_PASSBACK_TIME_WINDOW = 10;
    private static final int GLOBAL_ANTI_PASSBACK_TIME_WINDOW = 15;

    // 缂撳瓨杩囨湡鏃堕棿锛堝垎閽燂級
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "performAntiPassbackCheckFallback")
    @TimeLimiter(name = "antiPassbackService")
    @RateLimiter(name = "antiPassbackService")
    public CompletableFuture<AntiPassbackResult> performAntiPassbackCheck(
            Long userId, Long deviceId, Long areaId, String verificationData) {

        log.info("[鍙嶆綔鍥炴鏌 寮€濮嬫锟?userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 鑾峰彇璁惧鍜屽尯鍩熶俊锟?
                DeviceEntity device = getDeviceById(deviceId);
                AreaEntity area = getAreaById(areaId);

                if (device == null || area == null) {
                    return AntiPassbackResult.failure("璁惧鎴栧尯鍩熶笉瀛樺湪");
                }

                // 鑾峰彇鍙嶆綔鍥炵瓥锟?
                String antiPassbackType = area.getAntiPassbackType();
                if (antiPassbackType == null) {
                    antiPassbackType = "NONE";
                }

                // 鎵ц瀵瑰簲绫诲瀷鐨勫弽娼滃洖妫€锟?
                AntiPassbackResult result = switch (antiPassbackType) {
                    case "HARD" -> checkHardAntiPassback(userId, deviceId, areaId, device, area);
                    case "SOFT" -> checkSoftAntiPassback(userId, deviceId, areaId, device, area);
                    case "AREA" -> checkAreaAntiPassback(userId, deviceId, areaId, device, area);
                    case "GLOBAL" -> checkGlobalAntiPassback(userId, deviceId, areaId, device, area);
                    default -> AntiPassbackResult.success("鏃犲弽娼滃洖闄愬埗");
                };

                log.info("[鍙嶆綔鍥炴鏌 妫€鏌ュ畬锟?userId={}, result={}", userId, result.isAllowed());
                return result;

            } catch (Exception e) {
                log.error("[鍙嶆綔鍥炴鏌 妫€鏌ュ紓锟?userId={}, error={}", userId, e.getMessage(), e);
                return AntiPassbackResult.failure("绯荤粺寮傚父锟? + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "checkAreaAntiPassbackFallback")
    @TimeLimiter(name = "antiPassbackService")
    public CompletableFuture<AntiPassbackResult> checkAreaAntiPassback(Long userId, Long areaId, String accessType) {
        log.info("[鍖哄煙鍙嶆綔鍥炴鏌 寮€濮嬫锟?userId={}, areaId={}, accessType={}", userId, areaId, accessType);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String cacheKey = ANTI_PASSBACK_PREFIX + "area:" + areaId + ":" + userId;

                // 鑾峰彇鐢ㄦ埛鍦ㄥ尯鍩熷唴鐨勬渶鍚庨€氳璁板綍
                String lastAccessKey = USER_LAST_ACCESS_PREFIX + areaId + ":" + userId;
                String lastAccessStr = (String) redisTemplate.opsForValue().get(lastAccessKey);

                if (lastAccessStr == null) {
                    // 棣栨杩涘叆鍖哄煙锛岃褰曞苟鍏佽
                    recordAreaAccess(userId, areaId, accessType);
                    return AntiPassbackResult.success("棣栨杩涘叆鍖哄煙");
                }

                // 瑙ｆ瀽鏈€鍚庨€氳淇℃伅
                String[] lastAccessInfo = lastAccessStr.split(":");
                String lastAccessType = lastAccessInfo[0];
                LocalDateTime lastAccessTime = LocalDateTime.parse(lastAccessInfo[1]);

                // 妫€鏌ユ椂闂寸獥锟?
                if (Duration.between(lastAccessTime, LocalDateTime.now()).toMinutes() > AREA_ANTI_PASSBACK_TIME_WINDOW) {
                    // 瓒呮椂锛屽厑璁搁€氳
                    recordAreaAccess(userId, areaId, accessType);
                    return AntiPassbackResult.success("瓒呮椂閲嶆柊杩涘叆");
                }

                // 妫€鏌ラ€氳绫诲瀷鏄惁鍖归厤
                if (isAccessTypeValid(lastAccessType, accessType)) {
                    recordAreaAccess(userId, areaId, accessType);
                    return AntiPassbackResult.success("閫氳绫诲瀷鍖归厤");
                } else {
                    return AntiPassbackResult.failure("鍙嶆綔鍥炶繚瑙勶細閫氳绫诲瀷涓嶅尮閰嶃€備笂涓€娆★細" + lastAccessType + "锛屾湰娆★細" + accessType);
                }

            } catch (Exception e) {
                log.error("[鍖哄煙鍙嶆綔鍥炴鏌 妫€鏌ュ紓锟?userId={}, areaId={}, error={}", userId, areaId, e.getMessage(), e);
                return AntiPassbackResult.failure("绯荤粺寮傚父锟? + e.getMessage());
            }
        });
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "updatePolicyFallback")
    public AntiPassbackResult updatePolicy(String deviceId, String policy) {
        try {
            log.info("[鍙嶆綔鍥炵瓥鐣ユ洿鏂癩 寮€濮嬫洿锟?deviceId={}, policy={}", deviceId, policy);

            DeviceEntity device = getDeviceById(String.valueOf(Long.parseLong(deviceId)));
            if (device == null) {
                return AntiPassbackResult.failure("璁惧涓嶅瓨锟?);
            }

            // 鏇存柊璁惧鍙嶆綔鍥炵瓥鐣ラ厤锟?
            Map<String, Object> extendedAttributes = new HashMap<>();
            if (device.getExtendedAttributes() != null) {
                // 瑙ｆ瀽鐜版湁鎵╁睍灞烇拷?
                extendedAttributes = parseExtendedAttributes(device.getExtendedAttributes());
            }

            extendedAttributes.put("antiPassbackPolicy", policy);
            device.setExtendedAttributes(serializeExtendedAttributes(extendedAttributes));

            updateDevice(device);

            // 娓呴櫎鐩稿叧缂撳瓨
            clearDeviceCache(Long.parseLong(deviceId));

            log.info("[鍙嶆綔鍥炵瓥鐣ユ洿鏂癩 鏇存柊鎴愬姛 deviceId={}", deviceId);
            return AntiPassbackResult.success("绛栫暐鏇存柊鎴愬姛");

        } catch (Exception e) {
            log.error("[鍙嶆綔鍥炵瓥鐣ユ洿鏂癩 鏇存柊澶辫触 deviceId={}, error={}", deviceId, e.getMessage(), e);
            return AntiPassbackResult.failure("鏇存柊澶辫触锟? + e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "recordAccessEventFallback")
    @Retry(name = "antiPassbackService", fallbackMethod = "recordAccessEventFallback")
    public void recordAccessEvent(Long userId, Long deviceId, Long areaId, boolean allowed, String reason) {
        try {
            log.debug("[鍙嶆綔鍥炰簨浠惰褰昡 璁板綍浜嬩欢 userId={}, deviceId={}, areaId={}, allowed={}",
                     userId, deviceId, areaId, allowed);

            // 鍒涘缓閫氳璁板綍
            AccessRecordEntity record = new AccessRecordEntity();
            record.setUserId(userId);
            record.setDeviceId(deviceId);
            record.setAreaId(areaId);
            record.setAccessResult(allowed ? 1 : 2);
            record.setAccessTime(LocalDateTime.now());

            accessRecordDao.insert(record);

            // 鏇存柊缂撳瓨
            updateAccessCache(userId, deviceId, areaId, allowed);

        } catch (Exception e) {
            log.error("[鍙嶆綔鍥炰簨浠惰褰昡 璁板綍澶辫触 userId={}, deviceId={}, error={}",
                     userId, deviceId, e.getMessage(), e);
            // 璁板綍澶辫触涓嶅簲璇ュ奖鍝嶄富瑕佷笟鍔℃祦绋嬶紝鎵€浠ヨ繖閲屽彧璁板綍鏃ュ織
        }
    }

    @Override
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "resetAntiPassbackFallback")
    public AntiPassbackResult resetAntiPassback(Long userId, Long deviceId, Long areaId) {
        try {
            log.info("[鍙嶆綔鍥為噸缃甝 寮€濮嬮噸锟?userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);

            // 娓呴櫎鐩稿叧缂撳瓨
            clearUserCache(userId, deviceId, areaId);

            // 璁板綍閲嶇疆浜嬩欢
            recordResetEvent(userId, deviceId, areaId);

            log.info("[鍙嶆綔鍥為噸缃甝 閲嶇疆鎴愬姛 userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);
            return AntiPassbackResult.success("閲嶇疆鎴愬姛");

        } catch (Exception e) {
            log.error("[鍙嶆綔鍥為噸缃甝 閲嶇疆澶辫触 userId={}, deviceId={}, areaId={}, error={}",
                     userId, deviceId, areaId, e.getMessage(), e);
            return AntiPassbackResult.failure("閲嶇疆澶辫触锟? + e.getMessage());
        }
    }

    // ==================== 绉佹湁鏂规硶锛氱‖鍙嶆綔锟?====================

    /**
     * 纭弽娼滃洖妫€锟?
     * 涓ユ牸绂佹鍦ㄦ椂闂寸獥鍙ｅ唴閲嶅閫氳
     */
    private AntiPassbackResult checkHardAntiPassback(Long userId, Long deviceId, Long areaId,
                                                   DeviceEntity device, AreaEntity area) {
        String cacheKey = ANTI_PASSBACK_PREFIX + "hard:" + userId;

        // 妫€鏌ユ渶杩戦€氳璁板綍
        String lastAccessStr = (String) redisTemplate.opsForValue().get(cacheKey);
        if (lastAccessStr != null) {
            LocalDateTime lastAccessTime = LocalDateTime.parse(lastAccessStr);
            if (Duration.between(lastAccessTime, LocalDateTime.now()).toMinutes() < ANTI_PASSBACK_TIME_WINDOW) {
                return AntiPassbackResult.failure("纭弽娼滃洖杩濊锛氬湪鏃堕棿绐楀彛鍐呯姝㈤噸澶嶉€氳");
            }
        }

        // 璁板綍褰撳墠閫氳
        redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString(),
                                       Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        return AntiPassbackResult.success("纭弽娼滃洖妫€鏌ラ€氳繃");
    }

    // ==================== 绉佹湁鏂规硶锛氳蒋鍙嶆綔锟?====================

    /**
     * 杞弽娼滃洖妫€锟?
     * 鍏佽閫氳浣嗚褰曞紓锟?
     */
    private AntiPassbackResult checkSoftAntiPassback(Long userId, Long deviceId, Long areaId,
                                                   DeviceEntity device, AreaEntity area) {
        String cacheKey = ANTI_PASSBACK_PREFIX + "soft:" + userId;

        // 妫€鏌ユ渶杩戦€氳璁板綍
        String lastAccessStr = (String) redisTemplate.opsForValue().get(cacheKey);
        boolean isException = false;

        if (lastAccessStr != null) {
            LocalDateTime lastAccessTime = LocalDateTime.parse(lastAccessStr);
            if (Duration.between(lastAccessTime, LocalDateTime.now()).toMinutes() < ANTI_PASSBACK_TIME_WINDOW) {
                isException = true;
                log.warn("[杞弽娼滃洖] 妫€娴嬪埌閲嶅閫氳 userId={}, deviceId={}, lastTime={}",
                        userId, deviceId, lastAccessTime);

                // 璁板綍寮傚父浜嬩欢
                recordSoftException(userId, deviceId, areaId);
            }
        }

        // 璁板綍褰撳墠閫氳
        redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString(),
                                       Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        String message = isException ? "杞弽娼滃洖锛氭娴嬪埌閲嶅閫氳浣嗗厑璁搁€氳繃" : "杞弽娼滃洖妫€鏌ラ€氳繃";
        return AntiPassbackResult.success(message);
    }

    // ==================== 绉佹湁鏂规硶锛氬叏灞€鍙嶆綔锟?====================

    /**
     * 鍏ㄥ眬鍙嶆綔鍥炴锟?
     * 璺ㄥ尯鍩熴€佽法璁惧鐨勫叏灞€鍙嶆綔鍥炴锟?
     */
    private AntiPassbackResult checkGlobalAntiPassback(Long userId, Long deviceId, Long areaId,
                                                     DeviceEntity device, AreaEntity area) {
        String cacheKey = GLOBAL_USER_ACCESS_PREFIX + userId;

        // 鑾峰彇鐢ㄦ埛鍏ㄥ眬鏈€杩戦€氳璁板綍
        List<String> recentAccesses = (List<String>) redisTemplate.opsForValue().get(cacheKey);

        if (recentAccesses != null && !recentAccesses.isEmpty()) {
            // 妫€鏌ユ槸鍚︽湁鍦ㄦ椂闂寸獥鍙ｅ唴鐨勯€氳璁板綍
            LocalDateTime now = LocalDateTime.now();
            for (String accessStr : recentAccesses) {
                String[] accessInfo = accessStr.split(":");
                LocalDateTime accessTime = LocalDateTime.parse(accessInfo[0]);

                if (Duration.between(accessTime, now).toMinutes() < GLOBAL_ANTI_PASSBACK_TIME_WINDOW) {
                    return AntiPassbackResult.failure("鍏ㄥ眬鍙嶆綔鍥炶繚瑙勶細鍦ㄥ叏灞€鏃堕棿绐楀彛鍐呯姝㈠鍖哄煙閫氳");
                }
            }
        }

        // 璁板綍褰撳墠閫氳
        String currentAccess = LocalDateTime.now() + ":" + areaId + ":" + deviceId;
        if (recentAccesses == null) {
            recentAccesses = new ArrayList<>();
        }
        recentAccesses.add(currentAccess);

        // 淇濇寔鏈€锟?0鏉¤锟?
        if (recentAccesses.size() > 10) {
            recentAccesses = recentAccesses.subList(recentAccesses.size() - 10, recentAccesses.size());
        }

        redisTemplate.opsForValue().set(cacheKey, recentAccesses,
                                       Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        return AntiPassbackResult.success("鍏ㄥ眬鍙嶆綔鍥炴鏌ラ€氳繃");
    }

    // ==================== 绉佹湁鏂规硶锛氳緟鍔╁姛锟?====================

    /**
     * 妫€鏌ラ€氳绫诲瀷鏄惁鏈夋晥
     */
    private boolean isAccessTypeValid(String lastType, String currentType) {
        // 瀹炵幇杩涘嚭绫诲瀷鍖归厤閫昏緫
        // 渚嬪锛欼N鍜孫UT搴旇浜ゆ浛鍑虹幇
        if ("IN".equals(lastType) && "OUT".equals(currentType)) {
            return true;
        }
        if ("OUT".equals(lastType) && "IN".equals(currentType)) {
            return true;
        }
        return false;
    }

    /**
     * 璁板綍鍖哄煙閫氳
     */
    private void recordAreaAccess(Long userId, Long areaId, String accessType) {
        String key = USER_LAST_ACCESS_PREFIX + areaId + ":" + userId;
        String value = accessType + ":" + LocalDateTime.now();

        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(CACHE_EXPIRE_MINUTES));

        // 鏇存柊鍖哄煙浜烘暟缁熻
        updateUserCountInArea(areaId, accessType);
    }

    /**
     * 鏇存柊鍖哄煙鐢ㄦ埛浜烘暟
     */
    private void updateUserCountInArea(Long areaId, String accessType) {
        String countKey = AREA_USER_COUNT_PREFIX + areaId;

        if ("IN".equals(accessType)) {
            redisTemplate.opsForValue().increment(countKey);
        } else if ("OUT".equals(accessType)) {
            redisTemplate.opsForValue().decrement(countKey);
        }

        redisTemplate.expire(countKey, Duration.ofHours(24));
    }

    /**
     * 娓呴櫎璁惧缂撳瓨
     */
    private void clearDeviceCache(Long deviceId) {
        Set<String> keys = redisTemplate.keys(ANTI_PASSBACK_PREFIX + "*:" + deviceId + "*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 娓呴櫎鐢ㄦ埛缂撳瓨
     */
    private void clearUserCache(Long userId, Long deviceId, Long areaId) {
        Set<String> keys = redisTemplate.keys(ANTI_PASSBACK_PREFIX + "*:" + userId + "*");
        keys.addAll(redisTemplate.keys(USER_LAST_ACCESS_PREFIX + "*:" + userId));
        keys.addAll(redisTemplate.keys(GLOBAL_USER_ACCESS_PREFIX + userId));

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 鏇存柊璁块棶缂撳瓨
     */
    private void updateAccessCache(Long userId, Long deviceId, Long areaId, boolean allowed) {
        if (allowed) {
            // 鏇存柊鎴愬姛璁块棶鐨勭紦锟?
            String key = USER_LAST_ACCESS_PREFIX + areaId + ":" + userId;
            String value = "IN:" + LocalDateTime.now();
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(CACHE_EXPIRE_MINUTES));
        }
    }

    /**
     * 璁板綍杞弽娼滃洖寮傚父
     */
    private void recordSoftException(Long userId, Long deviceId, Long areaId) {
        log.warn("[杞弽娼滃洖寮傚父] userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);

        // 鍙互娣诲姞鍒板紓甯歌〃鎴栧彂閫佸憡锟?
        String exceptionKey = ANTI_PASSBACK_PREFIX + "soft_exception:" + userId;
        String exceptionData = LocalDateTime.now() + ":" + deviceId + ":" + areaId;

        redisTemplate.opsForList().rightPush(exceptionKey, exceptionData);
        redisTemplate.expire(exceptionKey, Duration.ofHours(24));
    }

    /**
     * 璁板綍閲嶇疆浜嬩欢
     */
    private void recordResetEvent(Long userId, Long deviceId, Long areaId) {
        String resetKey = ANTI_PASSBACK_PREFIX + "reset:" + userId;
        String resetData = LocalDateTime.now() + ":" + deviceId + ":" + areaId;

        redisTemplate.opsForList().rightPush(resetKey, resetData);
        redisTemplate.expire(resetKey, Duration.ofHours(24));
    }

    /**
     * 瑙ｆ瀽鎵╁睍灞烇拷?
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseExtendedAttributes(String extendedAttributes) {
        try {
            // 杩欓噷搴旇浣跨敤JSON瑙ｆ瀽锛岀畝鍖栧疄锟?
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 搴忓垪鍖栨墿灞曞睘锟?
     */
    private String serializeExtendedAttributes(Map<String, Object> attributes) {
        try {
            // 杩欓噷搴旇浣跨敤JSON搴忓垪鍖栵紝绠€鍖栧疄锟?
            return attributes.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    // ==================== 鐔旀柇闄嶇骇鏂规硶 ====================

    public CompletableFuture<AntiPassbackResult> performAntiPassbackCheckFallback(
            Long userId, Long deviceId, Long areaId, String verificationData, Exception e) {
        log.warn("[鍙嶆綔鍥炴鏌 鐔旀柇闄嶇骇 userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage());
        return CompletableFuture.completedFuture(AntiPassbackResult.failure("鏈嶅姟鏆傛椂涓嶅彲鐢紝宸查檷绾у锟?));
    }

    public CompletableFuture<AntiPassbackResult> checkAreaAntiPassbackFallback(
            Long userId, Long areaId, String accessType, Exception e) {
        log.warn("[鍖哄煙鍙嶆綔鍥炴鏌 鐔旀柇闄嶇骇 userId={}, areaId={}, error={}", userId, areaId, e.getMessage());
        return CompletableFuture.completedFuture(AntiPassbackResult.failure("鍖哄煙鍙嶆綔鍥炴鏌ユ湇鍔℃殏鏃朵笉鍙敤"));
    }

    public AntiPassbackResult updatePolicyFallback(String deviceId, String policy, Exception e) {
        log.warn("[鍙嶆綔鍥炵瓥鐣ユ洿鏂癩 鐔旀柇闄嶇骇 deviceId={}, error={}", deviceId, e.getMessage());
        return AntiPassbackResult.failure("绛栫暐鏇存柊鏈嶅姟鏆傛椂涓嶅彲锟?);
    }

    public void recordAccessEventFallback(Long userId, Long deviceId, Long areaId, boolean allowed, String reason, Exception e) {
        log.error("[鍙嶆綔鍥炰簨浠惰褰昡 鐔旀柇闄嶇骇 userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage());
        // 浜嬩欢璁板綍澶辫触涓嶅簲璇ュ奖鍝嶄富娴佺▼
    }

    public AntiPassbackResult resetAntiPassbackFallback(Long userId, Long deviceId, Long areaId, Exception e) {
        log.warn("[鍙嶆綔鍥為噸缃甝 鐔旀柇闄嶇骇 userId={}, deviceId={}, areaId={}, error={}", userId, deviceId, areaId, e.getMessage());
        return AntiPassbackResult.failure("閲嶇疆鏈嶅姟鏆傛椂涓嶅彲锟?);
    }

    // ==================== 棰勭暀鏂规硶 ====================

    @Override
    public Map<String, Object> getAntiPassbackStatistics() {
        // TODO: 瀹炵幇缁熻鍔熻兘
        return new HashMap<>();
    }

    @Override
    public AntiPassbackResult checkTimeWindowViolation(Long userId, Long deviceId, LocalDateTime accessTime) {
        // TODO: 瀹炵幇鏃堕棿绐楀彛杩濊妫€锟?
        return AntiPassbackResult.success("鍔熻兘寰呭疄锟?);
    }
}
