package net.lab1024.sa.device.comm.discovery.scanner.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.discovery.scanner.NetworkScanner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Ping网络扫描器
 * <p>
 * 使用ICMP Ping和TCP连接检查网络设备可达性：
 * 1. ICMP Ping检测设备在线状态
 * 2. TCP连接测试常用端口
 * 3. 响应时间测量
 * 4. 并发扫描提高效率
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class PingScanner implements NetworkScanner {

    private static final String[] COMMON_PORTS = {"80", "443", "8080", "554", "8000", "8088", "37777"};
    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int THREAD_POOL_SIZE = 10;

    @Override
    public ScanResult scanNetwork(String networkRange, int timeout) {
        ScanResult result = new ScanResult();
        result.setScannerType(getScannerType());
        result.setDevices(new ConcurrentHashMap<>());

        long startTime = System.currentTimeMillis();

        try {
            List<String> ipList = generateIPList(networkRange);
            result.setTotalCount(ipList.size());

            log.info("[Ping扫描器] 开始扫描网络: {}, IP数量: {}, 超时: {}ms",
                    networkRange, ipList.size(), timeout);

            // 并发扫描IP地址
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            List<DeviceScanTask> tasks = new ArrayList<>();

            for (String ip : ipList) {
                DeviceScanTask task = new DeviceScanTask(ip, timeout);
                tasks.add(task);
                executor.submit(task);
            }

            executor.shutdown();
            executor.awaitTermination(timeout * 2, TimeUnit.MILLISECONDS);

            // 收集扫描结果
            int reachableCount = 0;
            for (DeviceScanTask task : tasks) {
                if (task.getDeviceInfo() != null && task.getDeviceInfo().isReachable()) {
                    result.getDevices().put(task.getIp(), task.getDeviceInfo());
                    reachableCount++;
                }
            }

            result.setReachableCount(reachableCount);
            log.info("[Ping扫描器] 扫描完成，总IP数: {}, 可达设备: {}, 耗时: {}ms",
                    result.getTotalCount(), result.getReachableCount(), System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("[Ping扫描器] 网络扫描失败", e);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setScanDuration(System.currentTimeMillis() - startTime);
        }

        return result;
    }

    @Override
    public boolean isReachable(String ipAddress) {
        try {
            long startTime = System.currentTimeMillis();

            // 首先尝试ICMP Ping
            boolean reachable = pingByICMP(ipAddress);

            // 如果ICMP失败，尝试TCP连接
            if (!reachable) {
                reachable = pingByTCP(ipAddress);
            }

            long responseTime = System.currentTimeMillis() - startTime;
            log.debug("[Ping扫描器] IP: {}, 可达性: {}, 响应时间: {}ms", ipAddress, reachable, responseTime);

            return reachable;

        } catch (Exception e) {
            log.debug("[Ping扫描器] IP可达性检查失败: {}", ipAddress, e);
            return false;
        }
    }

    @Override
    public String getScannerType() {
        return "PING";
    }

    @Override
    public String getDescription() {
        return "ICMP Ping和TCP连接扫描器";
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
                }
            } else {
                // 单个IP
                ipList.add(networkRange);
            }
        } catch (Exception e) {
            log.error("[Ping扫描器] 生成IP列表失败: {}", networkRange, e);
        }

        return ipList;
    }

    /**
     * ICMP Ping检测
     */
    private boolean pingByICMP(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            return address.isReachable(DEFAULT_TIMEOUT);
        } catch (Exception e) {
            log.debug("[Ping扫描器] ICMP Ping失败: {}", ipAddress, e);
            return false;
        }
    }

    /**
     * TCP连接检测
     */
    private boolean pingByTCP(String ipAddress) {
        for (String portStr : COMMON_PORTS) {
            int port = Integer.parseInt(portStr);
            if (isPortOpen(ipAddress, port, 1000)) {
                return true;
            }
        }
        return false;
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
     * 设备扫描任务
     */
    private class DeviceScanTask implements Runnable {
        private final String ip;
        private final int timeout;
        private DeviceInfo deviceInfo;

        public DeviceScanTask(String ip, int timeout) {
            this.ip = ip;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                DeviceInfo info = new DeviceInfo();
                info.setIpAddress(ip);

                long startTime = System.currentTimeMillis();

                // 检查可达性
                boolean reachable = isReachable(ip);
                info.setReachable(reachable);
                info.setResponseTime(System.currentTimeMillis() - startTime);

                if (reachable) {
                    // 收集端口信息
                    Map<Integer, String> openPorts = new HashMap<>();
                    for (String portStr : COMMON_PORTS) {
                        int port = Integer.parseInt(portStr);
                        if (isPortOpen(ip, port, 1000)) {
                            openPorts.put(port, getPortService(port));
                        }
                    }
                    info.setOpenPorts(openPorts);

                    // 尝试获取主机名
                    try {
                        InetAddress address = InetAddress.getByName(ip);
                        String hostname = address.getHostName();
                        if (!ip.equals(hostname)) {
                            info.setHostname(hostname);
                        }
                    } catch (Exception e) {
                        // 忽略主机名解析错误
                    }

                    log.debug("[Ping扫描器] 发现可达设备: {}, 开放端口: {}", ip, openPorts.keySet());
                }

                this.deviceInfo = info;

            } catch (Exception e) {
                log.debug("[Ping扫描器] 设备扫描异常: {}", ip, e);
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
     * 获取端口对应的服务
     */
    private String getPortService(int port) {
        switch (port) {
            case 80: return "HTTP";
            case 443: return "HTTPS";
            case 8080: return "HTTP-ALT";
            case 554: return "RTSP";
            case 8000: return "HTTP-ALT2";
            case 8088: return "HTTP-ALT3";
            case 37777: return "DAHUA-SERVICE";
            default: return "UNKNOWN";
        }
    }
}