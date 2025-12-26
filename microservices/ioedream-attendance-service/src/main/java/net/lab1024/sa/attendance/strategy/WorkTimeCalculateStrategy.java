package net.lab1024.sa.attendance.strategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import net.lab1024.sa.attendance.entity.WorkShiftEntity;
import net.lab1024.sa.attendance.strategy.model.CalculateContext;
import net.lab1024.sa.attendance.strategy.model.CalculateResult;
import net.lab1024.sa.attendance.strategy.model.PunchRecord;
import net.lab1024.sa.attendance.strategy.model.WorkTimeSpan;

/**
 * 工时计算策略接口
 * <p>
 * 定义不同工时制度的计算方法：
 * 1. 标准工时制：固定上下班时间
 * 2. 轮班制：三班倒、四班三倒等
 * 3. 弹性工作制：弹性上下班时间
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
public interface WorkTimeCalculateStrategy {

    /**
     * 获取策略类型
     *
     * @return 策略类型（STANDARD, ROTATING, FLEXIBLE）
     */
    String getStrategyType();

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getStrategyName();

    /**
     * 计算工时
     * <p>
     * 根据班次配置和打卡记录，计算实际工时、加班时长等信息
     * </p>
     *
     * @param context 计算上下文
     * @return 计算结果
     */
    CalculateResult calculate(CalculateContext context);

    /**
     * 验证打卡记录
     * <p>
     * 验证打卡记录是否有效（如：是否在允许的时间范围内、是否满足最小打卡次数等）
     * </p>
     *
     * @param punchRecords 打卡记录列表
     * @param workShift   班次配置
     * @return 验证结果
     */
    boolean validatePunchRecords(List<PunchRecord> punchRecords, WorkShiftEntity workShift);

    /**
     * 计算迟到时长
     * <p>
     * 计算上班打卡迟到的时长（分钟）
     * </p>
     *
     * @param punchTime 实际打卡时间
     * @param workShift 班次配置
     * @return 迟到时长（分钟），负数表示不迟到
     */
    Integer calculateLateMinutes(LocalDateTime punchTime, WorkShiftEntity workShift);

    /**
     * 计算早退时长
     * <p>
     * 计算下班打卡早退的时长（分钟）
     * </p>
     *
     * @param punchTime 实际打卡时间
     * @param workShift 班次配置
     * @return 早退时长（分钟），负数表示不早退
     */
    Integer calculateEarlyLeaveMinutes(LocalDateTime punchTime, WorkShiftEntity workShift);

    /**
     * 计算加班时长
     * <p>
     * 计算超出正常工作时间的加班时长（分钟）
     * </p>
     *
     * @param endTime   实际下班打卡时间
     * @param workShift 班次配置
     * @return 加班时长（分钟）
     */
    Integer calculateOvertimeMinutes(LocalDateTime endTime, WorkShiftEntity workShift);

    /**
     * 计算工时时段
     * <p>
     * 根据打卡记录计算有效的工时时段
     * </p>
     *
     * @param punchRecords 打卡记录列表
     * @param workShift   班次配置
     * @return 工时时段列表
     */
    List<WorkTimeSpan> calculateWorkTimeSpans(List<PunchRecord> punchRecords, WorkShiftEntity workShift);

    /**
     * 判断是否在有效工作时段内
     *
     * @param punchTime 打卡时间
     * @param workShift 班次配置
     * @return 是否有效
     */
    boolean isValidWorkTime(LocalDateTime punchTime, WorkShiftEntity workShift);

    /**
     * 获取允许的打卡时间范围
     *
     * @param workShift 班次配置
     * @return 允许的打卡时间范围
     */
    WorkTimeSpan getAllowedPunchTimeRange(WorkShiftEntity workShift);

    /**
     * 计算核心工作时长
     * <p>
     * 计算必须工作的核心时长（不包含休息时间）
     * </p>
     *
     * @param workShift 班次配置
     * @return 核心工作时长（分钟）
     */
    Integer calculateCoreWorkMinutes(WorkShiftEntity workShift);

    /**
     * 计算休息时长
     * <p>
     * 计算扣除的休息时长
     * </p>
     *
     * @param workShift 班次配置
     * @return 休息时长（分钟）
     */
    Integer calculateBreakMinutes(WorkShiftEntity workShift);

    /**
     * 判断是否跨天班次
     *
     * @param workShift 班次配置
     * @return 是否跨天
     */
    boolean isOvernightShift(WorkShiftEntity workShift);

    /**
     * 计算跨天班次的结束时间
     * <p>
     * 如果是跨天班次，结束时间在第二天
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 实际结束日期时间
     */
    LocalDateTime calculateOvernightEndTime(LocalDateTime startTime, LocalTime endTime);
}
