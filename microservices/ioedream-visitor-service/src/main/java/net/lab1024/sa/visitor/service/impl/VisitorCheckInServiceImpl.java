package net.lab1024.sa.visitor.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.visitor.service.VisitorCheckInService;
import net.lab1024.sa.visitor.entity.VisitorAppointmentEntity;
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
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
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
    @Observed(name = "visitor.checkin.checkIn", contextualName = "visitor-checkin-check-in")
    @CircuitBreaker(name = "visitor-checkin-circuitbreaker", fallbackMethod = "checkInFallback")
    @Retry(name = "visitor-checkin-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "visitor.checkin", description = "访客签到耗时")
    @Counted(value = "visitor.checkin.count", description = "访客签到次数")
    public ResponseDTO<Void> checkIn(Long appointmentId) {
        log.info("[访客签到] 开始签到，appointmentId={}", appointmentId);

        try {
            // 参数验证
            if (appointmentId == null) {
                throw new ParamException("PARAM_ERROR", "预约ID不能为空");
            }

            // 查询预约信息
            VisitorAppointmentEntity appointment = visitorAppointmentDao.selectById(appointmentId);
            if (appointment == null) {
                throw new BusinessException("APPOINTMENT_NOT_FOUND", "预约不存在");
            }

            // 验证预约状态
            if (!"APPROVED".equals(appointment.getStatus())) {
                throw new BusinessException("APPOINTMENT_NOT_APPROVED", "预约未通过审核");
            }

            // 检查是否已经签到
            if (appointment.getCheckInTime() != null) {
                throw new BusinessException("ALREADY_CHECKED_IN", "已经签到过了");
            }

            // 更新签到时间
            appointment.setCheckInTime(LocalDateTime.now());
            appointment.setUpdateTime(LocalDateTime.now());
            visitorAppointmentDao.updateById(appointment);

            log.info("[访客签到] 签到成功，appointmentId={}", appointmentId);
            return ResponseDTO.ok();

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客签到] 签到参数错误，appointmentId={}", appointmentId, e);
            throw new ParamException("CHECKIN_PARAM_ERROR", "签到参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[访客签到] 签到业务异常，appointmentId={}", appointmentId, e);
            throw e;
        } catch (Exception e) {
            log.error("[访客签到] 签到系统异常，appointmentId={}", appointmentId, e);
            throw new SystemException("CHECKIN_ERROR", "签到失败: " + e.getMessage(), e);
        }
    }

    /**
     * 访客签退
     *
     * @param appointmentId 预约ID
     * @return 签退结果
     */
    @Override
    @Observed(name = "visitor.checkin.checkOut", contextualName = "visitor-checkin-check-out")
    @CircuitBreaker(name = "visitor-checkout-circuitbreaker", fallbackMethod = "checkOutFallback")
    @Retry(name = "visitor-checkout-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "visitor.checkout", description = "访客签退耗时")
    @Counted(value = "visitor.checkout.count", description = "访客签退次数")
    public ResponseDTO<Void> checkOut(Long appointmentId) {
        log.info("[访客签退] 开始签退，appointmentId={}", appointmentId);

        try {
            // 参数验证
            if (appointmentId == null) {
                throw new ParamException("PARAM_ERROR", "预约ID不能为空");
            }

            // 查询预约信息
            VisitorAppointmentEntity appointment = visitorAppointmentDao.selectById(appointmentId);
            if (appointment == null) {
                throw new BusinessException("APPOINTMENT_NOT_FOUND", "预约不存在");
            }

            // 验证是否已经签到
            if (appointment.getCheckInTime() == null) {
                throw new BusinessException("NOT_CHECKED_IN", "还未签到，不能签退");
            }

            // 检查是否已经签退
            if ("CHECKED_OUT".equals(appointment.getStatus())) {
                throw new BusinessException("ALREADY_CHECKED_OUT", "已经签退过了");
            }

            // 更新签退时间和状态
            appointment.setUpdateTime(LocalDateTime.now());
            appointment.setStatus("CHECKED_OUT"); // 已签退
            visitorAppointmentDao.updateById(appointment);

            log.info("[访客签退] 签退成功，appointmentId={}", appointmentId);
            return ResponseDTO.ok();

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客签退] 签退参数错误，appointmentId={}", appointmentId, e);
            throw new ParamException("CHECKOUT_PARAM_ERROR", "签退参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[访客签退] 签退业务异常，appointmentId={}", appointmentId, e);
            throw e;
        } catch (Exception e) {
            log.error("[访客签退] 签退系统异常，appointmentId={}", appointmentId, e);
            throw new SystemException("CHECKOUT_ERROR", "签退失败: " + e.getMessage(), e);
        }
    }

    /**
     * 访客签到降级方法
     */
    public ResponseDTO<Void> checkInFallback(Long appointmentId, Exception ex) {
        log.error("[访客签到] 签到降级，appointmentId={}, error={}", appointmentId, ex.getMessage());
        return ResponseDTO.error("CHECKIN_DEGRADED", "系统繁忙，请稍后重试");
    }

    /**
     * 访客签退降级方法
     */
    public ResponseDTO<Void> checkOutFallback(Long appointmentId, Exception ex) {
        log.error("[访客签退] 签退降级，appointmentId={}, error={}", appointmentId, ex.getMessage());
        return ResponseDTO.error("CHECKOUT_DEGRADED", "系统繁忙，请稍后重试");
    }
}
