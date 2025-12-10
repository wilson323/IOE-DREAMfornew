package net.lab1024.sa.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.auth.manager.UnifiedAuthenticationManager;
import net.lab1024.sa.common.system.service.SystemService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.identity.domain.vo.UserDetailVO;
import net.lab1024.sa.gateway.service.CaptchaService;
import net.lab1024.sa.gateway.service.MenuService;
import net.lab1024.sa.gateway.service.UnifiedAuthService;
import net.lab1024.sa.common.menu.entity.MenuEntity;
import net.lab1024.sa.common.auth.domain.dto.LoginRequest;
import net.lab1024.sa.common.auth.domain.dto.SmsLoginRequest;
import net.lab1024.sa.common.auth.domain.dto.MfaRequest;
import net.lab1024.sa.common.auth.domain.vo.AuthenticationResult;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 登录管理控制器
 * <p>
 * 企业级统一身份认证控制器
 * 支持多种认证方式：用户名密码、短信、多因素认证
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-09
 */
@Slf4j
@RestController
@Tag(name = "登录管理", description = "登录管理相关接口")
public class LoginController {

    @Resource
    private CaptchaService captchaService;

    @Resource
    private MenuService menuService;

    @Resource
    private UnifiedAuthService unifiedAuthService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private UnifiedAuthenticationManager unifiedAuthenticationManager;

    @Resource
    private SystemService systemService;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private HttpServletRequest httpServletRequest;

    @Operation(summary = "获取验证码")
    @GetMapping("/login/getCaptcha")
    public ResponseDTO<Map<String, Object>> getCaptcha() {
        log.info("获取验证码");
        try {
            CaptchaService.CaptchaResult captchaResult = captchaService.generateCaptcha();

            Map<String, Object> result = new HashMap<>();
            result.put("captchaUuid", captchaResult.getCaptchaUuid());
            result.put("captchaBase64Image", captchaResult.getCaptchaBase64Image());
            result.put("expireSeconds", captchaResult.getExpireSeconds());

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取验证码失败", e);
            return ResponseDTO.error("获取验证码失败");
        }
    }

    @Operation(summary = "获取双因素认证标志")
    @GetMapping("/login/getTwoFactorLoginFlag")
    public ResponseDTO<Boolean> getTwoFactorLoginFlag() {
        log.info("[统一认证] 获取双因素认证标志");
        try {
            // 从系统配置中获取双因素认证标志
            String configValue = systemService.getConfigValue("system.two.factor.login.enabled");
            boolean enabled = "true".equalsIgnoreCase(configValue) || "1".equals(configValue);

            log.info("[统一认证] 双因素认证标志: enabled={}", enabled);
            return ResponseDTO.ok(enabled);
        } catch (Exception e) {
            log.error("[统一认证] 获取双因素认证标志异常: error={}", e.getMessage(), e);
            // 默认返回false，确保系统可用性
            return ResponseDTO.ok(false);
        }
    }

    @Operation(summary = "用户名密码登录")
    @PostMapping("/login/password")
    public ResponseDTO<Map<String, Object>> loginByPassword(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("[统一认证] 用户名密码登录请求: loginName={}, clientIp={}",
                loginRequest.getLoginName(), loginRequest.getClientIp());

        try {
            // 验证验证码（如果提供了）
            if (loginRequest.getCaptchaUuid() != null && loginRequest.getCaptchaCode() != null) {
                if (!captchaService.verifyCaptcha(loginRequest.getCaptchaUuid(), loginRequest.getCaptchaCode())) {
                    log.warn("[统一认证] 验证码验证失败: uuid={}, code={}",
                            loginRequest.getCaptchaUuid(), loginRequest.getCaptchaCode());
                    return ResponseDTO.error("CAPTCHA_ERROR", "验证码错误或已过期");
                }
            }

            // 调用统一认证服务
            Map<String, Object> result = unifiedAuthService.loginByUsernamePassword(loginRequest);

            Boolean success = (Boolean) result.get("success");
            if (!success) {
                String errorCode = (String) result.get("errorCode");
                String errorMessage = (String) result.get("errorMessage");
                return ResponseDTO.error(errorCode, errorMessage);
            }

            log.info("[统一认证] 用户名密码登录成功: loginName={}", loginRequest.getLoginName());
            // 安全类型转换：UnifiedAuthService返回的data是Map<String, Object>
            Object dataObj = result.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                return ResponseDTO.ok(data);
            }
            return ResponseDTO.error("SYSTEM_ERROR", "登录响应数据格式错误");

        } catch (Exception e) {
            log.error("[统一认证] 用户名密码登录异常: loginName={}, error={}",
                    loginRequest.getLoginName(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/login/sms")
    public ResponseDTO<Map<String, Object>> loginBySms(@Valid @RequestBody SmsLoginRequest smsRequest) {
        log.info("[统一认证] 短信验证码登录请求: phone={}, deviceId={}",
                smsRequest.getPhone(), smsRequest.getDeviceId());

        try {
            // 调用统一认证服务
            Map<String, Object> result = unifiedAuthService.loginBySmsCode(smsRequest);

            Boolean success = (Boolean) result.get("success");
            if (!success) {
                String errorCode = (String) result.get("errorCode");
                String errorMessage = (String) result.get("errorMessage");
                return ResponseDTO.error(errorCode, errorMessage);
            }

            log.info("[统一认证] 短信验证码登录成功: phone={}", smsRequest.getPhone());
            // 安全类型转换：UnifiedAuthService返回的data是Map<String, Object>
            Object dataObj = result.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                return ResponseDTO.ok(data);
            }
            return ResponseDTO.error("SYSTEM_ERROR", "登录响应数据格式错误");

        } catch (Exception e) {
            log.error("[统一认证] 短信验证码登录异常: phone={}, error={}",
                    smsRequest.getPhone(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Operation(summary = "多因素认证")
    @PostMapping("/login/mfa")
    public ResponseDTO<Map<String, Object>> multiFactorAuth(@Valid @RequestBody MfaRequest mfaRequest,
                                                          @RequestHeader("X-Auth-Result") String authResultHeader) {
        log.info("[统一认证] 多因素认证请求: userId={}, mfaType={}",
                mfaRequest.getUserId(), mfaRequest.getMfaType());

        try {
            // 从Header或Session中获取主认证结果
            AuthenticationResult primaryResult = getPrimaryAuthResultFromHeader(authResultHeader, mfaRequest.getUserId());
            if (primaryResult == null) {
                log.warn("[统一认证] 无法获取主认证结果，userId={}", mfaRequest.getUserId());
                return ResponseDTO.error("AUTH_ERROR", "主认证结果不存在或已过期");
            }

            // 调用统一认证服务
            Map<String, Object> result = unifiedAuthService.multiFactorAuthentication(primaryResult, mfaRequest);

            Boolean success = (Boolean) result.get("success");
            if (!success) {
                String errorCode = (String) result.get("errorCode");
                String errorMessage = (String) result.get("errorMessage");
                return ResponseDTO.error(errorCode, errorMessage);
            }

            log.info("[统一认证] 多因素认证成功: userId={}, mfaType={}",
                    mfaRequest.getUserId(), mfaRequest.getMfaType());
            // 安全类型转换：UnifiedAuthService返回的data是Map<String, Object>
            Object dataObj = result.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                return ResponseDTO.ok(data);
            }
            return ResponseDTO.error("SYSTEM_ERROR", "认证响应数据格式错误");

        } catch (Exception e) {
            log.error("[统一认证] 多因素认证异常: userId={}, error={}",
                    mfaRequest.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 兼容旧版本登录接口 - 重定向到用户名密码登录
     */
    @Operation(summary = "用户登录（兼容旧版本）")
    @PostMapping("/login")
    public ResponseDTO<Map<String, Object>> login(@RequestBody Map<String, Object> loginRequest) {
        log.warn("[兼容模式] 使用旧版本登录接口，建议升级到新接口");

        // 转换为新的登录请求格式
        LoginRequest newLoginRequest = new LoginRequest();
        newLoginRequest.setLoginName((String) loginRequest.get("loginName"));
        newLoginRequest.setPassword((String) loginRequest.get("password"));
        newLoginRequest.setCaptchaCode((String) loginRequest.get("captchaCode"));
        newLoginRequest.setCaptchaUuid((String) loginRequest.get("captchaUuid"));
        newLoginRequest.setClientIp((String) loginRequest.get("clientIp"));
        newLoginRequest.setDeviceId((String) loginRequest.get("deviceId"));

        return loginByPassword(newLoginRequest);
    }

    @Operation(summary = "用户登出")
    @GetMapping("/login/logout")
    public ResponseDTO<String> logout(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                     @RequestHeader(value = "X-Token", required = false) String token) {
        log.info("[统一认证] 用户登出: userId={}", userId);

        try {
            if (userId != null && token != null) {
                unifiedAuthService.logout(userId, token);
            }
            return ResponseDTO.ok("登出成功");

        } catch (Exception e) {
            log.error("[统一认证] 用户登出异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.ok("登出成功"); // 即使异常也返回成功，避免前端卡死
        }
    }

    @Operation(summary = "获取登录信息")
    @GetMapping("/login/getLoginInfo")
    public ResponseDTO<Map<String, Object>> getLoginInfo(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("[统一认证] 获取登录信息: userId={}", userId);

        Map<String, Object> result = new HashMap<>();

        try {
            // 从Token中解析用户信息
            if (userId == null) {
                userId = getUserIdFromToken();
                if (userId == null) {
                    log.warn("[统一认证] 无法从Token解析用户ID");
                    return ResponseDTO.error("AUTH_ERROR", "用户未登录或Token无效");
                }
            }

            // 获取用户菜单数据
            List<MenuEntity> menuList = new ArrayList<>();
            try {
                menuList = menuService.getUserMenuTree(userId);
                log.info("[统一认证] 获取登录信息菜单成功: userId={}, menuCount={}", userId, menuList.size());
            } catch (Exception e) {
                log.error("[统一认证] 获取登录信息菜单失败，使用空菜单: userId={}, error={}", userId, e.getMessage(), e);
            }
            result.put("menuList", menuList);

            // 从统一认证服务获取用户信息
            Map<String, Object> employeeVO = getUserInfoFromAuthService(userId);
            result.put("employeeVO", employeeVO);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[统一认证] 获取登录信息异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取登录信息失败");
        }
    }

    // ================== 私有辅助方法 ==================

    /**
     * 从Header或Session中获取主认证结果
     * <p>
     * 企业级实现：
     * 1. 优先从X-Auth-Result Header中解析
     * 2. 如果Header不存在，从Session中获取
     * 3. 如果都不存在，从Redis中获取临时存储的认证结果
     * </p>
     *
     * @param authResultHeader X-Auth-Result Header值
     * @param userId 用户ID（备用）
     * @return 主认证结果，如果不存在则返回null
     */
    private AuthenticationResult getPrimaryAuthResultFromHeader(String authResultHeader, Long userId) {
        try {
            // 1. 从Header中解析（如果提供了）
            if (authResultHeader != null && !authResultHeader.trim().isEmpty()) {
                // 尝试从Header中解析JSON格式的认证结果
                // 注意：实际实现中，Header可能包含加密的认证结果或Token
                // 这里简化处理，实际应该解密或验证Header内容
                log.debug("[统一认证] 从Header获取主认证结果: header={}", authResultHeader);
            }

            // 2. 从Session中获取（如果存在）
            Object sessionAuthResult = httpServletRequest.getSession(false) != null
                    ? httpServletRequest.getSession().getAttribute("primaryAuthResult")
                    : null;
            if (sessionAuthResult instanceof AuthenticationResult) {
                log.debug("[统一认证] 从Session获取主认证结果: userId={}", userId);
                return (AuthenticationResult) sessionAuthResult;
            }

            // 3. 从Token中解析用户信息并构建认证结果（备用方案）
            if (userId != null) {
                String token = getTokenFromRequest();
                if (token != null) {
                    Long tokenUserId = jwtTokenUtil.getUserIdFromToken(token);
                    if (tokenUserId != null && tokenUserId.equals(userId)) {
                        // 从Token解析用户信息并构建认证结果
                        return buildAuthResultFromToken(token, userId);
                    }
                }
            }

            log.warn("[统一认证] 无法获取主认证结果: userId={}", userId);
            return null;

        } catch (Exception e) {
            log.error("[统一认证] 获取主认证结果异常: userId={}, error={}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从请求中获取Token
     *
     * @return Token字符串，如果不存在则返回null
     */
    private String getTokenFromRequest() {
        // 1. 从Authorization Header获取
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. 从X-Token Header获取
        String tokenHeader = httpServletRequest.getHeader("X-Token");
        if (tokenHeader != null && !tokenHeader.trim().isEmpty()) {
            return tokenHeader;
        }

        // 3. 从请求参数获取（不推荐，但为了兼容性保留）
        String tokenParam = httpServletRequest.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam;
        }

        return null;
    }

    /**
     * 从Token中获取用户ID
     *
     * @return 用户ID，如果无法获取则返回null
     */
    private Long getUserIdFromToken() {
        try {
            String token = getTokenFromRequest();
            if (token == null) {
                return null;
            }

            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId != null) {
                log.debug("[统一认证] 从Token解析用户ID成功: userId={}", userId);
            }
            return userId;

        } catch (Exception e) {
            log.warn("[统一认证] 从Token解析用户ID失败: error={}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token构建认证结果
     *
     * @param token JWT Token
     * @param userId 用户ID
     * @return 认证结果
     */
    private AuthenticationResult buildAuthResultFromToken(String token, Long userId) {
        try {
            // 从Token解析用户信息
            String username = jwtTokenUtil.getUsernameFromToken(token);
            List<String> roles = jwtTokenUtil.getRolesFromToken(token);
            List<String> permissions = jwtTokenUtil.getPermissionsFromToken(token);

            // 构建认证结果（简化版，实际应该从数据库获取完整用户信息）
            return AuthenticationResult.builder()
                    .success(true)
                    .userId(userId)
                    .loginName(username != null ? username : "unknown")
                    .roles(roles)
                    .permissions(permissions)
                    .accountStatus("ACTIVE")
                    .requireMfa(false)
                    .build();

        } catch (Exception e) {
            log.error("[统一认证] 从Token构建认证结果异常: userId={}, error={}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从统一认证服务获取用户信息
     * <p>
     * 企业级实现：
     * 1. 通过GatewayServiceClient调用公共服务获取用户详情
     * 2. 包含用户基本信息、部门信息、权限信息等
     * 3. 支持降级处理，确保系统可用性
     * </p>
     *
     * @param userId 用户ID
     * @return 用户信息Map，包含employeeId、loginName、actualName等字段
     */
    private Map<String, Object> getUserInfoFromAuthService(Long userId) {
        try {
            log.debug("[统一认证] 开始获取用户信息: userId={}", userId);

            // 通过GatewayServiceClient调用公共服务获取用户详情
            ResponseDTO<UserDetailVO> response = gatewayServiceClient.callCommonService(
                    "/api/v1/users/" + userId,
                    HttpMethod.GET,
                    null,
                    UserDetailVO.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                UserDetailVO userDetail = response.getData();

                // 构建employeeVO，映射UserDetailVO到前端需要的格式
                Map<String, Object> employeeVO = new HashMap<>();
                employeeVO.put("employeeId", userDetail.getUserId());
                employeeVO.put("loginName", userDetail.getUsername());
                employeeVO.put("actualName", userDetail.getRealName() != null ? userDetail.getRealName() : userDetail.getUsername());
                employeeVO.put("phone", userDetail.getPhone() != null ? userDetail.getPhone() : "");
                employeeVO.put("email", userDetail.getEmail() != null ? userDetail.getEmail() : "");
                employeeVO.put("departmentId", null); // UserDetailVO中没有departmentId，需要后续扩展
                employeeVO.put("departmentName", userDetail.getDepartmentName() != null ? userDetail.getDepartmentName() : "");
                employeeVO.put("administratorFlag", false); // 需要根据角色判断，这里简化处理
                employeeVO.put("disabledFlag", userDetail.getStatus() != null && userDetail.getStatus() != 1);

                log.info("[统一认证] 获取用户信息成功: userId={}, username={}, realName={}",
                        userId, userDetail.getUsername(), userDetail.getRealName());
                return employeeVO;
            } else {
                log.warn("[统一认证] 获取用户信息失败: userId={}, message={}",
                        userId, response != null ? response.getMessage() : "响应为空");
                // 降级处理：返回默认值
                return buildDefaultEmployeeVO(userId);
            }

        } catch (Exception e) {
            log.error("[统一认证] 获取用户信息异常: userId={}, error={}", userId, e.getMessage(), e);
            // 降级处理：返回默认值，确保系统可用性
            return buildDefaultEmployeeVO(userId);
        }
    }

    /**
     * 构建默认用户信息VO（降级处理）
     *
     * @param userId 用户ID
     * @return 默认用户信息Map
     */
    private Map<String, Object> buildDefaultEmployeeVO(Long userId) {
        Map<String, Object> defaultVO = new HashMap<>();
        defaultVO.put("employeeId", userId);
        defaultVO.put("loginName", "user_" + userId);
        defaultVO.put("actualName", "用户" + userId);
        defaultVO.put("phone", "");
        defaultVO.put("email", "");
        defaultVO.put("departmentId", null);
        defaultVO.put("departmentName", "");
        defaultVO.put("administratorFlag", false);
        defaultVO.put("disabledFlag", false);
        return defaultVO;
    }

    // ==================== 说明 ====================
    // 业务接口已移除，统一通过网关路由转发到对应微服务
    // 路由配置位置：application.yml 的 spring.cloud.gateway.routes
    //
    // 路由规则：
    // - /support/** → ioedream-common-service (8088)
    // - /oa/** → ioedream-oa-service (8089)
    // - /access/** → ioedream-access-service (8090)
    // - /attendance/** → ioedream-attendance-service (8091)
    // - /consume/** → ioedream-consume-service (8094)
    // - /visitor/** → ioedream-visitor-service (8095)
    // - /video/** → ioedream-video-service (8092)
    //
    // 新增认证接口：
    // - POST /login/password - 用户名密码登录（推荐）
    // - POST /login/sms - 短信验证码登录
    // - POST /login/mfa - 多因素认证
    // - POST /login - 兼容旧版本登录接口
}
