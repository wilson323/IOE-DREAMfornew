package net.lab1024.sa.common.entity.report;
import net.lab1024.sa.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表参数实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_parameter")
@Schema(description = "报表参数实体")
public class ReportParameterEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "参数ID")
    private Long parameterId;

    @Schema(description = "报表ID", required = true)
    private Long reportId;

    @Schema(description = "参数名称", required = true, example = "开始日期")
    private String parameterName;

    @Schema(description = "参数编码", required = true, example = "startDate")
    private String parameterCode;

    @Schema(description = "参数类型", required = true, example = "Date")
    private String parameterType;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "是否必填（1-是 0-否）")
    private Integer required;

    @Schema(description = "验证规则（正则表达式）")
    private String validationRule;

    @Schema(description = "排序号")
    private Integer sortOrder;
}
