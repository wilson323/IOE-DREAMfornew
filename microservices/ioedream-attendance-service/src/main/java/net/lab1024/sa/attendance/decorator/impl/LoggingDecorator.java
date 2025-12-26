package net.lab1024.sa.attendance.decorator.impl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import net.lab1024.sa.attendance.decorator.IPunchExecutor;
import net.lab1024.sa.attendance.decorator.PunchDecorator;

/**
 * 日志装饰器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 记录打卡请求和响应时间
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
@Slf4j
public class LoggingDecorator extends PunchDecorator {

    /**
     * 构造函数
     *
     * @param delegate 被装饰的执行器
     */
    public LoggingDecorator(IPunchExecutor delegate) {
        super(delegate);
    }

    @Override
    public IPunchExecutor.PunchResult execute(IPunchExecutor.MobilePunchRequest request) {
        log.info("[日志装饰器] 打卡请求: userId={}, type={}, location=({}, {})",
                request.getUserId(), request.getPunchType(),
                request.getLatitude(), request.getLongitude());

        long startTime = System.currentTimeMillis();
        try {
            IPunchExecutor.PunchResult result = super.execute(request);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[日志装饰器] 打卡完成: userId={}, result={}, duration={}ms",
                    request.getUserId(), result.isSuccess(), duration);

            return result;
        } catch (Exception e) {
            log.error("[日志装饰器] 打卡异常: userId={}", request.getUserId(), e);
            throw e;
        }
    }
}
