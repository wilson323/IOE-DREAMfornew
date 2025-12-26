package net.lab1024.sa.attendance.service;

/**
 * WiFi定位验证服务
 * <p>
 * 用于验证WiFi定位的准确性和合法性，防止虚假打卡
 * </p>
 * <p>
 * 核心功能：
 * 1. WiFi信号强度验证（确保在有效范围内）
 * 2. MAC地址白名单验证（仅允许公司WiFi）
 * 3. WiFi定位精度计算（提供精确的位置信息）
 * 4. 防止WiFi欺骗（检测虚假热点）
 * </p>
 * <p>
 * 性能要求：
 * - 验证速度: <50ms
 * - 定位精度: <5米
 * - 误报率: <5%
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-25
 */
public interface WiFiLocationValidator {

    /**
     * 验证WiFi信号强度
     * <p>
     * 判断WiFi信号是否在有效范围内，防止使用弱信号打卡
     * </p>
     *
     * @param macAddress    WiFi设备的MAC地址
     * @param signalStrength 信号强度（dBm，通常-100到0）
     * @return true-信号强度有效, false-信号无效
     */
    boolean validateWiFiSignal(String macAddress, int signalStrength);

    /**
     * 检查MAC地址是否在白名单中
     * <p>
     * 仅允许公司注册的WiFi设备，防止使用个人热点打卡
     * </p>
     *
     * @param macAddress WiFi设备的MAC地址
     * @return true-在白名单中, false-不在白名单中
     */
    boolean isWhitelisted(String macAddress);

    /**
     * 计算WiFi定位精度
     * <p>
     * 根据信号强度估算距离，计算定位精度
     * </p>
     *
     * @param signalStrength 信号强度（dBm）
     * @return 定位精度（米）
     */
    double calculateAccuracy(int signalStrength);

    /**
     * 检测WiFi欺骗
     * <p>
     * 检测是否存在虚假WiFi热点（如MAC地址欺骗）
     * </p>
     *
     * @param macAddress       WiFi设备的MAC地址
     * @param signalStrength  信号强度
     * @param connectedDevices 连接设备数
     * @return true-可能是虚假热点, false-正常
     */
    boolean detectMockWiFi(String macAddress, int signalStrength, int connectedDevices);

    /**
     * 获取WiFi位置信息
     * <p>
     * 根据MAC地址获取WiFi位置信息（楼栋、楼层、区域）
     * </p>
     *
     * @param macAddress WiFi设备的MAC地址
     * @return WiFi位置信息
     */
    WiFiLocationInfo getWiFiLocationInfo(String macAddress);

    /**
     * WiFi位置信息
     */
    class WiFiLocationInfo {
        private String macAddress;
        private String locationName;
        private Integer building;
        private Integer floor;
        private String area;
        private double longitude;
        private double latitude;

        // 构造函数、getter和setter
        public WiFiLocationInfo() {
        }

        public WiFiLocationInfo(String macAddress, String locationName, Integer building,
                                 Integer floor, String area, double longitude, double latitude) {
            this.macAddress = macAddress;
            this.locationName = locationName;
            this.building = building;
            this.floor = floor;
            this.area = area;
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        public String getLocationName() {
            return locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }

        public Integer getBuilding() {
            return building;
        }

        public void setBuilding(Integer building) {
            this.building = building;
        }

        public Integer getFloor() {
            return floor;
        }

        public void setFloor(Integer floor) {
            this.floor = floor;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
    }
}
