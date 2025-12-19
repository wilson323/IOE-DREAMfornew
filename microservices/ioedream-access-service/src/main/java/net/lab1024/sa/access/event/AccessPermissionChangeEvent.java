package net.lab1024.sa.access.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁权限变更事件
 * <p>
 * 用于通知权限变更，触发权限同步到设备
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessPermissionChangeEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 变更类型
     */
    private ChangeType changeType;

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        /**
         * 新增权限
         */
        ADDED,

        /**
         * 移除权限
         */
        REMOVED
    }
}
