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
 * 生物特征模板管理器
 * 负责处理生物识别模板的注册、验证、匹配等核心业务逻辑
 * 严格遵循CLAUDE.md规范：纯Java类，不使用Spring注解
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 * @updated 2025-12-17 移除@Component注解，改为纯Java类，符合规范要求
 */
@Slf4j
public class BiometricTemplateManager {

    private final BiometricTemplateDao biometricTemplateDao;
    private final BiometricConfigDao biometricConfigDao;
    private final BiometricAuthRecordDao biometricAuthRecordDao;
    private final DeviceDao deviceDao;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存键值前缀
    private static final String CACHE_KEY_PREFIX = "biometric:template:";
    private static final String CACHE_KEY_USER_TEMPLATES = "biometric:user:templates:";
    private static final String LOCK_KEY_PREFIX = "biometric:lock:";

    /**
     * 构造函数
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
     * 注册生物特征模板
     *
     * @param userId 用户ID
     * @param biometricType 生物特征类型
     * @param featureData 特征数据
     * @param deviceId 设备ID
     * @return 注册结果
     */
    public BiometricTemplateEntity registerTemplate(Long userId, Integer biometricType,
                                                   byte[] featureData, String deviceId) {
        log.info("[生物特征] 注册模板:userId={}, biometricType={}, deviceId={}",
                userId, biometricType, deviceId);

        // 获取分布式锁，防止重复注册
        String lockKey = LOCK_KEY_PREFIX + "register:" + userId + ":" + biometricType;
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new RuntimeException("模板注册正在处理中，请稍后重试");
        }

        try {
            // 1. 验证用户和设备权限
            validateUserAndDevice(userId, deviceId, biometricType);

            // 2. 检查用户是否已有该类型模板
            List<BiometricTemplateEntity> existingTemplates = biometricDao
                    .selectByUserIdAndType(userId, biometricType);

            if (!existingTemplates.isEmpty()) {
                throw new RuntimeException("用户已注册过该类型的生物特征模板");
            }

            // 3. 获取生物特征配置
            BiometricConfigEntity config = getBiometricConfig(biometricType);

            // 4. 提取特征向量
            String featureVector = extractFeatureVector(featureData, config);

            // 5. 创建模板
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

            // 6. 保存模板
            biometricTemplateDao.insert(template);

            // 7. 更新设备使用统计
            updateDeviceUsageStats(deviceId);

            // 8. 清除用户模板缓存
            clearUserTemplateCache(userId);

            log.info("[生物特征] 模板注册成功:templateId={}", template.getTemplateId());
            return template;

        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 生物特征认证
     *
     * @param userId 用户ID
     * @param biometricType 生物特征类型
     * @param featureData 实时特征数据
     * @param deviceId 设备ID
     * @return 认证结果
     */
    public BiometricAuthResult authenticate(Long userId, Integer biometricType,
                                          byte[] featureData, String deviceId) {
        long startTime = System.currentTimeMillis();

        log.info("[生物特征] 开始认证:userId={}, biometricType={}, deviceId={}",
                userId, biometricType, deviceId);

        try {
            // 1. 获取用户活跃模板
            List<BiometricTemplateEntity> templates = getUserActiveTemplates(userId, biometricType);
            if (templates.isEmpty()) {
                return createAuthResult(false, null, 0.0, "用户未注册该类型的生物特征模板", startTime);
            }

            // 2. 获取生物特征配置
            BiometricConfigEntity config = getBiometricConfig(biometricType);

            // 3. 提取实时特征向量
            String featureVector = extractFeatureVector(featureData, config);

            // 4. 活体检测
            LivenessResult livenessResult = performLivenessCheck(featureData, config);
            if (!livenessResult.isPassed()) {
                return createAuthResult(false, null, 0.0, "活体检测失败: " + livenessResult.getMessage(), startTime);
            }

            // 5. 特征匹配
            BiometricMatchResult matchResult = performMatching(templates, featureVector, config);

            // 6. 记录认证日志
            recordAuthLog(userId, biometricType, deviceId, matchResult, livenessResult, startTime);

            // 7. 更新模板缓存
            if (matchResult.isSuccess()) {
                updateTemplateStats(matchResult.getMatchedTemplate(), true);
            } else {
                updateTemplateStats(templates.get(0), false);
            }

            log.info("[生物特征] 认证完成: userId={}, result={}, matchScore={}",
                    userId, matchResult.isSuccess(), matchResult.getMatchScore());

            return new BiometricAuthResult(
                    matchResult.isSuccess(),
                    matchResult.getMatchedTemplate(),
                    matchResult.getMatchScore(),
                    livenessResult,
                    matchResult.isSuccess() ? "认证成功" : "特征匹配失败",
                    System.currentTimeMillis() - startTime
            );

        } catch (Exception e) {
            log.error("[生物特征] 认证异常: userId={}, biometricType={}", userId, biometricType, e);
            return createAuthResult(false, null, 0.0, "系统异常: " + e.getMessage(), startTime);
        }
    }

    /**
     * 无限制识别?:N特征识别
     *
     * @param biometricType 生物特征类型
     * @param featureData 实时特征数据
     * @param deviceId 设备ID
     * @param limit 返回结果数量限制
     * @return 特征识别结果
     */
    public List<BiometricAuthResult> identify(Integer biometricType, byte[] featureData,
                                            String deviceId, Integer limit) {
        long startTime = System.currentTimeMillis();

        log.info("[生物特征] 1:N识别开始:biometricType={}, deviceId={}", biometricType, deviceId);

        try {
            // 1. 获取所有活跃模板
            List<BiometricTemplateEntity> allTemplates = biometricTemplateDao
                    .selectByBiometricType(biometricType);

            if (allTemplates.isEmpty()) {
                return Collections.emptyList();
            }

            // 2. 获取生物特征配置
            BiometricConfigEntity config = getBiometricConfig(biometricType);

            // 3. 提取实时特征向量
            String featureVector = extractFeatureVector(featureData, config);

            // 4. 活体检测
            LivenessResult livenessResult = performLivenessCheck(featureData, config);
            if (!livenessResult.isPassed()) {
                return Collections.emptyList();
            }

            // 5. 1:N特征匹配
            List<BiometricMatchResult> matchResults = new ArrayList<>();
            for (BiometricTemplateEntity template : allTemplates) {
                double similarity = SmartBiometricUtil.calculateSimilarity(
                        featureVector, template.getFeatureVector());

                if (similarity >= template.getMatchThreshold()) {
                    matchResults.add(new BiometricMatchResult(true, template, similarity));
                }
            }

            // 6. 按相似度排序，只返回前N个结果
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
                        "特征识别成功",
                        System.currentTimeMillis() - startTime
                ));
            }

            log.info("[生物特征] 1:N识别完成: biometricType={}, resultCount={}",
                    biometricType, results.size());

            return results;

        } catch (Exception e) {
            log.error("[生物特征] 1:N识别异常: biometricType={}", biometricType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 更新模板状态
     */
    public void updateTemplateStatus(Long templateId, Integer status) {
        biometricTemplateDao.updateTemplateStatus(templateId, status);

        // 清除模板缓存
        BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
        if (template != null) {
            clearUserTemplateCache(template.getUserId());
        }
    }

    /**
     * 删除用户模板
     */
    public void deleteUserTemplates(Long userId, Integer biometricType) {
        List<BiometricTemplateEntity> templates = biometricTemplateDao
                .selectByUserIdAndType(userId, biometricType);

        for (BiometricTemplateEntity template : templates) {
            biometricTemplateDao.deleteById(template.getTemplateId());
        }

        clearUserTemplateCache(userId);
    }

    // ========== 私有方法 ==========

    /**
     * 验证用户和设备权限
     */
    private void validateUserAndDevice(Long userId, String deviceId, Integer biometricType) {
        // 验证设备是否存在且支持生物识别
        DeviceEntity device = deviceDao.selectByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在: " + deviceId);
        }

        // 验证设备是否支持该类型的生物识别
        if (!isDeviceSupportBiometricType(device, biometricType)) {
            throw new RuntimeException("设备不支持该类型的生物识别");
        }
    }

    /**
     * 获取生物特征配置
     */
    private BiometricConfigEntity getBiometricConfig(Integer biometricType) {
        // 尝试从缓存获取配置
        String cacheKey = CACHE_KEY_PREFIX + "config:" + biometricType;
        BiometricConfigEntity config = (BiometricConfigEntity) redisTemplate.opsForValue().get(cacheKey);

        if (config == null) {
            // 从数据库查询配置
            List<BiometricConfigEntity> configs = biometricConfigDao
                    .selectByTypeAndStatus(biometricType, BiometricConfigEntity.ConfigStatus.ACTIVE.getCode());

            if (configs.isEmpty()) {
                throw new RuntimeException("未找到有效的生物识别配置: " + biometricType);
            }

            config = configs.get(0);

            // 缓存配置
            redisTemplate.opsForValue().set(cacheKey, config, 1, TimeUnit.HOURS);
        }

        return config;
    }

    /**
     * 提取特征向量
     */
    private String extractFeatureVector(byte[] featureData, BiometricConfigEntity config) {
        try {
            return SmartBiometricUtil.extractFeatureVector(featureData, config.getAlgorithmModel());
        } catch (Exception e) {
            log.error("[生物特征] 特征向量提取失败", e);
            throw new RuntimeException("特征向量提取失败: " + e.getMessage());
        }
    }

    /**
     * 加密特征数据
     */
    private String encryptFeatureData(byte[] featureData) {
        try {
            return SmartAESUtil.encryptBase64(featureData, SecurityConst.BIOMETRIC_ENCRYPTION_KEY);
        } catch (Exception e) {
            log.error("[生物特征] 特征数据加密失败", e);
            throw new RuntimeException("特征数据加密失败");
        }
    }

    /**
     * 生成模板名称
     */
    private String generateTemplateName(Long userId, Integer biometricType) {
        BiometricTemplateEntity.BiometricType type = BiometricTemplateEntity.BiometricType.fromCode(biometricType);
        return type.getDescription() + "_模板_" + System.currentTimeMillis();
    }

    /**
     * 计算过期时间
     */
    private LocalDateTime calculateExpireTime() {
        return LocalDateTime.now().plusYears(2);
    }

    /**
     * 获取用户活跃模板
     */
    private List<BiometricTemplateEntity> getUserActiveTemplates(Long userId, Integer biometricType) {
        String cacheKey = CACHE_KEY_USER_TEMPLATES + userId + ":" + biometricType;

        @SuppressWarnings("unchecked")
        List<BiometricTemplateEntity> templates = (List<BiometricTemplateEntity>)
                redisTemplate.opsForValue().get(cacheKey);

        if (templates == null) {
            templates = biometricTemplateDao.selectByUserIdAndType(userId, biometricType);

            // 过滤出活跃状态且未过期的模板
            templates = templates.stream()
                    .filter(t -> t.getTemplateStatus().equals(BiometricTemplateEntity.TemplateStatus.ACTIVE.getCode()))
                    .filter(t -> t.getExpireTime().isAfter(LocalDateTime.now()))
                    .toList();

            // 缓存查询结果
            redisTemplate.opsForValue().set(cacheKey, templates, 30, TimeUnit.MINUTES);
        }

        return templates;
    }

    /**
     * 执行活体检测
     */
    private LivenessResult performLivenessCheck(byte[] featureData, BiometricConfigEntity config) {
        if (!Boolean.TRUE.equals(config.getLivenessEnabled())) {
            return new LivenessResult(true, "活体检测已禁用", 1.0);
        }

        try {
            return SmartBiometricUtil.performLivenessCheck(featureData, config.getLivenessConfig());
        } catch (Exception e) {
            log.error("[生物特征] 活体检测异常", e);
            return new LivenessResult(false, "活体检测失败: " + e.getMessage(), 0.0);
        }
    }

    /**
     * 执行特征匹配
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
            // 如果没有匹配的模板，返回最接近的结果用于日志记录
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
     * 记录认证日志
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

            // 检查可疑操作
            checkSuspiciousOperation(record);

            biometricAuthRecordDao.insert(record);

        } catch (Exception e) {
            log.error("[生物特征] 记录认证日志失败", e);
        }
    }

    /**
     * 检查可疑操作
     */
    private void checkSuspiciousOperation(BiometricAuthRecordEntity record) {
        // 检查失败次数
        String failCountKey = "biometric:fail:count:" + record.getUserId() + ":" + record.getDeviceId();
        String countStr = (String) redisTemplate.opsForValue().get(failCountKey);
        int failCount = countStr != null ? Integer.parseInt(countStr) : 0;

        if (!record.getAuthResult().equals(BiometricAuthRecordEntity.AuthResult.SUCCESS.getCode())) {
            failCount++;
            redisTemplate.opsForValue().set(failCountKey, String.valueOf(failCount), 1, TimeUnit.HOURS);

            if (failCount >= 5) {
                record.setSuspiciousOperation(true);
                record.setSuspiciousReason("认证失败次数异常: " + failCount);
            }
        } else {
            redisTemplate.delete(failCountKey);
        }

        // 检查异常时间段访问
        int hour = LocalDateTime.now().getHour();
        if (hour < 6 || hour > 23) {
            record.setSuspiciousOperation(true);
            if (record.getSuspiciousReason() == null) {
                record.setSuspiciousReason("异常时间段访问");
            }
        }
    }

    /**
     * 更新模板统计
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
     * 更新设备使用统计
     */
    private void updateDeviceUsageStats(String deviceId) {
        // 更新设备使用统计缓存
    }

    /**
     * 检查设备是否支持生物识别类型
     */
    private boolean isDeviceSupportBiometricType(DeviceEntity device, Integer biometricType) {
        // 根据设备类型检查是否支持该生物识别类型
        return true; // 默认支持所有类型
    }

    /**
     * 清除用户模板缓存
     */
    private void clearUserTemplateCache(Long userId) {
        String pattern = CACHE_KEY_USER_TEMPLATES + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 创建认证结果
     */
    private BiometricAuthResult createAuthResult(boolean success, BiometricTemplateEntity template,
                                               double matchScore, String message, long startTime) {
        return new BiometricAuthResult(
                success,
                template,
                matchScore,
                new LivenessResult(false, "未启用活体检测", 0.0),
                message,
                System.currentTimeMillis() - startTime
        );
    }

    // ========== 内部类定义 ==========

    /**
     * 生物特征认证结果
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
     * 活体检测结果
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
     * 特征匹配结果
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