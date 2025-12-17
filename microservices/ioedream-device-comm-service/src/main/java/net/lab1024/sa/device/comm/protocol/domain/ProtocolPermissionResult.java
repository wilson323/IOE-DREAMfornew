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
