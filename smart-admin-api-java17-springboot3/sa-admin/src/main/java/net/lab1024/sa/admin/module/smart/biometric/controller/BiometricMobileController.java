package net.lab1024.sa.admin.module.smart.biometric.controller;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricOfflineTokenRequest;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricOfflineTokenResponse;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricRegisterRequest;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricRegisterResult;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricTypeOption;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricVerifyRequest;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricVerifyResult;
import net.lab1024.sa.admin.module.smart.biometric.service.BiometricMobileService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.rbac.RequireResource;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;

/**
 * 移动端生物识别接口
 *
 * @author AI
 */
@Validated
@SaCheckLogin
@SaCheckPermission("biometric:mobile")
@Tag(name = "移动端生物识别", description = "提供移动端注册与验证接口")
@RestController
@RequestMapping("/api/smart/biometric/mobile")
@RequiredArgsConstructor
public class BiometricMobileController {

    private final BiometricMobileService biometricMobileService;

    @Operation(summary = "注册生物特征模板")
    @PostMapping("/register")
    public ResponseDTO<BiometricRegisterResult> register(
            @Valid @RequestBody BiometricRegisterRequest request) {
        return ResponseDTO.ok(biometricMobileService.registerTemplate(request));
    }

    @Operation(summary = "多模态生物识别验证")
    @PostMapping("/verify")
    @RequireResource(code = "biometric:verify", scope = "AREA")
    public ResponseDTO<BiometricVerifyResult> verify(
            @Valid @RequestBody BiometricVerifyRequest request) {
        return ResponseDTO.ok(biometricMobileService.verify(request));
    }

    @Operation(summary = "申请离线令牌")
    @PostMapping("/offline-token")
    public ResponseDTO<BiometricOfflineTokenResponse> offlineToken(
            @Valid @RequestBody BiometricOfflineTokenRequest request) {
        return ResponseDTO.ok(biometricMobileService.requestOfflineToken(request));
    }

    @Operation(summary = "支持的生物识别类型")
    @GetMapping("/types")
    public ResponseDTO<List<BiometricTypeOption>> types() {
        return ResponseDTO.ok(biometricMobileService.getSupportedTypes());
    }
}


