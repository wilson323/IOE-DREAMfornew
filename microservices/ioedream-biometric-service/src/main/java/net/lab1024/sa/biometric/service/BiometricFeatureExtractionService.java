package net.lab1024.sa.biometric.service;

import net.lab1024.sa.biometric.domain.vo.FeatureVector;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 生物特征提取服务接口
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - Service层只定义接口，不包含实现
 * - 使用@Resource依赖注入
 * - 完整的业务方法定义
 * </p>
 * <p>
 * 核心职责:
 * - 从用户上传照片提取特征向量
 * - 支持5大生物识别类型
 * - 质量检测和验证
 * </p>
 * <p>
 * ⚠️ 重要说明:
 * - 只在用户入职/更新模板时调用
 * - 不用于实时识别（实时识别由设备端完成）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface BiometricFeatureExtractionService {

    /**
     * 提取人脸特征
     * <p>
     * 场景：用户上传人脸照片时，提取512维特征向量
     * </p>
     *
     * @param photo 人脸照片文件
     * @return 特征向量
     */
    ResponseDTO<FeatureVector> extractFaceFeature(MultipartFile photo);

    /**
     * 提取指纹特征
     * <p>
     * 场景：用户上传指纹图像时，提取指纹特征向量
     * </p>
     *
     * @param fingerprintImage 指纹图像文件
     * @return 特征向量
     */
    ResponseDTO<FeatureVector> extractFingerprintFeature(MultipartFile fingerprintImage);

    /**
     * 提取虹膜特征
     * <p>
     * 场景：用户上传虹膜图像时，提取虹膜特征向量
     * </p>
     *
     * @param irisImage 虹膜图像文件
     * @return 特征向量
     */
    ResponseDTO<FeatureVector> extractIrisFeature(MultipartFile irisImage);

    /**
     * 提取掌纹特征
     * <p>
     * 场景：用户上传掌纹图像时，提取掌纹特征向量
     * </p>
     *
     * @param palmImage 掌纹图像文件
     * @return 特征向量
     */
    ResponseDTO<FeatureVector> extractPalmFeature(MultipartFile palmImage);

    /**
     * 提取声纹特征
     * <p>
     * 场景：用户上传语音文件时，提取声纹特征向量
     * </p>
     *
     * @param voiceFile 语音文件
     * @return 特征向量
     */
    ResponseDTO<FeatureVector> extractVoiceFeature(MultipartFile voiceFile);

    /**
     * 通用特征提取方法
     * <p>
     * 根据生物识别类型自动选择对应的提取策略
     * </p>
     *
     * @param file 生物特征文件
     * @param biometricType 生物识别类型
     * @return 特征向量
     */
    ResponseDTO<FeatureVector> extractFeature(MultipartFile file, Integer biometricType);

    /**
     * 验证特征质量
     * <p>
     * 检查提取的特征向量是否符合质量要求
     * </p>
     *
     * @param featureVector 特征向量
     * @return 质量验证结果
     */
    ResponseDTO<Boolean> validateFeatureQuality(FeatureVector featureVector);
}
