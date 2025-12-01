package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典类型更新表单
 * <p>
 * 用于字典类型更新的数据验证和传输
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据字典类型更新表单")
public class DictTypeUpdateForm extends DictTypeAddForm {

    /**
     * 字典类型ID
     */
    @NotNull(message = "字典类型ID不能为空")
    @Schema(description = "字典类型ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long dictTypeId;

    /**
     * 更新时间（乐观锁版本）
     */
    @Schema(description = "更新时间（乐观锁版本）", example = "2025-01-01 12:00:00")
    private String updateTime;
}
