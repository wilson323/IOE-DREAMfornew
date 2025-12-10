package net.lab1024.sa.common.system.manager;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.system.dao.SystemDictDao;
import net.lab1024.sa.common.system.domain.entity.SystemDictEntity;

/**
 * 字典Manager
 * 符合CLAUDE.md规范 - Manager层
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 *
 * 职责：
 * - 字典缓存管理
 * - 字典树形结构构建
 * - 字典数据验证
 * - 字典批量操作
 *
 * 企业级特性：
 * - 多级缓存（L1本地 + L2 Redis）
 * - 字典树形结构
 * - 字典数据验证
 * - 批量操作优化
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
@SuppressWarnings("null")
public class DictManager {

    private final SystemDictDao systemDictDao;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param systemDictDao 系统字典DAO
     * @param redisTemplate Redis模板
     */
    public DictManager(SystemDictDao systemDictDao, RedisTemplate<String, Object> redisTemplate) {
        this.systemDictDao = systemDictDao;
        this.redisTemplate = redisTemplate;
    }

    // L1本地缓存
    private final ConcurrentHashMap<String, List<SystemDictEntity>> localCache = new ConcurrentHashMap<>();

    private static final String DICT_CACHE_PREFIX = "system:dict:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    /**
     * 获取字典列表（多级缓存）
     *
     * 企业级特性：
     * - L1本地缓存（毫秒级）
     * - L2 Redis缓存（10ms级）
     * - L3数据库（100ms级）
     */
    public List<SystemDictEntity> getDictList(String dictTypeCode) {
        // L1本地缓存
        List<SystemDictEntity> list = localCache.get(dictTypeCode);
        if (list != null && !list.isEmpty()) {
            log.debug("从L1本地缓存获取字典 - typeCode: {}", dictTypeCode);
            return list;
        }

        // L2 Redis缓存
        String cacheKey = DICT_CACHE_PREFIX + dictTypeCode;
        @SuppressWarnings("unchecked")
        List<SystemDictEntity> cachedList = (List<SystemDictEntity>) redisTemplate.opsForValue().get(cacheKey);
        list = cachedList;
        if (list != null && !list.isEmpty()) {
            localCache.put(dictTypeCode, list);
            log.debug("从L2 Redis缓存获取字典 - typeCode: {}", dictTypeCode);
            return list;
        }

        // L3数据库
        list = systemDictDao.selectEnabledByTypeCode(dictTypeCode);
        if (list != null && !list.isEmpty()) {
            // 写入缓存
            localCache.put(dictTypeCode, list);
            redisTemplate.opsForValue().set(cacheKey, list, CACHE_TTL);

            log.debug("从数据库获取字典 - typeCode: {}, 数量: {}", dictTypeCode, list.size());
        }

        return list;
    }

    /**
     * 构建字典树形结构
     */
    public List<Map<String, Object>> buildDictTree(String dictTypeCode) {
        List<SystemDictEntity> list = getDictList(dictTypeCode);

        return list.stream()
                .map(dict -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("value", dict.getDictValue());
                    node.put("label", dict.getDictLabel());
                    node.put("sortOrder", dict.getSortOrder());
                    node.put("isDefault", dict.getIsDefault());
                    return node;
                })
                .collect(Collectors.toList());
    }

    /**
     * 刷新字典缓存
     *
     * 企业级特性：
     * - 清除所有级别缓存
     * - 支持动态刷新
     */
    public void refreshDictCache() {
        log.info("开始刷新字典缓存");

        // 清除L1本地缓存
        localCache.clear();

        // 清除L2 Redis缓存
        redisTemplate.delete(redisTemplate.keys(DICT_CACHE_PREFIX + "*"));

        log.info("字典缓存刷新完成");
    }

    /**
     * 刷新指定字典类型
     */
    public void refreshDict(String dictTypeCode) {
        log.info("刷新字典 - typeCode: {}", dictTypeCode);

        localCache.remove(dictTypeCode);
        redisTemplate.delete(DICT_CACHE_PREFIX + dictTypeCode);
    }

    /**
     * 预加载字典（字典预热）
     */
    public void preloadDicts() {
        log.info("开始预加载字典");

        List<SystemDictEntity> allDicts = systemDictDao.selectAllEnabled();

        // 按字典类型分组
        Map<String, List<SystemDictEntity>> groupedDicts = allDicts.stream()
                .collect(Collectors.groupingBy(SystemDictEntity::getDictTypeCode));

        // 加载到缓存
        for (Map.Entry<String, List<SystemDictEntity>> entry : groupedDicts.entrySet()) {
            String typeCode = entry.getKey();
            List<SystemDictEntity> dicts = entry.getValue();

            localCache.put(typeCode, dicts);
            redisTemplate.opsForValue().set(DICT_CACHE_PREFIX + typeCode, dicts, CACHE_TTL);
        }

        log.info("字典预加载完成 - 类型数: {}, 字典数: {}", groupedDicts.size(), allDicts.size());
    }

    /**
     * 清除其他默认值
     */
    public void clearOtherDefaultValues(Long dictTypeId, Long excludeId) {
        systemDictDao.clearOtherDefaultValues(dictTypeId, excludeId);
    }

    /**
     * 验证字典值是否唯一
     */
    public boolean checkDictValueUnique(Long dictTypeId, String dictValue, Long excludeId) {
        int count = systemDictDao.checkDictValueUnique(dictTypeId, dictValue, excludeId);
        return count == 0;
    }
}
