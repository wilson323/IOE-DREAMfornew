package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.form.FlexibleWorkScheduleForm;
import net.lab1024.sa.attendance.domain.vo.FlexibleWorkScheduleVO;
import net.lab1024.sa.attendance.domain.vo.FlexibleWorkScheduleDetailVO;
import net.lab1024.sa.common.domain.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 弹性工作制服务接口
 * <p>
 * 提供弹性工作制的完整配置和管理功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
public interface FlexibleWorkScheduleService {

    // ==================== 弹性工作制配置管理 ====================

    /**
     * 创建弹性工作制配置
     *
     * @param form 弹性工作制配置表单
     * @return 配置ID
     */
    Long createFlexibleSchedule(FlexibleWorkScheduleForm form);

    /**
     * 更新弹性工作制配置
     *
     * @param scheduleId 配置ID
     * @param form 弹性工作制配置表单
     * @return 是否成功
     */
    Boolean updateFlexibleSchedule(Long scheduleId, FlexibleWorkScheduleForm form);

    /**
     * 删除弹性工作制配置
     *
     * @param scheduleId 配置ID
     * @return 是否成功
     */
    Boolean deleteFlexibleSchedule(Long scheduleId);

    /**
     * 获取弹性工作制配置详情
     *
     * @param scheduleId 配置ID
     * @return 配置详情
     */
    FlexibleWorkScheduleDetailVO getFlexibleScheduleDetail(Long scheduleId);

    /**
     * 分页查询弹性工作制配置
     *
     * @param form 查询表单
     * @return 分页结果
     */
    PageResult<FlexibleWorkScheduleVO> queryFlexibleSchedules(FlexibleWorkScheduleForm form);

    /**
     * 获取所有启用的弹性工作制配置
     *
     * @return 配置列表
     */
    List<FlexibleWorkScheduleVO> getAllActiveSchedules();

    // ==================== 弹性时间计算 ====================

    /**
     * 计算弹性工作制考勤状态
     *
     * @param scheduleId 配置ID
     * @param employeeId 员工ID
     * @param attendanceDate 考勤日期
     * @param checkInTime 实际签到时间
     * @param checkOutTime 实际签退时间
     * @return 考勤结果（正常/迟到/早退/缺卡等）
     */
    String calculateAttendanceStatus(Long scheduleId, Long employeeId,
                                     LocalDateTime attendanceDate,
                                     LocalDateTime checkInTime,
                                     LocalDateTime checkOutTime);

    /**
     * 验证弹性工作时间是否合规
     *
     * @param scheduleId 配置ID
     * @param checkInTime 签到时间
     * @param checkOutTime 签退时间
     * @return 验证结果（true-合规 false-不合规）
     */
    Boolean validateFlexibleTime(Long scheduleId, LocalDateTime checkInTime, LocalDateTime checkOutTime);

    /**
     * 计算弹性工作时长
     *
     * @param scheduleId 配置ID
     * @param checkInTime 签到时间
     * @param checkOutTime 签退时间
     * @return 工作时长（分钟）
     */
    Integer calculateWorkDuration(Long scheduleId, LocalDateTime checkInTime, LocalDateTime checkOutTime);

    // ==================== 弹性模式管理 ====================

    /**
     * 切换弹性工作模式
     *
     * @param scheduleId 配置ID
     * @param flexMode 目标弹性模式（STANDARD-标准弹性 FLEXIBLE-完全弹性 HYBRID-混合弹性）
     * @return 是否成功
     */
    Boolean switchFlexMode(Long scheduleId, String flexMode);

    /**
     * 启用弹性工作制
     *
     * @param scheduleId 配置ID
     * @return 是否成功
     */
    Boolean enableFlexibleSchedule(Long scheduleId);

    /**
     * 禁用弹性工作制
     *
     * @param scheduleId 配置ID
     * @return 是否成功
     */
    Boolean disableFlexibleSchedule(Long scheduleId);

    // ==================== 员工弹性工作制分配 ====================

    /**
     * 为员工分配弹性工作制
     *
     * @param scheduleId 配置ID
     * @param employeeIds 员工ID列表
     * @param effectiveTime 生效时间
     * @return 分配的员工数量
     */
    Integer assignToEmployees(Long scheduleId, List<Long> employeeIds, LocalDateTime effectiveTime);

    /**
     * 批量移除员工的弹性工作制
     *
     * @param scheduleId 配置ID
     * @param employeeIds 员工ID列表
     * @return 移除的员工数量
     */
    Integer removeFromEmployees(Long scheduleId, List<Long> employeeIds);

    /**
     * 获取员工的弹性工作制配置
     *
     * @param employeeId 员工ID
     * @param effectiveDate 生效日期
     * @return 弹性工作制配置
     */
    FlexibleWorkScheduleVO getEmployeeSchedule(Long employeeId, LocalDateTime effectiveDate);

    // ==================== 统计分析 ====================

    /**
     * 获取弹性工作制使用统计
     *
     * @param scheduleId 配置ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据（分配人数、使用率、迟到率等）
     */
    String getScheduleStatistics(Long scheduleId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取弹性工作制对比分析
     *
     * @param scheduleIds 配置ID列表
     * @return 对比分析数据
     */
    String compareSchedules(List<Long> scheduleIds);
}
