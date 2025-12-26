package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanAddForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanQueryForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingPlanVO;
import net.lab1024.sa.video.service.VideoRecordingPlanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频录像计划管理Controller
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/api/v1/video/recording/plan")
@Tag(name = "视频录像计划管理")
@Slf4j
public class VideoRecordingPlanController {

    @Resource
    private VideoRecordingPlanService videoRecordingPlanService;

    @Operation(summary = "创建录像计划")
    @PostMapping("/create")
    public ResponseDTO<Long> createPlan(@Valid @RequestBody VideoRecordingPlanAddForm addForm) {
        log.info("[录像计划] 创建录像计划: planName={}, deviceId={}", addForm.getPlanName(), addForm.getDeviceId());
        Long planId = videoRecordingPlanService.createPlan(addForm);
        return ResponseDTO.ok(planId);
    }

    @Operation(summary = "更新录像计划")
    @PutMapping("/update")
    public ResponseDTO<Integer> updatePlan(@Valid @RequestBody VideoRecordingPlanUpdateForm updateForm) {
        log.info("[录像计划] 更新录像计划: planId={}", updateForm.getPlanId());
        Integer rows = videoRecordingPlanService.updatePlan(updateForm);
        return ResponseDTO.ok(rows);
    }

    @Operation(summary = "删除录像计划")
    @DeleteMapping("/delete/{planId}")
    public ResponseDTO<Integer> deletePlan(@PathVariable Long planId) {
        log.info("[录像计划] 删除录像计划: planId={}", planId);
        Integer rows = videoRecordingPlanService.deletePlan(planId);
        return ResponseDTO.ok(rows);
    }

    @Operation(summary = "启用/禁用录像计划")
    @PutMapping("/enable/{planId}")
    public ResponseDTO<Integer> enablePlan(@PathVariable Long planId,
                                           @RequestParam Boolean enabled) {
        log.info("[录像计划] 启用/禁用录像计划: planId={}, enabled={}", planId, enabled);
        Integer rows = videoRecordingPlanService.enablePlan(planId, enabled);
        return ResponseDTO.ok(rows);
    }

    @Operation(summary = "获取录像计划详情")
    @GetMapping("/detail/{planId}")
    public ResponseDTO<VideoRecordingPlanVO> getPlan(@PathVariable Long planId) {
        log.info("[录像计划] 获取录像计划详情: planId={}", planId);
        VideoRecordingPlanVO plan = videoRecordingPlanService.getPlan(planId);
        return ResponseDTO.ok(plan);
    }

    @Operation(summary = "分页查询录像计划")
    @PostMapping("/query")
    public ResponseDTO<PageResult<VideoRecordingPlanVO>> queryPlans(@Valid @RequestBody VideoRecordingPlanQueryForm queryForm) {
        log.info("[录像计划] 分页查询录像计划: pageNum={}, pageSize={}",
                queryForm.getPageNum(), queryForm.getPageSize());
        PageResult<VideoRecordingPlanVO> pageResult = videoRecordingPlanService.queryPlans(queryForm);
        return ResponseDTO.ok(pageResult);
    }

    @Operation(summary = "获取设备的启用录像计划")
    @GetMapping("/device/{deviceId}/enabled")
    public ResponseDTO<List<VideoRecordingPlanVO>> getEnabledPlansByDevice(@PathVariable String deviceId) {
        log.info("[录像计划] 获取设备的启用录像计划: deviceId={}", deviceId);
        List<VideoRecordingPlanVO> plans = videoRecordingPlanService.getEnabledPlansByDevice(deviceId);
        return ResponseDTO.ok(plans);
    }

    @Operation(summary = "检查设备是否有启用计划")
    @GetMapping("/device/{deviceId}/has-enabled")
    public ResponseDTO<Boolean> hasEnabledPlan(@PathVariable String deviceId) {
        log.info("[录像计划] 检查设备是否有启用计划: deviceId={}", deviceId);
        Boolean hasEnabled = videoRecordingPlanService.hasEnabledPlan(deviceId);
        return ResponseDTO.ok(hasEnabled);
    }

    @Operation(summary = "复制录像计划")
    @PostMapping("/copy/{planId}")
    public ResponseDTO<Long> copyPlan(@PathVariable Long planId,
                                     @RequestParam String newPlanName) {
        log.info("[录像计划] 复制录像计划: planId={}, newPlanName={}", planId, newPlanName);
        Long newPlanId = videoRecordingPlanService.copyPlan(planId, newPlanName);
        return ResponseDTO.ok(newPlanId);
    }

    @Operation(summary = "批量启用/禁用录像计划")
    @PutMapping("/batch-enable")
    public ResponseDTO<Integer> batchEnablePlans(@RequestParam List<Long> planIds,
                                                 @RequestParam Boolean enabled) {
        log.info("[录像计划] 批量启用/禁用录像计划: planIds={}, enabled={}", planIds, enabled);
        Integer rows = videoRecordingPlanService.batchEnablePlans(planIds, enabled);
        return ResponseDTO.ok(rows);
    }

    @Operation(summary = "批量删除录像计划")
    @DeleteMapping("/batch-delete")
    public ResponseDTO<Integer> batchDeletePlans(@RequestParam List<Long> planIds) {
        log.info("[录像计划] 批量删除录像计划: planIds={}", planIds);
        Integer rows = videoRecordingPlanService.batchDeletePlans(planIds);
        return ResponseDTO.ok(rows);
    }
}
