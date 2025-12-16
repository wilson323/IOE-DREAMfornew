package net.lab1024.sa.gateway.config;

import net.lab1024.sa.gateway.manager.RateLimitManager;
import net.lab1024.sa.gateway.manager.RouteManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manager配置类
 * <p>
 * 网关服务是响应式的，不依赖MyBatis DAO
 * 只注册网关特有的Manager
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Configuration("gatewayManagerConfiguration")
public class ManagerConfiguration {

    // 网关服务是响应式的，不支持MyBatis，MenuManager需要在业务服务中使用
    // @Bean
    // public MenuManager menuManager(MenuDao menuDao, EmployeeDao employeeDao) {
    //     return new MenuManager(menuDao, employeeDao);
    // }

    /**
     * 路由管理器Bean配置
     *
     * @return 路由管理器实例
     */
    @Bean
    public RouteManager routeManager() {
        return new RouteManager();
    }

    /**
     * 限流管理器Bean配置
     *
     * @return 限流管理器实例
     */
    @Bean
    public RateLimitManager rateLimitManager() {
        return new RateLimitManager();
    }
}
