package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 移动端排班信息结果
 * <p>
 * 封装移动端排班信息响应结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端排班信息结果")
public class MobileScheduleResult {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 排班列表
     */
    @Schema(description = "排班列表")
    private List<Map<String, Object>> schedules;

    /**
     * 总记录数（分页使用）
     */
    @Schema(description = "总记录数", example = "100")
    private Long totalCount;

    /**
     * 当前页码（分页使用）
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    /**
     * 每页大小（分页使用）
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;

    /**
     * 是否有下一页（分页使用）
     */
    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    /**
     * 是否有上一页（分页使用）
     */
    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrev;
}


