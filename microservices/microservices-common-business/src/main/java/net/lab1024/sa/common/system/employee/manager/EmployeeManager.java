package net.lab1024.sa.common.system.employee.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.system.employee.domain.entity.EmployeeEntity;
import net.lab1024.sa.common.system.employee.dao.EmployeeDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 员工管理Manager
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖（DAO等）
 * - 在微服务中通过配置类注册为Spring Bean
 * - 完整的异常处理和日志记录
 * - 复杂业务流程编排
 * - 多DAO数据组装和计算
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class EmployeeManager {

    private final EmployeeDao employeeDao;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param employeeDao 员工DAO
     */
    public EmployeeManager(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    /**
     * 根据手机号查询员工
     * <p>
     * 根据手机号精确查询员工信息，通常用于登录、找回密码等场景。
     * 如果存在多个相同手机号的员工（异常情况），返回第一个启用的员工。
     * </p>
     *
     * @param phone 手机号，不能为空或空白
     * @return 员工信息实体，如果不存在则返回null
     *
     * @example
     * <pre>
     * EmployeeEntity employee = employeeManager.getEmployeeByPhone("13800138000");
     * if (employee != null) {
     *     log.info("找到员工: employeeName={}", employee.getEmployeeName());
     * }
     * </pre>
     */
    public EmployeeEntity getEmployeeByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            log.warn("根据手机号查询员工失败：手机号为空");
            return null;
        }

        try {
            EmployeeEntity employee = employeeDao.selectByPhone(phone.trim());

            if (employee != null) {
                log.debug("根据手机号查询员工成功: phone={}, employeeId={}", phone, employee.getId());
            } else {
                log.debug("根据手机号未找到员工: phone={}", phone);
            }

            return employee;
        } catch (Exception e) {
            log.error("根据手机号查询员工失败: phone={}", phone, e);
            return null;
        }
    }

    /**
     * 根据邮箱查询员工
     * <p>
     * 根据邮箱地址精确查询员工信息，通常用于登录、邮件通知等场景。
     * 如果存在多个相同邮箱的员工（异常情况），返回第一个启用的员工。
     * </p>
     *
     * @param email 邮箱地址，不能为空或空白
     * @return 员工信息实体，如果不存在则返回null
     *
     * @example
     * <pre>
     * EmployeeEntity employee = employeeManager.getEmployeeByEmail("zhangsan@example.com");
     * if (employee != null) {
     *     log.info("找到员工: employeeName={}", employee.getEmployeeName());
     * }
     * </pre>
     */
    public EmployeeEntity getEmployeeByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.warn("根据邮箱查询员工失败：邮箱为空");
            return null;
        }

        try {
            EmployeeEntity employee = employeeDao.selectByEmail(email.trim().toLowerCase());

            if (employee != null) {
                log.debug("根据邮箱查询员工成功: email={}, employeeId={}", email, employee.getId());
            } else {
                log.debug("根据邮箱未找到员工: email={}", email);
            }

            return employee;
        } catch (Exception e) {
            log.error("根据邮箱查询员工失败: email={}", email, e);
            return null;
        }
    }

    /**
     * 根据员工工号查询员工
     *
     * @param employeeNo 员工工号
     * @return 员工信息
     */
    public EmployeeEntity getEmployeeByEmployeeNo(String employeeNo) {
        if (employeeNo == null || employeeNo.trim().isEmpty()) {
            log.warn("根据工号查询员工失败：工号为空");
            return null;
        }

        try {
            EmployeeEntity employee = employeeDao.selectByEmployeeNo(employeeNo.trim());

            if (employee != null) {
                log.debug("根据工号查询员工成功: employeeNo={}, employeeId={}", employeeNo, employee.getId());
            } else {
                log.debug("根据工号未找到员工: employeeNo={}", employeeNo);
            }

            return employee;
        } catch (Exception e) {
            log.error("根据工号查询员工失败: employeeNo={}", employeeNo, e);
            return null;
        }
    }

    /**
     * 根据用户ID查询员工
     *
     * @param userId 用户ID
     * @return 员工信息
     */
    public EmployeeEntity getEmployeeByUserId(Long userId) {
        if (userId == null) {
            log.warn("根据用户ID查询员工失败：用户ID为空");
            return null;
        }

        try {
            EmployeeEntity employee = employeeDao.selectByUserId(userId);

            if (employee != null) {
                log.debug("根据用户ID查询员工成功: userId={}, employeeId={}", userId, employee.getId());
            } else {
                log.debug("根据用户ID未找到员工: userId={}", userId);
            }

            return employee;
        } catch (Exception e) {
            log.error("根据用户ID查询员工失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 根据部门ID查询员工列表
     *
     * @param departmentId 部门ID
     * @return 员工列表
     */
    public List<EmployeeEntity> getEmployeesByDepartmentId(Long departmentId) {
        if (departmentId == null) {
            log.warn("根据部门ID查询员工失败：部门ID为空");
            return List.of();
        }

        try {
            List<EmployeeEntity> employees = employeeDao.selectByDepartmentId(departmentId);

            log.debug("根据部门ID查询员工成功: departmentId={}, count={}", departmentId, employees.size());

            return employees;
        } catch (Exception e) {
            log.error("根据部门ID查询员工失败: departmentId={}", departmentId, e);
            return List.of();
        }
    }

    /**
     * 根据上级ID查询下属员工列表
     *
     * @param supervisorId 上级ID
     * @return 员工列表
     */
    public List<EmployeeEntity> getEmployeesBySupervisorId(Long supervisorId) {
        if (supervisorId == null) {
            log.warn("根据上级ID查询下属失败：上级ID为空");
            return List.of();
        }

        try {
            List<EmployeeEntity> employees = employeeDao.selectBySupervisorId(supervisorId);

            log.debug("根据上级ID查询下属成功: supervisorId={}, count={}", supervisorId, employees.size());

            return employees;
        } catch (Exception e) {
            log.error("根据上级ID查询下属失败: supervisorId={}", supervisorId, e);
            return List.of();
        }
    }

    /**
     * 查询即将转正的员工
     *
     * @param days 提前天数
     * @return 员工列表
     */
    public List<EmployeeEntity> getUpcomingRegularEmployees(Integer days) {
        try {
            List<EmployeeEntity> employees = employeeDao.selectUpcomingRegularEmployees(days != null ? days : 7);

            log.debug("查询即将转正的员工成功: days={}, count={}", days, employees.size());

            return employees;
        } catch (Exception e) {
            log.error("查询即将转正的员工失败: days={}", days, e);
            return List.of();
        }
    }

    /**
     * 查询合同即将到期的员工
     *
     * @param days 提前天数
     * @return 员工列表
     */
    public List<EmployeeEntity> getUpcomingContractExpireEmployees(Integer days) {
        try {
            List<EmployeeEntity> employees = employeeDao.selectUpcomingContractExpireEmployees(days != null ? days : 30);

            log.debug("查询合同即将到期的员工成功: days={}, count={}", days, employees.size());

            return employees;
        } catch (Exception e) {
            log.error("查询合同即将到期的员工失败: days={}", days, e);
            return List.of();
        }
    }

    /**
     * 统计部门员工数量
     *
     * @param departmentId 部门ID
     * @return 员工数量
     */
    public int countByDepartmentId(Long departmentId) {
        if (departmentId == null) {
            return 0;
        }

        try {
            int count = employeeDao.countByDepartmentId(departmentId);

            log.debug("统计部门员工数量成功: departmentId={}, count={}", departmentId, count);

            return count;
        } catch (Exception e) {
            log.error("统计部门员工数量失败: departmentId={}", departmentId, e);
            return 0;
        }
    }

    /**
     * 获取员工统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getEmployeeStatistics() {
        try {
            Map<String, Object> statistics = new java.util.HashMap<>();

            // 按状态统计
            List<Map<String, Object>> statusStats = employeeDao.countByStatus();
            statistics.put("byStatus", statusStats);

            // 按类型统计
            List<Map<String, Object>> typeStats = employeeDao.countByEmployeeType();
            statistics.put("byType", typeStats);

            // 按部门统计
            List<Map<String, Object>> departmentStats = employeeDao.countByDepartment();
            statistics.put("byDepartment", departmentStats);

            log.debug("获取员工统计信息成功");

            return statistics;
        } catch (Exception e) {
            log.error("获取员工统计信息失败", e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 验证员工工号唯一性
     *
     * @param employeeNo 员工工号
     * @param excludeEmployeeId 排除的员工ID（用于更新时）
     * @return 是否唯一
     */
    public boolean isEmployeeNoUnique(String employeeNo, Long excludeEmployeeId) {
        if (employeeNo == null || employeeNo.trim().isEmpty()) {
            return false;
        }

        try {
            EmployeeEntity employee = employeeDao.selectByEmployeeNo(employeeNo.trim());

            if (employee == null) {
                return true;
            }

            // 如果是更新操作，排除自己
            if (excludeEmployeeId != null && excludeEmployeeId.equals(employee.getId())) {
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("验证员工工号唯一性失败: employeeNo={}", employeeNo, e);
            return false;
        }
    }

    /**
     * 验证手机号唯一性
     *
     * @param phone 手机号
     * @param excludeEmployeeId 排除的员工ID
     * @return 是否唯一
     */
    public boolean isPhoneUnique(String phone, Long excludeEmployeeId) {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }

        try {
            EmployeeEntity employee = employeeDao.selectByPhone(phone.trim());

            if (employee == null) {
                return true;
            }

            if (excludeEmployeeId != null && excludeEmployeeId.equals(employee.getId())) {
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("验证手机号唯一性失败: phone={}", phone, e);
            return false;
        }
    }

    /**
     * 验证邮箱唯一性
     *
     * @param email 邮箱
     * @param excludeEmployeeId 排除的员工ID
     * @return 是否唯一
     */
    public boolean isEmailUnique(String email, Long excludeEmployeeId) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        try {
            EmployeeEntity employee = employeeDao.selectByEmail(email.trim().toLowerCase());

            if (employee == null) {
                return true;
            }

            if (excludeEmployeeId != null && excludeEmployeeId.equals(employee.getId())) {
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("验证邮箱唯一性失败: email={}", email, e);
            return false;
        }
    }

    /**
     * 验证身份证号唯一性
     *
     * @param idCardNo 身份证号
     * @param excludeEmployeeId 排除的员工ID
     * @return 是否唯一
     */
    public boolean isIdCardNoUnique(String idCardNo, Long excludeEmployeeId) {
        if (idCardNo == null || idCardNo.trim().isEmpty()) {
            return true;
        }

        try {
            EmployeeEntity employee = employeeDao.selectByIdCardNo(idCardNo.trim());

            if (employee == null) {
                return true;
            }

            if (excludeEmployeeId != null && excludeEmployeeId.equals(employee.getId())) {
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("验证身份证号唯一性失败: idCardNo={}", idCardNo, e);
            return false;
        }
    }

    /**
     * 计算员工工龄（年）
     *
     * @param hireDate 入职日期
     * @return 工龄（年）
     */
    public int calculateWorkYears(LocalDate hireDate) {
        if (hireDate == null) {
            return 0;
        }

        try {
            LocalDate now = LocalDate.now();
            return now.getYear() - hireDate.getYear();
        } catch (Exception e) {
            log.error("计算工龄失败: hireDate={}", hireDate, e);
            return 0;
        }
    }

    /**
     * 检查员工是否可以离职
     *
     * @param employeeId 员工ID
     * @return 是否可以离职
     */
    public boolean canResign(Long employeeId) {
        if (employeeId == null) {
            return false;
        }

        try {
            EmployeeEntity employee = employeeDao.selectById(employeeId);

            if (employee == null) {
                log.warn("员工不存在: employeeId={}", employeeId);
                return false;
            }

            // 检查员工状态
            if (employee.getStatus() != 1) {
                log.warn("员工状态不是在职，无法离职: employeeId={}, status={}", employeeId, employee.getStatus());
                return false;
            }

            // 检查是否有未完成的任务、未结算的工资等
            // 注意：这里简化处理，实际应该查询相关业务表
            // 1. 检查是否有未完成的审批工作流
            // long pendingWorkflowCount = approvalWorkflowDao.countPendingByApplicant(employeeId);
            // if (pendingWorkflowCount > 0) {
            //     log.warn("员工有未完成的审批工作流，无法离职: employeeId={}, count={}", employeeId, pendingWorkflowCount);
            //     return false;
            // }

            // 2. 检查是否有未结算的工资（简化处理，实际应该查询工资表）
            // long unpaidSalaryCount = salaryDao.countUnpaidByEmployeeId(employeeId);
            // if (unpaidSalaryCount > 0) {
            //     log.warn("员工有未结算的工资，无法离职: employeeId={}, count={}", employeeId, unpaidSalaryCount);
            //     return false;
            // }

            // 3. 检查是否有未完成的考勤记录（简化处理，实际应该查询考勤表）
            // long incompleteAttendanceCount = attendanceDao.countIncompleteByEmployeeId(employeeId);
            // if (incompleteAttendanceCount > 0) {
            //     log.warn("员工有未完成的考勤记录，无法离职: employeeId={}, count={}", employeeId, incompleteAttendanceCount);
            //     return false;
            // }

            // 4. 检查是否有未完成的消费记录（简化处理，实际应该查询消费表）
            // long unpaidConsumeCount = consumeDao.countUnpaidByEmployeeId(employeeId);
            // if (unpaidConsumeCount > 0) {
            //     log.warn("员工有未结算的消费记录，无法离职: employeeId={}, count={}", employeeId, unpaidConsumeCount);
            //     return false;
            // }

            log.debug("员工离职检查通过: employeeId={}", employeeId);
            return true;
        } catch (Exception e) {
            log.error("检查员工是否可以离职失败: employeeId={}", employeeId, e);
            return false;
        }
    }

    /**
     * 批量更新员工状态
     *
     * @param employeeIds 员工ID列表
     * @param status 状态
     * @return 更新数量
     */
    public int batchUpdateStatus(List<Long> employeeIds, Integer status) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            log.warn("批量更新员工状态失败：员工ID列表为空");
            return 0;
        }

        try {
            int count = employeeDao.batchUpdateStatus(employeeIds, status);

            log.info("批量更新员工状态成功: count={}, status={}", count, status);

            return count;
        } catch (Exception e) {
            log.error("批量更新员工状态失败: status={}", status, e);
            return 0;
        }
    }

    /**
     * 获取部门员工树（包含组织架构）
     *
     * @param departmentId 部门ID
     * @return 员工树
     */
    public List<Map<String, Object>> getEmployeeTree(Long departmentId) {
        try {
            List<EmployeeEntity> employees = employeeDao.selectByDepartmentId(departmentId);

            // 构建员工树（按上下级关系）
            List<Map<String, Object>> tree = new java.util.ArrayList<>();

            for (EmployeeEntity employee : employees) {
                if (employee.getSupervisorId() == null) {
                    // 顶级员工（部门负责人）
                    Map<String, Object> node = buildEmployeeTreeNode(employee, employees);
                    tree.add(node);
                }
            }

            log.debug("获取部门员工树成功: departmentId={}, nodeCount={}", departmentId, tree.size());

            return tree;
        } catch (Exception e) {
            log.error("获取部门员工树失败: departmentId={}", departmentId, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 构建员工树节点
     */
    private Map<String, Object> buildEmployeeTreeNode(EmployeeEntity employee, List<EmployeeEntity> allEmployees) {
        Map<String, Object> node = new java.util.HashMap<>();
        node.put("employeeId", employee.getId());
        node.put("employeeName", employee.getEmployeeName());
        node.put("employeeNo", employee.getEmployeeNo());
        node.put("position", employee.getPosition());
        node.put("departmentName", employee.getDepartmentName());

        // 查找下属
        List<Map<String, Object>> children = new java.util.ArrayList<>();
        for (EmployeeEntity subordinate : allEmployees) {
            if (employee.getId().equals(subordinate.getSupervisorId())) {
                Map<String, Object> childNode = buildEmployeeTreeNode(subordinate, allEmployees);
                children.add(childNode);
            }
        }

        if (!children.isEmpty()) {
            node.put("children", children);
        }

        return node;
    }
}

