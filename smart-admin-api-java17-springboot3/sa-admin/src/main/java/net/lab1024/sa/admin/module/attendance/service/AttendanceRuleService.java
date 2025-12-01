package net.lab1024.sa.admin.module.attendance.service;

import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;

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
}