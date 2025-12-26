package net.lab1024.sa.video.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * FFmpeg配置类
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "video.ffmpeg")
public class VideoFFmpegConfig {

    /**
     * FFmpeg可执行文件路径
     */
    private String executable = "ffmpeg";

    /**
     * 是否启用FFmpeg
     */
    private Boolean enabled = true;

    /**
     * 缩略图宽度
     */
    private Integer thumbnailWidth = 320;

    /**
     * 缩略图高度
     */
    private Integer thumbnailHeight = 180;

    /**
     * 缩略图格式
     */
    private String thumbnailFormat = "jpg";

    /**
     * 预览切片最大时长（秒）
     */
    private Integer previewMaxDuration = 30;

    /**
     * 预览切片格式
     */
    private String previewFormat = "mp4";

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds = 30;
}
