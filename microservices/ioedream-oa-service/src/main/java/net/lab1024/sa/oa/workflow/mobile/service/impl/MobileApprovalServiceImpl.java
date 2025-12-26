package net.lab1024.sa.oa.workflow.mobile.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.oa.workflow.mobile.domain.*;
import net.lab1024.sa.oa.workflow.mobile.service.MobileApprovalService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 移动端审批服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class MobileApprovalServiceImpl implements MobileApprovalService {

    // 内存存储（实际应使用数据库）
    private final Map<String, MobileTaskVO> pendingTasksStorage = new ConcurrentHashMap<>();
    private final Map<String, MyApplicationVO> myApplicationsStorage = new ConcurrentHashMap<>();
    private final Map<String, CompletedTaskVO> completedTasksStorage = new ConcurrentHashMap<>();
    private final Map<String, List<CommentVO>> commentsStorage = new ConcurrentHashMap<>();
    private final Map<Long, ApprovalNotificationSettings> notificationSettingsStorage = new ConcurrentHashMap<>();

    // 语音识别关键词映射
    private final Map<String, List<String>> approvalKeywordsMap = new HashMap<>();
    private final Map<String, List<String>> rejectionKeywordsMap = new HashMap<>();

    public MobileApprovalServiceImpl() {
        log.info("[移动端审批] 初始化移动端审批服务");
        initializeKeywordMaps();
        initializeMockData();
    }

    // ==================== 待办管理 ====================

    @Override
    @Cacheable(value = "pendingTasks", key = "#pageNum + '_' + #pageSize + '_' + #sortBy")
    public PageResult<MobileTaskVO> getPendingTasks(Integer pageNum, Integer pageSize, String sortBy) {
        log.info("[移动端审批] 获取待办任务列表: pageNum={}, pageSize={}, sortBy={}",
                pageNum, pageSize, sortBy);

        // 从存储中获取所有待办任务
        List<MobileTaskVO> allTasks = new ArrayList<>(pendingTasksStorage.values());

        // 排序
        if ("createTime".equals(sortBy)) {
            allTasks.sort(Comparator.comparing(MobileTaskVO::getCreateTime).reversed());
        } else if ("dueDate".equals(sortBy)) {
            allTasks.sort(Comparator.comparing(MobileTaskVO::getDueDate));
        } else if ("priority".equals(sortBy)) {
            allTasks.sort(Comparator.comparing(MobileTaskVO::getPriority).reversed());
        }

        // 分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allTasks.size());

        List<MobileTaskVO> pageTasks = new ArrayList<>();
        if (start < allTasks.size()) {
            pageTasks = allTasks.subList(start, end);
        }

        log.info("[移动端审批] 获取待办任务成功: total={}, pageCount={}",
                allTasks.size(), pageTasks.size());

        return PageResult.of(pageTasks, (long) allTasks.size(), pageNum, pageSize);
    }

    @Override
    @Cacheable(value = "pendingStatistics")
    public PendingTaskStatistics getPendingCount() {
        log.info("[移动端审批] 获取待办统计");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayEnd = now.toLocalDate().atTime(23, 59, 59);
        LocalDateTime weekEnd = now.plusDays(7 - now.getDayOfWeek().getValue()).toLocalDate().atTime(23, 59, 59);

        int totalPending = pendingTasksStorage.size();
        int urgentCount = (int) pendingTasksStorage.values().stream()
                .filter(t -> t.getPriority() >= 3)
                .count();

        int overdueCount = (int) pendingTasksStorage.values().stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now))
                .count();

        int dueTodayCount = (int) pendingTasksStorage.values().stream()
                .filter(t -> t.getDueDate() != null &&
                        !t.getDueDate().isAfter(todayEnd))
                .count();

        int dueThisWeekCount = (int) pendingTasksStorage.values().stream()
                .filter(t -> t.getDueDate() != null &&
                        !t.getDueDate().isAfter(weekEnd))
                .count();

        // 今日已完成（简化，实际应从completedTasks统计）
        int completedTodayCount = 0;

        PendingTaskStatistics statistics = PendingTaskStatistics.builder()
                .totalPending(totalPending)
                .urgentCount(urgentCount)
                .overdueCount(overdueCount)
                .dueTodayCount(dueTodayCount)
                .dueThisWeekCount(dueThisWeekCount)
                .completedTodayCount(completedTodayCount)
                .build();

        log.info("[移动端审批] 获取待办统计成功: total={}, urgent={}, overdue={}",
                totalPending, urgentCount, overdueCount);

        return statistics;
    }

    @Override
    @Cacheable(value = "myApplications", key = "#pageNum + '_' + #pageSize + '_' + #status")
    public PageResult<MyApplicationVO> getMyApplications(Integer pageNum, Integer pageSize, String status) {
        log.info("[移动端审批] 获取我的申请: pageNum={}, pageSize={}, status={}",
                pageNum, pageSize, status);

        List<MyApplicationVO> allApplications = new ArrayList<>(myApplicationsStorage.values());

        // 状态筛选
        if (status != null && !status.isEmpty()) {
            allApplications = allApplications.stream()
                    .filter(app -> status.equals(app.getStatus()))
                    .collect(Collectors.toList());
        }

        // 按时间倒序
        allApplications.sort(Comparator.comparing(MyApplicationVO::getCreateTime).reversed());

        // 分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allApplications.size());

        List<MyApplicationVO> pageApplications = new ArrayList<>();
        if (start < allApplications.size()) {
            pageApplications = allApplications.subList(start, end);
        }

        log.info("[移动端审批] 获取我的申请成功: total={}, pageCount={}",
                allApplications.size(), pageApplications.size());

        return PageResult.of(pageApplications, (long) allApplications.size(), pageNum, pageSize);
    }

    @Override
    @Cacheable(value = "completedTasks", key = "#pageNum + '_' + #pageSize")
    public PageResult<CompletedTaskVO> getCompletedTasks(Integer pageNum, Integer pageSize) {
        log.info("[移动端审批] 获取已办任务: pageNum={}, pageSize={}", pageNum, pageSize);

        List<CompletedTaskVO> allTasks = new ArrayList<>(completedTasksStorage.values());
        allTasks.sort(Comparator.comparing(CompletedTaskVO::getApprovalTime).reversed());

        // 分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allTasks.size());

        List<CompletedTaskVO> pageTasks = new ArrayList<>();
        if (start < allTasks.size()) {
            pageTasks = allTasks.subList(start, end);
        }

        log.info("[移动端审批] 获取已办任务成功: total={}, pageCount={}",
                allTasks.size(), pageTasks.size());

        return PageResult.of(pageTasks, (long) allTasks.size(), pageNum, pageSize);
    }

    // ==================== 快速审批 ====================

    @Override
    @CacheEvict(value = {"pendingTasks", "pendingStatistics"}, allEntries = true)
    public QuickApprovalResult quickApprove(QuickApprovalRequest request) {
        log.info("[移动端审批] 快速审批: taskId={}, decision={}",
                request.getTaskId(), request.getDecision());

        try {
            // 查找任务
            MobileTaskVO task = pendingTasksStorage.get(request.getTaskId());
            if (task == null) {
                log.warn("[移动端审批] 任务不存在: taskId={}", request.getTaskId());
                return QuickApprovalResult.builder()
                        .success(false)
                        .taskId(request.getTaskId())
                        .message("任务不存在")
                        .build();
            }

            // 验证决策
            if (!"approved".equals(request.getDecision()) && !"rejected".equals(request.getDecision())) {
                return QuickApprovalResult.builder()
                        .success(false)
                        .taskId(request.getTaskId())
                        .message("无效的审批决策")
                        .build();
            }

            // TODO: 集成Flowable引擎
            // TaskService taskService = ...;
            // taskService.complete(request.getTaskId(),
            //     Collections.singletonMap("approved", "approved".equals(request.getDecision())));
            // taskService.addComment(request.getTaskId(), request.getComment());

            // 从待办移除
            pendingTasksStorage.remove(request.getTaskId());

            // 添加到已办
            CompletedTaskVO completedTask = CompletedTaskVO.builder()
                    .taskId(request.getTaskId())
                    .processName(task.getProcessName())
                    .taskName(task.getTaskName())
                    .approvalResult(request.getDecision())
                    .comment(request.getComment())
                    .approvalTime(LocalDateTime.now())
                    .durationMinutes(ChronoUnit.MINUTES.between(task.getCreateTime(), LocalDateTime.now()))
                    .build();

            completedTasksStorage.put(request.getTaskId(), completedTask);

            // 模拟下一个任务
            NextTaskInfo nextTask = null;
            if ("approved".equals(request.getDecision())) {
                // 实际应查询Flowable获取下一个任务
                // 这里简化处理
                nextTask = NextTaskInfo.builder()
                        .processEnded(true)
                        .build();
            }

            log.info("[移动端审批] 快速审批成功: taskId={}, decision={}",
                    request.getTaskId(), request.getDecision());

            return QuickApprovalResult.builder()
                    .success(true)
                    .taskId(request.getTaskId())
                    .decision(request.getDecision())
                    .message("审批成功")
                    .nextTask(nextTask)
                    .build();

        } catch (Exception e) {
            log.error("[移动端审批] 快速审批失败: taskId={}, error={}",
                    request.getTaskId(), e.getMessage(), e);
            return QuickApprovalResult.builder()
                    .success(false)
                    .taskId(request.getTaskId())
                    .message("审批失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @CacheEvict(value = {"pendingTasks", "pendingStatistics"}, allEntries = true)
    public BatchApprovalResult batchApprove(BatchApprovalRequest request) {
        log.info("[移动端审批] 批量审批: taskCount={}, decision={}",
                request.getTaskIds().size(), request.getDecision());

        int total = request.getTaskIds().size();
        int successCount = 0;
        int failureCount = 0;
        List<BatchApprovalFailure> failures = new ArrayList<>();
        List<String> successTaskIds = new ArrayList<>();
        List<String> failureTaskIds = new ArrayList<>();

        for (String taskId : request.getTaskIds()) {
            QuickApprovalRequest singleRequest = QuickApprovalRequest.builder()
                    .taskId(taskId)
                    .decision(request.getDecision())
                    .comment(request.getComment())
                    .build();

            QuickApprovalResult result = quickApprove(singleRequest);

            if (result.getSuccess()) {
                successCount++;
                successTaskIds.add(taskId);
            } else {
                failureCount++;
                failureTaskIds.add(taskId);
                if (!request.getSkipErrors()) {
                    failures.add(BatchApprovalFailure.builder()
                            .taskId(taskId)
                            .errorMessage(result.getMessage())
                            .build());
                }
            }
        }

        log.info("[移动端审批] 批量审批完成: total={}, success={}, failure={}",
                total, successCount, failureCount);

        return BatchApprovalResult.builder()
                .total(total)
                .successCount(successCount)
                .failureCount(failureCount)
                .failures(failures)
                .successTaskIds(successTaskIds)
                .failureTaskIds(failureTaskIds)
                .build();
    }

    @Override
    @CacheEvict(value = {"pendingTasks", "pendingStatistics"}, allEntries = true)
    public void withdrawApproval(String taskId, String reason) {
        log.info("[移动端审批] 撤销审批: taskId={}, reason={}", taskId, reason);

        MobileTaskVO task = pendingTasksStorage.get(taskId);
        if (task == null) {
            log.warn("[移动端审批] 任务不存在: taskId={}", taskId);
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // TODO: 集成Flowable引擎
        // TaskService taskService = ...;
        // taskService.setVariable(taskId, "withdrawReason", reason);
        // taskService.resolveTask(taskId);

        pendingTasksStorage.remove(taskId);

        log.info("[移动端审批] 撤销审批成功: taskId={}", taskId);
    }

    @Override
    @CacheEvict(value = {"pendingTasks", "pendingStatistics"}, allEntries = true)
    public void delegateTask(String taskId, Long assigneeId, String reason) {
        log.info("[移动端审批] 转办任务: taskId={}, assigneeId={}, reason={}",
                taskId, assigneeId, reason);

        MobileTaskVO task = pendingTasksStorage.get(taskId);
        if (task == null) {
            log.warn("[移动端审批] 任务不存在: taskId={}", taskId);
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // TODO: 集成Flowable引擎
        // TaskService taskService = ...;
        // taskService.delegateTask(taskId, assigneeId.toString());

        pendingTasksStorage.remove(taskId);

        log.info("[移动端审批] 转办任务成功: taskId={}, assigneeId={}",
                taskId, assigneeId);
    }

    // ==================== 语音审批 ====================

    @Override
    public ApprovalDecision parseApprovalDecision(String text) {
        log.info("[移动端审批] 解析语音审批决策: text={}", text);

        if (text == null || text.trim().isEmpty()) {
            return ApprovalDecision.builder()
                    .decisionType("unknown")
                    .confidence(0.0)
                    .build();
        }

        String normalizedText = text.toLowerCase().trim();

        // 检查同意关键词
        List<String> approvedKeywords = approvalKeywordsMap.get("zh-CN");
        List<String> matchedApproved = approvedKeywords.stream()
                .filter(normalizedText::contains)
                .collect(Collectors.toList());

        // 检查拒绝关键词
        List<String> rejectedKeywords = rejectionKeywordsMap.get("zh-CN");
        List<String> matchedRejected = rejectedKeywords.stream()
                .filter(normalizedText::contains)
                .collect(Collectors.toList());

        ApprovalDecision.ApprovalDecisionBuilder builder = ApprovalDecision.builder()
                .comment(text)
                .keywords(new ArrayList<>());

        if (!matchedApproved.isEmpty() && matchedRejected.isEmpty()) {
            // 只有同意关键词
            builder.decisionType("approved")
                    .confidence(0.9)
                    .keywords(matchedApproved);
        } else if (matchedApproved.isEmpty() && !matchedRejected.isEmpty()) {
            // 只有拒绝关键词
            builder.decisionType("rejected")
                    .confidence(0.9)
                    .keywords(matchedRejected);
        } else if (!matchedApproved.isEmpty() && !matchedRejected.isEmpty()) {
            // 同时有同意和拒绝关键词
            // 根据关键词数量判断
            if (matchedApproved.size() > matchedRejected.size()) {
                builder.decisionType("approved")
                        .confidence(0.5)
                        .keywords(matchedApproved);
            } else {
                builder.decisionType("rejected")
                        .confidence(0.5)
                        .keywords(matchedRejected);
            }
        } else {
            // 无法识别
            builder.decisionType("unknown")
                    .confidence(0.0);
        }

        log.info("[移动端审批] 解析结果: decision={}, confidence={}",
                builder.build().getDecisionType(), builder.build().getConfidence());

        return builder.build();
    }

    @Override
    @CacheEvict(value = {"pendingTasks", "pendingStatistics"}, allEntries = true)
    public QuickApprovalResult voiceApprove(VoiceApprovalRequest request) {
        log.info("[移动端审批] 语音审批: taskId={}", request.getTaskId());

        // 解析语音决策
        ApprovalDecision decision = parseApprovalDecision(request.getVoiceText());

        if ("unknown".equals(decision.getDecisionType())) {
            log.warn("[移动端审批] 无法识别语音决策: text={}", request.getVoiceText());
            return QuickApprovalResult.builder()
                    .success(false)
                    .taskId(request.getTaskId())
                    .message("无法识别语音指令，请重试")
                    .build();
        }

        // 执行审批
        QuickApprovalRequest approvalRequest = QuickApprovalRequest.builder()
                .taskId(request.getTaskId())
                .decision(decision.getDecisionType())
                .comment(decision.getComment())
                .build();

        return quickApprove(approvalRequest);
    }

    // ==================== 审批详情 ====================

    @Override
    @Cacheable(value = "approvalDetail", key = "#taskId")
    public MobileApprovalDetailVO getApprovalDetail(String taskId) {
        log.info("[移动端审批] 获取审批详情: taskId={}", taskId);

        MobileTaskVO task = pendingTasksStorage.get(taskId);
        if (task == null) {
            log.warn("[移动端审批] 任务不存在: taskId={}", taskId);
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // 构建发起人信息
        InitiatorInfo initiator = InitiatorInfo.builder()
                .userId(task.getInitiatorUserId())
                .userName(task.getInitiatorUserName())
                .avatar(task.getInitiatorAvatar())
                .department("技术部")
                .position("工程师")
                .build();

        // 构建当前节点信息
        CurrentNodeInfo currentNode = CurrentNodeInfo.builder()
                .nodeId(task.getTaskId())
                .nodeName(task.getTaskName())
                .createTime(task.getCreateTime())
                .dueDate(task.getDueDate())
                .overdue(task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now()))
                .build();

        // 获取审批历史
        List<ApprovalHistoryItemVO> history = getApprovalHistory(taskId);

        // 获取流程图
        MobileProcessDiagram diagram = getProcessDiagram(taskId);

        // 构建操作按钮
        List<ActionButton> actionButtons = new ArrayList<>();
        actionButtons.add(ActionButton.builder()
                .actionType("approve")
                .label("同意")
                .icon("check")
                .color("success")
                .visible(true)
                .disabled(false)
                .build());

        actionButtons.add(ActionButton.builder()
                .actionType("reject")
                .label("拒绝")
                .icon("close")
                .color("danger")
                .visible(true)
                .disabled(false)
                .build());

        actionButtons.add(ActionButton.builder()
                .actionType("delegate")
                .label("转办")
                .icon("share")
                .color("warning")
                .visible(task.getCanDelegate())
                .disabled(false)
                .build());

        actionButtons.add(ActionButton.builder()
                .actionType("withdraw")
                .label("撤回")
                .icon("rollback")
                .color("default")
                .visible(task.getCanWithdraw())
                .disabled(false)
                .build());

        log.info("[移动端审批] 获取审批详情成功: taskId={}", taskId);

        return MobileApprovalDetailVO.builder()
                .taskId(taskId)
                .processInstanceId(task.getProcessInstanceId())
                .processName(task.getProcessName())
                .taskName(task.getTaskName())
                .initiator(initiator)
                .currentNode(currentNode)
                .formData(task.getFormData())
                .formConfig(new HashMap<>())
                .processDiagram(diagram)
                .history(history)
                .canApprove(true)
                .canReject(true)
                .canDelegate(task.getCanDelegate())
                .canWithdraw(task.getCanWithdraw())
                .actionButtons(actionButtons)
                .build();
    }

    @Override
    @Cacheable(value = "approvalHistory", key = "#taskId")
    public List<ApprovalHistoryItemVO> getApprovalHistory(String taskId) {
        log.info("[移动端审批] 获取审批历史: taskId={}", taskId);

        // TODO: 从Flowable引擎获取审批历史
        // HistoryService historyService = ...;
        // List<HistoricActivityInstance> activities =
        //     historyService.createHistoricActivityInstanceQuery()
        //         .processInstanceId(processInstanceId)
        //         .orderByHistoricActivityInstanceStartTime()
        //         .asc()
        //         .list();

        // 临时返回模拟数据
        List<ApprovalHistoryItemVO> history = new ArrayList<>();

        MobileTaskVO task = pendingTasksStorage.get(taskId);
        if (task != null) {
            // 添加发起记录
            history.add(ApprovalHistoryItemVO.builder()
                    .historyId(UUID.randomUUID().toString())
                    .taskName(task.getTaskName())
                    .approverName(task.getInitiatorUserName())
                    .approverAvatar(task.getInitiatorAvatar())
                    .approvalResult("submitted")
                    .comment("提交申请")
                    .approvalTime(task.getCreateTime())
                    .nodeType("userTask")
                    .build());
        }

        log.info("[移动端审批] 获取审批历史成功: taskId={}, count={}",
                taskId, history.size());

        return history;
    }

    @Override
    @Cacheable(value = "processDiagram", key = "#taskId")
    public MobileProcessDiagram getProcessDiagram(String taskId) {
        log.info("[移动端审批] 获取流程图: taskId={}", taskId);

        // TODO: 从Flowable引擎获取流程图
        // RepositoryService repositoryService = ...;
        // ProcessDefinition processDefinition =
        //     repositoryService.createProcessDefinitionQuery()
        //         .processDefinitionId(processDefinitionId)
        //         .singleResult();
        // InputStream diagram = repositoryService.getDiagram(processDefinition.getId());

        // 临时返回简化数据
        return MobileProcessDiagram.builder()
                .svg("<svg>...</svg>")  // 实际应生成BPMN流程图SVG
                .currentPosition(NodePosition.builder()
                        .nodeId(taskId)
                        .x(100)
                        .y(100)
                        .width(100)
                        .height(80)
                        .build())
                .completedNodes(Arrays.asList("start"))
                .currentNodes(Arrays.asList(taskId))
                .pendingNodes(Arrays.asList("end"))
                .build();
    }

    @Override
    public Map<String, Object> getFormData(String taskId) {
        log.info("[移动端审批] 获取表单数据: taskId={}", taskId);

        MobileTaskVO task = pendingTasksStorage.get(taskId);
        if (task == null) {
            log.warn("[移动端审批] 任务不存在: taskId={}", taskId);
            throw new RuntimeException("任务不存在: " + taskId);
        }

        log.info("[移动端审批] 获取表单数据成功: taskId={}", taskId);
        return task.getFormData();
    }

    // ==================== 审批评论 ====================

    @Override
    @CacheEvict(value = "approvalDetail", key = "#taskId")
    public void addComment(String taskId, String comment, List<String> images) {
        log.info("[移动端审批] 添加评论: taskId={}, comment={}", taskId, comment);

        List<CommentVO> comments = commentsStorage.computeIfAbsent(taskId, k -> new ArrayList<>());

        CommentVO commentVO = CommentVO.builder()
                .commentId(UUID.randomUUID().toString())
                .taskId(taskId)
                .userId(1L)  // 实际应从Session获取
                .userName("当前用户")
                .avatar("/avatars/default.png")
                .content(comment)
                .attachments(images)
                .createTime(LocalDateTime.now())
                .commentType("comment")
                .build();

        comments.add(commentVO);

        log.info("[移动端审批] 添加评论成功: taskId={}, commentId={}",
                taskId, commentVO.getCommentId());
    }

    @Override
    @Cacheable(value = "comments", key = "#taskId")
    public List<CommentVO> getComments(String taskId) {
        log.info("[移动端审批] 获取评论列表: taskId={}", taskId);

        List<CommentVO> comments = commentsStorage.getOrDefault(taskId, new ArrayList<>());
        comments.sort(Comparator.comparing(CommentVO::getCreateTime).reversed());

        log.info("[移动端审批] 获取评论列表成功: taskId={}, count={}",
                taskId, comments.size());

        return comments;
    }

    // ==================== 通知设置 ====================

    @Override
    @CacheEvict(value = "notificationSettings", allEntries = true)
    public void setApprovalNotification(ApprovalNotificationRequest request) {
        log.info("[移动端审批] 设置审批通知: pushEnabled={}, smsEnabled={}",
                request.getPushEnabled(), request.getSmsEnabled());

        Long userId = 1L;  // 实际应从Session获取

        ApprovalNotificationSettings settings = ApprovalNotificationSettings.builder()
                .pushEnabled(request.getPushEnabled())
                .smsEnabled(request.getSmsEnabled())
                .emailEnabled(request.getEmailEnabled())
                .pushTimeRange(request.getPushTimeRange())
                .doNotDisturb(request.getDoNotDisturb())
                .doNotDisturbTimeRange(request.getDoNotDisturbTimeRange())
                .notificationTypes(Arrays.asList("new_task", "urgent", "overdue"))
                .build();

        notificationSettingsStorage.put(userId, settings);

        log.info("[移动端审批] 设置审批通知成功: userId={}", userId);
    }

    @Override
    @Cacheable(value = "notificationSettings", key = "#root")
    public ApprovalNotificationSettings getNotificationSettings() {
        log.info("[移动端审批] 获取通知设置");

        Long userId = 1L;  // 实际应从Session获取

        return notificationSettingsStorage.computeIfAbsent(userId, k ->
                ApprovalNotificationSettings.builder()
                        .pushEnabled(true)
                        .smsEnabled(false)
                        .emailEnabled(false)
                        .pushTimeRange("09:00-18:00")
                        .doNotDisturb(false)
                        .doNotDisturbTimeRange("22:00-08:00")
                        .notificationTypes(Arrays.asList("new_task", "urgent", "overdue"))
                        .build());
    }

    // ==================== 任务催办 ====================

    @Override
    public void urgeTask(String taskId, String message) {
        log.info("[移动端审批] 催办任务: taskId={}, message={}", taskId, message);

        MobileTaskVO task = pendingTasksStorage.get(taskId);
        if (task == null) {
            log.warn("[移动端审批] 任务不存在: taskId={}", taskId);
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // TODO: 发送催办通知
        // 1. 推送通知给审批人
        // 2. 发送短信（如果启用）
        // 3. 发送邮件（如果启用）

        log.info("[移动端审批] 催办任务成功: taskId={}", taskId);
    }

    @Override
    public void batchUrge(List<String> taskIds, String message) {
        log.info("[移动端审批] 批量催办: taskCount={}, message={}", taskIds.size(), message);

        for (String taskId : taskIds) {
            try {
                urgeTask(taskId, message);
            } catch (Exception e) {
                log.error("[移动端审批] 催办失败: taskId={}, error={}",
                        taskId, e.getMessage());
            }
        }

        log.info("[移动端审批] 批量催办完成: taskCount={}", taskIds.size());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 初始化关键词映射
     */
    private void initializeKeywordMaps() {
        // 同意关键词（中文）
        List<String> approvedKeywords = Arrays.asList(
                "同意", "批准", "通过", "可以", "行", "好", "ok", "yes", "确认"
        );

        // 拒绝关键词（中文）
        List<String> rejectedKeywords = Arrays.asList(
                "不同意", "拒绝", "不批准", "不通过", "不可以", "不行", "no", "驳回", "否"
        );

        approvalKeywordsMap.put("zh-CN", approvedKeywords);
        rejectionKeywordsMap.put("zh-CN", rejectedKeywords);

        log.info("[移动端审批] 关键词映射初始化完成");
    }

    /**
     * 初始化模拟数据
     */
    private void initializeMockData() {
        // 模拟待办任务
        for (int i = 1; i <= 10; i++) {
            String taskId = "task_" + i;
            LocalDateTime createTime = LocalDateTime.now().minusHours(i);
            LocalDateTime dueDate = createTime.plusDays(3);

            pendingTasksStorage.put(taskId, MobileTaskVO.builder()
                    .taskId(taskId)
                    .processInstanceId("process_" + i)
                    .processDefinitionId("leave_request:1:12345")
                    .processName("请假审批流程")
                    .taskName("部门审批")
                    .initiatorUserId((long) (10 + i))
                    .initiatorUserName("张三" + i)
                    .initiatorAvatar("/avatars/user" + i + ".png")
                    .createTime(createTime)
                    .dueDate(dueDate)
                    .priority(i % 3 + 1)
                    .status("pending")
                    .formKey("leave_request_form")
                    .formData(createMockFormData())
                    .currentNodeName("部门审批")
                    .processImage("/process-images/leave_request.png")
                    .canDelegate(true)
                    .canWithdraw(false)
                    .build());
        }

        // 模拟我的申请
        for (int i = 1; i <= 5; i++) {
            String processInstanceId = "my_process_" + i;
            myApplicationsStorage.put(processInstanceId, MyApplicationVO.builder()
                    .processInstanceId(processInstanceId)
                    .processName("请假审批流程")
                    .businessKey("LEAVE_" + System.currentTimeMillis())
                    .status(i % 2 == 0 ? "running" : "completed")
                    .currentNode("部门审批")
                    .createTime(LocalDateTime.now().minusDays(i))
                    .endTime(i % 2 == 0 ? null : LocalDateTime.now().minusDays(i).plusHours(2))
                    .processImage("/process-images/leave_request.png")
                    .build());
        }

        // 默认通知设置
        notificationSettingsStorage.put(1L, ApprovalNotificationSettings.builder()
                .pushEnabled(true)
                .smsEnabled(false)
                .emailEnabled(false)
                .pushTimeRange("09:00-18:00")
                .doNotDisturb(false)
                .doNotDisturbTimeRange("22:00-08:00")
                .notificationTypes(Arrays.asList("new_task", "urgent", "overdue"))
                .build());

        log.info("[移动端审批] 模拟数据初始化完成");
    }

    /**
     * 创建模拟表单数据
     */
    private Map<String, Object> createMockFormData() {
        Map<String, Object> formData = new HashMap<>();
        formData.put("applicantName", "张三");
        formData.put("department", "技术部");
        formData.put("leaveType", "年假");
        formData.put("startDate", "2025-12-27");
        formData.put("endDate", "2025-12-30");
        formData.put("leaveDays", 3);
        formData.put("reason", "回家探亲");
        return formData;
    }
}
