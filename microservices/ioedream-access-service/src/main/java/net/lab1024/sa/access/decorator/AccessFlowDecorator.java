package net.lab1024.sa.access.decorator;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessRequest;
import net.lab1024.sa.access.template.AbstractAccessFlowTemplate;

/**
 * 门禁通行流程装饰器基类
 * <p>
 * 使用装饰器模式增强通行流程功能
 * 严格遵循CLAUDE.md规范：
 * - 使用装饰器模式实现
 * - 支持功能增强和组合
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public abstract class AccessFlowDecorator implements IAccessFlowExecutor {

    /**
     * 被装饰的执行器
     */
    protected IAccessFlowExecutor delegate;

    /**
     * 构造函数
     *
     * @param delegate 被装饰的执行器
     */
    public AccessFlowDecorator(IAccessFlowExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public AbstractAccessFlowTemplate.AccessResult execute(AccessRequest request) {
        // 装饰器前置处理
        beforeExecute(request);

        // 执行被装饰的方法
        AbstractAccessFlowTemplate.AccessResult result = delegate.execute(request);

        // 装饰器后置处理
        afterExecute(request, result);

        return result;
    }

    /**
     * 前置处理(钩子方法)
     * <p>
     * 子类可以覆盖此方法以实现前置增强逻辑
     * </p>
     *
     * @param request 通行请求
     */
    protected void beforeExecute(AccessRequest request) {
        // 默认空实现
    }

    /**
     * 后置处理(钩子方法)
     * <p>
     * 子类可以覆盖此方法以实现后置增强逻辑
     * </p>
     *
     * @param request 通行请求
     * @param result 通行结果
     */
    protected void afterExecute(AccessRequest request, AbstractAccessFlowTemplate.AccessResult result) {
        // 默认空实现
    }
}
