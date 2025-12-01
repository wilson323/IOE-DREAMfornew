package net.lab1024.sa.system.dict.manager;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.system.dict.dao.DictDataDao;
import net.lab1024.sa.system.dict.dao.DictTypeDao;

/**
 * 字典类型管理器
 * <p>
 * 提供字典类型相关的业务逻辑封装
 * 遵循四层架构：Service → Manager → DAO
 *
 * @author SmartAdmin Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class DictTypeManager {

    @Resource
    private DictTypeDao dictTypeDao;

    @Resource
    private DictDataDao dictDataDao;

    /**
     * 统计指定字典类型下的字典数据数量
     *
     * @param dictTypeId 字典类型ID
     * @return 字典数据数量
     */
    public long countDictDataByTypeId(Long dictTypeId) {
        if (dictTypeId == null) {
            return 0;
        }
        try {
            Long count = dictDataDao.countDictDataByTypeId(dictTypeId);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("统计字典数据数量失败，dictTypeId：{}", dictTypeId, e);
            return 0;
        }
    }
}
