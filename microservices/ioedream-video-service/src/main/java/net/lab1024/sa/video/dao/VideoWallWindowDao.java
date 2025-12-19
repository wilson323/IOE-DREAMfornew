package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.video.entity.VideoWallWindowEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 电视墙窗口数据访问层
 * <p>
 * 提供电视墙窗口数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoWallWindowDao extends BaseMapper<VideoWallWindowEntity> {

    /**
     * 根据电视墙ID查询窗口列表
     *
     * @param wallId 电视墙ID
     * @return 窗口列表
     */
    @Select("SELECT * FROM t_video_wall_window WHERE wall_id = #{wallId} AND deleted_flag = 0 ORDER BY window_no ASC")
    List<VideoWallWindowEntity> selectByWallId(@Param("wallId") Long wallId);

    /**
     * 根据电视墙ID和窗口编号查询窗口
     *
     * @param wallId 电视墙ID
     * @param windowNo 窗口编号
     * @return 窗口实体
     */
    @Select("SELECT * FROM t_video_wall_window WHERE wall_id = #{wallId} AND window_no = #{windowNo} AND deleted_flag = 0 LIMIT 1")
    VideoWallWindowEntity selectByWallIdAndWindowNo(@Param("wallId") Long wallId, @Param("windowNo") Integer windowNo);

    /**
     * 根据状态查询窗口列表
     *
     * @param wallId 电视墙ID
     * @param status 状态：0-空闲，1-播放中
     * @return 窗口列表
     */
    @Select("SELECT * FROM t_video_wall_window WHERE wall_id = #{wallId} AND status = #{status} AND deleted_flag = 0 ORDER BY window_no ASC")
    List<VideoWallWindowEntity> selectByWallIdAndStatus(@Param("wallId") Long wallId, @Param("status") Integer status);

    /**
     * 更新窗口状态
     *
     * @param windowId 窗口ID
     * @param status 状态：0-空闲，1-播放中
     * @return 影响行数
     */
    @Update("UPDATE t_video_wall_window SET status = #{status}, update_time = NOW() WHERE window_id = #{windowId}")
    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("windowId") Long windowId, @Param("status") Integer status);

    /**
     * 批量删除电视墙的窗口
     *
     * @param wallId 电视墙ID
     * @return 影响行数
     */
    @Update("UPDATE t_video_wall_window SET deleted_flag = 1, update_time = NOW() WHERE wall_id = #{wallId}")
    @Transactional(rollbackFor = Exception.class)
    int deleteByWallId(@Param("wallId") Long wallId);
}
