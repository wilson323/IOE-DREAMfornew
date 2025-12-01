package net.lab1024.sa.admin.module.consume.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 索引优化查询表单
 * <p>
 * 用于索引优化相关功能的查询参数封装
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "索引优化查询表单")
public class IndexOptimizationForm extends PageParam {

    /**
     * 表名
     */
    @Schema(description = "表名")
    private String tableName;

    /**
     * 索引类型
     */
    @Schema(description = "索引类型", example = "BTREE")
    private String indexType;

    /**
     * 优化建议类型
     */
    @Schema(description = "优化建议类型", example = "MISSING_INDEX")
    private String suggestionType;

    /**
     * 严重程度
     */
    @Schema(description = "严重程度", example = "HIGH")
    private String severity;

    /**
     * 是否包含历史记录
     */
    @Schema(description = "是否包含历史记录", example = "false")
    private Boolean includeHistory = false;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private String startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private String endTime;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "create_time")
    private String sortField = "create_time";

    /**
     * 排序方向
     */
    @Schema(description = "排序方向", example = "desc")
    private String sortDirection = "desc";
}