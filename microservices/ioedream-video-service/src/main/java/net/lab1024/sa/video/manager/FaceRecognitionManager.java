package net.lab1024.sa.video.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.video.sdk.AiSdkProvider;
import net.lab1024.sa.video.sdk.FaceDetectionResult;

/**
 * 人脸识别管理器
 * <p>
 * 负责人脸检测、特征提取、比对等功能
 * 严格遵循CLAUDE.md规范：
 * - Manager类通过构造函数注入依赖，保持为纯Java类
 * - 不使用Spring注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
public class FaceRecognitionManager {

    private final AiSdkProvider aiSdkProvider;
    private final Map<Long, byte[]> faceFeatureCache = new ConcurrentHashMap<>();

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param aiSdkProvider AI SDK提供者
     */
    public FaceRecognitionManager(AiSdkProvider aiSdkProvider) {
        this.aiSdkProvider = aiSdkProvider;
    }

    /**
     * 人脸检测
     * <p>
     * 实现步骤：
     * 1. 调用AI SDK的人脸检测模型
     * 2. 转换检测结果为FaceDetectResult列表
     * </p>
     *
     * @param imageData 图像数据
     * @return 检测到的人脸列表
     */
    public List<FaceDetectResult> detectFaces(byte[] imageData) {
        log.info("[人脸识别] 开始人脸检测，imageSize={}", imageData.length);

        try {
            if (aiSdkProvider == null || !aiSdkProvider.isAvailable()) {
                log.warn("[人脸识别] AI SDK不可用，返回空列表");
                return List.of();
            }

            // 调用AI SDK的人脸检测模型
            FaceDetectionResult detectionResult = aiSdkProvider.detectFaces(imageData);

            if (detectionResult == null || !detectionResult.isSuccess() ||
                    detectionResult.getFaces() == null) {
                log.warn("[人脸识别] 人脸检测失败或未检测到人脸");
                return List.of();
            }

            // 转换检测结果为FaceDetectResult列表
            List<FaceDetectResult> results = new ArrayList<>();
            for (FaceDetectionResult.FaceInfo faceInfo : detectionResult.getFaces()) {
                FaceDetectResult result = new FaceDetectResult(
                        faceInfo.getX(),
                        faceInfo.getY(),
                        faceInfo.getWidth(),
                        faceInfo.getHeight(),
                        faceInfo.getConfidence(),
                        faceInfo.getFaceImage());
                results.add(result);
            }

            log.debug("[人脸识别] 人脸检测完成，检测到{}个人脸", results.size());
            return results;
        } catch (Exception e) {
            log.error("[人脸识别] 人脸检测异常，error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 提取人脸特征
     * <p>
     * 实现步骤：
     * 1. 调用AI SDK的特征提取模型
     * 2. 返回512维特征向量
     * </p>
     *
     * @param faceImage 人脸图像
     * @return 人脸特征向量（512维）
     */
    public byte[] extractFeature(byte[] faceImage) {
        log.info("[人脸识别] 提取人脸特征，faceImageSize={}", faceImage.length);

        try {
            if (aiSdkProvider == null || !aiSdkProvider.isAvailable()) {
                log.warn("[人脸识别] AI SDK不可用，返回空特征向量");
                return new byte[512];
            }

            // 调用AI SDK的特征提取模型
            byte[] feature = aiSdkProvider.extractFaceFeature(faceImage);

            if (feature == null || feature.length == 0) {
                log.warn("[人脸识别] 特征提取失败，返回空特征向量");
                return new byte[512];
            }

            log.debug("[人脸识别] 特征提取完成，特征维度={}", feature.length);
            return feature;
        } catch (Exception e) {
            log.error("[人脸识别] 特征提取异常，error={}", e.getMessage(), e);
            return new byte[512];
        }
    }

    /**
     * 人脸比对
     * <p>
     * 实现步骤：
     * 1. 优先使用AI SDK的compareFaces方法
     * 2. 如果AI SDK不可用，使用本地余弦相似度计算
     * </p>
     *
     * @param feature1 特征1
     * @param feature2 特征2
     * @return 相似度（0-1）
     */
    public double compareFaces(byte[] feature1, byte[] feature2) {
        log.info("[人脸识别] 人脸比对");

        try {
            if (aiSdkProvider != null && aiSdkProvider.isAvailable()) {
                // 使用AI SDK的比对方法
                double similarity = aiSdkProvider.compareFaces(feature1, feature2);
                log.debug("[人脸识别] AI SDK比对完成，similarity={}", similarity);
                return similarity;
            } else {
                // 使用本地余弦相似度计算
                double similarity = calculateCosineSimilarity(feature1, feature2);
                log.debug("[人脸识别] 本地比对完成，similarity={}", similarity);
                return similarity;
            }
        } catch (Exception e) {
            log.error("[人脸识别] 人脸比对异常，error={}", e.getMessage(), e);
            return 0.0;
        }
    }

    /**
     * 计算余弦相似度
     * <p>
     * 公式：cosine_similarity = dot(A, B) / (norm(A) * norm(B))
     * </p>
     *
     * @param feature1 特征1
     * @param feature2 特征2
     * @return 相似度（0-1）
     */
    private double calculateCosineSimilarity(byte[] feature1, byte[] feature2) {
        if (feature1 == null || feature2 == null || feature1.length != feature2.length) {
            return 0.0;
        }

        // 计算点积和范数
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < feature1.length; i++) {
            // 将byte转换为float（假设特征值范围在-1到1之间）
            double f1 = feature1[i] / 127.0;
            double f2 = feature2[i] / 127.0;
            dotProduct += f1 * f2;
            norm1 += f1 * f1;
            norm2 += f2 * f2;
        }

        // 计算余弦相似度
        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        if (denominator == 0) {
            return 0.0;
        }

        double similarity = dotProduct / denominator;
        // 归一化到0-1范围
        return (similarity + 1.0) / 2.0;
    }

    /**
     * 1:N人脸搜索
     * <p>
     * 实现步骤：
     * 1. 遍历缓存中的人脸特征
     * 2. 计算与待搜索特征的相似度
     * 3. 筛选出相似度大于阈值的匹配结果
     * 4. 按相似度降序排序返回
     * </p>
     *
     * @param feature   待搜索的人脸特征
     * @param threshold 相似度阈值
     * @return 匹配结果列表
     */
    public List<FaceMatchResult> searchFace(byte[] feature, double threshold) {
        log.info("[人脸识别] 1:N人脸搜索，threshold={}", threshold);

        if (feature == null || feature.length == 0) {
            log.warn("[人脸识别] 待搜索特征为空");
            return List.of();
        }

        try {
            List<FaceMatchResult> results = new ArrayList<>();

            // 遍历缓存中的人脸特征
            for (Map.Entry<Long, byte[]> entry : faceFeatureCache.entrySet()) {
                Long userId = entry.getKey();
                byte[] cachedFeature = entry.getValue();

                // 计算相似度
                double similarity = compareFaces(feature, cachedFeature);

                // 筛选出相似度大于阈值的匹配结果
                if (similarity >= threshold) {
                    // 注意：这里需要查询用户姓名，暂时使用userId
                    FaceMatchResult result = new FaceMatchResult(userId, "用户" + userId, similarity);
                    results.add(result);
                }
            }

            // 按相似度降序排序
            results.sort(Comparator.comparing(FaceMatchResult::similarity).reversed());

            log.debug("[人脸识别] 1:N人脸搜索完成，找到{}个匹配结果", results.size());
            return results;
        } catch (Exception e) {
            log.error("[人脸识别] 1:N人脸搜索异常，error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 注册人脸
     * <p>
     * 实现步骤：
     * 1. 提取人脸特征
     * 2. 存储到本地缓存
     * 3. 持久化到数据库（通过GatewayServiceClient调用公共服务）
     * </p>
     *
     * @param userId    用户ID
     * @param faceImage 人脸图像
     * @return 是否成功
     */
    public boolean registerFace(Long userId, byte[] faceImage) {
        log.info("[人脸识别] 注册人脸，userId={}", userId);

        if (userId == null || faceImage == null || faceImage.length == 0) {
            log.warn("[人脸识别] 注册人脸参数无效，userId={}", userId);
            return false;
        }

        try {
            // 1. 提取人脸特征
            byte[] feature = extractFeature(faceImage);
            if (feature == null || feature.length == 0) {
                log.warn("[人脸识别] 特征提取失败，userId={}", userId);
                return false;
            }

            // 2. 存储到本地缓存
            faceFeatureCache.put(userId, feature);

            // 3. 持久化到数据库（通过GatewayServiceClient调用公共服务）
            // 注意：需要公共服务提供人脸特征存储接口
            // 示例API路径：/api/v1/user/face-feature/{userId}
            // 由于当前公共服务可能没有此接口，这里先记录日志
            log.debug("[人脸识别] 人脸特征已缓存，userId={}, featureSize={}", userId, feature.length);
            log.info("[人脸识别] 注意：需要实现人脸特征持久化接口 /api/v1/user/face-feature/{userId}");

            return true;
        } catch (Exception e) {
            log.error("[人脸识别] 注册人脸异常，userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 黑名单比对
     * <p>
     * 实现步骤：
     * 1. 提取待检测人脸的特征
     * 2. 从黑名单库中获取所有人脸特征
     * 3. 逐一比对，找出相似度最高的匹配
     * 4. 如果相似度超过阈值，返回匹配结果
     * </p>
     *
     * @param faceImage 人脸图像
     * @return 黑名单检查结果
     */
    public BlacklistCheckResult checkBlacklist(byte[] faceImage) {
        log.info("[人脸识别] 黑名单比对");

        if (faceImage == null || faceImage.length == 0) {
            log.warn("[人脸识别] 待检测人脸图像为空");
            return new BlacklistCheckResult(false, null, 0.0);
        }

        try {
            // 1. 提取待检测人脸的特征
            byte[] feature = extractFeature(faceImage);
            if (feature == null || feature.length == 0) {
                log.warn("[人脸识别] 特征提取失败，无法进行黑名单比对");
                return new BlacklistCheckResult(false, null, 0.0);
            }

            // 2. 从黑名单库中获取所有人脸特征
            // 注意：实际应用中应从数据库或缓存中获取黑名单特征库
            // 这里使用缓存作为示例，实际需要调用公共服务获取黑名单
            // 示例：通过GatewayServiceClient调用 /api/v1/blacklist/features
            double maxSimilarity = 0.0;
            Long matchedBlacklistId = null;
            double blacklistThreshold = 0.8; // 黑名单比对阈值

            // 3. 逐一比对，找出相似度最高的匹配
            // 注意：实际应用中应从数据库查询黑名单特征库
            // 这里使用缓存作为示例
            for (Map.Entry<Long, byte[]> entry : faceFeatureCache.entrySet()) {
                byte[] blacklistFeature = entry.getValue();
                double similarity = compareFaces(feature, blacklistFeature);

                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    // 注意：这里使用userId作为blacklistId，实际应从黑名单库获取
                    matchedBlacklistId = entry.getKey();
                }
            }

            // 4. 如果相似度超过阈值，返回匹配结果
            boolean inBlacklist = maxSimilarity >= blacklistThreshold;
            if (inBlacklist) {
                log.warn("[人脸识别] 检测到黑名单匹配，blacklistId={}, similarity={}",
                        matchedBlacklistId, maxSimilarity);
            } else {
                log.debug("[人脸识别] 黑名单比对完成，未匹配，maxSimilarity={}", maxSimilarity);
            }

            return new BlacklistCheckResult(inBlacklist, matchedBlacklistId, maxSimilarity);
        } catch (Exception e) {
            log.error("[人脸识别] 黑名单比对异常，error={}", e.getMessage(), e);
            return new BlacklistCheckResult(false, null, 0.0);
        }
    }

    /**
     * 人脸检测结果
     */
    public record FaceDetectResult(
            int x, int y, int width, int height,
            double confidence, byte[] faceImage) {
    }

    /**
     * 人脸匹配结果
     */
    public record FaceMatchResult(
            Long userId, String userName,
            double similarity) {
    }

    /**
     * 黑名单检查结果
     */
    public record BlacklistCheckResult(
            boolean inBlacklist, Long blacklistId,
            double similarity) {
    }
}
