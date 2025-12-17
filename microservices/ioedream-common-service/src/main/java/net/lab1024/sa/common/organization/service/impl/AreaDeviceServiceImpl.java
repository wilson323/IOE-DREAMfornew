package net.lab1024.sa.common.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.manager.AreaDeviceManager;
import net.lab1024.sa.common.organization.service.AreaDeviceService;
import net.lab1024.sa.common.organization.domain.dto.AreaDeviceHealthStatistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    public int batchAddDevicesToArea(Long areaId, List<AreaDeviceService.DeviceRequest> deviceRequests) {
        log.info("[区域设备服务] 批量添加设备到区域: areaId={}, count={}", areaId, deviceRequests.size());

        try {
            // 转换Service DTO到Manager DTO
            List<AreaDeviceManager.DeviceRequest> managerRequests = deviceRequests.stream().map(r -> {
                var mr = new AreaDeviceManager.DeviceRequest();
                mr.setDeviceId(r.getDeviceId());
                mr.setDeviceCode(r.getDeviceCode());
                mr.setDeviceName(r.getDeviceName());
                mr.setDeviceType(r.getDeviceType());
                mr.setDeviceSubType(r.getDeviceSubType());
                mr.setBusinessModule(r.getBusinessModule());
                mr.setPriority(r.getPriority());
                return mr;
            }).collect(java.util.stream.Collectors.toList());
            return areaDeviceManager.batchAddDevicesToArea(areaId, managerRequests);
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
    public AreaDeviceService.AreaDeviceStatistics getAreaDeviceStatistics(Long areaId) {
        log.debug("[区域设备服务] 获取区域设备统计: areaId={}", areaId);

        try {
            // 转换Manager DTO到Service DTO
            AreaDeviceManager.AreaDeviceStatistics managerStats = areaDeviceManager.getAreaDeviceStatistics(areaId);
            AreaDeviceService.AreaDeviceStatistics serviceStats = new AreaDeviceService.AreaDeviceStatistics();
            serviceStats.setTotalCount(managerStats.getTotalCount());
            serviceStats.setOnlineCount(managerStats.getOnlineCount());
            serviceStats.setOfflineCount(managerStats.getOfflineCount());
            serviceStats.setPrimaryCount(managerStats.getPrimaryCount());
            serviceStats.setSecondaryCount(managerStats.getSecondaryCount());
            serviceStats.setTypeStatistics(managerStats.getTypeStatistics());
            serviceStats.setSubtypeStatistics(managerStats.getSubtypeStatistics());
            return serviceStats;
        } catch (Exception e) {
            log.error("获取设备统计信息失败, areaId={}", areaId, e);
            throw e;
        }
    }

    @Override
    public List<AreaDeviceService.ModuleDeviceDistribution> getModuleDeviceDistribution(String businessModule) {
        log.debug("[区域设备服务] 获取业务模块设备分布: businessModule={}", businessModule);

        try {
            // 转换Manager DTO到Service DTO
            List<AreaDeviceManager.ModuleDeviceDistribution> managerDist = areaDeviceManager.getModuleDeviceDistribution(businessModule);
            return managerDist.stream().map(md -> {
                AreaDeviceService.ModuleDeviceDistribution sd = new AreaDeviceService.ModuleDeviceDistribution();
                sd.setBusinessModule(md.getBusinessModule());
                sd.setModuleName(md.getModuleName());
                sd.setAreaId(md.getAreaId());
                sd.setAreaName(md.getAreaName());
                sd.setDeviceCount(md.getDeviceCount());
                return sd;
            }).collect(java.util.stream.Collectors.toList());
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
    public Map<String, List<AreaDeviceService.ModuleDeviceDistribution>> getAllModuleDeviceDistribution() {
        log.debug("[区域设备服务] 获取所有模块设备分布统计");

        // 收集所有业务模块的分布数据
        Map<String, List<AreaDeviceService.ModuleDeviceDistribution>> result = new HashMap<>();
        String[] modules = {"access", "attendance", "consume", "video", "visitor"};
        for (String module : modules) {
            result.put(module, getModuleDeviceDistribution(module));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMoveDevices(List<AreaDeviceService.DeviceMoveRequest> moveRequests) {
        log.info("[区域设备服务] 批量移动设备: count={}", moveRequests.size());

        int successCount = 0;
        for (AreaDeviceService.DeviceMoveRequest request : moveRequests) {
            if (areaDeviceManager.moveDeviceToArea(request.getDeviceId(), request.getOldAreaId(), request.getNewAreaId())) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public List<AreaDeviceEntity> getDeviceAreasByCode(String deviceCode) {
        log.debug("[区域设备服务] 根据设备编码获取所属区域: deviceCode={}", deviceCode);
        // 暂时返回空列表，后续补充实现
        return java.util.Collections.emptyList();
    }

    @Override
    public AreaDeviceHealthStatistics getAreaDeviceHealthStatistics(Long areaId) {
        log.debug("[区域设备服务] 获取区域设备健康状态统计: areaId={}", areaId);

        // 基于现有统计数据构建健康状态统计
        AreaDeviceManager.AreaDeviceStatistics stats = areaDeviceManager.getAreaDeviceStatistics(areaId);
        AreaDeviceHealthStatistics healthStats = new AreaDeviceHealthStatistics();
        healthStats.setTotalDevices(stats != null && stats.getTotalCount() != null ? stats.getTotalCount() : 0);
        healthStats.setNormalDevices(stats != null && stats.getOnlineCount() != null ? stats.getOnlineCount() : 0);
        healthStats.setOfflineDevices(stats != null && stats.getOfflineCount() != null ? stats.getOfflineCount() : 0);
        healthStats.setHealthRate(healthStats.getTotalDevices() > 0 ?
            (double) healthStats.getNormalDevices() / healthStats.getTotalDevices() : 0.0);
        return healthStats;
    }
}
