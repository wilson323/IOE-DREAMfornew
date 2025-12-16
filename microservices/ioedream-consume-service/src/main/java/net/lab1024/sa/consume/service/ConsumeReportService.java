package net.lab1024.sa.consume.service;

import java.time.LocalDateTime;
import java.util.Map;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.report.domain.form.ReportParams;

/**
 * 消费报表服务接口
 * <p>
 * 提供消费报表相关的业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回ResponseDTO格式
 * - 通过Service层调用Manager层
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeReportService {

    /**
     * 生成消费报表
     *
     * @param templateId 模板ID
     * @param params 报表参数
     * @return 报表数据
     */
    ResponseDTO<Map<String, Object>> generateReport(Long templateId, ReportParams params);

    /**
     * 导出报表
     *
     * @param templateId 模板ID
     * @param params 报表参数
     * @param exportFormat 导出格式（EXCEL/PDF/CSV）
     * @return 导出文件路径
     */
    ResponseDTO<String> exportReport(Long templateId, ReportParams params, String exportFormat);

    /**
     * 获取报表模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    ResponseDTO<?> getReportTemplates(String templateType);

    /**
     * 获取报表统计数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dimensions 统计维度
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getReportStatistics(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> dimensions);
}




