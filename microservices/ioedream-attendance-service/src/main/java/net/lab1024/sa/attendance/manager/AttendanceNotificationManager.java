package net.lab1024.sa.attendance.manager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
/**
 * 考勤通知管理器
 *
 * <p>
 * 考勤模块的通知和提醒管理器，提供智能化的考勤提醒和异常通知功能
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 实现四层架构中的Manager层，提供复杂的通知和消息推送逻辑
 * </p>
 *
 * <p>
 * 功能职责：
 * - 智能提醒：上下班打卡提醒、加班提醒等
 * - 异常通知：迟到、早退、缺勤等异常情况通知
 * - 消息推送：多渠道消息推送（短信、邮件、App推送等）
 * - 定时任务：定时触发各类考勤提醒和统计任务
 * - 通知规则：可配置的通知规则和模板
 * - 消息模板：标准化的消息模板管理
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-16
 */
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;

@Slf4j
@Component
public class AttendanceNotificationManager {

    @Resource
    private AttendanceDao attendanceRepository;

    // 线程池
    private final ExecutorService notificationExecutor = Executors.newFixedThreadPool(3);
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);

    // ===== 通知消息相关类 =====

    /**
     * 通知消息
     */
    public static class NotificationMessage {
        private Long recipientId; // 接收者ID
        private String recipientType; // 接收者类型（EMPLOYEE/MANAGER/ADMIN）
        private String messageType; // 消息类型
        private String title; // 消息标题
        private String content; // 消息内容
        private String channel; // 发送渠道（EMAIL/SMS/PUSH/WECHAT）
        private LocalDateTime sendTime; // 发送时间
        private Map<String, Object> data; // 附加数据
        private Boolean isRead; // 是否已读
        private Integer priority; // 优先级（1-5）

        // Getters and Setters
        public Long getRecipientId() {
            return recipientId;
        }

        public void setRecipientId(Long recipientId) {
            this.recipientId = recipientId;
        }

        public String getRecipientType() {
            return recipientType;
        }

        public void setRecipientType(String recipientType) {
            this.recipientType = recipientType;
        }

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public LocalDateTime getSendTime() {
            return sendTime;
        }

        public void setSendTime(LocalDateTime sendTime) {
            this.sendTime = sendTime;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        public Boolean getIsRead() {
            return isRead;
        }

        public void setIsRead(Boolean isRead) {
            this.isRead = isRead;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }
    }

    /**
     * 通知模板
     */
    public static class NotificationTemplate {
        private String templateCode; // 模板编码
        private String templateName; // 模板名称
        private String templateType; // 模板类型
        private String titleTemplate; // 标题模板
        private String contentTemplate; // 内容模板
        private Map<String, String> variables; // 模板变量
        private Boolean isActive; // 是否启用

        // Getters and Setters
        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public String getTemplateType() {
            return templateType;
        }

        public void setTemplateType(String templateType) {
            this.templateType = templateType;
        }

        public String getTitleTemplate() {
            return titleTemplate;
        }

        public void setTitleTemplate(String titleTemplate) {
            this.titleTemplate = titleTemplate;
        }

        public String getContentTemplate() {
            return contentTemplate;
        }

        public void setContentTemplate(String contentTemplate) {
            this.contentTemplate = contentTemplate;
        }

        public Map<String, String> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, String> variables) {
            this.variables = variables;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }

    /**
     * 通知发送结果
     */
    public static class NotificationSendResult {
        private boolean success;
        private String messageId;
        private String errorMessage;
        private String channel;
        private Long recipientId;
        private LocalDateTime sendTime;

        public static NotificationSendResult success(String messageId, String channel) {
            NotificationSendResult result = new NotificationSendResult();
            result.setSuccess(true);
            result.setMessageId(messageId);
            result.setChannel(channel);
            result.setSendTime(LocalDateTime.now());
            return result;
        }

        public static NotificationSendResult failure(String errorMessage, String channel) {
            NotificationSendResult result = new NotificationSendResult();
            result.setSuccess(false);
            result.setErrorMessage(errorMessage);
            result.setChannel(channel);
            result.setSendTime(LocalDateTime.now());
            return result;
        }

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public Long getRecipientId() {
            return recipientId;
        }

        public void setRecipientId(Long recipientId) {
            this.recipientId = recipientId;
        }

        public LocalDateTime getSendTime() {
            return sendTime;
        }

        public void setSendTime(LocalDateTime sendTime) {
            this.sendTime = sendTime;
        }
    }

    // ===== 核心通知方法 =====

    /**
     * 发送考勤异常通知
     *
     * @param record    考勤记录
     * @param exception 异常类型
     * @return 发送结果
     */
    public NotificationSendResult sendAttendanceExceptionNotification(AttendanceRecordEntity record, String exception) {
        if (record == null || record.getEmployeeId() == null) {
            log.warn("发送考勤异常通知失败：记录为空或员工ID为空");
            return NotificationSendResult.failure("参数无效", "SYSTEM");
        }

        try {
            log.info("发送考勤异常通知：员工ID={}, 异常类型={}", record.getEmployeeId(), exception);

            // 1. 获取通知模板
            NotificationTemplate template = getExceptionNotificationTemplate(exception);
            if (template == null) {
                return NotificationSendResult.failure("通知模板不存在", "SYSTEM");
            }

            // 2. 构建通知消息
            NotificationMessage message = buildExceptionNotificationMessage(record, exception, template);

            // 3. 发送通知
            return sendNotification(message);

        } catch (Exception e) {
            log.error("发送考勤异常通知异常：员工ID={}, 异常类型={}", record.getEmployeeId(), exception, e);
            return NotificationSendResult.failure("发送异常：" + e.getMessage(), "SYSTEM");
        }
    }

    /**
     * 发送上班打卡提醒
     *
     * @param employeeIds 员工ID列表
     * @return 发送结果列表
     */
    public List<NotificationSendResult> sendPunchInReminder(List<Long> employeeIds) {
        if (CollectionUtils.isEmpty(employeeIds)) {
            log.warn("发送上班打卡提醒失败：员工列表为空");
            return Collections.emptyList();
        }

        try {
            log.info("开始发送上班打卡提醒：员工数={}", employeeIds.size());

            // 1. 获取提醒模板
            NotificationTemplate template = getReminderNotificationTemplate("PUNCH_IN_REMINDER");
            if (template == null) {
                return employeeIds.stream()
                        .map(id -> NotificationSendResult.failure("通知模板不存在", "SYSTEM"))
                        .collect(Collectors.toList());
            }

            // 2. 异步批量发送
            List<CompletableFuture<NotificationSendResult>> futures = employeeIds.stream()
                    .map(employeeId -> CompletableFuture.supplyAsync(
                            () -> sendPunchInReminder(employeeId, template), notificationExecutor))
                    .toList();

            // 3. 等待所有任务完成
            List<NotificationSendResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            long successCount = results.stream().mapToLong(result -> result.isSuccess() ? 1 : 0).sum();
            log.info("上班打卡提醒发送完成：总数={}, 成功={}, 失败={}",
                    employeeIds.size(), successCount, employeeIds.size() - successCount);

            return results;

        } catch (Exception e) {
            log.error("发送上班打卡提醒异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 发送下班打卡提醒
     *
     * @param employeeIds 员工ID列表
     * @return 发送结果列表
     */
    public List<NotificationSendResult> sendPunchOutReminder(List<Long> employeeIds) {
        if (CollectionUtils.isEmpty(employeeIds)) {
            log.warn("发送下班打卡提醒失败：员工列表为空");
            return Collections.emptyList();
        }

        try {
            log.info("开始发送下班打卡提醒：员工数={}", employeeIds.size());

            // 1. 获取提醒模板
            NotificationTemplate template = getReminderNotificationTemplate("PUNCH_OUT_REMINDER");
            if (template == null) {
                return employeeIds.stream()
                        .map(id -> NotificationSendResult.failure("通知模板不存在", "SYSTEM"))
                        .collect(Collectors.toList());
            }

            // 2. 异步批量发送
            List<CompletableFuture<NotificationSendResult>> futures = employeeIds.stream()
                    .map(employeeId -> CompletableFuture.supplyAsync(
                            () -> sendPunchOutReminder(employeeId, template), notificationExecutor))
                    .toList();

            // 3. 等待所有任务完成
            List<NotificationSendResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            long successCount = results.stream().mapToLong(result -> result.isSuccess() ? 1 : 0).sum();
            log.info("下班打卡提醒发送完成：总数={}, 成功={}, 失败={}",
                    employeeIds.size(), successCount, employeeIds.size() - successCount);

            return results;

        } catch (Exception e) {
            log.error("发送下班打卡提醒异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 发送考勤统计报告
     *
     * @param recipientId 接收者ID
     * @param reportType  报告类型
     * @param period      统计周期
     * @return 发送结果
     */
    public NotificationSendResult sendAttendanceReport(Long recipientId, String reportType, String period) {
        if (recipientId == null || reportType == null) {
            log.warn("发送考勤统计报告失败：参数无效");
            return NotificationSendResult.failure("参数无效", "SYSTEM");
        }

        try {
            log.info("发送考勤统计报告：接收者={}, 报告类型={}, 统计周期={}", recipientId, reportType, period);

            // 1. 生成报告数据
            Map<String, Object> reportData = generateAttendanceReportData(recipientId, reportType, period);
            if (reportData.isEmpty()) {
                return NotificationSendResult.failure("生成报告数据失败", "SYSTEM");
            }

            // 2. 获取报告模板
            NotificationTemplate template = getReportNotificationTemplate(reportType);
            if (template == null) {
                return NotificationSendResult.failure("通知模板不存在", "SYSTEM");
            }

            // 3. 构建通知消息
            NotificationMessage message = buildReportNotificationMessage(recipientId, reportType, period, reportData,
                    template);

            // 4. 发送通知
            return sendNotification(message);

        } catch (Exception e) {
            log.error("发送考勤统计报告异常：接收者={}, 报告类型={}", recipientId, reportType, e);
            return NotificationSendResult.failure("发送异常：" + e.getMessage(), "SYSTEM");
        }
    }

    // ===== 定时任务方法 =====

    /**
     * 启动定时提醒任务
     */
    public void startScheduledReminders() {
        try {
            log.info("启动考勤定时提醒任务");

            // 1. 上班打卡提醒（每个工作日 8:30）
            scheduledExecutor.scheduleAtFixedRate(() -> {
                try {
                    if (isWorkdayNow()) {
                        List<Long> employees = getEmployeesNeedPunchInReminder();
                        sendPunchInReminder(employees);
                    }
                } catch (Exception e) {
                    log.error("定时上班提醒任务异常", e);
                }
            }, calculateInitialDelay(LocalTime.of(8, 30)), TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);

            // 2. 下班打卡提醒（每个工作日 18:00）
            scheduledExecutor.scheduleAtFixedRate(() -> {
                try {
                    if (isWorkdayNow()) {
                        List<Long> employees = getEmployeesNeedPunchOutReminder();
                        sendPunchOutReminder(employees);
                    }
                } catch (Exception e) {
                    log.error("定时下班提醒任务异常", e);
                }
            }, calculateInitialDelay(LocalTime.of(18, 0)), TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);

            // 3. 每日异常检查（每天 21:00）
            scheduledExecutor.scheduleAtFixedRate(() -> {
                try {
                    checkDailyAttendanceExceptions();
                } catch (Exception e) {
                    log.error("每日异常检查任务异常", e);
                }
            }, calculateInitialDelay(LocalTime.of(21, 0)), TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);

            log.info("考勤定时提醒任务启动完成");

        } catch (Exception e) {
            log.error("启动考勤定时提醒任务异常", e);
        }
    }

    /**
     * 停止定时提醒任务
     */
    public void stopScheduledReminders() {
        try {
            log.info("停止考勤定时提醒任务");
            scheduledExecutor.shutdown();
            if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
            log.info("考勤定时提醒任务已停止");
        } catch (Exception e) {
            log.error("停止考勤定时提醒任务异常", e);
        }
    }

    // ===== 辅助方法 =====

    /**
     * 获取异常通知模板
     *
     * @param exception 异常类型
     * @return 通知模板
     */
    private NotificationTemplate getExceptionNotificationTemplate(String exception) {
        try {
            NotificationTemplate template = new NotificationTemplate();
            template.setTemplateCode("ATTENDANCE_EXCEPTION_" + exception);
            template.setTemplateName("考勤异常通知");
            template.setTemplateType("EXCEPTION");

            // 根据异常类型设置模板内容
            switch (exception) {
                case "LATE":
                    template.setTitleTemplate("迟到提醒");
                    template.setContentTemplate("您今天上班迟到，请注意时间管理。");
                    break;
                case "EARLY_LEAVE":
                    template.setTitleTemplate("早退提醒");
                    template.setContentTemplate("您今天提前下班，请确认是否经过批准。");
                    break;
                case "ABSENCE":
                    template.setTitleTemplate("缺勤提醒");
                    template.setContentTemplate("您今天未打卡，请及时处理考勤异常。");
                    break;
                case "MISSING_PUNCH_IN":
                    template.setTitleTemplate("漏打卡提醒");
                    template.setContentTemplate("您今天忘记上班打卡，请及时补卡。");
                    break;
                case "MISSING_PUNCH_OUT":
                    template.setTitleTemplate("漏打卡提醒");
                    template.setContentTemplate("您今天忘记下班打卡，请及时补卡。");
                    break;
                default:
                    template.setTitleTemplate("考勤异常提醒");
                    template.setContentTemplate("您的考勤记录存在异常，请及时查看处理。");
            }

            template.setIsActive(true);
            return template;

        } catch (Exception e) {
            log.error("获取异常通知模板异常：异常类型={}", exception, e);
            return null;
        }
    }

    /**
     * 获取提醒通知模板
     *
     * @param reminderType 提醒类型
     * @return 通知模板
     */
    private NotificationTemplate getReminderNotificationTemplate(String reminderType) {
        try {
            NotificationTemplate template = new NotificationTemplate();
            template.setTemplateCode(reminderType);
            template.setIsActive(true);

            switch (reminderType) {
                case "PUNCH_IN_REMINDER":
                    template.setTemplateName("上班打卡提醒");
                    template.setTitleTemplate("上班打卡提醒");
                    template.setContentTemplate("现在是上班时间，请您及时打卡。祝您工作愉快！");
                    break;
                case "PUNCH_OUT_REMINDER":
                    template.setTemplateName("下班打卡提醒");
                    template.setTitleTemplate("下班打卡提醒");
                    template.setContentTemplate("现在是下班时间，请您记得打卡。祝您生活愉快！");
                    break;
                default:
                    return null;
            }

            return template;

        } catch (Exception e) {
            log.error("获取提醒通知模板异常：提醒类型={}", reminderType, e);
            return null;
        }
    }

    /**
     * 获取报告通知模板
     *
     * @param reportType 报告类型
     * @return 通知模板
     */
    private NotificationTemplate getReportNotificationTemplate(String reportType) {
        try {
            NotificationTemplate template = new NotificationTemplate();
            template.setTemplateCode("ATTENDANCE_REPORT_" + reportType);
            template.setTemplateName("考勤统计报告");
            template.setTemplateType("REPORT");
            template.setIsActive(true);

            switch (reportType) {
                case "DAILY":
                    template.setTitleTemplate("每日考勤统计报告");
                    template.setContentTemplate("今日考勤统计已生成，请查收详细报告。");
                    break;
                case "WEEKLY":
                    template.setTitleTemplate("每周考勤统计报告");
                    template.setContentTemplate("本周考勤统计已生成，请查收详细报告。");
                    break;
                case "MONTHLY":
                    template.setTitleTemplate("每月考勤统计报告");
                    template.setContentTemplate("本月考勤统计已生成，请查收详细报告。");
                    break;
                default:
                    template.setTitleTemplate("考勤统计报告");
                    template.setContentTemplate("考勤统计报告已生成，请查收详细信息。");
            }

            return template;

        } catch (Exception e) {
            log.error("获取报告通知模板异常：报告类型={}", reportType, e);
            return null;
        }
    }

    /**
     * 构建异常通知消息
     *
     * @param record    考勤记录
     * @param exception 异常类型
     * @param template  通知模板
     * @return 通知消息
     */
    private NotificationMessage buildExceptionNotificationMessage(AttendanceRecordEntity record, String exception,
            NotificationTemplate template) {
        NotificationMessage message = new NotificationMessage();
        message.setRecipientId(record.getEmployeeId());
        message.setRecipientType("EMPLOYEE");
        message.setMessageType("ATTENDANCE_EXCEPTION");
        message.setTitle(template.getTitleTemplate());
        message.setContent(template.getContentTemplate());
        message.setChannel("PUSH");
        message.setSendTime(LocalDateTime.now());
        message.setPriority(3);

        // 设置附加数据
        Map<String, Object> data = new HashMap<>();
        data.put("employeeId", record.getEmployeeId());
        data.put("attendanceDate", record.getAttendanceDate());
        data.put("exceptionType", exception);
        if (record.getPunchInTime() != null) {
            data.put("punchInTime", record.getPunchInTime());
        }
        if (record.getPunchOutTime() != null) {
            data.put("punchOutTime", record.getPunchOutTime());
        }
        message.setData(data);

        return message;
    }

    /**
     * 构建报告通知消息
     *
     * @param recipientId 接收者ID
     * @param reportType  报告类型
     * @param period      统计周期
     * @param reportData  报告数据
     * @param template    通知模板
     * @return 通知消息
     */
    private NotificationMessage buildReportNotificationMessage(Long recipientId, String reportType, String period,
            Map<String, Object> reportData, NotificationTemplate template) {
        NotificationMessage message = new NotificationMessage();
        message.setRecipientId(recipientId);
        message.setRecipientType("MANAGER");
        message.setMessageType("ATTENDANCE_REPORT");
        message.setTitle(template.getTitleTemplate());
        message.setContent(template.getContentTemplate());
        message.setChannel("EMAIL");
        message.setSendTime(LocalDateTime.now());
        message.setPriority(2);

        // 设置附加数据
        Map<String, Object> data = new HashMap<>();
        data.put("reportType", reportType);
        data.put("period", period);
        data.put("reportData", reportData);
        message.setData(data);

        return message;
    }

    /**
     * 发送单个上班打卡提醒
     *
     * @param employeeId 员工ID
     * @param template   通知模板
     * @return 发送结果
     */
    private NotificationSendResult sendPunchInReminder(Long employeeId, NotificationTemplate template) {
        try {
            // 检查员工今日是否已打卡
            Boolean hasPunchedResult = attendanceRepository.hasEmployeeTodayPunched(employeeId);
            boolean hasPunched = hasPunchedResult != null && hasPunchedResult;
            if (hasPunched) {
                return NotificationSendResult.success("ALREADY_PUNCHED", "SYSTEM");
            }

            // 构建提醒消息
            NotificationMessage message = new NotificationMessage();
            message.setRecipientId(employeeId);
            message.setRecipientType("EMPLOYEE");
            message.setMessageType("PUNCH_IN_REMINDER");
            message.setTitle(template.getTitleTemplate());
            message.setContent(template.getContentTemplate());
            message.setChannel("PUSH");
            message.setSendTime(LocalDateTime.now());
            message.setPriority(2);

            return sendNotification(message);

        } catch (Exception e) {
            log.error("发送单个上班打卡提醒异常：员工ID={}", employeeId, e);
            return NotificationSendResult.failure("发送异常：" + e.getMessage(), "SYSTEM");
        }
    }

    /**
     * 发送单个下班打卡提醒
     *
     * @param employeeId 员工ID
     * @param template   通知模板
     * @return 发送结果
     */
    private NotificationSendResult sendPunchOutReminder(Long employeeId, NotificationTemplate template) {
        try {
            // 检查员工今日是否已下班打卡
            AttendanceRecordEntity record = attendanceRepository.selectLastRecord(employeeId);
            boolean hasPunchedOut = record != null && record.getPunchOutTime() != null;
            if (hasPunchedOut) {
                return NotificationSendResult.success("ALREADY_PUNCHED_OUT", "SYSTEM");
            }

            // 构建提醒消息
            NotificationMessage message = new NotificationMessage();
            message.setRecipientId(employeeId);
            message.setRecipientType("EMPLOYEE");
            message.setMessageType("PUNCH_OUT_REMINDER");
            message.setTitle(template.getTitleTemplate());
            message.setContent(template.getContentTemplate());
            message.setChannel("PUSH");
            message.setSendTime(LocalDateTime.now());
            message.setPriority(2);

            return sendNotification(message);

        } catch (Exception e) {
            log.error("发送单个下班打卡提醒异常：员工ID={}", employeeId, e);
            return NotificationSendResult.failure("发送异常：" + e.getMessage(), "SYSTEM");
        }
    }

    /**
     * 发送通知
     *
     * @param message 通知消息
     * @return 发送结果
     */
    private NotificationSendResult sendNotification(NotificationMessage message) {
        try {
            // 这里应该调用实际的消息发送服务
            // 暂时返回成功，实际实现时需要集成邮件、短信、推送等服务
            log.info("发送通知：接收者={}, 类型={}, 渠道={}",
                    message.getRecipientId(), message.getMessageType(), message.getChannel());

            // 模拟发送延迟
            Thread.sleep(100);

            return NotificationSendResult.success("MSG_" + System.currentTimeMillis(), message.getChannel());

        } catch (Exception e) {
            log.error("发送通知异常：接收者={}", message.getRecipientId(), e);
            return NotificationSendResult.failure("发送异常：" + e.getMessage(), message.getChannel());
        }
    }

    /**
     * 生成考勤报告数据
     *
     * @param recipientId 接收者ID
     * @param reportType  报告类型
     * @param period      统计周期
     * @return 报告数据
     */
    private Map<String, Object> generateAttendanceReportData(Long recipientId, String reportType, String period) {
        try {
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("reportType", reportType);
            reportData.put("period", period);
            reportData.put("generateTime", LocalDateTime.now());

            // 这里应该根据报告类型生成实际的统计数据
            // 暂时返回空数据，实际实现时需要调用统计服务
            return reportData;

        } catch (Exception e) {
            log.error("生成考勤报告数据异常：接收者={}, 报告类型={}", recipientId, reportType, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 判断当前是否为工作日
     *
     * @return 是否为工作日
     */
    private boolean isWorkdayNow() {
        try {
            // 这里应该调用工作日历服务判断
            // 暂时返回true，实际实现时需要集成工作日历
            return true;
        } catch (Exception e) {
            log.error("判断工作日异常", e);
            return false;
        }
    }

    /**
     * 获取需要上班打卡提醒的员工列表
     *
     * @return 员工ID列表
     */
    private List<Long> getEmployeesNeedPunchInReminder() {
        try {
            // 这里应该查询需要提醒的员工
            // 暂时返回空列表，实际实现时需要查询员工和排班数据
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取需要上班打卡提醒的员工列表异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取需要下班打卡提醒的员工列表
     *
     * @return 员工ID列表
     */
    private List<Long> getEmployeesNeedPunchOutReminder() {
        try {
            // 这里应该查询需要提醒的员工
            // 暂时返回空列表，实际实现时需要查询员工和排班数据
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取需要下班打卡提醒的员工列表异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 检查每日考勤异常
     */
    private void checkDailyAttendanceExceptions() {
        try {
            log.info("开始检查每日考勤异常");

            // 这里应该查询今日的考勤异常记录
            // 并发送相应的通知给相关员工和管理者
            List<AttendanceRecordEntity> exceptionRecords = attendanceRepository.selectExceptionRecords(
                    LocalDateTime.now().toLocalDate(), LocalDateTime.now().toLocalDate(), null, null);

            if (!CollectionUtils.isEmpty(exceptionRecords)) {
                for (AttendanceRecordEntity record : exceptionRecords) {
                    if (record.getExceptionType() != null && Boolean.FALSE.equals(record.getIsProcessed())) {
                        sendAttendanceExceptionNotification(record, record.getExceptionType());
                    }
                }
            }

            log.info("每日考勤异常检查完成，发现异常记录数：{}", exceptionRecords.size());

        } catch (Exception e) {
            log.error("检查每日考勤异常异常", e);
        }
    }

    /**
     * 计算初始延迟时间（毫秒）
     *
     * @param targetTime 目标时间
     * @return 延迟时间（毫秒）
     */
    private long calculateInitialDelay(LocalTime targetTime) {
        LocalTime now = LocalTime.now();
        LocalTime todayTarget = targetTime;
        long delay = 0;

        if (now.isBefore(todayTarget)) {
            // 今天还未到目标时间
            delay = Duration.between(now, todayTarget).toMillis();
        } else {
            // 今天已过目标时间，安排到明天
            delay = Duration.between(now, todayTarget).plusHours(24).toMillis();
        }

        return Math.max(delay, 60000); // 最小延迟1分钟
    }
}
