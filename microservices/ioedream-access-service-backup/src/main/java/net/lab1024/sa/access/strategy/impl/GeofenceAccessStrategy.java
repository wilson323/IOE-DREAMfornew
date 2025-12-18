package net.lab1024.sa.access.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessRequest;
import net.lab1024.sa.access.strategy.IAccessPermissionStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.dao.AreaDao;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 基于地理围栏的门禁权限策略
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 验证用户是否在允许的地理围栏范围内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
@StrategyMarker(name = "GEOFENCE", type = "ACCESS_PERMISSION", priority = 90)
public class GeofenceAccessStrategy implements IAccessPermissionStrategy {

    @Resource
    private AreaDao areaDao;

    @Override
    public boolean hasPermission(AccessRequest request) {
        // 查询区域信息
        AreaEntity area = areaDao.selectById(request.getAreaId());
        if (area == null) {
            log.debug("[地理围栏策略] 区域{}不存在", request.getAreaId());
            return false;
        }

        // 如果区域没有配置地理围栏，则允许通行
        if (area.getGeofenceConfig() == null || area.getGeofenceConfig().isEmpty()) {
            log.debug("[地理围栏策略] 区域{}未配置地理围栏，默认允许", request.getAreaId());
            return true;
        }

        // 检查用户位置是否在围栏范围内
        // TODO: 实现GPS位置验证逻辑
        // 1. 获取用户当前位置（从请求中获取或通过GPS服务获取）
        // 2. 解析区域地理围栏配置（经纬度范围）
        // 3. 判断用户位置是否在围栏内

        // 临时实现：如果请求中包含位置信息，则验证
        if (request.getLocationLatitude() != null && request.getLocationLongitude() != null) {
            boolean inGeofence = checkLocationInGeofence(
                    request.getLocationLatitude(),
                    request.getLocationLongitude(),
                    area.getGeofenceConfig()
            );
            log.debug("[地理围栏策略] 用户{}位置({}, {})，区域{}围栏验证结果：{}",
                    request.getUserId(), request.getLocationLatitude(), request.getLocationLongitude(),
                    request.getAreaId(), inGeofence);
            return inGeofence;
        }

        // 如果没有位置信息，默认允许（实际场景中可能需要强制要求位置信息）
        log.debug("[地理围栏策略] 用户{}无位置信息，默认允许", request.getUserId());
        return true;
    }

    /**
     * 检查位置是否在围栏内
     * <p>
     * TODO: 实现具体的地理围栏判断逻辑
     * </p>
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param geofenceConfig 围栏配置（JSON格式）
     * @return 是否在围栏内
     */
    private boolean checkLocationInGeofence(Double latitude, Double longitude, String geofenceConfig) {
        // TODO: 解析geofenceConfig（可能包含圆形围栏、多边形围栏等）
        // 临时实现：返回true
        return true;
    }

    @Override
    public int getPriority() {
        return 90; // 地理围栏策略优先级中等
    }

    @Override
    public String getStrategyType() {
        return "GEOFENCE";
    }
}
