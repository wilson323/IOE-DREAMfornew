package net.lab1024.sa.data.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.data.service.CacheWarmupService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 应用启动Runner
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Configuration
public class ApplicationStartupRunner implements CommandLineRunner {

    @Resource
    private CacheWarmupService cacheWarmupService;

    @Override
    public void run(String... args) throws Exception {
        log.info("=================================================");
        log.info("【应用启动】IOE-DREAM数据分析服务启动完成");
        log.info("【应用启动】启动时间: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("【应用启动】开始执行启动后任务...");

        // 触发缓存预热（异步执行）
        cacheWarmupService.warmupCacheAsync();

        log.info("【应用启动】缓存预热任务已提交（异步执行）");
        log.info("=================================================");
    }
}
