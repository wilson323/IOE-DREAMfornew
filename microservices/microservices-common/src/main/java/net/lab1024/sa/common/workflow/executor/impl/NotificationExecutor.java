package net.lab1024.sa.common.workflow.executor.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.workflow.executor.NodeExecutionContext;
import net.lab1024.sa.common.workflow.executor.NodeExecutionResult;
import net.lab1024.sa.common.workflow.executor.NodeExecutor;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 通知执行器
 * <p>
 * 企业级通知节点执行器，支持邮件、短信、微信、钉钉等多种通知方式
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class NotificationExecutor implements NodeExecutor {

    private final GatewayServiceClient gatewayServiceClient;

    // 构造函数注入依赖
    public NotificationExecutor(GatewayServiceClient gatewayServiceClient) {
        this.gatewayServiceClient = gatewayServiceClient;
    }

    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) throws Exception {
        log.info("[通知执行器] 开始执行通知节点: instanceId={}, nodeId={}, nodeName={}",
                context.getInstanceId(), context.getNodeId(), context.getNodeName());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取通知配置
            Map<String, Object> nodeConfig = context.getNodeConfig();
            if (nodeConfig == null) {
                return NodeExecutionResult.failure("通知节点配置为空");
            }

            String notificationType = (String) nodeConfig.get("notificationType");
            String[] recipients = (String[]) nodeConfig.get("recipients");
            // subject, content, templateCode, templateData 在具体通知方法中使用，这里不需要提前提取
            Boolean async = (Boolean) nodeConfig.getOrDefault("async", true);
            Integer priority = (Integer) nodeConfig.getOrDefault("priority", 1);

            log.debug("[通知执行器] 通知配置: type={}, recipients={}, async={}, priority={}",
                    notificationType, recipients != null ? recipients.length : 0, async, priority);

            // 2. 根据通知类型执行
            NodeExecutionResult result;
            switch (notificationType != null ? notificationType.toLowerCase() : "email") {
                case "email":
                    result = executeEmailNotification(context, nodeConfig);
                    break;
                case "sms":
                    result = executeSmsNotification(context, nodeConfig);
                    break;
                case "wechat":
                    result = executeWechatNotification(context, nodeConfig);
                    break;
                case "dingtalk":
                    result = executeDingtalkNotification(context, nodeConfig);
                    break;
                case "push":
                    result = executePushNotification(context, nodeConfig);
                    break;
                case "system":
                    result = executeSystemNotification(context, nodeConfig);
                    break;
                case "webhook":
                    result = executeWebhookNotification(context, nodeConfig);
                    break;
                case "multi":
                    result = executeMultiNotification(context, nodeConfig);
                    break;
                default:
                    result = executeCustomNotification(context, nodeConfig);
                    break;
            }

            // 3. 异步处理
            if (async != null && async) {
                result = executeAsyncNotification(result, context, nodeConfig);
            }

            // 4. 记录执行结果
            result.setStartTime(LocalDateTime.now())
               .setEndTime(LocalDateTime.now())
               .calculateDuration();

            long duration = System.currentTimeMillis() - startTime;
            log.info("[通知执行器] 通知节点执行完成: instanceId={}, nodeId={}, type={}, status={}, duration={}ms",
                    context.getInstanceId(), context.getNodeId(), notificationType, result.getStatus(), duration);

            return result;

        } catch (Exception e) {
            log.error("[通知执行器] 通知节点执行异常: instanceId={}, nodeId={}, error={}",
                    context.getInstanceId(), context.getNodeId(), e.getMessage(), e);
            return NodeExecutionResult.failure("通知执行异常: " + e.getMessage());
        }
    }

    /**
     * 邮件通知执行
     */
    private NodeExecutionResult executeEmailNotification(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[通知执行器] 执行邮件通知");

        String[] recipients = (String[]) nodeConfig.get("recipients");
        String subject = (String) nodeConfig.get("subject");
        String content = (String) nodeConfig.get("content");
        String templateCode = (String) nodeConfig.get("templateCode");
        @SuppressWarnings("unchecked")
        Map<String, Object> templateData = (Map<String, Object>) nodeConfig.get("templateData");

        if (recipients == null || recipients.length == 0) {
            return NodeExecutionResult.failure("邮件收件人不能为空");
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("notificationType", "email");
            request.put("recipients", recipients);
            request.put("subject", subject);
            request.put("content", content);
            request.put("templateCode", templateCode);
            request.put("templateData", templateData);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());
            request.put("executionData", context.getExecutionData());

            // 调用邮件服务
            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/notification/email/send",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "notificationType", "email",
                "operation", "send",
                "recipients", recipients,
                "subject", subject,
                "response", response
            ));

        } catch (Exception e) {
            log.error("[通知执行器] 邮件发送失败: recipients={}, error={}", recipients, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 短信通知执行
     */
    private NodeExecutionResult executeSmsNotification(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[通知执行器] 执行短信通知");

        String[] phoneNumbers = (String[]) nodeConfig.get("phoneNumbers");
        String templateCode = (String) nodeConfig.get("templateCode");
        @SuppressWarnings("unchecked")
        Map<String, Object> templateParams = (Map<String, Object>) nodeConfig.get("templateParams");

        if (phoneNumbers == null || phoneNumbers.length == 0) {
            return NodeExecutionResult.failure("短信接收手机号不能为空");
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("notificationType", "sms");
            request.put("phoneNumbers", phoneNumbers);
            request.put("templateCode", templateCode);
            request.put("templateParams", templateParams);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());
            request.put("executionData", context.getExecutionData());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/notification/sms/send",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "notificationType", "sms",
                "operation", "send",
                "phoneNumbers", phoneNumbers,
                "templateCode", templateCode,
                "response", response
            ));

        } catch (Exception e) {
            log.error("[通知执行器] 短信发送失败: phoneNumbers={}, error={}", phoneNumbers, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 微信通知执行
     */
    private NodeExecutionResult executeWechatNotification(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[通知执行器] 执行微信通知");

        String[] tousers = (String[]) nodeConfig.get("tousers");
        String msgtype = (String) nodeConfig.getOrDefault("msgtype", "text");
        String agentId = (String) nodeConfig.get("agentId");
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) nodeConfig.get("message");

        if (tousers == null || tousers.length == 0) {
            return NodeExecutionResult.failure("微信通知接收人不能为空");
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("notificationType", "wechat");
            request.put("tousers", tousers);
            request.put("msgtype", msgtype);
            request.put("agentId", agentId);
            request.put("message", message);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());
            request.put("executionData", context.getExecutionData());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/notification/wechat/send",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "notificationType", "wechat",
                "operation", "send",
                "tousers", tousers,
                "msgtype", msgtype,
                "response", response
            ));

        } catch (Exception e) {
            log.error("[通知执行器] 微信通知发送失败: tousers={}, error={}", tousers, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 钉钉通知执行
     */
    private NodeExecutionResult executeDingtalkNotification(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[通知执行器] 执行钉钉通知");

        String[] userIds = (String[]) nodeConfig.get("userIds");
        String deptIdList = (String) nodeConfig.get("deptIdList");
        String msgtype = (String) nodeConfig.getOrDefault("msgtype", "text");
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) nodeConfig.get("message");
        String agentId = (String) nodeConfig.get("agentId");

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("notificationType", "dingtalk");
            request.put("userIds", userIds);
            request.put("deptIdList", deptIdList);
            request.put("msgtype", msgtype);
            request.put("message", message);
            request.put("agentId", agentId);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());
            request.put("executionData", context.getExecutionData());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/notification/dingtalk/send",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "notificationType", "dingtalk",
                "operation", "send",
                "userIds", userIds,
                "deptIdList", deptIdList,
                "msgtype", msgtype,
                "response", response
            ));

        } catch (Exception e) {
            log.error("[通知执行器] 钉钉通知发送失败: userIds={}, error={}", userIds, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 推送通知执行
     */
    private NodeExecutionResult executePushNotification(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[通知执行器] 执行推送通知");

        String[] userIds = (String[]) nodeConfig.get("userIds");
        String title = (String) nodeConfig.get("title");
        String content = (String) nodeConfig.get("content");
        String pushType = (String) nodeConfig.getOrDefault("pushType", "app");
        @SuppressWarnings("unchecked")
        Map<String, Object> extraData = (Map<String, Object>) nodeConfig.get("extraData");

        if (userIds == null || userIds.length == 0) {
            return NodeExecutionResult.failure("推送通知用户不能为空");
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("notificationType", "push");
            request.put("userIds", userIds);
            request.put("title", title);
            request.put("content", content);
            request.put("pushType", pushType);
            request.put("extraData", extraData);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());
            request.put("executionData", context.getExecutionData());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/notification/push/send",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "notificationType", "push",
                "operation", "send",
                "userIds", userIds,
                "pushType", pushType,
                "title", title,
                "response", response
            ));

        } catch (Exception e) {
            log.error("[通知执行器] 推送通知发送失败: userIds={}, error={}", userIds, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 系统通知执行
     */
    private NodeExecutionResult executeSystemNotification(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[通知执行器] 执行系统通知");

        String[] userIds = (String[]) nodeConfig.get("userIds");
        String message = (String) nodeConfig.get("message");
        String level = (String) nodeConfig.getOrDefault("level", "info");
        String category = (String) nodeConfig.get("category");

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("notificationType", "system");
            request.put("userIds", userIds);
            request.put("message", message);
            request.put("level", level);
            request.put("category", category);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());
            request.put("executionData", context.getExecutionData());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/notification/system/send",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "notificationType", "system",
                "operation", "send",
                "userIds", userIds,
                "level", level,
                "category", category,
                "response", response
            ));

        } catch (Exception e) {
            log.error("[通知执行器] 系统通知发送失败: userIds={}, error={}", userIds, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Webhook通知执行
     */
    private NodeExecutionResult executeWebhookNotification(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[通知执行器] 执行Webhook通知");

        String webhookUrl = (String) nodeConfig.get("webhookUrl");
        String method = (String) nodeConfig.getOrDefault("method", "POST");
        @SuppressWarnings("unchecked")
        Map<String, Object> headers = (Map<String, Object>) nodeConfig.get("headers");
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) nodeConfig.get("payload");
        Integer timeout = (Integer) nodeConfig.getOrDefault("timeout", 30);

        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            return NodeExecutionResult.failure("Webhook URL不能为空");
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("notificationType", "webhook");
            request.put("webhookUrl", webhookUrl);
            request.put("method", method);
            request.put("headers", headers);
            request.put("payload", payload);
            request.put("timeout", timeout);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());
            request.put("executionData", context.getExecutionData());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/notification/webhook/execute",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "notificationType", "webhook",
                "operation", "execute",
                "webhookUrl", webhookUrl,
                "method", method,
                "response", response
            ));

        } catch (Exception e) {
            log.error("[通知执行器] Webhook执行失败: url={}, error={}", webhookUrl, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 多渠道通知执行
     */
    private NodeExecutionResult executeMultiNotification(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[通知执行器] 执行多渠道通知");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> notifications = (List<Map<String, Object>>) nodeConfig.get("notifications");

        if (notifications == null || notifications.isEmpty()) {
            return NodeExecutionResult.failure("多渠道通知配置不能为空");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (Map<String, Object> notification : notifications) {
            try {
                // 合并配置
                Map<String, Object> mergedConfig = new HashMap<>(nodeConfig);
                mergedConfig.putAll(notification);

                // 执行单个通知
                NodeExecutionResult result = execute(context);
                results.add(Map.of(
                    "type", notification.get("notificationType"),
                    "status", result.getStatus() != null ? result.getStatus().toString() : "UNKNOWN",
                    "message", result.getErrorMessage() != null ? result.getErrorMessage() : "执行成功"
                ));

                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }

            } catch (Exception e) {
                log.error("[通知执行器] 多渠道通知单项失败: type={}, error={}",
                    notification.get("notificationType"), e.getMessage(), e);
                results.add(Map.of(
                    "type", notification.get("notificationType"),
                    "status", "FAILURE",
                    "message", e.getMessage()
                ));
                failureCount++;
            }
        }

        return NodeExecutionResult.success(Map.of(
            "notificationType", "multi",
            "operation", "send",
            "totalCount", notifications.size(),
            "successCount", successCount,
            "failureCount", failureCount,
            "results", results
        ));
    }

    /**
     * 自定义通知执行
     */
    private NodeExecutionResult executeCustomNotification(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[通知执行器] 执行自定义通知");

        String customType = (String) nodeConfig.get("customType");
        String customService = (String) nodeConfig.get("customService");
        String customMethod = (String) nodeConfig.get("customMethod");
        @SuppressWarnings("unchecked")
        Map<String, Object> customParams = (Map<String, Object>) nodeConfig.get("customParams");

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("notificationType", "custom");
            request.put("customType", customType);
            request.put("customService", customService);
            request.put("customMethod", customMethod);
            request.put("customParams", customParams);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());
            request.put("executionData", context.getExecutionData());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/notification/custom/execute",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "notificationType", "custom",
                "operation", "execute",
                "customType", customType,
                "customService", customService,
                "response", response
            ));

        } catch (Exception e) {
            log.error("[通知执行器] 自定义通知执行失败: customType={}, error={}", customType, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 异步通知执行
     */
    private NodeExecutionResult executeAsyncNotification(NodeExecutionResult result, NodeExecutionContext context, Map<String, Object> nodeConfig) {
        try {
            // 模拟异步处理
            log.debug("[通知执行器] 异步处理通知: nodeId={}", context.getNodeId());

            // 在实际实现中，这里应该将通知任务放入消息队列
            // 当前简化为同步执行，但返回异步标识
            result.getOutputData().put("async", true);
            result.getOutputData().put("taskId", "NOTIFY-" + context.getInstanceId() + "-" + context.getNodeId());

            return result;

        } catch (Exception e) {
            log.error("[通知执行器] 异步处理失败: error={}", e.getMessage(), e);
            return NodeExecutionResult.failure("异步处理失败: " + e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "notification";
    }

    @Override
    public String getDescription() {
        return "通知执行器，支持邮件、短信、微信、钉钉、推送、系统、Webhook等多种通知方式";
    }
}
