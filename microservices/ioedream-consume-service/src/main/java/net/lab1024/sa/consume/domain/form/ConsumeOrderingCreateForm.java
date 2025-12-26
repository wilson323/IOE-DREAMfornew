package net.lab1024.sa.consume.domain.form;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 在线订餐创建表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Schema(description = "在线订餐创建表单")
public class ConsumeOrderingCreateForm {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotEmpty(message = "菜品ID列表不能为空")
    @Schema(description = "菜品ID列表", example = "[1, 2, 3]")
    private List<Long> dishIds;

    @NotNull(message = "订餐日期不能为空")
    @Schema(description = "订餐日期", example = "2025-12-25")
    private LocalDate orderDate;

    @NotBlank(message = "餐别类型不能为空")
    @Schema(description = "餐别类型", example = "LUNCH", allowableValues = {"BREAKFAST", "LUNCH", "DINNER"})
    private String mealType;

    @Schema(description = "备注", example = "不要辣")
    private String remark;
}
