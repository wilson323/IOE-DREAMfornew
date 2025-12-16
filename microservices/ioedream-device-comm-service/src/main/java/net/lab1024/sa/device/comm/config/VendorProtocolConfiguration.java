package net.lab1024.sa.device.comm.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.factory.ProtocolAdapterFactory;
import net.lab1024.sa.device.comm.protocol.hikvision.VideoHikvisionV20Adapter;
import net.lab1024.sa.device.comm.protocol.dahua.VideoDahuaV20Adapter;
import net.lab1024.sa.device.comm.protocol.uniview.VideoUniviewV20Adapter;
import net.lab1024.sa.device.comm.protocol.ezviz.VideoEzvizV20Adapter;
import net.lab1024.sa.device.comm.vendor.DeviceVendorSupportManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 厂商协议配置类
 * <p>
 * 配置所有主流厂商的协议适配器：
 * 1. 注册海康威视视频协议适配器
 * 2. 注册大华技术视频协议适配器
 * 3. 注册宇视科技视频协议适配器
 * 4. 注册萤石科技视频协议适配器
 * 5. 配置厂商支持管理器
 * 6. 初始化协议适配器工厂
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Configuration
@Schema(description = "厂商协议配置")
public class VendorProtocolConfiguration {

    @Resource
    private ProtocolAdapterFactory protocolAdapterFactory;

    /**
     * 注册海康威视视频协议适配器
     *
     * @return 海康威视视频协议适配器
     */
    @Bean(name = "videoHikvisionV20Adapter")
    public VideoHikvisionV20Adapter videoHikvisionV20Adapter() {
        VideoHikvisionV20Adapter adapter = new VideoHikvisionV20Adapter();
        log.info("[厂商协议配置] 注册海康威视视频协议适配器成功");
        return adapter;
    }

    /**
     * 注册大华技术视频协议适配器
     *
     * @return 大华技术视频协议适配器
     */
    @Bean(name = "videoDahuaV20Adapter")
    public VideoDahuaV20Adapter videoDahuaV20Adapter() {
        VideoDahuaV20Adapter adapter = new VideoDahuaV20Adapter();
        log.info("[厂商协议配置] 注册大华技术视频协议适配器成功");
        return adapter;
    }

    /**
     * 注册宇视科技视频协议适配器
     *
     * @return 宇视科技视频协议适配器
     */
    @Bean(name = "videoUniviewV20Adapter")
    public VideoUniviewV20Adapter videoUniviewV20Adapter() {
        VideoUniviewV20Adapter adapter = new VideoUniviewV20Adapter();
        log.info("[厂商协议配置] 注册宇视科技视频协议适配器成功");
        return adapter;
    }

    /**
     * 注册萤石科技视频协议适配器
     *
     * @return 萤石科技视频协议适配器
     */
    @Bean(name = "videoEzvizV20Adapter")
    public VideoEzvizV20Adapter videoEzvizV20Adapter() {
        VideoEzvizV20Adapter adapter = new VideoEzvizV20Adapter();
        log.info("[厂商协议配置] 注册萤石科技视频协议适配器成功");
        return adapter;
    }

    /**
     * 配置厂商支持管理器
     *
     * @return 厂商支持管理器
     */
    @Bean(name = "deviceVendorSupportManager")
    public DeviceVendorSupportManager deviceVendorSupportManager() {
        DeviceVendorSupportManager manager = new DeviceVendorSupportManager(protocolAdapterFactory);

        // 注册所有厂商协议适配器
        registerVendorAdapters(manager);

        log.info("[厂商协议配置] 厂商支持管理器配置完成");
        return manager;
    }

    /**
     * 注册厂商适配器到协议工厂
     *
     * @return 协议适配器工厂配置
     */
    @Bean(name = "protocolAdapterFactoryConfig")
    public Map<String, Object> protocolAdapterFactoryConfig() {
        Map<String, Object> config = new HashMap<>();

        // 配置海康威视适配器
        Map<String, Object> hikvisionConfig = new HashMap<>();
        hikvisionConfig.put("protocolType", "HIKVISION_VIDEO_V2_0");
        hikvisionConfig.put("manufacturer", "海康威视");
        hikvisionConfig.put("version", "2.0");
        hikvisionConfig.put("adapterClass", "VideoHikvisionV20Adapter");
        config.put("HIKVISION_VIDEO_V2_0", hikvisionConfig);

        // 配置大华技术适配器
        Map<String, Object> dahuaConfig = new HashMap<>();
        dahuaConfig.put("protocolType", "DAHUA_VIDEO_V2_0");
        dahuaConfig.put("manufacturer", "大华技术");
        dahuaConfig.put("version", "2.0");
        dahuaConfig.put("adapterClass", "VideoDahuaV20Adapter");
        config.put("DAHUA_VIDEO_V2_0", dahuaConfig);

        // 配置宇视科技适配器
        Map<String, Object> univiewConfig = new HashMap<>();
        univiewConfig.put("protocolType", "UNIVIEW_VIDEO_V2_0");
        univiewConfig.put("manufacturer", "宇视科技");
        univiewConfig.put("version", "2.0");
        univiewConfig.put("adapterClass", "VideoUniviewV20Adapter");
        config.put("UNIVIEW_VIDEO_V2_0", univiewConfig);

        // 配置萤石科技适配器
        Map<String, Object> ezvizConfig = new HashMap<>();
        ezvizConfig.put("protocolType", "EZVIZ_VIDEO_V2_0");
        ezvizConfig.put("manufacturer", "萤石科技");
        ezvizConfig.put("version", "2.0");
        ezvizConfig.put("adapterClass", "VideoEzvizV20Adapter");
        config.put("EZVIZ_VIDEO_V2_0", ezvizConfig);

        log.info("[厂商协议配置] 协议适配器工厂配置完成，支持协议数: {}", config.size());
        return config;
    }

    /**
     * 注册厂商适配器
     *
     * @param manager 厂商支持管理器
     */
    private void registerVendorAdapters(DeviceVendorSupportManager manager) {
        try {
            // 注册海康威视适配器到工厂
            protocolAdapterFactory.registerAdapter("HIKVISION_VIDEO_V2_0", videoHikvisionV20Adapter());
            log.info("[厂商协议配置] 注册海康威视适配器到工厂");

            // 注册大华技术适配器到工厂
            protocolAdapterFactory.registerAdapter("DAHUA_VIDEO_V2_0", videoDahuaV20Adapter());
            log.info("[厂商协议配置] 注册大华技术适配器到工厂");

            // 注册宇视科技适配器到工厂
            protocolAdapterFactory.registerAdapter("UNIVIEW_VIDEO_V2_0", videoUniviewV20Adapter());
            log.info("[厂商协议配置] 注册宇视科技适配器到工厂");

            // 注册萤石科技适配器到工厂
            protocolAdapterFactory.registerAdapter("EZVIZ_VIDEO_V2_0", videoEzvizV20Adapter());
            log.info("[厂商协议配置] 注册萤石科技适配器到工厂");

            // 打印支持统计
            log.info("[厂商协议配置] 厂商适配器注册完成");
            logSupportedVendors(manager);

        } catch (Exception e) {
            log.error("[厂商协议配置] 注册厂商适配器失败", e);
        }
    }

    /**
     * 打印支持的厂商统计
     *
     * @param manager 厂商支持管理器
     */
    private void logSupportedVendors(DeviceVendorSupportManager manager) {
        try {
            var statistics = manager.getSupportStatistics();
            log.info("[厂商协议配置] 支持厂商统计:");
            log.info("  - 总厂商数: {}", statistics.getTotalVendors());
            log.info("  - 总设备数: {}", statistics.getTotalDevices());
            log.info("  - 总协议数: {}", statistics.getTotalProtocols());
            log.info("  - 协议类型数: {}", statistics.getProtocolTypeCount());

            // 打印各厂商设备数量
            var vendorDeviceCount = statistics.getVendorDeviceCount();
            vendorDeviceCount.forEach((vendor, count) ->
                log.info("  - {}: {} 个设备", vendor, count));

            // 打印设备类型分布
            var deviceTypeCount = statistics.getDeviceTypeCount();
            deviceTypeCount.forEach((deviceType, count) ->
                log.info("  - {}: {} 个设备", deviceType, count));

        } catch (Exception e) {
            log.error("[厂商协议配置] 打印支持统计失败", e);
        }
    }
}