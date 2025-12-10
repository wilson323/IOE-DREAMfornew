package net.lab1024.sa.common.system.employee.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.system.employee.domain.entity.EmployeeEntity;

/**
 * 员工DAO（System模块专用）
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Mapper注解（禁止@Repository）
 * - 使用Dao后缀（禁止Repository后缀）
 * - 继承BaseMapper提供基础CRUD
 * - 使用MyBatis-Plus（禁止JPA）
 * </p>
 * <p>
 * 注意：指定Bean名称为"systemEmployeeDao"以避免与hr.dao.EmployeeDao冲突
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Mapper
public interface EmployeeDao extends BaseMapper<EmployeeEntity> {

    /**
     * 根据用户ID查询员工
     *
     * @param userId 用户ID
     * @return 员工信息
     */
    EmployeeEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 根据员工工号查询员工
     *
     * @param employeeNo 员工工号
     * @return 员工信息
     */
    EmployeeEntity selectByEmployeeNo(@Param("employeeNo") String employeeNo);

    /**
     * 根据手机号查询员工
     *
     * @param phone 手机号
     * @return 员工信息
     */
    EmployeeEntity selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询员工
     *
     * @param email 邮箱
     * @return 员工信息
     */
    EmployeeEntity selectByEmail(@Param("email") String email);

    /**
     * 根据身份证号查询员工
     *
     * @param idCardNo 身份证号
     * @return 员工信息
     */
    EmployeeEntity selectByIdCardNo(@Param("idCardNo") String idCardNo);

    /**
     * 根据部门ID查询员工列表
     *
     * @param departmentId 部门ID
     * @return 员工列表
     */
    List<EmployeeEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据上级ID查询下属员工列表
     *
     * @param supervisorId 上级ID
     * @return 员工列表
     */
    List<EmployeeEntity> selectBySupervisorId(@Param("supervisorId") Long supervisorId);

    /**
     * 根据员工状态查询员工列表
     *
     * @param status 员工状态
     * @return 员工列表
     */
    List<EmployeeEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据员工类型查询员工列表
     *
     * @param employeeType 员工类型
     * @return 员工列表
     */
    List<EmployeeEntity> selectByEmployeeType(@Param("employeeType") Integer employeeType);

    /**
     * 统计部门员工数量
     *
     * @param departmentId 部门ID
     * @return 员工数量
     */
    int countByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 统计员工数量按状态
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countByStatus();

    /**
     * 统计员工数量按类型
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countByEmployeeType();

    /**
     * 统计员工数量按部门
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countByDepartment();

    /**
     * 查询即将转正的员工
     *
     * @param days 天数
     * @return 员工列表
     */
    List<EmployeeEntity> selectUpcomingRegularEmployees(@Param("days") Integer days);

    /**
     * 查询合同即将到期的员工
     *
     * @param days 天数
     * @return 员工列表
     */
    List<EmployeeEntity> selectUpcomingContractExpireEmployees(@Param("days") Integer days);

    /**
     * 批量更新员工状态
     *
     * @param employeeIds 员工ID列表
     * @param status      状态
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("employeeIds") List<Long> employeeIds, @Param("status") Integer status);
}
