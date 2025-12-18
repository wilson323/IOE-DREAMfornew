package net.lab1024.sa.biometric.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.domain.entity.BiometricType;
import net.lab1024.sa.biometric.strategy.IBiometricRecognitionStrategy;
import net.lab1024.sa.biometric.strategy.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生物识别策略配置类
 * <p>
 * 注册5大识别策略，并创建策略工厂
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Configuration
public class BiometricStrategyConfiguration {

    /**
     * 注册人脸识别策略
     */
    @Bean
    public FaceRecognitionStrategy faceRecognitionStrategy() {
        log.info("[策略配置] 注册人脸识别策略");
        return new FaceRecognitionStrategy();
    }

    /**
     * 注册指纹识别策略
     */
    @Bean
    public FingerprintRecognitionStrategy fingerprintRecognitionStrategy() {
        log.info("[策略配置] 注册指纹识别策略");
        return new FingerprintRecognitionStrategy();
    }

    /**
     * 注册虹膜识别策略
     */
    @Bean
    public IrisRecognitionStrategy irisRecognitionStrategy() {
        log.info("[策略配置] 注册虹膜识别策略");
        return new IrisRecognitionStrategy();
    }

    /**
     * 注册掌纹识别策略
     */
    @Bean
    public PalmRecognitionStrategy palmRecognitionStrategy() {
        log.info("[策略配置] 注册掌纹识别策略");
        return new PalmRecognitionStrategy();
    }

    /**
     * 注册声纹识别策略
     */
    @Bean
    public VoiceRecognitionStrategy voiceRecognitionStrategy() {
        log.info("[策略配置] 注册声纹识别策略");
        return new VoiceRecognitionStrategy();
    }

    /**
     * 创建策略工厂Bean
     * <p>
     * 根据生物识别类型返回对应的策略实现
     * </p>
     */
    @Bean
    public Map<BiometricType, IBiometricRecognitionStrategy> biometricStrategyFactory(
            List<IBiometricRecognitionStrategy> strategies) {
        log.info("[策略配置] 创建策略工厂, 策略数量={}", strategies.size());

        // 按优先级排序
        List<IBiometricRecognitionStrategy> sortedStrategies = strategies.stream()
                .sorted((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()))
                .collect(Collectors.toList());

        // 构建策略映射
        Map<BiometricType, IBiometricRecognitionStrategy> factory = new HashMap<>();
        for (IBiometricRecognitionStrategy strategy : sortedStrategies) {
            BiometricType type = strategy.getSupportedType();
            factory.put(type, strategy);
            log.info("[策略配置] 注册策略: type={}, priority={}", type, strategy.getPriority());
        }

        return factory;
    }
}
