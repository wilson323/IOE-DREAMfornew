package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoFaceAddForm;
import net.lab1024.sa.video.domain.form.VideoFaceSearchForm;
import net.lab1024.sa.video.domain.vo.VideoFaceVO;
import net.lab1024.sa.video.service.VideoFaceService;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 人脸识别控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Slf4j
@RestController
@PermissionCheck(value = "VIDEO_MANAGE", description = "人脸识别管理权限")
@RequestMapping("/api/v1/video/face")
@Tag(name = "人脸识别管理", description = "人脸识别相关接口")
@Validated
public class VideoFaceController {

    @Resource
    private VideoFaceService videoFaceService;

    // ==================== 人脸库管理 ====================

    @Operation(summary = "添加人脸", description = "添加新的人脸到人脸库")
    @PostMapping("/add")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<VideoFaceVO> addFace(@Valid @RequestBody VideoFaceAddForm addForm) {
        return videoFaceService.addFace(addForm);
    }

    @Operation(summary = "更新人脸信息", description = "更新现有人脸的信息")
    @PutMapping("/{faceId}")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<VideoFaceVO> updateFace(
            @Parameter(description = "人脸ID", required = true) @PathVariable Long faceId,
            @Valid @RequestBody VideoFaceAddForm updateForm) {
        return videoFaceService.updateFace(faceId, updateForm);
    }

    @Operation(summary = "删除人脸", description = "删除指定的人脸")
    @DeleteMapping("/{faceId}")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Void> deleteFace(
            @Parameter(description = "人脸ID", required = true) @PathVariable Long faceId) {
        return videoFaceService.deleteFace(faceId);
    }

    @Operation(summary = "批量删除人脸", description = "批量删除多个人脸")
    @DeleteMapping("/batch")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Void> batchDeleteFaces(@RequestBody List<Long> faceIds) {
        return videoFaceService.batchDeleteFaces(faceIds);
    }

    @Operation(summary = "删除人员的所有人脸", description = "删除指定人员的所有人脸记录")
    @DeleteMapping("/person/{personId}")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Void> deleteFacesByPersonId(
            @Parameter(description = "人员ID", required = true) @PathVariable Long personId) {
        return videoFaceService.deleteFacesByPersonId(personId);
    }

    @Operation(summary = "获取人脸详情", description = "获取指定人脸的详细信息")
    @GetMapping("/{faceId}")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<VideoFaceVO> getFaceDetail(
            @Parameter(description = "人脸ID", required = true) @PathVariable Long faceId) {
        return videoFaceService.getFaceDetail(faceId);
    }

    @Operation(summary = "根据人员ID查询人脸", description = "查询指定人员的所有人脸")
    @GetMapping("/person/{personId}")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<List<VideoFaceVO>> getFacesByPersonId(
            @Parameter(description = "人员ID", required = true) @PathVariable Long personId) {
        return videoFaceService.getFacesByPersonId(personId);
    }

    @Operation(summary = "根据人员编号查询人脸", description = "通过人员编号查询人脸")
    @GetMapping("/code/{personCode}")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<List<VideoFaceVO>> getFacesByPersonCode(
            @Parameter(description = "人员编号", required = true) @PathVariable String personCode) {
        return videoFaceService.getFacesByPersonCode(personCode);
    }

    @Operation(summary = "分页查询人脸列表", description = "分页查询人脸库列表，支持多条件筛选")
    @GetMapping("/page")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<PageResult<VideoFaceVO>> pageFaces(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "人员编号") @RequestParam(required = false) String personCode,
            @Parameter(description = "人员姓名") @RequestParam(required = false) String personName,
            @Parameter(description = "人员类型") @RequestParam(required = false) Integer personType,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "人脸状态") @RequestParam(required = false) Integer faceStatus) {

        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageNum);
        pageParam.setPageSize(pageSize);

        return videoFaceService.pageFaces(pageParam, personCode, personName, personType, departmentId, faceStatus);
    }

    @Operation(summary = "获取人脸库统计信息", description = "获取人脸库的各项统计数据")
    @GetMapping("/statistics")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<Map<String, Object>> getFaceLibraryStatistics() {
        return videoFaceService.getFaceLibraryStatistics();
    }

    @Operation(summary = "搜索人脸", description = "根据姓名或编号搜索人脸")
    @GetMapping("/search")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<List<VideoFaceVO>> searchFaces(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "20") Integer limit) {
        return videoFaceService.searchFaces(keyword, limit);
    }

    @Operation(summary = "获取最近添加的人脸", description = "获取最近添加的人脸列表")
    @GetMapping("/recent")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<List<VideoFaceVO>> getRecentFaces(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        return videoFaceService.getRecentFaces(limit);
    }

    @Operation(summary = "获取高质量人脸", description = "获取质量分数高于指定阈值的人脸")
    @GetMapping("/high-quality")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<List<VideoFaceVO>> getHighQualityFaces(
            @Parameter(description = "质量阈值") @RequestParam(defaultValue = "85.0") BigDecimal qualityThreshold,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "20") Integer limit) {
        return videoFaceService.getHighQualityFaces(qualityThreshold, limit);
    }

    @Operation(summary = "获取有效期内的人脸", description = "获取所有在有效期内的人脸")
    @GetMapping("/valid")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<List<VideoFaceVO>> getValidFaces() {
        return videoFaceService.getValidFaces();
    }

    @Operation(summary = "获取过期人脸", description = "获取所有已过期的人脸")
    @GetMapping("/expired")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<List<VideoFaceVO>> getExpiredFaces() {
        return videoFaceService.getExpiredFaces();
    }

    @Operation(summary = "批量更新人脸状态", description = "批量更新多个人脸的状态")
    @PutMapping("/status/batch")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Integer> batchUpdateFaceStatus(
            @Parameter(description = "人脸ID列表", required = true) @RequestParam List<Long> faceIds,
            @Parameter(description = "人脸状态", required = true) @RequestParam Integer faceStatus) {
        return videoFaceService.batchUpdateFaceStatus(faceIds, faceStatus);
    }

    @Operation(summary = "同步人脸到其他系统", description = "将人脸数据同步到其他系统")
    @PostMapping("/sync")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Integer> syncFacesToSystem(
            @Parameter(description = "人脸ID列表，-1表示同步所有未同步的人脸", required = true) @RequestParam List<Long> faceIds) {
        return videoFaceService.syncFacesToSystem(faceIds);
    }

    @Operation(summary = "清理过期人脸", description = "清理已过期的人脸，将其状态更新为过期")
    @PostMapping("/clean")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Integer> cleanExpiredFaces() {
        return videoFaceService.cleanExpiredFaces();
    }

    // ==================== 人脸搜索 ====================

    @Operation(summary = "人脸搜索", description = "上传人脸图片进行1:N人脸识别搜索")
    @PostMapping("/search")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<Map<String, Object>> faceSearch(@Valid @RequestBody VideoFaceSearchForm searchForm) {
        return videoFaceService.faceSearch(searchForm);
    }

    @Operation(summary = "人脸比对", description = "进行1:1人脸验证比对")
    @PostMapping("/compare")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<Map<String, Object>> faceCompare(
            @Parameter(description = "源人脸图片URL", required = true) @RequestParam String sourceFaceUrl,
            @Parameter(description = "目标人脸图片URL", required = true) @RequestParam String targetFaceUrl,
            @Parameter(description = "相似度阈值") @RequestParam(defaultValue = "80.0") BigDecimal similarityThreshold) {
        return videoFaceService.faceCompare(sourceFaceUrl, targetFaceUrl, similarityThreshold);
    }

    @Operation(summary = "以脸搜脸", description = "上传图片进行人脸搜索")
    @PostMapping("/search-by-image")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<Map<String, Object>> searchByFaceImage(
            @Parameter(description = "人脸图片URL", required = true) @RequestParam String imageUrl,
            @Parameter(description = "搜索库类型") @RequestParam(defaultValue = "1") Integer searchLibrary,
            @Parameter(description = "相似度阈值") @RequestParam(defaultValue = "75.0") BigDecimal similarityThreshold,
            @Parameter(description = "最大返回数量") @RequestParam(defaultValue = "50") Integer maxResults) {
        return videoFaceService.searchByFaceImage(imageUrl, searchLibrary, similarityThreshold, maxResults);
    }

    @Operation(summary = "获取人脸搜索建议", description = "根据人员信息获取人脸搜索建议")
    @GetMapping("/search/suggestions")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<Map<String, Object>> getFaceSearchSuggestions(
            @Parameter(description = "人员编号") @RequestParam(required = false) String personCode,
            @Parameter(description = "人员姓名") @RequestParam(required = false) String personName) {
        return videoFaceService.getFaceSearchSuggestions(personCode, personName);
    }

    @Operation(summary = "获取搜索历史记录", description = "获取人脸搜索的历史记录")
    @GetMapping("/search/history")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<PageResult<Map<String, Object>>> getSearchHistory(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageNum);
        pageParam.setPageSize(pageSize);

        return videoFaceService.getSearchHistory(pageParam, startTime, endTime);
    }

    // ==================== 人脸质量控制 ====================

    @Operation(summary = "评估人脸质量", description = "评估人脸图片的质量")
    @PostMapping("/quality/evaluate")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<Map<String, Object>> evaluateFaceQuality(
            @Parameter(description = "人脸图片URL", required = true) @RequestParam String faceImageUrl) {
        return videoFaceService.evaluateFaceQuality(faceImageUrl);
    }

    @Operation(summary = "检查人脸注册质量", description = "检查人脸是否满足注册要求")
    @PostMapping("/quality/check-register")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<Map<String, Object>> checkFaceQualityForRegister(
            @Parameter(description = "人脸图片URL", required = true) @RequestParam String faceImageUrl) {
        return videoFaceService.checkFaceQualityForRegister(faceImageUrl);
    }

    @Operation(summary = "检查人脸比对质量", description = "检查人脸是否满足比对要求")
    @PostMapping("/quality/check-compare")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<Map<String, Object>> checkFaceQualityForCompare(
            @Parameter(description = "人脸图片URL", required = true) @RequestParam String faceImageUrl) {
        return videoFaceService.checkFaceQualityForCompare(faceImageUrl);
    }

    @Operation(summary = "生成人脸特征向量", description = "为人脸图片生成特征向量")
    @PostMapping("/feature/generate")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<String> generateFaceFeature(
            @Parameter(description = "人脸图片URL", required = true) @RequestParam String faceImageUrl,
            @Parameter(description = "算法类型") @RequestParam(defaultValue = "1") Integer algorithmType) {
        return videoFaceService.generateFaceFeature(faceImageUrl, algorithmType);
    }

    @Operation(summary = "验证人脸特征", description = "验证人脸特征向量的有效性")
    @PostMapping("/feature/validate")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Boolean> validateFaceFeature(
            @Parameter(description = "人脸特征向量", required = true) @RequestParam String faceFeature,
            @Parameter(description = "特征维度") @RequestParam @Min(128) Integer featureDimension) {
        return videoFaceService.validateFaceFeature(faceFeature, featureDimension);
    }

    // ==================== 人脸检测记录 ====================

    @Operation(summary = "获取人脸检测记录", description = "分页查询人脸检测记录")
    @GetMapping("/detection/records")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<PageResult<Map<String, Object>>> getDetectionRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "人员ID") @RequestParam(required = false) Long personId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "处理状态") @RequestParam(required = false) Integer processStatus) {

        PageParam pageParam = new PageParam();
        pageParam.setPageNum(pageNum);
        pageParam.setPageSize(pageSize);

        return videoFaceService.getDetectionRecords(pageParam, deviceId, personId, startTime, endTime, processStatus);
    }

    @Operation(summary = "获取未处理的告警记录", description = "获取所有未处理的人脸检测告警")
    @GetMapping("/detection/alarms/unprocessed")
    @PermissionCheck(value = "VIDEO_USE", description = "人脸识别操作")
    public ResponseDTO<List<Map<String, Object>>> getUnprocessedAlarms() {
        return videoFaceService.getUnprocessedAlarms();
    }

    @Operation(summary = "处理人脸检测告警", description = "处理单个人脸检测告警")
    @PutMapping("/detection/alarm/{detectionId}")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Void> processDetectionAlarm(
            @Parameter(description = "检测记录ID", required = true) @PathVariable Long detectionId,
            @Parameter(description = "处理状态", required = true) @RequestParam Integer processStatus,
            @Parameter(description = "处理备注") @RequestParam(required = false) String remark) {
        return videoFaceService.processDetectionAlarm(detectionId, processStatus, remark);
    }

    @Operation(summary = "批量处理人脸检测告警", description = "批量处理多个人脸检测告警")
    @PutMapping("/detection/alarms/batch")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Integer> batchProcessDetectionAlarms(
            @Parameter(description = "检测记录ID列表", required = true) @RequestParam List<Long> detectionIds,
            @Parameter(description = "处理状态", required = true) @RequestParam Integer processStatus,
            @Parameter(description = "处理备注") @RequestParam(required = false) String remark) {
        return videoFaceService.batchProcessDetectionAlarms(detectionIds, processStatus, remark);
    }

    // ==================== 数据分析和报表 ====================

    @Operation(summary = "获取人脸库分析报告", description = "获取人脸库的详细分析报告")
    @GetMapping("/analysis/library-report")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Map<String, Object>> getFaceLibraryAnalysisReport() {
        return videoFaceService.getFaceLibraryAnalysisReport();
    }

    @Operation(summary = "获取人脸识别趋势分析", description = "获取指定时间段内的人脸识别趋势分析")
    @GetMapping("/analysis/recognition-trend")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Map<String, Object>> getRecognitionTrendAnalysis(
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime) {
        return videoFaceService.getRecognitionTrendAnalysis(startTime, endTime);
    }

    @Operation(summary = "获取部门人脸分布统计", description = "获取各部门的人脸分布统计")
    @GetMapping("/analysis/department-distribution")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Map<String, Object>> getDepartmentFaceDistribution() {
        return videoFaceService.getDepartmentFaceDistribution();
    }

    @Operation(summary = "获取算法效果分析", description = "获取各种人脸识别算法的效果分析")
    @GetMapping("/analysis/algorithm-performance")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Map<String, Object>> getAlgorithmPerformanceAnalysis() {
        return videoFaceService.getAlgorithmPerformanceAnalysis();
    }

    @Operation(summary = "获取人脸质量分布统计", description = "获取人脸质量分数的分布统计")
    @GetMapping("/analysis/quality-distribution")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Map<String, Object>> getFaceQualityDistribution() {
        return videoFaceService.getFaceQualityDistribution();
    }

    @Operation(summary = "获取设备检测统计", description = "获取指定设备的检测统计信息")
    @GetMapping("/analysis/device-statistics/{deviceId}")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Map<String, Object>> getDeviceDetectionStatistics(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        return videoFaceService.getDeviceDetectionStatistics(deviceId, startTime, endTime);
    }

    @Operation(summary = "获取人员活跃度统计", description = "获取指定人员的活跃度统计")
    @GetMapping("/analysis/person-activity/{personId}")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "人脸管理操作")
    public ResponseDTO<Map<String, Object>> getPersonActivityStatistics(
            @Parameter(description = "人员ID", required = true) @PathVariable Long personId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        return videoFaceService.getPersonActivityStatistics(personId, startTime, endTime);
    }
}
