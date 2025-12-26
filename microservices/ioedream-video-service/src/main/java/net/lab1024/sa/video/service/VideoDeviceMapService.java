package net.lab1024.sa.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.video.entity.VideoDeviceMapEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 视频设备地图Service接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
public interface VideoDeviceMapService extends IService<VideoDeviceMapEntity> {

    /**
     * 查询地图的所有设备
     */
    List<VideoDeviceMapEntity> getDevicesByMapId(Long mapImageId);

    /**
     * 查询区域的所有设备
     */
    List<VideoDeviceMapEntity> getDevicesByAreaId(Long areaId);

    /**
     * 查询楼层设备
     */
    List<VideoDeviceMapEntity> getDevicesByFloor(Long areaId, Integer floorLevel);

    /**
     * 查询显示状态的设备
     */
    List<VideoDeviceMapEntity> getDevicesByStatus(Integer displayStatus);

    /**
     * 查询范围内的设备
     */
    List<VideoDeviceMapEntity> getDevicesWithinBounds(Long mapImageId, BigDecimal minX, BigDecimal maxX,
                                                      BigDecimal minY, BigDecimal maxY);

    /**
     * 查询附近设备
     */
    List<VideoDeviceMapEntity> getNearbyDevices(Long mapImageId, BigDecimal x, BigDecimal y, BigDecimal radius);

    /**
     * 添加设备到地图
     */
    Long addDeviceToMap(VideoDeviceMapEntity deviceMap);

    /**
     * 批量添加设备到地图
     */
    Integer batchAddDevices(List<VideoDeviceMapEntity> deviceMaps);

    /**
     * 更新设备位置
     */
    void updateDevicePosition(Long id, BigDecimal x, BigDecimal y, BigDecimal z);

    /**
     * 更新显示状态
     */
    void updateDisplayStatus(Long id, Integer displayStatus);

    /**
     * 从地图移除设备
     */
    void removeDeviceFromMap(Long id);
}
