package net.lab1024.sa.attendance.engine.conflict;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.ConflictDetectionResult;
import net.lab1024.sa.attendance.engine.model.ConflictResolution;
import net.lab1024.sa.attendance.engine.model.ScheduleConflict;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排班冲突处理服务（P2-Batch3阶段1创建）
 * <p>
 * 负责检测和解决排班冲突，包括技能冲突、工时冲突、容量冲突等
 * 注意：这是一个纯Java类，通过@Configuration类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class ScheduleConflictService {

    private final ConflictDetector conflictDetector;
    private final ConflictResolver conflictResolver;

    /**
     * 构造函数注入依赖
     */
    public ScheduleConflictService(ConflictDetector conflictDetector,
            ConflictResolver conflictResolver) {
        this.conflictDetector = conflictDetector;
        this.conflictResolver = conflictResolver;
    }

    /**
     * 检测排班冲突
     * <p>
     * 检测排班数据中的各种冲突，包括技能冲突、工时冲突、容量冲突等
     * </p>
     *
     * @param scheduleData 排班数据
     * @return 冲突检测结果
     */
    public ConflictDetectionResult detectConflicts(ScheduleData scheduleData) {
        log.debug("[冲突处理服务] 检测排班冲突");

        // 从ScheduleData中提取排班记录列表
        List<ScheduleRecord> scheduleRecords = convertToModelRecords(
                scheduleData.getHistoryRecords() != null
                        ? scheduleData.getHistoryRecords()
                        : new ArrayList<>());

        // 调用ConflictDetector检测冲突
        net.lab1024.sa.attendance.engine.conflict.ConflictDetectionResult conflictResult =
                conflictDetector.detectConflicts(scheduleRecords, scheduleData);

        // 转换为model包中的ConflictDetectionResult
        ConflictDetectionResult modelResult = ConflictDetectionResult.builder()
                .detectionId(conflictResult.getDetectionId())
                .hasConflicts(conflictResult.getHasConflicts())
                .totalConflicts(conflictResult.getTotalConflicts())
                .skillConflicts(0) // 暂时设为0，后续完善
                .workHourConflicts(0) // 暂时设为0，后续完善
                .capacityConflicts(0) // 暂时设为0，后续完善
                .otherConflicts(0) // 暂时设为0，后续完善
                .conflicts(convertToModelConflicts(conflictResult.getConflicts()))
                .build();

        log.info("[冲突处理服务] 冲突检测完成, 是否存在冲突: {}, 冲突数量: {}",
                modelResult.getHasConflicts(), modelResult.getTotalConflicts());

        return modelResult;
    }

    /**
     * 解决排班冲突
     * <p>
     * 根据指定的解决策略解决排班冲突
     * </p>
     *
     * @param conflicts           冲突列表
     * @param resolutionStrategy 解决策略（如：优先级、公平性、成本最优等）
     * @return 冲突解决方案
     */
    public ConflictResolution resolveConflicts(List<ScheduleConflict> conflicts,
            String resolutionStrategy) {
        log.debug("[冲突处理服务] 解决排班冲突, 冲突数量: {}, 策略: {}",
                conflicts.size(), resolutionStrategy);

        // 调用ConflictResolver解决冲突
        net.lab1024.sa.attendance.engine.conflict.ConflictResolution resolution =
                conflictResolver.resolveConflicts(conflicts, resolutionStrategy);

        // 转换为model包中的ConflictResolution
        ConflictResolution modelResolution = new ConflictResolution();
        modelResolution.setResolutionId(resolution.getResolutionId());
        modelResolution.setResolutionSuccessful(resolution.getResolutionSuccessful());
        modelResolution.setResolutionStrategy(resolutionStrategy);
        modelResolution.setResolvedConflicts(resolution.getResolvedConflicts());
        modelResolution.setUnresolvedConflicts(resolution.getUnresolvedConflicts());
        modelResolution.setResolutionDetails(resolution.getResolutionDetails());

        log.info("[冲突处理服务] 冲突解决完成, 解决成功: {}, 已解决: {}, 未解决: {}",
                modelResolution.getResolutionSuccessful(),
                modelResolution.getResolvedConflicts(),
                modelResolution.getUnresolvedConflicts());

        return modelResolution;
    }

    /**
     * 应用冲突解决方案到排班结果
     * <p>
     * 将冲突解决方案应用到排班结果中，更新排班记录
     * </p>
     *
     * @param result    排班结果
     * @param resolution 冲突解决方案
     * @return 应用解决方案后的排班结果
     */
    public ScheduleResult applyResolution(ScheduleResult result, ConflictResolution resolution) {
        log.debug("[冲突处理服务] 应用冲突解决方案");

        if (resolution == null) {
            log.warn("[冲突处理服务] 冲突解决方案为空，保持原排班结果");
            return result;
        }

        if (!resolution.getResolutionSuccessful()) {
            log.warn("[冲突处理服务] 冲突解决失败，保持原排班结果");
            return result;
        }

        // 应用解决方案到排班结果
        // TODO: 实现具体的冲突解决应用逻辑
        // 1. 根据resolution中的解决方案更新排班记录
        // 2. 处理已解决的冲突
        // 3. 记录未解决的冲突

        log.info("[冲突处理服务] 冲突解决方案已应用");

        return result;
    }

    /**
     * 转换为模型记录列表
     */
    private List<ScheduleRecord> convertToModelRecords(List<Object> historyRecords) {
        if (historyRecords == null || historyRecords.isEmpty()) {
            return new ArrayList<>();
        }

        // TODO: 实现记录转换逻辑
        // 当前返回空列表，后续根据实际数据结构完善
        return new ArrayList<>();
    }

    /**
     * 转换为模型冲突列表
     */
    private List<ConflictDetectionResult.ScheduleConflict> convertToModelConflicts(
            List<net.lab1024.sa.attendance.engine.conflict.ScheduleConflict> conflicts) {
        if (conflicts == null || conflicts.isEmpty()) {
            return new ArrayList<>();
        }

        return conflicts.stream().map(conf -> {
            ConflictDetectionResult.ScheduleConflict modelConflict =
                    new ConflictDetectionResult.ScheduleConflict();
            modelConflict.setConflictId(conf.getConflictId());
            modelConflict.setConflictType(conf.getConflictType());
            modelConflict.setDescription(conf.getDescription());
            modelConflict.setSeverity(conf.getSeverity());
            modelConflict.setAffectedEmployees(conf.getAffectedEmployees());
            modelConflict.setConflictDate(conf.getConflictDate());
            modelConflict.setTimeSlots(conf.getTimeSlots());
            return modelConflict;
        }).collect(Collectors.toList());
    }
}
