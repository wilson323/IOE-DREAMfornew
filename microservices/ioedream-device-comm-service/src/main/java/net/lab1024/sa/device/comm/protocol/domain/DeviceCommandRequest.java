package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 设备命令请求
 * <p>
 * 用于设备命令执行的请求对象
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
public class DeviceCommandRequest {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 命令类型
     */
    private String commandType;

    /**
     * 命令数据
     */
    private Map<String, Object> commandData;

    /**
     * 超时时间（毫秒）
     */
    private Long timeout;

    /**
     * 是否异步执行
     */
    private Boolean async;
}
