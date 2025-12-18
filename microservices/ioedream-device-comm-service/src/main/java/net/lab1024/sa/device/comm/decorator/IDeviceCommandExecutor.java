package net.lab1024.sa.device.comm.decorator;

import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;

/**
 * 设备命令执行器接口
 * <p>
 * 使用装饰器模式增强设备命令执行功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface IDeviceCommandExecutor {

    /**
     * 执行设备命令
     *
     * @param command 设备命令请求
     * @return 执行结果
     */
    DeviceCommandDecorator.DeviceCommandResult execute(DeviceCommandRequest command);
}
