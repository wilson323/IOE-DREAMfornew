package net.lab1024.sa.device.comm.discovery;

/**
 * 协议探测器接口
 * <p>
 * 用于探测和识别设备协议类型
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
public interface ProtocolDetector {

    /**
     * 获取探测器类型
     *
     * @return 探测器类型标识
     */
    String getDetectorType();

    /**
     * 探测设备协议
     *
     * @param host 设备地址
     * @param port 设备端口
     * @return 协议探测结果
     */
    ProtocolDetectionResult detect(String host, int port);

    /**
     * 是否支持指定协议
     *
     * @param protocolType 协议类型
     * @return 是否支持
     */
    boolean supportsProtocol(String protocolType);

    /**
     * 协议探测结果
     */
    class ProtocolDetectionResult {
        private boolean detected;
        private String protocolType;
        private String protocolVersion;
        private String deviceModel;
        private String manufacturer;

        public boolean isDetected() { return detected; }
        public void setDetected(boolean detected) { this.detected = detected; }
        public String getProtocolType() { return protocolType; }
        public void setProtocolType(String protocolType) { this.protocolType = protocolType; }
        public String getProtocolVersion() { return protocolVersion; }
        public void setProtocolVersion(String protocolVersion) { this.protocolVersion = protocolVersion; }
        public String getDeviceModel() { return deviceModel; }
        public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }
        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

        public static ProtocolDetectionResult notDetected() {
            ProtocolDetectionResult result = new ProtocolDetectionResult();
            result.setDetected(false);
            return result;
        }

        public static ProtocolDetectionResult detected(String protocolType, String version) {
            ProtocolDetectionResult result = new ProtocolDetectionResult();
            result.setDetected(true);
            result.setProtocolType(protocolType);
            result.setProtocolVersion(version);
            return result;
        }
    }
}
