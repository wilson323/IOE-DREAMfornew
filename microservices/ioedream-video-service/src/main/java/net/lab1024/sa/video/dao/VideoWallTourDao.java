package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.video.entity.VideoWallTourEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 电视墙轮巡数据访问层
 * <p>
 * 提供电视墙轮巡数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoWallTourDao extends BaseMapper<VideoWallTourEntity> {

    /**
     * 根据电视墙ID查询轮巡列表
     *
     * @param wallId 电视墙ID
     * @return 轮巡列表
     */
    @Select("SELECT * FROM t_video_wall_tour WHERE wall_id = #{wallId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<VideoWallTourEntity> selectByWallId(@Param("wallId") Long wallId);

    /**
     * 根据状态查询轮巡列表
     *
     * @param wallId 电视墙ID
     * @param status 状态：0-停止，1-运行中
     * @return 轮巡列表
     */
    @Select("SELECT * FROM t_video_wall_tour WHERE wall_id = #{wallId} AND status = #{status} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<VideoWallTourEntity> selectByWallIdAndStatus(@Param("wallId") Long wallId, @Param("status") Integer status);

    /**
     * 更新轮巡状态
     *
     * @param tourId 轮巡ID
     * @param status 状态：0-停止，1-运行中
     * @return 影响行数
     */
    @Update("UPDATE t_video_wall_tour SET status = #{status}, update_time = NOW() WHERE tour_id = #{tourId}")
    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("tourId") Long tourId, @Param("status") Integer status);

    /**
     * 更新轮巡当前索引
     *
     * @param tourId 轮巡ID
     * @param currentIndex 当前索引
     * @return 影响行数
     */
    @Update("UPDATE t_video_wall_tour SET current_index = #{currentIndex}, update_time = NOW() WHERE tour_id = #{tourId}")
    @Transactional(rollbackFor = Exception.class)
    int updateCurrentIndex(@Param("tourId") Long tourId, @Param("currentIndex") Integer currentIndex);
}
