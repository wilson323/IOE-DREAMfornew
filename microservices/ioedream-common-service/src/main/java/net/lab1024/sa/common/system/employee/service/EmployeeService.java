package net.lab1024.sa.common.system.employee.service;

import java.util.List;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.system.employee.domain.dto.EmployeeAddDTO;
import net.lab1024.sa.common.system.employee.domain.dto.EmployeeQueryDTO;
import net.lab1024.sa.common.system.employee.domain.dto.EmployeeUpdateDTO;
import net.lab1024.sa.common.system.employee.domain.vo.EmployeeVO;

/**
 * 员工服务接口
 * <p>
 * 提供员工信息管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 */
public interface EmployeeService {

    /**
     * 分页查询员工
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<EmployeeVO> queryEmployeePage(EmployeeQueryDTO queryDTO);

    /**
     * 获取员工详情
     *
     * @param employeeId 员工ID
     * @return 员工信息
     */
    EmployeeVO getEmployeeDetail(Long employeeId);

    /**
     * 新增员工
     *
     * @param addDTO 员工新增DTO
     * @return 是否成功
     */
    boolean addEmployee(EmployeeAddDTO addDTO);

    /**
     * 更新员工
     *
     * @param updateDTO 员工更新DTO
     * @return 是否成功
     */
    boolean updateEmployee(EmployeeUpdateDTO updateDTO);

    /**
     * 删除员工
     *
     * @param employeeId 员工ID
     * @return 是否成功
     */
    boolean deleteEmployee(Long employeeId);

    /**
     * 批量删除员工
     *
     * @param employeeIds 员工ID列表
     * @return 是否成功
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
     * @return 是否成功
     */
    boolean updateEmployeeStatus(Long employeeId, Integer status);
}

