package net.lab1024.sa.device.domain.form;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设备协议配置表单
 * <p>
 * 用于配置设备的通信协议参数
 *
 * @author IOE-DREAM Team
 * @date 2025-01-30
 */
@Data
@Schema(description = "设备协议配置表单")
public class DeviceProtocolConfigForm {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 协议类型（TCP、UDP、HTTP、WEBSOCKET、MQTT等）
     */
    @NotBlank(message = "协议类型不能为空")
    @Schema(description = "协议类型", example = "TCP", requiredMode = Schema.RequiredMode.REQUIRED)
    private String protocolType;

    /**
     * 服务器地址（IP或域名）
     */
    @NotBlank(message = "服务器地址不能为空")
    @Schema(description = "服务器地址", example = "192.168.1.100", requiredMode = Schema.RequiredMode.REQUIRED)
    private String host;

    /**
     * 端口号
     */
    @NotNull(message = "端口号不能为空")
    @Schema(description = "端口号", example = "8080", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer port;

    /**
     * 连接超时时间（秒）
     */
    @Schema(description = "连接超时时间（秒）", example = "30")
    private Integer connectTimeout = 30;

    /**
     * 读取超时时间（秒）
     */
    @Schema(description = "读取超时时间（秒）", example = "60")
    private Integer readTimeout = 60;

    /**
     * 是否启用SSL/TLS
     */
    @Schema(description = "是否启用SSL/TLS", example = "false")
    private Boolean enableSsl = false;

    /**
     * SSL证书路径（可选）
     */
    @Schema(description = "SSL证书路径", example = "/path/to/cert.pem")
    private String sslCertPath;

    /**
     * 用户名（如果需要认证）
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 密码（如果需要认证）
     */
    @Schema(description = "密码", example = "password123")
    private String password;

    /**
     * 认证token（如果需要token认证）
     */
    @Schema(description = "认证token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String authToken;

    /**
     * 自定义配置参数（JSON格式）
     */
    @Schema(description = "自定义配置参数（JSON格式）", example = "{\"keepAlive\": true, \"heartbeatInterval\": 60}")
    private String customConfig;

    /**
     * 协议特定参数（Map格式）
     */
    @Schema(description = "协议特定参数")
    private Map<String, Object> protocolParams;

    /**
     * 是否启用心跳检测
     */
    @Schema(description = "是否启用心跳检测", example = "true")
    private Boolean enableHeartbeat = true;

    /**
     * 心跳间隔（秒）
     */
    @Schema(description = "心跳间隔（秒）", example = "30")
    private Integer heartbeatInterval = 30;

    /**
     * 心跳超时时间（秒）
     */
    @Schema(description = "心跳超时时间（秒）", example = "60")
    private Integer heartbeatTimeout = 60;

    /**
     * 重连间隔（秒）
     */
    @Schema(description = "重连间隔（秒）", example = "10")
    private Integer reconnectInterval = 10;

    /**
     * 最大重连次数
     */
    @Schema(description = "最大重连次数", example = "5")
    private Integer maxReconnectAttempts = 5;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "主门禁设备协议配置")
    private String remarks;
}
