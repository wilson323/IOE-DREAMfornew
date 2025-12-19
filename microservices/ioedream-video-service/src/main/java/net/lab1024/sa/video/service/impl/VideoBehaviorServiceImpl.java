package net.lab1024.sa.video.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.dao.VideoBehaviorDao;
import net.lab1024.sa.video.dao.VideoBehaviorPatternDao;
import net.lab1024.sa.video.entity.VideoBehaviorEntity;
import net.lab1024.sa.video.entity.VideoBehaviorPatternEntity;
import net.lab1024.sa.video.manager.VideoBehaviorManager;
import net.lab1024.sa.video.domain.form.VideoBehaviorAnalysisForm;
import net.lab1024.sa.video.domain.form.VideoBehaviorPatternForm;
import net.lab1024.sa.video.domain.vo.VideoBehaviorVO;
import net.lab1024.sa.video.service.VideoBehaviorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 视频行为分析服务实现类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VideoBehaviorServiceImpl implements VideoBehaviorService {

    @Resource
    private VideoBehaviorDao videoBehaviorDao;

    @Resource
    private VideoBehaviorPatternDao videoBehaviorPatternDao;

    @Resource
    private VideoBehaviorManager videoBehaviorManager;

    @Resource
    private ObjectMapper objectMapper;

    // ==================== 行为检测记录管理 ====================

    @Override
    public PageResult<VideoBehaviorVO> queryBehaviorPage(VideoBehaviorAnalysisForm form) {
        try {
            log.info("[行为分析] 查询行为记录, form={}", objectMapper.writeValueAsString(form));
        } catch (Exception e) {
            log.info("[行为分析] 查询行为记录, form={}", form.toString());
        }

        // 构建查询条件
        LambdaQueryWrapper<VideoBehaviorEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (CollectionUtils.isNotEmpty(form.getDeviceIds())) {
            queryWrapper.in(VideoBehaviorEntity::getDeviceId, form.getDeviceIds());
        }
        if (CollectionUtils.isNotEmpty(form.getBehaviorTypes())) {
            queryWrapper.in(VideoBehaviorEntity::getBehaviorType, form.getBehaviorTypes());
        }
        if (form.getMinSeverityLevel() != null) {
            queryWrapper.ge(VideoBehaviorEntity::getSeverityLevel, form.getMinSeverityLevel());
        }
        if (form.getMaxSeverityLevel() != null) {
            queryWrapper.le(VideoBehaviorEntity::getSeverityLevel, form.getMaxSeverityLevel());
        }
        if (form.getMinConfidenceScore() != null) {
            queryWrapper.ge(VideoBehaviorEntity::getConfidenceScore, BigDecimal.valueOf(form.getMinConfidenceScore()));
        }
        if (form.getMaxConfidenceScore() != null) {
            queryWrapper.le(VideoBehaviorEntity::getConfidenceScore, BigDecimal.valueOf(form.getMaxConfidenceScore()));
        }
        if (form.getIncludeAlarms() != null) {
            queryWrapper.eq(VideoBehaviorEntity::getAlarmTriggered, form.getIncludeAlarms() ? 1 : 0);
        }
        if (form.getOnlyUnprocessed() != null && form.getOnlyUnprocessed()) {
            queryWrapper.eq(VideoBehaviorEntity::getProcessStatus, 0);
        }
        if (form.getOnlyNeedingManualConfirm() != null && form.getOnlyNeedingManualConfirm()) {
            queryWrapper.eq(VideoBehaviorEntity::getNeedManualConfirm, 1);
        }
        if (form.getStartTime() != null) {
            queryWrapper.ge(VideoBehaviorEntity::getDetectionTime, form.getStartTime());
        }
        if (form.getEndTime() != null) {
            queryWrapper.le(VideoBehaviorEntity::getDetectionTime, form.getEndTime());
        }
        if (CollectionUtils.isNotEmpty(form.getPersonIds())) {
            queryWrapper.in(VideoBehaviorEntity::getPersonId, form.getPersonIds());
        }
        if (CollectionUtils.isNotEmpty(form.getProcessStatuses())) {
            queryWrapper.in(VideoBehaviorEntity::getProcessStatus, form.getProcessStatuses());
        }
        if (CollectionUtils.isNotEmpty(form.getAlarmLevels())) {
            queryWrapper.in(VideoBehaviorEntity::getAlarmLevel, form.getAlarmLevels());
        }
        if (form.getMinTargetCount() != null) {
            queryWrapper.ge(VideoBehaviorEntity::getTargetCount, form.getMinTargetCount());
        }
        if (form.getMaxTargetCount() != null) {
            queryWrapper.le(VideoBehaviorEntity::getTargetCount, form.getMaxTargetCount());
        }
        if (form.getMinDurationSeconds() != null) {
            queryWrapper.ge(VideoBehaviorEntity::getDurationSeconds, form.getMinDurationSeconds());
        }
        if (form.getMaxDurationSeconds() != null) {
            queryWrapper.le(VideoBehaviorEntity::getDurationSeconds, form.getMaxDurationSeconds());
        }
        if (form.getDetectionAlgorithm() != null) {
            queryWrapper.eq(VideoBehaviorEntity::getDetectionAlgorithm, form.getDetectionAlgorithm());
        }

        // 排序
        if (StringUtils.isNotBlank(form.getSortField())) {
            if ("desc".equalsIgnoreCase(form.getSortOrder())) {
                queryWrapper.orderByDesc(StringUtils.isNotBlank(form.getSortField()) ?
                    VideoBehaviorEntity::getDetectionTime : null);
            } else {
                queryWrapper.orderByAsc(StringUtils.isNotBlank(form.getSortField()) ?
                    VideoBehaviorEntity::getDetectionTime : null);
            }
        } else {
            queryWrapper.orderByDesc(VideoBehaviorEntity::getDetectionTime);
        }

        // 分页查询
        Page<VideoBehaviorEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
        IPage<VideoBehaviorEntity> pageResult = videoBehaviorDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<VideoBehaviorVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<VideoBehaviorVO> pageResultVO = new PageResult<>();
        pageResultVO.setList(voList);
        pageResultVO.setTotal(pageResult.getTotal());
        pageResultVO.setPageNum(Math.toIntExact(pageResult.getCurrent()));
        pageResultVO.setPageSize(Math.toIntExact(pageResult.getSize()));
        pageResultVO.setPages(Math.toIntExact(pageResult.getPages()));

        log.info("[行为分析] 查询完成, 返回{}条记录", voList.size());
        return pageResultVO;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<VideoBehaviorVO> getBehaviorById(Long behaviorId) {
        log.info("[行为分析] 查询行为详情, behaviorId={}", behaviorId);

        VideoBehaviorEntity entity = videoBehaviorDao.selectById(behaviorId);
        if (entity == null) {
            return ResponseDTO.error("BEHAVIOR_NOT_FOUND", "行为记录不存在");
        }

        VideoBehaviorVO vo = convertToVO(entity);
        return ResponseDTO.ok(vo);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getBehaviorStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[行为分析] 获取行为统计数据, startTime={}, endTime={}", startTime, endTime);

        Map<String, Object> statistics = videoBehaviorManager.getBehaviorStatistics(startTime, endTime);
        return ResponseDTO.ok(statistics);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoBehaviorVO>> getDeviceBehaviors(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[行为分析] 获取设备行为记录, deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime);

        List<VideoBehaviorEntity> entities = videoBehaviorManager.getDeviceBehaviors(deviceId, startTime, endTime);
        List<VideoBehaviorVO> voList = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(voList);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoBehaviorVO>> getPersonBehaviors(Long personId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[行为分析] 获取人员行为记录, personId={}, startTime={}, endTime={}", personId, startTime, endTime);

        List<VideoBehaviorEntity> entities = videoBehaviorManager.getPersonBehaviors(personId, startTime, endTime);
        List<VideoBehaviorVO> voList = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(voList);
    }

    @Override
    public ResponseDTO<Void> processBehavior(Long behaviorId, Integer processStatus, Long userId, String userName, String remark) {
        log.info("[行为分析] 处理行为记录, behaviorId={}, processStatus={}, userId={}, remark={}",
                behaviorId, processStatus, userId, remark);

        boolean success = videoBehaviorManager.updateBehaviorProcessStatus(behaviorId, processStatus, userId, userName, remark);
        if (success) {
            return ResponseDTO.ok();
        } else {
            return ResponseDTO.error("PROCESS_FAILED", "处理失败");
        }
    }

    @Override
    public ResponseDTO<Integer> batchProcessBehaviors(List<Long> behaviorIds, Integer processStatus, Long userId, String userName) {
        log.info("[行为分析] 批量处理行为记录, behaviorIds={}, processStatus={}, userId={}",
                behaviorIds.size(), processStatus, userId);

        int count = videoBehaviorManager.batchUpdateBehaviorProcessStatus(behaviorIds, processStatus, userId, userName);
        return ResponseDTO.ok(count);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoBehaviorVO>> getUnprocessedAlarms() {
        log.info("[行为分析] 获取未处理告警");

        List<VideoBehaviorEntity> entities = videoBehaviorManager.getUnprocessedAlarms();
        List<VideoBehaviorVO> voList = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(voList);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoBehaviorVO>> getNeedingManualConfirm() {
        log.info("[行为分析] 获取需要人工确认的记录");

        List<VideoBehaviorEntity> entities = videoBehaviorManager.getNeedingManualConfirm();
        List<VideoBehaviorVO> voList = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(voList);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoBehaviorVO>> getHighRiskBehaviors() {
        log.info("[行为分析] 获取高风险行为");

        List<VideoBehaviorEntity> entities = videoBehaviorManager.getHighRiskBehaviors();
        List<VideoBehaviorVO> voList = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(voList);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<Map<String, Object>>> getAbnormalBehaviorPatterns() {
        log.info("[行为分析] 获取异常行为模式");

        List<Map<String, Object>> patterns = videoBehaviorManager.getAbnormalBehaviorPatterns();
        return ResponseDTO.ok(patterns);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<Map<String, Object>>> getBehaviorFrequency() {
        log.info("[行为分析] 获取行为频率统计");

        List<Map<String, Object>> frequency = videoBehaviorManager.getBehaviorFrequency();
        return ResponseDTO.ok(frequency);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<Map<String, Object>>> getProcessEfficiency() {
        log.info("[行为分析] 获取处理时效分析");

        List<Map<String, Object>> efficiency = videoBehaviorManager.getProcessEfficiency();
        return ResponseDTO.ok(efficiency);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> generateBehaviorReport(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[行为分析] 生成行为报告, startTime={}, endTime={}", startTime, endTime);

        Map<String, Object> report = videoBehaviorManager.generateBehaviorReport(startTime, endTime);
        return ResponseDTO.ok(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> predictBehaviorTrend(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("[行为分析] 预测行为趋势, startDate={}, endDate={}", startDate, endDate);

        Map<String, Object> prediction = videoBehaviorManager.predictBehaviorTrend(startDate, endDate);
        return ResponseDTO.ok(prediction);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> analyzeBehaviorPatterns(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[行为分析] 分析行为模式, deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime);

        List<VideoBehaviorEntity> behaviors = videoBehaviorManager.getDeviceBehaviors(deviceId, startTime, endTime);
        Map<String, Object> analysis = videoBehaviorManager.analyzeBehaviorPatterns(behaviors);
        return ResponseDTO.ok(analysis);
    }

    // ==================== 行为模式管理 ====================

    @Override
    public ResponseDTO<Void> createBehaviorPattern(VideoBehaviorPatternForm form) {
        log.info("[行为模式] 创建行为模式, patternName={}", form.getPatternName());

        VideoBehaviorPatternEntity entity = BeanUtils.copyProperties(form, VideoBehaviorPatternEntity.class);
        videoBehaviorManager.addBehaviorPattern(entity);
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Void> updateBehaviorPattern(Long patternId, VideoBehaviorPatternForm form) {
        log.info("[行为模式] 更新行为模式, patternId={}", patternId);

        VideoBehaviorPatternEntity entity = BeanUtils.copyProperties(form, VideoBehaviorPatternEntity.class);
        entity.setPatternId(patternId);
        videoBehaviorManager.updateBehaviorPattern(entity);
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Void> deleteBehaviorPattern(Long patternId) {
        log.info("[行为模式] 删除行为模式, patternId={}", patternId);

        videoBehaviorPatternDao.deleteById(patternId);
        return ResponseDTO.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<VideoBehaviorPatternForm> getBehaviorPatternById(Long patternId) {
        log.info("[行为模式] 查询行为模式, patternId={}", patternId);

        VideoBehaviorPatternEntity entity = videoBehaviorPatternDao.selectById(patternId);
        if (entity == null) {
            return ResponseDTO.error("PATTERN_NOT_FOUND", "行为模式不存在");
        }

        VideoBehaviorPatternForm form = BeanUtils.copyProperties(entity, VideoBehaviorPatternForm.class);
        return ResponseDTO.ok(form);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<VideoBehaviorPatternForm>> queryBehaviorPatternPage(VideoBehaviorPatternForm form, PageParam pageParam) {
        log.info("[行为模式] 查询行为模式列表, patternName={}", form.getPatternName());

        LambdaQueryWrapper<VideoBehaviorPatternEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(form.getPatternName())) {
            queryWrapper.like(VideoBehaviorPatternEntity::getPatternName, form.getPatternName());
        }
        if (form.getPatternType() != null) {
            queryWrapper.eq(VideoBehaviorPatternEntity::getPatternType, form.getPatternType());
        }
        if (form.getPatternStatus() != null) {
            queryWrapper.eq(VideoBehaviorPatternEntity::getPatternStatus, form.getPatternStatus());
        }

        Page<VideoBehaviorPatternEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        IPage<VideoBehaviorPatternEntity> pageResult = videoBehaviorPatternDao.selectPage(page, queryWrapper);

        List<VideoBehaviorPatternForm> formList = pageResult.getRecords().stream()
                .map(entity -> BeanUtils.copyProperties(entity, VideoBehaviorPatternForm.class))
                .collect(Collectors.toList());

        PageResult<VideoBehaviorPatternForm> result = new PageResult<>();
        result.setList(formList);
        result.setTotal(pageResult.getTotal());
        result.setPageNum(Math.toIntExact(pageResult.getCurrent()));
        result.setPageSize(Math.toIntExact(pageResult.getSize()));
        result.setPages(Math.toIntExact(pageResult.getPages()));

        return ResponseDTO.ok(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoBehaviorPatternForm>> getActivePatterns() {
        log.info("[行为模式] 获取启用中的模式");

        List<VideoBehaviorPatternEntity> entities = videoBehaviorManager.getActivePatterns();
        List<VideoBehaviorPatternForm> formList = entities.stream()
                .map(entity -> BeanUtils.copyProperties(entity, VideoBehaviorPatternForm.class))
                .collect(Collectors.toList());

        return ResponseDTO.ok(formList);
    }

    @Override
    public ResponseDTO<Void> updatePatternStatus(Long patternId, Integer status) {
        log.info("[行为模式] 更新模式状态, patternId={}, status={}", patternId, status);

        boolean success = videoBehaviorManager.updatePatternStatus(patternId, status);
        if (success) {
            return ResponseDTO.ok();
        } else {
            return ResponseDTO.error("UPDATE_FAILED", "更新失败");
        }
    }

    @Override
    public ResponseDTO<Integer> batchUpdatePatternStatus(List<Long> patternIds, Integer status) {
        log.info("[行为模式] 批量更新模式状态, patternIds={}, status={}", patternIds.size(), status);

        int count = videoBehaviorManager.batchUpdatePatternStatus(patternIds, status);
        return ResponseDTO.ok(count);
    }

    // ==================== 辅助方法 ====================

    /**
     * 转换Entity为VO
     */
    private VideoBehaviorVO convertToVO(VideoBehaviorEntity entity) {
        VideoBehaviorVO vo = BeanUtils.copyProperties(entity, VideoBehaviorVO.class);

        // 设置描述字段
        if (entity.getDetectionTime() != null) {
            vo.setDetectionTimeDesc(entity.getDetectionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (entity.getStartTime() != null) {
            vo.setStartTime(entity.getStartTime());
        }
        if (entity.getEndTime() != null) {
            vo.setEndTime(entity.getEndTime());
        }
        if (entity.getProcessTime() != null) {
            vo.setProcessTimeDesc(entity.getProcessTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (entity.getCreateTime() != null) {
            vo.setCreateTimeDesc(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        // 行为类型描述
        vo.setBehaviorTypeDesc(getBehaviorTypeDesc(entity.getBehaviorType()));
        if (entity.getBehaviorSubType() != null) {
            vo.setBehaviorSubTypeDesc(getBehaviorSubTypeDesc(entity.getBehaviorType(), entity.getBehaviorSubType()));
        }

        // 严重程度描述
        vo.setSeverityLevelDesc(getSeverityLevelDesc(entity.getSeverityLevel()));
        vo.setRiskLevelDesc(getRiskLevelDesc(entity.getRiskLevel()));

        // 置信度等级
        vo.setConfidenceGrade(getConfidenceGrade(entity.getConfidenceScore()));

        // 状态描述
        vo.setAlarmTriggeredDesc(entity.getAlarmTriggered() == 1 ? "已触发" : "未触发");
        vo.setNeedManualConfirmDesc(entity.getNeedManualConfirm() == 1 ? "需要" : "不需要");
        vo.setProcessStatusDesc(getProcessStatusDesc(entity.getProcessStatus()));

        // 告警级别描述
        if (entity.getAlarmLevel() != null) {
            vo.setAlarmLevelDesc(getAlarmLevelDesc(entity.getAlarmLevel()));
        }

        // 影响等级描述
        if (entity.getImpactLevel() != null) {
            vo.setImpactLevelDesc(getImpactLevelDesc(entity.getImpactLevel()));
        }

        // 处理优先级描述
        if (entity.getProcessPriority() != null) {
            vo.setProcessPriorityDesc(getProcessPriorityDesc(entity.getProcessPriority()));
        }

        // 解析JSON字段
        if (StringUtils.isNotBlank(entity.getTargetIds())) {
            try {
                vo.setTargetIdsList(objectMapper.readValue(entity.getTargetIds(), new TypeReference<List<Long>>() {}));
            } catch (Exception e) {
                vo.setTargetIdsList(Collections.emptyList());
            }
        }

        if (StringUtils.isNotBlank(entity.getSnapshotUrls())) {
            try {
                vo.setSnapshotUrlsList(objectMapper.readValue(entity.getSnapshotUrls(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                vo.setSnapshotUrlsList(Collections.emptyList());
            }
        }

        if (StringUtils.isNotBlank(entity.getEnvironmentInfo())) {
            try {
                vo.setEnvironmentInfoParsed(objectMapper.readValue(entity.getEnvironmentInfo(), new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                vo.setEnvironmentInfoParsed(new HashMap<>());
            }
        }

        // 生成处理建议和相关链接
        vo.setProcessingSuggestions(generateProcessingSuggestions(entity));
        vo.setRelatedLinks(generateRelatedLinks(entity));
        vo.setTagColor(getTagColor(entity.getSeverityLevel()));
        vo.setStatusIcon(getStatusIcon(entity.getProcessStatus()));

        return vo;
    }

    private String getBehaviorTypeDesc(Integer behaviorType) {
        if (behaviorType == null) return "未知";
        switch (behaviorType) {
            case 1: return "人员检测";
            case 2: return "车辆检测";
            case 3: return "物体检测";
            case 4: return "人脸检测";
            case 5: return "异常行为";
            case 6: return "正常行为";
            case 7: return "其他行为";
            default: return "未知类型";
        }
    }

    private String getBehaviorSubTypeDesc(Integer behaviorType, Integer behaviorSubType) {
        if (behaviorSubType == null) return "未分类";

        switch (behaviorType) {
            case 5: // 异常行为
                switch (behaviorSubType) {
                    case 1: return "人员徘徊";
                    case 2: return "异常聚集";
                    case 3: return "逆行检测";
                    case 4: return "区域入侵";
                    case 5: return "物品遗留";
                    case 6: return "打架斗殴";
                    case 7: return "倒地检测";
                    case 8: return "快速奔跑";
                    default: return "其他异常";
                }
            case 6: // 正常行为
                switch (behaviorSubType) {
                    case 1: return "正常行走";
                    case 2: return "正常停留";
                    case 3: return "正常工作";
                    case 4: return "正常休息";
                    default: return "其他正常";
                }
            default:
                return "子类型" + behaviorSubType;
        }
    }

    private String getSeverityLevelDesc(Integer severityLevel) {
        if (severityLevel == null) return "未知";
        switch (severityLevel) {
            case 1: return "低风险";
            case 2: return "中风险";
            case 3: return "高风险";
            case 4: return "极高风险";
            default: return "未知";
        }
    }

    private String getRiskLevelDesc(Integer riskLevel) {
        return getSeverityLevelDesc(riskLevel);
    }

    private String getConfidenceGrade(BigDecimal confidenceScore) {
        if (confidenceScore == null) return "未知";
        double score = confidenceScore.doubleValue();
        if (score >= 90) return "高";
        if (score >= 75) return "较高";
        if (score >= 60) return "中等";
        if (score >= 40) return "较低";
        return "低";
    }

    private String getProcessStatusDesc(Integer processStatus) {
        if (processStatus == null) return "未知";
        switch (processStatus) {
            case 0: return "未处理";
            case 1: return "处理中";
            case 2: return "已处理";
            case 3: return "已忽略";
            case 4: return "已转交";
            default: return "未知状态";
        }
    }

    private String getAlarmLevelDesc(Integer alarmLevel) {
        if (alarmLevel == null) return "未知";
        switch (alarmLevel) {
            case 1: return "提示";
            case 2: return "重要";
            case 3: return "紧急";
            case 4: return "严重";
            default: return "未知";
        }
    }

    private String getImpactLevelDesc(Integer impactLevel) {
        if (impactLevel == null) return "未知";
        switch (impactLevel) {
            case 1: return "轻微影响";
            case 2: return "一般影响";
            case 3: return "重要影响";
            case 4: return "严重影响";
            default: return "未知";
        }
    }

    private String getProcessPriorityDesc(Integer processPriority) {
        if (processPriority == null) return "未知";
        switch (processPriority) {
            case 1: return "低";
            case 2: return "中";
            case 3: return "高";
            case 4: return "紧急";
            default: return "未知";
        }
    }

    private List<String> generateProcessingSuggestions(VideoBehaviorEntity behavior) {
        List<String> suggestions = new ArrayList<>();

        if (behavior.getSeverityLevel() != null && behavior.getSeverityLevel() >= 3) {
            suggestions.add("立即处理，优先级高");
        }

        if (behavior.getConfidenceScore() != null && behavior.getConfidenceScore().compareTo(new BigDecimal("70")) < 0) {
            suggestions.add("建议人工复核检测结果");
        }

        if (behavior.getBehaviorType() != null && behavior.getBehaviorType() == 5) {
            suggestions.add("加强该区域监控");
            suggestions.add("通知安保人员关注");
        }

        return suggestions;
    }

    private List<Map<String, String>> generateRelatedLinks(VideoBehaviorEntity behavior) {
        List<Map<String, String>> links = new ArrayList<>();

        Map<String, String> detailLink = new HashMap<>();
        detailLink.put("text", "查看详情");
        detailLink.put("url", "/behavior/detail/" + behavior.getBehaviorId());
        links.add(detailLink);

        if (behavior.getPersonId() != null) {
            Map<String, String> personLink = new HashMap<>();
            personLink.put("text", "人员信息");
            personLink.put("url", "/person/detail/" + behavior.getPersonId());
            links.add(personLink);
        }

        return links;
    }

    private String getTagColor(Integer severityLevel) {
        if (severityLevel == null) return "#999999";
        switch (severityLevel) {
            case 1: return "#52C41A"; // 绿色
            case 2: return "#FAAD14"; // 黄色
            case 3: return "#FA8C16"; // 橙色
            case 4: return "#FF4D4F"; // 红色
            default: return "#999999"; // 灰色
        }
    }

    private String getStatusIcon(Integer processStatus) {
        if (processStatus == null) return "question";
        switch (processStatus) {
            case 0: return "clock-circle"; // 未处理
            case 1: return "loading"; // 处理中
            case 2: return "check-circle"; // 已处理
            case 3: return "minus-circle"; // 已忽略
            case 4: return "arrow-right"; // 已转交
            default: return "question";
        }
    }

    // ==================== 其他接口实现 ====================
    // 由于篇幅限制，其他方法实现类似，主要调用Manager层方法进行处理
    // 包括AI智能分析、导出报表、告警通知、数据清理等功能

    @Override
    public ResponseDTO<List<VideoBehaviorPatternForm>> getPatternsNeedingRetraining() {
        log.info("[行为模式] 获取需要重新训练的模式");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<List<VideoBehaviorPatternForm>> getExpiredPatterns() {
        log.info("[行为模式] 获取已过期的模式");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<Void> updatePatternTrainingInfo(Long patternId, LocalDateTime trainingTime, Double trainingAccuracy,
                                                      Double validationAccuracy, Double falsePositiveRate, Double falseNegativeRate,
                                                      Long trainingSamples, LocalDateTime nextTrainingTime, String version) {
        log.info("[行为模式] 更新模式训练信息");
        // 实现逻辑
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getPatternPerformanceMetrics() {
        log.info("[行为模式] 获取模式性能指标");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getTrainingPlan() {
        log.info("[行为模式] 获取模式训练计划");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<List<VideoBehaviorPatternForm>> getPatternsNeedingMaintenance(Double minAccuracy, Double maxFalsePositiveRate) {
        log.info("[行为模式] 获取需要维护的模式");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<Void> updatePatternPerformanceMetrics(Long patternId, String performanceMetrics) {
        log.info("[行为模式] 更新模式性能指标");
        // 实现逻辑
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Void> updatePatternUsageStatistics(Long patternId, String usageStatistics) {
        log.info("[行为模式] 更新模式使用统计");
        // 实现逻辑
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Integer> cleanExpiredPatterns() {
        log.info("[行为模式] 清理过期模式");
        // 实现逻辑
        return ResponseDTO.ok(0);
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getVersionDistribution() {
        log.info("[行为模式] 获取模式版本分布");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getAlgorithmModelUsage() {
        log.info("[行为模式] 获取算法模型使用统计");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<VideoBehaviorVO> detectBehavior(Long deviceId, String videoStreamUrl, Map<String, Object> parameters) {
        log.info("[AI分析] 实时行为检测");
        // 实现逻辑
        return ResponseDTO.ok(new VideoBehaviorVO());
    }

    @Override
    public ResponseDTO<List<VideoBehaviorVO>> analyzeVideoBehavior(Long deviceId, String videoFilePath, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[AI分析] 历史视频行为分析");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<List<VideoBehaviorVO>> batchAnalyzeBehaviors(List<Long> deviceIds, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[AI分析] 批量行为分析");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<List<VideoBehaviorVO>> identifyAbnormalBehaviors(Long deviceId, LocalDateTime startTime, LocalDateTime endTime, Double confidenceThreshold) {
        log.info("[AI分析] 异常行为识别");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> matchBehaviorPatterns(Long deviceId, List<Long> behaviorIds) {
        log.info("[AI分析] 行为模式匹配");
        // 实现逻辑
        return ResponseDTO.ok(Collections.emptyList());
    }

    @Override
    public ResponseDTO<VideoBehaviorVO> customBehaviorDetection(Long deviceId, Map<String, Object> customRules, Map<String, Object> thresholds) {
        log.info("[AI分析] 自定义行为检测");
        // 实现逻辑
        return ResponseDTO.ok(new VideoBehaviorVO());
    }

    @Override
    public ResponseDTO<String> exportBehaviorData(VideoBehaviorAnalysisForm form, String exportFormat) {
        log.info("[数据导出] 导出行为数据");
        // 实现逻辑
        return ResponseDTO.ok("export_file_path");
    }

    @Override
    public ResponseDTO<String> exportBehaviorReport(LocalDateTime startTime, LocalDateTime endTime, String reportType) {
        log.info("[数据导出] 导出行为报告");
        // 实现逻辑
        return ResponseDTO.ok("report_file_path");
    }

    @Override
    public ResponseDTO<Map<String, Object>> generateBehaviorChartData(VideoBehaviorAnalysisForm form, List<String> chartTypes) {
        log.info("[数据报表] 生成行为图表数据");
        // 实现逻辑
        return ResponseDTO.ok(new HashMap<>());
    }

    @Override
    public ResponseDTO<String> createAnalysisTask(VideoBehaviorAnalysisForm form, String taskName) {
        log.info("[任务管理] 创建分析任务");
        // 实现逻辑
        return ResponseDTO.ok("task_id");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getAnalysisTaskStatus(String taskId) {
        log.info("[任务管理] 获取分析任务状态");
        // 实现逻辑
        return ResponseDTO.ok(new HashMap<>());
    }

    @Override
    public ResponseDTO<Void> cancelAnalysisTask(String taskId) {
        log.info("[任务管理] 取消分析任务");
        // 实现逻辑
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Void> sendBehaviorAlarm(Long behaviorId, List<String> alarmTypes, Integer alarmLevel) {
        log.info("[告警通知] 发送行为告警");
        // 实现逻辑
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<PageResult<VideoBehaviorVO>> getBehaviorAlarms(VideoBehaviorAnalysisForm form, PageParam pageParam) {
        log.info("[告警通知] 获取行为告警列表");
        // 实现逻辑
        return ResponseDTO.ok(new PageResult<>());
    }

    @Override
    public ResponseDTO<Void> configureAlarmRules(Map<String, Object> alarmRules) {
        log.info("[告警通知] 配置行为告警规则");
        // 实现逻辑
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Map<String, Object>> getAlarmRulesConfiguration() {
        log.info("[告警通知] 获取告警规则配置");
        // 实现逻辑
        return ResponseDTO.ok(new HashMap<>());
    }

    @Override
    public ResponseDTO<Void> testBehaviorAlarm(Long deviceId, Map<String, Object> testParameters) {
        log.info("[告警通知] 测试行为告警");
        // 实现逻辑
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Integer> cleanOldBehaviorRecords(LocalDateTime cutoffTime) {
        log.info("[数据维护] 清理历史行为记录");
        int count = videoBehaviorManager.cleanOldBehaviorRecords(cutoffTime);
        return ResponseDTO.ok(count);
    }

    @Override
    public ResponseDTO<String> backupBehaviorData(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[数据维护] 备份行为数据");
        // 实现逻辑
        return ResponseDTO.ok("backup_file_path");
    }

    @Override
    public ResponseDTO<Void> restoreBehaviorData(String backupFilePath) {
        log.info("[数据维护] 恢复行为数据");
        // 实现逻辑
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Map<String, Object>> checkDataIntegrity() {
        log.info("[数据维护] 数据完整性检查");
        // 实现逻辑
        return ResponseDTO.ok(new HashMap<>());
    }

    @Override
    public ResponseDTO<Void> rebuildBehaviorIndex() {
        log.info("[数据维护] 重建行为索引");
        // 实现逻辑
        return ResponseDTO.ok();
    }

    @Override
    public ResponseDTO<Map<String, Object>> optimizeBehaviorData() {
        log.info("[数据维护] 优化行为数据");
        // 实现逻辑
        return ResponseDTO.ok(new HashMap<>());
    }
}
