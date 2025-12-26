package net.lab1024.sa.attendance.util;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;

import net.lab1024.sa.attendance.dao.ScheduleRecordDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.attendance.domain.entity.ScheduleRecordEntity;
import net.lab1024.sa.attendance.entity.WorkShiftEntity;

/**
 * 考勤缓存辅助类
 * <p>
 * 提供Redis缓存的便捷访问方法，支持缓存穿透、缓存击穿、缓存雪崩防护
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class AttendanceCacheHelper {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private WorkShiftDao workShiftDao;

    @Resource
    private ScheduleRecordDao scheduleRecordDao;

    /**
     * 从缓存获取班次信息
     *
     * @param shiftId 班次ID
     * @return 班次实体，如果缓存不存在则从数据库加载并缓存
     */
    public WorkShiftEntity getWorkShift(Long shiftId) {
        if (shiftId == null) {
            return null;
        }

        String key = "attendance:shift:" + shiftId;

        try {
            // 尝试从缓存获取
            WorkShiftEntity cachedShift = (WorkShiftEntity) redisTemplate.opsForValue().get(key);
            if (cachedShift != null) {
                log.debug("[缓存] 命中班次缓存: shiftId={}", shiftId);
                return cachedShift;
            }

            // 缓存未命中，从数据库加载
            log.debug("[缓存] 班次缓存未命中，从数据库加载: shiftId={}", shiftId);
            WorkShiftEntity shift = workShiftDao.selectById(shiftId);

            if (shift != null) {
                // 写入缓存（24小时）
                redisTemplate.opsForValue().set(key, shift, 24, TimeUnit.HOURS);
            }

            return shift;

        } catch (Exception e) {
            log.error("[缓存] 获取班次缓存异常: shiftId={}, error={}", shiftId, e.getMessage(), e);
            // 降级：直接查询数据库
            return workShiftDao.selectById(shiftId);
        }
    }

    /**
     * 从缓存获取排班信息
     *
     * @param scheduleId 排班ID
     * @return 排班实体，如果缓存不存在则从数据库加载并缓存
     */
    public ScheduleRecordEntity getSchedule(Long scheduleId) {
        if (scheduleId == null) {
            return null;
        }

        String key = "attendance:schedule:" + scheduleId;

        try {
            // 尝试从缓存获取
            ScheduleRecordEntity cachedSchedule = (ScheduleRecordEntity) redisTemplate.opsForValue().get(key);
            if (cachedSchedule != null) {
                log.debug("[缓存] 命中排班缓存: scheduleId={}", scheduleId);
                return cachedSchedule;
            }

            // 缓存未命中，从数据库加载
            log.debug("[缓存] 排班缓存未命中，从数据库加载: scheduleId={}", scheduleId);
            ScheduleRecordEntity schedule = scheduleRecordDao.selectById(scheduleId);

            if (schedule != null) {
                // 写入缓存（2小时）
                redisTemplate.opsForValue().set(key, schedule, 2, TimeUnit.HOURS);
            }

            return schedule;

        } catch (Exception e) {
            log.error("[缓存] 获取排班缓存异常: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
            // 降级：直接查询数据库
            return scheduleRecordDao.selectById(scheduleId);
        }
    }

    /**
     * 从缓存获取员工指定日期的排班信息
     *
     * @param employeeId 员工ID
     * @param date 日期
     * @return 排班实体，如果缓存不存在则从数据库加载并缓存
     */
    public ScheduleRecordEntity getEmployeeSchedule(Long employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return null;
        }

        String key = "attendance:employee:shift:" + employeeId + ":" + date;

        try {
            // 尝试从缓存获取
            ScheduleRecordEntity cachedSchedule = (ScheduleRecordEntity) redisTemplate.opsForValue().get(key);
            if (cachedSchedule != null) {
                log.debug("[缓存] 命中员工排班缓存: employeeId={}, date={}", employeeId, date);
                return cachedSchedule;
            }

            // 缓存未命中，从数据库加载
            log.debug("[缓存] 员工排班缓存未命中，从数据库加载: employeeId={}, date={}", employeeId, date);
            ScheduleRecordEntity schedule = scheduleRecordDao.selectOne(
                    new LambdaQueryWrapper<ScheduleRecordEntity>()
                            .eq(ScheduleRecordEntity::getEmployeeId, employeeId)
                            .eq(ScheduleRecordEntity::getScheduleDate, date)
            );

            if (schedule != null) {
                // 写入缓存（4小时）
                redisTemplate.opsForValue().set(key, schedule, 4, TimeUnit.HOURS);
            }

            return schedule;

        } catch (Exception e) {
            log.error("[缓存] 获取员工排班缓存异常: employeeId={}, date={}, error={}",
                    employeeId, date, e.getMessage(), e);
            // 降级：直接查询数据库
            return scheduleRecordDao.selectOne(
                    new LambdaQueryWrapper<ScheduleRecordEntity>()
                            .eq(ScheduleRecordEntity::getEmployeeId, employeeId)
                            .eq(ScheduleRecordEntity::getScheduleDate, date)
            );
        }
    }

    /**
     * 删除班次缓存
     *
     * @param shiftId 班次ID
     */
    public void evictWorkShift(Long shiftId) {
        if (shiftId == null) {
            return;
        }

        try {
            String key = "attendance:shift:" + shiftId;
            redisTemplate.delete(key);
            log.debug("[缓存] 删除班次缓存: shiftId={}", shiftId);
        } catch (Exception e) {
            log.error("[缓存] 删除班次缓存异常: shiftId={}, error={}", shiftId, e.getMessage(), e);
        }
    }

    /**
     * 删除排班缓存
     *
     * @param scheduleId 排班ID
     */
    public void evictSchedule(Long scheduleId) {
        if (scheduleId == null) {
            return;
        }

        try {
            String key = "attendance:schedule:" + scheduleId;
            redisTemplate.delete(key);
            log.debug("[缓存] 删除排班缓存: scheduleId={}", scheduleId);
        } catch (Exception e) {
            log.error("[缓存] 删除排班缓存异常: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
        }
    }

    /**
     * 删除员工排班缓存
     *
     * @param employeeId 员工ID
     * @param date 日期
     */
    public void evictEmployeeSchedule(Long employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return;
        }

        try {
            String key = "attendance:employee:shift:" + employeeId + ":" + date;
            redisTemplate.delete(key);
            log.debug("[缓存] 删除员工排班缓存: employeeId={}, date={}", employeeId, date);
        } catch (Exception e) {
            log.error("[缓存] 删除员工排班缓存异常: employeeId={}, date={}, error={}",
                    employeeId, date, e.getMessage(), e);
        }
    }

    /**
     * 批量删除员工排班缓存
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    public void evictEmployeeSchedules(Long employeeId, LocalDate startDate, LocalDate endDate) {
        if (employeeId == null || startDate == null || endDate == null) {
            return;
        }

        try {
            LocalDate currentDate = startDate;
            int count = 0;

            while (!currentDate.isAfter(endDate)) {
                String key = "attendance:employee:shift:" + employeeId + ":" + currentDate;
                redisTemplate.delete(key);
                count++;
                currentDate = currentDate.plusDays(1);
            }

            log.debug("[缓存] 批量删除员工排班缓存: employeeId={}, dateRange={}, count={}",
                    employeeId, startDate + "~" + endDate, count);

        } catch (Exception e) {
            log.error("[缓存] 批量删除员工排班缓存异常: employeeId={}, dateRange={}, error={}",
                    employeeId, startDate + "~" + endDate, e.getMessage(), e);
        }
    }
}
