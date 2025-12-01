package net.lab1024.sa.attendance.manager.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.attendance.manager.AttendanceRuleManager;

/**
 * 考勤规则管理器实现类
 *
 * <p>
 * 负责考勤规则的业务逻辑协调和数据管理
 * 处理复杂的业务逻辑，包括规则优先级、适用性判断等
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-29
 */
@Slf4j
@Component
public class AttendanceRuleManagerImpl implements AttendanceRuleManager {

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    @Override
    public AttendanceRuleEntity getRuleByEmployeeId(Long employeeId) {
        log.debug("Manager层查询员工考勤规则: 员工ID={}", employeeId);

        try {
            LocalDate today = LocalDate.now();

            // 1. 查询个人规则（优先级最高）
            List<AttendanceRuleEntity> individualRules = attendanceRuleDao.selectIndividualRules(employeeId, today);
            if (individualRules != null && !individualRules.isEmpty()) {
                // 按优先级排序，取最高优先级的有效规则
                individualRules.sort(Comparator
                        .comparing(AttendanceRuleEntity::getPriority, Comparator.nullsLast(Integer::compareTo))
                        .reversed());

                for (AttendanceRuleEntity rule : individualRules) {
                    if (rule.isActive()) {
                        log.debug("找到个人考勤规则: 规则ID={}, 规则名称={}",
                                rule.getRuleId(), rule.getRuleName());
                        return rule;
                    }
                }
            }

            // 2. 查询部门规则（简化处理，暂时无法获取员工部门信息）
            // TODO: 需要从HR服务获取员工的部门信息
            // List<AttendanceRuleEntity> departmentRules =
            // attendanceRuleDao.selectDepartmentRules(departmentId, today);
            // if (departmentRules != null && !departmentRules.isEmpty()) {
            // departmentRules.sort(Comparator.comparing(AttendanceRuleEntity::getPriority,
            // Comparator.nullsLast(Integer::compareTo)).reversed());
            // for (AttendanceRuleEntity rule : departmentRules) {
            // if (rule.isActive()) {
            // log.debug("找到部门考勤规则: 规则ID={}, 规则名称={}",
            // rule.getRuleId(), rule.getRuleName());
            // return rule;
            // }
            // }
            // }

            // 3. 查询全局规则（优先级最低）
            List<AttendanceRuleEntity> globalRules = attendanceRuleDao.selectGlobalRules(null, today);
            if (globalRules != null && !globalRules.isEmpty()) {
                // 按优先级排序，取最高优先级的有效规则
                globalRules.sort(Comparator
                        .comparing(AttendanceRuleEntity::getPriority, Comparator.nullsLast(Integer::compareTo))
                        .reversed());

                for (AttendanceRuleEntity rule : globalRules) {
                    if (rule.isActive()) {
                        log.debug("找到全局考勤规则: 规则ID={}, 规则名称={}",
                                rule.getRuleId(), rule.getRuleName());
                        return rule;
                    }
                }
            }

            log.debug("未找到适用的考勤规则: 员工ID={}", employeeId);
            return null;
        } catch (Exception e) {
            log.error("Manager层查询员工考勤规则失败: 员工ID={}", employeeId, e);
            throw e;
        }
    }

    @Override
    public List<AttendanceRuleEntity> getRulesByDepartmentId(Long departmentId) {
        log.debug("Manager层查询部门考勤规则: 部门ID={}", departmentId);

        try {
            LocalDate today = LocalDate.now();
            return attendanceRuleDao.selectDepartmentRules(departmentId, today);
        } catch (Exception e) {
            log.error("Manager层查询部门考勤规则失败: 部门ID={}", departmentId, e);
            throw e;
        }
    }

    @Override
    public boolean saveOrUpdateRule(AttendanceRuleEntity rule) {
        log.info("Manager层保存考勤规则: 规则ID={}, 规则名称={}", rule.getRuleId(), rule.getRuleName());

        try {
            if (rule.getRuleId() == null) {
                // 新增
                int result = attendanceRuleDao.insert(rule);
                return result > 0;
            } else {
                // 更新
                int result = attendanceRuleDao.updateById(rule);
                return result > 0;
            }
        } catch (Exception e) {
            log.error("Manager层保存考勤规则失败: 规则名称={}", rule.getRuleName(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteRule(Long ruleId) {
        log.info("Manager层删除考勤规则: 规则ID={}", ruleId);

        try {
            // 软删除：更新状态为INACTIVE
            AttendanceRuleEntity rule = new AttendanceRuleEntity();
            rule.setRuleId(ruleId);
            rule.setStatus("INACTIVE");
            rule.setEnabled(false);

            int result = attendanceRuleDao.updateById(rule);
            return result > 0;
        } catch (Exception e) {
            log.error("Manager层删除考勤规则失败: 规则ID={}", ruleId, e);
            throw e;
        }
    }

    @Override
    public int batchDeleteRules(List<Long> ruleIds) {
        log.info("Manager层批量删除考勤规则: 规则数量={}", ruleIds.size());

        try {
            int deletedCount = 0;
            for (Long ruleId : ruleIds) {
                if (deleteRule(ruleId)) {
                    deletedCount++;
                }
            }
            return deletedCount;
        } catch (Exception e) {
            log.error("Manager层批量删除考勤规则失败: 规则数量={}", ruleIds.size(), e);
            throw e;
        }
    }

    @Override
    public List<AttendanceRuleEntity> getAllValidRules() {
        log.debug("Manager层查询所有有效的考勤规则");

        try {
            LocalDate today = LocalDate.now();
            // 使用selectActiveRules方法查询所有生效的规则
            List<AttendanceRuleEntity> activeRules = attendanceRuleDao.selectActiveRules(today);
            if (activeRules == null) {
                return new ArrayList<>();
            }

            // 按优先级排序
            return activeRules.stream()
                    .sorted(Comparator
                            .comparing(AttendanceRuleEntity::getPriority, Comparator.nullsLast(Integer::compareTo))
                            .reversed())
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("Manager层查询所有有效考勤规则失败", e);
            throw e;
        }
    }

    @Override
    public AttendanceRuleEntity getRuleById(Long ruleId) {
        log.debug("Manager层查询考勤规则详情: 规则ID={}", ruleId);

        try {
            return attendanceRuleDao.selectById(ruleId);
        } catch (Exception e) {
            log.error("Manager层查询考勤规则详情失败: 规则ID={}", ruleId, e);
            throw e;
        }
    }

    @Override
    public boolean updateRuleStatus(Long ruleId, Boolean enabled) {
        log.info("Manager层更新考勤规则状态: 规则ID={}, 启用状态={}", ruleId, enabled);

        try {
            AttendanceRuleEntity rule = new AttendanceRuleEntity();
            rule.setRuleId(ruleId);
            rule.setEnabled(enabled);
            rule.setStatus(enabled ? "ACTIVE" : "INACTIVE");

            int result = attendanceRuleDao.updateById(rule);
            return result > 0;
        } catch (Exception e) {
            log.error("Manager层更新考勤规则状态失败: 规则ID={}, 启用状态={}", ruleId, enabled, e);
            throw e;
        }
    }

    @Override
    public AttendanceRuleEntity getApplicableRule(Long employeeId, Long departmentId, String employeeType) {
        log.debug("Manager层获取员工适用的考勤规则: 员工ID={}, 部门ID={}, 员工类型={}",
                employeeId, departmentId, employeeType);

        try {
            LocalDate today = LocalDate.now();
            // 获取所有可能的适用规则
            List<AttendanceRuleEntity> applicableRules = new ArrayList<>();

            // 1. 个人规则
            if (employeeId != null) {
                List<AttendanceRuleEntity> individualRules = attendanceRuleDao.selectIndividualRules(employeeId, today);
                if (individualRules != null) {
                    for (AttendanceRuleEntity rule : individualRules) {
                        if (rule.isApplicable(employeeId, departmentId, employeeType) && rule.isActive()) {
                            applicableRules.add(rule);
                        }
                    }
                }
            }

            // 2. 部门规则
            if (departmentId != null) {
                List<AttendanceRuleEntity> departmentRules = attendanceRuleDao.selectDepartmentRules(departmentId,
                        today);
                if (departmentRules != null) {
                    for (AttendanceRuleEntity rule : departmentRules) {
                        if (rule.isApplicable(employeeId, departmentId, employeeType) && rule.isActive()) {
                            applicableRules.add(rule);
                        }
                    }
                }
            }

            // 3. 全局规则
            List<AttendanceRuleEntity> globalRules = attendanceRuleDao.selectGlobalRules(employeeType, today);
            if (globalRules != null) {
                for (AttendanceRuleEntity rule : globalRules) {
                    if (rule.isApplicable(employeeId, departmentId, employeeType) && rule.isActive()) {
                        applicableRules.add(rule);
                    }
                }
            }

            // 按优先级排序，返回最高优先级的规则
            if (!applicableRules.isEmpty()) {
                applicableRules.sort(Comparator
                        .comparing(AttendanceRuleEntity::getPriority, Comparator.nullsLast(Integer::compareTo))
                        .reversed());
                AttendanceRuleEntity selectedRule = applicableRules.get(0);

                log.debug("选择适用的考勤规则: 规则ID={}, 规则名称={}, 优先级={}",
                        selectedRule.getRuleId(), selectedRule.getRuleName(), selectedRule.getPriority());
                return selectedRule;
            }

            log.debug("未找到适用的考勤规则: 员工ID={}, 部门ID={}, 员工类型={}",
                    employeeId, departmentId, employeeType);
            return null;
        } catch (Exception e) {
            log.error("Manager层获取员工适用的考勤规则失败: 员工ID={}", employeeId, e);
            throw e;
        }
    }
}
