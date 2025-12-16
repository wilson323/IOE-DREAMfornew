package net.lab1024.sa.video.sdk;

/**
 * AI SDK提供者接口
 * <p>
 * 定义AI SDK的统一接口，支持多种AI引擎实现
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
public interface AiSdkProvider {

    /**
     * 获取SDK名称
     */
    String getName();

    /**
     * 初始化SDK
     */
    void initialize(AiSdkConfig config);

    /**
     * 销毁SDK
     */
    void destroy();

    /**
     * 检查SDK是否可用
     */
    boolean isAvailable();

    /**
     * 人脸检测
     *
     * @param imageData 图像数据
     * @return 检测结果
     */
    FaceDetectionResult detectFaces(byte[] imageData);

    /**
     * 人脸特征提取
     *
     * @param faceImage 人脸图像
     * @return 特征向量
     */
    byte[] extractFaceFeature(byte[] faceImage);

    /**
     * 人脸比对
     *
     * @param feature1 特征1
     * @param feature2 特征2
     * @return 相似度
     */
    double compareFaces(byte[] feature1, byte[] feature2);

    /**
     * 活体检测
     *
     * @param imageData 图像数据
     * @return 活体检测结果
     */
    LivenessResult detectLiveness(byte[] imageData);

    /**
     * 行为检测
     *
     * @param frameData 视频帧数据
     * @return 行为检测结果
     */
    BehaviorDetectionResult detectBehavior(byte[] frameData);

    /**
     * 人群计数
     *
     * @param frameData 视频帧数据
     * @return 人数
     */
    int countPeople(byte[] frameData);
}
