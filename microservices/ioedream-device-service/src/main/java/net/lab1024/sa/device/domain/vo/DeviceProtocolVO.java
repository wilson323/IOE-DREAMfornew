package net.lab1024.sa.device.domain.vo;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备协议配置视图对象
 * <p>
 * 用于返回设备协议配置信息
 *
 * @author IOE-DREAM Team
 * @date 2025-01-30
 */
@Data
@Schema(description = "设备协议配置视图对象")
public class DeviceProtocolVO {

    /**
     * 配置ID
     */
    @Schema(description = "配置ID", example = "1001")
    private Long configId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "DEV001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主门禁设备")
    private String deviceName;

    /**
     * 协议类型（TCP、UDP、HTTP、WEBSOCKET、MQTT等）
     */
    @Schema(description = "协议类型", example = "TCP")
    private String protocolType;

    /**
     * 协议类型描述
     */
    @Schema(description = "协议类型描述", example = "TCP协议")
    private String protocolTypeDesc;

    /**
     * 服务器地址（IP或域名）
     */
    @Schema(description = "服务器地址", example = "192.168.1.100")
    private String host;

    /**
     * 端口号
     */
    @Schema(description = "端口号", example = "8080")
    private Integer port;

    /**
     * 连接状态（CONNECTED-已连接、DISCONNECTED-未连接、CONNECTING-连接中、ERROR-错误）
     */
    @Schema(description = "连接状态", example = "CONNECTED")
    private String connectionStatus;

    /**
     * 连接状态描述
     */
    @Schema(description = "连接状态描述", example = "已连接")
    private String connectionStatusDesc;

    /**
     * 最后连接时间
     */
    @Schema(description = "最后连接时间", example = "2025-01-30 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastConnectTime;

    /**
     * 最后断开时间
     */
    @Schema(description = "最后断开时间", example = "2025-01-30 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastDisconnectTime;

    /**
     * 连接超时时间（秒）
     */
    @Schema(description = "连接超时时间（秒）", example = "30")
    private Integer connectTimeout;

    /**
     * 读取超时时间（秒）
     */
    @Schema(description = "读取超时时间（秒）", example = "60")
    private Integer readTimeout;

    /**
     * 是否启用SSL/TLS
     */
    @Schema(description = "是否启用SSL/TLS", example = "false")
    private Boolean enableSsl;

    /**
     * 是否启用心跳检测
     */
    @Schema(description = "是否启用心跳检测", example = "true")
    private Boolean enableHeartbeat;

    /**
     * 心跳间隔（秒）
     */
    @Schema(description = "心跳间隔（秒）", example = "30")
    private Integer heartbeatInterval;

    /**
     * 重连间隔（秒）
     */
    @Schema(description = "重连间隔（秒）", example = "10")
    private Integer reconnectInterval;

    /**
     * 最大重连次数
     */
    @Schema(description = "最大重连次数", example = "5")
    private Integer maxReconnectAttempts;

    /**
     * 当前重连次数
     */
    @Schema(description = "当前重连次数", example = "0")
    private Integer currentReconnectAttempts;

    /**
     * 自定义配置参数（JSON格式）
     */
    @Schema(description = "自定义配置参数（JSON格式）")
    private String customConfig;

    /**
     * 协议特定参数（Map格式）
     */
    @Schema(description = "协议特定参数")
    private Map<String, Object> protocolParams;

    /**
     * 配置状态（ENABLED-启用、DISABLED-禁用）
     */
    @Schema(description = "配置状态", example = "ENABLED")
    private String configStatus;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "主门禁设备协议配置")
    private String remarks;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-30 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
