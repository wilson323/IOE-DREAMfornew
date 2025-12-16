package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;

/**
 * 添加设备请求表单
 * 用于移动端添加新设备
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "添加设备请求表单")
public class AddDeviceRequest {

    @NotBlank(message = "设备编码不能为空")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "设备编码只能包含字母、数字、下划线和横线")
    @Schema(description = "设备编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "DEV_ACCESS_001")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "主入口门禁控制器")
    private String deviceName;

    @NotNull(message = "设备类型不能为空")
    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED,
           allowableValues = {"1", "2", "3", "4", "5", "6", "7", "8"}, example = "1")
    private Integer deviceType;

    @Schema(description = "设备子类型", example = "11")
    private Integer deviceSubType;

    @NotNull(message = "所属区域不能为空")
    @Schema(description = "所属区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long areaId;

    @NotBlank(message = "设备位置不能为空")
    @Size(max = 200, message = "设备位置长度不能超过200个字符")
    @Schema(description = "设备位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "A栋1楼大厅主入口")
    private String location;

    @Schema(description = "设备IP地址", example = "192.168.1.100")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "IP地址格式不正确")
    private String ipAddress;

    @Schema(description = "设备MAC地址", example = "AA:BB:CC:DD:EE:FF")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "MAC地址格式不正确")
    private String macAddress;

    @Schema(description = "设备型号", example = "IOE-ACCESS-2000")
    @Size(max = 50, message = "设备型号长度不能超过50个字符")
    private String deviceModel;

    @Schema(description = "固件版本", example = "v2.1.0")
    @Size(max = 20, message = "固件版本长度不能超过20个字符")
    private String firmwareVersion;

    @Schema(description = "设备厂商", example = "IOE科技")
    @Size(max = 100, message = "设备厂商长度不能超过100个字符")
    private String manufacturer;

    @Schema(description = "安装时间", example = "2024-01-15T10:30:00")
    private LocalDateTime installTime;

    @Schema(description = "设备描述", example = "主入口门禁控制器，支持人脸识别和刷卡")
    @Size(max = 500, message = "设备描述长度不能超过500个字符")
    private String description;

    @Schema(description = "是否支持远程控制", example = "true")
    private Boolean supportRemoteControl;

    @Schema(description = "是否支持固件升级", example = "true")
    private Boolean supportFirmwareUpgrade;

    @Schema(description = "设备扩展属性(JSON格式)",
           example = "{\"accessMode\":\"card+face\",\"antiPassback\":true,\"openTime\":3000}")
    private String extendedAttributes;

    @Schema(description = "权限级别", example = "1")
    @Min(value = 1, message = "权限级别不能小于1")
    @Max(value = 10, message = "权限级别不能大于10")
    private Integer permissionLevel;

    @Schema(description = "业务模块", example = "access")
    private String businessModule;

    // 安装相关参数

    @Schema(description = "安装负责人", example = "张工程师")
    @Size(max = 50, message = "安装负责人长度不能超过50个字符")
    private String installer;

    @Schema(description = "安装备注", example = "设备安装在标准机柜中，电源和网络连接正常")
    @Size(max = 300, message = "安装备注长度不能超过300个字符")
    private String installNotes;

    @Schema(description = "安装坐标-纬度", example = "39.9042")
    private Double latitude;

    @Schema(description = "安装坐标-经度", example = "116.4074")
    private Double longitude;

    @Schema(description = "安装高度(米)", example = "1.5")
    @Min(value = 0, message = "安装高度不能为负数")
    private Double installHeight;

    // 网络配置

    @Schema(description = "网络端口", example = "8080")
    @Min(value = 1, message = "网络端口不能小于1")
    @Max(value = 65535, message = "网络端口不能大于65535")
    private Integer networkPort;

    @Schema(description = "网络协议", example = "TCP", allowableValues = {"TCP", "UDP", "HTTP", "HTTPS"})
    private String networkProtocol;

    @Schema(description = "加密密钥", example = "")
    private String encryptionKey;

    // 验证信息

    @Schema(description = "设备序列号", example = "SN2024011500123")
    @Size(max = 50, message = "设备序列号长度不能超过50个字符")
    private String serialNumber;

    @Schema(description = "设备认证码", example = "AUTH20240115")
    @Size(max = 50, message = "设备认证码长度不能超过50个字符")
    private String authCode;
}