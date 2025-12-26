package net.lab1024.sa.visitor.strategy.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.entity.VisitorAppointmentEntity;
import net.lab1024.sa.visitor.entity.VisitorEntity;
import net.lab1024.sa.visitor.manager.VisitorAppointmentManager;
import net.lab1024.sa.visitor.strategy.IVisitorVerificationStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 临时访客验证策略
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现中心实时验证模式：临时访客必须通过中心验证
 * </p>
 *
 * <p><strong>验证流程：</strong></p>
 * <ol>
 *   <li>查询访客有效预约记录</li>
 *   <li>验证预约状态（已通过审批）</li>
 *   <li>检查访问时间范围</li>
 *   <li>检查黑名单状态</li>
 *   <li>统计访问次数（可选）</li>
 *   <li>更新签到时间</li>
 *   <li>返回验证结果（生成临时模板ID）</li>
 * </ol>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
@StrategyMarker(name = "TEMPORARY_VISITOR", type = "VISITOR_VERIFICATION", priority = 100)
@Slf4j
public class TemporaryVisitorStrategy implements IVisitorVerificationStrategy {

    @Resource
    private VisitorAppointmentManager visitorAppointmentManager;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 最大访问次数（可配置）
     */
    private static final int MAX_VISIT_COUNT = 10;

    @Override
    public String getStrategyName() {
        return "临时访客验证策略";
    }

    @Override
    public VisitorVerificationResult verify(Long visitorId, String verificationData) {
        log.info("[临时访客策略] 开始中心验证: visitorId={}", visitorId);

        try {
            // 1. 查询访客信息
            VisitorEntity visitor = getVisitorInfo(visitorId);
            if (visitor == null) {
                log.warn("[临时访客策略] 访客不存在: visitorId={}", visitorId);
                return VisitorVerificationResult.failed("访客不存在");
            }

            // 2. 检查黑名单
            if (visitorAppointmentManager.isVisitorBlacklisted(visitor)) {
                log.warn("[临时访客策略] 访客在黑名单: visitorId={}, reason={}",
                        visitorId, visitor.getBlacklistReason());
                return VisitorVerificationResult.failed("访客在黑名单，禁止访问：" + visitor.getBlacklistReason());
            }

            // 3. 查询有效预约
            List<VisitorAppointmentEntity> validAppointments = visitorAppointmentManager.getValidAppointments(visitorId);
            if (validAppointments == null || validAppointments.isEmpty()) {
                log.warn("[临时访客策略] 无有效预约: visitorId={}", visitorId);
                return VisitorVerificationResult.failed("无有效预约，请先申请访客预约");
            }

            // 4. 验证预约状态（使用最新的预约）
            VisitorAppointmentEntity appointment = validAppointments.get(0);
            if (!visitorAppointmentManager.validateAppointmentStatus(appointment)) {
                log.warn("[临时访客策略] 预约状态无效: appointmentId={}, status={}",
                        appointment.getAppointmentId(), appointment.getStatus());
                return VisitorVerificationResult.failed("预约状态无效：" + getAppointmentStatusMessage(appointment.getStatus()));
            }

            // 5. 检查访问次数限制
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = appointment.getAppointmentStartTime();
            LocalDateTime endTime = appointment.getAppointmentEndTime();
            int visitCount = visitorAppointmentManager.countVisitsWithinTimeRange(visitorId, startTime, endTime);

            if (visitCount >= MAX_VISIT_COUNT) {
                log.warn("[临时访客策略] 访问次数超限: visitorId={}, count={}, limit={}",
                        visitorId, visitCount, MAX_VISIT_COUNT);
                return VisitorVerificationResult.failed("访问次数已达上限：" + MAX_VISIT_COUNT + "次");
            }

            // 6. 更新签到时间
            boolean checkInSuccess = visitorAppointmentManager.updateCheckInTime(appointment.getAppointmentId());
            if (!checkInSuccess) {
                log.error("[临时访客策略] 更新签到时间失败: appointmentId={}",
                        appointment.getAppointmentId());
                // 签到失败不影响验证结果，只记录日志
            }

            // 7. 生成临时模板ID（用于下发到设备）
            String templateId = generateTemporaryTemplateId(visitorId, appointment.getAppointmentId());

            log.info("[临时访客策略] 验证成功: visitorId={}, appointmentId={}, templateId={}",
                    visitorId, appointment.getAppointmentId(), templateId);

            return VisitorVerificationResult.success(templateId, null);

        } catch (Exception e) {
            log.error("[临时访客策略] 验证异常: visitorId={}, error={}",
                    visitorId, e.getMessage(), e);
            return VisitorVerificationResult.failed("验证异常：" + e.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return 100; // 临时访客策略优先级最高
    }

    @Override
    public String getStrategyType() {
        return "TEMPORARY_VISITOR";
    }

    /**
     * 获取访客信息
     *
     * @param visitorId 访客ID
     * @return 访客实体
     */
    private VisitorEntity getVisitorInfo(Long visitorId) {
        log.debug("[临时访客策略] 获取访客信息: visitorId={}", visitorId);

        return visitorAppointmentManager.getVisitorById(visitorId);
    }

    /**
     * 生成临时模板ID
     * <p>
     * 格式：TEMP_TEMPLATE_{visitorId}_{appointmentId}_{timestamp}
     * </p>
     *
     * @param visitorId 访客ID
     * @param appointmentId 预约ID
     * @return 临时模板ID
     */
    private String generateTemporaryTemplateId(Long visitorId, Long appointmentId) {
        long timestamp = System.currentTimeMillis();
        return String.format("TEMP_TEMPLATE_%d_%d_%d", visitorId, appointmentId, timestamp);
    }

    /**
     * 获取预约状态消息
     *
     * @param status 预约状态
     * @return 状态消息
     */
    private String getAppointmentStatusMessage(String status) {
        if (status == null) {
            return "状态未知";
        }

        switch (status) {
            case "PENDING":
                return "预约待审批";
            case "APPROVED":
                return "预约已通过";
            case "REJECTED":
                return "预约已驳回";
            case "CANCELLED":
                return "预约已取消";
            case "CHECKED_IN":
                return "已签到";
            case "CHECKED_OUT":
                return "已签退";
            default:
                return "状态：" + status;
        }
    }

    /**
     * 解析验证数据
     * <p>
     * verificationData可能是JSON格式的验证信息
     * </p>
     *
     * @param verificationData 验证数据
     * @return 解析后的数据Map
     */
    private Map<String, Object> parseVerificationData(String verificationData) {
        if (verificationData == null || verificationData.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(verificationData, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("[临时访客策略] 解析验证数据失败: data={}, error={}",
                    verificationData, e.getMessage());
            return null;
        }
    }
}
