package net.lab1024.sa.attendance.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.conflict.ScheduleConflict;
import net.lab1024.sa.attendance.engine.conflict.ScheduleConflictDetector;
import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.service.ScheduleConflictService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排班冲突服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class ScheduleConflictServiceImpl implements ScheduleConflictService {

    @Resource
    private ScheduleConflictDetector conflictDetector;

    @Override
    public List<ScheduleConflict> detectConflicts(Chromosome chromosome, OptimizationConfig config) {
        log.info("[冲突服务] 检测排班冲突");
        return conflictDetector.detectConflicts(chromosome, config);
    }

    @Override
    public List<ScheduleConflict> detectEmployeeConflicts(Chromosome chromosome, OptimizationConfig config) {
        log.info("[冲突服务] 检测员工相关冲突");
        return conflictDetector.detectEmployeeConflicts(chromosome, config);
    }

    @Override
    public List<ScheduleConflict> detectShiftConflicts(Chromosome chromosome, OptimizationConfig config) {
        log.info("[冲突服务] 检测班次相关冲突");
        return conflictDetector.detectShiftConflicts(chromosome, config);
    }

    @Override
    public List<ScheduleConflict> detectDateConflicts(Chromosome chromosome, OptimizationConfig config) {
        log.info("[冲突服务] 检测日期相关冲突");
        return conflictDetector.detectDateConflicts(chromosome, config);
    }

    @Override
    public Chromosome resolveConflicts(List<ScheduleConflict> conflicts, OptimizationConfig config) {
        log.info("[冲突服务] 自动解决冲突: count={}", conflicts.size());

        // TODO: 实现冲突自动解决功能
        // 1. 根据冲突严重程度排序
        // 2. 优先解决严重冲突
        // 3. 调整排班方案
        // 4. 重新检测冲突

        throw new UnsupportedOperationException("冲突自动解决功能待实现");
    }

    @Override
    public Map<String, Object> generateConflictReport(List<ScheduleConflict> conflicts) {
        log.info("[冲突服务] 生成冲突报告: count={}", conflicts.size());

        Map<String, Object> report = new HashMap<>();

        // 基本统计
        report.put("totalConflicts", conflicts.size());
        report.put("summary", conflictDetector.generateConflictSummary(conflicts));

        // 严重程度分布
        Map<Integer, Integer> severityDist = conflictDetector.getConflictSeverityDistribution(conflicts);
        report.put("severityDistribution", severityDist);
        report.put("severeConflicts", severityDist.get(4));
        report.put("highConflicts", severityDist.get(3));
        report.put("mediumConflicts", severityDist.get(2));
        report.put("lowConflicts", severityDist.get(1));

        // 类型分布
        Map<ScheduleConflict.ConflictType, Integer> typeDist = conflictDetector.getConflictTypeDistribution(conflicts);
        Map<String, Integer> typeDistStr = new HashMap<>();
        typeDist.forEach((type, count) -> typeDistStr.put(type.getCode(), count));
        report.put("typeDistribution", typeDistStr);

        // 需要解决的冲突
        long needResolution = conflicts.stream().filter(ScheduleConflict::needsResolution).count();
        report.put("needResolution", needResolution);

        // 严重冲突
        long severeCount = conflicts.stream().filter(ScheduleConflict::isSevere).count();
        report.put("severeConflictsCount", severeCount);

        return report;
    }

    @Override
    public Map<Integer, Integer> getSeverityDistribution(List<ScheduleConflict> conflicts) {
        return conflictDetector.getConflictSeverityDistribution(conflicts);
    }

    @Override
    public Map<String, Integer> getTypeDistribution(List<ScheduleConflict> conflicts) {
        Map<ScheduleConflict.ConflictType, Integer> distribution =
                conflictDetector.getConflictTypeDistribution(conflicts);

        Map<String, Integer> result = new HashMap<>();
        distribution.forEach((type, count) -> result.put(type.getCode(), count));

        return result;
    }

    @Override
    public String generateConflictSummary(List<ScheduleConflict> conflicts) {
        return conflictDetector.generateConflictSummary(conflicts);
    }
}
