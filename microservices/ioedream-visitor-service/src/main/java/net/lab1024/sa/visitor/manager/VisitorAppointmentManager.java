package net.lab1024.sa.visitor.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;
import net.lab1024.sa.visitor.dao.VisitorDao;
import net.lab1024.sa.common.entity.visitor.VisitorAppointmentEntity;
import net.lab1024.sa.common.entity.visitor.VisitorEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客预约管理器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * Manager层负责业务编排和复杂业务逻辑处理
 * </p>
 * <p>
 * 核心职责：
 * - 预约记录查询和验证
 * - 访问时间范围检查
 * - 访问次数统计
 * - 预约状态管理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class VisitorAppointmentManager {

    @Resource
    private VisitorAppointmentDao visitorAppointmentDao;

    @Resource
    private VisitorDao visitorDao;

    /**
     * 根据访客ID查询有效预约
     * <p>
     * 查询访客当前时间范围内的有效预约记录
     * </p>
     *
     * @param visitorId 访客ID
     * @return 有效预约列表
     */
    public List<VisitorAppointmentEntity> getValidAppointments(Long visitorId) {
        log.debug("[访客预约] 查询有效预约: visitorId={}", visitorId);

        // 查询当前时间范围内的有效预约
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<VisitorAppointmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorAppointmentEntity::getVisitUserId, visitorId)
                .eq(VisitorAppointmentEntity::getStatus, "APPROVED") // 已通过审批
                .le(VisitorAppointmentEntity::getAppointmentStartTime, now) // 预约开始时间 <= 当前时间
                .ge(VisitorAppointmentEntity::getAppointmentEndTime, now) // 预约结束时间 >= 当前时间
                .orderByDesc(VisitorAppointmentEntity::getAppointmentStartTime);

        List<VisitorAppointmentEntity> appointments = visitorAppointmentDao.selectList(queryWrapper);
        log.debug("[访客预约] 查询到{}条有效预约", appointments.size());

        return appointments;
    }

    /**
     * 根据预约ID查询预约详情
     *
     * @param appointmentId 预约ID
     * @return 预约实体
     */
    public VisitorAppointmentEntity getAppointmentById(Long appointmentId) {
        log.debug("[访客预约] 查询预约详情: appointmentId={}", appointmentId);

        VisitorAppointmentEntity appointment = visitorAppointmentDao.selectById(appointmentId);
        if (appointment == null) {
            log.warn("[访客预约] 预约不存在: appointmentId={}", appointmentId);
        }

        return appointment;
    }

    /**
     * 验证预约状态
     * <p>
     * 检查预约是否审批通过且在有效期内
     * </p>
     *
     * @param appointment 预约实体
     * @return 验证结果
     */
    public boolean validateAppointmentStatus(VisitorAppointmentEntity appointment) {
        if (appointment == null) {
            log.warn("[访客预约] 预约为空");
            return false;
        }

        // 检查预约状态
        if (!"APPROVED".equals(appointment.getStatus())) {
            log.warn("[访客预约] 预约状态无效: status={}", appointment.getStatus());
            return false;
        }

        // 检查时间范围
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getAppointmentStartTime())) {
            log.warn("[访客预约] 访问时间未到: now={}, startTime={}",
                    now, appointment.getAppointmentStartTime());
            return false;
        }

        if (now.isAfter(appointment.getAppointmentEndTime())) {
            log.warn("[访客预约] 访问时间已过期: now={}, endTime={}",
                    now, appointment.getAppointmentEndTime());
            return false;
        }

        log.info("[访客预约] 预约状态有效: appointmentId={}", appointment.getAppointmentId());
        return true;
    }

    /**
     * 检查访问次数限制
     * <p>
     * 统计访客在指定时间范围内的访问次数
     * 防止临时访客超范围访问
     * </p>
     *
     * @param visitorId 访客ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 访问次数
     */
    public int countVisitsWithinTimeRange(Long visitorId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[访客预约] 统计访问次数: visitorId={}, startTime={}, endTime={}",
                visitorId, startTime, endTime);

        LambdaQueryWrapper<VisitorAppointmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorAppointmentEntity::getVisitUserId, visitorId)
                .eq(VisitorAppointmentEntity::getStatus, "CHECKED_IN") // 已签到
                .ge(VisitorAppointmentEntity::getCheckInTime, startTime)
                .le(VisitorAppointmentEntity::getCheckInTime, endTime);

        Long count = visitorAppointmentDao.selectCount(queryWrapper);
        log.debug("[访客预约] 访问次数统计结果: count={}", count);

        return count.intValue();
    }

    /**
     * 更新预约签到时间
     *
     * @param appointmentId 预约ID
     * @return 更新是否成功
     */
    public boolean updateCheckInTime(Long appointmentId) {
        log.info("[访客预约] 更新签到时间: appointmentId={}", appointmentId);

        VisitorAppointmentEntity appointment = visitorAppointmentDao.selectById(appointmentId);
        if (appointment == null) {
            log.warn("[访客预约] 预约不存在: appointmentId={}", appointmentId);
            return false;
        }

        appointment.setCheckInTime(LocalDateTime.now());
        appointment.setStatus("CHECKED_IN");

        int rows = visitorAppointmentDao.updateById(appointment);
        boolean success = rows > 0;

        if (success) {
            log.info("[访客预约] 签到时间更新成功: appointmentId={}", appointmentId);
        } else {
            log.error("[访客预约] 签到时间更新失败: appointmentId={}", appointmentId);
        }

        return success;
    }

    /**
     * 更新预约签退时间
     *
     * @param appointmentId 预约ID
     * @return 更新是否成功
     */
    public boolean updateCheckOutTime(Long appointmentId) {
        log.info("[访客预约] 更新签退时间: appointmentId={}", appointmentId);

        VisitorAppointmentEntity appointment = visitorAppointmentDao.selectById(appointmentId);
        if (appointment == null) {
            log.warn("[访客预约] 预约不存在: appointmentId={}", appointmentId);
            return false;
        }

        appointment.setStatus("CHECKED_OUT");

        int rows = visitorAppointmentDao.updateById(appointment);
        boolean success = rows > 0;

        if (success) {
            log.info("[访客预约] 签退时间更新成功: appointmentId={}", appointmentId);
        } else {
            log.error("[访客预约] 签退时间更新失败: appointmentId={}", appointmentId);
        }

        return success;
    }

    /**
     * 根据手机号查询访客
     *
     * @param phoneNumber 手机号
     * @return 访客实体
     */
    public VisitorEntity getVisitorByPhone(String phoneNumber) {
        log.debug("[访客预约] 根据手机号查询访客: phone={}", phoneNumber);

        LambdaQueryWrapper<VisitorEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitorEntity::getPhone, phoneNumber)
                .eq(VisitorEntity::getShelvesFlag, true); // 启用状态

        VisitorEntity visitor = visitorDao.selectOne(queryWrapper);

        if (visitor == null) {
            log.warn("[访客预约] 访客不存在: phone={}", phoneNumber);
        }

        return visitor;
    }

    /**
     * 检查访客是否在黑名单
     *
     * @param visitor 访客实体
     * @return 是否在黑名单
     */
    public boolean isVisitorBlacklisted(VisitorEntity visitor) {
        if (visitor == null) {
            return false;
        }

        boolean blacklisted = visitor.getBlacklisted() != null && visitor.getBlacklisted() == 1;

        if (blacklisted) {
            log.warn("[访客预约] 访客在黑名单: visitorId={}, reason={}",
                    visitor.getVisitorId(), visitor.getBlacklistReason());
        }

        return blacklisted;
    }

    /**
     * 根据访客ID查询访客信息
     *
     * @param visitorId 访客ID
     * @return 访客实体
     */
    public VisitorEntity getVisitorById(Long visitorId) {
        log.debug("[访客预约] 根据ID查询访客: visitorId={}", visitorId);

        VisitorEntity visitor = visitorDao.selectById(visitorId);

        if (visitor == null) {
            log.warn("[访客预约] 访客不存在: visitorId={}", visitorId);
        }

        return visitor;
    }
}
