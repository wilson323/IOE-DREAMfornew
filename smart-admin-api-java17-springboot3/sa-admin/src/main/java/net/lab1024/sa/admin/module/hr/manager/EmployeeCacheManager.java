package net.lab1024.sa.admin.module.hr.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.hr.dao.EmployeeDao;
import net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity;

/**
 * 员工缓存管理器（最小实现，支持编译通过）
 */
@Slf4j
@Component
public class EmployeeCacheManager {

    @Resource
    private EmployeeDao employeeDao;

    private final Map<Long, EmployeeEntity> employeeCache = new ConcurrentHashMap<>();
    private final Map<Long, EmployeeEntity> employeeDetailCache = new ConcurrentHashMap<>();

    public EmployeeEntity getEmployee(Long employeeId) {
        return employeeCache.computeIfAbsent(employeeId, id -> employeeDao.selectById(id));
    }

    public EmployeeEntity getEmployeeDetail(Long employeeId) {
        return employeeDetailCache.computeIfAbsent(employeeId, id -> employeeDao.selectById(id));
    }

    public void setEmployee(Long employeeId, EmployeeEntity employee) {
        employeeCache.put(employeeId, employee);
    }

    public void setEmployeeDetail(Long employeeId, EmployeeEntity employee) {
        employeeDetailCache.put(employeeId, employee);
    }

    public void removeEmployee(Long employeeId) {
        employeeCache.remove(employeeId);
        employeeDetailCache.remove(employeeId);
    }
}


