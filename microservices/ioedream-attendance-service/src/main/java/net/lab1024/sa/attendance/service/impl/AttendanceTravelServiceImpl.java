package net.lab1024.sa.attendance.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.workflow.constant.BusinessTypeEnum;
import net.lab1024.sa.common.workflow.constant.WorkflowDefinitionConstants;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.attendance.dao.AttendanceTravelDao;
import net.lab1024.sa.common.attendance.entity.AttendanceTravelEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceTravelForm;
import net.lab1024.sa.attendance.service.AttendanceTravelService;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceTravelServiceImpl implements AttendanceTravelService {

    @Resource
    private AttendanceTravelDao attendanceTravelDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private net.lab1024.sa.attendance.attendance.manager.AttendanceManager attendanceManager;

    @Override
    @Observed(name = "attendance.travel.submit", contextualName = "attendance-travel-submit")
    @Transactional(rollbackFor = Exception.class)
    @Retry(name = "external-service-retry", fallbackMethod = "submitTravelApplicationFallback")
    public AttendanceTravelEntity submitTravelApplication(AttendanceTravelForm form) {
        log.info("[出差申请] 提交出差申请，employeeId={}, destination={}", form.getEmployeeId(), form.getDestination());

        AttendanceTravelEntity entity = new AttendanceTravelEntity();
        entity.setTravelNo(generateTravelNo());
        entity.setEmployeeId(form.getEmployeeId());
        // 从员工服务获取员工姓名
        String employeeName = attendanceManager.getUserName(form.getEmployeeId());
        entity.setEmployeeName(employeeName);
        entity.setDestination(form.getDestination());
        entity.setStartDate(form.getStartDate());
        entity.setEndDate(form.getEndDate());
        entity.setTravelDays(form.getTravelDays());
        entity.setEstimatedCost(form.getEstimatedCost());
        entity.setReason(form.getReason());
        entity.setRemark(form.getRemark());
        entity.setStatus("PENDING");
        attendanceTravelDao.insert(entity);

        Map<String, Object> formData = new HashMap<>();
        formData.put("travelNo", entity.getTravelNo());
        formData.put("employeeId", form.getEmployeeId());
        formData.put("destination", form.getDestination());
        formData.put("startDate", form.getStartDate());
        formData.put("endDate", form.getEndDate());
        formData.put("travelDays", form.getTravelDays());
        formData.put("estimatedCost", form.getEstimatedCost());
        formData.put("reason", form.getReason());
        formData.put("applyTime", LocalDateTime.now());

        Map<String, Object> variables = new HashMap<>();
        variables.put("estimatedCost", form.getEstimatedCost());

        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
                WorkflowDefinitionConstants.ATTENDANCE_TRAVEL,
                entity.getTravelNo(),
                "出差申请-" + entity.getTravelNo(),
                form.getEmployeeId(),
                BusinessTypeEnum.ATTENDANCE_TRAVEL.name(),
                formData,
                variables
        );

        if (workflowResult == null || !workflowResult.isSuccess()) {
            log.error("[出差申请] 启动审批流程失败，travelNo={}", entity.getTravelNo());
            throw new BusinessException("启动审批流程失败: " +
                    (workflowResult != null ? workflowResult.getMessage() : "未知错误"));
        }

        Long workflowInstanceId = workflowResult.getData();
        entity.setWorkflowInstanceId(workflowInstanceId);
        attendanceTravelDao.updateById(entity);

        log.info("[出差申请] 出差申请提交成功，travelNo={}, workflowInstanceId={}", entity.getTravelNo(), workflowInstanceId);
        return entity;
    }

    /**
     * 出差申请提交降级方法
     * <p>
     * 当工作流服务调用失败时，使用此降级方法
     * </p>
     */
    public AttendanceTravelEntity submitTravelApplicationFallback(
            AttendanceTravelForm form, Exception e) {
        log.error("[出差申请] 启动审批流程失败，使用降级方案, employeeId={}, error={}", form.getEmployeeId(), e.getMessage(), e);
        throw new BusinessException("启动审批流程失败: " + e.getMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTravelStatus(String travelNo, String status, String approvalComment) {
        log.info("[出差申请] 更新出差状态，travelNo={}, status={}", travelNo, status);

        AttendanceTravelEntity entity = attendanceTravelDao.selectByTravelNo(travelNo);
        if (entity == null) {
            log.warn("[出差申请] 出差申请不存在，travelNo={}", travelNo);
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        attendanceTravelDao.updateById(entity);

        if ("APPROVED".equals(status)) {
            executeTravelApproval(entity);
        }

        log.info("[出差申请] 出差状态更新成功，travelNo={}, status={}", travelNo, status);
    }

    /**
     * 执行出差审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：出差审批通过后自动更新考勤状态（出差期间不计入考勤）
     * 严格遵循CLAUDE.md规范：
     * - 通过Manager层处理复杂业务逻辑
     * - 完整的异常处理和日志记录
     * </p>
     *
     * @param entity 出差实体
     */
    private void executeTravelApproval(AttendanceTravelEntity entity) {
        log.info("[出差申请] 执行出差审批通过，travelNo={}, employeeId={}, startDate={}, endDate={}",
                entity.getTravelNo(), entity.getEmployeeId(), entity.getStartDate(), entity.getEndDate());

        try {
            // 通过Manager层处理出差审批通过后的业务逻辑
            // 1. 更新员工出差记录
            // 2. 更新考勤状态（出差期间不计入考勤）
            boolean success = attendanceManager.processTravelApproval(
                    entity.getEmployeeId(),
                    entity.getStartDate(),
                    entity.getEndDate()
            );

            if (success) {
                log.info("[出差申请] 出差审批通过处理成功，travelNo={}, employeeId={}", entity.getTravelNo(), entity.getEmployeeId());
            } else {
                log.warn("[出差申请] 出差审批通过处理失败，travelNo={}, employeeId={}", entity.getTravelNo(), entity.getEmployeeId());
                // 不抛出异常，避免影响审批流程
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[出差申请] 出差审批通过处理参数错误: travelNo={}, employeeId={}, error={}", entity.getTravelNo(), entity.getEmployeeId(), e.getMessage());
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (BusinessException e) {
            log.warn("[出差申请] 出差审批通过处理业务异常: travelNo={}, employeeId={}, code={}, message={}", entity.getTravelNo(), entity.getEmployeeId(), e.getCode(), e.getMessage());
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (SystemException e) {
            log.error("[出差申请] 出差审批通过处理系统异常: travelNo={}, employeeId={}, code={}, message={}", entity.getTravelNo(), entity.getEmployeeId(), e.getCode(), e.getMessage(), e);
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (Exception e) {
            log.error("[出差申请] 出差审批通过处理未知异常: travelNo={}, employeeId={}", entity.getTravelNo(), entity.getEmployeeId(), e);
            // 不抛出异常，避免影响审批流程（优雅降级）
        }
    }

    private String generateTravelNo() {
        return "TR" + System.currentTimeMillis();
    }
}



