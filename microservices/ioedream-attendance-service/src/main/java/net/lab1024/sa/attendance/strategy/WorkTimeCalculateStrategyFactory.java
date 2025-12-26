package net.lab1024.sa.attendance.strategy;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.attendance.WorkShiftEntity;
import net.lab1024.sa.attendance.strategy.impl.StandardWorkTimeStrategy;
import net.lab1024.sa.attendance.strategy.impl.RotatingWorkTimeStrategy;
import net.lab1024.sa.attendance.strategy.impl.FlexibleWorkTimeStrategy;

/**
 * 工时计算策略工厂
 * <p>
 * 根据班次类型自动选择合适的计算策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class WorkTimeCalculateStrategyFactory {

    /**
     * 策略类型常量
     */
    public static final String STRATEGY_STANDARD = "STANDARD";
    public static final String STRATEGY_ROTATING = "ROTATING";
    public static final String STRATEGY_FLEXIBLE = "FLEXIBLE";

    /**
     * 获取计算策略
     * <p>
     * 根据班次类型自动选择合适的计算策略：
     * - 白班/夜班 → 标准工时制
     * - 轮班 → 轮班制
     * - 弹性班 → 弹性工作制
     * </p>
     *
     * @param workShift 班次配置
     * @return 计算策略
     */
    public WorkTimeCalculateStrategy getStrategy(WorkShiftEntity workShift) {
        if (workShift == null) {
            log.warn("[策略工厂] 班次配置为空，使用默认标准工时策略");
            return new StandardWorkTimeStrategy();
        }

        // 根据班次类型选择策略
        // 检查是否是弹性班次（shiftType == 4 或者配置了弹性时间）
        boolean isFlexibleShift = workShift.getShiftType() != null &&
                workShift.getShiftType().equals(WorkShiftEntity.ShiftType.FLEXIBLE_SHIFT.getCode());
        boolean hasFlexibleTimeConfig = workShift.getFlexStartEarliest() != null &&
                workShift.getFlexStartLatest() != null;

        if (isFlexibleShift || hasFlexibleTimeConfig) {
            log.debug("[策略工厂] 选择弹性工作制策略: shiftId={}, shiftName={}",
                    workShift.getShiftId(), workShift.getShiftName());
            return new FlexibleWorkTimeStrategy();
        }

        if (workShift.getShiftType() != null) {
            WorkShiftEntity.ShiftType shiftType = WorkShiftEntity.ShiftType.fromCode(workShift.getShiftType());
            return switch (shiftType) {
                case ROTATING_SHIFT -> {
                    log.debug("[策略工厂] 选择轮班制策略: shiftId={}, shiftName={}",
                            workShift.getShiftId(), workShift.getShiftName());
                    yield new RotatingWorkTimeStrategy();
                }
                case FLEXIBLE_SHIFT -> {
                    log.debug("[策略工厂] 选择弹性工作制策略: shiftId={}, shiftName={}",
                            workShift.getShiftId(), workShift.getShiftName());
                    yield new FlexibleWorkTimeStrategy();
                }
                default -> {
                    log.debug("[策略工厂] 选择标准工时制策略: shiftId={}, shiftName={}",
                            workShift.getShiftId(), workShift.getShiftName());
                    yield new StandardWorkTimeStrategy();
                }
            };
        }

        // 默认使用标准工时制
        log.debug("[策略工厂] 使用默认标准工时制策略: shiftId={}, shiftName={}",
                workShift.getShiftId(), workShift.getShiftName());
        return new StandardWorkTimeStrategy();
    }

    /**
     * 根据策略类型获取策略实例
     *
     * @param strategyType 策略类型
     * @return 策略实例
     */
    public WorkTimeCalculateStrategy getStrategyByType(String strategyType) {
        return switch (strategyType) {
            case STRATEGY_ROTATING -> new RotatingWorkTimeStrategy();
            case STRATEGY_FLEXIBLE -> new FlexibleWorkTimeStrategy();
            default -> new StandardWorkTimeStrategy();
        };
    }
}
