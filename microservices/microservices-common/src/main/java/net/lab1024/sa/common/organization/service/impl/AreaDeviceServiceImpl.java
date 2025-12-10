package net.lab1024.sa.common.organization.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.manager.AreaDeviceManager;
import net.lab1024.sa.common.organization.service.AreaDeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public AreaDeviceEntity addDeviceToArea(Long areaId, String deviceId, String deviceCode,
                                           String deviceName, Integer deviceType, String businessModule) {
        log.info("[区域设备服务] 添加设备到区域: areaId={}, deviceId={}, deviceType={}, module={}",
                areaId, deviceId, deviceType, businessModule);

        try {
            return areaDeviceManager.addDeviceToArea(areaId, deviceId, deviceCode, deviceName, deviceType, businessModule);
        } catch (Exception e) {
            log.error("[区域设备服务] 添加设备到区域失败: areaId={}, deviceId={}, error={}",
                    areaId, deviceId, e.getMessage(), e);
            throw new RuntimeException("添加设备到区域失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeDeviceFromArea(Long areaId, String deviceId) {
        log.info("[区域设备服务] 移除区域中的设备: areaId={}, deviceId={}", areaId, deviceId);

        try {
            return areaDeviceManager.removeDeviceFromArea(areaId, deviceId);
        } catch (Exception e) {
            log.error("[区域设备服务] 移除区域中的设备失败: areaId={}, deviceId={}, error={}",
                    areaId, deviceId, e.getMessage(), e);
            throw new RuntimeException("移除设备失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaDeviceEntity> getAreaDevices(Long areaId) {
        log.debug("[区域设备服务] 获取区域设备: areaId={}", areaId);

        try {
            return areaDeviceManager.getAreaDevices(areaId);
        } catch (Exception e) {
            log.error("[区域设备服务] 获取区域设备失败: areaId={}, error={}", areaId, e.getMessage(), e);
            throw new RuntimeException("获取区域设备失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaDeviceEntity> getAreaDevicesByModule(Long areaId, String businessModule) {
        log.debug("[区域设备服务] 获取区域业务模块设备: areaId={}, module={}", areaId, businessModule);

        try {
            return areaDeviceManager.getAreaDevicesByModule(areaId, businessModule);
        } catch (Exception e) {
            log.error("[区域设备服务] 获取区域业务模块设备失败: areaId={}, module={}, error={}",
                    areaId, businessModule, e.getMessage(), e);
            throw new RuntimeException("获取区域业务模块设备失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaDeviceEntity> getUserAccessibleDevices(Long userId, String businessModule) {
        log.debug("[区域设备服务] 获取用户可访问设备: userId={}, module={}", userId, businessModule);

        try {
            return areaDeviceManager.getUserAccessibleDevices(userId, businessModule);
        } catch (Exception e) {
            log.error("[区域设备服务] 获取用户可访问设备失败: userId={}, module={}, error={}",
                    userId, businessModule, e.getMessage(), e);
            throw new RuntimeException("获取用户可访问设备失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeviceInArea(Long areaId, String deviceId) {
        log.debug("[区域设备服务] 检查设备是否在区域: areaId={}, deviceId={}", areaId, deviceId);

        try {
            return areaDeviceManager.isDeviceInArea(areaId, deviceId);
        } catch (Exception e) {
            log.error("[区域设备服务] 检查设备区域关联失败: areaId={}, deviceId={}, error={}",
                    areaId, deviceId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDeviceBusinessAttributes(String deviceId, Long areaId, Map<String, Object> businessAttributes) {
        log.info("[区域设备服务] 设置设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        try {
            areaDeviceManager.setDeviceBusinessAttributes(deviceId, areaId, businessAttributes);
        } catch (Exception e) {
            log.error("[区域设备服务] 设置设备业务属性失败: deviceId={}, areaId={}, error={}",
                    deviceId, areaId, e.getMessage(), e);
            throw new RuntimeException("设置设备业务属性失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDeviceBusinessAttributes(String deviceId, Long areaId) {
        log.debug("[区域设备服务] 获取设备业务属性: deviceId={}, areaId={}", deviceId, areaId);

        try {
            return areaDeviceManager.getDeviceBusinessAttributes(deviceId, areaId);
        } catch (Exception e) {
            log.error("[区域设备服务] 获取设备业务属性失败: deviceId={}, areaId={}, error={}",
                    deviceId, areaId, e.getMessage(), e);
            return Map.of();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceRelationStatus(String relationId, Integer relationStatus) {
        log.info("[区域设备服务] 更新设备状态: relationId={}, status={}", relationId, relationStatus);

        try {
            areaDeviceManager.updateDeviceRelationStatus(relationId, relationStatus);
        } catch (Exception e) {
            log.error("[区域设备服务] 更新设备状态失败: relationId={}, status={}, error={}",
                    relationId, relationStatus, e.getMessage(), e);
            throw new RuntimeException("更新设备状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAreaDeviceStatistics(Long areaId) {
        log.debug("[区域设备服务] 获取区域设备统计: areaId={}", areaId);

        try {
            return areaDeviceManager.getAreaDeviceStatistics(areaId);
        } catch (Exception e) {
            log.error("[区域设备服务] 获取区域设备统计失败: areaId={}, error={}", areaId, e.getMessage(), e);
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
    @Transactional(readOnly = true)
    public Map<String, Object> getDeviceAttributeTemplate(Integer deviceType, Integer deviceSubType) {
        log.debug("[区域设备服务] 获取设备属性模板: deviceType={}, deviceSubType={}", deviceType, deviceSubType);

        try {
            return areaDeviceManager.getDeviceAttributeTemplate(deviceType, deviceSubType);
        } catch (Exception e) {
            log.error("[区域设备服务] 获取设备属性模板失败: deviceType={}, deviceSubType={}, error={}",
                    deviceType, deviceSubType, e.getMessage(), e);
            return Map.of();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncAreaUserPermissionsToDevice(Long areaId, String deviceId) {
        log.info("[区域设备服务] 同步区域用户权限到设备: areaId={}, deviceId={}", areaId, deviceId);

        try {
            areaDeviceManager.syncAreaUserPermissionsToDevice(areaId, deviceId);
        } catch (Exception e) {
            log.error("[区域设备服务] 同步区域用户权限到设备失败: areaId={}, deviceId={}, error={}",
                    areaId, deviceId, e.getMessage(), e);
            throw new RuntimeException("同步用户权限失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasDeviceAccessPermission(Long userId, Long areaId, String deviceId) {
        log.debug("[区域设备服务] 检查设备访问权限: userId={}, areaId={}, deviceId={}", userId, areaId, deviceId);

        try {
            return areaDeviceManager.hasDeviceAccessPermission(userId, areaId, deviceId);
        } catch (Exception e) {
            log.error("[区域设备服务] 检查设备访问权限失败: userId={}, areaId={}, deviceId={}, error={}",
                    userId, areaId, deviceId, e.getMessage(), e);
            return false;
        }
    }
}
