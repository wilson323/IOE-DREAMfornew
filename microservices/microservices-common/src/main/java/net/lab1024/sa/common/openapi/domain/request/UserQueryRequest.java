package net.lab1024.sa.common.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户查询请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "用户查询请求")
public class UserQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "搜索关键词（用户名、真实姓名、手机号、邮箱）", example = "张三")
    private String keyword;

    @Schema(description = "用户状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "角色ID", example = "1")
    private Long roleId;

    @Schema(description = "创建时间开始", example = "2025-01-01 00:00:00")
    private String createTimeStart;

    @Schema(description = "创建时间结束", example = "2025-12-31 23:59:59")
    private String createTimeEnd;

    @Schema(description = "排序字段", example = "createTime")
    private String sortField;

    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder;
}