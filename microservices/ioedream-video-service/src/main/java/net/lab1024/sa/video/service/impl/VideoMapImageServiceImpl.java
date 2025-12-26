package net.lab1024.sa.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoMapImageDao;
import net.lab1024.sa.video.entity.VideoMapImageEntity;
import net.lab1024.sa.video.service.VideoMapImageService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 视频地图图片Service实现
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Service
@Slf4j
public class VideoMapImageServiceImpl extends ServiceImpl<VideoMapImageDao, VideoMapImageEntity>
        implements VideoMapImageService {

    @Resource
    private VideoMapImageDao mapImageDao;

    @Override
    public List<VideoMapImageEntity> getMapsByAreaId(Long areaId) {
        log.debug("[视频地图图片] 查询区域地图: areaId={}", areaId);
        return mapImageDao.selectByAreaId(areaId);
    }

    @Override
    public VideoMapImageEntity getMapByFloor(Long areaId, Integer floorLevel) {
        log.debug("[视频地图图片] 查询楼层地图: areaId={}, floor={}", areaId, floorLevel);
        return mapImageDao.selectByFloor(areaId, floorLevel);
    }

    @Override
    public VideoMapImageEntity getDefaultMap(Long areaId) {
        log.debug("[视频地图图片] 查询默认地图: areaId={}", areaId);
        return mapImageDao.selectDefaultMap(areaId);
    }

    @Override
    public List<VideoMapImageEntity> getActiveMaps() {
        log.debug("[视频地图图片] 查询所有启用地图");
        return mapImageDao.selectActiveMaps();
    }

    @Override
    public void setDefaultMap(Long areaId, Long mapId) {
        log.info("[视频地图图片] 设置默认地图: areaId={}, mapId={}", areaId, mapId);

        // 取消该区域其他默认地图
        List<VideoMapImageEntity> maps = mapImageDao.selectByAreaId(areaId);
        maps.forEach(map -> {
            if (map.getIsDefault() == 1) {
                map.setIsDefault(0);
                this.updateById(map);
            }
        });

        // 设置新默认地图
        VideoMapImageEntity targetMap = this.getById(mapId);
        if (targetMap != null) {
            targetMap.setIsDefault(1);
            this.updateById(targetMap);
        }
    }

    @Override
    public Long uploadMap(VideoMapImageEntity mapImage) {
        log.info("[视频地图图片] 上传地图: imageName={}, areaId={}",
                mapImage.getImageName(), mapImage.getAreaId());
        this.save(mapImage);
        return mapImage.getId();
    }

    @Override
    public void updateMap(VideoMapImageEntity mapImage) {
        log.info("[视频地图图片] 更新地图: id={}", mapImage.getId());
        this.updateById(mapImage);
    }

    @Override
    public void deleteMap(Long mapId) {
        log.info("[视频地图图片] 删除地图: id={}", mapId);
        this.removeById(mapId);
    }
}
