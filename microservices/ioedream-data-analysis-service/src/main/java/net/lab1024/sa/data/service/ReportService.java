package net.lab1024.sa.data.service;

import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.data.domain.DataAnalysisDomain.*;

import java.util.List;

/**
 * 数据报表服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface ReportService {

    // ==================== 报表管理 ====================

    /**
     * 创建报表
     *
     * @param report 报表配置
     * @return 报表ID
     */
    Long createReport(ReportVO report);

    /**
     * 更新报表
     *
     * @param reportId 报表ID
     * @param report   报表配置
     */
    void updateReport(Long reportId, ReportVO report);

    /**
     * 删除报表
     *
     * @param reportId 报表ID
     */
    void deleteReport(Long reportId);

    /**
     * 获取报表详情
     *
     * @param reportId 报表ID
     * @return 报表详情
     */
    ReportVO getReportDetail(Long reportId);

    /**
     * 获取报表列表（分页）
     *
     * @param pageNum         页码
     * @param pageSize        页大小
     * @param businessModule  业务模块
     * @param reportType      报表类型
     * @param status          状态
     * @return 报表列表
     */
    PageResult<ReportVO> getReportList(Integer pageNum, Integer pageSize,
                                        String businessModule, String reportType, String status);

    /**
     * 复制报表
     *
     * @param reportId    报表ID
     * @param newReportName 新报表名称
     * @return 新报表ID
     */
    Long copyReport(Long reportId, String newReportName);

    // ==================== 报表查询 ====================

    /**
     * 查询报表数据
     *
     * @param request 查询请求
     * @return 查询结果
     */
    ReportQueryResult queryReportData(ReportQueryRequest request);

    /**
     * 刷新报表数据
     *
     * @param reportId 报表ID
     * @return 查询结果
     */
    ReportQueryResult refreshReportData(Long reportId);

    /**
     * 获取报表预览数据
     *
     * @param reportId 报表ID
     * @return 预览数据（前10条）
     */
    ReportQueryResult getReportPreview(Long reportId);

    // ==================== 数据导出 ====================

    /**
     * 导出报表数据
     *
     * @param request 导出请求
     * @return 导出任务ID
     */
    String exportReportData(DataExportRequest request);

    /**
     * 获取导出任务状态
     *
     * @param exportTaskId 导出任务ID
     * @return 导出结果
     */
    DataExportResult getExportStatus(String exportTaskId);

    /**
     * 批量导出
     *
     * @param reportIds 报表ID列表
     * @param format    导出格式
     * @return 导出任务ID列表
     */
    List<String> batchExport(List<Long> reportIds, String format);

    // ==================== 数据统计 ====================

    /**
     * 获取统计数据
     *
     * @param reportId  报表ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    List<DataStatisticsVO> getStatistics(Long reportId, String startDate, String endDate);

    /**
     * 获取趋势数据
     *
     * @param reportId    报表ID
     * @param field       统计字段
     * @param period      时间周期（day, week, month）
     * @param periodCount 周期数
     * @return 趋势数据
     */
    ChartData getTrendData(Long reportId, String field, String period, Integer periodCount);

    /**
     * 获取对比数据
     *
     * @param reportId       报表ID
     * @param compareField   对比字段
     * @param comparePeriods 对比周期（当前周期、上一周期）
     * @return 对比数据
     */
    Map<String, Object> getCompareData(Long reportId, String compareField, List<String> comparePeriods);

    // ==================== 报表缓存 ====================

    /**
     * 清除报表缓存
     *
     * @param reportId 报表ID
     */
    void clearReportCache(Long reportId);

    /**
     * 预加载报表数据
     *
     * @param reportIds 报表ID列表
     */
    void preloadReportData(List<Long> reportIds);

    // ==================== 报表权限 ====================

    /**
     * 设置报表权限
     *
     * @param reportId  报表ID
     * @param permission 权限配置
     */
    void setReportPermission(Long reportId, ReportPermission permission);

    /**
     * 检查报表权限
     *
     * @param reportId 报表ID
     * @param permissionType 权限类型（view, edit, export）
     * @return 是否有权限
     */
    Boolean checkReportPermission(Long reportId, String permissionType);

    /**
     * 获取用户可见的报表列表
     *
     * @param userId 用户ID
     * @return 报表列表
     */
    List<ReportVO> getUserVisibleReports(Long userId);
}
