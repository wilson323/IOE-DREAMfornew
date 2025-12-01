package net.lab1024.sa.device.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.domain.entity.DeviceEntity;
import net.lab1024.sa.device.repository.DeviceRepository;
import net.lab1024.sa.device.service.DeviceService;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 设备管理服务实现类
 * 基于现有SmartDeviceServiceImpl重构，提供统一设备管理功能
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原SmartDeviceServiceImpl重构)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceRepository, DeviceEntity> implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult<DeviceEntity> queryDevicePage(PageParam pageParam, String deviceType,
            String deviceStatus, String deviceName, Long areaId) {
        LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();

        if (deviceType != null && !deviceType.isEmpty()) {
            wrapper.eq(DeviceEntity::getDeviceType, deviceType);
        }
        if (deviceStatus != null && !deviceStatus.isEmpty()) {
            wrapper.eq(DeviceEntity::getDeviceStatus, deviceStatus);
        }
        if (deviceName != null && !deviceName.isEmpty()) {
            wrapper.like(DeviceEntity::getDeviceName, deviceName);
        }
        if (areaId != null) {
            wrapper.eq(DeviceEntity::getAreaId, areaId);
        }
        wrapper.orderByDesc(DeviceEntity::getCreateTime);

        Page<DeviceEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        Page<DeviceEntity> pageData = this.page(page, wrapper);

        PageResult<DeviceEntity> result = new PageResult<>();
        result.setList(pageData.getRecords());
        result.setTotal(pageData.getTotal());
        result.setPageNum(pageData.getCurrent());
        result.setPageSize(pageData.getSize());

        return result;
    }

    @Override
    @Cacheable(value = "device", key = "#deviceId", unless = "#result == null")
    public ResponseDTO<DeviceEntity> getDeviceById(Long deviceId) {
        DeviceEntity device = this.getById(deviceId);
        return device == null ? ResponseDTO.userErrorParam("设备不存在") : ResponseDTO.ok(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> addDevice(DeviceEntity device) {
        // 检查设备编码是否重复
        DeviceEntity existingDevice = deviceRepository.selectByDeviceCode(device.getDeviceCode());
        if (existingDevice != null) {
            return ResponseDTO.userErrorParam("设备编码已存在");
        }

        // 设置默认值（基于原SmartDeviceEntity逻辑）
        if (device.getDeviceStatus() == null) {
            device.setDeviceStatus("OFFLINE");
        }
        if (device.getEnabledFlag() == null) {
            device.setEnabledFlag(1);
        }
        if (device.getHeartbeatInterval() == null) {
            device.setHeartbeatInterval(60); // 默认60秒心跳
        }
        if (device.getWorkMode() == null) {
            device.setWorkMode(1); // 默认正常模式
        }

        boolean result = this.save(device);
        if (result) {
            log.info("设备新增成功: deviceId={}, deviceCode={}", device.getDeviceId(), device.getDeviceCode());
            return ResponseDTO.ok("设备新增成功");
        } else {
            return ResponseDTO.error("设备新增失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "device", key = "#device.deviceId")
    public ResponseDTO<String> updateDevice(DeviceEntity device) {
        DeviceEntity existingDevice = this.getById(device.getDeviceId());
        if (existingDevice == null) {
            return ResponseDTO.userErrorParam("设备不存在");
        }

        // 防止修改敏感字段
        device.setCreateTime(existingDevice.getCreateTime());
        device.setCreateUserId(existingDevice.getCreateUserId());

        boolean result = this.updateById(device);
        if (result) {
            log.info("设备更新成功: deviceId={}, deviceCode={}", device.getDeviceId(), device.getDeviceCode());
            return ResponseDTO.ok("设备更新成功");
        } else {
            return ResponseDTO.error("设备更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "device", key = "#deviceId")
    public ResponseDTO<String> deleteDevice(Long deviceId) {
        DeviceEntity device = this.getById(deviceId);
        if (device == null) {
            return ResponseDTO.userErrorParam("设备不存在");
        }

        // 检查设备是否可以删除
        if ("ONLINE".equals(device.getDeviceStatus())) {
            return ResponseDTO.error("在线设备不能删除，请先下线设备");
        }

        // 软删除
        device.setDeletedFlag(1);
        boolean result = this.updateById(device);
        if (result) {
            log.info("设备删除成功: deviceId={}, deviceCode={}", deviceId, device.getDeviceCode());
            return ResponseDTO.ok("设备删除成功");
        } else {
            return ResponseDTO.error("设备删除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchDeleteDevice(List<Long> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return ResponseDTO.userErrorParam("设备ID列表不能为空");
        }

        int successCount = 0;
        for (Long deviceId : deviceIds) {
            ResponseDTO<String> result = deleteDevice(deviceId);
            if (result.getOk()) {
                successCount++;
            }
        }

        log.info("批量删除设备完成: 成功{}, 总数{}", successCount, deviceIds.size());
        return ResponseDTO.ok(String.format("批量删除完成，成功%d个", successCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "device", key = "#deviceId")
    public ResponseDTO<String> updateDeviceStatus(Long deviceId, String deviceStatus) {
        DeviceEntity device = this.getById(deviceId);
        if (device == null) {
            return ResponseDTO.userErrorParam("设备不存在");
        }

        // 验证设备状态
        if (!isValidDeviceStatus(deviceStatus)) {
            return ResponseDTO.userErrorParam("无效的设备状态");
        }

        int result = deviceRepository.updateDeviceStatus(deviceId, deviceStatus);
        if (result > 0) {
            log.info("设备状态更新成功: deviceId={}, oldStatus={}, newStatus={}",
                    deviceId, device.getDeviceStatus(), deviceStatus);
            return ResponseDTO.ok("设备状态更新成功");
        } else {
            return ResponseDTO.error("设备状态更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchUpdateDeviceStatus(List<Long> deviceIds, String deviceStatus) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return ResponseDTO.userErrorParam("设备ID列表不能为空");
        }

        if (!isValidDeviceStatus(deviceStatus)) {
            return ResponseDTO.userErrorParam("无效的设备状态");
        }

        int result = deviceRepository.batchUpdateDeviceStatus(deviceIds, deviceStatus);
        log.info("批量更新设备状态: 影响行数={}, 状态={}", result, deviceStatus);
        return ResponseDTO.ok(String.format("批量更新完成，影响%d个设备", result));
    }

    @Override
    @Cacheable(value = "device:code", key = "#deviceCode", unless = "#result == null")
    public ResponseDTO<DeviceEntity> getDeviceByCode(String deviceCode) {
        DeviceEntity device = deviceRepository.selectByDeviceCode(deviceCode);
        return device == null ? ResponseDTO.userErrorParam("设备不存在") : ResponseDTO.ok(device);
    }

    @Override
    @Cacheable(value = "device:type", key = "#deviceType", unless = "#result == null")
    public ResponseDTO<List<DeviceEntity>> getDevicesByType(String deviceType) {
        List<DeviceEntity> devices = deviceRepository.selectByDeviceType(deviceType);
        return ResponseDTO.ok(devices);
    }

    @Override
    @Cacheable(value = "device:status", key = "#deviceStatus", unless = "#result == null")
    public ResponseDTO<List<DeviceEntity>> getDevicesByStatus(String deviceStatus) {
        List<DeviceEntity> devices = deviceRepository.selectByDeviceStatus(deviceStatus);
        return ResponseDTO.ok(devices);
    }

    @Override
    @Cacheable(value = "device:area", key = "#areaId", unless = "#result == null")
    public ResponseDTO<List<DeviceEntity>> getDevicesByAreaId(Long areaId) {
        List<DeviceEntity> devices = deviceRepository.selectByAreaId(areaId);
        return ResponseDTO.ok(devices);
    }

    @Override
    @Cacheable(value = "device:online", unless = "#result == null")
    public ResponseDTO<List<DeviceEntity>> getOnlineDevices() {
        List<DeviceEntity> devices = deviceRepository.selectOnlineDevices();
        return ResponseDTO.ok(devices);
    }

    @Override
    @Cacheable(value = "device:offline", unless = "#result == null")
    public ResponseDTO<List<DeviceEntity>> getOfflineDevices() {
        List<DeviceEntity> devices = deviceRepository.selectOfflineDevices();
        return ResponseDTO.ok(devices);
    }

    @Override
    @Cacheable(value = "device:maintenance", unless = "#result == null")
    public ResponseDTO<List<DeviceEntity>> getMaintenanceRequiredDevices() {
        List<DeviceEntity> devices = deviceRepository.selectMaintenanceRequiredDevices();
        return ResponseDTO.ok(devices);
    }

    @Override
    @Cacheable(value = "device:heartbeat-timeout", unless = "#result == null")
    public ResponseDTO<List<DeviceEntity>> getHeartbeatTimeoutDevices() {
        List<DeviceEntity> devices = deviceRepository.selectHeartbeatTimeoutDevices();
        return ResponseDTO.ok(devices);
    }

    @Override
    @Cacheable(value = "device:statistics:status", unless = "#result == null")
    public ResponseDTO<Map<String, Object>> getDeviceStatusStatistics() {
        Map<String, Object> statistics = deviceRepository.getDeviceHealthStatistics();
        return ResponseDTO.ok(statistics);
    }

    @Override
    @Cacheable(value = "device:statistics:type", unless = "#result == null")
    public ResponseDTO<Map<String, Object>> getDeviceTypeStatistics() {
        List<Map<String, Object>> typeStats = deviceRepository.countByDeviceType();
        return ResponseDTO.ok(Map.of("typeStatistics", typeStats));
    }

    @Override
    @Cacheable(value = "device:statistics:health", unless = "#result == null")
    public ResponseDTO<Map<String, Object>> getDeviceHealthStatistics() {
        Map<String, Object> healthStats = deviceRepository.getDeviceHealthStatistics();
        return ResponseDTO.ok(healthStats);
    }

    @Override
    @Cacheable(value = "device:statistics:area", unless = "#result == null")
    public ResponseDTO<Map<String, Object>> getAreaDeviceStatistics() {
        List<Map<String, Object>> areaStats = deviceRepository.countByAreaId();
        return ResponseDTO.ok(Map.of("areaStatistics", areaStats));
    }

    @Override
    @Cacheable(value = "device:statistics:manufacturer", unless = "#result == null")
    public ResponseDTO<Map<String, Object>> getManufacturerStatistics() {
        List<Map<String, Object>> manufacturerStats = deviceRepository.countByManufacturer();
        return ResponseDTO.ok(Map.of("manufacturerStatistics", manufacturerStats));
    }

    @Override
    public ResponseDTO<List<DeviceEntity>> searchDevices(String keyword, String deviceType,
            String deviceStatus, Long areaId) {
        List<DeviceEntity> devices = deviceRepository.searchDevices(keyword, deviceType, deviceStatus, areaId);
        return ResponseDTO.ok(devices);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deviceOnline(Long deviceId) {
        return updateDeviceStatus(deviceId, "ONLINE");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deviceOffline(Long deviceId) {
        return updateDeviceStatus(deviceId, "OFFLINE");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateHeartbeat(Long deviceId) {
        int result = deviceRepository.updateLastHeartbeatTime(deviceId, LocalDateTime.now());
        if (result > 0) {
            log.debug("设备心跳更新成功: deviceId={}", deviceId);
            return ResponseDTO.ok("心跳更新成功");
        } else {
            return ResponseDTO.error("心跳更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> reportDeviceFault(Long deviceId, String faultMessage) {
        return updateDeviceStatus(deviceId, "FAULT");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> reportDeviceRecovery(Long deviceId, String recoveryMessage) {
        return updateDeviceStatus(deviceId, "ONLINE");
    }

    // 兼容性方法实现

    @Override
    @Cacheable(value = "device:smart", key = "#smartDeviceId", unless = "#result == null")
    public ResponseDTO<DeviceEntity> getSmartDeviceById(Long smartDeviceId) {
        return getDeviceById(smartDeviceId);
    }

    @Override
    @Cacheable(value = "device:access", key = "#accessDeviceId", unless = "#result == null")
    public ResponseDTO<DeviceEntity> getAccessDeviceById(Long accessDeviceId) {
        return getDeviceById(accessDeviceId);
    }

    @Override
    @Cacheable(value = "device:consume", key = "#consumeDeviceId", unless = "#result == null")
    public ResponseDTO<DeviceEntity> getConsumeDeviceById(Long consumeDeviceId) {
        return getDeviceById(consumeDeviceId);
    }

    @Override
    @Cacheable(value = "device:attendance", key = "#attendanceDeviceId", unless = "#result == null")
    public ResponseDTO<DeviceEntity> getAttendanceDeviceById(Long attendanceDeviceId) {
        return getDeviceById(attendanceDeviceId);
    }

    @Override
    @Cacheable(value = "device:video", key = "#videoDeviceId", unless = "#result == null")
    public ResponseDTO<DeviceEntity> getVideoDeviceById(Long videoDeviceId) {
        return getDeviceById(videoDeviceId);
    }

    // 私有方法

    /**
     * 验证设备状态是否有效（基于原设备状态验证逻辑）
     */
    private boolean isValidDeviceStatus(String deviceStatus) {
        return "ONLINE".equals(deviceStatus) || "OFFLINE".equals(deviceStatus) ||
                "FAULT".equals(deviceStatus) || "MAINTAIN".equals(deviceStatus) ||
                "UNKNOWN".equals(deviceStatus);
    }

    /**
     * 清除设备相关缓存
     * 
     * @param deviceId 设备ID
     */
    @SuppressWarnings("unused")
    private void clearDeviceCache(Long deviceId) {
        // 清除基础缓存
        redisTemplate.delete("device:" + deviceId);

        // 可以根据需要清除其他相关缓存
        log.debug("清除设备缓存: deviceId={}", deviceId);
    }
}
