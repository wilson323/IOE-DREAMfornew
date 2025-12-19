package net.lab1024.sa.attendance.realtime.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量事件处理结果
 * <p>
 * 封装批量事件处理的完整结果信息
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
public class BatchEventProcessingResult {

    /**
     * 批次ID
     */
    private String batchId;

    /**
     * 总事件数
     */
    private Integer totalEvents;

    /**
     * 成功处理的事件数
     */
    private Integer successfulEvents;

    /**
     * 处理失败的事件数
     */
    private Integer failedEvents;

    /**
     * 批次处理是否成功
     */
    private Boolean batchSuccessful;

    /**
     * 处理开始时间
     */
    private LocalDateTime processingStartTime;

    /**
     * 处理结束时间
     */
    private LocalDateTime processingEndTime;

    /**
     * 总处理耗时（毫秒）
     */
    private Long totalProcessingTime;

    /**
     * 单个事件处理结果列表
     */
    private List<EventProcessingResult> results;

    /**
     * 错误信息
     */
    private String errorMessage;
}
