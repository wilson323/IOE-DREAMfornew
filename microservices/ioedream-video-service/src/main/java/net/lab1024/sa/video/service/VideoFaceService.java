package net.lab1024.sa.video.service;

import net.lab1024.sa.common.page.PageParam;
import net.lab1024.sa.common.page.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoFaceAddForm;
import net.lab1024.sa.video.domain.form.VideoFaceSearchForm;
import net.lab1024.sa.video.domain.vo.VideoFaceVO;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 人脸识别服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
public interface VideoFaceService {

    // ==================== 人脸库管理 ====================

    /**
     * 添加人脸
     */
    ResponseDTO<VideoFaceVO> addFace(@Valid VideoFaceAddForm addForm);

    /**
     * 更新人脸信息
     */
    ResponseDTO<VideoFaceVO> updateFace(Long faceId, @Valid VideoFaceAddForm updateForm);

    /**
     * 删除人脸
     */
    ResponseDTO<Void> deleteFace(Long faceId);

    /**
     * 批量删除人脸
     */
    ResponseDTO<Void> batchDeleteFaces(List<Long> faceIds);

    /**
     * 删除人员的所有人脸
     */
    ResponseDTO<Void> deleteFacesByPersonId(Long personId);

    /**
     * 获取人脸详情
     */
    ResponseDTO<VideoFaceVO> getFaceDetail(Long faceId);

    /**
     * 根据人员ID查询人脸
     */
    ResponseDTO<List<VideoFaceVO>> getFacesByPersonId(Long personId);

    /**
     * 根据人员编号查询人脸
     */
    ResponseDTO<List<VideoFaceVO>> getFacesByPersonCode(String personCode);

    /**
     * 分页查询人脸列表
     */
    ResponseDTO<PageResult<VideoFaceVO>> pageFaces(PageParam pageParam, String personCode, String personName,
                                                    Integer personType, Long departmentId, Integer faceStatus);

    /**
     * 获取人脸库统计信息
     */
    ResponseDTO<Map<String, Object>> getFaceLibraryStatistics();

    /**
     * 搜索人脸（按姓名或编号）
     */
    ResponseDTO<List<VideoFaceVO>> searchFaces(String keyword, Integer limit);

    /**
     * 获取最近添加的人脸
     */
    ResponseDTO<List<VideoFaceVO>> getRecentFaces(Integer limit);

    /**
     * 获取高质量人脸
     */
    ResponseDTO<List<VideoFaceVO>> getHighQualityFaces(BigDecimal qualityThreshold, Integer limit);

    /**
     * 获取有效期内的人脸
     */
    ResponseDTO<List<VideoFaceVO>> getValidFaces();

    /**
     * 获取过期人脸
     */
    ResponseDTO<List<VideoFaceVO>> getExpiredFaces();

    /**
     * 批量更新人脸状态
     */
    ResponseDTO<Integer> batchUpdateFaceStatus(List<Long> faceIds, Integer faceStatus);

    /**
     * 同步人脸到其他系统
     */
    ResponseDTO<Integer> syncFacesToSystem(List<Long> faceIds);

    /**
     * 清理过期人脸
     */
    ResponseDTO<Integer> cleanExpiredFaces();

    // ==================== 人脸搜索 ====================

    /**
     * 人脸搜索（1:N识别）
     */
    ResponseDTO<Map<String, Object>> faceSearch(@Valid VideoFaceSearchForm searchForm);

    /**
     * 人脸比对（1:1验证）
     */
    ResponseDTO<Map<String, Object>> faceCompare(String sourceFaceUrl, String targetFaceUrl, BigDecimal similarityThreshold);

    /**
     * 以脸搜脸（上传图片搜索）
     */
    ResponseDTO<Map<String, Object>> searchByFaceImage(String imageUrl, Integer searchLibrary, BigDecimal similarityThreshold, Integer maxResults);

    /**
     * 获取人脸搜索建议
     */
    ResponseDTO<Map<String, Object>> getFaceSearchSuggestions(String personCode, String personName);

    /**
     * 获取搜索历史记录
     */
    ResponseDTO<PageResult<Map<String, Object>>> getSearchHistory(PageParam pageParam, LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 人脸质量控制 ====================

    /**
     * 评估人脸质量
     */
    ResponseDTO<Map<String, Object>> evaluateFaceQuality(String faceImageUrl);

    /**
     * 检查人脸是否满足注册要求
     */
    ResponseDTO<Map<String, Object>> checkFaceQualityForRegister(String faceImageUrl);

    /**
     * 检查人脸是否满足比对要求
     */
    ResponseDTO<Map<String, Object>> checkFaceQualityForCompare(String faceImageUrl);

    // ==================== 人脸检测记录 ====================

    /**
     * 获取人脸检测记录
     */
    ResponseDTO<PageResult<Map<String, Object>>> getDetectionRecords(PageParam pageParam, Long deviceId, Long personId,
                                                                    LocalDateTime startTime, LocalDateTime endTime, Integer processStatus);

    /**
     * 获取未处理的告警记录
     */
    ResponseDTO<List<Map<String, Object>>> getUnprocessedAlarms();

    /**
     * 处理人脸检测告警
     */
    ResponseDTO<Void> processDetectionAlarm(Long detectionId, Integer processStatus, String remark);

    /**
     * 批量处理人脸检测告警
     */
    ResponseDTO<Integer> batchProcessDetectionAlarms(List<Long> detectionIds, Integer processStatus, String remark);

    /**
     * 获取设备检测统计
     */
    ResponseDTO<Map<String, Object>> getDeviceDetectionStatistics(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取人员活跃度统计
     */
    ResponseDTO<Map<String, Object>> getPersonActivityStatistics(Long personId, LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 数据分析和报表 ====================

    /**
     * 获取人脸库分析报告
     */
    ResponseDTO<Map<String, Object>> getFaceLibraryAnalysisReport();

    /**
     * 获取人脸识别趋势分析
     */
    ResponseDTO<Map<String, Object>> getRecognitionTrendAnalysis(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取部门人脸分布统计
     */
    ResponseDTO<Map<String, Object>> getDepartmentFaceDistribution();

    /**
     * 获取算法效果分析
     */
    ResponseDTO<Map<String, Object>> getAlgorithmPerformanceAnalysis();

    /**
     * 获取人脸质量分布统计
     */
    ResponseDTO<Map<String, Object>> getFaceQualityDistribution();

    // ==================== 工具方法 ====================

    /**
     * 生成人脸特征向量
     */
    ResponseDTO<String> generateFaceFeature(String faceImageUrl, Integer algorithmType);

    /**
     * 人脸图片预处理
     */
    ResponseDTO<String> preprocessFaceImage(String originalImageUrl);

    /**
     * 人脸图片质量增强
     */
    ResponseDTO<String> enhanceFaceImageQuality(String faceImageUrl);

    /**
     * 验证人脸特征有效性
     */
    ResponseDTO<Boolean> validateFaceFeature(String faceFeature, Integer featureDimension);
}