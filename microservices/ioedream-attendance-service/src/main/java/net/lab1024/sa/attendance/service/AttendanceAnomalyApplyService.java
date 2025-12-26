package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.entity.AttendanceAnomalyApplyEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤异常申请服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AttendanceAnomalyApplyService {

    /**
     * 创建补卡申请
     *
     * @param apply 申请实体
     * @return 申请ID
     */
    Long createSupplementCardApply(AttendanceAnomalyApplyEntity apply);

    /**
     * 创建迟到说明申请
     *
     * @param apply 申请实体
     * @return 申请ID
     */
    Long createLateExplanationApply(AttendanceAnomalyApplyEntity apply);

    /**
     * 创建早退说明申请
     *
     * @param apply 申请实体
     * @return 申请ID
     */
    Long createEarlyExplanationApply(AttendanceAnomalyApplyEntity apply);

    /**
     * 创建旷工申诉申请
     *
     * @param apply 申请实体
     * @return 申请ID
     */
    Long createAbsentAppealApply(AttendanceAnomalyApplyEntity apply);

    /**
     * 撤销申请
     *
     * @param applyId 申请ID
     * @param userId 申请人ID
     * @return 是否成功
     */
    Boolean cancelApply(Long applyId, Long userId);

    /**
     * 根据ID查询申请
     *
     * @param applyId 申请ID
     * @return 申请实体
     */
    AttendanceAnomalyApplyEntity getApplyById(Long applyId);

    /**
     * 查询用户的申请列表
     *
     * @param userId 用户ID
     * @return 申请列表
     */
    List<AttendanceAnomalyApplyEntity> getAppliesByUserId(Long userId);

    /**
     * 查询待审批的申请列表
     *
     * @return 待审批申请列表
     */
    List<AttendanceAnomalyApplyEntity> getPendingApplies();

    /**
     * 检查是否允许补卡（检查每月补卡次数限制）
     *
     * @param userId 用户ID
     * @param attendanceDate 考勤日期
     * @return 是否允许补卡
     */
    Boolean checkSupplementCardAllowed(Long userId, LocalDate attendanceDate);
}
