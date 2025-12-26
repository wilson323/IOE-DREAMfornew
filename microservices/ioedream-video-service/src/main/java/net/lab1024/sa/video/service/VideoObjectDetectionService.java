package net.lab1024.sa.video.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.video.domain.form.VideoObjectDetectionForm;
import net.lab1024.sa.video.domain.vo.VideoObjectDetectionVO;
import net.lab1024.sa.common.entity.video.VideoObjectDetectionEntity;
import net.lab1024.sa.common.entity.video.VideoObjectTrackingEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频目标检测服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
public interface VideoObjectDetectionService {

    // ==================== 基础CRUD操作 ====================

    /**
     * 分页查询目标检测记录
     */
    ResponseDTO<PageResult<VideoObjectDetectionVO>> queryDetectionPage(VideoObjectDetectionForm form);

    /**
     * 根据ID查询检测记录详情
     */
    ResponseDTO<VideoObjectDetectionVO> getDetectionById(Long detectionId);

    /**
     * 保存目标检测记录
     */
    ResponseDTO<VideoObjectDetectionVO> saveDetection(VideoObjectDetectionEntity detection);

    /**
     * 批量保存目标检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> batchSaveDetections(List<VideoObjectDetectionEntity> detections);

    /**
     * 更新目标检测记录
     */
    ResponseDTO<VideoObjectDetectionVO> updateDetection(VideoObjectDetectionEntity detection);

    /**
     * 删除目标检测记录
     */
    ResponseDTO<Void> deleteDetection(Long detectionId);

    /**
     * 批量删除目标检测记录
     */
    ResponseDTO<Integer> batchDeleteDetections(List<Long> detectionIds);

    // ==================== 目标检测操作 ====================

    /**
     * 执行目标检测
     */
    ResponseDTO<VideoObjectDetectionVO> detectObjects(Long deviceId, String videoStreamUrl, VideoObjectDetectionForm params);

    /**
     * 批量执行目标检测
     */
    ResponseDTO<List<VideoObjectDetectionVO>> batchDetectObjects(List<Long> deviceIds, VideoObjectDetectionForm params);

    /**
     * 检测图片中的目标
     */
    ResponseDTO<List<VideoObjectDetectionVO>> detectObjectsInImage(String imageUrl, VideoObjectDetectionForm params);

    /**
     * 检测视频文件中的目标
     */
    ResponseDTO<List<VideoObjectDetectionVO>> detectObjectsInVideo(String videoUrl, VideoObjectDetectionForm params);

    /**
     * 实时目标检测流
     */
    ResponseDTO<String> startRealTimeDetection(Long deviceId, VideoObjectDetectionForm params);

    /**
     * 停止实时目标检测
     */
    ResponseDTO<Void> stopRealTimeDetection(Long deviceId);

    // ==================== 目标跟踪操作 ====================

    /**
     * 启动目标跟踪
     */
    ResponseDTO<VideoObjectTrackingEntity> startObjectTracking(String objectId, Long deviceId, VideoObjectDetectionForm params);

    /**
     * 停止目标跟踪
     */
    ResponseDTO<VideoObjectTrackingEntity> stopObjectTracking(String trackingId);

    /**
     * 查询目标轨迹
     */
    ResponseDTO<List<Map<String, Object>>> getObjectTrajectory(String objectId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 预测目标运动轨迹
     */
    ResponseDTO<Map<String, Object>> predictObjectTrajectory(String objectId, Integer predictSeconds);

    // ==================== 目标查询和过滤 ====================

    /**
     * 根据设备查询检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryByDeviceId(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据目标类型查询检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryByObjectType(Integer objectType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询高置信度检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryHighConfidence(Double minConfidence, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询大目标检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryLargeObjects(Double minRelativeSize, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询快速运动目标
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryFastMovingObjects(Double minSpeed, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定区域内的检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryByArea(Long areaId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询禁区内的检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryRestrictedAreaDetections(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 告警相关操作 ====================

    /**
     * 查询触发了告警的检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryAlertTriggered(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定告警级别的检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryByAlertLevel(Integer alertLevel, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询区域入侵检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryAreaIntrusionDetections(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询未处理的告警记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryUnprocessedAlerts();

    /**
     * 手动触发告警
     */
    ResponseDTO<Void> triggerManualAlert(Long detectionId, String alertType, String alertDescription);

    // ==================== 关联查询操作 ====================

    /**
     * 查询关联人脸的检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryWithAssociatedFace(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询关联人员的检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryWithAssociatedPerson(Long personId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询关联车辆的检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryWithAssociatedVehicle(String plateNumber, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据目标ID查询检测记录
     */
    ResponseDTO<List<VideoObjectDetectionVO>> queryByObjectId(String objectId);

    // ==================== 统计分析操作 ====================

    /**
     * 获取目标检测统计数据
     */
    ResponseDTO<Map<String, Object>> getDetectionStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取目标类型分布统计
     */
    ResponseDTO<List<Map<String, Object>>> getObjectTypeStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取告警级别分布统计
     */
    ResponseDTO<List<Map<String, Object>>> getAlertLevelStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取设备检测统计
     */
    ResponseDTO<List<Map<String, Object>>> getDeviceDetectionStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取区域检测统计
     */
    ResponseDTO<List<Map<String, Object>>> getAreaDetectionStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取检测趋势数据
     */
    ResponseDTO<List<Map<String, Object>>> getDetectionTrend(LocalDateTime startTime, LocalDateTime endTime, String trendType);

    /**
     * 获取目标出现频率统计
     */
    ResponseDTO<List<Map<String, Object>>> getObjectFrequencyStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取检测性能统计
     */
    ResponseDTO<Map<String, Object>> getDetectionPerformanceStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取目标密度分析
     */
    ResponseDTO<Map<String, Object>> getObjectDensityAnalysis(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取每日检测统计
     */
    ResponseDTO<List<Map<String, Object>>> getDailyDetectionStatistics(LocalDateTime startDate);

    // ==================== 数据处理操作 ====================

    /**
     * 更新处理状态
     */
    ResponseDTO<Void> updateProcessStatus(Long detectionId, Integer processStatus);

    /**
     * 批量更新处理状态
     */
    ResponseDTO<Integer> batchUpdateProcessStatus(List<Long> detectionIds, Integer processStatus);

    /**
     * 验证检测结果
     */
    ResponseDTO<Void> verifyDetection(Long detectionId, Integer verificationResult, String verificationNote);

    /**
     * 批量验证检测结果
     */
    ResponseDTO<Integer> batchVerifyDetections(List<Long> detectionIds, Integer verificationResult, String verificationNote);

    /**
     * 关联人脸信息
     */
    ResponseDTO<Void> associateFace(Long detectionId, String faceId);

    /**
     * 关联人员信息
     */
    ResponseDTO<Void> associatePerson(Long detectionId, Long personId, String personName);

    /**
     * 关联车辆信息
     */
    ResponseDTO<Void> associateVehicle(Long detectionId, String plateNumber, String vehicleBrand, String vehicleModel, String vehicleColor);

    // ==================== 数据管理操作 ====================

    /**
     * 清理历史检测记录
     */
    ResponseDTO<Integer> cleanOldDetections(LocalDateTime cutoffTime);

    /**
     * 导出检测数据
     */
    ResponseDTO<String> exportDetectionData(VideoObjectDetectionForm form);

    /**
     * 导入检测数据
     */
    ResponseDTO<Integer> importDetectionData(String filePath);

    /**
     * 同步检测记录到索引表
     */
    ResponseDTO<Integer> syncDetectionIndex(LocalDateTime startTime);

    // ==================== 高级分析操作 ====================

    /**
     * 分析目标行为模式
     */
    ResponseDTO<Map<String, Object>> analyzeObjectBehaviorPattern(String objectId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检测异常行为
     */
    ResponseDTO<List<VideoObjectDetectionVO>> detectAbnormalBehavior(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 生成目标分析报告
     */
    ResponseDTO<Map<String, Object>> generateObjectAnalysisReport(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取目标轨迹分析
     */
    ResponseDTO<Map<String, Object>> getObjectTrajectoryAnalysis(String objectId);

    /**
     * 预测目标行为
     */
    ResponseDTO<Map<String, Object>> predictObjectBehavior(String objectId, Integer predictMinutes);

    // ==================== 配置管理操作 ====================

    /**
     * 获取检测算法配置
     */
    ResponseDTO<Map<String, Object>> getDetectionAlgorithmConfig();

    /**
     * 更新检测算法配置
     */
    ResponseDTO<Void> updateDetectionAlgorithmConfig(Map<String, Object> config);

    /**
     * 获取目标类型配置
     */
    ResponseDTO<List<Map<String, Object>>> getObjectTypeConfig();

    /**
     * 更新目标类型配置
     */
    ResponseDTO<Void> updateObjectTypeConfig(Integer objectType, Map<String, Object> config);

    // ==================== 质量控制操作 ====================

    /**
     * 获取检测质量统计
     */
    ResponseDTO<Map<String, Object>> getDetectionQualityStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取模型性能统计
     */
    ResponseDTO<Map<String, Object>> getModelPerformanceStatistics(String algorithm, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 校准检测精度
     */
    ResponseDTO<Map<String, Object>> calibrateDetectionAccuracy(List<Long> detectionIds, List<Integer> correctFlags);

    /**
     * 获取误报率统计
     */
    ResponseDTO<Map<String, Object>> getFalsePositiveStatistics(LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 实时监控操作 ====================

    /**
     * 获取实时检测状态
     */
    ResponseDTO<Map<String, Object>> getRealTimeDetectionStatus();

    /**
     * 获取当前活跃目标
     */
    ResponseDTO<List<VideoObjectDetectionVO>> getActiveObjects();

    /**
     * 获取实时告警列表
     */
    ResponseDTO<List<VideoObjectDetectionVO>> getRealTimeAlerts();

    /**
     * 设置检测参数
     */
    ResponseDTO<Void> setDetectionParameters(Long deviceId, Map<String, Object> parameters);
}
