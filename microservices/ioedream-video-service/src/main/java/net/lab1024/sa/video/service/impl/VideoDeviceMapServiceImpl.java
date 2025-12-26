package net.lab1024.sa.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoDeviceMapDao;
import net.lab1024.sa.video.entity.VideoDeviceMapEntity;
import net.lab1024.sa.video.service.VideoDeviceMapService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 视频设备地图Service实现
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Service
@Slf4j
public class VideoDeviceMapServiceImpl extends ServiceImpl<VideoDeviceMapDao, VideoDeviceMapEntity>
        implements VideoDeviceMapService {

    @Resource
    private VideoDeviceMapDao deviceMapDao;

    @Override
    public List<VideoDeviceMapEntity> getDevicesByMapId(Long mapImageId) {
        log.debug("[视频设备地图] 查询地图设备: mapImageId={}", mapImageId);
        return deviceMapDao.selectByMapImageId(mapImageId);
    }

    @Override
    public List<VideoDeviceMapEntity> getDevicesByAreaId(Long areaId) {
        log.debug("[视频设备地图] 查询区域设备: areaId={}", areaId);
        return deviceMapDao.selectByAreaId(areaId);
    }

    @Override
    public List<VideoDeviceMapEntity> getDevicesByFloor(Long areaId, Integer floorLevel) {
        log.debug("[视频设备地图] 查询楼层设备: areaId={}, floor={}", areaId, floorLevel);
        return deviceMapDao.selectByFloorLevel(areaId, floorLevel);
    }

    @Override
    public List<VideoDeviceMapEntity> getDevicesByStatus(Integer displayStatus) {
        log.debug("[视频设备地图] 查询状态设备: displayStatus={}", displayStatus);
        return deviceMapDao.selectByDisplayStatus(displayStatus);
    }

    @Override
    public List<VideoDeviceMapEntity> getDevicesWithinBounds(Long mapImageId, BigDecimal minX, BigDecimal maxX,
                                                              BigDecimal minY, BigDecimal maxY) {
        log.debug("[视频设备地图] 查询范围内设备: mapImageId={}, bounds=[{}, {}, {}, {}]",
                mapImageId, minX, maxX, minY, maxY);
        return deviceMapDao.selectWithinBounds(mapImageId, minX, maxX, minY, maxY);
    }

    @Override
    public List<VideoDeviceMapEntity> getNearbyDevices(Long mapImageId, BigDecimal x, BigDecimal y, BigDecimal radius) {
        log.debug("[视频设备地图] 查询附近设备: mapImageId={}, center=[{}, {}], radius={}",
                mapImageId, x, y, radius);
        return deviceMapDao.selectNearbyDevices(mapImageId, x, y, radius);
    }

    @Override
    public Long addDeviceToMap(VideoDeviceMapEntity deviceMap) {
        log.info("[视频设备地图] 添加设备到地图: deviceId={}, mapImageId={}",
                deviceMap.getDeviceId(), deviceMap.getMapImageId());
        this.save(deviceMap);
        return deviceMap.getId();
    }

    @Override
    public Integer batchAddDevices(List<VideoDeviceMapEntity> deviceMaps) {
        log.info("[视频设备地图] 批量添加设备: count={}", deviceMaps.size());
        deviceMaps.forEach(this::save);
        return deviceMaps.size();
    }

    @Override
    public void updateDevicePosition(Long id, BigDecimal x, BigDecimal y, BigDecimal z) {
        log.info("[视频设备地图] 更新设备位置: id=[{}, {}]", x, y);
        VideoDeviceMapEntity deviceMap = this.getById(id);
        if (deviceMap != null) {
            deviceMap.setXCoordinate(x);
            deviceMap.setYCoordinate(y);
            if (z != null) {
                deviceMap.setZCoordinate(z);
            }
            this.updateById(deviceMap);
        }
    }

    @Override
    public void updateDisplayStatus(Long id, Integer displayStatus) {
        log.info("[视频设备地图] 更新显示状态: id={}, status={}", id, displayStatus);
        VideoDeviceMapEntity deviceMap = this.getById(id);
        if (deviceMap != null) {
            deviceMap.setDisplayStatus(displayStatus);
            this.updateById(deviceMap);
        }
    }

    @Override
    public void removeDeviceFromMap(Long id) {
        log.info("[视频设备地图] 移除设备: id={}", id);
        this.removeById(id);
    }
}
