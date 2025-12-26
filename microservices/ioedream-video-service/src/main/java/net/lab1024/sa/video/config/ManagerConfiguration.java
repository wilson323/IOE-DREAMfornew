package net.lab1024.sa.video.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.lab1024.sa.video.manager.AIEventManager;
import net.lab1024.sa.video.manager.BehaviorDetectionManager;
import net.lab1024.sa.video.manager.CrowdAnalysisManager;
import net.lab1024.sa.video.manager.FaceRecognitionManager;
import net.lab1024.sa.video.manager.VideoDeviceManager;
import net.lab1024.sa.video.manager.VideoStreamManager;
import net.lab1024.sa.video.manager.VideoSystemIntegrationManager;
import net.lab1024.sa.video.sdk.AiSdkConfig;
import net.lab1024.sa.video.sdk.AiSdkProvider;
import net.lab1024.sa.video.sdk.impl.LocalAiSdkProvider;

/**
 * Manager配置类
 * <p>
 * 用于将视频模块特有的Manager实现类注册为Spring Bean
 * </p>
 * <p>
 * 注意：公共Manager（NotificationManager、WorkflowApprovalManager等）
 * 已由CommonBeanAutoConfiguration统一装配，无需在此重复定义
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-14 移除重复的公共Bean定义，改用统一自动装配
 */
@Slf4j
@Configuration("videoManagerConfiguration")
public class ManagerConfiguration {

    /**
     * 注册VideoDeviceManager为Spring Bean
     * <p>
     * 视频模块特有的Manager，负责视频设备管理、状态检查、批量操作等功能
     * </p>
     *
     * @return 视频设备管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(VideoDeviceManager.class)
    public VideoDeviceManager videoDeviceManager() {
        log.info("[VideoDeviceManager] 初始化视频设备管理器");
        return new VideoDeviceManager();
    }

    /**
     * 注册VideoStreamManager为Spring Bean
     * <p>
     * 视频模块特有的Manager，负责视频流会话管理、流媒体协议转换、质量监控等功能
     * </p>
     *
     * @return 视频流管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(VideoStreamManager.class)
    public VideoStreamManager videoStreamManager() {
        log.info("[VideoStreamManager] 初始化视频流管理器");
        return new VideoStreamManager();
    }

    /**
     * 注册VideoSystemIntegrationManager为Spring Bean
     * <p>
     * 系统集成管理器，负责协调视频服务内部各子系统的集成和监控
     * </p>
     *
     * @return 系统集成管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(VideoSystemIntegrationManager.class)
    public VideoSystemIntegrationManager videoSystemIntegrationManager() {
        log.info("[VideoSystemIntegrationManager] 初始化系统集成管理器");
        return new VideoSystemIntegrationManager();
    }

    /**
     * 注册AiSdkProvider为Spring Bean
     * <p>
     * AI SDK提供者，用于Manager类的依赖注入
     * </p>
     *
     * @return AI SDK提供者实例
     */
    @Bean
    @ConditionalOnMissingBean(AiSdkProvider.class)
    public AiSdkProvider aiSdkProvider() {
        log.info("[AiSdkProvider] 初始化AI SDK提供者");
        LocalAiSdkProvider provider = new LocalAiSdkProvider();
        // 初始化配置（可根据实际需求配置）
        AiSdkConfig config = new AiSdkConfig();
        provider.initialize(config);
        return provider;
    }

    /**
     * 注册FaceRecognitionManager为Spring Bean
     * <p>
     * 人脸识别管理器，负责人脸识别算法的调用、结果处理和模型管理
     * </p>
     *
     * @param aiSdkProvider AI SDK提供者
     * @return 人脸识别管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(FaceRecognitionManager.class)
    public FaceRecognitionManager faceRecognitionManager(AiSdkProvider aiSdkProvider) {
        log.info("[FaceRecognitionManager] 初始化人脸识别管理器");
        return new FaceRecognitionManager(aiSdkProvider);
    }

    /**
     * 注册BehaviorDetectionManager为Spring Bean
     * <p>
     * 行为检测管理器，负责行为分析算法的调用、异常检测和模式识别
     * </p>
     *
     * @return 行为检测管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(BehaviorDetectionManager.class)
    public BehaviorDetectionManager behaviorDetectionManager() {
        log.info("[BehaviorDetectionManager] 初始化行为检测管理器");
        return new BehaviorDetectionManager();
    }

    /**
     * 注册CrowdAnalysisManager为Spring Bean
     * <p>
     * 人群分析管理器，负责人群密度分析、人数统计、异常聚集检测等
     * </p>
     *
     * @param aiSdkProvider AI SDK提供者
     * @return 人群分析管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(CrowdAnalysisManager.class)
    public CrowdAnalysisManager crowdAnalysisManager(AiSdkProvider aiSdkProvider) {
        log.info("[CrowdAnalysisManager] 初始化人群分析管理器");
        return new CrowdAnalysisManager(aiSdkProvider);
    }

    /**
     * 注册AIEventManager为Spring Bean
     * <p>
     * AI事件管理器，负责AI智能分析事件的统一管理
     * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过配置类注册
     * </p>
     *
     * @return AI事件管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(AIEventManager.class)
    public AIEventManager aiEventManager() {
        log.info("[AIEventManager] 初始化AI事件管理器");
        return new AIEventManager();
    }
}
