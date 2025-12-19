package net.lab1024.sa.video.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.video.entity.VideoMonitorEntity;

/**
 * 视频监控会话数据访问层
 * <p>
 * 提供监控会话数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoMonitorDao extends BaseMapper<VideoMonitorEntity> {

    /**
     * 根据用户ID查询监控会话
     *
     * @param userId 用户ID
     * @return 监控会话列表
     */
    @Select("SELECT * FROM t_video_monitor WHERE user_id = #{userId} AND deleted = 0 ORDER BY start_time DESC")
    List<VideoMonitorEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户名查询监控会话
     *
     * @param username 用户名
     * @return 监控会话列表
     */
    @Select("SELECT * FROM t_video_monitor WHERE username = #{username} AND deleted = 0 ORDER BY start_time DESC")
    List<VideoMonitorEntity> selectByUsername(@Param("username") String username);

    /**
     * 根据会话状态查询监控会话
     *
     * @param sessionStatus 会话状态
     * @return 监控会话列表
     */
    @Select("SELECT * FROM t_video_monitor WHERE session_status = #{sessionStatus} AND deleted = 0 ORDER BY start_time DESC")
    List<VideoMonitorEntity> selectBySessionStatus(@Param("sessionStatus") Integer sessionStatus);

    /**
     * 查询活跃的监控会话
     *
     * @return 活跃监控会话列表
     */
    @Select("SELECT * FROM t_video_monitor WHERE session_status = 1 AND deleted = 0 ORDER BY last_active_time DESC")
    List<VideoMonitorEntity> selectActiveSessions();

    /**
     * 查询超时的监控会话（超过指定时间未活跃）
     *
     * @param timeoutMinutes 超时时间（分钟）
     * @return 超时监控会话列表
     */
    @Select("SELECT * FROM t_video_monitor WHERE session_status = 1 AND " +
            "last_active_time < DATE_SUB(NOW(), INTERVAL #{timeoutMinutes} MINUTE) AND deleted = 0")
    List<VideoMonitorEntity> selectTimeoutSessions(@Param("timeoutMinutes") Integer timeoutMinutes);

    /**
     * 根据客户端IP查询监控会话
     *
     * @param clientIp 客户端IP
     * @return 监控会话列表
     */
    @Select("SELECT * FROM t_video_monitor WHERE client_ip = #{clientIp} AND deleted = 0 ORDER BY start_time DESC")
    List<VideoMonitorEntity> selectByClientIp(@Param("clientIp") String clientIp);

    /**
     * 根据客户端类型查询监控会话
     *
     * @param clientType 客户端类型
     * @return 监控会话列表
     */
    @Select("SELECT * FROM t_video_monitor WHERE client_type = #{clientType} AND deleted = 0 ORDER BY start_time DESC")
    List<VideoMonitorEntity> selectByClientType(@Param("clientType") Integer clientType);

    /**
     * 根据屏幕布局查询监控会话
     *
     * @param screenLayout 屏幕布局
     * @return 监控会话列表
     */
    @Select("SELECT * FROM t_video_monitor WHERE screen_layout = #{screenLayout} AND deleted = 0 ORDER BY start_time DESC")
    List<VideoMonitorEntity> selectByScreenLayout(@Param("screenLayout") Integer screenLayout);

    /**
     * 根据区域ID查询监控会话
     *
     * @param areaId 区域ID
     * @return 监控会话列表
     */
    @Select("SELECT * FROM t_video_monitor WHERE area_id = #{areaId} AND deleted = 0 ORDER BY start_time DESC")
    List<VideoMonitorEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 分页查询监控会话
     *
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param sessionStatus 会话状态（可选）
     * @param clientType 客户端类型（可选）
     * @param screenLayout 屏幕布局（可选）
     * @param areaId 区域ID（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM t_video_monitor WHERE deleted = 0 " +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='sessionStatus != null'> AND session_status = #{sessionStatus} </if>" +
            "<if test='clientType != null'> AND client_type = #{clientType} </if>" +
            "<if test='screenLayout != null'> AND screen_layout = #{screenLayout} </if>" +
            "<if test='areaId != null'> AND area_id = #{areaId} </if>" +
            "ORDER BY start_time DESC" +
            "</script>")
    IPage<VideoMonitorEntity> selectMonitorPage(Page<VideoMonitorEntity> page,
                                                @Param("userId") Long userId,
                                                @Param("sessionStatus") Integer sessionStatus,
                                                @Param("clientType") Integer clientType,
                                                @Param("screenLayout") Integer screenLayout,
                                                @Param("areaId") Long areaId);

    /**
     * 更新监控会话状态
     *
     * @param monitorId 监控会话ID
     * @param sessionStatus 新状态
     * @return 影响行数
     */
    @Update("UPDATE t_video_monitor SET session_status = #{sessionStatus}, update_time = NOW() WHERE monitor_id = #{monitorId}")
    @Transactional(rollbackFor = Exception.class)
    int updateSessionStatus(@Param("monitorId") Long monitorId, @Param("sessionStatus") Integer sessionStatus);

    /**
     * 更新最后活跃时间
     *
     * @param monitorId 监控会话ID
     * @param lastActiveTime 最后活跃时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_monitor SET last_active_time = #{lastActiveTime}, update_time = NOW() WHERE monitor_id = #{monitorId}")
    @Transactional(rollbackFor = Exception.class)
    int updateLastActiveTime(@Param("monitorId") Long monitorId, @Param("lastActiveTime") LocalDateTime lastActiveTime);

    /**
     * 更新监控会话网络状态
     *
     * @param monitorId 监控会话ID
     * @param networkStatus 网络状态
     * @param networkLatency 网络延迟
     * @param packetLossRate 丢包率
     * @param bandwidthUsage 带宽占用
     * @return 影响行数
     */
    @Update("UPDATE t_video_monitor SET network_status = #{networkStatus}, network_latency = #{networkLatency}, " +
            "packet_loss_rate = #{packetLossRate}, bandwidth_usage = #{bandwidthUsage}, update_time = NOW() " +
            "WHERE monitor_id = #{monitorId}")
    @Transactional(rollbackFor = Exception.class)
    int updateNetworkStatus(@Param("monitorId") Long monitorId,
                            @Param("networkStatus") Integer networkStatus,
                            @Param("networkLatency") Integer networkLatency,
                            @Param("packetLossRate") Double packetLossRate,
                            @Param("bandwidthUsage") Integer bandwidthUsage);

    /**
     * 更新系统资源使用情况
     *
     * @param monitorId 监控会话ID
     * @param cpuUsage CPU使用率
     * @param memoryUsage 内存使用率
     * @return 影响行数
     */
    @Update("UPDATE t_video_monitor SET cpu_usage = #{cpuUsage}, memory_usage = #{memoryUsage}, update_time = NOW() " +
            "WHERE monitor_id = #{monitorId}")
    @Transactional(rollbackFor = Exception.class)
    int updateResourceUsage(@Param("monitorId") Long monitorId,
                            @Param("cpuUsage") Double cpuUsage,
                            @Param("memoryUsage") Double memoryUsage);

    /**
     * 记录异常信息
     *
     * @param monitorId 监控会话ID
     * @param exceptionTime 异常时间
     * @param exceptionMessage 异常信息
     * @return 影响行数
     */
    @Update("UPDATE t_video_monitor SET exception_count = IFNULL(exception_count, 0) + 1, " +
            "last_exception_time = #{exceptionTime}, exception_message = #{exceptionMessage}, update_time = NOW() " +
            "WHERE monitor_id = #{monitorId}")
    @Transactional(rollbackFor = Exception.class)
    int recordException(@Param("monitorId") Long monitorId,
                        @Param("exceptionTime") LocalDateTime exceptionTime,
                        @Param("exceptionMessage") String exceptionMessage);

    /**
     * 记录重连
     *
     * @param monitorId 监控会话ID
     * @return 影响行数
     */
    @Update("UPDATE t_video_monitor SET reconnect_count = IFNULL(reconnect_count, 0) + 1, update_time = NOW() " +
            "WHERE monitor_id = #{monitorId}")
    @Transactional(rollbackFor = Exception.class)
    int recordReconnect(@Param("monitorId") Long monitorId);

    /**
     * 终止监控会话
     *
     * @param monitorId 监控会话ID
     * @param endTime 结束时间
     * @param duration 持续时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_monitor SET session_status = 3, end_time = #{endTime}, " +
            "duration = #{duration}, update_time = NOW() WHERE monitor_id = #{monitorId}")
    @Transactional(rollbackFor = Exception.class)
    int terminateSession(@Param("monitorId") Long monitorId,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("duration") Long duration);

    /**
     * 强制断开指定用户的所有监控会话
     *
     * @param userId 用户ID
     * @param断开时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_monitor SET session_status = 3, end_time = #{currentTime}, " +
            "duration = TIMESTAMPDIFF(SECOND, start_time, #{currentTime}), update_time = NOW() " +
            "WHERE user_id = #{userId} AND session_status = 1")
    @Transactional(rollbackFor = Exception.class)
    int forceTerminateUserSessions(@Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime);

    /**
     * 清理过期的监控会话
     *
     * @param expireHours 过期时间（小时）
     * @return 影响行数
     */
    @Update("UPDATE t_video_monitor SET session_status = 3, end_time = NOW(), " +
            "duration = TIMESTAMPDIFF(SECOND, start_time, NOW()), update_time = NOW() " +
            "WHERE session_status = 1 AND start_time < DATE_SUB(NOW(), INTERVAL #{expireHours} HOUR)")
    @Transactional(rollbackFor = Exception.class)
    int cleanupExpiredSessions(@Param("expireHours") Integer expireHours);

    /**
     * 统计监控会话数量
     *
     * @param sessionStatus 会话状态（可选）
     * @param clientType 客户端类型（可选）
     * @param areaId 区域ID（可选）
     * @return 统计数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_video_monitor WHERE deleted = 0 " +
            "<if test='sessionStatus != null'> AND session_status = #{sessionStatus} </if>" +
            "<if test='clientType != null'> AND client_type = #{clientType} </if>" +
            "<if test='areaId != null'> AND area_id = #{areaId} </if>" +
            "</script>")
    Long countMonitors(@Param("sessionStatus") Integer sessionStatus,
                       @Param("clientType") Integer clientType,
                       @Param("areaId") Long areaId);

    /**
     * 统计各状态的监控会话数量
     *
     * @return 统计结果
     */
    @Select("SELECT session_status, COUNT(*) as count FROM t_video_monitor WHERE deleted = 0 GROUP BY session_status ORDER BY session_status")
    List<java.util.Map<String, Object>> countMonitorsByStatus();

    /**
     * 统计各客户端类型的监控会话数量
     *
     * @return 统计结果
     */
    @Select("SELECT client_type, COUNT(*) as count FROM t_video_monitor WHERE deleted = 0 GROUP BY client_type ORDER BY count DESC")
    List<java.util.Map<String, Object>> countMonitorsByClientType();

    /**
     * 统计各屏幕布局的监控会话数量
     *
     * @return 统计结果
     */
    @Select("SELECT screen_layout, COUNT(*) as count FROM t_video_monitor WHERE deleted = 0 GROUP BY screen_layout ORDER BY count DESC")
    List<java.util.Map<String, Object>> countMonitorsByLayout();

    /**
     * 查询指定时间段内的监控会话统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT DATE(start_time) as date, COUNT(*) as count, AVG(TIMESTAMPDIFF(SECOND, start_time, IFNULL(end_time, NOW()))) as avg_duration " +
            "FROM t_video_monitor WHERE start_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 " +
            "GROUP BY DATE(start_time) ORDER BY date DESC")
    List<java.util.Map<String, Object>> getSessionStatistics(@Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);
}
