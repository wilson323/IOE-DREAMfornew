package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 解码器新增表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class VideoDecoderAddForm {

    /**
     * 解码器编码（唯一）
     */
    @NotBlank(message = "解码器编码不能为空")
    @Size(max = 50, message = "解码器编码长度不能超过50个字符")
    private String decoderCode;

    /**
     * 解码器名称
     */
    @NotBlank(message = "解码器名称不能为空")
    @Size(max = 100, message = "解码器名称长度不能超过100个字符")
    private String decoderName;

    /**
     * 设备类型：1-硬解码器 2-软解码器 3-混合解码器
     */
    private Integer decoderType;

    /**
     * 品牌厂商
     */
    @Size(max = 50, message = "品牌厂商长度不能超过50个字符")
    private String brand;

    /**
     * 设备型号
     */
    @Size(max = 50, message = "设备型号长度不能超过50个字符")
    private String model;

    /**
     * 序列号
     */
    @Size(max = 64, message = "序列号长度不能超过64个字符")
    private String serialNumber;

    /**
     * IP地址
     */
    @NotBlank(message = "IP地址不能为空")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
             message = "IP地址格式不正确")
    private String ipAddress;

    /**
     * 端口号
     */
    @Positive(message = "端口号必须大于0")
    private Integer port;

    /**
     * 用户名
     */
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    /**
     * 密码
     */
    @Size(max = 256, message = "密码长度不能超过256个字符")
    private String password;

    /**
     * 最大通道数
     */
    @Positive(message = "最大通道数必须大于0")
    private Integer maxChannels;

    /**
     * 最大分辨率
     */
    @Size(max = 20, message = "最大分辨率长度不能超过20个字符")
    private String maxResolution;

    /**
     * 支持的格式
     */
    @Size(max = 200, message = "支持的格式长度不能超过200个字符")
    private String supportedFormats;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 描述
     */
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
}
