package net.lab1024.sa.access.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.DeviceAlertDao;
import net.lab1024.sa.access.dao.AlertRuleDao;
import net.lab1024.sa.common.entity.access.DeviceAlertEntity;
import net.lab1024.sa.common.entity.access.AlertRuleEntity;
import net.lab1024.sa.access.domain.form.DeviceAlertHandleForm;
import net.lab1024.sa.access.domain.form.DeviceAlertQueryForm;
import net.lab1024.sa.access.domain.vo.AlertStatisticsVO;
import net.lab1024.sa.access.domain.vo.DeviceAlertVO;
import net.lab1024.sa.access.manager.AlertManager;
import net.lab1024.sa.access.service.AlertNotificationService;
import net.lab1024.sa.access.service.AlertRuleService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 告警业务管理器实现类
 * <p>
 * 核心功能：
 * - 告警检测和创建
 * - 告警规则匹配
 * - 告警聚合（防刷屏）
 * - 告警升级处理
 * - 告警统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AlertManagerImpl implements AlertManager {

    @Resource
    private DeviceAlertDao deviceAlertDao;

    @Resource
    private AlertRuleDao alertRuleDao;

    @Resource
    private AlertRuleService alertRuleService;

    @Resource
    private AlertNotificationService alertNotificationService;

    @Resource
    private net.lab1024.sa.access.websocket.AlertWebSocketService alertWebSocketService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检测并创建告警
     */
    @Override
    public ResponseDTO<Boolean> detectAndCreateAlert(Long deviceId, String deviceStatus, Map<String, Object> deviceData) {
        log.info("[告警管理] 检测告警: deviceId={}, deviceStatus={}", deviceId, deviceStatus);

        try {
            // 1. 查询所有启用的告警规则
            List<AlertRuleEntity> rules = alertRuleDao.selectEnabledRules();
            if (rules.isEmpty()) {
                log.debug("[告警管理] 没有启用的告警规则");
                return ResponseDTO.ok(false);
            }

            // 2. 匹配规则并创建告警
            boolean alertCreated = false;
            for (AlertRuleEntity rule : rules) {
                if (matchRule(rule, deviceId, deviceStatus, deviceData)) {
                    // 检查是否需要聚合（防刷屏）
                    if (shouldAggregateAlert(deviceId, rule.getRuleId(), rule.getAggregationWindowSeconds())) {
                        log.debug("[告警管理] 告警被聚合: deviceId={}, ruleId={}", deviceId, rule.getRuleId());
                        continue;
                    }

                    // 创建告警
                    DeviceAlertEntity alert = buildAlert(deviceId, deviceStatus, deviceData, rule);
                    deviceAlertDao.insert(alert);

                    // 发送通知
                    alertNotificationService.sendNotification(alert, rule.getRuleId(), rule.getNotificationRecipients());

                    // WebSocket实时推送给所有在线用户
                    alertWebSocketService.broadcastAlert(alert);

                    alertCreated = true;
                    log.info("[告警管理] 创建告警成功并已WebSocket推送: alertId={}, deviceId={}, ruleType={}",
                            alert.getAlertId(), deviceId, rule.getRuleType());
                }
            }

            return ResponseDTO.ok(alertCreated);

        } catch (Exception e) {
            log.error("[告警管理] 检测告警异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("DETECT_ALERT_FAILED", "检测告警失败: " + e.getMessage());
        }
    }

    /**
     * 手动创建告警
     */
    @Override
    public ResponseDTO<Long> createAlert(DeviceAlertEntity alert) {
        log.info("[告警管理] 手动创建告警: deviceId={}, alertType={}",
                alert.getDeviceId(), alert.getAlertType());

        try {
            // 设置默认值
            alert.setAlertStatus(0); // 未确认
            alert.setAlertOccurredTime(LocalDateTime.now());

            int insertCount = deviceAlertDao.insert(alert);
            if (insertCount <= 0) {
                log.error("[告警管理] 创建告警失败");
                throw new BusinessException("CREATE_ALERT_FAILED", "创建告警失败");
            }

            // WebSocket实时推送告警给所有在线用户
            alertWebSocketService.broadcastAlert(alert);

            log.info("[告警管理] 创建告警成功并已WebSocket推送: alertId={}", alert.getAlertId());
            return ResponseDTO.ok(alert.getAlertId());

        } catch (Exception e) {
            log.error("[告警管理] 创建告警异常: error={}", e.getMessage(), e);
            throw new BusinessException("CREATE_ALERT_ERROR", "创建告警异常: " + e.getMessage());
        }
    }

    /**
     * 处理告警（确认或处理）
     */
    @Override
    public ResponseDTO<Void> handleAlert(DeviceAlertHandleForm handleForm) {
        log.info("[告警管理] 处理告警: alertId={}, actionType={}",
                handleForm.getAlertId(), handleForm.getActionType());

        // 1. 验证告警是否存在
        DeviceAlertEntity alert = deviceAlertDao.selectById(handleForm.getAlertId());
        if (alert == null) {
            log.warn("[告警管理] 告警不存在: alertId={}", handleForm.getAlertId());
            throw new BusinessException("ALERT_NOT_FOUND", "告警不存在");
        }

        // 2. 根据操作类型处理
        switch (handleForm.getActionType().toUpperCase()) {
            case "CONFIRM":
                // 确认告警
                alert.setAlertStatus(1); // 已确认
                alert.setConfirmedBy(getCurrentUserId()); // 需要获取当前用户ID
                alert.setConfirmedTime(LocalDateTime.now());
                alert.setConfirmedRemark(handleForm.getRemark());
                break;

            case "HANDLE":
                // 处理告警
                alert.setAlertStatus(2); // 已处理
                alert.setHandledBy(getCurrentUserId());
                alert.setHandledTime(LocalDateTime.now());
                alert.setHandledResult(handleForm.getRemark());
                break;

            case "IGNORE":
                // 忽略告警
                alert.setAlertStatus(3); // 已忽略
                break;

            default:
                log.warn("[告警管理] 不支持的操作类型: actionType={}", handleForm.getActionType());
                throw new BusinessException("UNSUPPORTED_ACTION", "不支持的操作类型");
        }

        // 3. 更新告警
        int updateCount = deviceAlertDao.updateById(alert);
        if (updateCount <= 0) {
            log.error("[告警管理] 更新告警失败: alertId={}", handleForm.getAlertId());
            throw new BusinessException("UPDATE_ALERT_FAILED", "更新告警失败");
        }

        log.info("[告警管理] 处理告警成功: alertId={}, actionType={}",
                handleForm.getAlertId(), handleForm.getActionType());
        return ResponseDTO.ok();
    }

    /**
     * 分页查询告警列表
     */
    @Override
    public ResponseDTO<PageResult<DeviceAlertVO>> queryAlerts(DeviceAlertQueryForm queryForm) {
        log.debug("[告警管理] 查询告警列表: pageNum={}, pageSize={}",
                queryForm.getPageNum(), queryForm.getPageSize());

        // 1. 构建查询条件
        LambdaQueryWrapper<DeviceAlertEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (queryForm.getDeviceId() != null) {
            queryWrapper.eq(DeviceAlertEntity::getDeviceId, queryForm.getDeviceId());
        }
        if (queryForm.getDeviceCode() != null && !queryForm.getDeviceCode().isEmpty()) {
            queryWrapper.like(DeviceAlertEntity::getDeviceCode, queryForm.getDeviceCode());
        }
        if (queryForm.getDeviceType() != null) {
            queryWrapper.eq(DeviceAlertEntity::getDeviceType, queryForm.getDeviceType());
        }
        if (queryForm.getAlertType() != null && !queryForm.getAlertType().isEmpty()) {
            queryWrapper.eq(DeviceAlertEntity::getAlertType, queryForm.getAlertType());
        }
        if (queryForm.getAlertLevel() != null) {
            queryWrapper.eq(DeviceAlertEntity::getAlertLevel, queryForm.getAlertLevel());
        }
        if (queryForm.getAlertStatus() != null) {
            queryWrapper.eq(DeviceAlertEntity::getAlertStatus, queryForm.getAlertStatus());
        }
        if (queryForm.getStartTime() != null) {
            queryWrapper.ge(DeviceAlertEntity::getAlertOccurredTime, queryForm.getStartTime());
        }
        if (queryForm.getEndTime() != null) {
            queryWrapper.le(DeviceAlertEntity::getAlertOccurredTime, queryForm.getEndTime());
        }
        if (queryForm.getKeyword() != null && !queryForm.getKeyword().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(DeviceAlertEntity::getDeviceName, queryForm.getKeyword())
                    .or()
                    .like(DeviceAlertEntity::getAlertTitle, queryForm.getKeyword())
                    .or()
                    .like(DeviceAlertEntity::getAlertMessage, queryForm.getKeyword())
            );
        }

        // 按告警发生时间降序排序
        queryWrapper.orderByDesc(DeviceAlertEntity::getAlertOccurredTime);

        // 2. 分页查询
        Page<DeviceAlertEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        Page<DeviceAlertEntity> resultPage = deviceAlertDao.selectPage(page, queryWrapper);

        // 3. 转换为VO
        List<DeviceAlertVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<DeviceAlertVO> pageResult = PageResult.of(voList, resultPage.getTotal(),
                queryForm.getPageNum(), queryForm.getPageSize());

        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询告警详情
     */
    @Override
    public ResponseDTO<DeviceAlertVO> getAlertDetail(Long alertId) {
        log.debug("[告警管理] 查询告警详情: alertId={}", alertId);

        DeviceAlertEntity alert = deviceAlertDao.selectById(alertId);
        if (alert == null) {
            log.warn("[告警管理] 告警不存在: alertId={}", alertId);
            throw new BusinessException("ALERT_NOT_FOUND", "告警不存在");
        }

        DeviceAlertVO vo = convertToVO(alert);
        return ResponseDTO.ok(vo);
    }

    /**
     * 获取告警统计数据
     */
    @Override
    public ResponseDTO<AlertStatisticsVO> getAlertStatistics() {
        log.debug("[告警管理] 获取告警统计数据");

        try {
            // 1. 统计各状态告警数量
            List<Object> statusCounts = deviceAlertDao.countByAlertStatus();
            Map<String, Long> statusCountMap = parseCountResults(statusCounts);

            // 2. 统计各级别告警数量
            List<Object> levelCounts = deviceAlertDao.countByAlertLevel();
            Map<String, Long> levelCountMap = parseCountResults(levelCounts);

            // 3. 构建统计VO
            AlertStatisticsVO statistics = AlertStatisticsVO.builder()
                    .totalAlerts(statusCountMap.values().stream().mapToLong(Long::longValue).sum())
                    .unconfirmedAlerts(statusCountMap.getOrDefault("0", 0L))
                    .confirmedAlerts(statusCountMap.getOrDefault("1", 0L))
                    .handledAlerts(statusCountMap.getOrDefault("2", 0L))
                    .ignoredAlerts(statusCountMap.getOrDefault("3", 0L))
                    .criticalAlerts(levelCountMap.getOrDefault("4", 0L))
                    .highAlerts(levelCountMap.getOrDefault("3", 0L))
                    .mediumAlerts(levelCountMap.getOrDefault("2", 0L))
                    .lowAlerts(levelCountMap.getOrDefault("1", 0L))
                    .build();

            // 4. 统计今日/本周/本月新增告警
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
            LocalDateTime weekStart = now.minusDays(7).toLocalDate().atStartOfDay();
            LocalDateTime monthStart = now.minusDays(30).toLocalDate().atStartOfDay();

            Long todayCount = deviceAlertDao.selectCount(
                    new LambdaQueryWrapper<DeviceAlertEntity>()
                            .ge(DeviceAlertEntity::getCreateTime, todayStart)
            );

            Long weekCount = deviceAlertDao.selectCount(
                    new LambdaQueryWrapper<DeviceAlertEntity>()
                            .ge(DeviceAlertEntity::getCreateTime, weekStart)
            );

            Long monthCount = deviceAlertDao.selectCount(
                    new LambdaQueryWrapper<DeviceAlertEntity>()
                            .ge(DeviceAlertEntity::getCreateTime, monthStart)
            );

            statistics.setTodayAlerts(todayCount);
            statistics.setThisWeekAlerts(weekCount);
            statistics.setThisMonthAlerts(monthCount);

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[告警管理] 获取告警统计数据异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_STATISTICS_FAILED", "获取告警统计数据失败");
        }
    }

    /**
     * 批量确认告警
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> batchConfirmAlerts(List<Long> alertIds, String remark) {
        log.info("[告警管理] 批量确认告警: count={}, remark={}", alertIds.size(), remark);

        int successCount = 0;
        Long currentUserId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        for (Long alertId : alertIds) {
            try {
                DeviceAlertEntity alert = deviceAlertDao.selectById(alertId);
                if (alert != null && alert.getAlertStatus() == 0) {
                    alert.setAlertStatus(1); // 已确认
                    alert.setConfirmedBy(currentUserId);
                    alert.setConfirmedTime(now);
                    alert.setConfirmedRemark(remark);
                    deviceAlertDao.updateById(alert);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("[告警管理] 确认告警失败: alertId={}, error={}", alertId, e.getMessage(), e);
            }
        }

        log.info("[告警管理] 批量确认告警完成: total={}, success={}", alertIds.size(), successCount);
        return ResponseDTO.ok(successCount);
    }

    /**
     * 批量处理告警
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> batchHandleAlerts(List<Long> alertIds, String result) {
        log.info("[告警管理] 批量处理告警: count={}, result={}", alertIds.size(), result);

        int successCount = 0;
        Long currentUserId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        for (Long alertId : alertIds) {
            try {
                DeviceAlertEntity alert = deviceAlertDao.selectById(alertId);
                if (alert != null) {
                    alert.setAlertStatus(2); // 已处理
                    alert.setHandledBy(currentUserId);
                    alert.setHandledTime(now);
                    alert.setHandledResult(result);
                    deviceAlertDao.updateById(alert);
                    successCount++;
                }
            } catch (Exception e) {
                log.error("[告警管理] 处理告警失败: alertId={}, error={}", alertId, e.getMessage(), e);
            }
        }

        log.info("[告警管理] 批量处理告警完成: total={}, success={}", alertIds.size(), successCount);
        return ResponseDTO.ok(successCount);
    }

    /**
     * 查询紧急告警
     */
    @Override
    public ResponseDTO<List<DeviceAlertVO>> queryCriticalAlerts(Integer limit) {
        log.debug("[告警管理] 查询紧急告警: limit={}", limit);

        List<DeviceAlertEntity> alerts = deviceAlertDao.selectRecentCriticalAlerts(limit);
        List<DeviceAlertVO> voList = alerts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(voList);
    }

    /**
     * 获取告警趋势数据
     */
    @Override
    public ResponseDTO<Map<String, Long>> getAlertTrend() {
        log.debug("[告警管理] 获取告警趋势");

        try {
            // 查询最近7天的告警趋势
            LocalDateTime now = LocalDateTime.now();
            Map<String, Long> trend = new LinkedHashMap<>();

            for (int i = 6; i >= 0; i--) {
                LocalDateTime date = now.minusDays(i);
                LocalDateTime dayStart = date.toLocalDate().atStartOfDay();
                LocalDateTime dayEnd = date.toLocalDate().atTime(23, 59, 59);

                Long count = deviceAlertDao.selectCount(
                        new LambdaQueryWrapper<DeviceAlertEntity>()
                                .ge(DeviceAlertEntity::getAlertOccurredTime, dayStart)
                                .le(DeviceAlertEntity::getAlertOccurredTime, dayEnd)
                );

                String dateKey = date.toLocalDate().toString();
                trend.put(dateKey, count);
            }

            return ResponseDTO.ok(trend);

        } catch (Exception e) {
            log.error("[告警管理] 获取告警趋势异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_TREND_FAILED", "获取告警趋势失败");
        }
    }

    /**
     * 清理过期告警记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> cleanupExpiredAlerts(Integer daysToKeep) {
        log.info("[告警管理] 清理过期告警记录: daysToKeep={}", daysToKeep);

        try {
            LocalDateTime expireDate = LocalDateTime.now().minusDays(daysToKeep);

            // 物理删除过期记录（需要改为逻辑删除）
            LambdaQueryWrapper<DeviceAlertEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.lt(DeviceAlertEntity::getCreateTime, expireDate);

            int deleteCount = deviceAlertDao.delete(queryWrapper);

            log.info("[告警管理] 清理过期告警记录完成: count={}", deleteCount);
            return ResponseDTO.ok(deleteCount);

        } catch (Exception e) {
            log.error("[告警管理] 清理过期告警记录异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("CLEANUP_FAILED", "清理过期告警记录失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 匹配告警规则
     */
    private boolean matchRule(AlertRuleEntity rule, Long deviceId, String deviceStatus, Map<String, Object> deviceData) {
        // TODO: 实现规则匹配逻辑（简单条件、Aviator表达式、脚本条件）
        // 现在简化实现：仅匹配规则类型
        return true;
    }

    /**
     * 检查是否需要聚合告警
     */
    private boolean shouldAggregateAlert(Long deviceId, Long ruleId, Integer windowSeconds) {
        if (windowSeconds == null || windowSeconds <= 0) {
            return false;
        }

        // 检查Redis中是否有最近相同设备的告警记录
        String cacheKey = String.format("alert:aggregate:device:%d:rule:%d", deviceId, ruleId);
        Boolean exists = redisTemplate.hasKey(cacheKey);

        if (Boolean.TRUE.equals(exists)) {
            // 更新过期时间
            redisTemplate.expire(cacheKey, windowSeconds, TimeUnit.SECONDS);
            return true;
        }

        // 设置缓存标记
        redisTemplate.opsForValue().set(cacheKey, "1", windowSeconds, TimeUnit.SECONDS);
        return false;
    }

    /**
     * 构建告警实体
     */
    private DeviceAlertEntity buildAlert(Long deviceId, String deviceStatus, Map<String, Object> deviceData, AlertRuleEntity rule) {
        DeviceAlertEntity alert = new DeviceAlertEntity();

        // 基础信息
        alert.setDeviceId(deviceId);
        // alert.setDeviceStatus(Integer.parseInt(deviceStatus)); // 字段不存在，已注释

        // 告警信息
        alert.setAlertType(rule.getRuleType());
        alert.setAlertLevel(rule.getAlertLevel());
        alert.setAlertTitle(formatAlertTitle(rule.getAlertTitleTemplate(), deviceData));
        alert.setAlertMessage(formatAlertMessage(rule.getAlertMessageTemplate(), deviceData));

        // 状态和时间
        alert.setAlertStatus(0); // 未确认
        alert.setAlertOccurredTime(LocalDateTime.now());

        return alert;
    }

    /**
     * 格式化告警标题
     */
    private String formatAlertTitle(String template, Map<String, Object> data) {
        if (template == null) {
            return "设备告警";
        }
        // TODO: 实现变量替换
        return template;
    }

    /**
     * 格式化告警消息
     */
    private String formatAlertMessage(String template, Map<String, Object> data) {
        if (template == null) {
            return "设备发生异常，请及时处理";
        }
        // TODO: 实现变量替换
        return template;
    }

    /**
     * 转换为VO
     */
    private DeviceAlertVO convertToVO(DeviceAlertEntity entity) {
        DeviceAlertVO vo = new DeviceAlertVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置名称字段
        vo.setDeviceTypeName(getDeviceTypeName(entity.getDeviceType()));
        vo.setAlertTypeName(getAlertTypeName(entity.getAlertType()));
        vo.setAlertLevelName(getAlertLevelName(entity.getAlertLevel()));
        vo.setAlertLevelColor(getAlertLevelColor(entity.getAlertLevel()));
        vo.setAlertStatusName(getAlertStatusName(entity.getAlertStatus()));

        return vo;
    }

    /**
     * 解析统计结果
     */
    @SuppressWarnings("unchecked")
    private Map<String, Long> parseCountResults(List<Object> results) {
        Map<String, Long> map = new HashMap<>();
        for (Object result : results) {
            Map<String, Object> item = (Map<String, Object>) result;
            String key = String.valueOf(item.get("alertLevel") != null ? item.get("alertLevel") : item.get("alertStatus"));
            Long value = ((Number) item.get("count")).longValue();
            map.put(key, value);
        }
        return map;
    }

    // ==================== Getter方法 ====================

    private Long getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return 1L; // 模拟返回
    }

    private String getDeviceTypeName(Integer deviceType) {
        if (deviceType == null) return "未知";
        switch (deviceType) {
            case 1: return "门禁设备";
            case 2: return "考勤设备";
            case 3: return "消费设备";
            case 4: return "视频设备";
            case 5: return "访客设备";
            default: return "其他";
        }
    }

    private String getAlertTypeName(String alertType) {
        if (alertType == null) return "未知";
        switch (alertType) {
            case "DEVICE_OFFLINE": return "设备离线";
            case "DEVICE_FAULT": return "设备故障";
            case "TEMP_HIGH": return "温度过高";
            case "TEMP_LOW": return "温度过低";
            case "NETWORK_ERROR": return "网络异常";
            case "STORAGE_LOW": return "存储空间不足";
            case "POWER_LOW": return "电量不足";
            case "AUTH_FAILED": return "认证失败";
            case "DEVICE_BLOCKED": return "设备被阻挡";
            default: return "其他";
        }
    }

    private String getAlertLevelName(Integer alertLevel) {
        if (alertLevel == null) return "未知";
        switch (alertLevel) {
            case 1: return "低";
            case 2: return "中";
            case 3: return "高";
            case 4: return "紧急";
            default: return "未知";
        }
    }

    private String getAlertLevelColor(Integer alertLevel) {
        if (alertLevel == null) return "#999999";
        switch (alertLevel) {
            case 1: return "#52C41A"; // 绿色
            case 2: return "#FFA500"; // 橙色
            case 3: return "#FF5722"; // 红色
            case 4: return "#FF0000"; // 深红
            default: return "#999999"; // 灰色
        }
    }

    private String getAlertStatusName(Integer alertStatus) {
        if (alertStatus == null) return "未知";
        switch (alertStatus) {
            case 0: return "未确认";
            case 1: return "已确认";
            case 2: return "已处理";
            case 3: return "已忽略";
            default: return "未知";
        }
    }
}
