package net.lab1024.sa.attendance.leave.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.leave.LeaveCancellationService;
import net.lab1024.sa.attendance.leave.model.*;
import net.lab1024.sa.attendance.leave.model.request.*;
import net.lab1024.sa.attendance.leave.model.result.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 销假服务实现类
 * <p>
 * 实现销假功能的核心业务逻辑
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component("leaveCancellationService")
public class LeaveCancellationServiceImpl implements LeaveCancellationService {

    /**
     * 销假申请存储
     */
    private final Map<String, LeaveCancellationApplication> cancellationApplications = new ConcurrentHashMap<>();

    /**
     * 员工销假索引
     */
    private final Map<Long, List<String>> employeeCancellationIndex = new ConcurrentHashMap<>();

    /**
     * 待审批销假索引
     */
    private final Map<Long, List<String>> pendingApprovalIndex = new ConcurrentHashMap<>();

    /**
     * 执行器服务
     */
    private final ExecutorService executorService;

    /**
     * 构造函数
     */
    public LeaveCancellationServiceImpl(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public CompletableFuture<LeaveCancellationApplicationResult> applyLeaveCancellation(LeaveCancellationApplicationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[销假服务] 员工申请销假: employeeId={}, originalLeaveId={}",
                        request.getEmployeeId(), request.getOriginalLeaveId());

                // 验证原请假记录
                if (!validateOriginalLeave(request.getOriginalLeaveId(), request.getEmployeeId())) {
                    return LeaveCancellationApplicationResult.builder()
                            .success(false)
                            .errorMessage("原请假记录不存在或不属于该员工")
                            .errorCode("INVALID_ORIGINAL_LEAVE")
                            .build();
                }

                // 创建销假申请
                LeaveCancellationApplication application = createCancellationApplication(request);

                // 执行影响评估
                performImpactAssessment(application);

                // 存储申请
                cancellationApplications.put(application.getCancellationId(), application);
                updateEmployeeIndex(application);
                updatePendingApprovalIndex(application);

                log.info("[销假服务] 销假申请创建成功: cancellationId={}", application.getCancellationId());

                return LeaveCancellationApplicationResult.builder()
                        .success(true)
                        .cancellationId(application.getCancellationId())
                        .message("销假申请提交成功")
                        .applicationTime(application.getApplicationTime())
                        .estimatedApprovalTime(estimateApprovalTime(application))
                        .build();

            } catch (Exception e) {
                log.error("[销假服务] 申请销假失败", e);
                return LeaveCancellationApplicationResult.builder()
                        .success(false)
                        .errorMessage("申请销假时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<LeaveCancellationApprovalResult> approveLeaveCancellation(LeaveCancellationApprovalRequest approvalRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[销假服务] 审批销假申请: cancellationId={}, approverId={}, result={}",
                        approvalRequest.getCancellationId(), approvalRequest.getApproverId(), approvalRequest.getApprovalResult());

                LeaveCancellationApplication application = cancellationApplications.get(approvalRequest.getCancellationId());
                if (application == null) {
                    return LeaveCancellationApprovalResult.builder()
                            .success(false)
                            .errorMessage("销假申请不存在")
                            .errorCode("CANCELLATION_NOT_FOUND")
                            .build();
                }

                // 验证审批权限
                if (!validateApprovalPermission(application, approvalRequest.getApproverId())) {
                    return LeaveCancellationApprovalResult.builder()
                            .success(false)
                            .errorMessage("无审批权限")
                            .errorCode("NO_APPROVAL_PERMISSION")
                            .build();
                }

                // 记录审批结果
                LeaveCancellationApplication.ApprovalRecord approvalRecord = createApprovalRecord(approvalRequest);
                application.getApprovalRecords().add(approvalRecord);

                // 更新申请状态
                updateApplicationStatus(application, approvalRequest);

                // 如果最终审批通过，执行销假生效
                if (isFinalApproval(application, LeaveCancellationApplication.ApprovalResult.valueOf(approvalRequest.getApprovalResult()))) {
                    executeCancellationEffect(application);
                }

                // 更新索引
                updateIndexesAfterApproval(application);

                log.info("[销假服务] 销假申请审批完成: cancellationId={}, status={}",
                        application.getCancellationId(), application.getStatus());

                return LeaveCancellationApprovalResult.builder()
                        .success(true)
                        .cancellationId(application.getCancellationId())
                        .newStatus(application.getStatus().name())
                        .message("审批完成")
                        .approvalTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[销假服务] 审批销假申请失败", e);
                return LeaveCancellationApprovalResult.builder()
                        .success(false)
                        .errorMessage("审批销假申请时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<LeaveCancellationRejectionResult> rejectLeaveCancellation(LeaveCancellationRejectionRequest rejectionRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[销假服务] 驳回销假申请: cancellationId={}, approverId={}",
                        rejectionRequest.getCancellationId(), rejectionRequest.getApproverId());

                LeaveCancellationApplication application = cancellationApplications.get(rejectionRequest.getCancellationId());
                if (application == null) {
                    return LeaveCancellationRejectionResult.builder()
                            .success(false)
                            .errorMessage("销假申请不存在")
                            .errorCode("CANCELLATION_NOT_FOUND")
                            .build();
                }

                // 验证审批权限
                if (!validateApprovalPermission(application, rejectionRequest.getApproverId())) {
                    return LeaveCancellationRejectionResult.builder()
                            .success(false)
                            .errorMessage("无审批权限")
                            .errorCode("NO_APPROVAL_PERMISSION")
                            .build();
                }

                // 更新申请状态为驳回
                application.setStatus(LeaveCancellationApplication.ApplicationStatus.REJECTED);
                application.setUpdateTime(LocalDateTime.now());

                // 更新索引
                updateIndexesAfterRejection(application);

                return LeaveCancellationRejectionResult.builder()
                        .success(true)
                        .cancellationId(application.getCancellationId())
                        .message("销假申请已驳回")
                        .rejectionTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[销假服务] 驳回销假申请失败", e);
                return LeaveCancellationRejectionResult.builder()
                        .success(false)
                        .errorMessage("驳回销假申请时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<LeaveCancellationWithdrawalResult> withdrawLeaveCancellation(String cancellationId, String reason, Long operatorId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[销假服务] 撤销销假申请: cancellationId={}, operatorId={}", cancellationId, operatorId);

                LeaveCancellationApplication application = cancellationApplications.get(cancellationId);
                if (application == null) {
                    return LeaveCancellationWithdrawalResult.builder()
                            .success(false)
                            .errorMessage("销假申请不存在")
                            .errorCode("CANCELLATION_NOT_FOUND")
                            .build();
                }

                // 验证撤销权限（只有申请人或管理员可以撤销）
                if (!application.getApplicantId().equals(operatorId) && !isAdmin(operatorId)) {
                    return LeaveCancellationWithdrawalResult.builder()
                            .success(false)
                            .errorMessage("无撤销权限")
                            .errorCode("NO_WITHDRAWAL_PERMISSION")
                            .build();
                }

                // 验证申请状态（只有草稿、已提交、审核中的状态可以撤销）
                if (!canWithdraw(application.getStatus())) {
                    return LeaveCancellationWithdrawalResult.builder()
                            .success(false)
                            .errorMessage("当前状态不允许撤销")
                            .errorCode("INVALID_STATUS_FOR_WITHDRAWAL")
                            .build();
                }

                // 更新申请状态
                application.setStatus(LeaveCancellationApplication.ApplicationStatus.WITHDRAWN);
                application.setUpdateTime(LocalDateTime.now());
                // 在扩展属性中记录撤销原因
                if (application.getExtendedAttributes() == null) {
                    application.setExtendedAttributes(new HashMap<>());
                }
                application.getExtendedAttributes().put("withdrawalReason", reason);
                application.getExtendedAttributes().put("withdrawalTime", LocalDateTime.now().toString());
                application.getExtendedAttributes().put("withdrawalOperatorId", operatorId);

                // 更新索引
                updateIndexesAfterWithdrawal(application);

                return LeaveCancellationWithdrawalResult.builder()
                        .success(true)
                        .cancellationId(cancellationId)
                        .message("销假申请已撤销")
                        .withdrawalTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[销假服务] 撤销销假申请失败", e);
                return LeaveCancellationWithdrawalResult.builder()
                        .success(false)
                        .errorMessage("撤销销假申请时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<LeaveCancellationDetail> getLeaveCancellationDetail(String cancellationId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LeaveCancellationApplication application = cancellationApplications.get(cancellationId);
                if (application == null) {
                    return LeaveCancellationDetail.builder()
                            .cancellationId(cancellationId)
                            .exists(false)
                            .build();
                }

                return LeaveCancellationDetail.builder()
                        .cancellationId(cancellationId)
                        .exists(true)
                        .application(application)
                        .queryTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[销假服务] 获取销假申请详情失败: cancellationId={}", cancellationId, e);
                return LeaveCancellationDetail.builder()
                        .cancellationId(cancellationId)
                        .exists(false)
                        .errorMessage("获取详情时发生异常: " + e.getMessage())
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<LeaveCancellationListResult> getEmployeeLeaveCancellations(Long employeeId, LeaveCancellationQueryParam queryParam) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> cancellationIds = employeeCancellationIndex.getOrDefault(employeeId, Collections.emptyList());
                List<LeaveCancellationApplication> applications = cancellationIds.stream()
                        .map(cancellationApplications::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // 应用查询过滤条件
                applications = filterApplications(applications, queryParam);

                // 排序和分页
                List<LeaveCancellationApplication> pageData = paginateApplications(applications, queryParam);

                return LeaveCancellationListResult.builder()
                        .success(true)
                        .data(pageData)
                        .total(applications.size())
                        .pageNum(queryParam.getPageNum())
                        .pageSize(queryParam.getPageSize())
                        .queryTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[销假服务] 获取员工销假申请列表失败: employeeId={}", employeeId, e);
                return LeaveCancellationListResult.builder()
                        .success(false)
                        .errorMessage("获取销假申请列表时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<LeaveCancellationListResult> getPendingLeaveCancellations(LeaveCancellationQueryParam queryParam) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<LeaveCancellationApplication> allApplications = new ArrayList<>();

                // 收集所有待审批的申请
                for (List<String> cancellationIds : pendingApprovalIndex.values()) {
                    List<LeaveCancellationApplication> applications = cancellationIds.stream()
                            .map(cancellationApplications::get)
                            .filter(Objects::nonNull)
                            .filter(app -> app.getStatus() == LeaveCancellationApplication.ApplicationStatus.UNDER_REVIEW ||
                                          app.getStatus() == LeaveCancellationApplication.ApplicationStatus.SUBMITTED)
                            .collect(Collectors.toList());
                    allApplications.addAll(applications);
                }

                // 应用查询过滤条件
                allApplications = filterApplications(allApplications, queryParam);

                // 排序和分页
                List<LeaveCancellationApplication> pageData = paginateApplications(allApplications, queryParam);

                return LeaveCancellationListResult.builder()
                        .success(true)
                        .data(pageData)
                        .total(allApplications.size())
                        .pageNum(queryParam.getPageNum())
                        .pageSize(queryParam.getPageSize())
                        .queryTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[销假服务] 获取待审批销假申请列表失败", e);
                return LeaveCancellationListResult.builder()
                        .success(false)
                        .errorMessage("获取待审批销假申请列表时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    // 其他接口方法的实现...
    @Override
    public CompletableFuture<BatchLeaveCancellationApprovalResult> batchApproveLeaveCancellations(BatchLeaveCancellationApprovalRequest batchApprovalRequest) {
        return CompletableFuture.completedFuture(
                BatchLeaveCancellationApprovalResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<LeaveCancellationStatisticsResult> getLeaveCancellationStatistics(LeaveCancellationStatisticsRequest statisticsRequest) {
        return CompletableFuture.completedFuture(
                LeaveCancellationStatisticsResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<LeaveCancellationValidationResult> validateLeaveCancellation(LeaveCancellationValidationRequest validationRequest) {
        return CompletableFuture.completedFuture(
                LeaveCancellationValidationResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<AutoLeaveCancellationApprovalResult> autoApproveLeaveCancellations(AutoLeaveCancellationApprovalRequest autoApprovalRequest) {
        return CompletableFuture.completedFuture(
                AutoLeaveCancellationApprovalResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<LeaveCancellationImpactAnalysisResult> analyzeLeaveCancellationImpact(LeaveCancellationImpactAnalysisRequest impactAnalysisRequest) {
        return CompletableFuture.completedFuture(
                LeaveCancellationImpactAnalysisResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<List<LeaveCancellationHistoryRecord>> getLeaveCancellationHistory(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<LeaveCancellationExportResult> exportLeaveCancellations(LeaveCancellationExportRequest exportRequest) {
        return CompletableFuture.completedFuture(
                LeaveCancellationExportResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    // 私有辅助方法

    /**
     * 生成销假申请ID
     */
    private String generateCancellationId() {
        return "LC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 验证原请假记录
     */
    private boolean validateOriginalLeave(String originalLeaveId, Long employeeId) {
        // 简化实现，实际需要查询请假系统
        return originalLeaveId != null && employeeId != null;
    }

    /**
     * 创建销假申请
     */
    private LeaveCancellationApplication createCancellationApplication(LeaveCancellationApplicationRequest request) {
        LeaveCancellationApplication application = LeaveCancellationApplication.builder()
                .cancellationId(generateCancellationId())
                .originalLeaveId(request.getOriginalLeaveId())
                .employeeId(request.getEmployeeId())
                .employeeName(request.getEmployeeName())
                .employeeCode(request.getEmployeeCode())
                .departmentId(request.getDepartmentId())
                .departmentName(request.getDepartmentName())
                .originalLeaveType(request.getOriginalLeaveType())
                .originalLeaveStartDate(request.getOriginalLeaveStartDate())
                .originalLeaveEndDate(request.getOriginalLeaveEndDate())
                .originalLeaveDays(request.getOriginalLeaveDays())
                .originalLeaveReason(request.getOriginalLeaveReason())
                .cancellationType(request.getCancellationType())
                .cancellationStartDate(request.getCancellationStartDate())
                .cancellationEndDate(request.getCancellationEndDate())
                .cancellationReason(request.getCancellationReason())
                .cancellationReasonDetail(request.getCancellationReasonDetail())
                .status(LeaveCancellationApplication.ApplicationStatus.SUBMITTED)
                .applicationTime(LocalDateTime.now())
                .applicantId(request.getApplicantId())
                .applicantName(request.getApplicantName())
                .approverIds(request.getApproverIds())
                .approverNames(request.getApproverNames())
                .currentApprovalStep("第一环节")
                .approvalRecords(new ArrayList<>())
                .urgencyLevel(request.getUrgencyLevel())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .extendedAttributes(new HashMap<>())
                .build();

        // 计算销假天数
        application.calculateCancellationDays();

        return application;
    }

    /**
     * 执行影响评估
     */
    private void performImpactAssessment(LeaveCancellationApplication application) {
        LeaveCancellationApplication.CancellationImpactAssessment assessment = LeaveCancellationApplication.CancellationImpactAssessment.builder()
                .attendanceImpact("销假将恢复考勤正常记录")
                .salaryImpact("可能影响请假期间的工资计算")
                .workScheduleImpact("需要重新安排工作")
                .riskLevel(LeaveCancellationApplication.RiskLevel.LOW)
                .riskDescription("常规销假操作，风险较低")
                .recommendedActions(Arrays.asList("通知相关部门", "更新考勤记录", "重新安排工作"))
                .requiresSpecialApproval(false)
                .build();

        application.setImpactAssessment(assessment);
    }

    /**
     * 更新员工索引
     */
    private void updateEmployeeIndex(LeaveCancellationApplication application) {
        employeeCancellationIndex.computeIfAbsent(application.getEmployeeId(), k -> new ArrayList<>())
                .add(application.getCancellationId());
    }

    /**
     * 更新待审批索引
     */
    private void updatePendingApprovalIndex(LeaveCancellationApplication application) {
        if (application.getApproverIds() != null) {
            for (Long approverId : application.getApproverIds()) {
                pendingApprovalIndex.computeIfAbsent(approverId, k -> new ArrayList<>())
                        .add(application.getCancellationId());
            }
        }
    }

    /**
     * 估算审批时间
     */
    private LocalDateTime estimateApprovalTime(LeaveCancellationApplication application) {
        // 根据紧急程度估算
        int hours = application.getUrgencyLevel() == LeaveCancellationApplication.UrgencyLevel.VERY_URGENT ? 2 :
                   application.getUrgencyLevel() == LeaveCancellationApplication.UrgencyLevel.URGENT ? 8 : 24;
        return LocalDateTime.now().plusHours(hours);
    }

    /**
     * 验证审批权限
     */
    private boolean validateApprovalPermission(LeaveCancellationApplication application, Long approverId) {
        return application.getApproverIds() != null && application.getApproverIds().contains(approverId);
    }

    /**
     * 创建审批记录
     */
    private LeaveCancellationApplication.ApprovalRecord createApprovalRecord(LeaveCancellationApprovalRequest request) {
        return LeaveCancellationApplication.ApprovalRecord.builder()
                .approvalId(UUID.randomUUID().toString())
                .approvalStep(request.getApprovalStep())
                .approverId(request.getApproverId())
                .approverName(request.getApproverName())
                .approvalResult(LeaveCancellationApplication.ApprovalResult.valueOf(request.getApprovalResult()))
                .approvalComment(request.getApprovalComment())
                .approvalTime(LocalDateTime.now())
                .isProxyApproval(request.getIsProxyApproval())
                .proxyApproverName(request.getProxyApproverName())
                .build();
    }

    /**
     * 更新申请状态
     */
    private void updateApplicationStatus(LeaveCancellationApplication application, LeaveCancellationApprovalRequest request) {
        LeaveCancellationApplication.ApprovalResult result = LeaveCancellationApplication.ApprovalResult.valueOf(request.getApprovalResult());

        switch (result) {
            case APPROVED:
                // 检查是否为最终审批
                if (isFinalApproval(application, result)) {
                    application.setStatus(LeaveCancellationApplication.ApplicationStatus.APPROVED);
                } else {
                    application.setStatus(LeaveCancellationApplication.ApplicationStatus.UNDER_REVIEW);
                    application.setCurrentApprovalStep(getNextApprovalStep(application));
                }
                break;
            case REJECTED:
                application.setStatus(LeaveCancellationApplication.ApplicationStatus.REJECTED);
                break;
            case RETURNED:
                application.setStatus(LeaveCancellationApplication.ApplicationStatus.SUBMITTED);
                break;
        }

        application.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 检查是否为最终审批
     */
    private boolean isFinalApproval(LeaveCancellationApplication application, LeaveCancellationApplication.ApprovalResult result) {
        // 简化实现，实际需要根据审批流程判断
        return result == LeaveCancellationApplication.ApprovalResult.APPROVED &&
               (application.getApproverIds() == null ||
                application.getApproverIds().size() <= application.getApprovalRecords().size());
    }

    /**
     * 获取下一个审批环节
     */
    private String getNextApprovalStep(LeaveCancellationApplication application) {
        int currentStep = application.getApprovalRecords().size();
        return "第" + (currentStep + 1) + "环节";
    }

    /**
     * 执行销假生效
     */
    private void executeCancellationEffect(LeaveCancellationApplication application) {
        application.setStatus(LeaveCancellationApplication.ApplicationStatus.EFFECTIVE);
        application.setEffectiveTime(LocalDateTime.now());
        application.setApprovalCompletedTime(LocalDateTime.now());

        // 实际实现中需要：
        // 1. 更新考勤系统
        // 2. 更新请假系统
        // 3. 通知相关部门
        // 4. 记录操作日志
        log.info("[销假服务] 销假已生效: cancellationId={}", application.getCancellationId());
    }

    /**
     * 审批后更新索引
     */
    private void updateIndexesAfterApproval(LeaveCancellationApplication application) {
        // 如果申请已通过，从待审批索引中移除
        if (application.getStatus() == LeaveCancellationApplication.ApplicationStatus.APPROVED ||
            application.getStatus() == LeaveCancellationApplication.ApplicationStatus.REJECTED) {
            removeFromPendingApprovalIndex(application);
        }
    }

    /**
     * 驳回后更新索引
     */
    private void updateIndexesAfterRejection(LeaveCancellationApplication application) {
        removeFromPendingApprovalIndex(application);
    }

    /**
     * 撤销后更新索引
     */
    private void updateIndexesAfterWithdrawal(LeaveCancellationApplication application) {
        removeFromPendingApprovalIndex(application);
    }

    /**
     * 从待审批索引中移除
     */
    private void removeFromPendingApprovalIndex(LeaveCancellationApplication application) {
        if (application.getApproverIds() != null) {
            for (Long approverId : application.getApproverIds()) {
                List<String> approverCancellations = pendingApprovalIndex.get(approverId);
                if (approverCancellations != null) {
                    approverCancellations.remove(application.getCancellationId());
                    if (approverCancellations.isEmpty()) {
                        pendingApprovalIndex.remove(approverId);
                    }
                }
            }
        }
    }

    /**
     * 验证是否为管理员
     */
    private boolean isAdmin(Long userId) {
        // 简化实现，实际需要查询权限系统
        return false;
    }

    /**
     * 检查是否可以撤销
     */
    private boolean canWithdraw(LeaveCancellationApplication.ApplicationStatus status) {
        return status == LeaveCancellationApplication.ApplicationStatus.DRAFT ||
               status == LeaveCancellationApplication.ApplicationStatus.SUBMITTED ||
               status == LeaveCancellationApplication.ApplicationStatus.UNDER_REVIEW;
    }

    /**
     * 过滤申请列表
     */
    private List<LeaveCancellationApplication> filterApplications(List<LeaveCancellationApplication> applications,
                                                                     LeaveCancellationQueryParam queryParam) {
        return applications.stream()
                .filter(app -> queryParam.getStatus() == null || queryParam.getStatus().equals(app.getStatus()))
                .filter(app -> queryParam.getCancellationType() == null ||
                               queryParam.getCancellationType().equals(app.getCancellationType()))
                .filter(app -> queryParam.getUrgencyLevel() == null ||
                               queryParam.getUrgencyLevel().equals(app.getUrgencyLevel()))
                .collect(Collectors.toList());
    }

    /**
     * 分页处理
     */
    private List<LeaveCancellationApplication> paginateApplications(List<LeaveCancellationApplication> applications,
                                                                  LeaveCancellationQueryParam queryParam) {
        int total = applications.size();
        int start = queryParam.getPageNum() * queryParam.getPageSize();
        int end = Math.min(start + queryParam.getPageSize(), total);

        // 排序
        applications.sort((a, b) -> b.getApplicationTime().compareTo(a.getApplicationTime()));

        return start < total ? applications.subList(start, end) : Collections.emptyList();
    }
}