package net.lab1024.sa.common.workflow.executor.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.workflow.executor.NodeExecutionContext;
import net.lab1024.sa.common.workflow.executor.NodeExecutionResult;
import net.lab1024.sa.common.workflow.executor.NodeExecutor;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 系统执行器
 * <p>
 * 企业级系统节点执行器，支持系统调用、数据处理、文件操作等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class SystemExecutor implements NodeExecutor {

    private final GatewayServiceClient gatewayServiceClient;

    // 构造函数注入依赖
    public SystemExecutor(GatewayServiceClient gatewayServiceClient) {
        this.gatewayServiceClient = gatewayServiceClient;
    }

    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) throws Exception {
        log.info("[系统执行器] 开始执行系统节点: instanceId={}, nodeId={}, nodeName={}",
                context.getInstanceId(), context.getNodeId(), context.getNodeName());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取系统节点配置
            Map<String, Object> nodeConfig = context.getNodeConfig();
            if (nodeConfig == null) {
                return NodeExecutionResult.failure("系统节点配置为空");
            }

            String systemType = (String) nodeConfig.get("systemType");
            String operationType = (String) nodeConfig.get("operationType");
            String targetService = (String) nodeConfig.get("targetService");
            String targetMethod = (String) nodeConfig.get("targetMethod");
            Integer timeoutSeconds = (Integer) nodeConfig.get("timeoutSeconds");

            log.debug("[系统执行器] 系统配置: type={}, operation={}, service={}, method={}, timeout={}s",
                    systemType, operationType, targetService, targetMethod, timeoutSeconds);

            // 2. 根据系统类型执行
            NodeExecutionResult result;
            switch (systemType != null ? systemType.toLowerCase() : "http") {
                case "http":
                    result = executeHttpCall(context, nodeConfig);
                    break;
                case "database":
                    result = executeDatabaseOperation(context, nodeConfig);
                    break;
                case "file":
                    result = executeFileOperation(context, nodeConfig);
                    break;
                case "notification":
                    result = executeNotificationOperation(context, nodeConfig);
                    break;
                case "integration":
                    result = executeIntegrationOperation(context, nodeConfig);
                    break;
                case "batch":
                    result = executeBatchOperation(context, nodeConfig);
                    break;
                default:
                    result = executeCustomOperation(context, nodeConfig);
                    break;
            }

            // 3. 设置超时处理
            if (timeoutSeconds != null && timeoutSeconds > 0) {
                result = executeWithTimeout(result, timeoutSeconds);
            }

            // 4. 记录执行结果
            result.setStartTime(LocalDateTime.now())
               .setEndTime(LocalDateTime.now())
               .calculateDuration();

            long duration = System.currentTimeMillis() - startTime;
            log.info("[系统执行器] 系统节点执行完成: instanceId={}, nodeId={}, status={}, duration={}ms",
                    context.getInstanceId(), context.getNodeId(), result.getStatus(), duration);

            return result;

        } catch (Exception e) {
            log.error("[系统执行器] 系统节点执行异常: instanceId={}, nodeId={}, error={}",
                    context.getInstanceId(), context.getNodeId(), e.getMessage(), e);
            return NodeExecutionResult.failure("系统执行异常: " + e.getMessage());
        }
    }

    /**
     * HTTP调用执行
     */
    private NodeExecutionResult executeHttpCall(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[系统执行器] 执行HTTP调用");

        String url = (String) nodeConfig.get("url");
        String method = (String) nodeConfig.get("method");
        @SuppressWarnings("unchecked")
        Map<String, Object> headers = (Map<String, Object>) nodeConfig.get("headers");
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) nodeConfig.get("body");

        if (url == null || url.trim().isEmpty()) {
            return NodeExecutionResult.failure("HTTP调用URL不能为空");
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("url", url);
            request.put("method", method != null ? method : "GET");
            request.put("headers", headers);
            request.put("body", body);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());

            // 调用HTTP服务
            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/system/http/call",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "systemType", "http",
                "operation", "call",
                "url", url,
                "method", method,
                "response", response,
                "executionData", context.getExecutionData()
            ));

        } catch (Exception e) {
            log.error("[系统执行器] HTTP调用失败: url={}, error={}", url, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 数据库操作执行
     */
    private NodeExecutionResult executeDatabaseOperation(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[系统执行器] 执行数据库操作");

        String operation = (String) nodeConfig.get("operation");
        String tableName = (String) nodeConfig.get("tableName");
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) nodeConfig.get("parameters");

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("operation", operation);
            request.put("tableName", tableName);
            request.put("parameters", parameters);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/system/database/operation",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "systemType", "database",
                "operation", operation,
                "tableName", tableName,
                "result", response,
                "affectedRows", response
            ));

        } catch (Exception e) {
            log.error("[系统执行器] 数据库操作失败: operation={}, tableName={}, error={}",
                    operation, tableName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 文件操作执行
     */
    private NodeExecutionResult executeFileOperation(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[系统执行器] 执行文件操作");

        String operation = (String) nodeConfig.get("operation");
        String filePath = (String) nodeConfig.get("filePath");
        String fileContent = (String) nodeConfig.get("fileContent");

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("operation", operation);
            request.put("filePath", filePath);
            request.put("fileContent", fileContent);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/system/file/operation",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "systemType", "file",
                "operation", operation,
                "filePath", filePath,
                "result", response
            ));

        } catch (Exception e) {
            log.error("[系统执行器] 文件操作失败: operation={}, filePath={}, error={}",
                    operation, filePath, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 通知操作执行
     */
    private NodeExecutionResult executeNotificationOperation(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[系统执行器] 执行通知操作");

        String notificationType = (String) nodeConfig.get("notificationType");
        String[] recipients = (String[]) nodeConfig.get("recipients");
        String subject = (String) nodeConfig.get("subject");
        String content = (String) nodeConfig.get("content");
        @SuppressWarnings("unchecked")
        Map<String, Object> templateData = (Map<String, Object>) nodeConfig.get("templateData");

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("notificationType", notificationType);
            request.put("recipients", recipients);
            request.put("subject", subject);
            request.put("content", content);
            request.put("templateData", templateData);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/system/notification/send",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "systemType", "notification",
                "operation", "send",
                "notificationType", notificationType,
                "recipients", recipients,
                "result", response
            ));

        } catch (Exception e) {
            log.error("[系统执行器] 通知操作失败: notificationType={}, recipients={}, error={}",
                    notificationType, recipients, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 集成操作执行
     */
    private NodeExecutionResult executeIntegrationOperation(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[系统执行器] 执行集成操作");

        String integrationType = (String) nodeConfig.get("integrationType");
        String targetSystem = (String) nodeConfig.get("targetSystem");
        @SuppressWarnings("unchecked")
        Map<String, Object> integrationConfig = (Map<String, Object>) nodeConfig.get("config");

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("integrationType", integrationType);
            request.put("targetSystem", targetSystem);
            request.put("config", integrationConfig);
            request.put("executionData", context.getExecutionData());
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/system/integration/execute",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "systemType", "integration",
                "operation", "execute",
                "integrationType", integrationType,
                "targetSystem", targetSystem,
                "result", response
            ));

        } catch (Exception e) {
            log.error("[系统执行器] 集成操作失败: integrationType={}, targetSystem={}, error={}",
                    integrationType, targetSystem, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量操作执行
     */
    private NodeExecutionResult executeBatchOperation(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[系统执行器] 执行批量操作");

        String batchType = (String) nodeConfig.get("batchType");
        Integer batchSize = (Integer) nodeConfig.get("batchSize");
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> batchItems = (java.util.List<Map<String, Object>>) nodeConfig.get("items");

        if (batchItems == null || batchItems.isEmpty()) {
            return NodeExecutionResult.failure("批量操作项目列表为空");
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("batchType", batchType);
            request.put("batchSize", batchSize != null ? batchSize : 100);
            request.put("items", batchItems);
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/system/batch/execute",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "systemType", "batch",
                "operation", "execute",
                "batchType", batchType,
                "batchSize", batchSize,
                "itemCount", batchItems.size(),
                "result", response
            ));

        } catch (Exception e) {
            log.error("[系统执行器] 批量操作失败: batchType={}, itemCount={}, error={}",
                    batchType, batchItems.size(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 自定义操作执行
     */
    private NodeExecutionResult executeCustomOperation(NodeExecutionContext context, Map<String, Object> nodeConfig) throws Exception {
        log.debug("[系统执行器] 执行自定义操作");

        String customOperation = (String) nodeConfig.get("customOperation");
        String script = (String) nodeConfig.get("script");
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) nodeConfig.get("parameters");

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("customOperation", customOperation);
            request.put("script", script);
            request.put("parameters", parameters);
            request.put("executionData", context.getExecutionData());
            request.put("instanceId", context.getInstanceId());
            request.put("nodeId", context.getNodeId());

            Object response = gatewayServiceClient.callCommonService(
                    "/api/v1/system/custom/execute",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Object.class
            ).getData();

            return NodeExecutionResult.success(Map.of(
                "systemType", "custom",
                "operation", "execute",
                "customOperation", customOperation,
                "result", response
            ));

        } catch (Exception e) {
            log.error("[系统执行器] 自定义操作失败: customOperation={}, error={}",
                    customOperation, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 带超时的执行
     */
    private NodeExecutionResult executeWithTimeout(NodeExecutionResult result, int timeoutSeconds) {
        try {
            CompletableFuture<NodeExecutionResult> future = CompletableFuture.completedFuture(result);
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("[系统执行器] 执行超时: timeout={}s", timeoutSeconds);
            return NodeExecutionResult.timeout("执行超时: " + timeoutSeconds + "秒");
        } catch (Exception e) {
            log.error("[系统执行器] 超时处理异常: error={}", e.getMessage(), e);
            return NodeExecutionResult.failure("超时处理异常: " + e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "system";
    }

    @Override
    public String getDescription() {
        return "系统执行器，支持HTTP调用、数据库操作、文件操作、通知发送、系统集成、批量处理等";
    }
}
