package net.lab1024.sa.visitor.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预约查询请求
 *
 * <p>用于开放API分页查询访客预约。</p>
 *
 * @author IOE-DREAM团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "预约查询请求")
public class AppointmentQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "手机号")
    private String phoneNumber;

    @Schema(description = "预约状态")
    private String appointmentStatus;

    @Schema(description = "预约类型")
    private String appointmentType;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;
}

