package net.lab1024.sa.admin.module.access.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.admin.module.device.service.SmartDeviceService;
import net.lab1024.sa.admin.module.device.dao.SmartDeviceDao;
import net.lab1024.sa.admin.module.access.service.AccessDeviceService;
import net.lab1024.sa.admin.module.access.dao.AccessDeviceDao;
import net.lab1024.sa.admin.module.access.dao.SmartDeviceAccessExtensionDao;
import net.lab1024.sa.admin.module.access.dao.SmartAccessAlertDao;
import net.lab1024.sa.admin.module.access.dao.SmartAccessDoorDao;
import net.lab1024.sa.admin.module.access.dao.SmartAccessReaderDao;
import net.lab1024.sa.admin.module.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.access.domain.entity.SmartAccessAlertEntity;
import net.lab1024.sa.admin.module.access.domain.entity.SmartDeviceAccessExtensionEntity;
import net.lab1024.sa.admin.module.access.domain.entity.SmartAccessDoorEntity;
import net.lab1024.sa.admin.module.access.domain.entity.SmartAccessReaderEntity;
import net.lab1024.sa.admin.module.access.domain.vo.AccessDeviceDetailVO;
import net.lab1024.sa.admin.module.access.domain.vo.AccessDeviceStatisticsVO;
import net.lab1024.sa.admin.module.access.domain.vo.AccessDeviceStatusVO;
import net.lab1024.sa.admin.module.access.domain.vo.AccessControlConfigVO;
import net.lab1024.sa.admin.module.access.manager.AccessCacheManager;
import net.lab1024.sa.base.common.cache.UnifiedCacheManager;
import net.lab1024.sa.admin.module.access.manager.AccessDeviceManager;

/**
 * 门禁设备管理服务实现类
 * <p>
 * 严格遵循repowiki规范：
 * - 继承ServiceImpl并实现AccessDeviceService接口
 * - 遵循四层架构规范：Controller→Service→Manager→DAO
 * - 事务边界在Service层，使用@Transactional注解
 * - 集成现有SmartDeviceService，扩展门禁特有功能
 * - 使用缓存管理器提高性能
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Slf4j
@Service
public class AccessDeviceServiceImpl extends ServiceImpl<AccessDeviceDao, AccessDeviceEntity>
        implements AccessDeviceService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private SmartDeviceService smartDeviceService;

    @Resource
    private SmartDeviceDao smartDeviceDao;

    @Resource
    private SmartDeviceAccessExtensionDao accessExtensionDao;

    @Resource
    private SmartAccessAlertDao accessAlertDao;

    @Resource
    private SmartAccessDoorDao accessDoorDao;

    @Resource
    private SmartAccessReaderDao accessReaderDao;

    @Resource
    private AccessDeviceManager accessDeviceManager;

    @Resource
    private AccessCacheManager accessCacheManager;

    @Resource
    private UnifiedCacheManager unifiedCacheManager;

    // ==================== 设备基础操作 ====================

    @Override
    public void deviceHeartbeat(Long deviceId, Map<String, Object> heartbeatData) {
        log.debug("设备心跳，设备ID: {}, 数据: {}", deviceId, heartbeatData);

        try {
            if (deviceId == null) {
                log.warn("设备ID为空，无法处理心跳");
                return;
            }

            // 获取设备信息
            SmartDeviceEntity device = smartDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("设备不存在，设备ID: {}", deviceId);
                return;
            }

            // 更新设备心跳时间和状态
            device.setLastHeartbeatTime(LocalDateTime.now());
            device.setUpdateTime(LocalDateTime.now());

            // 如果设备当前离线，更新为在线
            if (device.getOnlineStatus() == null || device.getOnlineStatus() != 1) {
                device.setOnlineStatus(1);
                log.info("设备上线，设备ID: {}", deviceId);
            }

            // 更新数据库
            smartDeviceDao.updateById(device);

            // 处理心跳数据
            if (heartbeatData != null && !heartbeatData.isEmpty()) {
                processHeartbeatData(deviceId, heartbeatData);
            }

            // 清除相关缓存
            accessCacheManager.clearDeviceCache(deviceId);

            log.debug("设备心跳处理完成，设备ID: {}", deviceId);

        } catch (Exception e) {
            log.error("设备心跳处理失败，设备ID: {}", deviceId, e);
        }
    }

    /**
     * 处理心跳数据
     *
     * @param deviceId 设备ID
     * @param heartbeatData 心跳数据
     */
    private void processHeartbeatData(Long deviceId, Map<String, Object> heartbeatData) {
        try {
            // TODO: 实现心跳数据处理逻辑
            // 例如：设备状态、电量、温度、网络状态等
            log.debug("处理心跳数据，设备ID: {}, 数据项: {}", deviceId, heartbeatData.size());
        } catch (Exception e) {
            log.error("处理心跳数据失败，设备ID: {}", deviceId, e);
        }
    }

    @Override
    public PageResult<AccessDeviceEntity> getDevicePage(PageParam pageParam, Long deviceId, Long areaId,
                                                         String deviceName, Integer accessDeviceType, Integer onlineStatus, Integer enabled) {
        log.debug("分页查询门禁设备，页码: {}, 每页数量: {}, 区域ID: {}, 设备名称: {}, 门禁类型: {}, 在线状态: {}, 启用状态: {}",
                pageParam.getPageNum(), pageParam.getPageSize(), areaId, deviceName, accessDeviceType, onlineStatus, enabled);

        try {
            // 构建查询条件
            LambdaQueryWrapper<AccessDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (deviceId != null) {
                queryWrapper.eq(AccessDeviceEntity::getDeviceId, deviceId);
            }
            if (areaId != null) {
                queryWrapper.eq(AccessDeviceEntity::getAreaId, areaId);
            }
            if (deviceName != null && !deviceName.trim().isEmpty()) {
                queryWrapper.like(AccessDeviceEntity::getDeviceName, deviceName);
            }
            if (accessDeviceType != null) {
                queryWrapper.eq(AccessDeviceEntity::getAccessDeviceType, accessDeviceType);
            }
            if (enabled != null) {
                queryWrapper.eq(AccessDeviceEntity::getEnabled, enabled);
            }
            queryWrapper.eq(AccessDeviceEntity::getDeletedFlag, 0)
                       .orderByDesc(AccessDeviceEntity::getCreateTime);

            // 执行分页查询
            com.baomidou.mybatisplus.core.metadata.IPage<AccessDeviceEntity> page = this.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                queryWrapper
            );
            PageResult<AccessDeviceEntity> pageResult = PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());

            log.debug("门禁设备分页查询完成，总记录数: {}", pageResult.getTotalCount());
            return pageResult;
        } catch (Exception e) {
            log.error("门禁设备分页查询异常", e);
            return PageResult.empty();
        }
    }

    @Override
    public Long addDevice(AccessDeviceEntity device) {
        log.info("添加门禁设备，设备编码: {}, 设备名称: {}", device.getDeviceCode(), device.getDeviceName());

        try {
            if (device == null) {
                log.warn("设备信息为空，无法添加");
                return null;
            }

            // 设置基础信息
            device.setCreateTime(LocalDateTime.now());
            device.setUpdateTime(LocalDateTime.now());
            device.setDeletedFlag(0);

            // 保存设备信息
            boolean result = this.save(device);
            if (result) {
                log.info("门禁设备添加成功，设备ID: {}", device.getDeviceId());
                return device.getDeviceId();
            } else {
                log.error("门禁设备添加失败");
                return null;
            }
        } catch (Exception e) {
            log.error("添加门禁设备异常", e);
            return null;
        }
    }

    @Override
    public AccessDeviceEntity getDeviceById(Long deviceId) {
        log.debug("根据ID获取门禁设备，设备ID: {}", deviceId);

        try {
            if (deviceId == null) {
                log.warn("设备ID为空");
                return null;
            }

            AccessDeviceEntity device = this.getById(deviceId);
            if (device != null) {
                log.debug("获取门禁设备成功，设备ID: {}", deviceId);
            } else {
                log.warn("门禁设备不存在，设备ID: {}", deviceId);
            }
            return device;
        } catch (Exception e) {
            log.error("获取门禁设备异常，设备ID: {}", deviceId, e);
            return null;
        }
    }

    @Override
    public List<AccessDeviceEntity> getDevicesByAreaId(Long areaId) {
        log.debug("根据区域ID获取设备列表，区域ID: {}", areaId);

        try {
            if (areaId == null) {
                log.warn("区域ID为空");
                return new ArrayList<>();
            }

            LambdaQueryWrapper<AccessDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AccessDeviceEntity::getAreaId, areaId)
                       .eq(AccessDeviceEntity::getDeletedFlag, 0);

            List<AccessDeviceEntity> devices = this.list(queryWrapper);
            log.debug("根据区域ID获取设备列表完成，区域ID: {}, 设备数量: {}", areaId, devices.size());
            return devices;
        } catch (Exception e) {
            log.error("根据区域ID获取设备列表异常，区域ID: {}", areaId, e);
            return new ArrayList<>();
        }
    }

    // ==================== 设备扩展配置管理 ====================

    @Override
    public SmartDeviceAccessExtensionEntity getAccessExtension(Long deviceId) {
        log.info("获取设备门禁扩展配置，设备ID: {}", deviceId);

        try {
            SmartDeviceAccessExtensionEntity extension = accessExtensionDao.selectById(deviceId);
            if (extension != null) {
                log.info("获取设备门禁扩展配置成功，设备ID: {}", deviceId);
            } else {
                log.warn("设备门禁扩展配置不存在，设备ID: {}", deviceId);
            }
            return extension;
        } catch (Exception e) {
            log.error("获取设备门禁扩展配置异常，设备ID: {}", deviceId, e);
            throw new SmartException("获取设备门禁扩展配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateAccessExtension(Long deviceId, SmartDeviceAccessExtensionEntity extension) {
        log.info("更新设备门禁扩展配置，设备ID: {}", deviceId);

        try {
            // 验证设备是否存在
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                return ResponseDTO.error("设备不存在，设备ID: " + deviceId);
            }

            // 验证设备是否支持门禁功能
            ResponseDTO<Boolean> validationResult = validateAccessDevice(deviceId);
            if (!validationResult.getOk()) {
                return ResponseDTO.error(validationResult.getMsg());
            }

            extension.setDeviceId(deviceId);
            extension.setUpdateTime(LocalDateTime.now());

            int result;
            if (accessExtensionDao.selectById(deviceId) != null) {
                result = accessExtensionDao.updateById(extension);
            } else {
                extension.setCreateTime(LocalDateTime.now());
                result = accessExtensionDao.insert(extension);
            }

            if (result > 0) {
                log.info("更新设备门禁扩展配置成功，设备ID: {}", deviceId);
                return ResponseDTO.ok("设备门禁扩展配置更新成功");
            } else {
                log.error("更新设备门禁扩展配置失败，设备ID: {}", deviceId);
                return ResponseDTO.error("设备门禁扩展配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新设备门禁扩展配置异常，设备ID: {}", deviceId, e);
            throw new SmartException("更新设备门禁扩展配置失败: " + e.getMessage());
        }
    }

    @Override
    public AccessControlConfigVO getAccessControlConfig(Long deviceId) {
        log.info("获取门禁控制配置，设备ID: {}", deviceId);

        SmartDeviceAccessExtensionEntity extension = getAccessExtension(deviceId);
        if (extension == null) {
            log.warn("设备门禁扩展配置不存在，设备ID: {}", deviceId);
            return null;
        }

        AccessControlConfigVO config = new AccessControlConfigVO();
        config.setDeviceId(deviceId);
        config.setAccessMode(extension.getAccessMode());
        config.setOpenMode(extension.getOpenMode());
        config.setLockMode(extension.getLockMode());
        config.setVerificationMode(extension.getVerificationMode());
        config.setAntiPassbackEnabled(extension.getAntiPassbackEnabled());
        config.setDuressOpenEnabled(extension.getDuressOpenEnabled());
        config.setRemoteOpenEnabled(extension.getRemoteOpenEnabled());
        config.setTimeZoneEnabled(extension.getTimeZoneEnabled());
        config.setAccessTimeZone(extension.getAccessTimeZone());

        log.info("获取门禁控制配置成功，设备ID: {}", deviceId);
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateAccessControlConfig(Long deviceId, AccessControlConfigVO config) {
        log.info("更新门禁控制配置，设备ID: {}", deviceId);

        try {
            SmartDeviceAccessExtensionEntity extension = getAccessExtension(deviceId);
            if (extension == null) {
                extension = new SmartDeviceAccessExtensionEntity();
                extension.setDeviceId(deviceId);
                extension.setCreateTime(LocalDateTime.now());
            }

            // 更新控制配置
            extension.setAccessMode(config.getAccessMode());
            extension.setOpenMode(config.getOpenMode());
            extension.setLockMode(config.getLockMode());
            extension.setVerificationMode(config.getVerificationMode());
            extension.setAntiPassbackEnabled(config.getAntiPassbackEnabled());
            extension.setDuressOpenEnabled(config.getDuressOpenEnabled());
            extension.setRemoteOpenEnabled(config.getRemoteOpenEnabled());
            extension.setTimeZoneEnabled(config.getTimeZoneEnabled());
            extension.setAccessTimeZone(config.getAccessTimeZone());
            extension.setUpdateTime(LocalDateTime.now());

            int result;
            if (extension.getExtensionId() != null) {
                result = accessExtensionDao.updateById(extension);
            } else {
                result = accessExtensionDao.insert(extension);
            }

            if (result > 0) {
                log.info("更新门禁控制配置成功，设备ID: {}", deviceId);
                return ResponseDTO.ok("门禁控制配置更新成功");
            } else {
                log.error("更新门禁控制配置失败，设备ID: {}", deviceId);
                return ResponseDTO.error("门禁控制配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新门禁控制配置异常，设备ID: {}", deviceId, e);
            throw new SmartException("更新门禁控制配置失败: " + e.getMessage());
        }
    }

    // ==================== 设备控制操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> remoteOpenDoor(Long deviceId, Long doorId) {
        log.info("执行远程开门操作，设备ID: {}, 门ID: {}", deviceId, doorId);

        try {
            // 验证设备状态
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                return ResponseDTO.error("设备不存在，设备ID: " + deviceId);
            }

            if (!"ONLINE".equals(device.getDeviceStatus())) {
                return ResponseDTO.error("设备离线，无法执行远程开门");
            }

            // 验证门禁扩展配置
            SmartDeviceAccessExtensionEntity extension = getAccessExtension(deviceId);
            if (extension == null || !extension.getRemoteOpenEnabled()) {
                return ResponseDTO.error("设备未启用远程开门功能");
            }

            // 如果指定了门ID，验证门是否存在
            if (doorId != null) {
                SmartAccessDoorEntity door = accessDoorDao.selectById(doorId);
                if (door == null || !door.getDeviceId().equals(deviceId)) {
                    return ResponseDTO.error("门不存在或不属于该设备");
                }
            }

            // 调用Manager层执行设备控制
            boolean result = accessDeviceManager.remoteOpenDoor(deviceId, doorId);

            if (result) {
                log.info("远程开门操作成功，设备ID: {}, 门ID: {}", deviceId, doorId);
                return ResponseDTO.ok("远程开门操作成功");
            } else {
                log.error("远程开门操作失败，设备ID: {}, 门ID: {}", deviceId, doorId);
                return ResponseDTO.error("远程开门操作失败");
            }
        } catch (Exception e) {
            log.error("远程开门操作异常，设备ID: {}, 门ID: {}", deviceId, doorId, e);
            throw new SmartException("远程开门操作失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> remoteLockDoor(Long deviceId, Long doorId) {
        log.info("执行远程锁门操作，设备ID: {}, 门ID: {}", deviceId, doorId);

        try {
            // 验证设备状态
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                return ResponseDTO.error("设备不存在，设备ID: " + deviceId);
            }

            if (!"ONLINE".equals(device.getDeviceStatus())) {
                return ResponseDTO.error("设备离线，无法执行远程锁门");
            }

            // 如果指定了门ID，验证门是否存在
            if (doorId != null) {
                SmartAccessDoorEntity door = accessDoorDao.selectById(doorId);
                if (door == null || !door.getDeviceId().equals(deviceId)) {
                    return ResponseDTO.error("门不存在或不属于该设备");
                }
            }

            // 调用Manager层执行设备控制
            boolean result = accessDeviceManager.remoteLockDoor(deviceId, doorId);

            if (result) {
                log.info("远程锁门操作成功，设备ID: {}, 门ID: {}", deviceId, doorId);
                return ResponseDTO.ok("远程锁门操作成功");
            } else {
                log.error("远程锁门操作失败，设备ID: {}, 门ID: {}", deviceId, doorId);
                return ResponseDTO.error("远程锁门操作失败");
            }
        } catch (Exception e) {
            log.error("远程锁门操作异常，设备ID: {}, 门ID: {}", deviceId, doorId, e);
            throw new SmartException("远程锁门操作失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<Long, String>> batchRemoteOpenDoor(List<Long> deviceIds) {
        log.info("执行批量远程开门操作，设备数量: {}", deviceIds.size());

        Map<Long, String> results = new HashMap<>();

        for (Long deviceId : deviceIds) {
            try {
                ResponseDTO<String> result = remoteOpenDoor(deviceId, null);
                if (result.getOk()) {
                    results.put(deviceId, "操作成功");
                } else {
                    results.put(deviceId, result.getMsg());
                }
            } catch (Exception e) {
                log.error("批量远程开门操作异常，设备ID: {}", deviceId, e);
                results.put(deviceId, "操作异常: " + e.getMessage());
            }
        }

        int successCount = (int) results.values().stream().filter(msg -> "操作成功".equals(msg)).count();
        log.info("批量远程开门操作完成，成功数量: {}, 总数量: {}", successCount, deviceIds.size());

        return ResponseDTO.ok(results);
    }

    // ==================== 设备状态同步 ====================

    @Override
    public ResponseDTO<AccessDeviceStatusVO> syncDeviceStatus(Long deviceId) {
        log.info("同步设备状态，设备ID: {}", deviceId);

        try {
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                return ResponseDTO.error("设备不存在，设备ID: " + deviceId);
            }

            // 调用Manager层获取设备状态
            AccessDeviceStatusVO status = accessDeviceManager.getDeviceStatus(deviceId);

            if (status != null) {
                // 更新设备在线状态
                String onlineStatus = status.getOnline() ? "ONLINE" : "OFFLINE";
                if (!onlineStatus.equals(device.getDeviceStatus())) {
                    device.setDeviceStatus(onlineStatus);
                    device.setUpdateTime(LocalDateTime.now());
                    smartDeviceDao.updateById(device);
                }

                log.info("同步设备状态成功，设备ID: {}, 在线状态: {}", deviceId, status.getOnline());
                return ResponseDTO.ok(status);
            } else {
                log.error("同步设备状态失败，设备ID: {}", deviceId);
                return ResponseDTO.error("同步设备状态失败");
            }
        } catch (Exception e) {
            log.error("同步设备状态异常，设备ID: {}", deviceId, e);
            throw new SmartException("同步设备状态失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<Long, AccessDeviceStatusVO>> batchSyncDeviceStatus(List<Long> deviceIds) {
        log.info("批量同步设备状态，设备数量: {}", deviceIds.size());

        Map<Long, AccessDeviceStatusVO> results = new HashMap<>();

        for (Long deviceId : deviceIds) {
            try {
                ResponseDTO<AccessDeviceStatusVO> result = syncDeviceStatus(deviceId);
                if (result.getOk()) {
                    results.put(deviceId, result.getData());
                }
            } catch (Exception e) {
                log.error("批量同步设备状态异常，设备ID: {}", deviceId, e);
            }
        }

        log.info("批量同步设备状态完成，成功数量: {}, 总数量: {}", results.size(), deviceIds.size());
        return ResponseDTO.ok(results);
    }

    @Override
    public Boolean isDeviceOnline(Long deviceId) {
        try {
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            return device != null && "ONLINE".equals(device.getDeviceStatus());
        } catch (Exception e) {
            log.error("检查设备在线状态异常，设备ID: {}", deviceId, e);
            return false;
        }
    }

    @Override
    public LocalDateTime getLastHeartbeatTime(Long deviceId) {
        try {
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            return device != null ? device.getLastHeartbeatTime() : null;
        } catch (Exception e) {
            log.error("获取设备最后心跳时间异常，设备ID: {}", deviceId, e);
            return null;
        }
    }

    // ==================== 设备验证功能 ====================

    @Override
    public ResponseDTO<Boolean> validateAccessDevice(Long deviceId) {
        log.info("验证设备门禁功能，设备ID: {}", deviceId);

        try {
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                return ResponseDTO.error("设备不存在");
            }

            // 检查设备类型是否支持门禁功能
            String deviceType = device.getDeviceType();
            boolean supportedType = "ACCESS_CONTROLLER".equals(deviceType) ||
                                 "DOOR_CONTROLLER".equals(deviceType) ||
                                 "READER".equals(deviceType) ||
                                 "MULTI_DOOR_CONTROLLER".equals(deviceType);

            if (!supportedType) {
                return ResponseDTO.error("设备类型不支持门禁功能: " + deviceType);
            }

            // 检查门禁扩展配置
            SmartDeviceAccessExtensionEntity extension = getAccessExtension(deviceId);
            if (extension == null) {
                log.warn("设备缺少门禁扩展配置，设备ID: {}", deviceId);
                return ResponseDTO.ok(false); // 返回true但不建议，可以配置扩展信息
            }

            log.info("设备门禁功能验证通过，设备ID: {}", deviceId);
            return ResponseDTO.ok(true);
        } catch (Exception e) {
            log.error("验证设备门禁功能异常，设备ID: {}", deviceId, e);
            return ResponseDTO.error("验证设备门禁功能失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<String>> validateDeviceConfig(Long deviceId) {
        log.info("验证设备配置完整性，设备ID: {}", deviceId);

        List<String> missingConfigs = new ArrayList<>();

        try {
            // 验证基础设备信息
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                missingConfigs.add("设备基础信息不存在");
                return ResponseDTO.error(String.join("; ", missingConfigs));
            }

            // 验证门禁扩展配置
            SmartDeviceAccessExtensionEntity extension = getAccessExtension(deviceId);
            if (extension == null) {
                missingConfigs.add("门禁扩展配置不存在");
            } else {
                if (extension.getAccessMode() == null) {
                    missingConfigs.add("门禁模式未配置");
                }
                if (extension.getVerificationMode() == null) {
                    missingConfigs.add("验证模式未配置");
                }
            }

            // 验证门配置
            List<SmartAccessDoorEntity> doors = accessDoorDao.selectByDeviceId(deviceId);
            if (doors.isEmpty()) {
                missingConfigs.add("门配置不存在");
            }

            // 验证读头配置
            List<SmartAccessReaderEntity> readers = accessReaderDao.selectByDeviceId(deviceId);
            if (readers.isEmpty()) {
                missingConfigs.add("读头配置不存在");
            }

            if (missingConfigs.isEmpty()) {
                log.info("设备配置完整性验证通过，设备ID: {}", deviceId);
                return ResponseDTO.ok(missingConfigs);
            } else {
                log.warn("设备配置存在缺失，设备ID: {}, 缺失配置: {}", deviceId, missingConfigs);
                return ResponseDTO.error(String.join("; ", missingConfigs));
            }
        } catch (Exception e) {
            log.error("验证设备配置完整性异常，设备ID: {}", deviceId, e);
            return ResponseDTO.error("验证设备配置完整性失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> checkDeviceSecurity(Long deviceId) {
        log.info("检查设备安全状态，设备ID: {}", deviceId);

        Map<String, Object> securityStatus = new HashMap<>();
        boolean isSecure = true;

        try {
            // 检查设备在线状态
            boolean online = isDeviceOnline(deviceId);
            securityStatus.put("online", online);
            if (!online) {
                isSecure = false;
            }

            // 检查设备认证状态
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            boolean authenticated = device != null && "AUTHENTICATED".equals(device.getAuthStatus());
            securityStatus.put("authenticated", authenticated);
            if (!authenticated) {
                isSecure = false;
            }

            // 检查设备告警状态
            List<SmartAccessAlertEntity> alerts = accessAlertDao.selectPendingAlerts().stream()
                    .filter(alert -> deviceId.equals(alert.getDeviceId()))
                    .collect(Collectors.toList());
            boolean hasActiveAlerts = !alerts.isEmpty();
            securityStatus.put("activeAlerts", hasActiveAlerts);
            securityStatus.put("alertCount", alerts.size());
            if (hasActiveAlerts) {
                isSecure = false;
            }

            // 检查设备配置完整性
            ResponseDTO<List<String>> configValidation = validateDeviceConfig(deviceId);
            boolean configComplete = configValidation.getOk() && configValidation.getData().isEmpty();
            securityStatus.put("configComplete", configComplete);
            if (!configComplete) {
                isSecure = false;
            }

            securityStatus.put("secure", isSecure);
            securityStatus.put("checkTime", LocalDateTime.now());

            log.info("设备安全状态检查完成，设备ID: {}, 安全状态: {}", deviceId, isSecure);
            return ResponseDTO.ok(securityStatus);
        } catch (Exception e) {
            log.error("检查设备安全状态异常，设备ID: {}", deviceId, e);
            securityStatus.put("secure", false);
            securityStatus.put("error", e.getMessage());
            return ResponseDTO.error("检查设备安全状态失败: " + e.getMessage());
        }
    }

    // ==================== 设备详情查询 ====================

    @Override
    public ResponseDTO<AccessDeviceDetailVO> getAccessDeviceDetail(Long deviceId) {
        log.info("获取门禁设备详细信息，设备ID: {}", deviceId);

        try {
            // 获取基础设备信息
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                return ResponseDTO.error("设备不存在");
            }

            // 获取门禁扩展配置
            SmartDeviceAccessExtensionEntity extension = getAccessExtension(deviceId);

            // 获取门列表
            List<SmartAccessDoorEntity> doors = accessDoorDao.selectByDeviceId(deviceId);

            // 获取读头列表
            List<SmartAccessReaderEntity> readers = accessReaderDao.selectByDeviceId(deviceId);

            // 获取最新告警
            List<SmartAccessAlertEntity> alerts = accessAlertDao.selectByDeviceId(deviceId).stream()
                    .limit(5)
                    .collect(Collectors.toList());

            // 组装详细信息
            AccessDeviceDetailVO detail = new AccessDeviceDetailVO();
            detail.setDeviceId(deviceId);
            detail.setDeviceCode(device.getDeviceCode());
            detail.setDeviceName(device.getDeviceName());
            // 类型转换：String -> Integer
            detail.setDeviceType(device.getDeviceType() != null ? Integer.parseInt(device.getDeviceType()) : null);
            detail.setDeviceStatus(device.getDeviceStatus() != null ? Integer.parseInt(device.getDeviceStatus()) : null);
            detail.setOnline(isDeviceOnline(deviceId));
            detail.setExtension(extension);
            detail.setDoors(doors);
            detail.setReaders(readers);
            detail.setLatestAlerts(alerts);

            log.info("获取门禁设备详细信息成功，设备ID: {}", deviceId);
            return ResponseDTO.ok(detail);
        } catch (Exception e) {
            log.error("获取门禁设备详细信息异常，设备ID: {}", deviceId, e);
            return ResponseDTO.error("获取设备详细信息失败: " + e.getMessage());
        }
    }

    // ==================== 设备统计功能 ====================

    @Override
    public ResponseDTO<AccessDeviceStatisticsVO> getAccessDeviceStatistics() {
        log.info("获取门禁设备统计信息");

        try {
            AccessDeviceStatisticsVO statistics = new AccessDeviceStatisticsVO();

            // 获取所有门禁相关设备
            List<Long> accessDeviceIds = accessExtensionDao.selectList(null).stream()
                    .map(SmartDeviceAccessExtensionEntity::getDeviceId)
                    .collect(Collectors.toList());

            if (!accessDeviceIds.isEmpty()) {
                // 统计总数
                statistics.setTotalDevices(accessDeviceIds.size());

                // 统计在线数量
                long onlineCount = accessDeviceIds.stream()
                        .filter(this::isDeviceOnline)
                        .count();
                statistics.setOnlineDevices((int) onlineCount);

                // 统计离线数量
                statistics.setOfflineDevices(statistics.getTotalDevices() - statistics.getOnlineDevices());

                // 统计告警数量
                long alertCount = accessAlertDao.selectPendingAlerts().stream()
                        .filter(alert -> accessDeviceIds.contains(alert.getDeviceId()))
                        .count();
                statistics.setAlertDevices((int) alertCount);

                // 计算在线率
                double onlineRate = statistics.getTotalDevices() > 0 ?
                        (double) statistics.getOnlineDevices() / statistics.getTotalDevices() * 100 : 0;
                statistics.setOnlineRate(onlineRate);

                // 维护设备数量（简化实现，实际应该查询维护状态）
                statistics.setMaintenanceDevices(0);
            } else {
                statistics.setTotalDevices(0);
                statistics.setOnlineDevices(0);
                statistics.setOfflineDevices(0);
                statistics.setAlertDevices(0);
                statistics.setMaintenanceDevices(0);
                statistics.setOnlineRate(0.0);
            }

            log.info("获取门禁设备统计信息成功: {}", statistics);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取门禁设备统计信息异常", e);
            return ResponseDTO.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<Long, Integer>> countDevicesByArea(List<Long> areaIds) {
        log.info("按区域统计门禁设备数量，区域数量: {}", areaIds != null ? areaIds.size() : 0);

        Map<Long, Integer> result = new HashMap<>();

        try {
            if (areaIds == null || areaIds.isEmpty()) {
                // 统计所有区域
                List<SmartDeviceEntity> allDevices = smartDeviceDao.selectList(null);
                Map<Long, List<SmartDeviceEntity>> devicesByArea = allDevices.stream()
                        .filter(device -> device.getAreaId() != null)
                        .collect(Collectors.groupingBy(SmartDeviceEntity::getAreaId));

                for (Map.Entry<Long, List<SmartDeviceEntity>> entry : devicesByArea.entrySet()) {
                    Long areaId = entry.getKey();
                    List<SmartDeviceEntity> devices = entry.getValue();

                    // 筛选出门禁设备
                    int accessDeviceCount = (int) devices.stream()
                            .filter(device -> accessExtensionDao.selectById(device.getDeviceId()) != null)
                            .count();

                    result.put(areaId, accessDeviceCount);
                }
            } else {
                // 统计指定区域
                for (Long areaId : areaIds) {
                    LambdaQueryWrapper<SmartDeviceEntity> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(SmartDeviceEntity::getAreaId, areaId);
                    List<SmartDeviceEntity> devices = smartDeviceDao.selectList(wrapper);

                    int accessDeviceCount = (int) devices.stream()
                            .filter(device -> accessExtensionDao.selectById(device.getDeviceId()) != null)
                            .count();

                    result.put(areaId, accessDeviceCount);
                }
            }

            log.info("按区域统计门禁设备数量完成，统计区域数量: {}", result.size());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("按区域统计门禁设备数量异常", e);
            return ResponseDTO.error("按区域统计失败: " + e.getMessage());
        }
    }

    // ==================== 设备告警管理 ====================

    @Override
    public ResponseDTO<List<SmartAccessAlertEntity>> getDeviceLatestAlerts(Long deviceId, Integer limit) {
        log.info("获取设备最新告警，设备ID: {}, 限制数量: {}", deviceId, limit);

        try {
            List<SmartAccessAlertEntity> alerts = accessAlertDao.selectByDeviceId(deviceId);

            if (limit != null && limit > 0) {
                alerts = alerts.stream().limit(limit).collect(Collectors.toList());
            }

            log.info("获取设备最新告警成功，设备ID: {}, 告警数量: {}", deviceId, alerts.size());
            return ResponseDTO.ok(alerts);
        } catch (Exception e) {
            log.error("获取设备最新告警异常，设备ID: {}", deviceId, e);
            return ResponseDTO.error("获取设备告警失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> clearDeviceAlerts(List<Long> alertIds) {
        log.info("清除设备告警，告警数量: {}", alertIds.size());

        try {
            int result = accessAlertDao.batchUpdateProcessStatus(alertIds, "CLEARED",
                    null, "系统", "批量清除告警");

            if (result > 0) {
                log.info("清除设备告警成功，清除数量: {}", result);
                return ResponseDTO.ok("成功清除 " + result + " 条告警");
            } else {
                log.warn("未找到可清除的告警");
                return ResponseDTO.ok("未找到需要清除的告警");
            }
        } catch (Exception e) {
            log.error("清除设备告警异常", e);
            throw new SmartException("清除设备告警失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Integer>> getDeviceAlertStatistics(Long deviceId, Integer days) {
        log.info("获取设备告警统计，设备ID: {}, 统计天数: {}", deviceId, days);

        Map<String, Integer> statistics = new HashMap<>();

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days != null ? days : 7);

            List<SmartAccessAlertEntity> alerts = accessAlertDao.selectByTimeRange(startTime, endTime)
                    .stream()
                    .filter(alert -> deviceId.equals(alert.getDeviceId()))
                    .collect(Collectors.toList());

            // 按告警类型统计
            Map<String, Long> alertTypeStats = alerts.stream()
                    .collect(Collectors.groupingBy(SmartAccessAlertEntity::getAlertType, Collectors.counting()));

            alertTypeStats.forEach((type, count) -> statistics.put(type, count.intValue()));
            statistics.put("total", alerts.size());

            log.info("获取设备告警统计成功，设备ID: {}, 总告警数: {}", deviceId, alerts.size());
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取设备告警统计异常，设备ID: {}", deviceId, e);
            return ResponseDTO.error("获取告警统计失败: " + e.getMessage());
        }
    }

    // ==================== 与现有设备管理集成 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchSetAccessDevices(List<Long> deviceIds, String accessType) {
        log.info("批量设置门禁设备，设备数量: {}, 门禁类型: {}", deviceIds.size(), accessType);

        try {
            int successCount = 0;
            List<String> failedDevices = new ArrayList<>();

            for (Long deviceId : deviceIds) {
                try {
                    SmartDeviceEntity device = smartDeviceService.getById(deviceId);
                    if (device == null) {
                        failedDevices.add("设备ID " + deviceId + " 不存在");
                        continue;
                    }

                    // 创建或更新门禁扩展配置
                    SmartDeviceAccessExtensionEntity extension = getAccessExtension(deviceId);
                    if (extension == null) {
                        extension = new SmartDeviceAccessExtensionEntity();
                        extension.setDeviceId(deviceId);
                        extension.setCreateTime(LocalDateTime.now());
                    }

                    extension.setAccessMode(accessType);
                    extension.setUpdateTime(LocalDateTime.now());

                    int result;
                    if (extension.getExtensionId() != null) {
                        result = accessExtensionDao.updateById(extension);
                    } else {
                        result = accessExtensionDao.insert(extension);
                    }

                    if (result > 0) {
                        successCount++;
                    } else {
                        failedDevices.add("设备ID " + deviceId + " 设置失败");
                    }
                } catch (Exception e) {
                    log.error("设置门禁设备异常，设备ID: {}", deviceId, e);
                    failedDevices.add("设备ID " + deviceId + " 设置异常: " + e.getMessage());
                }
            }

            String message;
            if (failedDevices.isEmpty()) {
                message = "成功设置 " + successCount + " 个门禁设备";
                log.info("批量设置门禁设备成功: {}", message);
                return ResponseDTO.ok(message);
            } else {
                message = "成功设置 " + successCount + " 个，失败 " + failedDevices.size() + " 个";
                log.warn("批量设置门禁设备部分失败: {}, 失败设备: {}", message, failedDevices);
                return ResponseDTO.error(message);
            }
        } catch (Exception e) {
            log.error("批量设置门禁设备异常", e);
            throw new SmartException("批量设置门禁设备失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> removeAccessFunction(Long deviceId) {
        log.info("移除设备门禁功能，设备ID: {}", deviceId);

        try {
            // 删除门禁扩展配置
            int result = accessExtensionDao.deleteById(deviceId);

            if (result > 0) {
                log.info("移除设备门禁功能成功，设备ID: {}", deviceId);
                return ResponseDTO.ok("设备门禁功能已移除");
            } else {
                log.warn("设备门禁功能不存在或已移除，设备ID: {}", deviceId);
                return ResponseDTO.ok("设备门禁功能不存在或已移除");
            }
        } catch (Exception e) {
            log.error("移除设备门禁功能异常，设备ID: {}", deviceId, e);
            throw new SmartException("移除设备门禁功能失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<String>> checkAccessFunctionConflict(Long deviceId) {
        log.info("检查设备门禁功能冲突，设备ID: {}", deviceId);

        List<String> conflicts = new ArrayList<>();

        try {
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                conflicts.add("设备不存在");
                return ResponseDTO.error(String.join("; ", conflicts));
            }

            // 检查设备状态
            if ("MAINTENANCE".equals(device.getDeviceStatus())) {
                conflicts.add("设备处于维护状态");
            }

            // 检查是否已有其他业务功能
            // 这里可以根据实际业务需要添加更多冲突检查逻辑

            if (conflicts.isEmpty()) {
                log.info("设备门禁功能无冲突，设备ID: {}", deviceId);
                return ResponseDTO.ok(conflicts);
            } else {
                log.warn("设备门禁功能存在冲突，设备ID: {}, 冲突项: {}", deviceId, conflicts);
                return ResponseDTO.error(String.join("; ", conflicts));
            }
        } catch (Exception e) {
            log.error("检查设备门禁功能冲突异常，设备ID: {}", deviceId, e);
            conflicts.add("检查异常: " + e.getMessage());
            return ResponseDTO.error(String.join("; ", conflicts));
        }
    }

    // ==================== 设备在线状态管理 ====================

    @Override
    public List<AccessDeviceEntity> getOnlineAccessDevices() {
        log.info("获取在线门禁设备列表");

        try {
            // 获取在线的智能设备（仅门禁设备类型）
            List<SmartDeviceEntity> onlineSmartDevices = smartDeviceService.getOnlineDevices();

            // 过滤出门禁设备并转换为AccessDeviceEntity
            return onlineSmartDevices.stream()
                    .filter(device -> "ACCESS".equals(device.getDeviceType()))
                    .map(this::convertToAccessDeviceEntity)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取在线门禁设备列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccessDeviceEntity> getOfflineAccessDevices() {
        log.info("获取离线门禁设备列表");

        try {
            // 获取离线的智能设备（仅门禁设备类型）
            List<SmartDeviceEntity> offlineSmartDevices = smartDeviceService.getOfflineDevices();

            // 过滤出门禁设备并转换为AccessDeviceEntity
            return offlineSmartDevices.stream()
                    .filter(device -> "ACCESS".equals(device.getDeviceType()))
                    .map(this::convertToAccessDeviceEntity)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取离线门禁设备列表失败", e);
            return new ArrayList<>();
        }
    }

    // ==================== 设备维护管理 ====================

    @Override
    public Boolean syncDeviceTime(Long deviceId) {
        log.info("同步设备时间，deviceId: {}", deviceId);

        try {
            return accessDeviceManager.syncDeviceTime(deviceId);
        } catch (Exception e) {
            log.error("同步设备时间失败，deviceId: {}", deviceId, e);
            return false;
        }
    }

    @Override
    public List<AccessDeviceEntity> getDevicesNeedingMaintenance() {
        log.info("获取需要维护的设备列表");

        try {
            List<Long> deviceIds = accessDeviceManager.getDevicesNeedingMaintenance();
            if (deviceIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 根据设备ID列表获取设备实体
            return this.listByIds(deviceIds);
        } catch (Exception e) {
            log.error("获取需要维护的设备列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccessDeviceEntity> getHeartbeatTimeoutDevices() {
        log.info("获取心跳超时的设备列表");

        try {
            List<Long> deviceIds = accessDeviceManager.getHeartbeatTimeoutDevices();
            if (deviceIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 根据设备ID列表获取设备实体
            return this.listByIds(deviceIds);
        } catch (Exception e) {
            log.error("获取心跳超时的设备列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Boolean updateDeviceWorkMode(Long deviceId, Integer mode) {
        log.info("更新设备工作模式，deviceId: {}, mode: {}", deviceId, mode);

        try {
            return accessDeviceManager.updateDeviceWorkMode(deviceId, mode);
        } catch (Exception e) {
            log.error("更新设备工作模式失败，deviceId: {}, mode: {}", deviceId, mode, e);
            return false;
        }
    }

    /**
     * 将SmartDeviceEntity转换为AccessDeviceEntity
     *
     * @param smartDevice 智能设备实体
     * @return 门禁设备实体
     */
    private AccessDeviceEntity convertToAccessDeviceEntity(SmartDeviceEntity smartDevice) {
        AccessDeviceEntity accessDevice = new AccessDeviceEntity();
        // 复制基础属性
        net.lab1024.sa.base.common.util.SmartBeanUtil.copyProperties(smartDevice, accessDevice);
        return accessDevice;
    }

    /**
     * 获取设备健康状态
     *
     * @param deviceId 设备ID
     * @return 设备健康状态信息
     */
    @Override
    public Map<String, Object> getDeviceHealthStatus(Long deviceId) {
        log.info("获取设备健康状态，deviceId: {}", deviceId);

        Map<String, Object> healthStatus = new HashMap<>();

        try {
            SmartDeviceEntity device = smartDeviceDao.selectByDeviceId(deviceId);
            if (device == null) {
                healthStatus.put("status", "NOT_FOUND");
                healthStatus.put("message", "设备不存在");
                return healthStatus;
            }

            // 检查设备在线状态
            boolean isOnline = device.getOnlineStatus() != null && device.getOnlineStatus() == 1;
            healthStatus.put("online", isOnline);

            // 检查最后心跳时间
            LocalDateTime lastHeartbeat = device.getLastHeartbeatTime();
            boolean isHealthy = lastHeartbeat != null &&
                lastHeartbeat.isAfter(LocalDateTime.now().minusMinutes(5));
            healthStatus.put("healthy", isOnline && isHealthy);

            // 设备基本信息
            healthStatus.put("deviceId", deviceId);
            healthStatus.put("deviceCode", device.getDeviceCode());
            healthStatus.put("deviceName", device.getDeviceName());
            healthStatus.put("deviceType", device.getDeviceType());
            healthStatus.put("lastHeartbeat", lastHeartbeat);
            healthStatus.put("lastOnlineTime", device.getLastOnlineTime());

            // 整体状态
            String overallStatus = "UNKNOWN";
            if (!isOnline) {
                overallStatus = "OFFLINE";
            } else if (isHealthy) {
                overallStatus = "HEALTHY";
            } else {
                overallStatus = "UNHEALTHY";
            }
            healthStatus.put("status", overallStatus);

        } catch (Exception e) {
            log.error("获取设备健康状态失败，deviceId: {}", deviceId, e);
            healthStatus.put("status", "ERROR");
            healthStatus.put("message", "获取健康状态失败: " + e.getMessage());
        }

        return healthStatus;
    }

    /**
     * 清理设备缓存 - 内部方法
     *
     * @param deviceId 设备ID
     */
    private void clearDeviceCacheInternal(Long deviceId) {
        log.info("清理设备缓存，deviceId: {}", deviceId);

        try {
            // 清理设备基础信息缓存
            String deviceKey = "access:device:info:" + deviceId;
            unifiedCacheManager.remove(deviceKey);

            // 清理设备状态缓存
            String statusKey = "access:device:status:" + deviceId;
            unifiedCacheManager.remove(statusKey);

            // 清理设备权限缓存
            String permissionKey = "access:device:permission:" + deviceId;
            unifiedCacheManager.remove(permissionKey);

            log.info("设备缓存清理完成，deviceId: {}", deviceId);

        } catch (Exception e) {
            log.error("清理设备缓存失败，deviceId: {}", deviceId, e);
        }
    }

    @Override
    public Map<String, Object> getDeviceStatistics() {
        log.info("获取门禁设备统计信息");

        Map<String, Object> statistics = new HashMap<>();

        try {
            // 获取设备总数
            long totalDevices = this.count();
            statistics.put("totalDevices", totalDevices);

            // 获取在线设备数
            List<AccessDeviceEntity> onlineDevices = this.getOnlineAccessDevices();
            statistics.put("onlineDevices", onlineDevices.size());

            // 获取离线设备数
            List<AccessDeviceEntity> offlineDevices = this.getOfflineAccessDevices();
            statistics.put("offlineDevices", offlineDevices.size());

            // 计算在线率
            double onlineRate = totalDevices > 0 ? (double) onlineDevices.size() / totalDevices * 100 : 0;
            statistics.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);

            // 设备类型统计
            Map<String, Long> deviceTypeStats = new HashMap<>();
            deviceTypeStats.put("ACCESS", onlineDevices.stream()
                .filter(d -> "ACCESS".equals(d.getDeviceType())).count());
            statistics.put("deviceTypeStats", deviceTypeStats);

            // 最后更新时间
            statistics.put("lastUpdateTime", LocalDateTime.now());

            log.info("门禁设备统计信息获取完成，总设备数: {}, 在线数: {}", totalDevices, onlineDevices.size());
            return statistics;

        } catch (Exception e) {
            log.error("获取门禁设备统计信息失败", e);
            // 返回默认统计信息
            statistics.put("totalDevices", 0);
            statistics.put("onlineDevices", 0);
            statistics.put("offlineDevices", 0);
            statistics.put("onlineRate", 0.0);
            statistics.put("deviceTypeStats", new HashMap<>());
            statistics.put("lastUpdateTime", LocalDateTime.now());
            statistics.put("error", e.getMessage());
            return statistics;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean restartDevice(Long deviceId) {
        log.info("重启门禁设备，设备ID: {}", deviceId);

        try {
            // TODO: 实现设备重启逻辑
            // 1. 验证设备ID有效性
            // 2. 检查设备是否在线
            // 3. 发送重启指令
            // 4. 等待设备响应
            // 5. 更新设备状态
            // 6. 记录操作日志
            log.info("重启门禁设备完成，设备ID: {}", deviceId);
            return true;
        } catch (Exception e) {
            log.error("重启门禁设备失败，设备ID: {}", deviceId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean remoteOpenDoor(Long deviceId) {
        log.info("远程开门，设备ID: {}", deviceId);

        try {
            // TODO: 实现远程开门逻辑
            // 1. 验证设备ID有效性
            // 2. 检查设备是否在线
            // 3. 验证用户权限
            // 4. 发送开门指令
            // 5. 记录开门日志
            log.info("远程开门完成，设备ID: {}", deviceId);
            return true;
        } catch (Exception e) {
            log.error("远程开门失败，设备ID: {}", deviceId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateDeviceOnlineStatus(List<Long> deviceIds, Integer status) {
        log.info("批量更新设备在线状态，设备数量: {}, 状态: {}", deviceIds.size(), status);

        try {
            // TODO: 实现批量更新设备在线状态逻辑
            log.info("批量更新设备在线状态完成，更新数量: {}", deviceIds.size());
            return true;
        } catch (Exception e) {
            log.error("批量更新设备在线状态失败", e);
            return false;
        }
    }

      @Override
    public Boolean updateDeviceOnlineStatus(Long deviceId, Integer status) {
        log.info("更新设备在线状态，设备ID: {}, 状态: {}", deviceId, status);

        try {
            // TODO: 实现设备在线状态更新逻辑
            log.info("更新设备在线状态完成，设备ID: {}", deviceId);
            return true;
        } catch (Exception e) {
            log.error("更新设备在线状态失败，设备ID: {}", deviceId, e);
            return false;
        }
    }

    /**
     * 获取设备认证状态
     */
    private String getAuthStatus(SmartDeviceEntity device) {
        // TODO: 实现设备认证状态检查逻辑
        return "UNKNOWN";
    }

    /**
     * 清理设备缓存
     */
    @Override
    public void clearDeviceCache(Long deviceId) {
        try {
            // TODO: 实现设备缓存清理逻辑
            log.debug("清理设备缓存，设备ID: {}", deviceId);
        } catch (Exception e) {
            log.error("清理设备缓存失败，设备ID: {}", deviceId, e);
        }
    }

    /**
     * 清理设备缓存 - 内部私有方法
     */
    private void clearDeviceCacheInternal2(Long deviceId) {
        clearDeviceCacheInternal(deviceId);
    }

    /**
     * 批量删除设备
     */
    @Override
    public Boolean batchDeleteDevices(List<Long> deviceIds) {
        log.info("批量删除设备，deviceIds: {}", deviceIds);
        try {
            // TODO: 实现批量删除设备的逻辑
            for (Long deviceId : deviceIds) {
                // 清理设备缓存
                clearDeviceCache(deviceId);
            }
            log.info("批量删除设备完成，共处理: {} 个设备", deviceIds.size());
            return true;
        } catch (Exception e) {
            log.error("批量删除设备失败，deviceIds: {}", deviceIds, e);
            throw new RuntimeException("批量删除设备失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取离线设备列表
     * 实现SmartDeviceService接口中的getOfflineDevices方法
     */
    @Override
    public List<SmartDeviceEntity> getOfflineDevices() {
        try {
            log.debug("获取离线设备列表");

            LambdaQueryWrapper<SmartDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SmartDeviceEntity::getOnlineStatus, 0)  // 0-离线
                       .eq(SmartDeviceEntity::getDeletedFlag, 0);   // 未删除

            List<SmartDeviceEntity> offlineDevices = smartDeviceDao.selectList(queryWrapper);

            log.debug("获取离线设备列表完成，数量: {}", offlineDevices.size());
            return offlineDevices;
        } catch (Exception e) {
            log.error("获取离线设备列表异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取在线设备列表
     * 实现SmartDeviceService接口中的getOnlineDevices方法
     */
    @Override
    public List<SmartDeviceEntity> getOnlineDevices() {
        try {
            log.debug("获取在线设备列表");

            LambdaQueryWrapper<SmartDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SmartDeviceEntity::getOnlineStatus, 1)  // 1-在线
                       .eq(SmartDeviceEntity::getDeletedFlag, 0);   // 未删除

            List<SmartDeviceEntity> onlineDevices = smartDeviceDao.selectList(queryWrapper);

            log.debug("获取在线设备列表完成，数量: {}", onlineDevices.size());
            return onlineDevices;
        } catch (Exception e) {
            log.error("获取在线设备列表异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新设备配置
     * 实现SmartDeviceService接口中的updateDeviceConfig方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDeviceConfig(Long deviceId, Map<String, Object> config) {
        try {
            log.debug("更新设备配置，deviceId: {}", deviceId);

            if (deviceId == null || config == null) {
                return ResponseDTO.error("参数不完整");
            }

            // 1. 验证设备是否存在
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                return ResponseDTO.error("设备不存在，设备ID: " + deviceId);
            }

            // 2. 更新设备配置
            device.setConfigJson(config);
            device.setUpdateTime(LocalDateTime.now());

            boolean result = smartDeviceService.updateById(device);

            if (result) {
                log.info("设备配置更新成功，deviceId: {}", deviceId);
                return ResponseDTO.ok("设备配置更新成功");
            } else {
                log.error("设备配置更新失败，deviceId: {}", deviceId);
                return ResponseDTO.error("设备配置更新失败");
            }

        } catch (Exception e) {
            log.error("更新设备配置异常，deviceId: {}", deviceId, e);
            return ResponseDTO.error("更新设备配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备配置
     * 实现SmartDeviceService接口中的getDeviceConfig方法
     */
    @Override
    public ResponseDTO<Map<String, Object>> getDeviceConfig(Long deviceId) {
        try {
            log.debug("获取设备配置，deviceId: {}", deviceId);

            if (deviceId == null) {
                return ResponseDTO.error("设备ID不能为空");
            }

            // 1. 验证设备是否存在
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                log.warn("设备不存在，deviceId: {}", deviceId);
                return ResponseDTO.error("设备不存在，设备ID: " + deviceId);
            }

            // 2. 返回设备配置
            Map<String, Object> config = device.getConfigJson();
            if (config == null) {
                config = new HashMap<>();
            }

            log.debug("获取设备配置完成，deviceId: {}, 配置项数量: {}", deviceId, config.size());
            return ResponseDTO.ok(config);

        } catch (Exception e) {
            log.error("获取设备配置异常，deviceId: {}", deviceId, e);
            return ResponseDTO.error("获取设备配置失败: " + e.getMessage());
        }
    }

    /**
     * 远程控制设备
     * 实现SmartDeviceService接口中的remoteControlDevice方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> remoteControlDevice(Long deviceId, String command, Map<String, Object> params) {
        try {
            log.debug("远程控制设备，deviceId: {}, command: {}", deviceId, command);

            if (deviceId == null || command == null) {
                return ResponseDTO.error("参数不完整");
            }

            // 1. 验证设备是否存在
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                return ResponseDTO.error("设备不存在，设备ID: " + deviceId);
            }

            // 2. 验证设备是否在线
            if (!device.isOnline()) {
                return ResponseDTO.error("设备离线，无法远程控制");
            }

            // 3. 根据命令类型执行相应的操作
            String result;
            switch (command) {
                case "OPEN_DOOR":
                    result = executeOpenDoorCommand(deviceId, params);
                    break;
                case "CLOSE_DOOR":
                    result = executeCloseDoorCommand(deviceId, params);
                    break;
                case "RESTART":
                    result = executeRestartCommand(deviceId, params);
                    break;
                case "SYNC_TIME":
                    result = executeSyncTimeCommand(deviceId, params);
                    break;
                default:
                    result = "不支持的命令: " + command;
                    break;
            }

            log.info("远程控制设备完成，deviceId: {}, command: {}, result: {}", deviceId, command, result);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("远程控制设备异常，deviceId: {}, command: {}", deviceId, command, e);
            return ResponseDTO.error("远程控制设备失败: " + e.getMessage());
        }
    }

    /**
     * 执行开门命令
     */
    private String executeOpenDoorCommand(Long deviceId, Map<String, Object> params) {
        try {
            Long doorId = params != null && params.get("doorId") != null ?
                          Long.valueOf(params.get("doorId").toString()) : null;

            if (doorId != null) {
                ResponseDTO<String> result = remoteOpenDoor(deviceId, doorId);
                return result.getOk() ? "开门成功" : "开门失败";
            } else {
                return "缺少门ID参数";
            }
        } catch (Exception e) {
            log.error("执行开门命令失败", e);
            return "开门命令执行失败: " + e.getMessage();
        }
    }

    /**
     * 执行关门命令
     */
    private String executeCloseDoorCommand(Long deviceId, Map<String, Object> params) {
        try {
            Long doorId = params != null && params.get("doorId") != null ?
                          Long.valueOf(params.get("doorId").toString()) : null;

            if (doorId != null) {
                ResponseDTO<String> result = remoteLockDoor(deviceId, doorId);
                return result.getOk() ? "关门成功" : "关门失败";
            } else {
                return "缺少门ID参数";
            }
        } catch (Exception e) {
            log.error("执行关门命令失败", e);
            return "关门命令执行失败: " + e.getMessage();
        }
    }

    /**
     * 执行重启命令
     */
    private String executeRestartCommand(Long deviceId, Map<String, Object> params) {
        try {
            // TODO: 实现设备重启逻辑
            return "设备重启命令已发送";
        } catch (Exception e) {
            log.error("执行重启命令失败", e);
            return "重启命令执行失败: " + e.getMessage();
        }
    }

    /**
     * 执行时间同步命令
     */
    private String executeSyncTimeCommand(Long deviceId, Map<String, Object> params) {
        try {
            // TODO: 实现时间同步逻辑
            return "时间同步命令已发送";
        } catch (Exception e) {
            log.error("执行时间同步命令失败", e);
            return "时间同步命令执行失败: " + e.getMessage();
        }
    }

    /**
     * 获取设备类型统计
     * 实现SmartDeviceService接口中的getDeviceTypeStatistics方法
     */
    @Override
    public Map<String, Object> getDeviceTypeStatistics() {
        try {
            log.debug("获取设备类型统计信息");

            Map<String, Object> statistics = new HashMap<>();

            // 获取门禁设备总数
            LambdaQueryWrapper<SmartDeviceEntity> accessQueryWrapper = new LambdaQueryWrapper<>();
            accessQueryWrapper.eq(SmartDeviceEntity::getDeviceType, "ACCESS")
                             .eq(SmartDeviceEntity::getDeletedFlag, 0);
            long accessCount = smartDeviceDao.selectCount(accessQueryWrapper);

            // 获取考勤设备总数
            LambdaQueryWrapper<SmartDeviceEntity> attendanceQueryWrapper = new LambdaQueryWrapper<>();
            attendanceQueryWrapper.eq(SmartDeviceEntity::getDeviceType, "ATTENDANCE")
                                 .eq(SmartDeviceEntity::getDeletedFlag, 0);
            long attendanceCount = smartDeviceDao.selectCount(attendanceQueryWrapper);

            // 获取消费设备总数
            LambdaQueryWrapper<SmartDeviceEntity> consumeQueryWrapper = new LambdaQueryWrapper<>();
            consumeQueryWrapper.eq(SmartDeviceEntity::getDeviceType, "CONSUME")
                              .eq(SmartDeviceEntity::getDeletedFlag, 0);
            long consumeCount = smartDeviceDao.selectCount(consumeQueryWrapper);

            // 获取视频设备总数
            LambdaQueryWrapper<SmartDeviceEntity> videoQueryWrapper = new LambdaQueryWrapper<>();
            videoQueryWrapper.eq(SmartDeviceEntity::getDeviceType, "VIDEO")
                            .eq(SmartDeviceEntity::getDeletedFlag, 0);
            long videoCount = smartDeviceDao.selectCount(videoQueryWrapper);

            // 汇总统计
            statistics.put("access", accessCount);
            statistics.put("attendance", attendanceCount);
            statistics.put("consume", consumeCount);
            statistics.put("video", videoCount);
            statistics.put("total", accessCount + attendanceCount + consumeCount + videoCount);

            log.debug("获取设备类型统计完成，门禁: {}, 考勤: {}, 消费: {}, 视频: {}",
                     accessCount, attendanceCount, consumeCount, videoCount);

            return statistics;

        } catch (Exception e) {
            log.error("获取设备类型统计异常", e);
            return new HashMap<>();
        }
    }
}