package net.lab1024.sa.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.visitor.dao.SelfCheckOutDao;
import net.lab1024.sa.common.entity.visitor.SelfCheckOutEntity;
import net.lab1024.sa.visitor.manager.SelfCheckOutManager;
import net.lab1024.sa.visitor.service.SelfCheckOutService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * 自助签离服务实现类
 * <p>
 * 实现访客自助签离的完整业务功能
 * 严格遵循CLAUDE.md全局架构规范和Service实现标准
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class SelfCheckOutServiceImpl implements SelfCheckOutService {

    @Resource
    private SelfCheckOutManager selfCheckOutManager;

    @Resource
    private SelfCheckOutDao selfCheckOutDao;

    @Override
    public SelfCheckOutEntity performCheckOut(String visitorCode,
                                              String terminalId,
                                              String terminalLocation,
                                              Integer cardReturnStatus,
                                              String visitorCard) {
        log.info("[自助签离服务] 执行自助签离: visitorCode={}, terminalId={}, location={}",
                visitorCode, terminalId, terminalLocation);

        return selfCheckOutManager.processCheckOut(visitorCode, terminalId, terminalLocation,
                cardReturnStatus, visitorCard);
    }

    @Override
    public SelfCheckOutEntity manualCheckOut(String visitorCode,
                                             Long operatorId,
                                             String operatorName,
                                             String reason) {
        log.info("[自助签离服务] 人工签离: visitorCode={}, operator={}, reason={}",
                visitorCode, operatorName, reason);

        return selfCheckOutManager.manualCheckOut(visitorCode, operatorId, operatorName, reason);
    }

    @Override
    public SelfCheckOutEntity getCheckOutByVisitorCode(String visitorCode) {
        log.info("[自助签离服务] 查询签离记录: visitorCode={}", visitorCode);
        return selfCheckOutDao.selectByVisitorCode(visitorCode);
    }

    @Override
    public SelfCheckOutEntity getCheckOutByRegistrationId(Long registrationId) {
        log.info("[自助签离服务] 查询签离记录: registrationId={}", registrationId);
        return selfCheckOutDao.selectByRegistrationId(registrationId);
    }

    @Override
    public List<SelfCheckOutEntity> getCheckOutByTerminal(String terminalId, Integer limit) {
        log.info("[自助签离服务] 查询终端签离记录: terminalId={}, limit={}", terminalId, limit);
        return selfCheckOutDao.selectByTerminalId(terminalId, limit);
    }

    @Override
    public List<SelfCheckOutEntity> getOvertimeRecords(LocalDate startDate, LocalDate endDate) {
        log.info("[自助签离服务] 查询超时签离记录: startDate={}, endDate={}", startDate, endDate);
        return selfCheckOutDao.selectOvertimeRecords(startDate, endDate);
    }

    @Override
    public List<SelfCheckOutEntity> getUnreturnedCards() {
        log.info("[自助签离服务] 查询未归还访客卡记录");
        return selfCheckOutDao.selectUnreturnedCards();
    }

    @Override
    public PageResult<SelfCheckOutEntity> queryPage(Integer pageNum,
                                                     Integer pageSize,
                                                     String visitorName,
                                                     String intervieweeName,
                                                     Integer isOvertime,
                                                     LocalDate startDate,
                                                     LocalDate endDate) {
        log.info("[自助签离服务] 分页查询签离记录: pageNum={}, pageSize={}, visitorName={}, intervieweeName={}, isOvertime={}",
                pageNum, pageSize, visitorName, intervieweeName, isOvertime);

        LambdaQueryWrapper<SelfCheckOutEntity> queryWrapper =
                new LambdaQueryWrapper<SelfCheckOutEntity>()
                        .orderByDesc(SelfCheckOutEntity::getCheckOutTime);

        // 访客姓名模糊查询
        if (visitorName != null && !visitorName.isEmpty()) {
            queryWrapper.like(SelfCheckOutEntity::getVisitorName, visitorName);
        }

        // 被访人姓名模糊查询
        if (intervieweeName != null && !intervieweeName.isEmpty()) {
            queryWrapper.like(SelfCheckOutEntity::getIntervieweeName, intervieweeName);
        }

        // 是否超时
        if (isOvertime != null) {
            queryWrapper.eq(SelfCheckOutEntity::getIsOvertime, isOvertime);
        }

        // 日期范围查询
        if (startDate != null) {
            queryWrapper.ge(SelfCheckOutEntity::getCheckOutTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            queryWrapper.le(SelfCheckOutEntity::getCheckOutTime, endDate.atTime(23, 59, 59));
        }

        // 分页查询
        Page<SelfCheckOutEntity> page = new Page<>(pageNum, pageSize);
        selfCheckOutDao.selectPage(page, queryWrapper);

        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<SelfCheckOutEntity> getByInterviewee(Long intervieweeId,
                                                       LocalDate startDate,
                                                       LocalDate endDate) {
        log.info("[自助签离服务] 查询被访人签离记录: intervieweeId={}, startDate={}, endDate={}",
                intervieweeId, startDate, endDate);
        return selfCheckOutDao.selectByInterviewee(intervieweeId, startDate, endDate);
    }

    @Override
    public List<SelfCheckOutEntity> getByDate(LocalDate date) {
        log.info("[自助签离服务] 查询指定日期签离记录: date={}", date);
        return selfCheckOutDao.selectByDate(date);
    }

    @Override
    public Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("[自助签离服务] 查询统计信息: startDate={}, endDate={}", startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();

        // 总签离数
        int totalCount = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            totalCount += selfCheckOutDao.countByDate(date);
        }
        statistics.put("totalCount", totalCount);

        // 超时签离数
        int overtimeCount = selfCheckOutDao.countOvertime(startDate, endDate);
        statistics.put("overtimeCount", overtimeCount);

        // 未归还访客卡数
        int unreturnedCount = selfCheckOutDao.countUnreturnedCards();
        statistics.put("unreturnedCount", unreturnedCount);

        // 今日签离数
        int todayCount = selfCheckOutDao.countByDate(LocalDate.now());
        statistics.put("todayCount", todayCount);

        // 平均访问时长
        Double avgDuration = selfCheckOutDao.getAverageDuration(startDate, endDate);
        statistics.put("averageDuration", avgDuration != null ? avgDuration.intValue() : 0);

        log.info("[自助签离服务] 统计信息查询成功: totalCount={}, overtimeCount={}, unreturnedCount={}, todayCount={}, avgDuration={}分钟",
                totalCount, overtimeCount, unreturnedCount, todayCount,
                avgDuration != null ? avgDuration.intValue() : 0);

        return statistics;
    }

    @Override
    public void updateSatisfaction(Long checkOutId,
                                   Integer satisfactionScore,
                                   String visitorFeedback) {
        log.info("[自助签离服务] 更新满意度: checkOutId={}, score={}", checkOutId, satisfactionScore);

        selfCheckOutManager.updateSatisfaction(checkOutId, satisfactionScore, visitorFeedback);
    }

    @Override
    public void updateCardReturnStatus(Long checkOutId,
                                        Integer cardReturnStatus,
                                        String visitorCard) {
        log.info("[自助签离服务] 更新访客卡归还状态: checkOutId={}, status={}", checkOutId, cardReturnStatus);

        selfCheckOutManager.updateCardReturnStatus(checkOutId, cardReturnStatus, visitorCard);
    }

    @Override
    public Map<String, Object> getDurationStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("[自助签离服务] 查询访问时长统计: startDate={}, endDate={}", startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();

        // 查询所有签离记录
        List<SelfCheckOutEntity> records = selfCheckOutDao.selectList(
                new LambdaQueryWrapper<SelfCheckOutEntity>()
                        .isNotNull(SelfCheckOutEntity::getVisitDuration)
                        .ge(SelfCheckOutEntity::getCheckOutTime, startDate.atStartOfDay())
                        .le(SelfCheckOutEntity::getCheckOutTime, endDate.atTime(23, 59, 59)));

        if (records.isEmpty()) {
            statistics.put("averageDuration", 0);
            statistics.put("minDuration", 0);
            statistics.put("maxDuration", 0);
            return statistics;
        }

        // 平均时长
        OptionalDouble avgDuration = records.stream()
                .mapToInt(SelfCheckOutEntity::getVisitDuration)
                .average();
        statistics.put("averageDuration", avgDuration.isPresent() ? (int) avgDuration.getAsDouble() : 0);

        // 最小时长
        int minDuration = records.stream()
                .mapToInt(SelfCheckOutEntity::getVisitDuration)
                .min()
                .orElse(0);
        statistics.put("minDuration", minDuration);

        // 最大时长
        int maxDuration = records.stream()
                .mapToInt(SelfCheckOutEntity::getVisitDuration)
                .max()
                .orElse(0);
        statistics.put("maxDuration", maxDuration);

        log.info("[自助签离服务] 访问时长统计: avg={}, min={}, max={}",
                avgDuration.isPresent() ? (int) avgDuration.getAsDouble() : 0,
                minDuration, maxDuration);

        return statistics;
    }

    @Override
    public Map<String, Object> getSatisfactionStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("[自助签离服务] 查询满意度统计: startDate={}, endDate={}", startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();

        // 查询有满意度评分的记录
        List<SelfCheckOutEntity> records = selfCheckOutDao.selectList(
                new LambdaQueryWrapper<SelfCheckOutEntity>()
                        .isNotNull(SelfCheckOutEntity::getSatisfactionScore)
                        .ge(SelfCheckOutEntity::getCheckOutTime, startDate.atStartOfDay())
                        .le(SelfCheckOutEntity::getCheckOutTime, endDate.atTime(23, 59, 59)));

        if (records.isEmpty()) {
            statistics.put("totalCount", 0);
            statistics.put("averageScore", 0.0);
            statistics.put("score5Count", 0);
            statistics.put("score4Count", 0);
            statistics.put("score3Count", 0);
            statistics.put("score2Count", 0);
            statistics.put("score1Count", 0);
            return statistics;
        }

        // 总评价数
        statistics.put("totalCount", records.size());

        // 平均评分
        OptionalDouble avgScore = records.stream()
                .mapToInt(SelfCheckOutEntity::getSatisfactionScore)
                .average();
        statistics.put("averageScore", avgScore.isPresent() ? avgScore.getAsDouble() : 0.0);

        // 各星级评分数量
        long score5Count = records.stream().filter(r -> r.getSatisfactionScore() == 5).count();
        long score4Count = records.stream().filter(r -> r.getSatisfactionScore() == 4).count();
        long score3Count = records.stream().filter(r -> r.getSatisfactionScore() == 3).count();
        long score2Count = records.stream().filter(r -> r.getSatisfactionScore() == 2).count();
        long score1Count = records.stream().filter(r -> r.getSatisfactionScore() == 1).count();

        statistics.put("score5Count", score5Count);
        statistics.put("score4Count", score4Count);
        statistics.put("score3Count", score3Count);
        statistics.put("score2Count", score2Count);
        statistics.put("score1Count", score1Count);

        // 满意率（4分和5分占比）
        double satisfactionRate = (double) (score5Count + score4Count) / records.size() * 100;
        statistics.put("satisfactionRate", satisfactionRate);

        log.info("[自助签离服务] 满意度统计: totalCount={}, avgScore={}, satisfactionRate={}%",
                records.size(), avgScore.isPresent() ? avgScore.getAsDouble() : 0.0, satisfactionRate);

        return statistics;
    }
}
