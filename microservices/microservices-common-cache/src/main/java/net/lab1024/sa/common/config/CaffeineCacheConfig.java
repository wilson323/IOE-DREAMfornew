package net.lab1024.sa.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.vo.UserBasicInfoVO;
import net.lab1024.sa.common.domain.vo.DeviceInfoVO;
import net.lab1024.sa.common.domain.vo.DictDataVO;
import net.lab1024.sa.common.domain.vo.PermissionVO;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine本地缓存配置
 *
 * 三级缓存架构 - L1层（本地缓存）
 *
 * 特点：
 * - 最快的访问速度（内存级别）
 * - 进程内缓存（无网络开销）
 * - 容量有限（需合理设置）
 * - 适合热点数据（高频访问）
 *
 * @author IOE-DREAM Team
 * @since 2025-01-XX
 */
@Slf4j
@EnableCaching
@Configuration
public class CaffeineCacheConfig {

    /**
     * 用户基本信息缓存
     *
     * 配置说明：
     * - initialCapacity=1000: 预分配1000个槽位（避免rehash）
     * - maximumSize=10000: 最多缓存10000个用户
     * - expireAfterWrite=5min: 写入5分钟后过期
     * - weakKeys: 使用弱引用键（GC时自动回收）
     * - recordStats: 记录统计信息（监控命中率）
     */
    @Bean("userBasicInfoCache")
    public Cache<String, UserBasicInfoVO> userBasicInfoCache() {
        log.info("[缓存配置] 初始化用户基本信息缓存");

        return Caffeine.newBuilder()
            .initialCapacity(1000)                   // 初始容量（关键优化）
            .maximumSize(10000)                      // 最大容量
            .expireAfterWrite(5, TimeUnit.MINUTES)   // 写入5分钟后过期
            .expireAfterAccess(3, TimeUnit.MINUTES)  // 访问3分钟后过期
            .weakKeys()                              // 弱引用键（防止内存泄漏）
            .recordStats()                           // 记录统计信息
            .build();
    }

    /**
     * 设备信息缓存
     *
     * 特点：
     * - 设备信息相对稳定（10分钟过期）
     * - 设备数量有限（maximumSize=5000）
     * - 支持快速设备信息查询
     */
    @Bean("deviceInfoCache")
    public Cache<String, DeviceInfoVO> deviceInfoCache() {
        log.info("[缓存配置] 初始化设备信息缓存");

        return Caffeine.newBuilder()
            .initialCapacity(500)                    // 初始容量500
            .maximumSize(5000)                       // 最大容量5000
            .expireAfterWrite(10, TimeUnit.MINUTES)  // 写入10分钟后过期
            .expireAfterAccess(5, TimeUnit.MINUTES)  // 访问5分钟后过期
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * 字典数据缓存
     *
     * 特点：
     * - 字典数据变化很少（30分钟过期）
     * - 数据量小（maximumSize=1000）
     * - 支持快速字典查询
     */
    @Bean("dictDataCache")
    public Cache<String, DictDataVO> dictDataCache() {
        log.info("[缓存配置] 初始化字典数据缓存");

        return Caffeine.newBuilder()
            .initialCapacity(200)
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)  // 写入30分钟后过期
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * 权限数据缓存
     *
     * 特点：
     * - 权限数据敏感（5分钟过期）
     * - 用户数量多（maximumSize=10000）
     * - 支持快速权限验证
     */
    @Bean("permissionCache")
    public Cache<String, PermissionVO> permissionCache() {
        log.info("[缓存配置] 初始化权限数据缓存");

        return Caffeine.newBuilder()
            .initialCapacity(1000)
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * Token黑名单缓存
     *
     * 特点：
     * - 黑名单数据短期有效（15分钟过期）
     * - 数据量小（maximumSize=500）
     * - 支持快速Token验证
     */
    @Bean("tokenBlacklistCache")
    public Cache<String, Boolean> tokenBlacklistCache() {
        log.info("[缓存配置] 初始化Token黑名单缓存");

        return Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(500)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * 部门信息缓存
     *
     * 特点：
     * - 部门数据相对稳定（20分钟过期）
     * - 部门数量有限（maximumSize=500）
     */
    @Bean("departmentInfoCache")
    public Cache<Long, Object> departmentInfoCache() {
        log.info("[缓存配置] 初始化部门信息缓存");

        return Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(500)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * 区域信息缓存
     *
     * 特点：
     * - 区域数据稳定（30分钟过期）
     * - 区域数量有限（maximumSize=200）
     */
    @Bean("areaInfoCache")
    public Cache<Long, Object> areaInfoCache() {
        log.info("[缓存配置] 初始化区域信息缓存");

        return Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(200)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }
}
