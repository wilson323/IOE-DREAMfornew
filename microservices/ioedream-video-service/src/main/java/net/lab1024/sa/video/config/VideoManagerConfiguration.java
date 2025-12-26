package net.lab1024.sa.video.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.video.dao.VideoMonitorDao;
import net.lab1024.sa.video.manager.VideoMonitorManager;
// import net.lab1024.sa.video.manager.VideoPTZManager; // 已移除
import net.lab1024.sa.video.manager.VideoStreamManager;

/**
 * 视频模块Manager配置类
 * <p>
 * 配置视频模块的Manager层Bean，注册为Spring容器管理的组件
 * 严格遵循CLAUDE.md全局架构规范
 * 统一使用GatewayServiceClient进行微服务间调用
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-16
 * @updated 2025-12-21 移除自定义HTTP客户端，统一使用GatewayServiceClient
 */
@Slf4j
@Configuration
public class VideoManagerConfiguration {

    /**
     * 注册视频流管理器
     *
     * @param videoStreamDao 视频流数据访问层
     * @return 视频流管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(VideoStreamManager.class)
    public VideoStreamManager videoStreamManager() {
        log.info("[视频配置] 注册VideoStreamManager Bean");
        return new VideoStreamManager();
    }

    /**
     * 注册监控会话管理器
     *
     * @param videoMonitorDao 监控会话数据访问层
     * @param gatewayServiceClient 网关服务客户端
     * @return 监控会话管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(VideoMonitorManager.class)
    public VideoMonitorManager videoMonitorManager(VideoMonitorDao videoMonitorDao, GatewayServiceClient gatewayServiceClient) {
        log.info("[视频配置] 注册VideoMonitorManager Bean，使用GatewayServiceClient");
        return new VideoMonitorManager(videoMonitorDao, gatewayServiceClient);
    }

    // 云台控制管理器已移除
    // 如需使用，请先创建VideoPTZManager类

    /**
     * 注册告警规则管理器（可选）
     * 注意：这里只是示例，实际可以根据需要添加VideoAlarmRuleManager
     *
     * @param videoAlarmRuleDao 告警规则数据访问层
     * @return 告警规则管理器实例（如果需要的话）
     */
    /*
     * @Bean
     *
     * @ConditionalOnMissingBean(VideoAlarmRuleManager.class)
     *
     * @ConditionalOnProperty(name = "video.alarm.rule.manager.enabled", havingValue
     * = "true", matchIfMissing = false)
     * public VideoAlarmRuleManager videoAlarmRuleManager(VideoAlarmRuleDao
     * videoAlarmRuleDao) {
     * log.info("[视频配置] 注册VideoAlarmRuleManager Bean");
     * return new VideoAlarmRuleManager(videoAlarmRuleDao);
     * }
     */
}
