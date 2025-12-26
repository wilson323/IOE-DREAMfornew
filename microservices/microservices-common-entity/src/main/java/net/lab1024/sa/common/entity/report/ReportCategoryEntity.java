package net.lab1024.sa.common.entity.report;
import net.lab1024.sa.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表分类实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_category")
@Schema(description = "报表分类实体")
public class ReportCategoryEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称", required = true, example = "考勤报表")
    private String categoryName;

    @Schema(description = "分类编码", required = true)
    private String categoryCode;

    @Schema(description = "父分类ID", example = "0")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;
}
