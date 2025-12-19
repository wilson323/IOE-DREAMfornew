package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 解码器更新表单
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
public class VideoDecoderUpdateForm {

    /**
     * 解码器ID
     */
    @NotNull(message = "解码器ID不能为空")
    private Long decoderId;

    /**
     * 解码器名称
     */
    @Size(max = 100, message = "解码器名称长度不能超过100个字符")
    private String decoderName;

    /**
     * IP地址
     */
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
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 描述
     */
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
}
