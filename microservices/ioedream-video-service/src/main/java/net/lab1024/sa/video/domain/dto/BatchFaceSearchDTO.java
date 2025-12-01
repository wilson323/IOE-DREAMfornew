package net.lab1024.sa.video.domain.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 批量人脸搜索DTO
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
public class BatchFaceSearchDTO {

    /**
     * 人脸图像列表
     */
    @NotNull(message = "人脸图像列表不能为空")
    @Valid
    private List<FaceImageRequest> faceImages;

    /**
     * 搜索配置
     */
    private SearchConfiguration searchConfig;

    /**
     * 单张人脸图像请求
     */
    @Data
    public static class FaceImageRequest {
        /**
         * 图像ID (用于结果标识)
         */
        private String imageId;

        /**
         * 人脸图像数据 (Base64)
         */
        @NotNull(message = "人脸图像数据不能为空")
        private String faceImageData;

        /**
         * 图像格式
         */
        private String imageFormat = "JPEG";

        /**
         * 图像质量评分
         */
        private Double qualityScore;

        /**
         * 人脸检测置信度
         */
        private Double detectionConfidence;

        /**
         * 图像元数据
         */
        private Map<String, Object> metadata;
    }

    /**
     * 搜索配置
     */
    @Data
    public static class SearchConfiguration {
        /**
         * 设备ID列表 (可选，指定搜索范围)
         */
        private List<Long> deviceIds;

        /**
         * 开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        /**
         * 相似度阈值 (0.0-1.0)
         * 默认: 0.6
         */
        private Double similarityThreshold = 0.6;

        /**
         * 每张人脸的最大匹配结果数
         * 默认: 5
         */
        private Integer maxMatchesPerFace = 5;

        /**
         * 是否并行处理
         * 默认: true
         */
        private Boolean parallelProcessing = true;

        /**
         * 超时时间 (秒)
         * 默认: 30
         */
        private Integer timeoutSeconds = 30;

        /**
         * 是否包含已识别人员
         */
        private Boolean includeIdentified = true;

        /**
         * 年龄范围过滤
         */
        private AgeRangeFilter ageRange;

        /**
         * 性别过滤
         */
        private String genderFilter;
    }

    /**
     * 年龄范围过滤
     */
    @Data
    public static class AgeRangeFilter {
        /**
         * 最小年龄
         */
        private Integer minAge;

        /**
         * 最大年龄
         */
        private Integer maxAge;

        /**
         * 是否启用
         */
        private Boolean enabled = false;
    }

    /**
     * 批量搜索结果
     */
    @Data
    public static class BatchSearchResult {
        /**
         * 总处理数量
         */
        private Integer totalProcessed;

        /**
         * 成功处理数量
         */
        private Integer successCount;

        /**
         * 失败处理数量
         */
        private Integer failureCount;

        /**
         * 处理时间 (毫秒)
         */
        private Long processingTimeMs;

        /**
         * 搜索结果列表
         */
        private List<FaceSearchResult> results;

        /**
         * 错误信息列表
         */
        private List<SearchError> errors;
    }

    /**
     * 单张人脸搜索结果
     */
    @Data
    public static class FaceSearchResult {
        /**
         * 原始图像ID
         */
        private String originalImageId;

        /**
         * 搜索状态
         * SUCCESS - 成功
         * FAILED - 失败
         * TIMEOUT - 超时
         */
        private String status;

        /**
         * 匹配结果列表
         */
        private List<FaceMatch> matches;

        /**
         * 处理时间 (毫秒)
         */
        private Long processingTimeMs;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 人脸特征向量
         */
        private String featureVector;

        /**
         * 提取的置信度
         */
        private Double extractionConfidence;
    }

    /**
     * 人脸匹配结果
     */
    @Data
    public static class FaceMatch {
        /**
         * 匹配ID
         */
        private String matchId;

        /**
         * 人员ID
         */
        private String personId;

        /**
         * 人员姓名
         */
        private String personName;

        /**
         * 相似度分数
         */
        private Double similarityScore;

        /**
         * 匹配时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime matchTime;

        /**
         * 设备ID
         */
        private Long deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 图像路径
         */
        private String imagePath;

        /**
         * 额外信息
         */
        private Map<String, Object> additionalInfo;
    }

    /**
     * 搜索错误
     */
    @Data
    public static class SearchError {
        /**
         * 图像ID
         */
        private String imageId;

        /**
         * 错误代码
         */
        private String errorCode;

        /**
         * 错误消息
         */
        private String errorMessage;

        /**
         * 错误时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime errorTime;

        /**
         * 重试次数
         */
        private Integer retryCount;
    }
}