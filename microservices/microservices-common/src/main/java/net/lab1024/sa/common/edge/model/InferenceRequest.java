package net.lab1024.sa.common.edge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 边缘推理请求类
 * 用于向边缘设备提交AI推理任务
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceRequest implements Serializable {

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
     * 任务类型 (FACE_RECOGNITION/OBJECT_DETECTION/BEHAVIOR_ANALYSIS等)
     */
    private String taskType;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 输入数据 (Base64编码的图像/视频帧等)
     */
    private String inputData;

    /**
     * 输入数据类型 (IMAGE/VIDEO/AUDIO等)
     */
    private String inputType;

    /**
     * 推理参数
     */
    private Map<String, Object> parameters;

    /**
     * 优先级 (1-10, 10最高)
     */
    private Integer priority;

    /**
     * 是否异步执行
     */
    private Boolean async;

    /**
     * 回调URL (异步任务完成后的回调地址)
     */
    private String callbackUrl;
}
