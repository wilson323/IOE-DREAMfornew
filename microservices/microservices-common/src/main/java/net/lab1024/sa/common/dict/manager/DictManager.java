package net.lab1024.sa.common.dict.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dict.dao.DictDataDao;
import net.lab1024.sa.common.dict.dao.DictTypeDao;
import net.lab1024.sa.common.dict.entity.DictDataEntity;
import net.lab1024.sa.common.dict.entity.DictTypeEntity;

import java.util.List;

/**
 * 字典管理器
 * <p>
 * 遵循四层架构规范：Controller → Service → Manager → DAO
 * 纯Java类，不使用Spring注解，通过构造函数注入依赖
 * 负责复杂业务流程编排和多DAO数据组装
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class DictManager {

    private final DictTypeDao dictTypeDao;
    private final DictDataDao dictDataDao;

    /**
     * 构造函数注入依赖
     *
     * @param dictTypeDao 字典类型DAO
     * @param dictDataDao 字典数据DAO
     */
    public DictManager(DictTypeDao dictTypeDao, DictDataDao dictDataDao) {
        this.dictTypeDao = dictTypeDao;
        this.dictDataDao = dictDataDao;
    }

    /**
     * 获取所有字典数据
     *
     * @return 字典数据列表
     */
    public List<DictDataEntity> getAllDictData() {
        log.debug("[字典管理器] 开始获取所有字典数据");
        return dictDataDao.selectList(null);
    }

    /**
     * 获取字典类型列表
     *
     * @return 字典类型列表
     */
    public List<DictTypeEntity> getTypeList() {
        log.debug("[字典管理器] 开始获取字典类型列表");
        return dictTypeDao.selectList(null);
    }

    /**
     * 根据类型编码获取字典数据
     *
     * @param typeCode 类型编码
     * @return 字典数据列表
     */
    public List<DictDataEntity> getDataList(String typeCode) {
        log.debug("[字典管理器] 根据类型编码获取字典数据，typeCode={}", typeCode);
        return dictDataDao.selectByTypeCode(typeCode);
    }
}