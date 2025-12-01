package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门更新表单
 * <p>
 * 用于部门更新的数据验证和传输
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "部门更新表单")
public class DepartmentUpdateForm extends DepartmentAddForm {

    /**
     * 部门ID
     */
    @NotNull(message = "部门ID不能为空")
    @Schema(description = "部门ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long departmentId;

    /**
     * 更新时间（乐观锁版本）
     */
    @Schema(description = "更新时间（乐观锁版本）", example = "2025-01-01 12:00:00")
    private String updateTime;
}
