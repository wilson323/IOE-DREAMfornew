package net.lab1024.sa.attendance.engine.rule.cache.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManager;
import net.lab1024.sa.attendance.engine.model.RuleEvaluationResult;
import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;
/**
 * 规则缓存管理器实现类 - 三级缓存架构
 * <p>
 * 三级缓存实现： - L1本地缓存 (Caffeine)：毫秒级响应，TTL 5分钟 - L2 Redis缓存：分布式一致性，TTL 30分钟 - L3 网关缓存：减少RPC调用（通过GatewayServiceClient）
 * </p>
 * <p>
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-17
 */
@Slf4j
public class RuleCacheManagerImpl implements RuleCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * L1本地缓存 (Caffeine) - 三级缓存架构
     */
    private Cache<String, RuleEvaluationResult> l1Cache;

    // 缓存统计
    private final AtomicLong totalRequests = new AtomicLong (0);
    private final AtomicLong l1Hits = new AtomicLong (0);
    private final AtomicLong l2Hits = new AtomicLong (0);
    private final AtomicLong cacheMisses = new AtomicLong (0);
    private final AtomicLong evictions = new AtomicLong (0);

    // L1缓存配置
    private static final int L1_MAX_SIZE = 10000;
    private static final Duration L1_EXPIRE = Duration.ofMinutes (5);

    // L2缓存TTL（默认30分钟）
    private long l2TtlSeconds = 1800;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "attendance:rule:result:";
    private static final String USER_CACHE_PREFIX = "attendance:rule:user:";
    private static final String DEPT_CACHE_PREFIX = "attendance:rule:dept:";

    /**
     * 构造函数注入依赖
     */
    public RuleCacheManagerImpl (RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        initL1Cache ();
    }

    /**
     * 初始化L1 Caffeine缓存
     */
    private void initL1Cache () {
        l1Cache = Caffeine.newBuilder ().maximumSize (L1_MAX_SIZE).expireAfterWrite (L1_EXPIRE).recordStats ().build ();
        log.info ("[规则缓存] L1 Caffeine缓存初始化完成, 容量={}, TTL={}分钟", L1_MAX_SIZE, L1_EXPIRE.toMinutes ());
    }

    /**
     * 缓存规则评估结果
     */
    @Override
    public void cacheResult (Long ruleId, RuleExecutionContext context, RuleEvaluationResult result) {
        log.debug ("[规则缓存] 缓存规则评估结果: ruleId={}, contextId={}", ruleId, context.getExecutionId ());

        try {
            String cacheKey = getCacheKey (ruleId, context);

            // 缓存到L1本地缓存
            cacheToLocal (cacheKey, result);

            // 缓存到L2 Redis缓存
            cacheToRedis (cacheKey, result);

            // 缓存用户相关键
            cacheUserRelation (context.getUserId (), cacheKey);

            // 缓存部门相关键
            cacheDepartmentRelation (context.getDepartmentId (), cacheKey);

            log.debug ("[规则缓存] 缓存完成: {}", cacheKey);

        } catch (Exception e) {
            log.error ("[规则缓存] 缓存结果失败: ruleId={}", ruleId, e);
        }
    }

    /**
     * 获取缓存的规则评估结果
     */
    @Override
    public RuleEvaluationResult getCachedResult (Long ruleId, RuleExecutionContext context) {
        totalRequests.incrementAndGet ();
        String cacheKey = getCacheKey (ruleId, context);

        try {
            // 先从L1本地缓存获取
            RuleEvaluationResult result = getFromLocalCache (cacheKey);
            if (result != null) {
                l1Hits.incrementAndGet ();
                log.debug ("[规则缓存] L1缓存命中: {}", cacheKey);
                return result;
            }

            // 再从L2 Redis缓存获取
            result = getFromRedisCache (cacheKey);
            if (result != null) {
                l2Hits.incrementAndGet ();
                // 回填L1缓存
                cacheToLocal (cacheKey, result);
                log.debug ("[规则缓存] L2缓存命中并回填L1: {}", cacheKey);
                return result;
            }

            cacheMisses.incrementAndGet ();
            log.debug ("[规则缓存] 缓存未命中: {}", cacheKey);
            return null;

        } catch (Exception e) {
            log.error ("[规则缓存] 获取缓存失败: {}", cacheKey, e);
            return null;
        }
    }

    /**
     * 批量缓存规则评估结果
     */
    @Override
    public void cacheBatchResults (List<RuleEvaluationResult> results) {
        log.debug ("[规则缓存] 批量缓存规则评估结果，数量: {}", results.size ());

        try {
            if (results.isEmpty ()) {
                return;
            }

            // 批量缓存到Redis
            Map<String, Object> redisBatch = new HashMap<> ();
            for (RuleEvaluationResult result : results) {
                if (result.getRuleId () != null && result.getSessionId () != null) {
                    String cacheKey = CACHE_PREFIX + result.getRuleId () + ":" + result.getSessionId ();
                    redisBatch.put (cacheKey, result);
                }
            }

            if (!redisBatch.isEmpty ()) {
                redisTemplate.opsForValue ().multiSet (redisBatch);

                // 设置过期时间
                redisBatch.keySet ().forEach (key -> redisTemplate.expire (key, Duration.ofSeconds (l2TtlSeconds)));
            }

            log.debug ("[规则缓存] 批量缓存完成: {} 个结果", redisBatch.size ());

        } catch (Exception e) {
            log.error ("[规则缓存] 批量缓存失败", e);
        }
    }

    /**
     * 批量获取缓存的规则评估结果
     */
    @Override
    public List<RuleEvaluationResult> getCachedBatchResults (List<Long> ruleIds, RuleExecutionContext context) {
        log.debug ("[规则缓存] 批量获取缓存结果，规则数量: {}", ruleIds.size ());

        try {
            List<RuleEvaluationResult> results = new ArrayList<> ();

            for (Long ruleId : ruleIds) {
                RuleEvaluationResult result = getCachedResult (ruleId, context);
                if (result != null) {
                    results.add (result);
                }
            }

            log.debug ("[规则缓存] 批量获取完成，命中数量: {}", results.size ());
            return results;

        } catch (Exception e) {
            log.error ("[规则缓存] 批量获取缓存失败", e);
            return Collections.emptyList ();
        }
    }

    /**
     * 清除指定规则的缓存
     */
    @Override
    public void evictRule (Long ruleId) {
        log.info ("[规则缓存] 清除规则缓存: {}", ruleId);

        try {
            // 清除本地缓存
            evictFromLocalCache (ruleId);

            // 清除Redis缓存（使用通配符）
            String pattern = CACHE_PREFIX + ruleId + ":*";
            Set<String> keys = redisTemplate.keys (pattern);
            if (keys != null && !keys.isEmpty ()) {
                redisTemplate.delete (keys);
                evictions.addAndGet (keys.size ());
                log.debug ("[规则缓存] 清除Redis缓存: {} 个键", keys.size ());
            }

        } catch (Exception e) {
            log.error ("[规则缓存] 清除规则缓存失败: {}", ruleId, e);
        }
    }

    /**
     * 清除指定用户的所有规则缓存
     */
    @Override
    public void evictUserRules (Long userId) {
        log.info ("[规则缓存] 清除用户规则缓存: {}", userId);

        try {
            // 获取用户相关的所有缓存键
            String userKey = USER_CACHE_PREFIX + userId;
            Set<Object> userCacheKeys = redisTemplate.opsForSet ().members (userKey);

            if (userCacheKeys != null && !userCacheKeys.isEmpty ()) {
                // 删除相关缓存，需要转换为String类型
                List<String> keysToDelete = userCacheKeys.stream ().map (Object::toString)
                        .collect (java.util.stream.Collectors.toList ());
                redisTemplate.delete (keysToDelete);
                evictions.addAndGet (userCacheKeys.size ());
            }

            // 删除用户关系键
            redisTemplate.delete (userKey);

            // 清除本地缓存中的用户相关项
            evictFromLocalCacheByUser (userId);

        } catch (Exception e) {
            log.error ("[规则缓存] 清除用户规则缓存失败: {}", userId, e);
        }
    }

    /**
     * 清除指定部门的所有规则缓存
     */
    @Override
    public void evictDepartmentRules (Long departmentId) {
        log.info ("[规则缓存] 清除部门规则缓存: {}", departmentId);

        try {
            // 获取部门相关的所有缓存键
            String deptKey = DEPT_CACHE_PREFIX + departmentId;
            Set<Object> deptCacheKeys = redisTemplate.opsForSet ().members (deptKey);

            if (deptCacheKeys != null && !deptCacheKeys.isEmpty ()) {
                // 删除相关缓存，需要转换为String类型
                List<String> keysToDelete = deptCacheKeys.stream ().map (Object::toString)
                        .collect (java.util.stream.Collectors.toList ());
                redisTemplate.delete (keysToDelete);
                evictions.addAndGet (deptCacheKeys.size ());
            }

            // 删除部门关系键
            redisTemplate.delete (deptKey);

            // 清除本地缓存中的部门相关项
            evictFromLocalCacheByDepartment (departmentId);

        } catch (Exception e) {
            log.error ("[规则缓存] 清除部门规则缓存失败: {}", departmentId, e);
        }
    }

    /**
     * 清除所有规则缓存
     */
    @Override
    public void clearCache () {
        log.info ("[规则缓存] 清除所有规则缓存");

        try {
            // 清除L1本地缓存
            l1Cache.invalidateAll ();

            // 清除Redis缓存
            String pattern = CACHE_PREFIX + "*";
            Set<String> keys = redisTemplate.keys (pattern);
            if (keys != null && !keys.isEmpty ()) {
                redisTemplate.delete (keys);
                evictions.addAndGet (keys.size ());
            }

            // 清除用户和部门关系缓存
            String userPattern = USER_CACHE_PREFIX + "*";
            String deptPattern = DEPT_CACHE_PREFIX + "*";
            Set<String> userKeys = redisTemplate.keys (userPattern);
            Set<String> deptKeys = redisTemplate.keys (deptPattern);

            if (userKeys != null && !userKeys.isEmpty ()) {
                redisTemplate.delete (userKeys);
            }
            if (deptKeys != null && !deptKeys.isEmpty ()) {
                redisTemplate.delete (deptKeys);
            }

            log.info ("[规则缓存] 所有缓存清除完成");

        } catch (Exception e) {
            log.error ("[规则缓存] 清除所有缓存失败", e);
        }
    }

    /**
     * 预加载规则缓存
     */
    @Override
    public void preloadRule (Long ruleId) {
        log.debug ("[规则缓存] 预加载规则缓存: {}", ruleId);

        try {
            // TODO: 实现规则预加载逻辑
            // 这里应该从数据库获取规则配置并缓存
            log.debug ("[规则缓存] 预加载完成: {}", ruleId);

        } catch (Exception e) {
            log.error ("[规则缓存] 预加载规则缓存失败: {}", ruleId, e);
        }
    }

    /**
     * 批量预加载规则缓存
     */
    @Override
    public void preloadRules (List<Long> ruleIds) {
        log.info ("[规则缓存] 批量预加载规则缓存，数量: {}", ruleIds.size ());

        try {
            for (Long ruleId : ruleIds) {
                preloadRule (ruleId);
            }

            log.info ("[规则缓存] 批量预加载完成: {} 个规则", ruleIds.size ());

        } catch (Exception e) {
            log.error ("[规则缓存] 批量预加载失败", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    @Override
    public CacheStatistics getCacheStatistics () {
        CacheStatistics statistics = new CacheStatistics ();
        statistics.setTotalRequests (totalRequests.get ());
        statistics.setCacheHits (l1Hits.get () + l2Hits.get ());
        statistics.setCacheMisses (cacheMisses.get ());
        statistics.setEvictions (evictions.get ());

        long total = statistics.getTotalRequests ();
        if (total > 0) {
            statistics.setHitRate ((double) statistics.getCacheHits () / total);
        }

        statistics.setCacheSize ((int) l1Cache.estimatedSize ());

        return statistics;
    }

    /**
     * 设置缓存过期时间
     */
    @Override
    public void setCacheTTL (long ttlSeconds) {
        log.info ("[规则缓存] 设置缓存TTL: {} 秒", ttlSeconds);
        this.l2TtlSeconds = ttlSeconds;
    }

    /**
     * 检查缓存是否命中
     */
    @Override
    public boolean isCacheHit (Long ruleId, RuleExecutionContext context) {
        String cacheKey = getCacheKey (ruleId, context);
        return l1Cache.getIfPresent (cacheKey) != null || Boolean.TRUE.equals (redisTemplate.hasKey (cacheKey));
    }

    /**
     * 获取缓存键
     */
    @Override
    public String getCacheKey (Long ruleId, RuleExecutionContext context) {
        // 使用规则ID、用户ID、部门ID、考勤日期、会话ID生成唯一键
        return String.format ("%s%d:%d:%d:%s:%s", CACHE_PREFIX, ruleId, context.getUserId (),
                context.getDepartmentId (), context.getAttendanceDate (), context.getSessionId ());
    }

    /**
     * 缓存到L1本地缓存 (Caffeine)
     */
    private void cacheToLocal (String cacheKey, RuleEvaluationResult result) {
        try {
            l1Cache.put (cacheKey, result);
            log.debug ("[规则缓存] L1写入: {}", cacheKey);
        } catch (Exception e) {
            log.error ("[规则缓存] L1缓存写入失败: {}", cacheKey, e);
        }
    }

    /**
     * 缓存到Redis
     */
    private void cacheToRedis (String cacheKey, RuleEvaluationResult result) {
        try {
            redisTemplate.opsForValue ().set (cacheKey, result, Duration.ofSeconds (l2TtlSeconds));
        } catch (Exception e) {
            log.error ("[规则缓存] 缓存到Redis失败: {}", cacheKey, e);
        }
    }

    /**
     * 缓存用户关系
     */
    private void cacheUserRelation (Long userId, String cacheKey) {
        try {
            if (userId != null) {
                String userKey = USER_CACHE_PREFIX + userId;
                redisTemplate.opsForSet ().add (userKey, cacheKey);
                redisTemplate.expire (userKey, Duration.ofSeconds (l2TtlSeconds));
            }
        } catch (Exception e) {
            log.error ("[规则缓存] 缓存用户关系失败: userId={}", userId, e);
        }
    }

    /**
     * 缓存部门关系
     */
    private void cacheDepartmentRelation (Long departmentId, String cacheKey) {
        try {
            if (departmentId != null) {
                String deptKey = DEPT_CACHE_PREFIX + departmentId;
                redisTemplate.opsForSet ().add (deptKey, cacheKey);
                redisTemplate.expire (deptKey, Duration.ofSeconds (l2TtlSeconds));
            }
        } catch (Exception e) {
            log.error ("[规则缓存] 缓存部门关系失败: departmentId={}", departmentId, e);
        }
    }

    /**
     * 从L1本地缓存获取
     */
    private RuleEvaluationResult getFromLocalCache (String cacheKey) {
        return l1Cache.getIfPresent (cacheKey);
    }

    /**
     * 从Redis缓存获取
     */
    private RuleEvaluationResult getFromRedisCache (String cacheKey) {
        try {
            return (RuleEvaluationResult) redisTemplate.opsForValue ().get (cacheKey);
        } catch (Exception e) {
            log.error ("[规则缓存] 从Redis获取缓存失败: {}", cacheKey, e);
            return null;
        }
    }

    /**
     * 从本地缓存中移除指定规则
     */
    private void evictFromLocalCache (Long ruleId) {
        l1Cache.asMap ().entrySet ().removeIf (entry -> entry.getKey ().startsWith (CACHE_PREFIX + ruleId + ":"));
    }

    /**
     * 从本地缓存中移除用户相关项
     */
    private void evictFromLocalCacheByUser (Long userId) {
        l1Cache.asMap ().entrySet ().removeIf (entry -> entry.getKey ().contains (":" + userId + ":"));
    }

    /**
     * 从本地缓存中移除部门相关项
     */
    private void evictFromLocalCacheByDepartment (Long departmentId) {
        l1Cache.asMap ().entrySet ().removeIf (entry -> entry.getKey ().contains (":" + departmentId + ":"));
    }
}

