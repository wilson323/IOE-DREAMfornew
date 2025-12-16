package net.lab1024.sa.common.auth.manager.client;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.security.entity.UserEntity;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户目录客户端
 * <p>
 * 统一封装 UnifiedAuthenticationManager 对用户域的网关调用，避免调用逻辑散落。
 * </p>
 */
@Slf4j
public class UserDirectoryClient {

    private final GatewayServiceClient gatewayServiceClient;

    public UserDirectoryClient(GatewayServiceClient gatewayServiceClient) {
        this.gatewayServiceClient = gatewayServiceClient;
    }

    public UserEntity getUserByLoginName(String loginName) {
        try {
            ResponseDTO<UserEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/username/" + loginName,
                    HttpMethod.GET,
                    null,
                    UserEntity.class
            );
            return response != null && response.getOk() ? response.getData() : null;
        } catch (Exception e) {
            log.warn("[统一认证] 获取用户信息失败: loginName={}, error={}", loginName, e.getMessage());
            return null;
        }
    }

    public UserEntity getUserByPhone(String phone) {
        try {
            ResponseDTO<UserEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/phone/" + phone,
                    HttpMethod.GET,
                    null,
                    UserEntity.class
            );
            return response != null && response.getOk() ? response.getData() : null;
        } catch (Exception e) {
            log.warn("[统一认证] 通过手机号获取用户失败: phone={}, error={}", phone, e.getMessage());
            return null;
        }
    }

    public UserEntity getUserById(Long userId) {
        try {
            ResponseDTO<UserEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/" + userId,
                    HttpMethod.GET,
                    null,
                    UserEntity.class
            );
            return response != null && response.getOk() ? response.getData() : null;
        } catch (Exception e) {
            log.warn("[统一认证] 获取用户信息失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getUserRoles(Long userId) {
        try {
            ResponseDTO<?> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/roles/" + userId,
                    HttpMethod.GET,
                    null,
                    List.class
            );
            if (response != null && response.getOk() && response.getData() instanceof List) {
                return (List<String>) response.getData();
            }
            return List.of();
        } catch (Exception e) {
            log.warn("[统一认证] 获取用户角色失败: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getUserPermissions(Long userId) {
        try {
            ResponseDTO<?> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/permissions/" + userId,
                    HttpMethod.GET,
                    null,
                    List.class
            );
            if (response != null && response.getOk() && response.getData() instanceof List) {
                return (List<String>) response.getData();
            }
            return List.of();
        } catch (Exception e) {
            log.warn("[统一认证] 获取用户权限失败: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }

    public void updateLoginInfo(Long userId, String clientIp) {
        try {
            gatewayServiceClient.callCommonService(
                    "/api/v1/user/update-login-info",
                    HttpMethod.POST,
                    Map.of(
                            "userId", userId,
                            "clientIp", clientIp,
                            "loginTime", LocalDateTime.now()
                    ),
                    Void.class
            );
        } catch (Exception e) {
            log.warn("[统一认证] 更新登录信息失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    public void updateLastLoginTime(Long userId) {
        try {
            gatewayServiceClient.callCommonService(
                    "/api/v1/user/update-last-login-time",
                    HttpMethod.POST,
                    Map.of("userId", userId, "lastLoginTime", LocalDateTime.now()),
                    Void.class
            );
        } catch (Exception e) {
            log.warn("[统一认证] 更新最后登录时间失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    public void updateLastLogoutTime(Long userId) {
        try {
            gatewayServiceClient.callCommonService(
                    "/api/v1/user/update-logout-time",
                    HttpMethod.POST,
                    Map.of("userId", userId, "logoutTime", LocalDateTime.now()),
                    Void.class
            );
        } catch (Exception e) {
            log.warn("[统一认证] 更新登出时间失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    public void clearUserSessionsInDatabase(Long userId) {
        try {
            gatewayServiceClient.callCommonService(
                    "/api/v1/user/session/clear/" + userId,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );
        } catch (Exception e) {
            log.warn("[统一认证] 清理数据库会话记录失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public String getUserPreferenceValue(Long userId, String group, String key) {
        try {
            ResponseDTO<?> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/preference/" + userId + "/" + group + "/" + key,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            if (response == null || !response.getOk() || !(response.getData() instanceof Map)) {
                return null;
            }
            Map<String, Object> preference = (Map<String, Object>) response.getData();
            if (preference == null) {
                return null;
            }
            Object value = preference.get("preferenceValue");
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.debug("[统一认证] 获取用户偏好失败: userId={}, group={}, key={}, error={}", userId, group, key, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public String getSystemConfigValue(String keyPath) {
        try {
            ResponseDTO<?> response = gatewayServiceClient.callCommonService(
                    "/api/v1/config/system/" + keyPath,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            if (response == null || !response.getOk() || !(response.getData() instanceof Map)) {
                return null;
            }
            Map<String, Object> config = (Map<String, Object>) response.getData();
            if (config == null) {
                return null;
            }
            Object value = config.get("configValue");
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.debug("[统一认证] 获取系统配置失败: keyPath={}, error={}", keyPath, e.getMessage());
            return null;
        }
    }
}

