package net.lab1024.sa.common.entity.report;
import net.lab1024.sa.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报表生成记录实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_generation")
@Schema(description = "报表生成记录实体")
public class ReportGenerationEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "生成记录ID")
    private Long generationId;

    @Schema(description = "报表ID", required = true)
    private Long reportId;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "请求参数（JSON格式）")
    private String parameters;

    @Schema(description = "生成方式（1-手动 2-定时 3-API）")
    private Integer generateType;

    @Schema(description = "文件类型（excel/pdf/word/csv）")
    private String fileType;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "状态（1-生成中 2-成功 3-失败）")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "生成时间")
    private LocalDateTime generateTime;

    @Schema(description = "创建人ID")
    private Long createUserId;
}
