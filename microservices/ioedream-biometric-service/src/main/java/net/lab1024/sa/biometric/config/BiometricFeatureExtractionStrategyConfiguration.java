package net.lab1024.sa.biometric.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.domain.entity.BiometricType;
import net.lab1024.sa.biometric.strategy.IBiometricFeatureExtractionStrategy;
import net.lab1024.sa.biometric.strategy.impl.FaceFeatureExtractionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生物特征提取策略配置类
 * <p>
 * ⚠️ 重要说明：
 * - 本配置类用于注册特征提取策略（只用于入职时处理上传的照片）
 * - 不包含识别、验证、活体检测等策略（这些由设备端完成）
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 只注册特征提取策略
 * - 创建策略工厂，支持动态加载策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
public class BiometricFeatureExtractionStrategyConfiguration {

    /**
     * 注册人脸特征提取策略
     */
    @Bean
    public FaceFeatureExtractionStrategy faceFeatureExtractionStrategy() {
        log.info("[特征提取策略配置] 注册人脸特征提取策略");
        return new FaceFeatureExtractionStrategy();
    }

    // TODO: 注册其他特征提取策略
    // @Bean
    // public FingerprintFeatureExtractionStrategy fingerprintFeatureExtractionStrategy() {
    //     return new FingerprintFeatureExtractionStrategy();
    // }

    /**
     * 创建特征提取策略工厂Bean
     * <p>
     * 根据生物识别类型返回对应的特征提取策略
     * </p>
     *
     * @param strategies 所有特征提取策略实现
     * @return 策略工厂（BiometricType -> IBiometricFeatureExtractionStrategy）
     */
    @Bean
    public Map<BiometricType, IBiometricFeatureExtractionStrategy> biometricFeatureExtractionStrategyFactory(
            List<IBiometricFeatureExtractionStrategy> strategies) {
        log.info("[特征提取策略配置] 创建策略工厂, 策略数量={}", strategies.size());

        // 按优先级排序
        List<IBiometricFeatureExtractionStrategy> sortedStrategies = strategies.stream()
                .sorted((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()))
                .collect(Collectors.toList());

        // 构建策略映射
        Map<BiometricType, IBiometricFeatureExtractionStrategy> factory = new HashMap<>();
        for (IBiometricFeatureExtractionStrategy strategy : sortedStrategies) {
            BiometricType type = strategy.getSupportedType();
            factory.put(type, strategy);
            log.info("[特征提取策略配置] 注册策略: type={}, priority={}", type, strategy.getPriority());
        }

        return factory;
    }
}
