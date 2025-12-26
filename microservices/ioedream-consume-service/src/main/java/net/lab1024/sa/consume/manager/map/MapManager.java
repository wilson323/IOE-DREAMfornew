package net.lab1024.sa.consume.manager.map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.dao.AreaDao;
import net.lab1024.sa.consume.dao.DeviceDao;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * 电子地图管理器 - 设备地图数据编排
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@Component
public class MapManager {

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private AreaDao areaDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 获取所有设备位置信息
     */
    public List<Map<String, Object>> getDeviceLocations() {
        log.debug("[电子地图管理器] 查询所有设备位置");

        // 查询所有有坐标的设备
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(DeviceEntity::getLongitude)
                .isNotNull(DeviceEntity::getLatitude)
                .orderByDesc(DeviceEntity::getCreateTime);

        List<DeviceEntity> devices = deviceDao.selectList(queryWrapper);

        // 转换为地图所需格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (DeviceEntity device : devices) {
            Map<String, Object> deviceMap = convertToMapFormat(device);
            result.add(deviceMap);
        }

        log.info("[电子地图管理器] 查询设备位置成功: count={}", result.size());
        return result;
    }

    /**
     * 获取指定区域的设备列表
     */
    public List<Map<String, Object>> getAreaDevices(Long areaId) {
        log.debug("[电子地图管理器] 查询区域设备: areaId={}", areaId);

        // 查询区域下所有有坐标的设备
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getAreaId, areaId)
                .isNotNull(DeviceEntity::getLongitude)
                .isNotNull(DeviceEntity::getLatitude)
                .orderByDesc(DeviceEntity::getCreateTime);

        List<DeviceEntity> devices = deviceDao.selectList(queryWrapper);

        // 转换为地图所需格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (DeviceEntity device : devices) {
            Map<String, Object> deviceMap = convertToMapFormat(device);
            result.add(deviceMap);
        }

        log.info("[电子地图管理器] 查询区域设备成功: areaId={}, count={}", areaId, result.size());
        return result;
    }

    /**
     * 获取设备详情
     */
    public Map<String, Object> getDeviceDetail(String deviceId) {
        log.debug("[电子地图管理器] 查询设备详情: deviceId={}", deviceId);

        DeviceEntity device = deviceDao.selectById(deviceId);
        if (device == null) {
            log.warn("[电子地图管理器] 设备不存在: deviceId={}", deviceId);
            return new HashMap<>();
        }

        Map<String, Object> result = convertToMapFormat(device);

        // 添加额外的详情信息
        result.put("firmwareVersion", device.getFirmwareVersion());
        result.put("ipAddress", device.getIpAddress());
        result.put("macAddress", device.getMacAddress());
        result.put("manufacturer", device.getManufacturer());
        result.put("model", device.getModel());
        result.put("installDate", device.getInstallDate());
        result.put("warrantyDate", device.getWarrantyDate());

        // TODO: 调用设备服务获取实时统计信息
        // result.put("todayPassCount", ...);
        // result.put("todayAlarmCount", ...);
        // result.put("healthScore", ...);

        log.info("[电子地图管理器] 查询设备详情成功: deviceId={}", deviceId);
        return result;
    }

    /**
     * 获取区域列表
     */
    public List<Map<String, Object>> getAreaList() {
        log.debug("[电子地图管理器] 查询区域列表");

        LambdaQueryWrapper<AreaEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaEntity::getStatus, 1)
                .orderByAsc(AreaEntity::getSortOrder);

        List<AreaEntity> areas = areaDao.selectList(queryWrapper);

        // 转换为地图所需格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (AreaEntity area : areas) {
            Map<String, Object> areaMap = new HashMap<>();
            areaMap.put("areaId", area.getAreaId());
            areaMap.put("areaName", area.getAreaName());
            areaMap.put("areaCode", area.getAreaCode());
            areaMap.put("areaType", area.getAreaType());
            areaMap.put("parentId", area.getParentId());
            areaMap.put("level", area.getLevel());
            result.add(areaMap);
        }

        log.info("[电子地图管理器] 查询区域列表成功: count={}", result.size());
        return result;
    }

    /**
     * 获取设备实时状态
     */
    public Map<String, Object> getDeviceStatus(String deviceId) {
        log.debug("[电子地图管理器] 查询设备状态: deviceId={}", deviceId);

        DeviceEntity device = deviceDao.selectById(deviceId);
        if (device == null) {
            log.warn("[电子地图管理器] 设备不存在: deviceId={}", deviceId);
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("deviceId", device.getDeviceId());
        result.put("status", device.getStatus());
        result.put("lastOnlineTime", device.getLastOnlineTime());
        result.put("onlineDuration", device.getOnlineDuration());

        // TODO: 调用设备服务获取实时状态
        // result.put("cpuUsage", ...);
        // result.put("memoryUsage", ...);
        // result.put("temperature", ...);

        log.info("[电子地图管理器] 查询设备状态成功: deviceId={}, status={}", deviceId, device.getStatus());
        return result;
    }

    /**
     * 根据业务模块获取设备
     */
    public List<Map<String, Object>> getDevicesByModule(String businessModule) {
        log.debug("[电子地图管理器] 按模块查询设备: module={}", businessModule);

        // 业务模块到设备类型的映射
        Integer deviceType = mapModuleToDeviceType(businessModule);
        if (deviceType == null) {
            log.warn("[电子地图管理器] 不支持的业务模块: module={}", businessModule);
            return new ArrayList<>();
        }

        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getDeviceType, deviceType)
                .isNotNull(DeviceEntity::getLongitude)
                .isNotNull(DeviceEntity::getLatitude)
                .orderByDesc(DeviceEntity::getCreateTime);

        List<DeviceEntity> devices = deviceDao.selectList(queryWrapper);

        // 转换为地图所需格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (DeviceEntity device : devices) {
            Map<String, Object> deviceMap = convertToMapFormat(device);
            result.add(deviceMap);
        }

        log.info("[电子地图管理器] 按模块查询设备成功: module={}, count={}", businessModule, result.size());
        return result;
    }

    /**
     * 根据状态获取设备
     */
    public List<Map<String, Object>> getDevicesByStatus(Integer status) {
        log.debug("[电子地图管理器] 按状态查询设备: status={}", status);

        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getStatus, status)
                .isNotNull(DeviceEntity::getLongitude)
                .isNotNull(DeviceEntity::getLatitude)
                .orderByDesc(DeviceEntity::getCreateTime);

        List<DeviceEntity> devices = deviceDao.selectList(queryWrapper);

        // 转换为地图所需格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (DeviceEntity device : devices) {
            Map<String, Object> deviceMap = convertToMapFormat(device);
            result.add(deviceMap);
        }

        log.info("[电子地图管理器] 按状态查询设备成功: status={}, count={}", status, result.size());
        return result;
    }

    /**
     * 业务模块映射到设备类型
     */
    private Integer mapModuleToDeviceType(String businessModule) {
        Map<String, Integer> moduleTypeMap = new HashMap<>();
        moduleTypeMap.put("access", 1);      // 门禁
        moduleTypeMap.put("attendance", 2);  // 考勤
        moduleTypeMap.put("consume", 3);     // 消费
        moduleTypeMap.put("video", 4);       // 视频
        moduleTypeMap.put("visitor", 5);     // 访客
        return moduleTypeMap.get(businessModule);
    }

    /**
     * 将DeviceEntity转换为地图所需格式
     */
    private Map<String, Object> convertToMapFormat(DeviceEntity device) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", device.getDeviceId());
        map.put("deviceCode", device.getDeviceCode());
        map.put("deviceName", device.getDeviceName());
        map.put("deviceType", device.getDeviceType());
        map.put("status", device.getStatus());
        map.put("longitude", device.getLongitude());
        map.put("latitude", device.getLatitude());
        map.put("areaId", device.getAreaId());
        map.put("deviceAddress", device.getDeviceAddress());

        // 查询区域名称
        if (device.getAreaId() != null) {
            AreaEntity area = areaDao.selectById(device.getAreaId());
            if (area != null) {
                map.put("areaName", area.getAreaName());
            }
        }

        return map;
    }
}
