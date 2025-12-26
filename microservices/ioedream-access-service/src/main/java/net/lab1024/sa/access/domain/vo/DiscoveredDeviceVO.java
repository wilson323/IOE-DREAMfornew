package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发现的设备视图对象
 * <p>
 * 用于展示通过网络扫描发现的门禁设备信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发现的设备")
public class DiscoveredDeviceVO {

    /**
     * IP地址
     */
    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    /**
     * MAC地址
     */
    @Schema(description = "MAC地址", example = "00:1A:2B:3C:4D:5E")
    private String macAddress;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "门禁控制器-01")
    private String deviceName;

    /**
     * 设备型号
     */
    @Schema(description = "设备型号", example = "AC-2000")
    private String deviceModel;

    /**
     * 设备厂商
     */
    @Schema(description = "设备厂商", example = "Hikvision")
    private String deviceBrand;

    /**
     * 固件版本
     */
    @Schema(description = "固件版本", example = "v2.1.5")
    private String firmwareVersion;

    /**
     * 设备端口
     */
    @Schema(description = "设备端口", example = "80")
    private Integer port;

    /**
     * 是否已验证
     * true: TCP连接验证成功
     * false: 仅UDP发现，未验证
     */
    @Schema(description = "是否已验证", example = "true")
    private Boolean verified;

    /**
     * 设备位置（描述性信息）
     */
    @Schema(description = "设备位置", example = "A栋1楼大厅")
    private String location;

    /**
     * 设备类型
     * 1-门禁控制器 2-读卡器 3-生物识别设备
     */
    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    /**
     * 设备状态
     * 1-在线 2-离线 3-未知
     */
    @Schema(description = "设备状态", example = "1")
    private Integer deviceStatus;

    /**
     * 设备详细信息（JSON格式）
     * 包含设备发现过程中的其他信息
     */
    @Schema(description = "设备详细信息（JSON格式）")
    private String deviceInfo;

    /**
     * 发现时间戳
     */
    @Schema(description = "发现时间戳", example = "1706584800000")
    private Long discoveryTime;

    /**
     * 是否已添加到系统
     * true: 已在系统中存在
     * false: 新设备，未添加
     */
    @Schema(description = "是否已添加到系统", example = "false")
    private Boolean existsInSystem;

    /**
     * 设备协议
     * ONVIF, PRIVATE, SNMP
     */
    @Schema(description = "设备协议", example = "ONVIF")
    private String protocol;

    /**
     * 设备位置URL（用于SSDP等协议）
     */
    @Schema(description = "设备位置URL", example = "http://192.168.1.100:80/onvif/device_service")
    private String deviceLocation;

    /**
     * 服务器信息
     */
    @Schema(description = "服务器信息", example = "Linux/4.4.0 UPnP/1.0 Device/1.0")
    private String server;

    /**
     * USN（唯一服务名称）
     */
    @Schema(description = "唯一服务名称", example = "uuid:11223344-5566-7788-99aa-bbccddeeff00")
    private String usn;

    // ==================== 显式添加 getter/setter 方法以确保编译通过 ====================

    public String getIpAddress() {
        return ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public Boolean getExistsInSystem() {
        return existsInSystem;
    }

    public void setExistsInSystem(Boolean existsInSystem) {
        this.existsInSystem = existsInSystem;
    }
}
