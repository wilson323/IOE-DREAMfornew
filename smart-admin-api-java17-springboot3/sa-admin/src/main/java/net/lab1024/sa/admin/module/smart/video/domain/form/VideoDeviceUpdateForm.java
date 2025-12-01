package net.lab1024.sa.admin.module.smart.video.domain.form;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 视频设备更新表单
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
@Schema(description = "视频设备更新表单")
public class VideoDeviceUpdateForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备名称
     */
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    @Schema(description = "设备名称", example = "主通道摄像机")
    private String deviceName;

    /**
     * 设备IP地址
     */
    @jakarta.validation.constraints.Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "IP地址格式不正确")
    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String deviceIp;

    /**
     * 设备端口
     */
    @Min(value = 1, message = "设备端口必须大于0")
    @Max(value = 65535, message = "设备端口不能超过65535")
    @Schema(description = "设备端口", example = "554")
    private Integer devicePort;

    /**
     * 流媒体端口
     */
    @Min(value = 1, message = "流媒体端口必须大于等于1")
    @Max(value = 65535, message = "流媒体端口不能超过65535")
    @Schema(description = "流媒体端口", example = "554")
    private Integer streamPort;

    /**
     * 设备状态
     */
    @Schema(description = "设备状态", example = "ONLINE",
            allowableValues = {"ONLINE", "OFFLINE", "FAULT"})
    private String deviceStatus;

    /**
     * 设备位置
     */
    @Size(max = 200, message = "设备位置长度不能超过200个字符")
    @Schema(description = "设备位置", example = "一楼大厅")
    private String deviceLocation;

    /**
     * 所属区域ID
     */
    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    /**
     * RTSP地址
     */
    @Size(max = 500, message = "RTSP地址长度不能超过500个字符")
    @Schema(description = "RTSP地址", example = "rtsp://192.168.1.100:554/stream")
    private String rtspUrl;

    /**
     * 登录用户名
     */
    @Size(max = 50, message = "登录用户名长度不能超过50个字符")
    @Schema(description = "登录用户名", example = "admin")
    private String username;

    /**
     * 登录密码（明文，系统自动加密）
     */
    @Size(max = 100, message = "登录密码长度不能超过100个字符")
    @Schema(description = "登录密码", example = "password123")
    private String password;

    /**
     * 是否启用云台控制
     */
    @Schema(description = "是否启用云台控制", example = "1", allowableValues = {"0", "1"})
    private Integer ptzEnabled;

    /**
     * 是否启用录像
     */
    @Schema(description = "是否启用录像", example = "1", allowableValues = {"0", "1"})
    private Integer recordEnabled;

    /**
     * 分辨率
     */
    @Size(max = 20, message = "分辨率长度不能超过20个字符")
    @Schema(description = "分辨率", example = "1080P")
    private String resolution;

    /**
     * 帧率
     */
    @Min(value = 1, message = "帧率必须大于0")
    @Max(value = 60, message = "帧率不能超过60")
    @Schema(description = "帧率", example = "25")
    private Integer frameRate;

    /**
     * 视频制式
     */
    @Size(max = 10, message = "视频制式长度不能超过10个字符")
    @Schema(description = "视频制式", example = "PAL", allowableValues = {"PAL", "NTSC"})
    private String videoFormat;

    /**
     * 设备描述
     */
    @Schema(description = "设备描述", example = "主通道高清摄像机，支持夜视")
    private String deviceDescription;

    /**
     * 最后在线时间
     */
    @Schema(description = "最后在线时间", example = "2025-01-15T10:30:00")
    private LocalDateTime lastOnlineTime;
}
