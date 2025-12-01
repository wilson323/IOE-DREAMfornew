package net.lab1024.sa.admin.module.smart.video.service;

import java.util.List;
import java.util.Map;

/**
 * 视频流处理器接口
 * <p>
 * 提供视频流的接入、转换、分发等核心功能
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-17
 */
public interface VideoStreamProcessor {

    /**
     * 启动RTSP流接入
     *
     * @param deviceId   设备ID
     * @param rtspUrl    RTSP流地址
     * @param streamId   流ID
     * @return 是否启动成功
     */
    boolean startRtspStream(Long deviceId, String rtspUrl, String streamId);

    /**
     * 停止RTSP流接入
     *
     * @param streamId 流ID
     * @return 是否停止成功
     */
    boolean stopRtspStream(String streamId);

    /**
     * 转换视频流为HLS格式
     *
     * @param streamId   原始流ID
     * @param outputUrl  输出URL
     * @return HLS流地址
     */
    String convertToHls(String streamId, String outputUrl);

    /**
     * 转换视频流为Web格式
     *
     * @param streamId   原始流ID
     * @param format     目标格式（flv、webm等）
     * @return Web流地址
     */
    String convertToWebFormat(String streamId, String format);

    /**
     * 多路视频同步
     *
     * @param streamIds 流ID列表
     * @param layout    布局模式（1x1, 2x2, 3x3, 4x4）
     * @return 合流后的流地址
     */
    String synchronizeStreams(List<String> streamIds, String layout);

    /**
     * 获取流状态
     *
     * @param streamId 流ID
     * @return 流状态信息
     */
    Map<String, Object> getStreamStatus(String streamId);

    /**
     * 获取所有活跃流列表
     *
     * @return 活跃流列表
     */
    List<Map<String, Object>> getActiveStreams();

    /**
     * 设置流参数
     *
     * @param streamId 流ID
     * @param params   参数配置
     * @return 是否设置成功
     */
    boolean setStreamParameters(String streamId, Map<String, Object> params);

    /**
     * 获取流统计信息
     *
     * @param streamId 流ID
     * @return 统计信息
     */
    Map<String, Object> getStreamStatistics(String streamId);

    /**
     * 录制流
     *
     * @param streamId   流ID
     * @param outputUrl  输出文件路径
     * @param duration   录制时长（秒）
     * @return 录制任务ID
     */
    String recordStream(String streamId, String outputUrl, Integer duration);

    /**
     * 停止录制
     *
     * @param recordId 录制任务ID
     * @return 是否停止成功
     */
    boolean stopRecording(String recordId);

    /**
     * 抓取快照
     *
     * @param streamId   流ID
     * @param outputUrl  输出文件路径
     * @return 快照文件路径
     */
    String captureSnapshot(String streamId, String outputUrl);
}