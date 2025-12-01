package net.lab1024.sa.video.service;

import java.util.List;

/**
 * 视频分析服务接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
public interface VideoAnalyticsService {

    /**
     * 人脸搜索
     *
     * @param imageUrl 图片URL
     * @param threshold 相似度阈值
     * @param limit 返回结果数量限制
     * @return 搜索结果列表
     */
    List<Object> faceSearch(String imageUrl, Double threshold, Integer limit);

    /**
     * 批量人脸搜索
     *
     * @param imageUrls 图片URL列表
     * @param threshold 相似度阈值
     * @param limit 返回结果数量限制
     * @return 搜索结果列表
     */
    List<Object> batchFaceSearch(List<String> imageUrls, Double threshold, Integer limit);

    /**
     * 目标检测
     *
     * @param recordId 录像ID
     * @param objectTypes 目标类型列表
     * @return 检测结果列表
     */
    List<Object> objectDetection(Long recordId, List<String> objectTypes);

    /**
     * 轨迹分析
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 轨迹分析结果列表
     */
    List<Object> trajectoryAnalysis(Long deviceId, String startTime, String endTime);

    /**
     * 行为分析
     *
     * @param recordId 录像ID
     * @param behaviorTypes 行为类型列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 行为分析结果
     */
    String behaviorAnalysis(Long recordId, List<String> behaviorTypes, String startTime, String endTime);

    /**
     * 区域入侵检测
     *
     * @param deviceId 设备ID
     * @param alertRegion 告警区域坐标
     * @return 入侵检测结果
     */
    String detectAreaIntrusion(Long deviceId, String alertRegion);

    /**
     * 获取分析事件列表
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件列表
     */
    List<Object> getAnalyticsEvents(Long deviceId, String startTime, String endTime);
}