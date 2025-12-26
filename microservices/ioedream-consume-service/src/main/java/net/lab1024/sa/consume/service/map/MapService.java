package net.lab1024.sa.consume.service.map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.manager.map.MapManager;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 电子地图服务 - 设备地图展示服务
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Service
public class MapService {

    @Resource
    private MapManager mapManager;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 获取所有设备位置信息
     */
    public List<Map<String, Object>> getDeviceLocations() {
        log.info("[电子地图] 查询所有设备位置");
        return mapManager.getDeviceLocations();
    }

    /**
     * 获取指定区域的设备列表
     */
    public List<Map<String, Object>> getAreaDevices(Long areaId) {
        log.info("[电子地图] 查询区域设备: areaId={}", areaId);
        return mapManager.getAreaDevices(areaId);
    }

    /**
     * 获取设备详情
     */
    public Map<String, Object> getDeviceDetail(String deviceId) {
        log.info("[电子地图] 查询设备详情: deviceId={}", deviceId);
        return mapManager.getDeviceDetail(deviceId);
    }

    /**
     * 获取区域列表
     */
    public List<Map<String, Object>> getAreaList() {
        log.info("[电子地图] 查询区域列表");
        return mapManager.getAreaList();
    }

    /**
     * 获取设备实时状态
     */
    public Map<String, Object> getDeviceStatus(String deviceId) {
        log.info("[电子地图] 查询设备状态: deviceId={}", deviceId);
        return mapManager.getDeviceStatus(deviceId);
    }

    /**
     * 根据业务模块获取设备
     */
    public List<Map<String, Object>> getDevicesByModule(String businessModule) {
        log.info("[电子地图] 按模块查询设备: module={}", businessModule);
        return mapManager.getDevicesByModule(businessModule);
    }

    /**
     * 根据状态获取设备
     */
    public List<Map<String, Object>> getDevicesByStatus(Integer status) {
        log.info("[电子地图] 按状态查询设备: status={}", status);
        return mapManager.getDevicesByStatus(status);
    }
}
