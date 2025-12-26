package net.lab1024.sa.access.service.impl;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.manager.DeviceDiscoveryManager;
import net.lab1024.sa.access.domain.form.DeviceDiscoveryRequestForm;
import net.lab1024.sa.access.domain.vo.DiscoveredDeviceVO;
import net.lab1024.sa.access.domain.vo.DeviceDiscoveryResultVO;
import net.lab1024.sa.access.service.DeviceDiscoveryService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.SmartRequestUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 门禁设备自动发现服务实现
 * <p>
 * 核心功能：
 * - UDP多播发现
 * - TCP单播验证
 * - 设备去重
 * - 结果缓存
 * </p>
 * <p>
 * 性能优化：
 * - 线程池并发扫描
 * - Redis缓存结果
 * - 异步处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class DeviceDiscoveryServiceImpl implements DeviceDiscoveryService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private DeviceDiscoveryManager deviceDiscoveryManager;

    /**
     * 多播地址和端口
     */
    private static final String MULTICAST_ADDRESS = "239.255.255.250";
    private static final int MULTICAST_PORT = 1900;

    /**
     * 扫描超时时间（秒）
     */
    private static final int SCAN_TIMEOUT = 180;

    /**
     * 缓存键前缀
     */
    private static final String CACHE_PREFIX = "device_discovery:";
    private static final Long CACHE_TTL = 1800L; // 30分钟

    /**
     * 线程池（用于并发扫描）
     */
    private final ExecutorService executorService = new ThreadPoolExecutor(
            10,  // 核心线程数
            50,  // 最大线程数
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 扫描任务存储
     */
    private final Map<String, Future<List<DiscoveredDeviceVO>>> scanTasks = new ConcurrentHashMap<>();

    /**
     * 启动设备自动发现
     */
    @Override
    public ResponseDTO<DeviceDiscoveryResultVO> discoverDevices(DeviceDiscoveryRequestForm requestForm) {
        log.info("[设备发现] 开始扫描: requestForm={}", JSON.toJSONString(requestForm));

        try {
            // 1. 生成扫描ID
            String scanId = UUID.randomUUID().toString();
            log.info("[设备发现] 生成扫描ID: scanId={}", scanId);

            // 2. 准备扫描参数
            String subnet = requestForm.getSubnet();
            Integer timeout = requestForm.getTimeout() != null ? requestForm.getTimeout() : SCAN_TIMEOUT;
            List<String> protocols = requestForm.getProtocols();

            log.info("[设备发现] 扫描参数: subnet={}, timeout={}s, protocols={}", subnet, timeout, protocols);

            // 3. 提交异步扫描任务
            Future<List<DiscoveredDeviceVO>> future = executorService.submit(() -> {
                return performDiscovery(subnet, timeout, protocols, scanId);
            });

            scanTasks.put(scanId, future);

            // 4. 返回初始结果
            DeviceDiscoveryResultVO result = DeviceDiscoveryResultVO.builder()
                    .scanId(scanId)
                    .status("RUNNING")
                    .progress(0)
                    .totalDevices(0)
                    .discoveredDevices(new ArrayList<>())
                    .build();

            log.info("[设备发现] 扫描任务已启动: scanId={}", scanId);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[设备发现] 启动扫描异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("DISCOVERY_START_ERROR", "启动设备发现失败: " + e.getMessage());
        }
    }

    /**
     * 执行设备发现（核心逻辑）
     */
    private List<DiscoveredDeviceVO> performDiscovery(String subnet, Integer timeout,
                                                       List<String> protocols, String scanId) {
        log.info("[设备发现] 开始设备发现: subnet={}, timeout={}s, protocols={}", subnet, timeout, protocols);

        long startTime = System.currentTimeMillis();
        List<DiscoveredDeviceVO> allDevices = new ArrayList<>();

        try {
            // 默认使用全部协议
            if (protocols == null || protocols.isEmpty()) {
                protocols = Arrays.asList("ONVIF", "SNMP", "PRIVATE", "SSDP");
            }

            int protocolCount = protocols.size();
            int currentProgress = 0;
            int progressPerProtocol = 80 / protocolCount;

            // 按协议执行发现
            for (String protocol : protocols) {
                try {
                    log.info("[设备发现] 执行协议发现: protocol={}", protocol);
                    List<DiscoveredDeviceVO> protocolDevices = discoverByProtocol(
                            protocol, subnet, timeout / protocolCount);

                    allDevices.addAll(protocolDevices);
                    log.info("[设备发现] {}协议发现设备数量: {}", protocol, protocolDevices.size());

                    currentProgress += progressPerProtocol;
                    updateProgress(scanId, currentProgress, allDevices);

                } catch (Exception e) {
                    log.warn("[设备发现] {}协议发现失败: error={}", protocol, e.getMessage());
                }
            }

            // 去重处理
            List<DiscoveredDeviceVO> deduplicatedDevices = deduplicateDevices(allDevices);
            log.info("[设备发现] 去重后设备数量: {}", deduplicatedDevices.size());

            // 缓存结果
            cacheDiscoveryResult(scanId, deduplicatedDevices);

            // 更新最终进度
            updateProgress(scanId, 100, deduplicatedDevices);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[设备发现] 扫描完成: scanId={}, discovered={}, duration={}ms",
                    scanId, deduplicatedDevices.size(), duration);

            return deduplicatedDevices;

        } catch (Exception e) {
            log.error("[设备发现] 扫描异常: scanId={}, error={}", scanId, e.getMessage(), e);
            throw new RuntimeException("设备发现失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据协议发现设备
     */
    private List<DiscoveredDeviceVO> discoverByProtocol(String protocol, String subnet, Integer timeout) {
        switch (protocol.toUpperCase()) {
            case "ONVIF":
                return onvifDiscovery(subnet, timeout);
            case "SNMP":
                return snmpDiscovery(subnet, timeout);
            case "PRIVATE":
                return privateProtocolDiscovery(subnet, timeout);
            case "SSDP":
                return udpMulticastDiscovery(subnet, timeout);
            default:
                log.warn("[设备发现] 不支持的协议: {}", protocol);
                return new ArrayList<>();
        }
    }

    /**
     * ONVIF协议发现
     * <p>
     * 用于发现IP摄像头和网络视频设备
     * 使用WS-Discovery协议
     * </p>
     */
    private List<DiscoveredDeviceVO> onvifDiscovery(String subnet, Integer timeout) {
        List<DiscoveredDeviceVO> devices = new ArrayList<>();

        try {
            // ONVIF使用WS-Discovery，也是基于UDP多播
            // 默认地址: 239.255.255.250:3702
            String onvifAddress = "239.255.255.250";
            int onvifPort = 3702;

            MulticastSocket multicastSocket = new MulticastSocket(onvifPort);
            InetAddress multicastGroup = InetAddress.getByName(onvifAddress);
            multicastSocket.joinGroup(multicastGroup);
            multicastSocket.setSoTimeout(timeout * 1000);

            log.info("[设备发现] ONVIF多播监听: address={}, port={}", onvifAddress, onvifPort);

            // 发送WS-Discovery Probe消息
            String probeMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"\n" +
                    "               xmlns:dn=\"http://www.onvif.org/ver10/network/wsdl\">\n" +
                    "  <soap:Header>\n" +
                    "    <wsa:MessageID xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
                    "urn:uuid:" + UUID.randomUUID().toString() + "</wsa:MessageID>\n" +
                    "    <wsa:To xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
                    "urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>\n" +
                    "    <wsa:Action xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
                    "http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>\n" +
                    "  </soap:Header>\n" +
                    "  <soap:Body>\n" +
                    "    <dn:Probe/>\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>";

            byte[] messageBytes = probeMessage.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(
                    messageBytes,
                    messageBytes.length,
                    multicastGroup,
                    onvifPort
            );
            multicastSocket.send(packet);

            // 接收响应
            long endTime = System.currentTimeMillis() + timeout * 1000;
            while (System.currentTimeMillis() < endTime) {
                try {
                    byte[] buffer = new byte[8192];
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(responsePacket);

                    // 解析ONVIF响应
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength(), "UTF-8");
                    DiscoveredDeviceVO device = parseOnvifResponse(response, responsePacket.getAddress());

                    if (device != null) {
                        device.setProtocol("ONVIF");
                        devices.add(device);
                    }

                } catch (SocketTimeoutException e) {
                    break;
                }
            }

            multicastSocket.leaveGroup(multicastGroup);
            multicastSocket.close();

        } catch (Exception e) {
            log.error("[设备发现] ONVIF发现异常: error={}", e.getMessage(), e);
        }

        return devices;
    }

    /**
     * SNMP协议发现
     * <p>
     * 用于发现网络设备
     * 注意: 此方法为框架实现，需要添加SNMP4J依赖才能完整工作
     * </p>
     */
    private List<DiscoveredDeviceVO> snmpDiscovery(String subnet, Integer timeout) {
        List<DiscoveredDeviceVO> devices = new ArrayList<>();

        log.warn("[设备发现] SNMP协议发现需要添加SNMP4J依赖，当前使用基础实现");

        // TODO: 添加SNMP4J依赖后实现完整SNMP发现
        // 1. 扫描子网IP范围
        // 2. 发送SNMP GET请求（OID: 1.3.6.1.2.1.1.1.0 - sysDescr）
        // 3. 解析响应获取设备信息

        // 基础实现: 使用TCP探测SNMP端口(161)
        try {
            String[] ipParts = subnet.split("/");
            String networkAddress = ipParts[0];
            int prefixLength = ipParts.length > 1 ? Integer.parseInt(ipParts[1]) : 24;

            // 生成IP范围
            String baseIp = networkAddress.substring(0, networkAddress.lastIndexOf('.'));
            List<String> ipList = new ArrayList<>();

            // 扫描前100个IP（避免扫描时间过长）
            for (int i = 1; i <= 100 && i <= (256 - (int) Math.pow(2, 32 - prefixLength)); i++) {
                ipList.add(baseIp + "." + i);
            }

            // 并发探测SNMP端口
            ExecutorService snmpExecutor = Executors.newFixedThreadPool(20);
            List<Future<DiscoveredDeviceVO>> futures = new ArrayList<>();

            for (String ip : ipList) {
                Future<DiscoveredDeviceVO> future = snmpExecutor.submit(() -> {
                    try {
                        // 探测SNMP端口161
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(ip, 161), 1000);
                        socket.close();

                        DiscoveredDeviceVO device = DiscoveredDeviceVO.builder()
                                .ipAddress(ip)
                                .port(161)
                                .deviceType(3) // 网络设备
                                .deviceStatus(1) // 在线
                                .verified(true)
                                .protocol("SNMP")
                                .deviceName("SNMP设备")
                                .discoveryTime(System.currentTimeMillis())
                                .build();

                        return device;

                    } catch (Exception e) {
                        return null;
                    }
                });
                futures.add(future);
            }

            // 收集结果
            for (Future<DiscoveredDeviceVO> future : futures) {
                try {
                    DiscoveredDeviceVO device = future.get(2, TimeUnit.SECONDS);
                    if (device != null) {
                        devices.add(device);
                    }
                } catch (Exception e) {
                    // 忽略超时和失败
                }
            }

            snmpExecutor.shutdown();

        } catch (Exception e) {
            log.error("[设备发现] SNMP基础发现异常: error={}", e.getMessage(), e);
        }

        return devices;
    }

    /**
     * 私有协议发现
     * <p>
     * 用于发现厂商特定协议的设备
     * 例如: 海康威视、大华等厂商的私有协议
     * </p>
     */
    private List<DiscoveredDeviceVO> privateProtocolDiscovery(String subnet, Integer timeout) {
        List<DiscoveredDeviceVO> devices = new ArrayList<>();

        log.info("[设备发现] 私有协议发现: subnet={}", subnet);

        // 常见门禁设备私有端口
        int[] privatePorts = { 37777, 8000, 8080, 9900, 8008 };

        try {
            String[] ipParts = subnet.split("/");
            String networkAddress = ipParts[0];
            int prefixLength = ipParts.length > 1 ? Integer.parseInt(ipParts[1]) : 24;

            // 生成IP范围
            String baseIp = networkAddress.substring(0, networkAddress.lastIndexOf('.'));
            List<String> ipList = new ArrayList<>();

            // 扫描前50个IP（私有协议扫描较慢）
            for (int i = 1; i <= 50 && i <= (256 - (int) Math.pow(2, 32 - prefixLength)); i++) {
                ipList.add(baseIp + "." + i);
            }

            // 并发探测私有端口
            ExecutorService privateExecutor = Executors.newFixedThreadPool(30);
            List<Future<DiscoveredDeviceVO>> futures = new ArrayList<>();

            for (String ip : ipList) {
                for (int port : privatePorts) {
                    Future<DiscoveredDeviceVO> future = privateExecutor.submit(() -> {
                        try {
                            // TCP连接探测
                            Socket socket = new Socket();
                            socket.connect(new InetSocketAddress(ip, port), 1000);

                            // 尝试发送厂商特定探测指令
                            String probeCommand = "DISCOVER\r\n";
                            socket.getOutputStream().write(probeCommand.getBytes());

                            byte[] buffer = new byte[1024];
                            int bytesRead = socket.getInputStream().read(buffer);

                            socket.close();

                            if (bytesRead > 0) {
                                String response = new String(buffer, 0, bytesRead);

                                // 根据响应判断设备厂商
                                String deviceBrand = detectDeviceBrand(response);
                                String deviceModel = detectDeviceModel(response);

                                DiscoveredDeviceVO device = DiscoveredDeviceVO.builder()
                                        .ipAddress(ip)
                                        .port(port)
                                        .deviceBrand(deviceBrand)
                                        .deviceModel(deviceModel)
                                        .deviceType(1) // 门禁设备
                                        .deviceStatus(1) // 在线
                                        .verified(true)
                                        .protocol("PRIVATE")
                                        .deviceName(deviceBrand + "门禁设备")
                                        .discoveryTime(System.currentTimeMillis())
                                        .deviceInfo(response)
                                        .build();

                                return device;
                            }

                        } catch (Exception e) {
                            // 端口未开放或不是门禁设备
                        }
                        return null;
                    });
                    futures.add(future);
                }
            }

            // 收集结果
            for (Future<DiscoveredDeviceVO> future : futures) {
                try {
                    DiscoveredDeviceVO device = future.get(3, TimeUnit.SECONDS);
                    if (device != null) {
                        devices.add(device);
                    }
                } catch (Exception e) {
                    // 忽略超时和失败
                }
            }

            privateExecutor.shutdown();

        } catch (Exception e) {
            log.error("[设备发现] 私有协议发现异常: error={}", e.getMessage(), e);
        }

        return devices;
    }

    /**
     * 解析ONVIF响应
     */
    private DiscoveredDeviceVO parseOnvifResponse(String response, InetAddress address) {
        try {
            // 简化的XML解析（实际应用应使用XPath或JAXB）
            DiscoveredDeviceVO device = DiscoveredDeviceVO.builder()
                    .ipAddress(address.getHostAddress())
                    .deviceType(4) // 视频设备
                    .deviceStatus(1) // 在线
                    .verified(false)
                    .discoveryTime(System.currentTimeMillis())
                    .build();

            // 提取设备信息（简化版）
            if (response.contains("Hikvision")) {
                device.setDeviceBrand("Hikvision");
                device.setDeviceModel("IP Camera");
            } else if (response.contains("Dahua")) {
                device.setDeviceBrand("Dahua");
                device.setDeviceModel("IP Camera");
            }

            // 提取设备服务地址
            int locationStart = response.indexOf("<wsa:Address>");
            if (locationStart > 0) {
                int locationEnd = response.indexOf("</wsa:Address>", locationStart);
                if (locationEnd > 0) {
                    String location = response.substring(locationStart + 14, locationEnd);
                    device.setDeviceLocation(location);

                    // 从URL提取端口
                    if (location.contains(":")) {
                        String[] parts = location.split(":");
                        if (parts.length > 2) {
                            try {
                                device.setPort(Integer.parseInt(parts[2].split("/")[0]));
                            } catch (NumberFormatException e) {
                                device.setPort(80);
                            }
                        }
                    }
                }
            }

            return device;

        } catch (Exception e) {
            log.debug("[设备发现] 解析ONVIF响应失败: response={}", response);
            return null;
        }
    }

    /**
     * 检测设备厂商（基于私有协议响应）
     */
    private String detectDeviceBrand(String response) {
        if (response.contains("HIKVISION") || response.contains("Hikvision")) {
            return "Hikvision";
        } else if (response.contains("DAHUA") || response.contains("Dahua")) {
            return "Dahua";
        } else if (response.contains("UNIVIEW") || response.contains("Uniview")) {
            return "Uniview";
        } else if (response.contains("ZKTECO") || response.contains("ZKTeco")) {
            return "ZKTeco";
        } else {
            return "Unknown";
        }
    }

    /**
     * 检测设备型号（基于私有协议响应）
     */
    private String detectDeviceModel(String response) {
        // 简化实现，实际需要解析厂商特定协议
        if (response.contains("DS-2CD")) {
            return "DS-2CD series";
        } else if (response.contains("IPC-HFW")) {
            return "IPC-HFW series";
        } else {
            return "Unknown Model";
        }
    }

    /**
     * UDP多播发现
     */
    private List<DiscoveredDeviceVO> udpMulticastDiscovery(String subnet, Integer timeout) {
        List<DiscoveredDeviceVO> devices = new ArrayList<>();

        try {
            // 创建多播Socket
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(multicastAddress);
            multicastSocket.setSoTimeout(timeout * 1000);

            log.info("[设备发现] UDP多播监听: address={}, port={}", MULTICAST_ADDRESS, MULTICAST_PORT);

            // 发送多播发现请求
            String discoveryMessage = "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: " + MULTICAST_ADDRESS + ":" + MULTICAST_PORT + "\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 3\r\n" +
                    "ST: ssdp:all\r\n\r\n";

            byte[] messageBytes = discoveryMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    messageBytes,
                    messageBytes.length,
                    multicastAddress,
                    MULTICAST_PORT
            );
            multicastSocket.send(packet);

            // 接收响应
            long endTime = System.currentTimeMillis() + timeout * 1000;
            while (System.currentTimeMillis() < endTime) {
                try {
                    byte[] buffer = new byte[8192];
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(responsePacket);

                    // 解析响应
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    DiscoveredDeviceVO device = parseDiscoveryResponse(response, responsePacket.getAddress());
                    if (device != null) {
                        devices.add(device);
                    }

                } catch (SocketTimeoutException e) {
                    // 超时退出
                    break;
                }
            }

            multicastSocket.leaveGroup(multicastAddress);
            multicastSocket.close();

        } catch (Exception e) {
            log.error("[设备发现] UDP多播发现异常: error={}", e.getMessage(), e);
        }

        return devices;
    }

    /**
     * TCP单播验证
     */
    private List<DiscoveredDeviceVO> tcpVerification(List<DiscoveredDeviceVO> devices) {
        List<DiscoveredDeviceVO> verifiedDevices = new ArrayList<>();
        ExecutorService verificationExecutor = Executors.newFixedThreadPool(20);

        try {
            List<Future<DiscoveredDeviceVO>> futures = new ArrayList<>();

            for (DiscoveredDeviceVO device : devices) {
                Future<DiscoveredDeviceVO> future = verificationExecutor.submit(() -> {
                    return verifyDevice(device);
                });
                futures.add(future);
            }

            // 收集验证结果
            for (Future<DiscoveredDeviceVO> future : futures) {
                try {
                    DiscoveredDeviceVO device = future.get(5, TimeUnit.SECONDS);
                    if (device != null) {
                        verifiedDevices.add(device);
                    }
                } catch (Exception e) {
                    log.debug("[设备发现] 设备验证超时或失败");
                }
            }

        } finally {
            verificationExecutor.shutdown();
        }

        return verifiedDevices;
    }

    /**
     * 验证单个设备
     */
    private DiscoveredDeviceVO verifyDevice(DiscoveredDeviceVO device) {
        String[] ports = { "80", "8000", "8080", "37777", "8000" };

        for (String port : ports) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(device.getIpAddress(), Integer.parseInt(port)), 2000);
                socket.close();

                device.setVerified(true);
                device.setPort(Integer.parseInt(port));
                log.debug("[设备发现] 设备验证成功: ip={}, port={}", device.getIpAddress(), port);
                return device;

            } catch (Exception e) {
                // 继续尝试下一个端口
            }
        }

        return null;
    }

    /**
     * 设备去重
     */
    private List<DiscoveredDeviceVO> deduplicateDevices(List<DiscoveredDeviceVO> devices) {
        Map<String, DiscoveredDeviceVO> deviceMap = new LinkedHashMap<>();

        for (DiscoveredDeviceVO device : devices) {
            String key = device.getIpAddress() + ":" + device.getMacAddress();

            // 保留信息最完整的记录
            if (!deviceMap.containsKey(key) || device.getDeviceInfo() != null) {
                deviceMap.put(key, device);
            }
        }

        return new ArrayList<>(deviceMap.values());
    }

    /**
     * 缓存发现结果
     */
    private void cacheDiscoveryResult(String scanId, List<DiscoveredDeviceVO> devices) {
        try {
            String cacheKey = CACHE_PREFIX + scanId;
            redisTemplate.opsForValue().set(cacheKey, devices, Duration.ofSeconds(CACHE_TTL));
            log.info("[设备发现] 结果已缓存: scanId={}, count={}", scanId, devices.size());
        } catch (Exception e) {
            log.warn("[设备发现] 缓存结果失败: scanId={}, error={}", scanId, e.getMessage());
        }
    }

    /**
     * 更新扫描进度
     */
    private void updateProgress(String scanId, Integer progress, List<DiscoveredDeviceVO> devices) {
        try {
            String cacheKey = CACHE_PREFIX + scanId + ":progress";
            DeviceDiscoveryResultVO result = DeviceDiscoveryResultVO.builder()
                    .scanId(scanId)
                    .status(progress < 100 ? "RUNNING" : "COMPLETED")
                    .progress(progress)
                    .totalDevices(devices.size())
                    .discoveredDevices(devices)
                    .build();

            redisTemplate.opsForValue().set(cacheKey, result, Duration.ofSeconds(CACHE_TTL));
        } catch (Exception e) {
            log.warn("[设备发现] 更新进度失败: scanId={}, error={}", scanId, e.getMessage());
        }
    }

    /**
     * 解析发现响应
     */
    private DiscoveredDeviceVO parseDiscoveryResponse(String response, InetAddress address) {
        try {
            // 简化的SSDP响应解析
            Map<String, String> headers = new HashMap<>();
            String[] lines = response.split("\r\n");
            for (String line : lines) {
                int colonPos = line.indexOf(':');
                if (colonPos > 0) {
                    String key = line.substring(0, colonPos).trim();
                    String value = line.substring(colonPos + 1).trim();
                    headers.put(key, value);
                }
            }

            DiscoveredDeviceVO device = new DiscoveredDeviceVO();
            device.setIpAddress(address.getHostAddress());
            device.setDeviceLocation(headers.get("LOCATION"));
            // 修复类型转换: String -> Integer
            device.setDeviceType(headers.get("ST") != null ? Integer.parseInt(headers.get("ST")) : null);
            device.setServer(headers.get("SERVER"));
            device.setUsn(headers.get("USN"));
            device.setVerified(false);

            return device;

        } catch (Exception e) {
            log.debug("[设备发现] 解析响应失败: response={}", response);
            return null;
        }
    }

    @Override
    public ResponseDTO<Void> stopDiscovery(String scanId) {
        log.info("[设备发现] 停止扫描: scanId={}", scanId);

        try {
            Future<List<DiscoveredDeviceVO>> future = scanTasks.get(scanId);
            if (future != null && !future.isDone()) {
                future.cancel(true);
                log.info("[设备发现] 扫描已停止: scanId={}", scanId);
            }
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[设备发现] 停止扫描异常: scanId={}, error={}", scanId, e.getMessage(), e);
            return ResponseDTO.error("STOP_DISCOVERY_ERROR", "停止扫描失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<DeviceDiscoveryResultVO> getDiscoveryProgress(String scanId) {
        log.info("[设备发现] 查询进度: scanId={}", scanId);

        try {
            String cacheKey = CACHE_PREFIX + scanId + ":progress";
            DeviceDiscoveryResultVO result = (DeviceDiscoveryResultVO) redisTemplate.opsForValue().get(cacheKey);

            if (result != null) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error("PROGRESS_NOT_FOUND", "扫描进度不存在");
            }
        } catch (Exception e) {
            log.error("[设备发现] 查询进度异常: scanId={}, error={}", scanId, e.getMessage(), e);
            return ResponseDTO.error("GET_PROGRESS_ERROR", "查询进度失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<DeviceDiscoveryResultVO> batchAddDevices(List<DiscoveredDeviceVO> devices) {
        log.info("[设备发现] 批量添加设备: count={}", devices.size());

        try {
            // 获取当前用户信息
            Long operatorId = SmartRequestUtil.getRequestUserId();
            String operatorName = SmartRequestUtil.getRequestUserName();

            // 调用Manager层进行批量添加
            DeviceDiscoveryResultVO result = deviceDiscoveryManager.batchAddDiscoveredDevices(
                    devices, operatorId, operatorName);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[设备发现] 批量添加设备异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("BATCH_ADD_ERROR", "批量添加失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> exportDiscoveryResult(String scanId) {
        log.info("[设备发现] 导出结果: scanId={}", scanId);

        try {
            // 1. 从缓存获取结果
            String cacheKey = CACHE_PREFIX + scanId;
            DeviceDiscoveryResultVO result = (DeviceDiscoveryResultVO) redisTemplate.opsForValue().get(cacheKey);

            if (result == null) {
                return ResponseDTO.error("RESULT_NOT_FOUND", "发现结果不存在或已过期");
            }

            List<DiscoveredDeviceVO> devices = result.getDiscoveredDevices();
            if (devices == null || devices.isEmpty()) {
                return ResponseDTO.error("NO_DEVICES", "没有可导出的设备");
            }

            // 2. 将设备列表转换为可导出的格式（CSV格式）
            StringBuilder csv = new StringBuilder();
            csv.append("IP地址,MAC地址,设备名称,设备型号,设备厂商,固件版本,端口,协议,设备类型,设备状态,已验证\n");

            for (DiscoveredDeviceVO device : devices) {
                csv.append(device.getIpAddress()).append(",");
                csv.append(device.getMacAddress() != null ? device.getMacAddress() : "").append(",");
                csv.append(device.getDeviceName() != null ? device.getDeviceName() : "").append(",");
                csv.append(device.getDeviceModel() != null ? device.getDeviceModel() : "").append(",");
                csv.append(device.getDeviceBrand() != null ? device.getDeviceBrand() : "").append(",");
                csv.append(device.getFirmwareVersion() != null ? device.getFirmwareVersion() : "").append(",");
                csv.append(device.getPort() != null ? device.getPort() : "").append(",");
                csv.append(device.getProtocol() != null ? device.getProtocol() : "").append(",");
                csv.append(getDeviceTypeName(device.getDeviceType())).append(",");
                csv.append(getDeviceStatusName(device.getDeviceStatus())).append(",");
                csv.append(device.getVerified() != null ? device.getVerified() : false).append("\n");
            }

            // 3. Base64编码CSV数据
            byte[] csvBytes = csv.toString().getBytes("UTF-8");
            String base64Data = java.util.Base64.getEncoder().encodeToString(csvBytes);

            Map<String, Object> exportData = new HashMap<>();
            exportData.put("fileName", "device_discovery_" + scanId + "_" +
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            exportData.put("fileSize", csvBytes.length);
            exportData.put("fileData", base64Data);
            exportData.put("deviceCount", devices.size());

            return ResponseDTO.ok(exportData);

        } catch (Exception e) {
            log.error("[设备发现] 导出结果异常: scanId={}, error={}", scanId, e.getMessage(), e);
            return ResponseDTO.error("EXPORT_ERROR", "导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备类型名称
     */
    private String getDeviceTypeName(Integer deviceType) {
        if (deviceType == null) return "未知";
        switch (deviceType) {
            case 1: return "门禁控制器";
            case 2: return "读卡器";
            case 3: return "生物识别设备";
            case 4: return "视频设备";
            case 5: return "网络设备";
            default: return "未知";
        }
    }

    /**
     * 获取设备状态名称
     */
    private String getDeviceStatusName(Integer deviceStatus) {
        if (deviceStatus == null) return "未知";
        switch (deviceStatus) {
            case 1: return "在线";
            case 2: return "离线";
            case 3: return "未知";
            default: return "未知";
        }
    }
}
