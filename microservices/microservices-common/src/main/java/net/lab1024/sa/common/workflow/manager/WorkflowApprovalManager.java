package net.lab1024.sa.common.workflow.manager;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import org.springframework.http.HttpMethod;

/**
 * 工作流审批Manager
 * <p>
 * 提供通用的工作流审批功能，供各个业务模块使用
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 启动审批流程
 * - 处理审批任务（同意/驳回/转办/委派）
 * - 查询审批状态
 * - 查询待办/已办任务
 * - 撤销审批流程
 * </p>
 * <p>
 * 企业级特性：
 * - 统一的审批接口，简化业务模块集成
 * - 完整的异常处理和日志记录
 * - 支持多种业务类型（报销、请假、出差等）
 * - 灵活的任务操作（同意、驳回、转办、委派）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class WorkflowApprovalManager {

    private final GatewayServiceClient gatewayServiceClient;
    private final ApprovalConfigManager approvalConfigManager;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     * @param approvalConfigManager 审批配置管理器（可选，如果为null则使用硬编码配置）
     */
    public WorkflowApprovalManager(GatewayServiceClient gatewayServiceClient, ApprovalConfigManager approvalConfigManager) {
        this.gatewayServiceClient = gatewayServiceClient;
        this.approvalConfigManager = approvalConfigManager;
    }

    /**
     * 构造函数注入依赖（兼容旧版本，approvalConfigManager为null）
     * <p>
     * 如果approvalConfigManager为null，则使用硬编码的流程定义ID
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     */
    public WorkflowApprovalManager(GatewayServiceClient gatewayServiceClient) {
        this(gatewayServiceClient, null);
    }

    /**
     * 启动审批流程
     * <p>
     * 供各个业务模块调用，启动对应的审批流程
     * 支持动态配置：如果definitionId为null，则从审批配置中获取
     * </p>
     *
     * @param definitionId 流程定义ID（如果为null，则从审批配置中获取）
     * @param businessKey 业务Key（业务模块的唯一标识）
     * @param instanceName 流程实例名称
     * @param initiatorId 发起人ID
     * @param businessType 业务类型（如：CONSUME_REFUND、ATTENDANCE_LEAVE、CUSTOM_APPROVAL_001等，支持自定义）
     * @param formData 表单数据（业务模块的业务数据）
     * @param variables 流程变量（可选）
     * @return 流程实例ID
     */
    public ResponseDTO<Long> startApprovalProcess(
            Long definitionId,
            String businessKey,
            String instanceName,
            Long initiatorId,
            String businessType,
            Map<String, Object> formData,
            Map<String, Object> variables) {
        log.info("启动审批流程，业务类型: {}, 业务Key: {}, 发起人ID: {}", businessType, businessKey, initiatorId);
        try {
            // 如果definitionId为null，尝试从审批配置中获取
            if (definitionId == null && approvalConfigManager != null) {
                Long configDefinitionId = approvalConfigManager.getDefinitionId(businessType);
                if (configDefinitionId != null) {
                    definitionId = configDefinitionId;
                    log.info("从审批配置中获取流程定义ID，业务类型: {}, definitionId: {}", businessType, definitionId);
                } else {
                    log.warn("未找到审批配置，业务类型: {}，请检查配置或使用硬编码的流程定义ID", businessType);
                    return ResponseDTO.error("未找到业务类型对应的审批配置: " + businessType);
                }
            }

            if (definitionId == null) {
                log.error("流程定义ID为空，无法启动审批流程，业务类型: {}", businessType);
                return ResponseDTO.error("流程定义ID为空，无法启动审批流程");
            }
            // 构建流程变量
            Map<String, Object> flowVariables = new HashMap<>();
            if (variables != null) {
                flowVariables.putAll(variables);
            }
            flowVariables.put("initiatorId", initiatorId);
            flowVariables.put("businessType", businessType);
            flowVariables.put("businessKey", businessKey);

            // 构建请求参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("definitionId", definitionId);
            requestParams.put("businessKey", businessKey);
            requestParams.put("instanceName", instanceName);
            requestParams.put("variables", flowVariables);
            requestParams.put("formData", formData);

            // 调用OA服务的工作流API
            ResponseDTO<Long> response = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/instance/start",
                    HttpMethod.POST,
                    requestParams,
                    Long.class
            );

            if (response != null && response.isSuccess()) {
                log.info("审批流程启动成功，流程实例ID: {}, 业务类型: {}", response.getData(), businessType);
            } else {
                log.error("审批流程启动失败，业务类型: {}, 错误: {}", businessType,
                        response != null ? response.getMessage() : "响应为空");
            }

            return response;
        } catch (Exception e) {
            log.error("启动审批流程异常，业务类型: {}, 业务Key: {}", businessType, businessKey, e);
            return ResponseDTO.error("启动审批流程失败: " + e.getMessage());
        }
    }

    /**
     * 完成任务（同意）
     * <p>
     * 审批人同意审批任务，流程继续推进
     * </p>
     *
     * @param taskId 任务ID
     * @param comment 审批意见
     * @param variables 流程变量（可选）
     * @param formData 表单数据（可选）
     * @return 操作结果
     */
    public ResponseDTO<String> approveTask(
            Long taskId,
            String comment,
            Map<String, Object> variables,
            Map<String, Object> formData) {
        log.info("同意审批任务，任务ID: {}", taskId);
        try {
            // 构建请求参数
            Map<String, Object> requestParams = new HashMap<>();
            if (variables != null) {
                requestParams.putAll(variables);
            }
            if (formData != null) {
                requestParams.put("formData", formData);
            }

            // 构建请求路径（URL编码处理）
            String path = "/api/v1/workflow/engine/task/" + taskId + "/complete";
            String queryParams = "outcome=同意";
            if (comment != null && !comment.isEmpty()) {
                try {
                    queryParams += "&comment=" + java.net.URLEncoder.encode(comment, "UTF-8");
                } catch (Exception e) {
                    log.warn("URL编码失败，使用原始注释: {}", comment);
                    queryParams += "&comment=" + comment;
                }
            }
            
            // 调用OA服务的工作流API
            ResponseDTO<String> response = gatewayServiceClient.callOAService(
                    path + "?" + queryParams,
                    HttpMethod.POST,
                    requestParams,
                    String.class
            );

            if (response != null && response.isSuccess()) {
                log.info("审批任务同意成功，任务ID: {}", taskId);
            } else {
                log.error("审批任务同意失败，任务ID: {}, 错误: {}", taskId,
                        response != null ? response.getMessage() : "响应为空");
            }

            return response;
        } catch (Exception e) {
            log.error("同意审批任务异常，任务ID: {}", taskId, e);
            return ResponseDTO.error("同意审批任务失败: " + e.getMessage());
        }
    }

    /**
     * 驳回任务
     * <p>
     * 审批人驳回审批任务，流程结束或回退
     * </p>
     *
     * @param taskId 任务ID
     * @param comment 驳回意见（必填）
     * @param variables 流程变量（可选）
     * @return 操作结果
     */
    public ResponseDTO<String> rejectTask(
            Long taskId,
            String comment,
            Map<String, Object> variables) {
        log.info("驳回审批任务，任务ID: {}, 意见: {}", taskId, comment);
        try {
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseDTO.error("REJECT_COMMENT_REQUIRED", "驳回意见不能为空");
            }

            // 构建请求参数
            Map<String, Object> requestParams = variables != null ? new HashMap<>(variables) : new HashMap<>();

            // 构建请求路径（URL编码处理）
            String path = "/api/v1/workflow/engine/task/" + taskId + "/reject";
            String encodedComment;
            try {
                encodedComment = java.net.URLEncoder.encode(comment, "UTF-8");
            } catch (Exception e) {
                log.warn("URL编码失败，使用原始注释: {}", comment);
                encodedComment = comment;
            }
            
            // 调用OA服务的工作流API
            ResponseDTO<String> response = gatewayServiceClient.callOAService(
                    path + "?comment=" + encodedComment,
                    HttpMethod.POST,
                    requestParams,
                    String.class
            );

            if (response != null && response.isSuccess()) {
                log.info("审批任务驳回成功，任务ID: {}", taskId);
            } else {
                log.error("审批任务驳回失败，任务ID: {}, 错误: {}", taskId,
                        response != null ? response.getMessage() : "响应为空");
            }

            return response;
        } catch (Exception e) {
            log.error("驳回审批任务异常，任务ID: {}", taskId, e);
            return ResponseDTO.error("驳回审批任务失败: " + e.getMessage());
        }
    }

    /**
     * 转办任务
     * <p>
     * 将任务转交给其他用户处理
     * </p>
     *
     * @param taskId 任务ID
     * @param targetUserId 目标用户ID
     * @return 操作结果
     */
    public ResponseDTO<String> transferTask(Long taskId, Long targetUserId) {
        log.info("转办审批任务，任务ID: {}, 目标用户ID: {}", taskId, targetUserId);
        try {
            // 调用OA服务的工作流API
            ResponseDTO<String> response = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/task/" + taskId + "/transfer?targetUserId=" + targetUserId,
                    HttpMethod.PUT,
                    null,
                    String.class
            );

            if (response != null && response.isSuccess()) {
                log.info("审批任务转办成功，任务ID: {}, 目标用户ID: {}", taskId, targetUserId);
            } else {
                log.error("审批任务转办失败，任务ID: {}, 错误: {}", taskId,
                        response != null ? response.getMessage() : "响应为空");
            }

            return response;
        } catch (Exception e) {
            log.error("转办审批任务异常，任务ID: {}", taskId, e);
            return ResponseDTO.error("转办审批任务失败: " + e.getMessage());
        }
    }

    /**
     * 委派任务
     * <p>
     * 将任务委派给其他用户处理，委派后原用户仍可查看任务
     * </p>
     *
     * @param taskId 任务ID
     * @param targetUserId 目标用户ID
     * @return 操作结果
     */
    public ResponseDTO<String> delegateTask(Long taskId, Long targetUserId) {
        log.info("委派审批任务，任务ID: {}, 目标用户ID: {}", taskId, targetUserId);
        try {
            // 调用OA服务的工作流API
            ResponseDTO<String> response = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/task/" + taskId + "/delegate?targetUserId=" + targetUserId,
                    HttpMethod.PUT,
                    null,
                    String.class
            );

            if (response != null && response.isSuccess()) {
                log.info("审批任务委派成功，任务ID: {}, 目标用户ID: {}", taskId, targetUserId);
            } else {
                log.error("审批任务委派失败，任务ID: {}, 错误: {}", taskId,
                        response != null ? response.getMessage() : "响应为空");
            }

            return response;
        } catch (Exception e) {
            log.error("委派审批任务异常，任务ID: {}", taskId, e);
            return ResponseDTO.error("委派审批任务失败: " + e.getMessage());
        }
    }

    /**
     * 受理任务
     * <p>
     * 受理待办任务，将任务分配给当前用户
     * </p>
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 操作结果
     */
    public ResponseDTO<String> claimTask(Long taskId, Long userId) {
        log.info("受理审批任务，任务ID: {}, 用户ID: {}", taskId, userId);
        try {
            // 调用OA服务的工作流API
            ResponseDTO<String> response = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/task/" + taskId + "/claim",
                    HttpMethod.PUT,
                    null,
                    String.class
            );

            if (response != null && response.isSuccess()) {
                log.info("审批任务受理成功，任务ID: {}, 用户ID: {}", taskId, userId);
            } else {
                log.error("审批任务受理失败，任务ID: {}, 错误: {}", taskId,
                        response != null ? response.getMessage() : "响应为空");
            }

            return response;
        } catch (Exception e) {
            log.error("受理审批任务异常，任务ID: {}", taskId, e);
            return ResponseDTO.error("受理审批任务失败: " + e.getMessage());
        }
    }

    /**
     * 撤销流程实例
     * <p>
     * 发起人撤销已启动的审批流程
     * </p>
     *
     * @param instanceId 流程实例ID
     * @param reason 撤销原因
     * @return 操作结果
     */
    public ResponseDTO<String> revokeProcess(Long instanceId, String reason) {
        log.info("撤销审批流程，实例ID: {}, 原因: {}", instanceId, reason);
        try {
            // 构建请求参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("reason", reason != null ? reason : "发起人撤销");

            // 构建请求路径（URL编码处理）
            String path = "/api/v1/workflow/engine/instance/" + instanceId + "/revoke";
            String revokeReason = reason != null ? reason : "发起人撤销";
            String encodedReason;
            try {
                encodedReason = java.net.URLEncoder.encode(revokeReason, "UTF-8");
            } catch (Exception e) {
                log.warn("URL编码失败，使用原始原因: {}", revokeReason);
                encodedReason = revokeReason;
            }
            
            // 调用OA服务的工作流API
            ResponseDTO<String> response = gatewayServiceClient.callOAService(
                    path + "?reason=" + encodedReason,
                    HttpMethod.POST,
                    null,
                    String.class
            );

            if (response != null && response.isSuccess()) {
                log.info("审批流程撤销成功，实例ID: {}", instanceId);
            } else {
                log.error("审批流程撤销失败，实例ID: {}, 错误: {}", instanceId,
                        response != null ? response.getMessage() : "响应为空");
            }

            return response;
        } catch (Exception e) {
            log.error("撤销审批流程异常，实例ID: {}", instanceId, e);
            return ResponseDTO.error("撤销审批流程失败: " + e.getMessage());
        }
    }

    /**
     * 查询流程实例状态
     * <p>
     * 查询指定流程实例的当前状态
     * </p>
     *
     * @param instanceId 流程实例ID
     * @return 流程实例信息
     */
    @SuppressWarnings("unchecked")
    public ResponseDTO<Map<String, Object>> getProcessInstanceStatus(Long instanceId) {
        log.debug("查询流程实例状态，实例ID: {}", instanceId);
        try {
            // 调用OA服务的工作流API
            @SuppressWarnings("rawtypes")
            ResponseDTO<Map> response = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/instance/" + instanceId,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            
            // 类型转换
            if (response != null && response.isSuccess()) {
                log.debug("查询流程实例状态成功，实例ID: {}", instanceId);
                return ResponseDTO.ok((Map<String, Object>) response.getData());
            } else {
                log.warn("查询流程实例状态失败，实例ID: {}, 错误: {}", instanceId,
                        response != null ? response.getMessage() : "响应为空");
                return (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) response;
            }
        } catch (Exception e) {
            log.error("查询流程实例状态异常，实例ID: {}", instanceId, e);
            return ResponseDTO.error("查询流程实例状态失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户的待办任务列表
     * <p>
     * 查询指定用户的待办审批任务
     * </p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param category 流程分类（可选）
     * @return 待办任务列表
     */
    @SuppressWarnings("unchecked")
    public ResponseDTO<Map<String, Object>> getPendingTasks(
            Long userId,
            Long pageNum,
            Long pageSize,
            String category) {
        log.debug("查询用户待办任务，用户ID: {}, 页码: {}, 每页数量: {}", userId, pageNum, pageSize);
        try {
            // 构建请求路径
            StringBuilder path = new StringBuilder("/api/v1/workflow/engine/task/my/pending");
            path.append("?pageNum=").append(pageNum != null ? pageNum : 1);
            path.append("&pageSize=").append(pageSize != null ? pageSize : 20);
            if (category != null && !category.isEmpty()) {
                path.append("&category=").append(category);
            }

            // 调用OA服务的工作流API
            @SuppressWarnings("rawtypes")
            ResponseDTO<Map> response = gatewayServiceClient.callOAService(
                    path.toString(),
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            
            // 类型转换
            if (response != null && response.isSuccess()) {
                log.debug("查询用户待办任务成功，用户ID: {}", userId);
                return ResponseDTO.ok((Map<String, Object>) response.getData());
            } else {
                log.warn("查询用户待办任务失败，用户ID: {}, 错误: {}", userId,
                        response != null ? response.getMessage() : "响应为空");
                return (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) response;
            }
        } catch (Exception e) {
            log.error("查询用户待办任务异常，用户ID: {}", userId, e);
            return ResponseDTO.error("查询用户待办任务失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户的已办任务列表
     * <p>
     * 查询指定用户已处理的审批任务
     * </p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     * @param category 流程分类（可选）
     * @return 已办任务列表
     */
    @SuppressWarnings("unchecked")
    public ResponseDTO<Map<String, Object>> getCompletedTasks(
            Long userId,
            Long pageNum,
            Long pageSize,
            String category) {
        log.debug("查询用户已办任务，用户ID: {}, 页码: {}, 每页数量: {}", userId, pageNum, pageSize);
        try {
            // 构建请求路径
            StringBuilder path = new StringBuilder("/api/v1/workflow/engine/task/my/completed");
            path.append("?pageNum=").append(pageNum != null ? pageNum : 1);
            path.append("&pageSize=").append(pageSize != null ? pageSize : 20);
            if (category != null && !category.isEmpty()) {
                path.append("&category=").append(category);
            }

            // 调用OA服务的工作流API
            @SuppressWarnings("rawtypes")
            ResponseDTO<Map> response = gatewayServiceClient.callOAService(
                    path.toString(),
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            
            // 类型转换
            if (response != null && response.isSuccess()) {
                log.debug("查询用户已办任务成功，用户ID: {}", userId);
                return ResponseDTO.ok((Map<String, Object>) response.getData());
            } else {
                log.warn("查询用户已办任务失败，用户ID: {}, 错误: {}", userId,
                        response != null ? response.getMessage() : "响应为空");
                return (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) response;
            }
        } catch (Exception e) {
            log.error("查询用户已办任务异常，用户ID: {}", userId, e);
            return ResponseDTO.error("查询用户已办任务失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务详情
     * <p>
     * 查询指定审批任务的详细信息
     * </p>
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @SuppressWarnings("unchecked")
    public ResponseDTO<Map<String, Object>> getTaskDetail(Long taskId) {
        log.debug("查询任务详情，任务ID: {}", taskId);
        try {
            // 调用OA服务的工作流API
            @SuppressWarnings("rawtypes")
            ResponseDTO<Map> response = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/task/" + taskId,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            
            // 类型转换
            if (response != null && response.isSuccess()) {
                log.debug("查询任务详情成功，任务ID: {}", taskId);
                return ResponseDTO.ok((Map<String, Object>) response.getData());
            } else {
                log.warn("查询任务详情失败，任务ID: {}, 错误: {}", taskId,
                        response != null ? response.getMessage() : "响应为空");
                return (ResponseDTO<Map<String, Object>>) (ResponseDTO<?>) response;
            }
        } catch (Exception e) {
            log.error("查询任务详情异常，任务ID: {}", taskId, e);
            return ResponseDTO.error("查询任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 查询流程历史记录
     * <p>
     * 查询指定流程实例的审批历史记录
     * </p>
     *
     * @param instanceId 流程实例ID
     * @return 历史记录列表
     */
    @SuppressWarnings("unchecked")
    public ResponseDTO<java.util.List<Map<String, Object>>> getProcessHistory(Long instanceId) {
        log.debug("查询流程历史记录，实例ID: {}", instanceId);
        try {
            // 调用OA服务的工作流API
            @SuppressWarnings("rawtypes")
            ResponseDTO<java.util.List> response = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/instance/" + instanceId + "/history",
                    HttpMethod.GET,
                    null,
                    java.util.List.class
            );
            
            // 类型转换
            if (response != null && response.isSuccess()) {
                log.debug("查询流程历史记录成功，实例ID: {}", instanceId);
                return ResponseDTO.ok((java.util.List<Map<String, Object>>) response.getData());
            } else {
                log.warn("查询流程历史记录失败，实例ID: {}, 错误: {}", instanceId,
                        response != null ? response.getMessage() : "响应为空");
                return (ResponseDTO<java.util.List<Map<String, Object>>>) (ResponseDTO<?>) response;
            }
        } catch (Exception e) {
            log.error("查询流程历史记录异常，实例ID: {}", instanceId, e);
            return ResponseDTO.error("查询流程历史记录失败: " + e.getMessage());
        }
    }
}

