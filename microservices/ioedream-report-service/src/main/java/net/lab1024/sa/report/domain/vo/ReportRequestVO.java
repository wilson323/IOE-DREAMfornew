package net.lab1024.sa.report.domain.vo;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报表请求VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class ReportRequestVO {

    /**
     * 模板ID
     */
    @NotNull(message = "模板ID不能为空")
    private Long templateId;

    /**
     * 报表参数
     */
    private Map<String, Object> params;

    /**
     * 是否包含图表
     */
    private Boolean includeCharts = true;

    /**
     * 是否异步生成
     */
    private Boolean async = false;

    /**
     * 导出格式
     */
    private String exportFormat;

    /**
     * 数据开始时间
     */
    private String startTime;

    /**
     * 数据结束时间
     */
    private String endTime;

    /**
     * 分页页码
     */
    private Integer pageNum = 1;

    /**
     * 分页大小
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式 ASC/DESC
     */
    private String sortOrder = "ASC";

    /**
     * 报表类型（可选）
     * 用于区分不同类型的报表，如：DATA, STATISTICS, ANALYSIS等
     */
    private String reportType;

    // ========== 兼容性方法 ==========

    /**
     * 获取报表类型
     *
     * @return 报表类型
     */
    public String getReportType() {
        return reportType;
    }

    /**
     * 设置报表类型
     *
     * @param reportType 报表类型
     */
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    /**
     * 获取报表参数（兼容getParameters方法名）
     *
     * @return 报表参数Map
     */
    public Map<String, Object> getParameters() {
        return params;
    }

    /**
     * 设置报表参数（兼容setParameters方法名）
     *
     * @param parameters 报表参数Map
     */
    public void setParameters(Map<String, Object> parameters) {
        this.params = parameters;
    }
}
