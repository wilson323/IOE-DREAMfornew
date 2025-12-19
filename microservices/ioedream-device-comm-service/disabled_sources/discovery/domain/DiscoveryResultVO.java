package net.lab1024.sa.device.comm.discovery.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 协议发现结果视图对象
 * <p>
 * 用于返回设备协议自动发现的详细结果：
 * 1. 发现任务基本信息
 * 2. 发现设备列表详情
 * 3. 协议检测结果
 * 4. 性能统计信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "协议发现结果")
public class DiscoveryResultVO {

    @Schema(description = "任务ID", example = "task-123456")
    private String taskId;

    @Schema(description = "网络范围", example = "192.168.1.0/24")
    private String networkRange;

    @Schema(description = "任务状态", example = "COMPLETED")
    private String status;

    @Schema(description = "检测到的协议类型", example = "HIKVISION_VIDEO_V2_0")
    private String detectedProtocol;

    @Schema(description = "发现的设备数量", example = "15")
    private Integer discoveredDeviceCount;

    @Schema(description = "可达设备数量", example = "12")
    private Integer reachableDeviceCount;

    @Schema(description = "扫描持续时间（毫秒）", example = "12345")
    private Long scanDuration;

    @Schema(description = "开始时间", example = "2025-12-16T10:30:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T10:32:15")
    private LocalDateTime endTime;

    @Schema(description = "成功扫描的IP数量", example = "254")
    private Integer scannedIpCount;

    @Schema(description = "发现设备列表")
    private List<DiscoveredDeviceVO> discoveredDevices;

    @Schema(description = "协议检测结果", example = "{\"HTTP\": 8, \"RTSP\": 5, \"ONVIF\": 3}")
    private Map<String, Integer> protocolDetectionResults;

    @Schema(description = "错误信息", example = "扫描部分IP超时")
    private String errorMessage;

    @Schema(description = "性能统计")
    private DiscoveryPerformanceVO performance;

    /**
     * 发现的设备信息
     */
    @Data
    @Builder
    @Schema(description = "发现的设备信息")
    public static class DiscoveredDeviceVO {

        @Schema(description = "IP地址", example = "192.168.1.100")
        private String ipAddress;

        @Schema(description = "MAC地址", example = "00:11:22:33:44:55")
        private String macAddress;

        @Schema(description = "主机名", example = "camera-office-01")
        private String hostname;

        @Schema(description = "厂商", example = "Hikvision")
        private String vendor;

        @Schema(description = "设备类型", example = "IP Camera")
        private String deviceType;

        @Schema(description = "检测到的协议", example = "HIKVISION_VIDEO_V2_0")
        private String detectedProtocol;

        @Schema(description = "开放端口", example = "{\"80\": \"HTTP\", \"554\": \"RTSP\"}")
        private Map<Integer, String> openPorts;

        @Schema(description = "响应时间（毫秒）", example = "125")
        private Long responseTime;

        @Schema(description = "是否可达", example = "true")
        private Boolean reachable;

        @Schema(description = "设备详细信息")
        private Map<String, Object> deviceDetails;

        @Schema(description = "发现时间", example = "2025-12-16T10:31:30")
        private LocalDateTime discoveryTime;

        @Schema(description = "置信度（0-100）", example = "95")
        private Integer confidence;
    }

    /**
     * 性能统计信息
     */
    @Data
    @Builder
    @Schema(description = "性能统计")
    public static class DiscoveryPerformanceVO {

        @Schema(description = "总扫描时间（毫秒）", example = "123456")
        private Long totalScanTime;

        @Schema(description = "平均响应时间（毫秒）", example = "125")
        private Long averageResponseTime;

        @Schema(description = "最小响应时间（毫秒）", example = "15")
        private Long minResponseTime;

        @Schema(description = "最大响应时间（毫秒）", example = "5000")
        private Long maxResponseTime;

        @Schema(description = "扫描速度（IP/秒）", example = "20.5")
        private Double scanSpeed;

        @Schema(description = "成功率（百分比）", example = "85.5")
        private Double successRate;

        @Schema(description = "协议检测成功率（百分比）", example = "92.3")
        private Double protocolDetectionRate;

        @Schema(description = "Ping检测统计", example = "{\"success\": 200, \"failed\": 54}")
        private Map<String, Integer> pingStatistics;

        @Schema(description = "端口扫描统计", example = "{\"total\": 5000, \"open\": 150}")
        private Map<String, Integer> portScanStatistics;

        @Schema(description = "协议检测统计", example = "{\"HTTP\": 8, \"RTSP\": 5, \"SNMP\": 3}")
        private Map<String, Integer> protocolDetectionStatistics;
    }
}