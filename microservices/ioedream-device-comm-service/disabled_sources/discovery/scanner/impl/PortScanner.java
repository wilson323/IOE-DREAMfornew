package net.lab1024.sa.device.comm.discovery.scanner.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.discovery.scanner.NetworkScanner;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 端口扫描器
 * <p>
 * 高性能端口扫描和设备识别：
 * 1. 多线程并发扫描
 * 2. 端口服务识别
 * 3. TCP连接测试
 * 4. Banner信息收集
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class PortScanner implements NetworkScanner {

    private static final Map<Integer, String> COMMON_SERVICES = new HashMap<>();
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int THREAD_POOL_SIZE = 50;

    static {
        // 初始化常见端口服务映射
        COMMON_SERVICES.put(21, "FTP");
        COMMON_SERVICES.put(22, "SSH");
        COMMON_SERVICES.put(23, "Telnet");
        COMMON_SERVICES.put(25, "SMTP");
        COMMON_SERVICES.put(53, "DNS");
        COMMON_SERVICES.put(80, "HTTP");
        COMMON_SERVICES.put(110, "POP3");
        COMMON_SERVICES.put(143, "IMAP");
        COMMON_SERVICES.put(443, "HTTPS");
        COMMON_SERVICES.put(554, "RTSP");
        COMMON_SERVICES.put(8080, "HTTP-Alt");
        COMMON_SERVICES.put(8000, "HTTP-Alt2");
        COMMON_SERVICES.put(8088, "HTTP-Alt3");
        COMMON_SERVICES.put(37777, "Dahua");
        COMMON_SERVICES.put(8001, "Hikvision");
        COMMON_SERVICES.put(8089, "Uniview");
        COMMON_SERVICES.put(4433, "HTTPS-Alt");
        COMMON_SERVICES.put(9000, "SonarQube");
        COMMON_SERVICES.put(9090, "XML-RPC");
    }

    @Override
    public ScanResult scanNetwork(String networkRange, int timeout) {
        ScanResult result = new ScanResult();
        result.setScannerType(getScannerType());
        result.setDevices(new ConcurrentHashMap<>());

        long startTime = System.currentTimeMillis();

        try {
            // 获取需要扫描的IP列表
            List<String> ipList = generateIPList(networkRange);
            if (ipList.isEmpty()) {
                result.setErrorMessage("无效的网络范围");
                return result;
            }

            result.setTotalCount(ipList.size());

            log.info("[端口扫描器] 开始端口扫描: {}, IP数量: {}, 超时: {}ms",
                    networkRange, ipList.size(), timeout);

            // 并发端口扫描
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            List<PortScanTask> tasks = new ArrayList<>();

            for (String ip : ipList) {
                PortScanTask task = new PortScanTask(ip, timeout);
                tasks.add(task);
                executor.submit(task);
            }

            executor.shutdown();
            executor.awaitTermination(timeout * 2, TimeUnit.MILLISECONDS);

            // 收集扫描结果
            int reachableCount = 0;
            Map<String, DeviceInfo> devices = new HashMap<>();

            for (PortScanTask task : tasks) {
                if (task.getDeviceInfo() != null) {
                    String ip = task.getIp();
                    DeviceInfo deviceInfo = task.getDeviceInfo();

                    // 检查是否有开放端口
                    if (!deviceInfo.getOpenPorts().isEmpty()) {
                        deviceInfo.setReachable(true);
                        devices.put(ip, deviceInfo);
                        reachableCount++;
                    } else {
                        deviceInfo.setReachable(false);
                    }
                }
            }

            result.setDevices(devices);
            result.setReachableCount(reachableCount);

            log.info("[端口扫描器] 端口扫描完成，总IP数: {}, 有开放端口设备: {}, 耗时: {}ms",
                    result.getTotalCount(), result.getReachableCount(), System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("[端口扫描器] 端口扫描失败", e);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setScanDuration(System.currentTimeMillis() - startTime);
        }

        return result;
    }

    @Override
    public boolean isReachable(String ipAddress) {
        try {
            // 快速检查常用端口
            for (Map.Entry<Integer, String> entry : COMMON_SERVICES.entrySet()) {
                if (isPortOpen(ipAddress, entry.getKey(), 1000)) {
                    log.debug("[端口扫描器] IP {} 通过端口 {} 检测可达性", ipAddress, entry.getKey());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.debug("[端口扫描器] IP可达性检查失败: {}", ipAddress, e);
            return false;
        }
    }

    @Override
    public String getScannerType() {
        return "PORT";
    }

    @Override
    public String getDescription() {
        return "多线程端口扫描器";
    }

    /**
     * 扫描单个设备的端口
     *
     * @param ipAddress IP地址
     * @param portRange 端口范围
     * @param timeout 超时时间
     * @return 开放端口映射
     */
    public Map<Integer, String> scanDevicePorts(String ipAddress, int timeout) {
        Map<Integer, String> openPorts = new HashMap<>();
        List<Integer> ports = COMMON_SERVICES.keySet().stream().collect(Collectors.toList());

        try {
            log.debug("[端口扫描器] 扫描设备端口: {}", ipAddress);

            ExecutorService executor = Executors.newFixedThreadPool(20);
            List<SinglePortScanTask> tasks = new ArrayList<>();

            for (Integer port : ports) {
                SinglePortScanTask task = new SinglePortScanTask(ipAddress, port, timeout);
                tasks.add(task);
                executor.submit(task);
            }

            executor.shutdown();
            executor.awaitTermination(timeout, TimeUnit.MILLISECONDS);

            for (SinglePortScanTask task : tasks) {
                if (task.isOpen()) {
                    String service = COMMON_SERVICES.getOrDefault(task.getPort(), "UNKNOWN");
                    openPorts.put(task.getPort(), service);

                    // 尝试获取Banner信息
                    String banner = task.getBanner();
                    if (banner != null && !banner.trim().isEmpty()) {
                        log.debug("[端口扫描器] 端口 {} Banner: {}", task.getPort(), banner.trim());
                    }
                }
            }

            log.debug("[端口扫描器] 设备 {} 端口扫描完成，开放端口数: {}", ipAddress, openPorts.size());

        } catch (Exception e) {
            log.error("[端口扫描器] 扫描设备端口失败: {}", ipAddress, e);
        }

        return openPorts;
    }

    /**
     * 生成IP地址列表
     */
    private List<String> generateIPList(String networkRange) {
        List<String> ipList = new ArrayList<>();

        try {
            if (networkRange.contains("/")) {
                // CIDR格式解析
                String[] parts = networkRange.split("/");
                String baseIP = parts[0];
                int cidr = Integer.parseInt(parts[1]);

                // 简化实现：只处理/24网络
                if (cidr == 24) {
                    String[] octets = baseIP.split("\\.");
                    String network = String.format("%s.%s.%s", octets[0], octets[1], octets[2]);

                    for (int i = 1; i < 255; i++) {
                        ipList.add(network + "." + i);
                    }
                } else if (cidr == 32) {
                    ipList.add(baseIP);
                }
            } else {
                // 单个IP或IP范围
                if (networkRange.contains("-")) {
                    // IP范围
                    String[] rangeParts = networkRange.split("-");
                    String startIP = rangeParts[0];
                    String endIP = rangeParts[1];

                    // 简化实现：只处理相同网段的范围
                    String[] startOctets = startIP.split("\\.");
                    String[] endOctets = endIP.split("\\.");

                    if (startOctets.length == 4 && endOctets.length == 4) {
                        int start = Integer.parseInt(startOctets[3]);
                        int end = Integer.parseInt(endOctets[3]);

                        for (int i = start; i <= end; i++) {
                            ipList.add(String.format("%s.%s.%s.%d", startOctets[0], startOctets[1], startOctets[2], i));
                        }
                    }
                } else {
                    // 单个IP
                    ipList.add(networkRange);
                }
            }
        } catch (Exception e) {
            log.error("[端口扫描器] 生成IP列表失败: {}", networkRange, e);
        }

        return ipList;
    }

    /**
     * 检查端口是否开放
     */
    private boolean isPortOpen(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 端口扫描任务
     */
    private class PortScanTask implements Runnable {
        private final String ip;
        private final int timeout;
        private DeviceInfo deviceInfo;

        public PortScanTask(String ip, int timeout) {
            this.ip = ip;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                DeviceInfo info = new DeviceInfo();
                info.setIpAddress(ip);

                // 扫描所有常见端口
                Map<Integer, String> openPorts = scanDevicePorts(ip, timeout);
                info.setOpenPorts(openPorts);

                // 如果有开放端口，尝试获取MAC地址（仅在局域网）
                if (!openPorts.isEmpty()) {
                    String macAddress = getMACAddress(ip);
                    if (macAddress != null) {
                        info.setMacAddress(macAddress);
                    }
                }

                this.deviceInfo = info;

            } catch (Exception e) {
                log.debug("[端口扫描器] 端口扫描任务异常: {}", ip, e);
                this.deviceInfo = null;
            }
        }

        public String getIp() {
            return ip;
        }

        public DeviceInfo getDeviceInfo() {
            return deviceInfo;
        }
    }

    /**
     * 单端口扫描任务
     */
    private class SinglePortScanTask implements Runnable {
        private final String host;
        private final int port;
        private final int timeout;
        private boolean isOpen;
        private String banner;

        public SinglePortScanTask(String host, int port, int timeout) {
            this.host = host;
            this.port = port;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeout);
                this.isOpen = true;

                // 尝试读取Banner信息
                if (socket.isConnected() && !socket.isClosed()) {
                    this.banner = readBanner(socket);
                }

            } catch (Exception e) {
                this.isOpen = false;
            }
        }

        /**
         * 读取Banner信息
         */
        private String readBanner(Socket socket) {
            try {
                byte[] buffer = new byte[1024];
                socket.setSoTimeout(1000);
                int bytesRead = socket.getInputStream().read(buffer);
                if (bytesRead > 0) {
                    return new String(buffer, 0, bytesRead).trim();
                }
            } catch (Exception e) {
                // 忽略Banner读取错误
            }
            return null;
        }

        public int getPort() {
            return port;
        }

        public boolean isOpen() {
            return isOpen;
        }

        public String getBanner() {
            return banner;
        }
    }

    /**
     * 获取MAC地址
     */
    private String getMACAddress(String ipAddress) {
        try {
            // 简化实现：通过ARP表获取MAC地址
            // 在实际实现中，可以使用JNI调用系统命令或第三方库
            return executeARPCommand(ipAddress);
        } catch (Exception e) {
            log.debug("[端口扫描器] 获取MAC地址失败: {}", ipAddress, e);
            return null;
        }
    }

    /**
     * 执行ARP命令获取MAC地址
     */
    private String executeARPCommand(String ipAddress) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb.command("arp", "-a", ipAddress);
            } else {
                pb.command("arp", "-n", ipAddress);
            }

            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(ipAddress)) {
                        // 解析MAC地址
                        String[] parts = line.split("\\s+");
                        for (String part : parts) {
                            if (part.matches("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}")) {
                                return part.toUpperCase();
                            }
                        }
                    }
                }
            }

            process.waitFor();
        } catch (Exception e) {
            log.debug("[端口扫描器] ARP命令执行失败", e);
        }
        return null;
    }
}