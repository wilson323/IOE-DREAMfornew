package net.lab1024.sa.base.module.area.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 区域DAO
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Mapper
@Repository
public interface AreaDao extends BaseMapper<Object> {

    /**
     * 根据用户ID查询区域ID列表
     *
     * @param userId 用户ID
     * @return 区域ID列表
     */
    List<Long> selectAreaIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据区域ID查询子区域ID列表
     *
     * @param areaId 区域ID
     * @return 子区域ID列表
     */
    List<Long> selectChildAreaIds(@Param("areaId") Long areaId);

    /**
     * 检查用户是否有区域权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    boolean checkAreaPermission(@Param("userId") Long userId, @Param("areaId") Long areaId);

    /**
     * 批量检查用户区域权限
     *
     * @param userId 用户ID
     * @param areaIds 区域ID集合
     * @return 有权限的区域ID集合
     */
    Set<Long> filterUserAreaIds(@Param("userId") Long userId, @Param("areaIds") Set<Long> areaIds);

    /**
     * 获取区域层级路径
     *
     * @param areaId 区域ID
     * @return 层级路径
     */
    String getAreaHierarchyPath(@Param("areaId") Long areaId);

    /**
     * 获取所有子区域ID
     *
     * @param areaId 区域ID
     * @return 子区域ID集合
     */
    Set<Long> getAllChildAreaIds(@Param("areaId") Long areaId);
}