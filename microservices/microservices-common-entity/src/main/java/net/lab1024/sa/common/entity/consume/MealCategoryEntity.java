package net.lab1024.sa.common.entity.consume;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜品分类实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_meal_category")
@Schema(description = "菜品分类实体")
public class MealCategoryEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称", required = true, example = "主食类")
    private String categoryName;

    @Schema(description = "分类编码", required = true)
    private String categoryCode;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
