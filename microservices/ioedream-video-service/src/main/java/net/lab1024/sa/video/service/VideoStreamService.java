package net.lab1024.sa.video.service;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoStreamQueryForm;
import net.lab1024.sa.video.domain.form.VideoStreamStartForm;
import net.lab1024.sa.video.domain.vo.VideoStreamSessionVO;
import net.lab1024.sa.video.domain.vo.VideoStreamVO;

/**
 * 视频流服务接口
 * <p>
 * 提供实时视频流管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口在ioedream-video-service中
 * - 提供统一的业务接口
 * - 支持视频流会话管理
 * - 包含完整的流媒体操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface VideoStreamService {

    /**
     * 分页查询视频流
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<VideoStreamVO> queryStreams(@Valid VideoStreamQueryForm queryForm);

    /**
     * 查询视频流详情
     *
     * @param streamId 流ID
     * @return 流详情
     */
    ResponseDTO<VideoStreamVO> getStreamDetail(Long streamId);

    /**
     * 启动视频流
     *
     * @param startForm 启动表单
     * @return 启动结果
     */
    ResponseDTO<VideoStreamSessionVO> startStream(@Valid VideoStreamStartForm startForm);

    /**
     * 停止视频流
     *
     * @param streamId 流ID
     * @return 停止结果
     */
    ResponseDTO<Void> stopStream(Long streamId);

    /**
     * 批量停止视频流
     *
     * @param streamIds 流ID列表
     * @return 停止结果
     */
    ResponseDTO<Void> batchStopStreams(List<Long> streamIds);

    /**
     * 重启视频流
     *
     * @param streamId 流ID
     * @return 重启结果
     */
    ResponseDTO<VideoStreamSessionVO> restartStream(Long streamId);

    /**
     * 获取视频流会话信息
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    ResponseDTO<VideoStreamSessionVO> getStreamSession(String sessionId);

    /**
     * 关闭视频流会话
     *
     * @param sessionId 会话ID
     * @return 关闭结果
     */
    ResponseDTO<Void> closeStreamSession(String sessionId);

    /**
     * 获取设备视频流列表
     *
     * @param deviceId 设备ID
     * @return 视频流列表
     */
    ResponseDTO<List<VideoStreamVO>> getStreamsByDeviceId(Long deviceId);

    /**
     * 获取活跃的视频流列表
     *
     * @return 活跃流列表
     */
    ResponseDTO<List<VideoStreamVO>> getActiveStreams();

    /**
     * 获取视频截图
     *
     * @param deviceId  设备ID
     * @param channelId 通道ID
     * @return 截图信息
     */
    ResponseDTO<Map<String, Object>> captureSnapshot(Long deviceId, Long channelId);

    /**
     * 切换视频流质量
     *
     * @param streamId 流ID
     * @param quality  质量等级
     * @return 切换结果
     */
    ResponseDTO<Void> switchStreamQuality(Long streamId, String quality);

    /**
     * 获取流媒体质量统计
     *
     * @param streamId 流ID
     * @return 质量统计
     */
    ResponseDTO<Map<String, Object>> getStreamQualityStats(Long streamId);

    /**
     * 获取视频流统计信息
     *
     * @return 统计信息
     */
    ResponseDTO<Object> getStreamStatistics();

    /**
     * 检测视频流状态
     *
     * @param streamId 流ID
     * @return 流状态
     */
    ResponseDTO<Map<String, Object>> checkStreamStatus(Long streamId);

    /**
     * 录制视频流
     *
     * @param streamId 流ID
     * @param duration 录制时长（秒）
     * @return 录制结果
     */
    ResponseDTO<Map<String, Object>> recordStream(Long streamId, Integer duration);

    /**
     * 停止录制
     *
     * @param streamId 流ID
     * @return 停止结果
     */
    ResponseDTO<Void> stopRecording(Long streamId);

    /**
     * 获取录制列表
     *
     * @param deviceId 设备ID
     * @return 录制列表
     */
    ResponseDTO<List<Map<String, Object>>> getRecordings(Long deviceId);

    /**
     * 获取流播放地址
     * <p>
     * 获取指定视频流的多种协议播放地址（RTSP、RTMP、HLS、WebRTC等）
     * </p>
     *
     * @param streamId 流ID
     * @return 播放地址Map（协议名 -> URL）
     */
    ResponseDTO<Map<String, String>> getStreamPlayUrls(Long streamId);

    /**
     * 重连失败的流
     * <p>
     * 尝试重新连接断开的视频流
     * </p>
     *
     * @param streamId 流ID
     * @return 操作结果
     */
    ResponseDTO<Void> reconnectStream(Long streamId);

    /**
     * 暂停视频流（兼容方法）
     *
     * @param streamId 流ID
     * @return 操作结果
     */
    default ResponseDTO<Void> pauseStream(Long streamId) {
        // 当前版本未提供真实"暂停"能力：降级为停止流
        return stopStream(streamId);
    }

    /**
     * 恢复视频流（兼容方法）
     *
     * @param streamId 流ID
     * @return 操作结果
     */
    default ResponseDTO<Void> resumeStream(Long streamId) {
        // 当前版本未提供真实"恢复"能力：降级为重启流
        ResponseDTO<VideoStreamSessionVO> restartResult = restartStream(streamId);
        if (restartResult != null && restartResult.isSuccess()) {
            return ResponseDTO.ok();
        }
        return ResponseDTO.<Void>error("RESUME_STREAM_FAILED", "恢复视频流失败");
    }

    /**
     * 获取视频流信息（兼容方法，映射到getStreamDetail）
     *
     * @param streamId 流ID
     * @return 流信息
     */
    default ResponseDTO<VideoStreamVO> getStreamById(Long streamId) {
        return getStreamDetail(streamId);
    }

    /**
     * 根据设备查询视频流（兼容方法，映射到getStreamsByDeviceId）
     *
     * @param deviceId 设备ID
     * @return 流列表
     */
    default ResponseDTO<List<VideoStreamVO>> getStreamsByDevice(Long deviceId) {
        return getStreamsByDeviceId(deviceId);
    }

    /**
     * 启用流录制（兼容方法，映射到recordStream）
     *
     * @param streamId 流ID
     * @param duration 录制时长（分钟）
     * @return 操作结果
     */
    default ResponseDTO<Void> startStreamRecording(Long streamId, Integer duration) {
        // Controller传入分钟，转换为秒
        Integer seconds = duration == null ? null : duration * 60;
        ResponseDTO<Map<String, Object>> recordResult = recordStream(streamId, seconds);
        if (recordResult != null && recordResult.isSuccess()) {
            return ResponseDTO.ok();
        }
        return ResponseDTO.<Void>error("RECORD_START_FAILED", "启动录制失败");
    }

    /**
     * 停止流录制（兼容方法，映射到stopRecording）
     *
     * @param streamId 流ID
     * @return 操作结果
     */
    default ResponseDTO<Void> stopStreamRecording(Long streamId) {
        return stopRecording(streamId);
    }
}
