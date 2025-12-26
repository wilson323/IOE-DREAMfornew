package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.video.VideoDeviceMapEntity;
import net.lab1024.sa.common.entity.video.VideoMapHotspotEntity;
import net.lab1024.sa.common.entity.video.VideoMapImageEntity;
import net.lab1024.sa.video.service.VideoDeviceMapService;
import net.lab1024.sa.video.service.VideoMapHotspotService;
import net.lab1024.sa.video.service.VideoMapImageService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 视频地图Controller
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@RestController
@RequestMapping("/api/video/map")
@Tag(name = "视频地图管理", description = "视频设备地图显示与交互")
@Slf4j
public class VideoMapController {

    @Resource
    private VideoMapImageService mapImageService;

    @Resource
    private VideoDeviceMapService deviceMapService;

    @Resource
    private VideoMapHotspotService hotspotService;

    // ==================== 地图图片管理 ====================

    /**
     * 查询区域地图
     */
    @GetMapping("/images/area/{areaId}")
    @Operation(summary = "查询区域地图")
    public ResponseDTO<List<VideoMapImageEntity>> getMapsByArea(
            @Parameter(description = "区域ID") @PathVariable Long areaId) {
        log.info("[视频地图API] 查询区域地图: areaId={}", areaId);
        List<VideoMapImageEntity> maps = mapImageService.getMapsByAreaId(areaId);
        return ResponseDTO.ok(maps);
    }

    /**
     * 查询楼层地图
     */
    @GetMapping("/images/floor")
    @Operation(summary = "查询楼层地图")
    public ResponseDTO<VideoMapImageEntity> getMapByFloor(
            @Parameter(description = "区域ID") @RequestParam Long areaId,
            @Parameter(description = "楼层") @RequestParam Integer floorLevel) {
        log.info("[视频地图API] 查询楼层地图: areaId={}, floor={}", areaId, floorLevel);
        VideoMapImageEntity map = mapImageService.getMapByFloor(areaId, floorLevel);
        return ResponseDTO.ok(map);
    }

    /**
     * 查询默认地图
     */
    @GetMapping("/images/default/{areaId}")
    @Operation(summary = "查询默认地图")
    public ResponseDTO<VideoMapImageEntity> getDefaultMap(
            @Parameter(description = "区域ID") @PathVariable Long areaId) {
        log.info("[视频地图API] 查询默认地图: areaId={}", areaId);
        VideoMapImageEntity map = mapImageService.getDefaultMap(areaId);
        return ResponseDTO.ok(map);
    }

    /**
     * 设置默认地图
     */
    @PutMapping("/images/{mapId}/default")
    @Operation(summary = "设置默认地图")
    public ResponseDTO<Void> setDefaultMap(
            @Parameter(description = "地图ID") @PathVariable Long mapId,
            @Parameter(description = "区域ID") @RequestParam Long areaId) {
        log.info("[视频地图API] 设置默认地图: mapId={}, areaId={}", mapId, areaId);
        mapImageService.setDefaultMap(areaId, mapId);
        return ResponseDTO.ok();
    }

    /**
     * 上传地图
     */
    @PostMapping("/images")
    @Operation(summary = "上传地图")
    public ResponseDTO<Long> uploadMap(@RequestBody VideoMapImageEntity mapImage) {
        log.info("[视频地图API] 上传地图: imageName={}", mapImage.getImageName());
        Long mapId = mapImageService.uploadMap(mapImage);
        return ResponseDTO.ok(mapId);
    }

    /**
     * 更新地图
     */
    @PutMapping("/images/{mapId}")
    @Operation(summary = "更新地图")
    public ResponseDTO<Void> updateMap(
            @Parameter(description = "地图ID") @PathVariable Long mapId,
            @RequestBody VideoMapImageEntity mapImage) {
        log.info("[视频地图API] 更新地图: mapId={}", mapId);
        mapImage.setId(mapId);
        mapImageService.updateMap(mapImage);
        return ResponseDTO.ok();
    }

    /**
     * 删除地图
     */
    @DeleteMapping("/images/{mapId}")
    @Operation(summary = "删除地图")
    public ResponseDTO<Void> deleteMap(
            @Parameter(description = "地图ID") @PathVariable Long mapId) {
        log.info("[视频地图API] 删除地图: mapId={}", mapId);
        mapImageService.deleteMap(mapId);
        return ResponseDTO.ok();
    }

    // ==================== 设备地图管理 ====================

    /**
     * 查询地图的所有设备
     */
    @GetMapping("/devices")
    @Operation(summary = "查询地图设备")
    public ResponseDTO<List<VideoDeviceMapEntity>> getDevicesByMap(
            @Parameter(description = "地图ID") @RequestParam Long mapImageId) {
        log.info("[视频地图API] 查询地图设备: mapImageId={}", mapImageId);
        List<VideoDeviceMapEntity> devices = deviceMapService.getDevicesByMapId(mapImageId);
        return ResponseDTO.ok(devices);
    }

    /**
     * 查询区域设备
     */
    @GetMapping("/devices/area/{areaId}")
    @Operation(summary = "查询区域设备")
    public ResponseDTO<List<VideoDeviceMapEntity>> getDevicesByArea(
            @Parameter(description = "区域ID") @PathVariable Long areaId) {
        log.info("[视频地图API] 查询区域设备: areaId={}", areaId);
        List<VideoDeviceMapEntity> devices = deviceMapService.getDevicesByAreaId(areaId);
        return ResponseDTO.ok(devices);
    }

    /**
     * 查询楼层设备
     */
    @GetMapping("/devices/floor")
    @Operation(summary = "查询楼层设备")
    public ResponseDTO<List<VideoDeviceMapEntity>> getDevicesByFloor(
            @Parameter(description = "区域ID") @RequestParam Long areaId,
            @Parameter(description = "楼层") @RequestParam Integer floorLevel) {
        log.info("[视频地图API] 查询楼层设备: areaId={}, floor={}", areaId, floorLevel);
        List<VideoDeviceMapEntity> devices = deviceMapService.getDevicesByFloor(areaId, floorLevel);
        return ResponseDTO.ok(devices);
    }

    /**
     * 查询范围内设备
     */
    @GetMapping("/devices/within")
    @Operation(summary = "查询范围内设备")
    public ResponseDTO<List<VideoDeviceMapEntity>> getDevicesWithin(
            @Parameter(description = "地图ID") @RequestParam Long mapImageId,
            @Parameter(description = "最小X") @RequestParam BigDecimal minX,
            @Parameter(description = "最大X") @RequestParam BigDecimal maxX,
            @Parameter(description = "最小Y") @RequestParam BigDecimal minY,
            @Parameter(description = "最大Y") @RequestParam BigDecimal maxY) {
        log.info("[视频地图API] 查询范围内设备: mapImageId={}, bounds=[{}, {}, {}, {}]",
                mapImageId, minX, maxX, minY, maxY);
        List<VideoDeviceMapEntity> devices = deviceMapService.getDevicesWithinBounds(
                mapImageId, minX, maxX, minY, maxY);
        return ResponseDTO.ok(devices);
    }

    /**
     * 查询附近设备
     */
    @GetMapping("/devices/nearby")
    @Operation(summary = "查询附近设备")
    public ResponseDTO<List<VideoDeviceMapEntity>> getNearbyDevices(
            @Parameter(description = "地图ID") @RequestParam Long mapImageId,
            @Parameter(description = "X坐标") @RequestParam BigDecimal x,
            @Parameter(description = "Y坐标") @RequestParam BigDecimal y,
            @Parameter(description = "半径") @RequestParam BigDecimal radius) {
        log.info("[视频地图API] 查询附近设备: mapImageId={}, center=[{}, {}], radius={}",
                mapImageId, x, y, radius);
        List<VideoDeviceMapEntity> devices = deviceMapService.getNearbyDevices(mapImageId, x, y, radius);
        return ResponseDTO.ok(devices);
    }

    /**
     * 添加设备到地图
     */
    @PostMapping("/devices")
    @Operation(summary = "添加设备到地图")
    public ResponseDTO<Long> addDevice(@RequestBody VideoDeviceMapEntity deviceMap) {
        log.info("[视频地图API] 添加设备到地图: deviceId={}, mapImageId={}",
                deviceMap.getDeviceId(), deviceMap.getMapImageId());
        Long id = deviceMapService.addDeviceToMap(deviceMap);
        return ResponseDTO.ok(id);
    }

    /**
     * 批量添加设备
     */
    @PostMapping("/devices/batch")
    @Operation(summary = "批量添加设备")
    public ResponseDTO<Integer> batchAddDevices(@RequestBody List<VideoDeviceMapEntity> deviceMaps) {
        log.info("[视频地图API] 批量添加设备: count={}", deviceMaps.size());
        Integer count = deviceMapService.batchAddDevices(deviceMaps);
        return ResponseDTO.ok(count);
    }

    /**
     * 更新设备位置
     */
    @PutMapping("/devices/{id}/position")
    @Operation(summary = "更新设备位置")
    public ResponseDTO<Void> updateDevicePosition(
            @Parameter(description = "记录ID") @PathVariable Long id,
            @Parameter(description = "X坐标") @RequestParam BigDecimal x,
            @Parameter(description = "Y坐标") @RequestParam BigDecimal y,
            @Parameter(description = "Z坐标") @RequestParam(required = false) BigDecimal z) {
        log.info("[视频地图API] 更新设备位置: id=[{}, {}]", x, y);
        deviceMapService.updateDevicePosition(id, x, y, z);
        return ResponseDTO.ok();
    }

    /**
     * 更新设备显示状态
     */
    @PutMapping("/devices/{id}/status")
    @Operation(summary = "更新设备显示状态")
    public ResponseDTO<Void> updateDeviceStatus(
            @Parameter(description = "记录ID") @PathVariable Long id,
            @Parameter(description = "显示状态") @RequestParam Integer displayStatus) {
        log.info("[视频地图API] 更新设备状态: id={}, status={}", id, displayStatus);
        deviceMapService.updateDisplayStatus(id, displayStatus);
        return ResponseDTO.ok();
    }

    /**
     * 移除设备
     */
    @DeleteMapping("/devices/{id}")
    @Operation(summary = "移除设备")
    public ResponseDTO<Void> removeDevice(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        log.info("[视频地图API] 移除设备: id={}", id);
        deviceMapService.removeDeviceFromMap(id);
        return ResponseDTO.ok();
    }

    // ==================== 地图热点管理 ====================

    /**
     * 查询地图热点
     */
    @GetMapping("/hotspots")
    @Operation(summary = "查询地图热点")
    public ResponseDTO<List<VideoMapHotspotEntity>> getHotspots(
            @Parameter(description = "地图ID") @RequestParam Long mapImageId) {
        log.info("[视频地图API] 查询地图热点: mapImageId={}", mapImageId);
        List<VideoMapHotspotEntity> hotspots = hotspotService.getHotspotsByMapId(mapImageId);
        return ResponseDTO.ok(hotspots);
    }

    /**
     * 查询可见热点
     */
    @GetMapping("/hotspots/visible")
    @Operation(summary = "查询可见热点")
    public ResponseDTO<List<VideoMapHotspotEntity>> getVisibleHotspots(
            @Parameter(description = "地图ID") @RequestParam Long mapImageId) {
        log.info("[视频地图API] 查询可见热点: mapImageId={}", mapImageId);
        List<VideoMapHotspotEntity> hotspots = hotspotService.getVisibleHotspots(mapImageId);
        return ResponseDTO.ok(hotspots);
    }

    /**
     * 添加热点
     */
    @PostMapping("/hotspots")
    @Operation(summary = "添加热点")
    public ResponseDTO<Long> addHotspot(@RequestBody VideoMapHotspotEntity hotspot) {
        log.info("[视频地图API] 添加热点: hotspotName={}", hotspot.getHotspotName());
        Long id = hotspotService.addHotspot(hotspot);
        return ResponseDTO.ok(id);
    }

    /**
     * 批量添加热点
     */
    @PostMapping("/hotspots/batch")
    @Operation(summary = "批量添加热点")
    public ResponseDTO<Integer> batchAddHotspots(@RequestBody List<VideoMapHotspotEntity> hotspots) {
        log.info("[视频地图API] 批量添加热点: count={}", hotspots.size());
        Integer count = hotspotService.batchAddHotspots(hotspots);
        return ResponseDTO.ok(count);
    }

    /**
     * 更新热点位置
     */
    @PutMapping("/hotspots/{id}/position")
    @Operation(summary = "更新热点位置")
    public ResponseDTO<Void> updateHotspotPosition(
            @Parameter(description = "热点ID") @PathVariable Long id,
            @Parameter(description = "X坐标") @RequestParam BigDecimal x,
            @Parameter(description = "Y坐标") @RequestParam BigDecimal y) {
        log.info("[视频地图API] 更新热点位置: id=[{}, {}]", x, y);
        hotspotService.updateHotspotPosition(id, x, y);
        return ResponseDTO.ok();
    }

    /**
     * 更新热点状态
     */
    @PutMapping("/hotspots/{id}/status")
    @Operation(summary = "更新热点状态")
    public ResponseDTO<Void> updateHotspotStatus(
            @Parameter(description = "热点ID") @PathVariable Long id,
            @Parameter(description = "显示状态") @RequestParam Integer displayStatus) {
        log.info("[视频地图API] 更新热点状态: id={}, status={}", id, displayStatus);
        hotspotService.updateHotspotStatus(id, displayStatus);
        return ResponseDTO.ok();
    }

    /**
     * 删除热点
     */
    @DeleteMapping("/hotspots/{id}")
    @Operation(summary = "删除热点")
    public ResponseDTO<Void> deleteHotspot(
            @Parameter(description = "热点ID") @PathVariable Long id) {
        log.info("[视频地图API] 删除热点: id={}", id);
        hotspotService.deleteHotspot(id);
        return ResponseDTO.ok();
    }
}
