package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.common.domain.PageParam;

import java.time.LocalDateTime;

/**
 * 设备导入查询表单
 * <p>
 * 用于查询设备导入批次记录：
 * - 分页查询
 * - 按状态过滤
 * - 按时间范围过滤
 * - 按操作人过滤
 * - 按关键字搜索
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "设备导入查询表单")
public class DeviceImportQueryForm extends PageParam {

    @Schema(description = "导入状态: 0-处理中 1-成功 2-部分失败 3-全部失败", example = "1")
    private Integer importStatus;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-01-31T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "关键字搜索（批次名称、文件名）", example = "2025年1月")
    private String keyword;

    @Schema(description = "排序字段", example = "createTime")
    private String sortField;

    @Schema(description = "排序方式: asc-升序 desc-降序", example = "desc")
    private String sortOrder;
}
