package net.lab1024.sa.data.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.data.dao.ReportDao;
import net.lab1024.sa.common.entity.data.ReportEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 缓存预热服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class CacheWarmupService {

    @Resource
    private ReportDao reportDao;

    @Resource
    private ReportService reportService;

    /**
     * 应用启动时预热缓存
     */
    @Async
    public CompletableFuture<Void> warmupCacheAsync() {
        return CompletableFuture.runAsync(() -> {
            log.info("[缓存预热] 开始预热报表缓存");

            try {
                // 查询所有启用的报表
                List<ReportEntity> reports = reportDao.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReportEntity>()
                        .eq(ReportEntity::getStatus, "active")
                );

                if (reports.isEmpty()) {
                    log.info("[缓存预热] 没有需要预热的报表");
                    return;
                }

                log.info("[缓存预热] 找到{}个启用的报表，开始预热", reports.size());

                // 预加载报表数据
                List<Long> reportIds = reports.stream()
                        .map(ReportEntity::getReportId)
                        .collect(Collectors.toList());

                int successCount = 0;
                int failCount = 0;

                for (Long reportId : reportIds) {
                    try {
                        // 触发缓存加载
                        reportService.getReportById(reportId);
                        successCount++;
                    } catch (Exception e) {
                        log.error("[缓存预热] 预热报表失败: reportId={}", reportId, e);
                        failCount++;
                    }
                }

                log.info("[缓存预热] 缓存预热完成: total={}, success={}, fail={}",
                         reportIds.size(), successCount, failCount);
            } catch (Exception e) {
                log.error("[缓存预热] 缓存预热失败", e);
            }
        });
    }

    /**
     * 手动触发缓存预热
     */
    public void warmupCacheManually() {
        log.info("[缓存预热] 手动触发缓存预热");

        CompletableFuture<Void> future = warmupCacheAsync();

        // 等待预热完成
        future.join();

        log.info("[缓存预热] 手动预热完成");
    }
}
