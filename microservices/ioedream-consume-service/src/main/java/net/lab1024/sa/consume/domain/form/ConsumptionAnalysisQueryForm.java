package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消费数据分析查询表单
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-12-24
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */
@Data
@Schema(description = "消费数据分析查询表单")
public class ConsumptionAnalysisQueryForm {

    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "时间周期类型: week-本周, month-本月, quarter-本季", example = "week")
    private String period = "week";

    @Schema(description = "开始日期", example = "2025-12-01")
    private String startDate;

    @Schema(description = "结束日期", example = "2025-12-24")
    private String endDate;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}
