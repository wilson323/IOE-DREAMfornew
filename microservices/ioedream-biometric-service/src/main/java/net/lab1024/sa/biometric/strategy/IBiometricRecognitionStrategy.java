package net.lab1024.sa.biometric.strategy;

import net.lab1024.sa.biometric.domain.entity.BiometricType;
import net.lab1024.sa.biometric.domain.vo.BiometricSample;
import net.lab1024.sa.biometric.domain.vo.FeatureVector;
import net.lab1024.sa.biometric.domain.vo.IdentificationResult;
import net.lab1024.sa.biometric.domain.vo.LivenessResult;
import net.lab1024.sa.biometric.domain.vo.MatchResult;

import java.util.List;

/**
 * 生物识别策略接口
 * <p>
 * 定义5大生物识别算法的统一接口：
 * - 人脸识别 (FaceRecognitionStrategy)
 * - 指纹识别 (FingerprintRecognitionStrategy)
 * - 虹膜识别 (IrisRecognitionStrategy)
 * - 掌纹识别 (PalmRecognitionStrategy)
 * - 声纹识别 (VoiceRecognitionStrategy)
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用策略模式实现
 * - 接口化设计，支持依赖倒置
 * - 支持动态加载策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface IBiometricRecognitionStrategy {

    /**
     * 获取支持的生物识别类型
     *
     * @return 生物识别类型
     */
    BiometricType getSupportedType();

    /**
     * 提取特征向量
     * <p>
     * 从生物样本中提取特征向量，用于后续匹配
     * </p>
     *
     * @param sample 生物样本（图像/音频数据）
     * @return 特征向量
     */
    FeatureVector extractFeature(BiometricSample sample);

    /**
     * 活体检测
     * <p>
     * 检测样本是否为活体，防止照片/视频/录音攻击
     * </p>
     *
     * @param sample 生物样本
     * @return 活体检测结果
     */
    LivenessResult detectLiveness(BiometricSample sample);

    /**
     * 1:1验证
     * <p>
     * 验证两个特征向量是否匹配
     * </p>
     *
     * @param probeFeature   待验证特征向量
     * @param galleryFeature 注册特征向量
     * @return 匹配结果（包含匹配分数）
     */
    MatchResult verify(FeatureVector probeFeature, FeatureVector galleryFeature);

    /**
     * 1:N识别
     * <p>
     * 从多个特征向量中识别最匹配的
     * </p>
     *
     * @param probeFeature  待识别特征向量
     * @param galleryFeatures 注册特征向量列表
     * @return 识别结果（包含匹配的用户ID和匹配分数）
     */
    IdentificationResult identify(FeatureVector probeFeature, List<FeatureVector> galleryFeatures);

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
