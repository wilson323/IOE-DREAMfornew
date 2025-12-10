package net.lab1024.sa.gateway.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.auth.manager.UnifiedAuthenticationManager;
import net.lab1024.sa.common.auth.domain.dto.*;
import net.lab1024.sa.common.auth.domain.vo.*;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.menu.entity.MenuEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关统一认证服务
 * <p>
 * 整合统一身份认证管理器，提供企业级认证服务
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UnifiedAuthService {

    @Resource
    private UnifiedAuthenticationManager unifiedAuthenticationManager;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private MenuService menuService;

    /**
     * 用户名密码登录
     *
     * @param request 登录请求
     * @return 登录结果
     */
    public Map<String, Object> loginByUsernamePassword(LoginRequest request) {
        log.info("[统一认证] 开始用户名密码登录: {}", request.getLoginName());

        try {
            // 调用统一认证管理器进行认证
            AuthenticationResult authResult = unifiedAuthenticationManager.authenticateByUsernamePassword(request);

            if (!authResult.getSuccess()) {
                log.warn("[统一认证] 登录失败: loginName={}, error={}", request.getLoginName(), authResult.getErrorMessage());
                return createErrorResponse(authResult.getErrorCode(), authResult.getErrorMessage());
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();

            // 设置令牌信息
            if (authResult.getToken() != null) {
                result.put("token", authResult.getToken().getAccessToken());
            }

            // 获取用户菜单信息
            List<MenuEntity> menuList = getUserMenuList(authResult.getUserId());
            result.put("menuList", menuList);

            // 构建用户信息
            Map<String, Object> employeeVO = buildEmployeeVO(authResult);
            result.put("employeeVO", employeeVO);

            // 添加额外认证信息
            result.put("requireMfa", authResult.getRequireMfa());
            result.put("supportedMfaTypes", authResult.getSupportedMfaTypes());
            result.put("permissions", authResult.getPermissions());
            result.put("roles", authResult.getRoles());

            log.info("[统一认证] 登录成功: loginName={}, userId={}", authResult.getLoginName(), authResult.getUserId());
            return createSuccessResponse(result);

        } catch (Exception e) {
            log.error("[统一认证] 登录异常: loginName={}, error={}", request.getLoginName(), e.getMessage(), e);
            return createErrorResponse("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 短信验证码登录
     *
     * @param request 短信登录请求
     * @return 登录结果
     */
    public Map<String, Object> loginBySmsCode(SmsLoginRequest request) {
        log.info("[统一认证] 开始短信验证码登录: {}", request.getPhone());

        try {
            // 调用统一认证管理器进行认证
            AuthenticationResult authResult = unifiedAuthenticationManager.authenticateBySmsCode(request);

            if (!authResult.getSuccess()) {
                log.warn("[统一认证] 短信登录失败: phone={}, error={}", request.getPhone(), authResult.getErrorMessage());
                return createErrorResponse(authResult.getErrorCode(), authResult.getErrorMessage());
            }

            // 构建返回结果（同用户名密码登录）
            Map<String, Object> result = new HashMap<>();

            if (authResult.getToken() != null) {
                result.put("token", authResult.getToken().getAccessToken());
            }

            List<MenuEntity> menuList = getUserMenuList(authResult.getUserId());
            result.put("menuList", menuList);

            Map<String, Object> employeeVO = buildEmployeeVO(authResult);
            result.put("employeeVO", employeeVO);

            result.put("requireMfa", authResult.getRequireMfa());
            result.put("supportedMfaTypes", authResult.getSupportedMfaTypes());
            result.put("permissions", authResult.getPermissions());
            result.put("roles", authResult.getRoles());

            log.info("[统一认证] 短信登录成功: phone={}, userId={}", authResult.getUserPhone(), authResult.getUserId());
            return createSuccessResponse(result);

        } catch (Exception e) {
            log.error("[统一认证] 短信登录异常: phone={}, error={}", request.getPhone(), e.getMessage(), e);
            return createErrorResponse("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 多因素认证
     *
     * @param primaryResult 主认证结果
     * @param mfaRequest 多因素认证请求
     * @return 认证结果
     */
    public Map<String, Object> multiFactorAuthentication(AuthenticationResult primaryResult, MfaRequest mfaRequest) {
        log.info("[统一认证] 开始多因素认证: userId={}, mfaType={}", primaryResult.getUserId(), mfaRequest.getMfaType());

        try {
            // 调用统一认证管理器进行多因素认证
            AuthenticationResult authResult = unifiedAuthenticationManager.multiFactorAuthentication(primaryResult, mfaRequest);

            if (!authResult.getSuccess()) {
                log.warn("[统一认证] 多因素认证失败: userId={}, error={}", primaryResult.getUserId(), authResult.getErrorMessage());
                return createErrorResponse(authResult.getErrorCode(), authResult.getErrorMessage());
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();

            if (authResult.getToken() != null) {
                result.put("token", authResult.getToken().getAccessToken());
            }

            List<MenuEntity> menuList = getUserMenuList(authResult.getUserId());
            result.put("menuList", menuList);

            Map<String, Object> employeeVO = buildEmployeeVO(authResult);
            result.put("employeeVO", employeeVO);

            result.put("permissions", authResult.getPermissions());
            result.put("roles", authResult.getRoles());

            log.info("[统一认证] 多因素认证成功: userId={}", authResult.getUserId());
            return createSuccessResponse(result);

        } catch (Exception e) {
            log.error("[统一认证] 多因素认证异常: userId={}, error={}", primaryResult.getUserId(), e.getMessage(), e);
            return createErrorResponse("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 用户登出
     *
     * @param userId 用户ID
     * @param token 令牌
     */
    public void logout(Long userId, String token) {
        log.info("[统一认证] 用户登出: userId={}", userId);

        try {
            unifiedAuthenticationManager.logout(userId, token);
            log.info("[统一认证] 登出成功: userId={}", userId);

        } catch (Exception e) {
            log.error("[统一认证] 登出异常: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 获取用户菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    private List<MenuEntity> getUserMenuList(Long userId) {
        try {
            return menuService.getUserMenuTree(userId);
        } catch (Exception e) {
            log.error("[统一认证] 获取用户菜单失败: userId={}, error={}", userId, e.getMessage(), e);
            return List.of(); // 返回空菜单，不影响登录
        }
    }

    /**
     * 构建用户信息VO
     *
     * @param authResult 认证结果
     * @return 用户信息VO
     */
    private Map<String, Object> buildEmployeeVO(AuthenticationResult authResult) {
        Map<String, Object> employeeVO = new HashMap<>();
        employeeVO.put("employeeId", authResult.getUserId());
        employeeVO.put("loginName", authResult.getLoginName());
        employeeVO.put("actualName", authResult.getUserName());
        employeeVO.put("phone", authResult.getUserPhone());
        employeeVO.put("email", authResult.getUserEmail());
        employeeVO.put("departmentId", authResult.getDepartmentId());
        employeeVO.put("departmentName", authResult.getDepartmentName());
        employeeVO.put("administratorFlag", authResult.getAdministratorFlag());
        employeeVO.put("disabledFlag", !"ACTIVE".equals(authResult.getAccountStatus()));
        return employeeVO;
    }

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @return 响应Map
     */
    private Map<String, Object> createSuccessResponse(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    /**
     * 创建错误响应
     *
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @return 响应Map
     */
    private Map<String, Object> createErrorResponse(String errorCode, String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("errorMessage", errorMessage);
        return response;
    }
}
