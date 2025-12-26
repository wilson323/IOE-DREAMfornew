package net.lab1024.sa.video.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.video.domain.entity.VideoRecordingTaskEntity;
import net.lab1024.sa.video.domain.form.VideoRecordingControlForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanQueryForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingTaskVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频录像控制服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
public interface VideoRecordingControlService {

    /**
     * 根据计划启动录像
     *
     * @param planId 计划ID
     * @return 任务ID
     */
    Long startRecordingByPlan(Long planId);

    /**
     * 手动启动录像
     *
     * @param controlForm 控制表单
     * @return 任务ID
     */
    Long startManualRecording(VideoRecordingControlForm controlForm);

    /**
     * 停止录像
     *
     * @param taskId 任务ID
     * @return 影响行数
     */
    Integer stopRecording(Long taskId);

    /**
     * 获取录像任务状态
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    VideoRecordingTaskVO getRecordingStatus(Long taskId);

    /**
     * 获取设备当前录像状态
     *
     * @param deviceId 设备ID
     * @return 任务详情
     */
    VideoRecordingTaskVO getDeviceRecordingStatus(String deviceId);

    /**
     * 分页查询录像任务
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<VideoRecordingTaskVO> queryTasks(VideoRecordingPlanQueryForm queryForm);

    /**
     * 查询指定设备的录像任务列表
     *
     * @param deviceId 设备ID
     * @return 任务列表
     */
    List<VideoRecordingTaskVO> getTasksByDevice(String deviceId);

    /**
     * 查询指定计划的录像任务列表
     *
     * @param planId 计划ID
     * @return 任务列表
     */
    List<VideoRecordingTaskVO> getTasksByPlan(Long planId);

    /**
     * 查询指定时间范围的录像任务
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 任务列表
     */
    List<VideoRecordingTaskVO> getTasksByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 重试失败的录像任务
     *
     * @param taskId 任务ID
     * @return 新任务ID
     */
    Long retryFailedTask(Long taskId);

    /**
     * 处理事件触发的录像
     *
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @return 任务ID
     */
    Long handleEventRecording(String deviceId, String eventType);

    /**
     * 获取可重试的失败任务列表
     *
     * @param limit 限制数量
     * @return 任务列表
     */
    List<VideoRecordingTaskEntity> getRetryableTasks(Integer limit);

    /**
     * 统计录像文件存储大小
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 文件总大小（字节）
     */
    Long sumRecordingFileSize(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期的已完成录像任务
     *
     * @param beforeDate 清理此日期之前的任务
     * @return 删除行数
     */
    Integer cleanExpiredTasks(LocalDateTime beforeDate);

    /**
     * 检查设备是否正在录像
     *
     * @param deviceId 设备ID
     * @return 是否正在录像
     */
    Boolean isDeviceRecording(String deviceId);
}
