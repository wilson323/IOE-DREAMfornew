package net.lab1024.sa.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 消费查询DTO
 *
 * @author IOE-DREAM
 * @since 2025-12-09
 */
@Data
@Schema(description = "消费查询DTO")
public class ConsumeQueryDTO {

    /**
     * 账户ID
     */
    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 消费类型
     */
    @Schema(description = "消费类型", example = "DINING")
    private String consumeType;

    /**
     * 消费状态
     */
    @Schema(description = "消费状态", allowableValues = {"0", "1", "2"}, example = "1")
    private Integer status;

    /**
     * 最小消费金额
     */
    @Schema(description = "最小消费金额", example = "0.00")
    private Double minAmount;

    /**
     * 最大消费金额
     */
    @Schema(description = "最大消费金额", example = "1000.00")
    private Double maxAmount;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2025-12-31T23:59:59")
    private LocalDateTime endTime;

    // 便捷方法：为了兼容现有代码调用setStartDate()
    public void setStartDate(LocalDateTime startDate) {
        this.startTime = startDate;
    }

    // 便捷方法：为了兼容现有代码调用setEndDate()
    public void setEndDate(LocalDateTime endDate) {
        this.endTime = endDate;
    }

    // 便捷方法：为了兼容现有代码调用getStartDate()
    public LocalDateTime getStartDate() {
        return this.startTime;
    }

    // 便捷方法：为了兼容现有代码调用getEndDate()
    public LocalDateTime getEndDate() {
        return this.endTime;
    }

    /**
     * 设备编号
     */
    @Schema(description = "设备编号", example = "POS001")
    private String deviceNo;

    /**
     * 商户ID
     */
    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "consumeTime")
    private String orderBy;

    /**
     * 排序方式
     */
    @Schema(description = "排序方式", allowableValues = {"ASC", "DESC"}, example = "DESC")
    private String orderDirection;

    /**
     * 关键字搜索
     */
    @Schema(description = "关键字搜索", example = "午餐")
    private String keyword;

    /**
     * 是否包含今日数据
     */
    @Schema(description = "是否包含今日数据", example = "true")
    private Boolean includeToday = true;

    /**
     * 是否导出
     */
    @Schema(description = "是否导出", example = "false")
    private Boolean export = false;
}