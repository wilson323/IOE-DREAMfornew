package net.lab1024.sa.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.video.dao.VideoRecordingPlanDao;
import net.lab1024.sa.video.dao.VideoRecordingTaskDao;
import net.lab1024.sa.video.domain.entity.VideoRecordingPlanEntity;
import net.lab1024.sa.video.domain.entity.VideoRecordingTaskEntity;
import net.lab1024.sa.video.domain.form.VideoRecordingControlForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanQueryForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingTaskVO;
import net.lab1024.sa.video.service.VideoRecordingControlService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频录像控制服务实现
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Service
@Slf4j
public class VideoRecordingControlServiceImpl implements VideoRecordingControlService {

    @Resource
    private VideoRecordingPlanDao videoRecordingPlanDao;

    @Resource
    private VideoRecordingTaskDao videoRecordingTaskDao;

    @Resource
    private net.lab1024.sa.video.manager.VideoRecordingManager videoRecordingManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startRecordingByPlan(Long planId) {
        log.info("[录像控制] 根据计划启动录像: planId={}", planId);

        // 获取录像计划
        VideoRecordingPlanEntity plan = videoRecordingPlanDao.selectById(planId);
        if (plan == null) {
            throw new BusinessException("PLAN_NOT_FOUND", "录像计划不存在");
        }

        if (!plan.getEnabled()) {
            throw new BusinessException("PLAN_DISABLED", "录像计划未启用");
        }

        // 检查设备是否正在录像
        VideoRecordingTaskEntity runningTask = videoRecordingTaskDao.selectRunningTaskByDevice(plan.getDeviceId());
        if (runningTask != null) {
            throw new BusinessException("DEVICE_RECORDING", "设备正在录像中");
        }

        // 创建录像任务
        VideoRecordingTaskEntity task = new VideoRecordingTaskEntity();
        task.setPlanId(planId);
        task.setDeviceId(plan.getDeviceId());
        task.setChannelId(plan.getChannelId());
        task.setStatus(VideoRecordingTaskEntity.TaskStatus.PENDING.getCode());
        task.setTriggerType(VideoRecordingTaskEntity.TriggerType.SCHEDULE.getCode());
        task.setQuality(plan.getQuality());
        task.setMaxRetryCount(5);
        task.setRetryCount(0);

        // 生成文件路径
        String filePath = generateRecordingFilePath(plan);
        task.setFilePath(filePath);

        videoRecordingTaskDao.insert(task);

        // 异步启动录像
        videoRecordingManager.startRecordingAsync(task.getTaskId(), plan);

        log.info("[录像控制] 录像任务创建成功: taskId={}, planId={}", task.getTaskId(), planId);
        return task.getTaskId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startManualRecording(VideoRecordingControlForm controlForm) {
        log.info("[录像控制] 手动启动录像: deviceId={}, operationType={}",
                controlForm.getDeviceId(), controlForm.getOperationType());

        if (controlForm.getOperationType() != 1) {
            throw new BusinessException("INVALID_OPERATION", "操作类型错误");
        }

        // 检查设备是否正在录像
        VideoRecordingTaskEntity runningTask = videoRecordingTaskDao.selectRunningTaskByDevice(controlForm.getDeviceId());
        if (runningTask != null) {
            throw new BusinessException("DEVICE_RECORDING", "设备正在录像中");
        }

        // 创建录像任务
        VideoRecordingTaskEntity task = new VideoRecordingTaskEntity();
        task.setDeviceId(controlForm.getDeviceId());
        task.setChannelId(controlForm.getChannelId());
        task.setStatus(VideoRecordingTaskEntity.TaskStatus.PENDING.getCode());
        task.setTriggerType(VideoRecordingTaskEntity.TriggerType.MANUAL.getCode());
        task.setQuality(controlForm.getQuality() != null ? controlForm.getQuality() : 3); // 默认高质量
        task.setMaxRetryCount(3);
        task.setRetryCount(0);

        // 生成文件路径
        String storageLocation = controlForm.getStorageLocation();
        if (storageLocation == null || storageLocation.isEmpty()) {
            storageLocation = "/recordings/manual/" + controlForm.getDeviceId() + "/";
        }
        String filePath = generateManualRecordingFilePath(controlForm.getDeviceId(), storageLocation);
        task.setFilePath(filePath);

        videoRecordingTaskDao.insert(task);

        // 异步启动录像
        videoRecordingManager.startManualRecordingAsync(task.getTaskId(), controlForm);

        log.info("[录像控制] 手动录像任务创建成功: taskId={}", task.getTaskId());
        return task.getTaskId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer stopRecording(Long taskId) {
        log.info("[录像控制] 停止录像: taskId={}", taskId);

        // 获取录像任务
        VideoRecordingTaskEntity task = videoRecordingTaskDao.selectById(taskId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "录像任务不存在");
        }

        if (!task.isRunning()) {
            throw new BusinessException("TASK_NOT_RUNNING", "录像任务未运行中");
        }

        // 停止录像
        videoRecordingManager.stopRecording(taskId);

        // 更新任务状态
        task.setStatus(VideoRecordingTaskEntity.TaskStatus.STOPPED.getCode());
        task.setEndTime(LocalDateTime.now());
        task.setCompletedTime(LocalDateTime.now());

        // 计算录像时长
        if (task.getStartTime() != null) {
            long durationSeconds = java.time.Duration.between(task.getStartTime(), task.getEndTime()).getSeconds();
            task.setDurationSeconds((int) durationSeconds);
        }

        videoRecordingTaskDao.updateById(task);

        log.info("[录像控制] 录像停止成功: taskId={}", taskId);
        return 1;
    }

    @Override
    public VideoRecordingTaskVO getRecordingStatus(Long taskId) {
        log.debug("[录像控制] 获取录像任务状态: taskId={}", taskId);

        VideoRecordingTaskEntity task = videoRecordingTaskDao.selectById(taskId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "录像任务不存在");
        }

        return convertToVO(task);
    }

    @Override
    public VideoRecordingTaskVO getDeviceRecordingStatus(String deviceId) {
        log.debug("[录像控制] 获取设备录像状态: deviceId={}", deviceId);

        VideoRecordingTaskEntity task = videoRecordingTaskDao.selectRunningTaskByDevice(deviceId);
        if (task == null) {
            return null;
        }

        return convertToVO(task);
    }

    @Override
    public PageResult<VideoRecordingTaskVO> queryTasks(VideoRecordingPlanQueryForm queryForm) {
        log.info("[录像控制] 分页查询录像任务: pageNum={}, pageSize={}",
                queryForm.getPageNum(), queryForm.getPageSize());

        LambdaQueryWrapper<VideoRecordingTaskEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 设备ID
        if (queryForm.getDeviceId() != null && !queryForm.getDeviceId().isEmpty()) {
            queryWrapper.eq(VideoRecordingTaskEntity::getDeviceId, queryForm.getDeviceId());
        }

        // 创建时间范围
        if (queryForm.getCreateTimeBegin() != null) {
            queryWrapper.ge(VideoRecordingTaskEntity::getCreateTime, queryForm.getCreateTimeBegin());
        }
        if (queryForm.getCreateTimeEnd() != null) {
            queryWrapper.le(VideoRecordingTaskEntity::getCreateTime, queryForm.getCreateTimeEnd());
        }

        // 排序
        queryWrapper.orderByDesc(VideoRecordingTaskEntity::getCreateTime);

        Page<VideoRecordingTaskEntity> page = videoRecordingTaskDao.selectPage(
                new Page<>(queryForm.getPageNum(), queryForm.getPageSize()),
                queryWrapper
        );

        List<VideoRecordingTaskVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<VideoRecordingTaskVO> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum((int) page.getCurrent());
        pageResult.setPageSize((int) page.getSize());
        pageResult.setPages((int) page.getPages());

        log.info("[录像控制] 分页查询完成: total={}, pages={}", page.getTotal(), page.getPages());
        return pageResult;
    }

    @Override
    public List<VideoRecordingTaskVO> getTasksByDevice(String deviceId) {
        log.debug("[录像控制] 获取设备的录像任务: deviceId={}", deviceId);

        LambdaQueryWrapper<VideoRecordingTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoRecordingTaskEntity::getDeviceId, deviceId)
                .orderByDesc(VideoRecordingTaskEntity::getCreateTime)
                .last("LIMIT 50");

        List<VideoRecordingTaskEntity> tasks = videoRecordingTaskDao.selectList(queryWrapper);

        return tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VideoRecordingTaskVO> getTasksByPlan(Long planId) {
        log.debug("[录像控制] 获取计划的录像任务: planId={}", planId);

        List<VideoRecordingTaskEntity> tasks = videoRecordingTaskDao.selectTasksByPlan(planId);

        return tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VideoRecordingTaskVO> getTasksByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[录像控制] 获取时间范围的录像任务: startTime={}, endTime={}", startTime, endTime);

        List<VideoRecordingTaskEntity> tasks = videoRecordingTaskDao.selectTasksByTimeRange(startTime, endTime);

        return tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long retryFailedTask(Long taskId) {
        log.info("[录像控制] 重试失败的录像任务: taskId={}", taskId);

        // 获取原任务
        VideoRecordingTaskEntity originalTask = videoRecordingTaskDao.selectById(taskId);
        if (originalTask == null) {
            throw new BusinessException("TASK_NOT_FOUND", "录像任务不存在");
        }

        if (!originalTask.isFailed()) {
            throw new BusinessException("TASK_NOT_FAILED", "任务未失败");
        }

        if (!originalTask.canRetry()) {
            throw new BusinessException("MAX_RETRY_REACHED", "已达到最大重试次数");
        }

        // 创建新任务
        VideoRecordingTaskEntity newTask = new VideoRecordingTaskEntity();
        newTask.setPlanId(originalTask.getPlanId());
        newTask.setDeviceId(originalTask.getDeviceId());
        newTask.setChannelId(originalTask.getChannelId());
        newTask.setStatus(VideoRecordingTaskEntity.TaskStatus.PENDING.getCode());
        newTask.setTriggerType(originalTask.getTriggerType());
        newTask.setQuality(originalTask.getQuality());
        newTask.setRetryCount(originalTask.getRetryCount() + 1);
        newTask.setMaxRetryCount(originalTask.getMaxRetryCount());
        newTask.setFilePath(originalTask.getFilePath());

        videoRecordingTaskDao.insert(newTask);

        // 异步启动录像
        if (newTask.getPlanId() != null) {
            VideoRecordingPlanEntity plan = videoRecordingPlanDao.selectById(newTask.getPlanId());
            videoRecordingManager.startRecordingAsync(newTask.getTaskId(), plan);
        } else {
            VideoRecordingControlForm form = new VideoRecordingControlForm();
            form.setDeviceId(newTask.getDeviceId());
            form.setChannelId(newTask.getChannelId());
            videoRecordingManager.startManualRecordingAsync(newTask.getTaskId(), form);
        }

        log.info("[录像控制] 录像任务重试成功: originalTaskId={}, newTaskId={}", taskId, newTask.getTaskId());
        return newTask.getTaskId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long handleEventRecording(String deviceId, String eventType) {
        log.info("[录像控制] 处理事件触发录像: deviceId={}, eventType={}", deviceId, eventType);

        // 查找事件录像计划
        List<VideoRecordingPlanEntity> plans = videoRecordingPlanDao.selectEnabledPlansByDevice(deviceId);
        VideoRecordingPlanEntity eventPlan = null;
        for (VideoRecordingPlanEntity plan : plans) {
            if (plan.getPlanType() == VideoRecordingPlanEntity.PlanType.EVENT.getCode()) {
                // 检查事件类型是否匹配
                if (plan.getEventTypes() != null && plan.getEventTypes().contains(eventType)) {
                    eventPlan = plan;
                    break;
                }
            }
        }

        if (eventPlan == null) {
            log.warn("[录像控制] 未找到匹配的事件录像计划: deviceId={}, eventType={}", deviceId, eventType);
            return null;
        }

        // 创建录像任务
        VideoRecordingTaskEntity task = new VideoRecordingTaskEntity();
        task.setPlanId(eventPlan.getPlanId());
        task.setDeviceId(deviceId);
        task.setChannelId(eventPlan.getChannelId());
        task.setStatus(VideoRecordingTaskEntity.TaskStatus.PENDING.getCode());
        task.setTriggerType(VideoRecordingTaskEntity.TriggerType.EVENT.getCode());
        task.setEventTriggerType(eventType);
        task.setQuality(eventPlan.getQuality());
        task.setMaxRetryCount(3);
        task.setRetryCount(0);

        // 生成文件路径
        String filePath = generateEventRecordingFilePath(deviceId, eventType);
        task.setFilePath(filePath);

        videoRecordingTaskDao.insert(task);

        // 异步启动录像
        videoRecordingManager.startRecordingAsync(task.getTaskId(), eventPlan);

        log.info("[录像控制] 事件录像任务创建成功: taskId={}, eventType={}", task.getTaskId(), eventType);
        return task.getTaskId();
    }

    @Override
    public List<VideoRecordingTaskEntity> getRetryableTasks(Integer limit) {
        return videoRecordingTaskDao.selectRetryableTasks(limit);
    }

    @Override
    public Long sumRecordingFileSize(LocalDateTime startTime, LocalDateTime endTime) {
        return videoRecordingTaskDao.sumFileSizeByTimeRange(startTime, endTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer cleanExpiredTasks(LocalDateTime beforeDate) {
        log.info("[录像控制] 清理过期录像任务: beforeDate={}", beforeDate);

        Integer rows = videoRecordingTaskDao.deleteCompletedTasksBefore(beforeDate);

        log.info("[录像控制] 过期任务清理完成: rows={}", rows);
        return rows;
    }

    @Override
    public Boolean isDeviceRecording(String deviceId) {
        VideoRecordingTaskEntity task = videoRecordingTaskDao.selectRunningTaskByDevice(deviceId);
        return task != null && task.isRunning();
    }

    /**
     * 生成录像文件路径
     */
    private String generateRecordingFilePath(VideoRecordingPlanEntity plan) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateStr = LocalDateTime.now().format(formatter);

        String filename = plan.getDeviceId() + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")) + ".mp4";

        StringBuilder pathBuilder = new StringBuilder();
        if (plan.getStorageLocation() != null && !plan.getStorageLocation().isEmpty()) {
            pathBuilder.append(plan.getStorageLocation());
        } else {
            pathBuilder.append("/recordings/");
            pathBuilder.append(plan.getDeviceId()).append("/");
        }
        pathBuilder.append(dateStr).append("/");
        pathBuilder.append(filename);

        return pathBuilder.toString();
    }

    /**
     * 生成手动录像文件路径
     */
    private String generateManualRecordingFilePath(String deviceId, String storageLocation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateStr = LocalDateTime.now().format(formatter);

        String filename = "manual_" + deviceId + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".mp4";

        StringBuilder pathBuilder = new StringBuilder(storageLocation);
        pathBuilder.append(dateStr).append("/");
        pathBuilder.append(filename);

        return pathBuilder.toString();
    }

    /**
     * 生成事件录像文件路径
     */
    private String generateEventRecordingFilePath(String deviceId, String eventType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateStr = LocalDateTime.now().format(formatter);

        String filename = "event_" + eventType + "_" + deviceId + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".mp4";

        StringBuilder pathBuilder = new StringBuilder("/recordings/events/");
        pathBuilder.append(deviceId).append("/");
        pathBuilder.append(dateStr).append("/");
        pathBuilder.append(filename);

        return pathBuilder.toString();
    }

    /**
     * 转换为VO对象
     */
    private VideoRecordingTaskVO convertToVO(VideoRecordingTaskEntity task) {
        VideoRecordingTaskVO vo = new VideoRecordingTaskVO();
        BeanUtils.copyProperties(task, vo);

        // 转换枚举值
        vo.setStatus(VideoRecordingTaskEntity.TaskStatus.fromCode(task.getStatus()).name());
        vo.setStatusName(VideoRecordingTaskEntity.TaskStatus.fromCode(task.getStatus()).getDescription());

        if (task.getTriggerType() != null) {
            vo.setTriggerType(VideoRecordingTaskEntity.TriggerType.fromCode(task.getTriggerType()).name());
            vo.setTriggerTypeName(VideoRecordingTaskEntity.TriggerType.fromCode(task.getTriggerType()).getDescription());
        }

        // 转换质量
        if (task.getQuality() != null) {
            VideoRecordingPlanEntity.RecordingQuality quality =
                    VideoRecordingPlanEntity.RecordingQuality.fromCode(task.getQuality());
            vo.setQuality(quality.name());
            vo.setQualityName(quality.getDescription());
        }

        // 转换文件大小
        if (task.getFileSize() != null) {
            vo.setFileSizeReadable(formatFileSize(task.getFileSize()));
        }

        // 转换时长
        if (task.getDurationSeconds() != null) {
            vo.setDurationReadable(formatDuration(task.getDurationSeconds()));
        }

        // 设置布尔状态
        vo.setCanRetry(task.canRetry());
        vo.setIsRunning(task.isRunning());
        vo.setIsCompleted(task.isCompleted());
        vo.setIsFailed(task.isFailed());

        // 获取计划名称
        if (task.getPlanId() != null) {
            VideoRecordingPlanEntity plan = videoRecordingPlanDao.selectById(task.getPlanId());
            if (plan != null) {
                vo.setPlanName(plan.getPlanName());
            }
        }

        return vo;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 格式化时长
     */
    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0) {
            return "0秒";
        }

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (secs > 0) {
            sb.append(secs).append("秒");
        }

        return sb.toString();
    }
}
