package net.lab1024.sa.common.organization.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.service.AreaDeviceManager;
import net.lab1024.sa.common.organization.service.AreaDeviceService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 区域设备服务实现类
 * <p>
 * 企业级区域设备管理服务实现
 * 严格遵循CLAUDE.md全局架构规范：
 * - 使用@Service注解标识服务层
 * - 使用@Resource进行依赖注入
 * - 事务管理注解使用
 * - 调用Manager层进行业务处理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AreaDeviceServiceImpl implements AreaDeviceService {

    @Resource
    private AreaDeviceManager areaDeviceManager;

    @Override
    @Observed(name = "areaDevice.addDeviceToArea", contextualName = "area-device-add")
    @Transactional(rollbackFor = Exception.class)
    public AreaDeviceEntity addDeviceToArea(Long areaId, String deviceId, String deviceCode,
                                           String deviceName, Integer deviceType, String businessModule) {
        log.info("[区域设备服务] 添加设备到区域: areaId={}, deviceId={}, deviceType={}, module={}",
                areaId, deviceId, deviceType, businessModule);

        try {
            boolean success = areaDeviceManager.addDeviceToArea(areaId, deviceId, deviceCode, deviceName, deviceType, businessModule);
            if (success) {
                // 构造返回实体
                AreaDeviceEntity entity = new AreaDeviceEntity();
                entity.setAreaId(areaId);
                entity.setDeviceId(deviceId);
                entity.setDeviceCode(deviceCode);
                entity.setDeviceName(deviceName);
                entity.setDeviceType(deviceType);
                entity.setBusinessModule(businessModule);
                return entity;
            }
            return null;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 添加设备到区域参数异常: areaId={}, deviceId={}, error={}", areaId, deviceId, e.getMessage());
            throw new ParamException("AREA_DEVICE_ADD_PARAM_ERROR", "添加设备到区域参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[区域设备服务] 添加设备到区域系统异常: areaId={}, deviceId={}", areaId, deviceId, e);
            throw new SystemException("AREA_DEVICE_ADD_SYSTEM_ERROR", "添加设备到区域系统异常", e);
        }
    }

    @Override
    @Observed(name = "areaDevice.removeDeviceFromArea", contextualName = "area-device-remove")
    @Transactional(rollbackFor = Exception.class)
    public boolean removeDeviceFromArea(Long areaId, String deviceId) {
        log.info("[区域设备服务] 移除区域中的设备: areaId={}, deviceId={}", areaId, deviceId);

        try {
            return areaDeviceManager.removeDeviceFromArea(areaId, deviceId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 移除区域中的设备参数异常: areaId={}, deviceId={}, error={}", areaId, deviceId, e.getMessage());
            throw new ParamException("AREA_DEVICE_REMOVE_PARAM_ERROR", "移除设备参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[区域设备服务] 移除区域中的设备系统异常: areaId={}, deviceId={}", areaId, deviceId, e);
            throw new SystemException("AREA_DEVICE_REMOVE_SYSTEM_ERROR", "移除设备系统异常", e);
        }
    }

    @Override
    @Observed(name = "areaDevice.getAreaDevices", contextualName = "area-device-get")
    @Transactional(readOnly = true)
    public List<AreaDeviceEntity> getAreaDevices(Long areaId) {
        log.debug("[区域设备服务] 获取区域设备: areaId={}", areaId);

        try {
            return areaDeviceManager.getAreaDevices(areaId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 获取区域设备参数异常: areaId={}, error={}", areaId, e.getMessage());
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            log.error("[区域设备服务] 获取区域设备系统异常: areaId={}", areaId, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    @Observed(name = "areaDevice.getAreaDevicesByModule", contextualName = "area-device-get-by-module")
    @Transactional(readOnly = true)
    public List<AreaDeviceEntity> getAreaDevicesByModule(Long areaId, String businessModule) {
        log.debug("[区域设备服务] 获取区域业务模块设备: areaId={}, module={}", areaId, businessModule);

        try {
            return areaDeviceManager.getAreaDevicesByModule(areaId, businessModule);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 获取区域业务模块设备参数异常: areaId={}, module={}, error={}", areaId, businessModule, e.getMessage());
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            log.error("[区域设备服务] 获取区域业务模块设备系统异常: areaId={}, module={}", areaId, businessModule, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    @Observed(name = "areaDevice.getUserAccessibleDevices", contextualName = "area-device-get-user-accessible")
    @Transactional(readOnly = true)
    public List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule) {
        log.debug("[区域设备服务] 获取用户可访问设备: userId={}, module={}", userId, businessModule);

        try {
            return areaDeviceManager.getUserAccessibleDevices(userId, businessModule);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 获取用户可访问设备参数异常: userId={}, module={}, error={}", userId, businessModule, e.getMessage());
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            log.error("[区域设备服务] 获取用户可访问设备系统异常: userId={}, module={}", userId, businessModule, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    @Observed(name = "areaDevice.isDeviceInArea", contextualName = "area-device-is-in-area")
    @Transactional(readOnly = true)
    public boolean isDeviceInArea(Long areaId, String deviceId) {
        log.debug("[区域设备服务] 检查设备是否在区域: areaId={}, deviceId={}", areaId, deviceId);

        try {
            return areaDeviceManager.isDeviceInArea(areaId, deviceId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 检查设备区域关联参数异常: areaId={}, deviceId={}, error={}", areaId, deviceId, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[区域设备服务] 检查设备区域关联系统异常: areaId={}, deviceId={}", areaId, deviceId, e);
            return false;
        }
    }

    @Override
    @Observed(name = "areaDevice.setDeviceBusinessAttributes", contextualName = "area-device-set-attributes")
    @Transactional(rollbackFor = Exception.class)
    public void setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> businessAttributes) {
        log.info("[区域设备服务] 设置设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        try {
            areaDeviceManager.setDeviceBusinessAttributes(deviceId, areaId, businessAttributes);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 设置设备业务属性参数异常: deviceId={}, areaId={}, error={}", deviceId, areaId, e.getMessage());
            throw new ParamException("AREA_DEVICE_SET_ATTR_PARAM_ERROR", "设置设备业务属性参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[区域设备服务] 设置设备业务属性系统异常: deviceId={}, areaId={}", deviceId, areaId, e);
            throw new SystemException("AREA_DEVICE_SET_ATTR_SYSTEM_ERROR", "设置设备业务属性系统异常", e);
        }
    }

    @Override
    @Observed(name = "areaDevice.getDeviceBusinessAttributes", contextualName = "area-device-get-attributes")
    @Transactional(readOnly = true)
    public Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId) {
        log.debug("[区域设备服务] 获取设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        try {
            return areaDeviceManager.getDeviceBusinessAttributes(deviceId, areaId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 获取设备业务属性参数异常: deviceId={}, areaId={}, error={}", deviceId, areaId, e.getMessage());
            return Map.of();
        } catch (Exception e) {
            log.error("[区域设备服务] 获取设备业务属性系统异常: deviceId={}, areaId={}", deviceId, areaId, e);
            return Map.of();
        }
    }

    @Override
    @Observed(name = "areaDevice.updateDeviceRelationStatus", contextualName = "area-device-update-status")
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceRelationStatus(String relationId, Integer relationStatus) {
        log.info("[区域设备服务] 更新设备状态: relationId={}, status={}", relationId, relationStatus);

        try {
            areaDeviceManager.updateDeviceRelationStatus(relationId, relationStatus);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 更新设备状态参数异常: relationId={}, status={}, error={}", relationId, relationStatus, e.getMessage());
            throw new ParamException("AREA_DEVICE_UPDATE_STATUS_PARAM_ERROR", "更新设备状态参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[区域设备服务] 更新设备状态系统异常: relationId={}, status={}", relationId, relationStatus, e);
            throw new SystemException("AREA_DEVICE_UPDATE_STATUS_SYSTEM_ERROR", "更新设备状态系统异常", e);
        }
    }

    @Override
    @Observed(name = "areaDevice.getAreaDeviceStatistics", contextualName = "area-device-get-statistics")
    @Transactional(readOnly = true)
    public Map<String, Object> getAreaDeviceStatistics(Long areaId) {
        log.debug("[区域设备服务] 获取区域设备统计: areaId={}", areaId);

        try {
            return areaDeviceManager.getAreaDeviceStatistics(areaId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 获取区域设备统计参数异常: areaId={}, error={}", areaId, e.getMessage());
            return Map.of(
                "totalCount", 0,
                "onlineCount", 0,
                "offlineCount", 0,
                "onlineRate", 0.0,
                "deviceTypeCount", Map.of()
            );
        } catch (Exception e) {
            log.error("[区域设备服务] 获取区域设备统计系统异常: areaId={}", areaId, e);
            return Map.of(
                "totalCount", 0,
                "onlineCount", 0,
                "offlineCount", 0,
                "onlineRate", 0.0,
                "deviceTypeCount", Map.of()
            );
        }
    }

    @Override
    @Observed(name = "areaDevice.getDeviceAttributeTemplate", contextualName = "area-device-get-template")
    @Transactional(readOnly = true)
    public Map<String, Object> getDeviceAttributeTemplate(Integer deviceType, Integer deviceSubType) {
        log.debug("[区域设备服务] 获取设备属性模板: deviceType={}, deviceSubType={}", deviceType, deviceSubType);

        try {
            return areaDeviceManager.getDeviceAttributeTemplate(deviceType, deviceSubType);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 获取设备属性模板参数异常: deviceType={}, deviceSubType={}, error={}", deviceType, deviceSubType, e.getMessage());
            return Map.of();
        } catch (Exception e) {
            log.error("[区域设备服务] 获取设备属性模板系统异常: deviceType={}, deviceSubType={}", deviceType, deviceSubType, e);
            return Map.of();
        }
    }

    @Override
    @Observed(name = "areaDevice.syncAreaUserPermissionsToDevice", contextualName = "area-device-sync-permissions")
    @Transactional(rollbackFor = Exception.class)
    public void syncAreaUserPermissionsToDevice(Long areaId, String deviceId) {
        log.info("[区域设备服务] 同步区域用户权限到设备: areaId={}, deviceId={}", areaId, deviceId);

        try {
            areaDeviceManager.syncAreaUserPermissionsToDevice(areaId, deviceId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 同步区域用户权限到设备参数异常: areaId={}, deviceId={}, error={}", areaId, deviceId, e.getMessage());
            throw new ParamException("AREA_DEVICE_SYNC_PERMISSION_PARAM_ERROR", "同步用户权限参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[区域设备服务] 同步区域用户权限到设备系统异常: areaId={}, deviceId={}", areaId, deviceId, e);
            throw new SystemException("AREA_DEVICE_SYNC_PERMISSION_SYSTEM_ERROR", "同步用户权限系统异常", e);
        }
    }

    @Override
    @Observed(name = "areaDevice.hasDeviceAccessPermission", contextualName = "area-device-has-permission")
    @Transactional(readOnly = true)
    public boolean hasDeviceAccessPermission(Long userId, Long areaId, String deviceId) {
        log.debug("[区域设备服务] 检查设备访问权限: userId={}, areaId={}, deviceId={}", userId, areaId, deviceId);

        try {
            return areaDeviceManager.hasDeviceAccessPermission(userId, areaId, deviceId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域设备服务] 检查设备访问权限参数异常: userId={}, areaId={}, deviceId={}, error={}", userId, areaId, deviceId, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[区域设备服务] 检查设备访问权限系统异常: userId={}, areaId={}, deviceId={}", userId, areaId, deviceId, e);
            return false;
        }
    }
}
