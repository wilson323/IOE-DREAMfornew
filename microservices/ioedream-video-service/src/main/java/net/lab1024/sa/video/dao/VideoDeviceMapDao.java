package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.video.entity.VideoDeviceMapEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频设备地图DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Mapper
public interface VideoDeviceMapDao extends BaseMapper<VideoDeviceMapEntity> {

    /**
     * 查询地图的所有设备
     */
    List<VideoDeviceMapEntity> selectByMapImageId(@Param("mapImageId") Long mapImageId);

    /**
     * 查询区域的所有设备
     */
    List<VideoDeviceMapEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询楼层设备
     */
    List<VideoDeviceMapEntity> selectByFloorLevel(@Param("areaId") Long areaId,
                                                  @Param("floorLevel") Integer floorLevel);

    /**
     * 查询显示状态的设备
     */
    List<VideoDeviceMapEntity> selectByDisplayStatus(@Param("displayStatus") Integer displayStatus);

    /**
     * 查询范围内的设备
     */
    List<VideoDeviceMapEntity> selectWithinBounds(@Param("mapImageId") Long mapImageId,
                                                 @Param("minX") java.math.BigDecimal minX,
                                                 @Param("maxX") java.math.BigDecimal maxX,
                                                 @Param("minY") java.math.BigDecimal minY,
                                                 @Param("maxY") java.math.BigDecimal maxY);

    /**
     * 查询附近设备
     */
    List<VideoDeviceMapEntity> selectNearbyDevices(@Param("mapImageId") Long mapImageId,
                                                   @Param("xCoordinate") java.math.BigDecimal x,
                                                   @Param("yCoordinate") java.math.BigDecimal y,
                                                   @Param("radius") java.math.BigDecimal radius);
}
