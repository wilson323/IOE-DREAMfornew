package net.lab1024.sa.audit.domain.vo;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

/**
 * 合规报告VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class ComplianceReportVO {

    /**
     * 报告ID
     */
    private Long reportId;

    /**
     * 报告类型
     */
    private String reportType;

    /**
     * 报告标题
     */
    private String reportTitle;

    /**
     * 报告描述
     */
    private String description;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 合规得分
     */
    private Double complianceScore;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 报告状态
     */
    private String status; // DRAFT, PUBLISHED, ARCHIVED

    /**
     * 报告内容
     */
    private Map<String, Object> content;

    /**
     * 建议项
     */
    private java.util.List<String> recommendations;

    /**
     * 检查项
     */
    private java.util.Map<String, Object> checkItems;

    /**
     * 生成时间
     */
    private LocalDateTime generatedTime;

    /**
     * 生成人
     */
    private String generatedBy;

    /**
     * 审核时间
     */
    private LocalDateTime reviewedTime;

    /**
     * 审核人
     */
    private String reviewedBy;

    /**
     * 报告周期
     */
    private String reportPeriod;

    /**
     * 生成时间
     */
    private LocalDateTime generateTime;

    /**
     * 合规检查项列表
     */
    private java.util.List<ComplianceItemVO> complianceItems;

    /**
     * 总体合规得分
     */
    private Double totalScore;

    /**
     * 合规等级
     */
    private String complianceLevel;

    /**
     * 改进建议
     */
    private java.util.List<String> improvementSuggestions;
}
