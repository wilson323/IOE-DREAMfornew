package net.lab1024.sa.admin.module.hr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;

public interface EmployeeService extends IService<EmployeeEntity> {

    PageResult<EmployeeEntity> pageEmployees(PageParam pageParam, String employeeName, Integer status, Long departmentId);

    boolean addEmployee(EmployeeEntity employee);
}

