package net.lab1024.sa.consume.config;

import net.lab1024.sa.consume.dao.MealOrderDao;
import net.lab1024.sa.consume.dao.MealOrderItemDao;
import net.lab1024.sa.consume.manager.MealOrderManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订餐模块配置类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Configuration
public class MealOrderConfig {

    @Bean
    public MealOrderManager mealOrderManager(MealOrderDao mealOrderDao, MealOrderItemDao mealOrderItemDao) {
        return new MealOrderManager(mealOrderDao, mealOrderItemDao);
    }
}
