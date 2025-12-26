package net.lab1024.sa.video.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 视频录像回放流服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
public interface VideoRecordingPlaybackService {

    /**
     * 流式播放录像文件
     *
     * @param taskId 任务ID
     * @param request HTTP请求（用于解析Range头）
     * @return 流式响应
     */
    ResponseEntity<StreamingResponseBody> playbackRecording(Long taskId, HttpServletRequest request);

    /**
     * 获取播放信息
     *
     * @param taskId 任务ID
     * @return 播放信息（时长、分辨率、码率等）
     */
    java.util.Map<String, Object> getPlaybackInfo(Long taskId);

    /**
     * 生成缩略图
     *
     * @param taskId 任务ID
     * @param timeOffset 时间偏移（秒）
     * @return 缩略图字节数组
     */
    byte[] generateThumbnail(Long taskId, Integer timeOffset);

    /**
     * 生成预览切片
     *
     * @param taskId 任务ID
     * @param startTime 开始时间（秒）
     * @param duration 时长（秒）
     * @return 预览视频字节数组
     */
    byte[] generatePreviewClip(Long taskId, Integer startTime, Integer duration);

    /**
     * 获取视频元数据
     *
     * @param taskId 任务ID
     * @return 视频元数据（编码格式、分辨率、帧率等）
     */
    java.util.Map<String, Object> getVideoMetadata(Long taskId);

    /**
     * 检查文件是否可播放
     *
     * @param taskId 任务ID
     * @return 是否可播放
     */
    boolean isPlayable(Long taskId);

    /**
     * 获取播放URL
     *
     * @param taskId 任务ID
     * @param expireSeconds URL有效期（秒）
     * @return 播放URL
     */
    String getPlaybackUrl(Long taskId, Integer expireSeconds);

    /**
     * 创建播放会话
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 会话ID
     */
    String createPlaybackSession(Long taskId, Long userId);

    /**
     * 结束播放会话
     *
     * @param sessionId 会话ID
     */
    void endPlaybackSession(String sessionId);

    /**
     * 记录播放进度
     *
     * @param sessionId 会话ID
     * @param progress 播放进度（秒）
     */
    void recordPlaybackProgress(String sessionId, Integer progress);
}
