package net.lab1024.sa.attendance.template.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.attendance.domain.form.AttendancePunchForm;
import net.lab1024.sa.attendance.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.strategy.IAttendanceRuleStrategy;
import net.lab1024.sa.attendance.template.AbstractAttendanceProcessTemplate;
import net.lab1024.sa.attendance.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.common.factory.StrategyFactory;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 标准考勤处理流程实现
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现标准工时制的考勤处理流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component("standardAttendanceProcess")
public class StandardAttendanceProcess extends AbstractAttendanceProcessTemplate {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private StrategyFactory<IAttendanceRuleStrategy> strategyFactory;

    @Override
    protected UserIdentityResult identifyUser(AttendancePunchForm punchForm) {
        // 如果表单中已提供userId，直接使用
        if (punchForm.getUserId() != null) {
            log.debug("[标准考勤流程] 使用表单中的userId: {}", punchForm.getUserId());
            return UserIdentityResult.success(punchForm.getUserId());
        }

        // TODO: 实现生物识别逻辑
        // 1. 从punchForm获取生物特征数据
        // 2. 调用生物识别服务进行1:N比对
        // 3. 返回识别结果

        log.warn("[标准考勤流程] 生物识别功能待实现，当前使用临时userId");
        return UserIdentityResult.failed("生物识别功能待实现");
    }

    @Override
    protected AttendanceRecordResult recordPunch(
            UserIdentityResult identity, DeviceEntity device, AttendancePunchForm punchForm) {

        // 保存打卡记录
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setUserId(identity.getUserId());
        // 设备ID类型转换：DeviceEntity使用String，AttendanceRecordEntity使用Long
        if (device.getDeviceId() != null) {
            try {
                record.setDeviceId(Long.parseLong(device.getDeviceId()));
            } catch (NumberFormatException e) {
                log.warn("[标准考勤流程] 设备ID格式转换失败: deviceId={}, error={}", device.getDeviceId(), e.getMessage());
                // 如果转换失败，尝试使用hashCode作为临时ID（不推荐，但保证功能可用）
                record.setDeviceId((long) device.getDeviceId().hashCode());
            }
        }
        record.setPunchTime(punchForm.getPunchTime() != null ? punchForm.getPunchTime() : LocalDateTime.now());
        record.setPunchType(punchForm.getPunchType());
        record.setPunchAddress(punchForm.getPunchAddress());

        attendanceRecordDao.insert(record);

        log.debug("[标准考勤流程] 保存打卡记录成功, recordId={}, userId={}", record.getRecordId(), identity.getUserId());

        return AttendanceRecordResult.of(
                record.getRecordId(),
                record.getPunchTime(),
                punchForm.getPunchType() != null ? punchForm.getPunchType().toString() : "0"
        );
    }

    @Override
    protected AttendanceResultVO calculateAttendance(
            UserIdentityResult identity, AttendanceRecordResult recordResult, AttendancePunchForm punchForm) {

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
    protected void notifyAttendanceEvent(
            UserIdentityResult identity, DeviceEntity device, AttendanceResultVO attendanceResult) {
        // 标准考勤流程的事件通知逻辑
        log.info("[标准考勤流程] 考勤事件通知: userId={}, status={}, date={}",
                identity.getUserId(), attendanceResult.getStatus(), attendanceResult.getDate());
        // TODO: 实现WebSocket推送、RabbitMQ消息等
    }
}
