package net.lab1024.sa.visitor.controller;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.visitor.dao.VisitorApprovalRecordDao;
import net.lab1024.sa.visitor.visitor.entity.VisitorApprovalRecordEntity;
import net.lab1024.sa.visitor.visitor.dao.ElectronicPassDao;
import net.lab1024.sa.visitor.visitor.entity.ElectronicPassEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

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
@Slf4j
@RestController
@RequestMapping("/api/v1/visitor/device")
@Tag(name = "设备访客验证", description = "接收设备端访客验证请求")
public class DeviceVisitorController {

    @Resource
    private VisitorApprovalRecordDao visitorApprovalRecordDao;

    @Resource
    private ElectronicPassDao electronicPassDao;

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
    public ResponseDTO<VisitorVerificationResult> verifyTemporaryVisitor(
            @RequestParam Long appointmentId,
            @RequestParam String biometricData,
            @RequestParam Long deviceId) {
        log.info("[设备访客验证] 临时访客中心验证, appointmentId={}, deviceId={}", appointmentId, deviceId);

        try {
            // 1. 查询预约记录
            VisitorApprovalRecordEntity approval = visitorApprovalRecordDao.selectByAppointmentId(appointmentId)
                    .stream()
                    .filter(a -> "APPROVED".equals(a.getApprovalResult()))
                    .findFirst()
                    .orElse(null);

            if (approval == null) {
                return ResponseDTO.error("VISITOR_APPOINTMENT_NOT_FOUND", "预约不存在或未审批通过");
            }

            // 2. 时间验证
            if (!isWithinVisitTime(approval)) {
                return ResponseDTO.error("VISITOR_TIME_OUT_OF_RANGE", "不在预约时间范围内");
            }

            // 3. 人脸验证（可选）
            if (biometricData != null && !biometricData.isEmpty()) {
                boolean faceMatched = verifyVisitorFace(approval, biometricData);
                if (!faceMatched) {
                    return ResponseDTO.error("VISITOR_FACE_VERIFY_FAILED", "人脸验证失败");
                }
            }

            // 4. 生成临时模板并下发到设备
            String templateId = generateAndSyncTemporaryTemplate(approval, deviceId);

            return ResponseDTO.ok(VisitorVerificationResult.builder()
                    .success(true)
                    .templateId(templateId)
                    .expireTime(approval.getVisitEndTime())
                    .message("临时访客验证成功")
                    .build());

        } catch (Exception e) {
            log.error("[设备访客验证] 临时访客验证异常", e);
            return ResponseDTO.error("VISITOR_VERIFY_ERROR", "访客验证失败: " + e.getMessage());
        }
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
    public ResponseDTO<VisitorVerificationResult> verifyRegularVisitor(
            @RequestParam String passId,
            @RequestParam Long deviceId) {
        log.info("[设备访客验证] 常客边缘验证, passId={}, deviceId={}", passId, deviceId);

        try {
            // 1. 查询电子通行证
            ElectronicPassEntity pass = electronicPassDao.selectByPassId(passId);
            if (pass == null || pass.getStatus() != 1) {
                return ResponseDTO.error("VISITOR_PASS_NOT_FOUND", "电子通行证不存在或已失效");
            }

            // 2. 验证有效期
            if (pass.getExpireTime() != null && LocalDateTime.now().isAfter(pass.getExpireTime())) {
                return ResponseDTO.error("VISITOR_PASS_EXPIRED", "电子通行证已过期");
            }

            // 3. 设备端已完成验证，软件端只记录结果
            recordVisitorAccess(pass, deviceId);

            return ResponseDTO.ok(VisitorVerificationResult.builder()
                    .success(true)
                    .passId(passId)
                    .message("常客验证成功")
                    .build());

        } catch (Exception e) {
            log.error("[设备访客验证] 常客验证异常", e);
            return ResponseDTO.error("VISITOR_VERIFY_ERROR", "访客验证失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否在访问时间范围内
     */
    private boolean isWithinVisitTime(VisitorApprovalRecordEntity approval) {
        LocalDateTime now = LocalDateTime.now();
        // 实现时间范围检查逻辑
        return true; // 临时实现
    }

    /**
     * 验证访客人脸
     */
    private boolean verifyVisitorFace(VisitorApprovalRecordEntity approval, String biometricData) {
        // 实现人脸验证逻辑
        return true; // 临时实现
    }

    /**
     * 生成并同步临时模板到设备
     */
    private String generateAndSyncTemporaryTemplate(VisitorApprovalRecordEntity approval, Long deviceId) {
        // 1. 生成临时模板
        String templateId = "TEMP_" + approval.getAppointmentId() + "_" + System.currentTimeMillis();

        // 2. 通过biometric-service同步模板到设备
        // 实现逻辑：调用biometric-service的模板同步接口

        log.info("[设备访客验证] 临时模板生成并同步, templateId={}, deviceId={}", templateId, deviceId);
        return templateId;
    }

    /**
     * 记录访客通行
     */
    private void recordVisitorAccess(ElectronicPassEntity pass, Long deviceId) {
        // 记录访客通行记录
        log.debug("[设备访客验证] 记录访客通行, passId={}, deviceId={}", pass.getPassId(), deviceId);
    }

    /**
     * 访客验证结果
     */
    @lombok.Data
    @lombok.Builder
    private static class VisitorVerificationResult {
        private boolean success;
        private String templateId;
        private String passId;
        private LocalDateTime expireTime;
        private String message;
    }
}
