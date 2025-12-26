package net.lab1024.sa.device.comm.decorator;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;

/**
 * 设备命令装饰器基类
 * <p>
 * 使用装饰器模式增强设备命令执行功能
 * 严格遵循CLAUDE.md规范：
 * - 使用装饰器模式实现
 * - 支持功能增强和组合
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public abstract class DeviceCommandDecorator implements IDeviceCommandExecutor {

    /**
     * 被装饰的执行器
     */
    protected IDeviceCommandExecutor delegate;

    /**
     * 构造函数
     *
     * @param delegate 被装饰的执行器
     */
    public DeviceCommandDecorator(IDeviceCommandExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public DeviceCommandResult execute(DeviceCommandRequest command) {
        // 装饰器前置处理
        beforeExecute(command);

        // 执行被装饰的方法
        DeviceCommandResult result = delegate.execute(command);

        // 装饰器后置处理
        afterExecute(command, result);

        return result;
    }

    /**
     * 前置处理(钩子方法)
     * <p>
     * 子类可以覆盖此方法以实现前置增强逻辑
     * </p>
     *
     * @param command 设备命令请求
     */
    protected void beforeExecute(DeviceCommandRequest command) {
        // 默认空实现
    }

    /**
     * 后置处理(钩子方法)
     * <p>
     * 子类可以覆盖此方法以实现后置增强逻辑
     * </p>
     *
     * @param command 设备命令请求
     * @param result 执行结果
     */
    protected void afterExecute(DeviceCommandRequest command, DeviceCommandResult result) {
        // 默认空实现
    }

    /**
     * 设备命令执行结果
     */
    public static class DeviceCommandResult {
        private final boolean success;
        private final String message;
        private final Object data;

        private DeviceCommandResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static DeviceCommandResult success(Object data) {
            return new DeviceCommandResult(true, "执行成功", data);
        }

        public static DeviceCommandResult failed(String message) {
            return new DeviceCommandResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }
}
