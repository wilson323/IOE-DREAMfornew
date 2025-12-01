package net.lab1024.sa.base.module.area.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量人员区域关联表单
 * 用于批量添加人员与区域的关联关系
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "批量人员区域关联表单")
public class BatchPersonAreaRelationForm {

    /**
     * 人员ID列表
     * 不能为空，最多支持1000个人员
     */
    @NotEmpty(message = "人员ID列表不能为空")
    @Schema(description = "人员ID列表", example = "[1, 2, 3]")
    private List<Long> personIds;

    /**
     * 区域ID
     * 不能为空
     */
    @NotNull(message = "区域ID不能为空")
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 关联类型
     * PRIMARY-主区域，SECONDARY-次区域，TEMPORARY-临时区域
     */
    @Schema(description = "关联类型", example = "PRIMARY", allowableValues = {"PRIMARY", "SECONDARY", "TEMPORARY"})
    private String relationType = "PRIMARY";

    /**
     * 有效开始时间
     * 可为空，空表示立即生效
     */
    @Schema(description = "有效开始时间", example = "2025-11-25T09:00:00")
    private java.time.LocalDateTime validFrom;

    /**
     * 有效结束时间
     * 可为空，空表示永久有效
     */
    @Schema(description = "有效结束时间", example = "2025-12-31T23:59:59")
    private java.time.LocalDateTime validTo;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "批量导入主区域关联")
    private String remark;

    /**
     * 跳过已存在的关联
     * true-跳过已存在的关联，false-更新已存在的关联
     */
    @Schema(description = "跳过已存在的关联", example = "true")
    private Boolean skipExisting = true;

    /**
     * 是否启用验证
     * true-验证人员是否存在，false-跳过验证
     */
    @Schema(description = "是否启用验证", example = "true")
    private Boolean enableValidation = true;
}