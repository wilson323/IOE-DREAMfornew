package net.lab1024.sa.common.edge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI模型信息类
 * 用于描述边缘设备上部署的AI模型
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型类型 (CLASSIFICATION/DETECTION/SEGMENTATION等)
     */
    private String modelType;

    /**
     * 模型版本
     */
    private String version;

    /**
     * 模型文件路径
     */
    private String filePath;

    /**
     * 模型大小(MB)
     */
    private Long fileSize;

    /**
     * 输入格式
     */
    private String inputFormat;

    /**
     * 输出格式
     */
    private String outputFormat;

    /**
     * 模型精度
     */
    private String precision;

    /**
     * 推理框架 (ONNX/TensorRT/OpenVINO等)
     */
    private String framework;

    /**
     * 是否已加载
     */
    private Boolean loaded;

    /**
     * 部署时间
     */
    private LocalDateTime deployTime;

    /**
     * 模型描述
     */
    private String description;
}
