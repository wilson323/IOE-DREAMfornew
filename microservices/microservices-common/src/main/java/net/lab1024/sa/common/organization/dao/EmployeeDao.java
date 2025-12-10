package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.EmployeeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工数据访问层
 * <p>
 * 员工信息的数据库访问接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface EmployeeDao extends BaseMapper<EmployeeEntity> {

    /**
     * 根据用户ID查询员工
     *
     * @param userId 用户ID
     * @return 员工信息
     */
    @Select("SELECT * FROM t_employee WHERE user_id = #{userId} AND deleted_flag = 0")
    EmployeeEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 根据部门ID查询员工列表
     *
     * @param departmentId 部门ID
     * @return 员工列表
     */
    @Select("SELECT * FROM t_employee WHERE department_id = #{departmentId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<EmployeeEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据状态查询员工列表
     *
     * @param status 状态
     * @return 员工列表
     */
    @Select("SELECT * FROM t_employee WHERE status = #{status} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<EmployeeEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 检查员工ID是否存在
     *
     * @param employeeId 员工ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM t_employee WHERE employee_id = #{employeeId} AND deleted_flag = 0")
    boolean existsById(@Param("employeeId") Long employeeId);
}