package net.lab1024.sa.attendance.engine.prediction.model;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预测模型（简化）
 *
 * <p>用于描述预测模型的元数据与参数配置。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionModel {

    /**
     * 模型类型标识
     */
    private String modelType;

    /**
     * 模型版本
     */
    private String version;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 模型参数
     */
    private Map<String, Object> parameters;
}

