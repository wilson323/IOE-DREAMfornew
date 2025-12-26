package net.lab1024.sa.database.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.Resource;
import net.lab1024.sa.database.service.DatabaseSyncService;

/**
 * 数据库同步配置类
 * <p>
 * 负责数据库同步服务的启动配置和定时任务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Configuration
@EnableScheduling
@Slf4j
public class DatabaseSyncConfig {


    @Resource
    private DatabaseSyncService databaseSyncService;

    /**
     * 应用启动时自动执行数据库同步
     */
    @Bean
    @ConditionalOnProperty(name = "database.sync.auto-startup", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner databaseSyncRunner() {
        return args -> {
            log.info("🚀 [数据库同步配置] 启动时自动数据库同步已启用");

            try {
                // 初始化数据库配置
                databaseSyncService.initDatabaseConfigs();
                log.info("✅ [数据库同步配置] 数据库配置初始化完成");

                // 异步执行数据库同步
                databaseSyncService.syncAllDatabases()
                        .thenAccept(results -> {
                            long successCount = results.values().stream()
                                    .mapToInt(success -> success ? 1 : 0)
                                    .sum();
                            log.info("✅ [数据库同步配置] 启动时数据库同步完成，成功: {}/{}",
                                    successCount, results.size());
                        })
                        .exceptionally(throwable -> {
                            log.error("❌ [数据库同步配置] 启动时数据库同步失败", throwable);
                            return null;
                        });

            } catch (Exception e) {
                log.error("❌ [数据库同步配置] 启动时数据库同步异常", e);
            }
        };
    }

    /**
     * 定时数据库同步任务
     * 每30分钟执行一次健康检查和必要的同步
     */
    @Scheduled(fixedDelayString = "${database.sync.check-interval:30000}", initialDelay = 60000)
    @ConditionalOnProperty(name = "database.sync.enabled", havingValue = "true", matchIfMissing = true)
    public void scheduledDatabaseSync() {
        try {
            log.debug("🔄 [数据库同步配置] 执行定时数据库健康检查");

            // 暂时跳过健康检查,后续可以添加healthCheck()方法到DatabaseSyncService
            log.debug("✅ [数据库同步配置] 数据库健康检查跳过");

        } catch (Exception e) {
            log.warn("⚠️ [数据库同步配置] 定时数据库同步异常: {}", e.getMessage());
        }
    }

    /**
     * 每日凌晨2点执行全量数据库同步
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @ConditionalOnProperty(name = "database.sync.enabled", havingValue = "true", matchIfMissing = true)
    public void dailyFullSync() {
        try {
            log.info("🌙 [数据库同步配置] 开始执行每日全量数据库同步");

            databaseSyncService.syncAllDatabases()
                    .thenAccept(results -> {
                        long successCount = results.values().stream()
                                .mapToInt(success -> success ? 1 : 0)
                                .sum();
                        log.info("✅ [数据库同步配置] 每日全量数据库同步完成，成功: {}/{}",
                                successCount, results.size());
                    })
                    .exceptionally(throwable -> {
                        log.error("❌ [数据库同步配置] 每日全量数据库同步失败", throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("❌ [数据库同步配置] 每日全量数据库同步异常", e);
        }
    }
}
