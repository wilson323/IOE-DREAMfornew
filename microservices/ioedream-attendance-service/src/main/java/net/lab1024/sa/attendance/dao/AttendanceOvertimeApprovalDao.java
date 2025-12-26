package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.entity.AttendanceOvertimeApprovalEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 加班审批记录DAO接口
 * <p>
 * 提供加班审批记录的数据访问操作
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AttendanceOvertimeApprovalDao extends BaseMapper<AttendanceOvertimeApprovalEntity> {

    /**
     * 根据申请ID查询审批记录列表
     *
     * @param applyId 申请ID
     * @return 审批记录列表（按审批层级排序）
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE apply_id = #{applyId} AND deleted_flag = 0 ORDER BY approval_level ASC")
    List<AttendanceOvertimeApprovalEntity> selectByApplyId(@Param("applyId") Long applyId);

    /**
     * 根据申请编号查询审批记录列表
     *
     * @param applyNo 申请编号
     * @return 审批记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE apply_no = #{applyNo} AND deleted_flag = 0 ORDER BY approval_level ASC")
    List<AttendanceOvertimeApprovalEntity> selectByApplyNo(@Param("applyNo") String applyNo);

    /**
     * 根据审批人ID查询审批记录列表
     *
     * @param approverId 审批人ID
     * @return 审批记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE approver_id = #{approverId} AND deleted_flag = 0 ORDER BY approval_time DESC")
    List<AttendanceOvertimeApprovalEntity> selectByApproverId(@Param("approverId") Long approverId);

    /**
     * 根据审批层级查询记录列表
     *
     * @param approvalLevel 审批层级
     * @return 审批记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE approval_level = #{approvalLevel} AND deleted_flag = 0 ORDER BY approval_time DESC")
    List<AttendanceOvertimeApprovalEntity> selectByApprovalLevel(@Param("approvalLevel") Integer approvalLevel);

    /**
     * 根据审批操作查询记录列表
     *
     * @param approvalAction 审批操作（APPROVE-批准 REJECT-驳回 CANCEL-撤销）
     * @return 审批记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE approval_action = #{approvalAction} AND deleted_flag = 0 ORDER BY approval_time DESC")
    List<AttendanceOvertimeApprovalEntity> selectByApprovalAction(@Param("approvalAction") String approvalAction);

    /**
     * 查询指定时间范围内的审批记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 审批记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE approval_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0 ORDER BY approval_time DESC")
    List<AttendanceOvertimeApprovalEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定申请的最新审批记录
     *
     * @param applyId 申请ID
     * @return 最新审批记录
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE apply_id = #{applyId} AND deleted_flag = 0 ORDER BY approval_time DESC LIMIT 1")
    AttendanceOvertimeApprovalEntity selectLatestByApplyId(@Param("applyId") Long applyId);

    /**
     * 查询指定申请和层级的审批记录
     *
     * @param applyId       申请ID
     * @param approvalLevel 审批层级
     * @return 审批记录
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE apply_id = #{applyId} AND approval_level = #{approvalLevel} AND deleted_flag = 0")
    AttendanceOvertimeApprovalEntity selectByApplyIdAndLevel(@Param("applyId") Long applyId,
                                                             @Param("approvalLevel") Integer approvalLevel);

    /**
     * 查询指定审批人在指定时间范围内的审批记录
     *
     * @param approverId 审批人ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 审批记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE approver_id = #{approverId} AND approval_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0 ORDER BY approval_time DESC")
    List<AttendanceOvertimeApprovalEntity> selectByApproverIdAndTimeRange(@Param("approverId") Long approverId,
                                                                          @Param("startTime") LocalDateTime startTime,
                                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询待处理的审批记录（指定审批人）
     *
     * @param approverId 审批人ID
     * @return 待处理申请的审批记录列表
     */
    @Select("SELECT a.* FROM t_attendance_overtime_approval a INNER JOIN t_attendance_overtime_apply s ON a.apply_id = s.apply_id WHERE a.approver_id = #{approverId} AND s.apply_status = 'PENDING' AND a.deleted_flag = 0 ORDER BY a.create_time ASC")
    List<AttendanceOvertimeApprovalEntity> selectPendingByApproverId(@Param("approverId") Long approverId);

    /**
     * 查询指定申请的所有已批准的审批记录
     *
     * @param applyId 申请ID
     * @return 已批准的审批记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE apply_id = #{applyId} AND approval_action = 'APPROVE' AND deleted_flag = 0 ORDER BY approval_level ASC")
    List<AttendanceOvertimeApprovalEntity> selectApprovedByApplyId(@Param("applyId") Long applyId);

    /**
     * 查询指定申请的所有已驳回的审批记录
     *
     * @param applyId 申请ID
     * @return 已驳回的审批记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_approval WHERE apply_id = #{applyId} AND approval_action = 'REJECT' AND deleted_flag = 0 ORDER BY approval_time DESC")
    List<AttendanceOvertimeApprovalEntity> selectRejectedByApplyId(@Param("applyId") Long applyId);

    /**
     * 统计审批人的审批数量（按操作类型）
     *
     * @param approverId      审批人ID
     * @param approvalAction  审批操作
     * @return 审批数量
     */
    @Select("SELECT COUNT(*) FROM t_attendance_overtime_approval WHERE approver_id = #{approverId} AND approval_action = #{approvalAction} AND deleted_flag = 0")
    Integer countByApproverAndAction(@Param("approverId") Long approverId,
                                     @Param("approvalAction") String approvalAction);

    /**
     * 统计审批人指定时间范围内的审批数量
     *
     * @param approverId 审批人ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 审批数量
     */
    @Select("SELECT COUNT(*) FROM t_attendance_overtime_approval WHERE approver_id = #{approverId} AND approval_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0")
    Integer countByApproverIdAndTimeRange(@Param("approverId") Long approverId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定申请的审批记录数
     *
     * @param applyId 申请ID
     * @return 审批记录数
     */
    @Select("SELECT COUNT(*) FROM t_attendance_overtime_approval WHERE apply_id = #{applyId} AND deleted_flag = 0")
    Integer countByApplyId(@Param("applyId") Long applyId);

    /**
     * 查询指定申请的最大审批层级
     *
     * @param applyId 申请ID
     * @return 最大审批层级
     */
    @Select("SELECT MAX(approval_level) FROM t_attendance_overtime_approval WHERE apply_id = #{applyId} AND deleted_flag = 0")
    Integer selectMaxApprovalLevelByApplyId(@Param("applyId") Long applyId);

    /**
     * 统计各部门的审批次数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果（包含部门ID、审批次数）
     */
    @Select("SELECT s.department_id, s.department_name, COUNT(*) as approval_count FROM t_attendance_overtime_approval a INNER JOIN t_attendance_overtime_apply s ON a.apply_id = s.apply_id WHERE a.approval_time BETWEEN #{startTime} AND #{endTime} AND a.deleted_flag = 0 GROUP BY s.department_id, s.department_name ORDER BY approval_count DESC")
    List<java.util.Map<String, Object>> statisticsApprovalsByDepartment(@Param("startTime") LocalDateTime startTime,
                                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各审批人的审批次数排名
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     返回记录数
     * @return 统计结果（包含审批人ID、审批人姓名、审批次数）
     */
    @Select("SELECT approver_id, approver_name, COUNT(*) as approval_count FROM t_attendance_overtime_approval WHERE approval_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0 GROUP BY approver_id, approver_name ORDER BY approval_count DESC LIMIT #{limit}")
    List<java.util.Map<String, Object>> statisticsTopApprovers(@Param("startTime") LocalDateTime startTime,
                                                                @Param("endTime") LocalDateTime endTime,
                                                                @Param("limit") Integer limit);

    /**
     * 统计审批操作类型的分布
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果（包含审批操作、数量）
     */
    @Select("SELECT approval_action, COUNT(*) as action_count FROM t_attendance_overtime_approval WHERE approval_time BETWEEN #{startTime} AND #{endTime} AND deleted_flag = 0 GROUP BY approval_action")
    List<java.util.Map<String, Object>> statisticsByApprovalAction(@Param("startTime") LocalDateTime startTime,
                                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询平均审批时长（从申请创建到最终审批）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 平均审批时长（小时）
     */
    @Select("SELECT AVG(TIMESTAMPDIFF(HOUR, s.create_time, a.approval_time)) as avg_hours FROM t_attendance_overtime_approval a INNER JOIN t_attendance_overtime_apply s ON a.apply_id = s.apply_id WHERE a.approval_action IN ('APPROVE', 'REJECT') AND a.approval_time BETWEEN #{startTime} AND #{endTime} AND a.deleted_flag = 0")
    Double calculateAverageApprovalHours(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
}
