package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 人脸搜索结果VO
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
public class FaceSearchResultVO {

    /**
     * 搜索ID
     */
    private String searchId;

    /**
     * 搜索时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime searchTime;

    /**
     * 搜索耗时 (毫秒)
     */
    private Long searchDurationMs;

    /**
     * 搜索状态
     * SUCCESS - 成功
     * FAILED - 失败
     * PARTIAL - 部分成功
     */
    private String searchStatus;

    /**
     * 匹配结果列表
     */
    private List<FaceMatch> matches;

    /**
     * 搜索统计
     */
    private SearchStatistics statistics;

    /**
     * 原始图像信息
     */
    private OriginalImageInfo originalImage;

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
         * 相似度分数 (0.0-1.0)
         */
        private Double similarityScore;

        /**
         * 匹配等级
         * HIGH - 高相似度 (>0.8)
         * MEDIUM - 中等相似度 (0.6-0.8)
         * LOW - 低相似度 (<0.6)
         */
        private String matchLevel;

        /**
         * 人员信息
         */
        private PersonInfo personInfo;

        /**
         * 检测信息
         */
        private DetectionInfo detectionInfo;

        /**
         * 图像信息
         */
        private ImageInfo imageInfo;

        /**
         * 匹配详情
         */
        private MatchDetails matchDetails;

        /**
         * 可信度评分
         */
        private Double confidenceScore;
    }

    /**
     * 人员信息
     */
    @Data
    public static class PersonInfo {
        /**
         * 人员ID
         */
        private String personId;

        /**
         * 人员姓名
         */
        private String personName;

        /**
         * 人员类型
         * EMPLOYEE - 员工
         * VISITOR - 访客
         * STRANGER - 陌生人
         */
        private String personType;

        /**
         * 部门
         */
        private String department;

        /**
         * 职位
         */
        private String position;

        /**
         * 联系电话
         */
        private String phone;

        /**
         * 证件号码
         */
        private String idNumber;

        /**
         * 人员照片
         */
        private String avatarUrl;

        /**
         * 风险等级
         * LOW - 低风险
         * MEDIUM - 中风险
         * HIGH - 高风险
         */
        private String riskLevel;

        /**
         * 标签列表
         */
        private List<String> tags;

        /**
         * 备注信息
         */
        private String remarks;
    }

    /**
     * 检测信息
     */
    @Data
    public static class DetectionInfo {
        /**
         * 检测时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime detectionTime;

        /**
         * 设备ID
         */
        private Long deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 设备位置
         */
        private String deviceLocation;

        /**
         * 检测置信度
         */
        private Double detectionConfidence;

        /**
         * 人脸框坐标 (x, y, width, height)
         */
        private FaceBoundingBox boundingBox;

        /**
         * 人脸角度
         */
        private Double faceAngle;

        /**
         * 表情识别结果
         */
        private String expression;

        /**
         * 是否佩戴口罩
         */
        private Boolean wearingMask;

        /**
         * 是否佩戴眼镜
         */
        private Boolean wearingGlasses;
    }

    /**
     * 人脸边界框
     */
    @Data
    public static class FaceBoundingBox {
        /**
         * 左上角X坐标
         */
        private Integer x;

        /**
         * 左上角Y坐标
         */
        private Integer y;

        /**
         * 宽度
         */
        private Integer width;

        /**
         * 高度
         */
        private Integer height;

        /**
         * 置信度
         */
        private Double confidence;
    }

    /**
     * 图像信息
     */
    @Data
    public static class ImageInfo {
        /**
         * 图像路径
         */
        private String imagePath;

        /**
         * 图像URL
         */
        private String imageUrl;

        /**
         * 图像格式
         */
        private String format;

        /**
         * 图像尺寸
         */
        private ImageSize imageSize;

        /**
         * 图像质量评分
         */
        private Double qualityScore;

        /**
         * 分辨率
         */
        private String resolution;

        /**
         * 缩略图URL
         */
        private String thumbnailUrl;
    }

    /**
     * 图像尺寸
     */
    @Data
    public static class ImageSize {
        /**
         * 宽度 (像素)
         */
        private Integer width;

        /**
         * 高度 (像素)
         */
        private Integer height;
    }

    /**
     * 匹配详情
     */
    @Data
    public static class MatchDetails {
        /**
         * 特征向量距离
         */
        private Double featureDistance;

        /**
         * 关键点匹配数量
         */
        private Integer keyPointMatches;

        /**
         * 关键点总数量
         */
        private Integer totalKeyPoints;

        /**
         * 匹配算法
         */
        private String algorithm;

        /**
         * 匹配参数
         */
        private Map<String, Object> parameters;

        /**
         * 调试信息
         */
        private String debugInfo;
    }

    /**
     * 搜索统计
     */
    @Data
    public static class SearchStatistics {
        /**
         * 总匹配数量
         */
        private Integer totalMatches;

        /**
         * 高相似度匹配数量
         */
        private Integer highSimilarityMatches;

        /**
         * 中等相似度匹配数量
         */
        private Integer mediumSimilarityMatches;

        /**
         * 低相似度匹配数量
         */
        private Integer lowSimilarityMatches;

        /**
         * 平均相似度
         */
        private Double averageSimilarity;

        /**
         * 最高相似度
         */
        private Double highestSimilarity;

        /**
         * 最低相似度
         */
        private Double lowestSimilarity;

        /**
         * 搜索的设备数量
         */
        private Integer deviceCount;

        /**
         * 搜索的时间范围 (小时)
         */
        private Double timeRangeHours;

        /**
         * 人员类型分布
         */
        private Map<String, Integer> personTypeDistribution;
    }

    /**
     * 原始图像信息
     */
    @Data
    public static class OriginalImageInfo {
        /**
         * 图像ID
         */
        private String imageId;

        /**
         * 图像大小 (字节)
         */
        private Long imageSize;

        /**
         * 图像格式
         */
        private String imageFormat;

        /**
         * 上传时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime uploadTime;

        /**
         * 处理状态
         */
        private String processingStatus;

        /**
         * 检测到的人脸数量
         */
        private Integer detectedFaces;

        /**
         * 特征提取时间 (毫秒)
         */
        private Long featureExtractionTimeMs;

        /**
         * 预处理信息
         */
        private PreprocessingInfo preprocessingInfo;
    }

    /**
     * 预处理信息
     */
    @Data
    public static class PreprocessingInfo {
        /**
         * 是否进行了图像增强
         */
        private Boolean imageEnhanced;

        /**
         * 亮度调整值
         */
        private Double brightnessAdjustment;

        /**
         * 对比度调整值
         */
        private Double contrastAdjustment;

        /**
         * 人脸对齐信息
         */
        private String faceAlignmentInfo;

        /**
         * 噪声减少信息
         */
        private String noiseReductionInfo;
    }
}