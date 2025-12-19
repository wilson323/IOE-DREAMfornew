package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 移动端图表结果
 * <p>
 * 封装移动端图表响应结果
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
@Schema(description = "移动端图表结果")
public class MobileChartsResult {

    /**
     * 图表类型
     */
    @Schema(description = "图表类型", example = "ATTENDANCE_TREND")
    private String chartType;

    /**
     * 图表数据
     */
    @Schema(description = "图表数据")
    private Map<String, Object> chartData;
}
