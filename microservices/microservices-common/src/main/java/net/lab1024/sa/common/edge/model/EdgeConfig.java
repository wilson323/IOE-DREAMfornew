package net.lab1024.sa.common.edge.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 边缘计算配置
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class EdgeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String configId;
    private String deviceId;
    private Integer inferenceMode;
    private Integer maxConcurrentTasks;
    private String modelPath;
    private Double threshold;
    private Integer batch_size;
    private Integer interval;
}
