package net.lab1024.sa.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessInterlockRuleDao;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleAddForm;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleQueryForm;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessInterlockRuleVO;
import net.lab1024.sa.common.entity.access.AccessInterlockRuleEntity;
import net.lab1024.sa.access.service.AccessInterlockService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.util.SmartBeanUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 门禁互锁规则服务实现类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Slf4j
@Service
public class AccessInterlockServiceImpl implements AccessInterlockService {

    @Resource
    private AccessInterlockRuleDao interlockRuleDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 互锁状态缓存（areaId -> locked状态）
     */
    private final Map<Long, Boolean> interlockStateCache = new ConcurrentHashMap<>();

    /**
     * 异步执行线程池
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public PageResult<AccessInterlockRuleVO> queryPage(AccessInterlockRuleQueryForm queryForm) {
        log.info("[互锁管理] 分页查询互锁规则: queryForm={}", queryForm);

        Page<AccessInterlockRuleEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        LambdaQueryWrapper<AccessInterlockRuleEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (queryForm.getRuleName() != null) {
            queryWrapper.like(AccessInterlockRuleEntity::getRuleName, queryForm.getRuleName());
        }
        if (queryForm.getInterlockMode() != null) {
            queryWrapper.eq(AccessInterlockRuleEntity::getInterlockMode, queryForm.getInterlockMode());
        }
        if (queryForm.getUnlockCondition() != null) {
            queryWrapper.eq(AccessInterlockRuleEntity::getUnlockCondition, queryForm.getUnlockCondition());
        }
        if (queryForm.getEnabled() != null) {
            queryWrapper.eq(AccessInterlockRuleEntity::getEnabled, queryForm.getEnabled());
        }

        queryWrapper.orderByAsc(AccessInterlockRuleEntity::getPriority)
                .orderByDesc(AccessInterlockRuleEntity::getCreateTime);

        Page<AccessInterlockRuleEntity> pageResult = interlockRuleDao.selectPage(page, queryWrapper);

        // Entity转VO
        List<AccessInterlockRuleVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
    }

    @Override
    public AccessInterlockRuleVO getById(Long ruleId) {
        log.info("[互锁管理] 获取规则详情: ruleId={}", ruleId);
        AccessInterlockRuleEntity entity = interlockRuleDao.selectById(ruleId);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRule(AccessInterlockRuleAddForm addForm) {
        log.info("[互锁管理] 新增互锁规则: addForm={}", addForm);

        AccessInterlockRuleEntity entity = SmartBeanUtil.copy(addForm, AccessInterlockRuleEntity.class);
        entity.setEnabled(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        interlockRuleDao.insert(entity);
        log.info("[互锁管理] 新增互锁规则成功: ruleId={}", entity.getRuleId());
        return entity.getRuleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRule(Long ruleId, AccessInterlockRuleUpdateForm updateForm) {
        log.info("[互锁管理] 更新互锁规则: ruleId={}, updateForm={}", ruleId, updateForm);

        AccessInterlockRuleEntity entity = interlockRuleDao.selectById(ruleId);
        if (entity == null) {
            throw new BusinessException("RULE_NOT_FOUND", "互锁规则不存在");
        }

        AccessInterlockRuleEntity updateEntity = SmartBeanUtil.copy(updateForm, AccessInterlockRuleEntity.class);
        entity.setRuleName(updateEntity.getRuleName());
        entity.setRuleCode(updateEntity.getRuleCode());
        entity.setAreaAId(updateEntity.getAreaAId());
        entity.setAreaBId(updateEntity.getAreaBId());
        entity.setInterlockMode(updateEntity.getInterlockMode());
        entity.setAreaADoorIds(updateEntity.getAreaADoorIds());
        entity.setAreaBDoorIds(updateEntity.getAreaBDoorIds());
        entity.setUnlockCondition(updateEntity.getUnlockCondition());
        entity.setUnlockDelaySeconds(updateEntity.getUnlockDelaySeconds());
        entity.setPriority(updateEntity.getPriority());
        entity.setDescription(updateEntity.getDescription());
        entity.setUpdateTime(LocalDateTime.now());

        int rows = interlockRuleDao.updateById(entity);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRule(Long ruleId) {
        log.info("[互锁管理] 删除互锁规则: ruleId={}", ruleId);

        AccessInterlockRuleEntity entity = interlockRuleDao.selectById(ruleId);
        if (entity == null) {
            throw new BusinessException("RULE_NOT_FOUND", "互锁规则不存在");
        }

        int rows = interlockRuleDao.deleteById(ruleId);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEnabled(Long ruleId, Integer enabled) {
        log.info("[互锁管理] 更新启用状态: ruleId={}, enabled={}", ruleId, enabled);

        AccessInterlockRuleEntity entity = interlockRuleDao.selectById(ruleId);
        if (entity == null) {
            throw new BusinessException("RULE_NOT_FOUND", "互锁规则不存在");
        }

        entity.setEnabled(enabled);
        entity.setUpdateTime(LocalDateTime.now());

        int rows = interlockRuleDao.updateById(entity);
        return rows > 0;
    }

    @Override
    public String triggerInterlock(Long areaId, Long doorId, String action) {
        log.info("[互锁管理] 触发互锁: areaId={}, doorId={}, action={}", areaId, doorId, action);

        // 查询所有启用的互锁规则
        List<AccessInterlockRuleEntity> rules = interlockRuleDao.selectEnabledRulesByArea(areaId);
        if (rules.isEmpty()) {
            return "未找到匹配的互锁规则";
        }

        // 异步执行互锁
        for (AccessInterlockRuleEntity rule : rules) {
            CompletableFuture.runAsync(() -> executeInterlock(rule, areaId, action), executorService);
        }

        return String.format("已触发%d条互锁规则", rules.size());
    }

    @Override
    public Boolean manualUnlock(Long ruleId, Long areaId) {
        log.info("[互锁管理] 手动解锁: ruleId={}, areaId={}", ruleId, areaId);

        AccessInterlockRuleEntity rule = interlockRuleDao.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException("RULE_NOT_FOUND", "互锁规则不存在");
        }

        // 解锁目标区域
        Long targetAreaId = (rule.getAreaAId().equals(areaId)) ? rule.getAreaBId() : rule.getAreaAId();
        Long targetDoorId = (rule.getAreaAId().equals(areaId)) ? rule.getDoorBId() : rule.getDoorAId();

        interlockStateCache.put(targetAreaId, false);

        // 发送解锁命令
        Map<String, Object> command = new HashMap<>();
        command.put("areaId", targetAreaId);
        command.put("doorId", targetDoorId);
        command.put("action", "UNLOCK");
        command.put("source", "INTERLOCK_MANUAL");

        try {
            gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/comm/control",
                    org.springframework.http.HttpMethod.POST,
                    command,
                    String.class
            );
            return true;
        } catch (Exception e) {
            log.error("[互锁管理] 解锁失败: targetAreaId={}, error={}", targetAreaId, e.getMessage(), e);
            throw new BusinessException("UNLOCK_FAILED", "解锁失败: " + e.getMessage());
        }
    }

    @Override
    public String testRule(Long ruleId) {
        log.info("[互锁管理] 测试互锁规则: ruleId={}", ruleId);

        AccessInterlockRuleEntity rule = interlockRuleDao.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException("RULE_NOT_FOUND", "互锁规则不存在");
        }

        // 模拟触发互锁
        String result = executeTestInterlock(rule);

        return result;
    }

    /**
     * 执行互锁规则
     */
    private void executeInterlock(AccessInterlockRuleEntity rule, Long triggerAreaId, String triggerAction) {
        log.info("[互锁管理] 执行互锁规则: ruleId={}, triggerAreaId={}", rule.getRuleId(), triggerAreaId);

        try {
            // 确定目标区域
            Long targetAreaId = rule.getAreaAId().equals(triggerAreaId) ? rule.getAreaBId() : rule.getAreaAId();
            Long targetDoorId = rule.getAreaAId().equals(triggerAreaId) ? rule.getDoorBId() : rule.getDoorAId();

            // 如果是单向互锁，检查触发方向
            if ("UNIDIRECTIONAL".equals(rule.getInterlockMode())) {
                if (!rule.getAreaAId().equals(triggerAreaId)) {
                    log.info("[互锁管理] 单向互锁，跳过: ruleId={}, triggerAreaId={}", rule.getRuleId(), triggerAreaId);
                    return;
                }
            }

            // 锁定目标区域
            interlockStateCache.put(targetAreaId, true);

            // 发送锁定命令
            Map<String, Object> command = new HashMap<>();
            command.put("areaId", targetAreaId);
            command.put("doorId", targetDoorId);
            command.put("action", "LOCK");
            command.put("source", "INTERLOCK");
            command.put("ruleId", rule.getRuleId());

            gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/comm/control",
                    org.springframework.http.HttpMethod.POST,
                    command,
                    String.class
            );

            log.info("[互锁管理] 互锁执行成功: ruleId={}, targetAreaId={}", rule.getRuleId(), targetAreaId);

            // 如果是定时解锁，安排解锁任务
            if ("TIMER".equals(rule.getUnlockCondition()) && rule.getUnlockDelaySeconds() != null && rule.getUnlockDelaySeconds() > 0) {
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(rule.getUnlockDelaySeconds() * 1000L);
                        unlockArea(rule, targetAreaId, targetDoorId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, executorService);
            }

        } catch (Exception e) {
            log.error("[互锁管理] 互锁执行失败: ruleId={}, error={}", rule.getRuleId(), e.getMessage(), e);
        }
    }

    /**
     * 解锁区域
     */
    private void unlockArea(AccessInterlockRuleEntity rule, Long areaId, Long doorId) {
        log.info("[互锁管理] 自动解锁: ruleId={}, areaId={}", rule.getRuleId(), areaId);

        interlockStateCache.put(areaId, false);

        Map<String, Object> command = new HashMap<>();
        command.put("areaId", areaId);
        command.put("doorId", doorId);
        command.put("action", "UNLOCK");
        command.put("source", "INTERLOCK_AUTO");

        try {
            gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/comm/control",
                    org.springframework.http.HttpMethod.POST,
                    command,
                    String.class
            );
        } catch (Exception e) {
            log.error("[互锁管理] 自动解锁失败: areaId={}, error={}", areaId, e.getMessage(), e);
        }
    }

    /**
     * 测试互锁规则执行
     */
    private String executeTestInterlock(AccessInterlockRuleEntity rule) {
        try {
            // 模拟锁定区域A
            interlockStateCache.put(rule.getAreaBId(), true);

            return String.format("测试成功: 已模拟锁定区域B(ID:%d)，解锁条件=%s",
                    rule.getAreaBId(), rule.getUnlockCondition());
        } catch (Exception e) {
            log.error("[互锁管理] 测试失败: ruleId={}, error={}", rule.getRuleId(), e.getMessage(), e);
            return "测试失败: " + e.getMessage();
        }
    }

    /**
     * Entity转VO
     */
    private AccessInterlockRuleVO convertToVO(AccessInterlockRuleEntity entity) {
        AccessInterlockRuleVO vo = new AccessInterlockRuleVO();
        vo.setRuleId(entity.getRuleId());
        vo.setRuleName(entity.getRuleName());
        vo.setRuleCode(entity.getRuleCode());
        vo.setRuleType(entity.getRuleType());
        vo.setAreaAId(entity.getAreaAId());
        vo.setAreaAName(entity.getAreaAName());
        vo.setDoorAId(entity.getDoorAId());
        vo.setAreaBId(entity.getAreaBId());
        vo.setAreaBName(entity.getAreaBName());
        vo.setDoorBId(entity.getDoorBId());
        vo.setInterlockMode(entity.getInterlockMode());
        vo.setUnlockCondition(entity.getUnlockCondition());
        vo.setUnlockDelaySeconds(entity.getUnlockDelaySeconds());
        vo.setEnabled(entity.getEnabled());
        vo.setPriority(entity.getPriority());
        vo.setDescription(entity.getDescription());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
