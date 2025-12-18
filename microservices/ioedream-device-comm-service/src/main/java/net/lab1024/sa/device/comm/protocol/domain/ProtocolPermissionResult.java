package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议权限验证结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolPermissionResult {

    /**
     * 权限验证是否通过
     */
    private boolean permitted;

    /**
     * 是否允许（别名）
     */
    private boolean allowed;

    /**
     * 是否有权限（别名）
     */
    private boolean hasPermission;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 拒绝原因
     */
    private String denyReason;

    /**
     * 权限级别
     */
    private String permissionLevel;

    /**
     * 权限详情
     */
    private String permissionDetails;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    public static ProtocolPermissionResult permit() {
        return ProtocolPermissionResult.builder()
                .permitted(true)
                .build();
    }

    public static ProtocolPermissionResult deny(String reason) {
        return ProtocolPermissionResult.builder()
                .permitted(false)
                .denyReason(reason)
                .build();
    }
}
