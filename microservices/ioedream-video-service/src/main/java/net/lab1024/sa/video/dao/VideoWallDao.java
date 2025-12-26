package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.video.VideoWallEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 电视墙数据访问层
 * <p>
 * 提供电视墙数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoWallDao extends BaseMapper<VideoWallEntity> {

    /**
     * 根据区域ID查询电视墙列表
     *
     * @param regionId 区域ID
     * @return 电视墙列表
     */
    @Select("SELECT * FROM t_video_wall WHERE region_id = #{regionId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<VideoWallEntity> selectByRegionId(@Param("regionId") Long regionId);

    /**
     * 根据状态查询电视墙列表
     *
     * @param status 状态：0-禁用，1-启用
     * @return 电视墙列表
     */
    @Select("SELECT * FROM t_video_wall WHERE status = #{status} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<VideoWallEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据编码查询电视墙
     *
     * @param wallCode 电视墙编码
     * @return 电视墙实体
     */
    @Select("SELECT * FROM t_video_wall WHERE wall_code = #{wallCode} AND deleted_flag = 0 LIMIT 1")
    VideoWallEntity selectByCode(@Param("wallCode") String wallCode);
}
