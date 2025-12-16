package net.lab1024.sa.common.auth.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.auth.domain.dto.LoginRequestDTO;
import net.lab1024.sa.common.auth.domain.dto.RefreshTokenRequestDTO;
import net.lab1024.sa.common.auth.domain.vo.LoginResponseVO;
import net.lab1024.sa.common.auth.domain.vo.UserInfoVO;
import net.lab1024.sa.common.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 统一认证控制器
 * <p>
 * 企业级统一身份认证控制器，从网关服务迁移至common-service
 * 支持多种认证方式：用户名密码、短信、多因素认证
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-14
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "统一认证", description = "统一身份认证相关接口")
public class AuthController {

    @Resource
    private AuthService authService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Observed(name = "auth.getCaptcha", contextualName = "auth-get-captcha")
    @Operation(summary = "获取验证码")
    @GetMapping("/getCaptcha")
    public ResponseDTO<Map<String, Object>> getCaptcha() {
        log.info("[认证服务] 获取验证码");
        // 简单验证码生成（生产环境应使用图形验证码）
        String captchaKey = "captcha:" + UUID.randomUUID().toString();
        String captchaCode = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        if (captchaCode != null) {
            stringRedisTemplate.opsForValue().set(captchaKey, captchaCode, 5, TimeUnit.MINUTES);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaCode", captchaCode); // 开发环境返回，生产环境应返回图片
        return ResponseDTO.ok(result);
    }

    @Observed(name = "auth.login", contextualName = "auth-login")
    @Operation(summary = "用户名密码登录")
    @PostMapping("/login")
    public ResponseDTO<LoginResponseVO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("[认证服务] 用户登录: {}", request.getUsername());
        LoginResponseVO result = authService.login(request);
        return ResponseDTO.ok(result);
    }

    @Observed(name = "auth.logout", contextualName = "auth-logout")
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ResponseDTO<String> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("[认证服务] 用户退出登录");
        authService.logout(token);
        return ResponseDTO.ok("退出成功");
    }

    @Observed(name = "auth.refreshToken", contextualName = "auth-refresh-token")
    @Operation(summary = "刷新令牌")
    @PostMapping("/refreshToken")
    public ResponseDTO<LoginResponseVO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        log.info("[认证服务] 刷新令牌");
        LoginResponseVO result = authService.refreshToken(request);
        return ResponseDTO.ok(result);
    }

    @Observed(name = "auth.getUserInfo", contextualName = "auth-get-user-info")
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userInfo")
    public ResponseDTO<UserInfoVO> getUserInfo(@RequestHeader("Authorization") String token) {
        log.info("[认证服务] 获取用户信息");
        UserInfoVO userInfo = authService.getUserInfo(token);
        return ResponseDTO.ok(userInfo);
    }

    @Observed(name = "auth.validateToken", contextualName = "auth-validate-token")
    @Operation(summary = "验证令牌")
    @GetMapping("/validateToken")
    public ResponseDTO<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        log.info("[认证服务] 验证令牌");
        boolean valid = authService.validateToken(token);
        return ResponseDTO.ok(valid);
    }
}
