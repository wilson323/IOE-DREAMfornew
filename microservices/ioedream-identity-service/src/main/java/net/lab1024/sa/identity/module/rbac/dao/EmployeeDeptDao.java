package net.lab1024.sa.identity.module.rbac.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工部门权限DAO
 * <p>
 * 用于查询员工所属部门信息，支持权限管理
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Mapper
public interface EmployeeDeptDao {

    /**
     * 根据员工ID查询部门ID列表
     * <p>
     * 查询员工所属的部门ID，支持查询员工及其上级部门
     * </p>
     *
     * @param employeeId 员工ID（对应t_hr_employee表的employee_id）
     * @return 部门ID列表，如果员工不存在或未分配部门，返回空列表
     */
    List<Long> getDepartmentIdsByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 根据员工ID查询部门ID（仅直接部门）
     * <p>
     * 仅查询员工直接所属的部门，不包含上级部门
     * </p>
     *
     * @param employeeId 员工ID
     * @return 部门ID，如果员工不存在或未分配部门，返回null
     */
    Long getDirectDepartmentIdByEmployeeId(@Param("employeeId") Long employeeId);
}