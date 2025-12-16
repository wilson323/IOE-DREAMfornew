package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 视频设备更新表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * - 包含完整的业务字段验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
public class VideoDeviceUpdateForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 设备名称
     */
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    private String deviceName;

    /**
     * 设备子类型
     * <p>
     * 枚举值：
     * - 1 - 枪机
     * - 2 - 球机
     * - 3 - 半球机
     * - 4 - 一体机
     * </p>
     */
    private Integer deviceSubType;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 设备IP地址
     */
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
             message = "设备IP地址格式不正确")
    private String deviceIp;

    /**
     * 设备端口
     */
    private Integer devicePort;

    /**
     * 设备用户名
     */
    @Size(max = 50, message = "设备用户名长度不能超过50个字符")
    private String deviceUsername;

    /**
     * 设备密码
     */
    @Size(max = 100, message = "设备密码长度不能超过100个字符")
    private String devicePassword;

    /**
     * 设备协议
     * <p>
     * 枚举值：
     * - 1 - RTSP
     * - 2 - RTMP
     * - 3 - HTTP
     * - 4 - TCP
     * - 5 - UDP
     * </p>
     */
    private Integer protocol;

    /**
     * 视频流地址
     */
    @Size(max = 500, message = "视频流地址长度不能超过500个字符")
    private String streamUrl;

    /**
     * 设备厂商
     */
    @Size(max = 50, message = "设备厂商长度不能超过50个字符")
    private String manufacturer;

    /**
     * 设备型号
     */
    @Size(max = 100, message = "设备型号长度不能超过100个字符")
    private String model;

    /**
     * 设备序列号
     */
    @Size(max = 100, message = "设备序列号长度不能超过100个字符")
    private String serialNumber;

    /**
     * 安装位置
     */
    @Size(max = 200, message = "安装位置长度不能超过200个字符")
    private String installLocation;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 海拔高度（米）
     */
    private Double altitude;

    /**
     * 是否支持PTZ控制
     * <p>
     * 0 - 不支持
     * 1 - 支持
     * </p>
     */
    private Integer ptzSupported;

    /**
     * 是否支持音频
     * <p>
     * 0 - 不支持
     * 1 - 支持
     * </p>
     */
    private Integer audioSupported;

    /**
     * 是否支持夜视
     * <p>
     * 0 - 不支持
     * 1 - 支持
     * </p>
     */
    private Integer nightVisionSupported;

    /**
     * 是否支持AI分析
     * <p>
     * 0 - 不支持
     * 1 - 支持
     * </p>
     */
    private Integer aiSupported;

    /**
     * 分辨率
     */
    @Size(max = 20, message = "分辨率长度不能超过20个字符")
    private String resolution;

    /**
     * 帧率
     */
    private Integer frameRate;

    /**
     * 启用标志
     * <p>
     * 0 - 禁用
     * 1 - 启用
     * </p>
     */
    private Integer enabledFlag;

    /**
     * 设备状态
     * <p>
     * 枚举值：
     * - ONLINE - 在线
     * - OFFLINE - 离线
     * - MAINTAIN - 故障
     * </p>
     */
    private String deviceStatus;

    /**
     * 备注信息
     */
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remark;
}