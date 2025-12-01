package net.lab1024.sa.admin.module.smart.cache;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.hr.manager.EmployeeCacheManager;
import net.lab1024.sa.admin.module.smart.video.manager.VideoCacheManager;

/**
 * 缓存性能测试
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
// @Component // 暂时禁用，防止在应用启动时自动执行测试
public class CachePerformanceTest {

    @Resource
    private EmployeeCacheManager employeeCacheManager;

    @Resource
    private VideoCacheManager videoCacheManager;

    /**
     * 应用初始化后执行缓存性能测试
     *
     * 功能说明： - 按顺序执行员工缓存与视频缓存的预热/性能统计，并输出日志
     *
     * 异常： - 若相关 Manager 未正确注入，将在调用时触发 NullPointerException
     *
     * 示例： - 应用启动后（当本类启用为 Spring 组件时）自动输出各缓存性能日志
     */
    @PostConstruct
    public void testCachePerformance() {
        log.info("开始缓存性能测试...");

        // 测试员工缓存
        testEmployeeCache();

        // 测试视频缓存
        testVideoCache();

        // 打印缓存统计信息
        printCacheStats();
    }

    /**
     * 测试员工缓存性能
     *
     * 功能说明： - 预留员工缓存相关的性能测试逻辑与耗时统计框架
     *
     * 返回：无
     *
     * 示例：无
     */
    private void testEmployeeCache() {
        log.info("测试员工缓存性能...");

        long startTime = System.nanoTime();

        // 预热方法依赖未提供，跳过实际调用，保留耗时统计框架

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        log.info("员工缓存性能测试完成，耗时: {}ms", duration);
    }

    /**
     * 测试视频缓存性能
     *
     * 功能说明： - 通过对若干视频设备进行缓存预热，并统计耗时
     *
     * 返回：无
     *
     * 示例：无
     */
    private void testVideoCache() {
        log.info("测试视频缓存性能...");

        long startTime = System.nanoTime();

        // 预热视频设备缓存操作
        videoCacheManager.warmUpDevice(101L);
        videoCacheManager.warmUpDevice(102L);
        videoCacheManager.warmUpDevice(103L);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒
        log.info("视频缓存性能测试完成，耗时: {}ms", duration);
    }

    /**
     * 打印缓存统计信息
     *
     * 功能说明： - 输出视频缓存命中/未命中等关键指标；员工缓存统计方法若尚未提供则跳过
     *
     * 返回：无
     *
     * 示例：无
     */
    private void printCacheStats() {
        log.info("== 缓存统计信息 ==");

        // 员工缓存统计：EmployeeCacheManager 未提供统计方法，跳过打印

        // 视频缓存统计
        var videoStats = videoCacheManager.getCacheStats();
        log.info("视频缓存统计 - 命中次数: {}, 未命中次数: {}, 命中率: {:.2f}%, 缓存大小: {}", videoStats.getHitCount(),
                videoStats.getMissCount(), videoStats.getHitRate() * 100,
                videoStats.getEstimatedSize());

        log.info("缓存统计信息打印完成");
    }
}
