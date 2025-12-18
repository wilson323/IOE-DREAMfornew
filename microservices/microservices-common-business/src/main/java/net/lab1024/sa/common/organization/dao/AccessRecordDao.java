package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁通行记录数据访问对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识数据访问层
 * - 继承BaseMapper<Entity>使用MyBatis-Plus
 * - 查询方法使用@Transactional(readOnly = true)
 * - 写操作方法使用@Transactional(rollbackFor = Exception.class)
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AccessRecordDao extends BaseMapper<AccessRecordEntity> {

    /**
     * 根据组合键查询记录（用于幂等性检查）
     * <p>
     * 使用 userId + deviceId + accessTime 的组合作为唯一标识
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param accessTime 通行时间
     * @return 记录，不存在返回null
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_record " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId} " +
            "AND access_time = #{accessTime} AND deleted_flag = 0 " +
            "LIMIT 1")
    AccessRecordEntity selectByCompositeKey(
            @Param("userId") Long userId,
            @Param("deviceId") Long deviceId,
            @Param("accessTime") LocalDateTime accessTime);

    /**
     * 批量插入通行记录
     * <p>
     * 使用MyBatis-Plus的批量插入功能
     * </p>
     *
     * @param records 记录列表
     * @return 插入数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Insert("<script>" +
            "INSERT INTO t_access_record (user_id, device_id, area_id, access_result, access_time, " +
            "access_type, verify_method, photo_path, create_time, deleted_flag) VALUES " +
            "<foreach collection='records' item='record' separator=','>" +
            "(#{record.userId}, #{record.deviceId}, #{record.areaId}, #{record.accessResult}, " +
            "#{record.accessTime}, #{record.accessType}, #{record.verifyMethod}, #{record.photoPath}, " +
            "NOW(), 0)" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("records") List<AccessRecordEntity> records);

    /**
     * 根据用户ID和时间范围查询记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_record " +
            "WHERE user_id = #{userId} AND deleted_flag = 0 " +
            "AND access_time >= #{startTime} AND access_time <= #{endTime} " +
            "ORDER BY access_time DESC")
    List<AccessRecordEntity> selectByUserIdAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据设备ID和时间范围查询记录
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_record " +
            "WHERE device_id = #{deviceId} AND deleted_flag = 0 " +
            "AND access_time >= #{startTime} AND access_time <= #{endTime} " +
            "ORDER BY access_time DESC")
    List<AccessRecordEntity> selectByDeviceIdAndTimeRange(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
