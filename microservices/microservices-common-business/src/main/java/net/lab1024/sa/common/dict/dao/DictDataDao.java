package net.lab1024.sa.common.dict.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.dict.entity.DictDataEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 字典数据数据访问层
 * <p>
 * 企业级真实实现 - 遵循CLAUDE.md规范
 * 统一使用@Mapper注解和Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Mapper
public interface DictDataDao extends BaseMapper<DictDataEntity> {
    
    /**
     * 根据字典类型编码查询字典数据
     *
     * @param typeCode 字典类型编码
     * @return 字典数据列表
     */
    default List<DictDataEntity> selectByTypeCode(String typeCode) {
        LambdaQueryWrapper<DictDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictDataEntity::getTypeCode, typeCode)
               .eq(DictDataEntity::getStatus, 1) // 只查询启用状态
               .orderByAsc(DictDataEntity::getSort); // 按排序号升序
        return selectList(wrapper);
    }
}
