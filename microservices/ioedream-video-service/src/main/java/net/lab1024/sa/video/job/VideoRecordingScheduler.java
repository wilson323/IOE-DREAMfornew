package net.lab1024.sa.video.job;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoRecordingPlanDao;
import net.lab1024.sa.video.dao.VideoRecordingTaskDao;
import net.lab1024.sa.video.domain.entity.VideoRecordingPlanEntity;
import net.lab1024.sa.video.domain.entity.VideoRecordingTaskEntity;
import net.lab1024.sa.video.service.VideoRecordingControlService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 视频录像计划调度器
 * 负责定时检查录像计划并启动录像任务
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Component
@Slf4j
public class VideoRecordingScheduler {

    @Resource
    private VideoRecordingPlanDao videoRecordingPlanDao;

    @Resource
    private VideoRecordingTaskDao videoRecordingTaskDao;

    @Resource
    private VideoRecordingControlService videoRecordingControlService;

    /**
     * 定时检查并启动录像计划
     * 每分钟检查一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scheduleRecordingPlans() {
        try {
            log.debug("[录像调度器] 开始检查录像计划");

            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            int currentHour = now.getHour();
            int currentMinute = now.getMinute();
            DayOfWeek currentDayOfWeek = now.getDayOfWeek();
            int currentDayOfWeekValue = currentDayOfWeek.getValue(); // 1=Monday, 7=Sunday

            // 查询所有启用的定时录像计划
            List<VideoRecordingPlanEntity> plans = videoRecordingPlanDao.selectEnabledPlansByDevice(null);

            for (VideoRecordingPlanEntity plan : plans) {
                // 只处理定时录像计划
                if (plan.getPlanType() != VideoRecordingPlanEntity.PlanType.SCHEDULE.getCode()) {
                    continue;
                }

                // 检查计划是否应该执行
                if (shouldExecutePlan(plan, now, currentHour, currentMinute, currentDayOfWeekValue)) {
                    // 检查设备是否正在录像
                    VideoRecordingTaskEntity runningTask = videoRecordingTaskDao.selectRunningTaskByDevice(plan.getDeviceId());
                    if (runningTask != null) {
                        log.debug("[录像调度器] 设备正在录像，跳过计划: deviceId={}, planId={}",
                                plan.getDeviceId(), plan.getPlanId());
                        continue;
                    }

                    try {
                        // 启动录像
                        log.info("[录像调度器] 启动录像计划: planId={}, planName={}, deviceId={}",
                                plan.getPlanId(), plan.getPlanName(), plan.getDeviceId());

                        videoRecordingControlService.startRecordingByPlan(plan.getPlanId());

                    } catch (Exception e) {
                        log.error("[录像调度器] 启动录像失败: planId={}, error={}",
                                plan.getPlanId(), e.getMessage(), e);
                    }
                }
            }

            log.debug("[录像调度器] 录像计划检查完成");

        } catch (Exception e) {
            log.error("[录像调度器] 录像调度异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 检查录像计划是否应该执行
     */
    private boolean shouldExecutePlan(VideoRecordingPlanEntity plan, LocalDateTime now,
                                       int currentHour, int currentMinute, int currentDayOfWeekValue) {
        // 检查开始时间
        if (plan.getStartTime() != null && now.isBefore(plan.getStartTime())) {
            return false;
        }

        // 检查结束时间
        if (plan.getEndTime() != null && now.isAfter(plan.getEndTime())) {
            return false;
        }

        // 检查星期设置
        if (plan.getWeekdays() != null && !plan.getWeekdays().isEmpty()) {
            String[] weekdayArray = plan.getWeekdays().split(",");
            boolean matchDay = false;
            for (String weekday : weekdayArray) {
                if (Integer.parseInt(weekday.trim()) == currentDayOfWeekValue) {
                    matchDay = true;
                    break;
                }
            }
            if (!matchDay) {
                return false;
            }
        }

        // 检查录像类型
        if (plan.getRecordingType() == VideoRecordingPlanEntity.RecordingType.FULL_TIME.getCode()) {
            // 全天录像：在开始时间的0分钟时启动
            if (plan.getStartTime() != null) {
                LocalDateTime planStart = plan.getStartTime();
                return currentHour == planStart.getHour() && currentMinute == planStart.getMinute();
            }
            return true;

        } else if (plan.getRecordingType() == VideoRecordingPlanEntity.RecordingType.TIMED.getCode()) {
            // 定时录像：在开始时间启动
            if (plan.getStartTime() != null) {
                LocalDateTime planStart = plan.getStartTime();
                return currentHour == planStart.getHour() && currentMinute == planStart.getMinute();
            }
            return false;

        } else if (plan.getRecordingType() == VideoRecordingPlanEntity.RecordingType.EVENT_TRIGGERED.getCode()) {
            // 事件触发录像：不在这里启动
            return false;
        }

        return false;
    }

    /**
     * 监控运行中的录像任务
     * 每5分钟检查一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void monitorRecordingTasks() {
        try {
            log.debug("[录像调度器] 开始监控录像任务");

            // 查询所有运行中的任务
            List<VideoRecordingTaskEntity> runningTasks = videoRecordingTaskDao.selectTasksByStatus(
                    VideoRecordingTaskEntity.TaskStatus.RUNNING.getCode(), 100);

            for (VideoRecordingTaskEntity task : runningTasks) {
                try {
                    // 检查任务是否超时
                    if (isTaskTimeout(task)) {
                        log.warn("[录像调度器] 录像任务超时，自动停止: taskId={}, deviceId={}",
                                task.getTaskId(), task.getDeviceId());

                        videoRecordingControlService.stopRecording(task.getTaskId());
                    }

                    // 检查录像文件是否正常增长
                    // TODO: 检查文件大小是否在增长

                } catch (Exception e) {
                    log.error("[录像调度器] 监控任务失败: taskId={}, error={}",
                            task.getTaskId(), e.getMessage(), e);
                }
            }

            log.debug("[录像调度器] 录像任务监控完成");

        } catch (Exception e) {
            log.error("[录像调度器] 监控录像任务异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 检查任务是否超时
     */
    private boolean isTaskTimeout(VideoRecordingTaskEntity task) {
        if (task.getMaxDurationMinutes() != null && task.getStartTime() != null) {
            long timeoutMinutes = java.time.Duration.between(task.getStartTime(), LocalDateTime.now()).toMinutes();
            return timeoutMinutes > task.getMaxDurationMinutes();
        }
        return false;
    }

    /**
     * 重试失败的录像任务
     * 每10分钟检查一次
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void retryFailedTasks() {
        try {
            log.debug("[录像调度器] 开始重试失败的录像任务");

            // 查询可重试的失败任务
            List<VideoRecordingTaskEntity> failedTasks = videoRecordingTaskDao.selectRetryableTasks(20);

            for (VideoRecordingTaskEntity task : failedTasks) {
                try {
                    log.info("[录像调度器] 重试失败的录像任务: taskId={}, deviceId={}, retryCount={}",
                            task.getTaskId(), task.getDeviceId(), task.getRetryCount());

                    videoRecordingControlService.retryFailedTask(task.getTaskId());

                } catch (Exception e) {
                    log.error("[录像调度器] 重试任务失败: taskId={}, error={}",
                            task.getTaskId(), e.getMessage(), e);
                }
            }

            log.debug("[录像调度器] 失败任务重试完成");

        } catch (Exception e) {
            log.error("[录像调度器] 重试失败任务异常: error={}", e.getMessage(), e);
        }
    }
}
