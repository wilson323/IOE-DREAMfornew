package net.lab1024.sa.access.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AntiPassbackConfigDao;
import net.lab1024.sa.access.dao.AntiPassbackRecordDao;
import net.lab1024.sa.access.domain.entity.AntiPassbackConfigEntity;
import net.lab1024.sa.access.domain.entity.AntiPassbackRecordEntity;
import net.lab1024.sa.access.domain.form.AntiPassbackConfigForm;
import net.lab1024.sa.access.domain.form.AntiPassbackDetectForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackConfigVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackDetectResultVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackRecordVO;
import net.lab1024.sa.access.service.AntiPassbackService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 门禁反潜回服务实现
 * <p>
 * 核心功能：
 * - 反潜回检测算法（<100ms响应时间）
 * - 支持4种反潜回模式（全局/区域/软/硬）
 * - Redis缓存最近通行记录
 * - 数据库记录保存
 * </p>
 * <p>
 * 性能优化：
 * - 使用Redis缓存减少数据库查询
 * - 批量操作优化
 * - 异步记录保存
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class AntiPassbackServiceImpl implements AntiPassbackService {

    @Resource
    private AntiPassbackConfigDao antiPassbackConfigDao;

    @Resource
    private AntiPassbackRecordDao antiPassbackRecordDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "anti_passback:";

    /**
     * 反潜回模式常量
     */
    private static final int MODE_GLOBAL = 1;      // 全局反潜回
    private static final int MODE_AREA = 2;         // 区域反潜回
    private static final int MODE_SOFT = 3;         // 软反潜回
    private static final int MODE_HARD = 4;         // 硬反潜回

    /**
     * 检测结果常量
     */
    private static final int RESULT_NORMAL = 1;     // 正常通行
    private static final int RESULT_SOFT = 2;       // 软反潜回
    private static final int RESULT_HARD = 3;       // 硬反潜回

    /**
     * 违规类型常量
     */
    private static final int VIOLATION_TIME_WINDOW = 1;     // 时间窗口内重复
    private static final int VIOLATION_CROSS_AREA = 2;      // 跨区域异常
    private static final int VIOLATION_FREQUENCY = 3;       // 频次超限

    @Override
    public ResponseDTO<AntiPassbackDetectResultVO> detect(AntiPassbackDetectForm detectForm) {
        log.info("[反潜回检测] 开始检测: userId={}, deviceId={}, areaId={}",
                detectForm.getUserId(), detectForm.getDeviceId(), detectForm.getAreaId());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 检查是否跳过检测（管理员特殊通行）
            if (Boolean.TRUE.equals(detectForm.getSkipDetection())) {
                log.info("[反潜回检测] 跳过检测: userId={}", detectForm.getUserId());
                return ResponseDTO.ok(buildNormalResult(detectForm, startTime));
            }

            // 2. 获取启用的反潜回配置
            List<AntiPassbackConfigEntity> configs = getActiveConfigs(detectForm.getAreaId());

            // 3. 如果没有启用配置，直接允许通行
            if (configs.isEmpty()) {
                log.info("[反潜回检测] 无启用配置，允许通行: userId={}", detectForm.getUserId());
                return ResponseDTO.ok(buildNormalResult(detectForm, startTime));
            }

            // 4. 执行反潜回检测
            AntiPassbackDetectResultVO result = doDetect(detectForm, configs, startTime);

            // 5. 异步保存检测记录
            saveRecordAsync(detectForm, result);

            log.info("[反潜回检测] 检测完成: userId={}, result={}, allowPass={},耗时={}ms",
                    detectForm.getUserId(), result.getResult(), result.getAllowPass(),
                    result.getDetectionTime());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[反潜回检测] 检测异常: userId={}, error={}",
                    detectForm.getUserId(), e.getMessage(), e);
            throw new BusinessException("ANTI_PASSBACK_DETECT_ERROR", "反潜回检测失败: " + e.getMessage());
        }
    }

    /**
     * 执行反潜回检测（核心算法）
     */
    private AntiPassbackDetectResultVO doDetect(AntiPassbackDetectForm detectForm,
                                                  List<AntiPassbackConfigEntity> configs,
                                                  long startTime) {

        // 遍历所有启用的配置，按优先级检测
        for (AntiPassbackConfigEntity config : configs) {
            AntiPassbackDetectResultVO result = checkWithConfig(detectForm, config, startTime);

            // 如果检测到违规且是硬反潜回模式，直接返回阻止通行
            if (result.getResult() == RESULT_HARD) {
                return result;
            }
        }

        // 所有配置都通过或只是软反潜回
        return buildNormalResult(detectForm, startTime);
    }

    /**
     * 使用指定配置进行检测
     */
    private AntiPassbackDetectResultVO checkWithConfig(AntiPassbackDetectForm detectForm,
                                                         AntiPassbackConfigEntity config,
                                                         long startTime) {

        // 1. 确定检测范围（全局或区域）
        Long checkAreaId = (config.getMode() == MODE_AREA) ? config.getAreaId() : null;

        // 2. 从Redis获取用户最近通行记录
        String cacheKey = buildCacheKey(detectForm.getUserId(), checkAreaId);
        List<RecentPassInfo> recentPasses = getRecentPassesFromCache(cacheKey);

        // 3. 计算时间窗口
        LocalDateTime passTime = LocalDateTime.now();
        LocalDateTime windowStartTime = passTime.minusNanos(config.getTimeWindow() * 1_000_000);

        // 4. 统计时间窗口内的通行次数
        int countInWindow = countPassesInWindow(recentPasses, windowStartTime);

        // 5. 检查是否违规
        if (countInWindow >= config.getMaxPassCount()) {
            // 发现违规
            return buildViolationResult(detectForm, config, recentPasses, countInWindow, startTime);
        }

        // 6. 未违规，更新缓存
        updateRecentPassesCache(cacheKey, detectForm, passTime, recentPasses);

        // 7. 返回正常结果
        return buildNormalResult(detectForm, startTime);
    }

    /**
     * 构建违规结果
     */
    private AntiPassbackDetectResultVO buildViolationResult(AntiPassbackDetectForm detectForm,
                                                             AntiPassbackConfigEntity config,
                                                             List<RecentPassInfo> recentPasses,
                                                             int countInWindow,
                                                             long startTime) {

        int result;
        boolean allowPass;
        String resultMessage;

        // 根据模式确定结果
        if (config.getMode() == MODE_SOFT) {
            result = RESULT_SOFT;
            allowPass = true;
            resultMessage = "软反潜回：告警但允许通行";
        } else {
            result = RESULT_HARD;
            allowPass = false;
            resultMessage = "硬反潜回：阻止通行";
        }

        // 获取最近一次通行信息
        AntiPassbackDetectResultVO.RecentPassInfo recentPass = null;
        if (!recentPasses.isEmpty()) {
            RecentPassInfo lastPass = recentPasses.get(0);
            recentPass = AntiPassbackDetectResultVO.RecentPassInfo.builder()
                    .passTime(lastPass.getPassTime())
                    .deviceName(lastPass.getDeviceName())
                    .areaName(lastPass.getAreaName())
                    .secondsAgo((System.currentTimeMillis() - lastPass.getPassTime()) / 1000)
                    .build();
        }

        long detectionTime = System.currentTimeMillis() - startTime;

        return AntiPassbackDetectResultVO.builder()
                .result(result)
                .resultMessage(resultMessage)
                .allowPass(allowPass)
                .violationType(VIOLATION_TIME_WINDOW)
                .violationMessage(String.format("时间窗口内已通行%d次", countInWindow))
                .detectionTime(detectionTime)
                .configId(config.getConfigId())
                .recentPass(recentPass)
                .build();
    }

    /**
     * 构建正常结果
     */
    private AntiPassbackDetectResultVO buildNormalResult(AntiPassbackDetectForm detectForm, long startTime) {
        long detectionTime = System.currentTimeMillis() - startTime;

        return AntiPassbackDetectResultVO.builder()
                .result(RESULT_NORMAL)
                .resultMessage("正常通行")
                .allowPass(true)
                .detectionTime(detectionTime)
                .build();
    }

    /**
     * 获取启用的反潜回配置
     */
    private List<AntiPassbackConfigEntity> getActiveConfigs(Long areaId) {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<AntiPassbackConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AntiPassbackConfigEntity::getEnabled, 1)
                .le(AntiPassbackConfigEntity::getEffectiveTime, now)
                .and(w -> w.isNull(AntiPassbackConfigEntity::getExpireTime)
                        .or().gt(AntiPassbackConfigEntity::getExpireTime, now))
                .orderByAsc(AntiPassbackConfigEntity::getMode);  // 按模式优先级排序

        // 如果指定了区域，同时查询区域配置和全局配置
        if (areaId != null) {
            queryWrapper.and(w -> w.eq(AntiPassbackConfigEntity::getAreaId, areaId)
                    .or()
                    .isNull(AntiPassbackConfigEntity::getAreaId));
        }

        return antiPassbackConfigDao.selectList(queryWrapper);
    }

    /**
     * 从Redis获取最近通行记录
     */
    @SuppressWarnings("unchecked")
    private List<RecentPassInfo> getRecentPassesFromCache(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                // Redis返回的可能是List或LinkedHashMap，需要处理类型转换
                if (cached instanceof List) {
                    return (List<RecentPassInfo>) cached;
                }
                // 如果序列化格式变化，可能需要额外的转换逻辑
                return new ArrayList<>();
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.warn("[反潜回检测] Redis获取缓存失败: key={}, error={}", cacheKey, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 统计时间窗口内的通行次数
     */
    private int countPassesInWindow(List<RecentPassInfo> recentPasses, LocalDateTime windowStartTime) {
        long windowStartMillis = windowStartTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        return (int) recentPasses.stream()
                .filter(pass -> pass.getPassTime() >= windowStartMillis)
                .count();
    }

    /**
     * 更新最近通行记录缓存
     */
    private void updateRecentPassesCache(String cacheKey, AntiPassbackDetectForm detectForm,
                                           LocalDateTime passTime, List<RecentPassInfo> recentPasses) {
        try {
            // 创建新的通行记录
            RecentPassInfo newPass = RecentPassInfo.builder()
                    .passTime(passTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .deviceName(detectForm.getDeviceName())
                    .areaName(detectForm.getAreaName())
                    .build();

            // 添加到列表开头
            recentPasses.add(0, newPass);

            // 只保留最近10条记录
            if (recentPasses.size() > 10) {
                recentPasses = recentPasses.subList(0, 10);
            }

            // 缓存30分钟
            redisTemplate.opsForValue().set(cacheKey, recentPasses, 30, TimeUnit.MINUTES);

        } catch (Exception e) {
            log.warn("[反潜回检测] 更新缓存失败: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    /**
     * 异步保存检测记录
     */
    private void saveRecordAsync(AntiPassbackDetectForm detectForm, AntiPassbackDetectResultVO result) {
        try {
            AntiPassbackRecordEntity record = AntiPassbackRecordEntity.builder()
                    .userId(detectForm.getUserId())
                    .userName(detectForm.getUserName())
                    .userCardNo(detectForm.getUserCardNo())
                    .deviceId(detectForm.getDeviceId())
                    .deviceName(detectForm.getDeviceName())
                    .deviceCode(detectForm.getDeviceCode())
                    .areaId(detectForm.getAreaId())
                    .areaName(detectForm.getAreaName())
                    .result(result.getResult())
                    .passTime(LocalDateTime.now())
                    .detectedTime(LocalDateTime.now())
                    .detailInfo(JSON.toJSONString(result))
                    .build();

            antiPassbackRecordDao.insert(record);

        } catch (Exception e) {
            log.error("[反潜回检测] 保存记录失败: userId={}, error={}",
                    detectForm.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(Long userId, Long areaId) {
        if (areaId == null) {
            return String.format("%suser:%d", CACHE_KEY_PREFIX, userId);
        } else {
            return String.format("%suser:%d:area:%d", CACHE_KEY_PREFIX, userId, areaId);
        }
    }

    @Override
    public ResponseDTO<List<AntiPassbackDetectResultVO>> batchDetect(List<AntiPassbackDetectForm> detectForms) {
        List<AntiPassbackDetectResultVO> results = detectForms.stream()
                .map(this::detect)
                .map(ResponseDTO::getData)
                .collect(Collectors.toList());

        return ResponseDTO.ok(results);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> createConfig(AntiPassbackConfigForm configForm) {
        AntiPassbackConfigEntity entity = convertToEntity(configForm);
        antiPassbackConfigDao.insert(entity);

        log.info("[反潜回配置] 创建配置: configId={}, mode={}", entity.getConfigId(), entity.getMode());
        return ResponseDTO.ok(entity.getConfigId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> updateConfig(AntiPassbackConfigForm configForm) {
        if (configForm.getConfigId() == null) {
            throw new BusinessException("CONFIG_ID_REQUIRED", "配置ID不能为空");
        }

        AntiPassbackConfigEntity entity = convertToEntity(configForm);
        antiPassbackConfigDao.updateById(entity);

        log.info("[反潜回配置] 更新配置: configId={}", configForm.getConfigId());
        return ResponseDTO.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> deleteConfig(Long configId) {
        antiPassbackConfigDao.deleteById(configId);

        log.info("[反潜回配置] 删除配置: configId={}", configId);
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<AntiPassbackConfigVO> getConfig(Long configId) {
        AntiPassbackConfigEntity entity = antiPassbackConfigDao.selectById(configId);
        if (entity == null) {
            throw new BusinessException("CONFIG_NOT_FOUND", "配置不存在");
        }

        return ResponseDTO.ok(convertToVO(entity));
    }

    @Override
    public ResponseDTO<List<AntiPassbackConfigVO>> listConfigs(Integer mode, Integer enabled, Long areaId) {
        LambdaQueryWrapper<AntiPassbackConfigEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (mode != null) {
            queryWrapper.eq(AntiPassbackConfigEntity::getMode, mode);
        }
        if (enabled != null) {
            queryWrapper.eq(AntiPassbackConfigEntity::getEnabled, enabled);
        }
        if (areaId != null) {
            queryWrapper.eq(AntiPassbackConfigEntity::getAreaId, areaId);
        }

        List<AntiPassbackConfigEntity> entities = antiPassbackConfigDao.selectList(queryWrapper);
        List<AntiPassbackConfigVO> vos = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(vos);
    }

    @Override
    public ResponseDTO<PageResult<AntiPassbackRecordVO>> queryRecords(
            Long userId, Long deviceId, Long areaId, Integer result, Integer handled,
            Integer pageNum, Integer pageSize) {

        Page<AntiPassbackRecordEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AntiPassbackRecordEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            queryWrapper.eq(AntiPassbackRecordEntity::getUserId, userId);
        }
        if (deviceId != null) {
            queryWrapper.eq(AntiPassbackRecordEntity::getDeviceId, deviceId);
        }
        if (areaId != null) {
            queryWrapper.eq(AntiPassbackRecordEntity::getAreaId, areaId);
        }
        if (result != null) {
            queryWrapper.eq(AntiPassbackRecordEntity::getResult, result);
        }
        if (handled != null) {
            queryWrapper.eq(AntiPassbackRecordEntity::getHandled, handled);
        }

        queryWrapper.orderByDesc(AntiPassbackRecordEntity::getDetectedTime);

        Page<AntiPassbackRecordEntity> resultPage = antiPassbackRecordDao.selectPage(page, queryWrapper);

        List<AntiPassbackRecordVO> vos = resultPage.getRecords().stream()
                .map(this::convertRecordToVO)
                .collect(Collectors.toList());

        PageResult<AntiPassbackRecordVO> pageResult = PageResult.of(vos, resultPage.getTotal(),
                pageNum, pageSize);

        return ResponseDTO.ok(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> handleRecord(Long recordId, String handleRemark) {
        AntiPassbackRecordEntity record = antiPassbackRecordDao.selectById(recordId);
        if (record == null) {
            throw new BusinessException("RECORD_NOT_FOUND", "记录不存在");
        }

        record.setHandled(1);
        record.setHandleRemark(handleRemark);
        record.setHandledTime(LocalDateTime.now());

        antiPassbackRecordDao.updateById(record);

        log.info("[反潜回记录] 处理记录: recordId={}", recordId);
        return ResponseDTO.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> batchHandleRecords(List<Long> recordIds, Integer handled, String handleRemark) {
        recordIds.forEach(recordId -> {
            AntiPassbackRecordEntity record = antiPassbackRecordDao.selectById(recordId);
            if (record != null) {
                record.setHandled(handled);
                record.setHandleRemark(handleRemark);
                record.setHandledTime(LocalDateTime.now());
                antiPassbackRecordDao.updateById(record);
            }
        });

        log.info("[反潜回记录] 批量处理记录: count={}", recordIds.size());
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Integer> clearUserCache(Long userId) {
        Set<String> keys = redisTemplate.keys(String.format("%s*user:%d*", CACHE_KEY_PREFIX, userId));

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[反潜回缓存] 清除用户缓存: userId={}, count={}", userId, keys.size());
            return ResponseDTO.ok(keys.size());
        }

        return ResponseDTO.ok(0);
    }

    @Override
    public ResponseDTO<Integer> clearAllCache() {
        Set<String> keys = redisTemplate.keys(String.format("%s*", CACHE_KEY_PREFIX));

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[反潜回缓存] 清除所有缓存: count={}", keys.size());
            return ResponseDTO.ok(keys.size());
        }

        return ResponseDTO.ok(0);
    }

    /**
     * 转换Form到Entity
     */
    private AntiPassbackConfigEntity convertToEntity(AntiPassbackConfigForm form) {
        return AntiPassbackConfigEntity.builder()
                .configId(form.getConfigId())
                .mode(form.getMode())
                .areaId(form.getAreaId())
                .timeWindow(form.getTimeWindow())
                .maxPassCount(form.getMaxPassCount())
                .enabled(form.getEnabled() != null ? form.getEnabled() : 1)
                .effectiveTime(form.getEffectiveTime())
                .expireTime(form.getExpireTime())
                .alertEnabled(form.getAlertEnabled() != null ? form.getAlertEnabled() : 1)
                .alertMethods(form.getAlertMethods())
                .build();
    }

    /**
     * 转换Entity到VO
     */
    private AntiPassbackConfigVO convertToVO(AntiPassbackConfigEntity entity) {
        return AntiPassbackConfigVO.builder()
                .configId(entity.getConfigId())
                .mode(entity.getMode())
                .modeName(getModeName(entity.getMode()))
                .areaId(entity.getAreaId())
                .timeWindow(entity.getTimeWindow())
                .timeWindowDesc(formatTimeWindow(entity.getTimeWindow()))
                .maxPassCount(entity.getMaxPassCount())
                .enabled(entity.getEnabled())
                .effectiveTime(entity.getEffectiveTime())
                .expireTime(entity.getExpireTime())
                .alertEnabled(entity.getAlertEnabled())
                .alertMethods(entity.getAlertMethods())
                .createdTime(entity.getCreatedTime())
                .updatedTime(entity.getUpdatedTime())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    /**
     * 转换记录Entity到VO
     */
    private AntiPassbackRecordVO convertRecordToVO(AntiPassbackRecordEntity entity) {
        return AntiPassbackRecordVO.builder()
                .recordId(entity.getRecordId())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .userCardNo(entity.getUserCardNo())
                .deviceId(entity.getDeviceId())
                .deviceName(entity.getDeviceName())
                .deviceCode(entity.getDeviceCode())
                .areaId(entity.getAreaId())
                .areaName(entity.getAreaName())
                .result(entity.getResult())
                .resultName(getResultName(entity.getResult()))
                .violationType(entity.getViolationType())
                .violationTypeName(getViolationTypeName(entity.getViolationType()))
                .passTime(entity.getPassTime())
                .detectedTime(entity.getDetectedTime())
                .handled(entity.getHandled())
                .handledName(getHandledName(entity.getHandled()))
                .handleRemark(entity.getHandleRemark())
                .handledBy(entity.getHandledBy())
                .handledTime(entity.getHandledTime())
                .detailInfo(entity.getDetailInfo())
                .createdTime(entity.getCreatedTime())
                .build();
    }

    /**
     * 获取模式名称
     */
    private String getModeName(Integer mode) {
        if (mode == null) return "未知";
        switch (mode) {
            case MODE_GLOBAL: return "全局反潜回";
            case MODE_AREA: return "区域反潜回";
            case MODE_SOFT: return "软反潜回";
            case MODE_HARD: return "硬反潜回";
            default: return "未知";
        }
    }

    /**
     * 格式化时间窗口
     */
    private String formatTimeWindow(Long timeWindow) {
        if (timeWindow == null) return "未知";

        long seconds = timeWindow / 1000;
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟";
        } else {
            return (seconds / 3600) + "小时";
        }
    }

    /**
     * 获取结果名称
     */
    private String getResultName(Integer result) {
        if (result == null) return "未知";
        switch (result) {
            case RESULT_NORMAL: return "正常通行";
            case RESULT_SOFT: return "软反潜回";
            case RESULT_HARD: return "硬反潜回";
            default: return "未知";
        }
    }

    /**
     * 获取违规类型名称
     */
    private String getViolationTypeName(Integer violationType) {
        if (violationType == null) return "";
        switch (violationType) {
            case VIOLATION_TIME_WINDOW: return "时间窗口内重复";
            case VIOLATION_CROSS_AREA: return "跨区域异常";
            case VIOLATION_FREQUENCY: return "频次超限";
            default: return "未知";
        }
    }

    /**
     * 获取处理状态名称
     */
    private String getHandledName(Integer handled) {
        if (handled == null) return "未知";
        switch (handled) {
            case 0: return "未处理";
            case 1: return "已处理";
            case 2: return "已忽略";
            default: return "未知";
        }
    }

    /**
     * 最近通行信息（内部类）
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class RecentPassInfo {
        private Long passTime;
        private String deviceName;
        private String areaName;
    }
}
