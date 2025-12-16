package net.lab1024.sa.attendance.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.workflow.constant.BusinessTypeEnum;
import net.lab1024.sa.common.workflow.constant.WorkflowDefinitionConstants;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeDao;
import net.lab1024.sa.common.attendance.entity.AttendanceOvertimeEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeForm;
import net.lab1024.sa.attendance.service.AttendanceOvertimeService;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceOvertimeServiceImpl implements AttendanceOvertimeService {

    @Resource
    private AttendanceOvertimeDao attendanceOvertimeDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private net.lab1024.sa.attendance.attendance.manager.AttendanceManager attendanceManager;

    @Override
    @Observed(name = "attendance.overtime.submit", contextualName = "attendance-overtime-submit")
    @Transactional(rollbackFor = Exception.class)
    @Retry(name = "external-service-retry", fallbackMethod = "submitOvertimeApplicationFallback")
    public AttendanceOvertimeEntity submitOvertimeApplication(AttendanceOvertimeForm form) {
        log.info("[加班申请] 提交加班申请，employeeId={}, overtimeDate={}", form.getEmployeeId(), form.getOvertimeDate());

        AttendanceOvertimeEntity entity = new AttendanceOvertimeEntity();
        entity.setOvertimeNo(generateOvertimeNo());
        entity.setEmployeeId(form.getEmployeeId());
        // 从员工服务获取员工姓名
        String employeeName = attendanceManager.getUserName(form.getEmployeeId());
        entity.setEmployeeName(employeeName);
        entity.setOvertimeDate(form.getOvertimeDate());
        entity.setStartTime(form.getStartTime());
        entity.setEndTime(form.getEndTime());
        entity.setOvertimeHours(form.getOvertimeHours());
        entity.setReason(form.getReason());
        entity.setRemark(form.getRemark());
        entity.setStatus("PENDING");
        attendanceOvertimeDao.insert(entity);

        Map<String, Object> formData = new HashMap<>();
        formData.put("overtimeNo", entity.getOvertimeNo());
        formData.put("employeeId", form.getEmployeeId());
        formData.put("overtimeDate", form.getOvertimeDate());
        formData.put("startTime", form.getStartTime());
        formData.put("endTime", form.getEndTime());
        formData.put("overtimeHours", form.getOvertimeHours());
        formData.put("reason", form.getReason());
        formData.put("applyTime", LocalDateTime.now());

        Map<String, Object> variables = new HashMap<>();
        variables.put("overtimeHours", form.getOvertimeHours());

        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
                WorkflowDefinitionConstants.ATTENDANCE_OVERTIME,
                entity.getOvertimeNo(),
                "加班申请-" + entity.getOvertimeNo(),
                form.getEmployeeId(),
                BusinessTypeEnum.ATTENDANCE_OVERTIME.name(),
                formData,
                variables
        );

        if (workflowResult == null || !workflowResult.isSuccess()) {
            log.error("[加班申请] 启动审批流程失败，overtimeNo={}", entity.getOvertimeNo());
            throw new BusinessException("启动审批流程失败: " +
                    (workflowResult != null ? workflowResult.getMessage() : "未知错误"));
        }

        Long workflowInstanceId = workflowResult.getData();
        entity.setWorkflowInstanceId(workflowInstanceId);
        attendanceOvertimeDao.updateById(entity);

        log.info("[加班申请] 加班申请提交成功，overtimeNo={}, workflowInstanceId={}", entity.getOvertimeNo(), workflowInstanceId);
        return entity;
    }

    /**
     * 加班申请提交降级方法
     * <p>
     * 当工作流服务调用失败时，使用此降级方法
     * </p>
     */
    public AttendanceOvertimeEntity submitOvertimeApplicationFallback(
            AttendanceOvertimeForm form, Exception e) {
        log.error("[加班申请] 启动审批流程失败，使用降级方案, employeeId={}, error={}", form.getEmployeeId(), e.getMessage(), e);
        throw new BusinessException("启动审批流程失败: " + e.getMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOvertimeStatus(String overtimeNo, String status, String approvalComment) {
        log.info("[加班申请] 更新加班状态，overtimeNo={}, status={}", overtimeNo, status);

        AttendanceOvertimeEntity entity = attendanceOvertimeDao.selectByOvertimeNo(overtimeNo);
        if (entity == null) {
            log.warn("[加班申请] 加班申请不存在，overtimeNo={}", overtimeNo);
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        attendanceOvertimeDao.updateById(entity);

        if ("APPROVED".equals(status)) {
            executeOvertimeApproval(entity);
        }

        log.info("[加班申请] 加班状态更新成功，overtimeNo={}, status={}", overtimeNo, status);
    }

    /**
     * 执行加班审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：加班审批通过后自动创建加班记录、计算加班费
     * 严格遵循CLAUDE.md规范：
     * - 通过Manager层处理复杂业务逻辑
     * - 完整的异常处理和日志记录
     * </p>
     *
     * @param entity 加班实体
     */
    private void executeOvertimeApproval(AttendanceOvertimeEntity entity) {
        log.info("[加班申请] 执行加班审批通过，overtimeNo={}, employeeId={}, overtimeDate={}, overtimeHours={}",
                entity.getOvertimeNo(), entity.getEmployeeId(), entity.getOvertimeDate(), entity.getOvertimeHours());

        try {
            // 通过Manager层处理加班审批通过后的业务逻辑
            // 1. 创建加班记录
            // 2. 计算加班费（基于班次规则）
            // 3. 更新考勤统计
            boolean success = attendanceManager.processOvertimeApproval(
                    entity.getEmployeeId(),
                    entity.getOvertimeDate(),
                    entity.getOvertimeHours()
            );

            if (success) {
                log.info("[加班申请] 加班审批通过处理成功，overtimeNo={}, employeeId={}", entity.getOvertimeNo(), entity.getEmployeeId());
            } else {
                log.warn("[加班申请] 加班审批通过处理失败，overtimeNo={}, employeeId={}", entity.getOvertimeNo(), entity.getEmployeeId());
                // 不抛出异常，避免影响审批流程
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[加班申请] 加班审批通过处理参数错误: overtimeNo={}, employeeId={}, error={}", entity.getOvertimeNo(), entity.getEmployeeId(), e.getMessage());
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (BusinessException e) {
            log.warn("[加班申请] 加班审批通过处理业务异常: overtimeNo={}, employeeId={}, code={}, message={}", entity.getOvertimeNo(), entity.getEmployeeId(), e.getCode(), e.getMessage());
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (SystemException e) {
            log.error("[加班申请] 加班审批通过处理系统异常: overtimeNo={}, employeeId={}, code={}, message={}", entity.getOvertimeNo(), entity.getEmployeeId(), e.getCode(), e.getMessage(), e);
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (Exception e) {
            log.error("[加班申请] 加班审批通过处理未知异常: overtimeNo={}, employeeId={}", entity.getOvertimeNo(), entity.getEmployeeId(), e);
            // 不抛出异常，避免影响审批流程（优雅降级）
        }
    }

    private String generateOvertimeNo() {
        return "OT" + System.currentTimeMillis();
    }
}



