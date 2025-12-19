package net.lab1024.sa.attendance.decorator.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.decorator.IPunchExecutor;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 基础打卡执行器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现基础的打卡逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class BasicPunchExecutor implements IPunchExecutor {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Override
    public IPunchExecutor.PunchResult execute(IPunchExecutor.MobilePunchRequest request) {
        // 基础打卡逻辑
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setUserId(request.getUserId());
        record.setPunchTime(LocalDateTime.now());
        // 设置打卡类型：0-上班打卡，1-下班打卡
        Integer punchType = request.getPunchType() != null ? Integer.parseInt(request.getPunchType()) : 0;
        record.setPunchType(punchType);

        attendanceRecordDao.insert(record);

        log.debug("[基础打卡执行器] 打卡成功, userId={}, recordId={}", request.getUserId(), record.getRecordId());
        return IPunchExecutor.PunchResult.success(record);
    }
}
