package net.lab1024.sa.common.entity.workflow;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批统计实体
 * <p>
 * 存储审批流程的统计数据，包括审批数量、通过率、平均耗时等指标
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong></p>
 * <ul>
 *   <li>审批数据统计</li>
 *   <li>审批效率分析</li>
 *   <li>审批工作量统计</li>
 *   <li>审批趋势分析</li>
 *   <li>审批报表生成</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>每日审批统计数据收集</li>
 *   <li>用户审批工作量统计</li>
 *   <li>部门审批效率分析</li>
 *   <li>业务类型审批统计</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 * @see net.lab1024.sa.oa.workflow.dao.ApprovalStatisticsDao 审批统计DAO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_common_approval_statistics")
@Schema(description = "审批统计实体")
public class ApprovalStatisticsEntity extends BaseEntity {

    /**
     * 统计ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "统计ID", example = "1001")
    private Long id;

    /**
     * 业务类型
     * <p>
     * 统计的业务类型，如：leave、reimbursement、purchase、contract等
     * </p>
     */
    @NotBlank
    @Size(max = 100)
    @TableField("business_type")
    @Schema(description = "业务类型", example = "leave")
    private String businessType;

    /**
     * 业务类型名称
     * <p>
     * 冗余字段，用于快速查询
     * </p>
     */
    @Size(max = 200)
    @TableField("business_type_name")
    @Schema(description = "业务类型名称", example = "请假申请")
    private String businessTypeName;

    /**
     * 统计日期
     * <p>
     * 统计数据的日期
     * 按天统计
     * </p>
     */
    @NotNull
    @TableField("statistics_date")
    @Schema(description = "统计日期", example = "2025-12-27")
    private LocalDate statisticsDate;

    /**
     * 统计维度
     * <p>
     * DAILY-每日统计
     * WEEKLY-每周统计
     * MONTHLY-每月统计
     * QUARTERLY-每季度统计
     * YEARLY-每年统计
     * USER-用户统计
     * DEPT-部门统计
     * </p>
     */
    @NotBlank
    @Size(max = 50)
    @TableField("statistics_dimension")
    @Schema(description = "统计维度", example = "DAILY")
    private String statisticsDimension;

    /**
     * 用户ID
     * <p>
     * 如果是用户维度的统计
     * 关联UserEntity.userId
     * </p>
     */
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 部门ID
     * <p>
     * 如果是部门维度的统计
     * 关联DepartmentEntity.departmentId
     * </p>
     */
    @TableField("department_id")
    @Schema(description = "部门ID", example = "1001")
    private Long departmentId;

    /**
     * 总审批数量
     * <p>
     * 统计时间段内的总审批任务数
     * </p>
     */
    @TableField("total_count")
    @Schema(description = "总审批数量", example = "100")
    private Integer totalCount;

    /**
     * 待审批数量
     */
    @TableField("pending_count")
    @Schema(description = "待审批数量", example = "20")
    private Integer pendingCount;

    /**
     * 审批中数量
     */
    @TableField("processing_count")
    @Schema(description = "审批中数量", example = "10")
    private Integer processingCount;

    /**
     * 已通过数量
     */
    @TableField("approved_count")
    @Schema(description = "已通过数量", example = "60")
    private Integer approvedCount;

    /**
     * 已驳回数量
     */
    @TableField("rejected_count")
    @Schema(description = "已驳回数量", example = "5")
    private Integer rejectedCount;

    /**
     * 已转交数量
     */
    @TableField("transferred_count")
    @Schema(description = "已转交数量", example = "3")
    private Integer transferredCount;

    /**
     * 已委派数量
     */
    @TableField("delegated_count")
    @Schema(description = "已委派数量", example = "2")
    private Integer delegatedCount;

    /**
     * 平均审批时长（分钟）
     * <p>
     * 从任务创建到完成的平均时长
     * </p>
     */
    @TableField("avg_duration_minutes")
    @Schema(description = "平均审批时长（分钟）", example = "120")
    private Long avgDurationMinutes;

    /**
     * 最大审批时长（分钟）
     */
    @TableField("max_duration_minutes")
    @Schema(description = "最大审批时长（分钟）", example = "480")
    private Long maxDurationMinutes;

    /**
     * 最小审批时长（分钟）
     */
    @TableField("min_duration_minutes")
    @Schema(description = "最小审批时长（分钟）", example = "5")
    private Long minDurationMinutes;

    /**
     * 超时任务数量
     * <p>
     * 超过到期时间未完成的任务数
     * </p>
     */
    @TableField("timeout_count")
    @Schema(description = "超时任务数量", example = "5")
    private Integer timeoutCount;

    /**
     * 通过率（百分比）
     * <p>
     * 已通过数量 / (已通过数量 + 已驳回数量) * 100
     * </p>
     */
    @TableField("approval_rate")
    @Schema(description = "通过率（百分比）", example = "92.3")
    private Double approvalRate;

    /**
     * 统计时间
     * <p>
     * 数据统计生成的时间
     * </p>
     */
    @NotNull
    @TableField("statistics_time")
    @Schema(description = "统计时间", example = "2025-12-27T23:59:59")
    private LocalDateTime statisticsTime;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    /**
     * 统计维度枚举
     */
    public enum StatisticsDimension {
        DAILY("DAILY", "每日统计"),
        WEEKLY("WEEKLY", "每周统计"),
        MONTHLY("MONTHLY", "每月统计"),
        QUARTERLY("QUARTERLY", "每季度统计"),
        YEARLY("YEARLY", "每年统计"),
        USER("USER", "用户统计"),
        DEPT("DEPT", "部门统计");

        private final String code;
        private final String description;

        StatisticsDimension(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static StatisticsDimension fromCode(String code) {
            for (StatisticsDimension dimension : values()) {
                if (dimension.code.equals(code)) {
                    return dimension;
                }
            }
            throw new IllegalArgumentException("Invalid statistics dimension code: " + code);
        }
    }
}
