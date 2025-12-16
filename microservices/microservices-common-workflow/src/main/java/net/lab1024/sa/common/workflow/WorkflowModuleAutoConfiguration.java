package net.lab1024.sa.common.workflow;

import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

/**
 * 工作流模块自动配置
 * <p>
 * 提供Aviator表达式引擎、工作流定义、流程调度、规则引擎等工作流相关功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Slf4j
@Configuration
public class WorkflowModuleAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("[模块化] microservices-common-workflow 工作流模块已加载");
    }
}
