package net.lab1024.sa.common.system.area.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新区域表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemAreaUpdateForm extends SystemAreaAddForm {

    @NotNull(message = "区域ID不能为空")
    private Long areaId;
}

