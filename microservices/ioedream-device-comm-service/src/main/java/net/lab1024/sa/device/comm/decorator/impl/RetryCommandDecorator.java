package net.lab1024.sa.device.comm.decorator.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import net.lab1024.sa.device.comm.decorator.DeviceCommandDecorator;
import net.lab1024.sa.device.comm.decorator.IDeviceCommandExecutor;
import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;

/**
 * 重试命令装饰器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 为设备命令执行添加重试机制，提高命令执行成功率
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
@Slf4j
public class RetryCommandDecorator extends DeviceCommandDecorator {

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 重试延迟（毫秒）
     */
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 构造函数
     *
     * @param delegate 被装饰的执行器
     */
    public RetryCommandDecorator(IDeviceCommandExecutor delegate) {
        super(delegate);
    }

    @Override
    public DeviceCommandDecorator.DeviceCommandResult execute(DeviceCommandRequest command) {
        int retryCount = 0;
        DeviceCommandDecorator.DeviceCommandResult result = null;

        while (retryCount <= MAX_RETRY_COUNT) {
            try {
                result = super.execute(command);

                // 如果执行成功，直接返回
                if (result.isSuccess()) {
                    if (retryCount > 0) {
                        log.info("[重试装饰器] 命令执行成功（重试{}次）, deviceId={}, commandType={}",
                                retryCount, command.getDeviceId(), command.getCommandType());
                    }
                    return result;
                }

                // 如果执行失败且还有重试机会，则重试
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++;
                    log.warn("[重试装饰器] 命令执行失败，准备重试（{}/{}）, deviceId={}, commandType={}, error={}",
                            retryCount, MAX_RETRY_COUNT, command.getDeviceId(), command.getCommandType(),
                            result.getMessage());

                    // 延迟后重试
                    try {
                        TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("[重试装饰器] 重试延迟被中断", e);
                        return DeviceCommandDecorator.DeviceCommandResult.failed("重试被中断");
                    }
                } else {
                    // 达到最大重试次数，返回失败
                    log.error("[重试装饰器] 命令执行失败（已重试{}次）, deviceId={}, commandType={}, error={}",
                            MAX_RETRY_COUNT, command.getDeviceId(), command.getCommandType(), result.getMessage());
                    return result;
                }
            } catch (Exception e) {
                // 异常情况，如果还有重试机会则重试
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++;
                    log.warn("[重试装饰器] 命令执行异常，准备重试（{}/{}）, deviceId={}, commandType={}, error={}",
                            retryCount, MAX_RETRY_COUNT, command.getDeviceId(), command.getCommandType(),
                            e.getMessage());

                    try {
                        TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[重试装饰器] 重试延迟被中断", ie);
                        return DeviceCommandDecorator.DeviceCommandResult.failed("重试被中断: " + e.getMessage());
                    }
                } else {
                    // 达到最大重试次数，返回失败
                    log.error("[重试装饰器] 命令执行异常（已重试{}次）, deviceId={}, commandType={}",
                            MAX_RETRY_COUNT, command.getDeviceId(), command.getCommandType(), e);
                    return DeviceCommandDecorator.DeviceCommandResult.failed("执行异常: " + e.getMessage());
                }
            }
        }

        // 理论上不会到达这里
        return result != null ? result : DeviceCommandDecorator.DeviceCommandResult.failed("未知错误");
    }
}
