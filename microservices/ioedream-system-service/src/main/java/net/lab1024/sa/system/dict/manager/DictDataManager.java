package net.lab1024.sa.system.dict.manager;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.system.dict.dao.DictDataDao;
import net.lab1024.sa.system.dict.dao.DictTypeDao;
import net.lab1024.sa.system.dict.domain.entity.DictDataEntity;

/**
 * 字典数据管理器
 * <p>
 * 提供字典数据相关的业务逻辑封装
 * 遵循四层架构：Service → Manager → DAO
 *
 * @author SmartAdmin Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class DictDataManager {

    @Resource
    private DictDataDao dictDataDao;

    @Resource
    private DictTypeDao dictTypeDao;

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

    /**
     * 清除指定字典类型下其他字典数据的默认值标记
     *
     * @param dictTypeId 字典类型ID
     */
    public void clearOtherDefaultValues(Long dictTypeId) {
        if (dictTypeId == null) {
            return;
        }
        try {
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DictDataEntity> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            updateWrapper.eq(DictDataEntity::getDictTypeId, dictTypeId)
                    .eq(DictDataEntity::getIsDefault, 1)
                    .eq(DictDataEntity::getDeletedFlag, 0)
                    .set(DictDataEntity::getIsDefault, 0);
            dictDataDao.update(null, updateWrapper);
            log.debug("清除字典类型下其他默认值成功，dictTypeId：{}", dictTypeId);
        } catch (Exception e) {
            log.error("清除字典类型下其他默认值失败，dictTypeId：{}", dictTypeId, e);
        }
    }

    /**
     * 获取指定字典类型下下一个可用的排序号
     *
     * @param dictTypeId 字典类型ID
     * @return 下一个排序号
     */
    public Integer getNextSortOrder(Long dictTypeId) {
        if (dictTypeId == null) {
            return 1;
        }
        try {
            Integer maxSortOrder = dictDataDao.getMaxSortOrder(dictTypeId);
            return maxSortOrder != null ? maxSortOrder + 1 : 1;
        } catch (Exception e) {
            log.error("获取下一个排序号失败，dictTypeId：{}", dictTypeId, e);
            return 1;
        }
    }
}
