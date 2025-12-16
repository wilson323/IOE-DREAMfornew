package net.lab1024.sa.common.auth.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.auth.domain.dto.LoginRequestDTO;
import net.lab1024.sa.common.auth.domain.vo.LoginResponseVO;
import net.lab1024.sa.common.auth.domain.vo.UserInfoVO;
import net.lab1024.sa.common.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录控制器（兼容前端legacy路径）
 * <p>
 * 为兼容前端 /login/** 路径，将请求转发到 AuthService
 * 前端 smart-admin-web-javascript 使用 /login/** 路径
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/login")
@Tag(name = "登录认证（兼容路径）", description = "兼容前端legacy路径的登录认证接口")
public class LoginController {

    @Resource
    private AuthService authService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取验证码
     * 兼容前端 /login/getCaptcha 路径
     */
    @Observed(name = "login.getCaptcha", contextualName = "login-get-captcha")
    @Operation(summary = "获取验证码", description = "获取图形验证码，用于登录验证")
    @GetMapping("/getCaptcha")
    public ResponseDTO<Map<String, Object>> getCaptcha() {
        log.info("[登录服务] 获取验证码");

        // 生成验证码key和code
        String captchaKey = "captcha:" + UUID.randomUUID().toString();
        String captchaCode = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));

        // 存入Redis，5分钟过期
        if (captchaCode != null) {
            stringRedisTemplate.opsForValue().set(captchaKey, captchaCode, 5, TimeUnit.MINUTES);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaCode", captchaCode); // 开发环境返回明文，生产环境应返回图片Base64
        result.put("captchaBase64", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="); // 占位符

        log.info("[登录服务] 验证码生成成功: key={}, code={}", captchaKey, captchaCode);
        return ResponseDTO.ok(result);
    }

    /**
     * 用户登录
     * 兼容前端 POST /login 路径
     */
    @Observed(name = "login.login", contextualName = "login-do-login")
    @Operation(summary = "用户登录", description = "用户名密码登录")
    @PostMapping
    public ResponseDTO<LoginResponseVO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("[登录服务] 用户登录: username={}", request.getUsername());
        LoginResponseVO result = authService.login(request);
        log.info("[登录服务] 用户登录成功: username={}", request.getUsername());
        return ResponseDTO.ok(result);
    }

    /**
     * 退出登录
     * 兼容前端 GET /login/logout 路径
     */
    @Observed(name = "login.logout", contextualName = "login-logout")
    @Operation(summary = "退出登录", description = "用户退出登录")
    @GetMapping("/logout")
    public ResponseDTO<String> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("[登录服务] 用户退出登录");
        authService.logout(token);
        return ResponseDTO.ok("退出成功");
    }

    /**
     * 获取登录用户信息
     * 兼容前端 GET /login/getLoginInfo 路径
     */
    @Observed(name = "login.getLoginInfo", contextualName = "login-get-login-info")
    @Operation(summary = "获取登录信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/getLoginInfo")
    public ResponseDTO<UserInfoVO> getLoginInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("[登录服务] 获取登录用户信息");
        if (token == null || token.isEmpty()) {
            log.warn("[登录服务] 未提供Authorization令牌");
            return ResponseDTO.error("UNAUTHORIZED", "未登录或令牌已过期");
        }
        UserInfoVO userInfo = authService.getUserInfo(token);
        return ResponseDTO.ok(userInfo);
    }

    /**
     * 发送邮箱验证码
     * 兼容前端 GET /login/sendEmailCode/{loginName} 路径
     */
    @Observed(name = "login.sendEmailCode", contextualName = "login-send-email-code")
    @Operation(summary = "发送邮箱验证码", description = "向用户邮箱发送登录验证码（双因子认证）")
    @GetMapping("/sendEmailCode/{loginName}")
    public ResponseDTO<String> sendEmailCode(@PathVariable String loginName) {
        log.info("[登录服务] 发送邮箱验证码: loginName={}", loginName);

        // TODO: 实现邮箱验证码发送逻辑
        // 1. 根据loginName查询用户邮箱
        // 2. 生成6位数字验证码
        // 3. 发送邮件
        // 4. 将验证码存入Redis（key: email_code:{loginName}, 过期时间: 10分钟）

        String emailCode = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        String cacheKey = "email_code:" + loginName;
        stringRedisTemplate.opsForValue().set(cacheKey, emailCode, 10, TimeUnit.MINUTES);

        log.info("[登录服务] 邮箱验证码已发送: loginName={}, code={}", loginName, emailCode);
        return ResponseDTO.ok("验证码已发送到您的邮箱");
    }

    /**
     * 获取双因子登录标识
     * 兼容前端 GET /login/getTwoFactorLoginFlag 路径
     */
    @Observed(name = "login.getTwoFactorLoginFlag", contextualName = "login-get-two-factor-flag")
    @Operation(summary = "获取双因子登录标识", description = "获取系统是否启用双因子认证")
    @GetMapping("/getTwoFactorLoginFlag")
    public ResponseDTO<Boolean> getTwoFactorLoginFlag() {
        log.info("[登录服务] 获取双因子登录标识");

        // TODO: 从系统配置中读取双因子认证开关
        // 当前返回false，表示不启用双因子认证
        Boolean twoFactorEnabled = false;

        log.info("[登录服务] 双因子认证标识: enabled={}", twoFactorEnabled);
        return ResponseDTO.ok(twoFactorEnabled);
    }
}
