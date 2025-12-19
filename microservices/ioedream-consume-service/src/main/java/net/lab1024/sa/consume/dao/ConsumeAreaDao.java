package net.lab1024.sa.consume.dao;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.entity.ConsumeAreaEntity;

/**
 * 消费区域DAO接口
 * <p>
 * 用于区域的数据访问操作
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@org.apache.ibatis.annotations.Mapper
public interface ConsumeAreaDao extends BaseMapper<ConsumeAreaEntity> {

    /**
     * 根据区域编号查询区域信息
     *
     * @param areaCode 区域编号
     * @return 区域信息
     */
    ConsumeAreaEntity selectByCode(@Param("areaCode") String areaCode);
}



