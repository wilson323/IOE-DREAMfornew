package net.lab1024.sa.video.config;

import net.lab1024.sa.video.dao.FirmwareUpgradeDao;
import net.lab1024.sa.video.manager.FirmwareUpgradeManager;
import net.lab1024.sa.video.sdk.DeviceProtocolAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 固件升级配置类
 * <p>
 * 注册FirmwareUpgradeManager为Spring Bean
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Configuration
public class FirmwareUpgradeConfiguration {

    @Bean
    public FirmwareUpgradeManager firmwareUpgradeManager(FirmwareUpgradeDao firmwareUpgradeDao,
                                                         DeviceProtocolAdapter deviceProtocolAdapter) {
        return new FirmwareUpgradeManager(firmwareUpgradeDao, deviceProtocolAdapter);
    }
}
