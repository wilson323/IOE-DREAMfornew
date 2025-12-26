package net.lab1024.sa.common.system.employee.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.system.employee.domain.entity.EmployeeEntity;

@Mapper
public interface EmployeeDao extends BaseMapper<EmployeeEntity> {

    EmployeeEntity selectByUserId(@Param("userId") Long userId);

    EmployeeEntity selectByEmployeeNo(@Param("employeeNo") String employeeNo);

    EmployeeEntity selectByPhone(@Param("phone") String phone);

    EmployeeEntity selectByEmail(@Param("email") String email);

    EmployeeEntity selectByIdCardNo(@Param("idCardNo") String idCardNo);

    List<EmployeeEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    List<EmployeeEntity> selectBySupervisorId(@Param("supervisorId") Long supervisorId);

    List<EmployeeEntity> selectByStatus(@Param("status") Integer status);

    List<EmployeeEntity> selectByEmployeeType(@Param("employeeType") Integer employeeType);

    int countByDepartmentId(@Param("departmentId") Long departmentId);

    List<Map<String, Object>> countByStatus();

    List<Map<String, Object>> countByEmployeeType();

    List<Map<String, Object>> countByDepartment();

    List<EmployeeEntity> selectUpcomingRegularEmployees(@Param("days") Integer days);

    List<EmployeeEntity> selectUpcomingContractExpireEmployees(@Param("days") Integer days);

    int batchUpdateStatus(@Param("employeeIds") List<Long> employeeIds, @Param("status") Integer status);
}
