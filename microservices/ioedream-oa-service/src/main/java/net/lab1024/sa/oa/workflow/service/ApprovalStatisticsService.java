package net.lab1024.sa.oa.workflow.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.workflow.entity.ApprovalStatisticsEntity;

/**
 * 审批统计服务接口
 * <p>
 * 提供审批统计查询功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在ioedream-oa-service中
 * - 使用@Resource注入依赖
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ApprovalStatisticsService {

    /**
     * 根据业务类型和统计日期查询统计
     *
     * @param businessType 业务类型
     * @param statisticsDate 统计日期
     * @param statisticsDimension 统计维度（DAILY-按日 WEEKLY-按周 MONTHLY-按月 YEARLY-按年）
     * @return 统计实体
     */
    ResponseDTO<ApprovalStatisticsEntity> getStatistics(
            String businessType,
            LocalDate statisticsDate,
            String statisticsDimension);

    /**
     * 根据业务类型和时间范围查询统计列表
     *
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statisticsDimension 统计维度
     * @return 统计列表
     */
    ResponseDTO<List<ApprovalStatisticsEntity>> getStatisticsByDateRange(
            String businessType,
            LocalDate startDate,
            LocalDate endDate,
            String statisticsDimension);

    /**
     * 获取审批效率统计
     * <p>
     * 统计平均审批时长、最长审批时长、最短审批时长等
     * </p>
     *
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 效率统计数据
     */
    ResponseDTO<Map<String, Object>> getEfficiencyStatistics(
            String businessType,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取审批通过率统计
     *
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 通过率统计数据
     */
    ResponseDTO<Map<String, Object>> getApprovalRateStatistics(
            String businessType,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取审批人效率统计
     *
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 审批人效率统计数据
     */
    ResponseDTO<List<Map<String, Object>>> getApproverEfficiencyStatistics(
            String businessType,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取部门审批统计
     *
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 部门审批统计数据
     */
    ResponseDTO<List<Map<String, Object>>> getDepartmentStatistics(
            String businessType,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 生成统计报表
     * <p>
     * 自动生成指定时间段的统计报表
     * </p>
     *
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statisticsDimension 统计维度
     * @return 操作结果
     */
    ResponseDTO<Void> generateStatisticsReport(
            String businessType,
            LocalDate startDate,
            LocalDate endDate,
            String statisticsDimension);
}

