package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.lab1024.sa.attendance.entity.AttendanceAnomalyApplyEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤异常申请DAO接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AttendanceAnomalyApplyDao extends BaseMapper<AttendanceAnomalyApplyEntity> {

    /**
     * 根据申请单号查询
     *
     * @param applyNo 申请单号
     * @return 申请记录
     */
    AttendanceAnomalyApplyEntity selectByApplyNo(@Param("applyNo") String applyNo);

    /**
     * 根据申请人ID查询申请记录
     *
     * @param applicantId 申请人ID
     * @return 申请记录列表
     */
    List<AttendanceAnomalyApplyEntity> selectByApplicantId(@Param("applicantId") Long applicantId);

    /**
     * 根据申请状态查询
     *
     * @param status 申请状态
     * @return 申请记录列表
     */
    List<AttendanceAnomalyApplyEntity> selectByStatus(@Param("status") String status);

    /**
     * 查询待审批的申请列表
     *
     * @return 待审批申请列表
     */
    List<AttendanceAnomalyApplyEntity> selectPendingApplications();

    /**
     * 根据异常记录ID查询申请记录
     *
     * @param anomalyId 异常记录ID
     * @return 申请记录列表
     */
    List<AttendanceAnomalyApplyEntity> selectByAnomalyId(@Param("anomalyId") Long anomalyId);

    /**
     * 统计用户在某月的补卡次数
     *
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @return 补卡次数
     */
    Integer countSupplementCardsByMonth(@Param("userId") Long userId,
                                        @Param("year") Integer year,
                                        @Param("month") Integer month);

    /**
     * 查询部门在日期范围内的申请记录
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 申请记录列表
     */
    List<AttendanceAnomalyApplyEntity> selectByDepartmentAndDateRange(@Param("departmentId") Long departmentId,
                                                                      @Param("startDate") LocalDate startDate,
                                                                      @Param("endDate") LocalDate endDate);
}
