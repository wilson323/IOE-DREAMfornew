package net.lab1024.sa.common.edge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 边缘推理结果类
 * 用于返回AI推理任务的执行结果
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 执行状态 (SUCCESS/FAILED/TIMEOUT)
     */
    private String status;

    /**
     * 推理结果数据
     */
    private Map<String, Object> resultData;

    /**
     * 置信度 (0.0-1.0)
     */
    private Double confidence;

    /**
     * 执行耗时(毫秒)
     */
    private Long executionTime;

    /**
     * 错误信息 (失败时)
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime endTime;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 扩展信息
     */
    private Map<String, Object> metadata;
}
