package net.lab1024.sa.common.organization.config;

import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.manager.AreaDeviceManager;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import net.lab1024.sa.common.device.manager.DeviceStatusManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 区域设备配置类
 * <p>
 * 注册区域设备管理器为Spring Bean
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Configuration
public class AreaDeviceConfig {

    /**
     * 注册区域设备管理器
     *
     * @param areaDeviceDao 区域设备DAO
     * @param areaDao 区域DAO
     * @param areaUserDao 区域用户DAO
     * @param deviceDao 设备DAO
     * @param deviceStatusManager 设备状态管理器
     * @param areaUnifiedService 区域统一服务
     * @param gatewayServiceClient 网关服务客户端
     * @return 区域设备管理器实例
     */
    @Bean
    public AreaDeviceManager areaDeviceManager(AreaDeviceDao areaDeviceDao,
                                             AreaDao areaDao,
                                             AreaUserDao areaUserDao,
                                             DeviceDao deviceDao,
                                             DeviceStatusManager deviceStatusManager,
                                             AreaUnifiedService areaUnifiedService,
                                             GatewayServiceClient gatewayServiceClient) {
        return new AreaDeviceManager(
                areaDao,
                areaDeviceDao,
                areaUserDao,
                deviceDao,
                deviceStatusManager,
                areaUnifiedService,
                gatewayServiceClient
        );
    }
}
