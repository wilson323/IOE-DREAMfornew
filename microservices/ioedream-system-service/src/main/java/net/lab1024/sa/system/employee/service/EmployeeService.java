package net.lab1024.sa.system.employee.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.system.employee.domain.entity.EmployeeEntity;
import net.lab1024.sa.system.employee.domain.form.EmployeeAddForm;
import net.lab1024.sa.system.employee.domain.form.EmployeeQueryForm;
import net.lab1024.sa.system.employee.domain.form.EmployeeUpdateForm;
import net.lab1024.sa.system.employee.domain.vo.EmployeeVO;

/**
 * 员工管理Service
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
public interface EmployeeService extends IService<EmployeeEntity> {

    /**
     * 分页查询员工
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<EmployeeVO> queryEmployeePage(EmployeeQueryForm queryForm);

    /**
     * 根据ID获取员工详情
     *
     * @param employeeId 员工ID
     * @return 员工详情
     */
    EmployeeVO getEmployeeDetail(Long employeeId);

    /**
     * 新增员工
     *
     * @param addForm 新增表单
     * @return 新增结果
     */
    boolean addEmployee(EmployeeAddForm addForm);

    /**
     * 更新员工
     *
     * @param updateForm 更新表单
     * @return 更新结果
     */
    boolean updateEmployee(EmployeeUpdateForm updateForm);

    /**
     * 删除员工
     *
     * @param employeeId 员工ID
     * @return 删除结果
     */
    boolean deleteEmployee(Long employeeId);

    /**
     * 批量删除员工
     *
     * @param employeeIds 员工ID列表
     * @return 删除结果
     */
    boolean batchDeleteEmployees(List<Long> employeeIds);

    /**
     * 根据部门ID获取员工列表
     *
     * @param departmentId 部门ID
     * @return 员工列表
     */
    List<EmployeeVO> getEmployeesByDepartmentId(Long departmentId);

    /**
     * 更新员工状态
     *
     * @param employeeId 员工ID
     * @param status     状态
     * @return 更新结果
     */
    boolean updateEmployeeStatus(Long employeeId, Integer status);
}
