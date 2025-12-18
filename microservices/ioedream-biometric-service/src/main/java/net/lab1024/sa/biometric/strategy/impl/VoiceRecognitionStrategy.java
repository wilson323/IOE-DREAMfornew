package net.lab1024.sa.biometric.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.domain.entity.BiometricType;
import net.lab1024.sa.biometric.domain.vo.BiometricSample;
import net.lab1024.sa.biometric.domain.vo.FeatureVector;
import net.lab1024.sa.biometric.domain.vo.IdentificationResult;
import net.lab1024.sa.biometric.domain.vo.LivenessResult;
import net.lab1024.sa.biometric.domain.vo.MatchResult;
import net.lab1024.sa.biometric.strategy.IBiometricRecognitionStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 声纹识别策略实现
 * <p>
 * ⚠️ 已废弃：本类实现了不应该在服务端实现的方法（verify、identify、detectLiveness）
 * 根据ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案，识别功能应由设备端完成
 * </p>
 * <p>
 * 请创建 {@link IBiometricFeatureExtractionStrategy} 的实现类替代
 * 新策略只实现特征提取功能，符合架构要求
 * </p>
 * <p>
 * 使用MFCC特征提取128维特征向量
 * 支持活体检测（防录音攻击）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 * @deprecated 请创建新的特征提取策略实现类替代
 * @see IBiometricFeatureExtractionStrategy
 */
@Deprecated
@Slf4j
@Component
public class VoiceRecognitionStrategy implements IBiometricRecognitionStrategy {

    @Override
    public BiometricType getSupportedType() {
        return BiometricType.VOICE;
    }

    @Override
    public FeatureVector extractFeature(BiometricSample sample) {
        log.debug("[声纹识别] 开始提取特征向量");
        // TODO: 实现声纹特征提取算法
        // 1. 音频预处理（降噪、分帧）
        // 2. MFCC特征提取
        // 3. 生成128维特征向量
        throw new UnsupportedOperationException("声纹特征提取功能待实现");
    }

    @Override
    public LivenessResult detectLiveness(BiometricSample sample) {
        log.debug("[声纹识别] 开始活体检测");
        // TODO: 实现活体检测算法
        // 1. 防录音攻击检测
        // 2. 音频质量检测
        throw new UnsupportedOperationException("声纹活体检测功能待实现");
    }

    @Override
    public MatchResult verify(FeatureVector probeFeature, FeatureVector galleryFeature) {
        log.debug("[声纹识别] 开始1:1验证");
        // TODO: 实现特征匹配算法
        throw new UnsupportedOperationException("声纹1:1验证功能待实现");
    }

    @Override
    public IdentificationResult identify(FeatureVector probeFeature, List<FeatureVector> galleryFeatures) {
        log.debug("[声纹识别] 开始1:N识别, gallerySize={}", galleryFeatures.size());
        // TODO: 实现1:N识别算法
        throw new UnsupportedOperationException("声纹1:N识别功能待实现");
    }

    @Override
    public int getPriority() {
        return 60; // 声纹识别优先级较低
    }
}
