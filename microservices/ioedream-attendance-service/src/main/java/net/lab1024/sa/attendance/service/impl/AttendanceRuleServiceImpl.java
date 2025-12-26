package net.lab1024.sa.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.common.util.QueryBuilder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.domain.form.AttendanceRuleAddForm;
import net.lab1024.sa.attendance.domain.form.AttendanceRuleQueryForm;
import net.lab1024.sa.attendance.domain.form.AttendanceRuleUpdateForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceRuleVO;
import net.lab1024.sa.common.entity.attendance.AttendanceRuleEntity;
import net.lab1024.sa.attendance.service.AttendanceRuleService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * 考勤规则服务实现类
 * <p>
 * 提供考勤规则管理相关业务功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
public class AttendanceRuleServiceImpl implements AttendanceRuleService {

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    /**
     * 查询员工考勤规则
     *
     * @param employeeId 员工ID
     * @return 规则列表
     */
    @Override
    public List<AttendanceRuleVO> getEmployeeRules(Long employeeId) {
        log.info("[考勤规则] 查询员工规则: employeeId={}", employeeId);

        // 查询个人规则
        LambdaQueryWrapper<AttendanceRuleEntity> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(AttendanceRuleEntity::getRuleStatus, 1)
                .and(wrapper -> wrapper.eq(AttendanceRuleEntity::getRuleScope, "USER")
                        .like(AttendanceRuleEntity::getUserIds, "\"" + employeeId + "\""))
                .orderByAsc(AttendanceRuleEntity::getExecutionOrder)
                .orderByAsc(AttendanceRuleEntity::getRulePriority);
        List<AttendanceRuleEntity> userRules = attendanceRuleDao.selectList(userQueryWrapper);

        // 查询全局规则
        LambdaQueryWrapper<AttendanceRuleEntity> globalQueryWrapper = new LambdaQueryWrapper<>();
        globalQueryWrapper.eq(AttendanceRuleEntity::getRuleStatus, 1)
                .eq(AttendanceRuleEntity::getRuleScope, "GLOBAL")
                .orderByAsc(AttendanceRuleEntity::getExecutionOrder)
                .orderByAsc(AttendanceRuleEntity::getRulePriority);
        List<AttendanceRuleEntity> globalRules = attendanceRuleDao.selectList(globalQueryWrapper);

        // 合并规则（全局规则 + 个人规则）
        List<AttendanceRuleEntity> allRules = new java.util.ArrayList<>(globalRules);
        allRules.addAll(userRules);

        log.info("[考勤规则] 查询员工规则成功: employeeId={}, count={}", employeeId, allRules.size());
        return allRules.stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询部门考勤规则
     *
     * @param departmentId 部门ID
     * @return 规则列表
     */
    @Override
    public List<AttendanceRuleVO> getDepartmentRules(Long departmentId) {
        log.info("[考勤规则] 查询部门规则: departmentId={}", departmentId);

        // 查询部门规则
        LambdaQueryWrapper<AttendanceRuleEntity> deptQueryWrapper = new LambdaQueryWrapper<>();
        deptQueryWrapper.eq(AttendanceRuleEntity::getRuleStatus, 1)
                .and(wrapper -> wrapper.eq(AttendanceRuleEntity::getRuleScope, "DEPARTMENT")
                        .like(AttendanceRuleEntity::getDepartmentIds, "\"" + departmentId + "\""))
                .orderByAsc(AttendanceRuleEntity::getExecutionOrder)
                .orderByAsc(AttendanceRuleEntity::getRulePriority);
        List<AttendanceRuleEntity> deptRules = attendanceRuleDao.selectList(deptQueryWrapper);

        // 查询全局规则
        LambdaQueryWrapper<AttendanceRuleEntity> globalQueryWrapper = new LambdaQueryWrapper<>();
        globalQueryWrapper.eq(AttendanceRuleEntity::getRuleStatus, 1)
                .eq(AttendanceRuleEntity::getRuleScope, "GLOBAL")
                .orderByAsc(AttendanceRuleEntity::getExecutionOrder)
                .orderByAsc(AttendanceRuleEntity::getRulePriority);
        List<AttendanceRuleEntity> globalRules = attendanceRuleDao.selectList(globalQueryWrapper);

        // 合并规则（全局规则 + 部门规则）
        List<AttendanceRuleEntity> allRules = new java.util.ArrayList<>(globalRules);
        allRules.addAll(deptRules);

        log.info("[考勤规则] 查询部门规则成功: departmentId={}, count={}", departmentId, allRules.size());
        return allRules.stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询规则详情
     *
     * @param ruleId 规则ID
     * @return 规则详情
     */
    @Override
    public AttendanceRuleVO getRuleDetail(Long ruleId) {
        log.info("[考勤规则] 查询规则详情: ruleId={}", ruleId);

        AttendanceRuleEntity entity = attendanceRuleDao.selectById(ruleId);
        if (entity == null) {
            log.warn("[考勤规则] 规则不存在: ruleId={}", ruleId);
            throw new BusinessException("RULE_NOT_FOUND", "考勤规则不存在");
        }

        log.info("[考勤规则] 查询规则详情成功: ruleId={}", ruleId);
        return convertToVO(entity);
    }

    /**
     * 更新考勤规则
     *
     * @param ruleId 规则ID
     * @param updateForm 更新表单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(Long ruleId, AttendanceRuleUpdateForm updateForm) {
        log.info("[考勤规则] 更新规则: ruleId={}, ruleName={}", ruleId, updateForm.getRuleName());

        AttendanceRuleEntity entity = attendanceRuleDao.selectById(ruleId);
        if (entity == null) {
            log.warn("[考勤规则] 规则不存在: ruleId={}", ruleId);
            throw new BusinessException("RULE_NOT_FOUND", "考勤规则不存在");
        }

        // 更新字段
        entity.setRuleName(updateForm.getRuleName());
        entity.setRuleCategory(updateForm.getRuleCategory());
        entity.setRuleType(updateForm.getRuleType());
        entity.setRuleCondition(updateForm.getRuleCondition());
        entity.setRuleAction(updateForm.getRuleAction());
        entity.setRulePriority(updateForm.getRulePriority());
        entity.setEffectiveStartTime(updateForm.getEffectiveStartTime());
        entity.setEffectiveEndTime(updateForm.getEffectiveEndTime());
        entity.setEffectiveDays(updateForm.getEffectiveDays());
        entity.setDepartmentIds(updateForm.getDepartmentIds());
        entity.setUserIds(updateForm.getUserIds());
        entity.setRuleStatus(updateForm.getRuleStatus());
        entity.setRuleScope(updateForm.getRuleScope());
        entity.setExecutionOrder(updateForm.getExecutionOrder());
        entity.setDescription(updateForm.getDescription());
        entity.setSortOrder(updateForm.getSortOrder());

        int result = attendanceRuleDao.updateById(entity);
        if (result <= 0) {
            log.error("[考勤规则] 更新规则失败: ruleId={}", ruleId);
            throw new BusinessException("UPDATE_FAILED", "更新考勤规则失败");
        }

        log.info("[考勤规则] 更新规则成功: ruleId={}", ruleId);
    }

    /**
     * 分页查询考勤规则
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @Override
    public PageResult<AttendanceRuleVO> queryRulePage(AttendanceRuleQueryForm queryForm) {
        log.info("[考勤规则] 分页查询规则: queryForm={}", queryForm);

        // 构建查询条件
        LambdaQueryWrapper<AttendanceRuleEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 规则名称模糊查询
        if (queryForm.getRuleName() != null && !queryForm.getRuleName().isEmpty()) {
            queryWrapper.like(AttendanceRuleEntity::getRuleName, queryForm.getRuleName());
        }

        // 规则分类
        if (queryForm.getRuleCategory() != null && !queryForm.getRuleCategory().isEmpty()) {
            queryWrapper.eq(AttendanceRuleEntity::getRuleCategory, queryForm.getRuleCategory());
        }

        // 规则状态
        if (queryForm.getRuleStatus() != null) {
            queryWrapper.eq(AttendanceRuleEntity::getRuleStatus, queryForm.getRuleStatus());
        }

        // 规则作用域
        if (queryForm.getRuleScope() != null && !queryForm.getRuleScope().isEmpty()) {
            queryWrapper.eq(AttendanceRuleEntity::getRuleScope, queryForm.getRuleScope());
        }

        // 排序
        queryWrapper.orderByAsc(AttendanceRuleEntity::getSortOrder)
                .orderByAsc(AttendanceRuleEntity::getRulePriority)
                .orderByDesc(AttendanceRuleEntity::getCreateTime);

        // 分页查询
        Page<AttendanceRuleEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        Page<AttendanceRuleEntity> resultPage = attendanceRuleDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<AttendanceRuleVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());

        PageResult<AttendanceRuleVO> pageResult = PageResult.of(voList, resultPage.getTotal(),
                queryForm.getPageNum(), queryForm.getPageSize());

        log.info("[考勤规则] 分页查询规则成功: total={}, pages={}", pageResult.getTotal(), pageResult.getPages());
        return pageResult;
    }

    /**
     * 创建考勤规则
     *
     * @param addForm 新增表单
     * @return 规则ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(AttendanceRuleAddForm addForm) {
        log.info("[考勤规则] 创建规则: ruleName={}, ruleScope={}", addForm.getRuleName(), addForm.getRuleScope());

        // 检查规则名称是否重复
        LambdaQueryWrapper<AttendanceRuleEntity> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AttendanceRuleEntity::getRuleName, addForm.getRuleName());
        Long count = attendanceRuleDao.selectCount(checkWrapper);
        if (count > 0) {
            log.warn("[考勤规则] 规则名称已存在: ruleName={}", addForm.getRuleName());
            throw new BusinessException("RULE_NAME_EXISTS", "规则名称已存在");
        }

        // 创建实体（ID由MyBatis-Plus自动生成）
        AttendanceRuleEntity entity = new AttendanceRuleEntity();
        entity.setRuleCode(generateRuleCode(addForm.getRuleCategory(), addForm.getRuleType()));
        entity.setRuleName(addForm.getRuleName());
        entity.setRuleCategory(addForm.getRuleCategory());
        entity.setRuleType(addForm.getRuleType());
        entity.setRuleCondition(addForm.getRuleCondition());
        entity.setRuleAction(addForm.getRuleAction());
        entity.setRulePriority(addForm.getRulePriority() != null ? addForm.getRulePriority() : 1);
        entity.setEffectiveStartTime(addForm.getEffectiveStartTime());
        entity.setEffectiveEndTime(addForm.getEffectiveEndTime());
        entity.setEffectiveDays(addForm.getEffectiveDays());
        entity.setDepartmentIds(addForm.getDepartmentIds());
        entity.setUserIds(addForm.getUserIds());
        entity.setRuleStatus(addForm.getRuleStatus());
        entity.setRuleScope(addForm.getRuleScope());
        entity.setExecutionOrder(addForm.getExecutionOrder() != null ? addForm.getExecutionOrder() : 1);
        entity.setRuleVersion("1");
        entity.setDescription(addForm.getDescription());
        entity.setSortOrder(addForm.getSortOrder() != null ? addForm.getSortOrder() : 1);

        int result = attendanceRuleDao.insert(entity);
        if (result <= 0) {
            log.error("[考勤规则] 创建规则失败: ruleName={}", addForm.getRuleName());
            throw new BusinessException("CREATE_FAILED", "创建考勤规则失败");
        }

        log.info("[考勤规则] 创建规则成功: ruleId={}, ruleName={}", entity.getRuleId(), entity.getRuleName());
        return entity.getRuleId();
    }

    /**
     * 删除考勤规则
     *
     * @param ruleId 规则ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long ruleId) {
        log.info("[考勤规则] 删除规则: ruleId={}", ruleId);

        AttendanceRuleEntity entity = attendanceRuleDao.selectById(ruleId);
        if (entity == null) {
            log.warn("[考勤规则] 规则不存在: ruleId={}", ruleId);
            throw new BusinessException("RULE_NOT_FOUND", "考勤规则不存在");
        }

        int result = attendanceRuleDao.deleteById(ruleId);
        if (result <= 0) {
            log.error("[考勤规则] 删除规则失败: ruleId={}", ruleId);
            throw new BusinessException("DELETE_FAILED", "删除考勤规则失败");
        }

        log.info("[考勤规则] 删除规则成功: ruleId={}", ruleId);
    }

    /**
     * 批量删除考勤规则
     *
     * @param ruleIds 规则ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRules(List<Long> ruleIds) {
        log.info("[考勤规则] 批量删除规则: count={}", ruleIds.size());

        if (ruleIds == null || ruleIds.isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "规则ID列表不能为空");
        }

        int result = attendanceRuleDao.deleteBatchIds(ruleIds);
        if (result <= 0) {
            log.error("[考勤规则] 批量删除规则失败: count={}", ruleIds.size());
            throw new BusinessException("DELETE_FAILED", "批量删除考勤规则失败");
        }

        log.info("[考勤规则] 批量删除规则成功: deletedCount={}", result);
    }

    /**
     * 生成规则编码
     *
     * @param ruleCategory 规则分类
     * @param ruleType 规则类型
     * @return 规则编码
     */
    private String generateRuleCode(String ruleCategory, String ruleType) {
        String prefix = ruleCategory.substring(0, 1).toUpperCase() + "_";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + ruleType + "_" + timestamp.substring(timestamp.length() - 6);
    }

    /**
     * 实体转换为VO
     *
     * @param entity 实体对象
     * @return 视图对象
     */
    private AttendanceRuleVO convertToVO(AttendanceRuleEntity entity) {
        return AttendanceRuleVO.builder()
                .ruleId(entity.getRuleId())
                .ruleCode(entity.getRuleCode())
                .ruleName(entity.getRuleName())
                .ruleCategory(entity.getRuleCategory())
                .ruleType(entity.getRuleType())
                .ruleCondition(entity.getRuleCondition())
                .ruleAction(entity.getRuleAction())
                .rulePriority(entity.getRulePriority())
                .effectiveStartTime(entity.getEffectiveStartTime())
                .effectiveEndTime(entity.getEffectiveEndTime())
                .effectiveDays(entity.getEffectiveDays())
                .departmentIds(entity.getDepartmentIds())
                .userIds(entity.getUserIds())
                .ruleStatus(entity.getRuleStatus())
                .ruleScope(entity.getRuleScope())
                .executionOrder(entity.getExecutionOrder())
                .parentRuleId(entity.getParentRuleId())
                .ruleVersion(entity.getRuleVersion())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
