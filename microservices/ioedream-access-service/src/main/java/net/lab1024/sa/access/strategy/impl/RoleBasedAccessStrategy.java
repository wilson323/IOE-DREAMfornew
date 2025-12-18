package net.lab1024.sa.access.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessRequest;
import net.lab1024.sa.access.strategy.IAccessPermissionStrategy;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 基于角色的门禁权限策略
 * <p>
 * 验证用户角色是否在允许的角色列表中
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class RoleBasedAccessStrategy implements IAccessPermissionStrategy {

    @Resource
    private AreaDao areaDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Override
    public boolean hasPermission(AccessRequest request) {
        // 查询区域信息
        AreaEntity area = areaDao.selectById(request.getAreaId());
        if (area == null) {
            log.debug("[角色策略] 区域{}不存在", request.getAreaId());
            return false;
        }

        // 如果区域没有配置允许的角色，则允许所有角色
        if (area.getAllowedRoles() == null || area.getAllowedRoles().isEmpty()) {
            log.debug("[角色策略] 区域{}未配置允许角色，默认允许", request.getAreaId());
            return true;
        }

        // 获取用户角色（通过网关调用common-service）
        List<String> userRoles = getUserRoles(request.getUserId());
        if (userRoles == null || userRoles.isEmpty()) {
            log.debug("[角色策略] 用户{}无角色", request.getUserId());
            return false;
        }

        // 解析区域允许的角色列表
        List<String> allowedRoles = parseRoles(area.getAllowedRoles());

        // 检查用户角色是否在允许列表中
        boolean hasRole = userRoles.stream().anyMatch(allowedRoles::contains);
        log.debug("[角色策略] 用户{}角色{}，区域{}允许角色{}，结果：{}",
                request.getUserId(), userRoles, request.getAreaId(), allowedRoles, hasRole);

        return hasRole;
    }

    /**
     * 获取用户角色
     */
    private List<String> getUserRoles(Long userId) {
        // 通过网关调用common-service获取用户角色
        try {
            TypeReference<ResponseDTO<List<String>>> typeRef = new TypeReference<ResponseDTO<List<String>>>() {};
            ResponseDTO<List<String>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/" + userId + "/roles",
                    HttpMethod.GET,
                    null,
                    typeRef);
            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("[角色策略] 获取用户角色失败: userId={}", userId, e);
        }
        return List.of();
    }

    /**
     * 解析角色列表
     */
    private List<String> parseRoles(String rolesJson) {
        if (rolesJson == null || rolesJson.trim().isEmpty()) {
            return List.of();
        }
        try {
            return JsonUtil.fromJson(rolesJson, List.class);
        } catch (Exception e) {
            log.error("[角色策略] 解析角色列表失败: {}", rolesJson, e);
            return List.of();
        }
    }

    @Override
    public int getPriority() {
        return 80; // 角色策略优先级较低
    }

    @Override
    public String getStrategyType() {
        return "ROLE_BASED";
    }
}
