package net.lab1024.sa.access.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.BiometricTemplateDao;
import net.lab1024.sa.access.dao.BiometricConfigDao;
import net.lab1024.sa.access.dao.BiometricAuthRecordDao;
import net.lab1024.sa.access.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.access.domain.entity.BiometricAuthRecordEntity;
import net.lab1024.sa.access.domain.entity.BiometricConfigEntity;
import net.lab1024.sa.common.util.SmartBiometricUtil;
import net.lab1024.sa.common.util.SmartAESUtil;
import net.lab1024.sa.common.constant.SecurityConst;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 鐢熺墿璇嗗埆妯℃澘绠＄悊锟?
 * 璐熻矗澶嶆潅鐨勭敓鐗╄瘑鍒ā鏉夸笟鍔℃祦绋嬬紪锟?
 * 涓ユ牸閬靛惊CLAUDE.md鍏ㄥ眬鏋舵瀯瑙勮寖锛氱函Java绫伙紝涓嶄娇鐢⊿pring娉ㄨВ
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 * @updated 2025-12-17 绉婚櫎@Component娉ㄨВ锛屾敼涓虹函Java绫伙紝浣跨敤鏋勯€犲嚱鏁版敞锟?
 */
@Slf4j
public class BiometricTemplateManager {

    private final BiometricTemplateDao biometricTemplateDao;
    private final BiometricConfigDao biometricConfigDao;
    private final BiometricAuthRecordDao biometricAuthRecordDao;
    private final DeviceDao deviceDao;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缂撳瓨閿墠缂€
    private static final String CACHE_KEY_PREFIX = "biometric:template:";
    private static final String CACHE_KEY_USER_TEMPLATES = "biometric:user:templates:";
    private static final String LOCK_KEY_PREFIX = "biometric:lock:";

    /**
     * 鏋勯€犲嚱鏁版敞鍏ヤ緷锟?
     */
    public BiometricTemplateManager(BiometricTemplateDao biometricTemplateDao,
                                   BiometricConfigDao biometricConfigDao,
                                   BiometricAuthRecordDao biometricAuthRecordDao,
                                   DeviceDao deviceDao,
                                   RedisTemplate<String, Object> redisTemplate) {
        this.biometricTemplateDao = biometricTemplateDao;
        this.biometricConfigDao = biometricConfigDao;
        this.biometricAuthRecordDao = biometricAuthRecordDao;
        this.deviceDao = deviceDao;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 娉ㄥ唽鐢熺墿璇嗗埆妯℃澘
     *
     * @param userId 鐢ㄦ埛ID
     * @param biometricType 鐢熺墿璇嗗埆绫诲瀷
     * @param featureData 鐗瑰緛鏁版嵁
     * @param deviceId 璁惧ID
     * @return 娉ㄥ唽缁撴灉
     */
    public BiometricTemplateEntity registerTemplate(Long userId, Integer biometricType,
                                                   byte[] featureData, String deviceId) {
        log.info("[鐢熺墿璇嗗埆] 娉ㄥ唽妯℃澘寮€锟?userId={}, biometricType={}, deviceId={}",
                userId, biometricType, deviceId);

        // 鑾峰彇鍒嗗竷寮忛攣锛岄槻姝㈤噸澶嶆敞锟?
        String lockKey = LOCK_KEY_PREFIX + "register:" + userId + ":" + biometricType;
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new RuntimeException("妯℃澘娉ㄥ唽涓紝璇风◢鍚庨噸锟?);
        }

        try {
            // 1. 楠岃瘉鐢ㄦ埛鍜岃锟?
            validateUserAndDevice(userId, deviceId, biometricType);

            // 2. 妫€鏌ユ槸鍚﹀凡瀛樺湪鐩稿悓绫诲瀷鐨勬ā锟?
            List<BiometricTemplateEntity> existingTemplates = biometricTemplateDao
                    .selectByUserIdAndType(userId, biometricType);

            if (!existingTemplates.isEmpty()) {
                throw new RuntimeException("鐢ㄦ埛宸插瓨鍦ㄨ绫诲瀷鐨勭敓鐗╄瘑鍒ā锟?);
            }

            // 3. 鑾峰彇鐢熺墿璇嗗埆閰嶇疆
            BiometricConfigEntity config = getBiometricConfig(biometricType);

            // 4. 鎻愬彇鐗瑰緛鍚戦噺
            String featureVector = extractFeatureVector(featureData, config);

            // 5. 鍒涘缓妯℃澘
            BiometricTemplateEntity template = new BiometricTemplateEntity();
            template.setUserId(userId);
            template.setBiometricType(biometricType);
            template.setTemplateName(generateTemplateName(userId, biometricType));
            template.setTemplateStatus(BiometricTemplateEntity.TemplateStatus.ACTIVE.getCode());
            template.setFeatureData(encryptFeatureData(featureData));
            template.setFeatureVector(featureVector);
            template.setMatchThreshold(config.getMatchThreshold());
            template.setAlgorithmVersion(config.getAlgorithmVersion());
            template.setDeviceId(deviceId);
            template.setCaptureTime(LocalDateTime.now());
            template.setExpireTime(calculateExpireTime());
            template.setUseCount(0);
            template.setSuccessCount(0);
            template.setFailCount(0);

            // 6. 淇濆瓨妯℃澘
            biometricTemplateDao.insert(template);

            // 7. 鏇存柊璁惧浣跨敤缁熻
            updateDeviceUsageStats(deviceId);

            // 8. 娓呴櫎鐢ㄦ埛妯℃澘缂撳瓨
            clearUserTemplateCache(userId);

            log.info("[鐢熺墿璇嗗埆] 妯℃澘娉ㄥ唽鎴愬姛 templateId={}", template.getTemplateId());
            return template;

        } finally {
            // 閲婃斁锟?
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 鐢熺墿璇嗗埆楠岃瘉
     *
     * @param userId 鐢ㄦ埛ID
     * @param biometricType 鐢熺墿璇嗗埆绫诲瀷
     * @param featureData 寰呴獙璇佺殑鐗瑰緛鏁版嵁
     * @param deviceId 璁惧ID
     * @return 楠岃瘉缁撴灉
     */
    public BiometricAuthResult authenticate(Long userId, Integer biometricType,
                                          byte[] featureData, String deviceId) {
        long startTime = System.currentTimeMillis();

        log.info("[鐢熺墿璇嗗埆] 楠岃瘉寮€锟?userId={}, biometricType={}, deviceId={}",
                userId, biometricType, deviceId);

        try {
            // 1. 鑾峰彇鐢ㄦ埛鏈夋晥妯℃澘
            List<BiometricTemplateEntity> templates = getUserActiveTemplates(userId, biometricType);
            if (templates.isEmpty()) {
                return createAuthResult(false, null, 0.0, "鐢ㄦ埛鏈敞鍐岃绫诲瀷鐨勭敓鐗╄瘑鍒ā锟?, startTime);
            }

            // 2. 鑾峰彇鐢熺墿璇嗗埆閰嶇疆
            BiometricConfigEntity config = getBiometricConfig(biometricType);

            // 3. 鎻愬彇寰呴獙璇佺壒寰佸悜锟?
            String featureVector = extractFeatureVector(featureData, config);

            // 4. 娲讳綋妫€锟?
            LivenessResult livenessResult = performLivenessCheck(featureData, config);
            if (!livenessResult.isPassed()) {
                return createAuthResult(false, null, 0.0, "娲讳綋妫€娴嬪け锟? " + livenessResult.getMessage(), startTime);
            }

            // 5. 鐗瑰緛鍖归厤
            BiometricMatchResult matchResult = performMatching(templates, featureVector, config);

            // 6. 璁板綍楠岃瘉鏃ュ織
            recordAuthLog(userId, biometricType, deviceId, matchResult, livenessResult, startTime);

            // 7. 鏇存柊妯℃澘缁熻
            if (matchResult.isSuccess()) {
                updateTemplateStats(matchResult.getMatchedTemplate(), true);
            } else {
                updateTemplateStats(templates.get(0), false);
            }

            log.info("[鐢熺墿璇嗗埆] 楠岃瘉瀹屾垚 userId={}, result={}, matchScore={}",
                    userId, matchResult.isSuccess(), matchResult.getMatchScore());

            return new BiometricAuthResult(
                    matchResult.isSuccess(),
                    matchResult.getMatchedTemplate(),
                    matchResult.getMatchScore(),
                    livenessResult,
                    matchResult.isSuccess() ? "楠岃瘉鎴愬姛" : "鐗瑰緛鍖归厤澶辫触",
                    System.currentTimeMillis() - startTime
            );

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆] 楠岃瘉寮傚父 userId={}, biometricType={}", userId, biometricType, e);
            return createAuthResult(false, null, 0.0, "绯荤粺寮傚父: " + e.getMessage(), startTime);
        }
    }

    /**
     * 鎵归噺楠岃瘉锟?:N璇嗗埆锟?
     *
     * @param biometricType 鐢熺墿璇嗗埆绫诲瀷
     * @param featureData 寰呰瘑鍒殑鐗瑰緛鏁版嵁
     * @param deviceId 璁惧ID
     * @param limit 杩斿洖缁撴灉鏁伴噺闄愬埗
     * @return 璇嗗埆缁撴灉
     */
    public List<BiometricAuthResult> identify(Integer biometricType, byte[] featureData,
                                            String deviceId, Integer limit) {
        long startTime = System.currentTimeMillis();

        log.info("[鐢熺墿璇嗗埆] 1:N璇嗗埆寮€锟?biometricType={}, deviceId={}", biometricType, deviceId);

        try {
            // 1. 鑾峰彇鎵€鏈夋縺娲荤殑妯℃澘
            List<BiometricTemplateEntity> allTemplates = biometricTemplateDao
                    .selectByBiometricType(biometricType);

            if (allTemplates.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 鑾峰彇鐢熺墿璇嗗埆閰嶇疆
            BiometricConfigEntity config = getBiometricConfig(biometricType);

            // 3. 鎻愬彇寰呰瘑鍒壒寰佸悜锟?
            String featureVector = extractFeatureVector(featureData, config);

            // 4. 娲讳綋妫€锟?
            LivenessResult livenessResult = performLivenessCheck(featureData, config);
            if (!livenessResult.isPassed()) {
                return Collections.emptyList();
            }

            // 5. 鎵归噺鍖归厤
            List<BiometricMatchResult> matchResults = new ArrayList<>();
            for (BiometricTemplateEntity template : allTemplates) {
                double similarity = SmartBiometricUtil.calculateSimilarity(
                        featureVector, template.getFeatureVector());

                if (similarity >= template.getMatchThreshold()) {
                    matchResults.add(new BiometricMatchResult(true, template, similarity));
                }
            }

            // 6. 鎸夌浉浼煎害鎺掑簭锛岃繑鍥炲墠N涓粨锟?
            matchResults.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

            List<BiometricAuthResult> results = new ArrayList<>();
            int resultCount = Math.min(limit != null ? limit : 10, matchResults.size());

            for (int i = 0; i < resultCount; i++) {
                BiometricMatchResult matchResult = matchResults.get(i);
                results.add(new BiometricAuthResult(
                        true,
                        matchResult.getMatchedTemplate(),
                        matchResult.getMatchScore(),
                        livenessResult,
                        "璇嗗埆鎴愬姛",
                        System.currentTimeMillis() - startTime
                ));
            }

            log.info("[鐢熺墿璇嗗埆] 1:N璇嗗埆瀹屾垚 biometricType={}, resultCount={}",
                    biometricType, results.size());

            return results;

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆] 1:N璇嗗埆寮傚父 biometricType={}", biometricType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 鏇存柊妯℃澘鐘讹拷?
     */
    public void updateTemplateStatus(Long templateId, Integer status) {
        biometricTemplateDao.updateTemplateStatus(templateId, status);

        // 娓呴櫎鐩稿叧缂撳瓨
        BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
        if (template != null) {
            clearUserTemplateCache(template.getUserId());
        }
    }

    /**
     * 鍒犻櫎鐢ㄦ埛妯℃澘
     */
    public void deleteUserTemplates(Long userId, Integer biometricType) {
        List<BiometricTemplateEntity> templates = biometricTemplateDao
                .selectByUserIdAndType(userId, biometricType);

        for (BiometricTemplateEntity template : templates) {
            biometricTemplateDao.deleteById(template.getTemplateId());
        }

        clearUserTemplateCache(userId);
    }

    // ========== 绉佹湁鏂规硶 ==========

    /**
     * 楠岃瘉鐢ㄦ埛鍜岃锟?
     */
    private void validateUserAndDevice(Long userId, String deviceId, Integer biometricType) {
        // 楠岃瘉璁惧鏄惁瀛樺湪涓旀敮鎸佽鐢熺墿璇嗗埆绫诲瀷
        DeviceEntity device = deviceDao.selectByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("璁惧涓嶅瓨锟? " + deviceId);
        }

        // 妫€鏌ヨ澶囨槸鍚︽敮鎸佽鐢熺墿璇嗗埆绫诲瀷
        if (!isDeviceSupportBiometricType(device, biometricType)) {
            throw new RuntimeException("璁惧涓嶆敮鎸佽鐢熺墿璇嗗埆绫诲瀷");
        }
    }

    /**
     * 鑾峰彇鐢熺墿璇嗗埆閰嶇疆
     */
    private BiometricConfigEntity getBiometricConfig(Integer biometricType) {
        // 浠庣紦瀛樿幏鍙栭厤锟?
        String cacheKey = CACHE_KEY_PREFIX + "config:" + biometricType;
        BiometricConfigEntity config = (BiometricConfigEntity) redisTemplate.opsForValue().get(cacheKey);

        if (config == null) {
            // 浠庢暟鎹簱鑾峰彇
            List<BiometricConfigEntity> configs = biometricConfigDao
                    .selectByTypeAndStatus(biometricType, BiometricConfigEntity.ConfigStatus.ACTIVE.getCode());

            if (configs.isEmpty()) {
                throw new RuntimeException("鏈壘鍒扮敓鐗╄瘑鍒厤锟? " + biometricType);
            }

            config = configs.get(0);

            // 缂撳瓨閰嶇疆
            redisTemplate.opsForValue().set(cacheKey, config, 1, TimeUnit.HOURS);
        }

        return config;
    }

    /**
     * 鎻愬彇鐗瑰緛鍚戦噺
     */
    private String extractFeatureVector(byte[] featureData, BiometricConfigEntity config) {
        try {
            return SmartBiometricUtil.extractFeatureVector(featureData, config.getAlgorithmModel());
        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆] 鐗瑰緛鍚戦噺鎻愬彇澶辫触", e);
            throw new RuntimeException("鐗瑰緛鍚戦噺鎻愬彇澶辫触: " + e.getMessage());
        }
    }

    /**
     * 鍔犲瘑鐗瑰緛鏁版嵁
     */
    private String encryptFeatureData(byte[] featureData) {
        try {
            return SmartAESUtil.encryptBase64(featureData, SecurityConst.BIOMETRIC_ENCRYPTION_KEY);
        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆] 鐗瑰緛鏁版嵁鍔犲瘑澶辫触", e);
            throw new RuntimeException("鐗瑰緛鏁版嵁鍔犲瘑澶辫触");
        }
    }

    /**
     * 鐢熸垚妯℃澘鍚嶇О
     */
    private String generateTemplateName(Long userId, Integer biometricType) {
        BiometricTemplateEntity.BiometricType type = BiometricTemplateEntity.BiometricType.fromCode(biometricType);
        return type.getDescription() + "_妯℃澘_" + System.currentTimeMillis();
    }

    /**
     * 璁＄畻杩囨湡鏃堕棿
     */
    private LocalDateTime calculateExpireTime() {
        return LocalDateTime.now().plusYears(2);
    }

    /**
     * 鑾峰彇鐢ㄦ埛婵€娲荤殑妯℃澘
     */
    private List<BiometricTemplateEntity> getUserActiveTemplates(Long userId, Integer biometricType) {
        String cacheKey = CACHE_KEY_USER_TEMPLATES + userId + ":" + biometricType;

        @SuppressWarnings("unchecked")
        List<BiometricTemplateEntity> templates = (List<BiometricTemplateEntity>)
                redisTemplate.opsForValue().get(cacheKey);

        if (templates == null) {
            templates = biometricTemplateDao.selectByUserIdAndType(userId, biometricType);

            // 杩囨护鎺夐潪婵€娲荤姸鎬佺殑妯℃澘
            templates = templates.stream()
                    .filter(t -> t.getTemplateStatus().equals(BiometricTemplateEntity.TemplateStatus.ACTIVE.getCode()))
                    .filter(t -> t.getExpireTime().isAfter(LocalDateTime.now()))
                    .toList();

            // 缂撳瓨缁撴灉
            redisTemplate.opsForValue().set(cacheKey, templates, 30, TimeUnit.MINUTES);
        }

        return templates;
    }

    /**
     * 鎵ц娲讳綋妫€锟?
     */
    private LivenessResult performLivenessCheck(byte[] featureData, BiometricConfigEntity config) {
        if (!Boolean.TRUE.equals(config.getLivenessEnabled())) {
            return new LivenessResult(true, "娲讳綋妫€娴嬫湭鍚敤", 1.0);
        }

        try {
            return SmartBiometricUtil.performLivenessCheck(featureData, config.getLivenessConfig());
        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆] 娲讳綋妫€娴嬪け锟?, e);
            return new LivenessResult(false, "娲讳綋妫€娴嬪紓锟? " + e.getMessage(), 0.0);
        }
    }

    /**
     * 鎵ц鐗瑰緛鍖归厤
     */
    private BiometricMatchResult performMatching(List<BiometricTemplateEntity> templates,
                                               String featureVector, BiometricConfigEntity config) {
        BiometricTemplateEntity bestMatch = null;
        double bestScore = 0.0;

        for (BiometricTemplateEntity template : templates) {
            double similarity = SmartBiometricUtil.calculateSimilarity(
                    featureVector, template.getFeatureVector());

            if (similarity >= template.getMatchThreshold() && similarity > bestScore) {
                bestScore = similarity;
                bestMatch = template;
            }
        }

        if (bestMatch != null) {
            return new BiometricMatchResult(true, bestMatch, bestScore);
        } else {
            // 杩斿洖鏈€鎺ヨ繎鐨勭粨鏋滐紙鍗充娇鏈揪鍒伴槇鍊硷級
            if (!templates.isEmpty()) {
                BiometricTemplateEntity closestTemplate = templates.get(0);
                double closestScore = SmartBiometricUtil.calculateSimilarity(
                        featureVector, closestTemplate.getFeatureVector());
                return new BiometricMatchResult(false, closestTemplate, closestScore);
            }
            return new BiometricMatchResult(false, null, 0.0);
        }
    }

    /**
     * 璁板綍楠岃瘉鏃ュ織
     */
    private void recordAuthLog(Long userId, Integer biometricType, String deviceId,
                               BiometricMatchResult matchResult, LivenessResult livenessResult,
                               long startTime) {
        try {
            BiometricAuthRecordEntity record = new BiometricAuthRecordEntity();
            record.setUserId(userId);
            record.setDeviceId(deviceId);
            record.setBiometricType(biometricType);
            record.setAuthType(BiometricAuthRecordEntity.AuthType.ACCESS.getCode());
            record.setAuthResult(matchResult.isSuccess() ?
                    BiometricAuthRecordEntity.AuthResult.SUCCESS.getCode() :
                    BiometricAuthRecordEntity.AuthResult.FAILED.getCode());
            record.setMatchScore(BigDecimal.valueOf(matchResult.getMatchScore()).setScale(4, RoundingMode.HALF_UP));
            record.setLivenessScore(BigDecimal.valueOf(livenessResult.getScore()).setScale(4, RoundingMode.HALF_UP));
            record.setLivenessPassed(livenessResult.isPassed());
            record.setAuthDuration(System.currentTimeMillis() - startTime);
            record.setAuthTime(LocalDateTime.now());

            if (matchResult.getMatchedTemplate() != null) {
                record.setTemplateId(matchResult.getMatchedTemplate().getTemplateId());
            }

            // 妫€鏌ユ槸鍚︿负鍙枒鎿嶄綔
            checkSuspiciousOperation(record);

            biometricAuthRecordDao.insert(record);

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆] 璁板綍楠岃瘉鏃ュ織澶辫触", e);
        }
    }

    /**
     * 妫€鏌ュ彲鐤戞搷锟?
     */
    private void checkSuspiciousOperation(BiometricAuthRecordEntity record) {
        // 妫€鏌ラ绻佸け锟?
        String failCountKey = "biometric:fail:count:" + record.getUserId() + ":" + record.getDeviceId();
        String countStr = (String) redisTemplate.opsForValue().get(failCountKey);
        int failCount = countStr != null ? Integer.parseInt(countStr) : 0;

        if (!record.getAuthResult().equals(BiometricAuthRecordEntity.AuthResult.SUCCESS.getCode())) {
            failCount++;
            redisTemplate.opsForValue().set(failCountKey, String.valueOf(failCount), 1, TimeUnit.HOURS);

            if (failCount >= 5) {
                record.setSuspiciousOperation(true);
                record.setSuspiciousReason("楠岃瘉澶辫触娆℃暟杩囧: " + failCount);
            }
        } else {
            redisTemplate.delete(failCountKey);
        }

        // 妫€鏌ュ紓甯告椂闂存
        int hour = LocalDateTime.now().getHour();
        if (hour < 6 || hour > 23) {
            record.setSuspiciousOperation(true);
            if (record.getSuspiciousReason() == null) {
                record.setSuspiciousReason("寮傚父鏃堕棿娈甸獙锟?);
            }
        }
    }

    /**
     * 鏇存柊妯℃澘缁熻
     */
    private void updateTemplateStats(BiometricTemplateEntity template, boolean success) {
        if (success) {
            biometricTemplateDao.updateSuccessStats(template.getTemplateId());
        } else {
            biometricTemplateDao.updateFailStats(template.getTemplateId());
        }
        biometricTemplateDao.updateUsageStats(template.getTemplateId());
    }

    /**
     * 鏇存柊璁惧浣跨敤缁熻
     */
    private void updateDeviceUsageStats(String deviceId) {
        // 鏇存柊璁惧浣跨敤缁熻閫昏緫
    }

    /**
     * 妫€鏌ヨ澶囨槸鍚︽敮鎸佺敓鐗╄瘑鍒被锟?
     */
    private boolean isDeviceSupportBiometricType(DeviceEntity device, Integer biometricType) {
        // 鏍规嵁璁惧绫诲瀷鍜岄厤缃垽鏂槸鍚︽敮锟?
        return true; // 绠€鍖栧疄锟?
    }

    /**
     * 娓呴櫎鐢ㄦ埛妯℃澘缂撳瓨
     */
    private void clearUserTemplateCache(Long userId) {
        String pattern = CACHE_KEY_USER_TEMPLATES + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 鍒涘缓璁よ瘉缁撴灉
     */
    private BiometricAuthResult createAuthResult(boolean success, BiometricTemplateEntity template,
                                               double matchScore, String message, long startTime) {
        return new BiometricAuthResult(
                success,
                template,
                matchScore,
                new LivenessResult(false, "鏈墽锟?, 0.0),
                message,
                System.currentTimeMillis() - startTime
        );
    }

    // ========== 鍐呴儴锟?==========

    /**
     * 鐢熺墿璇嗗埆楠岃瘉缁撴灉
     */
    public static class BiometricAuthResult {
        private boolean success;
        private BiometricTemplateEntity matchedTemplate;
        private double matchScore;
        private LivenessResult livenessResult;
        private String message;
        private long duration;

        public BiometricAuthResult(boolean success, BiometricTemplateEntity matchedTemplate,
                                 double matchScore, LivenessResult livenessResult,
                                 String message, long duration) {
            this.success = success;
            this.matchedTemplate = matchedTemplate;
            this.matchScore = matchScore;
            this.livenessResult = livenessResult;
            this.message = message;
            this.duration = duration;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public BiometricTemplateEntity getMatchedTemplate() { return matchedTemplate; }
        public double getMatchScore() { return matchScore; }
        public LivenessResult getLivenessResult() { return livenessResult; }
        public String getMessage() { return message; }
        public long getDuration() { return duration; }
    }

    /**
     * 娲讳綋妫€娴嬬粨锟?
     */
    public static class LivenessResult {
        private boolean passed;
        private String message;
        private double score;

        public LivenessResult(boolean passed, String message, double score) {
            this.passed = passed;
            this.message = message;
            this.score = score;
        }

        // Getters
        public boolean isPassed() { return passed; }
        public String getMessage() { return message; }
        public double getScore() { return score; }
    }

    /**
     * 鍖归厤缁撴灉
     */
    private static class BiometricMatchResult {
        private boolean success;
        private BiometricTemplateEntity matchedTemplate;
        private double matchScore;

        public BiometricMatchResult(boolean success, BiometricTemplateEntity matchedTemplate, double matchScore) {
            this.success = success;
            this.matchedTemplate = matchedTemplate;
            this.matchScore = matchScore;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public BiometricTemplateEntity getMatchedTemplate() { return matchedTemplate; }
        public double getMatchScore() { return matchScore; }
    }
}
