package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskForm;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskQueryForm;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeDeviceVO;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeTaskVO;
import net.lab1024.sa.access.service.FirmwareUpgradeService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 固件升级任务管理Controller
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@Tag(name = "固件升级任务管理")
@RequestMapping("/api/v1/access/firmware-upgrade/task")
public class FirmwareUpgradeTaskController {

    @Resource
    private FirmwareUpgradeService firmwareUpgradeService;

    // ==================== 任务管理 ====================

    @PostMapping("/create")
    @Operation(summary = "创建固件升级任务")
    public ResponseDTO<Long> createUpgradeTask(
            @Valid @RequestBody FirmwareUpgradeTaskForm taskForm,
            @Parameter(description = "操作人ID", hidden = true) @RequestParam(required = false) Long operatorId,
            @Parameter(description = "操作人姓名", hidden = true) @RequestParam(required = false, defaultValue = "系统") String operatorName
    ) {
        log.info("[固件升级] 创建升级任务: taskForm={}, operatorId={}", taskForm, operatorId);

        // TODO: 从登录上下文获取操作人信息
        if (operatorId == null) {
            operatorId = 1L; // 临时使用系统用户
        }

        Long taskId = firmwareUpgradeService.createUpgradeTask(taskForm, operatorId, operatorName);
        return ResponseDTO.ok(taskId);
    }

    @PostMapping("/{taskId}/start")
    @Operation(summary = "启动升级任务")
    public ResponseDTO<Void> startUpgradeTask(@PathVariable Long taskId) {
        log.info("[固件升级] 启动升级任务: taskId={}", taskId);

        Boolean result = firmwareUpgradeService.startUpgradeTask(taskId);
        return ResponseDTO.ok();
    }

    @PostMapping("/{taskId}/pause")
    @Operation(summary = "暂停升级任务")
    public ResponseDTO<Void> pauseUpgradeTask(@PathVariable Long taskId) {
        log.info("[固件升级] 暂停升级任务: taskId={}", taskId);

        Boolean result = firmwareUpgradeService.pauseUpgradeTask(taskId);
        return ResponseDTO.ok();
    }

    @PostMapping("/{taskId}/resume")
    @Operation(summary = "恢复升级任务")
    public ResponseDTO<Void> resumeUpgradeTask(@PathVariable Long taskId) {
        log.info("[固件升级] 恢复升级任务: taskId={}", taskId);

        Boolean result = firmwareUpgradeService.resumeUpgradeTask(taskId);
        return ResponseDTO.ok();
    }

    @PostMapping("/{taskId}/stop")
    @Operation(summary = "停止升级任务")
    public ResponseDTO<Void> stopUpgradeTask(@PathVariable Long taskId) {
        log.info("[固件升级] 停止升级任务: taskId={}", taskId);

        Boolean result = firmwareUpgradeService.stopUpgradeTask(taskId);
        return ResponseDTO.ok();
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除升级任务")
    public ResponseDTO<Void> deleteUpgradeTask(@PathVariable Long taskId) {
        log.info("[固件升级] 删除升级任务: taskId={}", taskId);

        Boolean result = firmwareUpgradeService.deleteUpgradeTask(taskId);
        return ResponseDTO.ok();
    }

    // ==================== 任务查询 ====================

    @PostMapping("/page")
    @Operation(summary = "分页查询升级任务列表")
    public ResponseDTO<PageResult<FirmwareUpgradeTaskVO>> queryTasksPage(@Valid @RequestBody FirmwareUpgradeTaskQueryForm queryForm) {
        log.info("[固件升级] 分页查询升级任务列表: queryForm={}", queryForm);

        PageResult<FirmwareUpgradeTaskVO> pageResult = firmwareUpgradeService.queryTasksPage(queryForm);
        return ResponseDTO.ok(pageResult);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "获取升级任务详情")
    public ResponseDTO<FirmwareUpgradeTaskVO> getTaskDetail(@PathVariable Long taskId) {
        log.info("[固件升级] 获取升级任务详情: taskId={}", taskId);

        FirmwareUpgradeTaskVO taskDetail = firmwareUpgradeService.getTaskDetail(taskId);
        return ResponseDTO.ok(taskDetail);
    }

    @GetMapping("/{taskId}/devices")
    @Operation(summary = "获取任务设备列表")
    public ResponseDTO<List<FirmwareUpgradeDeviceVO>> getTaskDevices(@PathVariable Long taskId) {
        log.info("[固件升级] 获取任务设备列表: taskId={}", taskId);

        List<FirmwareUpgradeDeviceVO> deviceList = firmwareUpgradeService.getTaskDevices(taskId);
        return ResponseDTO.ok(deviceList);
    }

    @GetMapping("/{taskId}/progress")
    @Operation(summary = "获取任务进度统计")
    public ResponseDTO<Map<String, Object>> getTaskProgress(@PathVariable Long taskId) {
        log.info("[固件升级] 获取任务进度统计: taskId={}", taskId);

        Map<String, Object> progress = firmwareUpgradeService.getTaskProgress(taskId);
        return ResponseDTO.ok(progress);
    }

    // ==================== 设备升级管理 ====================

    @PostMapping("/{taskId}/retry")
    @Operation(summary = "重试失败的设备")
    public ResponseDTO<Integer> retryFailedDevices(@PathVariable Long taskId) {
        log.info("[固件升级] 重试失败的设备: taskId={}", taskId);

        Integer retryCount = firmwareUpgradeService.retryFailedDevices(taskId);
        return ResponseDTO.ok(retryCount);
    }

    // ==================== 回滚管理 ====================

    @PostMapping("/{taskId}/rollback")
    @Operation(summary = "回滚升级任务")
    public ResponseDTO<Long> rollbackUpgradeTask(@PathVariable Long taskId) {
        log.info("[固件升级] 回滚升级任务: taskId={}", taskId);

        Long rollbackTaskId = firmwareUpgradeService.rollbackUpgradeTask(taskId);
        return ResponseDTO.ok(rollbackTaskId);
    }

    @GetMapping("/{taskId}/rollback-supported")
    @Operation(summary = "检查任务是否支持回滚")
    public ResponseDTO<Boolean> isRollbackSupported(@PathVariable Long taskId) {
        log.info("[固件升级] 检查任务是否支持回滚: taskId={}", taskId);

        Boolean supported = firmwareUpgradeService.isRollbackSupported(taskId);
        return ResponseDTO.ok(supported);
    }
}
