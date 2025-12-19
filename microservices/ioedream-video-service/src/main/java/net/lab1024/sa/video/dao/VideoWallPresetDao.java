package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.video.entity.VideoWallPresetEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 电视墙预案数据访问层
 * <p>
 * 提供电视墙预案数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoWallPresetDao extends BaseMapper<VideoWallPresetEntity> {

    /**
     * 根据电视墙ID查询预案列表
     *
     * @param wallId 电视墙ID
     * @return 预案列表
     */
    @Select("SELECT * FROM t_video_wall_preset WHERE wall_id = #{wallId} AND deleted_flag = 0 ORDER BY is_default DESC, create_time DESC")
    List<VideoWallPresetEntity> selectByWallId(@Param("wallId") Long wallId);

    /**
     * 查询电视墙的默认预案
     *
     * @param wallId 电视墙ID
     * @return 默认预案实体
     */
    @Select("SELECT * FROM t_video_wall_preset WHERE wall_id = #{wallId} AND is_default = 1 AND deleted_flag = 0 LIMIT 1")
    VideoWallPresetEntity selectDefaultPreset(@Param("wallId") Long wallId);

    /**
     * 根据编码查询预案
     *
     * @param presetCode 预案编码
     * @return 预案实体
     */
    @Select("SELECT * FROM t_video_wall_preset WHERE preset_code = #{presetCode} AND deleted_flag = 0 LIMIT 1")
    VideoWallPresetEntity selectByCode(@Param("presetCode") String presetCode);
}
