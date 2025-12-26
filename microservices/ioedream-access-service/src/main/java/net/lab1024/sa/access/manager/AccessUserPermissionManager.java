package net.lab1024.sa.access.manager;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessUserPermissionDao;
import net.lab1024.sa.access.domain.entity.AccessUserPermissionEntity;

/**
 * 门禁设备权限管理器
 * <p>
 * 独立负责门禁设备权限的有效性判定，避免与软件权限混用。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Slf4j
public class AccessUserPermissionManager {

    // 显式添加logger声明以确保编译通过

    private final AccessUserPermissionDao accessUserPermissionDao;

    public AccessUserPermissionManager(AccessUserPermissionDao accessUserPermissionDao) {
        this.accessUserPermissionDao = accessUserPermissionDao;
    }

    public AccessUserPermissionEntity getValidPermission(Long userId, Long areaId) {
        AccessUserPermissionEntity permission = accessUserPermissionDao.selectByUserAndArea(userId, areaId);
        if (permission == null) {
            return null;
        }
        if (permission.getDeletedFlag() != null && permission.getDeletedFlag() == 1) {
            return null;
        }
        if (permission.getPermissionStatus() != null && permission.getPermissionStatus() != 1) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (permission.getStartTime() != null && now.isBefore(permission.getStartTime())) {
            return null;
        }
        if (permission.getEndTime() != null && now.isAfter(permission.getEndTime())) {
            return null;
        }
        return permission;
    }
}
