package net.lab1024.sa.video.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.manager.VideoBehaviorManager;
import net.lab1024.sa.video.dao.VideoBehaviorDao;
import net.lab1024.sa.video.dao.VideoBehaviorPatternDao;
import net.lab1024.sa.video.entity.VideoBehaviorPatternEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 视频行为分析配置类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Slf4j
@Configuration
@EnableScheduling
public class VideoBehaviorConfig {

    @Resource
    private VideoBehaviorDao videoBehaviorDao;

    @Resource
    private VideoBehaviorPatternDao videoBehaviorPatternDao;

    /**
     * 视频行为管理器Bean
     */
    @Bean
    public VideoBehaviorManager videoBehaviorManager() {
        log.info("[行为分析配置] 创建视频行为管理器Bean");
        return new VideoBehaviorManager(videoBehaviorDao, videoBehaviorPatternDao);
    }

    /**
     * 行为分析专用ObjectMapper
     */
    @Bean("behaviorObjectMapper")
    public ObjectMapper behaviorObjectMapper() {
        log.info("[行为分析配置] 创建行为分析专用ObjectMapper");
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java 8时间序列化器
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Long类型序列化为字符串，防止前端精度丢失
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);

        mapper.registerModule(module);

        return mapper;
    }

    /**
     * 行为检测统计分析定时任务
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void behaviorStatisticsTask() {
        try {
            log.info("[行为分析定时任务] 开始执行行为统计分析");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);

            // 调用存储过程更新统计
            videoBehaviorDao.updateBehaviorStatistics(yesterday, "daily");

            log.info("[行为分析定时任务] 行为统计分析完成");

        } catch (Exception e) {
            log.error("[行为分析定时任务] 行为统计分析执行失败", e);
        }
    }

    /**
     * 行为模式训练计划检查定时任务
     * 每小时检查一次是否需要重新训练
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void patternTrainingScheduleTask() {
        try {
            log.info("[行为分析定时任务] 检查行为模式训练计划");

            LocalDateTime now = LocalDateTime.now();
            List<VideoBehaviorPatternEntity> patterns = videoBehaviorPatternDao.selectPatternsNeedingRetraining(now);

            if (!patterns.isEmpty()) {
                log.info("[行为分析定时任务] 发现{}个需要重新训练的模式", patterns.size());

                // 这里可以触发异步训练任务
                // patternTrainingService.triggerBatchTraining(patterns);
            }

            log.info("[行为分析定时任务] 行为模式训练计划检查完成");

        } catch (Exception e) {
            log.error("[行为分析定时任务] 行为模式训练计划检查失败", e);
        }
    }

    /**
     * 行为数据清理定时任务
     * 每天凌晨2点执行，清理90天前的数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void behaviorDataCleanupTask() {
        try {
            log.info("[行为分析定时任务] 开始执行行为数据清理");

            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(90);
            int deletedCount = videoBehaviorDao.cleanOldBehaviors(cutoffTime);

            log.info("[行为分析定时任务] 行为数据清理完成，清理了{}条记录", deletedCount);

        } catch (Exception e) {
            log.error("[行为分析定时任务] 行为数据清理执行失败", e);
        }
    }

    /**
     * 行为模式过期检查定时任务
     * 每天凌晨3点执行，清理过期模式
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void patternExpirationCheckTask() {
        try {
            log.info("[行为分析定时任务] 开始检查行为模式过期状态");

            LocalDateTime now = LocalDateTime.now();
            int expiredCount = videoBehaviorPatternDao.cleanExpiredPatterns(now);

            log.info("[行为分析定时任务] 行为模式过期检查完成，更新了{}个过期模式", expiredCount);

        } catch (Exception e) {
            log.error("[行为分析定时任务] 行为模式过期检查执行失败", e);
        }
    }

    /**
     * 索引表同步定时任务
     * 每5分钟执行一次，同步行为检测记录到索引表
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void behaviorIndexSyncTask() {
        try {
            log.debug("[行为分析定时任务] 开始同步行为检测索引");

            // 同步最近5分钟的数据
            LocalDateTime startTime = LocalDateTime.now().minusMinutes(5);
            int syncedCount = videoBehaviorDao.syncBehaviorIndex(startTime);

            if (syncedCount > 0) {
                log.info("[行为分析定时任务] 同步了{}条行为检测记录到索引表", syncedCount);
            }

        } catch (Exception e) {
            log.error("[行为分析定时任务] 行为检测索引同步失败", e);
        }
    }
}