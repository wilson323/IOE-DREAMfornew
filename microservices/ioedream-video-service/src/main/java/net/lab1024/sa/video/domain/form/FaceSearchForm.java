package net.lab1024.sa.video.domain.form;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 人脸搜索表单
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
public class FaceSearchForm {

    /**
     * 人脸图像数据 (Base64)
     */
    @NotNull(message = "人脸图像不能为空")
    private String faceImage;

    /**
     * 设备ID (可选，指定搜索范围)
     */
    private Long deviceId;

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
     * 最大返回结果数
     * 默认: 10
     */
    private Integer maxResults = 10;

    /**
     * 搜索模式
     * EXACT - 精确匹配
     * SIMILAR - 相似匹配
     * FUZZY - 模糊匹配
     */
    private String searchMode = "SIMILAR";

    /**
     * 包含已识别人员
     * true - 包含已识别人员
     * false - 仅未识别人员
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

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

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
     * 性别枚举
     */
    public enum Gender {
        MALE("男"),
        FEMALE("女"),
        UNKNOWN("未知");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 搜索模式枚举
     */
    public enum SearchMode {
        EXACT("精确匹配"),
        SIMILAR("相似匹配"),
        FUZZY("模糊匹配");

        private final String description;

        SearchMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}