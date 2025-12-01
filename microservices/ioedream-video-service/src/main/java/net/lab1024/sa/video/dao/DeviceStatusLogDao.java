package net.lab1024.sa.video.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.video.domain.entity.DeviceStatusLogEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备状态日志DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface DeviceStatusLogDao extends BaseMapper<DeviceStatusLogEntity> {

    /**
     * 根据设备ID查询状态日志
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 状态日志列表
     */
    List<DeviceStatusLogEntity> selectByDeviceId(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 根据日志类型查询状态日志
     *
     * @param logType 日志类型
     * @param limit 限制数量
     * @return 状态日志列表
     */
    List<DeviceStatusLogEntity> selectByLogType(@Param("logType") String logType, @Param("limit") Integer limit);

    /**
     * 根据日志级别查询状态日志
     *
     * @param logLevel 日志级别
     * @param limit 限制数量
     * @return 状态日志列表
     */
    List<DeviceStatusLogEntity> selectByLogLevel(@Param("logLevel") String logLevel, @Param("limit") Integer limit);

    /**
     * 根据时间范围查询状态日志
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 状态日志列表
     */
    List<DeviceStatusLogEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("limit") Integer limit);

    /**
     * 查询错误日志
     *
     * @param deviceId 设备ID（可选）
     * @param limit 限制数量
     * @return 错误日志列表
     */
    List<DeviceStatusLogEntity> selectErrorLogs(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 查询操作日志
     *
     * @param deviceId 设备ID（可选）
     * @param limit 限制数量
     * @return 操作日志列表
     */
    List<DeviceStatusLogEntity> selectOperationLogs(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);

    /**
     * 统计指定时间范围内的日志数量
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志数量
     */
    Long countLogsByTimeRange(@Param("deviceId") Long deviceId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的日志
     *
     * @param beforeTime 时间点
     * @return 删除行数
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据操作用户查询日志
     *
     * @param operateUserId 操作用户ID
     * @param limit 限制数量
     * @return 日志列表
     */
    List<DeviceStatusLogEntity> selectByOperateUserId(@Param("operateUserId") Long operateUserId, @Param("limit") Integer limit);

    /**
     * 查询设备心跳日志
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 心跳日志列表
     */
    List<DeviceStatusLogEntity> selectHeartbeatLogs(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);
}