package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.entity.AccessEventEntity;
import net.lab1024.sa.access.domain.entity.AccessRecordEntity;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Access record service interface (增强版)
 * <p>
 * 严格遵循repowiki规范：
 * - 基于现有AccessRecordService增强，避免重复建设
 * - 添加高级查询、统计报表、数据导出功能
 * - 支持大数据量查询（≥100万条记录）
 * - 复杂查询响应时间≤3秒
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 * @version 2.0 Enhanced for OpenSpec Task 2.4
 */
public interface AccessRecordService {

    // ==================== 原有基础功能 ====================

    /**
     * Record access event
     */
    ResponseDTO<String> recordAccessEvent(AccessRecordEntity record);

    /**
     * Query access records by page
     */
    PageResult<AccessRecordEntity> queryAccessRecordPage(PageParam pageParam, Long userId, Long deviceId,
            String accessResult);

    /**
     * Get access record detail
     */
    ResponseDTO<AccessRecordEntity> getAccessRecordDetail(Long recordId);

    /**
     * Verify access permission
     */
    ResponseDTO<Boolean> verifyAccessPermission(Long userId, Long deviceId);

    // ==================== 增强功能 - OpenSpec Task 2.4 ====================

    /**
     * 多维条件分页查询门禁事件（增强版）
     * 支持时间、用户、设备、区域、验证结果等多维度组合筛选
     *
     * @param pageParam 分页参数
     * @param queryParams 查询参数（包含所有筛选条件）
     * @return 分页结果
     */
    PageResult<AccessEventEntity> queryEventsByMultipleConditions(PageParam pageParam, Map<String, Object> queryParams);

    /**
     * 生成访问统计报表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param reportType 报表类型（daily/weekly/monthly/custom）
     * @param areaIds 区域ID列表（可选）
     * @return 统计报表数据
     */
    ResponseDTO<Map<String, Object>> generateAccessStatisticsReport(LocalDateTime startTime,
        LocalDateTime endTime, String reportType, List<Long> areaIds);

    /**
     * 导出访问记录数据
     *
     * @param queryParams 查询参数
     * @param exportFormat 导出格式（EXCEL/PDF/CSV）
     * @param exportType 导出类型（all/currentPage/selected）
     * @param selectedIds 选中的记录ID（exportType为selected时使用）
     * @return 导出文件路径或下载URL
     */
    ResponseDTO<String> exportAccessRecords(Map<String, Object> queryParams, String exportFormat,
        String exportType, List<Long> selectedIds);

    /**
     * 获取用户访问轨迹
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户访问轨迹数据
     */
    ResponseDTO<List<Map<String, Object>>> getUserAccessTrajectory(Long userId,
        LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取设备访问统计
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 设备访问统计
     */
    ResponseDTO<Map<String, Object>> getDeviceAccessStatistics(Long deviceId,
        LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取区域访问统计
     *
     * @param areaIds 区域ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 区域访问统计
     */
    ResponseDTO<List<Map<String, Object>>> getAreaAccessStatistics(List<Long> areaIds,
        LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取异常事件列表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param eventLevel 事件级别（可选）
     * @param alarmType 报警类型（可选）
     * @return 异常事件列表
     */
    ResponseDTO<List<AccessEventEntity>> getAbnormalEvents(LocalDateTime startTime,
        LocalDateTime endTime, Integer eventLevel, Integer alarmType);

    /**
     * 获取黑名单访问记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 黑名单访问记录分页结果
     */
    PageResult<AccessEventEntity> getBlacklistEvents(LocalDateTime startTime,
        LocalDateTime endTime, Integer pageNum, Integer pageSize);

    /**
     * 批量处理事件记录
     *
     * @param events 事件记录列表
     * @return 处理结果
     */
    ResponseDTO<Map<String, Object>> batchProcessEvents(List<AccessEventEntity> events);

    /**
     * 获取实时访问数据看板
     *
     * @return 实时数据看板信息
     */
    ResponseDTO<Map<String, Object>> getRealtimeAccessDashboard();

    /**
     * 获取访问趋势分析
     *
     * @param timeRange 时间范围（hour/day/week/month）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dimension 分析维度（user/device/area/verifyMethod）
     * @return 趋势分析数据
     */
    ResponseDTO<Map<String, Object>> getAccessTrendAnalysis(String timeRange,
        LocalDateTime startTime, LocalDateTime endTime, String dimension);

    /**
     * 清理过期事件记录
     *
     * @param retainDays 保留天数
     * @return 清理结果
     */
    ResponseDTO<Map<String, Object>> cleanupExpiredEvents(Integer retainDays);

    /**
     * 获取访问数据质量报告
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数据质量报告
     */
    ResponseDTO<Map<String, Object>> getDataQualityReport(LocalDateTime startTime,
        LocalDateTime endTime);
}