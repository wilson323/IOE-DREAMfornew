package net.lab1024.sa.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.common.entity.video.VideoMapImageEntity;

import java.util.List;

/**
 * 视频地图图片Service接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
public interface VideoMapImageService extends IService<VideoMapImageEntity> {

    /**
     * 查询区域地图
     */
    List<VideoMapImageEntity> getMapsByAreaId(Long areaId);

    /**
     * 查询楼层地图
     */
    VideoMapImageEntity getMapByFloor(Long areaId, Integer floorLevel);

    /**
     * 查询默认地图
     */
    VideoMapImageEntity getDefaultMap(Long areaId);

    /**
     * 查询所有启用地图
     */
    List<VideoMapImageEntity> getActiveMaps();

    /**
     * 设置默认地图
     */
    void setDefaultMap(Long areaId, Long mapId);

    /**
     * 上传地图图片
     */
    Long uploadMap(VideoMapImageEntity mapImage);

    /**
     * 更新地图图片
     */
    void updateMap(VideoMapImageEntity mapImage);

    /**
     * 删除地图
     */
    void deleteMap(Long mapId);
}
