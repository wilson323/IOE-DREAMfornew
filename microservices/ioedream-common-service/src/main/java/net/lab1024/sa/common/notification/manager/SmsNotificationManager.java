package net.lab1024.sa.common.notification.manager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.identity.domain.vo.UserDetailVO;
import net.lab1024.sa.common.monitor.domain.entity.NotificationEntity;
import net.lab1024.sa.common.notification.constant.NotificationConfigKey;

/**
 * 短信通知管理器
 * <p>
 * 负责短信通知的发送和管理
 * 严格遵循CLAUDE.md规范:
 * - 使用@Component注解标识
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 企业级特性：
 * - 支持阿里云短信服务
 * - 异步发送，不阻塞主业务流程
 * - 支持模板短信
 * - 批量发送优化
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class SmsNotificationManager {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private NotificationConfigManager notificationConfigManager;

    @Resource
    private NotificationRateLimiter notificationRateLimiter;

    @Resource
    private NotificationMetricsCollector notificationMetricsCollector;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 发送短信通知
     * <p>
     * 基于阿里云短信服务实现短信发送
     * 支持模板短信和自定义内容
     * </p>
     *
     * @param notification 通知实体
     * @return 发送是否成功
     */
    @Async("notificationExecutor")
    public boolean sendSms(NotificationEntity notification) {
        log.info("[短信通知] 开始发送，通知ID：{}，接收人：{}，标题：{}",
                notification.getNotificationId(), notification.getReceiverIds(), notification.getTitle());

        long startTime = System.currentTimeMillis();
        String channel = "SMS";
        int successCount = 0;
        int failureCount = 0;

        try {
            // 1. 限流检查（使用统一限流管理器）
            if (!notificationRateLimiter.tryAcquire(channel)) {
                log.warn("[短信通知] 超过限流阈值，通知ID：{}", notification.getNotificationId());
                notificationMetricsCollector.recordRateLimit(channel);
                return false;
            }

            // 2. 获取接收人手机号列表
            List<String> phoneList = parseReceiverPhoneNumbers(notification);
            if (phoneList.isEmpty()) {
                log.warn("[短信通知] 接收人列表为空，通知ID：{}", notification.getNotificationId());
                return false;
            }

            // 3. 构建短信内容
            String content = buildSmsContent(notification);

            // 4. 调用阿里云短信API发送
            for (String phone : phoneList) {
                boolean success = sendSmsToPhone(phone, content, notification);
                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                    log.warn("[短信通知] 发送失败，手机号：{}，通知ID：{}", phone, notification.getNotificationId());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            boolean overallSuccess = failureCount == 0; // 全部成功才算成功

            if (overallSuccess) {
                log.info("[短信通知] 发送成功，通知ID：{}，接收人数量：{}，耗时：{}ms",
                        notification.getNotificationId(), phoneList.size(), duration);
                // 更新限流计数
                notificationRateLimiter.incrementCount(channel);
                // 记录成功指标
                notificationMetricsCollector.recordSuccess(channel, duration);
            } else {
                log.warn("[短信通知] 部分发送失败，通知ID：{}，成功：{}，失败：{}，耗时：{}ms",
                        notification.getNotificationId(), successCount, failureCount, duration);
                // 记录失败指标
                notificationMetricsCollector.recordFailure(channel, duration, "partial_failure");
            }

            return overallSuccess;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[短信通知] 发送异常，通知ID：{}，耗时：{}ms", notification.getNotificationId(), duration, e);
            // 记录异常指标
            notificationMetricsCollector.recordFailure(channel, duration, e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * 发送短信到指定手机号
     * <p>
     * 调用阿里云短信服务API
     * 从配置获取阿里云短信配置
     * </p>
     *
     * @param phone        手机号
     * @param content      短信内容
     * @param notification 通知实体
     * @return 是否发送成功
     */
    private boolean sendSmsToPhone(String phone, String content, NotificationEntity notification) {
        try {
            // 1. 从配置获取阿里云短信配置
            String accessKeyId = notificationConfigManager.getConfigValue(NotificationConfigKey.Sms.ACCESS_KEY_ID);
            String accessKeySecret = notificationConfigManager.getConfigValue(NotificationConfigKey.Sms.ACCESS_KEY_SECRET);
            String signName = notificationConfigManager.getConfigValue(NotificationConfigKey.Sms.SIGN_NAME, "IOE-DREAM");
            String templateCode = notificationConfigManager.getConfigValue(NotificationConfigKey.Sms.TEMPLATE_CODE);
            String region = notificationConfigManager.getConfigValue(NotificationConfigKey.Sms.REGION, "cn-hangzhou");

            if (accessKeyId == null || accessKeyId.isEmpty() || accessKeySecret == null || accessKeySecret.isEmpty()) {
                log.error("[短信通知] 阿里云短信配置不完整，通知ID：{}", notification.getNotificationId());
                return false;
            }

            // 2. 创建阿里云短信客户端并发送短信
            try {
                com.aliyun.dysmsapi20170525.Client client = createAliyunSmsClient(accessKeyId, accessKeySecret, region);
                com.aliyun.dysmsapi20170525.models.SendSmsRequest request = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                        .setPhoneNumbers(phone)
                        .setSignName(signName);

                // 如果配置了模板代码，使用模板短信；否则使用自定义内容
                if (templateCode != null && !templateCode.isEmpty()) {
                    request.setTemplateCode(templateCode);
                    // 构建模板参数（JSON格式）
                    Map<String, String> templateParams = new HashMap<>();
                    templateParams.put("content", content);
                    String templateParamJson = objectMapper.writeValueAsString(templateParams);
                    request.setTemplateParam(templateParamJson);
                } else {
                    // 自定义内容短信（需要先申请自定义短信模板）
                    log.warn("[短信通知] 未配置模板代码，使用自定义内容，手机号：{}", phone);
                    // 注意：阿里云短信服务通常需要模板，自定义内容需要特殊申请
                }

                // 发送短信
                com.aliyun.dysmsapi20170525.models.SendSmsResponse response = client.sendSms(request);
                String code = response.getBody().getCode();

                if ("OK".equals(code)) {
                    log.info("[短信通知] 短信发送成功，手机号：{}，通知ID：{}", phone, notification.getNotificationId());
                    return true;
                } else {
                    String message = response.getBody().getMessage();
                    log.error("[短信通知] 短信发送失败，手机号：{}，错误码：{}，错误信息：{}，通知ID：{}",
                            phone, code, message, notification.getNotificationId());
                    return false;
                }

            } catch (Exception e) {
                log.error("[短信通知] 调用阿里云短信API异常，手机号：{}，通知ID：{}",
                        phone, notification.getNotificationId(), e);
                return false;
            }

        } catch (Exception e) {
            log.error("[短信通知] 发送短信到手机号失败，手机号：{}，通知ID：{}",
                    phone, notification.getNotificationId(), e);
            return false;
        }
    }

    /**
     * 创建阿里云短信客户端
     * <p>
     * 使用AccessKey和Secret创建阿里云短信服务客户端
     * </p>
     *
     * @param accessKeyId     AccessKey ID
     * @param accessKeySecret AccessKey Secret
     * @param region          地域（如：cn-hangzhou）
     * @return 阿里云短信客户端
     * @throws Exception 创建客户端异常
     */
    private com.aliyun.dysmsapi20170525.Client createAliyunSmsClient(String accessKeyId, String accessKeySecret, String region)
            throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dysmsapi.aliyuncs.com")
                .setRegionId(region);

        return new com.aliyun.dysmsapi20170525.Client(config);
    }

    /**
     * 构建短信内容
     * <p>
     * 根据通知类型和内容构建短信正文
     * 短信内容需要符合运营商规范（通常不超过70个字符）
     * </p>
     *
     * @param notification 通知实体
     * @return 短信内容
     */
    private String buildSmsContent(NotificationEntity notification) {
        // 短信内容简洁明了，通常只包含关键信息
        return String.format("【IOE-DREAM】%s：%s", notification.getTitle(), notification.getContent());
    }

    /**
     * 解析接收人手机号列表
     * <p>
     * 根据接收人类型（用户ID/角色/部门）查询对应的手机号
     * 接收人类型定义：
     * - 1: 指定用户（receiverIds为用户ID列表）
     * - 2: 角色（receiverIds为角色ID列表）
     * - 3: 部门（receiverIds为部门ID列表）
     * </p>
     *
     * @param notification 通知实体
     * @return 接收人手机号列表
     */
    private List<String> parseReceiverPhoneNumbers(NotificationEntity notification) {
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
                            log.warn("[短信通知] 用户ID格式错误，跳过：{}", id);
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
            log.warn("[短信通知] 未知的接收人类型：{}，使用默认处理（指定用户）", receiverType);
            userIdList = Arrays.stream(receiverIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("[短信通知] 用户ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .toList();
        }

        // 将用户ID转换为手机号（通过调用公共服务获取用户手机号）
        return userIdList.stream()
                .map(this::getUserPhone)
                .filter(phone -> phone != null && !phone.trim().isEmpty())
                .filter(phone -> phone.matches("^1[3-9]\\d{9}$")) // 验证手机号格式
                .collect(Collectors.toList());
    }

    /**
     * 根据用户ID获取手机号
     * <p>
     * 通过GatewayServiceClient调用公共服务获取用户手机号
     * </p>
     *
     * @param userId 用户ID
     * @return 手机号，如果获取失败则返回null
     */
    private String getUserPhone(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 通过GatewayServiceClient调用公共服务获取用户详情
            ResponseDTO<UserDetailVO> response = gatewayServiceClient.callCommonService(
                    "/api/v1/users/" + userId,
                    HttpMethod.GET,
                    null,
                    UserDetailVO.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                UserDetailVO userDetail = response.getData();
                String phone = userDetail.getPhone();
                if (phone != null && !phone.trim().isEmpty()) {
                    log.debug("[短信通知] 获取用户手机号成功，userId={}, phone={}", userId, phone);
                    return phone;
                } else {
                    log.warn("[短信通知] 用户手机号为空，userId={}", userId);
                    return null;
                }
            } else {
                log.warn("[短信通知] 获取用户详情失败，userId={}, message={}", userId,
                        response != null ? response.getMessage() : "响应为空");
                return null;
            }
        } catch (Exception e) {
            log.error("[短信通知] 获取用户手机号异常，userId={}", userId, e);
            return null;
        }
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
                            log.warn("[短信通知] 角色ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            if (roleIdList.isEmpty()) {
        return List.of();
            }

            // 通过GatewayServiceClient调用公共服务获取角色下的所有用户
            // 注意：如果公共服务支持批量查询，可以优化为一次调用
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
                        log.debug("[短信通知] 根据角色查询用户成功，roleId={}, 用户数量={}", roleId, roleUserIds.size());
                    } else {
                        log.warn("[短信通知] 根据角色查询用户失败，roleId={}, message={}", roleId,
                                response != null ? response.getMessage() : "响应为空");
                    }
                } catch (Exception e) {
                    log.error("[短信通知] 根据角色查询用户异常，roleId={}", roleId, e);
                    // 单个角色查询失败不影响其他角色，继续处理
                }
            }

            // 去重
            return userIdList.stream().distinct().collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[短信通知] 根据角色查询用户异常，roleIds={}", roleIds, e);
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
                            log.warn("[短信通知] 部门ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            if (departmentIdList.isEmpty()) {
                return List.of();
            }

            // 通过GatewayServiceClient调用公共服务获取部门下的所有用户
            // 注意：如果公共服务支持批量查询，可以优化为一次调用
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
                        log.debug("[短信通知] 根据部门查询用户成功，departmentId={}, 用户数量={}", departmentId, deptUserIds.size());
                    } else {
                        log.warn("[短信通知] 根据部门查询用户失败，departmentId={}, message={}", departmentId,
                                response != null ? response.getMessage() : "响应为空");
                    }
                } catch (Exception e) {
                    log.error("[短信通知] 根据部门查询用户异常，departmentId={}", departmentId, e);
                    // 单个部门查询失败不影响其他部门，继续处理
                }
            }

            // 去重
            return userIdList.stream().distinct().collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[短信通知] 根据部门查询用户异常，departmentIds={}", departmentIds, e);
        return List.of();
        }
    }
}
