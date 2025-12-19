package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 移动端日历查询参数
 * <p>
 * 封装移动端日历查询的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端日历查询参数")
public class MobileCalendarQueryParam {

    /**
     * 年份
     */
    @Schema(description = "年份", example = "2025")
    private Integer year;

    /**
     * 月份
     */
    @Schema(description = "月份", example = "1")
    private Integer month;
}
