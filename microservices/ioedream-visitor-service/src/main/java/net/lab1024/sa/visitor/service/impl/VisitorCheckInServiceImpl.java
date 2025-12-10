package net.lab1024.sa.visitor.service.impl;

import java.time.LocalDateTime;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.service.VisitorCheckInService;
import net.lab1024.sa.visitor.domain.entity.VisitorAppointmentEntity;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;

/**
 * 访客签到服务实现类
 * <p>
 * 实现访客签到管理的核心业务功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识服务类
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 遵循四层架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 访客预约签到
 * - 访客预约签退
 * - 签到记录管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VisitorCheckInServiceImpl implements VisitorCheckInService {

    @Resource
    private VisitorAppointmentDao visitorAppointmentDao;

    /**
     * 访客签到
     *
     * @param appointmentId 预约ID
     * @return 签到结果
     */
    @Override
    public ResponseDTO<Void> checkIn(Long appointmentId) {
        log.info("[访客签到] 开始签到，appointmentId={}", appointmentId);

        try {
            // 参数验证
            if (appointmentId == null) {
                return ResponseDTO.error("PARAM_ERROR", "预约ID不能为空");
            }

            // 查询预约信息
            VisitorAppointmentEntity appointment = visitorAppointmentDao.selectById(appointmentId);
            if (appointment == null) {
                return ResponseDTO.error("APPOINTMENT_NOT_FOUND", "预约不存在");
            }

            // 验证预约状态
            if (!"APPROVED".equals(appointment.getStatus())) {
                return ResponseDTO.error("APPOINTMENT_NOT_APPROVED", "预约未通过审核");
            }

            // 检查是否已经签到
            if (appointment.getCheckInTime() != null) {
                return ResponseDTO.error("ALREADY_CHECKED_IN", "已经签到过了");
            }

            // 更新签到时间
            appointment.setCheckInTime(LocalDateTime.now());
            appointment.setUpdateTime(LocalDateTime.now());
            visitorAppointmentDao.updateById(appointment);

            log.info("[访客签到] 签到成功，appointmentId={}", appointmentId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[访客签到] 签到异常，appointmentId={}", appointmentId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "签到失败：" + e.getMessage());
        }
    }

    /**
     * 访客签退
     *
     * @param appointmentId 预约ID
     * @return 签退结果
     */
    @Override
    public ResponseDTO<Void> checkOut(Long appointmentId) {
        log.info("[访客签退] 开始签退，appointmentId={}", appointmentId);

        try {
            // 参数验证
            if (appointmentId == null) {
                return ResponseDTO.error("PARAM_ERROR", "预约ID不能为空");
            }

            // 查询预约信息
            VisitorAppointmentEntity appointment = visitorAppointmentDao.selectById(appointmentId);
            if (appointment == null) {
                return ResponseDTO.error("APPOINTMENT_NOT_FOUND", "预约不存在");
            }

            // 验证是否已经签到
            if (appointment.getCheckInTime() == null) {
                return ResponseDTO.error("NOT_CHECKED_IN", "还未签到，不能签退");
            }

            // 检查是否已经签退
            if ("CHECKED_OUT".equals(appointment.getStatus())) {
                return ResponseDTO.error("ALREADY_CHECKED_OUT", "已经签退过了");
            }

            // 更新签退时间和状态
            appointment.setUpdateTime(LocalDateTime.now());
            appointment.setStatus("CHECKED_OUT"); // 已签退
            visitorAppointmentDao.updateById(appointment);

            log.info("[访客签退] 签退成功，appointmentId={}", appointmentId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[访客签退] 签退异常，appointmentId={}", appointmentId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "签退失败：" + e.getMessage());
        }
    }
}
