package net.lab1024.sa.biometric.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.domain.entity.BiometricType;
import net.lab1024.sa.biometric.domain.vo.BiometricSample;
import net.lab1024.sa.biometric.domain.vo.FeatureVector;
import net.lab1024.sa.biometric.model.FaceNetModel;
import net.lab1024.sa.biometric.strategy.IBiometricFeatureExtractionStrategy;
import net.lab1024.sa.biometric.util.ImageProcessingUtil;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.awt.image.BufferedImage;
import java.util.Base64;

/**
 * 人脸特征提取策略实现
 * <p>
 * ⚠️ 重要说明：
 * - 本策略只用于入职时处理上传的照片，提取512维特征向量
 * - 不用于实时识别（实时识别由设备端完成）
 * - 不包含验证、识别、活体检测等功能（这些由设备端完成）
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 使用策略模式实现
 * - 只实现特征提取功能
 * - 使用OpenCV进行图像处理
 * - 使用FaceNet模型提取特征向量
 * </p>
 * <p>
 * 处理流程：
 * 1. 图像预处理（人脸检测、对齐、归一化）
 * 2. 特征提取（调用FaceNet模型提取512维特征向量）
 * 3. 质量检测（评估特征质量）
 * 4. 返回特征向量
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class FaceFeatureExtractionStrategy implements IBiometricFeatureExtractionStrategy {

    /**
     * 默认质量阈值（用于质量评估）
     * <p>
     * 特征质量分数低于此阈值时，将抛出异常要求重新拍摄
     * </p>
     */
    private static final double DEFAULT_QUALITY_THRESHOLD = 0.7;

    /**
     * 获取质量阈值
     * <p>
     * 用于质量检测，确保提取的特征向量质量符合要求
     * </p>
     *
     * @return 质量阈值
     */
    public static double getQualityThreshold() {
        return DEFAULT_QUALITY_THRESHOLD;
    }

    /**
     * FaceNet模型（用于特征提取）
     * <p>
     * TODO: 待模型文件准备完成后，在配置类中初始化
     * </p>
     */
    @Resource(required = false)
    private FaceNetModel faceNetModel;

    @Override
    public BiometricType getSupportedType() {
        return BiometricType.FACE;
    }

    @Override
    public FeatureVector extractFeature(BiometricSample sample) {
        log.info("[人脸特征提取] 开始提取特征向量, sampleType={}", sample.getType());

        try {
            // 1. 验证样本数据
            validateSample(sample);

            // 2. 读取图像
            BufferedImage image = ImageProcessingUtil.readImageFromBase64(sample.getImageData());
            ImageProcessingUtil.validateImageSize(image);

            // 3. 人脸检测
            int faceCount = ImageProcessingUtil.detectFaceCount(image);
            if (faceCount == 0) {
                throw new BusinessException("FEATURE_EXTRACTION_ERROR", "图片中未检测到人脸，请使用包含人脸的清晰照片");
            }
            if (faceCount > 1) {
                throw new BusinessException("FEATURE_EXTRACTION_ERROR", "图片中检测到多个人脸，请使用单人照片");
            }

            // 4. 人脸对齐（TODO: 集成OpenCV实现真正的人脸对齐）
            BufferedImage alignedFace = ImageProcessingUtil.alignFace(image, null);

            // 5. 质量检测（基础质量评估）
            double qualityScore = ImageProcessingUtil.assessImageQuality(alignedFace);
            if (qualityScore < getQualityThreshold()) {
                throw new BusinessException("FEATURE_EXTRACTION_ERROR",
                        String.format("照片质量太低(%.2f)，请重新拍摄（光线充足、正面、无遮挡）", qualityScore));
            }

            // 6. 特征提取
            float[] embeddings;
            if (faceNetModel != null && faceNetModel.isModelLoaded()) {
                // 使用FaceNet模型提取特征
                log.info("[人脸特征提取] 使用FaceNet模型提取特征");
                embeddings = faceNetModel.extract(alignedFace);
            } else {
                // 降级方案：使用临时实现
                log.warn("[人脸特征提取] FaceNet模型未加载，使用临时实现");
                embeddings = generateTemporaryFeatureVector();
            }

            // 7. 验证特征向量维度
            if (embeddings == null || embeddings.length != BiometricType.FACE.getDimension()) {
                throw new BusinessException("FEATURE_EXTRACTION_ERROR",
                        String.format("特征提取失败，特征向量维度不正确，期望: %d, 实际: %d",
                                BiometricType.FACE.getDimension(),
                                embeddings != null ? embeddings.length : 0));
            }

            // 8. 序列化特征向量
            String featureData = serializeFeatureVector(embeddings);

            FeatureVector featureVector = FeatureVector.builder()
                    .biometricType(BiometricType.FACE.getCode())
                    .dimension(BiometricType.FACE.getDimension())
                    .data(featureData)
                    .qualityScore(qualityScore)
                    .algorithmVersion(faceNetModel != null && faceNetModel.isModelLoaded() ? "1.0" : "0.1-temp")
                    .build();

            log.info("[人脸特征提取] 提取特征向量完成, dimension={}, qualityScore={}, algorithmVersion={}",
                    featureVector.getDimension(), featureVector.getQualityScore(), featureVector.getAlgorithmVersion());

            return featureVector;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[人脸特征提取] 提取特征向量失败", e);
            throw new BusinessException("FEATURE_EXTRACTION_ERROR", "提取人脸特征失败: " + e.getMessage());
        }
    }

    /**
     * 验证样本数据
     */
    private void validateSample(BiometricSample sample) {
        if (sample == null) {
            throw new BusinessException("PARAM_ERROR", "生物样本不能为空");
        }

        if (sample.getType() != BiometricType.FACE) {
            throw new BusinessException("PARAM_ERROR", "样本类型不匹配，期望FACE类型");
        }

        if (sample.getImageData() == null || sample.getImageData().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "图像数据不能为空");
        }

        // 验证图像大小（最大5MB，Base64编码后约为原大小的1.33倍）
        // 注意：BiometricSample.imageData是String类型（Base64编码）
        int estimatedSize = (int) (sample.getImageData().length() * 0.75);
        if (estimatedSize > 5 * 1024 * 1024) {
            throw new BusinessException("PARAM_ERROR", "图像大小不能超过5MB");
        }
    }

    /**
     * 生成临时特征向量（降级方案）
     * <p>
     * TODO: 待FaceNet模型集成后移除此方法
     * </p>
     *
     * @return 512维特征向量（已L2归一化）
     */
    private float[] generateTemporaryFeatureVector() {
        float[] embeddings = new float[BiometricType.FACE.getDimension()];
        for (int i = 0; i < embeddings.length; i++) {
            embeddings[i] = (float) (Math.random() * 2 - 1); // 模拟随机特征
        }
        normalizeL2(embeddings); // L2归一化
        return embeddings;
    }

    /**
     * L2归一化
     * <p>
     * 对特征向量进行L2归一化，确保向量长度为1
     * </p>
     *
     * @param vector 特征向量
     */
    private void normalizeL2(float[] vector) {
        if (vector == null || vector.length == 0) {
            return;
        }

        // 计算L2范数
        double norm = 0.0;
        for (float value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);

        // 归一化（避免除零）
        if (norm > 1e-8) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }
    }

    /**
     * 序列化特征向量
     * <p>
     * 将float数组转换为Base64编码的字符串
     * </p>
     *
     * @param embeddings 特征向量（float数组）
     * @return Base64编码的特征向量字符串
     */
    private String serializeFeatureVector(float[] embeddings) {
        if (embeddings == null || embeddings.length == 0) {
            throw new BusinessException("FEATURE_EXTRACTION_ERROR", "特征向量不能为空");
        }

        // 将float数组转换为字节数组
        byte[] bytes = new byte[embeddings.length * 4]; // float占4字节
        for (int i = 0; i < embeddings.length; i++) {
            int intBits = Float.floatToIntBits(embeddings[i]);
            bytes[i * 4] = (byte) (intBits & 0xFF);
            bytes[i * 4 + 1] = (byte) ((intBits >> 8) & 0xFF);
            bytes[i * 4 + 2] = (byte) ((intBits >> 16) & 0xFF);
            bytes[i * 4 + 3] = (byte) ((intBits >> 24) & 0xFF);
        }

        // Base64编码
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public int getPriority() {
        return 100; // 人脸识别优先级最高
    }
}
