package net.lab1024.sa.biometric.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.model.FaceNetModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FaceNet模型配置类
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Configuration注解
 * - 使用@ConditionalOnProperty控制是否加载模型
 * - 完整的错误处理
 * </p>
 * <p>
 * ⚠️ 重要说明:
 * - 模型文件需要提前准备（TensorFlow/PyTorch/ONNX格式）
 * - 模型路径通过配置文件指定（biometric.facenet.model-path）
 * - 如果模型文件不存在或未配置，将不加载模型，使用降级方案
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
@Slf4j
public class FaceNetModelConfiguration {

    /**
     * 创建FaceNet模型Bean
     * <p>
     * 只有当配置文件中指定了模型路径时才创建Bean
     * </p>
     *
     * @param modelPath 模型文件路径（从配置文件读取）
     * @return FaceNetModel实例
     */
    @Bean
    @ConditionalOnProperty(name = "biometric.facenet.model-path", matchIfMissing = false)
    public FaceNetModel faceNetModel(@Value("${biometric.facenet.model-path:}") String modelPath) {
        if (modelPath == null || modelPath.isEmpty()) {
            log.warn("[FaceNet配置] 模型路径未配置，将使用降级方案（临时特征提取）");
            return null;
        }

        log.info("[FaceNet配置] 开始初始化FaceNet模型, modelPath={}", modelPath);

        FaceNetModel model = new FaceNetModel();
        try {
            model.initialize(modelPath);
            log.info("[FaceNet配置] FaceNet模型初始化成功");
        } catch (Exception e) {
            log.error("[FaceNet配置] FaceNet模型初始化失败, modelPath={}", modelPath, e);
            // 不抛出异常，允许使用降级方案
            log.warn("[FaceNet配置] 将使用降级方案（临时特征提取）");
            return null;
        }

        return model;
    }
}
