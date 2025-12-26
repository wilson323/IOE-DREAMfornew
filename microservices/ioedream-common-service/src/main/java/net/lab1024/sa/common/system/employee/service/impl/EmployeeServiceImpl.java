package net.lab1024.sa.common.system.employee.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.system.employee.dao.EmployeeDao;
import net.lab1024.sa.common.system.employee.domain.dto.EmployeeAddDTO;
import net.lab1024.sa.common.system.employee.domain.dto.EmployeeQueryDTO;
import net.lab1024.sa.common.system.employee.domain.dto.EmployeeUpdateDTO;
import net.lab1024.sa.common.system.employee.domain.entity.EmployeeEntity;
import net.lab1024.sa.common.system.employee.domain.vo.EmployeeVO;
import net.lab1024.sa.common.system.employee.manager.EmployeeManager;
import net.lab1024.sa.common.system.employee.service.EmployeeService;

/**
 * 员工管理Service实现
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解标识服务实现
 * - 使用@Resource依赖注入（符合架构规范）
 * - 使用@Transactional管理事务
 * - 完整的异常处理和日志记录
 * - 完整的业务逻辑实现（禁止简化）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Service
@Transactional(rollbackFor = Exception.class)
@SuppressWarnings("null")
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {


    @Resource
    private EmployeeManager employeeManager;

    @Resource
    private EmployeeDao employeeDao;

    @Override
    @Observed(name = "employee.queryEmployeePage", contextualName = "employee-query-page")
    @Transactional(readOnly = true)
    public PageResult<EmployeeVO> queryEmployeePage(EmployeeQueryDTO queryDTO) {
        log.info("分页查询员工，页码：{}，页大小：{}", queryDTO.getPageNum(), queryDTO.getPageSize());

        try {
            // 构建查询条件
            LambdaQueryWrapper<EmployeeEntity> wrapper = new LambdaQueryWrapper<>();

            if (queryDTO.getEmployeeName() != null && !queryDTO.getEmployeeName().trim().isEmpty()) {
                wrapper.like(EmployeeEntity::getEmployeeName, queryDTO.getEmployeeName().trim());
            }
            if (queryDTO.getEmployeeNo() != null && !queryDTO.getEmployeeNo().trim().isEmpty()) {
                wrapper.like(EmployeeEntity::getEmployeeNo, queryDTO.getEmployeeNo().trim());
            }
            if (queryDTO.getDepartmentId() != null) {
                wrapper.eq(EmployeeEntity::getDepartmentId, queryDTO.getDepartmentId());
            }
            if (queryDTO.getPosition() != null && !queryDTO.getPosition().trim().isEmpty()) {
                wrapper.like(EmployeeEntity::getPosition, queryDTO.getPosition().trim());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(EmployeeEntity::getStatus, queryDTO.getStatus());
            }
            if (queryDTO.getGender() != null) {
                wrapper.eq(EmployeeEntity::getGender, queryDTO.getGender());
            }
            if (queryDTO.getPhone() != null && !queryDTO.getPhone().trim().isEmpty()) {
                wrapper.like(EmployeeEntity::getPhone, queryDTO.getPhone().trim());
            }
            if (queryDTO.getEmail() != null && !queryDTO.getEmail().trim().isEmpty()) {
                wrapper.like(EmployeeEntity::getEmail, queryDTO.getEmail().trim());
            }
            if (queryDTO.getEmployeeType() != null) {
                wrapper.eq(EmployeeEntity::getEmployeeType, queryDTO.getEmployeeType());
            }

            wrapper.eq(EmployeeEntity::getDeletedFlag, 0);
            wrapper.orderByDesc(EmployeeEntity::getCreateTime);

            // 分页查询
            Page<EmployeeEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            Page<EmployeeEntity> resultPage = employeeDao.selectPage(page, wrapper);

            // 转换为VO
            List<EmployeeVO> voList = resultPage.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("分页查询员工成功，记录数：{}", voList.size());

            return PageResult.of(voList, resultPage.getTotal(),
                    queryDTO.getPageNum(), queryDTO.getPageSize());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[分页查询员工] 参数异常，error={}", e.getMessage());
            throw new ParamException("EMPLOYEE_QUERY_PARAM_ERROR", "分页查询员工参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[分页查询员工] 系统异常", e);
            throw new SystemException("EMPLOYEE_QUERY_ERROR", "分页查询员工失败，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "employee.getEmployeeDetail", contextualName = "employee-get-detail")
    @Transactional(readOnly = true)
    public EmployeeVO getEmployeeDetail(Long employeeId) {
        log.info("获取员工详情，员工ID：{}", employeeId);

        try {
            EmployeeEntity entity = employeeDao.selectById(employeeId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                log.warn("员工不存在或已删除：{}", employeeId);
                return null;
            }

            EmployeeVO vo = convertToVO(entity);

            log.info("获取员工详情成功，员工姓名：{}", vo.getEmployeeName());

            return vo;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[获取员工详情] 参数异常，employeeId: {}, error={}", employeeId, e.getMessage());
            throw new ParamException("EMPLOYEE_ID_INVALID", "员工ID无效: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[获取员工详情] 系统异常，employeeId: {}", employeeId, e);
            throw new SystemException("EMPLOYEE_GET_DETAIL_ERROR", "获取员工详情失败，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "employee.addEmployee", contextualName = "employee-add")
    public boolean addEmployee(EmployeeAddDTO addDTO) {
        log.info("新增员工，姓名：{}，工号：{}", addDTO.getEmployeeName(), addDTO.getEmployeeNo());

        try {
            // 验证工号唯一性
            if (!employeeManager.isEmployeeNoUnique(addDTO.getEmployeeNo(), null)) {
                log.warn("[新增员工] 员工工号已存在：{}", addDTO.getEmployeeNo());
                throw new BusinessException("EMPLOYEE_NO_EXISTS", "员工工号已存在");
            }

            // 验证手机号唯一性
            if (!employeeManager.isPhoneUnique(addDTO.getPhone(), null)) {
                log.warn("[新增员工] 手机号已存在：{}", addDTO.getPhone());
                throw new BusinessException("EMPLOYEE_PHONE_EXISTS", "手机号已存在");
            }

            // 验证邮箱唯一性
            if (!employeeManager.isEmailUnique(addDTO.getEmail(), null)) {
                log.warn("[新增员工] 邮箱已存在：{}", addDTO.getEmail());
                throw new BusinessException("EMPLOYEE_EMAIL_EXISTS", "邮箱已存在");
            }

            // 验证身份证号唯一性
            if (!employeeManager.isIdCardNoUnique(addDTO.getIdCardNo(), null)) {
                log.warn("[新增员工] 身份证号已存在：{}", addDTO.getIdCardNo());
                throw new BusinessException("EMPLOYEE_ID_CARD_EXISTS", "身份证号已存在");
            }

            EmployeeEntity entity = new EmployeeEntity();
            BeanUtils.copyProperties(addDTO, entity);

            // 默认启用状态
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }

            int result = employeeDao.insert(entity);

            if (result > 0) {
                log.info("员工新增成功，员工ID：{}，姓名：{}", entity.getId(), entity.getEmployeeName());
            }

            return result > 0;

        } catch (BusinessException e) {
            log.warn("[新增员工] 业务异常，error={}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[新增员工] 参数异常，error={}", e.getMessage());
            throw new ParamException("EMPLOYEE_ADD_PARAM_ERROR", "新增员工参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[新增员工] 系统异常", e);
            throw new SystemException("EMPLOYEE_ADD_ERROR", "新增员工失败，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "employee.updateEmployee", contextualName = "employee-update")
    public boolean updateEmployee(EmployeeUpdateDTO updateDTO) {
        log.info("更新员工，员工ID：{}，姓名：{}", updateDTO.getEmployeeId(), updateDTO.getEmployeeName());

        try {
            // 验证员工是否存在
            EmployeeEntity existingEmployee = employeeDao.selectById(updateDTO.getEmployeeId());
            if (existingEmployee == null || existingEmployee.getDeletedFlag() == 1) {
                log.warn("[更新员工] 员工不存在或已删除：{}", updateDTO.getEmployeeId());
                throw new BusinessException("EMPLOYEE_NOT_FOUND", "员工不存在或已删除");
            }

            // 验证工号唯一性（排除自己）
            if (updateDTO.getEmployeeNo() != null &&
                    !employeeManager.isEmployeeNoUnique(updateDTO.getEmployeeNo(), updateDTO.getEmployeeId())) {
                log.warn("[更新员工] 员工工号已存在：{}", updateDTO.getEmployeeNo());
                throw new BusinessException("EMPLOYEE_NO_EXISTS", "员工工号已存在");
            }

            // 验证手机号唯一性（排除自己）
            if (updateDTO.getPhone() != null &&
                    !employeeManager.isPhoneUnique(updateDTO.getPhone(), updateDTO.getEmployeeId())) {
                log.warn("[更新员工] 手机号已存在：{}", updateDTO.getPhone());
                throw new BusinessException("EMPLOYEE_PHONE_EXISTS", "手机号已存在");
            }

            // 验证邮箱唯一性（排除自己）
            if (updateDTO.getEmail() != null &&
                    !employeeManager.isEmailUnique(updateDTO.getEmail(), updateDTO.getEmployeeId())) {
                log.warn("[更新员工] 邮箱已存在：{}", updateDTO.getEmail());
                throw new BusinessException("EMPLOYEE_EMAIL_EXISTS", "邮箱已存在");
            }

            EmployeeEntity entity = new EmployeeEntity();
            BeanUtils.copyProperties(updateDTO, entity);

            // 手动映射ID字段（DTO使用employeeId，Entity使用id）
            entity.setId(updateDTO.getEmployeeId());

            int result = employeeDao.updateById(entity);

            if (result > 0) {
                log.info("员工更新成功，员工ID：{}", updateDTO.getEmployeeId());
            }

            return result > 0;

        } catch (BusinessException e) {
            log.warn("[更新员工] 业务异常，error={}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[更新员工] 参数异常，error={}", e.getMessage());
            throw new ParamException("EMPLOYEE_UPDATE_PARAM_ERROR", "更新员工参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[更新员工] 系统异常", e);
            throw new SystemException("EMPLOYEE_UPDATE_ERROR", "更新员工失败，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "employee.deleteEmployee", contextualName = "employee-delete")
    public boolean deleteEmployee(Long employeeId) {
        log.info("删除员工，员工ID：{}", employeeId);

        try {
            // 验证员工是否存在
            EmployeeEntity employee = employeeDao.selectById(employeeId);
            if (employee == null || employee.getDeletedFlag() == 1) {
                log.warn("[员工管理] 员工不存在或已删除, employeeId={}", employeeId);
                throw new BusinessException("EMPLOYEE_NOT_FOUND", "员工不存在或已删除");
            }

            // 逻辑删除
            EmployeeEntity entity = new EmployeeEntity();
            entity.setId(employeeId);
            entity.setDeletedFlag(1);

            int result = employeeDao.updateById(entity);

            if (result > 0) {
                log.info("员工删除成功，员工ID：{}", employeeId);
            }

            return result > 0;

        } catch (BusinessException e) {
            log.warn("[删除员工] 业务异常，employeeId: {}, error={}", employeeId, e.getMessage());
            throw e;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[删除员工] 参数异常，employeeId: {}, error={}", employeeId, e.getMessage());
            throw new ParamException("EMPLOYEE_DELETE_PARAM_ERROR", "删除员工参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[删除员工] 系统异常，employeeId: {}", employeeId, e);
            throw new SystemException("EMPLOYEE_DELETE_ERROR", "删除员工失败，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "employee.batchDeleteEmployees", contextualName = "employee-batch-delete")
    public boolean batchDeleteEmployees(List<Long> employeeIds) {
        log.info("批量删除员工，数量：{}", employeeIds.size());

        try {
            int successCount = 0;

            for (Long employeeId : employeeIds) {
                try {
                    boolean result = deleteEmployee(employeeId);
                    if (result) {
                        successCount++;
                    }
                } catch (BusinessException | ParamException e) {
                    log.warn("[批量删除员工] 业务/参数异常，员工ID: {}, error={}", employeeId, e.getMessage());
                } catch (Exception e) {
                    log.error("[批量删除员工] 系统异常，员工ID: {}", employeeId, e);
                }
            }

            log.info("批量删除员工完成，成功：{}/{}", successCount, employeeIds.size());

            return successCount == employeeIds.size();

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[批量删除员工] 参数异常，error={}", e.getMessage());
            throw new ParamException("EMPLOYEE_BATCH_DELETE_PARAM_ERROR", "批量删除员工参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[批量删除员工] 系统异常", e);
            throw new SystemException("EMPLOYEE_BATCH_DELETE_ERROR", "批量删除员工失败，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "employee.getEmployeesByDepartmentId", contextualName = "employee-get-by-department")
    @Transactional(readOnly = true)
    public List<EmployeeVO> getEmployeesByDepartmentId(Long departmentId) {
        log.info("根据部门ID查询员工，部门ID：{}", departmentId);

        try {
            List<EmployeeEntity> employees = employeeManager.getEmployeesByDepartmentId(departmentId);

            List<EmployeeVO> voList = employees.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("根据部门ID查询员工成功，数量：{}", voList.size());

            return voList;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[根据部门ID查询员工] 参数异常，departmentId: {}, error={}", departmentId, e.getMessage());
            throw new ParamException("EMPLOYEE_DEPARTMENT_ID_INVALID", "部门ID无效: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[根据部门ID查询员工] 系统异常，departmentId: {}", departmentId, e);
            throw new SystemException("EMPLOYEE_GET_BY_DEPARTMENT_ERROR", "根据部门ID查询员工失败，请稍后重试", e);
        }
    }

    @Override
    @Observed(name = "employee.updateEmployeeStatus", contextualName = "employee-update-status")
    public boolean updateEmployeeStatus(Long employeeId, Integer status) {
        log.info("更新员工状态，员工ID：{}，状态：{}", employeeId, status);

        try {
            // 验证员工是否存在
            EmployeeEntity employee = employeeDao.selectById(employeeId);
            if (employee == null || employee.getDeletedFlag() == 1) {
                log.warn("[更新员工状态] 员工不存在或已删除：{}", employeeId);
                throw new BusinessException("EMPLOYEE_NOT_FOUND", "员工不存在或已删除");
            }

            // 验证状态值
            if (status == null || status < 1 || status > 4) {
                log.warn("[更新员工状态] 员工状态值无效：{}", status);
                throw new ParamException("EMPLOYEE_STATUS_INVALID", "员工状态值无效，有效范围：1-4");
            }

            EmployeeEntity entity = new EmployeeEntity();
            entity.setId(employeeId);
            entity.setStatus(status);

            int result = employeeDao.updateById(entity);

            if (result > 0) {
                log.info("员工状态更新成功，员工ID：{}，状态：{}", employeeId, status);
            }

            return result > 0;

        } catch (BusinessException e) {
            log.warn("[更新员工状态] 业务异常，employeeId: {}, error={}", employeeId, e.getMessage());
            throw e;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[更新员工状态] 参数异常，employeeId: {}, error={}", employeeId, e.getMessage());
            throw new ParamException("EMPLOYEE_STATUS_UPDATE_PARAM_ERROR", "更新员工状态参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[更新员工状态] 系统异常，employeeId: {}", employeeId, e);
            throw new SystemException("EMPLOYEE_STATUS_UPDATE_ERROR", "更新员工状态失败，请稍后重试", e);
        }
    }

    /**
     * 转换Entity为VO
     */
    private EmployeeVO convertToVO(EmployeeEntity entity) {
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(entity, vo);

        // 手动映射ID字段（Entity使用id，VO使用employeeId）
        vo.setEmployeeId(entity.getId());

        // 计算工龄
        if (entity.getHireDate() != null) {
            int workYears = employeeManager.calculateWorkYears(entity.getHireDate());
            vo.setWorkYears(workYears);
        }

        // 性别描述
        vo.setGenderDesc(getGenderDesc(entity.getGender()));

        // 状态描述
        vo.setStatusDesc(getStatusDesc(entity.getStatus()));

        // 员工类型描述
        vo.setEmployeeTypeDesc(getEmployeeTypeDesc(entity.getEmployeeType()));

        return vo;
    }

    /**
     * 获取性别描述
     */
    private String getGenderDesc(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "在职";
            case 2:
                return "离职";
            case 3:
                return "休假";
            case 4:
                return "停职";
            default:
                return "未知";
        }
    }

    /**
     * 获取员工类型描述
     */
    private String getEmployeeTypeDesc(Integer employeeType) {
        if (employeeType == null) {
            return "未知";
        }
        switch (employeeType) {
            case 1:
                return "正式";
            case 2:
                return "实习";
            case 3:
                return "外包";
            case 4:
                return "兼职";
            default:
                return "未知";
        }
    }
}

