package net.lab1024.sa.hr.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.hr.domain.entity.EmployeeEntity;
import net.lab1024.sa.hr.domain.form.EmployeeQueryForm;
import net.lab1024.sa.hr.domain.dto.EmployeeAddDTO;
import net.lab1024.sa.hr.domain.dto.EmployeeUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 员工服务接口
 *
 * @author IOE-DREAM Team
 */
public interface EmployeeService extends IService<EmployeeEntity> {

    /**
     * 分页查询员工
     */
    Page<EmployeeEntity> queryPage(EmployeeQueryForm queryForm);

    /**
     * 新增员工
     */
    Long add(EmployeeAddDTO addDTO);

    /**
     * 更新员工信息
     */
    void update(EmployeeUpdateDTO updateDTO);

    /**
     * 批量删除员工
     */
    int batchDelete(List<Long> employeeIds);

    /**
     * 根据员工编号查询
     */
    EmployeeEntity getByEmployeeNo(String employeeNo);

    /**
     * 根据用户ID查询
     */
    EmployeeEntity getByUserId(Long userId);

    /**
     * 根据部门ID查询
     */
    List<EmployeeEntity> getByDepartment(Long departmentId);

    /**
     * 验证员工编号是否存在
     */
    boolean validateEmployeeNo(String employeeNo);

    /**
     * 员工离职
     */
    void resign(Long employeeId);

    /**
     * 导入员工数据
     */
    String importEmployees(MultipartFile file);

    /**
     * 导出员工数据
     */
    String exportEmployees(EmployeeQueryForm queryForm);
}