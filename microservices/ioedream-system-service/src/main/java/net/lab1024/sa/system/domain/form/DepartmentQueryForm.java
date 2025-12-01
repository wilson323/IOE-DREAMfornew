package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.page.PageForm;

/**
 * 部门查询表单
 * <p>
 * 用于部门列表查询的数据传输对象
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "部门查询表单")
public class DepartmentQueryForm extends PageForm {

    /**
     * 部门名称（模糊查询）
     */
    @Schema(description = "部门名称（模糊查询）", example = "技术")
    private String departmentName;

    /**
     * 部门编码（精确查询）
     */
    @Schema(description = "部门编码（精确查询）", example = "TECH")
    private String departmentCode;

    /**
     * 父部门ID
     */
    @Schema(description = "父部门ID", example = "0")
    private Long parentId;

    /**
     * 部门层级
     */
    @Schema(description = "部门层级", example = "1")
    private Integer level;

    /**
     * 部门状态（1-启用，0-禁用）
     */
    @Schema(description = "部门状态", example = "1", allowableValues = { "0", "1" })
    private Integer status;

    /**
     * 部门负责人（模糊查询）
     */
    @Schema(description = "部门负责人（模糊查询）", example = "张三")
    private String manager;

    /**
     * 创建时间开始
     */
    @Schema(description = "创建时间开始", example = "2025-01-01 00:00:00")
    private String createTimeStart;

    /**
     * 创建时间结束
     */
    @Schema(description = "创建时间结束", example = "2025-01-31 23:59:59")
    private String createTimeEnd;

    /**
     * 是否包含子部门（true-包含子部门，false-只查询当前层级）
     */
    @Schema(description = "是否包含子部门", example = "true")
    private Boolean includeChildren;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "sortNumber", allowableValues = { "departmentId", "sortNumber",
            "createTime", "updateTime" })
    private String sortField;

    /**
     * 排序方式（asc-升序，desc-降序）
     */
    @Schema(description = "排序方式", example = "asc", allowableValues = { "asc", "desc" })
    private String sortOrder;

    /**
     * 是否只查询启用的部门
     */
    @Schema(description = "是否只查询启用的部门", example = "true")
    private Boolean onlyEnabled;
}
