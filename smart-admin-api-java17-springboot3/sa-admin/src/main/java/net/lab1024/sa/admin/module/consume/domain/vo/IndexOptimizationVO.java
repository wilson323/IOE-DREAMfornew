package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 索引优化展示对象
 * <p>
 * 用于索引优化相关功能的数据展示
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "索引优化展示对象")
public class IndexOptimizationVO {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 表名
     */
    @Schema(description = "表名", example = "t_consume_record")
    private String tableName;

    /**
     * 索引名称
     */
    @Schema(description = "索引名称", example = "idx_user_id_create_time")
    private String indexName;

    /**
     * 索引类型
     */
    @Schema(description = "索引类型", example = "BTREE")
    private String indexType;

    /**
     * 优化建议类型
     */
    @Schema(description = "优化建议类型", example = "MISSING_INDEX")
    private String suggestionType;

    /**
     * 建议内容
     */
    @Schema(description = "建议内容", example = "建议在user_id和create_time字段上创建复合索引")
    private String suggestion;

    /**
     * 严重程度
     */
    @Schema(description = "严重程度", example = "HIGH")
    private String severity;

    /**
     * 性能影响评估
     */
    @Schema(description = "性能影响评估", example = "预计可提升查询性能60%")
    private String performanceImpact;

    /**
     * 建议的索引SQL
     */
    @Schema(description = "建议的索引SQL", example = "CREATE INDEX idx_user_id_create_time ON t_consume_record(user_id, create_time)")
    private String suggestedIndexSql;

    /**
     * 当前索引状态
     */
    @Schema(description = "当前索引状态", example = "NOT_EXIST")
    private String currentStatus;

    /**
     * 预计性能提升
     */
    @Schema(description = "预计性能提升(%)", example = "60.5")
    private Double performanceImprovement;

    /**
     * 查询频率
     */
    @Schema(description = "查询频率(次/天)", example = "1000")
    private Long queryFrequency;

    /**
     * 表大小(MB)
     */
    @Schema(description = "表大小(MB)", example = "256.8")
    private Double tableSizeMb;

    /**
     * 是否已应用
     */
    @Schema(description = "是否已应用", example = "false")
    private Boolean applied = false;

    /**
     * 应用时间
     */
    @Schema(description = "应用时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime appliedTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remarks;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}