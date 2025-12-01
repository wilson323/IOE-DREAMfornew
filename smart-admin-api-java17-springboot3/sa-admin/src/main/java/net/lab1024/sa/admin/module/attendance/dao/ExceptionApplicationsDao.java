package net.lab1024.sa.admin.module.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.attendance.domain.entity.ExceptionApplicationsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异常申请数据访问层
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
@Mapper
@Repository
public interface ExceptionApplicationsDao extends BaseMapper<ExceptionApplicationsEntity> {

    /**
     * 根据条件分页查询异常申请
     *
     * @param page 分页参数
     * @param queryMap 查询条件
     * @return 分页结果
     */
    Page<ExceptionApplicationsEntity> selectPageByCondition(Page<ExceptionApplicationsEntity> page, @Param("query") Map<String, Object> queryMap);

    /**
     * 根据员工ID查询异常申请
     *
     * @param employeeId 员工ID
     * @param limit 限制数量
     * @return 申请列表
     */
    List<ExceptionApplicationsEntity> selectByEmployeeId(@Param("employeeId") Long employeeId, @Param("limit") Integer limit);

    /**
     * 根据状态查询异常申请
     *
     * @param status 申请状态
     * @param limit 限制数量
     * @return 申请列表
     */
    List<ExceptionApplicationsEntity> selectByStatus(@Param("status") String status, @Param("limit") Integer limit);

    /**
     * 根据员工ID和日期范围查询异常申请
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 申请列表
     */
    List<ExceptionApplicationsEntity> selectByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据异常类型和日期范围查询异常申请
     *
     * @param exceptionType 异常类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 申请列表
     */
    List<ExceptionApplicationsEntity> selectByExceptionTypeAndDateRange(
            @Param("exceptionType") String exceptionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询待审批的异常申请数量
     *
     * @param approverId 审批人ID（可选）
     * @return 数量
     */
    Long selectPendingCount(@Param("approverId") Long approverId);

    /**
     * 更新申请状态
     *
     * @param applicationId 申请ID
     * @param status 新状态
     * @param updateTime 更新时间
     * @return 更新行数
     */
    int updateStatus(@Param("applicationId") Long applicationId,
                    @Param("status") String status,
                    @Param("updateTime") LocalDateTime updateTime);

    /**
     * 批量更新申请状态
     *
     * @param applicationIds 申请ID列表
     * @param status 新状态
     * @param updateTime 更新时间
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("applicationIds") List<Long> applicationIds,
                         @Param("status") String status,
                         @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据工作流实例ID查询异常申请
     *
     * @param workflowInstanceId 工作流实例ID
     * @return 申请信息
     */
    ExceptionApplicationsEntity selectByWorkflowInstanceId(@Param("workflowInstanceId") String workflowInstanceId);

    /**
     * 查询指定时间范围内的申请统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    Map<String, Object> selectStatisticsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询员工的申请统计
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    Map<String, Object> selectEmployeeStatistics(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}