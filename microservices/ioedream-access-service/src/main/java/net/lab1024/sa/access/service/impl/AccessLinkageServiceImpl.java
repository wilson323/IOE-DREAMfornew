package net.lab1024.sa.access.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessLinkageLogDao;
import net.lab1024.sa.access.dao.AccessLinkageRuleDao;
import net.lab1024.sa.common.entity.access.AccessLinkageLogEntity;
import net.lab1024.sa.common.entity.access.AccessLinkageRuleEntity;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleAddForm;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleQueryForm;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessLinkageRuleVO;
import net.lab1024.sa.access.service.AccessLinkageService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.util.SmartBeanUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 门禁联动规则服务实现类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Slf4j
@Service
public class AccessLinkageServiceImpl implements AccessLinkageService {

    @Resource
    private AccessLinkageRuleDao linkageRuleDao;

    @Resource
    private AccessLinkageLogDao linkageLogDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 异步执行线程池
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public PageResult<AccessLinkageRuleVO> queryPage(AccessLinkageRuleQueryForm queryForm) {
        log.info("[联动管理] 分页查询联动规则: queryForm={}", queryForm);

        Page<AccessLinkageRuleEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        LambdaQueryWrapper<AccessLinkageRuleEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 查询条件
        if (queryForm.getRuleName() != null) {
            queryWrapper.like(AccessLinkageRuleEntity::getRuleName, queryForm.getRuleName());
        }
        if (queryForm.getRuleType() != null) {
            queryWrapper.eq(AccessLinkageRuleEntity::getRuleType, queryForm.getRuleType());
        }
        if (queryForm.getEnabled() != null) {
            queryWrapper.eq(AccessLinkageRuleEntity::getEnabled, queryForm.getEnabled());
        }

        queryWrapper.orderByAsc(AccessLinkageRuleEntity::getPriority)
                .orderByDesc(AccessLinkageRuleEntity::getCreateTime);

        Page<AccessLinkageRuleEntity> pageResult = linkageRuleDao.selectPage(page, queryWrapper);

        List<AccessLinkageRuleVO> voList = SmartBeanUtil.copyList(pageResult.getRecords(), AccessLinkageRuleVO.class);

        return PageResult.of(voList, pageResult.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
    }

    @Override
    public AccessLinkageRuleVO getById(Long ruleId) {
        log.info("[联动管理] 查询联动规则详情: ruleId={}", ruleId);

        AccessLinkageRuleEntity entity = linkageRuleDao.selectById(ruleId);
        if (entity == null) {
            throw new BusinessException("RULE_NOT_FOUND", "联动规则不存在");
        }

        return SmartBeanUtil.copy(entity, AccessLinkageRuleVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AccessLinkageRuleAddForm addForm) {
        log.info("[联动管理] 新增联动规则: addForm={}", addForm);

        // 检查规则编码唯一性
        LambdaQueryWrapper<AccessLinkageRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLinkageRuleEntity::getRuleCode, addForm.getRuleCode());
        Long count = linkageRuleDao.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("RULE_CODE_EXISTS", "规则编码已存在");
        }

        AccessLinkageRuleEntity entity = SmartBeanUtil.copy(addForm, AccessLinkageRuleEntity.class);
        entity.setEnabled(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        linkageRuleDao.insert(entity);
        log.info("[联动管理] 联动规则新增成功: ruleId={}", entity.getRuleId());

        return entity.getRuleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long ruleId, AccessLinkageRuleUpdateForm updateForm) {
        log.info("[联动管理] 更新联动规则: ruleId={}, updateForm={}", ruleId, updateForm);

        AccessLinkageRuleEntity entity = linkageRuleDao.selectById(ruleId);
        if (entity == null) {
            throw new BusinessException("RULE_NOT_FOUND", "联动规则不存在");
        }

        AccessLinkageRuleEntity updateEntity = SmartBeanUtil.copy(updateForm, AccessLinkageRuleEntity.class);
        entity.setRuleName(updateEntity.getRuleName());
        entity.setActionType(updateEntity.getActionType());
        entity.setDelaySeconds(updateEntity.getDelaySeconds());
        entity.setConditionExpression(updateEntity.getConditionExpression());
        entity.setPriority(updateEntity.getPriority());
        entity.setDescription(updateEntity.getDescription());
        entity.setUpdateTime(LocalDateTime.now());

        linkageRuleDao.updateById(entity);
        log.info("[联动管理] 联动规则更新成功: ruleId={}", ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long ruleId) {
        log.info("[联动管理] 删除联动规则: ruleId={}", ruleId);

        AccessLinkageRuleEntity entity = linkageRuleDao.selectById(ruleId);
        if (entity == null) {
            throw new BusinessException("RULE_NOT_FOUND", "联动规则不存在");
        }

        linkageRuleDao.deleteById(ruleId);
        log.info("[联动管理] 联动规则删除成功: ruleId={}", ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEnabled(Long ruleId, Integer enabled) {
        log.info("[联动管理] 更新联动规则启用状态: ruleId={}, enabled={}", ruleId, enabled);

        AccessLinkageRuleEntity entity = linkageRuleDao.selectById(ruleId);
        if (entity == null) {
            throw new BusinessException("RULE_NOT_FOUND", "联动规则不存在");
        }

        entity.setEnabled(enabled);
        entity.setUpdateTime(LocalDateTime.now());

        linkageRuleDao.updateById(entity);
        log.info("[联动管理] 联动规则启用状态更新成功: ruleId={}, enabled={}", ruleId, enabled);
    }

    @Override
    public String triggerLinkage(Long triggerDeviceId, Long triggerDoorId, String triggerEvent) {
        log.info("[联动管理] 触发联动: triggerDeviceId={}, triggerDoorId={}, triggerEvent={}",
                triggerDeviceId, triggerDoorId, triggerEvent);

        // 查询匹配的联动规则
        LambdaQueryWrapper<AccessLinkageRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessLinkageRuleEntity::getTriggerDeviceId, triggerDeviceId)
                .eq(AccessLinkageRuleEntity::getEnabled, 1);

        List<AccessLinkageRuleEntity> rules = linkageRuleDao.selectList(queryWrapper);
        if (rules.isEmpty()) {
            log.info("[联动管理] 未找到匹配的联动规则: triggerDeviceId={}", triggerDeviceId);
            return "未找到匹配的联动规则";
        }

        // 异步执行联动
        for (AccessLinkageRuleEntity rule : rules) {
            CompletableFuture.runAsync(() -> executeLinkage(rule, triggerEvent), executorService);
        }

        return String.format("已触发%d条联动规则", rules.size());
    }

    @Override
    public String testRule(Long ruleId) {
        log.info("[联动管理] 测试联动规则: ruleId={}", ruleId);

        AccessLinkageRuleEntity rule = linkageRuleDao.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException("RULE_NOT_FOUND", "联动规则不存在");
        }

        // 执行联动测试
        String result = executeLinkage(rule, "TEST");

        log.info("[联动管理] 联动规则测试完成: ruleId={}, result={}", ruleId, result);
        return result;
    }

    /**
     * 执行联动规则
     *
     * @param rule         联动规则
     * @param triggerEvent 触发事件
     * @return 执行结果
     */
    private String executeLinkage(AccessLinkageRuleEntity rule, String triggerEvent) {
        log.info("[联动管理] 执行联动规则: ruleId={}, targetDeviceId={}, actionType={}",
                rule.getRuleId(), rule.getTargetDeviceId(), rule.getActionType());

        // 创建联动日志
        AccessLinkageLogEntity logEntity = new AccessLinkageLogEntity();
        logEntity.setRuleId(rule.getRuleId());
        logEntity.setTriggerDeviceId(rule.getTriggerDeviceId());
        logEntity.setTriggerDoorId(rule.getTriggerDoorId());
        logEntity.setTriggerEvent(triggerEvent);
        logEntity.setTargetDeviceId(rule.getTargetDeviceId());
        logEntity.setTargetDoorId(rule.getTargetDoorId());
        logEntity.setActionType(rule.getActionType());
        logEntity.setExecutionStatus("PENDING");
        logEntity.setCreateTime(LocalDateTime.now());

        try {
            // 延迟执行
            if (rule.getDelaySeconds() != null && rule.getDelaySeconds() > 0) {
                Thread.sleep(rule.getDelaySeconds() * 1000L);
            }

            // 构建门禁控制命令
            Map<String, Object> command = new HashMap<>();
            command.put("deviceId", rule.getTargetDeviceId());
            command.put("doorId", rule.getTargetDoorId());
            command.put("action", rule.getActionType());
            command.put("source", "LINKAGE");
            command.put("ruleId", rule.getRuleId());

            // 调用设备通讯服务执行开门/关门/锁定/解锁
            String result = gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/comm/control",
                    org.springframework.http.HttpMethod.POST,
                    command,
                    String.class
            );

            logEntity.setExecutionStatus("SUCCESS");
            logEntity.setExecutionTime(LocalDateTime.now());
            logEntity.setExecutionResult(result);

            linkageLogDao.insert(logEntity);

            log.info("[联动管理] 联动执行成功: ruleId={}, result={}", rule.getRuleId(), result);
            return "联动执行成功: " + result;

        } catch (Exception e) {
            log.error("[联动管理] 联动执行失败: ruleId={}, error={}", rule.getRuleId(), e.getMessage(), e);

            logEntity.setExecutionStatus("FAILED");
            logEntity.setExecutionTime(LocalDateTime.now());
            logEntity.setErrorMessage(e.getMessage());

            linkageLogDao.insert(logEntity);

            return "联动执行失败: " + e.getMessage();
        }
    }
}
