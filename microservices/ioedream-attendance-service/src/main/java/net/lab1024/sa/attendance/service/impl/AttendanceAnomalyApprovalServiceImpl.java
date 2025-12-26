package net.lab1024.sa.attendance.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyApplyDao;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.entity.AttendanceAnomalyApplyEntity;
import net.lab1024.sa.attendance.entity.AttendanceAnomalyEntity;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.service.AttendanceAnomalyApprovalService;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 考勤异常审批服务实现类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceAnomalyApprovalServiceImpl implements AttendanceAnomalyApprovalService {

    private final AttendanceAnomalyApplyDao applyDao;
    private final AttendanceAnomalyDao anomalyDao;
    private final AttendanceRecordDao recordDao;

    public AttendanceAnomalyApprovalServiceImpl(AttendanceAnomalyApplyDao applyDao,
                                                 AttendanceAnomalyDao anomalyDao,
                                                 AttendanceRecordDao recordDao) {
        this.applyDao = applyDao;
        this.anomalyDao = anomalyDao;
        this.recordDao = recordDao;
    }

    @Override
    public Boolean approveApply(Long applyId, Long approverId, String approverName, String comment) {
        log.info("[异常审批] 批准申请: applyId={}, approverId={}, approverName={}",
                applyId, approverId, approverName);

        try {
            // 1. 查询申请记录
            AttendanceAnomalyApplyEntity apply = applyDao.selectById(applyId);
            if (apply == null) {
                throw new BusinessException("APPLY_NOT_FOUND", "申请记录不存在");
            }

            // 2. 检查申请状态
            if (!"PENDING".equals(apply.getApplyStatus())) {
                throw new BusinessException("APPLY_ALREADY_PROCESSED", "申请已处理，无法重复审批");
            }

            // 3. 更新申请状态
            apply.setApplyStatus("APPROVED");
            apply.setApproverId(approverId);
            apply.setApproverName(approverName);
            apply.setApproveTime(LocalDateTime.now());
            apply.setApproveComment(comment);
            applyDao.updateById(apply);

            // 4. 更新异常记录状态
            if (apply.getAnomalyId() != null) {
                AttendanceAnomalyEntity anomaly = anomalyDao.selectById(apply.getAnomalyId());
                if (anomaly != null) {
                    anomaly.setAnomalyStatus("APPROVED");
                    anomaly.setHandlerId(approverId);
                    anomaly.setHandlerName(approverName);
                    anomaly.setHandleTime(LocalDateTime.now());
                    anomaly.setHandleComment(comment);
                    anomalyDao.updateById(anomaly);

                    // 5. 根据申请类型处理异常
                    processApprovedAnomaly(apply, anomaly, approverId, approverName);
                }
            }

            log.info("[异常审批] 申请批准成功: applyId={}, type={}", applyId, apply.getApplyType());
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[异常审批] 批准申请异常: applyId={}, error={}", applyId, e.getMessage(), e);
            throw new BusinessException("APPROVE_ERROR", "批准申请失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean rejectApply(Long applyId, Long approverId, String approverName, String comment) {
        log.info("[异常审批] 驳回申请: applyId={}, approverId={}, reason={}", applyId, approverId, comment);

        try {
            // 1. 查询申请记录
            AttendanceAnomalyApplyEntity apply = applyDao.selectById(applyId);
            if (apply == null) {
                throw new BusinessException("APPLY_NOT_FOUND", "申请记录不存在");
            }

            // 2. 检查申请状态
            if (!"PENDING".equals(apply.getApplyStatus())) {
                throw new BusinessException("APPLY_ALREADY_PROCESSED", "申请已处理，无法重复审批");
            }

            // 3. 更新申请状态
            apply.setApplyStatus("REJECTED");
            apply.setApproverId(approverId);
            apply.setApproverName(approverName);
            apply.setApproveTime(LocalDateTime.now());
            apply.setApproveComment(comment);
            applyDao.updateById(apply);

            // 4. 恢复异常记录状态为待处理
            if (apply.getAnomalyId() != null) {
                AttendanceAnomalyEntity anomaly = anomalyDao.selectById(apply.getAnomalyId());
                if (anomaly != null) {
                    anomaly.setAnomalyStatus("PENDING");
                    anomaly.setApplyId(null);
                    anomalyDao.updateById(anomaly);
                }
            }

            log.info("[异常审批] 申请驳回成功: applyId={}", applyId);
            return true;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[异常审批] 驳回申请异常: applyId={}, error={}", applyId, e.getMessage(), e);
            throw new BusinessException("REJECT_ERROR", "驳回申请失败: " + e.getMessage());
        }
    }

    @Override
    public Integer batchApprove(Long[] applyIds, Long approverId, String approverName,
                                String comment, Boolean approve) {
        log.info("[异常审批] 批量审批: count={}, approverId={}, approve={}",
                applyIds.length, approverId, approve);

        int successCount = 0;
        for (Long applyId : applyIds) {
            try {
                if (approve) {
                    approveApply(applyId, approverId, approverName, comment);
                } else {
                    rejectApply(applyId, approverId, approverName, comment);
                }
                successCount++;
            } catch (Exception e) {
                log.error("[异常审批] 批量审批单项失败: applyId={}, error={}", applyId, e.getMessage());
            }
        }

        log.info("[异常审批] 批量审批完成: total={}, success={}", applyIds.length, successCount);
        return successCount;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 处理已批准的异常
     */
    private void processApprovedAnomaly(AttendanceAnomalyApplyEntity apply,
                                        AttendanceAnomalyEntity anomaly,
                                        Long approverId,
                                        String approverName) {
        String applyType = apply.getApplyType();

        switch (applyType) {
            case "SUPPLEMENT_CARD":
                // 补卡申请：创建补充打卡记录
                createSupplementCardRecord(apply, anomaly);
                // 标记异常已修正
                anomaly.setIsCorrected(1);
                anomaly.setCorrectedTime(LocalDateTime.now());
                anomaly.setCorrectedPunchTime(apply.getAppliedPunchTime());
                anomalyDao.updateById(anomaly);
                break;

            case "LATE_EXPLANATION":
            case "EARLY_EXPLANATION":
                // 迟到/早退说明：仅更新状态，不修正考勤记录
                log.info("[异常审批] {}说明已批准: anomalyId={}", applyType, anomaly.getAnomalyId());
                break;

            case "ABSENT_APPEAL":
                // 旷工申诉：如果申诉成功，可以修正考勤记录
                log.info("[异常审批] 旷工申诉已批准: anomalyId={}", anomaly.getAnomalyId());
                // 可以根据实际情况创建打卡记录
                break;

            default:
                log.warn("[异常审批] 未知的申请类型: {}", applyType);
        }
    }

    /**
     * 创建补充打卡记录
     */
    private void createSupplementCardRecord(AttendanceAnomalyApplyEntity apply,
                                           AttendanceAnomalyEntity anomaly) {
        try {
            AttendanceRecordEntity record = new AttendanceRecordEntity();
            record.setUserId(anomaly.getUserId());
            record.setUserName(anomaly.getUserName());
            record.setDepartmentId(anomaly.getDepartmentId());
            record.setDepartmentName(anomaly.getDepartmentName());
            record.setShiftId(anomaly.getShiftId());
            record.setShiftName(anomaly.getShiftName());
            record.setAttendanceDate(apply.getAttendanceDate());
            record.setPunchTime(apply.getAppliedPunchTime());
            record.setAttendanceType(apply.getPunchType());
            record.setAttendanceStatus("NORMAL"); // 补卡后状态为正常
            record.setRemark("补卡 - 申请单号: " + apply.getApplyNo());

            recordDao.insert(record);

            log.info("[异常审批] 补卡记录创建成功: recordId={}, userId={}, time={}",
                    record.getRecordId(), record.getUserId(), record.getPunchTime());

        } catch (Exception e) {
            log.error("[异常审批] 创建补卡记录异常: applyId={}, error={}",
                    apply.getApplyId(), e.getMessage(), e);
            throw new BusinessException("SUPPLEMENT_CARD_CREATE_ERROR", "创建补卡记录失败: " + e.getMessage());
        }
    }
}
