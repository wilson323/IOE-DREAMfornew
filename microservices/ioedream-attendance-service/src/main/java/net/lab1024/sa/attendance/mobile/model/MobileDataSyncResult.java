package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动端数据同步结果
 * <p>
 * 封装移动端数据同步响应结果
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
@Schema(description = "移动端数据同步结果")
public class MobileDataSyncResult {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 同步时间戳
     */
    @Schema(description = "同步时间戳", example = "1703020800000")
    private Long syncTimestamp;

    /**
     * 同步时间
     */
    @Schema(description = "同步时间", example = "2025-01-30T10:00:00")
    private java.time.LocalDateTime syncTime;

    /**
     * 消息
     */
    @Schema(description = "消息", example = "数据同步成功")
    private String message;
}
