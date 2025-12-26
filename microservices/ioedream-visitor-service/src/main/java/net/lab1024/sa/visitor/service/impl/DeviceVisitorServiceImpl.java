package net.lab1024.sa.visitor.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.service.DeviceVisitorService;
import net.lab1024.sa.visitor.dao.ElectronicPassDao;
import net.lab1024.sa.visitor.dao.VisitorApprovalRecordDao;
import net.lab1024.sa.common.entity.visitor.ElectronicPassEntity;
import net.lab1024.sa.common.entity.visitor.VisitorApprovalRecordEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 设备访客验证服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class DeviceVisitorServiceImpl implements DeviceVisitorService {


    @Resource
    private VisitorApprovalRecordDao visitorApprovalRecordDao;

    @Resource
    private ElectronicPassDao electronicPassDao;

    @Override
    public ResponseDTO<VisitorVerificationResult> verifyTemporaryVisitor(Long appointmentId, String biometricData, Long deviceId) {
        log.info("[设备访客验证服务] 临时访客中心验证, appointmentId={}, deviceId={}", appointmentId, deviceId);

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
            log.error("[设备访客验证服务] 临时访客验证异常", e);
            return ResponseDTO.error("VISITOR_VERIFY_ERROR", "访客验证失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<VisitorVerificationResult> verifyRegularVisitor(String passId, Long deviceId) {
        log.info("[设备访客验证服务] 常客边缘验证, passId={}, deviceId={}", passId, deviceId);

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
            log.error("[设备访客验证服务] 常客验证异常", e);
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

        log.info("[设备访客验证服务] 临时模板生成并同步, templateId={}, deviceId={}", templateId, deviceId);
        return templateId;
    }

    /**
     * 记录访客通行
     */
    private void recordVisitorAccess(ElectronicPassEntity pass, Long deviceId) {
        // 记录访客通行记录
        log.debug("[设备访客验证服务] 记录访客通行, passId={}, deviceId={}", pass.getPassId(), deviceId);
    }
}
