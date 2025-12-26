package net.lab1024.sa.visitor.controller;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.service.DeviceVisitorService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 设备访客验证控制器
 * <p>
 * 接收设备端访客验证请求（混合验证模式）
 * 严格遵循CLAUDE.md规范：
 * - 临时访客：中心实时验证
 * - 常客：边缘验证
 * - 权限下发机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/visitor/device")
@Tag(name = "设备访客验证", description = "接收设备端访客验证请求")
 public class DeviceVisitorController {

    @Resource
    private DeviceVisitorService deviceVisitorService;

    /**
     * 临时访客中心验证
     * <p>
     * ⭐ 临时访客必须中心验证
     * ⭐ 软件端验证：预约状态、时间范围、人脸验证
     * ⭐ 生成临时模板并下发到设备
     * </p>
     *
     * @param appointmentId 预约ID
     * @param biometricData 生物特征数据
     * @param deviceId 设备ID
     * @return 验证结果
     */
    @PostMapping("/verify-temporary-visitor")
    @Operation(summary = "临时访客中心验证", description = "临时访客必须通过中心验证，生成临时模板下发")
    public ResponseDTO<DeviceVisitorService.VisitorVerificationResult> verifyTemporaryVisitor(
            @RequestParam Long appointmentId,
            @RequestParam String biometricData,
            @RequestParam Long deviceId) {
        log.info("[设备访客验证] 临时访客中心验证, appointmentId={}, deviceId={}", appointmentId, deviceId);
        return deviceVisitorService.verifyTemporaryVisitor(appointmentId, biometricData, deviceId);
    }

    /**
     * 常客边缘验证
     * <p>
     * ⭐ 常客支持边缘验证
     * ⭐ 设备端验证：本地模板比对
     * ⭐ 软件端记录：上传验证结果
     * </p>
     *
     * @param passId 电子通行证ID
     * @param deviceId 设备ID
     * @return 验证结果
     */
    @PostMapping("/verify-regular-visitor")
    @Operation(summary = "常客边缘验证", description = "常客通过设备端边缘验证，软件端记录结果")
    public ResponseDTO<DeviceVisitorService.VisitorVerificationResult> verifyRegularVisitor(
            @RequestParam String passId,
            @RequestParam Long deviceId) {
        log.info("[设备访客验证] 常客边缘验证, passId={}, deviceId={}", passId, deviceId);
        return deviceVisitorService.verifyRegularVisitor(passId, deviceId);
    }
}
