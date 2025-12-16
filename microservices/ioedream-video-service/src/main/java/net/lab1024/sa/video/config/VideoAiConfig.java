package net.lab1024.sa.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.manager.BehaviorDetectionManager;
import net.lab1024.sa.video.manager.CrowdAnalysisManager;
import net.lab1024.sa.video.manager.FaceRecognitionManager;
import net.lab1024.sa.video.sdk.AiSdkConfig;
import net.lab1024.sa.video.sdk.AiSdkFactory;
import net.lab1024.sa.video.sdk.AiSdkProvider;

/**
 * 视频AI分析模块配置类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Configuration注解标识配置类
 * - 使用@Bean注册Manager实例
 * - Manager类通过构造函数注入依赖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Configuration
public class VideoAiConfig {

    @Resource
    private AiSdkFactory aiSdkFactory;

    /**
     * 注册FaceRecognitionManager为Spring Bean
     * <p>
     * 通过AiSdkFactory获取默认的AI SDK提供者并注入到FaceRecognitionManager
     * </p>
     *
     * @return 人脸识别管理器实例
     */
    @Bean
    public FaceRecognitionManager faceRecognitionManager() {
        log.info("[VideoAiConfig] 初始化FaceRecognitionManager");

        // 创建默认AI SDK配置
        AiSdkConfig defaultConfig = new AiSdkConfig();
        defaultConfig.setSdkType("LOCAL");
        defaultConfig.setModelPath("/models");
        defaultConfig.setDeviceType("CPU");

        // 获取默认AI SDK提供者
        AiSdkProvider aiSdkProvider = aiSdkFactory.getProvider("LOCAL", defaultConfig);

        // 通过构造函数注入依赖
        return new FaceRecognitionManager(aiSdkProvider);
    }

    /**
     * 注册BehaviorDetectionManager为Spring Bean
     *
     * @return 行为检测管理器实例
     */
    @Bean
    public BehaviorDetectionManager behaviorDetectionManager() {
        log.info("[VideoAiConfig] 初始化BehaviorDetectionManager");
        return new BehaviorDetectionManager();
    }

    /**
     * 注册CrowdAnalysisManager为Spring Bean
     * <p>
     * 通过AiSdkFactory获取默认的AI SDK提供者并注入到CrowdAnalysisManager
     * </p>
     *
     * @return 人群分析管理器实例
     */
    @Bean
    public CrowdAnalysisManager crowdAnalysisManager() {
        log.info("[VideoAiConfig] 初始化CrowdAnalysisManager");

        // 创建默认AI SDK配置
        AiSdkConfig defaultConfig = new AiSdkConfig();
        defaultConfig.setSdkType("LOCAL");
        defaultConfig.setModelPath("/models");
        defaultConfig.setDeviceType("CPU");

        // 获取默认AI SDK提供者
        AiSdkProvider aiSdkProvider = aiSdkFactory.getProvider("LOCAL", defaultConfig);

        // 通过构造函数注入依赖
        return new CrowdAnalysisManager(aiSdkProvider);
    }
}
