package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 移动端请假记录查询参数
 * <p>
 * 封装移动端请假记录查询的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端请假记录查询参数")
public class MobileLeaveQueryParam {

    /**
     * 开始日期
     */
    @Schema(description = "开始日期", example = "2025-01-01")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期", example = "2025-01-31")
    private LocalDate endDate;

    /**
     * 请假类型
     */
    @Schema(description = "请假类型", example = "ANNUAL")
    private String leaveType;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;
}


