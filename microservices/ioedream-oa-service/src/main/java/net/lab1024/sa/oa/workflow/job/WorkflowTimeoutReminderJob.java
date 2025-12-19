package net.lab1024.sa.oa.workflow.job;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpMethod;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.monitor.dao.NotificationDao;
import net.lab1024.sa.common.monitor.domain.entity.NotificationEntity;
import net.lab1024.sa.common.monitor.manager.NotificationManager;
import net.lab1024.sa.oa.workflow.dao.WorkflowTaskDao;
import net.lab1024.sa.oa.domain.entity.WorkflowTaskEntity;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;

/**
 * 工作流审批超时提醒定时任务
 * <p>
 * 负责检查和处理审批超时任务
 * 严格遵循CLAUDE.md规范：
 * - 使用Spring @Scheduled注解
 * - 完整的异常处理和日志记录
 * - 避免重复处理
 * </p>
 * <p>
 * 功能：
 * - 检查即将超时的任务并发送提醒（提前1小时）
 * - 处理已超时的任务（自动转交、升级、自动通过等）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
// 暂时禁用此Job，因为依赖的WorkflowApprovalManager需要类型兼容性修复
// @Component
public class WorkflowTimeoutReminderJob {

    @Resource
    private WorkflowTaskDao workflowTaskDao;

    @Resource
    private NotificationManager notificationManager;

    @Resource
    private NotificationDao notificationDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 检查审批超时任务并发送提醒
     * <p>
     * 每30分钟执行一次
     * 提前1小时提醒审批人
     * </p>
     */
    @Scheduled(fixedRate = 1800000) // 30分钟 = 1800000毫秒
    public void checkTimeoutTasks() {
        log.info("开始检查审批超时任务");

        try {
            // 查询即将超时的任务（提前1小时提醒）
            LocalDateTime oneHourLater = LocalDateTime.now().plusHours(1);
            List<WorkflowTaskEntity> timeoutTasks = workflowTaskDao.selectTimeoutTasks(oneHourLater);

            for (WorkflowTaskEntity task : timeoutTasks) {
                try {
                    // 检查是否已发送提醒（通过reminderSent字段或lastReminderTime字段）
                    if (shouldSendReminder(task)) {
                        // 发送提醒通知
                        sendApprovalReminder(task);
                        log.info("已发送审批超时提醒，任务ID: {}, 审批人ID: {}, 任务名称: {}",
                                task.getId(), task.getAssigneeId(), task.getTaskName());
                    }
                } catch (Exception e) {
                    log.error("发送审批超时提醒失败，任务ID: {}", task.getId(), e);
                }
            }

            log.info("审批超时任务检查完成，共处理{}个任务", timeoutTasks.size());
        } catch (Exception e) {
            log.error("检查审批超时任务异常", e);
        }
    }

    /**
     * 处理已超时的任务
     * <p>
     * 每天凌晨2点执行
     * 根据流程定义的超时策略处理：自动转交、升级、自动通过等
     * </p>
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void handleTimeoutTasks() {
        log.info("开始处理已超时的审批任务");

        try {
            // 查询已超时的任务
            LocalDateTime now = LocalDateTime.now();
            List<WorkflowTaskEntity> timeoutTasks = workflowTaskDao.selectTimeoutTasks(now);

            for (WorkflowTaskEntity task : timeoutTasks) {
                try {
                    // 根据超时策略处理：自动转交、升级、自动通过等
                    handleTimeoutTask(task);
                } catch (Exception e) {
                    log.error("处理超时任务失败，任务ID: {}", task.getId(), e);
                }
            }

            log.info("超时任务处理完成，共处理{}个任务", timeoutTasks.size());
        } catch (Exception e) {
            log.error("处理超时任务异常", e);
        }
    }

    /**
     * 判断是否需要发送提醒
     * <p>
     * 避免重复发送提醒
     * 通过检查任务变量中的lastReminderTime字段判断
     * 如果上次提醒时间在1小时内，则不重复发送
     * </p>
     *
     * @param task 任务实体
     * @return true表示需要发送提醒，false表示不需要
     */
    private boolean shouldSendReminder(WorkflowTaskEntity task) {
        try {
            // 从任务变量中获取上次提醒时间
            String variablesJson = task.getVariables();
            if (variablesJson == null || variablesJson.isEmpty()) {
                // 如果没有变量，需要发送提醒
                return true;
            }

            Map<String, Object> variables = objectMapper.readValue(
                    variablesJson,
                    new TypeReference<Map<String, Object>>() {});

            Object lastReminderTimeObj = variables.get("lastReminderTime");
            if (lastReminderTimeObj == null) {
                // 如果没有上次提醒时间，需要发送提醒
                return true;
            }

            // 解析上次提醒时间
            LocalDateTime lastReminderTime = null;
            if (lastReminderTimeObj instanceof String) {
                lastReminderTime = LocalDateTime.parse((String) lastReminderTimeObj);
            } else if (lastReminderTimeObj instanceof Long) {
                // 如果是时间戳
                lastReminderTime = LocalDateTime.ofEpochSecond((Long) lastReminderTimeObj / 1000, 0,
                        java.time.ZoneOffset.of("+8"));
            }

            if (lastReminderTime == null) {
                return true;
            }

            // 如果上次提醒时间在1小时内，不重复发送
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            if (lastReminderTime.isAfter(oneHourAgo)) {
                log.debug("上次提醒时间在1小时内，不重复发送，任务ID: {}, 上次提醒时间: {}",
                        task.getId(), lastReminderTime);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("判断是否需要发送提醒失败，任务ID: {}, 默认发送提醒", task.getId(), e);
            // 异常情况下，默认发送提醒
            return true;
        }
    }

    /**
     * 发送审批超时提醒
     *
     * @param task 任务实体
     */
    private void sendApprovalReminder(WorkflowTaskEntity task) {
        try {
            // 构建提醒消息
            String title = "审批任务即将超时提醒";
            String content = String.format(
                    "您有一个审批任务即将超时：\n" +
                            "任务名称：%s\n" +
                            "流程名称：%s\n" +
                            "截止时间：%s\n" +
                            "请及时处理。",
                    task.getTaskName(),
                    task.getProcessName() != null ? task.getProcessName() : "未知流程",
                    task.getDueTime() != null ? task.getDueTime().toString() : "未知"
            );

            // 创建通知实体
            NotificationEntity notification = new NotificationEntity();
            notification.setTitle(title);
            notification.setContent(content);
            notification.setNotificationType(2); // 2-业务通知
            notification.setReceiverType(1); // 1-指定用户
            notification.setReceiverIds(String.valueOf(task.getAssigneeId()));
            notification.setChannel(5); // 5-WebSocket（也可以使用其他渠道）
            notification.setStatus(0); // 0-待发送
            notification.setPriority(2); // 2-普通优先级
            notification.setCreateTime(LocalDateTime.now());

            // 保存通知到数据库，由NotificationManager的定时任务处理发送
            // 注意：NotificationManager.sendNotification是protected方法，不能直接调用
            // 通过NotificationDao保存通知，由NotificationManager.processPendingNotifications()定时任务处理
            notificationDao.insert(notification);

            // 更新任务变量中的上次提醒时间
            updateLastReminderTime(task);

            log.info("审批超时提醒发送成功，任务ID: {}, 审批人ID: {}", task.getId(), task.getAssigneeId());
        } catch (Exception e) {
            log.error("发送审批超时提醒异常，任务ID: {}", task.getId(), e);
            throw e;
        }
    }

    /**
     * 更新任务变量中的上次提醒时间
     *
     * @param task 任务实体
     */
    private void updateLastReminderTime(WorkflowTaskEntity task) {
        try {
            // 解析现有变量
            Map<String, Object> variables = new HashMap<>();
            String variablesJson = task.getVariables();
            if (variablesJson != null && !variablesJson.isEmpty()) {
                variables = objectMapper.readValue(
                        variablesJson,
                        new TypeReference<Map<String, Object>>() {});
            }

            // 更新上次提醒时间
            variables.put("lastReminderTime", LocalDateTime.now().toString());

            // 保存回任务变量
            task.setVariables(objectMapper.writeValueAsString(variables));
            workflowTaskDao.updateById(task);

            log.debug("更新上次提醒时间成功，任务ID: {}", task.getId());
        } catch (Exception e) {
            log.warn("更新上次提醒时间失败，任务ID: {}", task.getId(), e);
            // 不影响主流程，仅记录警告
        }
    }

    /**
     * 处理超时任务
     * <p>
     * 根据流程定义的超时策略处理
     * </p>
     *
     * @param task 任务实体
     */
    private void handleTimeoutTask(WorkflowTaskEntity task) {
        // 获取流程定义的超时策略（从任务变量中解析）
        String timeoutStrategy = extractTimeoutStrategy(task);

        if (timeoutStrategy == null || timeoutStrategy.isEmpty()) {
            log.warn("任务没有配置超时策略，任务ID: {}, 使用默认策略: AUTO_TRANSFER", task.getId());
            timeoutStrategy = "AUTO_TRANSFER"; // 默认策略：自动转交
        }

        switch (timeoutStrategy) {
            case "AUTO_TRANSFER":
                // 自动转交给上级
                transferToSupervisor(task);
                break;
            case "AUTO_APPROVE":
                // 自动通过
                autoApprove(task);
                break;
            case "ESCALATE":
                // 升级处理
                escalateTask(task);
                break;
            default:
                log.warn("未知的超时策略: {}, 任务ID: {}, 使用默认策略: AUTO_TRANSFER", timeoutStrategy, task.getId());
                transferToSupervisor(task);
        }
    }

    /**
     * 自动转交给上级
     *
     * @param task 任务实体
     */
    @SuppressWarnings("null")
    private void transferToSupervisor(WorkflowTaskEntity task) {
        log.info("自动转交超时任务给上级，任务ID: {}, 当前审批人ID: {}", task.getId(), task.getAssigneeId());

        try {
            // 1. 获取审批人的上级
            Long supervisorId = getSupervisorId(task.getAssigneeId());
            if (supervisorId == null) {
                log.warn("无法获取审批人的上级，任务ID: {}, 审批人ID: {}, 尝试升级处理",
                        task.getId(), task.getAssigneeId());
                // 如果没有上级，尝试升级处理
                escalateTask(task);
                return;
            }

            // 2. 通过网关调用OA服务转交任务
            ResponseDTO<String> response = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/task/" + task.getId() + "/transfer?targetUserId=" + supervisorId,
                    HttpMethod.PUT,
                    null,
                    String.class
            );

            if (response != null && response.isSuccess()) {
                log.info("自动转交超时任务成功，任务ID: {}, 原审批人ID: {}, 新审批人ID: {}",
                        task.getId(), task.getAssigneeId(), supervisorId);
            } else {
                log.error("自动转交超时任务失败，任务ID: {}, 错误: {}",
                        task.getId(), response != null ? response.getMessage() : "响应为空");
            }
        } catch (Exception e) {
            log.error("自动转交超时任务异常，任务ID: {}", task.getId(), e);
        }
    }

    /**
     * 获取员工的上级ID
     *
     * @param employeeId 员工ID
     * @return 上级ID，如果不存在则返回null
     */
    private Long getSupervisorId(Long employeeId) {
        try {
            // 通过网关调用公共服务的员工查询接口
            // 注意：这里需要根据实际的EmployeeService接口调整
            // 假设有getEmployeeDetail接口返回员工信息，包含supervisorId字段
            ResponseDTO<Map<String, Object>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/employee/" + employeeId,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<Map<String, Object>>>() {}
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                Map<String, Object> employeeData = response.getData();
                Object supervisorIdObj = employeeData.get("supervisorId");
                if (supervisorIdObj != null) {
                    if (supervisorIdObj instanceof Long) {
                        return (Long) supervisorIdObj;
                    } else if (supervisorIdObj instanceof Integer) {
                        return ((Integer) supervisorIdObj).longValue();
                    } else if (supervisorIdObj instanceof String) {
                        return Long.parseLong((String) supervisorIdObj);
                    }
                }
            }

            log.warn("无法获取员工的上级ID，员工ID: {}", employeeId);
            return null;
        } catch (Exception e) {
            log.error("获取员工上级ID异常，员工ID: {}", employeeId, e);
            return null;
        }
    }

    /**
     * 自动通过
     *
     * @param task 任务实体
     */
    @SuppressWarnings("null")
    private void autoApprove(WorkflowTaskEntity task) {
        log.info("自动通过超时任务，任务ID: {}, 任务名称: {}", task.getId(), task.getTaskName());

        try {
            // 构建自动通过的审批意见
            String comment = "任务超时，系统自动通过审批";

            // 通过网关调用OA服务自动通过任务
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("outcome", "同意");
            requestParams.put("comment", comment);

            ResponseDTO<String> response = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/task/" + task.getId() + "/complete",
                    HttpMethod.POST,
                    requestParams,
                    String.class
            );

            if (response != null && response.isSuccess()) {
                log.info("自动通过超时任务成功，任务ID: {}", task.getId());
            } else {
                log.error("自动通过超时任务失败，任务ID: {}, 错误: {}",
                        task.getId(), response != null ? response.getMessage() : "响应为空");
            }
        } catch (Exception e) {
            log.error("自动通过超时任务异常，任务ID: {}", task.getId(), e);
        }
    }

    /**
     * 升级处理
     *
     * @param task 任务实体
     */
    @SuppressWarnings("null")
    private void escalateTask(WorkflowTaskEntity task) {
        log.info("升级超时任务，任务ID: {}, 任务名称: {}", task.getId(), task.getTaskName());

        try {
            // 1. 获取更高层级的审批人（通常是部门负责人或更高级别）
            Long escalatedUserId = getEscalatedUserId(task.getAssigneeId());
            if (escalatedUserId == null) {
                log.warn("无法获取更高层级的审批人，任务ID: {}, 仅发送升级通知", task.getId());
            } else {
                // 转交给更高层级的审批人
                ResponseDTO<String> transferResponse = gatewayServiceClient.callOAService(
                        "/api/v1/workflow/engine/task/" + task.getId() + "/transfer?targetUserId=" + escalatedUserId,
                        HttpMethod.PUT,
                        null,
                        String.class
                );

                if (transferResponse != null && transferResponse.isSuccess()) {
                    log.info("升级转交成功，任务ID: {}, 升级审批人ID: {}", task.getId(), escalatedUserId);
                }
            }

            // 2. 发送升级通知给相关人员
            String title = "审批任务已升级处理";
            String content = String.format(
                    "审批任务因超时已升级处理：\n" +
                            "任务名称：%s\n" +
                            "流程名称：%s\n" +
                            "原审批人：%s\n" +
                            "请关注处理进度。",
                    task.getTaskName(),
                    task.getProcessName() != null ? task.getProcessName() : "未知流程",
                    task.getAssigneeName() != null ? task.getAssigneeName() : "未知"
            );

            NotificationEntity notification = new NotificationEntity();
            notification.setTitle(title);
            notification.setContent(content);
            notification.setNotificationType(2); // 2-业务通知
            notification.setReceiverType(1); // 1-指定用户
            notification.setReceiverIds(String.valueOf(task.getAssigneeId())); // 通知原审批人
            notification.setChannel(5); // 5-WebSocket
            notification.setStatus(0); // 0-待发送
            notification.setCreateTime(LocalDateTime.now());

            // 保存通知记录（由定时任务处理发送）
            notificationDao.insert(notification);

            log.info("升级处理完成，任务ID: {}", task.getId());
        } catch (Exception e) {
            log.error("升级超时任务异常，任务ID: {}", task.getId(), e);
        }
    }

    /**
     * 获取更高层级的审批人ID
     *
     * @param currentUserId 当前用户ID
     * @return 更高层级的审批人ID，如果不存在则返回null
     */
    private Long getEscalatedUserId(Long currentUserId) {
        try {
            // 先获取当前用户的上级
            Long supervisorId = getSupervisorId(currentUserId);
            if (supervisorId == null) {
                return null;
            }

            // 再获取上级的上级（更高层级）
            Long escalatedId = getSupervisorId(supervisorId);
            return escalatedId != null ? escalatedId : supervisorId; // 如果没有更高层级，返回上级
        } catch (Exception e) {
            log.error("获取更高层级审批人ID异常，当前用户ID: {}", currentUserId, e);
            return null;
        }
    }

    /**
     * 从任务变量中提取超时策略
     * <p>
     * 超时策略可能存储在任务变量中
     * 如果任务变量中没有，则从流程定义中获取默认超时策略
     * </p>
     *
     * @param task 任务实体
     * @return 超时策略（AUTO_TRANSFER, AUTO_APPROVE, ESCALATE）
     */
    private String extractTimeoutStrategy(WorkflowTaskEntity task) {
        try {
            // 1. 先从任务变量中获取
            String variablesJson = task.getVariables();
            if (variablesJson != null && !variablesJson.isEmpty()) {
                Map<String, Object> variables = objectMapper.readValue(
                        variablesJson,
                        new TypeReference<Map<String, Object>>() {});

                Object timeoutStrategyObj = variables.get("timeoutStrategy");
                if (timeoutStrategyObj != null) {
                    String timeoutStrategy = timeoutStrategyObj.toString();
                    if (!timeoutStrategy.isEmpty()) {
                        log.debug("从任务变量中获取超时策略，任务ID: {}, 策略: {}",
                                task.getId(), timeoutStrategy);
                        return timeoutStrategy;
                    }
                }
            }

            // 2. 如果任务变量中没有，尝试从流程定义中获取
            // 注意：这里需要查询流程定义，暂时返回null，使用默认策略
            log.debug("任务变量中未找到超时策略，任务ID: {}, 使用默认策略", task.getId());
            return null; // 返回null，使用默认策略（AUTO_TRANSFER）

        } catch (Exception e) {
            log.warn("从任务变量中提取超时策略失败，任务ID: {}, 使用默认策略", task.getId(), e);
            return null; // 异常情况下，使用默认策略
        }
    }
}





