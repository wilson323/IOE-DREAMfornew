package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.video.VideoMapImageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频地图图片DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Mapper
public interface VideoMapImageDao extends BaseMapper<VideoMapImageEntity> {

    /**
     * 查询区域地图
     */
    List<VideoMapImageEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询楼层地图
     */
    VideoMapImageEntity selectByFloor(@Param("areaId") Long areaId,
                                       @Param("floorLevel") Integer floorLevel);

    /**
     * 查询默认地图
     */
    VideoMapImageEntity selectDefaultMap(@Param("areaId") Long areaId);

    /**
     * 查询启用状态的地图
     */
    List<VideoMapImageEntity> selectActiveMaps();
}
