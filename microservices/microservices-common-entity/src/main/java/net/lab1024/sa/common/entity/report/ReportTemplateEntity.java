package net.lab1024.sa.common.entity.report;
import net.lab1024.sa.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表模板实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_template")
@Schema(description = "报表模板实体")
public class ReportTemplateEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "报表ID", required = true)
    private Long reportId;

    @Schema(description = "模板名称", required = true, example = "考勤汇总Excel模板")
    private String templateName;

    @Schema(description = "模板类型（1-Excel 2-PDF 3-Word）")
    private Integer templateType;

    @Schema(description = "模板文件路径", required = true)
    private String filePath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "版本号", example = "1.0")
    private String version;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;
}
