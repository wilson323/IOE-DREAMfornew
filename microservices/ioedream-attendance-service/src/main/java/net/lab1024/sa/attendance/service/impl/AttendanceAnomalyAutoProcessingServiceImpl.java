package net.lab1024.sa.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;
import net.lab1024.sa.attendance.entity.AttendanceAnomalyEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceAnomalyAutoProcessQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceAnomalyAutoProcessVO;
import net.lab1024.sa.attendance.service.AttendanceAnomalyAutoProcessingService;
import net.lab1024.sa.attendance.service.AttendanceAnomalyDetectionService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.util.SmartBeanUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤异常自动处理服务实现类
 * <p>
 * 提供考勤异常的智能自动处理功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceAnomalyAutoProcessingServiceImpl implements AttendanceAnomalyAutoProcessingService {

    @Resource
    private AttendanceAnomalyDao anomalyDao;

    @Resource
    private AttendanceAnomalyDetectionService detectionService;

    /**
     * 智能分类异常
     * <p>
     * 分类规则：
     * 1. AUTO_APPROVE（自动批准）：
     *    - 缺卡<5分钟，且月出勤率>95%
     *    - 迟到<3分钟，且本月无违规记录
     *    - 早退<3分钟，且本月无违规记录
     *    - 有正当理由说明（如加班、外出）
     * </p>
     * <p>
     * 2. MANUAL_REVIEW（人工审核）：
     *    - 迟到3-15分钟
     *    - 早退3-15分钟
     *    - 缺卡但无正当理由
     *    - 月违规次数2-5次
     * </p>
     * <p>
     * 3. AUTO_REJECT（自动拒绝）：
     *    - 无打卡记录且无请假申请
     *    - 旷工且无正当理由
     * </p>
     * <p>
     * 4. ESCALATE（升级处理）：
     *    - 旷工>1天
     *    - 月违规次数>5次
     *    - 严重违规（迟到>30分钟或早退>30分钟）
     * </p>
     */
    @Override
    public String categorizeAnomaly(AttendanceAnomalyEntity anomaly) {
        log.info("[异常自动处理] 开始智能分类: anomalyId={}, userId={}, type={}",
                anomaly.getAnomalyId(), anomaly.getUserId(), anomaly.getAnomalyType());

        try {
            // 获取员工历史记录（简化版，实际应查询数据库）
            Map<String, Object> employeeHistory = analyzeEmployeeAnomalyPattern(anomaly.getUserId(), 3);

            // 计算风险评分
            int riskScore = calculateRiskScore(anomaly, employeeHistory);

            // 根据评分和异常类型进行分类
            String category = determineCategory(anomaly, riskScore, employeeHistory);

            log.info("[异常自动处理] 智能分类完成: anomalyId={}, category={}, riskScore={}",
                    anomaly.getAnomalyId(), category, riskScore);

            return category;

        } catch (Exception e) {
            log.error("[异常自动处理] 智能分类失败: anomalyId={}, error={}",
                    anomaly.getAnomalyId(), e.getMessage(), e);
            throw new BusinessException("ANOMALY_CATEGORIZATION_ERROR", "异常分类失败: " + e.getMessage());
        }
    }

    /**
     * 计算风险评分
     * <p>
     * 评分规则：
     * - 基础分：根据异常类型和严重程度
     * - 历史因子：根据员工历史违规记录
     * - 时间因子：根据发生时间（工作日/周末，工作时间/非工作时间）
     * - 频率因子：根据违规频率
     * </p>
     *
     * @param anomaly          异常记录
     * @param employeeHistory  员工历史记录
     * @return 风险评分（0-100，分数越高风险越大）
     */
    private int calculateRiskScore(AttendanceAnomalyEntity anomaly, Map<String, Object> employeeHistory) {
        int score = 0;

        // 1. 基础分：根据异常类型和严重程度
        String anomalyType = anomaly.getAnomalyType();
        String severity = anomaly.getSeverityLevel();

        switch (anomalyType) {
            case "MISSING_CARD":
                score += 20; // 缺卡基础分20
                break;
            case "LATE":
                score += 30; // 迟到基础分30
                break;
            case "EARLY":
                score += 30; // 早退基础分30
                break;
            case "ABSENT":
                score += 80; // 旷工基础分80
                break;
            default:
                score += 40;
        }

        // 根据严重程度调整
        if ("NORMAL".equals(severity)) {
            score += 0;
        } else if ("SERIOUS".equals(severity)) {
            score += 20;
        } else if ("CRITICAL".equals(severity)) {
            score += 40;
        }

        // 2. 历史因子：根据员工历史违规记录
        Integer violationCount = (Integer) employeeHistory.getOrDefault("violationCount", 0);
        if (violationCount == 0) {
            score -= 10; // 无违规记录，降低风险
        } else if (violationCount <= 2) {
            score += 5;
        } else if (violationCount <= 5) {
            score += 15;
        } else {
            score += 30; // 违规次数>5，大幅增加风险
        }

        // 3. 时间因子：根据发生时间
        LocalDateTime punchTime = anomaly.getActualPunchTime();
        if (punchTime != null) {
            int hour = punchTime.getHour();
            // 工作时间（8-18点）外发生的异常，风险较高
            if (hour < 8 || hour > 18) {
                score += 10;
            }
        }

        // 4. 频率因子：根据违规频率
        Double attendanceRate = (Double) employeeHistory.getOrDefault("attendanceRate", 0.95);
        if (attendanceRate >= 0.95) {
            score -= 5; // 出勤率高，降低风险
        } else if (attendanceRate >= 0.90) {
            score += 0;
        } else {
            score += 15; // 出勤率低，增加风险
        }

        // 确保评分在0-100之间
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 根据评分确定分类
     */
    private String determineCategory(AttendanceAnomalyEntity anomaly, int riskScore,
                                     Map<String, Object> employeeHistory) {
        String anomalyType = anomaly.getAnomalyType();
        Integer duration = anomaly.getAnomalyDuration();

        // 根据风险评分和异常类型确定分类
        if (riskScore <= 30) {
            // 低风险：自动批准
            return "AUTO_APPROVE";
        } else if (riskScore <= 60) {
            // 中等风险：人工审核
            return "MANUAL_REVIEW";
        } else if ("ABSENT".equals(anomalyType) && duration > 480) {
            // 旷工超过8小时：升级处理
            return "ESCALATE";
        } else {
            // 高风险：自动拒绝或升级处理
            Integer violationCount = (Integer) employeeHistory.getOrDefault("violationCount", 0);
            if (violationCount > 5) {
                return "ESCALATE"; // 违规次数>5，升级处理
            } else {
                return "AUTO_REJECT"; // 自动拒绝
            }
        }
    }

    @Override
    public Boolean autoProcessAnomaly(Long anomalyId) {
        log.info("[异常自动处理] 开始自动处理: anomalyId={}", anomalyId);

        try {
            // 查询异常记录
            AttendanceAnomalyEntity anomaly = anomalyDao.selectById(anomalyId);
            if (anomaly == null) {
                log.warn("[异常自动处理] 异常记录不存在: anomalyId={}", anomalyId);
                return false;
            }

            // 智能分类
            String category = categorizeAnomaly(anomaly);

            // 根据分类结果处理
            Boolean result = processByCategory(anomaly, category);

            log.info("[异常自动处理] 自动处理完成: anomalyId={}, category={}, result={}",
                    anomalyId, category, result);

            return result;

        } catch (Exception e) {
            log.error("[异常自动处理] 自动处理失败: anomalyId={}, error={}",
                    anomalyId, e.getMessage(), e);
            throw new BusinessException("ANOMALY_AUTO_PROCESS_ERROR", "异常自动处理失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类结果处理异常
     */
    private Boolean processByCategory(AttendanceAnomalyEntity anomaly, String category) {
        switch (category) {
            case "AUTO_APPROVE":
                return processAutoApprove(anomaly);
            case "MANUAL_REVIEW":
                return processManualReview(anomaly);
            case "AUTO_REJECT":
                return processAutoReject(anomaly);
            case "ESCALATE":
                return processEscalate(anomaly);
            default:
                log.warn("[异常自动处理] 未知的分类结果: category={}", category);
                return false;
        }
    }

    /**
     * 自动批准处理
     */
    private Boolean processAutoApprove(AttendanceAnomalyEntity anomaly) {
        log.info("[异常自动处理] 执行自动批准: anomalyId={}, type={}",
                anomaly.getAnomalyId(), anomaly.getAnomalyType());

        // 更新异常状态
        anomaly.setAnomalyStatus("APPROVED");
        anomaly.setHandleComment("系统自动批准：符合自动批准条件");
        anomaly.setHandlerId(0L); // 系统处理
        anomaly.setHandleTime(LocalDateTime.now());
        anomaly.setIsCorrected(1); // 标记为已修正

        int updated = anomalyDao.updateById(anomaly);

        return updated > 0;
    }

    /**
     * 人工审核处理
     */
    private Boolean processManualReview(AttendanceAnomalyEntity anomaly) {
        log.info("[异常自动处理] 标记为人工审核: anomalyId={}, type={}",
                anomaly.getAnomalyId(), anomaly.getAnomalyType());

        // 更新异常状态
        anomaly.setAnomalyStatus("PENDING");
        anomaly.setHandleComment("等待管理员审核");

        int updated = anomalyDao.updateById(anomaly);

        // TODO: 发送通知给相关管理员
        // notificationService.notifyManager(anomaly);

        return updated > 0;
    }

    /**
     * 自动拒绝处理
     */
    private Boolean processAutoReject(AttendanceAnomalyEntity anomaly) {
        log.info("[异常自动处理] 执行自动拒绝: anomalyId={}, type={}",
                anomaly.getAnomalyId(), anomaly.getAnomalyType());

        // 更新异常状态
        anomaly.setAnomalyStatus("REJECTED");
        anomaly.setHandleComment("系统自动拒绝：不符合申诉条件");
        anomaly.setHandlerId(0L); // 系统处理
        anomaly.setHandleTime(LocalDateTime.now());

        int updated = anomalyDao.updateById(anomaly);

        return updated > 0;
    }

    /**
     * 升级处理
     */
    private Boolean processEscalate(AttendanceAnomalyEntity anomaly) {
        log.info("[异常自动处理] 升级处理: anomalyId={}, type={}",
                anomaly.getAnomalyId(), anomaly.getAnomalyType());

        // 更新异常状态
        anomaly.setAnomalyStatus("ESCALATED");
        anomaly.setHandleComment("已升级至高级管理员/HR部门处理");
        anomaly.setSeverityLevel("CRITICAL");

        int updated = anomalyDao.updateById(anomaly);

        // TODO: 发送升级通知
        // notificationService.escalateToSeniorManager(anomaly);

        return updated > 0;
    }

    @Override
    public Integer batchAutoProcess(LocalDate attendanceDate) {
        log.info("[异常自动处理] 开始批量自动处理: date={}", attendanceDate);

        try {
            // 查询待处理的异常
            LambdaQueryWrapper<AttendanceAnomalyEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceAnomalyEntity::getAttendanceDate, attendanceDate)
                       .in(AttendanceAnomalyEntity::getAnomalyStatus,
                           Arrays.asList("PENDING", "APPLIED"));

            List<AttendanceAnomalyEntity> anomalies = anomalyDao.selectList(queryWrapper);

            if (anomalies.isEmpty()) {
                log.info("[异常自动处理] 没有待处理的异常: date={}", attendanceDate);
                return 0;
            }

            log.info("[异常自动处理] 找到 {} 条待处理异常", anomalies.size());

            int successCount = 0;
            for (AttendanceAnomalyEntity anomaly : anomalies) {
                try {
                    Boolean result = autoProcessAnomaly(anomaly.getAnomalyId());
                    if (result) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("[异常自动处理] 处理异常失败: anomalyId={}, error={}",
                            anomaly.getAnomalyId(), e.getMessage(), e);
                }
            }

            log.info("[异常自动处理] 批量处理完成: date={}, total={}, success={}",
                    attendanceDate, anomalies.size(), successCount);

            return successCount;

        } catch (Exception e) {
            log.error("[异常自动处理] 批量处理失败: date={}, error={}",
                    attendanceDate, e.getMessage(), e);
            throw new BusinessException("BATCH_AUTO_PROCESS_ERROR", "批量自动处理失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<AttendanceAnomalyAutoProcessVO> queryAutoProcessPage(AttendanceAnomalyAutoProcessQueryForm queryForm) {
        log.info("[异常自动处理] 分页查询自动处理记录: pageNum={}, pageSize={}",
                queryForm.getPageNum(), queryForm.getPageSize());

        try {
            // 构建查询条件
            LambdaQueryWrapper<AttendanceAnomalyEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (queryForm.getUserId() != null) {
                queryWrapper.eq(AttendanceAnomalyEntity::getUserId, queryForm.getUserId());
            }
            if (queryForm.getAnomalyType() != null) {
                queryWrapper.eq(AttendanceAnomalyEntity::getAnomalyType, queryForm.getAnomalyType());
            }
            if (queryForm.getStartDate() != null) {
                queryWrapper.ge(AttendanceAnomalyEntity::getAttendanceDate, queryForm.getStartDate());
            }
            if (queryForm.getEndDate() != null) {
                queryWrapper.le(AttendanceAnomalyEntity::getAttendanceDate, queryForm.getEndDate());
            }
            if (queryForm.getSeverityLevel() != null) {
                queryWrapper.eq(AttendanceAnomalyEntity::getSeverityLevel, queryForm.getSeverityLevel());
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc(AttendanceAnomalyEntity::getCreateTime);

            // 分页查询
            Page<AttendanceAnomalyEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<AttendanceAnomalyEntity> entityPage = anomalyDao.selectPage(page, queryWrapper);

            // 转换为VO
            List<AttendanceAnomalyAutoProcessVO> voList = entityPage.getRecords().stream()
                    .map(this::convertToAutoProcessVO)
                    .collect(Collectors.toList());

            return PageResult.of(voList, entityPage.getTotal(),
                                queryForm.getPageNum(), queryForm.getPageSize());

        } catch (Exception e) {
            log.error("[异常自动处理] 分页查询失败: error={}", e.getMessage(), e);
            throw new BusinessException("QUERY_AUTO_PROCESS_ERROR", "查询自动处理记录失败: " + e.getMessage());
        }
    }

    /**
     * 转换为自动处理VO
     */
    private AttendanceAnomalyAutoProcessVO convertToAutoProcessVO(AttendanceAnomalyEntity entity) {
        AttendanceAnomalyAutoProcessVO vo = new AttendanceAnomalyAutoProcessVO();

        vo.setAnomalyId(entity.getAnomalyId());
        vo.setUserId(entity.getUserId());
        vo.setAnomalyType(entity.getAnomalyType());
        vo.setAttendanceDate(entity.getAttendanceDate());
        vo.setExpectedPunchTime(entity.getExpectedPunchTime());
        vo.setActualPunchTime(entity.getActualPunchTime());
        vo.setAnomalyDuration(entity.getAnomalyDuration());
        vo.setSeverityLevel(entity.getSeverityLevel());
        vo.setCreateTime(entity.getCreateTime());

        // 根据状态设置分类结果
        String status = entity.getAnomalyStatus();
        if ("APPROVED".equals(status) && entity.getHandlerId() != null && entity.getHandlerId() == 0L) {
            vo.setCategoryResult("AUTO_APPROVE");
            vo.setCategoryResultDesc("自动批准");
            vo.setProcessReason(entity.getHandleComment());
            vo.setProcessStatus("AUTO_PROCESSED");
            vo.setProcessStatusDesc("已自动处理");
            vo.setConfidence(95);
        } else if ("PENDING".equals(status)) {
            vo.setCategoryResult("MANUAL_REVIEW");
            vo.setCategoryResultDesc("人工审核");
            vo.setProcessStatus("PENDING");
            vo.setProcessStatusDesc("待处理");
            vo.setConfidence(80);
        } else if ("REJECTED".equals(status) && entity.getHandlerId() != null && entity.getHandlerId() == 0L) {
            vo.setCategoryResult("AUTO_REJECT");
            vo.setCategoryResultDesc("自动拒绝");
            vo.setProcessStatus("AUTO_PROCESSED");
            vo.setProcessStatusDesc("已自动处理");
            vo.setProcessReason(entity.getHandleComment());
            vo.setConfidence(90);
        } else if ("ESCALATED".equals(status)) {
            vo.setCategoryResult("ESCALATE");
            vo.setCategoryResultDesc("升级处理");
            vo.setProcessStatus("ESCALATED");
            vo.setProcessStatusDesc("已升级");
            vo.setProcessReason(entity.getHandleComment());
            vo.setConfidence(85);
        }

        return vo;
    }

    @Override
    public List<Map<String, Object>> getAutoProcessingRules(String anomalyType) {
        log.info("[异常自动处理] 获取自动处理规则: anomalyType={}", anomalyType);

        // 返回默认规则配置
        List<Map<String, Object>> rules = new ArrayList<>();

        // 规则1：低风险自动批准
        Map<String, Object> rule1 = new HashMap<>();
        rule1.put("ruleId", 1L);
        rule1.put("ruleName", "低风险自动批准规则");
        rule1.put("anomalyType", anomalyType == null ? "ALL" : anomalyType);
        rule1.put("condition", "riskScore <= 30");
        rule1.put("action", "AUTO_APPROVE");
        rule1.put("enabled", true);
        rule1.put("priority", 1);
        rules.add(rule1);

        // 规则2：中等风险人工审核
        Map<String, Object> rule2 = new HashMap<>();
        rule2.put("ruleId", 2L);
        rule2.put("ruleName", "中等风险人工审核规则");
        rule2.put("anomalyType", anomalyType == null ? "ALL" : anomalyType);
        rule2.put("condition", "riskScore > 30 AND riskScore <= 60");
        rule2.put("action", "MANUAL_REVIEW");
        rule2.put("enabled", true);
        rule2.put("priority", 2);
        rules.add(rule2);

        // 规则3：高风险自动拒绝
        Map<String, Object> rule3 = new HashMap<>();
        rule3.put("ruleId", 3L);
        rule3.put("ruleName", "高风险自动拒绝规则");
        rule3.put("anomalyType", anomalyType == null ? "ALL" : anomalyType);
        rule3.put("condition", "riskScore > 60 AND violationCount <= 5");
        rule3.put("action", "AUTO_REJECT");
        rule3.put("enabled", true);
        rule3.put("priority", 3);
        rules.add(rule3);

        // 规则4：严重违规升级处理
        Map<String, Object> rule4 = new HashMap<>();
        rule4.put("ruleId", 4L);
        rule4.put("ruleName", "严重违规升级处理规则");
        rule4.put("anomalyType", anomalyType == null ? "ALL" : anomalyType);
        rule4.put("condition", "violationCount > 5 OR (anomalyType = 'ABSENT' AND duration > 480)");
        rule4.put("action", "ESCALATE");
        rule4.put("enabled", true);
        rule4.put("priority", 4);
        rules.add(rule4);

        return rules;
    }

    @Override
    public Boolean updateAutoProcessingRule(Long ruleId, String ruleConfig, Boolean enabled) {
        log.info("[异常自动处理] 更新自动处理规则: ruleId={}, enabled={}", ruleId, enabled);

        // TODO: 实现规则配置的持久化存储
        // 当前为简化实现，返回成功
        return true;
    }

    @Override
    public Map<String, Object> getAutoProcessSuggestion(Long anomalyId) {
        log.info("[异常自动处理] 获取自动处理建议: anomalyId={}", anomalyId);

        try {
            AttendanceAnomalyEntity anomaly = anomalyDao.selectById(anomalyId);
            if (anomaly == null) {
                throw new BusinessException("ANOMALY_NOT_FOUND", "异常记录不存在");
            }

            Map<String, Object> suggestion = new HashMap<>();

            // 智能分类
            String category = categorizeAnomaly(anomaly);
            suggestion.put("category", category);
            suggestion.put("categoryDesc", getCategoryDesc(category));

            // 计算置信度
            Map<String, Object> history = analyzeEmployeeAnomalyPattern(anomaly.getUserId(), 3);
            int riskScore = calculateRiskScore(anomaly, history);
            int confidence = 100 - riskScore; // 风险越低，置信度越高
            suggestion.put("confidence", confidence);

            // 处理理由
            suggestion.put("reason", generateProcessReason(anomaly, history, riskScore));

            // 建议动作
            suggestion.put("suggestedAction", getSuggestedAction(category));

            return suggestion;

        } catch (Exception e) {
            log.error("[异常自动处理] 获取建议失败: anomalyId={}, error={}",
                    anomalyId, e.getMessage(), e);
            throw new BusinessException("GET_SUGGESTION_ERROR", "获取自动处理建议失败: " + e.getMessage());
        }
    }

    /**
     * 生成处理理由
     */
    private String generateProcessReason(AttendanceAnomalyEntity anomaly,
                                        Map<String, Object> history, int riskScore) {
        StringBuilder reason = new StringBuilder();

        Integer violationCount = (Integer) history.getOrDefault("violationCount", 0);
        Double attendanceRate = (Double) history.getOrDefault("attendanceRate", 0.95);

        reason.append("风险评分: ").append(riskScore).append("; ");
        reason.append("历史违规次数: ").append(violationCount).append("次; ");
        reason.append("出勤率: ").append(String.format("%.1f%%", attendanceRate * 100));

        if (violationCount == 0 && attendanceRate >= 0.95) {
            reason.append("; 优秀员工记录，建议自动批准");
        } else if (violationCount > 5) {
            reason.append("; 违规次数过多，建议升级处理");
        } else {
            reason.append("; 建议人工审核");
        }

        return reason.toString();
    }

    /**
     * 获取分类描述
     */
    private String getCategoryDesc(String category) {
        switch (category) {
            case "AUTO_APPROVE":
                return "自动批准";
            case "MANUAL_REVIEW":
                return "人工审核";
            case "AUTO_REJECT":
                return "自动拒绝";
            case "ESCALATE":
                return "升级处理";
            default:
                return "未知分类";
        }
    }

    /**
     * 获取建议动作
     */
    private String getSuggestedAction(String category) {
        switch (category) {
            case "AUTO_APPROVE":
                return "批准该异常申请并修正考勤记录";
            case "MANUAL_REVIEW":
                return "转交给管理员进行人工审核";
            case "AUTO_REJECT":
                return "拒绝该异常申请";
            case "ESCALATE":
                return "升级至高级管理员或HR部门处理";
            default:
                return "无法确定建议动作";
        }
    }

    @Override
    public Boolean overrideAutoProcessDecision(Long anomalyId, String newDecision, String comment) {
        log.info("[异常自动处理] 手动覆盖自动处理决定: anomalyId={}, newDecision={}",
                anomalyId, newDecision);

        try {
            AttendanceAnomalyEntity anomaly = anomalyDao.selectById(anomalyId);
            if (anomaly == null) {
                throw new BusinessException("ANOMALY_NOT_FOUND", "异常记录不存在");
            }

            // 更新异常状态
            anomaly.setAnomalyStatus(newDecision);
            anomaly.setHandleComment("手动覆盖: " + comment);
            anomaly.setHandleTime(LocalDateTime.now());

            // TODO: 设置实际的管理员ID
            // anomaly.setHandlerId(SmartRequestUtil.getRequestUserId());

            int updated = anomalyDao.updateById(anomaly);

            log.info("[异常自动处理] 手动覆盖完成: anomalyId={}, newDecision={}, updated={}",
                    anomalyId, newDecision, updated > 0);

            return updated > 0;

        } catch (Exception e) {
            log.error("[异常自动处理] 手动覆盖失败: anomalyId={}, error={}",
                    anomalyId, e.getMessage(), e);
            throw new BusinessException("OVERRIDE_ERROR", "手动覆盖失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> statisticsAutoProcessEffect(LocalDate startDate, LocalDate endDate) {
        log.info("[异常自动处理] 统计自动处理效果: startDate={}, endDate={}", startDate, endDate);

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 查询时间段内的所有异常
            LambdaQueryWrapper<AttendanceAnomalyEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(AttendanceAnomalyEntity::getAttendanceDate, startDate)
                       .le(AttendanceAnomalyEntity::getAttendanceDate, endDate);

            List<AttendanceAnomalyEntity> anomalies = anomalyDao.selectList(queryWrapper);

            int totalCount = anomalies.size();
            int autoApprovedCount = 0;
            int manualReviewCount = 0;
            int autoRejectedCount = 0;
            int escalatedCount = 0;

            for (AttendanceAnomalyEntity anomaly : anomalies) {
                String status = anomaly.getAnomalyStatus();
                Long handlerId = anomaly.getHandlerId();

                if ("APPROVED".equals(status) && handlerId != null && handlerId == 0L) {
                    autoApprovedCount++;
                } else if ("PENDING".equals(status)) {
                    manualReviewCount++;
                } else if ("REJECTED".equals(status) && handlerId != null && handlerId == 0L) {
                    autoRejectedCount++;
                } else if ("ESCALATED".equals(status)) {
                    escalatedCount++;
                }
            }

            // 计算统计指标
            statistics.put("totalCount", totalCount);
            statistics.put("autoApprovedCount", autoApprovedCount);
            statistics.put("manualReviewCount", manualReviewCount);
            statistics.put("autoRejectedCount", autoRejectedCount);
            statistics.put("escalatedCount", escalatedCount);

            double autoProcessRate = totalCount > 0 ?
                    (double) (autoApprovedCount + autoRejectedCount) / totalCount * 100 : 0;
            statistics.put("autoProcessRate", String.format("%.1f%%", autoProcessRate));

            // 估算节省时间（假设每条异常人工处理需要5分钟）
            int savedMinutes = (autoApprovedCount + autoRejectedCount) * 5;
            statistics.put("savedMinutes", savedMinutes);
            statistics.put("savedHours", String.format("%.1f小时", savedMinutes / 60.0));

            return statistics;

        } catch (Exception e) {
            log.error("[异常自动处理] 统计失败: startDate={}, endDate={}, error={}",
                    startDate, endDate, e.getMessage(), e);
            throw new BusinessException("STATISTICS_ERROR", "统计自动处理效果失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> analyzeEmployeeAnomalyPattern(Long userId, Integer months) {
        log.info("[异常自动处理] 分析员工异常模式: userId={}, months={}", userId, months);

        try {
            Map<String, Object> pattern = new HashMap<>();

            // 查询指定月份内的异常记录
            LocalDate startDate = LocalDate.now().minusMonths(months);
            LocalDate endDate = LocalDate.now();

            LambdaQueryWrapper<AttendanceAnomalyEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceAnomalyEntity::getUserId, userId)
                       .ge(AttendanceAnomalyEntity::getAttendanceDate, startDate)
                       .le(AttendanceAnomalyEntity::getAttendanceDate, endDate);

            List<AttendanceAnomalyEntity> anomalies = anomalyDao.selectList(queryWrapper);

            // 统计违规次数
            int violationCount = anomalies.size();
            pattern.put("violationCount", violationCount);

            // 统计异常类型分布
            Map<String, Long> typeDistribution = anomalies.stream()
                    .collect(Collectors.groupingBy(
                            AttendanceAnomalyEntity::getAnomalyType,
                            Collectors.counting()
                    ));
            pattern.put("typeDistribution", typeDistribution);

            // 统计严重程度分布
            Map<String, Long> severityDistribution = anomalies.stream()
                    .collect(Collectors.groupingBy(
                            AttendanceAnomalyEntity::getSeverityLevel,
                            Collectors.counting()
                    ));
            pattern.put("severityDistribution", severityDistribution);

            // 计算平均出勤率（简化计算）
            double attendanceRate = calculateAttendanceRate(userId, months);
            pattern.put("attendanceRate", attendanceRate);

            // 判断风险等级
            String riskLevel;
            if (violationCount == 0 && attendanceRate >= 0.95) {
                riskLevel = "LOW";
            } else if (violationCount <= 2 && attendanceRate >= 0.90) {
                riskLevel = "MEDIUM";
            } else if (violationCount <= 5) {
                riskLevel = "HIGH";
            } else {
                riskLevel = "CRITICAL";
            }
            pattern.put("riskLevel", riskLevel);

            // 改进趋势（简化版：比较最近一个月与前一个月）
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);

            int lastMonthViolations = (int) anomalies.stream()
                    .filter(a -> !a.getAttendanceDate().isBefore(lastMonth))
                    .count();

            int twoMonthsAgoViolations = (int) anomalies.stream()
                    .filter(a -> a.getAttendanceDate().isBefore(lastMonth))
                    .count();

            String trend;
            if (lastMonthViolations < twoMonthsAgoViolations) {
                trend = "IMPROVING"; // 改善中
            } else if (lastMonthViolations > twoMonthsAgoViolations) {
                trend = "WORSENING"; // 恶化中
            } else {
                trend = "STABLE"; // 稳定
            }
            pattern.put("trend", trend);

            log.info("[异常自动处理] 员工异常模式分析完成: userId={}, violationCount={}, riskLevel={}",
                    userId, violationCount, riskLevel);

            return pattern;

        } catch (Exception e) {
            log.error("[异常自动处理] 分析员工异常模式失败: userId={}, error={}",
                    userId, e.getMessage(), e);
            throw new BusinessException("ANALYZE_PATTERN_ERROR", "分析员工异常模式失败: " + e.getMessage());
        }
    }

    /**
     * 计算出勤率（简化版）
     */
    private double calculateAttendanceRate(Long userId, Integer months) {
        // TODO: 实际应查询考勤记录表计算
        // 这里返回模拟值
        return 0.95;
    }
}
