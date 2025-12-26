package net.lab1024.sa.data.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据导出任务实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_data_export_task")
@Schema(description = "数据导出任务实体")
public class ExportTaskEntity {

    @TableId(type = IdType.INPUT)
    @Schema(description = "导出任务ID")
    private String exportTaskId;

    @Schema(description = "报表ID")
    private Long reportId;

    @Schema(description = "导出格式")
    private String exportFormat;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "请求参数")
    private String requestParams;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;
}
