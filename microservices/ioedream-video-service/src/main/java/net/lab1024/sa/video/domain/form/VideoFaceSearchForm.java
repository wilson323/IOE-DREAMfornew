package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.common.domain.PageParam;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 人脸搜索表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Schema(description = "人脸搜索表单")
public class VideoFaceSearchForm extends PageParam {

    /**
     * 搜索人脸图片URL
     */
    @Schema(description = "搜索人脸图片URL", example = "/uploads/search/1698745325654325123.jpg")
    @NotBlank(message = "搜索人脸图片URL不能为空")
    @Size(max = 500, message = "搜索人脸图片URL长度不能超过500个字符")
    private String searchFaceUrl;

    /**
     * 搜索类型
     */
    @Schema(description = "搜索类型", example = "1")
    @NotNull(message = "搜索类型不能为空")
    @Min(value = 1, message = "搜索类型必须在1-5之间")
    @Max(value = 5, message = "搜索类型必须在1-5之间")
    private Integer searchType;

    /**
     * 搜索库类型
     */
    @Schema(description = "搜索库类型", example = "1")
    @NotNull(message = "搜索库类型不能为空")
    @Min(value = 1, message = "搜索库类型必须在1-6之间")
    @Max(value = 6, message = "搜索库类型必须在1-6之间")
    private Integer searchLibrary;

    /**
     * 搜索库ID列表
     */
    @Schema(description = "搜索库ID列表", example = "[1001,1002,1003]")
    private List<Long> libraryIds;

    /**
     * 相似度阈值
     */
    @Schema(description = "相似度阈值", example = "75.0")
    @NotNull(message = "相似度阈值不能为空")
    @DecimalMin(value = "0.0", message = "相似度阈值不能小于0")
    @DecimalMax(value = "100.0", message = "相似度阈值不能大于100")
    private BigDecimal similarityThreshold;

    /**
     * 最大返回数量
     */
    @Schema(description = "最大返回数量", example = "50")
    @Min(value = 1, message = "最大返回数量不能小于1")
    @Max(value = 1000, message = "最大返回数量不能大于1000")
    private Integer maxResults;

    /**
     * 算法类型
     */
    @Schema(description = "算法类型", example = "1")
    @Min(value = 1, message = "算法类型必须在1-8之间")
    @Max(value = 8, message = "算法类型必须在1-8之间")
    private Integer algorithmType;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 通道ID
     */
    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    /**
     * 年龄范围过滤
     */
    @Schema(description = "年龄范围", example = "20-60")
    @Pattern(regexp = "^(\\d+)-(\\d+)$", message = "年龄范围格式不正确，应为：最小年龄-最大年龄")
    private String ageRange;

    /**
     * 性别过滤
     */
    @Schema(description = "性别过滤", example = "0")
    @Min(value = 0, message = "性别过滤必须在0-2之间")
    @Max(value = 2, message = "性别过滤必须在0-2之间")
    private Integer genderFilter;

    /**
     * 部门过滤
     */
    @Schema(description = "部门过滤", example = "技术部,产品部")
    @Size(max = 500, message = "部门过滤长度不能超过500个字符")
    private String departmentFilter;

    /**
     * 人员类型过滤
     */
    @Schema(description = "人员类型过滤", example = "[1,2,5]")
    private List<Integer> personTypeFilter;

    /**
     * 搜索时间范围开始
     */
    @Schema(description = "搜索时间范围开始", example = "2025-12-15T00:00:00")
    private LocalDateTime timeRangeStart;

    /**
     * 搜索时间范围结束
     */
    @Schema(description = "搜索时间范围结束", example = "2025-12-16T23:59:59")
    private LocalDateTime timeRangeEnd;

    /**
     * 是否包含活体检测
     */
    @Schema(description = "是否包含活体检测", example = "true")
    private Boolean includeLivenessCheck;

    /**
     * 最小人脸质量分数
     */
    @Schema(description = "最小人脸质量分数", example = "75.0")
    @DecimalMin(value = "0.0", message = "最小人脸质量分数不能小于0")
    @DecimalMax(value = "100.0", message = "最小人脸质量分数不能大于100")
    private BigDecimal minQualityScore;

    /**
     * 是否返回高相似度匹配
     */
    @Schema(description = "是否返回高相似度匹配", example = "true")
    private Boolean includeHighSimilarity;

    /**
     * 高相似度阈值
     */
    @Schema(description = "高相似度阈值", example = "90.0")
    @DecimalMin(value = "0.0", message = "高相似度阈值不能小于0")
    @DecimalMax(value = "100.0", message = "高相似度阈值不能大于100")
    private BigDecimal highSimilarityThreshold;

    /**
     * 是否返回详细结果
     */
    @Schema(description = "是否返回详细结果", example = "true")
    private Boolean returnDetailedResults;

    /**
     * 是否返回人脸图片URL
     */
    @Schema(description = "是否返回人脸图片URL", example = "true")
    private Boolean returnFaceImages;

    /**
     * 搜索目的
     */
    @Schema(description = "搜索目的", example = "身份验证")
    @Size(max = 200, message = "搜索目的长度不能超过200个字符")
    private String searchPurpose;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "VIP客户身份搜索")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remark;
}
