package net.lab1024.sa.access.manager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.access.domain.form.AntiPassbackPolicyForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackStatisticsVO;
import net.lab1024.sa.common.access.entity.AccessRecordEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.http.HttpMethod;

/**
 * 闂ㄧ鍙嶆綔鍥炵鐞嗗櫒
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锟?
 * - 浣跨敤Manager鍚庣紑鏍囪瘑涓氬姟缂栨帓锟?
 * - 澶勭悊澶嶆潅涓氬姟娴佺▼鍜屾暟鎹粍锟?
 * - 绾疛ava绫伙紝涓嶄娇鐢⊿pring娉ㄨВ锛園Component, @Service绛夛級
 * - 閫氳繃鏋勯€犲嚱鏁版敞鍏ヤ緷锟?
 * - 闆嗘垚Redis缂撳瓨鎻愬崌鎬ц兘
 * - 鎻愪緵缁熻鍒嗘瀽鍜屽喅绛栨敮锟?
 * </p>
 * <p>
 * 鑱岃矗锟?
 * - 鍙嶆綔鍥炵瓥鐣ョ锟?
 * - 杩濊鏁版嵁缁熻鍒嗘瀽
 * - 瀹炴椂鐩戞帶鍜屽憡锟?
 * - 鎬ц兘浼樺寲鍜岀紦瀛樼锟?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class AntiPassbackManager {

    private final AccessRecordDao accessRecordDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GatewayServiceClient gatewayServiceClient;

    // 缂撳瓨閿墠缂€
    private static final String POLICY_CACHE_PREFIX = "anti_passback_policy:";
    private static final String STATISTICS_CACHE_PREFIX = "anti_passback_stats:";
    private static final String VIOLATION_RECORD_PREFIX = "violation_record:";

    // 缂撳瓨杩囨湡鏃堕棿
    private static final long CACHE_EXPIRE_MINUTES = 30;
    private static final long STATISTICS_CACHE_EXPIRE_MINUTES = 15;

    /**
     * 鏋勯€犲嚱锟?
     */
    public AntiPassbackManager(AccessRecordDao accessRecordDao,
                              RedisTemplate<String, Object> redisTemplate,
                              GatewayServiceClient gatewayServiceClient) {
        this.accessRecordDao = accessRecordDao;
        this.redisTemplate = redisTemplate;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    /**
     * 閫氳繃缃戝叧鑾峰彇鍖哄煙淇℃伅锛堥€傞厤鍣ㄦ柟娉曪級
     *
     * @param areaId 鍖哄煙ID
     * @return 鍖哄煙瀹炰綋
     */
    private AreaEntity getAreaById(Long areaId) {
        try {
            ResponseDTO<AreaEntity> result = gatewayServiceClient.callCommonService(
                    "/api/v1/organization/area/" + areaId,
                    HttpMethod.GET,
                    null,
                    AreaEntity.class
            );
            return result != null && result.getOk() ? result.getData() : null;
        } catch (Exception e) {
            log.warn("[鍙嶆綔鍥炵鐞嗗櫒] 鑾峰彇鍖哄煙淇℃伅澶辫触, areaId={}", areaId, e);
            return null;
        }
    }

    /**
     * 閫氳繃缃戝叧鑾峰彇璁惧淇℃伅锛堥€傞厤鍣ㄦ柟娉曪級
     *
     * @param deviceId 璁惧ID
     * @return 璁惧瀹炰綋
     */
    private DeviceEntity getDeviceById(String deviceId) {
        try {
            ResponseDTO<DeviceEntity> result = gatewayServiceClient.callCommonService(
                    "/api/v1/organization/device/" + deviceId,
                    HttpMethod.GET,
                    null,
                    DeviceEntity.class
            );
            return result != null && result.getOk() ? result.getData() : null;
        } catch (Exception e) {
            log.warn("[鍙嶆綔鍥炵鐞嗗櫒] 鑾峰彇璁惧淇℃伅澶辫触, deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 鑾峰彇鍙嶆綔鍥炵粺璁℃暟锟?
     */
    public AntiPassbackStatisticsVO getAntiPassbackStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[鍙嶆綔鍥炵鐞嗗櫒] 鑾峰彇缁熻鏁版嵁 startTime={}, endTime={}", startTime, endTime);

        String cacheKey = STATISTICS_CACHE_PREFIX + startTime.toLocalDate() + "_" + endTime.toLocalDate();

        // 灏濊瘯浠庣紦瀛樿幏锟?
        AntiPassbackStatisticsVO cachedStats = (AntiPassbackStatisticsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStats != null) {
            return cachedStats;
        }

        // 鏌ヨ鏁版嵁搴撶粺璁℃暟锟?
        AntiPassbackStatisticsVO statistics = buildStatisticsFromDatabase(startTime, endTime);

        // 缂撳瓨缁撴灉
        redisTemplate.opsForValue().set(cacheKey, statistics,
                                       Duration.ofMinutes(STATISTICS_CACHE_EXPIRE_MINUTES));

        return statistics;
    }

    /**
     * 鍒嗘瀽鍙嶆綔鍥炶秼锟?
     */
    public Map<String, Object> analyzeAntiPassbackTrends(int days) {
        log.debug("[鍙嶆綔鍥炵鐞嗗櫒] 鍒嗘瀽瓒嬪娍 days={}", days);

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        Map<String, Object> trends = new HashMap<>();

        // 鏃ュ潎杩濊瓒嬪娍
        List<Map<String, Object>> dailyTrends = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = startTime.plusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            Long dailyViolations = countViolationsByTimeRange(dayStart, dayEnd);
            Long dailyTotalChecks = countTotalChecksByTimeRange(dayStart, dayEnd);

            Map<String, Object> dayTrend = new HashMap<>();
            dayTrend.put("date", dayStart.toLocalDate());
            dayTrend.put("violations", dailyViolations);
            dayTrend.put("totalChecks", dailyTotalChecks);
            dayTrend.put("violationRate", dailyTotalChecks > 0 ? (double) dailyViolations / dailyTotalChecks : 0.0);

            dailyTrends.add(dayTrend);
        }
        trends.put("dailyTrends", dailyTrends);

        // 楂橀闄╂椂娈靛垎锟?
        Map<String, Long> highRiskPeriods = analyzeHighRiskPeriods(startTime, endTime);
        trends.put("highRiskPeriods", highRiskPeriods);

        // 杩濊绫诲瀷鍒嗗竷
        Map<String, Long> violationTypeDistribution = analyzeViolationTypeDistribution(startTime, endTime);
        trends.put("violationTypeDistribution", violationTypeDistribution);

        return trends;
    }

    /**
     * 妫€鏌ュ尯鍩熷锟?
     */
    public boolean checkAreaCapacity(Long areaId) {
        log.debug("[鍙嶆綔鍥炵鐞嗗櫒] 妫€鏌ュ尯鍩熷锟?areaId={}", areaId);

        try {
            AreaEntity area = getAreaById(areaId);
            if (area == null || area.getMaxCapacity() == null) {
                return true; // 鏃犲閲忛檺锟?
            }

            String currentCountKey = "area_current_count:" + areaId;
            Integer currentCount = (Integer) redisTemplate.opsForValue().get(currentCountKey);

            if (currentCount == null) {
                // 濡傛灉缂撳瓨涓病鏈夛紝浠庢暟鎹簱璁＄畻
                currentCount = calculateCurrentAreaOccupancy(areaId);
                redisTemplate.opsForValue().set(currentCountKey, currentCount, Duration.ofMinutes(5));
            }

            return currentCount < area.getMaxCapacity();

        } catch (Exception e) {
            log.error("[鍙嶆綔鍥炵鐞嗗櫒] 妫€鏌ュ尯鍩熷閲忓紓锟?areaId={}, error={}", areaId, e.getMessage(), e);
            return true; // 寮傚父鏃跺厑璁搁€氳
        }
    }

    /**
     * 鏇存柊鍖哄煙浜烘暟
     */
    public void updateAreaOccupancy(Long areaId, String accessType) {
        log.debug("[鍙嶆綔鍥炵鐞嗗櫒] 鏇存柊鍖哄煙浜烘暟 areaId={}, accessType={}", areaId, accessType);

        String currentCountKey = "area_current_count:" + areaId;

        if ("IN".equals(accessType)) {
            redisTemplate.opsForValue().increment(currentCountKey);
        } else if ("OUT".equals(accessType)) {
            redisTemplate.opsForValue().decrement(currentCountKey);
        }

        // 璁剧疆杩囨湡鏃堕棿
        redisTemplate.expire(currentCountKey, Duration.ofMinutes(10));
    }

    /**
     * 璁板綍杩濊浜嬩欢
     */
    public void recordViolation(Long userId, Long deviceId, Long areaId, String violationType, String reason) {
        log.warn("[鍙嶆綔鍥炵鐞嗗櫒] 璁板綍杩濊 userId={}, deviceId={}, areaId={}, type={}",
                userId, deviceId, areaId, violationType);

        try {
            // 璁板綍鍒癛edis
            String violationKey = VIOLATION_RECORD_PREFIX + LocalDateTime.now().toLocalDate();
            String violationData = String.format("%d:%d:%d:%s:%s:%d",
                    userId, deviceId, areaId, violationType, reason, System.currentTimeMillis());

            redisTemplate.opsForList().rightPush(violationKey, violationData);
            redisTemplate.expire(violationKey, Duration.ofDays(7));

            // 妫€鏌ユ槸鍚﹂渶瑕佸彂閫佸憡锟?
            checkAndSendAlert(userId, violationType);

        } catch (Exception e) {
            log.error("[鍙嶆綔鍥炵鐞嗗櫒] 璁板綍杩濊寮傚父 userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 鑾峰彇鐢ㄦ埛鍙嶆綔鍥炲巻锟?
     */
    public Page<Map<String, Object>> getUserAntiPassbackHistory(Long userId, int page, int size) {
        log.debug("[鍙嶆綔鍥炵鐞嗗櫒] 鑾峰彇鐢ㄦ埛鍙嶆綔鍥炲巻锟?userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);

        // 鏌ヨ鐢ㄦ埛閫氳璁板綍
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(30);

        List<AccessRecordEntity> records = accessRecordDao.selectByUserIdAndTimeRange(
                userId, startTime, endTime, size * (page + 1));

        // 杞崲涓哄寘鍚弽娼滃洖淇℃伅鐨勫巻鍙茶锟?
        List<Map<String, Object>> history = records.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::buildAntiPassbackHistory)
                .collect(Collectors.toList());

        return new PageImpl<>(history, pageable, records.size());
    }

    /**
     * 娓呯悊杩囨湡缂撳瓨
     */
    public void cleanExpiredCache() {
        log.debug("[鍙嶆綔鍥炵鐞嗗櫒] 娓呯悊杩囨湡缂撳瓨");

        try {
            Set<String> keys = redisTemplate.keys(POLICY_CACHE_PREFIX + "*");
            Set<String> statsKeys = redisTemplate.keys(STATISTICS_CACHE_PREFIX + "*");

            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(keys);
            allKeys.addAll(statsKeys);

            if (!allKeys.isEmpty()) {
                redisTemplate.delete(allKeys);
                log.info("[鍙嶆綔鍥炵鐞嗗櫒] 娓呯悊杩囨湡缂撳瓨瀹屾垚锛屾竻鐞唟}涓紦锟?, allKeys.size());
            }

        } catch (Exception e) {
            log.error("[鍙嶆綔鍥炵鐞嗗櫒] 娓呯悊杩囨湡缂撳瓨寮傚父 error={}", e.getMessage(), e);
        }
    }

    // ==================== 绉佹湁鏂规硶 ====================

    /**
     * 浠庢暟鎹簱鏋勫缓缁熻鏁版嵁
     */
    private AntiPassbackStatisticsVO buildStatisticsFromDatabase(LocalDateTime startTime, LocalDateTime endTime) {
        // 鏌ヨ鎬绘鏌ユ锟?
        Long totalCheckCount = countTotalChecksByTimeRange(startTime, endTime);

        // 鏌ヨ鎴愬姛娆℃暟
        Long successCount = countSuccessChecksByTimeRange(startTime, endTime);

        // 鏌ヨ鍚勭被杩濊娆℃暟
        Long hardViolations = countViolationsByType(startTime, endTime, "HARD");
        Long softExceptions = countViolationsByType(startTime, endTime, "SOFT");
        Long areaViolations = countViolationsByType(startTime, endTime, "AREA");
        Long globalViolations = countViolationsByType(startTime, endTime, "GLOBAL");

        // 璁＄畻澶辫触娆℃暟
        Long failureCount = totalCheckCount - successCount;

        // 璁＄畻鎴愬姛锟?
        Double successRate = totalCheckCount > 0 ? (double) successCount / totalCheckCount : 0.0;

        return AntiPassbackStatisticsVO.builder()
                .statisticsTime(LocalDateTime.now())
                .totalCheckCount(totalCheckCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successRate(successRate * 100)
                .hardAntiPassbackViolations(hardViolations)
                .softAntiPassbackExceptions(softExceptions)
                .areaAntiPassbackViolations(areaViolations)
                .globalAntiPassbackViolations(globalViolations)
                .areaStatisticsList(buildAreaStatistics(startTime, endTime))
                .deviceStatisticsList(buildDeviceStatistics(startTime, endTime))
                .timeDistribution(buildTimeDistribution(startTime, endTime))
                .build();
    }

    /**
     * 鏋勫缓鍖哄煙缁熻
     */
    private List<AntiPassbackStatisticsVO.AreaStatistics> buildAreaStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: 瀹炵幇鍖哄煙缁熻鏌ヨ
        return new ArrayList<>();
    }

    /**
     * 鏋勫缓璁惧缁熻
     */
    private List<AntiPassbackStatisticsVO.DeviceStatistics> buildDeviceStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: 瀹炵幇璁惧缁熻鏌ヨ
        return new ArrayList<>();
    }

    /**
     * 鏋勫缓鏃堕棿鍒嗗竷
     */
    private Map<String, Long> buildTimeDistribution(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Long> distribution = new HashMap<>();

        // 鎸夊皬鏃剁粺锟?
        for (int hour = 0; hour < 24; hour++) {
            Long count = countChecksByHour(startTime, endTime, hour);
            distribution.put(String.format("%02d:00", hour), count);
        }

        return distribution;
    }

    /**
     * 鏋勫缓鍙嶆綔鍥炲巻鍙茶锟?
     */
    private Map<String, Object> buildAntiPassbackHistory(AccessRecordEntity record) {
        Map<String, Object> history = new HashMap<>();

        history.put("recordId", record.getRecordId());
        history.put("accessTime", record.getAccessTime());
        history.put("accessResult", record.getAccessResult());
        history.put("accessType", record.getAccessType());
        history.put("verifyMethod", record.getVerifyMethod());
        history.put("deviceId", record.getDeviceId());
        history.put("areaId", record.getAreaId());

        // 鑾峰彇璁惧淇℃伅
        DeviceEntity device = getDeviceById(record.getDeviceId());
        if (device != null) {
            history.put("deviceName", device.getDeviceName());
            history.put("deviceLocation", device.getDeviceLocation());
        }

        // 鑾峰彇鍖哄煙淇℃伅
        AreaEntity area = getAreaById(record.getAreaId());
        if (area != null) {
            history.put("areaName", area.getAreaName());
        }

        return history;
    }

    /**
     * 妫€鏌ュ苟鍙戦€佸憡锟?
     */
    private void checkAndSendAlert(Long userId, String violationType) {
        String alertKey = "violation_alert:" + userId;

        // 妫€鏌ユ渶杩戠殑杩濊娆℃暟
        List<String> recentViolations = redisTemplate.opsForList().range(
                VIOLATION_RECORD_PREFIX + LocalDateTime.now().toLocalDate(), 0, -1);

        if (recentViolations.size() >= 5) { // 杩濊5娆¤Е鍙戝憡锟?
            log.warn("[鍙嶆綔鍥炵鐞嗗櫒] 鐢ㄦ埛杩濊娆℃暟杩囧锛岃Е鍙戝憡锟?userId={}, count={}", userId, recentViolations.size());

            // 鍙戦€佸憡璀︼紙杩欓噷鍙互闆嗘垚鍛婅绯荤粺锟?
            sendAlert(userId, violationType, recentViolations.size());
        }
    }

    /**
     * 鍙戦€佸憡锟?
     */
    private void sendAlert(Long userId, String violationType, int violationCount) {
        // TODO: 瀹炵幇鍛婅鍙戦€侀€昏緫
        log.info("[鍙嶆綔鍥炵鐞嗗櫒] 鍙戦€佸憡锟?userId={}, type={}, count={}", userId, violationType, violationCount);
    }

    /**
     * 璁＄畻褰撳墠鍖哄煙鍗犵敤浜烘暟
     */
    private Integer calculateCurrentAreaOccupancy(Long areaId) {
        // 鏌ヨ鏈€杩戠殑杩涘叆鍜岀寮€璁板綍
        // TODO: 瀹炵幇鏇寸簿纭殑浜烘暟璁＄畻
        return 0;
    }

    // 鏁版嵁鏌ヨ杈呭姪鏂规硶锛堢畝鍖栧疄鐜帮紝瀹為檯搴旇浣跨敤SQL锟?
    private Long countTotalChecksByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return 0L; // TODO: 瀹炵幇鏁版嵁搴撴煡锟?
    }

    private Long countSuccessChecksByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return 0L; // TODO: 瀹炵幇鏁版嵁搴撴煡锟?
    }

    private Long countViolationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return 0L; // TODO: 瀹炵幇鏁版嵁搴撴煡锟?
    }

    private Long countViolationsByType(LocalDateTime startTime, LocalDateTime endTime, String type) {
        return 0L; // TODO: 瀹炵幇鏁版嵁搴撴煡锟?
    }

    private Long countChecksByHour(LocalDateTime startTime, LocalDateTime endTime, int hour) {
        return 0L; // TODO: 瀹炵幇鏁版嵁搴撴煡锟?
    }

    private Map<String, Long> analyzeHighRiskPeriods(LocalDateTime startTime, LocalDateTime endTime) {
        return new HashMap<>(); // TODO: 瀹炵幇楂橀闄╂椂娈靛垎锟?
    }

    private Map<String, Long> analyzeViolationTypeDistribution(LocalDateTime startTime, LocalDateTime endTime) {
        return new HashMap<>(); // TODO: 瀹炵幇杩濊绫诲瀷鍒嗗竷鍒嗘瀽
    }
}
