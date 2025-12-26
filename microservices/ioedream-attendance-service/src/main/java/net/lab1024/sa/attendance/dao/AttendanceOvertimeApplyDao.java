package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.AttendanceOvertimeApplyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 加班申请DAO接口
 * <p>
 * 提供加班申请的数据访问操作
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AttendanceOvertimeApplyDao extends BaseMapper<AttendanceOvertimeApplyEntity> {

    /**
     * 根据申请编号查询
     *
     * @param applyNo 申请编号
     * @return 加班申请实体
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE apply_no = #{applyNo} AND deleted_flag = 0")
    AttendanceOvertimeApplyEntity selectByApplyNo(@Param("applyNo") String applyNo);

    /**
     * 根据申请人ID查询申请列表
     *
     * @param applicantId 申请人ID
     * @return 申请列表
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE applicant_id = #{applicantId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AttendanceOvertimeApplyEntity> selectByApplicantId(@Param("applicantId") Long applicantId);

    /**
     * 根据部门ID查询申请列表
     *
     * @param departmentId 部门ID
     * @return 申请列表
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE department_id = #{departmentId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AttendanceOvertimeApplyEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据申请状态查询列表
     *
     * @param applyStatus 申请状态
     * @return 申请列表
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE apply_status = #{applyStatus} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AttendanceOvertimeApplyEntity> selectByStatus(@Param("applyStatus") String applyStatus);

    /**
     * 根据加班类型查询列表
     *
     * @param overtimeType 加班类型
     * @return 申请列表
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE overtime_type = #{overtimeType} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AttendanceOvertimeApplyEntity> selectByOvertimeType(@Param("overtimeType") String overtimeType);

    /**
     * 查询指定日期范围内的申请列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 申请列表
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE overtime_date BETWEEN #{startDate} AND #{endDate} AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeApplyEntity> selectByDateRange(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    /**
     * 查询待审批的申请列表（根据当前审批人）
     *
     * @param approverId 当前审批人ID
     * @return 申请列表
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE approver_id = #{approverId} AND apply_status = 'PENDING' AND deleted_flag = 0 ORDER BY create_time ASC")
    List<AttendanceOvertimeApplyEntity> selectPendingApprovalsByApprover(@Param("approverId") Long approverId);

    /**
     * 查询指定审批层级的申请列表
     *
     * @param approvalLevel 审批层级
     * @return 申请列表
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE approval_level = #{approvalLevel} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AttendanceOvertimeApplyEntity> selectByApprovalLevel(@Param("approvalLevel") Integer approvalLevel);

    /**
     * 查询指定工作流的申请列表
     *
     * @param workflowInstanceId 工作流实例ID
     * @return 申请列表
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE workflow_instance_id = #{workflowInstanceId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AttendanceOvertimeApplyEntity> selectByWorkflowInstanceId(@Param("workflowInstanceId") Long workflowInstanceId);

    /**
     * 统计指定用户的申请数量（按状态）
     *
     * @param applicantId 申请人ID
     * @param applyStatus 申请状态
     * @return 申请数量
     */
    @Select("SELECT COUNT(*) FROM t_attendance_overtime_apply WHERE applicant_id = #{applicantId} AND apply_status = #{applyStatus} AND deleted_flag = 0")
    Integer countByApplicantAndStatus(@Param("applicantId") Long applicantId,
                                       @Param("applyStatus") String applyStatus);

    /**
     * 统计指定部门的加班时长
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 总加班时长（小时）
     */
    @Select("SELECT SUM(IFNULL(actual_hours, 0)) FROM t_attendance_overtime_apply WHERE department_id = #{departmentId} AND overtime_date BETWEEN #{startDate} AND #{endDate} AND apply_status = 'APPROVED' AND deleted_flag = 0")
    java.math.BigDecimal sumOvertimeHoursByDepartment(@Param("departmentId") Long departmentId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 查询重复申请（同一人员、同一日期、已批准）
     *
     * @param applicantId  申请人ID
     * @param overtimeDate 加班日期
     * @return 申请列表
     */
    @Select("SELECT * FROM t_attendance_overtime_apply WHERE applicant_id = #{applicantId} AND overtime_date = #{overtimeDate} AND apply_status IN ('APPROVED', 'PENDING') AND deleted_flag = 0")
    List<AttendanceOvertimeApplyEntity> selectDuplicateApply(@Param("applicantId") Long applicantId,
                                                             @Param("overtimeDate") java.time.LocalDate overtimeDate);
}
