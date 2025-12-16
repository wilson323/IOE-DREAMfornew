package net.lab1024.sa.video.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.manager.VideoDeviceManager;
import net.lab1024.sa.video.manager.VideoStreamManager;

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
}
