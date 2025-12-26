package net.lab1024.sa.attendance.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import net.lab1024.sa.attendance.dao.ScheduleRecordDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.attendance.domain.entity.ScheduleRecordEntity;
import net.lab1024.sa.attendance.entity.WorkShiftEntity;

/**
 * Redis缓存预热服务
 * <p>
 * 在应用启动时预热常用数据到Redis缓存，提升移动端接口响应速度
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@ConditionalOnBean(RedisTemplate.class)  // 只有当RedisTemplate可用时才加载此服务
public class CacheWarmupService {


    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private WorkShiftDao workShiftDao;

    @Resource
    private ScheduleRecordDao scheduleRecordDao;

    /**
     * 缓存过期时间配置（秒）
     */
    private static final long WORK_SHIFT_CACHE_TTL = 24 * 60 * 60; // 24小时
    private static final long SCHEDULE_CACHE_TTL = 2 * 60 * 60; // 2小时
    private static final long EMPLOYEE_SHIFT_CACHE_TTL = 4 * 60 * 60; // 4小时

    /**
     * 应用启动完成后自动执行缓存预热
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCacheOnStartup() {
        log.info("[缓存预热] 开始执行Redis缓存预热...");

        try {
            long startTime = System.currentTimeMillis();

            // 1. 预热班次信息
            int shiftCount = warmupWorkShifts();

            // 2. 预热今日排班信息
            int scheduleCount = warmupTodaySchedules();

            // 3. 预热未来7天排班信息
            int futureScheduleCount = warmupFutureSchedules(7);

            long duration = System.currentTimeMillis() - startTime;

            log.info("[缓存预热] 缓存预热完成:班次={}, 今日排班={}, 未来排班={}, 耗时={}ms",
                    shiftCount, scheduleCount, futureScheduleCount, duration);

        } catch (Exception e) {
            log.error("[缓存预热] 缓存预热失败: error={}", e.getMessage(), e);
            // 预热失败不影响应用启动
        }
    }

    /**
     * 预热所有班次信息
     */
    private int warmupWorkShifts() {
        try {
            List<WorkShiftEntity> shifts = workShiftDao.selectList(
                    new LambdaQueryWrapper<WorkShiftEntity>()
                            .eq(WorkShiftEntity::getDeletedFlag, 0)
                            .orderByAsc(WorkShiftEntity::getShiftId)
            );

            int count = 0;
            for (WorkShiftEntity shift : shifts) {
                String key = "attendance:shift:" + shift.getShiftId();
                try {
                    redisTemplate.opsForValue().set(key, shift, WORK_SHIFT_CACHE_TTL, TimeUnit.SECONDS);
                    count++;
                } catch (Exception e) {
                    log.warn("[缓存预热] 班次缓存失败: shiftId={}, error={}", shift.getShiftId(), e.getMessage());
                }
            }

            log.info("[缓存预热] 班次信息预热完成: count={}", count);
            return count;

        } catch (Exception e) {
            log.error("[缓存预热] 班次信息预热异常: error={}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 预热今日排班信息
     */
    private int warmupTodaySchedules() {
        return warmupSchedulesByDateRange(LocalDate.now(), LocalDate.now());
    }

    /**
     * 预热未来N天排班信息
     */
    private int warmupFutureSchedules(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return warmupSchedulesByDateRange(today.plusDays(1), endDate);
    }

    /**
     * 按日期范围预热排班信息
     */
    private int warmupSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            List<ScheduleRecordEntity> schedules = scheduleRecordDao.selectList(
                    new LambdaQueryWrapper<ScheduleRecordEntity>()
                            .between(ScheduleRecordEntity::getScheduleDate, startDate, endDate)
                            .orderByAsc(ScheduleRecordEntity::getScheduleDate)
                            .orderByAsc(ScheduleRecordEntity::getEmployeeId)
            );

            int count = 0;
            for (ScheduleRecordEntity schedule : schedules) {
                String key = "attendance:schedule:" + schedule.getScheduleId();
                try {
                    redisTemplate.opsForValue().set(key, schedule, SCHEDULE_CACHE_TTL, TimeUnit.SECONDS);

                    // 预热员工班次映射关系（便于快速查询员工今日班次）
                    String employeeShiftKey = "attendance:employee:shift:" +
                            schedule.getEmployeeId() + ":" + schedule.getScheduleDate();
                    redisTemplate.opsForValue().set(employeeShiftKey, schedule, EMPLOYEE_SHIFT_CACHE_TTL, TimeUnit.SECONDS);

                    count++;
                } catch (Exception e) {
                    log.warn("[缓存预热] 排班缓存失败: scheduleId={}, error={}",
                            schedule.getScheduleId(), e.getMessage());
                }
            }

            log.info("[缓存预热] 排班信息预热完成: dateRange={}, count={}",
                    startDate + "~" + endDate, count);
            return count;

        } catch (Exception e) {
            log.error("[缓存预热] 排班信息预热异常: dateRange={}, error={}",
                    startDate + "~" + endDate, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 手动触发缓存预热（可用于定时刷新）
     */
    public void manualWarmup() {
        log.info("[缓存预热] 手动触发缓存预热...");
        warmupCacheOnStartup();
    }

    /**
     * 预热指定员工的排班信息
     *
     * @param employeeId 员工ID
     * @param days 预热天数
     * @return 预热的排班数量
     */
    public int warmupEmployeeSchedules(Long employeeId, int days) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(days);

            List<ScheduleRecordEntity> schedules = scheduleRecordDao.selectList(
                    new LambdaQueryWrapper<ScheduleRecordEntity>()
                            .eq(ScheduleRecordEntity::getEmployeeId, employeeId)
                            .between(ScheduleRecordEntity::getScheduleDate, today, endDate)
                            .orderByAsc(ScheduleRecordEntity::getScheduleDate)
            );

            int count = 0;
            for (ScheduleRecordEntity schedule : schedules) {
                String key = "attendance:schedule:" + schedule.getScheduleId();
                try {
                    redisTemplate.opsForValue().set(key, schedule, SCHEDULE_CACHE_TTL, TimeUnit.SECONDS);

                    String employeeShiftKey = "attendance:employee:shift:" +
                            schedule.getEmployeeId() + ":" + schedule.getScheduleDate();
                    redisTemplate.opsForValue().set(employeeShiftKey, schedule, EMPLOYEE_SHIFT_CACHE_TTL, TimeUnit.SECONDS);

                    count++;
                } catch (Exception e) {
                    log.warn("[缓存预热] 员工排班缓存失败: employeeId={}, scheduleId={}, error={}",
                            employeeId, schedule.getScheduleId(), e.getMessage());
                }
            }

            log.info("[缓存预热] 员工排班预热完成: employeeId={}, days={}, count={}",
                    employeeId, days, count);
            return count;

        } catch (Exception e) {
            log.error("[缓存预热] 员工排班预热异常: employeeId={}, error={}", employeeId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 清空所有预热缓存
     */
    public void clearWarmupCache() {
        try {
            // 清空班次缓存
            Set<String> shiftKeys = redisTemplate.keys("attendance:shift:*");
            if (shiftKeys != null && !shiftKeys.isEmpty()) {
                redisTemplate.delete(shiftKeys);
                log.info("[缓存预热] 清空班次缓存: count={}", shiftKeys.size());
            }

            // 清空排班缓存
            Set<String> scheduleKeys = redisTemplate.keys("attendance:schedule:*");
            if (scheduleKeys != null && !scheduleKeys.isEmpty()) {
                redisTemplate.delete(scheduleKeys);
                log.info("[缓存预热] 清空排班缓存: count={}", scheduleKeys.size());
            }

            // 清空员工班次映射缓存
            Set<String> employeeShiftKeys = redisTemplate.keys("attendance:employee:shift:*");
            if (employeeShiftKeys != null && !employeeShiftKeys.isEmpty()) {
                redisTemplate.delete(employeeShiftKeys);
                log.info("[缓存预热] 清空员工班次缓存: count={}", employeeShiftKeys.size());
            }

            log.info("[缓存预热] 缓存清空完成");

        } catch (Exception e) {
            log.error("[缓存预热] 缓存清空异常: error={}", e.getMessage(), e);
        }
    }
}
