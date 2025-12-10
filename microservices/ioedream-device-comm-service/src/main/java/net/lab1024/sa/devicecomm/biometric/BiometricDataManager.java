package net.lab1024.sa.devicecomm.biometric;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.devicecomm.protocol.enums.VerifyTypeEnum;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 生物识别数据管理器
 * <p>
 * 统一管理各类生物识别数据的存储、检索、匹配和处理：
 * - 人脸特征数据
 * - 指纹特征数据
 * - 虹膜特征数据
 * - 掌纹特征数据
 * - 指静脉特征数据
 * - 掌静脉特征数据
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class BiometricDataManager {

    // 生物识别数据缓存
    private final Map<Long, Map<VerifyTypeEnum, BiometricData>> biometricDataCache = new ConcurrentHashMap<>();

    // 特征匹配阈值配置
    private final Map<VerifyTypeEnum, Double> matchThresholds = new ConcurrentHashMap<>();

    // 数据过期时间配置（毫秒）
    private static final long DATA_EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24小时

    public BiometricDataManager() {
        // 初始化匹配阈值
        initializeMatchThresholds();
    }

    /**
     * 初始化匹配阈值
     */
    private void initializeMatchThresholds() {
        matchThresholds.put(VerifyTypeEnum.FACE, 0.85);        // 人脸匹配阈值
        matchThresholds.put(VerifyTypeEnum.FINGERPRINT, 0.90); // 指纹匹配阈值
        matchThresholds.put(VerifyTypeEnum.IRIS, 0.95);        // 虹膜匹配阈值
        matchThresholds.put(VerifyTypeEnum.PALM, 0.85);        // 掌纹匹配阈值
        matchThresholds.put(VerifyTypeEnum.FINGER_VEIN, 0.90); // 指静脉匹配阈值
        matchThresholds.put(VerifyTypeEnum.PALM_VEIN, 0.85);   // 掌静脉匹配阈值
    }

    /**
     * 保存生物识别数据
     *
     * @param userId 用户ID
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @param templateData 模板数据
     * @param deviceId 设备ID
     * @return 是否保存成功
     */
    public boolean saveBiometricData(Long userId, VerifyTypeEnum verifyType,
                                   byte[] featureData, byte[] templateData, Long deviceId) {
        try {
            BiometricData data = new BiometricData();
            data.setUserId(userId);
            data.setVerifyType(verifyType);
            data.setFeatureData(featureData);
            data.setTemplateData(templateData);
            data.setDeviceId(deviceId);
            data.setCreateTime(System.currentTimeMillis());
            data.setUpdateTime(System.currentTimeMillis());
            data.setActive(true);

            // 获取用户的生物识别数据映射
            Map<VerifyTypeEnum, BiometricData> userData = biometricDataCache.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

            // 保存或更新数据
            userData.put(verifyType, data);

            log.info("[生物识别数据] 保存成功: 用户={}, 验证方式={}, 数据长度={}, 设备={}",
                    userId, verifyType.getName(), featureData.length, deviceId);

            return true;

        } catch (Exception e) {
            log.error("[生物识别数据] 保存失败: 用户={}, 验证方式={}, 错误={}",
                    userId, verifyType.getName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取用户的生物识别数据
     *
     * @param userId 用户ID
     * @param verifyType 验证类型
     * @return 生物识别数据
     */
    public BiometricData getBiometricData(Long userId, VerifyTypeEnum verifyType) {
        Map<VerifyTypeEnum, BiometricData> userData = biometricDataCache.get(userId);
        if (userData == null) {
            return null;
        }

        BiometricData data = userData.get(verifyType);
        if (data != null && isDataExpired(data)) {
            // 数据过期，移除并返回null
            userData.remove(verifyType);
            if (userData.isEmpty()) {
                biometricDataCache.remove(userId);
            }
            return null;
        }

        return data;
    }

    /**
     * 获取用户的所有生物识别数据
     *
     * @param userId 用户ID
     * @return 生物识别数据列表
     */
    public List<BiometricData> getAllBiometricData(Long userId) {
        Map<VerifyTypeEnum, BiometricData> userData = biometricDataCache.get(userId);
        if (userData == null) {
            return Collections.emptyList();
        }

        List<BiometricData> result = new ArrayList<>();
        for (BiometricData data : userData.values()) {
            if (!isDataExpired(data)) {
                result.add(data);
            }
        }

        return result;
    }

    /**
     * 删除生物识别数据
     *
     * @param userId 用户ID
     * @param verifyType 验证类型
     * @return 是否删除成功
     */
    public boolean deleteBiometricData(Long userId, VerifyTypeEnum verifyType) {
        try {
            Map<VerifyTypeEnum, BiometricData> userData = biometricDataCache.get(userId);
            if (userData == null) {
                return false;
            }

            BiometricData removedData = userData.remove(verifyType);
            if (userData.isEmpty()) {
                biometricDataCache.remove(userId);
            }

            if (removedData != null) {
                log.info("[生物识别数据] 删除成功: 用户={}, 验证方式={}",
                        userId, verifyType.getName());
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("[生物识别数据] 删除失败: 用户={}, 验证方式={}, 错误={}",
                    userId, verifyType.getName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 匹配生物识别特征
     *
     * @param userId 用户ID
     * @param verifyType 验证类型
     * @param featureData 待匹配的特征数据
     * @return 匹配结果
     */
    public BiometricMatchResult matchBiometricFeature(Long userId, VerifyTypeEnum verifyType, byte[] featureData) {
        BiometricData storedData = getBiometricData(userId, verifyType);
        if (storedData == null) {
            return BiometricMatchResult.notFound("用户生物识别数据不存在");
        }

        try {
            double similarity = calculateSimilarity(featureData, storedData.getFeatureData(), verifyType);
            double threshold = matchThresholds.getOrDefault(verifyType, 0.80);

            boolean matched = similarity >= threshold;

            BiometricMatchResult result = new BiometricMatchResult();
            result.setUserId(userId);
            result.setVerifyType(verifyType);
            result.setSimilarity(similarity);
            result.setThreshold(threshold);
            result.setMatched(matched);
            result.setMatchTime(System.currentTimeMillis());
            result.setMessage(matched ? "匹配成功" : "匹配失败");

            log.debug("[生物识别匹配] 用户={}, 验证方式={}, 相似度={}, 阈值={}, 结果={}",
                    userId, verifyType.getName(), similarity, threshold, matched ? "成功" : "失败");

            return result;

        } catch (Exception e) {
            log.error("[生物识别匹配] 计算失败: 用户={}, 验证方式={}, 错误={}",
                    userId, verifyType.getName(), e.getMessage(), e);
            return BiometricMatchResult.error("匹配计算失败: " + e.getMessage());
        }
    }

    /**
     * 查找最佳匹配的用户
     *
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @param candidateUserIds 候选用户ID列表
     * @return 最佳匹配结果
     */
    public BiometricMatchResult findBestMatch(VerifyTypeEnum verifyType, byte[] featureData, List<Long> candidateUserIds) {
        if (candidateUserIds == null || candidateUserIds.isEmpty()) {
            return BiometricMatchResult.notFound("候选用户列表为空");
        }

        BiometricMatchResult bestMatch = null;
        double bestSimilarity = 0.0;
        double threshold = matchThresholds.getOrDefault(verifyType, 0.80);

        for (Long userId : candidateUserIds) {
            BiometricMatchResult result = matchBiometricFeature(userId, verifyType, featureData);
            if (result.isMatched() && result.getSimilarity() > bestSimilarity) {
                bestMatch = result;
                bestSimilarity = result.getSimilarity();
            }
        }

        if (bestMatch != null && bestSimilarity >= threshold) {
            log.info("[生物识别最佳匹配] 验证方式={}, 用户={}, 相似度={}, 阈值={}",
                    verifyType.getName(), bestMatch.getUserId(), bestSimilarity, threshold);
            return bestMatch;
        }

        return BiometricMatchResult.notFound("未找到匹配的用户（相似度未达到阈值）");
    }

    /**
     * 获取用户支持的所有验证方式
     *
     * @param userId 用户ID
     * @return 验证方式列表
     */
    public List<VerifyTypeEnum> getSupportedVerifyTypes(Long userId) {
        List<BiometricData> userData = getAllBiometricData(userId);
        List<VerifyTypeEnum> result = new ArrayList<>();

        for (BiometricData data : userData) {
            if (data.isActive()) {
                result.add(data.getVerifyType());
            }
        }

        return result;
    }

    /**
     * 检查数据是否过期
     */
    private boolean isDataExpired(BiometricData data) {
        return System.currentTimeMillis() - data.getUpdateTime() > DATA_EXPIRE_TIME;
    }

    /**
     * 计算特征相似度
     */
    private double calculateSimilarity(byte[] feature1, byte[] feature2, VerifyTypeEnum verifyType) {
        // 简化的相似度计算算法
        // 实际项目中应该使用专业的生物识别算法库
        try {
            if (feature1 == null || feature2 == null || feature1.length != feature2.length) {
                return 0.0;
            }

            double similarity = 0.0;
            int length = Math.min(feature1.length, feature2.length);

            switch (verifyType) {
                case FACE:
                    // 人脸相似度计算（简化版）
                    similarity = calculateFaceSimilarity(feature1, feature2, length);
                    break;
                case FINGERPRINT:
                    // 指纹相似度计算（简化版）
                    similarity = calculateFingerprintSimilarity(feature1, feature2, length);
                    break;
                case IRIS:
                    // 虹膜相似度计算（简化版）
                    similarity = calculateIrisSimilarity(feature1, feature2, length);
                    break;
                default:
                    // 通用相似度计算
                    similarity = calculateGenericSimilarity(feature1, feature2, length);
                    break;
            }

            return Math.min(1.0, Math.max(0.0, similarity));

        } catch (Exception e) {
            log.error("[相似度计算] 计算失败: 验证方式={}, 错误={}", verifyType.getName(), e.getMessage());
            return 0.0;
        }
    }

    /**
     * 人脸相似度计算（简化版）
     */
    private double calculateFaceSimilarity(byte[] feature1, byte[] feature2, int length) {
        int matches = 0;
        for (int i = 0; i < length; i++) {
            int diff = Math.abs((feature1[i] & 0xFF) - (feature2[i] & 0xFF));
            if (diff < 20) { // 差异小于20认为匹配
                matches++;
            }
        }
        return (double) matches / length;
    }

    /**
     * 指纹相似度计算（简化版）
     */
    private double calculateFingerprintSimilarity(byte[] feature1, byte[] feature2, int length) {
        int matches = 0;
        for (int i = 0; i < length; i++) {
            if ((feature1[i] & 0xFF) == (feature2[i] & 0xFF)) {
                matches++;
            }
        }
        return (double) matches / length;
    }

    /**
     * 虹膜相似度计算（简化版）
     */
    private double calculateIrisSimilarity(byte[] feature1, byte[] feature2, int length) {
        int matches = 0;
        for (int i = 0; i < length; i++) {
            int diff = Math.abs((feature1[i] & 0xFF) - (feature2[i] & 0xFF));
            if (diff < 10) { // 虹膜要求更严格的匹配
                matches++;
            }
        }
        return (double) matches / length;
    }

    /**
     * 通用相似度计算
     */
    private double calculateGenericSimilarity(byte[] feature1, byte[] feature2, int length) {
        int matches = 0;
        for (int i = 0; i < length; i++) {
            if ((feature1[i] & 0xFF) == (feature2[i] & 0xFF)) {
                matches++;
            }
        }
        return (double) matches / length;
    }

    /**
     * 清理过期数据
     */
    public void cleanExpiredData() {
        long currentTime = System.currentTimeMillis();
        int cleanedCount = 0;

        for (Map.Entry<Long, Map<VerifyTypeEnum, BiometricData>> userEntry : biometricDataCache.entrySet()) {
            Long userId = userEntry.getKey();
            Map<VerifyTypeEnum, BiometricData> userData = userEntry.getValue();

            Iterator<Map.Entry<VerifyTypeEnum, BiometricData>> iterator = userData.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<VerifyTypeEnum, BiometricData> entry = iterator.next();
                BiometricData data = entry.getValue();

                if (currentTime - data.getUpdateTime() > DATA_EXPIRE_TIME) {
                    iterator.remove();
                    cleanedCount++;
                }
            }

            // 如果用户没有数据了，移除用户
            if (userData.isEmpty()) {
                biometricDataCache.remove(userId);
            }
        }

        if (cleanedCount > 0) {
            log.info("[生物识别数据] 清理过期数据完成: 清理数量={}", cleanedCount);
        }
    }

    /**
     * 获取统计信息
     */
    public BiometricDataStatistics getStatistics() {
        BiometricDataStatistics statistics = new BiometricDataStatistics();

        Map<VerifyTypeEnum, Integer> typeCountMap = new HashMap<>();
        int totalUsers = 0;
        int totalData = 0;

        for (Map.Entry<Long, Map<VerifyTypeEnum, BiometricData>> userEntry : biometricDataCache.entrySet()) {
            Map<VerifyTypeEnum, BiometricData> userData = userEntry.getValue();

            totalUsers++;
            for (BiometricData data : userData.values()) {
                totalData++;
                VerifyTypeEnum type = data.getVerifyType();
                typeCountMap.put(type, typeCountMap.getOrDefault(type, 0) + 1);
            }
        }

        statistics.setTotalUsers(totalUsers);
        statistics.setTotalDataCount(totalData);
        statistics.setTypeCountMap(typeCountMap);
        statistics.setCacheSize(biometricDataCache.size());

        return statistics;
    }

    /**
     * 生物识别数据实体类
     */
    public static class BiometricData {
        private Long userId;
        private VerifyTypeEnum verifyType;
        private byte[] featureData;
        private byte[] templateData;
        private Long deviceId;
        private Long createTime;
        private Long updateTime;
        private Boolean active;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }

        public byte[] getTemplateData() { return templateData; }
        public void setTemplateData(byte[] templateData) { this.templateData = templateData; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public Long getCreateTime() { return createTime; }
        public void setCreateTime(Long createTime) { this.createTime = createTime; }

        public Long getUpdateTime() { return updateTime; }
        public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }

        public Boolean isActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    /**
     * 生物识别匹配结果类
     */
    public static class BiometricMatchResult {
        private Long userId;
        private VerifyTypeEnum verifyType;
        private Double similarity;
        private Double threshold;
        private Boolean matched;
        private Long matchTime;
        private String message;

        public static BiometricMatchResult notFound(String message) {
            BiometricMatchResult result = new BiometricMatchResult();
            result.setMatched(false);
            result.setMessage(message);
            result.setMatchTime(System.currentTimeMillis());
            return result;
        }

        public static BiometricMatchResult error(String message) {
            BiometricMatchResult result = new BiometricMatchResult();
            result.setMatched(false);
            result.setMessage(message);
            result.setMatchTime(System.currentTimeMillis());
            return result;
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public Double getSimilarity() { return similarity; }
        public void setSimilarity(Double similarity) { this.similarity = similarity; }

        public Double getThreshold() { return threshold; }
        public void setThreshold(Double threshold) { this.threshold = threshold; }

        public Boolean isMatched() { return matched; }
        public void setMatched(Boolean matched) { this.matched = matched; }

        public Long getMatchTime() { return matchTime; }
        public void setMatchTime(Long matchTime) { this.matchTime = matchTime; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 生物识别数据统计类
     */
    public static class BiometricDataStatistics {
        private Integer totalUsers;
        private Integer totalDataCount;
        private Map<VerifyTypeEnum, Integer> typeCountMap;
        private Integer cacheSize;

        // Getters and Setters
        public Integer getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }

        public Integer getTotalDataCount() { return totalDataCount; }
        public void setTotalDataCount(Integer totalDataCount) { this.totalDataCount = totalDataCount; }

        public Map<VerifyTypeEnum, Integer> getTypeCountMap() { return typeCountMap; }
        public void setTypeCountMap(Map<VerifyTypeEnum, Integer> typeCountMap) { this.typeCountMap = typeCountMap; }

        public Integer getCacheSize() { return cacheSize; }
        public void setCacheSize(Integer cacheSize) { this.cacheSize = cacheSize; }
    }
}