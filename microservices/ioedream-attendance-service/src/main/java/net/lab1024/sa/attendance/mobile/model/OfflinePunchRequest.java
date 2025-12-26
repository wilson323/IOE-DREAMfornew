package net.lab1024.sa.attendance.mobile.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 离线打卡请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@Data
@Schema(description = "离线打卡请求")
public class OfflinePunchRequest {

    @Schema(description = "员工ID")
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @Schema(description = "离线打卡数据列表")
    private List<OfflinePunchData> punchDataList;
}
