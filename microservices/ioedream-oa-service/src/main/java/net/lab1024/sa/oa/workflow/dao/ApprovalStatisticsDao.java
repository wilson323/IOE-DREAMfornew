package net.lab1024.sa.oa.workflow.dao;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.oa.workflow.entity.ApprovalStatisticsEntity;

/**
 * 审批统计DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ApprovalStatisticsDao extends BaseMapper<ApprovalStatisticsEntity> {

    /**
     * 根据业务类型和统计日期查询统计
     */
    ApprovalStatisticsEntity selectByBusinessTypeAndDate(
            @Param("businessType") String businessType,
            @Param("statisticsDate") LocalDate statisticsDate,
            @Param("statisticsDimension") String statisticsDimension);

    /**
     * 根据业务类型和时间范围查询统计列表
     */
    List<ApprovalStatisticsEntity> selectByBusinessTypeAndDateRange(
            @Param("businessType") String businessType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statisticsDimension") String statisticsDimension);

    /**
     * 查询统计信息
     */
    net.lab1024.sa.oa.workflow.domain.vo.ApprovalStatisticsVO selectStatistics(
            @Param("userId") Long userId,
            @Param("departmentId") Long departmentId,
            @Param("businessType") String businessType);
}




