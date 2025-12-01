package net.lab1024.sa.admin.module.access.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;
import net.lab1024.sa.base.module.area.domain.entity.AreaAccessExtEntity;
import net.lab1024.sa.admin.module.access.domain.entity.AccessAreaEntity;

/**
 * 门禁区域合并工具类
 * 负责将基础区域实体和门禁区域扩展实体合并为完整的门禁区域实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public class AccessAreaMergeUtil {

    /**
     * 将基础区域列表转换为门禁区域列表
     *
     * @param areaEntities 基础区域实体列表
     * @return 门禁区域实体列表
     */
    public static List<AccessAreaEntity> mergeAreaToAccessArea(List<AreaEntity> areaEntities) {
        if (areaEntities == null || areaEntities.isEmpty()) {
            return List.of();
        }

        return areaEntities.stream()
                .map(area -> {
                    AccessAreaEntity accessArea = new AccessAreaEntity();
                    // 手动复制属性以避免泛型类型推断问题
                    accessArea.setAreaId(area.getAreaId());
                    accessArea.setAreaCode(area.getAreaCode());
                    accessArea.setAreaName(area.getAreaName());
                    accessArea.setParentId(area.getParentId());
                    accessArea.setLevel(area.getLevel());
                    accessArea.setSortOrder(area.getSortOrder());
                    accessArea.setAreaType(area.getAreaType());
                    accessArea.setStatus(area.getStatus());
                    accessArea.setDescription(area.getDescription());
                    accessArea.setCreateTime(area.getCreateTime());
                    accessArea.setUpdateTime(area.getUpdateTime());
                    accessArea.setCreateUserId(area.getCreateUserId());
                    accessArea.setUpdateUserId(area.getUpdateUserId());
                    accessArea.setDeletedFlag(area.getDeletedFlag());
                    return accessArea;
                })
                .collect(Collectors.toList());
    }

    /**
     * 合并基础区域实体和扩展实体为门禁区域实体
     *
     * @param areaEntity 基础区域实体
     * @param extEntity 门禁区域扩展实体
     * @return 合并后的门禁区域实体
     */
    public static AccessAreaEntity mergeWithExtension(AreaEntity areaEntity, AreaAccessExtEntity extEntity) {
        if (areaEntity == null) {
            return null;
        }

        AccessAreaEntity accessAreaEntity = new AccessAreaEntity();

        // 手动复制基础区域属性以避免泛型类型推断问题
        accessAreaEntity.setAreaId(areaEntity.getAreaId());
        accessAreaEntity.setAreaCode(areaEntity.getAreaCode());
        accessAreaEntity.setAreaName(areaEntity.getAreaName());
        accessAreaEntity.setParentId(areaEntity.getParentId());
        accessAreaEntity.setLevel(areaEntity.getLevel());
        accessAreaEntity.setSortOrder(areaEntity.getSortOrder());
        accessAreaEntity.setAreaType(areaEntity.getAreaType());
        accessAreaEntity.setStatus(areaEntity.getStatus());
        accessAreaEntity.setDescription(areaEntity.getDescription());
        accessAreaEntity.setCreateTime(areaEntity.getCreateTime());
        accessAreaEntity.setUpdateTime(areaEntity.getUpdateTime());
        accessAreaEntity.setCreateUserId(areaEntity.getCreateUserId());
        accessAreaEntity.setUpdateUserId(areaEntity.getUpdateUserId());
        accessAreaEntity.setDeletedFlag(areaEntity.getDeletedFlag());

        // 设置扩展属性
        if (extEntity != null) {
            // TODO: 根据实际的AccessAreaEntity字段来设置扩展属性
            // 暂时注释掉可能不存在的字段调用
            // accessAreaEntity.setAccessEnabled(extEntity.getAccessEnabled() != null ? extEntity.getAccessEnabled() : 1);
            // accessAreaEntity.setAccessLevel(extEntity.getAccessLevel() != null ? extEntity.getAccessLevel() : 1);
            // accessAreaEntity.setAccessMode(extEntity.getAccessMode());
            // accessAreaEntity.setSpecialAuthRequired(extEntity.getSpecialAuthRequired() != null ? extEntity.getSpecialAuthRequired() : 0);
            // accessAreaEntity.setValidTimeStart(extEntity.getValidTimeStart());
            // accessAreaEntity.setValidTimeEnd(extEntity.getValidTimeEnd());
            // accessAreaEntity.setValidWeekdays(extEntity.getValidWeekdays());
            // accessAreaEntity.setDeviceCount(extEntity.getDeviceCount() != null ? extEntity.getDeviceCount() : 0);
            // accessAreaEntity.setGuardRequired(extEntity.getGuardRequired() != null ? extEntity.getGuardRequired() : false);
            // accessAreaEntity.setTimeRestrictions(extEntity.getTimeRestrictions());
            // accessAreaEntity.setVisitorAllowed(extEntity.getVisitorAllowed() != null ? extEntity.getVisitorAllowed() : true);
            // accessAreaEntity.setEmergencyAccess(extEntity.getEmergencyAccess() != null ? extEntity.getEmergencyAccess() : false);
            // accessAreaEntity.setMonitoringEnabled(extEntity.getMonitoringEnabled() != null ? extEntity.getMonitoringEnabled() : true);
            // accessAreaEntity.setAlertConfig(extEntity.getAlertConfig());
        }

        return accessAreaEntity;
    }

    /**
     * 批量合并基础区域实体和扩展实体
     *
     * @param areaEntities 基础区域实体列表
     * @param extEntities 扩展实体列表
     * @return 合并后的门禁区域实体列表
     */
    public static List<AccessAreaEntity> batchMergeWithExtension(List<AreaEntity> areaEntities, List<AreaAccessExtEntity> extEntities) {
        if (areaEntities == null || areaEntities.isEmpty()) {
            return List.of();
        }

        // 构建扩展实体映射
        Map<Long, AreaAccessExtEntity> extMap = new HashMap<>();
        if (extEntities != null) {
            extMap = extEntities.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            AreaAccessExtEntity::getAreaId,
                            entity -> entity,
                            (existing, replacement) -> existing
                    ));
        }

        return areaEntities.stream()
                .map(area -> mergeWithExtension(area, extMap.get(area.getAreaId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 将门禁区域实体转换为扩展实体
     *
     * @param accessAreaEntity 门禁区域实体
     * @return 扩展实体
     */
    public static AreaAccessExtEntity convertToExtEntity(AccessAreaEntity accessAreaEntity) {
        if (accessAreaEntity == null) {
            return null;
        }

        AreaAccessExtEntity extEntity = new AreaAccessExtEntity();
        // 只设置基础字段，避免访问可能不存在的方法
        extEntity.setAreaId(accessAreaEntity.getAreaId());
        // TODO: 根据实际的AccessAreaEntity字段来设置扩展属性
        // extEntity.setAccessLevel(accessAreaEntity.getAccessLevel());
        // extEntity.setAccessMode(accessAreaEntity.getAccessMode());
        // 修复boolean/Integer转换问题
        if (accessAreaEntity.getEmergencyAccess() != null) {
            extEntity.setEmergencyAccess(accessAreaEntity.getEmergencyAccess() ? 1 : 0);
        }
        if (accessAreaEntity.getMonitoringEnabled() != null) {
            extEntity.setMonitoringEnabled(accessAreaEntity.getMonitoringEnabled() ? 1 : 0);
        }
        extEntity.setAlertConfig(accessAreaEntity.getAlertConfig());

        return extEntity;
    }

    /**
     * 从门禁区域实体提取扩展字段更新到扩展实体
     *
     * @param accessAreaEntity 门禁区域实体
     * @param extEntity 扩展实体（将被更新）
     * @return 更新后的扩展实体
     */
    public static AreaAccessExtEntity updateExtEntityFromAccessArea(AccessAreaEntity accessAreaEntity, AreaAccessExtEntity extEntity) {
        if (accessAreaEntity == null || extEntity == null) {
            return extEntity;
        }

        // 更新扩展字段
        extEntity.setAreaId(accessAreaEntity.getAreaId());
        // TODO: 根据实际的AccessAreaEntity字段来设置扩展属性
        // 暂时注释掉可能不存在的字段调用
        // extEntity.setAccessLevel(accessAreaEntity.getAccessLevel());
        // extEntity.setAccessMode(accessAreaEntity.getAccessMode());
        // extEntity.setDeviceCount(accessAreaEntity.getDeviceCount());
        // extEntity.setGuardRequired(accessAreaEntity.getGuardRequired() ? 1 : 0);
        // extEntity.setTimeRestrictions(accessAreaEntity.getTimeRestrictions());
        // extEntity.setVisitorAllowed(accessAreaEntity.getVisitorAllowed() ? 1 : 0);

        return extEntity;
    }

    /**
     * 检查区域是否启用门禁
     *
     * @param accessAreaEntity 门禁区域实体
     * @return 是否启用门禁
     */
    public static boolean isAccessEnabled(AccessAreaEntity accessAreaEntity) {
        if (accessAreaEntity == null) {
            return false;
        }
        Boolean accessEnabled = accessAreaEntity.getAccessEnabled();
        return accessEnabled == null || accessEnabled == true;
    }

    /**
     * 获取门禁级别描述
     *
     * @param accessLevel 门禁级别
     * @return 级别描述
     */
    public static String getAccessLevelDesc(Integer accessLevel) {
        if (accessLevel == null) {
            return "未知";
        }
        return switch (accessLevel) {
            case 1 -> "普通";
            case 2 -> "重要";
            case 3 -> "核心";
            default -> "未知";
        };
    }

    /**
     * 解析门禁模式
     *
     * @param accessMode 门禁模式字符串
     * @return 门禁模式数组
     */
    public static String[] parseAccessMode(String accessMode) {
        if (!StringUtils.hasText(accessMode)) {
            return new String[0];
        }
        return accessMode.split("[,，、]");
    }

    /**
     * 格式化门禁模式
     *
     * @param accessModes 门禁模式数组
     * @return 格式化的门禁模式字符串
     */
    public static String formatAccessMode(String[] accessModes) {
        if (accessModes == null || accessModes.length == 0) {
            return "";
        }
        return String.join(",", accessModes);
    }
}