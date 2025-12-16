package net.lab1024.sa.visitor.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;
import net.lab1024.sa.visitor.domain.entity.VisitorAppointmentEntity;
import net.lab1024.sa.visitor.service.VisitorStatisticsService;

/**
 * 访客统计服务实现类
 * <p>
 * 实现访客数据统计分析功能
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-visitor-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VisitorStatisticsServiceImpl implements VisitorStatisticsService {

    @Resource
    private VisitorAppointmentDao visitorAppointmentDao;

    @Override
    @Observed(name = "visitor.statistics.getStatistics", contextualName = "visitor-statistics-get-statistics")
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getStatistics() {
        log.info("[访客统计] 获取访客统计数据");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 统计总预约数
            long totalAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0));

            // 统计已通过预约数
            long approvedAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .eq(VisitorAppointmentEntity::getStatus, "APPROVED"));

            // 统计已驳回预约数
            long rejectedAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .eq(VisitorAppointmentEntity::getStatus, "REJECTED"));

            // 统计待审批预约数
            long pendingAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .eq(VisitorAppointmentEntity::getStatus, "PENDING"));

            // 统计今日预约数
            LocalDate today = LocalDate.now();
            long todayAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .ge(VisitorAppointmentEntity::getAppointmentStartTime, today.atStartOfDay())
                            .le(VisitorAppointmentEntity::getAppointmentStartTime, today.atTime(23, 59, 59)));

            // 统计本月预约数
            LocalDate monthStart = today.withDayOfMonth(1);
            long monthAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .ge(VisitorAppointmentEntity::getAppointmentStartTime, monthStart.atStartOfDay()));

            // 构建统计数据
            statistics.put("totalAppointments", totalAppointments);
            statistics.put("approvedAppointments", approvedAppointments);
            statistics.put("rejectedAppointments", rejectedAppointments);
            statistics.put("pendingAppointments", pendingAppointments);
            statistics.put("todayAppointments", todayAppointments);
            statistics.put("monthAppointments", monthAppointments);

            // 计算通过率
            if (totalAppointments > 0) {
                double approvalRate = (double) approvedAppointments / totalAppointments * 100;
                statistics.put("approvalRate", Math.round(approvalRate * 100.0) / 100.0);
            } else {
                statistics.put("approvalRate", 0.0);
            }

            log.info("[访客统计] 获取访客统计数据成功，总预约数={}, 已通过={}, 待审批={}",
                    totalAppointments, approvedAppointments, pendingAppointments);
            return ResponseDTO.ok(statistics);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客统计] 参数错误，获取访客统计数据失败", e);
            throw new ParamException("GET_STATISTICS_PARAM_ERROR", "参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[访客统计] 业务异常，获取访客统计数据失败", e);
            throw e;
        } catch (Exception e) {
            log.error("[访客统计] 系统异常，获取访客统计数据失败", e);
            throw new SystemException("GET_STATISTICS_SYSTEM_ERROR", "获取统计数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "visitor.statistics.getStatisticsByDateRange", contextualName = "visitor-statistics-get-by-date-range")
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("[访客统计] 根据时间范围获取访客统计数据，startDate={}, endDate={}", startDate, endDate);

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 构建时间范围
            LocalDateTime startTime = startDate.atStartOfDay();
            LocalDateTime endTime = endDate.atTime(23, 59, 59);

            // 统计时间范围内的预约数
            long totalAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .ge(VisitorAppointmentEntity::getAppointmentStartTime, startTime)
                            .le(VisitorAppointmentEntity::getAppointmentStartTime, endTime));

            // 统计已通过预约数
            long approvedAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .eq(VisitorAppointmentEntity::getStatus, "APPROVED")
                            .ge(VisitorAppointmentEntity::getAppointmentStartTime, startTime)
                            .le(VisitorAppointmentEntity::getAppointmentStartTime, endTime));

            // 统计已驳回预约数
            long rejectedAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .eq(VisitorAppointmentEntity::getStatus, "REJECTED")
                            .ge(VisitorAppointmentEntity::getAppointmentStartTime, startTime)
                            .le(VisitorAppointmentEntity::getAppointmentStartTime, endTime));

            // 统计待审批预约数
            long pendingAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .eq(VisitorAppointmentEntity::getStatus, "PENDING")
                            .ge(VisitorAppointmentEntity::getAppointmentStartTime, startTime)
                            .le(VisitorAppointmentEntity::getAppointmentStartTime, endTime));

            // 构建统计数据
            statistics.put("totalAppointments", totalAppointments);
            statistics.put("approvedAppointments", approvedAppointments);
            statistics.put("rejectedAppointments", rejectedAppointments);
            statistics.put("pendingAppointments", pendingAppointments);
            statistics.put("startDate", startDate);
            statistics.put("endDate", endDate);

            // 计算通过率
            if (totalAppointments > 0) {
                double approvalRate = (double) approvedAppointments / totalAppointments * 100;
                statistics.put("approvalRate", Math.round(approvalRate * 100.0) / 100.0);
            } else {
                statistics.put("approvalRate", 0.0);
            }

            log.info("[访客统计] 根据时间范围获取访客统计数据成功，总预约数={}, 已通过={}, 待审批={}",
                    totalAppointments, approvedAppointments, pendingAppointments);
            return ResponseDTO.ok(statistics);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客统计] 参数错误，根据时间范围获取访客统计数据失败", e);
            throw new ParamException("GET_STATISTICS_PARAM_ERROR", "参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[访客统计] 业务异常，根据时间范围获取访客统计数据失败", e);
            throw e;
        } catch (Exception e) {
            log.error("[访客统计] 系统异常，根据时间范围获取访客统计数据失败", e);
            throw new SystemException("GET_STATISTICS_SYSTEM_ERROR", "获取统计数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "visitor.statistics.getPersonalStatistics", contextualName = "visitor-statistics-get-personal")
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getPersonalStatistics(Long userId) {
        log.info("[访客统计] 获取个人访问统计，userId={}", userId);

        try {
            Map<String, Object> statistics = new HashMap<>();

            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);

            // 统计今日预约数（作为被访人）
            long todayAppointments = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .eq(VisitorAppointmentEntity::getVisitUserId, userId)
                            .ge(VisitorAppointmentEntity::getAppointmentStartTime, today.atStartOfDay())
                            .le(VisitorAppointmentEntity::getAppointmentStartTime, today.atTime(23, 59, 59)));

            // 统计当前在访访客数（已签到未签退）
            long activeVisitors = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .eq(VisitorAppointmentEntity::getVisitUserId, userId)
                            .eq(VisitorAppointmentEntity::getStatus, "CHECKED_IN"));

            // 统计本月访问数
            long monthlyVisitors = visitorAppointmentDao.selectCount(
                    new LambdaQueryWrapper<VisitorAppointmentEntity>()
                            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
                            .eq(VisitorAppointmentEntity::getVisitUserId, userId)
                            .ge(VisitorAppointmentEntity::getAppointmentStartTime, monthStart.atStartOfDay()));

            // 平均停留时长（模拟值，实际需要从签到签退记录计算）
            int averageDuration = 45;

            // 构建统计数据
            statistics.put("todayAppointments", todayAppointments);
            statistics.put("activeVisitors", activeVisitors);
            statistics.put("monthlyVisitors", monthlyVisitors);
            statistics.put("averageDuration", averageDuration);

            log.info("[访客统计] 获取个人访问统计成功，userId={}, todayAppointments={}, activeVisitors={}",
                    userId, todayAppointments, activeVisitors);
            return ResponseDTO.ok(statistics);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客统计] 参数错误，获取个人访问统计失败，userId={}", userId, e);
            throw new ParamException("GET_PERSONAL_STATISTICS_PARAM_ERROR", "参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[访客统计] 业务异常，获取个人访问统计失败，userId={}", userId, e);
            throw e;
        } catch (Exception e) {
            log.error("[访客统计] 系统异常，获取个人访问统计失败，userId={}", userId, e);
            throw new SystemException("GET_PERSONAL_STATISTICS_SYSTEM_ERROR", "获取个人统计数据失败: " + e.getMessage(), e);
        }
    }
}


