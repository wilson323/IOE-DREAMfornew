package net.lab1024.sa.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.gateway.service.CaptchaService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 登录管理控制器
 * <p>
 * ⚠️ 警告：这是临时Mock实现，仅用于前端开发调试
 * 生产环境必须替换为真实的认证服务调用
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
@RestController
@Tag(name = "登录管理", description = "登录管理相关接口")
public class LoginController {

    @Resource
    private CaptchaService captchaService;

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
        return ResponseDTO.ok(false);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseDTO<Map<String, Object>> login(@RequestBody Map<String, Object> loginRequest) {
        log.warn("⚠️ 使用Mock登录实现，仅用于开发调试");
        log.info("用户登录，loginRequest：{}", loginRequest);
        
        String loginName = (String) loginRequest.get("loginName");
        String password = (String) loginRequest.get("password");
        String captchaCode = (String) loginRequest.get("captchaCode");
        String captchaUuid = (String) loginRequest.get("captchaUuid");
        
        // 验证验证码
        if (!captchaService.verifyCaptcha(captchaUuid, captchaCode)) {
            log.warn("验证码验证失败，uuid: {}, code: {}", captchaUuid, captchaCode);
            return ResponseDTO.error("CAPTCHA_ERROR", "验证码错误或已过期");
        }
        
        // ⚠️ Mock登录验证 - 生产环境必须替换为真实验证
        // TODO: 调用认证服务进行真实的用户名密码验证
        if (!"admin".equals(loginName) && !"123456".equals(password)) {
            log.warn("Mock登录验证失败，loginName: {}", loginName);
            return ResponseDTO.error("LOGIN_FAILED", "用户名或密码错误");
        }
        
        // ⚠️ Mock响应数据 - 生产环境必须从认证服务获取
        Map<String, Object> result = new HashMap<>();
        result.put("token", "sa-token-" + UUID.randomUUID().toString());
        result.put("menuList", new ArrayList<>());
        
        // Mock用户信息
        Map<String, Object> employeeVO = new HashMap<>();
        employeeVO.put("employeeId", 1L);
        employeeVO.put("loginName", loginName);
        employeeVO.put("actualName", "管理员");
        employeeVO.put("phone", "13800138000");
        employeeVO.put("departmentId", 1L);
        employeeVO.put("departmentName", "技术部");
        employeeVO.put("administratorFlag", true);
        employeeVO.put("disabledFlag", false);
        result.put("employeeVO", employeeVO);
        
        log.info("Mock登录成功，userName：{}", loginName);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "用户登出")
    @GetMapping("/login/logout")
    public ResponseDTO<String> logout() {
        log.info("用户登出");
        return ResponseDTO.ok("登出成功");
    }

    @Operation(summary = "获取登录信息")
    @GetMapping("/login/getLoginInfo")
    public ResponseDTO<Map<String, Object>> getLoginInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("menuList", new ArrayList<>());
        
        Map<String, Object> employeeVO = new HashMap<>();
        employeeVO.put("employeeId", 1L);
        employeeVO.put("loginName", "admin");
        employeeVO.put("actualName", "管理员");
        employeeVO.put("phone", "13800138000");
        employeeVO.put("departmentId", 1L);
        employeeVO.put("departmentName", "技术部");
        employeeVO.put("administratorFlag", true);
        employeeVO.put("disabledFlag", false);
        result.put("employeeVO", employeeVO);
        
        return ResponseDTO.ok(result);
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
}
