package net.lab1024.sa.video.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.VideoDisplayTaskDao;
import net.lab1024.sa.video.dao.VideoWallWindowDao;
import net.lab1024.sa.video.entity.VideoDisplayTaskEntity;
import net.lab1024.sa.video.entity.VideoWallWindowEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上墙任务管理器
 * <p>
 * 符合CLAUDE.md规范 - Manager层
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 上墙任务编排
 * - 任务执行管理
 * - 批量任务处理
 * - 轮巡任务调度
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class WallTaskManager {

    private final VideoDisplayTaskDao videoDisplayTaskDao;
    private final VideoWallWindowDao videoWallWindowDao;

    /**
     * 构造函数
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param videoDisplayTaskDao 上墙任务数据访问层
     * @param videoWallWindowDao 窗口数据访问层
     */
    public WallTaskManager(VideoDisplayTaskDao videoDisplayTaskDao,
                          VideoWallWindowDao videoWallWindowDao) {
        this.videoDisplayTaskDao = videoDisplayTaskDao;
        this.videoWallWindowDao = videoWallWindowDao;
        log.info("[WallTaskManager] 初始化上墙任务管理器");
    }

    /**
     * 执行上墙任务
     * <p>
     * 创建任务记录并更新窗口状态
     * </p>
     *
     * @param task 上墙任务实体
     * @return 任务ID
     */
    public Long executeDisplayTask(VideoDisplayTaskEntity task) {
        log.info("[上墙任务管理] 执行上墙任务: wallId={}, windowId={}, deviceId={}, taskType={}",
                task.getWallId(), task.getWindowId(), task.getDeviceId(), task.getTaskType());

        // 检查窗口是否存在
        VideoWallWindowEntity window = videoWallWindowDao.selectById(task.getWindowId());
        if (window == null) {
            log.warn("[上墙任务管理] 窗口不存在: windowId={}", task.getWindowId());
            throw new IllegalArgumentException("窗口不存在: " + task.getWindowId());
        }

        // 检查窗口是否被占用（如果有正在执行的任务）
        VideoDisplayTaskEntity currentTask = videoDisplayTaskDao.selectCurrentTaskByWindowId(task.getWindowId());
        if (currentTask != null && currentTask.getStatus() == 1) {
            log.warn("[上墙任务管理] 窗口正在使用中: windowId={}, currentTaskId={}", task.getWindowId(), currentTask.getTaskId());
            throw new IllegalStateException("窗口正在使用中，请先取消当前任务");
        }

        // 设置任务初始状态
        task.setStatus(0); // 0-等待
        task.setStartTime(LocalDateTime.now());

        // 保存任务
        videoDisplayTaskDao.insert(task);

        // 更新窗口状态为播放中
        videoWallWindowDao.updateStatus(task.getWindowId(), 1);

        log.info("[上墙任务管理] 上墙任务创建成功: taskId={}", task.getTaskId());
        return task.getTaskId();
    }

    /**
     * 批量执行上墙任务
     * <p>
     * 为多个窗口同时创建上墙任务
     * </p>
     *
     * @param tasks 任务列表
     * @return 成功创建的任务ID列表
     */
    public List<Long> batchExecuteTasks(List<VideoDisplayTaskEntity> tasks) {
        log.info("[上墙任务管理] 批量执行上墙任务: taskCount={}", tasks.size());

        for (VideoDisplayTaskEntity task : tasks) {
            try {
                executeDisplayTask(task);
            } catch (Exception e) {
                log.error("[上墙任务管理] 批量执行任务失败: wallId={}, windowId={}, error={}",
                        task.getWallId(), task.getWindowId(), e.getMessage(), e);
                // 继续执行其他任务
            }
        }

        return tasks.stream()
                .map(VideoDisplayTaskEntity::getTaskId)
                .filter(id -> id != null)
                .toList();
    }

    /**
     * 取消上墙任务
     * <p>
     * 更新任务状态为失败，并释放窗口
     * </p>
     *
     * @param taskId 任务ID
     */
    public void cancelDisplayTask(Long taskId) {
        log.info("[上墙任务管理] 取消上墙任务: taskId={}", taskId);

        VideoDisplayTaskEntity task = videoDisplayTaskDao.selectById(taskId);
        if (task == null) {
            log.warn("[上墙任务管理] 任务不存在: taskId={}", taskId);
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        // 更新任务状态为失败
        videoDisplayTaskDao.updateStatus(taskId, 3, "任务已取消");

        // 更新窗口状态为空闲
        if (task.getWindowId() != null) {
            videoWallWindowDao.updateStatus(task.getWindowId(), 0);
        }

        log.info("[上墙任务管理] 上墙任务取消成功: taskId={}", taskId);
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 状态：0-等待，1-执行中，2-完成，3-失败
     * @param errorMsg 错误信息（可选）
     */
    public void updateTaskStatus(Long taskId, Integer status, String errorMsg) {
        log.debug("[上墙任务管理] 更新任务状态: taskId={}, status={}, errorMsg={}", taskId, status, errorMsg);

        if (status == 1) {
            // 开始执行
            videoDisplayTaskDao.updateStartTime(taskId, LocalDateTime.now());
        } else if (status == 2 || status == 3) {
            // 完成或失败
            videoDisplayTaskDao.updateEndTime(taskId, LocalDateTime.now(), status);
        } else {
            // 其他状态
            videoDisplayTaskDao.updateStatus(taskId, status, errorMsg);
        }

        log.debug("[上墙任务管理] 任务状态更新成功: taskId={}, status={}", taskId, status);
    }

    /**
     * 查询电视墙的任务列表
     *
     * @param wallId 电视墙ID
     * @return 任务列表
     */
    public List<VideoDisplayTaskEntity> getWallTasks(Long wallId) {
        return videoDisplayTaskDao.selectByWallId(wallId);
    }

    /**
     * 查询窗口的任务列表
     *
     * @param windowId 窗口ID
     * @return 任务列表
     */
    public List<VideoDisplayTaskEntity> getWindowTasks(Long windowId) {
        return videoDisplayTaskDao.selectByWindowId(windowId);
    }
}
