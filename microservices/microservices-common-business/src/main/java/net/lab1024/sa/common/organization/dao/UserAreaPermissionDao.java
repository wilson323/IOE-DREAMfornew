package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.UserAreaPermissionEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户区域权限DAO接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper<Entity>
 * - 使用@Select、@Update等注解
 * - 方法命名规范：selectByXxx、updateXxx
 * </p>
 * <p>
 * 核心职责：
 * - 用户区域权限的CRUD操作
 * - 权限查询和验证
 * - 时间段权限查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Mapper
public interface UserAreaPermissionDao extends BaseMapper<UserAreaPermissionEntity> {

    /**
     * 根据用户ID和区域ID查询权限
     * <p>
     * 对应文档中的permissionDao.selectByUserAndArea()方法
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 权限实体
     */
    @Select("SELECT * FROM t_access_permission " +
            "WHERE user_id = #{userId} " +
            "AND area_id = #{areaId} " +
            "AND status = 1 " +
            "AND deleted_flag = 0 " +
            "LIMIT 1")
    @Transactional(readOnly = true)
    UserAreaPermissionEntity selectByUserAndArea(@Param("userId") Long userId, @Param("areaId") Long areaId);

    /**
     * 根据用户ID查询所有权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT * FROM t_access_permission " +
            "WHERE user_id = #{userId} " +
            "AND status = 1 " +
            "AND deleted_flag = 0 " +
            "ORDER BY area_id")
    @Transactional(readOnly = true)
    List<UserAreaPermissionEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据区域ID查询所有权限
     *
     * @param areaId 区域ID
     * @return 权限列表
     */
    @Select("SELECT * FROM t_access_permission " +
            "WHERE area_id = #{areaId} " +
            "AND status = 1 " +
            "AND deleted_flag = 0 " +
            "ORDER BY user_id")
    @Transactional(readOnly = true)
    List<UserAreaPermissionEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询有效的权限（未过期）
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param currentTime 当前时间
     * @return 权限实体
     */
    @Select("SELECT * FROM t_access_permission " +
            "WHERE user_id = #{userId} " +
            "AND area_id = #{areaId} " +
            "AND status = 1 " +
            "AND deleted_flag = 0 " +
            "AND (permission_type = 'ALWAYS' " +
            "     OR (permission_type = 'TIME_LIMITED' " +
            "         AND start_time <= #{currentTime} " +
            "         AND (end_time IS NULL OR end_time >= #{currentTime}))) " +
            "LIMIT 1")
    @Transactional(readOnly = true)
    UserAreaPermissionEntity selectValidPermission(@Param("userId") Long userId,
                                                    @Param("areaId") Long areaId,
                                                    @Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询过期的权限
     *
     * @param currentTime 当前时间
     * @return 过期权限列表
     */
    @Select("SELECT * FROM t_access_permission " +
            "WHERE permission_type = 'TIME_LIMITED' " +
            "AND end_time < #{currentTime} " +
            "AND status = 1 " +
            "AND deleted_flag = 0")
    @Transactional(readOnly = true)
    List<UserAreaPermissionEntity> selectExpiredPermissions(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 批量更新权限状态
     *
     * @param permissionIds 权限ID列表
     * @param status 新状态
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("<script>" +
            "UPDATE t_access_permission SET " +
            "status = #{status}, " +
            "update_time = NOW() " +
            "WHERE permission_id IN " +
            "<foreach collection='permissionIds' item='permissionId' open='(' separator=',' close=')'>" +
            "#{permissionId}" +
            "</foreach>" +
            "AND deleted_flag = 0" +
            "</script>")
    int batchUpdateStatus(@Param("permissionIds") List<Long> permissionIds, @Param("status") Integer status);

    /**
     * 检查用户是否有区域权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    @Select("SELECT COUNT(*) > 0 FROM t_access_permission " +
            "WHERE user_id = #{userId} " +
            "AND area_id = #{areaId} " +
            "AND status = 1 " +
            "AND deleted_flag = 0 " +
            "AND (permission_type = 'ALWAYS' " +
            "     OR (permission_type = 'TIME_LIMITED' " +
            "         AND start_time <= NOW() " +
            "         AND (end_time IS NULL OR end_time >= NOW())))")
    @Transactional(readOnly = true)
    boolean hasPermission(@Param("userId") Long userId, @Param("areaId") Long areaId);
}
