package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.entity.AttendanceOvertimeRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 加班规则配置DAO接口
 * <p>
 * 提供加班规则的数据访问操作
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AttendanceOvertimeRuleDao extends BaseMapper<AttendanceOvertimeRuleEntity> {

    /**
     * 根据规则编码查询
     *
     * @param ruleCode 规则编码
     * @return 规则实体
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE rule_code = #{ruleCode} AND deleted_flag = 0")
    AttendanceOvertimeRuleEntity selectByRuleCode(@Param("ruleCode") String ruleCode);

    /**
     * 根据规则分类查询列表
     *
     * @param ruleCategory 规则分类（CALCULATION-计算规则 COMPENSATION-补偿规则 LIMITATION-限制规则）
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE rule_category = #{ruleCategory} AND deleted_flag = 0 ORDER BY execution_order ASC, sort_order ASC")
    List<AttendanceOvertimeRuleEntity> selectByRuleCategory(@Param("ruleCategory") String ruleCategory);

    /**
     * 根据规则类型查询列表
     *
     * @param ruleType 规则类型
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE rule_type = #{ruleType} AND deleted_flag = 0 ORDER BY execution_order ASC, sort_order ASC")
    List<AttendanceOvertimeRuleEntity> selectByRuleType(@Param("ruleType") String ruleType);

    /**
     * 根据应用范围查询列表
     *
     * @param applyScope 应用范围（GLOBAL-全局 DEPARTMENT-部门 SHIFT-班次 EMPLOYEE-员工）
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE apply_scope = #{applyScope} AND deleted_flag = 0 ORDER BY rule_priority ASC, sort_order ASC")
    List<AttendanceOvertimeRuleEntity> selectByApplyScope(@Param("applyScope") String applyScope);

    /**
     * 查询启用的规则列表
     *
     * @return 启用规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE rule_status = 1 AND deleted_flag = 0 ORDER BY rule_priority ASC, execution_order ASC")
    List<AttendanceOvertimeRuleEntity> selectEnabledRules();

    /**
     * 查询指定优先级范围的规则
     *
     * @param minPriority 最小优先级
     * @param maxPriority 最大优先级
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE rule_priority BETWEEN #{minPriority} AND #{maxPriority} AND deleted_flag = 0 ORDER BY rule_priority ASC")
    List<AttendanceOvertimeRuleEntity> selectByPriorityRange(@Param("minPriority") Integer minPriority,
                                                             @Param("maxPriority") Integer maxPriority);

    /**
     * 查询全局生效的规则（按时间范围过滤）
     *
     * @param currentTime 当前时间
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE apply_scope = 'GLOBAL' AND rule_status = 1 AND (effective_start_time IS NULL OR effective_start_time <= #{currentTime}) AND (effective_end_time IS NULL OR effective_end_time >= #{currentTime}) AND deleted_flag = 0 ORDER BY rule_priority ASC")
    List<AttendanceOvertimeRuleEntity> selectGlobalEffectiveRules(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询指定部门适用的规则
     *
     * @param departmentId 部门ID
     * @param currentTime  当前时间
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE (apply_scope IN ('GLOBAL', 'DEPARTMENT') AND rule_status = 1 AND (department_ids IS NULL OR department_ids LIKE CONCAT('%', #{departmentId}, '%'))) AND (effective_start_time IS NULL OR effective_start_time <= #{currentTime}) AND (effective_end_time IS NULL OR effective_end_time >= #{currentTime}) AND deleted_flag = 0 ORDER BY rule_priority ASC")
    List<AttendanceOvertimeRuleEntity> selectByDepartmentId(@Param("departmentId") Long departmentId,
                                                            @Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询指定班次适用的规则
     *
     * @param shiftId     班次ID
     * @param currentTime 当前时间
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE (apply_scope IN ('GLOBAL', 'SHIFT') AND rule_status = 1 AND (shift_ids IS NULL OR shift_ids LIKE CONCAT('%', #{shiftId}, '%'))) AND (effective_start_time IS NULL OR effective_start_time <= #{currentTime}) AND (effective_end_time IS NULL OR effective_end_time >= #{currentTime}) AND deleted_flag = 0 ORDER BY rule_priority ASC")
    List<AttendanceOvertimeRuleEntity> selectByShiftId(@Param("shiftId") Long shiftId,
                                                        @Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询指定员工适用的规则
     *
     * @param userId      用户ID
     * @param currentTime 当前时间
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE (apply_scope IN ('GLOBAL', 'EMPLOYEE') AND rule_status = 1 AND (user_ids IS NULL OR user_ids LIKE CONCAT('%', #{userId}, '%'))) AND (effective_start_time IS NULL OR effective_start_time <= #{currentTime}) AND (effective_end_time IS NULL OR effective_end_time >= #{currentTime}) AND deleted_flag = 0 ORDER BY rule_priority ASC")
    List<AttendanceOvertimeRuleEntity> selectByUserId(@Param("userId") Long userId,
                                                       @Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询指定工作流的规则
     *
     * @param workflowId 工作流ID
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE approval_workflow_id = #{workflowId} AND deleted_flag = 0")
    List<AttendanceOvertimeRuleEntity> selectByWorkflowId(@Param("workflowId") Long workflowId);

    /**
     * 查询需要审批的规则
     *
     * @return 需要审批的规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE approval_required = 1 AND deleted_flag = 0 ORDER BY rule_priority ASC")
    List<AttendanceOvertimeRuleEntity> selectApprovalRequiredRules();

    /**
     * 查询自动批准规则（时长小于阈值）
     *
     * @param overtimeHours 加班时长（小时）
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE approval_required = 1 AND auto_approve_hours IS NOT NULL AND auto_approve_hours > 0 AND #{overtimeHours} < auto_approve_hours AND deleted_flag = 0")
    List<AttendanceOvertimeRuleEntity> selectAutoApproveRules(@Param("overtimeHours") java.math.BigDecimal overtimeHours);

    /**
     * 查询补偿类规则
     *
     * @return 补偿类规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE rule_category = 'COMPENSATION' AND rule_status = 1 AND deleted_flag = 0 ORDER BY rule_priority ASC")
    List<AttendanceOvertimeRuleEntity> selectCompensationRules();

    /**
     * 查询限制类规则
     *
     * @return 限制类规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE rule_category = 'LIMITATION' AND rule_status = 1 AND deleted_flag = 0 ORDER BY rule_priority ASC")
    List<AttendanceOvertimeRuleEntity> selectLimitationRules();

    /**
     * 查询计算类规则
     *
     * @return 计算类规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE rule_category = 'CALCULATION' AND rule_status = 1 AND deleted_flag = 0 ORDER BY rule_priority ASC")
    List<AttendanceOvertimeRuleEntity> selectCalculationRules();

    /**
     * 根据执行顺序查询规则
     *
     * @param executionOrder 执行顺序
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE execution_order = #{executionOrder} AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<AttendanceOvertimeRuleEntity> selectByExecutionOrder(@Param("executionOrder") Integer executionOrder);

    /**
     * 查询指定优先级的规则
     *
     * @param rulePriority 规则优先级
     * @return 规则列表
     */
    @Select("SELECT * FROM t_attendance_overtime_rule WHERE rule_priority = #{rulePriority} AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<AttendanceOvertimeRuleEntity> selectByRulePriority(@Param("rulePriority") Integer rulePriority);

    /**
     * 统计启用的规则数量
     *
     * @return 启用规则数量
     */
    @Select("SELECT COUNT(*) FROM t_attendance_overtime_rule WHERE rule_status = 1 AND deleted_flag = 0")
    Integer countEnabledRules();

    /**
     * 统计指定分类的规则数量
     *
     * @param ruleCategory 规则分类
     * @return 规则数量
     */
    @Select("SELECT COUNT(*) FROM t_attendance_overtime_rule WHERE rule_category = #{ruleCategory} AND deleted_flag = 0")
    Integer countByRuleCategory(@Param("ruleCategory") String ruleCategory);

    /**
     * 查询规则优先级最大值
     *
     * @return 最大优先级
     */
    @Select("SELECT MAX(rule_priority) FROM t_attendance_overtime_rule WHERE deleted_flag = 0")
    Integer selectMaxPriority();

    /**
     * 查询规则优先级最小值
     *
     * @return 最小优先级
     */
    @Select("SELECT MIN(rule_priority) FROM t_attendance_overtime_rule WHERE deleted_flag = 0")
    Integer selectMinPriority();
}
