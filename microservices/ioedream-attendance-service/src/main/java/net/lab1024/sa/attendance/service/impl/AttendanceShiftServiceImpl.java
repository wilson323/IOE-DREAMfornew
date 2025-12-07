package net.lab1024.sa.attendance.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.workflow.constant.BusinessTypeEnum;
import net.lab1024.sa.common.workflow.constant.WorkflowDefinitionConstants;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.attendance.dao.AttendanceShiftDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceShiftEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceShiftForm;
import net.lab1024.sa.attendance.service.AttendanceShiftService;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceShiftServiceImpl implements AttendanceShiftService {

    @Resource
    private AttendanceShiftDao attendanceShiftDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private net.lab1024.sa.common.attendance.manager.AttendanceManager attendanceManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceShiftEntity submitShiftApplication(AttendanceShiftForm form) {
        log.info("[调班申请] 提交调班申请，employeeId={}, shiftDate={}", form.getEmployeeId(), form.getShiftDate());

        AttendanceShiftEntity entity = new AttendanceShiftEntity();
        entity.setShiftNo(generateShiftNo());
        entity.setEmployeeId(form.getEmployeeId());
        // 从员工服务获取员工姓名
        String employeeName = attendanceManager.getUserName(form.getEmployeeId());
        entity.setEmployeeName(employeeName);
        entity.setShiftDate(form.getShiftDate());
        entity.setOriginalShiftId(form.getOriginalShiftId());
        entity.setTargetShiftId(form.getTargetShiftId());
        entity.setReason(form.getReason());
        entity.setRemark(form.getRemark());
        entity.setStatus("PENDING");
        attendanceShiftDao.insert(entity);

        Map<String, Object> formData = new HashMap<>();
        formData.put("shiftNo", entity.getShiftNo());
        formData.put("employeeId", form.getEmployeeId());
        formData.put("shiftDate", form.getShiftDate());
        formData.put("originalShiftId", form.getOriginalShiftId());
        formData.put("targetShiftId", form.getTargetShiftId());
        formData.put("reason", form.getReason());
        formData.put("applyTime", LocalDateTime.now());

        Map<String, Object> variables = new HashMap<>();

        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
                WorkflowDefinitionConstants.ATTENDANCE_SHIFT,
                entity.getShiftNo(),
                "调班申请-" + entity.getShiftNo(),
                form.getEmployeeId(),
                BusinessTypeEnum.ATTENDANCE_SHIFT.name(),
                formData,
                variables
        );

        if (workflowResult == null || !workflowResult.isSuccess()) {
            log.error("[调班申请] 启动审批流程失败，shiftNo={}", entity.getShiftNo());
            throw new BusinessException("启动审批流程失败: " +
                    (workflowResult != null ? workflowResult.getMessage() : "未知错误"));
        }

        Long workflowInstanceId = workflowResult.getData();
        entity.setWorkflowInstanceId(workflowInstanceId);
        attendanceShiftDao.updateById(entity);

        log.info("[调班申请] 调班申请提交成功，shiftNo={}, workflowInstanceId={}", entity.getShiftNo(), workflowInstanceId);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShiftStatus(String shiftNo, String status, String approvalComment) {
        log.info("[调班申请] 更新调班状态，shiftNo={}, status={}", shiftNo, status);

        AttendanceShiftEntity entity = attendanceShiftDao.selectByShiftNo(shiftNo);
        if (entity == null) {
            log.warn("[调班申请] 调班申请不存在，shiftNo={}", shiftNo);
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        attendanceShiftDao.updateById(entity);

        if ("APPROVED".equals(status)) {
            executeShiftApproval(entity);
        }

        log.info("[调班申请] 调班状态更新成功，shiftNo={}, status={}", shiftNo, status);
    }

    /**
     * 执行调班审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：调班审批通过后自动更新排班记录
     * 严格遵循CLAUDE.md规范：
     * - 通过Manager层处理复杂业务逻辑
     * - 完整的异常处理和日志记录
     * </p>
     *
     * @param entity 调班实体
     */
    private void executeShiftApproval(AttendanceShiftEntity entity) {
        log.info("[调班申请] 执行调班审批通过，shiftNo={}, employeeId={}, shiftDate={}, originalShiftId={}, targetShiftId={}",
                entity.getShiftNo(), entity.getEmployeeId(), entity.getShiftDate(),
                entity.getOriginalShiftId(), entity.getTargetShiftId());

        try {
            // 通过Manager层处理调班审批通过后的业务逻辑
            // 1. 更新员工排班记录（释放原班次，分配新班次）
            boolean success = attendanceManager.processShiftApproval(
                    entity.getEmployeeId(),
                    entity.getShiftDate(),
                    entity.getOriginalShiftId(),
                    entity.getTargetShiftId()
            );

            if (success) {
                log.info("[调班申请] 调班审批通过处理成功，shiftNo={}, employeeId={}", entity.getShiftNo(), entity.getEmployeeId());
            } else {
                log.warn("[调班申请] 调班审批通过处理失败，shiftNo={}, employeeId={}", entity.getShiftNo(), entity.getEmployeeId());
                // 不抛出异常，避免影响审批流程
            }
        } catch (Exception e) {
            log.error("[调班申请] 调班审批通过处理异常，shiftNo={}, employeeId={}", entity.getShiftNo(), entity.getEmployeeId(), e);
            // 不抛出异常，避免影响审批流程
        }
    }

    private String generateShiftNo() {
        return "SH" + System.currentTimeMillis();
    }
}

