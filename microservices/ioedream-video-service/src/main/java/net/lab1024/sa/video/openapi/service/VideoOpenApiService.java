package net.lab1024.sa.video.openapi.service;

import net.lab1024.sa.video.openapi.domain.request.*;
import net.lab1024.sa.video.openapi.domain.response.*;
import net.lab1024.sa.common.domain.PageResult;

import java.util.List;

/**
 * 视频监控开放API服务接口
 * 提供实时视频、录像回放、AI分析等开放服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface VideoOpenApiService {

    /**
     * 获取实时视频流地址
     *
     * @param deviceId 设备ID
     * @param streamType 流类型
     * @param protocol 播放协议
     * @param quality 清晰度
     * @param token 访问令牌
     * @return 实时视频流信息
     */
    LiveStreamResponse getLiveStream(String deviceId, String streamType, String protocol, String quality, String token);

    /**
     * 开始实时视频推流
     *
     * @param deviceId 设备ID
     * @param request 推流请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 推流控制响应
     */
    StreamControlResponse startLiveStream(String deviceId, StartStreamRequest request, String token, String clientIp);

    /**
     * 停止实时视频推流
     *
     * @param deviceId 设备ID
     * @param request 停止推流请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 推流控制响应
     */
    StreamControlResponse stopLiveStream(String deviceId, StopStreamRequest request, String token, String clientIp);

    /**
     * 获取录像文件列表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 分页录像文件列表
     */
    PageResult<VideoRecordingResponse> getRecordings(RecordingQueryRequest request, String token);

    /**
     * 获取录像回放地址
     *
     * @param recordingId 录像ID
     * @param protocol 播放协议
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param token 访问令牌
     * @�回放响应
     */
    PlaybackResponse getPlaybackUrl(String recordingId, String protocol, String startTime, String endTime, String token);

    /**
     * 下载录像文件
     *
     * @param recordingId 录像ID
     * @param request 下载请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 下载任务响应
     */
    DownloadTaskResponse downloadRecording(String recordingId, DownloadRequest request, String token, String clientIp);

    /**
     * 人脸识别检测
     *
     * @param request 人脸检测请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 人脸检测结果
     */
    FaceDetectionResponse faceDetection(FaceDetectionRequest request, String token, String clientIp);

    /**
     * 行为分析检测
     *
     * @param request 行为分析请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 行为分析结果
     */
    BehaviorAnalysisResponse behaviorAnalysis(BehaviorAnalysisRequest request, String token, String clientIp);

    /**
     * 目标跟踪分析
     *
     * @param request 目标跟踪请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 目标跟踪结果
     */
    ObjectTrackingResponse objectTracking(ObjectTrackingRequest request, String token, String clientIp);

    /**
     * 获取AI分析结果列表
     *
     * @param request AI分析查询请求
     * @param token 访问令牌
     * @return 分页AI分析结果列表
     */
    PageResult<AIAnalysisResultResponse> getAIAnalysisResults(AIAnalysisQueryRequest request, String token);

    /**
     * 获取视频设备列表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 分页视频设备列表
     */
    PageResult<VideoDeviceResponse> getVideoDevices(VideoDeviceQueryRequest request, String token);

    /**
     * 获取视频设备详情
     *
     * @param deviceId 设备ID
     * @param token 访问令牌
     * @return 视频设备详情
     */
    VideoDeviceDetailResponse getVideoDeviceDetail(String deviceId, String token);

    /**
     * 云台控制
     *
     * @param deviceId 设备ID
     * @param request 云台控制请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     */
    void ptzControl(String deviceId, PTZControlRequest request, String token, String clientIp);

    /**
     * 获取视频设备实时状态
     *
     * @param deviceId 设备ID
     * @param token 访问令牌
     * @return 视频设备状态信息
     */
    VideoDeviceStatusResponse getVideoDeviceStatus(String deviceId, String token);

    /**
     * 获取视频统计分析
     *
     * @param statisticsType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param token 访问令牌
     * @return 视频统计分析
     */
    VideoStatisticsResponse getVideoStatistics(String statisticsType, String startDate, String endDate,
                                              String deviceId, Long areaId, String token);

    /**
     * 获取AI分析统计
     *
     * @param statisticsType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param analysisType 分析类型
     * @param deviceId 设备ID
     * @param token 访问令牌
     * @return AI分析统计信息
     */
    AIAnalysisStatisticsResponse getAIAnalysisStatistics(String statisticsType, String startDate, String endDate,
                                                        String analysisType, String deviceId, String token);
}
