package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
import net.lab1024.sa.common.monitor.dao.NotificationDao;
import net.lab1024.sa.common.monitor.manager.NotificationManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 监控通知相关Bean自动装配配置
 * <p>
 * 在 microservices-common-monitor 模块中，专门管理监控通知相关的Bean装配
 * </p>
 * <p>
 * 包含的Bean：
 * - NotificationManager: 通知管理器（依赖NotificationDao和AlertRuleDao）
 * </p>
 * <p>
 * 使用条件：
 * - 服务必须依赖 microservices-common-monitor 模块
 * - 服务必须扫描 net.lab1024.sa.common.monitor.dao 包以创建DAO Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-15
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(NotificationManager.class)
public class MonitorBeanAutoConfiguration {

    /**
     * 注册 NotificationManager Bean
     * <p>
     * 通知管理器，负责告警通知发送、通知渠道管理、重试机制等功能
     * 仅当 NotificationDao 和 AlertRuleDao Bean 都存在时才注册
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({NotificationDao.class, AlertRuleDao.class})
    public NotificationManager notificationManager(
            NotificationDao notificationDao,
            AlertRuleDao alertRuleDao) {
        log.info("[监控Bean] 初始化NotificationManager");
        return new NotificationManager(notificationDao, alertRuleDao);
    }
}
