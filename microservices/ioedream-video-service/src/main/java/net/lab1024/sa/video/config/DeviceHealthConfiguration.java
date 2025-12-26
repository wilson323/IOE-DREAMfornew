package net.lab1024.sa.video.config;

import net.lab1024.sa.video.dao.DeviceHealthDao;
import net.lab1024.sa.video.manager.DeviceHealthManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 设备健康检查配置类
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Configuration
public class DeviceHealthConfiguration {

    @Bean
    public DeviceHealthManager deviceHealthManager(DeviceHealthDao deviceHealthDao) {
        return new DeviceHealthManager(deviceHealthDao);
    }
}
