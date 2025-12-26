package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 设备发现请求表单
 * <p>
 * 用于启动设备自动发现任务的请求参数
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "设备发现请求")
public class DeviceDiscoveryRequestForm {

    /**
     * 子网地址
     * 示例: 192.168.1.0/24
     */
    @Schema(description = "子网地址", example = "192.168.1.0/24", required = true)
    @NotBlank(message = "子网地址不能为空")
    private String subnet;

    /**
     * 扫描超时时间（秒）
     * 默认: 180秒（3分钟）
     * 最大: 600秒（10分钟）
     */
    @Schema(description = "扫描超时时间（秒）", example = "180")
    private Integer timeout;

    /**
     * 发现协议列表
     * 可选值: ONVIF, PRIVATE, SNMP
     * 默认: 全部协议
     */
    @Schema(description = "发现协议列表", example = "[\"ONVIF\", \"PRIVATE\", \"SNMP\"]")
    private List<String> protocols;

    /**
     * 是否包含已发现的设备
     * true: 包含已发现的设备
     * false: 只包含新发现的设备
     */
    @Schema(description = "是否包含已发现的设备", example = "true")
    private Boolean includeDiscovered;

    /**
     * 区域ID（可选）
     * 如果指定，只扫描该区域内的设备
     */
    @Schema(description = "区域ID（可选）", example = "1")
    private Long areaId;
}
