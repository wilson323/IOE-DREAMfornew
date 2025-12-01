package net.lab1024.sa.attendance.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.attendance.service.AttendanceRuleService;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartPageUtil;

/**
 * 考勤规则服务实现类
 *
 * <p>
 * 严格遵循repowiki规范:
 * - 实现考勤规则的CRUD操作
 * - 支持员工规则绑定和查询
 * - 提供规则缓存和版本管理
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Slf4j
@Service
public class AttendanceRuleServiceImpl extends ServiceImpl<AttendanceRuleDao, AttendanceRuleEntity>
        implements AttendanceRuleService {

    // TEMP: HR functionality disabled
    // @Resource
    // private EmployeeDao employeeDao;

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    // TEMP: Cache functionality disabled
    // @Resource
    // private AttendanceCacheService attendanceCacheService;

    @Override
    public AttendanceRuleEntity getRuleByEmployeeId(Long employeeId) {
        try {
            log.debug("查询员工考勤规则: 员工ID={}", employeeId);

            LambdaQueryWrapper<AttendanceRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRuleEntity::getEmployeeId, employeeId)
                    .eq(AttendanceRuleEntity::getDeletedFlag, 0)
                    .eq(AttendanceRuleEntity::getEnabled, Boolean.TRUE)
                    .orderByDesc(AttendanceRuleEntity::getCreateTime)
                    .last("LIMIT 1");

            return this.getOne(queryWrapper);
        } catch (Exception e) {
            log.error("查询员工考勤规则失败: 员工ID" + employeeId, e);
            return null;
        }
    }

    @Override
    public List<AttendanceRuleEntity> getRulesByDepartmentId(Long departmentId) {
        try {
            log.debug("查询部门考勤规则: 部门ID={}", departmentId);

            LambdaQueryWrapper<AttendanceRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRuleEntity::getDepartmentId, departmentId)
                    .eq(AttendanceRuleEntity::getDeletedFlag, 0)
                    .eq(AttendanceRuleEntity::getEnabled, Boolean.TRUE)
                    .orderByAsc(AttendanceRuleEntity::getPriority)
                    .orderByDesc(AttendanceRuleEntity::getCreateTime);

            return this.list(queryWrapper);
        } catch (Exception e) {
            log.error("查询部门考勤规则失败: 部门ID" + departmentId, e);
            return List.of();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateRule(AttendanceRuleEntity rule) {
        try {
            log.info("保存考勤规则: {}", rule.getRuleName());

            // 设置默认值
            if (rule.getCreateTime() == null) {
                rule.setCreateTime(LocalDateTime.now());
            }
            rule.setUpdateTime(LocalDateTime.now());

            // 设置默认删除标识
            if (rule.getDeletedFlag() == null) {
                rule.setDeletedFlag(0);
            }

            // 设置默认启用状态
            if (rule.getEnabled() == null) {
                rule.setEnabled(true);
            }

            // 设置默认优先级
            if (rule.getPriority() == null) {
                rule.setPriority(1);
            }

            boolean result = this.saveOrUpdate(rule);

            if (result) {
                log.info("考勤规则保存成功: 规则ID={}, 规则名称={}",
                        rule.getRuleId(), rule.getRuleName());
            } else {
                log.warn("考勤规则保存失败: 规则名称={}", rule.getRuleName());
            }

            return result;
        } catch (Exception e) {
            log.error("保存考勤规则失败: " + rule.getRuleName(), e);
            throw new RuntimeException("保存考勤规则失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(Long ruleId) {
        try {
            log.info("删除考勤规则: 规则ID={}", ruleId);

            AttendanceRuleEntity rule = this.getById(ruleId);
            if (rule == null) {
                log.warn("要删除的考勤规则不存在: 规则ID={}", ruleId);
                return false;
            }

            // 软删除
            rule.setDeletedFlag(1);
            rule.setUpdateTime(LocalDateTime.now());

            boolean result = this.updateById(rule);

            if (result) {
                log.info("考勤规则删除成功: 规则ID={}, 规则名称={}",
                        ruleId, rule.getRuleName());
            }

            return result;
        } catch (Exception e) {
            log.error("删除考勤规则失败: 规则ID" + ruleId, e);
            throw new RuntimeException("删除考勤规则失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteRules(List<Long> ruleIds) {
        try {
            log.info("批量删除考勤规则: 规则ID数量={}", ruleIds.size());

            if (ruleIds == null || ruleIds.isEmpty()) {
                return 0;
            }

            int count = 0;
            for (Long ruleId : ruleIds) {
                if (deleteRule(ruleId)) {
                    count++;
                }
            }

            log.info("批量删除考勤规则完成: 成功删除数量={}", count);
            return count;
        } catch (Exception e) {
            log.error("批量删除考勤规则失败", e);
            throw new RuntimeException("批量删除考勤规则失败: " + e.getMessage());
        }
    }

    @Override
    public List<AttendanceRuleEntity> getAllValidRules() {
        try {
            log.debug("查询所有有效的考勤规则");

            LambdaQueryWrapper<AttendanceRuleEntity> queryWrapper = new LambdaQueryWrapper<>();

            queryWrapper.eq(AttendanceRuleEntity::getDeletedFlag, 0)
                    .eq(AttendanceRuleEntity::getEnabled, Boolean.TRUE)
                    .orderByAsc(AttendanceRuleEntity::getPriority)
                    .orderByDesc(AttendanceRuleEntity::getCreateTime);

            return this.list(queryWrapper);
        } catch (Exception e) {
            log.error("查询所有有效的考勤规则失败", e);
            return List.of();
        }
    }

    @Override
    public AttendanceRuleEntity getRuleById(Long ruleId) {
        try {
            log.debug("查询考勤规则详情: 规则ID={}", ruleId);
            AttendanceRuleEntity rule = this.getById(ruleId);
            if (rule != null && rule.getDeletedFlag() != null && rule.getDeletedFlag() == 0) {
                return rule;
            }
            return null;
        } catch (Exception e) {
            log.error("查询考勤规则详情失败: 规则ID={}", ruleId, e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRuleStatus(Long ruleId, Boolean enabled) {
        try {
            log.info("更新考勤规则状态: 规则ID={}, 启用状态={}", ruleId, enabled);
            AttendanceRuleEntity rule = this.getById(ruleId);
            if (rule == null) {
                log.warn("要修改状态的考勤规则不存在: 规则ID={}", ruleId);
                return false;
            }
            rule.setEnabled(enabled);
            rule.setUpdateTime(LocalDateTime.now());
            boolean result = this.updateById(rule);
            if (result) {
                log.info("考勤规则状态更新成功: 规则ID={}, 启用状态={}", ruleId, enabled);
            }
            return result;
        } catch (Exception e) {
            log.error("更新考勤规则状态失败: 规则ID={}", ruleId, e);
            throw new RuntimeException("更新考勤规则状态失败: " + e.getMessage());
        }
    }

    @Override
    public AttendanceRuleEntity getApplicableRule(Long employeeId, Long departmentId, String employeeType) {
        try {
            log.debug("获取员工适用的考勤规则: 员工ID={}, 部门ID={}, 员工类型={}", employeeId, departmentId, employeeType);

            // 优先级：个人规则 > 部门规则 > 全局规则
            // 1. 先查询个人规则
            if (employeeId != null) {
                AttendanceRuleEntity personalRule = getRuleByEmployeeId(employeeId);
                if (personalRule != null) {
                    log.debug("找到个人考勤规则: 规则ID={}, 规则名称={}", personalRule.getRuleId(), personalRule.getRuleName());
                    return personalRule;
                }
            }

            // 2. 查询部门规则
            if (departmentId != null) {
                List<AttendanceRuleEntity> departmentRules = getRulesByDepartmentId(departmentId);
                if (departmentRules != null && !departmentRules.isEmpty()) {
                    // 按优先级排序，返回优先级最高的
                    AttendanceRuleEntity bestRule = departmentRules.stream()
                            .min((r1, r2) -> Integer.compare(
                                    r1.getPriority() != null ? r1.getPriority() : Integer.MAX_VALUE,
                                    r2.getPriority() != null ? r2.getPriority() : Integer.MAX_VALUE))
                            .orElse(null);
                    if (bestRule != null) {
                        log.debug("找到部门考勤规则: 规则ID={}, 规则名称={}, 优先级={}",
                                bestRule.getRuleId(), bestRule.getRuleName(), bestRule.getPriority());
                        return bestRule;
                    }
                }
            }

            // 3. 查询全局规则（没有员工ID和部门ID的规则）
            LambdaQueryWrapper<AttendanceRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRuleEntity::getDeletedFlag, 0)
                    .eq(AttendanceRuleEntity::getEnabled, Boolean.TRUE)
                    .isNull(AttendanceRuleEntity::getEmployeeId)
                    .isNull(AttendanceRuleEntity::getDepartmentId)
                    .orderByAsc(AttendanceRuleEntity::getPriority)
                    .orderByDesc(AttendanceRuleEntity::getCreateTime)
                    .last("LIMIT 1");

            AttendanceRuleEntity globalRule = this.getOne(queryWrapper);
            if (globalRule != null) {
                log.debug("找到全局考勤规则: 规则ID={}, 规则名称={}", globalRule.getRuleId(), globalRule.getRuleName());
                return globalRule;
            }

            log.debug("未找到适用的考勤规则: 员工ID={}, 部门ID={}", employeeId, departmentId);
            return null;
        } catch (Exception e) {
            log.error("获取员工适用的考勤规则失败: 员工ID={}", employeeId, e);
            return null;
        }
    }

    @Override
    public boolean validateRuleConflict(AttendanceRuleEntity rule) {
        try {
            log.debug("验证考勤规则冲突: 规则名称={}", rule.getRuleName());

            LambdaQueryWrapper<AttendanceRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRuleEntity::getDeletedFlag, 0)
                    .eq(AttendanceRuleEntity::getEnabled, Boolean.TRUE);

            // 如果规则有员工ID，检查是否有其他规则绑定同一员工
            if (rule.getEmployeeId() != null) {
                queryWrapper.eq(AttendanceRuleEntity::getEmployeeId, rule.getEmployeeId());
                if (rule.getRuleId() != null) {
                    queryWrapper.ne(AttendanceRuleEntity::getRuleId, rule.getRuleId());
                }
                long count = this.count(queryWrapper);
                if (count > 0) {
                    log.warn("发现考勤规则冲突: 员工ID={}已绑定其他规则", rule.getEmployeeId());
                    return true;
                }
            }

            // 如果规则有部门ID，检查是否有其他规则绑定同一部门且优先级相同
            if (rule.getDepartmentId() != null && rule.getPriority() != null) {
                queryWrapper.clear();
                queryWrapper.eq(AttendanceRuleEntity::getDeletedFlag, 0)
                        .eq(AttendanceRuleEntity::getEnabled, Boolean.TRUE)
                        .eq(AttendanceRuleEntity::getDepartmentId, rule.getDepartmentId())
                        .eq(AttendanceRuleEntity::getPriority, rule.getPriority());
                if (rule.getRuleId() != null) {
                    queryWrapper.ne(AttendanceRuleEntity::getRuleId, rule.getRuleId());
                }
                long count = this.count(queryWrapper);
                if (count > 0) {
                    log.warn("发现考勤规则冲突: 部门ID={}, 优先级={}已存在相同规则",
                            rule.getDepartmentId(), rule.getPriority());
                    return true;
                }
            }

            log.debug("考勤规则无冲突: 规则名称={}", rule.getRuleName());
            return false;
        } catch (Exception e) {
            log.error("验证考勤规则冲突失败: 规则名称={}", rule.getRuleName(), e);
            return false;
        }
    }

    /**
     * 分页查询考勤规则
     *
     * @param pageParam    分页参数
     * @param ruleName     规则名称（可选）
     * @param departmentId 部门ID（可选）
     * @param enabled      启用状态（可选）
     * @return 分页结果
     */
    public PageResult<AttendanceRuleEntity> queryRulePage(PageParam pageParam, String ruleName,
            Long departmentId, Boolean enabled) {
        try {
            log.debug("分页查询考勤规则: 页码={}, 大小={}", pageParam.getPageNum(), pageParam.getPageSize());

            // 构建查询条件
            LambdaQueryWrapper<AttendanceRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRuleEntity::getDeletedFlag, 0);

            // 规则名称模糊查询
            if (ruleName != null && !ruleName.trim().isEmpty()) {
                queryWrapper.like(AttendanceRuleEntity::getRuleName, ruleName.trim());
            }

            // 部门过滤
            if (departmentId != null) {
                queryWrapper.eq(AttendanceRuleEntity::getDepartmentId, departmentId);
            }

            // 启用状态过滤
            if (enabled != null) {
                queryWrapper.eq(AttendanceRuleEntity::getEnabled, enabled);
            }

            queryWrapper.orderByAsc(AttendanceRuleEntity::getPriority)
                    .orderByDesc(AttendanceRuleEntity::getCreateTime);

            // 分页查询
            @SuppressWarnings("unchecked")
            Page<AttendanceRuleEntity> page = (Page<AttendanceRuleEntity>) SmartPageUtil.convert2PageQuery(pageParam);
            IPage<AttendanceRuleEntity> pageResult = this.page(page, queryWrapper);

            return SmartPageUtil.convert2PageResult(page, pageResult.getRecords());
        } catch (Exception e) {
            log.error("分页查询考勤规则失败", e);
            return PageResult.empty();
        }
    }

    /**
     * 启用/禁用考勤规则
     *
     * @param ruleId  规则ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleRuleStatus(Long ruleId, boolean enabled) {
        try {
            log.info("{}考勤规则: 规则ID={}", enabled ? "启用" : "禁用", ruleId);

            AttendanceRuleEntity rule = this.getById(ruleId);
            if (rule == null) {
                log.warn("要修改状态的考勤规则不存在: 规则ID={}", ruleId);
                return false;
            }

            rule.setEnabled(enabled);
            rule.setUpdateTime(LocalDateTime.now());

            boolean result = this.updateById(rule);

            if (result) {
                log.info("考勤规则状态修改成功: 规则ID={}, 状态={}", ruleId, enabled ? "启用" : "禁用");
            }

            return result;
        } catch (Exception e) {
            log.error("修改考勤规则状态失败: 规则ID" + ruleId, e);
            throw new RuntimeException("修改考勤规则状态失败: " + e.getMessage());
        }
    }

    /*
     *
     *
     * @param sourceRuleId 源规则ID
     *
     * @param newRuleName 新规则名称
     *
     * @param employeeId 绑定的员工ID（可选）
     *
     * @param departmentId 绑定的部门ID（可选）
     *
     */
    @Transactional(rollbackFor = Exception.class)
    public AttendanceRuleEntity copyRule(Long sourceRuleId, String newRuleName,
            Long employeeId, Long departmentId) {
        try {
            log.info("复制考勤规则: 源规则ID={}, 新规则名称={}", sourceRuleId, newRuleName);

            AttendanceRuleEntity sourceRule = this.getById(sourceRuleId);
            if (sourceRule == null) {
                throw new RuntimeException("源考勤规则不存在: " + sourceRuleId);
            }

            // 创建新规则
            AttendanceRuleEntity newRule = new AttendanceRuleEntity();

            // 复制所有字段，但排除主键和时间字段
            SmartBeanUtil.copyProperties(sourceRule, newRule);
            newRule.setRuleId(null); // 清空主键
            newRule.setRuleName(newRuleName);

            // 设置新的绑定关系
            if (employeeId != null) {
                newRule.setEmployeeId(employeeId);
            }
            if (departmentId != null) {
                newRule.setDepartmentId(departmentId);
            }

            // 设置创建时间和更新时间
            newRule.setCreateTime(LocalDateTime.now());
            newRule.setUpdateTime(LocalDateTime.now());

            // 保存新规则
            this.save(newRule);

            log.info("考勤规则复制成功: 新规则ID={}, 源规则ID={}",
                    newRule.getRuleId(), sourceRuleId);

            return newRule;
        } catch (Exception e) {
            log.error("复制考勤规则失败: 源规则ID" + sourceRuleId, e);
            throw new RuntimeException("复制考勤规则失败: " + e.getMessage());
        }
    }

    /**
     * 获取规则的适用员工数量
     *
     * @param ruleId 规则ID
     * @return 适用员工数量
     */
    public long getApplicableEmployeeCount(Long ruleId) {
        try {
            AttendanceRuleEntity rule = this.getById(ruleId);
            if (rule == null) {
                return 0;
            }

            if (rule.getEmployeeId() != null) {
                // 个人规则：1个员工
                return 1;
            } else if (rule.getDepartmentId() != null) {
                // 部门规则：查询部门及其子部门下的员工数量
                try {
                    // 1. 获取部门及其所有子部门的ID列表
                    // TEMP: Cache functionality disabled
                    // List<Long> departmentIds = departmentCacheManager
                    // .getDepartmentSelfAndChildren(rule.getDepartmentId());
                    List<Long> departmentIds = List.of(rule.getDepartmentId());

                    // 2. 统计所有部门下的员工数量
                    // TEMP: HR functionality disabled
                    // Long count = employeeDao.countByDepartmentIds(departmentIds);
                    Long count = 0L;

                    log.debug("查询部门员工数量: departmentId={}, 包含子部门数量={}, 员工总数={}",
                            rule.getDepartmentId(), departmentIds.size(), count);
                    return count != null ? count : 0L;
                } catch (Exception e) {
                    log.error("查询部门员工数量失败: departmentId={}", rule.getDepartmentId(), e);
                    // 降级处理：只查询当前部门
                    try {
                        // TEMP: HR functionality disabled
                        // Long count = employeeDao.countByDepartmentId(rule.getDepartmentId());
                        Long count = 0L;
                        return count != null ? count : 0L;
                    } catch (Exception e2) {
                        log.error("降级查询部门员工数量也失败: departmentId={}", rule.getDepartmentId(), e2);
                        return 0L;
                    }
                }
            } else {
                // 全局规则：查询所有员工数量
                try {
                    // TEMP: HR functionality disabled
                    // Long count = employeeDao.countAll();
                    Long count = 0L;
                    return count != null ? count : 0L;
                } catch (Exception e) {
                    log.error("查询所有员工数量失败", e);
                    return 0L;
                }
            }
        } catch (Exception e) {
            log.error("查询规则适用员工数量失败: 规则ID" + ruleId, e);
            return 0;
        }
    }
}
