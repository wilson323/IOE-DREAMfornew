package net.lab1024.sa.device.comm.discovery.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 协议发现请求表单
 * <p>
 * 用于接收设备协议自动发现的请求参数：
 * 1. 网络扫描范围配置
 * 2. 扫描参数设置
 * 3. 协议检测选项
 * 4. 结果处理配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "协议发现请求")
public class DiscoveryRequestForm {

    @Schema(description = "网络范围（CIDR格式，如192.168.1.0/24）", required = true, example = "192.168.1.0/24")
    @NotBlank(message = "网络范围不能为空")
    private String networkRange;

    @Schema(description = "扫描超时时间（毫秒）", example = "5000")
    @Min(value = 1000, message = "超时时间不能小于1000毫秒")
    @Max(value = 60000, message = "超时时间不能大于60000毫秒")
    private int timeout = 5000;

    @Schema(description = "最大并发任务数", example = "20")
    @Min(value = 1, message = "并发任务数不能小于1")
    @Max(value = 100, message = "并发任务数不能大于100")
    private int maxConcurrentTasks = 20;

    @Schema(description = "是否启用Ping扫描", example = "true")
    private Boolean enablePingScan = true;

    @Schema(description = "是否启用端口扫描", example = "true")
    private Boolean enablePortScan = true;

    @Schema(description = "是否启用协议检测", example = "true")
    private Boolean enableProtocolDetection = true;

    @Schema(description = "是否自动注册设备", example = "false")
    private Boolean autoRegister = false;

    @Schema(description = "端口扫描列表（空表示使用默认端口）", example = "[80, 443, 8080, 554]")
    private String portScanList;

    @Schema(description = "启用扫描器类型列表", example = "['PING', 'PORT', 'HTTP', 'RTSP']")
    private String enabledScanners;

    @Schema(description = "协议检测器列表", example = "['HTTP', 'RTSP', 'ONVIF', 'SNMP', 'MODBUS']")
    private String protocolDetectors;

    @Schema(description = "扫描优先级", example = "NORMAL")
    private String scanPriority = "NORMAL";

    @Schema(description = "任务描述", example = "办公室网络设备扫描")
    private String taskDescription;

    @Schema(description = "是否启用详细信息收集", example = "true")
    private Boolean enableDetailedInfo = true;

    @Schema(description = "设备类型过滤", example = "['CAMERA', 'PLC']")
    private String deviceTypeFilter;

    @Schema(description = "厂商过滤", example = "['Hikvision', 'Dahua']")
    private String vendorFilter;
}