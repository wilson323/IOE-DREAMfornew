package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 移动端离线数据上传结果
 * <p>
 * 封装移动端离线数据上传响应结果
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
@Schema(description = "移动端离线数据上传结果")
public class MobileOfflineDataUploadResult {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 同步结果列表
     */
    @Schema(description = "同步结果列表")
    private List<OfflineSyncResult> syncResults;

    /**
     * 消息
     */
    @Schema(description = "消息", example = "离线数据上传成功")
    private String message;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间", example = "2025-01-30T10:00:00")
    private java.time.LocalDateTime uploadTime;
}
