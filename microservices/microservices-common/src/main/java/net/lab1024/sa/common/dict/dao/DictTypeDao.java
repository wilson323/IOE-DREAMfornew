package net.lab1024.sa.common.dict.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.dict.entity.DictTypeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典类型数据访问层
 * <p>
 * 企业级真实实现 - 遵循CLAUDE.md规范
 * 统一使用@Mapper注解和Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Mapper
public interface DictTypeDao extends BaseMapper<DictTypeEntity> {
    // MyBatis-Plus提供基础CRUD方法
    // selectList(null) - 查询所有数据
    // selectById(id) - 根据ID查询
    // insert(entity) - 插入数据
    // updateById(entity) - 根据ID更新
    // deleteById(id) - 根据ID删除
}
