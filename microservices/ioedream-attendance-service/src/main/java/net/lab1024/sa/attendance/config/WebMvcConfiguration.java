package net.lab1024.sa.attendance.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.lab1024.sa.attendance.monitor.ApiPerformanceInterceptor;

import jakarta.annotation.Resource;

/**
 * Web MVC配置
 * <p>
 * 配置拦截器
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Resource
    private ApiPerformanceInterceptor performanceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(performanceInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns("/actuator/**");  // 排除actuator端点
    }
}
