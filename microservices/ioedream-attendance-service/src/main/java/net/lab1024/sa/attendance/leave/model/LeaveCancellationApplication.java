package net.lab1024.sa.attendance.leave.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 销假申请
 * <p>
 * 封装销假申请的详细信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationApplication {

    /**
     * 销假申请ID
     */
    private String cancellationId;

    /**
     * 原请假申请ID
     */
    private String originalLeaveId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 员工工号
     */
    private String employeeCode;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 原请假类型
     */
    private LeaveType originalLeaveType;

    /**
     * 原请假开始日期
     */
    private LocalDate originalLeaveStartDate;

    /**
     * 原请假结束日期
     */
    private LocalDate originalLeaveEndDate;

    /**
     * 原请假天数
     */
    private BigDecimal originalLeaveDays;

    /**
     * 原请假事由
     */
    private String originalLeaveReason;

    /**
     * 销假类型
     */
    private CancellationType cancellationType;

    /**
     * 销假开始日期
     */
    private LocalDate cancellationStartDate;

    /**
     * 销假结束日期
     */
    private LocalDate cancellationEndDate;

    /**
     * 销假天数
     */
    private BigDecimal cancellationDays;

    /**
     * 销假事由
     */
    private String cancellationReason;

    /**
     * 销假原因详情
     */
    private String cancellationReasonDetail;

    /**
     * 申请状态
     */
    private ApplicationStatus status;

    /**
     * 申请时间
     */
    private LocalDateTime applicationTime;

    /**
     * 申请人
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 审批人ID列表
     */
    private List<Long> approverIds;

    /**
     * 审批人姓名列表
     */
    private List<String> approverNames;

    /**
     * 当前审批环节
     */
    private String currentApprovalStep;

    /**
     * 审批记录
     */
    private List<ApprovalRecord> approvalRecords;

    /**
     * 销假影响评估
     */
    private CancellationImpactAssessment impactAssessment;

    /**
     * 附件信息
     */
    private List<AttachmentInfo> attachments;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 紧急程度
     */
    private UrgencyLevel urgencyLevel;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 审批完成时间
     */
    private LocalDateTime approvalCompletedTime;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 请假类型枚举
     */
    public enum LeaveType {
        SICK_LEAVE("病假"),
        PERSONAL_LEAVE("事假"),
        ANNUAL_LEAVE("年假"),
        MATERNITY_LEAVE("产假"),
        PATERNITY_LEAVE("陪产假"),
        MARRIAGE_LEAVE("婚假"),
        FUNERAL_LEAVE("丧假"),
        COMPENSATORY_LEAVE("调休"),
        INJURY_LEAVE("工伤假"),
        BREASTFEEDING_LEAVE("哺乳假"),
        HOME_LEAVE("探亲假"),
        OTHER_LEAVE("其他假");

        private final String description;

        LeaveType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 销假类型枚举
     */
    public enum CancellationType {
        FULL_CANCELLATION("全部销假"),
        PARTIAL_CANCELLATION("部分销假"),
        EARLY_RETURN("提前销假"),
        POSTPONED_RETURN("延后销假"),
        CANCELLED_LEAVE("取消请假");

        private final String description;

        CancellationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 申请状态枚举
     */
    public enum ApplicationStatus {
        DRAFT("草稿"),
        SUBMITTED("已提交"),
        UNDER_REVIEW("审核中"),
        APPROVED("已通过"),
        REJECTED("已驳回"),
        WITHDRAWN("已撤销"),
        EFFECTIVE("已生效"),
        INVALID("已失效"),
        CANCELLED("已取消");

        private final String description;

        ApplicationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 紧急程度枚举
     */
    public enum UrgencyLevel {
        NORMAL("普通"),
        URGENT("紧急"),
        VERY_URGENT("非常紧急");

        private final String description;

        UrgencyLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 审批记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalRecord {
        /**
         * 审批ID
         */
        private String approvalId;

        /**
         * 审批环节
         */
        private String approvalStep;

        /**
         * 审批人ID
         */
        private Long approverId;

        /**
         * 审批人姓名
         */
        private String approverName;

        /**
         * 审批结果
         */
        private ApprovalResult approvalResult;

        /**
         * 审批意见
         */
        private String approvalComment;

        /**
         * 审批时间
         */
        private LocalDateTime approvalTime;

        /**
         * 是否为代理审批
         */
        private Boolean isProxyApproval;

        /**
         * 代理审批人
         */
        private String proxyApproverName;

        /**
         * 审批耗时（分钟）
         */
        private Integer approvalDurationMinutes;
    }

    /**
     * 审批结果枚举
     */
    public enum ApprovalResult {
        APPROVED("同意"),
        REJECTED("驳回"),
        RETURNED("退回修改"),
        TRANSFERRED("转交");

        private final String description;

        ApprovalResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 销假影响评估
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancellationImpactAssessment {
        /**
         * 对考勤的影响
         */
        private String attendanceImpact;

        /**
         * 对薪资的影响
         */
        private String salaryImpact;

        /**
         * 对工作安排的影响
         */
        private String workScheduleImpact;

        /**
         * 风险等级
         */
        private RiskLevel riskLevel;

        /**
         * 风险描述
         */
        private String riskDescription;

        /**
         * 建议措施
         */
        private List<String> recommendedActions;

        /**
         * 是否需要特殊审批
         */
        private Boolean requiresSpecialApproval;

        /**
         * 特殊审批理由
         */
        private String specialApprovalReason;
    }

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中等风险"),
        HIGH("高风险"),
        VERY_HIGH("极高风险");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 附件信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentInfo {
        /**
         * 附件ID
         */
        private String attachmentId;

        /**
         * 文件名称
         */
        private String fileName;

        /**
         * 文件类型
         */
        private String fileType;

        /**
         * 文件大小（字节）
         */
        private Long fileSize;

        /**
         * 文件URL
         */
        private String fileUrl;

        /**
         * 上传时间
         */
        private LocalDateTime uploadTime;

        /**
         * 上传人
         */
        private Long uploaderId;

        /**
         * 附件描述
         */
        private String attachmentDescription;
    }

    /**
     * 计算销假天数
     */
    public void calculateCancellationDays() {
        if (cancellationStartDate != null && cancellationEndDate != null) {
            long days = java.time.Duration.between(
                    cancellationStartDate.atStartOfDay(),
                    cancellationEndDate.atStartOfDay().plusDays(1)
            ).toDays();
            this.cancellationDays = BigDecimal.valueOf(days);
        } else {
            this.cancellationDays = BigDecimal.ZERO;
        }
    }

    /**
     * 计算剩余请假天数
     */
    public BigDecimal getRemainingLeaveDays() {
        if (originalLeaveDays == null || cancellationDays == null) {
            return BigDecimal.ZERO;
        }
        return originalLeaveDays.subtract(cancellationDays);
    }

    /**
     * 检查是否为紧急销假
     */
    public boolean isUrgentCancellation() {
        return urgencyLevel == UrgencyLevel.URGENT || urgencyLevel == UrgencyLevel.VERY_URGENT;
    }

    /**
     * 检查是否需要多级审批
     */
    public boolean requiresMultiLevelApproval() {
        return (cancellationType == CancellationType.FULL_CANCELLATION) ||
               (urgencyLevel == UrgencyLevel.VERY_URGENT) ||
               (impactAssessment != null && Boolean.TRUE.equals(impactAssessment.getRequiresSpecialApproval()));
    }

    /**
     * 获取当前审批状态描述
     */
    public String getCurrentApprovalStatusDescription() {
        if (status == ApplicationStatus.DRAFT) {
            return "草稿状态";
        } else if (status == ApplicationStatus.SUBMITTED) {
            return "等待审批 - " + (currentApprovalStep != null ? currentApprovalStep : "第一环节");
        } else if (status == ApplicationStatus.UNDER_REVIEW) {
            return "审批中 - " + (currentApprovalStep != null ? currentApprovalStep : "处理中");
        } else if (status == ApplicationStatus.APPROVED) {
            return "已通过";
        } else if (status == ApplicationStatus.REJECTED) {
            return "已驳回";
        } else {
            return status.getDescription();
        }
    }

    /**
     * 获取销假申请摘要
     */
    public String getCancellationSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append(employeeName != null ? employeeName : "未知员工");
        summary.append(" 申请");

        if (cancellationType != null) {
            summary.append(cancellationType.getDescription());
        } else {
            summary.append("销假");
        }

        if (originalLeaveType != null) {
            summary.append(" (原").append(originalLeaveType.getDescription()).append(")");
        }

        if (cancellationDays != null) {
            summary.append(" - ").append(cancellationDays).append("天");
        }

        if (cancellationStartDate != null && cancellationEndDate != null) {
            summary.append(" (").append(cancellationStartDate)
                    .append(" 至 ").append(cancellationEndDate).append(")");
        }

        if (urgencyLevel != null && urgencyLevel != UrgencyLevel.NORMAL) {
            summary.append(" [").append(urgencyLevel.getDescription()).append("]");
        }

        return summary.toString();
    }

    /**
     * 检查申请是否有效
     */
    public boolean isValid() {
        if (cancellationId == null || employeeId == null || originalLeaveId == null) {
            return false;
        }

        if (cancellationType == null) {
            return false;
        }

        if (cancellationStartDate != null && cancellationEndDate != null) {
            return !cancellationStartDate.isAfter(cancellationEndDate);
        }

        if (originalLeaveStartDate != null && originalLeaveEndDate != null) {
            return !originalLeaveStartDate.isAfter(originalLeaveEndDate);
        }

        if (cancellationStartDate != null && originalLeaveStartDate != null) {
            return !cancellationStartDate.isBefore(originalLeaveStartDate);
        }

        return true;
    }

    /**
     * 检查销假是否已生效
     */
    public boolean isEffective() {
        return status == ApplicationStatus.EFFECTIVE && effectiveTime != null &&
               LocalDateTime.now().isAfter(effectiveTime);
    }

    /**
     * 获取申请进度百分比
     */
    public Integer getApplicationProgress() {
        switch (status) {
            case DRAFT:
                return 10;
            case SUBMITTED:
                return 30;
            case UNDER_REVIEW:
                return 60;
            case APPROVED:
            case REJECTED:
            case WITHDRAWN:
            case CANCELLED:
                return 100;
            case EFFECTIVE:
                return 100;
            case INVALID:
                return 0;
            default:
                return 0;
        }
    }
}