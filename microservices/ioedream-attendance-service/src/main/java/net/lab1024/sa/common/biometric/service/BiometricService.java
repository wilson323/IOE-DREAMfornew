package net.lab1024.sa.common.biometric.service;

/**
 * 生物识别服务接口
 * <p>
 * 提供1:N人脸识别、指纹识别等生物识别功能
 * 注意：根据架构设计，设备端识别优先，此服务仅作为fallback
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface BiometricService {

    /**
     * 1:N人脸识别
     * <p>
     * 用于设备端无法识别时的中心识别
     * </p>
     *
     * @param faceImageData 人脸图像数据（base64编码）
     * @param deviceId 设备ID
     * @return 识别到的用户ID，失败返回null
     */
    Long recognizeFace(String faceImageData, String deviceId);

    /**
     * 1:N指纹识别
     *
     * @param fingerprintData 指纹特征数据
     * @param deviceId 设备ID
     * @return 识别到的用户ID，失败返回null
     */
    Long recognizeFingerprint(String fingerprintData, String deviceId);

    /**
     * 检查生物识别服务是否可用
     *
     * @return true=服务可用
     */
    boolean isAvailable();
}
