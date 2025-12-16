package net.lab1024.sa.consume.report.manager;

import java.util.Map;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.report.domain.form.ReportParams;

/**
 * 消费报表管理Manager接口
 * <p>
 * 用于报表相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中不使用Spring注解
 * - Manager类通过构造函数注入依赖
 * - 保持为纯Java类
 * </p>
 * <p>
 * 业务场景：
 * - 报表数据生成
 * - 报表模板管理
 * - 报表统计分析
 * - 报表导出
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeReportManager {

    /**
     * 生成消费报表
     * <p>
     * 类型安全改进：使用ReportParams类型，而非Object
     * 提升代码可读性和类型安全性
     * </p>
     *
     * @param templateId 模板ID
     * @param params 报表参数（如果为null则使用默认参数）
     * @return 报表数据
     */
    ResponseDTO<Map<String, Object>> generateReport(Long templateId, ReportParams params);

    /**
     * 获取报表模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    ResponseDTO<?> getReportTemplates(String templateType);

    /**
     * 获取报表统计数据
     * <p>
     * 类型安全改进：使用Map<String, Object>类型，而非Object
     * 提升代码可读性和类型安全性
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dimensions 统计维度（Map格式，如果为null则使用默认维度）
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getReportStatistics(
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime,
            Map<String, Object> dimensions);

    /**
     * 导出报表（Excel/PDF/CSV）
     * <p>
     * 功能说明：
     * 1. 根据模板ID生成报表数据
     * 2. 根据导出格式（EXCEL/PDF/CSV）导出文件
     * 3. 返回文件路径或下载URL
     * </p>
     *
     * @param templateId 模板ID
     * @param params 报表参数
     * @param exportFormat 导出格式（EXCEL/PDF/CSV）
     * @return 导出文件路径
     */
    ResponseDTO<String> exportReport(Long templateId, ReportParams params, String exportFormat);
}



