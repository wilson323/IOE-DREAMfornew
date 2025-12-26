package net.lab1024.sa.common.edge.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模型信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class ModelInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String modelId;
    private String modelName;
    private String modelVersion;
    private String modelType;
    private String modelPath;
    private Long modelSize;
    private String checksum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdateTime;

    private String description;
    private Integer status;
}
