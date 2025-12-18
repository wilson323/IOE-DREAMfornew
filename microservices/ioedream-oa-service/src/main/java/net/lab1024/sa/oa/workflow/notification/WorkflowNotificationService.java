package net.lab1024.sa.oa.workflow.notification;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.lab1024.sa.oa.workflow.websocket.WorkflowWebSocketController;

/**
 * 工作流实时通知服务
 * <p>
 * 基于WebSocket基础设施，提供企业级实时通知功能
 * 支持多种通知类型和用户订阅管理
 * 集成指标收集，支持性能监控和告警
 * </p>
 * <p>
 * 支持的通知类型：
 * 1. 任务通知 - 新任务、任务状态变更、任务分配
 * 2. 流程通知 - 流程启动、完成、暂停、恢复
 * 3. 审批通知 - 审批请求、审批结果、超时提醒
 * 4. 系统通知 - 错误告警、维护通知、升级通知
 * 5. 统计通知 - 任务完成统计、流程效率统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
public class WorkflowNotificationService {

    @Resource
    private WorkflowWebSocketController webSocketController;

    @Resource
    private MeterRegistry meterRegistry;

    // 通知类型计数器
    private Counter taskNotificationCounter;
    private Counter processNotificationCounter;
    private Counter approvalNotificationCounter;
    private Counter systemNotificationCounter;
    private Counter broadcastNotificationCounter;

    // 通知处理计时器
    private Timer notificationProcessingTimer;

    // 用户订阅管理
    private final Map<Long, UserSubscription> userSubscriptions = new ConcurrentHashMap<>();
    private final AtomicLong totalSubscriptions = new AtomicLong(0);
    private final AtomicLong totalNotifications = new AtomicLong(0);

    // 通知类型常量
    public static final String NOTIFICATION_TYPE_NEW_TASK = "NEW_TASK";
    public static final String NOTIFICATION_TYPE_TASK_STATUS_CHANGED = "TASK_STATUS_CHANGED";
    public static final String NOTIFICATION_TYPE_TASK_ASSIGNED = "TASK_ASSIGNED";
    public static final String NOTIFICATION_TYPE_TASK_COMPLETED = "TASK_COMPLETED";
    public static final String NOTIFICATION_TYPE_TASK_TIMEOUT = "TASK_TIMEOUT";

    public static final String NOTIFICATION_TYPE_PROCESS_STARTED = "PROCESS_STARTED";
    public static final String NOTIFICATION_TYPE_PROCESS_COMPLETED = "PROCESS_COMPLETED";
    public static final String NOTIFICATION_TYPE_PROCESS_TERMINATED = "PROCESS_TERMINATED";
    public static final String NOTIFICATION_TYPE_PROCESS_SUSPENDED = "PROCESS_SUSPENDED";
    public static final String NOTIFICATION_TYPE_PROCESS_RESUMED = "PROCESS_RESUMED";

    public static final String NOTIFICATION_TYPE_APPROVAL_REQUESTED = "APPROVAL_REQUESTED";
    public static final String NOTIFICATION_TYPE_APPROVAL_APPROVED = "APPROVAL_APPROVED";
    public static final String NOTIFICATION_TYPE_APPROVAL_REJECTED = "APPROVAL_REJECTED";
    public static final String NOTIFICATION_TYPE_APPROVAL_TIMEOUT = "APPROVAL_TIMEOUT";

    public static final String NOTIFICATION_TYPE_SYSTEM_ERROR = "SYSTEM_ERROR";
    public static final String NOTIFICATION_TYPE_SYSTEM_MAINTENANCE = "SYSTEM_MAINTENANCE";
    public static final String NOTIFICATION_TYPE_SYSTEM_UPGRADE = "SYSTEM_UPGRADE";

    /**
     * 初始化指标收集器
     */
    @jakarta.annotation.PostConstruct
    public void initMetrics() {
        taskNotificationCounter = Counter.builder("workflow.notification.task.count")
                .description("任务通知总数")
                .register(meterRegistry);

        processNotificationCounter = Counter.builder("workflow.notification.process.count")
                .description("流程通知总数")
                .register(meterRegistry);

        approvalNotificationCounter = Counter.builder("workflow.notification.approval.count")
                .description("审批通知总数")
                .register(meterRegistry);

        systemNotificationCounter = Counter.builder("workflow.notification.system.count")
                .description("系统通知总数")
                .register(meterRegistry);

        broadcastNotificationCounter = Counter.builder("workflow.notification.broadcast.count")
                .description("广播通知总数")
                .register(meterRegistry);

        notificationProcessingTimer = Timer.builder("workflow.notification.processing.duration")
                .description("通知处理耗时")
                .register(meterRegistry);

        log.info("[工作流通知] 工作流实时通知服务初始化完成");
    }

    // ==================== 任务通知 ====================

    /**
     * 发送新任务通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendNewTaskNotification(Long userId, Map<String, Object> taskData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅任务通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_NEW_TASK, taskData);
                webSocketController.sendNewTaskNotification(userId, message);

                taskNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 新任务通知已发送 - 用户ID: {}, 任务ID: {}",
                    userId, taskData.get("taskId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送新任务通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送任务状态变更通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendTaskStatusChangedNotification(Long userId, Map<String, Object> taskData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅任务通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_TASK_STATUS_CHANGED, taskData);
                webSocketController.sendTaskStatusChangedNotification(userId, message);

                taskNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 任务状态变更通知已发送 - 用户ID: {}, 任务ID: {}, 状态: {}",
                    userId, taskData.get("taskId"), taskData.get("status"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送任务状态变更通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送任务分配通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendTaskAssignedNotification(Long userId, Map<String, Object> taskData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅任务通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_TASK_ASSIGNED, taskData);
                webSocketController.sendNewTaskNotification(userId, message);

                taskNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 任务分配通知已发送 - 用户ID: {}, 任务ID: {}",
                    userId, taskData.get("taskId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送任务分配通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送任务完成通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendTaskCompletedNotification(Long userId, Map<String, Object> taskData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅任务通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_TASK_COMPLETED, taskData);
                webSocketController.sendTaskStatusChangedNotification(userId, message);

                taskNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 任务完成通知已发送 - 用户ID: {}, 任务ID: {}",
                    userId, taskData.get("taskId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送任务完成通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送任务超时提醒
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendTaskTimeoutNotification(Long userId, Map<String, Object> taskData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅任务通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_TASK_TIMEOUT, taskData);
                webSocketController.sendNewTaskNotification(userId, message);

                taskNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 任务超时提醒已发送 - 用户ID: {}, 任务ID: {}",
                    userId, taskData.get("taskId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送任务超时提醒失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    // ==================== 流程通知 ====================

    /**
     * 发送流程启动通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendProcessStartedNotification(Long userId, Map<String, Object> processData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅流程通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_PROCESS_STARTED, processData);
                webSocketController.sendInstanceStatusChangedNotification(userId, message);

                processNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 流程启动通知已发送 - 用户ID: {}, 流程实例ID: {}",
                    userId, processData.get("instanceId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送流程启动通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送流程完成通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendProcessCompletedNotification(Long userId, Map<String, Object> processData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅流程通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_PROCESS_COMPLETED, processData);
                webSocketController.sendInstanceStatusChangedNotification(userId, message);

                processNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 流程完成通知已发送 - 用户ID: {}, 流程实例ID: {}",
                    userId, processData.get("instanceId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送流程完成通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送流程终止通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendProcessTerminatedNotification(Long userId, Map<String, Object> processData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅流程通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_PROCESS_TERMINATED, processData);
                webSocketController.sendInstanceStatusChangedNotification(userId, message);

                processNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 流程终止通知已发送 - 用户ID: {}, 流程实例ID: {}",
                    userId, processData.get("instanceId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送流程终止通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    // ==================== 审批通知 ====================

    /**
     * 发送审批请求通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendApprovalRequestedNotification(Long userId, Map<String, Object> approvalData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅审批通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_APPROVAL_REQUESTED, approvalData);
                webSocketController.sendNewTaskNotification(userId, message);

                approvalNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 审批请求通知已发送 - 用户ID: {}, 审批ID: {}",
                    userId, approvalData.get("approvalId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送审批请求通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送审批通过通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendApprovalApprovedNotification(Long userId, Map<String, Object> approvalData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅审批通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_APPROVAL_APPROVED, approvalData);
                webSocketController.sendInstanceStatusChangedNotification(userId, message);

                approvalNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 审批通过通知已发送 - 用户ID: {}, 审批ID: {}",
                    userId, approvalData.get("approvalId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送审批通过通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送审批驳回通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendApprovalRejectedNotification(Long userId, Map<String, Object> approvalData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅审批通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_APPROVAL_REJECTED, approvalData);
                webSocketController.sendInstanceStatusChangedNotification(userId, message);

                approvalNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 审批驳回通知已发送 - 用户ID: {}, 审批ID: {}",
                    userId, approvalData.get("approvalId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送审批驳回通知失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送审批超时提醒
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendApprovalTimeoutNotification(Long userId, Map<String, Object> approvalData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (!isUserSubscribed(userId)) {
                    log.debug("[工作流通知] 用户{}未订阅审批通知，跳过发送", userId);
                    return;
                }

                Map<String, Object> message = createNotificationMessage(NOTIFICATION_TYPE_APPROVAL_TIMEOUT, approvalData);
                webSocketController.sendNewTaskNotification(userId, message);

                approvalNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 审批超时提醒已发送 - 用户ID: {}, 审批ID: {}",
                    userId, approvalData.get("approvalId"));
            } catch (Exception e) {
                log.error("[工作流通知] 发送审批超时提醒失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    // ==================== 系统通知 ====================

    /**
     * 发送系统错误通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendSystemErrorNotification(String errorType, String errorMessage, Map<String, Object> errorData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                Map<String, Object> message = Map.of(
                    "type", NOTIFICATION_TYPE_SYSTEM_ERROR,
                    "errorType", errorType,
                    "errorMessage", errorMessage,
                    "data", errorData,
                    "timestamp", System.currentTimeMillis()
                );

                webSocketController.broadcastMessage(message);
                systemNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.warn("[工作流通知] 系统错误通知已广播 - 错误类型: {}, 错误信息: {}", errorType, errorMessage);
            } catch (Exception e) {
                log.error("[工作流通知] 发送系统错误通知失败 - 错误类型: {}, 错误: {}", errorType, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    /**
     * 发送系统维护通知
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendSystemMaintenanceNotification(String maintenanceType, String startTime, String endTime, Map<String, Object> maintenanceData) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                Map<String, Object> message = Map.of(
                    "type", NOTIFICATION_TYPE_SYSTEM_MAINTENANCE,
                    "maintenanceType", maintenanceType,
                    "startTime", startTime,
                    "endTime", endTime,
                    "data", maintenanceData,
                    "timestamp", System.currentTimeMillis()
                );

                webSocketController.broadcastMessage(message);
                systemNotificationCounter.increment();
                totalNotifications.incrementAndGet();

                log.info("[工作流通知] 系统维护通知已广播 - 维护类型: {}, 开始时间: {}", maintenanceType, startTime);
            } catch (Exception e) {
                log.error("[工作流通知] 发送系统维护通知失败 - 维护类型: {}, 错误: {}", maintenanceType, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    // ==================== 批量通知 ====================

    /**
     * 批量发送通知给多个用户
     */
    @Async("workflowNotificationExecutor")
    public CompletableFuture<Void> sendBatchNotifications(List<Long> userIds, String notificationType, Map<String, Object> data) {
        return CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                if (userIds == null || userIds.isEmpty()) {
                    log.debug("[工作流通知] 用户列表为空，跳过批量发送");
                    return;
                }

                // 过滤已订阅的用户
                List<Long> subscribedUsers = userIds.stream()
                        .filter(this::isUserSubscribed)
                        .toList();

                if (subscribedUsers.isEmpty()) {
                    log.debug("[工作流通知] 没有用户订阅通知，跳过批量发送");
                    return;
                }

                Map<String, Object> message = createNotificationMessage(notificationType, data);
                int successCount = 0;

                for (Long userId : subscribedUsers) {
                    try {
                        // 根据通知类型选择发送方法
                        switch (notificationType) {
                            case NOTIFICATION_TYPE_NEW_TASK:
                            case NOTIFICATION_TYPE_TASK_ASSIGNED:
                            case NOTIFICATION_TYPE_TASK_TIMEOUT:
                                webSocketController.sendNewTaskNotification(userId, message);
                                break;
                            case NOTIFICATION_TYPE_TASK_STATUS_CHANGED:
                            case NOTIFICATION_TYPE_TASK_COMPLETED:
                                webSocketController.sendTaskStatusChangedNotification(userId, message);
                                break;
                            default:
                                // 默认发送到任务队列
                                webSocketController.sendNewTaskNotification(userId, message);
                        }
                        successCount++;
                    } catch (Exception e) {
                        log.warn("[工作流通知] 批量发送通知失败 - 用户ID: {}, 通知类型: {}, 错误: {}",
                            userId, notificationType, e.getMessage());
                    }
                }

                log.info("[工作流通知] 批量通知发送完成 - 通知类型: {}, 目标用户数: {}, 成功发送数: {}",
                    notificationType, userIds.size(), successCount);

            } catch (Exception e) {
                log.error("[工作流通知] 批量发送通知失败 - 通知类型: {}, 错误: {}", notificationType, e.getMessage(), e);
            } finally {
                sample.stop(notificationProcessingTimer);
            }
        });
    }

    // ==================== 用户订阅管理 ====================

    /**
     * 用户订阅通知
     */
    public void subscribeUser(Long userId, List<String> subscriptionTypes) {
        UserSubscription subscription = userSubscriptions.computeIfAbsent(userId,
            id -> new UserSubscription(id));

        subscription.setSubscribed(true);
        subscription.setSubscriptionTypes(subscriptionTypes);
        subscription.setSubscribeTime(System.currentTimeMillis());

        totalSubscriptions.incrementAndGet();

        log.info("[工作流通知] 用户订阅通知 - 用户ID: {}, 订阅类型: {}", userId, subscriptionTypes);
    }

    /**
     * 用户取消订阅
     */
    public void unsubscribeUser(Long userId) {
        UserSubscription subscription = userSubscriptions.remove(userId);
        if (subscription != null) {
            subscription.setSubscribed(false);
            totalSubscriptions.decrementAndGet();

            log.info("[工作流通知] 用户取消订阅 - 用户ID: {}", userId);
        }
    }

    /**
     * 检查用户是否已订阅
     */
    public boolean isUserSubscribed(Long userId) {
        UserSubscription subscription = userSubscriptions.get(userId);
        return subscription != null && subscription.isSubscribed();
    }

    /**
     * 获取用户订阅信息
     */
    public UserSubscription getUserSubscription(Long userId) {
        return userSubscriptions.get(userId);
    }

    // ==================== 统计信息 ====================

    /**
     * 获取通知统计信息
     */
    public Map<String, Object> getNotificationStatistics() {
        return Map.of(
            "totalSubscriptions", totalSubscriptions.get(),
            "totalNotifications", totalNotifications.get(),
            "taskNotifications", taskNotificationCounter.count(),
            "processNotifications", processNotificationCounter.count(),
            "approvalNotifications", approvalNotificationCounter.count(),
            "systemNotifications", systemNotificationCounter.count(),
            "broadcastNotifications", broadcastNotificationCounter.count(),
            "averageProcessingTime", notificationProcessingTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS)
        );
    }

    // ==================== 工具方法 ====================

    /**
     * 创建通知消息
     */
    private Map<String, Object> createNotificationMessage(String notificationType, Map<String, Object> data) {
        Map<String, Object> message = Map.of(
            "type", notificationType,
            "data", data,
            "timestamp", System.currentTimeMillis(),
            "notificationId", java.util.UUID.randomUUID().toString()
        );

        // 添加通知级别
        message.put("priority", determineNotificationPriority(notificationType));

        return message;
    }

    /**
     * 确定通知优先级
     */
    private String determineNotificationPriority(String notificationType) {
        switch (notificationType) {
            case NOTIFICATION_TYPE_SYSTEM_ERROR:
            case NOTIFICATION_TYPE_TASK_TIMEOUT:
            case NOTIFICATION_TYPE_APPROVAL_TIMEOUT:
                return "HIGH";
            case NOTIFICATION_TYPE_NEW_TASK:
            case NOTIFICATION_TYPE_APPROVAL_REQUESTED:
            case NOTIFICATION_TYPE_PROCESS_STARTED:
                return "MEDIUM";
            default:
                return "NORMAL";
        }
    }

    // ==================== 内部类 ====================

    /**
     * 用户订阅信息
     */
    public static class UserSubscription {
        private final Long userId;
        private boolean subscribed;
        private List<String> subscriptionTypes;
        private long subscribeTime;
        private long lastActivityTime;

        public UserSubscription(Long userId) {
            this.userId = userId;
            this.subscribed = false;
            this.subscriptionTypes = new java.util.ArrayList<>();
            this.subscribeTime = 0;
            this.lastActivityTime = System.currentTimeMillis();
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public boolean isSubscribed() { return subscribed; }
        public void setSubscribed(boolean subscribed) {
            this.subscribed = subscribed;
            this.lastActivityTime = System.currentTimeMillis();
        }
        public List<String> getSubscriptionTypes() { return subscriptionTypes; }
        public void setSubscriptionTypes(List<String> subscriptionTypes) {
            this.subscriptionTypes = subscriptionTypes != null ? subscriptionTypes : new java.util.ArrayList<>();
        }
        public long getSubscribeTime() { return subscribeTime; }
        public void setSubscribeTime(long subscribeTime) { this.subscribeTime = subscribeTime; }
        public long getLastActivityTime() { return lastActivityTime; }
    }
}
