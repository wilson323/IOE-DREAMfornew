package net.lab1024.sa.common.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 字典创建DTO
 * 整合自ioedream-system-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Data
public class DictCreateDTO {

    @NotNull(message = "字典类型ID不能为空")
    private Long dictTypeId;

    @NotBlank(message = "字典类型编码不能为空")
    private String dictTypeCode;

    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    private String cssClass;
    private String listClass;
    private Integer isDefault;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}

