package net.lab1024.sa.device.comm.discovery.scanner;

import java.util.Map;

/**
 * 网络扫描器接口
 * <p>
 * 定义设备网络扫描的标准接口：
 * 1. IP地址扫描
 * 2. 端口状态检测
 * 3. 设备信息收集
 * 4. 网络拓扑发现
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface NetworkScanner {

    /**
     * 扫描网络范围内的设备
     *
     * @param networkRange 网络范围（如192.168.1.0/24）
     * @param timeout 扫描超时时间（毫秒）
     * @return 发现的设备信息
     */
    ScanResult scanNetwork(String networkRange, int timeout);

    /**
     * 检查IP地址是否可达
     *
     * @param ipAddress IP地址
     * @return 是否可达
     */
    boolean isReachable(String ipAddress);

    /**
     * 获取扫描器类型
     *
     * @return 扫描器类型
     */
    String getScannerType();

    /**
     * 获取扫描器描述
     *
     * @return 扫描器描述
     */
    String getDescription();

    /**
     * 扫描结果
     */
    class ScanResult {
        private String scannerType;
        private Map<String, DeviceInfo> devices;
        private int totalCount;
        private int reachableCount;
        private long scanDuration;
        private String errorMessage;

        // getters and setters
        public String getScannerType() { return scannerType; }
        public void setScannerType(String scannerType) { this.scannerType = scannerType; }

        public Map<String, DeviceInfo> getDevices() { return devices; }
        public void setDevices(Map<String, DeviceInfo> devices) { this.devices = devices; }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public int getReachableCount() { return reachableCount; }
        public void setReachableCount(int reachableCount) { this.reachableCount = reachableCount; }

        public long getScanDuration() { return scanDuration; }
        public void setScanDuration(long scanDuration) { this.scanDuration = scanDuration; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * 设备信息
     */
    class DeviceInfo {
        private String ipAddress;
        private String macAddress;
        private String hostname;
        private String vendor;
        private String deviceType;
        private Map<Integer, String> openPorts;
        private long responseTime;
        private boolean reachable;

        // getters and setters
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getMacAddress() { return macAddress; }
        public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

        public String getHostname() { return hostname; }
        public void setHostname(String hostname) { this.hostname = hostname; }

        public String getVendor() { return vendor; }
        public void setVendor(String vendor) { this.vendor = vendor; }

        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

        public Map<Integer, String> getOpenPorts() { return openPorts; }
        public void setOpenPorts(Map<Integer, String> openPorts) { this.openPorts = openPorts; }

        public long getResponseTime() { return responseTime; }
        public void setResponseTime(long responseTime) { this.responseTime = responseTime; }

        public boolean isReachable() { return reachable; }
        public void setReachable(boolean reachable) { this.reachable = reachable; }
    }
}