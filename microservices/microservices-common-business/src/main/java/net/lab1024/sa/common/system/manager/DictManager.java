package net.lab1024.sa.common.system.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 * - 字典数据查询（无缓存，缓存由Service层@Cacheable注解处理）
 * - 字典树形结构构建
 * - 字典数据验证
 * - 字典批量操作
 *
 * 企业级特性：
 * - 字典树形结构
 * - 字典数据验证
 * - 批量操作优化
 * - 缓存由Service层@Cacheable注解统一管理
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 * @updated 2025-01-30 移除缓存逻辑，改为纯Java类，符合CLAUDE.md规范，缓存由Service层@Cacheable注解处理
 */
@Slf4j
public class DictManager {

    private final SystemDictDao systemDictDao;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     * <p>
     * ⚠️ 注意：已移除RedisTemplate依赖，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param systemDictDao 系统字典DAO
     */
    public DictManager(SystemDictDao systemDictDao) {
        this.systemDictDao = systemDictDao;
    }

    /**
     * 获取字典列表（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param dictTypeCode 字典类型代码
     * @return 字典列表
     */
    public List<SystemDictEntity> getDictList(String dictTypeCode) {
        // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
        List<SystemDictEntity> list = systemDictDao.selectEnabledByTypeCode(dictTypeCode);
        if (list == null || list.isEmpty()) {
            log.warn("字典数据不存在或已禁用 - typeCode: {}", dictTypeCode);
            return new ArrayList<>();
        }

        log.debug("从数据库获取字典 - typeCode: {}, 数量: {}", dictTypeCode, list.size());
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
