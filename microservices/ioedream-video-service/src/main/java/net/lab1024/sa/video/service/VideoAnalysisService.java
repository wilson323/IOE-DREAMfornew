package net.lab1024.sa.video.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.video.domain.form.FaceSearchForm;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频AI分析服务接口
 * <p>
 * 提供视频智能分析的核心功能，包括：
 * </p>
 * <ul>
 *   <li>人脸识别与搜索</li>
 *   <li>目标检测与跟踪</li>
 *   <li>行为分析与异常检测</li>
 *   <li>轨迹分析与预测</li>
 *   <li>智能告警管理</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
public interface VideoAnalysisService {

    /**
     * 人脸搜索
     *
     * @param searchForm 搜索条件
     * @return 搜索结果
     */
    PageResult<Map<String, Object>> searchFace(FaceSearchForm searchForm);

    /**
     * 批量人脸搜索
     *
     * @param faceList 人脸图像列表
     * @return 搜索结果
     */
    List<Map<String, Object>> batchSearchFace(List<Map<String, Object>> faceList);

    /**
     * 轨迹分析
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param targetType 目标类型
     * @return 轨迹分析结果
     */
    Map<String, Object> analyzeTrajectory(Long deviceId, LocalDateTime startTime,
                                         LocalDateTime endTime, String targetType);

    /**
     * 行为分析
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param behaviorType 行为类型
     * @return 行为分析结果
     */
    List<Map<String, Object>> analyzeBehavior(Long deviceId, LocalDateTime startTime,
                                             LocalDateTime endTime, String behaviorType);

    /**
     * 异常检测
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常检测结果
     */
    List<Map<String, Object>> detectAnomaly(Long deviceId, LocalDateTime startTime,
                                           LocalDateTime endTime);

    /**
     * 获取AI分析配置
     *
     * @param deviceId 设备ID
     * @return 配置信息
     */
    Map<String, Object> getAnalyticsConfig(Long deviceId);

    /**
     * 更新AI分析配置
     *
     * @param deviceId 设备ID
     * @param config 配置信息
     */
    void updateAnalyticsConfig(Long deviceId, Map<String, Object> config);

    /**
     * 启动AI分析任务
     *
     * @param deviceId 设备ID
     * @return 任务ID
     */
    String startAnalyticsTask(Long deviceId);

    /**
     * 停止AI分析任务
     *
     * @param deviceId 设备ID
     */
    void stopAnalyticsTask(Long deviceId);

    /**
     * 获取AI分析任务状态
     *
     * @param deviceId 设备ID
     * @return 任务状态
     */
    Map<String, Object> getAnalyticsTaskStatus(Long deviceId);

    /**
     * 获取分析结果统计
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> getAnalyticsStatistics(Long deviceId, LocalDateTime startTime,
                                               LocalDateTime endTime);
}