package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型视图对象
 * <p>
 * 用于AI模型的前端展示：
 * 1. 模型基础信息
 * 2. 模型文件信息
 * 3. 模型状态信息
 * 4. 发布信息
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "AI模型视图对象")
public class AiModelVO {

    @Schema(description = "模型ID", example = "1")
    private Long modelId;

    @Schema(description = "模型名称", example = "fall_detection_v2")
    private String modelName;

    @Schema(description = "模型版本", example = "2.0.0")
    private String modelVersion;

    @Schema(description = "模型类型", example = "YOLOv8")
    private String modelType;

    @Schema(description = "文件路径", example = "/models/yolov8_fall_v2.onnx")
    private String filePath;

    @Schema(description = "文件大小（字节）", example = "52428800")
    private Long fileSize;

    @Schema(description = "文件大小（MB）", example = "50.00")
    private String fileSizeMb;

    @Schema(description = "文件MD5", example = "a1b2c3d4e5f6...")
    private String fileMd5;

    @Schema(description = "支持的事件类型", example = "[\"FALL_DETECTION\", \"ABNORMAL_GAIT\"]")
    private String supportedEvents;

    @Schema(description = "模型状态（0-草稿 1-已发布 2-已弃用）", example = "1")
    private Integer modelStatus;

    @Schema(description = "模型状态名称", example = "已发布")
    private String modelStatusName;

    @Schema(description = "准确率（0.0-1.0）", example = "0.95")
    private BigDecimal accuracyRate;

    @Schema(description = "准确率百分比", example = "95%")
    private String accuracyPercent;

    @Schema(description = "发布时间", example = "2025-01-30T10:00:00")
    private LocalDateTime publishTime;

    @Schema(description = "发布人ID", example = "1")
    private Long publishedBy;

    @Schema(description = "发布人名称", example = "管理员")
    private String publishedByName;

    @Schema(description = "创建时间", example = "2025-01-29T15:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-30T10:00:00")
    private LocalDateTime updateTime;
}
