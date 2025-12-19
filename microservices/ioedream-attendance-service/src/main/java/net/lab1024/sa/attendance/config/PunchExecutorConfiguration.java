package net.lab1024.sa.attendance.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.decorator.IPunchExecutor;
import net.lab1024.sa.attendance.decorator.impl.AntiCheatingDecorator;
import net.lab1024.sa.attendance.decorator.impl.BasicPunchExecutor;
import net.lab1024.sa.attendance.decorator.impl.GPSValidationDecorator;
import net.lab1024.sa.attendance.decorator.impl.LoggingDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 打卡执行器配置
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用装饰器模式组装打卡流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Configuration
public class PunchExecutorConfiguration {

    /**
     * 配置打卡执行器（装饰器组装）
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 装饰器顺序：基础执行器 -> GPS验证 -> 防作弊 -> 日志记录
     * </p>
     *
     * @return 组装后的打卡执行器
     */
    @Bean
    public IPunchExecutor punchExecutor() {
        log.info("[打卡执行器配置] 开始组装打卡执行器装饰器链");

        // 基础执行器
        IPunchExecutor executor = new BasicPunchExecutor();

        // GPS验证装饰器
        executor = new GPSValidationDecorator(executor);

        // 防作弊装饰器
        executor = new AntiCheatingDecorator(executor);

        // 日志装饰器
        executor = new LoggingDecorator(executor);

        log.info("[打卡执行器配置] 打卡执行器装饰器链组装完成");
        return executor;
    }
}
