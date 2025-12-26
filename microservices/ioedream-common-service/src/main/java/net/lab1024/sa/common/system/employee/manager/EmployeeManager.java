package net.lab1024.sa.common.system.employee.manager;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import net.lab1024.sa.common.system.employee.dao.EmployeeDao;
import net.lab1024.sa.common.system.employee.domain.entity.EmployeeEntity;

/**
 * 员工管理器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Slf4j
public class EmployeeManager {


    private final EmployeeDao employeeDao;

    public EmployeeManager(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    public EmployeeEntity getEmployeeByPhone(String phone) {
        return employeeDao.selectByPhone(phone);
    }

    public EmployeeEntity getEmployeeByEmail(String email) {
        return employeeDao.selectByEmail(email);
    }

    public EmployeeEntity getEmployeeByEmployeeNo(String employeeNo) {
        return employeeDao.selectByEmployeeNo(employeeNo);
    }

    public EmployeeEntity getEmployeeByUserId(Long userId) {
        return employeeDao.selectByUserId(userId);
    }

    public List<EmployeeEntity> getEmployeesByDepartmentId(Long departmentId) {
        return employeeDao.selectByDepartmentId(departmentId);
    }

    public List<EmployeeEntity> getEmployeesBySupervisorId(Long supervisorId) {
        return employeeDao.selectBySupervisorId(supervisorId);
    }

    public List<EmployeeEntity> getUpcomingRegularEmployees(Integer days) {
        return employeeDao.selectUpcomingRegularEmployees(days);
    }

    public List<EmployeeEntity> getUpcomingContractExpireEmployees(Integer days) {
        return employeeDao.selectUpcomingContractExpireEmployees(days);
    }

    public int countByDepartmentId(Long departmentId) {
        return employeeDao.countByDepartmentId(departmentId);
    }

    public Map<String, Object> getEmployeeStatistics() {
        return Collections.emptyMap();
    }

    public boolean isEmployeeNoUnique(String employeeNo, Long excludeId) {
        EmployeeEntity existing = employeeDao.selectByEmployeeNo(employeeNo);
        return isUnique(existing, excludeId);
    }

    public boolean isPhoneUnique(String phone, Long excludeId) {
        EmployeeEntity existing = employeeDao.selectByPhone(phone);
        return isUnique(existing, excludeId);
    }

    public boolean isEmailUnique(String email, Long excludeId) {
        EmployeeEntity existing = employeeDao.selectByEmail(email);
        return isUnique(existing, excludeId);
    }

    public boolean isIdCardNoUnique(String idCardNo, Long excludeId) {
        EmployeeEntity existing = employeeDao.selectByIdCardNo(idCardNo);
        return isUnique(existing, excludeId);
    }

    public int calculateWorkYears(LocalDate hireDate) {
        if (hireDate == null) {
            return 0;
        }
        return Period.between(hireDate, LocalDate.now()).getYears();
    }

    public boolean canResign(Long employeeId) {
        return true;
    }

    public int batchUpdateStatus(List<Long> employeeIds, Integer status) {
        return employeeDao.batchUpdateStatus(employeeIds, status);
    }

    public List<Map<String, Object>> getEmployeeTree(Long departmentId) {
        return Collections.emptyList();
    }

    private boolean isUnique(EmployeeEntity existing, Long excludeId) {
        if (existing == null) {
            return true;
        }
        if (excludeId == null) {
            return false;
        }
        return excludeId.equals(existing.getId());
    }
}
