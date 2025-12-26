package net.lab1024.sa.video.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.entity.VideoFaceEntity;
import net.lab1024.sa.video.entity.VideoFaceDetectionEntity;
import net.lab1024.sa.video.entity.VideoFaceCompareEntity;
import net.lab1024.sa.video.entity.VideoFaceSearchEntity;
import net.lab1024.sa.video.dao.VideoFaceDao;
import net.lab1024.sa.video.dao.VideoFaceDetectionDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 人脸识别管理器
 * 处理人脸库管理、人脸检测、人脸比对、人脸搜索等业务编排
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Slf4j
public class VideoFaceManager {

    private final VideoFaceDao videoFaceDao;
    private final VideoFaceDetectionDao videoFaceDetectionDao;

    public VideoFaceManager(VideoFaceDao videoFaceDao, VideoFaceDetectionDao videoFaceDetectionDao) {
        this.videoFaceDao = videoFaceDao;
        this.videoFaceDetectionDao = videoFaceDetectionDao;
    }

    // ==================== 人脸库管理 ====================

    /**
     * 添加人脸到人脸库
     */
    public VideoFaceEntity addFace(VideoFaceEntity faceEntity) {
        // 验证人员是否已存在人脸
        int existingFaceCount = videoFaceDao.countByPersonId(faceEntity.getPersonId());
        if (existingFaceCount > 0) {
            throw new RuntimeException("人员已存在人脸记录，请先删除原有人脸记录");
        }

        // 验证人员编号是否已存在
        int existingCodeCount = videoFaceDao.countByPersonCode(faceEntity.getPersonCode());
        if (existingCodeCount > 0) {
            throw new RuntimeException("人员编号已存在：" + faceEntity.getPersonCode());
        }

        // 验证证件信息是否已存在
        if (faceEntity.getIdCardNumber() != null && !faceEntity.getIdCardNumber().trim().isEmpty()) {
            int existingIdCardCount = videoFaceDao.countByIdCard(faceEntity.getIdCardType(), faceEntity.getIdCardNumber());
            if (existingIdCardCount > 0) {
                throw new RuntimeException("证件信息已存在：" + faceEntity.getIdCardNumber());
            }
        }

        // 设置默认值
        if (faceEntity.getFaceStatus() == null) {
            faceEntity.setFaceStatus(1); // 正常状态
        }
        if (faceEntity.getSyncFlag() == null) {
            faceEntity.setSyncFlag(0); // 未同步
        }

        // 插入人脸记录
        videoFaceDao.insert(faceEntity);
        return faceEntity;
    }

    /**
     * 更新人脸信息
     */
    public VideoFaceEntity updateFace(VideoFaceEntity faceEntity) {
        // 验证人脸是否存在
        VideoFaceEntity existingFace = videoFaceDao.selectById(faceEntity.getFaceId());
        if (existingFace == null) {
            throw new RuntimeException("人脸不存在");
        }

        // 如果变更了人员编号，检查是否重复
        if (!existingFace.getPersonCode().equals(faceEntity.getPersonCode())) {
            int codeCount = videoFaceDao.countByPersonCode(faceEntity.getPersonCode());
            if (codeCount > 0) {
                throw new RuntimeException("人员编号已存在：" + faceEntity.getPersonCode());
            }
        }

        // 如果变更了证件信息，检查是否重复
        if (faceEntity.getIdCardNumber() != null && !faceEntity.getIdCardNumber().trim().isEmpty()
                && !faceEntity.getIdCardNumber().equals(existingFace.getIdCardNumber())) {
            int idCardCount = videoFaceDao.countByIdCard(faceEntity.getIdCardType(), faceEntity.getIdCardNumber());
            if (idCardCount > 0) {
                throw new RuntimeException("证件信息已存在：" + faceEntity.getIdCardNumber());
            }
        }

        // 更新人脸记录
        videoFaceDao.updateById(faceEntity);
        return faceEntity;
    }

    /**
     * 删除人脸
     */
    public boolean deleteFace(Long faceId) {
        VideoFaceEntity faceEntity = videoFaceDao.selectById(faceId);
        if (faceEntity == null) {
            return false;
        }
        return videoFaceDao.deleteById(faceId) > 0;
    }

    /**
     * 删除人员的所有人脸
     */
    public int deleteFacesByPersonId(Long personId) {
        return videoFaceDao.deleteByPersonId(personId);
    }

    /**
     * 根据人员ID查询人脸
     */
    public List<VideoFaceEntity> getFacesByPersonId(Long personId) {
        return videoFaceDao.selectByPersonId(personId);
    }

    /**
     * 根据人员编号查询人脸
     */
    public List<VideoFaceEntity> getFacesByPersonCode(String personCode) {
        return videoFaceDao.selectByPersonCode(personCode);
    }

    /**
     * 查询有效期内的人脸
     */
    public List<VideoFaceEntity> getValidFaces() {
        return videoFaceDao.selectValidFaces(LocalDateTime.now());
    }

    /**
     * 获取人脸库统计信息
     */
    public Map<String, Object> getFaceLibraryStatistics() {
        // 总人脸数
        Long totalFaces = videoFaceDao.selectCount(null);

        // 按人员类型统计
        List<Map<String, Object>> personTypeStats = videoFaceDao.countByPersonType();

        // 按部门统计
        List<Map<String, Object>> departmentStats = videoFaceDao.countByDepartment();

        // 按状态统计
        List<Map<String, Object>> statusStats = videoFaceDao.countByFaceStatus();

        return Map.of(
                "totalFaces", totalFaces,
                "personTypeStats", personTypeStats,
                "departmentStats", departmentStats,
                "statusStats", statusStats
        );
    }

    /**
     * 清理过期人脸
     */
    public int cleanExpiredFaces() {
        return videoFaceDao.cleanExpiredFaces(LocalDateTime.now());
    }

    // ==================== 人脸检测管理 ====================

    /**
     * 保存人脸检测结果
     */
    public VideoFaceDetectionEntity saveDetectionResult(VideoFaceDetectionEntity detectionEntity) {
        // 设置默认值
        if (detectionEntity.getProcessStatus() == null) {
            detectionEntity.setProcessStatus(0); // 未处理
        }
        if (detectionEntity.getAlarmTriggered() == null) {
            detectionEntity.setAlarmTriggered(0); // 未触发告警
        }

        // 插入检测记录
        videoFaceDetectionDao.insert(detectionEntity);
        return detectionEntity;
    }

    /**
     * 处理人脸检测告警
     */
    public boolean processDetectionAlarm(Long detectionId, Integer processStatus, Long userId, String remark) {
        int result = videoFaceDetectionDao.updateProcessStatus(detectionId, processStatus, userId, remark);
        return result > 0;
    }

    /**
     * 批量处理人脸检测告警
     */
    public int batchProcessDetectionAlarms(String detectionIds, Integer processStatus, Long userId) {
        return videoFaceDetectionDao.batchUpdateProcessStatus(detectionIds, processStatus, userId);
    }

    /**
     * 获取未处理的告警记录
     */
    public List<VideoFaceDetectionEntity> getUnprocessedAlarms() {
        return videoFaceDetectionDao.selectUnprocessedAlarms();
    }

    /**
     * 获取设备检测记录
     */
    public List<VideoFaceDetectionEntity> getDeviceDetections(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return videoFaceDetectionDao.selectByDeviceIdAndTimeRange(deviceId, startTime, endTime);
    }

    /**
     * 获取人员检测记录
     */
    public List<VideoFaceDetectionEntity> getPersonDetections(Long personId, LocalDateTime startTime, LocalDateTime endTime) {
        List<VideoFaceDetectionEntity> allDetections = videoFaceDetectionDao.selectByMatchedPersonId(personId);

        // 按时间过滤
        return allDetections.stream()
                .filter(detection -> {
                    if (startTime != null && detection.getDetectionTime().isBefore(startTime)) {
                        return false;
                    }
                    if (endTime != null && detection.getDetectionTime().isAfter(endTime)) {
                        return false;
                    }
                    return true;
                })
                .toList();
    }

    /**
     * 获取检测统计信息
     */
    public Map<String, Object> getDetectionStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        // 按日期统计
        List<Map<String, Object>> dateStats = videoFaceDetectionDao.countByDate(startTime, endTime);

        // 按设备统计
        List<Map<String, Object>> deviceStats = videoFaceDetectionDao.countByDevice();

        // 告警统计
        List<Map<String, Object>> alarmStats = videoFaceDetectionDao.countAlarmTypes();

        // 处理状态统计
        List<Map<String, Object>> processStats = videoFaceDetectionDao.countByProcessStatus();

        return Map.of(
                "dateStats", dateStats,
                "deviceStats", deviceStats,
                "alarmStats", alarmStats,
                "processStats", processStats
        );
    }

    /**
     * 清理历史检测记录
     */
    public int cleanOldDetectionRecords(LocalDateTime cutoffTime) {
        return videoFaceDetectionDao.cleanOldDetections(cutoffTime);
    }

    // ==================== 人脸比对管理 ====================

    /**
     * 执行人脸比对（1:1验证）
     */
    public Map<String, Object> performFaceCompare(String sourceFaceUrl, String targetFaceUrl, BigDecimal similarityThreshold) {
        // TODO: 调用人脸比对算法服务
        // 这里需要集成具体的人脸识别算法SDK

        // 模拟比对结果
        BigDecimal similarity = new BigDecimal("92.5");
        boolean isMatch = similarity.compareTo(similarityThreshold) >= 0;
        long duration = 150; // 毫秒

        return Map.of(
                "similarity", similarity,
                "isMatch", isMatch,
                "duration", duration,
                "confidence", new BigDecimal("95.8")
        );
    }

    /**
     * 执行人脸搜索（1:N识别）
     */
    public Map<String, Object> performFaceSearch(String searchFaceUrl, Integer searchLibrary, BigDecimal similarityThreshold, Integer maxResults) {
        // TODO: 调用人脸搜索算法服务
        // 这里需要集成具体的人脸识别算法SDK

        // 模拟搜索结果
        List<Map<String, Object>> searchResults = List.of(
                Map.of("faceId", "1698745325654325123", "personId", "1001", "personName", "张三", "similarity", 96.5),
                Map.of("faceId", "1698745325654325124", "personId", "1002", "personName", "李四", "similarity", 89.2),
                Map.of("faceId", "1698745325654325125", "personId", "1003", "personName", "王五", "similarity", 85.7)
        );

        long duration = 1250; // 毫秒
        int matchedCount = searchResults.size();
        BigDecimal maxSimilarity = searchResults.stream()
                .map(result -> new BigDecimal(result.get("similarity").toString()))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal avgSimilarity = searchResults.stream()
                .map(result -> new BigDecimal(result.get("similarity").toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(matchedCount), 2, BigDecimal.ROUND_HALF_UP);

        return Map.of(
                "searchResults", searchResults,
                "matchedCount", matchedCount,
                "maxSimilarity", maxSimilarity,
                "avgSimilarity", avgSimilarity,
                "duration", duration,
                "searchStatus", 1 // 搜索完成
        );
    }

    /**
     * 获取人脸搜索建议
     */
    public Map<String, Object> getFaceSearchSuggestions(String personCode, String personName) {
        // 根据人员信息搜索相似人脸
        List<VideoFaceEntity> suggestedFaces = List.of();

        if (personCode != null && !personCode.trim().isEmpty()) {
            suggestedFaces = videoFaceDao.selectByPersonCode(personCode);
        }

        // 如果没有找到，按姓名模糊搜索
        if (suggestedFaces.isEmpty() && personName != null && !personName.trim().isEmpty()) {
            suggestedFaces = videoFaceDao.selectByNameKeyword(personName);
        }

        return Map.of(
                "suggestions", suggestedFaces,
                "count", suggestedFaces.size()
        );
    }

    // ==================== 人脸质量控制 ====================

    /**
     * 评估人脸质量
     */
    public Map<String, Object> evaluateFaceQuality(String faceImageUrl) {
        // TODO: 调用人脸质量评估算法

        // 模拟质量评估结果
        BigDecimal qualityScore = new BigDecimal("88.5");
        boolean isAcceptable = qualityScore.compareTo(new BigDecimal("75.0")) >= 0;

        return Map.of(
                "qualityScore", qualityScore,
                "isAcceptable", isAcceptable,
                "sharpness", new BigDecimal("90.2"),
                "brightness", new BigDecimal("85.7"),
                "clarity", new BigDecimal("87.3"),
                "completeness", new BigDecimal("91.8")
        );
    }

    /**
     * 检查人脸是否满足比对要求
     */
    public boolean checkFaceQualityForCompare(BigDecimal qualityScore) {
        return qualityScore != null && qualityScore.compareTo(new BigDecimal("75.0")) >= 0;
    }

    /**
     * 检查人脸是否满足注册要求
     */
    public boolean checkFaceQualityForRegister(BigDecimal qualityScore) {
        return qualityScore != null && qualityScore.compareTo(new BigDecimal("80.0")) >= 0;
    }
}
