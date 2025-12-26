package net.lab1024.sa.common.organization.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.AreaAccessExtEntity;

/**
 * 区域门禁扩展DAO接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AreaAccessExtDao extends BaseMapper<AreaAccessExtEntity> {

    /**
     * 根据区域ID查询区域扩展配置
     * <p>
     * 使用MyBatis-Plus的LambdaQueryWrapper实现查询
     * </p>
     *
     * @param areaId 区域ID
     * @return 区域扩展配置实体，如果不存在则返回null
     */
    default AreaAccessExtEntity selectByAreaId(Long areaId) {
        return this.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AreaAccessExtEntity>()
                        .eq(AreaAccessExtEntity::getAreaId, areaId)
                        .eq(AreaAccessExtEntity::getDeleted, false)
                        .last("LIMIT 1"));
    }
}
