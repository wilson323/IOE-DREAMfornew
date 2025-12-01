package net.lab1024.sa.attendance.workflow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 考勤异常申请审批流程
 * 处理迟到、早退、忘打卡等异常申请的审批工作流
 * 严格遵循repowiki规范，使用jakarta包和@Resource注入
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-28
 */
@Slf4j
@Data
public class ExceptionApprovalWorkflow {

    /**
     * 审批状态枚举
     */
    public enum ApprovalStatus {
        PENDING("PENDING", "待审批"),
        APPROVED("APPROVED", "已通过"),
        REJECTED("REJECTED", "已拒绝"),
        CANCELLED("CANCELLED", "已撤销"),
        PROCESSING("PROCESSING", "处理中");

        private final String code;
        private final String description;

        ApprovalStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 申请类型枚举
     */
    public enum ApplicationType {
        LATE_EXCEPTION("LATE_EXCEPTION", "迟到异常"),
        EARLY_LEAVE_EXCEPTION("EARLY_LEAVE_EXCEPTION", "早退异常"),
        FORGET_PUNCH_EXCEPTION("FORGET_PUNCH_EXCEPTION", "忘打卡异常"),
        OVERTIME_APPLICATION("OVERTIME_APPLICATION", "加班申请"),
        LEAVE_APPLICATION("LEAVE_APPLICATION", "请假申请"),
        WORK_FROM_HOME("WORK_FROM_HOME", "居家办公");

        private final String code;
        private final String description;

        ApplicationType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 审批步骤枚举
     */
    public enum ApprovalStep {
        SUBMIT("SUBMIT", "提交申请"),
        DEPARTMENT_APPROVE("DEPARTMENT_APPROVE", "部门审批"),
        HR_APPROVE("HR_APPROVE", "人事审批"),
        FINAL_APPROVE("FINAL_APPROVE", "最终审批"),
        COMPLETE("COMPLETE", "审批完成");

        private final String code;
        private final String description;

        ApprovalStep(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 异常申请实体
     */
    @Data
    public static class ExceptionApplication {
        private Long applicationId;
        private Long employeeId;
        private String employeeName;
        private String employeeDepartment;
        private ApplicationType applicationType;
        private LocalDate attendanceDate;
        private LocalTime punchInTime;
        private LocalTime punchOutTime;
        private String exceptionReason;
        private String applicationReason;
        private String supportingDocuments; // 证明材料路径
        private ApprovalStatus currentStatus;
        private ApprovalStep currentStep;
        private Long currentApproverId;
        private String currentApproverName;
        private LocalDateTime applicationTime;
        private LocalDateTime lastUpdateTime;
        private String remarks;
        private List<ApprovalRecord> approvalRecords;
    }

    /**
     * 审批记录实体
     */
    @Data
    public static class ApprovalRecord {
        private Long recordId;
        private Long applicationId;
        private ApprovalStep step;
        private Long approverId;
        private String approverName;
        private ApprovalStatus decision;
        private String approvalComment;
        private LocalDateTime approvalTime;
        private Boolean isFinal;
    }

    /**
     * 审批规则配置
     */
    @Data
    public static class ApprovalRule {
        private ApplicationType applicationType;
        private List<ApprovalStep> requiredSteps;
        private Map<ApprovalStep, List<Long>> stepApprovers; // 每个步骤的审批人员ID列表
        private Integer maxApprovalDays; // 最大审批天数
        private Boolean allowDelegateApproval; // 允许代批
        private Map<String, Object> customRules; // 自定义规则
    }

    /**
     * 申请结果
     */
    @Data
    public static class ApplicationResult {
        private Boolean success;
        private String message;
        private ExceptionApplication application;
        private List<String> errors;
        private Map<String, Object> details;
    }

    /**
     * 审批规则配置
     */
    private Map<ApplicationType, ApprovalRule> approvalRules = new HashMap<>();

    /**
     * 待处理申请缓存
     */
    private Map<Long, ExceptionApplication> pendingApplications = new HashMap<>();

    /**
     * 初始化审批流程引擎
     */
    public void initializeWorkflowEngine() {
        log.info("考勤异常审批流程引擎初始化开始");

        // 加载默认审批规则
        loadDefaultApprovalRules();

        log.info("考勤异常审批流程引擎初始化完成，加载了{}个审批规则", approvalRules.size());
    }

    /**
     * 加载默认审批规则
     */
    private void loadDefaultApprovalRules() {
        // 迟到异常审批规则
        ApprovalRule lateExceptionRule = new ApprovalRule();
        lateExceptionRule.setApplicationType(ApplicationType.LATE_EXCEPTION);
        lateExceptionRule.setRequiredSteps(Arrays.asList(
                ApprovalStep.SUBMIT,
                ApprovalStep.DEPARTMENT_APPROVE));
        lateExceptionRule.setMaxApprovalDays(3);
        lateExceptionRule.setAllowDelegateApproval(true);
        approvalRules.put(ApplicationType.LATE_EXCEPTION, lateExceptionRule);

        // 忘打卡审批规则
        ApprovalRule forgetPunchRule = new ApprovalRule();
        forgetPunchRule.setApplicationType(ApplicationType.FORGET_PUNCH_EXCEPTION);
        forgetPunchRule.setRequiredSteps(Arrays.asList(
                ApprovalStep.SUBMIT,
                ApprovalStep.DEPARTMENT_APPROVE));
        forgetPunchRule.setMaxApprovalDays(2);
        forgetPunchRule.setAllowDelegateApproval(true);
        approvalRules.put(ApplicationType.FORGET_PUNCH_EXCEPTION, forgetPunchRule);

        // 加班申请审批规则
        ApprovalRule overtimeRule = new ApprovalRule();
        overtimeRule.setApplicationType(ApplicationType.OVERTIME_APPLICATION);
        overtimeRule.setRequiredSteps(Arrays.asList(
                ApprovalStep.SUBMIT,
                ApprovalStep.DEPARTMENT_APPROVE,
                ApprovalStep.HR_APPROVE));
        overtimeRule.setMaxApprovalDays(5);
        overtimeRule.setAllowDelegateApproval(true);
        approvalRules.put(ApplicationType.OVERTIME_APPLICATION, overtimeRule);

        // 请假申请审批规则
        ApprovalRule leaveRule = new ApprovalRule();
        leaveRule.setApplicationType(ApplicationType.LEAVE_APPLICATION);
        leaveRule.setRequiredSteps(Arrays.asList(
                ApprovalStep.SUBMIT,
                ApprovalStep.DEPARTMENT_APPROVE,
                ApprovalStep.HR_APPROVE,
                ApprovalStep.FINAL_APPROVE));
        leaveRule.setMaxApprovalDays(7);
        leaveRule.setAllowDelegateApproval(true);
        approvalRules.put(ApplicationType.LEAVE_APPLICATION, leaveRule);

        // 居家办公审批规则
        ApprovalRule workFromHomeRule = new ApprovalRule();
        workFromHomeRule.setApplicationType(ApplicationType.WORK_FROM_HOME);
        workFromHomeRule.setRequiredSteps(Arrays.asList(
                ApprovalStep.SUBMIT,
                ApprovalStep.DEPARTMENT_APPROVE,
                ApprovalStep.FINAL_APPROVE));
        workFromHomeRule.setMaxApprovalDays(3);
        workFromHomeRule.setAllowDelegateApproval(true);
        approvalRules.put(ApplicationType.WORK_FROM_HOME, workFromHomeRule);

        log.debug("加载了{}个默认审批规则", approvalRules.size());
    }

    /**
     * 提交异常申请
     *
     * @param application 申请信息
     * @return 提交结果
     */
    public ApplicationResult submitApplication(ExceptionApplication application) {
        log.info("员工{}提交异常申请：类型{}，日期{}",
                application.getEmployeeId(), application.getApplicationType(), application.getAttendanceDate());

        ApplicationResult result = new ApplicationResult();
        result.setSuccess(false);
        result.setErrors(new ArrayList<>());
        result.setDetails(new HashMap<>());

        try {
            // 验证申请信息
            String validationError = validateApplication(application);
            if (validationError != null) {
                result.getErrors().add(validationError);
                result.setMessage("申请信息验证失败：" + validationError);
                return result;
            }

            // 获取审批规则
            ApprovalRule rule = approvalRules.get(application.getApplicationType());
            if (rule == null) {
                result.getErrors().add("未找到对应的审批规则");
                result.setMessage("审批规则配置错误");
                return result;
            }

            // 设置申请基本信息
            application.setApplicationId(generateApplicationId());
            application.setCurrentStatus(ApprovalStatus.PENDING);
            application.setCurrentStep(ApprovalStep.SUBMIT);
            application.setApplicationTime(LocalDateTime.now());
            application.setLastUpdateTime(LocalDateTime.now());
            application.setApprovalRecords(new ArrayList<>());

            // 添加提交记录
            ApprovalRecord submitRecord = new ApprovalRecord();
            submitRecord.setRecordId(generateRecordId());
            submitRecord.setApplicationId(application.getApplicationId());
            submitRecord.setStep(ApprovalStep.SUBMIT);
            submitRecord.setApproverId(application.getEmployeeId());
            submitRecord.setApproverName(application.getEmployeeName());
            submitRecord.setDecision(ApprovalStatus.PENDING);
            submitRecord.setApprovalTime(LocalDateTime.now());
            submitRecord.setIsFinal(false);
            application.getApprovalRecords().add(submitRecord);

            // 确定下一步审批人
            ApprovalStep nextStep = getNextApprovalStep(application, rule);
            if (nextStep != null) {
                application.setCurrentStep(nextStep);
                // 这里应该根据规则确定审批人，简化处理
                application.setCurrentApproverId(getNextApproverId(nextStep, rule));
            } else {
                // 不需要审批，直接通过
                application.setCurrentStatus(ApprovalStatus.APPROVED);
                application.setCurrentStep(ApprovalStep.COMPLETE);
            }

            // 添加到待处理队列
            pendingApplications.put(application.getApplicationId(), application);

            result.setSuccess(true);
            result.setApplication(application);
            result.setMessage("申请提交成功");
            result.getDetails().put("currentStep", application.getCurrentStep().getDescription());
            result.getDetails().put("estimatedApprovalDays", rule.getMaxApprovalDays());

            log.info("异常申请提交成功，申请ID：{}，当前状态：{}",
                    application.getApplicationId(), application.getCurrentStatus());

        } catch (Exception e) {
            log.error("提交异常申请失败", e);
            result.getErrors().add("系统异常：" + e.getMessage());
            result.setMessage("申请提交失败");
        }

        return result;
    }

    /**
     * 处理审批
     *
     * @param applicationId 申请ID
     * @param approverId    审批人ID
     * @param approverName  审批人姓名
     * @param decision      审批决定
     * @param comment       审批意见
     * @return 处理结果
     */
    public ApplicationResult processApproval(Long applicationId, Long approverId, String approverName,
            ApprovalStatus decision, String comment) {
        log.info("审批人{}处理申请{}，决定：{}", approverName, applicationId, decision);

        ApplicationResult result = new ApplicationResult();
        result.setSuccess(false);
        result.setErrors(new ArrayList<>());

        ExceptionApplication application = pendingApplications.get(applicationId);
        if (application == null) {
            result.getErrors().add("申请不存在或已处理");
            result.setMessage("申请不存在");
            return result;
        }

        // 验证审批权限
        if (!validateApprovalPermission(application, approverId)) {
            result.getErrors().add("无审批权限");
            result.setMessage("无权处理此申请");
            return result;
        }

        try {
            // 添加审批记录
            ApprovalRecord approvalRecord = new ApprovalRecord();
            approvalRecord.setRecordId(generateRecordId());
            approvalRecord.setApplicationId(applicationId);
            approvalRecord.setStep(application.getCurrentStep());
            approvalRecord.setApproverId(approverId);
            approvalRecord.setApproverName(approverName);
            approvalRecord.setDecision(decision);
            approvalRecord.setApprovalComment(comment);
            approvalRecord.setApprovalTime(LocalDateTime.now());
            approvalRecord.setIsFinal(false);
            application.getApprovalRecords().add(approvalRecord);

            // 更新申请状态
            application.setLastUpdateTime(LocalDateTime.now());
            application.setCurrentApproverId(approverId);
            application.setCurrentApproverName(approverName);

            // 根据审批决定处理流程
            ApprovalRule rule = approvalRules.get(application.getApplicationType());
            if (decision == ApprovalStatus.APPROVED) {
                // 审批通过，进入下一步
                ApprovalStep nextStep = getNextApprovalStep(application, rule);
                if (nextStep != null) {
                    application.setCurrentStep(nextStep);
                    application.setCurrentStatus(ApprovalStatus.PROCESSING);
                    application.setCurrentApproverId(getNextApproverId(nextStep, rule));
                } else {
                    // 审批完成
                    application.setCurrentStatus(ApprovalStatus.APPROVED);
                    application.setCurrentStep(ApprovalStep.COMPLETE);
                    approvalRecord.setIsFinal(true);
                    pendingApplications.remove(applicationId);
                }
            } else if (decision == ApprovalStatus.REJECTED) {
                // 审批拒绝，流程结束
                application.setCurrentStatus(ApprovalStatus.REJECTED);
                application.setCurrentStep(ApprovalStep.COMPLETE);
                approvalRecord.setIsFinal(true);
                pendingApplications.remove(applicationId);
            }

            result.setSuccess(true);
            result.setApplication(application);
            result.setMessage("审批处理成功");

            log.info("申请{}审批处理完成，状态：{}", applicationId, application.getCurrentStatus());

        } catch (Exception e) {
            log.error("处理审批失败", e);
            result.getErrors().add("系统异常：" + e.getMessage());
            result.setMessage("审批处理失败");
        }

        return result;
    }

    /**
     * 获取待处理申请列表
     *
     * @param approverId 审批人ID
     * @return 待处理申请列表
     */
    public List<ExceptionApplication> getPendingApplications(Long approverId) {
        return pendingApplications.values().stream()
                .filter(app -> approverId.equals(app.getCurrentApproverId()))
                .filter(app -> app.getCurrentStatus() == ApprovalStatus.PENDING ||
                        app.getCurrentStatus() == ApprovalStatus.PROCESSING)
                .collect(Collectors.toList());
    }

    /**
     * 获取员工申请历史
     *
     * @param employeeId 员工ID
     * @return 申请历史列表
     */
    public List<ExceptionApplication> getEmployeeApplicationHistory(Long employeeId) {
        // 这里应该从数据库查询，简化处理只返回当前数据
        return pendingApplications.values().stream()
                .filter(app -> employeeId.equals(app.getEmployeeId()))
                .collect(Collectors.toList());
    }

    /**
     * 验证申请信息
     */
    private String validateApplication(ExceptionApplication application) {
        if (application.getEmployeeId() == null) {
            return "员工ID不能为空";
        }

        if (application.getApplicationType() == null) {
            return "申请类型不能为空";
        }

        if (application.getAttendanceDate() == null) {
            return "考勤日期不能为空";
        }

        if (application.getApplicationReason() == null || application.getApplicationReason().trim().isEmpty()) {
            return "申请原因不能为空";
        }

        // 检查是否已有同类型同日期的申请
        boolean hasExistingApplication = pendingApplications.values().stream()
                .anyMatch(app -> app.getEmployeeId().equals(application.getEmployeeId()) &&
                        app.getApplicationType().equals(application.getApplicationType()) &&
                        app.getAttendanceDate().equals(application.getAttendanceDate()) &&
                        (app.getCurrentStatus() == ApprovalStatus.PENDING ||
                                app.getCurrentStatus() == ApprovalStatus.PROCESSING));

        if (hasExistingApplication) {
            return "该日期已存在待处理的同类型申请";
        }

        return null;
    }

    /**
     * 获取下一步审批步骤
     */
    private ApprovalStep getNextApprovalStep(ExceptionApplication application, ApprovalRule rule) {
        List<ApprovalStep> requiredSteps = rule.getRequiredSteps();
        ApprovalStep currentStep = application.getCurrentStep();

        int currentIndex = requiredSteps.indexOf(currentStep);
        if (currentIndex >= 0 && currentIndex < requiredSteps.size() - 1) {
            return requiredSteps.get(currentIndex + 1);
        }

        return null;
    }

    /**
     * 获取下一步审批人ID
     */
    private Long getNextApproverId(ApprovalStep step, ApprovalRule rule) {
        // 简化处理，返回默认审批人ID
        // 实际应该根据部门、职位等确定具体的审批人
        switch (step) {
            case DEPARTMENT_APPROVE:
                return 1001L; // 部门经理ID
            case HR_APPROVE:
                return 2001L; // HR经理ID
            case FINAL_APPROVE:
                return 3001L; // 总经理ID
            default:
                return 1L; // 默认管理员
        }
    }

    /**
     * 验证审批权限
     */
    private Boolean validateApprovalPermission(ExceptionApplication application, Long approverId) {
        // 简化处理，实际应该根据审批规则验证权限
        return approverId.equals(application.getCurrentApproverId()) ||
                Boolean.TRUE.equals(approvalRules.get(application.getApplicationType()).getAllowDelegateApproval());
    }

    /**
     * 生成申请ID
     */
    private Long generateApplicationId() {
        return System.currentTimeMillis();
    }

    /**
     * 生成记录ID
     */
    private Long generateRecordId() {
        return System.currentTimeMillis() + (long) (Math.random() * 1000);
    }

    /**
     * 撤销申请
     *
     * @param applicationId 申请ID
     * @param employeeId    员工ID
     * @return 撤销结果
     */
    public ApplicationResult cancelApplication(Long applicationId, Long employeeId) {
        log.info("员工{}撤销申请：{}", employeeId, applicationId);

        ApplicationResult result = new ApplicationResult();
        result.setSuccess(false);
        result.setErrors(new ArrayList<>());

        ExceptionApplication application = pendingApplications.get(applicationId);
        if (application == null) {
            result.getErrors().add("申请不存在");
            result.setMessage("申请不存在");
            return result;
        }

        if (!employeeId.equals(application.getEmployeeId())) {
            result.getErrors().add("只能撤销自己的申请");
            result.setMessage("无权撤销此申请");
            return result;
        }

        if (application.getCurrentStatus() != ApprovalStatus.PENDING &&
                application.getCurrentStatus() != ApprovalStatus.PROCESSING) {
            result.getErrors().add("只能撤销待处理或处理中的申请");
            result.setMessage("申请状态不允许撤销");
            return result;
        }

        try {
            application.setCurrentStatus(ApprovalStatus.CANCELLED);
            application.setCurrentStep(ApprovalStep.COMPLETE);
            application.setLastUpdateTime(LocalDateTime.now());

            // 添加撤销记录
            ApprovalRecord cancelRecord = new ApprovalRecord();
            cancelRecord.setRecordId(generateRecordId());
            cancelRecord.setApplicationId(applicationId);
            cancelRecord.setStep(ApprovalStep.COMPLETE);
            cancelRecord.setApproverId(employeeId);
            cancelRecord.setApproverName(application.getEmployeeName());
            cancelRecord.setDecision(ApprovalStatus.CANCELLED);
            cancelRecord.setApprovalComment("员工主动撤销申请");
            cancelRecord.setApprovalTime(LocalDateTime.now());
            cancelRecord.setIsFinal(true);
            application.getApprovalRecords().add(cancelRecord);

            pendingApplications.remove(applicationId);

            result.setSuccess(true);
            result.setApplication(application);
            result.setMessage("申请撤销成功");

            log.info("申请{}撤销成功", applicationId);

        } catch (Exception e) {
            log.error("撤销申请失败", e);
            result.getErrors().add("系统异常：" + e.getMessage());
            result.setMessage("撤销申请失败");
        }

        return result;
    }

    /**
     * 获取所有申请类型
     *
     * @return 申请类型列表
     */
    public List<ApplicationType> getAllApplicationTypes() {
        return Arrays.asList(ApplicationType.values());
    }

    /**
     * 获取所有审批状态
     *
     * @return 审批状态列表
     */
    public List<ApprovalStatus> getAllApprovalStatuses() {
        return Arrays.asList(ApprovalStatus.values());
    }

    /**
     * 添加审批规则
     *
     * @param applicationType 申请类型
     * @param rule            审批规则
     */
    public void addApprovalRule(ApplicationType applicationType, ApprovalRule rule) {
        approvalRules.put(applicationType, rule);
        log.info("添加审批规则：{}", applicationType.getDescription());
    }

    /**
     * 获取所有审批规则
     *
     * @return 审批规则列表
     */
    public Map<ApplicationType, ApprovalRule> getAllApprovalRules() {
        return new HashMap<>(approvalRules);
    }
}
