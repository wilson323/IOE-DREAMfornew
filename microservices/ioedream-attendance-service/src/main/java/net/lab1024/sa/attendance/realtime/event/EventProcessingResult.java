package net.lab1024.sa.attendance.realtime.event;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事件处理结果
 * <p>
 * 封装事件处理的完整结果信息
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
public class EventProcessingResult {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 处理器名称
     */
    private String processorName;

    /**
     * 处理是否成功
     */
    private Boolean success;

    /**
     * 处理时间戳（毫秒）
     */
    private Long processingTime;

    /**
     * 处理后的数据
     */
    private Map<String, Object> processedData;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 处理耗时（毫秒）
     */
    private Long duration;
}
