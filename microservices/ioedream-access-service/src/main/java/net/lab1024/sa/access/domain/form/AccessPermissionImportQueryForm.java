package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁权限导入查询表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用OpenAPI 3.0注解（@Schema）
 * - 提供清晰的字段说明
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "门禁权限导入查询表单")
public class AccessPermissionImportQueryForm {

    /**
     * 批次名称
     */
    @Schema(description = "批次名称", example = "2025-01权限导入")
    private String batchName;

    /**
     * 导入状态
     */
    @Schema(description = "导入状态（PENDING-待处理 PROCESSING-处理中 SUCCESS-成功 FAILED-失败 CANCELLED-已取消）",
             example = "SUCCESS")
    private String importStatus;

    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间（yyyy-MM-dd HH:mm:ss）", example = "2025-01-01 00:00:00")
    private String startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间（yyyy-MM-dd HH:mm:ss）", example = "2025-01-31 23:59:59")
    private String endTime;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;
}
