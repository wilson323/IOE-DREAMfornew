package net.lab1024.sa.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.video.entity.VideoMapHotspotEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 地图热点Service接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
public interface VideoMapHotspotService extends IService<VideoMapHotspotEntity> {

    /**
     * 查询地图的所有热点
     */
    List<VideoMapHotspotEntity> getHotspotsByMapId(Long mapImageId);

    /**
     * 查询显示状态的热点
     */
    List<VideoMapHotspotEntity> getVisibleHotspots(Long mapImageId);

    /**
     * 查询设备关联热点
     */
    VideoMapHotspotEntity getHotspotByDeviceId(Long deviceId);

    /**
     * 查询类型热点
     */
    List<VideoMapHotspotEntity> getHotspotsByType(Long mapImageId, String hotspotType);

    /**
     * 查询范围内热点
     */
    List<VideoMapHotspotEntity> getHotspotsWithinBounds(Long mapImageId, BigDecimal minX, BigDecimal maxX,
                                                        BigDecimal minY, BigDecimal maxY);

    /**
     * 添加热点
     */
    Long addHotspot(VideoMapHotspotEntity hotspot);

    /**
     * 批量添加热点
     */
    Integer batchAddHotspots(List<VideoMapHotspotEntity> hotspots);

    /**
     * 更新热点位置
     */
    void updateHotspotPosition(Long id, BigDecimal x, BigDecimal y);

    /**
     * 更新热点显示状态
     */
    void updateHotspotStatus(Long id, Integer displayStatus);

    /**
     * 删除热点
     */
    void deleteHotspot(Long id);
}
