package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 区域人员关联DAO
 * <p>
 * 区域与人员关联的数据访问层
 * 支持人员信息下发到区域设备的查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Mapper
public interface AreaUserDao extends BaseMapper<AreaUserEntity> {

    /**
     * 根据区域ID查询关联人员
     *
     * @param areaId 区域ID
     * @return 关联人员列表
     */
    @Select("SELECT * FROM t_area_user_relation WHERE area_id = #{areaId} AND relation_status = 1 AND deleted_flag = 0")
    List<AreaUserEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据用户ID查询关联区域
     *
     * @param userId 用户ID
     * @return 关联区域列表
     */
    @Select("SELECT * FROM t_area_user_relation WHERE user_id = #{userId} AND relation_status = 1 AND deleted_flag = 0")
    List<AreaUserEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据区域ID和用户ID查询关联关系
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     * @return 关联关系
     */
    @Select("SELECT * FROM t_area_user_relation WHERE area_id = #{areaId} AND user_id = #{userId} AND relation_status = 1 AND deleted_flag = 0")
    AreaUserEntity selectByAreaIdAndUserId(@Param("areaId") Long areaId, @Param("userId") Long userId);

    /**
     * 查询需要设备同步的关联关系
     *
     * @return 需要同步的关联列表
     */
    @Select("SELECT * FROM t_area_user_relation WHERE device_sync_status IN (0, 3, 4) AND relation_status = 1 AND deleted_flag = 0 LIMIT 100")
    List<AreaUserEntity> selectNeedSync();

    /**
     * 根据权限级别查询关联人员
     *
     * @param areaId 区域ID
     * @param permissionLevel 权限级别
     * @return 关联人员列表
     */
    @Select("SELECT * FROM t_area_user_relation WHERE area_id = #{areaId} AND permission_level = #{permissionLevel} AND relation_status = 1 AND deleted_flag = 0")
    List<AreaUserEntity> selectByAreaIdAndPermissionLevel(@Param("areaId") Long areaId, @Param("permissionLevel") Integer permissionLevel);

    /**
     * 查询子区域的关联人员（包含继承权限）
     *
     * @param parentAreaPath 父区域路径
     * @return 关联人员列表
     */
    @Select("SELECT * FROM t_area_user_relation aur " +
            "JOIN t_area a ON aur.area_id = a.area_id " +
            "WHERE a.path LIKE CONCAT(#{parentAreaPath}, '%') " +
            "AND aur.inherit_children = 1 " +
            "AND aur.relation_status = 1 " +
            "AND aur.deleted_flag = 0 " +
            "AND a.deleted_flag = 0")
    List<AreaUserEntity> selectByParentAreaPath(@Param("parentAreaPath") String parentAreaPath);

    /**
     * 查询用户在指定区域的访问权限
     *
     * @param userId 用户ID
     * @param areaIds 区域ID列表
     * @return 权限列表
     */
    @Select("<script>" +
            "SELECT * FROM t_area_user_relation " +
            "WHERE user_id = #{userId} " +
            "AND area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            "AND relation_status = 1 " +
            "AND deleted_flag = 0 " +
            "ORDER BY permission_level DESC" +
            "</script>")
    List<AreaUserEntity> selectUserAreaPermissions(@Param("userId") Long userId, @Param("areaIds") List<Long> areaIds);

    /**
     * 更新设备同步状态
     *
     * @param relationId 关联ID
     * @param syncStatus 同步状态
     * @param failureReason 失败原因
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_area_user_relation SET " +
            "device_sync_status = #{syncStatus}, " +
            "last_sync_time = #{syncTime}, " +
            "sync_failure_reason = #{failureReason}, " +
            "update_time = NOW() " +
            "WHERE relation_id = #{relationId}")
    int updateSyncStatus(@Param("relationId") String relationId,
                         @Param("syncStatus") Integer syncStatus,
                         @Param("failureReason") String failureReason,
                         @Param("syncTime") LocalDateTime syncTime);

    /**
     * 增加重试次数
     *
     * @param relationId 关联ID
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_area_user_relation SET " +
            "retry_count = retry_count + 1, " +
            "update_time = NOW() " +
            "WHERE relation_id = #{relationId}")
    int incrementRetryCount(@Param("relationId") String relationId);

    /**
     * 批量更新同步状态
     *
     * @param relationIds 关联ID列表
     * @param syncStatus 同步状态
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("<script>" +
            "UPDATE t_area_user_relation SET " +
            "device_sync_status = #{syncStatus}, " +
            "last_sync_time = #{syncTime}, " +
            "update_time = NOW() " +
            "WHERE relation_id IN " +
            "<foreach collection='relationIds' item='relationId' open='(' separator=',' close=')'>" +
            "#{relationId}" +
            "</foreach>" +
            "</script>")
    int batchUpdateSyncStatus(@Param("relationIds") List<String> relationIds,
                              @Param("syncStatus") Integer syncStatus,
                              @Param("syncTime") LocalDateTime syncTime);

    /**
     * 统计区域关联人员数量
     *
     * @param areaId 区域ID
     * @return 人员数量
     */
    @Select("SELECT COUNT(*) FROM t_area_user_relation WHERE area_id = #{areaId} AND relation_status = 1 AND deleted_flag = 0")
    int countByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询过期的关联关系
     *
     * @param currentTime 当前时间
     * @return 过期的关联列表
     */
    @Select("SELECT * FROM t_area_user_relation " +
            "WHERE permanent = 0 " +
            "AND expire_time <= #{currentTime} " +
            "AND relation_status = 1 " +
            "AND deleted_flag = 0")
    List<AreaUserEntity> selectExpired(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询即将过期的关联关系（7天内）
     *
     * @param currentTime 当前时间
     * @return 即将过期的关联列表
     */
    @Select("SELECT * FROM t_area_user_relation " +
            "WHERE permanent = 0 " +
            "AND expire_time BETWEEN #{currentTime} AND DATE_ADD(#{currentTime}, INTERVAL 7 DAY) " +
            "AND relation_status = 1 " +
            "AND deleted_flag = 0")
    List<AreaUserEntity> selectSoonToExpire(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据关联类型查询
     *
     * @param areaId 区域ID
     * @param relationType 关联类型
     * @return 关联列表
     */
    @Select("SELECT * FROM t_area_user_relation WHERE area_id = #{areaId} AND relation_type = #{relationType} AND relation_status = 1 AND deleted_flag = 0")
    List<AreaUserEntity> selectByRelationType(@Param("areaId") Long areaId, @Param("relationType") Integer relationType);

    /**
     * 检查用户是否在区域内有权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @param permissionLevel 最低权限级别
     * @return 是否有权限
     */
    @Select("SELECT COUNT(*) > 0 FROM t_area_user_relation " +
            "WHERE user_id = #{userId} " +
            "AND area_id = #{areaId} " +
            "AND permission_level >= #{permissionLevel} " +
            "AND relation_status = 1 " +
            "AND (permanent = 1 OR (permanent = 0 AND expire_time > NOW())) " +
            "AND deleted_flag = 0")
    boolean hasPermission(@Param("userId") Long userId,
                          @Param("areaId") Long areaId,
                          @Param("permissionLevel") Integer permissionLevel);
}
