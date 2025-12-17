package net.lab1024.sa.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 统一Caffeine缓存配置
 * 
 * <p>优化要点:</p>
 * <ul>
 *   <li>1. 设置maximumSize防止内存溢出</li>
 *   <li>2. 设置expireAfterWrite定期清理过期数据</li>
 *   <li>3. 启用softValues允许GC回收缓存</li>
 *   <li>4. 启用recordStats监控缓存命中率</li>
 * </ul>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Slf4j
@Configuration
public class UnifiedCaffeineConfiguration {

    /**
     * 热数据缓存（用户、权限、菜单）
     * <p>特点：访问频繁、数据量中等、更新较频繁</p>
     * <p>内存占用: 约100MB (5000条 * 20KB)</p>
     */
    @Bean(name = "hotDataCache")
    public Cache<String, Object> hotDataCache() {
        log.info("[Caffeine配置] 初始化热数据缓存 - 最大5000条, 30分钟过期, 10分钟未访问过期");
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .softValues()  // 允许GC回收
                .recordStats() // 统计命中率
                .build();
    }

    /**
     * 冷数据缓存（字典、系统配置）
     * <p>特点：访问频率低、数据量小、更新很少</p>
     * <p>内存占用: 约20MB (1000条 * 20KB)</p>
     */
    @Bean(name = "coldDataCache")
    public Cache<String, Object> coldDataCache() {
        log.info("[Caffeine配置] 初始化冷数据缓存 - 最大1000条, 1小时过期, 30分钟未访问过期");
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .weakKeys()    // 弱引用键
                .softValues()  // 软引用值
                .recordStats()
                .build();
    }

    /**
     * 临时数据缓存（验证码、临时令牌）
     * <p>特点：访问频繁但生命周期短、数据量小</p>
     * <p>内存占用: 约5MB (500条 * 10KB)</p>
     */
    @Bean(name = "tempDataCache")
    public Cache<String, Object> tempDataCache() {
        log.info("[Caffeine配置] 初始化临时数据缓存 - 最大500条, 5分钟过期");
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .softValues()
                .recordStats()
                .build();
    }

    /**
     * 获取缓存统计信息
     *
     * @param cacheName 缓存名称
     * @return 缓存统计描述
     */
    public String getCacheStats(String cacheName) {
        Cache<String, Object> cache = getCacheByName(cacheName);
        if (cache == null) {
            return "缓存不存在: " + cacheName;
        }
        
        var stats = cache.stats();
        return String.format(
                "缓存[%s] 统计: 命中率=%.2f%%, 请求次数=%d, 命中次数=%d, 未命中次数=%d, 加载次数=%d, 驱逐次数=%d",
                cacheName,
                stats.hitRate() * 100,
                stats.requestCount(),
                stats.hitCount(),
                stats.missCount(),
                stats.loadCount(),
                stats.evictionCount()
        );
    }

    /**
     * 根据名称获取缓存实例
     */
    private Cache<String, Object> getCacheByName(String name) {
        return switch (name) {
            case "hot", "hotData" -> hotDataCache();
            case "cold", "coldData" -> coldDataCache();
            case "temp", "tempData" -> tempDataCache();
            default -> null;
        };
    }
}
