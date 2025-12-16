package net.lab1024.sa.consume.service;

/**
 * 消费区域缓存服务接口
 * <p>
 * 使用Spring Cache注解（@Cacheable、@CacheEvict、@CachePut）替代直接使用CacheService
 * 严格遵循CLAUDE.md规范：
 * - 统一使用Spring Cache + Caffeine + Redis
 * - 禁止使用自定义CacheManager
 * - 在Service层处理缓存逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
public interface ConsumeAreaCacheService {

    /**
     * 验证用户是否有权限在指定区域消费（带缓存）
     * <p>
     * 使用Spring Cache注解进行缓存，缓存键格式：perm:area:{accountId}:{areaId}
     * 缓存过期时间：30分钟
     * </p>
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    boolean validateAreaPermission(Long accountId, String areaId);

    /**
     * 清除指定账户和区域的权限缓存
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     */
    void evictAreaPermissionCache(Long accountId, String areaId);

    /**
     * 清除指定账户的所有区域权限缓存
     *
     * @param accountId 账户ID
     */
    void evictAccountAreaPermissionCache(Long accountId);
}




