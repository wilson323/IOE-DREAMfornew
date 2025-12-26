package net.lab1024.sa.common.entity.report;
import net.lab1024.sa.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表定义实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_definition")
@Schema(description = "报表定义实体")
public class ReportDefinitionEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "报表ID")
    private Long reportId;

    @Schema(description = "报表名称", required = true, example = "每日考勤汇总表")
    private String reportName;

    @Schema(description = "报表编码", required = true)
    private String reportCode;

    @Schema(description = "报表类型（1-列表 2-汇总 3-图表 4-交叉表）")
    private Integer reportType;

    @Schema(description = "业务模块", example = "attendance")
    private String businessModule;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "数据源类型（1-SQL 2-API 3-静态）")
    private Integer dataSourceType;

    @Schema(description = "数据源配置（JSON格式）")
    private String dataSourceConfig;

    @Schema(description = "模板类型（1-Excel 2-PDF 3-Word）")
    private Integer templateType;

    @Schema(description = "模板配置（JSON格式）")
    private String templateConfig;

    @Schema(description = "导出格式（excel,pdf,word,csv）")
    private String exportFormats;

    @Schema(description = "报表描述")
    private String description;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @Schema(description = "排序号")
    private Integer sortOrder;
}
