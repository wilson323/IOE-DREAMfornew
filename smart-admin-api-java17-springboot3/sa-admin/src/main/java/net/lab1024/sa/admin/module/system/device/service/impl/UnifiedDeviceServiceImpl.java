package net.lab1024.sa.admin.module.system.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.system.device.dao.UnifiedDeviceDao;
import net.lab1024.sa.admin.module.system.device.domain.entity.UnifiedDeviceEntity;
import net.lab1024.sa.admin.module.system.device.manager.UnifiedDeviceManager;
import net.lab1024.sa.admin.module.system.device.service.UnifiedDeviceService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.code.SystemErrorCode;
import net.lab1024.sa.base.common.exception.SmartException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 统一设备管理服务实现类
 * <p>
 * 严格遵循repowiki四层架构规范：
 * - Controller → Service → Manager → DAO
 * - Service层负责业务逻辑和事务管理
 * - 复杂业务逻辑委托给Manager层处理
 * - 统一异常处理和日志记录
 * - 事件驱动架构支持
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Service
public class UnifiedDeviceServiceImpl extends ServiceImpl<UnifiedDeviceDao, UnifiedDeviceEntity> implements UnifiedDeviceService {

    @Resource
    private UnifiedDeviceManager unifiedDeviceManager;

    @Override
    public PageResult<UnifiedDeviceEntity> queryDevicePage(PageParam pageParam, String deviceType,
                                                           String deviceStatus, String deviceName, Long areaId) {
        try {
            log.info("查询设备列表，设备类型：{}，设备状态：{}，设备名称：{}，区域ID：{}",
                    deviceType, deviceStatus, deviceName, areaId);

            // 构建查询条件
            LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UnifiedDeviceEntity::getDeletedFlag, 0);

            if (deviceType != null && !deviceType.isEmpty()) {
                queryWrapper.eq(UnifiedDeviceEntity::getDeviceType, deviceType);
            }
            if (deviceStatus != null && !deviceStatus.isEmpty()) {
                queryWrapper.eq(UnifiedDeviceEntity::getDeviceStatus, deviceStatus);
            }
            if (deviceName != null && !deviceName.isEmpty()) {
                queryWrapper.like(UnifiedDeviceEntity::getDeviceName, deviceName);
            }
            if (areaId != null) {
                queryWrapper.eq(UnifiedDeviceEntity::getAreaId, areaId);
            }

            // 按创建时间倒序排列
            queryWrapper.orderByDesc(UnifiedDeviceEntity::getCreateTime);

            // 分页查询
            IPage<UnifiedDeviceEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            IPage<UnifiedDeviceEntity> result = this.page(page, queryWrapper);

            // 设置虚拟字段
            List<UnifiedDeviceEntity> records = result.getRecords();
            for (UnifiedDeviceEntity device : records) {
                setVirtualFields(device);
            }

            log.info("查询设备列表成功，共查询到{}条记录", result.getTotal());
            return PageResult.of(result);

        } catch (Exception e) {
            log.error("查询设备列表失败", e);
            throw new SmartException(SystemErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> registerDevice(UnifiedDeviceEntity deviceEntity) {
        try {
            log.info("注册新设备，设备编号：{}，设备名称：{}", deviceEntity.getDeviceCode(), deviceEntity.getDeviceName());

            // 参数验证
            if (deviceEntity.getDeviceCode() == null || deviceEntity.getDeviceCode().isEmpty()) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备编号不能为空");
            }
            if (deviceEntity.getDeviceName() == null || deviceEntity.getDeviceName().isEmpty()) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备名称不能为空");
            }
            if (deviceEntity.getDeviceType() == null || deviceEntity.getDeviceType().isEmpty()) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备类型不能为空");
            }

            // 检查设备编号是否已存在
            LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UnifiedDeviceEntity::getDeviceCode, deviceEntity.getDeviceCode());
            queryWrapper.eq(UnifiedDeviceEntity::getDeletedFlag, 0);
            long count = this.count(queryWrapper);
            if (count > 0) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备编号已存在");
            }

            // 设置默认值
            deviceEntity.setOnlineStatus(0); // 默认离线
            deviceEntity.setEnabled(1); // 默认启用
            deviceEntity.setDeviceStatus("NORMAL"); // 默认正常状态
            deviceEntity.setHeartbeatInterval(60); // 默认心跳间隔60秒
            deviceEntity.setInstallTime(LocalDateTime.now());

            // 保存设备
            boolean success = this.save(deviceEntity);
            if (!success) {
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备注册失败");
            }

            log.info("设备注册成功，设备ID：{}", deviceEntity.getDeviceId());
            return ResponseDTO.ok("设备注册成功");

        } catch (Exception e) {
            log.error("注册设备失败", e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备注册失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDevice(UnifiedDeviceEntity deviceEntity) {
        try {
            log.info("更新设备信息，设备ID：{}", deviceEntity.getDeviceId());

            // 检查设备是否存在
            UnifiedDeviceEntity existingDevice = this.getById(deviceEntity.getDeviceId());
            if (existingDevice == null || existingDevice.getDeletedFlag() == 1) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            // 检查设备编号是否被其他设备使用
            if (deviceEntity.getDeviceCode() != null && !deviceEntity.getDeviceCode().equals(existingDevice.getDeviceCode())) {
                LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UnifiedDeviceEntity::getDeviceCode, deviceEntity.getDeviceCode());
                queryWrapper.ne(UnifiedDeviceEntity::getDeviceId, deviceEntity.getDeviceId());
                queryWrapper.eq(UnifiedDeviceEntity::getDeletedFlag, 0);
                long count = this.count(queryWrapper);
                if (count > 0) {
                    return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备编号已存在");
                }
            }

            // 更新设备
            boolean success = this.updateById(deviceEntity);
            if (!success) {
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备更新失败");
            }

            // 发布设备配置变更事件
            publishDeviceConfigChangeEvent(deviceEntity.getDeviceId(), getDeviceConfigMap(deviceEntity));

            log.info("设备更新成功，设备ID：{}", deviceEntity.getDeviceId());
            return ResponseDTO.ok("设备更新成功");

        } catch (Exception e) {
            log.error("更新设备失败，设备ID：{}", deviceEntity.getDeviceId(), e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteDevice(Long deviceId) {
        try {
            log.info("删除设备，设备ID：{}", deviceId);

            // 检查设备是否存在
            UnifiedDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            // 软删除设备
            device.setDeletedFlag(1);
            boolean success = this.updateById(device);
            if (!success) {
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备删除失败");
            }

            log.info("设备删除成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("设备删除成功");

        } catch (Exception e) {
            log.error("删除设备失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备删除失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<UnifiedDeviceEntity> getDeviceDetail(Long deviceId) {
        try {
            log.info("获取设备详情，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            // 设置虚拟字段
            setVirtualFields(device);

            log.info("获取设备详情成功，设备ID：{}", deviceId);
            return ResponseDTO.ok(device);

        } catch (Exception e) {
            log.error("获取设备详情失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "获取设备详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> enableDevice(Long deviceId) {
        try {
            log.info("启用设备，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            device.setEnabled(1);
            boolean success = this.updateById(device);
            if (!success) {
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备启用失败");
            }

            log.info("设备启用成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("设备启用成功");

        } catch (Exception e) {
            log.error("启用设备失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备启用失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> disableDevice(Long deviceId) {
        try {
            log.info("禁用设备，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            device.setEnabled(0);
            boolean success = this.updateById(device);
            if (!success) {
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备禁用失败");
            }

            log.info("设备禁用成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("设备禁用成功");

        } catch (Exception e) {
            log.error("禁用设备失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备禁用失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchOperateDevices(List<Long> deviceIds, String operation) {
        try {
            log.info("批量操作设备，设备数量：{}，操作类型：{}", deviceIds.size(), operation);

            List<UnifiedDeviceEntity> devices = this.listByIds(deviceIds);
            if (devices.isEmpty()) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "没有找到有效的设备");
            }

            int successCount = 0;
            for (UnifiedDeviceEntity device : devices) {
                try {
                    switch (operation.toUpperCase()) {
                        case "ENABLE":
                            device.setEnabled(1);
                            break;
                        case "DISABLE":
                            device.setEnabled(0);
                            break;
                        case "DELETE":
                            device.setDeletedFlag(1);
                            break;
                        default:
                            log.warn("未知的操作类型：{}", operation);
                            continue;
                    }

                    if (this.updateById(device)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("批量操作设备失败，设备ID：{}，操作：{}", device.getDeviceId(), operation, e);
                }
            }

            log.info("批量操作设备完成，成功：{}，总数：{}", successCount, deviceIds.size());
            return ResponseDTO.ok(String.format("批量操作完成，成功：%d，总数：%d", successCount, deviceIds.size()));

        } catch (Exception e) {
            log.error("批量操作设备失败", e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "批量操作设备失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deviceHeartbeat(Long deviceId) {
        try {
            log.debug("设备心跳，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return false;
            }

            String oldStatus = device.getDeviceStatus();

            // 更新心跳时间和在线状态
            device.setLastHeartbeatTime(LocalDateTime.now());
            device.setOnlineStatus(1);

            // 如果设备之前是离线状态，现在更新为在线状态，发布状态变更事件
            if ("OFFLINE".equals(oldStatus)) {
                device.setDeviceStatus("NORMAL");
                publishDeviceStatusChangeEvent(deviceId, oldStatus, "NORMAL");
            }

            return this.updateById(device);

        } catch (Exception e) {
            log.error("设备心跳处理失败，设备ID：{}", deviceId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDeviceStatus(Long deviceId, String deviceStatus) {
        try {
            log.info("更新设备状态，设备ID：{}，新状态：{}", deviceId, deviceStatus);

            UnifiedDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            String oldStatus = device.getDeviceStatus();
            device.setDeviceStatus(deviceStatus);

            // 如果状态变为故障或离线，设置在线状态为离线
            if ("FAULT".equals(deviceStatus) || "OFFLINE".equals(deviceStatus)) {
                device.setOnlineStatus(0);
            } else if ("NORMAL".equals(deviceStatus)) {
                device.setOnlineStatus(1);
            }

            boolean success = this.updateById(device);
            if (!success) {
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备状态更新失败");
            }

            // 发布状态变更事件
            publishDeviceStatusChangeEvent(deviceId, oldStatus, deviceStatus);

            log.info("设备状态更新成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("设备状态更新成功");

        } catch (Exception e) {
            log.error("更新设备状态失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备状态更新失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDeviceStatusStatistics(String deviceType) {
        try {
            log.info("获取设备状态统计，设备类型：{}", deviceType);

            return unifiedDeviceManager.getDeviceStatusStatistics(deviceType);

        } catch (Exception e) {
            log.error("获取设备状态统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getDeviceTypeStatistics() {
        try {
            log.info("获取设备类型统计");

            return unifiedDeviceManager.getDeviceTypeStatistics();

        } catch (Exception e) {
            log.error("获取设备类型统计失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public ResponseDTO<String> remoteControlDevice(Long deviceId, String command, Map<String, Object> params) {
        try {
            log.info("远程控制设备，设备ID：{}，命令：{}", deviceId, command);

            // 委托给Manager层处理复杂的设备控制逻辑
            return unifiedDeviceManager.remoteControlDevice(deviceId, command, params);

        } catch (Exception e) {
            log.error("远程控制设备失败，设备ID：{}，命令：{}", deviceId, command, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备控制失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceConfig(Long deviceId) {
        try {
            log.info("获取设备配置，设备ID：{}", deviceId);

            return unifiedDeviceManager.getDeviceConfig(deviceId);

        } catch (Exception e) {
            log.error("获取设备配置失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "获取设备配置失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDeviceConfig(Long deviceId, Map<String, Object> config) {
        try {
            log.info("更新设备配置，设备ID：{}", deviceId);

            UnifiedDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            // 将配置转换为JSON字符串并保存
            device.setDeviceConfig(unifiedDeviceManager.convertConfigToJson(config));

            boolean success = this.updateById(device);
            if (!success) {
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备配置更新失败");
            }

            // 发布配置变更事件
            publishDeviceConfigChangeEvent(deviceId, config);

            log.info("设备配置更新成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("设备配置更新成功");

        } catch (Exception e) {
            log.error("更新设备配置失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备配置更新失败：" + e.getMessage());
        }
    }

    @Override
    public List<UnifiedDeviceEntity> getOnlineDevices(String deviceType) {
        try {
            log.debug("获取在线设备列表，设备类型：{}", deviceType);

            LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UnifiedDeviceEntity::getDeletedFlag, 0);
            queryWrapper.eq(UnifiedDeviceEntity::getOnlineStatus, 1);
            queryWrapper.eq(UnifiedDeviceEntity::getEnabled, 1);

            if (deviceType != null && !deviceType.isEmpty()) {
                queryWrapper.eq(UnifiedDeviceEntity::getDeviceType, deviceType);
            }

            queryWrapper.orderByDesc(UnifiedDeviceEntity::getLastHeartbeatTime);

            List<UnifiedDeviceEntity> devices = this.list(queryWrapper);

            // 设置虚拟字段
            for (UnifiedDeviceEntity device : devices) {
                setVirtualFields(device);
            }

            return devices;

        } catch (Exception e) {
            log.error("获取在线设备列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<UnifiedDeviceEntity> getOfflineDevices(String deviceType) {
        try {
            log.debug("获取离线设备列表，设备类型：{}", deviceType);

            LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UnifiedDeviceEntity::getDeletedFlag, 0);
            queryWrapper.eq(UnifiedDeviceEntity::getOnlineStatus, 0);
            queryWrapper.eq(UnifiedDeviceEntity::getEnabled, 1);

            if (deviceType != null && !deviceType.isEmpty()) {
                queryWrapper.eq(UnifiedDeviceEntity::getDeviceType, deviceType);
            }

            queryWrapper.orderByDesc(UnifiedDeviceEntity::getLastHeartbeatTime);

            List<UnifiedDeviceEntity> devices = this.list(queryWrapper);

            // 设置虚拟字段
            for (UnifiedDeviceEntity device : devices) {
                setVirtualFields(device);
            }

            return devices;

        } catch (Exception e) {
            log.error("获取离线设备列表失败", e);
            return new ArrayList<>();
        }
    }

    // ========== 门禁设备专用方法实现 ==========

    @Override
    public ResponseDTO<String> remoteOpenDoor(Long deviceId) {
        try {
            log.info("远程开门，设备ID：{}", deviceId);

            return unifiedDeviceManager.remoteOpenDoor(deviceId);

        } catch (Exception e) {
            log.error("远程开门失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "远程开门失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> restartDevice(Long deviceId) {
        try {
            log.info("重启设备，设备ID：{}", deviceId);

            return unifiedDeviceManager.restartDevice(deviceId);

        } catch (Exception e) {
            log.error("重启设备失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "重启设备失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> syncDeviceTime(Long deviceId) {
        try {
            log.info("同步设备时间，设备ID：{}", deviceId);

            return unifiedDeviceManager.syncDeviceTime(deviceId);

        } catch (Exception e) {
            log.error("同步设备时间失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "同步设备时间失败：" + e.getMessage());
        }
    }

    // ========== 视频设备专用方法实现 ==========

    @Override
    public ResponseDTO<String> ptzControl(Long deviceId, String command, Integer speed) {
        try {
            log.info("云台控制，设备ID：{}，命令：{}，速度：{}", deviceId, command, speed);

            return unifiedDeviceManager.ptzControl(deviceId, command, speed);

        } catch (Exception e) {
            log.error("云台控制失败，设备ID：{}，命令：{}", deviceId, command, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "云台控制失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Long> startRecording(Long deviceId) {
        try {
            log.info("启动设备录像，设备ID：{}", deviceId);

            return unifiedDeviceManager.startRecording(deviceId);

        } catch (Exception e) {
            log.error("启动设备录像失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "启动录像失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> stopRecording(Long recordId) {
        try {
            log.info("停止设备录像，录像ID：{}", recordId);

            return unifiedDeviceManager.stopRecording(recordId);

        } catch (Exception e) {
            log.error("停止设备录像失败，录像ID：{}", recordId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "停止录像失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> getLiveStream(Long deviceId) {
        try {
            log.info("获取实时视频流，设备ID：{}", deviceId);

            return unifiedDeviceManager.getLiveStream(deviceId);

        } catch (Exception e) {
            log.error("获取实时视频流失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "获取视频流失败：" + e.getMessage());
        }
    }

    // ========== 高级功能方法实现 ==========

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceHealthStatus(Long deviceId) {
        try {
            log.info("获取设备健康状态，设备ID：{}", deviceId);

            return unifiedDeviceManager.getDeviceHealthStatus(deviceId);

        } catch (Exception e) {
            log.error("获取设备健康状态失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "获取设备健康状态失败：" + e.getMessage());
        }
    }

    @Override
    public List<UnifiedDeviceEntity> getDevicesNeedingMaintenance(String deviceType) {
        try {
            log.debug("获取需要维护的设备列表，设备类型：{}", deviceType);

            return unifiedDeviceManager.getDevicesNeedingMaintenance(deviceType);

        } catch (Exception e) {
            log.error("获取需要维护的设备列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<UnifiedDeviceEntity> getHeartbeatTimeoutDevices(String deviceType) {
        try {
            log.debug("获取心跳超时的设备列表，设备类型：{}", deviceType);

            return unifiedDeviceManager.getHeartbeatTimeoutDevices(deviceType);

        } catch (Exception e) {
            log.error("获取心跳超时的设备列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDeviceWorkMode(Long deviceId, Integer workMode) {
        try {
            log.info("更新设备工作模式，设备ID：{}，工作模式：{}", deviceId, workMode);

            UnifiedDeviceEntity device = this.getById(deviceId);
            if (device == null || device.getDeletedFlag() == 1) {
                return ResponseDTO.error(SystemErrorCode.PARAM_ERROR, "设备不存在");
            }

            device.setWorkMode(workMode);
            boolean success = this.updateById(device);
            if (!success) {
                return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备工作模式更新失败");
            }

            log.info("设备工作模式更新成功，设备ID：{}", deviceId);
            return ResponseDTO.ok("设备工作模式更新成功");

        } catch (Exception e) {
            log.error("更新设备工作模式失败，设备ID：{}", deviceId, e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "设备工作模式更新失败：" + e.getMessage());
        }
    }

    @Override
    public List<UnifiedDeviceEntity> getDevicesByAreaId(Long areaId, String deviceType) {
        try {
            log.debug("根据区域获取设备列表，区域ID：{}，设备类型：{}", areaId, deviceType);

            LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UnifiedDeviceEntity::getDeletedFlag, 0);
            queryWrapper.eq(UnifiedDeviceEntity::getAreaId, areaId);
            queryWrapper.eq(UnifiedDeviceEntity::getEnabled, 1);

            if (deviceType != null && !deviceType.isEmpty()) {
                queryWrapper.eq(UnifiedDeviceEntity::getDeviceType, deviceType);
            }

            queryWrapper.orderByAsc(UnifiedDeviceEntity::getDeviceName);

            return this.list(queryWrapper);

        } catch (Exception e) {
            log.error("根据区域获取设备列表失败，区域ID：{}", areaId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void publishDeviceStatusChangeEvent(Long deviceId, String oldStatus, String newStatus) {
        try {
            log.info("发布设备状态变更事件，设备ID：{}，旧状态：{}，新状态：{}", deviceId, oldStatus, newStatus);

            // 委托给Manager层处理事件发布
            unifiedDeviceManager.publishDeviceStatusChangeEvent(deviceId, oldStatus, newStatus);

        } catch (Exception e) {
            log.error("发布设备状态变更事件失败，设备ID：{}", deviceId, e);
        }
    }

    @Override
    public void publishDeviceConfigChangeEvent(Long deviceId, Map<String, Object> config) {
        try {
            log.info("发布设备配置变更事件，设备ID：{}", deviceId);

            // 委托给Manager层处理事件发布
            unifiedDeviceManager.publishDeviceConfigChangeEvent(deviceId, config);

        } catch (Exception e) {
            log.error("发布设备配置变更事件失败，设备ID：{}", deviceId, e);
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 设置虚拟字段
     */
    private void setVirtualFields(UnifiedDeviceEntity device) {
        // 在线状态描述
        device.setOnlineStatusDesc(device.getOnlineStatus() == 1 ? "在线" : "离线");

        // 启用状态描述
        device.setEnabledDesc(device.getEnabled() == 1 ? "启用" : "禁用");

        // 设备类型描述
        for (DeviceType type : DeviceType.values()) {
            if (type.getCode().equals(device.getDeviceType())) {
                device.setDeviceTypeDesc(type.getDescription());
                break;
            }
        }

        // 设备状态描述
        switch (device.getDeviceStatus()) {
            case "NORMAL":
                device.setDeviceStatusDesc("正常");
                break;
            case "FAULT":
                device.setDeviceStatusDesc("故障");
                break;
            case "MAINTENANCE":
                device.setDeviceStatusDesc("维护中");
                break;
            case "OFFLINE":
                device.setDeviceStatusDesc("离线");
                break;
            default:
                device.setDeviceStatusDesc("未知");
                break;
        }

        // 工作模式描述（主要用于门禁设备）
        if (device.getWorkMode() != null) {
            switch (device.getWorkMode()) {
                case 0:
                    device.setWorkModeDesc("普通模式");
                    break;
                case 1:
                    device.setWorkModeDesc("刷卡模式");
                    break;
                case 2:
                    device.setWorkModeDesc("人脸模式");
                    break;
                case 3:
                    device.setWorkModeDesc("指纹模式");
                    break;
                case 4:
                    device.setWorkModeDesc("混合模式");
                    break;
                default:
                    device.setWorkModeDesc("未知模式");
                    break;
            }
        }

        // 是否需要维护
        device.setNeedMaintenance(unifiedDeviceManager.isDeviceNeedingMaintenance(device));

        // 是否心跳超时
        device.setHeartbeatTimeout(unifiedDeviceManager.isDeviceHeartbeatTimeout(device));
    }

    /**
     * 获取设备配置Map
     */
    private Map<String, Object> getDeviceConfigMap(UnifiedDeviceEntity device) {
        if (device.getDeviceConfig() == null || device.getDeviceConfig().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return unifiedDeviceManager.parseConfigFromJson(device.getDeviceConfig());
        } catch (Exception e) {
            log.warn("解析设备配置失败，设备ID：{}", device.getDeviceId(), e);
            return new HashMap<>();
        }
    }
}