package net.lab1024.sa.device.comm.discovery.config;

import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;
import net.lab1024.sa.device.comm.discovery.detector.ProtocolDetector;
import net.lab1024.sa.device.comm.discovery.detector.impl.*;
import net.lab1024.sa.device.comm.discovery.scanner.NetworkScanner;
import net.lab1024.sa.device.comm.discovery.scanner.impl.PingScanner;
import net.lab1024.sa.device.comm.discovery.scanner.impl.PortScanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 协议自动发现配置类
 * <p>
 * 配置协议自动发现相关的Bean：
 * 1. 发现管理器Bean
 * 2. 网络扫描器Bean
 * 3. 协议检测器Bean
 * 4. 线程池配置
 * 5. 协议指纹库初始化
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Configuration
@EnableAsync
public class ProtocolDiscoveryConfiguration {

    /**
     * 协议自动发现管理器
     */
    @Bean
    public ProtocolAutoDiscoveryManager protocolAutoDiscoveryManager(
            List<NetworkScanner> networkScanners,
            List<ProtocolDetector> protocolDetectors,
            Executor discoveryExecutor) {

        ProtocolAutoDiscoveryManager manager = new ProtocolAutoDiscoveryManager();

        // 注册网络扫描器
        for (NetworkScanner scanner : networkScanners) {
            manager.registerScanner(scanner);
        }

        // 注册协议检测器
        for (ProtocolDetector detector : protocolDetectors) {
            manager.registerDetector(detector);
        }

        // 设置线程池
        manager.setExecutor(discoveryExecutor);

        // 初始化协议指纹库
        manager.initializeProtocolFingerprints(initProtocolFingerprints());

        return manager;
    }

    /**
     * Ping网络扫描器
     */
    @Bean
    public NetworkScanner pingScanner() {
        return new PingScanner();
    }

    /**
     * 端口扫描器
     */
    @Bean
    public NetworkScanner portScanner() {
        return new PortScanner();
    }

    /**
     * HTTP协议检测器
     */
    @Bean
    public ProtocolDetector httpProtocolDetector() {
        return new HTTPProtocolDetector();
    }

    /**
     * RTSP协议检测器
     */
    @Bean
    public ProtocolDetector rtspProtocolDetector() {
        return new RTSPProtocolDetector();
    }

    /**
     * ONVIF协议检测器
     */
    @Bean
    public ProtocolDetector onvifProtocolDetector() {
        return new ONVIFProtocolDetector();
    }

    /**
     * SNMP协议检测器
     */
    @Bean
    public ProtocolDetector snmpProtocolDetector() {
        return new SNMPProtocolDetector();
    }

    /**
     * Modbus协议检测器
     */
    @Bean
    public ProtocolDetector modbusProtocolDetector() {
        return new ModbusProtocolDetector();
    }

    /**
     * 发现任务线程池
     */
    @Bean(name = "discoveryExecutor")
    public Executor discoveryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(10);

        // 最大线程数
        executor.setMaxPoolSize(50);

        // 队列容量
        executor.setQueueCapacity(1000);

        // 线程名前缀
        executor.setThreadNamePrefix("protocol-discovery-");

        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);

        // 线程空闲时间
        executor.setKeepAliveSeconds(60);

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    /**
     * 初始化协议指纹库
     */
    private Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> initProtocolFingerprints() {
        Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints = new HashMap<>();

        // 海康威视摄像头指纹
        fingerprints.put("HIKVISION_VIDEO_V2_0", createHikvisionFingerprint());

        // 大华摄像头指纹
        fingerprints.put("DAHUA_VIDEO_V2_0", createDahuaFingerprint());

        // 宇视摄像头指纹
        fingerprints.put("UNIVIEW_VIDEO_V2_0", createUniviewFingerprint());

        // 萤石摄像头指纹
        fingerprints.put("EZVIZ_VIDEO_V2_0", createEzvizFingerprint());

        // Axis摄像头指纹
        fingerprints.put("AXIS_CAMERA", createAxisFingerprint());

        // 思科网络设备指纹
        fingerprints.put("CISCO_NETWORK_DEVICE", createCiscoFingerprint());

        // 西门子PLC指纹
        fingerprints.put("SIEMENS_PLC", createSiemensPLCFingerprint());

        // 施耐德PLC指纹
        fingerprints.put("SCHNEIDER_PLC", createSchneiderPLCFingerprint());

        return fingerprints;
    }

    /**
     * 创建海康威视设备指纹
     */
    private ProtocolAutoDiscoveryManager.ProtocolFingerprint createHikvisionFingerprint() {
        ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = new ProtocolAutoDiscoveryManager.ProtocolFingerprint();

        fingerprint.setProtocolName("HIKVISION_VIDEO_V2_0");
        fingerprint.setVendor("Hikvision");
        fingerprint.setDeviceType("IP Camera");

        // HTTP特征
        fingerprint.setHttpHeaders(Arrays.asList("Hikvision-Webs", "Hikvision"));
        fingerprint.setHttpPaths(Arrays.asList("/doc/page/login.asp", "/PSIA/System/deviceInfo"));

        // RTSP特征
        fingerprint.setRtspHeaders(Arrays.asList("Hikvision"));
        fingerprint.setRtspPaths(Arrays.asList("/Streaming/Channels/"));

        // ONVIF服务
        fingerprint.setOnvifServices(Arrays.asList("/onvif/device_service", "/onvif/media_service"));

        // 设备描述特征
        fingerprint.setDeviceDescriptions(Arrays.asList("DS-2CD", "DS-2DE", "DS-2DF"));

        // SNMP OID
        fingerprint.setSnmpOids(Arrays.asList("1.3.6.1.4.1.5902"));

        return fingerprint;
    }

    /**
     * 创建大华设备指纹
     */
    private ProtocolAutoDiscoveryManager.ProtocolFingerprint createDahuaFingerprint() {
        ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = new ProtocolAutoDiscoveryManager.ProtocolFingerprint();

        fingerprint.setProtocolName("DAHUA_VIDEO_V2_0");
        fingerprint.setVendor("Dahua");
        fingerprint.setDeviceType("IP Camera");

        // HTTP特征
        fingerprint.setHttpHeaders(Arrays.asList("DahuaWEB", "Dahua"));
        fingerprint.setHttpPaths(Arrays.asList("/cgi-bin/global.cgi", "/cgi-bin/magicBox.cgi"));

        // RTSP特征
        fingerprint.setRtspHeaders(Arrays.asList("Dahua"));
        fingerprint.setRtspPaths(Arrays.asList("/cam/realmonitor?channel="));

        // ONVIF服务
        fingerprint.setOnvifServices(Arrays.asList("/onvif/device_service"));

        // 设备描述特征
        fingerprint.setDeviceDescriptions(Arrays.asList("DH-IPC", "DH-PTZ", "DH-NVR"));

        return fingerprint;
    }

    /**
     * 创建宇视设备指纹
     */
    private ProtocolAutoDiscoveryManager.ProtocolFingerprint createUniviewFingerprint() {
        ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = new ProtocolAutoDiscoveryManager.ProtocolFingerprint();

        fingerprint.setProtocolName("UNIVIEW_VIDEO_V2_0");
        fingerprint.setVendor("Uniview");
        fingerprint.setDeviceType("IP Camera");

        // HTTP特征
        fingerprint.setHttpHeaders(Arrays.asList("Uniview"));
        fingerprint.setHttpPaths(Arrays.asList("/api/deviceInfo", "/Login.htm"));

        // RTSP特征
        fingerprint.setRtspHeaders(Arrays.asList("Uniview"));

        // ONVIF服务
        fingerprint.setOnvifServices(Arrays.asList("/onvif/device_service"));

        // 设备描述特征
        fingerprint.setDeviceDescriptions(Arrays.asList("IPC", "NVR"));

        return fingerprint;
    }

    /**
     * 创建萤石设备指纹
     */
    private ProtocolAutoDiscoveryManager.ProtocolFingerprint createEzvizFingerprint() {
        ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = new ProtocolAutoDiscoveryManager.ProtocolFingerprint();

        fingerprint.setProtocolName("EZVIZ_VIDEO_V2_0");
        fingerprint.setVendor("EZVIZ");
        fingerprint.setDeviceType("IP Camera");

        // HTTP特征
        fingerprint.setHttpHeaders(Arrays.asList("EZVIZ"));
        fingerprint.setHttpPaths(Arrays.asList("/deviceInfo", "/login"));

        // RTSP特征
        fingerprint.setRtspHeaders(Arrays.asList("EZVIZ"));

        // ONVIF服务
        fingerprint.setOnvifServices(Arrays.asList("/onvif/device_service"));

        // 设备描述特征
        fingerprint.setDeviceDescriptions(Arrays.asList("CS-", "C6"));

        return fingerprint;
    }

    /**
     * 创建Axis设备指纹
     */
    private ProtocolAutoDiscoveryManager.ProtocolFingerprint createAxisFingerprint() {
        ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = new ProtocolAutoDiscoveryManager.ProtocolFingerprint();

        fingerprint.setProtocolName("AXIS_CAMERA");
        fingerprint.setVendor("Axis");
        fingerprint.setDeviceType("IP Camera");

        // HTTP特征
        fingerprint.setHttpHeaders(Arrays.asList("Axis"));
        fingerprint.setHttpPaths(Arrays.asList("/axis-cgi/", "/cgi-bin/video_server.cgi"));

        // RTSP特征
        fingerprint.setRtspHeaders(Arrays.asList("Axis"));
        fingerprint.setRtspPaths(Arrays.asList("/axis-media/media.amp"));

        // ONVIF服务
        fingerprint.setOnvifServices(Arrays.asList("/onvif/device_service"));

        // 设备描述特征
        fingerprint.setDeviceDescriptions(Arrays.asList("AXIS", "M10", "P32"));

        return fingerprint;
    }

    /**
     * 创建思科网络设备指纹
     */
    private ProtocolAutoDiscoveryManager.ProtocolFingerprint createCiscoFingerprint() {
        ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = new ProtocolAutoDiscoveryManager.ProtocolFingerprint();

        fingerprint.setProtocolName("CISCO_NETWORK_DEVICE");
        fingerprint.setVendor("Cisco");
        fingerprint.setDeviceType("Network Device");

        // HTTP特征
        fingerprint.setHttpHeaders(Arrays.asList("Cisco"));
        fingerprint.setHttpPaths(Arrays.asList("/level/15/exec/-/show/version"));

        // SNMP OID
        fingerprint.setSnmpOids(Arrays.asList("1.3.6.1.4.1.9.1"));

        // 设备描述特征
        fingerprint.setDeviceDescriptions(Arrays.asList("Cisco", "IOS", "NX-OS"));

        return fingerprint;
    }

    /**
     * 创建西门子PLC指纹
     */
    private ProtocolAutoDiscoveryManager.ProtocolFingerprint createSiemensPLCFingerprint() {
        ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = new ProtocolAutoDiscoveryManager.ProtocolFingerprint();

        fingerprint.setProtocolName("SIEMENS_PLC");
        fingerprint.setVendor("Siemens");
        fingerprint.setDeviceType("PLC");

        // SNMP OID
        fingerprint.setSnmpOids(Arrays.asList("1.3.6.1.4.1.2262"));

        // Modbus寄存器
        fingerprint.setModbusRegisters(Arrays.asList("1000:10", "2000:10"));

        // 设备描述特征
        fingerprint.setDeviceDescriptions(Arrays.asList("S7-", "LOGO", "ET200"));

        return fingerprint;
    }

    /**
     * 创建施耐德PLC指纹
     */
    private ProtocolAutoDiscoveryManager.ProtocolFingerprint createSchneiderPLCFingerprint() {
        ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = new ProtocolAutoDiscoveryManager.ProtocolFingerprint();

        fingerprint.setProtocolName("SCHNEIDER_PLC");
        fingerprint.setVendor("Schneider");
        fingerprint.setDeviceType("PLC");

        // SNMP OID
        fingerprint.setSnmpOids(Arrays.asList("1.3.6.1.4.1.200"));

        // Modbus寄存器
        fingerprint.setModbusRegisters(Arrays.asList("1000:10", "4000:10"));

        // 设备描述特征
        fingerprint.setDeviceDescriptions(Arrays.asList("Modicon", "Premium", "Quantum"));

        return fingerprint;
    }
}