package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门禁设备响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁设备响应")
public class AccessDeviceResponse {

    @Schema(description = "设备ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "主门禁")
    private String deviceName;

    @Schema(description = "设备类型", example = "access", allowableValues = {"access", "turnstile", "gate"})
    private String deviceType;

    @Schema(description = "设备类型名称", example = "门禁控制器")
    private String deviceTypeName;

    @Schema(description = "设备型号", example = "MODEL-A100")
    private String deviceModel;

    @Schema(description = "设备厂商", example = "海康威视")
    private String deviceManufacturer;

    @Schema(description = "设备状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer deviceStatus;

    @Schema(description = "设备状态名称", example = "在线")
    private String deviceStatusName;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "区域名称", example = "一楼大厅")
    private String areaName;

    @Schema(description = "设备位置", example = "一楼大厅东侧")
    private String location;

    @Schema(description = "设备IP", example = "192.168.1.100")
    private String deviceIp;

    @Schema(description = "设备端口", example = "8000")
    private Integer devicePort;

    @Schema(description = "最后在线时间", example = "2025-12-16T15:30:00")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "最后离线时间", example = "2025-12-16T10:30:00")
    private LocalDateTime lastOfflineTime;

    @Schema(description = "安装时间", example = "2025-01-01T10:30:00")
    private LocalDateTime installTime;

    @Schema(description = "固件版本", example = "v1.2.3")
    private String firmwareVersion;

    @Schema(description = "是否支持远程控制", example = "true")
    private Boolean supportRemoteControl;

    @Schema(description = "是否支持人脸识别", example = "true")
    private Boolean supportFaceRecognition;

    @Schema(description = "是否支持指纹识别", example = "true")
    private Boolean supportFingerprint;

    @Schema(description = "是否支持刷卡", example = "true")
    private Boolean supportCard;

    @Schema(description = "是否支持密码", example = "true")
    private Boolean supportPassword;

    @Schema(description = "是否支持二维码", example = "true")
    private Boolean supportQrCode;

    @Schema(description = "是否支持体温检测", example = "true")
    private Boolean supportTemperature;

    @Schema(description = "设备描述", example = "一楼大厅主入口门禁设备")
    private String description;

    @Schema(description = "备注", example = "")
    private String remark;
}
