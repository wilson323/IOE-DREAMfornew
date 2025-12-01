package net.lab1024.sa.admin.module.smart.access.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.dao.AccessDeviceDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.smart.access.service.AccessDeviceService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartPageUtil;

/**
 * 门禁设备管理服务实现
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - 复杂业务逻辑事务管理
 * - 完整的异常处理和日志记录
 * - 设备状态管理和性能优化
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Service
public class AccessDeviceServiceImpl implements AccessDeviceService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolAdapterFactory protocolAdapterFactory;

    @Override
    public PageResult<AccessDeviceEntity> getDevicePage(PageParam pageParam, Long deviceId, Long areaId,
            String deviceName, Integer accessDeviceType,
            Integer onlineStatus, Integer enabled) {
        log.info("分页查询设备，deviceId: {}, areaId: {}, deviceName: {}, accessDeviceType: {}, onlineStatus: {}, enabled: {}",
                deviceId, areaId, deviceName, accessDeviceType, onlineStatus, enabled);

        try {
            Page<AccessDeviceEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            LambdaQueryWrapper<AccessDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();

            // 构建查询条件
            if (deviceId != null) {
                queryWrapper.eq(AccessDeviceEntity::getDeviceId, deviceId);
            }
            if (areaId != null) {
                queryWrapper.eq(AccessDeviceEntity::getAreaId, areaId);
            }
            if (StringUtils.hasText(deviceName)) {
                queryWrapper.like(AccessDeviceEntity::getDeviceModel, deviceName)
                        .or()
                        .like(AccessDeviceEntity::getSerialNumber, deviceName);
            }
            if (accessDeviceType != null) {
                queryWrapper.eq(AccessDeviceEntity::getAccessDeviceType, accessDeviceType);
            }
            if (onlineStatus != null) {
                queryWrapper.eq(AccessDeviceEntity::getOnlineStatus, onlineStatus);
            }
            if (enabled != null) {
                queryWrapper.eq(AccessDeviceEntity::getEnabled, enabled);
            }

            queryWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0)
                    .orderByDesc(AccessDeviceEntity::getCreateTime);

            Page<AccessDeviceEntity> result = accessDeviceDao.selectPage(page, queryWrapper);

            log.info("设备分页查询完成，总数: {}", result.getTotal());
            return SmartPageUtil.convert2PageResult(result, result.getRecords());

        } catch (Exception e) {
            log.error("分页查询设备失败，deviceId: {}, areaId: {}, deviceName: {}", deviceId, areaId, deviceName, e);
            throw new SmartException("分页查询设备失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "access_device", key = "#accessDeviceId", condition = "#accessDeviceId != null")
    public AccessDeviceEntity getDeviceById(Long accessDeviceId) {
        log.debug("获取设备详情，accessDeviceId: {}", accessDeviceId);

        AccessDeviceEntity device = accessDeviceDao.selectById(accessDeviceId);
        if (device == null || device.getDeletedFlag() == 1) {
            throw new BusinessException("设备不存在或已删除");
        }

        log.debug("设备详情查询完成并缓存，accessDeviceId: {}", accessDeviceId);
        return device;
    }

    @Override
    @Cacheable(value = "access_device:area", key = "#areaId", condition = "#areaId != null")
    public List<AccessDeviceEntity> getDevicesByAreaId(Long areaId) {
        log.debug("获取区域设备列表，areaId: {}", areaId);

        if (areaId == null) {
            return new ArrayList<>();
        }

        try {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectByAreaId(areaId);
            log.debug("区域设备列表查询完成并缓存，areaId: {}, 数量: {}", areaId, devices.size());
            return devices;

        } catch (Exception e) {
            log.error("获取区域设备列表失败，areaId: {}", areaId, e);
            throw new SmartException("获取区域设备列表失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "access_device:online", key = "'all'")
    public List<AccessDeviceEntity> getOnlineDevices() {
        log.debug("获取在线设备列表");

        try {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectOnlineDevices();
            log.debug("在线设备列表查询完成并缓存，数量: {}", devices.size());
            return devices;

        } catch (Exception e) {
            log.error("获取在线设备列表失败", e);
            throw new SmartException("获取在线设备列表失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "access_device:offline", key = "'all'")
    public List<AccessDeviceEntity> getOfflineDevices() {
        log.debug("获取离线设备列表");

        try {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectOfflineDevices();
            log.debug("离线设备列表查询完成并缓存，数量: {}", devices.size());
            return devices;

        } catch (Exception e) {
            log.error("获取离线设备列表失败", e);
            throw new SmartException("获取离线设备列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "access_device:area", "access_device:online" }, allEntries = true)
    public void addDevice(AccessDeviceEntity device) {
        log.info("创建设备，device: {}", device);

        try {
            // 设置默认值
            if (device.getOnlineStatus() == null) {
                device.setOnlineStatus(0); // 默认离线
            }
            if (device.getEnabled() == null) {
                device.setEnabled(1); // 默认启用
            }
            if (device.getHeartbeatInterval() == null) {
                device.setHeartbeatInterval(60); // 默认60秒心跳
            }

            // 插入数据库
            int result = accessDeviceDao.insert(device);
            if (result <= 0) {
                throw new BusinessException("设备创建失败");
            }

            log.info("设备创建成功并清除相关缓存，accessDeviceId: {}", device.getAccessDeviceId());

        } catch (BusinessException e) {
            log.warn("创建设备业务异常，device: {}", device, e);
            throw e;
        } catch (Exception e) {
            log.error("创建设备失败，device: {}", device, e);
            throw new SmartException("创建设备失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CachePut(value = "access_device", key = "#device.accessDeviceId")
    @CacheEvict(value = { "access_device:area", "access_device:online", "access_device:offline" }, allEntries = true)
    public void updateDevice(AccessDeviceEntity device) {
        log.info("更新设备，device: {}", device);

        try {
            // 验证设备存在
            AccessDeviceEntity existingDevice = getDeviceById(device.getAccessDeviceId());

            // 更新数据库
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                throw new BusinessException("设备更新失败");
            }

            log.info("设备更新成功并更新相关缓存，accessDeviceId: {}", device.getAccessDeviceId());

        } catch (BusinessException e) {
            log.warn("更新设备业务异常，device: {}", device, e);
            throw e;
        } catch (Exception e) {
            log.error("更新设备失败，device: {}", device, e);
            throw new SmartException("更新设备失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "access_device", "access_device:area", "access_device:online",
            "access_device:offline" }, allEntries = true)
    public void deleteDevice(Long accessDeviceId) {
        log.info("删除设备，accessDeviceId: {}", accessDeviceId);

        try {
            // 验证设备存在
            getDeviceById(accessDeviceId);

            // 软删除
            LambdaUpdateWrapper<AccessDeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AccessDeviceEntity::getAccessDeviceId, accessDeviceId)
                    .set(AccessDeviceEntity::getDeletedFlag, 1)
                    .set(AccessDeviceEntity::getUpdateTime, LocalDateTime.now());

            int result = accessDeviceDao.update(updateWrapper);
            if (result <= 0) {
                throw new BusinessException("设备删除失败");
            }

            log.info("设备删除成功并清除所有相关缓存，accessDeviceId: {}", accessDeviceId);

        } catch (BusinessException e) {
            log.warn("删除设备业务异常，accessDeviceId: {}", accessDeviceId, e);
            throw e;
        } catch (Exception e) {
            log.error("删除设备失败，accessDeviceId: {}", accessDeviceId, e);
            throw new SmartException("删除设备失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteDevices(List<Long> accessDeviceIds) {
        log.info("批量删除设备，accessDeviceIds: {}", accessDeviceIds);

        try {
            if (accessDeviceIds == null || accessDeviceIds.isEmpty()) {
                throw new BusinessException("设备ID列表不能为空");
            }

            int deletedCount = 0;
            for (Long accessDeviceId : accessDeviceIds) {
                deleteDevice(accessDeviceId);
                deletedCount++;
            }

            log.info("批量删除设备完成，数量: {}", deletedCount);

        } catch (Exception e) {
            log.error("批量删除设备失败，accessDeviceIds: {}", accessDeviceIds, e);
            throw new SmartException("批量删除设备失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceOnlineStatus(Long accessDeviceId, Integer onlineStatus) {
        log.info("更新设备在线状态，accessDeviceId: {}, onlineStatus: {}", accessDeviceId, onlineStatus);

        try {
            // 验证设备存在
            getDeviceById(accessDeviceId);

            // 更新状态
            LambdaUpdateWrapper<AccessDeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AccessDeviceEntity::getAccessDeviceId, accessDeviceId)
                    .set(AccessDeviceEntity::getOnlineStatus, onlineStatus)
                    .set(AccessDeviceEntity::getLastCommTime, LocalDateTime.now())
                    .set(AccessDeviceEntity::getUpdateTime, LocalDateTime.now());

            int result = accessDeviceDao.update(updateWrapper);
            if (result <= 0) {
                throw new BusinessException("设备状态更新失败");
            }

            log.info("设备状态更新成功，accessDeviceId: {}, onlineStatus: {}", accessDeviceId, onlineStatus);

        } catch (BusinessException e) {
            log.warn("更新设备状态业务异常，accessDeviceId: {}, onlineStatus: {}", accessDeviceId, onlineStatus, e);
            throw e;
        } catch (Exception e) {
            log.error("更新设备状态失败，accessDeviceId: {}, onlineStatus: {}", accessDeviceId, onlineStatus, e);
            throw new SmartException("更新设备状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateDeviceOnlineStatus(List<Long> accessDeviceIds, Integer onlineStatus) {
        log.info("批量更新设备在线状态，accessDeviceIds: {}, onlineStatus: {}", accessDeviceIds, onlineStatus);

        try {
            if (accessDeviceIds == null || accessDeviceIds.isEmpty()) {
                throw new BusinessException("设备ID列表不能为空");
            }

            int result = accessDeviceDao.batchUpdateOnlineStatus(accessDeviceIds, onlineStatus);
            log.info("批量更新设备状态完成，影响行数: {}", result);

        } catch (Exception e) {
            log.error("批量更新设备状态失败，accessDeviceIds: {}, onlineStatus: {}", accessDeviceIds, onlineStatus, e);
            throw new SmartException("批量更新设备状态失败: " + e.getMessage());
        }
    }

    @Override
    public boolean remoteOpenDoor(Long accessDeviceId) {
        log.info("远程开门，accessDeviceId: {}", accessDeviceId);

        try {
            AccessDeviceEntity device = getDeviceById(accessDeviceId);

            // 检查设备是否支持远程开门
            if (device.getRemoteOpenEnabled() == null || device.getRemoteOpenEnabled() != 1) {
                throw new BusinessException("设备不支持远程开门");
            }

            // 检查设备是否在线
            if (device.getOnlineStatus() == null || device.getOnlineStatus() != 1) {
                throw new BusinessException("设备离线，无法远程开门");
            }

            // 获取协议适配器并执行远程开门
            net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolAdapter adapter = protocolAdapterFactory
                    .getAdapter(device);
            boolean success = adapter.remoteOpen(device);

            if (success) {
                log.info("远程开门指令已发送并执行成功，accessDeviceId: {}", accessDeviceId);
            } else {
                log.warn("远程开门指令已发送但设备返回失败，accessDeviceId: {}", accessDeviceId);
                throw new BusinessException("远程开门失败，设备返回失败响应");
            }
            return success;

        } catch (BusinessException e) {
            log.warn("远程开门业务异常，accessDeviceId: {}", accessDeviceId, e);
            throw e;
        } catch (net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolException e) {
            log.error("远程开门协议异常，accessDeviceId: {}", accessDeviceId, e);
            throw new BusinessException("远程开门失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("远程开门失败，accessDeviceId: {}", accessDeviceId, e);
            throw new SmartException("远程开门失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDeviceStatistics() {
        log.info("获取设备统计信息");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总设备数
            Long totalDevices = accessDeviceDao.countTotalDevices();
            statistics.put("totalDevices", totalDevices);

            // 在线设备数
            Long onlineDevices = accessDeviceDao.countOnlineDevices();
            statistics.put("onlineDevices", onlineDevices);

            // 离线设备数
            statistics.put("offlineDevices", totalDevices - onlineDevices);

            // 在线率
            double onlineRate = totalDevices > 0 ? (double) onlineDevices / totalDevices * 100 : 0;
            statistics.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);

            log.info("设备统计信息获取完成: {}", statistics);
            return statistics;

        } catch (Exception e) {
            log.error("获取设备统计信息失败", e);
            throw new SmartException("获取设备统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDeviceHealthStatus(Long accessDeviceId) {
        log.info("获取设备健康状态，accessDeviceId: {}", accessDeviceId);

        try {
            AccessDeviceEntity device = getDeviceById(accessDeviceId);

            Map<String, Object> healthStatus = new HashMap<>();

            // 基础状态
            healthStatus.put("online", device.isOnline());
            healthStatus.put("enabled", device.isEnabled());
            healthStatus.put("lastCommTime", device.getLastCommTime());
            healthStatus.put("lastHeartbeatTime", device.getLastHeartbeatTime());

            // 心跳状态
            boolean heartbeatTimeout = device.isHeartbeatTimeout();
            healthStatus.put("heartbeatTimeout", heartbeatTimeout);

            // 维护状态
            boolean needsMaintenance = device.needsMaintenance();
            healthStatus.put("needsMaintenance", needsMaintenance);

            // 整体健康评分 (0-100)
            int healthScore = 100;
            if (!device.isOnline())
                healthScore -= 50;
            if (heartbeatTimeout)
                healthScore -= 30;
            if (needsMaintenance)
                healthScore -= 20;
            healthStatus.put("healthScore", Math.max(0, healthScore));

            log.info("设备健康状态获取完成，accessDeviceId: {}, healthScore: {}", accessDeviceId, healthScore);
            return healthStatus;

        } catch (BusinessException e) {
            log.warn("获取设备健康状态业务异常，accessDeviceId: {}", accessDeviceId, e);
            throw e;
        } catch (Exception e) {
            log.error("获取设备健康状态失败，accessDeviceId: {}", accessDeviceId, e);
            throw new SmartException("获取设备健康状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deviceHeartbeat(Long accessDeviceId, Map<String, Object> heartbeatData) {
        log.debug("设备心跳上报，accessDeviceId: {}, heartbeatData: {}", accessDeviceId, heartbeatData);

        try {
            // 更新心跳时间
            LambdaUpdateWrapper<AccessDeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AccessDeviceEntity::getAccessDeviceId, accessDeviceId)
                    .set(AccessDeviceEntity::getLastHeartbeatTime, LocalDateTime.now())
                    .set(AccessDeviceEntity::getOnlineStatus, 1)
                    .set(AccessDeviceEntity::getUpdateTime, LocalDateTime.now());

            int result = accessDeviceDao.update(updateWrapper);
            if (result <= 0) {
                log.warn("设备心跳更新失败，accessDeviceId: {}", accessDeviceId);
            }

        } catch (Exception e) {
            log.error("设备心跳处理失败，accessDeviceId: {}", accessDeviceId, e);
            // 心跳失败不影响主流程，只记录日志
        }
    }

    @Override
    public boolean restartDevice(Long accessDeviceId) {
        log.info("重启设备，accessDeviceId: {}", accessDeviceId);

        try {
            AccessDeviceEntity device = getDeviceById(accessDeviceId);

            // 检查设备是否在线
            if (!device.isOnline()) {
                throw new BusinessException("设备离线，无法重启");
            }

            // 获取协议适配器并执行设备重启
            net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolAdapter adapter = protocolAdapterFactory
                    .getAdapter(device);
            boolean success = adapter.restartDevice(device);

            if (success) {
                log.info("设备重启指令已发送并执行成功，accessDeviceId: {}", accessDeviceId);
            } else {
                log.warn("设备重启指令已发送但设备返回失败，accessDeviceId: {}", accessDeviceId);
                throw new BusinessException("设备重启失败，设备返回失败响应");
            }
            return success;

        } catch (BusinessException e) {
            log.warn("重启设备业务异常，accessDeviceId: {}", accessDeviceId, e);
            throw e;
        } catch (net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolException e) {
            log.error("重启设备协议异常，accessDeviceId: {}", accessDeviceId, e);
            throw new BusinessException("重启设备失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("重启设备失败，accessDeviceId: {}", accessDeviceId, e);
            throw new SmartException("重启设备失败: " + e.getMessage());
        }
    }

    @Override
    public boolean syncDeviceTime(Long accessDeviceId) {
        log.info("同步设备时间，accessDeviceId: {}", accessDeviceId);

        try {
            AccessDeviceEntity device = getDeviceById(accessDeviceId);

            if (!device.isOnline()) {
                throw new BusinessException("设备离线，无法同步时间");
            }

            // 获取协议适配器并执行时间同步
            net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolAdapter adapter = protocolAdapterFactory
                    .getAdapter(device);
            boolean success = adapter.syncDeviceTime(device);

            if (success) {
                log.info("设备时间同步指令已发送并执行成功，accessDeviceId: {}", accessDeviceId);
            } else {
                log.warn("设备时间同步指令已发送但设备返回失败，accessDeviceId: {}", accessDeviceId);
                throw new BusinessException("设备时间同步失败，设备返回失败响应");
            }
            return success;

        } catch (BusinessException e) {
            log.warn("同步设备时间业务异常，accessDeviceId: {}", accessDeviceId, e);
            throw e;
        } catch (net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolException e) {
            log.error("同步设备时间协议异常，accessDeviceId: {}", accessDeviceId, e);
            throw new BusinessException("同步设备时间失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("同步设备时间失败，accessDeviceId: {}", accessDeviceId, e);
            throw new SmartException("同步设备时间失败: " + e.getMessage());
        }
    }

    @Override
    public List<AccessDeviceEntity> getDevicesNeedingMaintenance() {
        log.debug("获取需要维护的设备列表");

        try {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectDevicesNeedingMaintenance();
            log.debug("需要维护的设备列表查询完成，数量: {}", devices.size());
            return devices;

        } catch (Exception e) {
            log.error("获取需要维护的设备列表失败", e);
            throw new SmartException("获取需要维护的设备列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<AccessDeviceEntity> getHeartbeatTimeoutDevices() {
        log.debug("获取心跳超时的设备列表");

        try {
            List<AccessDeviceEntity> devices = accessDeviceDao.selectHeartbeatTimeoutDevices();
            log.debug("心跳超时的设备列表查询完成，数量: {}", devices.size());
            return devices;

        } catch (Exception e) {
            log.error("获取心跳超时的设备列表失败", e);
            throw new SmartException("获取心跳超时的设备列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceWorkMode(Long accessDeviceId, Integer workMode) {
        log.info("更新设备工作模式，accessDeviceId: {}, workMode: {}", accessDeviceId, workMode);

        try {
            // 验证设备存在
            getDeviceById(accessDeviceId);

            // 更新工作模式
            LambdaUpdateWrapper<AccessDeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AccessDeviceEntity::getAccessDeviceId, accessDeviceId)
                    .set(AccessDeviceEntity::getWorkMode, workMode)
                    .set(AccessDeviceEntity::getUpdateTime, LocalDateTime.now());

            int result = accessDeviceDao.update(updateWrapper);
            if (result <= 0) {
                throw new BusinessException("设备工作模式更新失败");
            }

            log.info("设备工作模式更新成功，accessDeviceId: {}, workMode: {}", accessDeviceId, workMode);

        } catch (BusinessException e) {
            log.warn("更新设备工作模式业务异常，accessDeviceId: {}, workMode: {}", accessDeviceId, workMode, e);
            throw e;
        } catch (Exception e) {
            log.error("更新设备工作模式失败，accessDeviceId: {}, workMode: {}", accessDeviceId, workMode, e);
            throw new SmartException("更新设备工作模式失败: " + e.getMessage());
        }
    }
}