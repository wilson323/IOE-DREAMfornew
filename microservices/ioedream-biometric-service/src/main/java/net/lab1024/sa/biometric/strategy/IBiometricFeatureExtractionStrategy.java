package net.lab1024.sa.biometric.strategy;

import net.lab1024.sa.biometric.domain.entity.BiometricType;
import net.lab1024.sa.biometric.domain.vo.BiometricSample;
import net.lab1024.sa.biometric.domain.vo.FeatureVector;

/**
 * 生物特征提取策略接口
 * <p>
 * ⚠️ 重要说明：
 * - 本接口只用于入职时处理上传的照片，提取特征向量
 * - 不用于实时识别（实时识别由设备端完成）
 * - 不包含验证、识别、活体检测等方法（这些由设备端完成）
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 使用策略模式实现
 * - 接口化设计，支持依赖倒置
 * - 支持动态加载策略
 * - 只实现特征提取功能，不实现识别功能
 * </p>
 * <p>
 * 支持的生物识别类型：
 * - 人脸识别 (FaceFeatureExtractionStrategy)
 * - 指纹识别 (FingerprintFeatureExtractionStrategy)
 * - 虹膜识别 (IrisFeatureExtractionStrategy)
 * - 掌纹识别 (PalmFeatureExtractionStrategy)
 * - 声纹识别 (VoiceFeatureExtractionStrategy)
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
public interface IBiometricFeatureExtractionStrategy {

    /**
     * 获取支持的生物识别类型
     *
     * @return 生物识别类型
     */
    BiometricType getSupportedType();

    /**
     * 提取特征向量
     * <p>
     * ⭐ 核心方法：从生物样本中提取特征向量
     * </p>
     * <p>
     * 使用场景：
     * - 用户入职时上传人脸照片，提取512维特征向量
     * - 用户更新生物模板时，重新提取特征向量
     * </p>
     * <p>
     * 处理流程：
     * 1. 图像预处理（人脸检测、对齐、归一化）
     * 2. 特征提取（调用深度学习模型提取特征向量）
     * 3. 质量检测（评估特征质量，不符合要求则抛出异常）
     * 4. 返回特征向量
     * </p>
     *
     * @param sample 生物样本（图像/音频数据）
     * @return 特征向量（包含特征数据、质量分数、维度等信息）
     * @throws net.lab1024.sa.common.exception.BusinessException 如果特征提取失败或质量不符合要求
     */
    FeatureVector extractFeature(BiometricSample sample);

    /**
     * 验证特征质量
     * <p>
     * 检查提取的特征向量是否符合质量要求
     * </p>
     *
     * @param featureVector 特征向量
     * @return true表示质量合格，false表示质量不合格
     */
    default boolean validateFeatureQuality(FeatureVector featureVector) {
        if (featureVector == null) {
            return false;
        }

        // 1. 验证特征向量维度
        BiometricType type = BiometricType.fromCode(featureVector.getBiometricType());
        if (!featureVector.getDimension().equals(type.getDimension())) {
            return false;
        }

        // 2. 验证质量分数（最低0.7）
        if (featureVector.getQualityScore() == null || featureVector.getQualityScore() < 0.7) {
            return false;
        }

        // 3. 验证特征数据
        if (featureVector.getData() == null || featureVector.getData().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * 获取策略优先级
     * <p>
     * 用于策略工厂排序，优先级高的策略优先使用
     * </p>
     *
     * @return 优先级（数字越大优先级越高）
     */
    default int getPriority() {
        return 100;
    }
}
