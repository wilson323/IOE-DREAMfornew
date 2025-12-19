package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 移动端通知列表结果
 * <p>
 * 封装移动端通知列表响应结果
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
@Schema(description = "移动端通知列表结果")
public class MobileNotificationsResult {

    /**
     * 通知列表
     */
    @Schema(description = "通知列表")
    private List<Map<String, Object>> notifications;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "10")
    private Integer totalCount;

    /**
     * 未读数量
     */
    @Schema(description = "未读数量", example = "5")
    private Integer unreadCount;
}
