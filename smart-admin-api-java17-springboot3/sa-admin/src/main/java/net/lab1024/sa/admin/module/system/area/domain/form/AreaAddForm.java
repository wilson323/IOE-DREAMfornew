package net.lab1024.sa.admin.module.system.area.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 区域添加表单
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Data
public class AreaAddForm {

    @Schema(description = "区域编码")
    @NotBlank(message = "区域编码不能为空")
    @Length(max = 100, message = "区域编码长度不能超过100")
    private String areaCode;

    @Schema(description = "区域名称")
    @NotBlank(message = "区域名称不能为空")
    @Length(max = 200, message = "区域名称长度不能超过200")
    private String areaName;

    @Schema(description = "区域类型")
    @NotBlank(message = "区域类型不能为空")
    @Length(max = 50, message = "区域类型长度不能超过50")
    private String areaType;

    @Schema(description = "区域层级")
    private Integer areaLevel;

    @Schema(description = "父区域ID")
    @NotNull(message = "父区域ID不能为空")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "区域配置JSON")
    private Map<String, Object> areaConfig;

    @Schema(description = "区域描述")
    @Length(max = 5000, message = "区域描述长度不能超过5000")
    private String areaDesc;

    @Schema(description = "区域负责人ID")
    private Long managerId;

    @Schema(description = "联系电话")
    @Length(max = 20, message = "联系电话长度不能超过20")
    private String contactPhone;

    @Schema(description = "详细地址")
    @Length(max = 5000, message = "详细地址长度不能超过5000")
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

}
