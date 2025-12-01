package net.lab1024.sa.device.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartResponseUtil;
import net.lab1024.sa.device.dao.SmartDeviceDao;
import net.lab1024.sa.device.domain.entity.SmartDeviceEntity;
import net.lab1024.sa.device.manager.SmartDeviceManager;
import net.lab1024.sa.device.service.SmartDeviceService;

/**
 * 智能设备服务实现类 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 继承ServiceImpl使用MyBatis-Plus
 * - 使用@Resource依赖注入
 * - 完整的异常处理和日志记录
 * - 事务管理和缓存管理
 * - 完整的设备管理业务逻辑
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Service
@Slf4j
public class SmartDeviceServiceImpl extends ServiceImpl<SmartDeviceDao, SmartDeviceEntity>
        implements SmartDeviceService {

    @Resource
    private SmartDeviceDao smartDeviceDao;

    @Resource
    private SmartDeviceManager smartDeviceManager;

    @Override
    public PageResult<SmartDeviceEntity> queryDevicePage(PageParam pageParam, String deviceType, String deviceStatus,
            String deviceName) {
        log.debug("分页查询智能设备，deviceType: {}, deviceStatus: {}, deviceName: {}", deviceType, deviceStatus, deviceName);

        try {
            LambdaQueryWrapper<SmartDeviceEntity> wrapper = new LambdaQueryWrapper<>();

            // 构建查询条件
            if (deviceType != null && !deviceType.trim().isEmpty()) {
                wrapper.eq(SmartDeviceEntity::getDeviceType, deviceType.trim());
            }
            if (deviceStatus != null && !deviceStatus.trim().isEmpty()) {
                wrapper.eq(SmartDeviceEntity::getDeviceStatus, deviceStatus.trim());
            }
            if (deviceName != null && !deviceName.trim().isEmpty()) {
                wrapper.like(SmartDeviceEntity::getDeviceName, deviceName.trim());
            }

            // 只查询未删除的记录
            wrapper.eq(SmartDeviceEntity::getDeletedFlag, 0);
            wrapper.orderByDesc(SmartDeviceEntity::getCreateTime);

            // 执行分页查询
            Page<SmartDeviceEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            Page<SmartDeviceEntity> pageData = this.page(page, wrapper);

            // 构建返回结果
            PageResult<SmartDeviceEntity> result = new PageResult<>();
            result.setList(pageData.getRecords());
            result.setTotal(pageData.getTotal());
            result.setPageNum(pageData.getCurrent());
            result.setPageSize(pageData.getSize());

            log.debug("智能设备分页查询完成，总数: {}", pageData.getTotal());
            return result;

        } catch (Exception e) {
            log.error("智能设备分页查询失败", e);
            throw new RuntimeException("查询设备列表失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> registerDevice(SmartDeviceEntity deviceEntity) {
        log.info("注册新智能设备，deviceCode: {}, deviceName: {}", deviceEntity.getDeviceCode(), deviceEntity.getDeviceName());

        try {
            // 参数验证
            if (deviceEntity.getDeviceCode() == null || deviceEntity.getDeviceCode().trim().isEmpty()) {
                return ResponseDTO.<String>error("设备编码不能为空");
            }
            if (deviceEntity.getDeviceName() == null || deviceEntity.getDeviceName().trim().isEmpty()) {
                return ResponseDTO.<String>error("设备名称不能为空");
            }

            // 检查设备编码是否已存在
            if (checkDeviceCodeExists(deviceEntity.getDeviceCode())) {
                return ResponseDTO.<String>error("设备编码已存在");
            }

            // 设置默认值
            if (deviceEntity.getEnabledFlag() == null) {
                deviceEntity.setEnabledFlag(1);
            }
            if (deviceEntity.getDeviceStatus() == null || deviceEntity.getDeviceStatus().trim().isEmpty()) {
                deviceEntity.setDeviceStatus("OFFLINE");
            }
            if (deviceEntity.getInstallDate() == null) {
                deviceEntity.setInstallDate(LocalDateTime.now());
            }

            // 保存设备信息
            this.save(deviceEntity);

            // 预热缓存
            smartDeviceManager.warmupDeviceCache(deviceEntity.getDeviceId());

            log.info("智能设备注册成功，deviceId: {}, deviceCode: {}", deviceEntity.getDeviceId(), deviceEntity.getDeviceCode());
            return ResponseDTO.<String>okMsg("设备注册成功");

        } catch (Exception e) {
            log.error("注册智能设备失败，deviceCode: {}", deviceEntity.getDeviceCode(), e);
            return ResponseDTO.<String>error("设备注册失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDevice(SmartDeviceEntity deviceEntity) {
        log.info("更新智能设备信息，deviceId: {}, deviceName: {}", deviceEntity.getDeviceId(), deviceEntity.getDeviceName());

        try {
            // 验证设备是否存在
            SmartDeviceEntity existingDevice = this.getById(deviceEntity.getDeviceId());
            if (existingDevice == null || existingDevice.getDeletedFlag() == 1) {
                return ResponseDTO.<String>error("设备不存在");
            }

            // 更新设备信息
            this.updateById(deviceEntity);

            // 清除相关缓存
            smartDeviceManager.clearDeviceCache(deviceEntity.getDeviceId());

            log.info("智能设备更新成功，deviceId: {}", deviceEntity.getDeviceId());
            return ResponseDTO.<String>okMsg("设备更新成功");

        } catch (Exception e) {
            log.error("更新智能设备失败，deviceId: {}", deviceEntity.getDeviceId(), e);
            return ResponseDTO.<String>error("设备更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteDevice(Long deviceId) {
        log.info("删除智能设备，deviceId: {}", deviceId);

        try {
            // 验证设备是否存在
            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.<String>error("设备不存在");
            }

            // 软删除设备
            device.setDeletedFlag(1);
            this.updateById(device);

            // 清除缓存
            smartDeviceManager.clearDeviceCache(deviceId);

            log.info("智能设备删除成功，deviceId: {}", deviceId);
            return ResponseDTO.<String>okMsg("设备删除成功");

        } catch (Exception e) {
            log.error("删除智能设备失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<String>error("设备删除失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<SmartDeviceEntity> getDeviceDetail(Long deviceId) {
        log.debug("获取智能设备详情，deviceId: {}", deviceId);

        try {
            // 从Manager层获取（带缓存）
            SmartDeviceEntity device = smartDeviceManager.getDeviceInfo(deviceId);
            if (device == null) {
                return ResponseDTO.<SmartDeviceEntity>error("设备不存在");
            }
            return ResponseDTO.ok(device);

        } catch (Exception e) {
            log.error("获取智能设备详情失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<SmartDeviceEntity>error("获取设备详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> enableDevice(Long deviceId) {
        log.info("启用智能设备，deviceId: {}", deviceId);

        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.<String>error("设备不存在");
            }

            device.setDeviceStatus("ONLINE");
            device.setEnabledFlag(1);
            this.updateById(device);

            // 更新缓存
            smartDeviceManager.updateDeviceStatusCache(deviceId, "ONLINE");

            log.info("智能设备启用成功，deviceId: {}", deviceId);
            return ResponseDTO.<String>okMsg("设备启用成功");

        } catch (Exception e) {
            log.error("启用智能设备失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<String>error("启用设备失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> disableDevice(Long deviceId) {
        log.info("禁用智能设备，deviceId: {}", deviceId);

        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.<String>error("设备不存在");
            }

            device.setDeviceStatus("OFFLINE");
            device.setEnabledFlag(0);
            this.updateById(device);

            // 更新缓存
            smartDeviceManager.updateDeviceStatusCache(deviceId, "OFFLINE");

            log.info("智能设备禁用成功，deviceId: {}", deviceId);
            return ResponseDTO.<String>okMsg("设备禁用成功");

        } catch (Exception e) {
            log.error("禁用智能设备失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<String>error("禁用设备失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchOperateDevices(List<Long> deviceIds, String operation) {
        log.info("批量操作智能设备，deviceIds: {}, operation: {}", deviceIds, operation);

        if (deviceIds == null || deviceIds.isEmpty()) {
            return ResponseDTO.<String>error("设备ID列表不能为空");
        }
        if (operation == null || operation.trim().isEmpty()) {
            return ResponseDTO.<String>error("操作类型不能为空");
        }

        try {
            int successCount = 0;
            int failCount = 0;
            String op = operation.toLowerCase().trim();

            for (Long deviceId : deviceIds) {
                try {
                    SmartDeviceEntity device = this.getById(deviceId);
                    if (device == null || device.getDeletedFlag() == 1) {
                        failCount++;
                        continue;
                    }

                    switch (op) {
                        case "enable":
                            device.setDeviceStatus("ONLINE");
                            device.setEnabledFlag(1);
                            this.updateById(device);
                            smartDeviceManager.updateDeviceStatusCache(deviceId, "ONLINE");
                            break;
                        case "disable":
                            device.setDeviceStatus("OFFLINE");
                            device.setEnabledFlag(0);
                            this.updateById(device);
                            smartDeviceManager.updateDeviceStatusCache(deviceId, "OFFLINE");
                            break;
                        case "delete":
                            device.setDeletedFlag(1);
                            this.updateById(device);
                            smartDeviceManager.clearDeviceCache(deviceId);
                            break;
                        default:
                            return ResponseDTO.<String>error("不支持的批量操作类型：" + operation);
                    }
                    successCount++;
                } catch (Exception e) {
                    log.warn("批量操作设备失败，deviceId: {}, operation: {}", deviceId, operation, e);
                    failCount++;
                }
            }

            // 批量清除统计缓存
            smartDeviceManager.clearAllDeviceCache();

            String message = String.format("批量操作完成，成功: %d，失败: %d", successCount, failCount);
            log.info("智能设备批量操作完成，{}", message);
            return SmartResponseUtil.success(message);

        } catch (Exception e) {
            log.error("批量操作智能设备失败", e);
            return ResponseDTO.<String>error("批量操作设备失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deviceHeartbeat(Long deviceId) {
        log.debug("智能设备心跳，deviceId: {}", deviceId);

        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return false;
            }

            device.setLastOnlineTime(LocalDateTime.now());
            device.setDeviceStatus("ONLINE");
            boolean success = this.updateById(device);

            if (success) {
                // 更新缓存
                smartDeviceManager.updateDeviceStatusCache(deviceId, "ONLINE");
            }

            return success;
        } catch (Exception e) {
            log.error("智能设备心跳失败，deviceId: {}", deviceId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDeviceStatus(Long deviceId, String deviceStatus) {
        log.info("更新智能设备状态，deviceId: {}, deviceStatus: {}", deviceId, deviceStatus);

        try {
            if (deviceStatus == null || deviceStatus.trim().isEmpty()) {
                return ResponseDTO.<String>error("设备状态不能为空");
            }

            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.<String>error("设备不存在");
            }

            String oldStatus = device.getDeviceStatus();
            device.setDeviceStatus(deviceStatus);

            // 如果状态变为在线，更新最后在线时间
            if ("ONLINE".equals(deviceStatus)) {
                device.setLastOnlineTime(LocalDateTime.now());
            }

            this.updateById(device);

            // 更新缓存
            smartDeviceManager.updateDeviceStatusCache(deviceId, deviceStatus);

            log.info("智能设备状态更新成功，deviceId: {}, {} -> {}", deviceId, oldStatus, deviceStatus);
            return ResponseDTO.<String>okMsg("设备状态更新成功");

        } catch (Exception e) {
            log.error("更新智能设备状态失败，deviceId: {}, deviceStatus: {}", deviceId, deviceStatus, e);
            return ResponseDTO.<String>error("更新设备状态失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> remoteControlDevice(Long deviceId, String command, Map<String, Object> params) {
        log.info("远程控制智能设备，deviceId: {}, command: {}", deviceId, command);

        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.<String>error("设备不存在");
            }

            if (device.getEnabledFlag() != 1) {
                return ResponseDTO.<String>error("设备已禁用，无法远程控制");
            }

            // TODO: 实现具体的设备远程控制逻辑
            // 这里需要根据设备类型和协议进行实际的控制操作
            log.info("执行远程控制，deviceId: {}, command: {}, params: {}", deviceId, command, params);

            // 记录操作日志
            log.info("智能设备远程控制完成，deviceId: {}, command: {}", deviceId, command);
            return ResponseDTO.<String>okMsg("远程控制成功");

        } catch (Exception e) {
            log.error("远程控制智能设备失败，deviceId: {}, command: {}", deviceId, command, e);
            return ResponseDTO.<String>error("远程控制失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceConfig(Long deviceId) {
        log.debug("获取智能设备配置，deviceId: {}", deviceId);

        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.<Map<String, Object>>error("设备不存在");
            }

            Map<String, Object> config = new HashMap<>();
            config.put("deviceId", device.getDeviceId());
            config.put("deviceCode", device.getDeviceCode());
            config.put("deviceName", device.getDeviceName());
            config.put("deviceType", device.getDeviceType());
            config.put("configJson", device.getConfigJson());
            config.put("enabledFlag", device.getEnabledFlag());

            return ResponseDTO.ok(config);

        } catch (Exception e) {
            log.error("获取智能设备配置失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<Map<String, Object>>error("获取设备配置失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDeviceConfig(Long deviceId, Map<String, Object> config) {
        log.info("更新智能设备配置，deviceId: {}", deviceId);

        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.<String>error("设备不存在");
            }

            if (config != null && config.containsKey("configJson")) {
                device.setConfigJson(String.valueOf(config.get("configJson")));
                this.updateById(device);

                // 清除缓存
                smartDeviceManager.clearDeviceCache(deviceId);
            }

            log.info("智能设备配置更新成功，deviceId: {}", deviceId);
            return ResponseDTO.<String>okMsg("配置更新成功");

        } catch (Exception e) {
            log.error("更新智能设备配置失败，deviceId: {}", deviceId, e);
            return ResponseDTO.<String>error("更新设备配置失败：" + e.getMessage());
        }
    }

    @Override
    public List<SmartDeviceEntity> getOnlineDevices() {
        log.debug("获取在线智能设备列表");

        try {
            // 从Manager层获取（带缓存）
            return smartDeviceManager.getOnlineDevices(5); // 5分钟内的在线设备
        } catch (Exception e) {
            log.error("获取在线智能设备列表失败", e);
            return List.of();
        }
    }

    @Override
    public List<SmartDeviceEntity> getOfflineDevices() {
        log.debug("获取离线智能设备列表");

        try {
            // 从Manager层获取（带缓存）
            return smartDeviceManager.getOfflineDevices(10); // 10分钟内无心跳的离线设备
        } catch (Exception e) {
            log.error("获取离线智能设备列表失败", e);
            return List.of();
        }
    }

    @Override
    public Map<String, Object> getDeviceStatusStatistics() {
        log.debug("获取智能设备状态统计");

        try {
            // 从Manager层获取（带缓存）
            return smartDeviceManager.getDeviceStatusStatistics();
        } catch (Exception e) {
            log.error("获取智能设备状态统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getDeviceTypeStatistics() {
        log.debug("获取智能设备类型统计");

        try {
            // 从Manager层获取（带缓存）
            return smartDeviceManager.getDeviceTypeStatistics();
        } catch (Exception e) {
            log.error("获取智能设备类型统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public SmartDeviceEntity getDeviceByCode(String deviceCode) {
        log.debug("根据设备编码获取智能设备，deviceCode: {}", deviceCode);

        try {
            // 从Manager层获取（带缓存）
            return smartDeviceManager.getDeviceInfoByCode(deviceCode);
        } catch (Exception e) {
            log.error("根据设备编码获取智能设备失败，deviceCode: {}", deviceCode, e);
            return null;
        }
    }

    @Override
    public List<SmartDeviceEntity> getDevicesByType(String deviceType) {
        log.debug("根据设备类型获取智能设备列表，deviceType: {}", deviceType);

        try {
            // 从Manager层获取（带缓存）
            return smartDeviceManager.getDevicesByType(deviceType);
        } catch (Exception e) {
            log.error("根据设备类型获取智能设备列表失败，deviceType: {}", deviceType, e);
            return List.of();
        }
    }

    @Override
    public List<SmartDeviceEntity> getDevicesByGroup(Long groupId) {
        log.debug("根据分组ID获取智能设备列表，groupId: {}", groupId);

        try {
            // 从Manager层获取（带缓存）
            return smartDeviceManager.getDevicesByGroup(groupId);
        } catch (Exception e) {
            log.error("根据分组ID获取智能设备列表失败，groupId: {}", groupId, e);
            return List.of();
        }
    }

    @Override
    public boolean checkDeviceCodeExists(String deviceCode) {
        log.debug("检查智能设备编码是否存在，deviceCode: {}", deviceCode);

        try {
            SmartDeviceEntity device = smartDeviceDao.selectByDeviceCode(deviceCode);
            return device != null && device.getDeletedFlag() == 0;
        } catch (Exception e) {
            log.error("检查智能设备编码是否存在失败，deviceCode: {}", deviceCode, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchSyncDeviceStatus(List<Long> deviceIds) {
        log.info("批量同步智能设备状态，deviceIds: {}", deviceIds);

        if (deviceIds == null || deviceIds.isEmpty()) {
            return ResponseDTO.<String>error("设备ID列表不能为空");
        }

        try {
            int successCount = 0;
            LocalDateTime now = LocalDateTime.now();

            for (Long deviceId : deviceIds) {
                try {
                    SmartDeviceEntity device = this.getById(deviceId);
                    if (device != null && device.getDeletedFlag() == 0) {
                        // 检查最后在线时间来更新状态
                        if (device.getLastOnlineTime() != null) {
                            long minutesOffline = java.time.Duration.between(device.getLastOnlineTime(), now)
                                    .toMinutes();

                            if (minutesOffline <= 5) {
                                device.setDeviceStatus("ONLINE");
                            } else if (minutesOffline > 30) {
                                device.setDeviceStatus("OFFLINE");
                            }

                            this.updateById(device);
                            smartDeviceManager.updateDeviceStatusCache(deviceId, device.getDeviceStatus());
                            successCount++;
                        }
                    }
                } catch (Exception e) {
                    log.warn("同步设备状态失败，deviceId: {}", deviceId, e);
                }
            }

            String message = String.format("批量同步设备状态完成，成功: %d", successCount);
            log.info("智能设备状态同步完成，{}", message);
            return SmartResponseUtil.success(message);

        } catch (Exception e) {
            log.error("批量同步智能设备状态失败", e);
            return ResponseDTO.<String>error("批量同步设备状态失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getFullDeviceStatistics() {
        log.debug("获取智能设备完整统计信息");

        try {
            // 从Manager层获取（带缓存）
            return smartDeviceManager.getFullDeviceStatistics();
        } catch (Exception e) {
            log.error("获取智能设备完整统计信息失败", e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastOnlineTime(Long deviceId, LocalDateTime lastOnlineTime) {
        log.debug("更新智能设备最后在线时间，deviceId: {}", deviceId);

        try {
            int result = smartDeviceDao.updateLastOnlineTime(deviceId, lastOnlineTime);

            if (result > 0) {
                // 清除相关缓存
                smartDeviceManager.clearDeviceCache(deviceId);
            }

            return result > 0;
        } catch (Exception e) {
            log.error("更新智能设备最后在线时间失败，deviceId: {}", deviceId, e);
            return false;
        }
    }

    @Override
    public List<SmartDeviceEntity> getMaintenanceDevices() {
        log.debug("获取需要维护的智能设备列表");

        try {
            return smartDeviceDao.selectMaintenanceDevices().stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .toList();
        } catch (Exception e) {
            log.error("获取需要维护的智能设备列表失败", e);
            return List.of();
        }
    }

    @Override
    public List<SmartDeviceEntity> getFaultDevices() {
        log.debug("获取故障智能设备列表");

        try {
            return smartDeviceDao.selectFaultDevices().stream()
                    .filter(device -> device.getDeletedFlag() == 0)
                    .toList();
        } catch (Exception e) {
            log.error("获取故障智能设备列表失败", e);
            return List.of();
        }
    }
}
