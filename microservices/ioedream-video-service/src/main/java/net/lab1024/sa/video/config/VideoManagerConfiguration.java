package net.lab1024.sa.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.video.dao.VideoStreamDao;
import net.lab1024.sa.video.dao.VideoMonitorDao;
import net.lab1024.sa.video.dao.VideoPTZDao;
import net.lab1024.sa.video.dao.VideoAlarmRuleDao;
import net.lab1024.sa.video.manager.VideoStreamManager;
import net.lab1024.sa.video.manager.VideoMonitorManager;
// import net.lab1024.sa.video.manager.VideoPTZManager; // 已移除

/**
 * 视频模块Manager配置类
 * <p>
 * 配置视频模块的Manager层Bean，注册为Spring容器管理的组件
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
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
    public VideoStreamManager videoStreamManager(VideoStreamDao videoStreamDao) {
        log.info("[视频配置] 注册VideoStreamManager Bean");
        return new VideoStreamManager(videoStreamDao);
    }

    /**
     * 注册监控会话管理器
     *
     * @param videoMonitorDao 监控会话数据访问层
     * @return 监控会话管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(VideoMonitorManager.class)
    public VideoMonitorManager videoMonitorManager(VideoMonitorDao videoMonitorDao) {
        log.info("[视频配置] 注册VideoMonitorManager Bean");
        return new VideoMonitorManager(videoMonitorDao);
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
    @Bean
    @ConditionalOnMissingBean(VideoAlarmRuleManager.class)
    @ConditionalOnProperty(name = "video.alarm.rule.manager.enabled", havingValue = "true", matchIfMissing = false)
    public VideoAlarmRuleManager videoAlarmRuleManager(VideoAlarmRuleDao videoAlarmRuleDao) {
        log.info("[视频配置] 注册VideoAlarmRuleManager Bean");
        return new VideoAlarmRuleManager(videoAlarmRuleDao);
    }
    */
}

