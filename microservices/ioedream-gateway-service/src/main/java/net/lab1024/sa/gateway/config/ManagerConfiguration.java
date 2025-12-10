package net.lab1024.sa.gateway.config;

import net.lab1024.sa.common.menu.dao.MenuDao;
import net.lab1024.sa.common.menu.manager.MenuManager;
import net.lab1024.sa.common.organization.dao.EmployeeDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manager配置类
 * <p>
 * 将microservices-common中的Manager类注册为Spring Bean
 * 确保Manager类保持纯Java特性，不使用Spring注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Configuration
public class ManagerConfiguration {

    /**
     * 菜单管理器Bean配置
     *
     * @param menuDao 菜单DAO
     * @param employeeDao 员工DAO
     * @return 菜单管理器实例
     */
    @Bean
    public MenuManager menuManager(MenuDao menuDao, EmployeeDao employeeDao) {
        return new MenuManager(menuDao, employeeDao);
    }
}
