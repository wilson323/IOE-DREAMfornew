package net.lab1024.sa.access.decorator.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.decorator.IAccessFlowExecutor;
import net.lab1024.sa.access.domain.form.AccessRequest;
import net.lab1024.sa.access.template.AbstractAccessFlowTemplate;
import org.springframework.stereotype.Component;

/**
 * 基础门禁通行流程执行器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现基础的通行流程逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class BasicAccessFlowExecutor implements IAccessFlowExecutor {

    @Override
    public AbstractAccessFlowTemplate.AccessResult execute(AccessRequest request) {
        // 基础通行逻辑
        // 实际应该调用AbstractAccessFlowTemplate的processAccess方法
        // 这里简化处理，实际应该注入具体的流程实现（如BiometricAccessFlow）
        log.debug("[基础通行执行器] 执行通行流程, userId={}, deviceId={}", 
                request.getUserId(), request.getDeviceId());
        
        // TODO: 调用具体的流程实现
        return AbstractAccessFlowTemplate.AccessResult.success();
    }
}
