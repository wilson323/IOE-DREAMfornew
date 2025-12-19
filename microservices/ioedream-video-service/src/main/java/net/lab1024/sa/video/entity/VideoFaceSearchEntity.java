package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 人脸搜索记录实体
 * 记录1:N人脸识别（以脸搜脸）的搜索结果
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_face_search")
@Schema(description = "人脸搜索记录实体")
public class VideoFaceSearchEntity extends BaseEntity {

    /**
     * 搜索ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "搜索ID", example = "1698745325654325123")
    private Long searchId;

    /**
     * 请求ID（前端请求标识）
     */
    @TableField("request_id")
    @Size(max = 64, message = "请求ID长度不能超过64个字符")
    @Schema(description = "请求ID", example = "REQ202512161030001")
    private String requestId;

    /**
     * 搜索类型
     * 1-实时搜索 2-历史搜索 3-批量搜索 4-跨库搜索 5-智能推荐
     */
    @TableField("search_type")
    @NotNull(message = "搜索类型不能为空")
    @Min(value = 1, message = "搜索类型必须在1-5之间")
    @Max(value = 5, message = "搜索类型必须在1-5之间")
    @Schema(description = "搜索类型", example = "1")
    private Integer searchType;

    /**
     * 搜索人脸图片URL
     */
    @TableField("search_face_url")
    @NotNull(message = "搜索人脸图片URL不能为空")
    @Size(max = 500, message = "搜索人脸图片URL长度不能超过500个字符")
    @Schema(description = "搜索人脸图片URL", example = "/uploads/search/1698745325654325123.jpg")
    private String searchFaceUrl;

    /**
     * 搜索人脸特征向量
     */
    @TableField("search_face_feature")
    @Schema(description = "搜索人脸特征向量", example = "eyJmZWF0dXJlIjogWzAuMTIzLCAwLjQ1NiwgLi4uXX0=")
    private String searchFaceFeature;

    /**
     * 搜索库类型
     * 1-全库搜索 2-白名单 3-黑名单 4-员工库 5-访客库 6-重点关注库
     */
    @TableField("search_library")
    @NotNull(message = "搜索库类型不能为空")
    @Min(value = 1, message = "搜索库类型必须在1-6之间")
    @Max(value = 6, message = "搜索库类型必须在1-6之间")
    @Schema(description = "搜索库类型", example = "1")
    private Integer searchLibrary;

    /**
     * 搜索库ID列表（多个库ID，逗号分隔）
     */
    @TableField("library_ids")
    @Size(max = 500, message = "搜索库ID列表长度不能超过500个字符")
    @Schema(description = "搜索库ID列表", example = "1001,1002,1003")
    private String libraryIds;

    /**
     * 相似度阈值
     */
    @TableField("similarity_threshold")
    @NotNull(message = "相似度阈值不能为空")
    @Min(value = 0, message = "相似度阈值不能小于0")
    @Max(value = 100, message = "相似度阈值不能大于100")
    @Schema(description = "相似度阈值", example = "75.0")
    private BigDecimal similarityThreshold;

    /**
     * 最大返回数量
     */
    @TableField("max_results")
    @Min(value = 1, message = "最大返回数量不能小于1")
    @Max(value = 1000, message = "最大返回数量不能大于1000")
    @Schema(description = "最大返回数量", example = "50")
    private Integer maxResults;

    /**
     * 实际匹配数量
     */
    @TableField("matched_count")
    @Min(value = 0, message = "实际匹配数量不能小于0")
    @Schema(description = "实际匹配数量", example = "15")
    private Integer matchedCount;

    /**
     * 最高相似度
     */
    @TableField("max_similarity")
    @Min(value = 0, message = "最高相似度不能小于0")
    @Max(value = 100, message = "最高相似度不能大于100")
    @Schema(description = "最高相似度", example = "96.5")
    private BigDecimal maxSimilarity;

    /**
     * 平均相似度
     */
    @TableField("avg_similarity")
    @Min(value = 0, message = "平均相似度不能小于0")
    @Max(value = 100, message = "平均相似度不能大于100")
    @Schema(description = "平均相似度", example = "82.3")
    private BigDecimal avgSimilarity;

    /**
     * 搜索耗时（毫秒）
     */
    @TableField("search_duration")
    @Min(value = 0, message = "搜索耗时不能小于0")
    @Schema(description = "搜索耗时（毫秒）", example = "1250")
    private Long searchDuration;

    /**
     * 搜索算法类型
     * 1-商汤 2-旷视 3-依图 4-百度 5-腾讯优图 6-虹软 7-华为 8-阿里云
     */
    @TableField("algorithm_type")
    @NotNull(message = "算法类型不能为空")
    @Min(value = 1, message = "算法类型必须在1-8之间")
    @Max(value = 8, message = "算法类型必须在1-8之间")
    @Schema(description = "搜索算法类型", example = "1")
    private Integer algorithmType;

    /**
     * 算法版本
     */
    @TableField("algorithm_version")
    @Size(max = 32, message = "算法版本长度不能超过32个字符")
    @Schema(description = "算法版本", example = "ArcFace-v1.2")
    private String algorithmVersion;

    /**
     * 搜索状态
     * 0-搜索中 1-搜索完成 2-搜索失败 3-已取消
     */
    @TableField("search_status")
    @NotNull(message = "搜索状态不能为空")
    @Min(value = 0, message = "搜索状态必须在0-3之间")
    @Max(value = 3, message = "搜索状态必须在0-3之间")
    @Schema(description = "搜索状态", example = "1")
    private Integer searchStatus;

    /**
     * 搜索结果（JSON数组，包含前N个匹配结果）
     */
    @TableField("search_results")
    @Schema(description = "搜索结果", example = "[{\"faceId\":\"1698745325654325123\",\"similarity\":96.5}]")
    private String searchResults;

    /**
     * 是否存在高相似度匹配
     * 0-否 1-是
     */
    @TableField("has_high_similarity")
    @Min(value = 0, message = "高相似度匹配标志必须在0-1之间")
    @Max(value = 1, message = "高相似度匹配标志必须在0-1之间")
    @Schema(description = "是否存在高相似度匹配", example = "1")
    private Integer hasHighSimilarity;

    /**
     * 高相似度阈值
     */
    @TableField("high_similarity_threshold")
    @Min(value = 0, message = "高相似度阈值不能小于0")
    @Max(value = 100, message = "高相似度阈值不能大于100")
    @Schema(description = "高相似度阈值", example = "90.0")
    private BigDecimal highSimilarityThreshold;

    /**
     * 高相似度匹配结果
     */
    @TableField("high_similarity_results")
    @Schema(description = "高相似度匹配结果", example = "[{\"faceId\":\"1698745325654325123\",\"similarity\":96.5}]")
    private String highSimilarityResults;

    /**
     * 设备ID
     */
    @TableField("device_id")
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    @Size(max = 64, message = "设备编码长度不能超过64个字符")
    @Schema(description = "设备编码", example = "CAM001")
    private String deviceCode;

    /**
     * 通道ID
     */
    @TableField("channel_id")
    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    /**
     * 用户ID（发起搜索的用户）
     */
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    @Size(max = 100, message = "用户姓名长度不能超过100个字符")
    @Schema(description = "用户姓名", example = "管理员")
    private String userName;

    /**
     * IP地址
     */
    @TableField("client_ip")
    @Size(max = 64, message = "IP地址长度不能超过64个字符")
    @Schema(description = "IP地址", example = "192.168.1.100")
    private String clientIp;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @Schema(description = "用户代理", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

    /**
     * 搜索人脸质量分数
     */
    @TableField("search_face_quality")
    @Min(value = 0, message = "搜索人脸质量分数不能小于0")
    @Max(value = 100, message = "搜索人脸质量分数不能大于100")
    @Schema(description = "搜索人脸质量分数", example = "91.2")
    private BigDecimal searchFaceQuality;

    /**
     * 活体检测结果
     * 0-未检测 1-真人 2-照片 3-视频 4-面具 5-其他攻击
     */
    @TableField("liveness_result")
    @Min(value = 0, message = "活体检测结果必须在0-5之间")
    @Max(value = 5, message = "活体检测结果必须在0-5之间")
    @Schema(description = "活体检测结果", example = "1")
    private Integer livenessResult;

    /**
     * 活体检测置信度
     */
    @TableField("liveness_confidence")
    @Min(value = 0, message = "活体检测置信度不能小于0")
    @Max(value = 100, message = "活体检测置信度不能大于100")
    @Schema(description = "活体检测置信度", example = "87.6")
    private BigDecimal livenessConfidence;

    /**
     * 人脸区域坐标（JSON格式）
     */
    @TableField("face_region")
    @Schema(description = "人脸区域坐标", example = "{\"x\":100,\"y\":200,\"w\":150,\"h\":180}")
    private String faceRegion;

    /**
     * 年龄范围过滤
     */
    @TableField("age_range")
    @Size(max = 32, message = "年龄范围长度不能超过32个字符")
    @Schema(description = "年龄范围", example = "20-60")
    private String ageRange;

    /**
     * 性别过滤
     * 0-全部 1-男 2-女
     */
    @TableField("gender_filter")
    @Min(value = 0, message = "性别过滤必须在0-2之间")
    @Max(value = 2, message = "性别过滤必须在0-2之间")
    @Schema(description = "性别过滤", example = "0")
    private Integer genderFilter;

    /**
     * 部门过滤
     */
    @TableField("department_filter")
    @Size(max = 500, message = "部门过滤长度不能超过500个字符")
    @Schema(description = "部门过滤", example = "技术部,产品部")
    private String departmentFilter;

    /**
     * 人员类型过滤
     */
    @TableField("person_type_filter")
    @Size(max = 200, message = "人员类型过滤长度不能超过200个字符")
    @Schema(description = "人员类型过滤", example = "1,2,5")
    private String personTypeFilter;

    /**
     * 搜索时间范围开始
     */
    @TableField("time_range_start")
    @Schema(description = "搜索时间范围开始", example = "2025-12-15T00:00:00")
    private LocalDateTime timeRangeStart;

    /**
     * 搜索时间范围结束
     */
    @TableField("time_range_end")
    @Schema(description = "搜索时间范围结束", example = "2025-12-16T23:59:59")
    private LocalDateTime timeRangeEnd;

    /**
     * 错误码
     */
    @TableField("error_code")
    @Size(max = 32, message = "错误码长度不能超过32个字符")
    @Schema(description = "错误码", example = "NO_FACE_DETECTED")
    private String errorCode;

    /**
     * 错误信息
     */
    @TableField("error_message")
    @Size(max = 500, message = "错误信息长度不能超过500个字符")
    @Schema(description = "错误信息", example = "未检测到有效人脸")
    private String errorMessage;

    /**
     * 备注信息
     */
    @TableField("remark")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    @Schema(description = "备注信息", example = "VIP客户身份搜索")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性", example = "{\"priority\":\"high\",\"purpose\":\"security_check\"}")
    private String extendedAttributes;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "2025-12-16T10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2025-12-16T10:30:00")
    private LocalDateTime updateTime;

    /**
     * 删除标志
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标志", example = "0")
    private Integer deletedFlag;
}
