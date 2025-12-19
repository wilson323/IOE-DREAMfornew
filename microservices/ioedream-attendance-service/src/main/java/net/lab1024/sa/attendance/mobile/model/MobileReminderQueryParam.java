package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 移动端提醒查询参数
 * <p>
 * 封装移动端提醒查询的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端提醒查询参数")
public class MobileReminderQueryParam {

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


