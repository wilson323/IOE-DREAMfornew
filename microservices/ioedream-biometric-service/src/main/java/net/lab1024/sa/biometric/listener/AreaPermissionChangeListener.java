package net.lab1024.sa.biometric.listener;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.event.AreaPermissionChangeEvent;
import net.lab1024.sa.biometric.service.BiometricTemplateSyncService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 区域权限变更监听器
 * <p>
 * 监听区域权限变更事件，自动同步/删除生物模板
 * 严格遵循CLAUDE.md规范:
 * - 使用@EventListener监听事件
 * - 使用@Async异步处理
 * - 完整的错误处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class AreaPermissionChangeListener {

    @Resource
    private BiometricTemplateSyncService biometricTemplateSyncService;

    /**
     * 监听区域权限变更事件
     * <p>
     * 当用户新增区域权限时，自动同步模板到该区域设备
     * 当用户移除区域权限时，自动从该区域设备删除模板
     * </p>
     */
    @Async("permissionSyncExecutor")
    @EventListener
    public void handleAreaPermissionChange(AreaPermissionChangeEvent event) {
        try {
            log.info("[权限变更监听] 处理权限变更事件 userId={}, areaId={}, changeType={}",
                    event.getUserId(), event.getAreaId(), event.getChangeType());

            if (event.getChangeType() == AreaPermissionChangeEvent.ChangeType.ADDED) {
                // 新增权限 → 同步模板到该区域设备
                biometricTemplateSyncService.syncOnPermissionAdded(event.getUserId(), event.getAreaId());
            } else if (event.getChangeType() == AreaPermissionChangeEvent.ChangeType.REMOVED) {
                // 移除权限 → 从该区域设备删除模板
                biometricTemplateSyncService.deleteOnPermissionRemoved(event.getUserId(), event.getAreaId());
            }

            log.info("[权限变更监听] 权限变更处理完成 userId={}, areaId={}, changeType={}",
                    event.getUserId(), event.getAreaId(), event.getChangeType());

        } catch (Exception e) {
            log.error("[权限变更监听] 处理权限变更事件失败 userId={}, areaId={}, changeType={}",
                    event.getUserId(), event.getAreaId(), event.getChangeType(), e);
            // 不抛出异常，避免影响主流程
        }
    }
}
