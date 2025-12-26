package net.lab1024.sa.attendance.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.workflow.constant.BusinessTypeEnum;
import net.lab1024.sa.common.workflow.constant.WorkflowDefinitionConstants;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.attendance.dao.AttendanceLeaveDao;
import net.lab1024.sa.common.entity.attendance.AttendanceLeaveEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceLeaveForm;
import net.lab1024.sa.attendance.service.AttendanceLeaveService;

/**
 * 考勤请假服务实现类
 * <p>
 * 实现考勤请假相关业务功能，集成工作流审批
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-attendance-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AttendanceLeaveServiceImpl implements AttendanceLeaveService {


    @Resource
    private AttendanceLeaveDao attendanceLeaveDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private net.lab1024.sa.attendance.manager.AttendanceManager attendanceManager;

    @Override
    @Observed(name = "attendance.leave.submit", contextualName = "attendance-leave-submit")
    @Transactional(rollbackFor = Exception.class)
    @Retry(name = "external-service-retry", fallbackMethod = "submitLeaveApplicationFallback")
    public AttendanceLeaveEntity submitLeaveApplication(AttendanceLeaveForm form) {
        log.info("[请假申请] 提交请假申请，employeeId={}, leaveType={}, startDate={}, endDate={}",
                form.getEmployeeId(), form.getLeaveType(), form.getStartDate(), form.getEndDate());

        // 1. 创建请假申请记录
        AttendanceLeaveEntity entity = new AttendanceLeaveEntity();
        entity.setLeaveNo(generateLeaveNo());
        entity.setEmployeeId(form.getEmployeeId());
        // 从员工服务获取员工姓名
        String employeeName = attendanceManager.getUserName(form.getEmployeeId());
        entity.setEmployeeName(employeeName);
        entity.setLeaveType(form.getLeaveType());
        entity.setStartDate(form.getStartDate());
        entity.setEndDate(form.getEndDate());
        entity.setLeaveDays(form.getLeaveDays());
        entity.setReason(form.getReason());
        entity.setRemark(form.getRemark());
        entity.setStatus("PENDING");
        attendanceLeaveDao.insert(entity);

        // 2. 构建表单数据（用于审批流程）
        Map<String, Object> formData = new HashMap<>();
        formData.put("leaveNo", entity.getLeaveNo());
        formData.put("employeeId", form.getEmployeeId());
        formData.put("employeeName", entity.getEmployeeName());
        formData.put("leaveType", form.getLeaveType());
        formData.put("startDate", form.getStartDate());
        formData.put("endDate", form.getEndDate());
        formData.put("leaveDays", form.getLeaveDays());
        formData.put("reason", form.getReason());
        formData.put("applyTime", LocalDateTime.now());

        // 3. 构建流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("leaveDays", form.getLeaveDays()); // 用于判断审批层级（如：超过3天需要HR审批）

        // 4. 启动审批流程
        Long workflowInstanceId = workflowApprovalManager.startApprovalProcess(
                WorkflowDefinitionConstants.ATTENDANCE_LEAVE, // 流程定义ID
                entity.getLeaveNo(), // 业务Key
                "请假申请-" + entity.getLeaveNo(), // 流程实例名称
                form.getEmployeeId(), // 发起人ID
                BusinessTypeEnum.ATTENDANCE_LEAVE.name(), // 业务类型
                formData, // 表单数据
                variables // 流程变量
        );

        if (workflowInstanceId == null) {
            log.error("[请假申请] 启动审批流程失败，leaveNo={}", entity.getLeaveNo());
            throw new BusinessException("启动审批流程失败");
        }

        // 5. 更新请假申请的workflowInstanceId
        entity.setWorkflowInstanceId(workflowInstanceId);
        attendanceLeaveDao.updateById(entity);

        log.info("[请假申请] 请假申请提交成功，leaveNo={}, workflowInstanceId={}",
                entity.getLeaveNo(), workflowInstanceId);

        return entity;
    }

    /**
     * 请假申请提交降级方法
     * <p>
     * 当工作流服务调用失败时，使用此降级方法
     * </p>
     */
    public AttendanceLeaveEntity submitLeaveApplicationFallback(
            AttendanceLeaveForm form, Exception e) {
        log.error("[请假申请] 启动审批流程失败，使用降级方案, employeeId={}, error={}", form.getEmployeeId(), e.getMessage(), e);
        throw new BusinessException("启动审批流程失败: " + e.getMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeaveStatus(String leaveNo, String status, String approvalComment) {
        log.info("[请假申请] 更新请假状态，leaveNo={}, status={}", leaveNo, status);

        AttendanceLeaveEntity entity = attendanceLeaveDao.selectByLeaveNo(leaveNo);
        if (entity == null) {
            log.warn("[请假申请] 请假申请不存在，leaveNo={}", leaveNo);
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        attendanceLeaveDao.updateById(entity);

        // 如果审批通过，执行请假逻辑
        if ("APPROVED".equals(status)) {
            executeLeaveApproval(entity);
        }

        log.info("[请假申请] 请假状态更新成功，leaveNo={}, status={}", leaveNo, status);
    }

    /**
     * 执行请假审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：请假审批通过后自动扣除年假余额、更新考勤统计
     * 严格遵循CLAUDE.md规范：
     * - 通过Manager层处理复杂业务逻辑
     * - 完整的异常处理和日志记录
     * </p>
     *
     * @param entity 请假实体
     */
    private void executeLeaveApproval(AttendanceLeaveEntity entity) {
        log.info("[请假申请] 执行请假审批通过，leaveNo={}, employeeId={}, leaveType={}, leaveDays={}",
                entity.getLeaveNo(), entity.getEmployeeId(), entity.getLeaveType(), entity.getLeaveDays());

        try {
            // 通过Manager层处理请假审批通过后的业务逻辑
            // 1. 扣除年假余额（如果是年假类型）
            // 2. 更新考勤统计（按日期更新）
            boolean success = attendanceManager.processLeaveApproval(
                    entity.getEmployeeId(),
                    entity.getLeaveType(),
                    entity.getStartDate(),
                    entity.getEndDate(),
                    entity.getLeaveDays()
            );

            if (success) {
                log.info("[请假申请] 请假审批通过处理成功，leaveNo={}, employeeId={}, leaveDays={}",
                        entity.getLeaveNo(), entity.getEmployeeId(), entity.getLeaveDays());
            } else {
                log.warn("[请假申请] 请假审批通过处理部分失败，leaveNo={}, employeeId={}, leaveDays={}",
                        entity.getLeaveNo(), entity.getEmployeeId(), entity.getLeaveDays());
                // 不抛出异常，避免影响审批流程
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[请假申请] 请假审批通过处理参数错误: leaveNo={}, employeeId={}, error={}", entity.getLeaveNo(), entity.getEmployeeId(), e.getMessage());
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (BusinessException e) {
            log.warn("[请假申请] 请假审批通过处理业务异常: leaveNo={}, employeeId={}, code={}, message={}", entity.getLeaveNo(), entity.getEmployeeId(), e.getCode(), e.getMessage());
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (SystemException e) {
            log.error("[请假申请] 请假审批通过处理系统异常: leaveNo={}, employeeId={}, code={}, message={}", entity.getLeaveNo(), entity.getEmployeeId(), e.getCode(), e.getMessage(), e);
            // 不抛出异常，避免影响审批流程（优雅降级）
        } catch (Exception e) {
            log.error("[请假申请] 请假审批通过处理未知异常: leaveNo={}, employeeId={}", entity.getLeaveNo(), entity.getEmployeeId(), e);
            // 不抛出异常，避免影响审批流程（优雅降级）
        }
    }

    /**
     * 生成请假申请编号
     *
     * @return 请假申请编号
     */
    private String generateLeaveNo() {
        return "LV" + System.currentTimeMillis();
    }
}


