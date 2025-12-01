package net.lab1024.sa.video.service;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频预览服务接口
 *
 * 提供实时视频流、截图、云台控制等预览功能
 * 遵循repowiki架构设计规范: Manager层负责复杂业务逻辑和第三方集成
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface VideoPreviewService {

    /**
     * 获取实时视频流地址
     *
     * @param deviceId 设备ID
     * @param quality 视频质量 (HD/SD/AUTO)
     * @return 视频流地址
     */
    String getLiveStreamUrl(Long deviceId, String quality);

    /**
     * 获取多路视频流地址（多摄像头支持）
     *
     * @param deviceIds 设备ID列表
     * @return 视频流地址映射
     */
    java.util.Map<Long, String> getMultiStreamUrls(List<Long> deviceIds);

    /**
     * 获取设备实时截图
     *
     * @param deviceId 设备ID
     * @return 截图路径
     */
    String getRealtimeSnapshot(Long deviceId);

    /**
     * 批量获取设备截图
     *
     * @param deviceIds 设备ID列表
     * @return 截图路径映射
     */
    java.util.Map<Long, String> getBatchSnapshots(List<Long> deviceIds);

    /**
     * 云台控制
     *
     * @param deviceId 设备ID
     * @param command 控制命令 (UP/DOWN/LEFT/RIGHT/ZOOM_IN/ZOOM_OUT/FOCUS_NEAR/FOCUS_FAR)
     * @param speed 速度(1-7)
     * @param preset 预置位(可选)
     * @return 控制结果
     */
    boolean ptzControl(Long deviceId, String command, Integer speed, Integer preset);

    /**
     * 设置预置位
     *
     * @param deviceId 设备ID
     * @param preset 预置位编号
     * @param name 预置位名称
     * @return 设置结果
     */
    boolean setPreset(Long deviceId, Integer preset, String name);

    /**
     * 调用预置位
     *
     * @param deviceId 设备ID
     * @param preset 预置位编号
     * @return 调用结果
     */
    boolean callPreset(Long deviceId, Integer preset);

    /**
     * 获取设备预览状态
     *
     * @param deviceId 设备ID
     * @return 设备预览状态信息
     */
    Object getPreviewStatus(Long deviceId);

    /**
     * 启动视频预览会话
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @return 会话ID
     */
    String startPreviewSession(Long deviceId, Long userId);

    /**
     * 停止视频预览会话
     *
     * @param sessionId 会话ID
     * @return 停止结果
     */
    boolean stopPreviewSession(String sessionId);

    /**
     * 获取当前预览会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Object> getActivePreviewSessions(Long userId);

    /**
     * 视频流质量切换
     *
     * @param deviceId 设备ID
     * @param quality 目标质量
     * @return 切换结果
     */
    boolean switchStreamQuality(Long deviceId, String quality);

    /**
     * 音频控制
     *
     * @param deviceId 设备ID
     * @param enabled 是否启用音频
     * @return 控制结果
     */
    boolean controlAudio(Long deviceId, boolean enabled);

    /**
     * 录像控制
     *
     * @param deviceId 设备ID
     * @param enabled 是否启用录像
     * @return 控制结果
     */
    boolean controlRecording(Long deviceId, boolean enabled);

    /**
     * 获取设备能力信息
     *
     * @param deviceId 设备ID
     * @return 设备能力信息
     */
    Object getDeviceCapabilities(Long deviceId);
}