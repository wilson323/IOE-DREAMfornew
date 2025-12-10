package net.lab1024.sa.common.workflow.dao;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.workflow.domain.vo.ApprovalStatisticsVO;
import net.lab1024.sa.common.workflow.entity.ApprovalStatisticsEntity;

/**
 * 审批统计DAO
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ApprovalStatisticsDao extends BaseMapper<ApprovalStatisticsEntity> {

    /**
     * 根据业务类型和统计日期查询统计
     *
     * @param businessType 业务类型
     * @param statisticsDate 统计日期
     * @param statisticsDimension 统计维度
     * @return 统计实体
     */
    ApprovalStatisticsEntity selectByBusinessTypeAndDate(
            @Param("businessType") String businessType,
            @Param("statisticsDate") LocalDate statisticsDate,
            @Param("statisticsDimension") String statisticsDimension);

    /**
     * 根据业务类型和时间范围查询统计列表
     *
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statisticsDimension 统计维度
     * @return 统计列表
     */
    List<ApprovalStatisticsEntity> selectByBusinessTypeAndDateRange(
            @Param("businessType") String businessType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statisticsDimension") String statisticsDimension);

    /**
     * 查询审批统计信息
     *
     * @param userId 用户ID（可选）
     * @param departmentId 部门ID（可选）
     * @param statisticsType 统计类型（day/week/month）
     * @return 统计信息VO
     */
    @Transactional(readOnly = true)
    ApprovalStatisticsVO selectStatistics(
            @Param("userId") Long userId,
            @Param("departmentId") Long departmentId,
            @Param("statisticsType") String statisticsType);
}

