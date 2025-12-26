package net.lab1024.sa.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AlertRuleDao;
import net.lab1024.sa.common.entity.access.AlertRuleEntity;
import net.lab1024.sa.access.domain.form.AlertRuleForm;
import net.lab1024.sa.access.domain.form.AlertRuleQueryForm;
import net.lab1024.sa.access.domain.vo.AlertRuleVO;
import net.lab1024.sa.access.service.AlertRuleService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 告警规则服务实现类
 * <p>
 * 提供告警规则的完整生命周期管理：
 * - 规则CRUD操作
 * - 规则启用/禁用
 * - 规则表达式验证
 * - Aviator表达式集成
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AlertRuleServiceImpl implements AlertRuleService {

    @Resource
    private AlertRuleDao alertRuleDao;

    /**
     * 创建告警规则
     */
    @Override
    public ResponseDTO<Long> createRule(AlertRuleForm ruleForm) {
        log.info("[告警规则] 创建告警规则: ruleName={}, ruleCode={}, ruleType={}",
                ruleForm.getRuleName(), ruleForm.getRuleCode(), ruleForm.getRuleType());

        // 1. 验证规则编码是否已存在
        LambdaQueryWrapper<AlertRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlertRuleEntity::getRuleCode, ruleForm.getRuleCode());
        Long count = alertRuleDao.selectCount(queryWrapper);
        if (count > 0) {
            log.warn("[告警规则] 规则编码已存在: ruleCode={}", ruleForm.getRuleCode());
            throw new BusinessException("RULE_CODE_EXISTS", "规则编码已存在");
        }

        // 2. 验证规则表达式（如果有）
        if (ruleForm.getConditionExpression() != null && !ruleForm.getConditionExpression().isEmpty()) {
            ResponseDTO<Boolean> validation = validateRuleExpression(ruleForm);
            if (!validation.getData()) {
                log.warn("[告警规则] 规则表达式验证失败: ruleCode={}, expression={}",
                        ruleForm.getRuleCode(), ruleForm.getConditionExpression());
                throw new BusinessException("INVALID_EXPRESSION", "规则表达式无效");
            }
        }

        // 3. 创建规则实体
        AlertRuleEntity entity = new AlertRuleEntity();
        BeanUtils.copyProperties(ruleForm, entity);

        // 设置默认值
        if (entity.getEnabled() == null) {
            entity.setEnabled(1);
        }
        if (entity.getPriority() == null) {
            entity.setPriority(0);
        }
        if (entity.getAlertAggregationEnabled() == null) {
            entity.setAlertAggregationEnabled(0);
        }
        if (entity.getAlertEscalationEnabled() == null) {
            entity.setAlertEscalationEnabled(0);
        }

        // 4. 保存到数据库
        int insertCount = alertRuleDao.insert(entity);
        if (insertCount <= 0) {
            log.error("[告警规则] 创建规则失败: ruleName={}", ruleForm.getRuleName());
            throw new BusinessException("CREATE_RULE_FAILED", "创建规则失败");
        }

        log.info("[告警规则] 创建规则成功: ruleId={}, ruleName={}", entity.getRuleId(), entity.getRuleName());
        return ResponseDTO.ok(entity.getRuleId());
    }

    /**
     * 更新告警规则
     */
    @Override
    public ResponseDTO<Void> updateRule(AlertRuleForm ruleForm) {
        log.info("[告警规则] 更新告警规则: ruleId={}, ruleName={}",
                ruleForm.getRuleId(), ruleForm.getRuleName());

        // 1. 验证规则是否存在
        AlertRuleEntity existingRule = alertRuleDao.selectById(ruleForm.getRuleId());
        if (existingRule == null) {
            log.warn("[告警规则] 规则不存在: ruleId={}", ruleForm.getRuleId());
            throw new BusinessException("RULE_NOT_FOUND", "规则不存在");
        }

        // 2. 验证规则编码是否与其他规则冲突
        LambdaQueryWrapper<AlertRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlertRuleEntity::getRuleCode, ruleForm.getRuleCode());
        queryWrapper.ne(AlertRuleEntity::getRuleId, ruleForm.getRuleId());
        Long count = alertRuleDao.selectCount(queryWrapper);
        if (count > 0) {
            log.warn("[告警规则] 规则编码已被其他规则使用: ruleCode={}", ruleForm.getRuleCode());
            throw new BusinessException("RULE_CODE_EXISTS", "规则编码已被其他规则使用");
        }

        // 3. 验证规则表达式（如果有）
        if (ruleForm.getConditionExpression() != null && !ruleForm.getConditionExpression().isEmpty()) {
            ResponseDTO<Boolean> validation = validateRuleExpression(ruleForm);
            if (!validation.getData()) {
                log.warn("[告警规则] 规则表达式验证失败: ruleId={}, expression={}",
                        ruleForm.getRuleId(), ruleForm.getConditionExpression());
                throw new BusinessException("INVALID_EXPRESSION", "规则表达式无效");
            }
        }

        // 4. 更新规则
        AlertRuleEntity entity = new AlertRuleEntity();
        BeanUtils.copyProperties(ruleForm, entity);

        int updateCount = alertRuleDao.updateById(entity);
        if (updateCount <= 0) {
            log.error("[告警规则] 更新规则失败: ruleId={}", ruleForm.getRuleId());
            throw new BusinessException("UPDATE_RULE_FAILED", "更新规则失败");
        }

        log.info("[告警规则] 更新规则成功: ruleId={}", ruleForm.getRuleId());
        return ResponseDTO.ok();
    }

    /**
     * 删除告警规则
     */
    @Override
    public ResponseDTO<Void> deleteRule(Long ruleId) {
        log.info("[告警规则] 删除告警规则: ruleId={}", ruleId);

        // 1. 验证规则是否存在
        AlertRuleEntity existingRule = alertRuleDao.selectById(ruleId);
        if (existingRule == null) {
            log.warn("[告警规则] 规则不存在: ruleId={}", ruleId);
            throw new BusinessException("RULE_NOT_FOUND", "规则不存在");
        }

        // 2. 逻辑删除规则
        int deleteCount = alertRuleDao.deleteById(ruleId);
        if (deleteCount <= 0) {
            log.error("[告警规则] 删除规则失败: ruleId={}", ruleId);
            throw new BusinessException("DELETE_RULE_FAILED", "删除规则失败");
        }

        log.info("[告警规则] 删除规则成功: ruleId={}", ruleId);
        return ResponseDTO.ok();
    }

    /**
     * 查询告警规则详情
     */
    @Override
    public ResponseDTO<AlertRuleVO> getRule(Long ruleId) {
        log.debug("[告警规则] 查询规则详情: ruleId={}", ruleId);

        AlertRuleEntity entity = alertRuleDao.selectById(ruleId);
        if (entity == null) {
            log.warn("[告警规则] 规则不存在: ruleId={}", ruleId);
            throw new BusinessException("RULE_NOT_FOUND", "规则不存在");
        }

        AlertRuleVO vo = convertToVO(entity);
        return ResponseDTO.ok(vo);
    }

    /**
     * 分页查询告警规则列表
     */
    @Override
    public ResponseDTO<PageResult<AlertRuleVO>> queryRules(AlertRuleQueryForm queryForm) {
        log.debug("[告警规则] 查询规则列表: ruleType={}, enabled={}, pageNum={}, pageSize={}",
                queryForm.getRuleType(), queryForm.getEnabled(), queryForm.getPageNum(), queryForm.getPageSize());

        // 1. 构建查询条件
        LambdaQueryWrapper<AlertRuleEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (queryForm.getRuleName() != null && !queryForm.getRuleName().isEmpty()) {
            queryWrapper.like(AlertRuleEntity::getRuleName, queryForm.getRuleName());
        }
        if (queryForm.getRuleType() != null && !queryForm.getRuleType().isEmpty()) {
            queryWrapper.eq(AlertRuleEntity::getRuleType, queryForm.getRuleType());
        }
        if (queryForm.getAlertLevel() != null) {
            queryWrapper.eq(AlertRuleEntity::getAlertLevel, queryForm.getAlertLevel());
        }
        if (queryForm.getEnabled() != null) {
            queryWrapper.eq(AlertRuleEntity::getEnabled, queryForm.getEnabled());
        }

        // 按优先级降序、创建时间降序排序
        queryWrapper.orderByDesc(AlertRuleEntity::getPriority)
                .orderByDesc(AlertRuleEntity::getCreateTime);

        // 2. 分页查询
        Page<AlertRuleEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        Page<AlertRuleEntity> resultPage = alertRuleDao.selectPage(page, queryWrapper);

        // 3. 转换为VO
        List<AlertRuleVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<AlertRuleVO> pageResult = PageResult.of(voList, resultPage.getTotal(),
                queryForm.getPageNum(), queryForm.getPageSize());

        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询所有启用的规则（按优先级排序）
     */
    @Override
    public ResponseDTO<List<AlertRuleVO>> queryEnabledRules() {
        log.debug("[告警规则] 查询启用的规则列表");

        List<AlertRuleEntity> entities = alertRuleDao.selectEnabledRules();
        List<AlertRuleVO> voList = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(voList);
    }

    /**
     * 启用/禁用告警规则
     */
    @Override
    public ResponseDTO<Void> toggleRule(Long ruleId, Integer enabled) {
        log.info("[告警规则] 切换规则状态: ruleId={}, enabled={}", ruleId, enabled);

        // 1. 验证规则是否存在
        AlertRuleEntity existingRule = alertRuleDao.selectById(ruleId);
        if (existingRule == null) {
            log.warn("[告警规则] 规则不存在: ruleId={}", ruleId);
            throw new BusinessException("RULE_NOT_FOUND", "规则不存在");
        }

        // 2. 更新启用状态 - 使用updateById避免单元测试中LambdaUpdateWrapper的lambda cache问题
        existingRule.setEnabled(enabled);
        int updateCount = alertRuleDao.updateById(existingRule);
        if (updateCount <= 0) {
            log.error("[告警规则] 更新规则状态失败: ruleId={}", ruleId);
            throw new BusinessException("UPDATE_RULE_FAILED", "更新规则状态失败");
        }

        log.info("[告警规则] 切换规则状态成功: ruleId={}, enabled={}", ruleId, enabled);
        return ResponseDTO.ok();
    }

    /**
     * 验证规则表达式
     */
    @Override
    public ResponseDTO<Boolean> validateRuleExpression(AlertRuleForm ruleForm) {
        log.debug("[告警规则] 验证规则表达式: conditionType={}, expression={}",
                ruleForm.getConditionType(), ruleForm.getConditionExpression());

        try {
            // 1. 简单条件（条件类型=1）- 直接验证通过
            if (ruleForm.getConditionType() == 1) {
                return ResponseDTO.ok(true);
            }

            // 2. Aviator表达式（条件类型=2）- 使用Aviator引擎验证
            if (ruleForm.getConditionType() == 2 && ruleForm.getConditionExpression() != null) {
                // TODO: 集成Aviator表达式引擎进行验证
                // 这里暂时返回true，实际应该使用Aviator.validateExpression()
                return ResponseDTO.ok(true);
            }

            // 3. 脚本条件（条件类型=3）- 需要脚本引擎验证
            if (ruleForm.getConditionType() == 3) {
                // TODO: 集成脚本引擎（如Groovy、JavaScript）进行验证
                return ResponseDTO.ok(true);
            }

            return ResponseDTO.ok(false);
        } catch (Exception e) {
            log.error("[告警规则] 规则表达式验证异常: expression={}, error={}",
                    ruleForm.getConditionExpression(), e.getMessage(), e);
            return ResponseDTO.error("EXPRESSION_VALIDATION_ERROR", "规则表达式验证失败: " + e.getMessage());
        }
    }

    /**
     * 批量启用/禁用告警规则
     */
    @Override
    public ResponseDTO<Void> batchToggleRules(List<Long> ruleIds, Integer enabled) {
        log.info("[告警规则] 批量切换规则状态: ruleIds={}, enabled={}", ruleIds, enabled);

        if (ruleIds == null || ruleIds.isEmpty()) {
            throw new BusinessException("RULE_IDS_EMPTY", "规则ID列表不能为空");
        }

        // 批量更新
        LambdaUpdateWrapper<AlertRuleEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(AlertRuleEntity::getRuleId, ruleIds)
                .set(AlertRuleEntity::getEnabled, enabled);

        int updateCount = alertRuleDao.update(null, updateWrapper);
        log.info("[告警规则] 批量切换规则状态成功: count={}", updateCount);

        return ResponseDTO.ok();
    }

    /**
     * 将Entity转换为VO
     */
    private AlertRuleVO convertToVO(AlertRuleEntity entity) {
        return AlertRuleVO.builder()
                .ruleId(entity.getRuleId())
                .ruleName(entity.getRuleName())
                .ruleCode(entity.getRuleCode())
                .ruleType(entity.getRuleType())
                .ruleTypeName(getRuleTypeName(entity.getRuleType()))
                .conditionType(entity.getConditionType())
                .conditionTypeName(getConditionTypeName(entity.getConditionType()))
                .conditionExpression(entity.getConditionExpression())
                .conditionConfig(entity.getConditionConfig())
                .alertLevel(entity.getAlertLevel())
                .alertLevelName(getAlertLevelName(entity.getAlertLevel()))
                .alertTitleTemplate(entity.getAlertTitleTemplate())
                .alertMessageTemplate(entity.getAlertMessageTemplate())
                .notificationMethods(entity.getNotificationMethods())
                .notificationRecipients(entity.getNotificationRecipients())
                .notificationTemplate(entity.getNotificationTemplate())
                .alertAggregationEnabled(entity.getAlertAggregationEnabled())
                .aggregationWindowSeconds(entity.getAggregationWindowSeconds())
                .alertEscalationEnabled(entity.getAlertEscalationEnabled())
                .escalationRules(entity.getEscalationRules())
                .enabled(entity.getEnabled())
                .priority(entity.getPriority())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 获取规则类型名称
     */
    private String getRuleTypeName(String ruleType) {
        if (ruleType == null) {
            return "未知";
        }
        switch (ruleType) {
            case "DEVICE_OFFLINE":
                return "设备离线";
            case "DEVICE_FAULT":
                return "设备故障";
            case "TEMP_HIGH":
                return "温度过高";
            case "TEMP_LOW":
                return "温度过低";
            case "NETWORK_ERROR":
                return "网络异常";
            case "STORAGE_LOW":
                return "存储空间不足";
            case "POWER_LOW":
                return "电量不足";
            case "AUTH_FAILED":
                return "认证失败";
            case "DEVICE_BLOCKED":
                return "设备被阻挡";
            default:
                return "其他";
        }
    }

    /**
     * 获取条件类型名称
     */
    private String getConditionTypeName(Integer conditionType) {
        if (conditionType == null) {
            return "未知";
        }
        switch (conditionType) {
            case 1:
                return "简单条件";
            case 2:
                return "Aviator表达式";
            case 3:
                return "脚本条件";
            default:
                return "未知";
        }
    }

    /**
     * 获取告警级别名称
     */
    private String getAlertLevelName(Integer alertLevel) {
        if (alertLevel == null) {
            return "未知";
        }
        switch (alertLevel) {
            case 1:
                return "低";
            case 2:
                return "中";
            case 3:
                return "高";
            case 4:
                return "紧急";
            default:
                return "未知";
        }
    }
}
