package net.lab1024.sa.common.system.area.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 区域分页查询表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
public class SystemAreaQueryForm {

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    private String keyword;

    private String areaType;

    private Integer status;
}

