package net.lab1024.sa.audit.domain.form;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 合规报告查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class ComplianceReportQueryForm {

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 报告类型
     */
    private String reportType;

    /**
     * 合规标准
     */
    private String complianceStandard;
}
