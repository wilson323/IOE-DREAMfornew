package net.lab1024.sa.oa.workflow.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.oa.workflow.service.ApprovalService;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalTaskQueryForm;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalActionForm;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalTaskVO;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalInstanceVO;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalStatisticsVO;
import net.lab1024.sa.oa.workflow.dao.WorkflowTaskDao;
import net.lab1024.sa.oa.workflow.dao.ApprovalInstanceDao;
import net.lab1024.sa.oa.workflow.dao.ApprovalStatisticsDao;
import net.lab1024.sa.oa.domain.entity.WorkflowTaskEntity;
import net.lab1024.sa.oa.domain.entity.WorkflowInstanceEntity;

import io.micrometer.observation.annotation.Observed;
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
    private WorkflowTaskDao workflowTaskDao;

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
    @Observed(name = "approval.task.getTodo", contextualName = "approval-task-get-todo")
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
            List<WorkflowTaskEntity> taskEntities = workflowTaskDao.selectTodoTasks(queryForm);
            Long total = workflowTaskDao.countTodoTasks(queryForm);

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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 查询待办任务参数错误: userId={}, error={}", queryForm.getUserId(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批服务] 查询待办任务业务异常: userId={}, code={}, message={}", queryForm.getUserId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批服务] 查询待办任务系统异常: userId={}, code={}, message={}", queryForm.getUserId(), e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_TODO_TASKS_SYSTEM_ERROR", "查询待办任务失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 查询待办任务未知异常: userId={}", queryForm.getUserId(), e);
            throw new SystemException("GET_TODO_TASKS_SYSTEM_ERROR", "查询待办任务失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "approval.task.getCompleted", contextualName = "approval-task-get-completed")
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
            List<WorkflowTaskEntity> taskEntities = workflowTaskDao.selectCompletedTasks(queryForm);
            Long total = workflowTaskDao.countCompletedTasks(queryForm);

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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 查询已办任务参数错误: userId={}, error={}", queryForm.getUserId(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批服务] 查询已办任务业务异常: userId={}, code={}, message={}", queryForm.getUserId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批服务] 查询已办任务系统异常: userId={}, code={}, message={}", queryForm.getUserId(), e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_COMPLETED_TASKS_SYSTEM_ERROR", "查询已办任务失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 查询已办任务未知异常: userId={}", queryForm.getUserId(), e);
            throw new SystemException("GET_COMPLETED_TASKS_SYSTEM_ERROR", "查询已办任务失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "approval.application.getMy", contextualName = "approval-application-get-my")
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
            List<WorkflowTaskEntity> taskEntities = workflowTaskDao.selectMyApplications(queryForm);
            Long total = workflowTaskDao.countMyApplications(queryForm);

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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 查询我的申请参数错误: applicantId={}, error={}", queryForm.getApplicantId(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批服务] 查询我的申请业务异常: applicantId={}, code={}, message={}", queryForm.getApplicantId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批服务] 查询我的申请系统异常: applicantId={}, code={}, message={}", queryForm.getApplicantId(), e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_MY_APPLICATIONS_SYSTEM_ERROR", "查询我的申请失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 查询我的申请未知异常: applicantId={}", queryForm.getApplicantId(), e);
            throw new SystemException("GET_MY_APPLICATIONS_SYSTEM_ERROR", "查询我的申请失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "approval.task.approve", contextualName = "approval-task-approve")
    public String approveTask(ApprovalActionForm actionForm) {
        log.info("[审批服务] 审批同意: taskId={}, userId={}", actionForm.getTaskId(), actionForm.getUserId());

        try {
            // 参数验证
            validateApprovalAction(actionForm, "APPROVE");

            // 查询任务信息
            WorkflowTaskEntity task = workflowTaskDao.selectById(actionForm.getTaskId());
            if (task == null) {
                throw new BusinessException("TASK_NOT_FOUND", "审批任务不存在: " + actionForm.getTaskId());
            }

            // 验证任务状态（1-待受理 2-处理中）
            Integer status = task.getStatus();
            if (status == null || (status != 1 && status != 2)) {
                throw new BusinessException("TASK_STATUS_INVALID", "任务状态不允许审批: " + status);
            }

            // 验证审批人权限
            if (!actionForm.getUserId().equals(task.getAssigneeId())) {
                throw new BusinessException("NO_PERMISSION", "无权限审批此任务");
            }

            // 更新任务状态（3-已完成）
            task.setStatus(3);
            task.setResult(1); // 1-同意
            task.setComment(actionForm.getComment());
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            int updated = workflowTaskDao.updateById(task);
            if (updated <= 0) {
                throw new BusinessException("TASK_UPDATE_FAILED", "更新审批任务状态失败");
            }

            // 更新流程实例状态
            updateInstanceStatusAfterTask(task.getInstanceId(), actionForm.getUserId());

            // 发送通知（异步）
            sendApprovalNotification(task, "APPROVED", actionForm.getUserId());

            // 记录操作日志
            logApprovalAction(task, "APPROVE", actionForm.getUserId(), actionForm.getComment());

            log.info("[审批服务] 审批同意成功: taskId={}, taskName={}", task.getId(), task.getTaskName());
            return "审批同意成功";

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 审批同意参数错误: taskId={}, userId={}, error={}", actionForm.getTaskId(), actionForm.getUserId(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批服务] 审批同意业务异常: taskId={}, userId={}, code={}, message={}", actionForm.getTaskId(), actionForm.getUserId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批服务] 审批同意系统异常: taskId={}, userId={}, code={}, message={}", actionForm.getTaskId(), actionForm.getUserId(), e.getCode(), e.getMessage(), e);
            throw new SystemException("APPROVE_TASK_SYSTEM_ERROR", "审批同意失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 审批同意未知异常: taskId={}, userId={}", actionForm.getTaskId(), actionForm.getUserId(), e);
            throw new SystemException("APPROVE_TASK_SYSTEM_ERROR", "审批同意失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "approval.task.reject", contextualName = "approval-task-reject")
    public String rejectTask(ApprovalActionForm actionForm) {
        log.info("[审批服务] 审批驳回: taskId={}, userId={}, comment={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getComment());

        try {
            // 参数验证
            validateApprovalAction(actionForm, "REJECT");

            // 查询任务信息
            WorkflowTaskEntity task = workflowTaskDao.selectById(actionForm.getTaskId());
            if (task == null) {
                throw new BusinessException("TASK_NOT_FOUND", "审批任务不存在: " + actionForm.getTaskId());
            }

            // 验证任务状态（1-待受理 2-处理中）
            Integer status = task.getStatus();
            if (status == null || (status != 1 && status != 2)) {
                throw new BusinessException("TASK_STATUS_INVALID", "任务状态不允许审批: " + status);
            }

            // 验证审批人权限
            if (!actionForm.getUserId().equals(task.getAssigneeId())) {
                throw new BusinessException("NO_PERMISSION", "无权限审批此任务");
            }

            // 更新任务状态（6-已驳回）
            task.setStatus(6);
            task.setResult(2); // 2-驳回
            task.setComment(actionForm.getComment());
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            int updated = workflowTaskDao.updateById(task);
            if (updated <= 0) {
                throw new BusinessException("TASK_UPDATE_FAILED", "更新审批任务状态失败");
            }

            // 更新流程实例状态
            updateInstanceStatusAfterTask(task.getInstanceId(), actionForm.getUserId());

            // 发送通知（异步）
            sendApprovalNotification(task, "REJECTED", actionForm.getUserId());

            // 记录操作日志
            logApprovalAction(task, "REJECT", actionForm.getUserId(), actionForm.getComment());

            log.info("[审批服务] 审批驳回成功: taskId={}, taskName={}", task.getId(), task.getTaskName());
            return "审批驳回成功";

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 审批驳回参数错误: taskId={}, userId={}, error={}", actionForm.getTaskId(), actionForm.getUserId(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批服务] 审批驳回业务异常: taskId={}, userId={}, code={}, message={}", actionForm.getTaskId(), actionForm.getUserId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批服务] 审批驳回系统异常: taskId={}, userId={}, code={}, message={}", actionForm.getTaskId(), actionForm.getUserId(), e.getCode(), e.getMessage(), e);
            throw new SystemException("REJECT_TASK_SYSTEM_ERROR", "审批驳回失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 审批驳回未知异常: taskId={}, userId={}", actionForm.getTaskId(), actionForm.getUserId(), e);
            throw new SystemException("REJECT_TASK_SYSTEM_ERROR", "审批驳回失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "approval.task.transfer", contextualName = "approval-task-transfer")
    public String transferTask(ApprovalActionForm actionForm) {
        log.info("[审批服务] 审批转办: taskId={}, userId={}, targetUserId={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId());

        try {
            // 参数验证
            validateApprovalAction(actionForm, "TRANSFER");
            if (actionForm.getTargetUserId() == null) {
                throw new ParamException("TARGET_USER_ID_REQUIRED", "目标审批人ID不能为空");
            }

            // 查询任务信息
            WorkflowTaskEntity task = workflowTaskDao.selectById(actionForm.getTaskId());
            if (task == null) {
                throw new BusinessException("TASK_NOT_FOUND", "审批任务不存在: " + actionForm.getTaskId());
            }

            // 验证任务状态（1-待受理 2-处理中）
            Integer status = task.getStatus();
            if (status == null || (status != 1 && status != 2)) {
                throw new BusinessException("TASK_STATUS_INVALID", "任务状态不允许转办: " + status);
            }

            // 验证审批人权限
            if (!actionForm.getUserId().equals(task.getAssigneeId())) {
                throw new BusinessException("NO_PERMISSION", "无权限转办此任务");
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

            int updated = workflowTaskDao.updateById(task);
            if (updated <= 0) {
                throw new BusinessException("TASK_UPDATE_FAILED", "更新审批任务转办失败");
            }

            // 发送通知（异步）
            sendTransferNotification(task, oldAssigneeId, actionForm.getUserId(), actionForm.getTargetUserId());

            // 记录操作日志
            logApprovalAction(task, "TRANSFER", actionForm.getUserId(),
                    "转办给用户" + actionForm.getTargetUserId() + ": " + actionForm.getComment());

            log.info("[审批服务] 审批转办成功: taskId={}, oldAssigneeId={}, newAssigneeId={}",
                    task.getId(), oldAssigneeId, actionForm.getTargetUserId());
            return "审批转办成功";

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 审批转办参数错误: taskId={}, userId={}, targetUserId={}, error={}", actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批服务] 审批转办业务异常: taskId={}, userId={}, targetUserId={}, code={}, message={}", actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批服务] 审批转办系统异常: taskId={}, userId={}, targetUserId={}, code={}, message={}", actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getCode(), e.getMessage(), e);
            throw new SystemException("TRANSFER_TASK_SYSTEM_ERROR", "审批转办失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 审批转办未知异常: taskId={}, userId={}, targetUserId={}", actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e);
            throw new SystemException("TRANSFER_TASK_SYSTEM_ERROR", "审批转办失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "approval.task.delegate", contextualName = "approval-task-delegate")
    public String delegateTask(ApprovalActionForm actionForm) {
        log.info("[审批服务] 审批委派: taskId={}, userId={}, targetUserId={}",
                actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId());

        try {
            // 参数验证
            validateApprovalAction(actionForm, "DELEGATE");
            if (actionForm.getTargetUserId() == null) {
                throw new ParamException("TARGET_USER_ID_REQUIRED", "委派人ID不能为空");
            }

            // 查询任务信息
            WorkflowTaskEntity task = workflowTaskDao.selectById(actionForm.getTaskId());
            if (task == null) {
                throw new BusinessException("TASK_NOT_FOUND", "审批任务不存在: " + actionForm.getTaskId());
            }

            // 验证任务状态（1-待受理 2-处理中）
            Integer status = task.getStatus();
            if (status == null || (status != 1 && status != 2)) {
                throw new BusinessException("TASK_STATUS_INVALID", "任务状态不允许委派: " + status);
            }

            // 验证审批人权限
            if (!actionForm.getUserId().equals(task.getAssigneeId())) {
                throw new BusinessException("NO_PERMISSION", "无权限委派此任务");
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

            int updated = workflowTaskDao.updateById(task);
            if (updated <= 0) {
                throw new BusinessException("TASK_UPDATE_FAILED", "更新审批任务委派失败");
            }

            // 发送通知（异步）
            sendDelegateNotification(task, originalAssigneeId, actionForm.getUserId(), actionForm.getTargetUserId());

            // 记录操作日志
            logApprovalAction(task, "DELEGATE", actionForm.getUserId(),
                    "委派给用户" + actionForm.getTargetUserId() + ": " + actionForm.getComment());

            log.info("[审批服务] 审批委派成功: taskId={}, originalAssigneeId={}, targetUserId={}",
                    task.getId(), originalAssigneeId, actionForm.getTargetUserId());
            return "审批委派成功";

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 审批委派参数错误: taskId={}, userId={}, targetUserId={}, error={}", actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批服务] 审批委派业务异常: taskId={}, userId={}, targetUserId={}, code={}, message={}", actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批服务] 审批委派系统异常: taskId={}, userId={}, targetUserId={}, code={}, message={}", actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e.getCode(), e.getMessage(), e);
            throw new SystemException("DELEGATE_TASK_SYSTEM_ERROR", "审批委派失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 审批委派未知异常: taskId={}, userId={}, targetUserId={}", actionForm.getTaskId(), actionForm.getUserId(), actionForm.getTargetUserId(), e);
            throw new SystemException("DELEGATE_TASK_SYSTEM_ERROR", "审批委派失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "approval.task.getDetail", contextualName = "approval-task-get-detail")
    @Transactional(readOnly = true)
    public ApprovalTaskVO getTaskDetail(Long taskId) {
        log.info("[审批服务] 获取审批任务详情: taskId={}", taskId);

        try {
            if (taskId == null) {
                log.warn("[审批服务] 任务ID为空");
                return null;
            }

            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                log.warn("[审批服务] 审批任务不存在: taskId={}", taskId);
                return null;
            }

            ApprovalTaskVO taskVO = convertToTaskVO(task);

            log.info("[审批服务] 审批任务详情获取成功: taskId={}", taskId);
            return taskVO;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 获取审批任务详情参数错误: taskId={}, error={}", taskId, e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.warn("[审批服务] 获取审批任务详情业务异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.error("[审批服务] 获取审批任务详情系统异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("[审批服务] 获取审批任务详情未知异常: taskId={}", taskId, e);
            return null;
        }
    }

    @Override
    @Observed(name = "approval.instance.getDetail", contextualName = "approval-instance-get-detail")
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 获取审批流程详情参数错误: instanceId={}, error={}", instanceId, e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.warn("[审批服务] 获取审批流程详情业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.error("[审批服务] 获取审批流程详情系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("[审批服务] 获取审批流程详情未知异常: instanceId={}", instanceId, e);
            return null;
        }
    }

    @Override
    @Observed(name = "approval.statistics.get", contextualName = "approval-statistics-get")
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 获取审批统计参数错误: userId={}, departmentId={}, type={}, error={}", userId, departmentId, statisticsType, e.getMessage());
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
        } catch (BusinessException e) {
            log.warn("[审批服务] 获取审批统计业务异常: userId={}, departmentId={}, type={}, code={}, message={}", userId, departmentId, statisticsType, e.getCode(), e.getMessage());
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
        } catch (SystemException e) {
            log.error("[审批服务] 获取审批统计系统异常: userId={}, departmentId={}, type={}, code={}, message={}", userId, departmentId, statisticsType, e.getCode(), e.getMessage(), e);
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
        } catch (Exception e) {
            log.error("[审批服务] 获取审批统计未知异常: userId={}, departmentId={}, type={}", userId, departmentId, statisticsType, e);
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
    @Observed(name = "approval.businessTypes.get", contextualName = "approval-business-types-get")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBusinessTypes() {
        log.debug("[审批服务] 获取业务类型列表");

        try {
            // 返回缓存的数据
            return new ArrayList<>(BUSINESS_TYPES_CACHE);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 获取业务类型列表参数错误: error={}", e.getMessage());
            return Collections.emptyList();
        } catch (BusinessException e) {
            log.warn("[审批服务] 获取业务类型列表业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return Collections.emptyList();
        } catch (SystemException e) {
            log.error("[审批服务] 获取业务类型列表系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("[审批服务] 获取业务类型列表未知异常", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Observed(name = "approval.priorities.get", contextualName = "approval-priorities-get")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPriorities() {
        log.debug("[审批服务] 获取审批优先级列表");

        try {
            // 返回缓存的数据
            return new ArrayList<>(PRIORITIES_CACHE);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 获取优先级列表参数错误: error={}", e.getMessage());
            return Collections.emptyList();
        } catch (BusinessException e) {
            log.warn("[审批服务] 获取优先级列表业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return Collections.emptyList();
        } catch (SystemException e) {
            log.error("[审批服务] 获取优先级列表系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("[审批服务] 获取优先级列表未知异常", e);
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
                        throw new ParamException("BATCH_ACTION_INVALID", "不支持的批量操作类型: " + action);
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

            } catch (IllegalArgumentException | ParamException e) {
                log.warn("[审批服务] 批量处理任务参数错误: taskId={}, action={}, error={}",
                        taskId, action, e.getMessage());
                Map<String, Object> failedTask = new HashMap<>();
                failedTask.put("taskId", taskId);
                failedTask.put("error", "参数错误：" + e.getMessage());
                failedIds.add(failedTask);
                failedCount++;
            } catch (BusinessException e) {
                log.warn("[审批服务] 批量处理任务业务异常: taskId={}, action={}, code={}, message={}",
                        taskId, action, e.getCode(), e.getMessage());
                Map<String, Object> failedTask = new HashMap<>();
                failedTask.put("taskId", taskId);
                failedTask.put("error", e.getMessage());
                failedIds.add(failedTask);
                failedCount++;
            } catch (SystemException e) {
                log.error("[审批服务] 批量处理任务系统异常: taskId={}, action={}, code={}, message={}",
                        taskId, action, e.getCode(), e.getMessage(), e);
                Map<String, Object> failedTask = new HashMap<>();
                failedTask.put("taskId", taskId);
                failedTask.put("error", "系统异常：" + e.getMessage());
                failedIds.add(failedTask);
                failedCount++;
            } catch (Exception e) {
                log.error("[审批服务] 批量处理任务未知异常: taskId={}, action={}",
                        taskId, action, e);
                Map<String, Object> failedTask = new HashMap<>();
                failedTask.put("taskId", taskId);
                failedTask.put("error", "未知异常：" + e.getMessage());
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
                throw new ParamException("WITHDRAW_INSTANCE_ID_NULL", "流程实例ID不能为空");
            }
            if (applicantId == null) {
                throw new ParamException("WITHDRAW_APPLICANT_ID_NULL", "申请人ID不能为空");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new ParamException("WITHDRAW_REASON_NULL", "撤回理由不能为空");
            }

            // 查询流程实例
            WorkflowInstanceEntity instance = approvalInstanceDao.selectById(instanceId);
            if (instance == null) {
                throw new BusinessException("APPROVAL_INSTANCE_NOT_FOUND", "审批流程实例不存在: " + instanceId);
            }

            // 验证申请人权限
            if (!applicantId.equals(instance.getInitiatorId())) {
                throw new BusinessException("WITHDRAW_PERMISSION_DENIED", "无权限撤回此申请");
            }

            // 验证流程状态（1-运行中）
            Integer status = instance.getStatus();
            if (status == null || status != 1) {
                throw new BusinessException("WITHDRAW_STATUS_INVALID", "流程状态不允许撤回: " + status);
            }

            // 更新流程状态（3-已终止）
            instance.setStatus(3);
            instance.setReason(reason);
            instance.setEndTime(LocalDateTime.now());
            instance.setUpdateTime(LocalDateTime.now());

            int updated = approvalInstanceDao.updateById(instance);
            if (updated <= 0) {
                throw new SystemException("WITHDRAW_UPDATE_FAILED", "更新流程状态失败");
            }

            // 更新相关任务状态
            updateTasksStatusAfterWithdraw(instanceId);

            // 发送通知（异步）
            sendWithdrawNotification(instance, applicantId, reason);

            log.info("[审批服务] 申请撤回成功: instanceId={}", instanceId);
            return "申请撤回成功";

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 撤回申请参数错误: instanceId={}, applicantId={}, error={}",
                    instanceId, applicantId, e.getMessage());
            throw new ParamException("WITHDRAW_PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批服务] 撤回申请业务异常: instanceId={}, applicantId={}, code={}, message={}",
                    instanceId, applicantId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批服务] 撤回申请系统异常: instanceId={}, applicantId={}, code={}, message={}",
                    instanceId, applicantId, e.getCode(), e.getMessage(), e);
            throw new SystemException("WITHDRAW_SYSTEM_ERROR", "撤回申请失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 撤回申请未知异常: instanceId={}, applicantId={}",
                    instanceId, applicantId, e);
            throw new SystemException("WITHDRAW_SYSTEM_ERROR", "撤回申请失败：" + e.getMessage(), e);
        }
    }

    // ==================== Flowable工作流引擎集成方法实现 ====================

    @Override
    @Observed(name = "approval.handleTaskCreated", contextualName = "approval-handle-task-created")
    public void handleTaskCreated(String taskId, String processInstanceId) {
        log.info("[审批服务] 处理Flowable任务创建事件: taskId={}, processInstanceId={}", taskId, processInstanceId);

        try {
            // 参数验证
            if (taskId == null || processInstanceId == null) {
                log.warn("[审批服务] 任务创建事件参数为空: taskId={}, processInstanceId={}", taskId, processInstanceId);
                return;
            }

            // 查询本地流程实例
            WorkflowInstanceEntity instance = approvalInstanceDao.selectByFlowableInstanceId(processInstanceId);
            if (instance == null) {
                log.warn("[审批服务] 未找到本地流程实例: flowableInstanceId={}", processInstanceId);
                return;
            }

            // 检查是否已存在本地任务记录
            WorkflowTaskEntity existingTask = workflowTaskDao.selectByFlowableTaskId(taskId);
            if (existingTask != null) {
                log.debug("[审批服务] 本地任务记录已存在: taskId={}", taskId);
                return;
            }

            // 创建本地任务记录
            WorkflowTaskEntity task = new WorkflowTaskEntity();
            task.setFlowableTaskId(taskId);
            task.setFlowableProcessInstanceId(processInstanceId);
            task.setInstanceId(instance.getId());
            task.setTaskName(instance.getProcessName() + "-待审批");
            task.setTaskType("APPROVAL");
            task.setNodeId("approval");
            task.setNodeName("审批节点");
            task.setAssigneeId(instance.getCurrentApproverId());
            task.setAssigneeName(instance.getCurrentApproverName());
            task.setPriority(instance.getPriority() != null ? instance.getPriority() : 3);
            task.setStatus(1); // 1-待受理
            task.setOutcome(0); // 0-未处理
            task.setTaskCreateTime(LocalDateTime.now());
            task.setStartTime(LocalDateTime.now());
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            int inserted = workflowTaskDao.insert(task);
            if (inserted > 0) {
                log.info("[审批服务] 本地任务记录创建成功: taskId={}, localTaskId={}", taskId, task.getId());
            } else {
                log.error("[审批服务] 本地任务记录创建失败: taskId={}", taskId);
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 处理任务创建事件参数错误: taskId={}, processInstanceId={}, error={}",
                    taskId, processInstanceId, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 处理任务创建事件业务异常: taskId={}, processInstanceId={}, code={}, message={}",
                    taskId, processInstanceId, e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 处理任务创建事件系统异常: taskId={}, processInstanceId={}, code={}, message={}",
                    taskId, processInstanceId, e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 处理任务创建事件未知异常: taskId={}, processInstanceId={}",
                    taskId, processInstanceId, e);
        }
    }

    @Override
    @Observed(name = "approval.handleTaskAssigned", contextualName = "approval-handle-task-assigned")
    public void handleTaskAssigned(String taskId, Long assigneeId) {
        log.info("[审批服务] 处理Flowable任务分配事件: taskId={}, assigneeId={}", taskId, assigneeId);

        try {
            // 参数验证
            if (taskId == null || assigneeId == null) {
                log.warn("[审批服务] 任务分配事件参数为空: taskId={}, assigneeId={}", taskId, assigneeId);
                return;
            }

            // 查询本地任务记录
            WorkflowTaskEntity task = workflowTaskDao.selectByFlowableTaskId(taskId);
            if (task == null) {
                log.warn("[审批服务] 未找到本地任务记录: flowableTaskId={}", taskId);
                return;
            }

            // 更新任务分配信息
            task.setAssigneeId(assigneeId);
            task.setAssigneeTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            int updated = workflowTaskDao.updateById(task);
            if (updated > 0) {
                log.info("[审批服务] 任务分配信息更新成功: taskId={}, assigneeId={}", taskId, assigneeId);
            } else {
                log.error("[审批服务] 任务分配信息更新失败: taskId={}", taskId);
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 处理任务分配事件参数错误: taskId={}, assigneeId={}, error={}",
                    taskId, assigneeId, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 处理任务分配事件业务异常: taskId={}, assigneeId={}, code={}, message={}",
                    taskId, assigneeId, e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 处理任务分配事件系统异常: taskId={}, assigneeId={}, code={}, message={}",
                    taskId, assigneeId, e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 处理任务分配事件未知异常: taskId={}, assigneeId={}",
                    taskId, assigneeId, e);
        }
    }

    @Override
    @Observed(name = "approval.handleTaskCompleted", contextualName = "approval-handle-task-completed")
    public void handleTaskCompleted(String taskId, Integer outcome, String comment) {
        log.info("[审批服务] 处理Flowable任务完成事件: taskId={}, outcome={}, comment={}", taskId, outcome, comment);

        try {
            // 参数验证
            if (taskId == null || outcome == null) {
                log.warn("[审批服务] 任务完成事件参数为空: taskId={}, outcome={}", taskId, outcome);
                return;
            }

            // 查询本地任务记录
            WorkflowTaskEntity task = workflowTaskDao.selectByFlowableTaskId(taskId);
            if (task == null) {
                log.warn("[审批服务] 未找到本地任务记录: flowableTaskId={}", taskId);
                return;
            }

            // 更新任务完成信息
            task.setStatus(3); // 3-已完成
            task.setResult(outcome); // 1-同意 2-驳回
            task.setComment(comment);
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            // 计算处理时长
            if (task.getStartTime() != null) {
                long duration = java.time.Duration.between(task.getStartTime(), LocalDateTime.now()).toMinutes();
                task.setDuration(duration);
            }

            int updated = workflowTaskDao.updateById(task);
            if (updated > 0) {
                log.info("[审批服务] 任务完成信息更新成功: taskId={}, outcome={}", taskId, outcome);
            } else {
                log.error("[审批服务] 任务完成信息更新失败: taskId={}", taskId);
            }

            // 更新流程实例状态
            updateInstanceStatusAfterTask(task.getInstanceId(), task.getAssigneeId());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 处理任务完成事件参数错误: taskId={}, outcome={}, error={}",
                    taskId, outcome, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 处理任务完成事件业务异常: taskId={}, outcome={}, code={}, message={}",
                    taskId, outcome, e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 处理任务完成事件系统异常: taskId={}, outcome={}, code={}, message={}",
                    taskId, outcome, e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 处理任务完成事件未知异常: taskId={}, outcome={}",
                    taskId, outcome, e);
        }
    }

    @Override
    @Observed(name = "approval.handleProcessStarted", contextualName = "approval-handle-process-started")
    public void handleProcessStarted(String processInstanceId, String processDefinitionId) {
        log.info("[审批服务] 处理Flowable流程启动事件: processInstanceId={}, processDefinitionId={}", processInstanceId, processDefinitionId);

        try {
            // 参数验证
            if (processInstanceId == null || processDefinitionId == null) {
                log.warn("[审批服务] 流程启动事件参数为空: processInstanceId={}, processDefinitionId={}", processInstanceId, processDefinitionId);
                return;
            }

            // 查询本地流程实例
            WorkflowInstanceEntity instance = approvalInstanceDao.selectByFlowableInstanceId(processInstanceId);
            if (instance == null) {
                log.warn("[审批服务] 未找到本地流程实例: flowableInstanceId={}", processInstanceId);
                return;
            }

            // 更新流程实例状态为运行中
            instance.setStatus(1); // 1-运行中
            instance.setUpdateTime(LocalDateTime.now());

            int updated = approvalInstanceDao.updateById(instance);
            if (updated > 0) {
                log.info("[审批服务] 流程实例状态更新成功: processInstanceId={}, status=RUNNING", processInstanceId);
            } else {
                log.error("[审批服务] 流程实例状态更新失败: processInstanceId={}", processInstanceId);
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 处理流程启动事件参数错误: processInstanceId={}, processDefinitionId={}, error={}",
                    processInstanceId, processDefinitionId, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 处理流程启动事件业务异常: processInstanceId={}, processDefinitionId={}, code={}, message={}",
                    processInstanceId, processDefinitionId, e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 处理流程启动事件系统异常: processInstanceId={}, processDefinitionId={}, code={}, message={}",
                    processInstanceId, processDefinitionId, e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 处理流程启动事件未知异常: processInstanceId={}, processDefinitionId={}",
                    processInstanceId, processDefinitionId, e);
        }
    }

    @Override
    @Observed(name = "approval.handleProcessCompleted", contextualName = "approval-handle-process-completed")
    public void handleProcessCompleted(String processInstanceId, String processDefinitionId) {
        log.info("[审批服务] 处理Flowable流程完成事件: processInstanceId={}, processDefinitionId={}", processInstanceId, processDefinitionId);

        try {
            // 参数验证
            if (processInstanceId == null || processDefinitionId == null) {
                log.warn("[审批服务] 流程完成事件参数为空: processInstanceId={}, processDefinitionId={}", processInstanceId, processDefinitionId);
                return;
            }

            // 查询本地流程实例
            WorkflowInstanceEntity instance = approvalInstanceDao.selectByFlowableInstanceId(processInstanceId);
            if (instance == null) {
                log.warn("[审批服务] 未找到本地流程实例: flowableInstanceId={}", processInstanceId);
                return;
            }

            // 更新流程实例状态为已完成
            instance.setStatus(2); // 2-已完成
            instance.setEndTime(LocalDateTime.now());
            instance.setUpdateTime(LocalDateTime.now());

            // 计算流程总时长
            if (instance.getStartTime() != null) {
                long duration = java.time.Duration.between(instance.getStartTime(), LocalDateTime.now()).toMinutes();
                instance.setDuration(duration);
            }

            int updated = approvalInstanceDao.updateById(instance);
            if (updated > 0) {
                log.info("[审批服务] 流程实例完成状态更新成功: processInstanceId={}, status=COMPLETED", processInstanceId);
            } else {
                log.error("[审批服务] 流程实例完成状态更新失败: processInstanceId={}", processInstanceId);
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 处理流程完成事件参数错误: processInstanceId={}, processDefinitionId={}, error={}",
                    processInstanceId, processDefinitionId, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 处理流程完成事件业务异常: processInstanceId={}, processDefinitionId={}, code={}, message={}",
                    processInstanceId, processDefinitionId, e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 处理流程完成事件系统异常: processInstanceId={}, processDefinitionId={}, code={}, message={}",
                    processInstanceId, processDefinitionId, e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 处理流程完成事件未知异常: processInstanceId={}, processDefinitionId={}",
                    processInstanceId, processDefinitionId, e);
        }
    }

    @Override
    @Observed(name = "approval.handleStartNode", contextualName = "approval-handle-start-node")
    public void handleStartNode(String processInstanceId, Map<String, Object> variables) {
        log.info("[审批服务] 处理开始节点业务逻辑: processInstanceId={}", processInstanceId);

        try {
            // 参数验证
            if (processInstanceId == null) {
                log.warn("[审批服务] 开始节点处理参数为空: processInstanceId={}", processInstanceId);
                return;
            }

            if (variables == null || variables.isEmpty()) {
                log.debug("[审批服务] 开始节点无流程变量: processInstanceId={}", processInstanceId);
                return;
            }

            // 查询本地流程实例
            WorkflowInstanceEntity instance = approvalInstanceDao.selectByFlowableInstanceId(processInstanceId);
            if (instance == null) {
                log.warn("[审批服务] 未找到本地流程实例: flowableInstanceId={}", processInstanceId);
                return;
            }

            // 处理开始节点的业务逻辑
            // 1. 初始化流程变量
            if (!variables.containsKey("initiatorId")) {
                variables.put("initiatorId", instance.getInitiatorId());
            }
            if (!variables.containsKey("businessType")) {
                variables.put("businessType", instance.getBusinessType());
            }
            if (!variables.containsKey("businessId")) {
                variables.put("businessId", instance.getBusinessId());
            }

            // 2. 设置当前审批节点信息
            instance.setCurrentNodeId("approval");
            instance.setCurrentNodeName("审批节点");
            instance.setUpdateTime(LocalDateTime.now());

            int updated = approvalInstanceDao.updateById(instance);
            if (updated > 0) {
                log.info("[审批服务] 开始节点处理完成: processInstanceId={}", processInstanceId);
            } else {
                log.error("[审批服务] 开始节点处理失败: processInstanceId={}", processInstanceId);
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 开始节点处理参数错误: processInstanceId={}, error={}", processInstanceId, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 开始节点处理业务异常: processInstanceId={}, code={}, message={}", processInstanceId, e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 开始节点处理系统异常: processInstanceId={}, code={}, message={}", processInstanceId, e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 开始节点处理未知异常: processInstanceId={}", processInstanceId, e);
        }
    }

    @Override
    @Observed(name = "approval.validateBusinessRule", contextualName = "approval-validate-business-rule")
    public boolean validateBusinessRule(Map<String, Object> variables) {
        log.info("[审批服务] 验证业务规则: variables={}", variables);

        try {
            if (variables == null || variables.isEmpty()) {
                log.warn("[审批服务] 业务规则验证失败: 流程变量为空");
                return false;
            }

            // 业务规则验证逻辑
            // 1. 检查必要的流程变量
            String businessType = (String) variables.get("businessType");
            Long initiatorId = (Long) variables.get("initiatorId");
            Long businessId = (Long) variables.get("businessId");

            if (businessType == null || initiatorId == null || businessId == null) {
                log.warn("[审批服务] 业务规则验证失败: 缺少必要变量 businessType={}, initiatorId={}, businessId={}",
                        businessType, initiatorId, businessId);
                return false;
            }

            // 2. 根据业务类型进行特定验证
            boolean isValid = true;
            String validationMessage = null;

            switch (businessType.toUpperCase()) {
                case "LEAVE":
                    // 请假申请验证：检查请假天数、余额等
                    Integer leaveDays = (Integer) variables.get("leaveDays");
                    if (leaveDays != null && leaveDays > 30) {
                        isValid = false;
                        validationMessage = "请假天数不能超过30天";
                    }
                    break;

                case "EXPENSE":
                    // 报销申请验证：检查金额限制等
                    Double expenseAmount = (Double) variables.get("expenseAmount");
                    if (expenseAmount != null && expenseAmount > 10000.0) {
                        isValid = false;
                        validationMessage = "报销金额不能超过10000元";
                    }
                    break;

                case "TRAVEL":
                    // 出差申请验证：检查出差天数等
                    Integer travelDays = (Integer) variables.get("travelDays");
                    if (travelDays != null && travelDays > 15) {
                        isValid = false;
                        validationMessage = "出差天数不能超过15天";
                    }
                    break;

                default:
                    // 其他业务类型默认通过
                    break;
            }

            if (!isValid) {
                log.warn("[审批服务] 业务规则验证失败: businessType={}, message={}", businessType, validationMessage);
            } else {
                log.info("[审批服务] 业务规则验证通过: businessType={}", businessType);
            }

            return isValid;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 业务规则验证参数错误: error={}", e.getMessage());
            return false;
        } catch (BusinessException e) {
            log.warn("[审批服务] 业务规则验证业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return false;
        } catch (SystemException e) {
            log.error("[审批服务] 业务规则验证系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[审批服务] 业务规则验证未知异常", e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证审批操作参数
     */
    private void validateApprovalAction(ApprovalActionForm actionForm, String actionType) {
        if (actionForm.getTaskId() == null) {
            throw new ParamException("APPROVAL_TASK_ID_NULL", "任务ID不能为空");
        }
        if (actionForm.getUserId() == null) {
            throw new ParamException("APPROVAL_USER_ID_NULL", "操作人ID不能为空");
        }
        if ("REJECT".equals(actionType) && (actionForm.getComment() == null || actionForm.getComment().trim().isEmpty())) {
            throw new ParamException("APPROVAL_REJECT_COMMENT_NULL", "驳回操作需要填写审批意见");
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
            List<WorkflowTaskEntity> pendingTasks = workflowTaskDao.selectPendingTasksByInstance(instanceId);

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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 更新流程实例状态参数错误: instanceId={}, error={}", instanceId, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 更新流程实例状态业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 更新流程实例状态系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 更新流程实例状态未知异常: instanceId={}", instanceId, e);
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
            List<WorkflowTaskEntity> pendingTasks = workflowTaskDao.selectPendingTasksByInstance(instanceId);

            for (WorkflowTaskEntity task : pendingTasks) {
                // 任务状态保持原样，因为流程实例已终止
                task.setUpdateTime(LocalDateTime.now());
                workflowTaskDao.updateById(task);
            }

            log.info("[审批服务] 任务状态已更新: instanceId={}, count={}", instanceId, pendingTasks.size());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 更新任务状态参数错误: instanceId={}, error={}", instanceId, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 更新任务状态业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 更新任务状态系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 更新任务状态未知异常: instanceId={}", instanceId, e);
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

                } catch (IllegalArgumentException | ParamException e) {
                    log.warn("[审批服务] 发送审批通知参数错误: taskId={}, action={}, error={}",
                            task.getId(), action, e.getMessage());
                } catch (BusinessException e) {
                    log.warn("[审批服务] 发送审批通知业务异常: taskId={}, action={}, code={}, message={}",
                            task.getId(), action, e.getCode(), e.getMessage());
                } catch (SystemException e) {
                    log.error("[审批服务] 发送审批通知系统异常: taskId={}, action={}, code={}, message={}",
                            task.getId(), action, e.getCode(), e.getMessage(), e);
                } catch (Exception e) {
                    log.error("[审批服务] 发送审批通知未知异常: taskId={}, action={}",
                            task.getId(), action, e);
                }
            });

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 发送审批通知参数错误: taskId={}, error={}", task.getId(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 发送审批通知业务异常: taskId={}, code={}, message={}", task.getId(), e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 发送审批通知系统异常: taskId={}, code={}, message={}", task.getId(), e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 发送审批通知未知异常: taskId={}", task.getId(), e);
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 发送转办通知参数错误: taskId={}, error={}", task.getId(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 发送转办通知业务异常: taskId={}, code={}, message={}", task.getId(), e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 发送转办通知系统异常: taskId={}, code={}, message={}", task.getId(), e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 发送转办通知未知异常: taskId={}", task.getId(), e);
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 发送委派通知参数错误: taskId={}, error={}", task.getId(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 发送委派通知业务异常: taskId={}, code={}, message={}", task.getId(), e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 发送委派通知系统异常: taskId={}, code={}, message={}", task.getId(), e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 发送委派通知未知异常: taskId={}", task.getId(), e);
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 发送撤回通知参数错误: instanceId={}, error={}", instance.getId(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 发送撤回通知业务异常: instanceId={}, code={}, message={}", instance.getId(), e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 发送撤回通知系统异常: instanceId={}, code={}, message={}", instance.getId(), e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 发送撤回通知未知异常: instanceId={}", instance.getId(), e);
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批服务] 记录审批操作日志参数错误: error={}", e.getMessage());
        } catch (BusinessException e) {
            log.warn("[审批服务] 记录审批操作日志业务异常: code={}, message={}", e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[审批服务] 记录审批操作日志系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批服务] 记录审批操作日志未知异常", e);
        }
    }
}




