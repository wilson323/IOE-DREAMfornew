package net.lab1024.sa.video.sdk;

import lombok.Data;

/**
 * AI SDK配置
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
public class AiSdkConfig {

    /**
     * SDK类型：BAIDU, FACE++, ARCSOFT, LOCAL
     */
    private String sdkType;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * Secret Key
     */
    private String secretKey;

    /**
     * API地址
     */
    private String apiUrl;

    /**
     * 模型路径（本地SDK使用）
     */
    private String modelPath;

    /**
     * 设备类型：CPU, GPU
     */
    private String deviceType = "CPU";

    /**
     * GPU设备ID
     */
    private int gpuDeviceId = 0;

    /**
     * 线程数
     */
    private int threadCount = 4;

    /**
     * 人脸检测阈值
     */
    private double faceDetectThreshold = 0.6;

    /**
     * 人脸比对阈值
     */
    private double faceCompareThreshold = 0.8;

    /**
     * 活体检测阈值
     */
    private double livenessThreshold = 0.9;
}
