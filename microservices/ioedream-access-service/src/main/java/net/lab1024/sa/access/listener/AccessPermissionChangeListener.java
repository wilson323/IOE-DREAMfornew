package net.lab1024.sa.access.listener;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.AccessPermissionSyncService;
import net.lab1024.sa.common.organization.service.impl.AreaPermissionServiceImpl;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 门禁权限变更监听器
 * <p>
 * 监听权限变更事件，自动同步/删除权限到设备
 * 严格遵循CLAUDE.md规范:
 * - 使用@EventListener监听事件
 * - 使用@Async异步处理
 * - 完整的错误处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class AccessPermissionChangeListener {

    @Resource
    private AccessPermissionSyncService accessPermissionSyncService;

    /**
     * 监听权限变更事件
     * <p>
     * 监听来自AreaPermissionServiceImpl发布的PermissionChangeEvent事件
     * 当用户新增区域权限时，自动同步权限到该区域设备
     * 当用户移除区域权限时，自动从该区域设备删除权限
     * </p>
     */
    @Async("permissionSyncExecutor")
    @EventListener
    public void handlePermissionChange(AreaPermissionServiceImpl.PermissionChangeEvent event) {
        try {
            log.info("[权限变更监听] 处理权限变更事件 userId={}, areaId={}, changeType={}",
                    event.getUserId(), event.getAreaId(), event.getChangeType());

            if ("ADDED".equals(event.getChangeType())) {
                // 新增权限 → 同步权限到该区域设备
                accessPermissionSyncService.syncPermissionToDevices(event.getUserId(), event.getAreaId());
            } else if ("REMOVED".equals(event.getChangeType())) {
                // 移除权限 → 从该区域设备删除权限
                accessPermissionSyncService.removePermissionFromDevices(event.getUserId(), event.getAreaId());
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
