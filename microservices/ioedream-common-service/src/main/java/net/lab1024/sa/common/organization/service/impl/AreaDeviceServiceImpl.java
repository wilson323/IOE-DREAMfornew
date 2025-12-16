package net.lab1024.sa.common.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.manager.AreaDeviceManager;
import net.lab1024.sa.common.organization.service.AreaDeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 区域设备关联管理服务实现
 * 提供区域与设备的双向关联管理功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AreaDeviceServiceImpl implements AreaDeviceService {

    private final AreaDeviceManager areaDeviceManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addDeviceToArea(Long areaId, String deviceId, String deviceCode, String deviceName,
                               Integer deviceType, Integer deviceSubType, String businessModule, Integer priority) {
        log.info("[区域设备服务] 添加设备到区域: areaId={}, deviceId={}, deviceType={}, module={}",
                areaId, deviceId, deviceType, businessModule);

        try {
            return areaDeviceManager.addDeviceToArea(areaId, deviceId, deviceCode, deviceName,
                    deviceType, deviceSubType, businessModule, priority);
        } catch (Exception e) {
            log.error("添加设备到区域失败, areaId={}, deviceId={}", areaId, deviceId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeDeviceFromArea(Long areaId, String deviceId) {
        log.info("[区域设备服务] 移除区域中的设备: areaId={}, deviceId={}", areaId, deviceId);

        try {
            return areaDeviceManager.removeDeviceFromArea(areaId, deviceId);
        } catch (Exception e) {
            log.error("从区域移除设备失败, areaId={}, deviceId={}", areaId, deviceId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveDeviceToArea(String deviceId, Long oldAreaId, Long newAreaId) {
        log.info("[区域设备服务] 移动设备到新区域: deviceId={}, oldAreaId={}, newAreaId={}", deviceId, oldAreaId, newAreaId);

        try {
            return areaDeviceManager.moveDeviceToArea(deviceId, oldAreaId, newAreaId);
        } catch (Exception e) {
            log.error("移动设备到新区域失败, deviceId={}, oldAreaId={}, newAreaId={}", deviceId, oldAreaId, newAreaId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddDevicesToArea(Long areaId, List<AreaDeviceManager.DeviceRequest> deviceRequests) {
        log.info("[区域设备服务] 批量添加设备到区域: areaId={}, count={}", areaId, deviceRequests.size());

        try {
            return areaDeviceManager.batchAddDevicesToArea(areaId, deviceRequests);
        } catch (Exception e) {
            log.error("批量添加设备到区域失败, areaId={}", areaId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> attributes) {
        log.info("[区域设备服务] 设置设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        try {
            return areaDeviceManager.setDeviceBusinessAttributes(deviceId, areaId, attributes);
        } catch (Exception e) {
            log.error("设置设备业务属性失败, deviceId={}, areaId={}", deviceId, areaId, e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId) {
        log.debug("[区域设备服务] 获取设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        try {
            return areaDeviceManager.getDeviceBusinessAttributes(deviceId, areaId);
        } catch (Exception e) {
            log.error("获取设备业务属性失败, deviceId={}, areaId={}", deviceId, areaId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeviceRelationStatus(String relationId, Integer status) {
        log.info("[区域设备服务] 更新设备关联状态: relationId={}, status={}", relationId, status);

        try {
            return areaDeviceManager.updateDeviceRelationStatus(relationId, status);
        } catch (Exception e) {
            log.error("更新设备关联状态失败, relationId={}, status={}", relationId, status, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateAreaDeviceStatus(Long areaId, Integer status) {
        log.info("[区域设备服务] 批量更新区域设备状态: areaId={}, status={}", areaId, status);

        try {
            return areaDeviceManager.batchUpdateAreaDeviceStatus(areaId, status);
        } catch (Exception e) {
            log.error("批量更新区域设备状态失败, areaId={}, status={}", areaId, status, e);
            throw e;
        }
    }

    @Override
    public boolean isDeviceInArea(Long areaId, String deviceId) {
        log.debug("[区域设备服务] 检查设备是否在区域: areaId={}, deviceId={}", areaId, deviceId);

        try {
            return areaDeviceManager.isDeviceInArea(areaId, deviceId);
        } catch (Exception e) {
            log.error("检查设备是否在区域中失败, areaId={}, deviceId={}", areaId, deviceId, e);
            throw e;
        }
    }

    @Override
    public List<AreaDeviceEntity> getAreaDevices(Long areaId) {
        log.debug("[区域设备服务] 获取区域设备: areaId={}", areaId);

        try {
            return areaDeviceManager.getAreaDevices(areaId);
        } catch (Exception e) {
            log.error("获取区域设备列表失败, areaId={}", areaId, e);
            throw e;
        }
    }

    @Override
    public List<AreaDeviceEntity> getAreaDevicesByType(Long areaId, Integer deviceType) {
        log.debug("[区域设备服务] 获取区域指定类型设备: areaId={}, deviceType={}", areaId, deviceType);

        try {
            return areaDeviceManager.getAreaDevicesByType(areaId, deviceType);
        } catch (Exception e) {
            log.error("获取区域指定类型设备失败, areaId={}, deviceType={}", areaId, deviceType, e);
            throw e;
        }
    }

    @Override
    public List<AreaDeviceEntity> getAreaDevicesByModule(Long areaId, String businessModule) {
        log.debug("[区域设备服务] 获取区域指定业务模块设备: areaId={}, businessModule={}", areaId, businessModule);

        try {
            return areaDeviceManager.getAreaDevicesByModule(areaId, businessModule);
        } catch (Exception e) {
            log.error("获取区域指定业务模块设备失败, areaId={}, businessModule={}", areaId, businessModule, e);
            throw e;
        }
    }

    @Override
    public List<AreaDeviceEntity> getAreaPrimaryDevices(Long areaId) {
        log.debug("[区域设备服务] 获取区域主设备: areaId={}", areaId);

        try {
            return areaDeviceManager.getAreaPrimaryDevices(areaId);
        } catch (Exception e) {
            log.error("获取区域主设备失败, areaId={}", areaId, e);
            throw e;
        }
    }

    @Override
    public List<AreaDeviceEntity> getDeviceAreas(String deviceId) {
        log.debug("[区域设备服务] 获取设备所属区域: deviceId={}", deviceId);

        try {
            return areaDeviceManager.getDeviceAreas(deviceId);
        } catch (Exception e) {
            log.error("获取设备所属区域失败, deviceId={}", deviceId, e);
            throw e;
        }
    }

    @Override
    public AreaDeviceManager.AreaDeviceStatistics getAreaDeviceStatistics(Long areaId) {
        log.debug("[区域设备服务] 获取区域设备统计: areaId={}", areaId);

        try {
            return areaDeviceManager.getAreaDeviceStatistics(areaId);
        } catch (Exception e) {
            log.error("获取设备统计信息失败, areaId={}", areaId, e);
            throw e;
        }
    }

    @Override
    public List<AreaDeviceManager.ModuleDeviceDistribution> getModuleDeviceDistribution(String businessModule) {
        log.debug("[区域设备服务] 获取业务模块设备分布: businessModule={}", businessModule);

        try {
            return areaDeviceManager.getModuleDeviceDistribution(businessModule);
        } catch (Exception e) {
            log.error("获取业务模块设备分布失败, businessModule={}", businessModule, e);
            throw e;
        }
    }

    @Override
    public List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule) {
        log.debug("[区域设备服务] 获取用户可访问设备: userId={}, businessModule={}", userId, businessModule);

        try {
            return areaDeviceManager.getUserAccessibleDevices(userId, businessModule);
        } catch (Exception e) {
            log.error("获取用户可访问设备失败, userId={}, businessModule={}", userId, businessModule, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int expireDeviceRelations(String deviceId) {
        log.info("[区域设备服务] 设置设备关联为过期状态: deviceId={}", deviceId);

        try {
            return areaDeviceManager.expireDeviceRelations(deviceId);
        } catch (Exception e) {
            log.error("设置设备关联为过期状态失败, deviceId={}", deviceId, e);
            throw e;
        }
    }

    @Override
    public int getAreaOnlineDeviceCount(Long areaId) {
        log.debug("[区域设备服务] 获取区域在线设备数量: areaId={}", areaId);

        try {
            return areaDeviceManager.getAreaOnlineDeviceCount(areaId);
        } catch (Exception e) {
            log.error("获取区域在线设备数量失败, areaId={}", areaId, e);
            throw e;
        }
    }

    @Override
    public Map<String, List<AreaDeviceManager.ModuleDeviceDistribution>> getAllModuleDeviceDistribution() {
        log.debug("[区域设备服务] 获取所有模块设备分布统计");

        try {
            return areaDeviceManager.getAllModuleDeviceDistribution();
        } catch (Exception e) {
            log.error("获取所有模块设备分布统计失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMoveDevices(List<DeviceMoveRequest> moveRequests) {
        log.info("[区域设备服务] 批量移动设备: count={}", moveRequests.size());

        try {
            return areaDeviceManager.batchMoveDevices(moveRequests);
        } catch (Exception e) {
            log.error("批量移动设备失败, moveRequests={}", moveRequests, e);
            throw e;
        }
    }

    @Override
    public List<AreaDeviceEntity> getDeviceAreasByCode(String deviceCode) {
        log.debug("[区域设备服务] 根据设备编码获取所属区域: deviceCode={}", deviceCode);

        try {
            return areaDeviceManager.getDeviceAreasByCode(deviceCode);
        } catch (Exception e) {
            log.error("根据设备编码获取所属区域失败, deviceCode={}", deviceCode, e);
            throw e;
        }
    }

    @Override
    public AreaDeviceHealthStatistics getAreaDeviceHealthStatistics(Long areaId) {
        log.debug("[区域设备服务] 获取区域设备健康状态统计: areaId={}", areaId);

        try {
            return areaDeviceManager.getAreaDeviceHealthStatistics(areaId);
        } catch (Exception e) {
            log.error("获取区域设备健康状态统计失败, areaId={}", areaId, e);
            throw e;
        }
    }
}