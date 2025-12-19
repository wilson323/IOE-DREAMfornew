package net.lab1024.sa.common.util;

import java.util.Arrays;

/**
 * 生物识别工具类
 * <p>
 * 提供生物识别相关的工具方法
 * 严格遵循CLAUDE.md规范：业务特定工具类，保留在common-core中
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public class SmartBiometricUtil {

    private SmartBiometricUtil() {
        // 私有构造，禁止实例化
    }

    /**
     * 提取特征向量
     * <p>
     * 从原始特征数据中提取特征向量
     * 这里使用简化的实现，实际应该调用AI模型提取特征
     * </p>
     *
     * @param featureData 原始特征数据
     * @param algorithmModel 算法模型标识
     * @return 特征向量字符串（Base64编码）
     */
    public static String extractFeatureVector(byte[] featureData, String algorithmModel) {
        if (featureData == null || featureData.length == 0) {
            throw new IllegalArgumentException("特征数据不能为空");
        }
        // 简化实现：实际应该调用AI模型提取特征
        // 这里返回Base64编码的特征数据作为特征向量
        return SmartBase64Util.encode(featureData);
    }

    /**
     * 计算相似度
     * <p>
     * 计算两个特征向量的相似度（0-1之间的值）
     * 这里使用简化的实现，实际应该使用余弦相似度等算法
     * </p>
     *
     * @param featureVector1 特征向量1（Base64编码）
     * @param featureVector2 特征向量2（Base64编码）
     * @return 相似度（0-1之间）
     */
    public static double calculateSimilarity(String featureVector1, String featureVector2) {
        if (featureVector1 == null || featureVector2 == null) {
            return 0.0;
        }
        if (featureVector1.equals(featureVector2)) {
            return 1.0;
        }

        try {
            // 解码特征向量
            byte[] vector1 = SmartBase64Util.decodeToBytes(featureVector1);
            byte[] vector2 = SmartBase64Util.decodeToBytes(featureVector2);

            // 计算余弦相似度（简化实现）
            return calculateCosineSimilarity(vector1, vector2);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 计算余弦相似度
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 余弦相似度（0-1之间）
     */
    private static double calculateCosineSimilarity(byte[] vector1, byte[] vector2) {
        if (vector1.length != vector2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            double v1 = (double) (vector1[i] & 0xFF);
            double v2 = (double) (vector2[i] & 0xFF);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 验证特征数据格式
     *
     * @param featureData 特征数据
     * @return 是否有效
     */
    public static boolean validateFeatureData(byte[] featureData) {
        return featureData != null && featureData.length > 0;
    }

    /**
     * 标准化特征向量
     *
     * @param featureVector 特征向量（Base64编码）
     * @return 标准化后的特征向量
     */
    public static String normalizeFeatureVector(String featureVector) {
        // 简化实现：实际应该进行L2归一化等操作
        return featureVector;
    }
}


