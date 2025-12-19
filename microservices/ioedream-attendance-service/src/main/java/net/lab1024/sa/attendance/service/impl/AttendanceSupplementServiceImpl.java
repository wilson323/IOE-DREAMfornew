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
import net.lab1024.sa.attendance.dao.AttendanceSupplementDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceSupplementEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceSupplementForm;
import net.lab1024.sa.attendance.service.AttendanceSupplementService;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceSupplementServiceImpl implements AttendanceSupplementService {

    @Resource
    private AttendanceSupplementDao attendanceSupplementDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private net.lab1024.sa.attendance.manager.AttendanceManager attendanceManager;

    @Override
    @Observed(name = "attendance.supplement.submit", contextualName = "attendance-supplement-submit")
    @Transactional(rollbackFor = Exception.class)
    @Retry(name = "external-service-retry", fallbackMethod = "submitSupplementApplicationFallback")
    public AttendanceSupplementEntity submitSupplementApplication(AttendanceSupplementForm form) {
        log.info("[补签申请] 提交补签申请，employeeId={}, supplementDate={}", form.getEmployeeId(), form.getSupplementDate());

        AttendanceSupplementEntity entity = new AttendanceSupplementEntity();
        entity.setSupplementNo(generateSupplementNo());
        entity.setEmployeeId(form.getEmployeeId());
        // 从员工服务获取员工姓名
        String employeeName = attendanceManager.getUserName(form.getEmployeeId());
        entity.setEmployeeName(employeeName);
        entity.setSupplementDate(form.getSupplementDate());
        entity.setPunchTime(form.getPunchTime());
        entity.setPunchType(form.getPunchType());
        entity.setReason(form.getReason());
        entity.setRemark(form.getRemark());
        entity.setStatus("PENDING");
        attendanceSupplementDao.insert(entity);

        Map<String, Object> formData = new HashMap<>();
        formData.put("supplementNo", entity.getSupplementNo());
        formData.put("employeeId", form.getEmployeeId());
        formData.put("supplementDate", form.getSupplementDate());
        formData.put("punchTime", form.getPunchTime());
        formData.put("punchType", form.getPunchType());
        formData.put("reason", form.getReason());
        formData.put("applyTime", LocalDateTime.now());

        Map<String, Object> variables = new HashMap<>();

        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
                WorkflowDefinitionConstants.ATTENDANCE_SUPPLEMENT,
                entity.getSupplementNo(),
                "补签申请-" + entity.getSupplementNo(),
                form.getEmployeeId(),
                BusinessTypeEnum.ATTENDANCE_SUPPLEMENT.name(),
                formData,
                variables
        );

        if (workflowResult == null || !workflowResult.isSuccess()) {
            log.error("[补签申请] 启动审批流程失败，supplementNo={}", entity.getSupplementNo());
            throw new BusinessException("启动审批流程失败: " +
                    (workflowResult != null ? workflowResult.getMessage() : "未知错误"));
        }

        Long workflowInstanceId = workflowResult.getData();
        entity.setWorkflowInstanceId(workflowInstanceId);
        attendanceSupplementDao.updateById(entity);

        log.info("[补签申请] 补签申请提交成功，supplementNo={}, workflowInstanceId={}", entity.getSupplementNo(), workflowInstanceId);
        return entity;
    }

    /**
     * 补签申请提交降级方法
     * <p>
     * 当工作流服务调用失败时，使用此降级方法
     * </p>
     */
    public AttendanceSupplementEntity submitSupplementApplicationFallback(
            AttendanceSupplementForm form, Exception e) {
        log.error("[补签申请] 启动审批流程失败，使用降级方案, employeeId={}, error={}", form.getEmployeeId(), e.getMessage(), e);
        throw new BusinessException("启动审批流程失败: " + e.getMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSupplementStatus(String supplementNo, String status, String approvalComment) {
        log.info("[补签申请] 更新补签状态，supplementNo={}, status={}", supplementNo, status);

        AttendanceSupplementEntity entity = attendanceSupplementDao.selectBySupplementNo(supplementNo);
        if (entity == null) {
            log.warn("[补签申请] 补签申请不存在，supplementNo={}", supplementNo);
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        attendanceSupplementDao.updateById(entity);

        if ("APPROVED".equals(status)) {
            executeSupplementApproval(entity);
        }

        log.info("[补签申请] 补签状态更新成功，supplementNo={}, status={}", supplementNo, status);
    }

    /**
     * 执行补签审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：补签审批通过后自动创建考勤记录
     * 严格遵循CLAUDE.md规范：
     * - 通过Manager层处理复杂业务逻辑
     * - 完整的异常处理和日志记录
     * </p>
     *
     * @param entity 补签实体
     */
    private void executeSupplementApproval(AttendanceSupplementEntity entity) {
        log.info("[补签申请] 执行补签审批通过，supplementNo={}, employeeId={}, supplementDate={}, punchTime={}, punchType={}",
                entity.getSupplementNo(), entity.getEmployeeId(), entity.getSupplementDate(),
                entity.getPunchTime(), entity.getPunchType());

        try {
            // 通过Manager层处理补签审批通过后的业务逻辑
            // 1. 创建补签考勤记录
            // 2. 更新考勤统计
            boolean success = attendanceManager.processSupplementApproval(
                    entity.getEmployeeId(),
                    entity.getSupplementDate(),
                    entity.getPunchTime() != null ? entity.getPunchTime().toString() : null,
                    entity.getPunchType()
            );

            if (success) {
                log.info("[补签申请] 补签审批通过处理成功，supplementNo={}, employeeId={}", entity.getSupplementNo(), entity.getEmployeeId());
            } else {
                log.warn("[补签申请] 补签审批通过处理失败，supplementNo={}, employeeId={}", entity.getSupplementNo(), entity.getEmployeeId());
                // 不抛出异常，避免影响审批流程
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[补签申请] 补签审批通过处理参数错误: supplementNo={}, employeeId={}, error={}", entity.getSupplementNo(), entity.getEmployeeId(), e.getMessage());
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (BusinessException e) {
            log.warn("[补签申请] 补签审批通过处理业务异常: supplementNo={}, employeeId={}, code={}, message={}", entity.getSupplementNo(), entity.getEmployeeId(), e.getCode(), e.getMessage());
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (SystemException e) {
            log.error("[补签申请] 补签审批通过处理系统异常: supplementNo={}, employeeId={}, code={}, message={}", entity.getSupplementNo(), entity.getEmployeeId(), e.getCode(), e.getMessage(), e);
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (Exception e) {
            log.error("[补签申请] 补签审批通过处理未知异常: supplementNo={}, employeeId={}", entity.getSupplementNo(), entity.getEmployeeId(), e);
            // 不抛出异常，避免影响审批流程（优雅降级）
        }
    }

    private String generateSupplementNo() {
        return "SP" + System.currentTimeMillis();
    }
}



