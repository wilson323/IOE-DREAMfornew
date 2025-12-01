package net.lab1024.sa.system.employee.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.system.employee.dao.EmployeeDao;
import net.lab1024.sa.system.employee.domain.entity.EmployeeEntity;
import net.lab1024.sa.system.employee.domain.form.EmployeeAddForm;
import net.lab1024.sa.system.employee.domain.form.EmployeeQueryForm;
import net.lab1024.sa.system.employee.domain.form.EmployeeUpdateForm;
import net.lab1024.sa.system.employee.domain.vo.EmployeeVO;
import net.lab1024.sa.system.employee.manager.EmployeeManager;
import net.lab1024.sa.system.employee.service.EmployeeService;

/**
 * 员工管理Service实现
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Service
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeDao, EmployeeEntity> implements EmployeeService {

    @Resource
    private EmployeeManager employeeManager;

    @Override
    public PageResult<EmployeeVO> queryEmployeePage(EmployeeQueryForm queryForm) {
        // 构建查询条件
        LambdaQueryWrapper<EmployeeEntity> wrapper = new LambdaQueryWrapper<>();

        if (queryForm.getEmployeeName() != null && !queryForm.getEmployeeName().trim().isEmpty()) {
            wrapper.like(EmployeeEntity::getEmployeeName, queryForm.getEmployeeName().trim());
        }
        if (queryForm.getDepartmentId() != null) {
            wrapper.eq(EmployeeEntity::getDepartmentId, queryForm.getDepartmentId());
        }
        if (queryForm.getPosition() != null && !queryForm.getPosition().trim().isEmpty()) {
            wrapper.like(EmployeeEntity::getPosition, queryForm.getPosition().trim());
        }
        if (queryForm.getStatus() != null) {
            wrapper.eq(EmployeeEntity::getStatus, queryForm.getStatus());
        }
        if (queryForm.getGender() != null) {
            wrapper.eq(EmployeeEntity::getGender, queryForm.getGender());
        }
        if (queryForm.getPhone() != null && !queryForm.getPhone().trim().isEmpty()) {
            wrapper.like(EmployeeEntity::getPhone, queryForm.getPhone().trim());
        }
        if (queryForm.getEmail() != null && !queryForm.getEmail().trim().isEmpty()) {
            wrapper.like(EmployeeEntity::getEmail, queryForm.getEmail().trim());
        }

        wrapper.orderByDesc(EmployeeEntity::getCreateTime);

        // 分页查询 - 使用反射访问 Lombok 生成的 getter 方法
        int pageNum = 1;
        int pageSize = 20;
        try {
            java.lang.reflect.Method getPageNumMethod = queryForm.getClass().getMethod("getPageNum");
            Object pageNumObj = getPageNumMethod.invoke(queryForm);
            if (pageNumObj != null) {
                pageNum = (Integer) pageNumObj;
            }

            java.lang.reflect.Method getPageSizeMethod = queryForm.getClass().getMethod("getPageSize");
            Object pageSizeObj = getPageSizeMethod.invoke(queryForm);
            if (pageSizeObj != null) {
                pageSize = (Integer) pageSizeObj;
            }
        } catch (Exception e) {
            log.warn("无法通过反射访问分页参数，使用默认值", e);
        }

        Page<EmployeeEntity> page = new Page<>(pageNum, pageSize);
        Page<EmployeeEntity> resultPage = this.page(page, wrapper);

        // 转换为VO
        List<EmployeeVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, resultPage.getTotal(),
                Long.valueOf(pageNum), Long.valueOf(pageSize));
    }

    @Override
    public EmployeeVO getEmployeeDetail(Long employeeId) {
        EmployeeEntity entity = this.getById(employeeId);
        if (entity == null) {
            return null;
        }
        return convertToVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addEmployee(EmployeeAddForm addForm) {
        EmployeeEntity entity = new EmployeeEntity();
        BeanUtils.copyProperties(addForm, entity);

        // 默认启用状态
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }

        boolean result = this.save(entity);

        if (result) {
            log.info("员工新增成功 employeeId: {}, employeeName: {}",
                    entity.getEmployeeId(), entity.getEmployeeName());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEmployee(EmployeeUpdateForm updateForm) {
        EmployeeEntity entity = new EmployeeEntity();
        BeanUtils.copyProperties(updateForm, entity);

        boolean result = this.updateById(entity);

        if (result) {
            log.info("员工更新成功 employeeId: {}, employeeName: {}",
                    entity.getEmployeeId(), entity.getEmployeeName());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEmployee(Long employeeId) {
        boolean result = this.removeById(employeeId);

        if (result) {
            log.info("员工删除成功 employeeId: {}", employeeId);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteEmployees(List<Long> employeeIds) {
        if (CollectionUtils.isEmpty(employeeIds)) {
            return false;
        }

        boolean result = this.removeByIds(employeeIds);

        if (result) {
            log.info("员工批量删除成功 employeeIds: {}", employeeIds);
        }

        return result;
    }

    @Override
    public List<EmployeeVO> getEmployeesByDepartmentId(Long departmentId) {
        LambdaQueryWrapper<EmployeeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeeEntity::getDepartmentId, departmentId)
                .eq(EmployeeEntity::getStatus, 1)
                .orderByAsc(EmployeeEntity::getEmployeeName);

        List<EmployeeEntity> entities = this.list(wrapper);

        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEmployeeStatus(Long employeeId, Integer status) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setEmployeeId(employeeId);
        entity.setStatus(status);

        boolean result = this.updateById(entity);

        if (result) {
            log.info("员工状态更新成功 employeeId: {}, status: {}", employeeId, status);
        }

        return result;
    }

    /**
     * 实体转VO
     *
     * @param entity 实体
     * @return VO
     */
    private EmployeeVO convertToVO(EmployeeEntity entity) {
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置性别描述
        if (entity.getGender() != null) {
            vo.setGenderDesc(entity.getGender() == 1 ? "男" : "女");
        }

        // 设置状态描述
        if (entity.getStatus() != null) {
            vo.setStatusDesc(entity.getStatus() == 1 ? "启用" : "禁用");
        }

        // TODO: 设置部门名称 - 需要调用部门服务获取
        return vo;
    }
}
