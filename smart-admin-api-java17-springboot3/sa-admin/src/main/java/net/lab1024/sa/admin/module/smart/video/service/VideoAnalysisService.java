package net.lab1024.sa.admin.module.smart.video.service;

import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.admin.module.video.domain.entity.MonitorEventEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频分析服务接口
 *
 * 提供AI视频分析、行为识别、异常检测等智能分析功能
 * 遵循repowiki架构设计规范: Manager层负责复杂业务逻辑和第三方集成
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface VideoAnalysisService {

    /**
     * 分页查询监控事件
     *
     * @param pageParam 分页参数
     * @param deviceId 设备ID(可选)
     * @param eventType 事件类型(可选)
     * @param eventLevel 事件级别(可选)
     * @param startTime 开始时间(可选)
     * @param endTime 结束时间(可选)
     * @param isHandled 是否已处理(可选)
     * @return 分页结果
     */
    PageResult<MonitorEventEntity> pageMonitorEvents(PageParam pageParam, Long deviceId,
                                                        String eventType, String eventLevel,
                                                        LocalDateTime startTime, LocalDateTime endTime,
                                                        Integer isHandled);

    /**
     * 启动实时分析
     *
     * @param deviceId 设备ID
     * @param analysisTypes 分析类型列表
     * @return 分析任务ID
     */
    String startRealTimeAnalysis(Long deviceId, List<String> analysisTypes);

    /**
     * 停止实时分析
     *
     * @param analysisTaskId 分析任务ID
     * @return 停止结果
     */
    boolean stopRealTimeAnalysis(String analysisTaskId);

    /**
     * 人脸检测分析
     *
     * @param recordId 录像ID
     * @param startTime 分析开始时间
     * @param endTime 分析结束时间
     * @return 人脸检测结果
     */
    Object detectFaces(Long recordId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 车辆检测分析
     *
     * @param recordId 录像ID
     * @param startTime 分析开始时间
     * @param endTime 分析结束时间
     * @return 车辆检测结果
     */
    Object detectVehicles(Long recordId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 行为识别分析
     *
     * @param recordId 录像ID
     * @param behaviorTypes 行为类型列表
     * @param startTime 分析开始时间
     * @param endTime 分析结束时间
     * @return 行为识别结果
     */
    Object recognizeBehaviors(Long recordId, List<String> behaviorTypes,
                                 LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 人群密度分析
     *
     * @param deviceId 设备ID
     * @param analysisArea 分析区域坐标
     * @return 人群密度信息
     */
    Object analyzeCrowdDensity(Long deviceId, String analysisArea);

    /**
     * 轨向轨迹分析
     *
     * @param deviceId 设备ID
     * @param targetId 目标ID
     * @param startTime 分析开始时间
     * @param endTime 分析结束时间
     * @return 轨向轨迹数据
     */
    Object analyzeTrajectory(Long deviceId, String targetId,
                              LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 异常行为检测
     *
     * @param deviceId 设备ID
     * @param detectionRules 检测规则列表
     * @return 异常检测结果
     */
    Object detectAnomalies(Long deviceId, List<String> detectionRules);

    /**
     * 区域入侵检测
     *
     * @param deviceId 设备ID
     * @param alertRegion 告警区域坐标
     * @return 入侵检测结果
     */
    Object detectIntrusion(Long deviceId, String alertRegion);

    /**
     * 越线检测分析
     *
     * @param deviceId 设备ID
     * @param crossingLines 越线坐标列表
     * @return 越线检测结果
     */
    Object detectLineCrossing(Long deviceId, List<String> crossingLines);

    /**
     * 物体识别分析
     *
     * @param recordId 录像ID
     * @param targetTypes 目标类型列表
     * @param startTime 分析开始时间
     * @param endTime 分析结束时间
     * @return 物体识别结果
     */
    Object recognizeObjects(Long recordId, List<String> targetTypes,
                            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 声音事件检测
     *
     * @param recordId 录像ID
     * @param audioTypes 音频类型列表
     * @return 声音事件检测结果
     */
    Object detectAudioEvents(Long recordId, List<String> audioTypes);

    /**
     * 人员计数分析
     *
     * @param deviceId 设备ID
     * @param countingArea 计数区域坐标
     * @param timeWindow 时间窗口(秒)
     * @return 人员计数结果
     */
    Object countPeople(Long deviceId, String countingArea, Integer timeWindow);

    /**
     * 热力图分析
     *
     * @param deviceId 设备ID
     * @param analysisArea 分析区域
     * @param timeRange 时间范围
     * @return 热力图数据
     */
    Object generateHeatmap(Long deviceId, String analysisArea, String timeRange);

    /**
     * 车流量统计
     *
     * @param deviceId 设备ID
     * @param laneLines 车道线坐标
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @return 车流量统计数据
     */
    Object analyzeTrafficFlow(Long deviceId, List<String> laneLines,
                              LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 设置分析规则
     *
     * @param deviceId 设备ID
     * @param analysisRules 分析规则配置
     * @return 设置结果
     */
    boolean setAnalysisRules(Long deviceId, Map<String, Object> analysisRules);

    /**
     * 获取分析规则
     *
     * @param deviceId 设备ID
     * @return 分析规则配置
     */
    Object getAnalysisRules(Long deviceId);

    /**
     * 创建分析报告
     *
     * @param deviceId 设备ID
     * @param reportType 报告类型
     * @param timeRange 时间范围
     * @return 报告ID
     */
    String createAnalysisReport(Long deviceId, String reportType, String timeRange);

    /**
     * 获取分析报告
     *
     * @param reportId 报告ID
     * @return 分析报告内容
     */
    Object getAnalysisReport(String reportId);

    /**
     * 获取AI分析统计
     *
     * @param deviceId 设备ID(可选)
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return AI分析统计数据
     */
    Object getAnalysisStatistics(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 模型训练管理
     *
     * @param modelType 模型类型
     * @param trainingData 训练数据
     * @return 训练任务ID
     */
    String trainModel(String modelType, List<Object> trainingData);

    /**
     * 获取训练状态
     *
     * @param trainingTaskId 训练任务ID
     * @return 训练状态信息
     */
    Object getTrainingStatus(String trainingTaskId);

    /**
     * AI算法性能测试
     *
     * @param testDataset 测试数据集
     * @param algorithmIds 算法ID列表
     * @return 性能测试结果
     */
    Object benchmarkAlgorithms(List<Object> testDataset, List<String> algorithmIds);
}