package net.lab1024.sa.admin.module.system.area.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 区域查询表单
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Data
public class AreaQueryForm extends PageParam {

    @Schema(description = "区域编码")
    private String areaCode;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "区域类型")
    private String areaType;

    @Schema(description = "区域层级")
    private Integer areaLevel;

    @Schema(description = "父区域ID")
    private Long parentId;

    @Schema(description = "区域负责人ID")
    private Long managerId;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "是否查询删除的数据")
    private Boolean includeDeleted;

}
