package net.lab1024.sa.consume.config;

import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.OfflineConsumeRecordDao;
import net.lab1024.sa.consume.manager.OfflineSyncManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 离线同步模块配置类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Configuration
@EnableScheduling
public class OfflineSyncConfig {

    @Bean
    public OfflineSyncManager offlineSyncManager(OfflineConsumeRecordDao offlineConsumeRecordDao,
                                                  AccountDao accountDao) {
        return new OfflineSyncManager(offlineConsumeRecordDao, accountDao);
    }
}
