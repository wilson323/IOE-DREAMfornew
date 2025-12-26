package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.domain.form.BaseQueryForm;

/**
 * 消费餐次分类查询表单（临时基础实现）
 * <p>
 * 用于快速解决编译错误
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费餐次分类查询表单")
public class ConsumeMealCategoryQueryForm extends BaseQueryForm {

    @Schema(description = "分类名称", example = "主食")
    private String categoryName;

    @Schema(description = "分类编码", example = "MAIN_FOOD")
    private String categoryCode;

    @Schema(description = "状态", example = "1")
    private Integer status;
}