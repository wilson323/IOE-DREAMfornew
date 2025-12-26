package net.lab1024.sa.attendance.template.impl;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.domain.form.AttendancePunchForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;
import net.lab1024.sa.attendance.strategy.IAttendanceRuleStrategy;
import net.lab1024.sa.attendance.template.AbstractAttendanceProcessTemplate;
import net.lab1024.sa.common.factory.StrategyFactory;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.gateway.domain.response.DeviceResponse;
import org.springframework.http.HttpMethod;

/**
 * 标准考勤处理流程实现
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求 实现标准工时制的考勤处理流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component("standardAttendanceProcess")
@Slf4j
public class StandardAttendanceProcess extends AbstractAttendanceProcessTemplate {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private StrategyFactory<IAttendanceRuleStrategy> strategyFactory;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Override
    protected UserIdentityResult identifyUser(AttendancePunchForm punchForm) {
        // 如果表单中已提供userId，直接使用
        if (punchForm.getUserId() != null) {
            log.debug("[标准考勤流程] 使用表单中的userId: {}", punchForm.getUserId());
            return UserIdentityResult.success(punchForm.getUserId());
        }

        // 如果没有userId但提供了生物特征数据，进行生物识别
        if (punchForm.getBiometricData() != null && !punchForm.getBiometricData().isEmpty()) {
            log.info("[标准考勤流程] 开始生物识别: deviceId={}", punchForm.getDeviceId());

            try {
                // 调用生物识别服务进行1:N人脸识别
                Map<String, Object> params = new HashMap<>();
                params.put("faceImageData", punchForm.getBiometricData());
                params.put("deviceId", String.valueOf(punchForm.getDeviceId()));
                params.put("recognizeType", "1:N");

                BiometricResult result = gatewayServiceClient.callCommonService(
                    "/api/biometric/recognize-face",
                    HttpMethod.POST,
                    params,
                    BiometricResult.class
                );

                if (result != null && result.getMatchedUserId() != null) {
                    log.info("[标准考勤流程] 生物识别成功: userId={}, confidence={}",
                        result.getMatchedUserId(), result.getConfidence());
                    return UserIdentityResult.success(result.getMatchedUserId());
                } else {
                    log.warn("[标准考勤流程] 生物识别失败: 无法识别用户");
                    return UserIdentityResult.failed("生物识别失败：无法识别用户");
                }
            } catch (Exception e) {
                log.error("[标准考勤流程] 生物识别异常: error={}", e.getMessage(), e);
                return UserIdentityResult.error("生物识别异常: " + e.getMessage());
            }
        }

        // 既没有userId也没有生物特征数据，返回失败
        log.warn("[标准考勤流程] 缺少用户识别信息");
        return UserIdentityResult.failed("缺少用户识别信息：需要提供userId或生物特征数据");
    }

    @Override
    protected AttendanceRecordResult recordPunch(UserIdentityResult identity, DeviceResponse device,
            AttendancePunchForm punchForm) {

        // 保存打卡记录
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setUserId(identity.getUserId());
        // 设备ID直接使用，DeviceEntity和AttendanceRecordEntity都使用Long类型
        if (device.getDeviceId() != null) {
            record.setDeviceId(device.getDeviceId());
        }
        record.setPunchTime(punchForm.getPunchTime() != null ? punchForm.getPunchTime() : LocalDateTime.now());
        record.setPunchType(punchForm.getPunchType());
        record.setPunchAddress(punchForm.getPunchAddress());

        attendanceRecordDao.insert(record);

        log.debug("[标准考勤流程] 保存打卡记录成功, recordId={}, userId={}", record.getRecordId(), identity.getUserId());

        return AttendanceRecordResult.of(record.getRecordId(), record.getPunchTime(),
                punchForm.getPunchType() != null ? punchForm.getPunchType().toString() : "0");
    }

    @Override
    protected AttendanceResultVO calculateAttendance(UserIdentityResult identity, AttendanceRecordResult recordResult,
            AttendancePunchForm punchForm) {

        // 查询打卡记录
        AttendanceRecordEntity record = attendanceRecordDao.selectById(recordResult.getRecordId());
        if (record == null) {
            log.error("[标准考勤流程] 打卡记录不存在, recordId={}", recordResult.getRecordId());
            AttendanceResultVO result = new AttendanceResultVO();
            result.setUserId(identity.getUserId());
            result.setDate(recordResult.getPunchTime().toLocalDate());
            result.setStatus("ERROR");
            return result;
        }

        // 使用策略模式计算考勤结果
        List<IAttendanceRuleStrategy> strategies = strategyFactory.getAll(IAttendanceRuleStrategy.class);
        strategies.sort(Comparator.comparingInt(IAttendanceRuleStrategy::getPriority).reversed());

        // 使用第一个策略（优先级最高）计算考勤结果
        if (!strategies.isEmpty()) {
            IAttendanceRuleStrategy strategy = strategies.get(0);
            log.debug("[标准考勤流程] 使用策略计算考勤: {}", strategy.getRuleName());
            return strategy.calculate(record, null);
        }

        // 默认返回正常状态
        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(identity.getUserId());
        result.setDate(recordResult.getPunchTime().toLocalDate());
        result.setStatus("NORMAL");
        return result;
    }

    @Override
    protected void notifyAttendanceEvent(UserIdentityResult identity, DeviceResponse device,
            AttendanceResultVO attendanceResult) {
        // 标准考勤流程的事件通知逻辑
        log.info("[标准考勤流程] 考勤事件通知: userId={}, status={}, date={}", identity.getUserId(),
                attendanceResult.getStatus(), attendanceResult.getDate());
        // TODO: 实现WebSocket推送、RabbitMQ消息等
    }

    /**
     * 生物识别结果内部类
     */
    @lombok.Data
    public static class BiometricResult {
        /**
         * 匹配到的用户ID
         */
        private Long matchedUserId;

        /**
         * 置信度（0-100）
         */
        private Double confidence;
    }
}
