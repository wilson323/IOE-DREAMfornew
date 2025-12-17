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

import net.lab1024.sa.video.entity.VideoPTZEntity;

/**
 * 视频云台控制数据访问层
 * <p>
 * 提供PTZ控制数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoPTZDao extends BaseMapper<VideoPTZEntity> {

    /**
     * 根据设备ID查询PTZ控制记录
     *
     * @param deviceId 设备ID
     * @return PTZ控制记录列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE device_id = #{deviceId} AND deleted = 0 ORDER BY control_time DESC")
    List<VideoPTZEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据用户ID查询PTZ控制记录
     *
     * @param userId 用户ID
     * @return PTZ控制记录列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE user_id = #{userId} AND deleted = 0 ORDER BY control_time DESC")
    List<VideoPTZEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据会话ID查询PTZ控制记录
     *
     * @param sessionId 会话ID
     * @return PTZ控制记录列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY control_time DESC")
    List<VideoPTZEntity> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据控制类型查询PTZ控制记录
     *
     * @param controlType 控制类型
     * @return PTZ控制记录列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE control_type = #{controlType} AND deleted = 0 ORDER BY control_time DESC")
    List<VideoPTZEntity> selectByControlType(@Param("controlType") Integer controlType);

    /**
     * 根据控制命令查询PTZ控制记录
     *
     * @param controlCommand 控制命令
     * @return PTZ控制记录列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE control_command = #{controlCommand} AND deleted = 0 ORDER BY control_time DESC")
    List<VideoPTZEntity> selectByControlCommand(@Param("controlCommand") String controlCommand);

    /**
     * 根据控制状态查询PTZ控制记录
     *
     * @param controlStatus 控制状态
     * @return PTZ控制记录列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE control_status = #{controlStatus} AND deleted = 0 ORDER BY control_time DESC")
    List<VideoPTZEntity> selectByControlStatus(@Param("controlStatus") Integer controlStatus);

    /**
     * 查询正在执行中的PTZ控制
     *
     * @return 执行中的PTZ控制列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE control_status = 1 AND deleted = 0 ORDER BY control_time ASC")
    List<VideoPTZEntity> selectExecutingControls();

    /**
     * 查询失败的PTZ控制
     *
     * @return 失败的PTZ控制列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE control_status = 3 AND deleted = 0 ORDER BY control_time DESC")
    List<VideoPTZEntity> selectFailedControls();

    /**
     * 查询超时的PTZ控制
     *
     * @param timeoutSeconds 超时时间（秒）
     * @return 超时的PTZ控制列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE control_status = 1 AND " +
            "execute_start_time IS NOT NULL AND execute_start_time < DATE_SUB(NOW(), INTERVAL #{timeoutSeconds} SECOND) AND deleted = 0")
    List<VideoPTZEntity> selectTimeoutControls(@Param("timeoutSeconds") Integer timeoutSeconds);

    /**
     * 根据设备ID和通道号查询PTZ控制记录
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @return PTZ控制记录列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE device_id = #{deviceId} AND channel_no = #{channelNo} AND deleted = 0 ORDER BY control_time DESC")
    List<VideoPTZEntity> selectByDeviceAndChannel(@Param("deviceId") Long deviceId, @Param("channelNo") Integer channelNo);

    /**
     * 查询预置位记录
     *
     * @param deviceId 设备ID
     * @return 预置位记录列表
     */
    @Select("SELECT * FROM t_video_ptz WHERE device_id = #{deviceId} AND control_type = 3 AND deleted = 0 ORDER BY preset_number ASC")
    List<VideoPTZEntity> selectPresetsByDevice(@Param("deviceId") Long deviceId);

    /**
     * 根据预置位编号查询记录
     *
     * @param deviceId 设备ID
     * @param presetNumber 预置位编号
     * @return 预置位记录
     */
    @Select("SELECT * FROM t_video_ptz WHERE device_id = #{deviceId} AND control_type = 3 AND preset_number = #{presetNumber} AND deleted = 0 LIMIT 1")
    VideoPTZEntity selectPresetByNumber(@Param("deviceId") Long deviceId, @Param("presetNumber") Integer presetNumber);

    /**
     * 分页查询PTZ控制记录
     *
     * @param page 分页参数
     * @param deviceId 设备ID（可选）
     * @param userId 用户ID（可选）
     * @param controlType 控制类型（可选）
     * @param controlStatus 控制状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM t_video_ptz WHERE deleted = 0 " +
            "<if test='deviceId != null'> AND device_id = #{deviceId} </if>" +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='controlType != null'> AND control_type = #{controlType} </if>" +
            "<if test='controlStatus != null'> AND control_status = #{controlStatus} </if>" +
            "<if test='startTime != null'> AND control_time >= #{startTime} </if>" +
            "<if test='endTime != null'> AND control_time <= #{endTime} </if>" +
            "ORDER BY control_time DESC" +
            "</script>")
    IPage<VideoPTZEntity> selectPTZPage(Page<VideoPTZEntity> page,
                                       @Param("deviceId") Long deviceId,
                                       @Param("userId") Long userId,
                                       @Param("controlType") Integer controlType,
                                       @Param("controlStatus") Integer controlStatus,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 更新PTZ控制状态
     *
     * @param ptzId PTZ控制ID
     * @param controlStatus 新状态
     * @return 影响行数
     */
    @Update("UPDATE t_video_ptz SET control_status = #{controlStatus}, update_time = NOW() WHERE ptz_id = #{ptzId}")
    @Transactional(rollbackFor = Exception.class)
    int updateControlStatus(@Param("ptzId") Long ptzId, @Param("controlStatus") Integer controlStatus);

    /**
     * 更新PTZ控制结果
     *
     * @param ptzId PTZ控制ID
     * @param controlResult 控制结果
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @return 影响行数
     */
    @Update("UPDATE t_video_ptz SET control_result = #{controlResult}, error_code = #{errorCode}, " +
            "error_message = #{errorMessage}, execute_end_time = NOW(), update_time = NOW() WHERE ptz_id = #{ptzId}")
    @Transactional(rollbackFor = Exception.class)
    int updateControlResult(@Param("ptzId") Long ptzId,
                           @Param("controlResult") Integer controlResult,
                           @Param("errorCode") String errorCode,
                           @Param("errorMessage") String errorMessage);

    /**
     * 开始执行PTZ控制
     *
     * @param ptzId PTZ控制ID
     * @param executeStartTime 执行开始时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_ptz SET control_status = 1, execute_start_time = #{executeStartTime}, update_time = NOW() WHERE ptz_id = #{ptzId}")
    @Transactional(rollbackFor = Exception.class)
    int startExecution(@Param("ptzId") Long ptzId, @Param("executeStartTime") LocalDateTime executeStartTime);

    /**
     * 完成PTZ控制执行
     *
     * @param ptzId PTZ控制ID
     * @param executeEndTime 执行结束时间
     * @param executeDuration 执行时长
     * @param controlResult 控制结果
     * @return 影响行数
     */
    @Update("UPDATE t_video_ptz SET control_status = 2, execute_end_time = #{executeEndTime}, " +
            "execute_duration = #{executeDuration}, control_result = #{controlResult}, update_time = NOW() WHERE ptz_id = #{ptzId}")
    @Transactional(rollbackFor = Exception.class)
    int completeExecution(@Param("ptzId") Long ptzId,
                          @Param("executeEndTime") LocalDateTime executeEndTime,
                          @Param("executeDuration") Long executeDuration,
                          @Param("controlResult") Integer controlResult);

    /**
     * 更新云台当前位置
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @param panAngle 水平角度
     * @param tiltAngle 垂直角度
     * @param zoomRatio 变倍倍数
     * @return 影响行数
     */
    @Update("UPDATE t_video_ptz SET pan_angle = #{panAngle}, tilt_angle = #{tiltAngle}, " +
            "zoom_ratio = #{zoomRatio}, update_time = NOW() " +
            "WHERE device_id = #{deviceId} AND channel_no = #{channelNo} AND control_type = 1 AND deleted = 0 " +
            "ORDER BY control_time DESC LIMIT 1")
    @Transactional(rollbackFor = Exception.class)
    int updateCurrentPosition(@Param("deviceId") Long deviceId,
                             @Param("channelNo") Integer channelNo,
                             @Param("panAngle") Double panAngle,
                             @Param("tiltAngle") Double tiltAngle,
                             @Param("zoomRatio") Double zoomRatio);

    /**
     * 删除设备所有预置位
     *
     * @param deviceId 设备ID
     * @return 影响行数
     */
    @Update("UPDATE t_video_ptz SET deleted = 1, update_time = NOW() " +
            "WHERE device_id = #{deviceId} AND control_type = 3 AND deleted = 0")
    @Transactional(rollbackFor = Exception.class)
    int deleteAllPresets(@Param("deviceId") Long deviceId);

    /**
     * 统计PTZ控制数量
     *
     * @param deviceId 设备ID（可选）
     * @param userId 用户ID（可选）
     * @param controlType 控制类型（可选）
     * @param controlStatus 控制状态（可选）
     * @return 统计数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_video_ptz WHERE deleted = 0 " +
            "<if test='deviceId != null'> AND device_id = #{deviceId} </if>" +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='controlType != null'> AND control_type = #{controlType} </if>" +
            "<if test='controlStatus != null'> AND control_status = #{controlStatus} </if>" +
            "</script>")
    Long countPTZControls(@Param("deviceId") Long deviceId,
                         @Param("userId") Long userId,
                         @Param("controlType") Integer controlType,
                         @Param("controlStatus") Integer controlStatus);

    /**
     * 统计各控制类型的PTZ控制数量
     *
     * @return 统计结果
     */
    @Select("SELECT control_type, COUNT(*) as count FROM t_video_ptz WHERE deleted = 0 GROUP BY control_type ORDER BY count DESC")
    List<java.util.Map<String, Object>> countPTZByType();

    /**
     * 统计各控制状态的PTZ控制数量
     *
     * @return 统计结果
     */
    @Select("SELECT control_status, COUNT(*) as count FROM t_video_ptz WHERE deleted = 0 GROUP BY control_status ORDER BY control_status")
    List<java.util.Map<String, Object>> countPTZByStatus();

    /**
     * 统计各用户PTZ控制数量
     *
     * @return 统计结果
     */
    @Select("SELECT user_id, username, COUNT(*) as count FROM t_video_ptz WHERE deleted = 0 " +
            "GROUP BY user_id, username ORDER BY count DESC LIMIT 20")
    List<java.util.Map<String, Object>> countPTZByUser();

    /**
     * 查询指定时间段内的PTZ控制统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT DATE(control_time) as date, COUNT(*) as count, " +
            "AVG(IFNULL(execute_duration, 0)) as avg_duration " +
            "FROM t_video_ptz WHERE control_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 " +
            "GROUP BY DATE(control_time) ORDER BY date DESC")
    List<java.util.Map<String, Object>> getPTZStatistics(@Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询设备PTZ控制频率
     *
     * @param deviceId 设备ID
     * @param hours 统计时间范围（小时）
     * @return 控制频率统计
     */
    @Select("SELECT control_type, control_command, COUNT(*) as count " +
            "FROM t_video_ptz WHERE device_id = #{deviceId} AND control_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) AND deleted = 0 " +
            "GROUP BY control_type, control_command ORDER BY count DESC")
    List<java.util.Map<String, Object>> getDevicePTZFrequency(@Param("deviceId") Long deviceId, @Param("hours") Integer hours);
}
