package net.lab1024.sa.admin.module.oa.document.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文档搜索表单
 *
 * 控制层用于高级搜索/普通搜索的公共载体，包含分页与基本过滤条件。
 */
@Data
public class DocumentSearchForm {

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 文档类型
     */
    private String documentType;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 起始时间（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
     */
    private String startDate;

    /**
     * 结束时间（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
     */
    private String endDate;

    /**
     * 页码
     */
    @NotNull
    @Min(1)
    private Integer pageNum;

    /**
     * 页大小
     */
    @NotNull
    @Min(1)
    private Integer pageSize;
}
