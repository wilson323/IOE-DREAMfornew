package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AntiPassbackRecordEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反潜记录数据访问对象
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
public interface AntiPassbackRecordDao extends BaseMapper<AntiPassbackRecordEntity> {

    /**
     * 查询用户最近的进出记录
     * <p>
     * 用于反潜验证，检查用户是否从正确的门进出
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param limit 查询数量限制
     * @return 进出记录列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_anti_passback_record " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId} AND deleted_flag = 0 " +
            "ORDER BY record_time DESC " +
            "LIMIT #{limit}")
    List<AntiPassbackRecordEntity> selectRecentRecords(
            @Param("userId") Long userId,
            @Param("deviceId") Long deviceId,
            @Param("limit") Integer limit);

    /**
     * 查询用户在时间窗口内的进出记录
     * <p>
     * 用于反潜验证，检查用户在指定时间内的进出状态
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 进出记录列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_anti_passback_record " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId} " +
            "AND record_time >= #{startTime} AND record_time <= #{endTime} " +
            "AND deleted_flag = 0 " +
            "ORDER BY record_time DESC")
    List<AntiPassbackRecordEntity> selectRecordsInTimeWindow(
            @Param("userId") Long userId,
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户最近的进入记录
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 最近的进入记录，不存在返回null
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_anti_passback_record " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId} " +
            "AND in_out_status = 1 AND deleted_flag = 0 " +
            "ORDER BY record_time DESC " +
            "LIMIT 1")
    AntiPassbackRecordEntity selectLastInRecord(
            @Param("userId") Long userId,
            @Param("deviceId") Long deviceId);

    /**
     * 查询用户最近的离开记录
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 最近的离开记录，不存在返回null
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_anti_passback_record " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId} " +
            "AND in_out_status = 2 AND deleted_flag = 0 " +
            "ORDER BY record_time DESC " +
            "LIMIT 1")
    AntiPassbackRecordEntity selectLastOutRecord(
            @Param("userId") Long userId,
            @Param("deviceId") Long deviceId);
}
