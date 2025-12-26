package net.lab1024.sa.common.edge.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 推理请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class InferenceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private String deviceId;
    private String modelType;
    private String inputData;
    private Integer inferenceMode;
    private String parameters;
}
