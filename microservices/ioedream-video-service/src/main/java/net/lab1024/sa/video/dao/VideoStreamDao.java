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

import net.lab1024.sa.video.entity.VideoStreamEntity;

/**
 * 视频流数据访问层
 * <p>
 * 提供视频流数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoStreamDao extends BaseMapper<VideoStreamEntity> {

    /**
     * 根据设备ID查询视频流列表
     *
     * @param deviceId 设备ID
     * @return 视频流列表
     */
    @Select("SELECT * FROM t_video_stream WHERE device_id = #{deviceId} AND deleted = 0 ORDER BY stream_type, channel_no")
    List<VideoStreamEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据设备编码查询视频流列表
     *
     * @param deviceCode 设备编码
     * @return 视频流列表
     */
    @Select("SELECT * FROM t_video_stream WHERE device_code = #{deviceCode} AND deleted = 0 ORDER BY stream_type, channel_no")
    List<VideoStreamEntity> selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据设备和通道查询视频流
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @return 视频流实体
     */
    @Select("SELECT * FROM t_video_stream WHERE device_id = #{deviceId} AND channel_no = #{channelNo} AND deleted = 0 LIMIT 1")
    VideoStreamEntity selectByDeviceAndChannel(@Param("deviceId") Long deviceId, @Param("channelNo") Integer channelNo);

    /**
     * 根据流类型查询视频流列表
     *
     * @param streamType 流类型
     * @return 视频流列表
     */
    @Select("SELECT * FROM t_video_stream WHERE stream_type = #{streamType} AND deleted = 0 ORDER BY create_time DESC")
    List<VideoStreamEntity> selectByStreamType(@Param("streamType") Integer streamType);

    /**
     * 根据协议类型查询视频流列表
     *
     * @param protocol 协议类型
     * @return 视频流列表
     */
    @Select("SELECT * FROM t_video_stream WHERE protocol = #{protocol} AND deleted = 0 ORDER BY create_time DESC")
    List<VideoStreamEntity> selectByProtocol(@Param("protocol") Integer protocol);

    /**
     * 查询活跃的视频流（状态正常且在直播中）
     *
     * @return 活跃视频流列表
     */
    @Select("SELECT * FROM t_video_stream WHERE stream_status = 1 AND start_time IS NOT NULL AND (end_time IS NULL OR end_time > NOW()) AND deleted = 0 ORDER BY viewer_count DESC")
    List<VideoStreamEntity> selectActiveStreams();

    /**
     * 查询可用的视频流（状态正常且未达到最大观看人数）
     *
     * @return 可用视频流列表
     */
    @Select("SELECT * FROM t_video_stream WHERE stream_status = 1 AND (viewer_count < max_viewers OR max_viewers IS NULL) AND deleted = 0 ORDER BY viewer_count ASC")
    List<VideoStreamEntity> selectAvailableStreams();

    /**
     * 根据用户权限查询可访问的视频流
     *
     * @param userId 用户ID
     * @param permissionLevel 权限级别
     * @return 可访问视频流列表
     */
    @Select("SELECT vs.* FROM t_video_stream vs " +
            "LEFT JOIN t_user_device_permission udp ON vs.device_id = udp.device_id " +
            "WHERE udp.user_id = #{userId} AND udp.permission_type LIKE CONCAT('%', #{permissionLevel}, '%') AND vs.deleted = 0 " +
            "ORDER BY vs.stream_type, vs.channel_no")
    List<VideoStreamEntity> selectByUserPermission(@Param("userId") Long userId, @Param("permissionLevel") Integer permissionLevel);

    /**
     * 分页查询视频流
     *
     * @param page 分页参数
     * @param deviceId 设备ID（可选）
     * @param streamType 流类型（可选）
     * @param streamStatus 流状态（可选）
     * @param protocol 协议（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM t_video_stream WHERE deleted = 0 " +
            "<if test='deviceId != null'> AND device_id = #{deviceId} </if>" +
            "<if test='streamType != null'> AND stream_type = #{streamType} </if>" +
            "<if test='streamStatus != null'> AND stream_status = #{streamStatus} </if>" +
            "<if test='protocol != null'> AND protocol = #{protocol} </if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<VideoStreamEntity> selectStreamPage(Page<VideoStreamEntity> page,
                                               @Param("deviceId") Long deviceId,
                                               @Param("streamType") Integer streamType,
                                               @Param("streamStatus") Integer streamStatus,
                                               @Param("protocol") Integer protocol);

    /**
     * 更新视频流状态
     *
     * @param streamId 流ID
     * @param streamStatus 新状态
     * @return 影响行数
     */
    @Update("UPDATE t_video_stream SET stream_status = #{streamStatus}, update_time = NOW() WHERE stream_id = #{streamId}")
    @Transactional(rollbackFor = Exception.class)
    int updateStreamStatus(@Param("streamId") Long streamId, @Param("streamStatus") Integer streamStatus);

    /**
     * 增加观看人数
     *
     * @param streamId 流ID
     * @return 影响行数
     */
    @Update("UPDATE t_video_stream SET viewer_count = IFNULL(viewer_count, 0) + 1, update_time = NOW() WHERE stream_id = #{streamId}")
    @Transactional(rollbackFor = Exception.class)
    int increaseViewerCount(@Param("streamId") Long streamId);

    /**
     * 减少观看人数
     *
     * @param streamId 流ID
     * @return 影响行数
     */
    @Update("UPDATE t_video_stream SET viewer_count = GREATEST(IFNULL(viewer_count, 0) - 1, 0), update_time = NOW() WHERE stream_id = #{streamId}")
    @Transactional(rollbackFor = Exception.class)
    int decreaseViewerCount(@Param("streamId") Long streamId);

    /**
     * 更新视频流质量指标
     *
     * @param streamId 流ID
     * @param latency 延迟
     * @param packetLoss 丢包率
     * @param bandwidthUsage 带宽占用
     * @return 影响行数
     */
    @Update("UPDATE t_video_stream SET latency = #{latency}, packet_loss = #{packetLoss}, " +
            "bandwidth_usage = #{bandwidthUsage}, update_time = NOW() WHERE stream_id = #{streamId}")
    @Transactional(rollbackFor = Exception.class)
    int updateStreamQuality(@Param("streamId") Long streamId,
                            @Param("latency") Integer latency,
                            @Param("packetLoss") Double packetLoss,
                            @Param("bandwidthUsage") Integer bandwidthUsage);

    /**
     * 开始视频流
     *
     * @param streamId 流ID
     * @param startTime 开始时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_stream SET stream_status = 1, start_time = #{startTime}, end_time = NULL, update_time = NOW() WHERE stream_id = #{streamId}")
    @Transactional(rollbackFor = Exception.class)
    int startStream(@Param("streamId") Long streamId, @Param("startTime") LocalDateTime startTime);

    /**
     * 结束视频流
     *
     * @param streamId 流ID
     * @param endTime 结束时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_stream SET stream_status = 3, end_time = #{endTime}, viewer_count = 0, update_time = NOW() WHERE stream_id = #{streamId}")
    @Transactional(rollbackFor = Exception.class)
    int endStream(@Param("streamId") Long streamId, @Param("endTime") LocalDateTime endTime);

    /**
     * 记录断流
     *
     * @param streamId 流ID
     * @param disconnectTime 断流时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_stream SET stream_status = 2, disconnect_count = IFNULL(disconnect_count, 0) + 1, " +
            "last_disconnect_time = #{disconnectTime}, update_time = NOW() WHERE stream_id = #{streamId}")
    @Transactional(rollbackFor = Exception.class)
    int recordDisconnect(@Param("streamId") Long streamId, @Param("disconnectTime") LocalDateTime disconnectTime);

    /**
     * 记录重连
     *
     * @param streamId 流ID
     * @param reconnectTime 重连时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_stream SET stream_status = 1, reconnect_time = #{reconnectTime}, update_time = NOW() WHERE stream_id = #{streamId}")
    @Transactional(rollbackFor = Exception.class)
    int recordReconnect(@Param("streamId") Long streamId, @Param("reconnectTime") LocalDateTime reconnectTime);

    /**
     * 统计视频流数量
     *
     * @param deviceId 设备ID（可选）
     * @param streamType 流类型（可选）
     * @param streamStatus 流状态（可选）
     * @return 统计数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_video_stream WHERE deleted = 0 " +
            "<if test='deviceId != null'> AND device_id = #{deviceId} </if>" +
            "<if test='streamType != null'> AND stream_type = #{streamType} </if>" +
            "<if test='streamStatus != null'> AND stream_status = #{streamStatus} </if>" +
            "</script>")
    Long countStreams(@Param("deviceId") Long deviceId,
                      @Param("streamType") Integer streamType,
                      @Param("streamStatus") Integer streamStatus);

    /**
     * 统计各协议类型的视频流数量
     *
     * @return 统计结果
     */
    @Select("SELECT protocol, COUNT(*) as count FROM t_video_stream WHERE deleted = 0 GROUP BY protocol ORDER BY count DESC")
    List<java.util.Map<String, Object>> countStreamsByProtocol();

    /**
     * 统计各状态的视频流数量
     *
     * @return 统计结果
     */
    @Select("SELECT stream_status, COUNT(*) as count FROM t_video_stream WHERE deleted = 0 GROUP BY stream_status ORDER BY stream_status")
    List<java.util.Map<String, Object>> countStreamsByStatus();
}
