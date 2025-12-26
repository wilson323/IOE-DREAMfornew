package net.lab1024.sa.attendance.monitor;

import java.lang.reflect.Method;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * API性能监控拦截器
 * <p>
 * 拦截API请求并记录性能指标
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class ApiPerformanceInterceptor implements HandlerInterceptor {

    private final ApiPerformanceMonitor performanceMonitor;

    public ApiPerformanceInterceptor(ApiPerformanceMonitor performanceMonitor) {
        this.performanceMonitor = performanceMonitor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        request.setAttribute("REQUEST_START_TIME", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            Long startTime = (Long) request.getAttribute("REQUEST_START_TIME");
            if (startTime == null) {
                return;
            }

            long duration = System.currentTimeMillis() - startTime;
            String uri = request.getRequestURI();
            String method = request.getMethod();
            int status = response.getStatus();

            // 记录性能指标
            performanceMonitor.recordRequest(uri, method, status, duration);

            // 慢请求日志
            if (duration > 3000) {
                log.warn("[性能监控] 慢请求检测: method={}, uri={}, duration={}ms, status={}",
                        method, uri, duration, status);
            }

            // 异常日志
            if (ex != null) {
                log.error("[性能监控] 请求异常: method={}, uri={}, error={}",
                        method, uri, ex.getMessage(), ex);
            }

        } catch (Exception e) {
            log.error("[性能监控] 记录失败: error={}", e.getMessage(), e);
        }
    }
}
