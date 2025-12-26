package net.lab1024.sa.consume.service;

import jakarta.servlet.http.HttpServletResponse;
import net.lab1024.sa.consume.domain.form.ConsumptionAnalysisQueryForm;

import java.io.IOException;

/**
 * 消费数据导出服务接口
 * <p>
 * 提供PDF和Excel格式的数据导出功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
public interface ConsumeExportService {

    /**
     * 导出消费分析PDF报告
     *
     * @param queryForm 查询条件
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportAnalysisPdf(ConsumptionAnalysisQueryForm queryForm, HttpServletResponse response)
            throws IOException;

    /**
     * 导出消费分析Excel报告
     *
     * @param queryForm 查询条件
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportAnalysisExcel(ConsumptionAnalysisQueryForm queryForm, HttpServletResponse response)
            throws IOException;

    /**
     * 导出消费记录Excel
     *
     * @param userId 用户ID
     * @param period 时间周期
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportRecordsExcel(Long userId, String period, HttpServletResponse response)
            throws IOException;
}
