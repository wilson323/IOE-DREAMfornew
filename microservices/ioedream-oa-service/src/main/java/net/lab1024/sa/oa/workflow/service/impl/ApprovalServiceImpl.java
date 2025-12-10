package net.lab1024.sa.oa.workflow.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.oa.workflow.service.ApprovalService;
import net.lab1024.sa.common.workflow.domain.form.ApprovalTaskQueryForm;
import net.lab1024.sa.common.workflow.domain.form.ApprovalActionForm;
import net.lab1024.sa.common.workflow.domain.vo.ApprovalTaskVO;
import net.lab1024.sa.common.workflow.domain.vo.ApprovalInstanceVO;
import net.lab1024.sa.common.workflow.domain.vo.ApprovalStatisticsVO;
import net.lab1024.sa.common.workflow.dao.ApprovalTaskDao;
import net.lab1024.sa.common.workflow.dao.ApprovalInstanceDao;
import net.lab1024.sa.common.workflow.dao.ApprovalStatisticsDao;
import net.lab1024.sa.common.workflow.entity.WorkflowTaskEntity;
import net.lab1024.sa.common.workflow.entity.WorkflowInstanceEntity;

import jakarta.annotation.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 企业级审批服务实现类
 * <p>
 * 实现完整的审批管理功能，严格遵循CLAUDE.md规范：
 * - 四层架构规范：Controller → Service → Manager → DAO
 * - 统一使用@Resource依赖注入
 * - 完善的事务管理和异常处理
 * - 详细的日志记录和性能优化
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ApprovalServiceImpl implements ApprovalService {

    @Resource
    private ApprovalTaskDao approvalTaskDao;

    @Resource
    private ApprovalInstanceDao approvalInstanceDao;

    @Resource
    private ApprovalStatisticsDao approvalStatisticsDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    // 缓存业务类型和优先级数据
    private static final List<Map<String, Object>> BUSINESS_TYPES_CACHE = Arrays.asList(
        Map.of("code", "LEAVE", "name", "请假", "description", "员工请假申请"),
        Map.of("code", "EXPENSE", "name", "报销", "description", "费用报销申请"),
        Map.of("code", "TRAVEL", "name", "出差", "description", "出差申请"),
        Map.of("code", "OVERTIME", "name", "加班", "description", "加班申请"),
        Map.of("code", "RESIGNATION", "name", "离职", "description", "员工离职申请"),
        Map.of("code", "PROCUREMENT", "name", "采购", "description", "物品采购申请"),
        Map.of("code", "EQUIPMENT", "name", "设备", "description", "设备申请"),
        Map.of("code", "OTHER", "name", "其他", "description", "其他类型申请")
    );

    private static final List<Map<String, Object>> PRIORITIES_CACHE = Arrays.asList(
        Map.of("code", "URGENT", "name", "紧急", "level", 1, "description", "紧急审批，需立即处理"),
        Map.of("code", "HIGH", "name", "高", "level", 2, "description", "高优先级，建议尽快处理"),
        Map.of("code", "NORMAL", "name", "普通", "level", 3, "description", "普通优先级，按常规流程处理"),
        Map.of("code", "LOW", "name", "低", "level", 4, "description", "低优先级，可延后处理")
    );

    @Override
    @Transactional(readOnly = true)
    public PageResult<ApprovalTaskVO> getTodoTasks(ApprovalTaskQueryForm queryForm) {
        log.info("[审批服务] 查询待办任务: userId={}, status={}, businessType={}",
                queryForm.getUserId(), queryForm.getStatus(), queryForm.getBusinessType());

        try {
            // 参数验证
            if (queryForm.getUserId() == null) {
                log.warn("[审批服务] 待办任务查询失败: 用户ID为空");
                return PageResult.empty(queryForm.getPageNum(), queryForm.getPageSize());
            }

            // 设置默认查询条件
            if (queryForm.getStatus() == null) {
                queryForm.setStatus("PENDING");
            }

            // 查询待办任务
            List<WorkflowTaskEntity> taskEntities = approvalTaskDao.selectTodoTasks(queryForm);
            Long total = approvalTaskDao.countTodoTasks(queryForm);

            // 转换为VO对象
            List<ApprovalTaskVO> taskVOs = taskEntities.stream()
                    .map(this::convertToTaskVO)
                    .collect(Collectors.toList());

            PageResult<ApprovalTaskVO> result = new PageResult<>();
            result.setList(taskVOs);
            result.setTotal(total);
            result.setPageNum(queryForm.getPageNum());
            result.setPageSize(queryForm.getPageSize());

            log.info("[审批服务] 待办任务查询完成: total={}, count={}", total, taskVOs.size());
            return result;

        } catch (Exception e) {
            log.error("[审批服务] 查询待办任务异常: userId={}, error={}",
                    queryForm.getUserId(), e.getMessage(), e);
            return PageResult.empty(queryForm.getPageNum(), queryForm.getPageSize());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ApprovalTaskVO> getCompletedTasks(ApprovalTaskQueryForm queryForm) {
        log.info("[审批服务] 查询已办任务: userId={}, businessType={}",
                queryForm.getUserId(), queryForm.getBusinessType());

        try {
            // 参数验证
            if (queryForm.getUserId() == null) {
                log.warn("[审批服务] 已办任务查询失败: 用户ID为空");
                return PageResult.empty(queryForm.getPageNum(), queryForm.getPageSize());
            }

            // 查询已办任务
            List<WorkflowTaskEntity> taskEntities = approvalTaskDao.selectCompletedTasks(queryForm);
            Long total = approvalTaskDao.countCompletedTasks(queryForm);

            // 转换为VO对象
            List<ApprovalTaskVO> taskVOs = taskEntities.stream()
                    .map(this::convertToTaskVO)
                    .collect(Collectors.toList());

            PageResult<ApprovalTaskVO> result = new PageResult<>();
            result.setList(taskVOs);
            result.setTotal(total);
            result.setPageNum(queryForm.getPageNum());
            result.setPageSize(queryForm.getPageSize());

            log.info("[审批服务] 已办任务查询完成: total={}, count={}", total, taskVOs.size());
            return result;

        } catch (Exception e) {
            log.error("[审批服务] 查询已办任务异常: userId={}, error={}",
                    queryForm.getUserId(), e.getMessage(), e);
            return PageResult.empty(queryForm.getPageNum(), queryForm.getPageSize());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ApprovalTaskVO> getMyApplications(ApprovalTaskQueryForm queryForm) {
        log.info("[审批服务] 查询我的申请: applicantId={}, businessType={}",
                queryForm.getApplicantId(), queryForm.getBusinessType());

        try {
            // 参数验证
            if (queryForm.getApplicantId() == null) {
                log.warn("[审批服务] 我的申请查询失败: 申请人ID为空");
                return PageResult.empty(queryForm.getPageNum(), queryForm.getPageSize());
            }

            // 查询我的申请
            List<WorkflowTaskEntity> taskEntities = approvalTaskDao.selectMyApplications(queryForm);
            Long total = approvalTaskDao.countMyApplications(queryForm);

            // 转换为VO对象
            List<ApprovalTaskVO> taskVOs = taskEntities.stream()
                    .map(this::convertToTaskVO)
                    .collect(Collectors.toList());

            PageResult<ApprovalTaskVO> result = new PageResult<>();
            result.setList(taskVOs);
            result.setTotal(total);
            result.setPageNum(queryForm.getPageNum());
            result.setPageSize(queryForm.getPageSize());

            log.info("[审批服务] 我的申请查询完成: total={}, count={}", total, taskVOs.size());
            return result;

        } catch (Exception e) {
            log.error("[审批服务] 查询我的申请异常: applicantId={}, error={}",
                    queryForm.getApplicantId(), e.getMessage(), e);
            return PageResult.empty(queryForm.getPageNum(), queryForm.getPageSize());
        }
    }

    @Override
    public String approveTask(ApprovalActionForm actionForm) {
        log.info("[审批服务] 审批同意: taskId={}, userId={}", actionForm.getTaskId(), actionForm.getUserId());

        try {
            // 参数验证
            validateApprovalAction(actionForm, "APPROVE");

            // 查询任务信息
            WorkflowTaskEntity task = approvalTaskDao.selectById(actionForm.getTaskId());
            if (task == null) {
                throw new RuntimeException("审批任务不存在: " + actionForm.getTaskId());
            }

            // 验证任务状态（1-待受理 2-处理中）
            Integer status = task.getStatus();
            if (status == null || (status != 1 && status != 2)) {
                throw new RuntimeException("任务状态不允许审批: " + status);
            }

            // 验证审批人权限
            if (!actionForm.getUserId().equals(task.getAssigneeId())) {
                throw new RuntimeException("无权限审批此任务");
            }

            // 更新任务状态（3-已完成）
            task.setStatus(3);
            task.setResult(1); // 1-同意
            task.setComment(actionForm.getComment());
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            int updated = approvalTaskDao.updateById(task);
            if (updated <= 0) {
                throw new RuntimeException("更新审批任务状态失败");
            }

            // 更新流程实例状态
            updateInstanceStatusAfterTask(task.getInstanceId(), actionForm.getUserId());

            // 发送通知（异步）
            sendApprovalNotification(task, "APPROVED", actionForm.getUserId());

            // 记录操作日志
            logApprovalAction(task, "APPROVE", actionForm.getUserId(), actionForm.getComment());

            log.info("[审批服务] 审批同意成功: taskId={}, taskName={}", task.getId(), task.getTaskName());
            return "审批同意成功";

        } catch (Exception e) {
            log.error("[审批服务] 审批同意异常: taskId={}, userId={}, error={}",
                    actionForm.getTaskId(), actionForm.getUserId(), e.getMessage(), e);
            throw new RuntimeException("审批同意失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String rejectTask(ApprovalActionForm actionForm) {
        log.info("[审批服务] 审批驳回: taskId={}, userId={}, comment={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getComment());

        try {
            // 参数验证
            validateApprovalAction(actionForm, "REJECT");

            // 查询任务信息
            WorkflowTaskEntity task = approvalTaskDao.selectById(actionForm.getTaskId());
            if (task == null) {
                throw new RuntimeException("审批任务不存在: " + actionForm.getTaskId());
            }

            // 验证任务状态（1-待受理 2-处理中）
            Integer status = task.getStatus();
            if (status == null || (status != 1 && status != 2)) {
                throw new RuntimeException("任务状态不允许审批: " + status);
            }

            // 验证审批人权限
            if (!actionForm.getUserId().equals(task.getAssigneeId())) {
                throw new RuntimeException("无权限审批此任务");
            }

            // 更新任务状态（6-已驳回）
            task.setStatus(6);
            task.setResult(2); // 2-驳回
            task.setComment(actionForm.getComment());
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            int updated = approvalTaskDao.updateById(task);
            if (updated <= 0) {
                throw new RuntimeException("更新审批任务状态失败");
            }

            // 更新流程实例状态
            updateInstanceStatusAfterTask(task.getInstanceId(), actionForm.getUserId());

            // 发送通知（异步）
            sendApprovalNotification(task, "REJECTED", actionForm.getUserId());

            // 记录操作日志
            logApprovalAction(task, "REJECT", actionForm.getUserId(), actionForm.getComment());

            log.info("[审批服务] 审批驳回成功: taskId={}, taskName={}", task.getId(), task.getTaskName());
            return "审批驳回成功";

        } catch (Exception e) {
            log.error("[审批服务] 审批驳回异常: taskId={}, userId={}, error={}",
                    actionForm.getTaskId(), actionForm.getUserId(), e.getMessage(), e);
            throw new RuntimeException("审批驳回失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String transferTask(ApprovalActionForm actionForm) {
        log.info("[审批服务] 审批转办: taskId={}, userId={}, targetUserId={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId());

        try {
            // 参数验证
            validateApprovalAction(actionForm, "TRANSFER");
            if (actionForm.getTargetUserId() == null) {
                throw new RuntimeException("目标审批人ID不能为空");
            }

            // 查询任务信息
            WorkflowTaskEntity task = approvalTaskDao.selectById(actionForm.getTaskId());
            if (task == null) {
                throw new RuntimeException("审批任务不存在: " + actionForm.getTaskId());
            }

            // 验证任务状态（1-待受理 2-处理中）
            Integer status = task.getStatus();
            if (status == null || (status != 1 && status != 2)) {
                throw new RuntimeException("任务状态不允许转办: " + status);
            }

            // 验证审批人权限
            if (!actionForm.getUserId().equals(task.getAssigneeId())) {
                throw new RuntimeException("无权限转办此任务");
            }

            // 更新任务审批人（转办：4-已转交）
            Long oldAssigneeId = task.getAssigneeId();
            task.setOriginalAssigneeId(oldAssigneeId);
            task.setOriginalAssigneeName(task.getAssigneeName());
            task.setAssigneeId(actionForm.getTargetUserId());
            task.setAssigneeName(actionForm.getTargetUserName());
            task.setStatus(4); // 4-已转交
            task.setResult(3); // 3-转交
            task.setComment(actionForm.getComment());
            task.setUpdateTime(LocalDateTime.now());

            int updated = approvalTaskDao.updateById(task);
            if (updated <= 0) {
                throw new RuntimeException("更新审批任务转办失败");
            }

            // 发送通知（异步）
            sendTransferNotification(task, oldAssigneeId, actionForm.getUserId(), actionForm.getTargetUserId());

            // 记录操作日志
            logApprovalAction(task, "TRANSFER", actionForm.getUserId(),
                    "转办给用户" + actionForm.getTargetUserId() + ": " + actionForm.getComment());

            log.info("[审批服务] 审批转办成功: taskId={}, oldAssigneeId={}, newAssigneeId={}",
                    task.getId(), oldAssigneeId, actionForm.getTargetUserId());
            return "审批转办成功";

        } catch (Exception e) {
            log.error("[审批服务] 审批转办异常: taskId={}, userId={}, targetUserId={}, error={}",
                    actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getMessage(), e);
            throw new RuntimeException("审批转办失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String delegateTask(ApprovalActionForm actionForm) {
        log.info("[审批服务] 审批委派: taskId={}, userId={}, targetUserId={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId());

        try {
            // 参数验证
            validateApprovalAction(actionForm, "DELEGATE");
            if (actionForm.getTargetUserId() == null) {
                throw new RuntimeException("委派人ID不能为空");
            }

            // 查询任务信息
            WorkflowTaskEntity task = approvalTaskDao.selectById(actionForm.getTaskId());
            if (task == null) {
                throw new RuntimeException("审批任务不存在: " + actionForm.getTaskId());
            }

            // 验证任务状态（1-待受理 2-处理中）
            Integer status = task.getStatus();
            if (status == null || (status != 1 && status != 2)) {
                throw new RuntimeException("任务状态不允许委派: " + status);
            }

            // 验证审批人权限
            if (!actionForm.getUserId().equals(task.getAssigneeId())) {
                throw new RuntimeException("无权限委派此任务");
            }

            // 更新任务委派信息（委派：5-已委派）
            Long originalAssigneeId = task.getAssigneeId();
            task.setOriginalAssigneeId(originalAssigneeId);
            task.setOriginalAssigneeName(task.getAssigneeName());
            task.setAssigneeId(actionForm.getTargetUserId());
            task.setAssigneeName(actionForm.getTargetUserName());
            task.setStatus(5); // 5-已委派
            task.setResult(4); // 4-委派
            task.setComment(actionForm.getComment());
            task.setUpdateTime(LocalDateTime.now());

            int updated = approvalTaskDao.updateById(task);
            if (updated <= 0) {
                throw new RuntimeException("更新审批任务委派失败");
            }

            // 发送通知（异步）
            sendDelegateNotification(task, originalAssigneeId, actionForm.getUserId(), actionForm.getTargetUserId());

            // 记录操作日志
            logApprovalAction(task, "DELEGATE", actionForm.getUserId(),
                    "委派给用户" + actionForm.getTargetUserId() + ": " + actionForm.getComment());

            log.info("[审批服务] 审批委派成功: taskId={}, originalAssigneeId={}, targetUserId={}",
                    task.getId(), originalAssigneeId, actionForm.getTargetUserId());
            return "审批委派成功";

        } catch (Exception e) {
            log.error("[审批服务] 审批委派异常: taskId={}, userId={}, targetUserId={}, error={}",
                    actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getMessage(), e);
            throw new RuntimeException("审批委派失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalTaskVO getTaskDetail(Long taskId) {
        log.info("[审批服务] 获取审批任务详情: taskId={}", taskId);

        try {
            if (taskId == null) {
                log.warn("[审批服务] 任务ID为空");
                return null;
            }

            WorkflowTaskEntity task = approvalTaskDao.selectById(taskId);
            if (task == null) {
                log.warn("[审批服务] 审批任务不存在: taskId={}", taskId);
                return null;
            }

            ApprovalTaskVO taskVO = convertToTaskVO(task);

            log.info("[审批服务] 审批任务详情获取成功: taskId={}", taskId);
            return taskVO;

        } catch (Exception e) {
            log.error("[审批服务] 获取审批任务详情异常: taskId={}, error={}", taskId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalInstanceVO getInstanceDetail(Long instanceId) {
        log.info("[审批服务] 获取审批流程详情: instanceId={}", instanceId);

        try {
            if (instanceId == null) {
                log.warn("[审批服务] 流程实例ID为空");
                return null;
            }

            WorkflowInstanceEntity instance = approvalInstanceDao.selectById(instanceId);
            if (instance == null) {
                log.warn("[审批服务] 审批流程实例不存在: instanceId={}", instanceId);
                return null;
            }

            ApprovalInstanceVO instanceVO = convertToInstanceVO(instance);

            log.info("[审批服务] 审批流程详情获取成功: instanceId={}", instanceId);
            return instanceVO;

        } catch (Exception e) {
            log.error("[审批服务] 获取审批流程详情异常: instanceId={}, error={}", instanceId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalStatisticsVO getApprovalStatistics(Long userId, Long departmentId, String statisticsType) {
        log.info("[审批服务] 获取审批统计: userId={}, departmentId={}, type={}", userId, departmentId, statisticsType);

        try {
            ApprovalStatisticsVO statistics = approvalStatisticsDao.selectStatistics(userId, departmentId, statisticsType);

            if (statistics == null) {
                // 返回默认统计信息
                statistics = new ApprovalStatisticsVO();
                statistics.setTodoCount(0L);
                statistics.setCompletedCount(0L);
                statistics.setApprovedCount(0L);
                statistics.setRejectedCount(0L);
                statistics.setTransferCount(0L);
                statistics.setDelegateCount(0L);
                statistics.setOverdueCount(0L);
            }

            log.info("[审批服务] 审批统计获取成功: userId={}, departmentId={}, type={}",
                    userId, departmentId, statisticsType);
            return statistics;

        } catch (Exception e) {
            log.error("[审批服务] 获取审批统计异常: userId={}, departmentId={}, type={}, error={}",
                    userId, departmentId, statisticsType, e.getMessage(), e);

            // 返回默认统计信息
            ApprovalStatisticsVO defaultStats = new ApprovalStatisticsVO();
            defaultStats.setTodoCount(0L);
            defaultStats.setCompletedCount(0L);
            defaultStats.setApprovedCount(0L);
            defaultStats.setRejectedCount(0L);
            defaultStats.setTransferCount(0L);
            defaultStats.setDelegateCount(0L);
            defaultStats.setOverdueCount(0L);
            return defaultStats;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBusinessTypes() {
        log.debug("[审批服务] 获取业务类型列表");

        try {
            // 返回缓存的数据
            return new ArrayList<>(BUSINESS_TYPES_CACHE);

        } catch (Exception e) {
            log.error("[审批服务] 获取业务类型列表异常: error={}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPriorities() {
        log.debug("[审批服务] 获取审批优先级列表");

        try {
            // 返回缓存的数据
            return new ArrayList<>(PRIORITIES_CACHE);

        } catch (Exception e) {
            log.error("[审批服务] 获取审批优先级列表异常: error={}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> batchProcessTasks(List<Long> taskIds, String action, Long userId, String comment) {
        log.info("[审批服务] 批量审批处理: taskIds={}, action={}, userId={}",
                taskIds, action, userId);

        Map<String, Object> result = new HashMap<>();
        List<Long> successIds = new ArrayList<>();
        List<Map<String, Object>> failedIds = new ArrayList<>();

        int successCount = 0;
        int failedCount = 0;

        for (Long taskId : taskIds) {
            try {
                ApprovalActionForm actionForm = new ApprovalActionForm();
                actionForm.setTaskId(taskId);
                actionForm.setUserId(userId);
                actionForm.setComment(comment);

                String processResult;
                switch (action.toUpperCase()) {
                    case "APPROVE":
                        processResult = approveTask(actionForm);
                        break;
                    case "REJECT":
                        processResult = rejectTask(actionForm);
                        break;
                    default:
                        throw new RuntimeException("不支持的批量操作类型: " + action);
                }

                // 检查处理结果（如果返回null或空字符串，表示失败）
                if (processResult != null && !processResult.isEmpty()) {
                    successIds.add(taskId);
                    successCount++;
                } else {
                    Map<String, Object> failedInfo = new HashMap<>();
                    failedInfo.put("taskId", taskId);
                    failedInfo.put("reason", "处理失败");
                    failedIds.add(failedInfo);
                    failedCount++;
                }

            } catch (Exception e) {
                log.error("[审批服务] 批量处理任务失败: taskId={}, action={}, error={}",
                        taskId, action, e.getMessage());

                Map<String, Object> failedTask = new HashMap<>();
                failedTask.put("taskId", taskId);
                failedTask.put("error", e.getMessage());
                failedIds.add(failedTask);
                failedCount++;
            }
        }

        result.put("totalCount", taskIds.size());
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("successIds", successIds);
        result.put("failedTasks", failedIds);

        log.info("[审批服务] 批量审批处理完成: totalCount={}, successCount={}, failedCount={}",
                taskIds.size(), successCount, failedCount);
        return result;
    }

    @Override
    public String withdrawApplication(Long instanceId, Long applicantId, String reason) {
        log.info("[审批服务] 撤回申请: instanceId={}, applicantId={}, reason={}",
                instanceId, applicantId, reason);

        try {
            // 参数验证
            if (instanceId == null) {
                throw new RuntimeException("流程实例ID不能为空");
            }
            if (applicantId == null) {
                throw new RuntimeException("申请人ID不能为空");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new RuntimeException("撤回理由不能为空");
            }

            // 查询流程实例
            WorkflowInstanceEntity instance = approvalInstanceDao.selectById(instanceId);
            if (instance == null) {
                throw new RuntimeException("审批流程实例不存在: " + instanceId);
            }

            // 验证申请人权限
            if (!applicantId.equals(instance.getInitiatorId())) {
                throw new RuntimeException("无权限撤回此申请");
            }

            // 验证流程状态（1-运行中）
            Integer status = instance.getStatus();
            if (status == null || status != 1) {
                throw new RuntimeException("流程状态不允许撤回: " + status);
            }

            // 更新流程状态（3-已终止）
            instance.setStatus(3);
            instance.setReason(reason);
            instance.setEndTime(LocalDateTime.now());
            instance.setUpdateTime(LocalDateTime.now());

            int updated = approvalInstanceDao.updateById(instance);
            if (updated <= 0) {
                throw new RuntimeException("更新流程状态失败");
            }

            // 更新相关任务状态
            updateTasksStatusAfterWithdraw(instanceId);

            // 发送通知（异步）
            sendWithdrawNotification(instance, applicantId, reason);

            log.info("[审批服务] 申请撤回成功: instanceId={}", instanceId);
            return "申请撤回成功";

        } catch (Exception e) {
            log.error("[审批服务] 撤回申请异常: instanceId={}, applicantId={}, error={}",
                    instanceId, applicantId, e.getMessage(), e);
            throw new RuntimeException("撤回申请失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证审批操作参数
     */
    private void validateApprovalAction(ApprovalActionForm actionForm, String actionType) {
        if (actionForm.getTaskId() == null) {
            throw new RuntimeException("任务ID不能为空");
        }
        if (actionForm.getUserId() == null) {
            throw new RuntimeException("操作人ID不能为空");
        }
        if ("REJECT".equals(actionType) && (actionForm.getComment() == null || actionForm.getComment().trim().isEmpty())) {
            throw new RuntimeException("驳回操作需要填写审批意见");
        }
    }

    /**
     * 实体转VO对象
     *
     * @param entity 任务实体
     * @return 任务VO
     */
    private ApprovalTaskVO convertToTaskVO(WorkflowTaskEntity entity) {
        ApprovalTaskVO vo = new ApprovalTaskVO();
        vo.setTaskId(entity.getId());
        vo.setInstanceId(entity.getInstanceId());
        vo.setTaskName(entity.getTaskName());
        vo.setTaskType(entity.getTaskType());
        vo.setNodeId(entity.getNodeId());
        vo.setNodeName(entity.getNodeName());
        vo.setAssigneeId(entity.getAssigneeId());
        vo.setAssigneeName(entity.getAssigneeName());
        vo.setOriginalAssigneeId(entity.getOriginalAssigneeId());
        vo.setOriginalAssigneeName(entity.getOriginalAssigneeName());
        vo.setPriority(entity.getPriority());
        vo.setDueTime(entity.getDueTime());
        vo.setStatus(entity.getStatus());
        vo.setStatusDesc(getStatusDesc(entity.getStatus()));
        vo.setResult(entity.getResult());
        vo.setComment(entity.getComment());
        vo.setTaskCreateTime(entity.getTaskCreateTime());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setDuration(entity.getDuration());
        vo.setProcessName(entity.getProcessName());
        
        // 从流程实例获取业务信息
        if (entity.getInstanceId() != null) {
            WorkflowInstanceEntity instance = approvalInstanceDao.selectById(entity.getInstanceId());
            if (instance != null) {
                vo.setBusinessType(instance.getBusinessType());
                vo.setBusinessId(instance.getBusinessId());
                vo.setInitiatorId(instance.getInitiatorId());
                vo.setInitiatorName(instance.getInitiatorName());
            }
        }
        
        return vo;
    }

    /**
     * 获取状态描述
     *
     * @param status 状态值
     * @return 状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "待受理";
            case 2:
                return "处理中";
            case 3:
                return "已完成";
            case 4:
                return "已转交";
            case 5:
                return "已委派";
            case 6:
                return "已驳回";
            default:
                return "未知";
        }
    }

    /**
     * 实体转VO对象（流程实例）
     *
     * @param entity 流程实例实体
     * @return 流程实例VO
     */
    private ApprovalInstanceVO convertToInstanceVO(WorkflowInstanceEntity entity) {
        ApprovalInstanceVO vo = new ApprovalInstanceVO();
        vo.setInstanceId(entity.getId());
        vo.setProcessDefinitionId(entity.getProcessDefinitionId());
        vo.setProcessKey(entity.getProcessKey());
        vo.setProcessName(entity.getProcessName());
        vo.setBusinessType(entity.getBusinessType());
        vo.setBusinessId(entity.getBusinessId());
        vo.setInitiatorId(entity.getInitiatorId());
        vo.setInitiatorName(entity.getInitiatorName());
        vo.setCurrentNodeId(entity.getCurrentNodeId());
        vo.setCurrentNodeName(entity.getCurrentNodeName());
        vo.setStatus(entity.getStatus());
        vo.setStatusDesc(getInstanceStatusDesc(entity.getStatus()));
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setDuration(entity.getDuration());
        vo.setReason(entity.getReason());
        return vo;
    }

    /**
     * 获取流程实例状态描述
     *
     * @param status 状态值
     * @return 状态描述
     */
    private String getInstanceStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "运行中";
            case 2:
                return "已完成";
            case 3:
                return "已终止";
            case 4:
                return "已挂起";
            default:
                return "未知";
        }
    }

    /**
     * 更新流程实例状态（任务完成后）
     *
     * @param instanceId 流程实例ID
     * @param userId 用户ID
     */
    private void updateInstanceStatusAfterTask(Long instanceId, Long userId) {
        try {
            // 查询该实例的所有待办任务
            List<WorkflowTaskEntity> pendingTasks = approvalTaskDao.selectPendingTasksByInstance(instanceId);

            if (pendingTasks.isEmpty()) {
                // 没有待办任务，流程已完成
                WorkflowInstanceEntity instance = approvalInstanceDao.selectById(instanceId);
                if (instance != null) {
                    instance.setStatus(2); // 2-已完成
                    instance.setEndTime(LocalDateTime.now());
                    instance.setUpdateTime(LocalDateTime.now());
                    approvalInstanceDao.updateById(instance);
                    log.info("[审批服务] 流程实例已自动完成: instanceId={}", instanceId);
                }
            }
        } catch (Exception e) {
            log.error("[审批服务] 更新流程实例状态异常: instanceId={}, error={}", instanceId, e.getMessage(), e);
        }
    }

    /**
     * 更新任务状态（撤回后）
     *
     * @param instanceId 流程实例ID
     */
    private void updateTasksStatusAfterWithdraw(Long instanceId) {
        try {
            // 查询该实例的所有待办任务
            List<WorkflowTaskEntity> pendingTasks = approvalTaskDao.selectPendingTasksByInstance(instanceId);

            for (WorkflowTaskEntity task : pendingTasks) {
                // 任务状态保持原样，因为流程实例已终止
                task.setUpdateTime(LocalDateTime.now());
                approvalTaskDao.updateById(task);
            }

            log.info("[审批服务] 任务状态已更新: instanceId={}, count={}", instanceId, pendingTasks.size());
        } catch (Exception e) {
            log.error("[审批服务] 更新任务状态异常: instanceId={}, error={}", instanceId, e.getMessage(), e);
        }
    }

    /**
     * 发送审批通知
     */
    private void sendApprovalNotification(WorkflowTaskEntity task, String action, Long operatorId) {
        try {
            // 异步发送通知，避免阻塞主流程
            CompletableFuture.runAsync(() -> {
                try {
                    // 获取流程实例信息
                    WorkflowInstanceEntity instance = approvalInstanceDao.selectById(task.getInstanceId());
                    
                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("taskId", task.getId());
                    notificationData.put("title", task.getTaskName());
                    notificationData.put("action", action);
                    notificationData.put("operatorId", operatorId);
                    notificationData.put("comment", task.getComment());
                    if (instance != null) {
                        notificationData.put("applicantId", instance.getInitiatorId());
                        notificationData.put("businessType", instance.getBusinessType());
                    }

                    // 调用通知服务
                    if (gatewayServiceClient != null) {
                        gatewayServiceClient.callCommonService(
                            "/api/v1/notification/approval",
                            HttpMethod.POST,
                            Map.of("notification", notificationData),
                            Map.class
                        );
                    }

                } catch (Exception e) {
                    log.error("[审批服务] 发送审批通知异常: taskId={}, action={}, error={}",
                            task.getId(), action, e.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("[审批服务] 发送审批通知异常: taskId={}, error={}", task.getId(), e.getMessage());
        }
    }

    /**
     * 发送转办通知
     */
    private void sendTransferNotification(WorkflowTaskEntity task, Long oldApproverId, Long operatorId, Long newApproverId) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("taskId", task.getId());
            notificationData.put("title", task.getTaskName());
            notificationData.put("type", "TRANSFER");
            notificationData.put("oldApproverId", oldApproverId);
            notificationData.put("newApproverId", newApproverId);
            notificationData.put("operatorId", operatorId);
            notificationData.put("comment", task.getComment());

            if (gatewayServiceClient != null) {
                gatewayServiceClient.callCommonService(
                    "/api/v1/notification/approval/transfer",
                    HttpMethod.POST,
                    Map.of("notification", notificationData),
                    Map.class
                );
            }

        } catch (Exception e) {
            log.error("[审批服务] 发送转办通知异常: taskId={}, error={}", task.getId(), e.getMessage());
        }
    }

    /**
     * 发送委派通知
     */
    private void sendDelegateNotification(WorkflowTaskEntity task, Long originalApproverId, Long operatorId, Long delegateUserId) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("taskId", task.getId());
            notificationData.put("title", task.getTaskName());
            notificationData.put("type", "DELEGATE");
            notificationData.put("originalApproverId", originalApproverId);
            notificationData.put("delegateUserId", delegateUserId);
            notificationData.put("operatorId", operatorId);
            notificationData.put("comment", task.getComment());

            if (gatewayServiceClient != null) {
                gatewayServiceClient.callCommonService(
                    "/api/v1/notification/approval/delegate",
                    HttpMethod.POST,
                    Map.of("notification", notificationData),
                    Map.class
                );
            }

        } catch (Exception e) {
            log.error("[审批服务] 发送委派通知异常: taskId={}, error={}", task.getId(), e.getMessage());
        }
    }

    /**
     * 发送撤回通知
     */
    private void sendWithdrawNotification(WorkflowInstanceEntity instance, Long applicantId, String reason) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("instanceId", instance.getId());
            notificationData.put("title", instance.getProcessName());
            notificationData.put("type", "WITHDRAW");
            notificationData.put("applicantId", applicantId);
            notificationData.put("reason", reason);

            if (gatewayServiceClient != null) {
                gatewayServiceClient.callCommonService(
                    "/api/v1/notification/approval/withdraw",
                    HttpMethod.POST,
                    Map.of("notification", notificationData),
                    Map.class
                );
            }

        } catch (Exception e) {
            log.error("[审批服务] 发送撤回通知异常: instanceId={}, error={}", instance.getId(), e.getMessage());
        }
    }

    /**
     * 记录审批操作日志
     *
     * @param task 任务实体
     * @param action 操作类型
     * @param operatorId 操作人ID
     * @param comment 操作意见
     */
    private void logApprovalAction(WorkflowTaskEntity task, String action, Long operatorId, String comment) {
        try {
            log.info("[审批操作] 记录审批操作: taskId={}, action={}, operatorId={}, taskName={}, comment={}",
                    task.getId(), action, operatorId, task.getTaskName(), comment);
        } catch (Exception e) {
            log.error("[审批服务] 记录审批操作日志异常: error={}", e.getMessage());
        }
    }
}