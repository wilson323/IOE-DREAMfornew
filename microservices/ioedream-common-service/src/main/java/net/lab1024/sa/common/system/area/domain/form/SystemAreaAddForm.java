package net.lab1024.sa.common.system.area.domain.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增区域表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
public class SystemAreaAddForm {

    @NotBlank(message = "区域名称不能为空")
    private String areaName;

    private String areaCode;

    @NotBlank(message = "区域类型不能为空")
    private String areaType;

    /**
     * 父区域ID（可选，根节点默认为0）
     */
    private Long parentId;

    private Integer level;

    private Integer sortOrder;

    private Long managerId;

    private Integer capacity;

    private String description;

    private Integer status;
}
