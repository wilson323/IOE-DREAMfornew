package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 移动端设备信息VO
 * 移动端设备识别和状态信息
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端设备信息")
public class MobileDeviceInfoVO {

    @Schema(description = "设备ID", example = "MOBILE_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "移动设备")
    private String deviceName;

    @Schema(description = "设备类型", example = "MOBILE")
    private String deviceType;

    @Schema(description = "设备类型描述", example = "移动端设备")
    private String deviceTypeDescription;

    @Schema(description = "设备品牌", example = "Apple")
    private String deviceBrand;

    @Schema(description = "设备型号", example = "iPhone 14 Pro")
    private String deviceModel;

    @Schema(description = "操作系统", example = "iOS")
    private String operatingSystem;

    @Schema(description = "系统版本", example = "17.2.1")
    private String osVersion;

    @Schema(description = "应用版本", example = "1.0.0")
    private String appVersion;

    @Schema(description = "设备唯一标识", example = "A1B2C3D4E5F6")
    private String deviceIdentifier;

    @Schema(description = "位置", example = "公司园区")
    private String location;

    @Schema(description = "详细地址", example = "北京市朝阳区xxx公司")
    private String address;

    @Schema(description = "经度", example = "116.404")
    private Double longitude;

    @Schema(description = "纬度", example = "39.915")
    private Double latitude;

    @Schema(description = "设备状态", example = "ONLINE")
    private String status;

    @Schema(description = "状态描述", example = "在线")
    private String statusDescription;

    @Schema(description = "最后活跃时间", example = "2025-01-30 14:30:00")
    private LocalDateTime lastActiveTime;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "MAC地址", example = "00:1A:2B:3C:4D:5E")
    private String macAddress;

    @Schema(description = "网络类型", example = "WiFi")
    private String networkType;

    @Schema(description = "网络运营商", example = "中国移动")
    private String networkOperator;

    @Schema(description = "信号强度", example = "-50")
    private Integer signalStrength;

    @Schema(description = "电池电量", example = "85")
    private Integer batteryLevel;

    @Schema(description = "是否充电中", example = "false")
    private Boolean isCharging;

    @Schema(description = "设备所有者", example = "张三")
    private String owner;

    @Schema(description = "所有者ID", example = "1001")
    private Long ownerId;

    @Schema(description = "部门", example = "技术部")
    private String department;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "设备权限", example = "USER")
    private String permissionLevel;

    @Schema(description = "权限描述", example = "普通用户")
    private String permissionDescription;

    @Schema(description = "是否可信设备", example = "true")
    private Boolean isTrustedDevice;

    @Schema(description = "设备认证状态", example = "VERIFIED")
    private String verificationStatus;

    @Schema(description = "认证时间", example = "2025-01-01 00:00:00")
    private LocalDateTime verificationTime;

    @Schema(description = "设备注册时间", example = "2024-01-01 00:00:00")
    private LocalDateTime registrationTime;

    @Schema(description = "最后更新时间", example = "2025-01-30 14:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "设备标签", example = "正式员工,iPhone,技术部")
    private String tags;

    @Schema(description = "设备备注", example = "员工自用设备")
    private String remark;

    @Schema(description = "设备配置信息")
    private Object deviceConfiguration;

    @Schema(description = "设备能力列表")
    private java.util.List<String> capabilities;

    @Schema(description = "支持的支付方式")
    private java.util.List<String> supportedPaymentMethods;

    @Schema(description = "安全级别", example = "HIGH")
    private String securityLevel;

    @Schema(description = "是否启用生物识别", example = "true")
    private Boolean biometricEnabled;

    @Schema(description = "生物识别类型", example = "FACE,FINGERPRINT")
    private String biometricTypes;
}