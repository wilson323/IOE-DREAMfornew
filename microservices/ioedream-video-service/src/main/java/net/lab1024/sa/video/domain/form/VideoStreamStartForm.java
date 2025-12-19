package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 视频流启动表单
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
public class VideoStreamStartForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 流类型
     * <p>
     * 枚举值：
     * - main - 主流
     * - sub - 子流
     * - mobile - 移动流
     * </p>
     */
    @Pattern(regexp = "^(main|sub|mobile)$", message = "流类型只能是main、sub或mobile")
    private String streamType;

    /**
     * 流协议
     * <p>
     * 枚举值：
     * - 1 - RTSP
     * - 2 - RTMP
     * - 3 - HLS
     * - 4 - WebRTC
     * - 5 - HTTP-FLV
     * </p>
     */
    private Integer protocol;

    /**
     * 视频质量
     * <p>
     * 枚举值：
     * - high - 高清
     * - medium - 标清
     * - low - 流畅
     * </p>
     */
    @Pattern(regexp = "^(high|medium|low)$", message = "视频质量只能是high、medium或low")
    private String quality;

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
     * 码率（kbps）
     */
    private Integer bitrate;

    /**
     * 是否启用音频
     * <p>
     * 0 - 不启用
     * 1 - 启用
     * </p>
     */
    private Integer audioEnabled;

    /**
     * 是否启用录制
     * <p>
     * 0 - 不录制
     * 1 - 录制
     * </p>
     */
    private Integer recordEnabled;

    /**
     * 录制时长（分钟）
     */
    private Integer recordDuration;

    /**
     * 客户端IP
     */
    @Size(max = 45, message = "客户端IP长度不能超过45个字符")
    private String clientIp;

    /**
     * 用户代理
     */
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    private String userAgent;

    /**
     * 会话超时时间（分钟）
     */
    private Integer sessionTimeout;

    /**
     * 是否启用动态码率
     * <p>
     * 0 - 不启用
     * 1 - 启用
     * </p>
     */
    private Integer dynamicBitrate;

    /**
     * 扩展参数（JSON格式）
     */
    @Size(max = 1000, message = "扩展参数长度不能超过1000个字符")
    private String extendedParams;
}