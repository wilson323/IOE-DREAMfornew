package net.lab1024.sa.device.comm.decorator.impl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import net.lab1024.sa.device.comm.decorator.DeviceCommandDecorator;
import net.lab1024.sa.device.comm.decorator.IDeviceCommandExecutor;
import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;

/**
 * 日志命令装饰器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 为设备命令执行添加详细的日志记录，便于问题追踪和性能分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
@Slf4j
public class LoggingCommandDecorator extends DeviceCommandDecorator {

    /**
     * 构造函数
     *
     * @param delegate 被装饰的执行器
     */
    public LoggingCommandDecorator(IDeviceCommandExecutor delegate) {
        super(delegate);
    }

    @Override
    protected void beforeExecute(DeviceCommandRequest command) {
        log.info("[日志装饰器] 开始执行设备命令, deviceId={}, commandType={}, commandData={}",
                command.getDeviceId(), command.getCommandType(), command.getCommandData());
    }

    @Override
    protected void afterExecute(DeviceCommandRequest command, DeviceCommandDecorator.DeviceCommandResult result) {
        if (result.isSuccess()) {
            log.info("[日志装饰器] 设备命令执行成功, deviceId={}, commandType={}, response={}",
                    command.getDeviceId(), command.getCommandType(), result.getData());
        } else {
            log.error("[日志装饰器] 设备命令执行失败, deviceId={}, commandType={}, error={}",
                    command.getDeviceId(), command.getCommandType(), result.getMessage());
        }
    }

    @Override
    public DeviceCommandDecorator.DeviceCommandResult execute(DeviceCommandRequest command) {
        long startTime = System.currentTimeMillis();

        // 执行命令
        DeviceCommandDecorator.DeviceCommandResult result = super.execute(command);

        // 记录执行时间
        long duration = System.currentTimeMillis() - startTime;
        log.debug("[日志装饰器] 设备命令执行耗时, deviceId={}, commandType={}, duration={}ms",
                command.getDeviceId(), command.getCommandType(), duration);

        return result;
    }
}
