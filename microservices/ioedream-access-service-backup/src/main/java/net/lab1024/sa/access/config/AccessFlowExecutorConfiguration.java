package net.lab1024.sa.access.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.decorator.IAccessFlowExecutor;
import net.lab1024.sa.access.decorator.impl.AntiPassbackDecorator;
import net.lab1024.sa.access.decorator.impl.BasicAccessFlowExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 门禁通行流程执行器配置
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用装饰器模式组装通行流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Configuration
public class AccessFlowExecutorConfiguration {

    /**
     * 配置门禁通行流程执行器（装饰器组装）
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 装饰器顺序：基础执行器 -> 反潜回装饰器
     * </p>
     *
     * @return 组装后的通行流程执行器
     */
    @Bean
    public IAccessFlowExecutor accessFlowExecutor() {
        log.info("[门禁通行流程配置] 开始组装通行流程装饰器链");

        // 基础执行器
        IAccessFlowExecutor executor = new BasicAccessFlowExecutor();

        // 反潜回装饰器
        executor = new AntiPassbackDecorator(executor);

        log.info("[门禁通行流程配置] 通行流程装饰器链组装完成");
        return executor;
    }
}
