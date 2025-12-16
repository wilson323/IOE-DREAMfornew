package net.lab1024.sa.consume.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;
import net.lab1024.sa.consume.service.ConsumeAreaCacheService;

/**
 * 消费区域缓存服务实现类
 * <p>
 * 使用Spring Cache注解（@Cacheable、@CacheEvict、@CachePut）替代直接使用CacheService
 * 严格遵循CLAUDE.md规范：
 * - 统一使用Spring Cache + Caffeine + Redis
 * - 禁止使用自定义CacheManager
 * - 缓存配置在LightCacheConfiguration中统一管理
 * </p>
 * <p>
 * 缓存配置：
 * - 缓存名称：consume:area:permission
 * - 缓存过期时间：30分钟（在LightCacheConfiguration中配置）
 * - 缓存键格式：perm:area:{accountId}:{areaId}
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ConsumeAreaCacheServiceImpl implements ConsumeAreaCacheService {

    @Resource
    private ConsumeAreaManager consumeAreaManager;

    /**
     * 验证用户是否有权限在指定区域消费（带缓存）
     * <p>
     * 使用Spring Cache注解进行缓存：
     * - @Cacheable：如果缓存中存在，直接返回；否则调用Manager方法并将结果缓存
     * - unless = "#result == null"：只有当结果不为null时才缓存
     * </p>
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    @Override
    @Observed(name = "consume.areaCache.validateAreaPermission", contextualName = "consume-area-cache-validate-permission")
    @Cacheable(value = "consume:area:permission", key = "'perm:area:' + #accountId + ':' + #areaId")
    public boolean validateAreaPermission(Long accountId, String areaId) {
        log.debug("[区域缓存服务] 从Manager查询区域权限（无缓存），accountId={}, areaId={}", accountId, areaId);
        // 调用Manager的validateAreaPermission方法（无缓存版本，缓存由Spring Cache注解处理）
        return consumeAreaManager.validateAreaPermission(accountId, areaId);
    }

    /**
     * 清除指定账户和区域的权限缓存
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     */
    @Override
    @Observed(name = "consume.areaCache.evictAreaPermissionCache", contextualName = "consume-area-cache-evict-permission")
    @CacheEvict(value = "consume:area:permission", key = "'perm:area:' + #accountId + ':' + #areaId")
    public void evictAreaPermissionCache(Long accountId, String areaId) {
        log.debug("[区域缓存服务] 清除区域权限缓存，accountId={}, areaId={}", accountId, areaId);
        // Spring Cache会自动处理缓存清除
    }

    /**
     * 清除指定账户的所有区域权限缓存
     * <p>
     * 注意：Spring Cache不支持按前缀清除缓存，此方法需要手动实现
     * 或者使用RedisTemplate直接操作Redis
     * </p>
     *
     * @param accountId 账户ID
     */
    @Override
    @Observed(name = "consume.areaCache.evictAccountAreaPermissionCache", contextualName = "consume-area-cache-evict-account")
    @CacheEvict(value = "consume:area:permission", allEntries = true)
    public void evictAccountAreaPermissionCache(Long accountId) {
        log.debug("[区域缓存服务] 清除账户所有区域权限缓存，accountId={}", accountId);
        // 清除所有缓存（因为Spring Cache不支持按前缀清除，这里清除所有）
        // 如果需要更精确的控制，可以使用RedisTemplate直接操作
    }
}




