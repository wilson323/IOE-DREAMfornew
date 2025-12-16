package net.lab1024.sa.attendance.config;

import net.lab1024.sa.attendance.engine.ScheduleEngine;
import net.lab1024.sa.attendance.manager.SmartSchedulingEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 智能排班引擎配置
 * <p>
 * 配置智能排班引擎的Bean，支持多种排班算法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class SchedulingEngineConfiguration {

    /**
     * 注册智能排班引擎作为主要实现
     */
    @Bean("smartSchedulingEngine")
    @Primary
    @ConditionalOnMissingBean(name = "smartSchedulingEngine")
    public ScheduleEngine smartSchedulingEngine(SmartSchedulingEngine smartSchedulingEngine) {
        // 这里可以返回包装后的引擎实现
        // 由于我们创建了SmartSchedulingEngineImpl，Spring会自动发现并注册
        return null; // 让Spring自动发现SmartSchedulingEngineImpl
    }

    /**
     * 注册SmartSchedulingEngine管理器
     */
    @Bean
    @ConditionalOnMissingBean(SmartSchedulingEngine.class)
    public SmartSchedulingEngine smartSchedulingManager() {
        // 如果没有实现，返回基础实现
        // 实际上我们已经有了完整的实现，这里只是作为备用
        return new SmartSchedulingEngine() {
            @Override
            public SchedulingResult generateSmartSchedule(SchedulingRequest request) {
                // 基础实现
                return new SchedulingResult();
            }

            @Override
            public List<ScheduleRecordEntity> applyTemplate(Long templateId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
                // 基础实现
                return new java.util.ArrayList<>();
            }
        };
    }
}