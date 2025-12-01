package net.lab1024.sa.admin.module.hr.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.hr.dao.EmployeeDao;
import net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity;
import net.lab1024.sa.admin.module.hr.manager.EmployeeCacheManager;
import net.lab1024.sa.admin.module.hr.service.EmployeeService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;

@Service
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeDao, EmployeeEntity>
        implements EmployeeService {

    @Resource
    private EmployeeCacheManager employeeCacheManager;

    @Override
    public PageResult<EmployeeEntity> pageEmployees(PageParam pageParam, String employeeName,
            Integer status, Long departmentId) {
        LambdaQueryWrapper<EmployeeEntity> wrapper = new LambdaQueryWrapper<>();
        if (employeeName != null && !employeeName.isEmpty()) {
            wrapper.like(EmployeeEntity::getEmployeeName, employeeName);
        }
        if (status != null) {
            wrapper.eq(EmployeeEntity::getStatus, status);
        }
        if (departmentId != null) {
            wrapper.eq(EmployeeEntity::getDepartmentId, departmentId);
        }
        wrapper.orderByDesc(EmployeeEntity::getCreateTime);

        Page<EmployeeEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        Page<EmployeeEntity> data = this.page(page, wrapper);

        PageResult<EmployeeEntity> result = new PageResult<>();
        result.setList(data.getRecords());
        result.setTotal(data.getTotal());
        result.setPageNum(data.getCurrent());
        result.setPageSize(data.getSize());
        return result;
    }

    /**
     * 根据ID获取员工信息（缓存）
     *
     * @param employeeId 员工ID
     * @return 员工信息
     */
    public EmployeeEntity getEmployeeById(Long employeeId) {
        return employeeCacheManager.getEmployee(employeeId);
    }

    /**
     * 根据ID获取员工详细信息（缓存）
     *
     * @param employeeId 员工ID
     * @return 员工详细信息
     */
    public EmployeeEntity getEmployeeDetailById(Long employeeId) {
        return employeeCacheManager.getEmployeeDetail(employeeId);
    }

    /**
     * 设置员工缓存
     *
     * @param employee 员工信息
     */
    public void setEmployeeCache(EmployeeEntity employee) {
        if (employee != null && employee.getEmployeeId() != null) {
            employeeCacheManager.setEmployee(employee.getEmployeeId(), employee);
            employeeCacheManager.setEmployeeDetail(employee.getEmployeeId(), employee);
        }
    }

    /**
     * 清理员工相关缓存
     *
     * @param employeeId 员工ID
     */
    public void removeEmployeeCache(Long employeeId) {
        employeeCacheManager.removeEmployee(employeeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addEmployee(EmployeeEntity employee) {
        // 默认启用
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }

        boolean result = this.save(employee);

        // 成功新增后设置缓存
        if (result && employee.getEmployeeId() != null) {
            this.setEmployeeCache(employee);
            log.info("员工新增成功并设置缓存 employeeId: {}", employee.getEmployeeId());
        }

        return result;
    }

    /**
     * 更新员工信息
     *
     * @param employee 员工信息
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEmployee(EmployeeEntity employee) {
        boolean result = this.updateById(employee);

        // 更新成功后清理相关缓存
        if (result && employee.getEmployeeId() != null) {
            this.removeEmployeeCache(employee.getEmployeeId());
            log.info("员工更新成功并清理缓存 employeeId: {}", employee.getEmployeeId());
        }

        return result;
    }

    /**
     * 删除员工
     *
     * @param employeeId 员工ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEmployee(Long employeeId) {
        boolean result = this.removeById(employeeId);

        // 删除成功后清理相关缓存
        if (result) {
            this.removeEmployeeCache(employeeId);
            log.info("员工删除成功并清理缓存 employeeId: {}", employeeId);
        }

        return result;
    }

}

