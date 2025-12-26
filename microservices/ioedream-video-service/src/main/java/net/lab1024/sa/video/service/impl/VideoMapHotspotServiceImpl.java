package net.lab1024.sa.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoMapHotspotDao;
import net.lab1024.sa.video.entity.VideoMapHotspotEntity;
import net.lab1024.sa.video.service.VideoMapHotspotService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 地图热点Service实现
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Service
@Slf4j
public class VideoMapHotspotServiceImpl extends ServiceImpl<VideoMapHotspotDao, VideoMapHotspotEntity>
        implements VideoMapHotspotService {

    @Resource
    private VideoMapHotspotDao hotspotDao;

    @Override
    public List<VideoMapHotspotEntity> getHotspotsByMapId(Long mapImageId) {
        log.debug("[地图热点] 查询地图热点: mapImageId={}", mapImageId);
        return hotspotDao.selectByMapImageId(mapImageId);
    }

    @Override
    public List<VideoMapHotspotEntity> getVisibleHotspots(Long mapImageId) {
        log.debug("[地图热点] 查询可见热点: mapImageId={}", mapImageId);
        return hotspotDao.selectVisibleHotspots(mapImageId);
    }

    @Override
    public VideoMapHotspotEntity getHotspotByDeviceId(Long deviceId) {
        log.debug("[地图热点] 查询设备热点: deviceId={}", deviceId);
        return hotspotDao.selectByDeviceId(deviceId);
    }

    @Override
    public List<VideoMapHotspotEntity> getHotspotsByType(Long mapImageId, String hotspotType) {
        log.debug("[地图热点] 查询类型热点: mapImageId={}, type={}", mapImageId, hotspotType);
        return hotspotDao.selectByType(mapImageId, hotspotType);
    }

    @Override
    public List<VideoMapHotspotEntity> getHotspotsWithinBounds(Long mapImageId, BigDecimal minX, BigDecimal maxX,
                                                                  BigDecimal minY, BigDecimal maxY) {
        log.debug("[地图热点] 查询范围内热点: mapImageId={}, bounds=[{}, {}, {}, {}]",
                mapImageId, minX, maxX, minY, maxY);
        return hotspotDao.selectWithinBounds(mapImageId, minX, maxX, minY, maxY);
    }

    @Override
    public Long addHotspot(VideoMapHotspotEntity hotspot) {
        log.info("[地图热点] 添加热点: hotspotName={}, mapImageId={}",
                hotspot.getHotspotName(), hotspot.getMapImageId());
        this.save(hotspot);
        return hotspot.getId();
    }

    @Override
    public Integer batchAddHotspots(List<VideoMapHotspotEntity> hotspots) {
        log.info("[地图热点] 批量添加热点: count={}", hotspots.size());
        hotspots.forEach(this::save);
        return hotspots.size();
    }

    @Override
    public void updateHotspotPosition(Long id, BigDecimal x, BigDecimal y) {
        log.info("[地图热点] 更新热点位置: id=[{}, {}]", x, y);
        VideoMapHotspotEntity hotspot = this.getById(id);
        if (hotspot != null) {
            hotspot.setXCoordinate(x);
            hotspot.setYCoordinate(y);
            this.updateById(hotspot);
        }
    }

    @Override
    public void updateHotspotStatus(Long id, Integer displayStatus) {
        log.info("[地图热点] 更新热点状态: id={}, status={}", id, displayStatus);
        VideoMapHotspotEntity hotspot = this.getById(id);
        if (hotspot != null) {
            hotspot.setDisplayStatus(displayStatus);
            this.updateById(hotspot);
        }
    }

    @Override
    public void deleteHotspot(Long id) {
        log.info("[地图热点] 删除热点: id={}", id);
        this.removeById(id);
    }
}
