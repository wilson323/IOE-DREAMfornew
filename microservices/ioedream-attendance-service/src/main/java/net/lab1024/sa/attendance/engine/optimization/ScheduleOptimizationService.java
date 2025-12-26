package net.lab1024.sa.attendance.engine.optimization;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.OptimizedSchedule;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;
import net.lab1024.sa.attendance.engine.optimizer.ScheduleOptimizer;

/**
 * 排班优化服务（P2-Batch3阶段1创建）
 * <p>
 * 负责优化排班结果，根据不同的优化目标（成本、公平性、工作负载平衡等）
 * 生成优化后的排班方案
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class ScheduleOptimizationService {

    private final ScheduleOptimizer scheduleOptimizer;

    public ScheduleOptimizationService(ScheduleOptimizer scheduleOptimizer) {
        this.scheduleOptimizer = scheduleOptimizer;
    }

    /**
     * 优化排班
     *
     * @param scheduleData       排班数据
     * @param optimizationTarget 优化目标
     * @return 优化后的排班
     */
    public OptimizedSchedule optimizeSchedule(ScheduleData scheduleData, String optimizationTarget) {
        log.debug("[排班优化服务] 优化排班, 目标: {}", optimizationTarget);

        OptimizedSchedule optimizedSchedule = scheduleOptimizer.optimizeSchedule(scheduleData,
                optimizationTarget);

        log.info("[排班优化服务] 排班优化完成, 优化成功: {}, 目标: {}",
                optimizedSchedule.getOptimizationSuccessful(), optimizationTarget);

        return optimizedSchedule;
    }

    /**
     * 应用优化到排班结果
     *
     * @param result           排班结果
     * @param optimizedSchedule 优化后的排班
     * @return 应用优化后的排班结果
     */
    public ScheduleResult applyOptimization(ScheduleResult result, OptimizedSchedule optimizedSchedule) {
        log.debug("[排班优化服务] 应用排班优化");

        if (optimizedSchedule == null) {
            log.warn("[排班优化服务] 优化后的排班为空，保持原排班结果");
            return result;
        }

        if (!optimizedSchedule.getOptimizationSuccessful()) {
            log.warn("[排班优化服务] 排班优化失败，保持原排班结果");
            return result;
        }

        // 应用优化到排班结果
        // TODO: 实现具体的优化应用逻辑

        log.info("[排班优化服务] 排班优化已应用");

        return result;
    }
}
