package net.lab1024.sa.base.config;

import net.lab1024.sa.base.module.support.rbac.ResourcePermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

/**
 * RBAC权限配置
 * <p>
 * 配置资源权限拦截器，注册到Spring MVC拦截器链中
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-16
 */
@Configuration
public class RbacConfig implements WebMvcConfigurer {

    @Resource
    private ResourcePermissionInterceptor resourcePermissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(resourcePermissionInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns(
                        "/api/auth/**",          // 排除认证相关接口
                        "/api/health",          // 排除健康检查
                        "/api/public/**",        // 排除公开接口
                        "/api/doc.html",         // 排除API文档
                        "/api/swagger-ui/**",    // 排除Swagger UI
                        "/api/v3/api-docs/**",   // 排除OpenAPI文档
                        "/error"                 // 排除错误页面
                );
    }
}