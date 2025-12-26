package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.form.AttendanceRuleAddForm;
import net.lab1024.sa.attendance.domain.form.AttendanceRuleQueryForm;
import net.lab1024.sa.attendance.domain.form.AttendanceRuleUpdateForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceRuleVO;
import net.lab1024.sa.common.domain.PageResult;

import java.util.List;

/**
 * 考勤规则服务接口
 * <p>
 * 提供考勤规则管理相关业务功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
public interface AttendanceRuleService {

    /**
     * 查询员工考勤规则
     *
     * @param employeeId 员工ID
     * @return 规则列表
     */
    List<AttendanceRuleVO> getEmployeeRules(Long employeeId);

    /**
     * 查询部门考勤规则
     *
     * @param departmentId 部门ID
     * @return 规则列表
     */
    List<AttendanceRuleVO> getDepartmentRules(Long departmentId);

    /**
     * 查询规则详情
     *
     * @param ruleId 规则ID
     * @return 规则详情
     */
    AttendanceRuleVO getRuleDetail(Long ruleId);

    /**
     * 分页查询考勤规则
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<AttendanceRuleVO> queryRulePage(AttendanceRuleQueryForm queryForm);

    /**
     * 创建考勤规则
     *
     * @param addForm 新增表单
     * @return 规则ID
     */
    Long createRule(AttendanceRuleAddForm addForm);

    /**
     * 更新考勤规则
     *
     * @param ruleId 规则ID
     * @param updateForm 更新表单
     */
    void updateRule(Long ruleId, AttendanceRuleUpdateForm updateForm);

    /**
     * 删除考勤规则
     *
     * @param ruleId 规则ID
     */
    void deleteRule(Long ruleId);

    /**
     * 批量删除考勤规则
     *
     * @param ruleIds 规则ID列表
     */
    void batchDeleteRules(List<Long> ruleIds);
}
