package net.lab1024.sa.common.notification.manager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.identity.domain.vo.UserDetailVO;
import net.lab1024.sa.common.monitor.domain.entity.NotificationEntity;

/**
 * WebSocket通知管理器
 * <p>
 * 负责WebSocket实时通知的发送和管理
 * 严格遵循CLAUDE.md规范:
 * - 使用@Component注解标识
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 企业级特性：
 * - WebSocket实时推送
 * - 连接管理
 * - 消息队列缓冲
 * - 支持点对点和广播
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class WebSocketNotificationManager {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 发送WebSocket通知
     * <p>
     * 通过WebSocket实时推送到客户端
     * 支持点对点推送和广播
     * </p>
     *
     * @param notification 通知实体
     * @return 发送是否成功
     */
    @Async("notificationExecutor")
    public boolean sendWebSocket(NotificationEntity notification) {
        log.info("发送WebSocket通知，通知ID：{}，接收人：{}，标题：{}",
                notification.getNotificationId(), notification.getReceiverIds(), notification.getTitle());

        try {
            // 检查WebSocket是否可用
            if (messagingTemplate == null) {
                log.warn("WebSocket消息模板未配置，通知ID：{}", notification.getNotificationId());
                return false;
            }

            // 1. 获取接收人用户ID列表
            List<String> userIdList = parseReceiverUserIds(notification);

            // 2. 构建WebSocket消息
            Map<String, Object> message = buildWebSocketMessage(notification);

            // 3. 发送WebSocket消息
            if (message == null) {
                log.warn("WebSocket消息构建失败，通知ID：{}", notification.getNotificationId());
                return false;
            }
            
            if (userIdList.isEmpty()) {
                // 广播消息（发送给所有连接的客户端）
                messagingTemplate.convertAndSend("/topic/notifications", (Object) message);
                log.debug("WebSocket广播消息发送成功，通知ID：{}", notification.getNotificationId());
            } else {
                // 点对点推送（发送给指定用户）
                for (String userId : userIdList) {
                    if (userId != null && !userId.isEmpty()) {
                        messagingTemplate.convertAndSendToUser(userId, "/notifications", (Object) message);
                    log.debug("WebSocket点对点消息发送成功，用户ID：{}，通知ID：{}", userId, notification.getNotificationId());
                    }
                }
            }

            log.info("WebSocket通知发送成功，通知ID：{}，接收人数量：{}", notification.getNotificationId(), userIdList.size());
            return true;

        } catch (Exception e) {
            log.error("发送WebSocket通知失败，通知ID：{}", notification.getNotificationId(), e);
            return false;
        }
    }

    /**
     * 构建WebSocket消息
     * <p>
     * 根据通知内容构建WebSocket消息格式
     * </p>
     *
     * @param notification 通知实体
     * @return WebSocket消息Map
     */
    private Map<String, Object> buildWebSocketMessage(NotificationEntity notification) {
        Map<String, Object> message = new HashMap<>();
        if (notification.getNotificationId() != null) {
            message.put("notificationId", notification.getNotificationId());
        }
        if (notification.getTitle() != null) {
            message.put("title", notification.getTitle());
        }
        if (notification.getContent() != null) {
            message.put("content", notification.getContent());
        }
        if (notification.getNotificationType() != null) {
            message.put("notificationType", notification.getNotificationType());
        }
        if (notification.getPriority() != null) {
            message.put("priority", notification.getPriority());
        }
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * 解析接收人用户ID列表
     * <p>
     * 根据接收人类型（用户ID/角色/部门）查询对应的用户ID
     * 接收人类型定义：
     * - 1: 指定用户（receiverIds为用户ID列表）
     * - 2: 角色（receiverIds为角色ID列表）
     * - 3: 部门（receiverIds为部门ID列表）
     * </p>
     *
     * @param notification 通知实体
     * @return 接收人用户ID列表
     */
    private List<String> parseReceiverUserIds(NotificationEntity notification) {
        if (notification == null) {
            return List.of();
        }

        String receiverIds = notification.getReceiverIds();
        Integer receiverType = notification.getReceiverType();

        if (receiverIds == null || receiverIds.trim().isEmpty()) {
            return List.of();
        }

        // 根据接收人类型查询对应的用户ID列表
        List<Long> userIdList;
        if (receiverType == null || receiverType == 1) {
            // 1-指定用户：直接解析用户ID列表
            userIdList = Arrays.stream(receiverIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("[WebSocket通知] 用户ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .toList();
        } else if (receiverType == 2) {
            // 2-角色：查询角色下的所有用户ID
            userIdList = queryUserIdsByRoleIds(receiverIds);
        } else if (receiverType == 3) {
            // 3-部门：查询部门下的所有用户ID
            userIdList = queryUserIdsByDepartmentIds(receiverIds);
        } else {
            log.warn("[WebSocket通知] 未知的接收人类型：{}，使用默认处理（指定用户）", receiverType);
            userIdList = Arrays.stream(receiverIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("[WebSocket通知] 用户ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .toList();
        }

        // 将用户ID转换为字符串列表
        return userIdList.stream()
                .map(String::valueOf)
                .toList();
    }

    /**
     * 根据角色ID列表查询用户ID列表
     * <p>
     * 通过GatewayServiceClient调用公共服务获取角色下的所有用户
     * 参考竞品实现：钉钉、企业微信等均支持按角色批量查询用户
     * </p>
     *
     * @param roleIds 角色ID列表（逗号分隔）
     * @return 用户ID列表
     */
    private List<Long> queryUserIdsByRoleIds(String roleIds) {
        if (roleIds == null || roleIds.trim().isEmpty()) {
            return List.of();
        }

        try {
            // 解析角色ID列表
            List<Long> roleIdList = Arrays.stream(roleIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("[WebSocket通知] 角色ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            if (roleIdList.isEmpty()) {
        return List.of();
            }

            // 通过GatewayServiceClient调用公共服务获取角色下的所有用户
            List<Long> userIdList = new java.util.ArrayList<>();
            for (Long roleId : roleIdList) {
                try {
                    // 调用公共服务：GET /api/v1/roles/{roleId}/users
                    ResponseDTO<List<UserDetailVO>> response = gatewayServiceClient.callCommonService(
                            "/api/v1/roles/" + roleId + "/users",
                            HttpMethod.GET,
                            null,
                            new TypeReference<ResponseDTO<List<UserDetailVO>>>() {}
                    );

                    if (response != null && response.isSuccess() && response.getData() != null) {
                        List<Long> roleUserIds = response.getData().stream()
                                .map(UserDetailVO::getUserId)
                                .filter(id -> id != null)
                                .collect(Collectors.toList());
                        userIdList.addAll(roleUserIds);
                        log.debug("[WebSocket通知] 根据角色查询用户成功，roleId={}, 用户数量={}", roleId, roleUserIds.size());
                    } else {
                        log.warn("[WebSocket通知] 根据角色查询用户失败，roleId={}, message={}", roleId,
                                response != null ? response.getMessage() : "响应为空");
                    }
                } catch (Exception e) {
                    log.error("[WebSocket通知] 根据角色查询用户异常，roleId={}", roleId, e);
                    // 单个角色查询失败不影响其他角色，继续处理
                }
            }

            // 去重
            return userIdList.stream().distinct().collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[WebSocket通知] 根据角色查询用户异常，roleIds={}", roleIds, e);
            return List.of();
        }
    }

    /**
     * 根据部门ID列表查询用户ID列表
     * <p>
     * 通过GatewayServiceClient调用公共服务获取部门下的所有用户
     * 参考竞品实现：钉钉、企业微信等均支持按部门批量查询用户
     * </p>
     *
     * @param departmentIds 部门ID列表（逗号分隔）
     * @return 用户ID列表
     */
    private List<Long> queryUserIdsByDepartmentIds(String departmentIds) {
        if (departmentIds == null || departmentIds.trim().isEmpty()) {
            return List.of();
        }

        try {
            // 解析部门ID列表
            List<Long> departmentIdList = Arrays.stream(departmentIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("[WebSocket通知] 部门ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            if (departmentIdList.isEmpty()) {
                return List.of();
            }

            // 通过GatewayServiceClient调用公共服务获取部门下的所有用户
            List<Long> userIdList = new java.util.ArrayList<>();
            for (Long departmentId : departmentIdList) {
                try {
                    // 调用公共服务：GET /api/v1/departments/{departmentId}/users
                    ResponseDTO<List<UserDetailVO>> response = gatewayServiceClient.callCommonService(
                            "/api/v1/departments/" + departmentId + "/users",
                            HttpMethod.GET,
                            null,
                            new TypeReference<ResponseDTO<List<UserDetailVO>>>() {}
                    );

                    if (response != null && response.isSuccess() && response.getData() != null) {
                        List<Long> deptUserIds = response.getData().stream()
                                .map(UserDetailVO::getUserId)
                                .filter(id -> id != null)
                                .collect(Collectors.toList());
                        userIdList.addAll(deptUserIds);
                        log.debug("[WebSocket通知] 根据部门查询用户成功，departmentId={}, 用户数量={}", departmentId, deptUserIds.size());
                    } else {
                        log.warn("[WebSocket通知] 根据部门查询用户失败，departmentId={}, message={}", departmentId,
                                response != null ? response.getMessage() : "响应为空");
                    }
                } catch (Exception e) {
                    log.error("[WebSocket通知] 根据部门查询用户异常，departmentId={}", departmentId, e);
                    // 单个部门查询失败不影响其他部门，继续处理
                }
            }

            // 去重
            return userIdList.stream().distinct().collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[WebSocket通知] 根据部门查询用户异常，departmentIds={}", departmentIds, e);
        return List.of();
        }
    }
}
