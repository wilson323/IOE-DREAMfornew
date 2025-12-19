package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.video.entity.VideoDisplayTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上墙任务数据访问层
 * <p>
 * 提供上墙任务数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoDisplayTaskDao extends BaseMapper<VideoDisplayTaskEntity> {

    /**
     * 根据电视墙ID查询任务列表
     *
     * @param wallId 电视墙ID
     * @return 任务列表
     */
    @Select("SELECT * FROM t_video_display_task WHERE wall_id = #{wallId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<VideoDisplayTaskEntity> selectByWallId(@Param("wallId") Long wallId);

    /**
     * 根据窗口ID查询任务列表
     *
     * @param windowId 窗口ID
     * @return 任务列表
     */
    @Select("SELECT * FROM t_video_display_task WHERE window_id = #{windowId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<VideoDisplayTaskEntity> selectByWindowId(@Param("windowId") Long windowId);

    /**
     * 根据状态查询任务列表
     *
     * @param wallId 电视墙ID
     * @param status 状态：0-等待，1-执行中，2-完成，3-失败
     * @return 任务列表
     */
    @Select("SELECT * FROM t_video_display_task WHERE wall_id = #{wallId} AND status = #{status} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<VideoDisplayTaskEntity> selectByWallIdAndStatus(@Param("wallId") Long wallId, @Param("status") Integer status);

    /**
     * 查询窗口的当前任务（执行中或等待中）
     *
     * @param windowId 窗口ID
     * @return 任务实体
     */
    @Select("SELECT * FROM t_video_display_task WHERE window_id = #{windowId} AND status IN (0, 1) AND deleted_flag = 0 ORDER BY create_time DESC LIMIT 1")
    VideoDisplayTaskEntity selectCurrentTaskByWindowId(@Param("windowId") Long windowId);

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 状态：0-等待，1-执行中，2-完成，3-失败
     * @param errorMsg 错误信息（可选）
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE t_video_display_task SET status = #{status}, update_time = NOW()" +
            "<if test='errorMsg != null'>, error_msg = #{errorMsg}</if>" +
            "WHERE task_id = #{taskId}" +
            "</script>")
    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("taskId") Long taskId, @Param("status") Integer status, @Param("errorMsg") String errorMsg);

    /**
     * 更新任务开始时间
     *
     * @param taskId 任务ID
     * @param startTime 开始时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_display_task SET start_time = #{startTime}, status = 1, update_time = NOW() WHERE task_id = #{taskId}")
    @Transactional(rollbackFor = Exception.class)
    int updateStartTime(@Param("taskId") Long taskId, @Param("startTime") LocalDateTime startTime);

    /**
     * 更新任务结束时间
     *
     * @param taskId 任务ID
     * @param endTime 结束时间
     * @param status 状态：2-完成，3-失败
     * @return 影响行数
     */
    @Update("UPDATE t_video_display_task SET end_time = #{endTime}, status = #{status}, update_time = NOW() WHERE task_id = #{taskId}")
    @Transactional(rollbackFor = Exception.class)
    int updateEndTime(@Param("taskId") Long taskId, @Param("endTime") LocalDateTime endTime, @Param("status") Integer status);
}
