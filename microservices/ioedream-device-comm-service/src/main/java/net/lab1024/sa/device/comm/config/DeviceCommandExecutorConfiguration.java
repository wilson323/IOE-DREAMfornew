package net.lab1024.sa.device.comm.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.decorator.IDeviceCommandExecutor;
import net.lab1024.sa.device.comm.decorator.impl.BasicCommandExecutor;
import net.lab1024.sa.device.comm.decorator.impl.LoggingCommandDecorator;
import net.lab1024.sa.device.comm.decorator.impl.RetryCommandDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * 设备命令执行器配置
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用装饰器模式组装命令执行器链
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Configuration
public class DeviceCommandExecutorConfiguration {

    @Resource
    private BasicCommandExecutor basicCommandExecutor;

    /**
     * 配置设备命令执行器（装饰器组装）
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 装饰器顺序：基础执行器 -> 重试装饰器 -> 日志装饰器
     * </p>
     *
     * @return 组装后的命令执行器
     */
    @Bean
    public IDeviceCommandExecutor deviceCommandExecutor() {
        log.info("[设备命令执行器配置] 开始组装命令执行器装饰器链");

        // 基础执行器
        IDeviceCommandExecutor executor = basicCommandExecutor;

        // 重试装饰器
        executor = new RetryCommandDecorator(executor);

        // 日志装饰器（最外层，记录完整执行过程）
        executor = new LoggingCommandDecorator(executor);

        log.info("[设备命令执行器配置] 命令执行器装饰器链组装完成");
        return executor;
    }
}
