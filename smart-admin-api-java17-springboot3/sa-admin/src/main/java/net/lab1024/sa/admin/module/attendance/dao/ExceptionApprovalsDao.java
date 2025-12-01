package net.lab1024.sa.admin.module.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.attendance.domain.entity.ExceptionApprovalsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异常审批记录数据访问层
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
@Mapper
@Repository
public interface ExceptionApprovalsDao extends BaseMapper<ExceptionApprovalsEntity> {

    /**
     * 根据申请ID查询审批记录
     *
     * @param applicationId 申请ID
     * @return 审批记录列表
     */
    List<ExceptionApprovalsEntity> selectByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 根据审批人ID查询审批记录
     *
     * @param approverId 审批人ID
     * @param limit 限制数量
     * @return 审批记录列表
     */
    List<ExceptionApprovalsEntity> selectByApproverId(@Param("approverId") Long approverId, @Param("limit") Integer limit);

    /**
     * 根据申请ID和审批人ID查询审批记录
     *
     * @param applicationId 申请ID
     * @param approverId 审批人ID
     * @return 审批记录
     */
    ExceptionApprovalsEntity selectByApplicationIdAndApproverId(
            @Param("applicationId") Long applicationId,
            @Param("approverId") Long approverId);

    /**
     * 查询指定时间范围内的审批记录
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param approverId 审批人ID（可选）
     * @return 审批记录列表
     */
    List<ExceptionApprovalsEntity> selectByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("approverId") Long approverId);

    /**
     * 分页查询审批记录
     *
     * @param page 分页参数
     * @param queryMap 查询条件
     * @return 分页结果
     */
    Page<ExceptionApprovalsEntity> selectPageByCondition(Page<ExceptionApprovalsEntity> page, @Param("query") Map<String, Object> queryMap);

    /**
     * 查询审批人的审批统计
     *
     * @param approverId 审批人ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 统计结果
     */
    Map<String, Object> selectApproverStatistics(
            @Param("approverId") Long approverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 查询申请的最新审批记录
     *
     * @param applicationId 申请ID
     * @return 最新审批记录
     */
    ExceptionApprovalsEntity selectLatestByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 删除申请的所有审批记录
     *
     * @param applicationId 申请ID
     * @return 删除行数
     */
    int deleteByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 统计审批记录数量
     *
     * @param approverId 审批人ID（可选）
     * @param approvalResult 审批结果（可选）
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @return 数量
     */
    Long countApprovals(@Param("approverId") Long approverId,
                        @Param("approvalResult") String approvalResult,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

    /**
     * 查询延迟审批的记录
     *
     * @param hours 延迟小时数
     * @return 延迟审批记录列表
     */
    List<ExceptionApprovalsEntity> selectDelayedApprovals(@Param("hours") Integer hours);
}