package net.lab1024.sa.access.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.EdgeOfflineRecordReplayService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.Resource;

/**
 * 边缘验证离线记录补录定时任务配置
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Configuration注解
 * - 使用@EnableScheduling启用定时任务
 * - 使用@ConditionalOnProperty控制是否启用
 * - 使用@Scheduled定义定时任务
 * </p>
 * <p>
 * 核心职责：
 * - 定时补录Redis中缓存的离线记录
 * - 网络恢复后自动同步离线记录到数据库
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "access.verification.edge.offline-enabled", havingValue = "true", matchIfMissing = true)
public class EdgeOfflineRecordReplayConfig {

    @Resource
    private EdgeOfflineRecordReplayService edgeOfflineRecordReplayService;

    /**
     * 定时补录离线记录
     * <p>
     * 每5分钟执行一次，补录Redis中缓存的离线记录
     * </p>
     */
    @Scheduled(fixedDelayString = "${access.verification.edge.replay-interval:300000}", initialDelay = 60000)
    public void scheduledReplayOfflineRecords() {
        try {
            log.debug("[离线记录补录] 定时任务开始执行");
            edgeOfflineRecordReplayService.replayOfflineRecords();
            log.debug("[离线记录补录] 定时任务执行完成");
        } catch (Exception e) {
            log.error("[离线记录补录] 定时任务执行异常: error={}", e.getMessage(), e);
        }
    }
}
