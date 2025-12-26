package net.lab1024.sa.video.config;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoDecoderDao;
import net.lab1024.sa.video.dao.VideoDisplayTaskDao;
import net.lab1024.sa.video.dao.VideoWallDao;
import net.lab1024.sa.video.dao.VideoWallPresetDao;
import net.lab1024.sa.video.dao.VideoWallWindowDao;
import net.lab1024.sa.video.manager.DecoderManager;
import net.lab1024.sa.video.manager.VideoWallManager;
import net.lab1024.sa.video.manager.WallTaskManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * 解码上墙模块配置类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 在配置类中注册Manager Bean
 * - 使用@ConditionalOnMissingBean避免重复注册
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
 public class VideoWallConfig {

    @Resource
    private VideoWallDao videoWallDao;

    @Resource
    private VideoWallWindowDao videoWallWindowDao;

    @Resource
    private VideoWallPresetDao videoWallPresetDao;

    @Resource
    private VideoDecoderDao videoDecoderDao;

    @Resource
    private VideoDisplayTaskDao videoDisplayTaskDao;

    /**
     * 电视墙管理器Bean
     */
    @Bean
    @ConditionalOnMissingBean(VideoWallManager.class)
    public VideoWallManager videoWallManager() {
        log.info("[解码上墙配置] 创建电视墙管理器Bean");
        return new VideoWallManager(videoWallDao, videoWallWindowDao, videoWallPresetDao);
    }

    /**
     * 解码器管理器Bean
     */
    @Bean
    @ConditionalOnMissingBean(DecoderManager.class)
    public DecoderManager decoderManager() {
        log.info("[解码上墙配置] 创建解码器管理器Bean");
        return new DecoderManager(videoDecoderDao);
    }

    /**
     * 上墙任务管理器Bean
     */
    @Bean
    @ConditionalOnMissingBean(WallTaskManager.class)
    public WallTaskManager wallTaskManager() {
        log.info("[解码上墙配置] 创建上墙任务管理器Bean");
        return new WallTaskManager(videoDisplayTaskDao, videoWallWindowDao);
    }
}


