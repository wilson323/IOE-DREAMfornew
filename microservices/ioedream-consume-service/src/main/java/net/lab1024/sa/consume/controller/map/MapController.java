package net.lab1024.sa.consume.controller.map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.service.map.MapService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 电子地图控制器 - 园区设备地图展示
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/map")
@Tag(name = "电子地图")
public class MapController {

    @Resource
    private MapService mapService;

    @GetMapping("/device/locations")
    @Operation(summary = "获取所有设备位置信息")
    public ResponseDTO<List<Map<String, Object>>> getDeviceLocations() {
        log.info("[电子地图] 查询所有设备位置");
        List<Map<String, Object>> locations = mapService.getDeviceLocations();
        return ResponseDTO.ok(locations);
    }

    @GetMapping("/area/{areaId}/devices")
    @Operation(summary = "获取指定区域的设备列表")
    public ResponseDTO<List<Map<String, Object>>> getAreaDevices(@PathVariable Long areaId) {
        log.info("[电子地图] 查询区域设备: areaId={}", areaId);
        List<Map<String, Object>> devices = mapService.getAreaDevices(areaId);
        return ResponseDTO.ok(devices);
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "获取设备详情")
    public ResponseDTO<Map<String, Object>> getDeviceDetail(@PathVariable String deviceId) {
        log.info("[电子地图] 查询设备详情: deviceId={}", deviceId);
        Map<String, Object> device = mapService.getDeviceDetail(deviceId);
        return ResponseDTO.ok(device);
    }

    @GetMapping("/areas")
    @Operation(summary = "获取区域列表")
    public ResponseDTO<List<Map<String, Object>>> getAreaList() {
        log.info("[电子地图] 查询区域列表");
        List<Map<String, Object>> areas = mapService.getAreaList();
        return ResponseDTO.ok(areas);
    }

    @GetMapping("/device/{deviceId}/status")
    @Operation(summary = "获取设备实时状态")
    public ResponseDTO<Map<String, Object>> getDeviceStatus(@PathVariable String deviceId) {
        log.info("[电子地图] 查询设备状态: deviceId={}", deviceId);
        Map<String, Object> status = mapService.getDeviceStatus(deviceId);
        return ResponseDTO.ok(status);
    }

    @GetMapping("/devices/module/{businessModule}")
    @Operation(summary = "根据业务模块获取设备")
    public ResponseDTO<List<Map<String, Object>>> getDevicesByModule(@PathVariable String businessModule) {
        log.info("[电子地图] 按模块查询设备: module={}", businessModule);
        List<Map<String, Object>> devices = mapService.getDevicesByModule(businessModule);
        return ResponseDTO.ok(devices);
    }

    @GetMapping("/devices/status/{status}")
    @Operation(summary = "根据状态获取设备")
    public ResponseDTO<List<Map<String, Object>>> getDevicesByStatus(@PathVariable Integer status) {
        log.info("[电子地图] 按状态查询设备: status={}", status);
        List<Map<String, Object>> devices = mapService.getDevicesByStatus(status);
        return ResponseDTO.ok(devices);
    }
}
