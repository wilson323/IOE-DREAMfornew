package net.lab1024.sa.access.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.entity.access.AccessUserPermissionEntity;

/**
 * 门禁设备权限DAO
 * <p>
 * 门禁设备验证权限的独立DAO，避免与组织关系混用。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Mapper
public interface AccessUserPermissionDao extends BaseMapper<AccessUserPermissionEntity> {

    @Select("SELECT * FROM t_access_user_permission WHERE area_id = #{areaId} AND deleted_flag = 0")
    List<AccessUserPermissionEntity> selectByAreaId(@Param("areaId") Long areaId);

    @Select("SELECT * FROM t_access_user_permission WHERE user_id = #{userId} AND area_id = #{areaId} AND deleted_flag = 0 LIMIT 1")
    AccessUserPermissionEntity selectByUserAndArea(@Param("userId") Long userId, @Param("areaId") Long areaId);

    @Select("SELECT COUNT(1) FROM t_access_user_permission WHERE user_id = #{userId} AND area_id = #{areaId} AND permission_status = 1 AND deleted_flag = 0")
    Integer countValidPermission(@Param("userId") Long userId, @Param("areaId") Long areaId);

    default boolean hasPermission(Long userId, Long areaId) {
        Integer count = countValidPermission(userId, areaId);
        return count != null && count > 0;
    }

    @Update("UPDATE t_access_user_permission SET device_sync_status = #{syncStatus}, last_sync_error = #{errorMessage}, last_sync_time = #{syncTime} WHERE permission_id = #{permissionId}")
    int updateSyncStatus(@Param("permissionId") Long permissionId, @Param("syncStatus") Integer syncStatus,
                         @Param("errorMessage") String errorMessage, @Param("syncTime") LocalDateTime syncTime);
}
