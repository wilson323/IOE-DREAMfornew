package net.lab1024.sa.consume.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.consume.domain.form.ConsumeReportQueryForm;

/**
 * 消费报表服务接口
 * <p>
 * 完整的企业级实现，包含：
 * - 日常业务报表
 * - 统计分析报表
 * - 数据导出功能
 * - 图表数据生成
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Tag(name = "消费报表服务", description = "消费报表相关业务接口")
public interface ConsumeReportService {

    // ==================== 消费统计报表 ====================

    /**
     * 获取每日消费报表
     *
     * @param date 日期 (yyyy-MM-dd格式)
     * @return 日报表数据
     */
    @Operation(summary = "获取每日消费报表", description = "获取指定日期的消费日报表数据")
    Map<String, Object> getDailyReport(String date);

    /**
     * 获取月度消费报表
     *
     * @param year  年份
     * @param month 月份
     * @return 月报表数据
     */
    @Operation(summary = "获取月度消费报表", description = "获取指定年月的消费月报表数据")
    Map<String, Object> getMonthlyReport(Integer year, Integer month);

    /**
     * 获取年度消费报表
     *
     * @param year 年份
     * @return 年度报表数据
     */
    @Operation(summary = "获取年度消费报表", description = "获取指定年份的消费年度报表数据")
    Map<String, Object> getYearlyReport(Integer year);

    /**
     * 获取自定义报表
     *
     * @param form 查询表单
     * @return 自定义报表数据
     */
    @Operation(summary = "获取自定义报表", description = "根据自定义条件生成报表")
    Map<String, Object> getCustomReport(ConsumeReportQueryForm form);

    /**
     * 获取设备运营报表
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 设备运营报表数据
     */
    @Operation(summary = "获取设备运营报表", description = "获取指定设备在时间范围内的运营数据")
    Map<String, Object> getDeviceOperationReport(String deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户消费报表
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户消费报表数据
     */
    @Operation(summary = "获取用户消费报表", description = "获取指定用户在时间范围内的消费数据")
    Map<String, Object> getUserConsumptionReport(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取财务报表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 财务报表数据
     */
    @Operation(summary = "获取财务报表", description = "获取指定时间范围的财务数据报表")
    Map<String, Object> getFinancialReport(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取产品销售报表
     *
     * @param productId 产品ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 产品销售报表数据
     */
    @Operation(summary = "获取产品销售报表", description = "获取指定产品在时间范围内的销售数据")
    Map<String, Object> getProductSalesReport(Long productId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取趋势分析
     *
     * @param type 分析类型
     * @param days 统计天数
     * @return 趋势分析数据
     */
    @Operation(summary = "获取趋势分析", description = "获取指定类型和天数范围的消费趋势分析")
    Map<String, Object> getTrendAnalysis(String type, Integer days);

    /**
     * 获取对比分析
     *
     * @param currentStart 当前周期开始时间
     * @param currentEnd 当前周期结束时间
     * @param previousStart 对比周期开始时间
     * @param previousEnd 对比周期结束时间
     * @return 对比分析数据
     */
    @Operation(summary = "获取对比分析", description = "获取两个时间段的消费数据对比分析")
    Map<String, Object> getComparisonAnalysis(LocalDateTime currentStart, LocalDateTime currentEnd,
                                               LocalDateTime previousStart, LocalDateTime previousEnd);

    /**
     * 导出报表
     *
     * @param reportType 报表类型
     * @param format 导出格式
     * @param form 查询条件
     * @return 导出文件路径
     */
    @Operation(summary = "导出报表", description = "导出指定类型的报表文件")
    String exportReport(String reportType, String format, ConsumeReportQueryForm form);

    /**
     * 获取实时概览
     *
     * @return 实时概览数据
     */
    @Operation(summary = "获取实时概览", description = "获取消费系统的实时数据概览")
    Map<String, Object> getRealtimeOverview();

    /**
     * 获取异常分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 异常分析数据
     */
    @Operation(summary = "获取异常分析", description = "获取指定时间范围内的消费异常分析")
    Map<String, Object> getAbnormalAnalysis(String startDate, String endDate);

    /**
     * 获取消费排行
     *
     * @param type 排行类型
     * @param limit 限制数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 消费排行数据
     */
    @Operation(summary = "获取消费排行", description = "获取指定类型的消费排行榜")
    List<Map<String, Object>> getConsumptionRanking(String type, Integer limit, String startDate, String endDate);

    /**
     * 生成每日消费报表
     *
     * @param date    日期
     * @param format  报表格式 (excel/pdf/html)
     * @return 报表结果
     */
    @Operation(summary = "生成每日消费报表", description = "生成指定日期的消费汇总报表")
    Map<String, Object> generateDailyConsumptionReport(LocalDate date, String format);

    /**
     * 生成月度汇总报表
     *
     * @param yearMonth 年月 (yyyy-MM)
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成月度汇总报表", description = "生成指定月份的消费汇总报表")
    Map<String, Object> generateMonthlySummaryReport(String yearMonth, String format);

    /**
     * 生成产品分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成产品分析报表", description = "生成指定时间段的产品销售分析报表")
    Map<String, Object> generateProductAnalysisReport(String startDate, String endDate, String format);

    /**
     * 生成用户分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成用户分析报表", description = "生成指定时间段的用户消费分析报表")
    Map<String, Object> generateUserAnalysisReport(String startDate, String endDate, String format);

    // ==================== 财务分析报表 ====================

    /**
     * 生成收入分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成收入分析报表", description = "生成指定时间段的收入分析报表")
    Map<String, Object> generateRevenueAnalysisReport(String startDate, String endDate, String format);

    /**
     * 生成补贴使用报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成补贴使用报表", description = "生成指定时间段的补贴使用分析报表")
    Map<String, Object> generateSubsidyUsageReport(String startDate, String endDate, String format);

    /**
     * 生成充值统计报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成充值统计报表", description = "生成指定时间段的充值统计报表")
    Map<String, Object> generateRechargeStatisticsReport(String startDate, String endDate, String format);

    /**
     * 生成退款分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成退款分析报表", description = "生成指定时间段的退款分析报表")
    Map<String, Object> generateRefundAnalysisReport(String startDate, String endDate, String format);

    // ==================== 运营分析报表 ====================

    /**
     * 生成设备运营报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成设备运营报表", description = "生成指定时间段的设备运营分析报表")
    Map<String, Object> generateDeviceOperationReport(String startDate, String endDate, String format);

    /**
     * 生成商户分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成商户分析报表", description = "生成指定时间段的商户销售分析报表")
    Map<String, Object> generateMerchantAnalysisReport(String startDate, String endDate, String format);

    /**
     * 生成时段分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成时段分析报表", description = "生成指定时间段的消费时段分析报表")
    Map<String, Object> generateTimeSlotAnalysisReport(String startDate, String endDate, String format);

    /**
     * 生成区域分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    报表格式
     * @return 报表结果
     */
    @Operation(summary = "生成区域分析报表", description = "生成指定时间段的消费区域分析报表")
    Map<String, Object> generateAreaAnalysisReport(String startDate, String endDate, String format);

    // ==================== 实时数据报表 ====================

    /**
     * 生成实时消费概览
     *
     * @return 实时消费概览数据
     */
    @Operation(summary = "生成实时消费概览", description = "获取当前实时消费概览数据")
    Map<String, Object> generateRealTimeConsumptionOverview();

    /**
     * 生成今日实时统计
     *
     * @return 今日实时统计数据
     */
    @Operation(summary = "生成今日实时统计", description = "获取今日实时消费统计数据")
    Map<String, Object> generateTodayRealTimeStatistics();

    /**
     * 生成设备实时状态
     *
     * @return 设备实时状态数据
     */
    @Operation(summary = "生成设备实时状态", description = "获取所有设备的实时状态信息")
    Map<String, Object> generateDeviceRealTimeStatus();

    // ==================== 趋势分析报表 ====================

    /**
     * 生成消费趋势报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param period     统计周期 (day/week/month)
     * @return 趋势分析数据
     */
    @Operation(summary = "生成消费趋势报表", description = "生成指定时间段的消费趋势分析")
    Map<String, Object> generateConsumptionTrendReport(String startDate, String endDate, String period);

    /**
     * 生成同比增长报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param compareType 对比类型 (month/year)
     * @return 同比分析数据
     */
    @Operation(summary = "生成同比增长报表", description = "生成指定时间段的同比增长分析")
    Map<String, Object> generateYearOverYearGrowthReport(String startDate, String endDate, String compareType);

    /**
     * 生成预测分析报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param forecastDays 预测天数
     * @return 预测分析数据
     */
    @Operation(summary = "生成预测分析报表", description = "基于历史数据生成未来消费预测")
    Map<String, Object> generateForecastAnalysisReport(String startDate, String endDate, Integer forecastDays);

    // ==================== 自定义报表 ====================

    /**
     * 生成自定义报表
     *
     * @param reportConfig 报表配置
     * @return 自定义报表数据
     */
    @Operation(summary = "生成自定义报表", description = "根据配置生成自定义报表")
    Map<String, Object> generateCustomReport(Map<String, Object> reportConfig);

    /**
     * 保存报表模板
     *
     * @param templateName 模板名称
     * @param templateConfig 模板配置
     * @return 保存结果
     */
    @Operation(summary = "保存报表模板", description = "保存自定义报表模板")
    String saveReportTemplate(String templateName, Map<String, Object> templateConfig);

    /**
     * 获取报表模板列表
     *
     * @return 报表模板列表
     */
    @Operation(summary = "获取报表模板列表", description = "获取所有已保存的报表模板")
    Map<String, Object> getReportTemplateList();

    /**
     * 使用报表模板生成报表
     *
     * @param templateId 模板ID
     * @param parameters 参数
     * @return 报表数据
     */
    @Operation(summary = "使用报表模板生成报表", description = "使用已保存的模板生成报表")
    Map<String, Object> generateReportFromTemplate(String templateId, Map<String, Object> parameters);

    // ==================== 数据导出功能 ====================

    /**
     * 导出报表到文件
     *
     * @param reportData 报表数据
     * @param format     导出格式
     * @param response   HTTP响应
     */
    @Operation(summary = "导出报表到文件", description = "将报表数据导出为文件")
    void exportReportToFile(Map<String, Object> reportData, String format, HttpServletResponse response);

    /**
     * 异步生成报表
     *
     * @param reportType 报表类型
     * @param parameters 参数
     * @return 任务ID
     */
    @Operation(summary = "异步生成报表", description = "异步生成大型报表，返回任务ID")
    String generateReportAsync(String reportType, Map<String, Object> parameters);

    /**
     * 查询报表任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @Operation(summary = "查询报表任务状态", description = "查询异步报表生成任务的状态")
    Map<String, Object> getReportTaskStatus(String taskId);

    /**
     * 下载生成的报表
     *
     * @param taskId   任务ID
     * @param response HTTP响应
     */
    @Operation(summary = "下载生成的报表", description = "下载已生成的报表文件")
    void downloadGeneratedReport(String taskId, HttpServletResponse response);

    // ==================== 报表分析功能 ====================

    /**
     * 获取报表数据概要
     *
     * @param reportData 报表数据
     * @return 数据概要信息
     */
    @Operation(summary = "获取报表数据概要", description = "分析报表数据的统计概要")
    Map<String, Object> getReportDataSummary(Map<String, Object> reportData);

    /**
     * 获取报表建议
     *
     * @param reportType 报表类型
     * @param reportData 报表数据
     * @return 优化建议
     */
    @Operation(summary = "获取报表建议", description = "基于报表数据提供业务建议")
    Map<String, Object> getReportRecommendations(String reportType, Map<String, Object> reportData);

    /**
     * 验证报表数据
     *
     * @param reportData 报表数据
     * @return 验证结果
     */
    @Operation(summary = "验证报表数据", description = "验证报表数据的完整性和准确性")
    Map<String, Object> validateReportData(Map<String, Object> reportData);
}