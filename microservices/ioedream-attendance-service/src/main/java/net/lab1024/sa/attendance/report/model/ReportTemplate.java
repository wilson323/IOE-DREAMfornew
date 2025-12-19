package net.lab1024.sa.attendance.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报表模板
 * <p>
 * 用于保存报表的可复用配置（简化模型）。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTemplate {

    private String templateId;

    private String templateName;

    private ReportType reportType;

    /**
     * 模板配置（保留为扩展对象，避免过度设计）
     */
    private Object config;
}

