package net.lab1024.sa.biometric.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 区域权限变更事件
 * <p>
 * 用于通知权限变更，触发模板同步
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaPermissionChangeEvent {

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
