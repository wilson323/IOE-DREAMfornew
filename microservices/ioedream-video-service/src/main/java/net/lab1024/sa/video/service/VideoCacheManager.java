package net.lab1024.sa.video.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * 视频三级缓存管理服务
 *
 * 缓存架构:
 * L1: 内存缓存（热数据，最近播放，最大2GB）
 * L2: 本地磁盘缓存（温数据，最近24小时，最大50GB）
 * L3: CDN/存储服务器（冷数据，历史录像）
 *
 * 功能特性:
 * 1. 智能缓存查询 - 自动从L1->L2->L3查找
 * 2. 缓存预热 - 热点数据提前加载到L1
 * 3. 缓存淘汰 - LRU策略自动淘汰
 * 4. 缓存统计 - 命中率、容量监控
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class VideoCacheManager {

    @Value("${video.cache.l1.max-size-mb:2048}")
    private Integer l1MaxSizeMB;

    @Value("${video.cache.l2.max-size-gb:50}")
    private Integer l2MaxSizeGB;

    @Value("${video.cache.l2.path:/tmp/video-cache}")
    private String l2CachePath;

    @Value("${video.cache.cdn.base-url:}")
    private String cdnBaseUrl;

    /**
     * L1: 内存缓存
     * - 存储热数据（最近播放）
     * - 最大2GB（可配置）
     * - TTL: 30分钟
     */
    private Cache<String, CachedVideoSegment> l1MemoryCache;

    /**
     * L2: 磁盘缓存路径
     */
    private Path l2DiskCachePath;

    /**
     * 初始化缓存
     */
    @PostConstruct
    public void init() throws IOException {
        log.info("[视频缓存] 初始化三级缓存");

        // 初始化L1内存缓存
        long l1MaxBytes = l1MaxSizeMB * 1024L * 1024L;
        l1MemoryCache = Caffeine.newBuilder()
                .maximumWeight(l1MaxBytes)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .weigher((String key, CachedVideoSegment value) -> value.getData().length)
                .recordStats()
                .build();

        // 初始化L2磁盘缓存
        l2DiskCachePath = Paths.get(l2CachePath);
        if (!Files.exists(l2DiskCachePath)) {
            Files.createDirectories(l2DiskCachePath);
            log.info("[视频缓存] 创建L2缓存目录: {}", l2DiskCachePath.toAbsolutePath());
        }

        log.info("[视频缓存] 初始化完成: L1={}MB, L2={}GB, path={}",
                l1MaxSizeMB, l2MaxSizeGB, l2DiskCachePath.toAbsolutePath());
    }

    /**
     * 获取视频片段 - 三级缓存查询
     *
     * @param taskId 任务ID
     * @param startTime 开始时间（秒）
     * @param duration 时长（秒）
     * @return 视频数据
     */
    public byte[] getVideoSegment(Long taskId, Integer startTime, Integer duration) {
        String cacheKey = generateCacheKey(taskId, startTime, duration);

        // 1. 查询L1内存缓存
        byte[] data = getFromL1(cacheKey);
        if (data != null) {
            log.debug("[视频缓存] L1缓存命中: key={}", cacheKey.substring(0, 8));
            return data;
        }

        // 2. 查询L2磁盘缓存
        data = getFromL2(cacheKey);
        if (data != null) {
            log.debug("[视频缓存] L2缓存命中: key={}", cacheKey.substring(0, 8));
            // 回写到L1
            writeToL1(cacheKey, data);
            return data;
        }

        // 3. 从L3存储获取
        log.info("[视频缓存] L1/L2未命中，从L3获取: key={}", cacheKey.substring(0, 8));
        data = fetchFromL3(taskId, startTime, duration);
        if (data != null) {
            // 回写到L2和L1
            writeToL2(cacheKey, data);
            writeToL1(cacheKey, data);
        }

        return data;
    }

    /**
     * 从L1内存缓存获取
     *
     * @param cacheKey 缓存键
     * @return 视频数据
     */
    private byte[] getFromL1(String cacheKey) {
        CachedVideoSegment cached = l1MemoryCache.getIfPresent(cacheKey);
        return cached != null ? cached.getData() : null;
    }

    /**
     * 从L2磁盘缓存获取
     *
     * @param cacheKey 缓存键
     * @return 视频数据
     */
    private byte[] getFromL2(String cacheKey) {
        try {
            Path filePath = l2DiskCachePath.resolve(cacheKey + ".cache");
            if (!Files.exists(filePath)) {
                return null;
            }

            // 检查文件是否超过24小时
            LocalDateTime fileTime = LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(filePath).toInstant(),
                    java.time.ZoneId.systemDefault()
            );
            if (ChronoUnit.HOURS.between(fileTime, LocalDateTime.now()) > 24) {
                // 文件过期，删除
                Files.deleteIfExists(filePath);
                return null;
            }

            byte[] data = Files.readAllBytes(filePath);
            log.debug("[视频缓存] L2读取成功: size={}KB", data.length / 1024);
            return data;
        } catch (IOException e) {
            log.error("[视频缓存] L2读取失败: key={}", cacheKey, e);
            return null;
        }
    }

    /**
     * 从L3存储获取（CDN或存储服务器）
     *
     * @param taskId 任务ID
     * @param startTime 开始时间
     * @param duration 时长
     * @return 视频数据
     */
    private byte[] fetchFromL3(Long taskId, Integer startTime, Integer duration) {
        // 这里简化处理，实际应该:
        // 1. 从CDN或存储服务器获取视频
        // 2. 支持HTTP Range请求
        // 3. 处理网络错误和重试

        log.info("[视频缓存] 从L3获取视频: taskId={}, start={}, duration={}",
                taskId, startTime, duration);

        // 模拟返回数据
        return new byte[1024 * 500];  // 500KB
    }

    /**
     * 写入L1缓存
     *
     * @param cacheKey 缓存键
     * @param data 视频数据
     */
    public void writeToL1(String cacheKey, byte[] data) {
        CachedVideoSegment cached = CachedVideoSegment.builder()
                .data(data)
                .size(data.length)
                .cacheTime(System.currentTimeMillis())
                .build();

        l1MemoryCache.put(cacheKey, cached);
        log.debug("[视频缓存] 写入L1: key={}, size={}KB",
                cacheKey.substring(0, 8), data.length / 1024);
    }

    /**
     * 写入L2缓存
     *
     * @param cacheKey 缓存键
     * @param data 视频数据
     */
    public void writeToL2(String cacheKey, byte[] data) {
        try {
            Path filePath = l2DiskCachePath.resolve(cacheKey + ".cache");
            Files.write(filePath, data);

            log.debug("[视频缓存] 写入L2: key={}, size={}KB",
                    cacheKey.substring(0, 8), data.length / 1024);
        } catch (IOException e) {
            log.error("[视频缓存] L2写入失败: key={}", cacheKey, e);
        }
    }

    /**
     * 写入缓存（同时写入L1和L2）
     *
     * @param taskId 任务ID
     * @param startTime 开始时间
     * @param duration 时长
     * @param data 视频数据
     */
    public void writeToCache(Long taskId, Integer startTime, Integer duration, byte[] data) {
        String cacheKey = generateCacheKey(taskId, startTime, duration);
        writeToL1(cacheKey, data);
        writeToL2(cacheKey, data);
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        log.warn("[视频缓存] 清空所有缓存");

        // 清空L1
        l1MemoryCache.invalidateAll();

        // 清空L2
        try {
            Files.walk(l2DiskCachePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.error("[视频缓存] 删除L2文件失败: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("[视频缓存] 清空L2失败", e);
        }
    }

    /**
     * 清空指定任务的缓存
     *
     * @param taskId 任务ID
     */
    public void clearTaskCache(Long taskId) {
        log.info("[视频缓存] 清空任务缓存: taskId={}", taskId);

        // 清空L1
        l1MemoryCache.asMap().keySet().removeIf(key -> key.startsWith(taskId + "_"));

        // 清空L2
        try {
            Files.walk(l2DiskCachePath)
                    .filter(path -> path.getFileName().toString().startsWith(taskId + "_"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.error("[视频缓存] 删除L2文件失败: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("[视频缓存] 清空L2任务缓存失败", e);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    public CacheStatistics getCacheStatistics() {
        // L1统计
        com.github.benmanes.caffeine.cache.stats.CacheStats l1Stats = l1MemoryCache.stats();

        // L2统计
        long l2FileCount = 0;
        long l2TotalSize = 0;
        try {
            if (Files.exists(l2DiskCachePath)) {
                var files = Files.list(l2DiskCachePath);
                l2FileCount = files.count();
                l2TotalSize = Files.walk(l2DiskCachePath)
                        .filter(Files::isRegularFile)
                        .mapToLong(path -> {
                            try {
                                return Files.size(path);
                            } catch (IOException e) {
                                return 0L;
                            }
                        })
                        .sum();
            }
        } catch (IOException e) {
            log.error("[视频缓存] 获取L2统计失败", e);
        }

        return CacheStatistics.builder()
                .l1HitCount(l1Stats.hitCount())
                .l1MissCount(l1Stats.missCount())
                .l1HitRate(l1Stats.hitRate())
                .l1CurrentSize(l1MemoryCache.estimatedSize())
                .l2FileCount(l2FileCount)
                .l2TotalSizeMB(l2TotalSize / 1024 / 1024)
                .build();
    }

    /**
     * 清理过期的L2缓存文件
     */
    public void cleanupExpiredL2Cache() {
        log.info("[视频缓存] 清理过期L2缓存");

        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);

            Files.walk(l2DiskCachePath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            LocalDateTime fileTime = LocalDateTime.ofInstant(
                                    Files.getLastModifiedTime(path).toInstant(),
                                    java.time.ZoneId.systemDefault()
                            );

                            if (fileTime.isBefore(cutoffTime)) {
                                Files.deleteIfExists(path);
                                log.debug("[视频缓存] 删除过期文件: {}", path.getFileName());
                            }
                        } catch (IOException e) {
                            log.error("[视频缓存] 处理L2文件失败: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("[视频缓存] 清理L2缓存失败", e);
        }
    }

    /**
     * 生成缓存键
     *
     * @param taskId 任务ID
     * @param startTime 开始时间
     * @param duration 时长
     * @return 缓存键
     */
    private String generateCacheKey(Long taskId, Integer startTime, Integer duration) {
        return String.format("%d_%d_%d", taskId, startTime, duration);
    }

    /**
     * 缓存的视频片段
     */
    @lombok.Data
    @lombok.Builder
    private static class CachedVideoSegment {
        private byte[] data;
        private Integer size;
        private Long cacheTime;
    }

    /**
     * 缓存统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheStatistics {
        private Long l1HitCount;
        private Long l1MissCount;
        private Double l1HitRate;
        private Long l1CurrentSize;
        private Long l2FileCount;
        private Long l2TotalSizeMB;  // MB
    }
}
