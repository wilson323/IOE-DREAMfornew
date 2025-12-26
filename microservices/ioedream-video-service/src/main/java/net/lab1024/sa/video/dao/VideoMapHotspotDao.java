package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.video.VideoMapHotspotEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地图热点DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Mapper
public interface VideoMapHotspotDao extends BaseMapper<VideoMapHotspotEntity> {

    /**
     * 查询地图的所有热点
     */
    List<VideoMapHotspotEntity> selectByMapImageId(@Param("mapImageId") Long mapImageId);

    /**
     * 查询显示状态的热点
     */
    List<VideoMapHotspotEntity> selectVisibleHotspots(@Param("mapImageId") Long mapImageId);

    /**
     * 查询设备关联热点
     */
    VideoMapHotspotEntity selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 查询类型热点
     */
    List<VideoMapHotspotEntity> selectByType(@Param("mapImageId") Long mapImageId,
                                             @Param("hotspotType") String hotspotType);

    /**
     * 查询范围内热点
     */
    List<VideoMapHotspotEntity> selectWithinBounds(@Param("mapImageId") Long mapImageId,
                                                  @Param("minX") java.math.BigDecimal minX,
                                                  @Param("maxX") java.math.BigDecimal maxX,
                                                  @Param("minY") java.math.BigDecimal minY,
                                                  @Param("maxY") java.math.BigDecimal maxY);
}
