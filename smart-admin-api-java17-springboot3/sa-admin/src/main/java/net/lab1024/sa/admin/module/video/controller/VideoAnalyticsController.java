package net.lab1024.sa.admin.module.video.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.base.common.annotation.SaCheckLogin;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.admin.module.video.domain.entity.MonitorEventEntity;
import net.lab1024.sa.admin.module.video.domain.form.FaceSearchForm;
import net.lab1024.sa.admin.module.video.service.VideoAnalysisService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频AI分析控制器
 * <p>
 * 提供视频智能分析功能，包括：
 * </p>
 * <ul>
 * <li>人脸识别搜索</li>
 * <li>目标检测与识别</li>
 * <li>行为分析与异常检测</li>
 * <li>轨迹分析</li>
 * <li>智能告警</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
import lombok.extern.slf4j.Slf4j;
@RestController
@RequestMapping("/api/video/analytics")
@SaCheckLogin
@Tag(name = "视频AI分析", description = "人脸识别、目标检测、行为分析接口")
public class VideoAnalyticsController {

    @Resource
    private VideoAnalysisService videoAnalysisService;

    /**
     * 人脸特征搜索
     *
     * @param searchForm 人脸搜索表单
     * @return 人脸搜索结果
     */
    @Operation(summary = "人脸特征搜索")
    @PostMapping("/face-search")
    @SaCheckPermission("video:analytics:face-search")
    public ResponseDTO<Object> faceSearch(@RequestBody @Valid FaceSearchForm searchForm) {
        try {
            log.info("开始人脸特征搜索: deviceId={}, threshold={}",
                    searchForm.getDeviceId(), searchForm.getSimilarityThreshold());

            // 使用现有的detectFaces方法实现人脸搜索，需要先创建录像记录
            Object result = videoAnalysisService.detectFaces(
                    1L, // 模拟录像ID
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now()
            );

            log.info("人脸搜索完成");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("人脸搜索失败", e);
            return ResponseDTO.error("人脸搜索失败: " + e.getMessage());
        }
    }

    /**
     * 批量人脸特征搜索
     *
     * @param searchForms 搜索表单列表
     * @return 人脸搜索结果列表
     */
    @Operation(summary = "批量人脸特征搜索")
    @PostMapping("/face-search-batch")
    @SaCheckPermission("video:analytics:face-search")
    public ResponseDTO<List<Object>> faceSearchBatch(@RequestBody @Valid List<FaceSearchForm> searchForms) {
        try {
            log.info("开始批量人脸搜索: 搜索数量={}", searchForms.size());

            List<Object> results = List.of();
            for (FaceSearchForm searchForm : searchForms) {
                Object result = videoAnalysisService.detectFaces(
                        1L, // 模拟录像ID
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now()
                );
                results.add(result);
            }

            log.info("批量人脸搜索完成: 总数={}", results.size());
            return ResponseDTO.ok(results);
        } catch (Exception e) {
            log.error("批量人脸搜索失败", e);
            return ResponseDTO.error("批量人脸搜索失败: " + e.getMessage());
        }
    }

    /**
     * 目标检测
     *
     * @param recordId 录像ID
     * @param detectionType 检测类型：PERSON, VEHICLE, OBJECT
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 检测结果
     */
    @Operation(summary = "目标检测")
    @PostMapping("/object-detection")
    @SaCheckPermission("video:analytics:detect")
    public ResponseDTO<Object> objectDetection(
            @Parameter(description = "录像ID") @RequestParam @NotNull(message = "录像ID不能为空") Long recordId,
            @Parameter(description = "检测类型") @RequestParam(required = false) String detectionType,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        try {
            log.info("开始目标检测: recordId={}, detectionType={}", recordId, detectionType);

            List<String> targetTypes = detectionType != null ? List.of(detectionType) : List.of("PERSON", "VEHICLE");
            Object result = videoAnalysisService.recognizeObjects(
                    recordId,
                    targetTypes,
                    startTime != null ? startTime : LocalDateTime.now().minusHours(1),
                    endTime != null ? endTime : LocalDateTime.now()
            );

            log.info("目标检测完成");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("目标检测失败: recordId={}", recordId, e);
            return ResponseDTO.error("目标检测失败: " + e.getMessage());
        }
    }

    /**
     * 行为分析
     *
     * @param recordId 录像ID
     * @param behaviorTypes 行为类型列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 行为分析结果
     */
    @Operation(summary = "行为分析")
    @PostMapping("/behavior-analysis")
    @SaCheckPermission("video:analytics:analyze")
    public ResponseDTO<Object> behaviorAnalysis(
            @Parameter(description = "录像ID") @RequestParam @NotNull(message = "录像ID不能为空") Long recordId,
            @Parameter(description = "行为类型") @RequestParam(required = false) List<String> behaviorTypes,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        try {
            log.info("开始行为分析: recordId={}, behaviorTypes={}", recordId, behaviorTypes);

            List<String> types = behaviorTypes != null ? behaviorTypes : List.of("WALKING", "RUNNING", "STANDING");
            Object result = videoAnalysisService.recognizeBehaviors(
                    recordId,
                    types,
                    startTime != null ? startTime : LocalDateTime.now().minusHours(1),
                    endTime != null ? endTime : LocalDateTime.now()
            );

            log.info("行为分析完成");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("行为分析失败: recordId={}", recordId, e);
            return ResponseDTO.error("行为分析失败: " + e.getMessage());
        }
    }

    /**
     * 轨迹分析
     *
     * @param deviceId 设备ID
     * @param targetId 目标ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 轨迹分析结果
     */
    @Operation(summary = "轨迹分析")
    @PostMapping("/trajectory-analysis")
    @SaCheckPermission("video:analytics:analyze")
    public ResponseDTO<Object> trajectoryAnalysis(
            @Parameter(description = "设备ID") @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "目标ID") @RequestParam(required = false) String targetId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        try {
            log.info("开始轨迹分析: deviceId={}, targetId={}", deviceId, targetId);

            Object result = videoAnalysisService.analyzeTrajectory(
                    deviceId,
                    targetId,
                    startTime != null ? startTime : LocalDateTime.now().minusHours(1),
                    endTime != null ? endTime : LocalDateTime.now()
            );

            log.info("轨迹分析完成");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("轨迹分析失败: deviceId={}", deviceId, e);
            return ResponseDTO.error("轨迹分析失败: " + e.getMessage());
        }
    }

    /**
     * 区域入侵检测
     *
     * @param deviceId 设备ID
     * @param alertRegion 告警区域坐标
     * @return 入侵检测结果
     */
    @Operation(summary = "区域入侵检测")
    @PostMapping("/area-intrusion")
    @SaCheckPermission("video:analytics:intrusion")
    public ResponseDTO<Object> areaIntrusionDetection(
            @Parameter(description = "设备ID") @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "告警区域坐标") @RequestParam String alertRegion) {
        try {
            log.info("开始区域入侵检测: deviceId={}, alertRegion={}", deviceId, alertRegion);

            Object result = videoAnalysisService.detectIntrusion(deviceId, alertRegion);

            log.info("区域入侵检测完成");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("区域入侵检测失败: deviceId={}", deviceId, e);
            return ResponseDTO.error("区域入侵检测失败: " + e.getMessage());
        }
    }

    /**
     * 人群密度分析
     *
     * @param deviceId 设备ID
     * @param analysisArea 分析区域坐标
     * @return 人群密度分析结果
     */
    @Operation(summary = "人群密度分析")
    @PostMapping("/crowd-density")
    @SaCheckPermission("video:analytics:density")
    public ResponseDTO<Object> crowdDensityAnalysis(
            @Parameter(description = "设备ID") @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "分析区域坐标") @RequestParam String analysisArea) {
        try {
            log.info("开始人群密度分析: deviceId={}, analysisArea={}", deviceId, analysisArea);

            Object result = videoAnalysisService.analyzeCrowdDensity(deviceId, analysisArea);

            log.info("人群密度分析完成");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("人群密度分析失败: deviceId={}", deviceId, e);
            return ResponseDTO.error("人群密度分析失败: " + e.getMessage());
        }
    }

    /**
     * 异常行为检测
     *
     * @param deviceId 设备ID
     * @param detectionRules 检测规则列表
     * @return 异常行为检测结果
     */
    @Operation(summary = "异常行为检测")
    @PostMapping("/abnormal-behavior")
    @SaCheckPermission("video:analytics:alert")
    public ResponseDTO<Object> abnormalBehaviorDetection(
            @Parameter(description = "设备ID") @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "检测规则") @RequestParam(required = false) List<String> detectionRules) {
        try {
            log.info("开始异常行为检测: deviceId={}, detectionRules={}", deviceId, detectionRules);

            List<String> rules = detectionRules != null ? detectionRules : List.of("VIOLENCE", "SUSPICIOUS", "ABNORMAL");
            Object result = videoAnalysisService.detectAnomalies(deviceId, rules);

            log.info("异常行为检测完成");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("异常行为检测失败: deviceId={}", deviceId, e);
            return ResponseDTO.error("异常行为检测失败: " + e.getMessage());
        }
    }

    /**
     * 启动实时分析
     *
     * @param deviceId 设备ID
     * @param analysisTypes 分析类型列表
     * @return 分析任务ID
     */
    @Operation(summary = "启动实时分析")
    @PostMapping("/real-time/start")
    @SaCheckPermission("video:analytics:realtime")
    public ResponseDTO<String> startRealTimeAnalysis(
            @Parameter(description = "设备ID") @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "分析类型") @RequestParam(required = false) List<String> analysisTypes) {
        try {
            log.info("启动实时分析: deviceId={}, analysisTypes={}", deviceId, analysisTypes);

            List<String> types = analysisTypes != null ? analysisTypes :
                    List.of("FACE_DETECTION", "OBJECT_DETECTION", "BEHAVIOR_ANALYSIS");
            String taskId = videoAnalysisService.startRealTimeAnalysis(deviceId, types);

            log.info("实时分析启动成功: taskId={}", taskId);
            return ResponseDTO.ok(taskId, "实时分析已启动");
        } catch (Exception e) {
            log.error("启动实时分析失败: deviceId={}", deviceId, e);
            return ResponseDTO.error("启动实时分析失败: " + e.getMessage());
        }
    }

    /**
     * 停止实时分析
     *
     * @param analysisTaskId 分析任务ID
     * @return 操作结果
     */
    @Operation(summary = "停止实时分析")
    @PostMapping("/real-time/stop")
    @SaCheckPermission("video:analytics:realtime")
    public ResponseDTO<String> stopRealTimeAnalysis(
            @Parameter(description = "分析任务ID") @RequestParam @NotNull(message = "分析任务ID不能为空") String analysisTaskId) {
        try {
            log.info("停止实时分析: taskId={}", analysisTaskId);

            boolean result = videoAnalysisService.stopRealTimeAnalysis(analysisTaskId);

            log.info("实时分析停止成功: taskId={}, result={}", analysisTaskId, result);
            return ResponseDTO.ok("实时分析已停止");
        } catch (Exception e) {
            log.error("停止实时分析失败: taskId={}", analysisTaskId, e);
            return ResponseDTO.error("停止实时分析失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询监控事件
     *
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @param eventLevel 事件级别
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param isHandled 是否已处理
     * @return 分页结果
     */
    @Operation(summary = "分页查询监控事件")
    @GetMapping("/events")
    @SaCheckPermission("video:analytics:events")
    public ResponseDTO<PageResult<MonitorEventEntity>> pageMonitorEvents(
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "事件类型") @RequestParam(required = false) String eventType,
            @Parameter(description = "事件级别") @RequestParam(required = false) String eventLevel,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Long pageSize,
            @Parameter(description = "是否已处理") @RequestParam(required = false) Integer isHandled) {
        try {
            log.info("分页查询监控事件: deviceId={}, eventType={}, eventLevel={}",
                    deviceId, eventType, eventLevel);

            PageParam pageParam = new PageParam();
            pageParam.setPageNum(pageNum);
            pageParam.setPageSize(pageSize);

            PageResult<MonitorEventEntity> result = videoAnalysisService.pageMonitorEvents(
                    pageParam, deviceId, eventType, eventLevel, startTime, endTime, isHandled);

            log.info("分页查询监控事件成功: 总数={}", result.getTotal());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询监控事件失败", e);
            return ResponseDTO.error("分页查询监控事件失败: " + e.getMessage());
        }
    }
}