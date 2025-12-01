package net.lab1024.sa.admin.module.smart.video.domain.form;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 视频设备新增表单
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
@Schema(description = "视频设备新增表单")
public class VideoDeviceAddForm {

    @Schema(description = "设备编码", example = "CAM-ENTRANCE-001")
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    private String deviceCode;

    @Schema(description = "设备名称", example = "入口监控摄像头")
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    private String deviceName;

    @Schema(description = "设备IP地址", example = "192.168.1.100")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "IP地址格式不正确")
    private String deviceIp;

    @Schema(description = "设备端口", example = "554")
    @Min(value = 1, message = "设备端口必须大于0")
    @Max(value = 65535, message = "设备端口不能超过65535")
    private Integer devicePort;

    @Schema(description = "流媒体端口", example = "554")
    @Min(value = 1, message = "流媒体端口必须大于0")
    @Max(value = 65535, message = "流媒体端口不能超过65535")
    private Integer streamPort;

    @Schema(description = "设备状态", example = "ONLINE",
            allowableValues = {"ONLINE", "OFFLINE", "FAULT"})
    private String deviceStatus;

    @Schema(description = "设备位置", example = "一楼大厅")
    @Size(max = 200, message = "设备位置长度不能超过200个字符")
    private String deviceLocation;

    @Schema(description = "设备类型", example = "IPC", allowableValues = {"IPC", "NVR", "DVR"})
    @NotBlank(message = "设备类型不能为空")
    @Size(max = 50, message = "设备类型长度不能超过50个字符")
    private String deviceType;

    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    @Schema(description = "RTSP地址", example = "rtsp://192.168.1.100:554/stream")
    @Size(max = 500, message = "RTSP地址长度不能超过500个字符")
    private String rtspUrl;

    @Schema(description = "登录用户名", example = "admin")
    @Size(max = 50, message = "登录用户名长度不能超过50个字符")
    private String username;

    @Schema(description = "登录密码", example = "password123")
    @Size(max = 100, message = "登录密码长度不能超过100个字符")
    private String password;

    @Schema(description = "是否启用云台控制", example = "1", allowableValues = {"0", "1"})
    private Integer ptzEnabled;

    @Schema(description = "录像开关", example = "1", allowableValues = {"0", "1"})
    private Integer recordEnabled;

    @Schema(description = "分辨率", example = "1080P")
    @Size(max = 20, message = "分辨率长度不能超过20个字符")
    private String resolution;

    @Schema(description = "帧率", example = "25")
    @Min(value = 1, message = "帧率必须大于0")
    @Max(value = 60, message = "帧率不能超过60")
    private Integer frameRate;

    @Schema(description = "视频制式", example = "PAL", allowableValues = {"PAL", "NTSC"})
    @Size(max = 10, message = "视频制式长度不能超过10个字符")
    private String videoFormat;

    @Schema(description = "设备描述", example = "入口监控摄像头，支持夜视功能")
    private String deviceDescription;

    @Schema(description = "最后在线时间", example = "2025-01-15T10:30:00")
    private LocalDateTime lastOnlineTime;
}


