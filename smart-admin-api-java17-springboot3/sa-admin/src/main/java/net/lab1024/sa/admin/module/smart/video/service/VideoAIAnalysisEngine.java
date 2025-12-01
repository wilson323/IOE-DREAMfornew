package net.lab1024.sa.admin.module.smart.video.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频AI分析引擎接口
 * <p>
 * 提供人脸识别、目标检测、行为分析等AI功能
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-17
 */
public interface VideoAIAnalysisEngine {

    /**
     * 人脸检测和识别
     *
     * @param recordId   录像ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param threshold  相似度阈值
     * @return 人脸识别结果
     */
    Map<String, Object> detectFaces(String recordId, LocalDateTime startTime,
                                   LocalDateTime endTime, Double threshold);

    /**
     * 人脸特征搜索
     *
     * @param faceFeature   人脸特征数据
     * @param deviceIds     设备ID列表
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param similarityThreshold 相似度阈值
     * @return 搜索结果
     */
    List<Map<String, Object>> searchFace(byte[] faceFeature, List<Long> deviceIds,
                                        LocalDateTime startTime, LocalDateTime endTime,
                                        Double similarityThreshold);

    /**
     * 添加人脸特征到库
     *
     * @param personId     人员ID
     * @param faceFeature  人脸特征
     * @param faceImage    人脸图像
     * @return 特征ID
     */
    String addFaceFeature(Long personId, byte[] faceFeature, byte[] faceImage);

    /**
     * 删除人脸特征
     *
     * @param featureId 特征ID
     * @return 是否删除成功
     */
    boolean deleteFaceFeature(String featureId);

    /**
     * 目标检测和识别
     *
     * @param recordId     录像ID
     * @param targetTypes  目标类型列表（PERSON、VEHICLE、ANIMAL等）
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 检测结果
     */
    Map<String, Object> detectObjects(String recordId, List<String> targetTypes,
                                     LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 车牌识别
     *
     * @param recordId   录像ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 车牌识别结果
     */
    List<Map<String, Object>> recognizeLicensePlates(String recordId,
                                                    LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 行为分析
     *
     * @param recordId       录像ID
     * @param behaviorTypes  行为类型列表
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @return 行为分析结果
     */
    Map<String, Object> analyzeBehaviors(String recordId, List<String> behaviorTypes,
                                        LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 轨迹追踪
     *
     * @param deviceId   设备ID
     * @param targetId   目标ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 轨迹信息
     */
    Map<String, Object> trackTrajectory(Long deviceId, String targetId,
                                       LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 跨摄像头轨迹追踪
     *
     * @param targetId     目标ID
     * @param deviceIds    设备ID列表
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 跨摄像头轨迹
     */
    Map<String, Object> trackCrossCameraTrajectory(String targetId, List<Long> deviceIds,
                                                  LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 区域入侵检测
     *
     * @param deviceId     设备ID
     * @param alertRegion  告警区域坐标
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 入侵检测结果
     */
    List<Map<String, Object>> detectIntrusion(Long deviceId, String alertRegion,
                                             LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 人群密度分析
     *
     * @param deviceId      设备ID
     * @param analysisArea 分析区域坐标
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return 人群密度分析结果
     */
    Map<String, Object> analyzeCrowdDensity(Long deviceId, String analysisArea,
                                            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 异常行为检测
     *
     * @param deviceId       设备ID
     * @param detectionRules 检测规则列表
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @return 异常行为检测结果
     */
    List<Map<String, Object>> detectAnomalies(Long deviceId, List<String> detectionRules,
                                             LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 启动实时分析
     *
     * @param deviceId     设备ID
     * @param analysisType 分析类型
     * @param parameters   分析参数
     * @return 分析任务ID
     */
    String startRealTimeAnalysis(Long deviceId, String analysisType, Map<String, Object> parameters);

    /**
     * 停止实时分析
     *
     * @param analysisTaskId 分析任务ID
     * @return 是否停止成功
     */
    boolean stopRealTimeAnalysis(String analysisTaskId);

    /**
     * 获取实时分析结果
     *
     * @param analysisTaskId 分析任务ID
     * @return 分析结果
     */
    Map<String, Object> getRealTimeAnalysisResult(String analysisTaskId);

    /**
     * 人群流量统计
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param direction 方向（IN、OUT、BOTH）
     * @return 流量统计结果
     */
    Map<String, Object> analyzePeopleFlow(Long deviceId, LocalDateTime startTime, LocalDateTime endTime,
                                         String direction);

    /**
     * 车辆流量统计
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param laneIndex 车道索引（可选）
     * @return 车辆流量统计结果
     */
    Map<String, Object> analyzeVehicleFlow(Long deviceId, LocalDateTime startTime, LocalDateTime endTime,
                                          Integer laneIndex);

    /**
     * 声音事件检测
     *
     * @param recordId   录像ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 声音事件检测结果
     */
    List<Map<String, Object>> detectAudioEvents(String recordId,
                                               LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 烟雾火焰检测
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 检测结果
     */
    List<Map<String, Object>> detectSmokeAndFire(Long deviceId,
                                                LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取AI分析配置
     *
     * @param deviceId 设备ID
     * @return 配置信息
     */
    Map<String, Object> getAnalysisConfig(Long deviceId);

    /**
     * 更新AI分析配置
     *
     * @param deviceId 设备ID
     * @param config   配置信息
     * @return 是否更新成功
     */
    boolean updateAnalysisConfig(Long deviceId, Map<String, Object> config);

    /**
     * 获取AI模型状态
     *
     * @return 模型状态信息
     */
    Map<String, Object> getModelStatus();

    /**
     * 更新AI模型
     *
     * @param modelType  模型类型
     * @param modelPath  模型文件路径
     * @return 是否更新成功
     */
    boolean updateModel(String modelType, String modelPath);
}