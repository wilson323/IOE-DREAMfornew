package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.entity.AttendanceRuleEntity;

import java.util.List;

/**
 * 考勤规则服务接口
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
public interface AttendanceRuleService {

    /**
     * 根据员工ID获取考勤规则
     *
     * @param employeeId 员工ID
     * @return 考勤规则
     */
    AttendanceRuleEntity getRuleByEmployeeId(Long employeeId);

    /**
     * 根据部门ID获取考勤规则
     *
     * @param departmentId 部门ID
     * @return 考勤规则列表
     */
    List<AttendanceRuleEntity> getRulesByDepartmentId(Long departmentId);

    /**
     * 保存或更新考勤规则
     *
     * @param rule 考勤规则
     * @return 是否成功
     */
    boolean saveOrUpdateRule(AttendanceRuleEntity rule);

    /**
     * 删除考勤规则
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    boolean deleteRule(Long ruleId);

    /**
     * 批量删除考勤规则
     *
     * @param ruleIds 规则ID列表
     * @return 删除数量
     */
    int batchDeleteRules(List<Long> ruleIds);

    /**
     * 获取所有有效的考勤规则
     *
     * @return 考勤规则列表
     */
    List<AttendanceRuleEntity> getAllValidRules();

    /**
     * 根据规则ID获取考勤规则
     *
     * @param ruleId 规则ID
     * @return 考勤规则
     */
    AttendanceRuleEntity getRuleById(Long ruleId);

    /**
     * 启用/禁用考勤规则
     *
     * @param ruleId  规则ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean updateRuleStatus(Long ruleId, Boolean enabled);

    /**
     * 获取员工适用的有效考勤规则（考虑优先级）
     *
     * @param employeeId   员工ID
     * @param departmentId  部门ID
     * @param employeeType  员工类型
     * @return 最适用的考勤规则
     */
    AttendanceRuleEntity getApplicableRule(Long employeeId, Long departmentId, String employeeType);

    /**
     * 验证考勤规则冲突
     *
     * @param rule 考勤规则实体
     * @return 是否有冲突
     */
    boolean validateRuleConflict(AttendanceRuleEntity rule);
}