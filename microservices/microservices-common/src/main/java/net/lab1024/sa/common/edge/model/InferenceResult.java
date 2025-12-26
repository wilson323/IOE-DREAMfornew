package net.lab1024.sa.common.edge.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 推理结果
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class InferenceResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String resultId;
    private String requestId;
    private String deviceId;
    private String resultData;
    private Double confidence;
    private Integer statusCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime;

    private Long processDuration;
    private String errorMessage;
}
