package net.lab1024.sa.visitor.service;

import io.github.resilience4j.annotation.CircuitBreaker;
import io.github.resilience4j.annotation.TimeLimiter;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.concurrent.CompletableFuture;

/**
 * 访客人脸识别服务接口
 * <p>
 * 内存优化设计原则：
 * - 接口精简，职责单一
 * - 使用异步处理，提高并发性能
 * - 熔断器保护，防止级联故障
 * - 批量操作支持，减少IO开销
 * - 合理的缓存策略，减少重复计算
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VisitorFaceRecognitionService {

    /**
     * 人脸特征注册
     * <p>
     * 为访客注册人脸特征，用于后续识别验证
     * 支持多张人脸照片注册，提高识别准确率
     * 自动进行人脸质量检测和特征提取
     * </p>
     *
     * @param visitorId 访客ID
     * @param faceImages 人脸图像数据（Base64编码）
     * @return 注册结果，包含特征ID
     */
    @CircuitBreaker(name = "visitorFaceRecognition", fallbackMethod = "registerFaceFeatureFallback")
    @TimeLimiter(name = "visitorFaceRecognition")
    CompletableFuture<ResponseDTO<String>> registerFaceFeature(Long visitorId, String[] faceImages);

    /**
     * 人脸识别验证
     * <p>
     * 实时人脸识别验证，支持活体检测
     * 返回匹配度和访客基本信息
     * 支持多人脸场景下的最佳匹配
     * </p>
     *
     * @param faceImage 人脸图像数据（Base64编码）
     * @param deviceId 设备ID（可选，用于设备关联）
     * @return 识别结果，包含访客信息和匹配度
     */
    @CircuitBreaker(name = "visitorFaceRecognition", fallbackMethod = "recognizeFaceFallback")
    @TimeLimiter(name = "visitorFaceRecognition")
    CompletableFuture<ResponseDTO<Object>> recognizeFace(String faceImage, String deviceId);

    /**
     * 批量人脸识别验证
     * <p>
     * 批量处理多张人脸图像，提高处理效率
     * 适用于批量访客验证场景
     * 返回每张图像的识别结果
     * </p>
     *
     * @param faceImages 人脸图像数组
     * @param deviceId 设备ID
     * @return 批量识别结果
     */
    @CircuitBreaker(name = "batchFaceRecognition")
    CompletableFuture<ResponseDTO<Object>> batchRecognizeFaces(String[] faceImages, String deviceId);

    /**
     * 更新人脸特征
     * <p>
     * 更新访客的人脸特征信息
     * 支持增量更新和全量替换
     * 自动清除旧的特征数据
     * </p>
     *
     * @param visitorId 访客ID
     * @param featureId 特征ID
     * @param faceImages 新的人脸图像数据
     * @return 更新结果
     */
    @CircuitBreaker(name = "visitorFaceRecognition", fallbackMethod = "updateFaceFeatureFallback")
    @TimeLimiter(name = "visitorFaceRecognition")
    CompletableFuture<ResponseDTO<Void>> updateFaceFeature(Long visitorId, String featureId, String[] faceImages);

    /**
     * 删除人脸特征
     * <p>
     * 删除指定访客的人脸特征数据
     * 同时清除相关的缓存数据
     * 支持批量删除操作
     * </p>
     *
     * @param visitorId 访客ID
     * @param featureId 特征ID（可选，不传则删除所有特征）
     * @return 删除结果
     */
    @CircuitBreaker(name = "visitorFaceRecognition", fallbackMethod = "deleteFaceFeatureFallback")
    @TimeLimiter(name = "visitorFaceRecognition")
    CompletableFuture<ResponseDTO<Void>> deleteFaceFeature(Long visitorId, String featureId);

    /**
     * 人脸特征比对
     * <p>
     * 比较两张人脸图像的相似度
     * 支持多种比对算法和阈值设置
     * 返回详细的比对结果
     * </p>
     *
     * @param faceImage1 第一张人脸图像
     * @param faceImage2 第二张人脸图像
     * @param threshold 比对阈值（0-1）
     * @return 比对结果，包含相似度得分
     */
    @CircuitBreaker(name = "visitorFaceRecognition")
    CompletableFuture<ResponseDTO<Object>> compareFaces(String faceImage1, String faceImage2, Double threshold);

    /**
     * 人脸质量检测
     * <p>
     * 检测人脸图像的质量，包括清晰度、角度、光照等
     * 返回质量评分和建议
     * 支持实时质量反馈
     * </p>
     *
     * @param faceImage 人脸图像数据
     * @return 质量检测结果
     */
    @CircuitBreaker(name = "visitorFaceRecognition")
    CompletableFuture<ResponseDTO<Object>> detectFaceQuality(String faceImage);

    /**
     * 活体检测
     * <p>
     * 检测人脸是否为真实活体，防止照片、视频等攻击
     * 支持多种活体检测算法
     * 返回活体检测结果和置信度
     * </p>
     *
     * @param faceImage 人脸图像数据
     * @param actionType 动作类型（blink、nod、mouth等）
     * @return 活体检测结果
     */
    @CircuitBreaker(name = "visitorFaceRecognition")
    CompletableFuture<ResponseDTO<Object>> livenessDetection(String faceImage, String actionType);

    /**
     * 获取访客人脸特征列表
     * <p>
     * 查询指定访客的所有人脸特征信息
     * 包含注册时间、使用次数等统计数据
     * 支持分页查询
     * </p>
     *
     * @param visitorId 访客ID
     * @return 人脸特征列表
     */
    @CircuitBreaker(name = "visitorFaceRecognition")
    CompletableFuture<ResponseDTO<Object>> getVisitorFaceFeatures(Long visitorId);

    /**
     * 人脸识别统计
     * <p>
     * 获取人脸识别系统的统计数据
     * 包括识别成功率、平均响应时间、错误分布等
     * 用于系统性能分析和优化
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @CircuitBreaker(name = "visitorFaceRecognition")
    CompletableFuture<ResponseDTO<Object>> getFaceRecognitionStatistics(String startTime, String endTime);

    // ==================== 降级处理方法 ====================

    /**
     * 注册人脸特征降级处理
     */
    default CompletableFuture<ResponseDTO<String>> registerFaceFeatureFallback(
            Long visitorId,
            String[] faceImages,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("FACE_RECOGNITION_SERVICE_UNAVAILABLE", "人脸识别服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 人脸识别降级处理
     */
    default CompletableFuture<ResponseDTO<Object>> recognizeFaceFallback(
            String faceImage,
            String deviceId,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("FACE_RECOGNITION_SERVICE_UNAVAILABLE", "人脸识别服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 更新人脸特征降级处理
     */
    default CompletableFuture<ResponseDTO<Void>> updateFaceFeatureFallback(
            Long visitorId,
            String featureId,
            String[] faceImages,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("FACE_RECOGNITION_SERVICE_UNAVAILABLE", "人脸识别服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 删除人脸特征降级处理
     */
    default CompletableFuture<ResponseDTO<Void>> deleteFaceFeatureFallback(
            Long visitorId,
            String featureId,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("FACE_RECOGNITION_SERVICE_UNAVAILABLE", "人脸识别服务暂时不可用，请稍后重试")
        );
    }
}