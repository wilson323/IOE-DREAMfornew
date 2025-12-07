package net.lab1024.sa.consume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.lab1024.sa.common.recommend.RecommendationEngine;

/**
 * 推荐引擎配置类
 *
 * @Author IOE-DREAM Team
 * @Date 2025-12-05
 * @Copyright IOE-DREAM智慧园区一卡通管理平台
 *
 *            说明：RecommendationEngine在microservices-common中是纯Java类，
 *            在此配置类中注册为Spring Bean供ConsumeRecommendService使用
 */
@Configuration
public class RecommendationEngineConfig {

    /**
     * 注册推荐引擎Bean
     *
     * @return RecommendationEngine实例
     */
    @Bean
    public RecommendationEngine recommendationEngine() {
        return new RecommendationEngine();
    }
}
