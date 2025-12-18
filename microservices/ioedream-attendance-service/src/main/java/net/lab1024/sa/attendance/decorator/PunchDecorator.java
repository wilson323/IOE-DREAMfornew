package net.lab1024.sa.attendance.decorator;

/**
 * 打卡装饰器基类
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用装饰器模式实现打卡流程的动态增强
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public abstract class PunchDecorator implements IPunchExecutor {

    /**
     * 被装饰的执行器
     */
    protected IPunchExecutor delegate;

    /**
     * 构造函数
     *
     * @param delegate 被装饰的执行器
     */
    public PunchDecorator(IPunchExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public PunchResult execute(MobilePunchRequest request) {
        return delegate.execute(request);
    }
}
