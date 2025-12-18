package net.lab1024.sa.access.decorator;

import net.lab1024.sa.access.domain.form.AccessRequest;
import net.lab1024.sa.access.template.AbstractAccessFlowTemplate;

/**
 * 门禁通行流程执行器接口
 * <p>
 * 使用装饰器模式增强通行流程功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface IAccessFlowExecutor {

    /**
     * 执行通行流程
     *
     * @param request 通行请求
     * @return 通行结果
     */
    AbstractAccessFlowTemplate.AccessResult execute(AccessRequest request);
}
