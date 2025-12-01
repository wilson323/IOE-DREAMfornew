package net.lab1024.sa.system.employee.manager;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.system.employee.dao.EmployeeDao;
import net.lab1024.sa.system.employee.domain.entity.EmployeeEntity;

/**
 * 员工管理Manager
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Service
@Slf4j
public class EmployeeManager {

    @Resource
    private EmployeeDao employeeDao;

    /**
     * 根据手机号查询员工
     *
     * 根据手机号精确查询员工信息，通常用于登录、找回密码等场景。
     * 如果存在多个相同手机号的员工（异常情况），返回第一个启用的员工。
     *
     * @param phone 手机号，不能为空或空白
     * @return 员工信息实体，如果不存在则返回null
     *
     * @example
     *
     *          <pre>
     *          EmployeeEntity employee = employeeManager.getEmployeeByPhone("13800138000");
     *          if (employee != null) {
     *              System.out.println("找到员工: " + employee.getEmployeeName());
     *          }
     *          </pre>
     */
    public EmployeeEntity getEmployeeByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            log.warn("根据手机号查询员工失败：手机号为空");
            return null;
        }

        try {
            LambdaQueryWrapper<EmployeeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeeEntity::getPhone, phone.trim())
                    .eq(EmployeeEntity::getStatus, 1) // 只查询启用的员工
                    .orderByDesc(EmployeeEntity::getCreateTime) // 如果有多条，返回最新创建的
                    .last("LIMIT 1");

            EmployeeEntity employee = employeeDao.selectOne(queryWrapper);

            if (employee != null) {
                log.debug("根据手机号查询员工成功: phone={}, employeeId={}", phone, employee.getEmployeeId());
            } else {
                log.debug("根据手机号未找到员工: phone={}", phone);
            }

            return employee;
        } catch (Exception e) {
            log.error("根据手机号查询员工失败: phone={}", phone, e);
            return null;
        }
    }

    /**
     * 根据邮箱查询员工
     *
     * 根据邮箱地址精确查询员工信息，通常用于登录、邮件通知等场景。
     * 如果存在多个相同邮箱的员工（异常情况），返回第一个启用的员工。
     *
     * @param email 邮箱地址，不能为空或空白
     * @return 员工信息实体，如果不存在则返回null
     *
     * @example
     *
     *          <pre>
     *          EmployeeEntity employee = employeeManager.getEmployeeByEmail("zhangsan@example.com");
     *          if (employee != null) {
     *              System.out.println("找到员工: " + employee.getEmployeeName());
     *          }
     *          </pre>
     */
    public EmployeeEntity getEmployeeByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            log.warn("根据邮箱查询员工失败：邮箱为空");
            return null;
        }

        try {
            LambdaQueryWrapper<EmployeeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeeEntity::getEmail, email.trim().toLowerCase()) // 邮箱统一转为小写
                    .eq(EmployeeEntity::getStatus, 1) // 只查询启用的员工
                    .orderByDesc(EmployeeEntity::getCreateTime) // 如果有多条，返回最新创建的
                    .last("LIMIT 1");

            EmployeeEntity employee = employeeDao.selectOne(queryWrapper);

            if (employee != null) {
                log.debug("根据邮箱查询员工成功: email={}, employeeId={}", email, employee.getEmployeeId());
            } else {
                log.debug("根据邮箱未找到员工: email={}", email);
            }

            return employee;
        } catch (Exception e) {
            log.error("根据邮箱查询员工失败: email={}", email, e);
            return null;
        }
    }

    /**
     * 根据身份证号查询员工
     *
     * 根据身份证号精确查询员工信息，通常用于身份验证、社保等场景。
     * 身份证号在系统中应该是唯一的，如果存在多条（异常情况），返回第一个启用的员工。
     *
     * @param idCard 身份证号，不能为空或空白
     * @return 员工信息实体，如果不存在则返回null
     *
     * @example
     *
     *          <pre>
     *          EmployeeEntity employee = employeeManager.getEmployeeByIdCard("110101199001011234");
     *          if (employee != null) {
     *              System.out.println("找到员工: " + employee.getEmployeeName());
     *          }
     *          </pre>
     */
    public EmployeeEntity getEmployeeByIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            log.warn("根据身份证号查询员工失败：身份证号为空");
            return null;
        }

        try {
            LambdaQueryWrapper<EmployeeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeeEntity::getIdCard, idCard.trim())
                    .eq(EmployeeEntity::getStatus, 1) // 只查询启用的员工
                    .orderByDesc(EmployeeEntity::getCreateTime) // 如果有多条，返回最新创建的
                    .last("LIMIT 1");

            EmployeeEntity employee = employeeDao.selectOne(queryWrapper);

            if (employee != null) {
                log.debug("根据身份证号查询员工成功: idCard={}, employeeId={}",
                        maskIdCard(idCard), employee.getEmployeeId());
            } else {
                log.debug("根据身份证号未找到员工: idCard={}", maskIdCard(idCard));
            }

            return employee;
        } catch (Exception e) {
            log.error("根据身份证号查询员工失败: idCard={}", maskIdCard(idCard), e);
            return null;
        }
    }

    /**
     * 检查手机号是否存在
     *
     * 检查指定手机号是否已被其他员工使用，用于新增或更新员工时的唯一性校验。
     * 可以排除指定的员工ID，用于更新时检查手机号是否与其他员工冲突。
     *
     * @param phone     手机号，不能为空或空白
     * @param excludeId 排除的员工ID（用于更新时检查，不需要排除时传null）
     * @return 如果手机号已存在（被其他员工使用）返回true，否则返回false
     *
     * @example
     *
     *          <pre>
     *          // 新增员工时检查
     *          boolean exists = employeeManager.checkPhoneExists("13800138000", null);
     *
     *          // 更新员工时检查（排除当前员工）
     *          boolean exists = employeeManager.checkPhoneExists("13800138000", 123L);
     *          </pre>
     */
    public boolean checkPhoneExists(String phone, Long excludeId) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        try {
            LambdaQueryWrapper<EmployeeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeeEntity::getPhone, phone.trim())
                    .eq(EmployeeEntity::getStatus, 1); // 只检查启用的员工

            // 如果指定了排除ID，则排除该员工
            if (excludeId != null) {
                queryWrapper.ne(EmployeeEntity::getEmployeeId, excludeId);
            }

            long count = employeeDao.selectCount(queryWrapper);
            boolean exists = count > 0;

            log.debug("检查手机号是否存在: phone={}, excludeId={}, exists={}",
                    phone, excludeId, exists);

            return exists;
        } catch (Exception e) {
            log.error("检查手机号是否存在失败: phone={}, excludeId={}", phone, excludeId, e);
            // 发生异常时返回false，不阻塞业务流程
            return false;
        }
    }

    /**
     * 检查邮箱是否存在
     *
     * 检查指定邮箱是否已被其他员工使用，用于新增或更新员工时的唯一性校验。
     * 可以排除指定的员工ID，用于更新时检查邮箱是否与其他员工冲突。
     *
     * @param email     邮箱地址，不能为空或空白
     * @param excludeId 排除的员工ID（用于更新时检查，不需要排除时传null）
     * @return 如果邮箱已存在（被其他员工使用）返回true，否则返回false
     *
     * @example
     *
     *          <pre>
     *          // 新增员工时检查
     *          boolean exists = employeeManager.checkEmailExists("zhangsan@example.com", null);
     *
     *          // 更新员工时检查（排除当前员工）
     *          boolean exists = employeeManager.checkEmailExists("zhangsan@example.com", 123L);
     *          </pre>
     */
    public boolean checkEmailExists(String email, Long excludeId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        try {
            LambdaQueryWrapper<EmployeeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeeEntity::getEmail, email.trim().toLowerCase()) // 邮箱统一转为小写
                    .eq(EmployeeEntity::getStatus, 1); // 只检查启用的员工

            // 如果指定了排除ID，则排除该员工
            if (excludeId != null) {
                queryWrapper.ne(EmployeeEntity::getEmployeeId, excludeId);
            }

            long count = employeeDao.selectCount(queryWrapper);
            boolean exists = count > 0;

            log.debug("检查邮箱是否存在: email={}, excludeId={}, exists={}",
                    email, excludeId, exists);

            return exists;
        } catch (Exception e) {
            log.error("检查邮箱是否存在失败: email={}, excludeId={}", email, excludeId, e);
            // 发生异常时返回false，不阻塞业务流程
            return false;
        }
    }

    /**
     * 根据部门ID统计员工数量
     *
     * 统计指定部门下的员工数量，只统计启用状态的员工。
     * 通常用于部门管理、人员统计等场景。
     *
     * @param departmentId 部门ID，不能为空
     * @return 该部门下的员工数量，如果部门ID为空则返回0
     *
     * @example
     *
     *          <pre>
     *          Long count = employeeManager.countEmployeesByDepartmentId(1L);
     *          System.out.println("部门1有 " + count + " 名员工");
     *          </pre>
     */
    public long countEmployeesByDepartmentId(Long departmentId) {
        if (departmentId == null) {
            log.warn("统计部门员工数量失败：部门ID为空");
            return 0;
        }

        try {
            LambdaQueryWrapper<EmployeeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeeEntity::getDepartmentId, departmentId)
                    .eq(EmployeeEntity::getStatus, 1); // 只统计启用的员工

            long count = employeeDao.selectCount(queryWrapper);

            log.debug("统计部门员工数量: departmentId={}, count={}", departmentId, count);

            return count;
        } catch (Exception e) {
            log.error("统计部门员工数量失败: departmentId={}", departmentId, e);
            // 发生异常时返回0，不中断业务流程
            return 0;
        }
    }

    /**
     * 批量更新员工部门
     *
     * 将指定部门下的所有员工批量转移到新部门，通常用于部门合并、拆分等场景。
     * 只更新启用状态的员工，并记录更新操作日志。
     *
     * @param oldDepartmentId 原部门ID，不能为空
     * @param newDepartmentId 新部门ID，不能为空
     * @return 成功更新的员工数量，如果参数无效则返回0
     *
     * @example
     *
     *          <pre>
     *          // 将部门1的所有员工转移到部门2
     *          int updated = employeeManager.batchUpdateDepartment(1L, 2L);
     *          System.out.println("成功更新 " + updated + " 名员工的部门");
     *          </pre>
     */
    public int batchUpdateDepartment(Long oldDepartmentId, Long newDepartmentId) {
        if (oldDepartmentId == null || newDepartmentId == null) {
            log.warn("批量更新员工部门失败：部门ID为空，oldDepartmentId={}, newDepartmentId={}",
                    oldDepartmentId, newDepartmentId);
            return 0;
        }

        if (oldDepartmentId.equals(newDepartmentId)) {
            log.warn("批量更新员工部门失败：原部门和新部门相同，departmentId={}", oldDepartmentId);
            return 0;
        }

        try {
            LambdaUpdateWrapper<EmployeeEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(EmployeeEntity::getDepartmentId, oldDepartmentId)
                    .eq(EmployeeEntity::getStatus, 1) // 只更新启用的员工
                    .set(EmployeeEntity::getDepartmentId, newDepartmentId);

            int updatedCount = employeeDao.update(null, updateWrapper);

            log.info("批量更新员工部门成功: oldDepartmentId={}, newDepartmentId={}, updatedCount={}",
                    oldDepartmentId, newDepartmentId, updatedCount);

            return updatedCount;
        } catch (Exception e) {
            log.error("批量更新员工部门失败: oldDepartmentId={}, newDepartmentId={}",
                    oldDepartmentId, newDepartmentId, e);
            // 发生异常时返回0，表示更新失败
            return 0;
        }
    }

    /**
     * 掩码处理身份证号（脱敏）
     *
     * 用于日志记录时保护敏感信息，只显示前6位和后4位，中间用*替代。
     *
     * @param idCard 身份证号
     * @return 掩码后的身份证号，如果长度不足则返回原始值
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        int length = idCard.length();
        return idCard.substring(0, 6) + "****" + idCard.substring(length - 4);
    }
}
