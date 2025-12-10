package net.lab1024.sa.common.workflow.executor.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.workflow.executor.NodeExecutionContext;
import net.lab1024.sa.common.workflow.executor.NodeExecutionResult;
import net.lab1024.sa.common.workflow.executor.NodeExecutor;
import net.lab1024.sa.common.workflow.manager.ExpressionEngineManager;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * 条件执行器
 * <p>
 * 企业级条件节点执行器，支持复杂条件表达式、业务规则判断
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class ConditionExecutor implements NodeExecutor {

    private final ExpressionEngineManager expressionEngineManager;
    private final GatewayServiceClient gatewayServiceClient;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param expressionEngineManager 表达式引擎管理器
     * @param gatewayServiceClient 网关服务客户端
     */
    public ConditionExecutor(ExpressionEngineManager expressionEngineManager, GatewayServiceClient gatewayServiceClient) {
        this.expressionEngineManager = expressionEngineManager;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) throws Exception {
        log.info("[条件执行器] 开始执行条件节点: instanceId={}, nodeId={}, nodeName={}",
                context.getInstanceId(), context.getNodeId(), context.getNodeName());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取条件配置
            Map<String, Object> nodeConfig = context.getNodeConfig();
            if (nodeConfig == null) {
                return NodeExecutionResult.failure("条件节点配置为空");
            }

            String conditionType = (String) nodeConfig.get("conditionType");
            String conditionExpression = (String) nodeConfig.get("conditionExpression");
            String conditionRule = (String) nodeConfig.get("conditionRule");
            Object conditionValue = nodeConfig.get("conditionValue");
            String trueBranch = (String) nodeConfig.get("trueBranch");
            String falseBranch = (String) nodeConfig.get("falseBranch");
            boolean defaultResult = (Boolean) nodeConfig.getOrDefault("defaultResult", false);

            log.debug("[条件执行器] 条件配置: type={}, expression={}, rule={}, value={}, default={}",
                    conditionType, conditionExpression, conditionRule, conditionValue, defaultResult);

            // 2. 根据条件类型执行
            boolean conditionResult;
            switch (conditionType != null ? conditionType.toLowerCase() : "expression") {
                case "expression":
                    conditionResult = evaluateExpression(context, conditionExpression);
                    break;
                case "rule":
                    conditionResult = evaluateRule(context, conditionRule, conditionValue);
                    break;
                case "comparison":
                    conditionResult = evaluateComparison(context, nodeConfig);
                    break;
                case "business":
                    conditionResult = evaluateBusinessCondition(context, conditionRule, conditionValue);
                    break;
                case "custom":
                    conditionResult = evaluateCustomCondition(context, nodeConfig);
                    break;
                default:
                    conditionResult = defaultResult;
                    break;
            }

            // 3. 构建执行结果
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("conditionType", conditionType);
            outputData.put("conditionResult", conditionResult);
            outputData.put("conditionExpression", conditionExpression);
            outputData.put("conditionRule", conditionRule);
            outputData.put("conditionValue", conditionValue);
            outputData.put("trueBranch", trueBranch);
            outputData.put("falseBranch", falseBranch);
            outputData.put("executionData", context.getExecutionData());

            // 4. 根据条件结果选择分支
            String selectedBranch = conditionResult ? trueBranch : falseBranch;
            if (selectedBranch != null && !selectedBranch.trim().isEmpty()) {
                outputData.put("selectedBranch", selectedBranch);
                outputData.put("branchDirection", conditionResult ? "true" : "false");
            }

            // 5. 记录执行结果
            NodeExecutionResult result = NodeExecutionResult.success(outputData);
            result.setStartTime(LocalDateTime.now())
               .setEndTime(LocalDateTime.now())
               .calculateDuration();

            long duration = System.currentTimeMillis() - startTime;
            log.info("[条件执行器] 条件节点执行完成: instanceId={}, nodeId={}, result={}, duration={}ms, branch={}",
                    context.getInstanceId(), context.getNodeId(), conditionResult, duration, selectedBranch);

            return result;

        } catch (Exception e) {
            log.error("[条件执行器] 条件节点执行异常: instanceId={}, nodeId={}, error={}",
                    context.getInstanceId(), context.getNodeId(), e.getMessage(), e);
            return NodeExecutionResult.failure("条件执行异常: " + e.getMessage());
        }
    }

    /**
     * 表达式条件评估
     */
    private boolean evaluateExpression(NodeExecutionContext context, String expression) {
        log.debug("[条件执行器] 评估表达式条件: expression={}", expression);

        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        try {
            // 使用ExpressionEngineManager执行表达式
            // 添加执行数据到表达式上下文
            Map<String, Object> exprContext = new HashMap<>();
            if (context.getExecutionData() != null) {
                exprContext.putAll(context.getExecutionData());
            }
            exprContext.put("instanceId", context.getInstanceId());
            exprContext.put("nodeId", context.getNodeId());
            exprContext.put("userId", context.getUserId());
            exprContext.put("tenantId", context.getTenantId());

            // 使用ExpressionEngineManager执行布尔表达式
            // ExpressionEngineManager已经注册了所有自定义函数（IsEmptyFunction, IsNotEmptyFunction, ContainsFunction等）
            boolean result = expressionEngineManager.executeBooleanExpression(expression, exprContext);
            log.debug("[条件执行器] 表达式评估结果: expression={}, result={}", expression, result);

            return result;

        } catch (Exception e) {
            log.error("[条件执行器] 表达式评估异常: expression={}, error={}", expression, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 规则条件评估
     */
    private boolean evaluateRule(NodeExecutionContext context, String rule, Object value) {
        log.debug("[条件执行器] 评估规则条件: rule={}, value={}", rule, value);

        if (rule == null || rule.trim().isEmpty()) {
            return false;
        }

        try {
            // 解析规则：operator.value 或 field.operator.value
            String[] ruleParts = rule.split("\\.");
            if (ruleParts.length != 3) {
                log.warn("[条件执行器] 规则格式不正确，使用默认结果: rule={}", rule);
                return false;
            }

            String field = ruleParts[0];
            String operator = ruleParts[1];
            String expectedValue = ruleParts[2];

            // 从执行数据中获取字段值
            Object actualValue = getFieldValue(context, field);
            if (actualValue == null) {
                log.debug("[条件执行器] 字段值为空: field={}", field);
                return false;
            }

            // 执行比较操作
            return performComparison(actualValue, operator, expectedValue);

        } catch (Exception e) {
            log.error("[条件执行器] 规则评估异常: rule={}, error={}", rule, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 比较条件评估
     */
    private boolean evaluateComparison(NodeExecutionContext context, Map<String, Object> nodeConfig) {
        log.debug("[条件执行器] 评估比较条件");

        String leftOperand = (String) nodeConfig.get("leftOperand");
        String operator = (String) nodeConfig.get("operator");
        String rightOperand = (String) nodeConfig.get("rightOperand");

        if (leftOperand == null || rightOperand == null || operator == null) {
            return false;
        }

        try {
            Object leftValue = resolveOperand(context, leftOperand);
            Object rightValue = resolveOperand(context, rightOperand);

            return performComparison(leftValue, operator, rightValue);

        } catch (Exception e) {
            log.error("[条件执行器] 比较条件异常: left={}, op={}, right={}, error={}",
                    leftOperand, operator, rightOperand, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 业务条件评估
     */
    private boolean evaluateBusinessCondition(NodeExecutionContext context, String businessRule, Object conditionValue) {
        log.debug("[条件执行器] 评估业务条件: rule={}, value={}", businessRule, conditionValue);

        try {
            // 根据业务规则调用相应的业务服务
            switch (businessRule) {
                case "user_permission":
                    return evaluateUserPermission(context, conditionValue);
                case "device_status":
                    return evaluateDeviceStatus(context, conditionValue);
                case "area_access":
                    return evaluateAreaAccess(context, conditionValue);
                case "time_range":
                    return evaluateTimeRange(context, conditionValue);
                case "amount_limit":
                    return evaluateAmountLimit(context, conditionValue);
                default:
                    log.warn("[条件执行器] 未知的业务规则: {}", businessRule);
                    return false;
            }

        } catch (Exception e) {
            log.error("[条件执行器] 业务条件评估异常: rule={}, error={}", businessRule, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 自定义条件评估
     * <p>
     * 支持两种自定义条件评估方式：
     * 1. 表达式方式：customEvaluator为表达式字符串，通过表达式引擎执行
     * 2. 函数方式：customEvaluator为函数名，通过表达式引擎调用注册的自定义函数
     * </p>
     *
     * @param context 节点执行上下文
     * @param nodeConfig 节点配置
     * @return 评估结果
     */
    private boolean evaluateCustomCondition(NodeExecutionContext context, Map<String, Object> nodeConfig) {
        log.debug("[条件执行器] 评估自定义条件");

        try {
            String customEvaluator = (String) nodeConfig.get("customEvaluator");
            @SuppressWarnings("unchecked")
            Map<String, Object> customParams = (Map<String, Object>) nodeConfig.get("customParams");

            if (customEvaluator == null || customEvaluator.trim().isEmpty()) {
                log.warn("[条件执行器] 自定义评估器名称为空");
                return false;
            }

            // 构建表达式上下文
            Map<String, Object> exprContext = new HashMap<>();
            if (context.getExecutionData() != null) {
                exprContext.putAll(context.getExecutionData());
            }
            exprContext.put("instanceId", context.getInstanceId());
            exprContext.put("nodeId", context.getNodeId());
            exprContext.put("userId", context.getUserId());
            exprContext.put("tenantId", context.getTenantId());

            // 添加自定义参数到上下文
            if (customParams != null) {
                exprContext.putAll(customParams);
            }

            // 调用自定义条件评估器
            // 如果customEvaluator是表达式，直接执行
            // 如果是函数名，则调用对应的函数
            boolean result = expressionEngineManager.executeBooleanExpression(customEvaluator, exprContext);

            log.info("[条件执行器] 自定义条件评估完成: evaluator={}, params={}, result={}",
                    customEvaluator, customParams, result);
            return result;

        } catch (Exception e) {
            log.error("[条件执行器] 自定义条件评估异常: evaluator={}, error={}",
                    nodeConfig.get("customEvaluator"), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取字段值
     */
    private Object getFieldValue(NodeExecutionContext context, String fieldPath) {
        if (fieldPath == null || fieldPath.trim().isEmpty()) {
            return null;
        }

        try {
            // 首先从执行数据中查找
            if (context.getExecutionData() != null) {
                Object value = context.getExecutionData().get(fieldPath);
                if (value != null) {
                    return value;
                }
            }

            // 支持嵌套字段访问：user.profile.name
            String[] fieldParts = fieldPath.split("\\.");
            Object current = context.getExecutionData();

            for (String part : fieldParts) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                } else {
                    return null;
                }

                if (current == null) {
                    break;
                }
            }

            return current;

        } catch (Exception e) {
            log.error("[条件执行器] 获取字段值异常: fieldPath={}, error={}", fieldPath, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析操作数
     */
    private Object resolveOperand(NodeExecutionContext context, String operand) {
        if (operand == null || operand.trim().isEmpty()) {
            return null;
        }

        // 检查是否是字段引用
        if (operand.startsWith("$") || operand.startsWith("data.")) {
            String fieldPath = operand.startsWith("$") ? operand.substring(1) : operand.substring(5);
            return getFieldValue(context, fieldPath);
        }

        // 检查是否是字符串常量
        if (operand.startsWith("\"") && operand.endsWith("\"")) {
            return operand.substring(1, operand.length() - 1);
        }

        // 检查是否是数字常量
        try {
            if (operand.contains(".")) {
                return Double.parseDouble(operand);
            } else {
                return Long.parseLong(operand);
            }
        } catch (NumberFormatException e) {
            // 不是数字，继续检查其他类型
        }

        // 检查是否是布尔常量
        if ("true".equalsIgnoreCase(operand)) {
            return true;
        }
        if ("false".equalsIgnoreCase(operand)) {
            return false;
        }

        // 默认返回字符串
        return operand;
    }

    /**
     * 执行比较操作
     */
    private boolean performComparison(Object leftValue, String operator, Object rightValue) {
        try {
            if (leftValue == null && rightValue == null) {
                return "equals".equals(operator);
            }

            if (leftValue == null || rightValue == null) {
                return "not_equals".equals(operator);
            }

            switch (operator) {
                case "equals":
                return leftValue.equals(rightValue);
                case "not_equals":
                    return !leftValue.equals(rightValue);
                case "greater":
                    return compareNumbers(leftValue, rightValue) > 0;
                case "less":
                    return compareNumbers(leftValue, rightValue) < 0;
                case "greater_equal":
                    return compareNumbers(leftValue, rightValue) >= 0;
                case "less_equal":
                    return compareNumbers(leftValue, rightValue) <= 0;
                case "contains":
                    return leftValue.toString().contains(rightValue.toString());
                case "not_contains":
                    return !leftValue.toString().contains(rightValue.toString());
                case "starts_with":
                    return leftValue.toString().startsWith(rightValue.toString());
                case "ends_with":
                    return leftValue.toString().endsWith(rightValue.toString());
                case "matches":
                    return leftValue.toString().matches(rightValue.toString());
                case "in":
                    if (rightValue instanceof List) {
                        return ((List<?>) rightValue).contains(leftValue);
                    }
                    return false;
                case "not_in":
                    if (rightValue instanceof List) {
                        return !((List<?>) rightValue).contains(leftValue);
                    }
                    return false;
                default:
                    log.warn("[条件执行器] 未知操作符: {}", operator);
                    return false;
            }

        } catch (Exception e) {
            log.error("[条件执行器] 比较操作异常: left={}, op={}, right={}, error={}",
                    leftValue, operator, rightValue, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 比较数字
     */
    private int compareNumbers(Object left, Object right) {
        double leftNum = left instanceof Number ? ((Number) left).doubleValue() : Double.parseDouble(left.toString());
        double rightNum = right instanceof Number ? ((Number) right).doubleValue() : Double.parseDouble(right.toString());
        return Double.compare(leftNum, rightNum);
    }

    /**
     * 用户权限条件评估
     * <p>
     * 通过GatewayServiceClient调用公共服务检查用户权限
     * 严格遵循CLAUDE.md规范：所有服务间调用必须通过API网关
     * </p>
     *
     * @param context 节点执行上下文
     * @param conditionValue 条件值（权限标识）
     * @return 是否有权限
     */
    private boolean evaluateUserPermission(NodeExecutionContext context, Object conditionValue) {
        try {
            Long userId = context.getUserId();
            String permission = conditionValue != null ? conditionValue.toString() : null;

            if (userId == null || permission == null || permission.trim().isEmpty()) {
                log.warn("[条件执行器] 用户权限检查参数不完整: userId={}, permission={}", userId, permission);
                return false;
            }

            log.debug("[条件执行器] 检查用户权限: userId={}, permission={}", userId, permission);

            // 调用权限服务检查用户权限
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("userId", userId);
            requestData.put("permission", permission);

            ResponseDTO<Boolean> response = gatewayServiceClient.callCommonService(
                    "/api/v1/permission/check",
                    HttpMethod.POST,
                    requestData,
                    Boolean.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                boolean hasPermission = response.getData();
                log.debug("[条件执行器] 用户权限检查结果: userId={}, permission={}, hasPermission={}",
                        userId, permission, hasPermission);
                return hasPermission;
            } else {
                log.warn("[条件执行器] 用户权限检查失败: userId={}, permission={}, message={}",
                        userId, permission, response != null ? response.getMessage() : "响应为空");
                return false;
            }

        } catch (Exception e) {
            log.error("[条件执行器] 用户权限检查异常: userId={}, permission={}, error={}",
                    context.getUserId(), conditionValue, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 设备状态条件评估
     * <p>
     * 通过GatewayServiceClient调用设备通讯服务检查设备状态
     * 严格遵循CLAUDE.md规范：所有服务间调用必须通过API网关
     * </p>
     *
     * @param context 节点执行上下文
     * @param conditionValue 条件值（期望的设备状态）
     * @return 设备状态是否匹配
     */
    private boolean evaluateDeviceStatus(NodeExecutionContext context, Object conditionValue) {
        try {
            Object deviceIdObj = context.getExecutionData() != null ?
                    context.getExecutionData().get("deviceId") : null;
            String deviceId = deviceIdObj != null ? deviceIdObj.toString() : null;
            String expectedStatus = conditionValue != null ? conditionValue.toString() : null;

            if (deviceId == null || deviceId.trim().isEmpty() || expectedStatus == null) {
                log.warn("[条件执行器] 设备状态检查参数不完整: deviceId={}, expectedStatus={}", deviceId, expectedStatus);
                return false;
            }

            log.debug("[条件执行器] 检查设备状态: deviceId={}, expectedStatus={}", deviceId, expectedStatus);

            // 调用设备通讯服务检查设备状态
            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>)
                    gatewayServiceClient.callDeviceCommService(
                            "/api/v1/device/" + deviceId + "/status",
                            HttpMethod.GET,
                            null,
                            Map.class
                    );

            if (response != null && response.isSuccess() && response.getData() != null) {
                Map<String, Object> deviceInfo = response.getData();
                String actualStatus = deviceInfo.get("status") != null ?
                        deviceInfo.get("status").toString() : null;

                boolean matches = expectedStatus.equalsIgnoreCase(actualStatus);
                log.debug("[条件执行器] 设备状态检查结果: deviceId={}, expectedStatus={}, actualStatus={}, matches={}",
                        deviceId, expectedStatus, actualStatus, matches);
                return matches;
            } else {
                log.warn("[条件执行器] 设备状态检查失败: deviceId={}, expectedStatus={}, message={}",
                        deviceId, expectedStatus, response != null ? response.getMessage() : "响应为空");
                return false;
            }

        } catch (Exception e) {
            log.error("[条件执行器] 设备状态检查异常: deviceId={}, expectedStatus={}, error={}",
                    context.getExecutionData() != null ? context.getExecutionData().get("deviceId") : null,
                    conditionValue, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 区域访问条件评估
     * <p>
     * 通过GatewayServiceClient调用公共服务检查区域访问权限
     * 严格遵循CLAUDE.md规范：所有服务间调用必须通过API网关
     * </p>
     *
     * @param context 节点执行上下文
     * @param conditionValue 条件值（所需权限标识）
     * @return 是否有区域访问权限
     */
    private boolean evaluateAreaAccess(NodeExecutionContext context, Object conditionValue) {
        try {
            Long userId = context.getUserId();
            Object areaIdObj = context.getExecutionData() != null ?
                    context.getExecutionData().get("areaId") : null;
            String areaId = areaIdObj != null ? areaIdObj.toString() : null;
            String requiredPermission = conditionValue != null ? conditionValue.toString() : null;

            if (userId == null || areaId == null || areaId.trim().isEmpty() || requiredPermission == null) {
                log.warn("[条件执行器] 区域访问权限检查参数不完整: userId={}, areaId={}, permission={}",
                        userId, areaId, requiredPermission);
                return false;
            }

            log.debug("[条件执行器] 检查区域访问权限: userId={}, areaId={}, permission={}",
                    userId, areaId, requiredPermission);

            // 调用区域权限服务检查区域访问权限
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("userId", userId);
            requestData.put("areaId", areaId);
            requestData.put("permission", requiredPermission);

            ResponseDTO<Boolean> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/check-access",
                    HttpMethod.POST,
                    requestData,
                    Boolean.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                boolean hasAccess = response.getData();
                log.debug("[条件执行器] 区域访问权限检查结果: userId={}, areaId={}, permission={}, hasAccess={}",
                        userId, areaId, requiredPermission, hasAccess);
                return hasAccess;
            } else {
                log.warn("[条件执行器] 区域访问权限检查失败: userId={}, areaId={}, permission={}, message={}",
                        userId, areaId, requiredPermission, response != null ? response.getMessage() : "响应为空");
                return false;
            }

        } catch (Exception e) {
            log.error("[条件执行器] 区域访问权限检查异常: userId={}, areaId={}, permission={}, error={}",
                    context.getUserId(),
                    context.getExecutionData() != null ? context.getExecutionData().get("areaId") : null,
                    conditionValue, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 时间范围条件评估
     * <p>
     * 支持多种时间范围判断：
     * 1. 时间段判断：检查当前时间是否在指定时间段内（如：09:00-18:00）
     * 2. 日期范围判断：检查当前日期是否在指定日期范围内
     * 3. 工作日判断：检查当前日期是否为工作日
     * </p>
     *
     * @param context 节点执行上下文
     * @param conditionValue 条件值（时间范围配置，支持JSON格式或字符串格式）
     * @return 是否在时间范围内
     */
    private boolean evaluateTimeRange(NodeExecutionContext context, Object conditionValue) {
        try {
            if (conditionValue == null) {
                log.warn("[条件执行器] 时间范围条件值为空");
                return false;
            }

            log.debug("[条件执行器] 时间范围条件评估: conditionValue={}", conditionValue);

            LocalDateTime currentTime = LocalDateTime.now();
            LocalTime currentTimeOnly = currentTime.toLocalTime();

            // 解析条件值
            Map<String, Object> timeRangeConfig = null;
            if (conditionValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) conditionValue;
                timeRangeConfig = config;
            } else if (conditionValue instanceof String) {
                // 尝试解析字符串格式：如 "09:00-18:00" 或 JSON字符串
                String configStr = (String) conditionValue;
                if (configStr.contains("-") && configStr.contains(":")) {
                    // 简单的时间段格式：09:00-18:00
                    String[] parts = configStr.split("-");
                    if (parts.length == 2) {
                        timeRangeConfig = new HashMap<>();
                        timeRangeConfig.put("startTime", parts[0].trim());
                        timeRangeConfig.put("endTime", parts[1].trim());
                    }
                }
            }

            if (timeRangeConfig == null || timeRangeConfig.isEmpty()) {
                log.warn("[条件执行器] 时间范围配置格式不正确: conditionValue={}", conditionValue);
                return false;
            }

            // 检查时间段
            if (timeRangeConfig.containsKey("startTime") && timeRangeConfig.containsKey("endTime")) {
                String startTimeStr = timeRangeConfig.get("startTime").toString();
                String endTimeStr = timeRangeConfig.get("endTime").toString();

                try {
                    LocalTime startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));

                    boolean inRange;
                    if (startTime.isBefore(endTime)) {
                        // 正常时间段：09:00-18:00
                        inRange = !currentTimeOnly.isBefore(startTime) && !currentTimeOnly.isAfter(endTime);
                    } else {
                        // 跨天时间段：22:00-06:00
                        inRange = !currentTimeOnly.isBefore(startTime) || !currentTimeOnly.isAfter(endTime);
                    }

                    log.debug("[条件执行器] 时间范围检查结果: currentTime={}, startTime={}, endTime={}, inRange={}",
                            currentTimeOnly, startTime, endTime, inRange);
                    return inRange;

                } catch (Exception e) {
                    log.error("[条件执行器] 时间解析失败: startTime={}, endTime={}, error={}",
                            startTimeStr, endTimeStr, e.getMessage());
                    return false;
                }
            }

            // 检查日期范围
            if (timeRangeConfig.containsKey("startDate") && timeRangeConfig.containsKey("endDate")) {
                try {
                    String startDateStr = timeRangeConfig.get("startDate").toString();
                    String endDateStr = timeRangeConfig.get("endDate").toString();

                    LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate currentDate = currentTime.toLocalDate();

                    boolean inDateRange = !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate);

                    log.debug("[条件执行器] 日期范围检查结果: currentDate={}, startDate={}, endDate={}, inDateRange={}",
                            currentDate, startDate, endDate, inDateRange);
                    return inDateRange;

                } catch (Exception e) {
                    log.error("[条件执行器] 日期解析失败: startDate={}, endDate={}, error={}",
                            timeRangeConfig.get("startDate"), timeRangeConfig.get("endDate"), e.getMessage());
                    return false;
                }
            }

            // 检查工作日
            if (timeRangeConfig.containsKey("workdayOnly")) {
                boolean workdayOnly = Boolean.parseBoolean(timeRangeConfig.get("workdayOnly").toString());
                if (workdayOnly) {
                    // 检查是否为工作日（周一到周五）
                    int dayOfWeek = currentTime.getDayOfWeek().getValue();
                    boolean isWorkday = dayOfWeek >= 1 && dayOfWeek <= 5;
                    log.debug("[条件执行器] 工作日检查结果: currentDate={}, dayOfWeek={}, isWorkday={}",
                            currentTime.toLocalDate(), dayOfWeek, isWorkday);
                    return isWorkday;
                }
            }

            log.warn("[条件执行器] 未识别的时间范围配置: conditionValue={}", conditionValue);
            return false;

        } catch (Exception e) {
            log.error("[条件执行器] 时间范围条件评估异常: conditionValue={}, error={}",
                    conditionValue, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 金额限制条件评估
     */
    private boolean evaluateAmountLimit(NodeExecutionContext context, Object conditionValue) {
        try {
            Object amountObj = context.getExecutionData().get("amount");
            if (amountObj == null || conditionValue == null) {
                return false;
            }

            double amount = amountObj instanceof Number ? ((Number) amountObj).doubleValue() : Double.parseDouble(amountObj.toString());
            double limit = conditionValue instanceof Number ? ((Number) conditionValue).doubleValue() : Double.parseDouble(conditionValue.toString());

            return amount <= limit;

        } catch (Exception e) {
            log.error("[条件执行器] 金额限制条件评估异常: error={}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getType() {
        return "condition";
    }

    @Override
    public String getDescription() {
        return "条件执行器，支持表达式评估、业务规则判断、条件分支控制";
    }
}
