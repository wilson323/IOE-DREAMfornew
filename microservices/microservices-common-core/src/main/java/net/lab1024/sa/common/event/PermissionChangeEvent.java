package net.lab1024.sa.common.event;

/**
 * 权限变更事件
 * <p>
 * 用于通知其他服务（如门禁服务、生物识别服务）权限变更
 * 跨服务事件应该在公共模块定义，避免服务间直接依赖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class PermissionChangeEvent {
    private final Long userId;
    private final Long areaId;
    private final String changeType; // "ADDED" 或 "REMOVED"

    /**
     * 构造函数
     *
     * @param userId     用户ID
     * @param areaId     区域ID
     * @param changeType 变更类型（"ADDED" 或 "REMOVED"）
     */
    public PermissionChangeEvent(Long userId, Long areaId, String changeType) {
        this.userId = userId;
        this.areaId = areaId;
        this.changeType = changeType;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAreaId() {
        return areaId;
    }

    public String getChangeType() {
        return changeType;
    }
}
