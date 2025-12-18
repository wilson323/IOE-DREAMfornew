package net.lab1024.sa.common.edge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 边缘设备配置类
 * 用于配置边缘设备的性能参数和AI模型参数
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 最大并发推理数
     */
    private Integer maxConcurrentInferences;

    /**
     * 推理超时时间(毫秒)
     */
    private Long inferenceTimeout;

    /**
     * 模型缓存大小(MB)
     */
    private Integer modelCacheSize;

    /**
     * GPU使用配置
     */
    private Boolean useGpu;

    /**
     * GPU设备ID
     */
    private String gpuDeviceId;

    /**
     * 批处理大小
     */
    private Integer batchSize;

    /**
     * 模型精度 (FP32/FP16/INT8)
     */
    private String modelPrecision;

    /**
     * 是否启用模型预热
     */
    private Boolean enableWarmup;

    /**
     * 心跳间隔(秒)
     */
    private Integer heartbeatInterval;
}
