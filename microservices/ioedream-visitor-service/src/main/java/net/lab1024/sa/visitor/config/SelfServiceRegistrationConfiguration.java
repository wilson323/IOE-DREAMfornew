package net.lab1024.sa.visitor.config;

import net.lab1024.sa.visitor.dao.SelfServiceRegistrationDao;
import net.lab1024.sa.visitor.manager.SelfServiceRegistrationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自助登记配置类
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Configuration
public class SelfServiceRegistrationConfiguration {

    @Bean
    public SelfServiceRegistrationManager selfServiceRegistrationManager(SelfServiceRegistrationDao dao) {
        return new SelfServiceRegistrationManager(dao);
    }
}
