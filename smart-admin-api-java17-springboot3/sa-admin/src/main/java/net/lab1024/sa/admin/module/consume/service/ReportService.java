package net.lab1024.sa.admin.module.consume.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报表服务接口
 * 严格遵循repowiki规范：定义消费模块报表生成的标准接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
public interface ReportService {

    /**
     * 生成消费报表
     *
     * @param params 查询参数
     * @return 报表数据
     */
    Map<String, Object> generateConsumeReport(Map<String, Object> params);

    /**
     * 生成充值报表
     *
     * @param params 查询参数
     * @return 报表数据
     */
    Map<String, Object> generateRechargeReport(Map<String, Object> params);

    /**
     * 生成用户消费统计报表
     *
     * @param params 查询参数
     * @return 报表数据
     */
    Map<String, Object> generateUserConsumeReport(Map<String, Object> params);

    /**
     * 生成设备使用报表
     *
     * @param params 查询参数
     * @return 报表数据
     */
    Map<String, Object> generateDeviceUsageReport(Map<String, Object> params);

    /**
     * 导出报表
     *
     * @param reportType 报表类型
     * @param params     查询参数
     * @param format     导出格式
     * @return 导出文件路径
     */
    String exportReport(String reportType, Map<String, Object> params, String format);

    /**
     * 获取报表列表
     *
     * @param params 查询参数
     * @return 报表列表
     */
    List<Map<String, Object>> getReportList(Map<String, Object> params);

    /**
     * 删除报表
     *
     * @param reportId 报表ID
     * @return 删除结果
     */
    boolean deleteReport(Long reportId);

    /**
     * 获取消费汇总
     */
    Map<String, Object> getConsumeSummary(String timeDimension, LocalDateTime startTime, LocalDateTime endTime,
            Long deviceId, String consumeMode);

    /**
     * 获取消费趋势
     */
    List<Map<String, Object>> getConsumeTrend(String timeDimension, LocalDateTime startTime, LocalDateTime endTime,
            String trendType, Long deviceId, String consumeMode);

    /**
     * 获取消费模式分布
     */
    List<Map<String, Object>> getConsumeModeDistribution(LocalDateTime startTime, LocalDateTime endTime, Long deviceId);

    /**
     * 获取设备消费排行
     */
    List<Map<String, Object>> getDeviceRanking(LocalDateTime startTime, LocalDateTime endTime, String rankingType,
            Integer limit);

    /**
     * 获取用户消费排行
     */
    List<Map<String, Object>> getUserRanking(LocalDateTime startTime, LocalDateTime endTime, String rankingType,
            Integer limit);

    /**
     * 获取时段分布
     */
    List<Map<String, Object>> getHourDistribution(LocalDateTime startTime, LocalDateTime endTime, Long deviceId);

    /**
     * 获取地区分布
     */
    List<Map<String, Object>> getRegionDistribution(LocalDateTime startTime, LocalDateTime endTime,
            String regionMetric);

    /**
     * 获取同比环比数据
     */
    Map<String, Object> getComparisonData(String comparisonType, LocalDateTime startTime, LocalDateTime endTime,
            Long deviceId, String consumeMode);

    /**
     * 导出报表（重载方法）
     */
    String exportReport(String reportType, String timeDimension, LocalDateTime startTime, LocalDateTime endTime,
            String format, Map<String, Object> exportParams);

    /**
     * 获取仪表盘数据
     */
    Map<String, Object> getDashboardData(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取实时统计
     */
    Map<String, Object> getRealTimeStatistics();

    /**
     * 获取异常检测结果
     */
    List<Map<String, Object>> getAnomalyDetection(LocalDateTime startTime, LocalDateTime endTime, String detectionType);

    /**
     * 获取预测分析数据
     */
    Map<String, Object> getForecastAnalysis(String forecastType, LocalDateTime startTime, LocalDateTime endTime,
            Integer forecastPeriod);

    // ========== 新增的方法 ==========

    /**
     * 导出Excel格式报表
     *
     * @param reportType 报表类型
     * @param params     查询参数
     * @return Excel文件路径
     */
    String exportExcelReport(String reportType, Map<String, Object> params);

    /**
     * 生成自定义报表
     *
     * @param params 查询参数
     * @return 报表数据
     */
    Map<String, Object> generateCustomReport(Map<String, Object> params);

    /**
     * 获取报表模板
     *
     * @param templateType 模板类型
     * @return 模板数据
     */
    Map<String, Object> getReportTemplate(String templateType);

    /**
     * 保存报表模板
     *
     * @param templateData 模板数据
     * @return 保存结果
     */
    boolean saveReportTemplate(Map<String, Object> templateData);

    /**
     * 定时生成报表
     *
     * @param reportType 报表类型
     * @param params     查询参数
     * @param schedule   定时配置
     * @return 调度ID
     */
    String scheduleReport(String reportType, Map<String, Object> params, Map<String, Object> schedule);

    /**
     * 获取报表历史记录
     *
     * @param params 查询参数
     * @return 历史记录列表
     */
    List<Map<String, Object>> getReportHistory(Map<String, Object> params);

    /**
     * 高级统计分析
     *
     * @param params 查询参数
     * @return 统计分析结果
     */
    Map<String, Object> advancedStatistics(Map<String, Object> params);

    /**
     * 获取异常分析报表
     *
     * @param params 查询参数
     * @return 异常分析结果
     */
    Map<String, Object> getAnomalyReport(Map<String, Object> params);

    /**
     * 获取预测分析报表
     *
     * @param params 查询参数
     * @return 预测分析结果
     */
    Map<String, Object> getForecastReport(Map<String, Object> params);

    /**
     * 批量生成报表
     *
     * @param reportTypes 报表类型列表
     * @param params      查询参数
     * @return 批量生成结果
     */
    Map<String, Object> batchGenerateReports(List<String> reportTypes, Map<String, Object> params);

    /**
     * 获取报表元数据
     *
     * @param reportType 报表类型
     * @return 元数据信息
     */
    Map<String, Object> getReportMetadata(String reportType);

    /**
     * 验证报表参数
     *
     * @param reportType 报表类型
     * @param params     查询参数
     * @return 验证结果
     */
    Map<String, Object> validateReportParams(String reportType, Map<String, Object> params);
}
