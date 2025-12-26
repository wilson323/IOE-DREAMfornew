package net.lab1024.sa.common.entity.video;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型实体
 *
 * @author IOE-DREAM AI Team
 * @since 2025-01-30
 */
@Data
@TableName("t_video_ai_model")
@Schema(description = "AI模型实体")
public class AiModelEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型版本号")
    private String modelVersion;

    @Schema(description = "模型类型")
    private String modelType;

    @Schema(description = "MinIO文件路径")
    private String filePath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件MD5值")
    private String fileMd5;

    @Schema(description = "模型状态（0-草稿 1-已发布 2-已废弃）")
    private Integer modelStatus;

    @Schema(description = "支持的事件类型（JSON数组）")
    private String supportedEvents;

    @Schema(description = "模型元数据（JSON格式）")
    private String modelMetadata;

    @Schema(description = "模型准确率")
    private BigDecimal accuracyRate;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "发布人ID")
    private Long publishedBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
