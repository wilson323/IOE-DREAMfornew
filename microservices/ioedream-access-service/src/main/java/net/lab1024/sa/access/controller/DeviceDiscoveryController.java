package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.DeviceDiscoveryRequestForm;
import net.lab1024.sa.access.domain.vo.DeviceDiscoveryResultVO;
import net.lab1024.sa.access.domain.vo.DiscoveredDeviceVO;
import net.lab1024.sa.access.service.DeviceDiscoveryService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 门禁设备自动发现控制器
 * <p>
 * 功能说明：
 * - 启动设备自动发现
 * - 查询发现进度
 * - 停止发现任务
 * - 批量添加发现的设备
 * - 导出发现结果
 * - SSE实时推送进度
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/device/discovery")
@Tag(name = "门禁设备自动发现", description = "门禁设备自动发现接口")
public class DeviceDiscoveryController {

    @Resource
    private DeviceDiscoveryService deviceDiscoveryService;

    /**
     * 启动设备自动发现
     */
    @PostMapping("/start")
    @Operation(summary = "启动设备自动发现")
    public ResponseDTO<DeviceDiscoveryResultVO> startDiscovery(
            @Valid @RequestBody DeviceDiscoveryRequestForm requestForm) {
        log.info("[设备发现] 启动扫描: subnet={}", requestForm.getSubnet());

        try {
            return deviceDiscoveryService.discoverDevices(requestForm);
        } catch (Exception e) {
            log.error("[设备发现] 启动扫描异常: subnet={}, error={}",
                    requestForm.getSubnet(), e.getMessage(), e);
            return ResponseDTO.error("START_DISCOVERY_ERROR", "启动扫描失败: " + e.getMessage());
        }
    }

    /**
     * 停止设备自动发现
     */
    @PostMapping("/{scanId}/stop")
    @Operation(summary = "停止设备自动发现")
    public ResponseDTO<Void> stopDiscovery(@PathVariable String scanId) {
        log.info("[设备发现] 停止扫描: scanId={}", scanId);
        return deviceDiscoveryService.stopDiscovery(scanId);
    }

    /**
     * 查询发现进度
     */
    @GetMapping("/{scanId}/progress")
    @Operation(summary = "查询发现进度")
    public ResponseDTO<DeviceDiscoveryResultVO> getProgress(@PathVariable String scanId) {
        log.info("[设备发现] 查询进度: scanId={}", scanId);
        return deviceDiscoveryService.getDiscoveryProgress(scanId);
    }

    /**
     * 订阅发现进度（SSE实时推送）
     */
    @GetMapping("/{scanId}/subscribe")
    @Operation(summary = "订阅发现进度（SSE）")
    public SseEmitter subscribeProgress(@PathVariable String scanId) {
        log.info("[设备发现] 订阅进度: scanId={}", scanId);

        // 创建SSE Emitter（超时时间5分钟）
        SseEmitter emitter = new SseEmitter(300000L);

        try {
            // 发送连接成功事件
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("订阅成功"));

            // 启动异步线程推送进度
            startProgressPushTask(scanId, emitter);

        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 启动进度推送任务
     */
    private void startProgressPushTask(String scanId, SseEmitter emitter) {
        new Thread(() -> {
            try {
                int lastProgress = -1;
                int checkCount = 0;
                final int MAX_CHECKS = 150; // 最多检查150次（5分钟）

                while (checkCount < MAX_CHECKS) {
                    ResponseDTO<DeviceDiscoveryResultVO> response = deviceDiscoveryService.getDiscoveryProgress(scanId);

                    if (response != null && response.getData() != null) {
                        DeviceDiscoveryResultVO result = response.getData();
                        Integer currentProgress = result.getProgress();

                        if (currentProgress != null && !currentProgress.equals(lastProgress)) {
                            emitter.send(SseEmitter.event()
                                    .name("progress")
                                    .data(result));
                            lastProgress = currentProgress;
                        }

                        if ("COMPLETED".equals(result.getStatus()) ||
                            "FAILED".equals(result.getStatus())) {
                            emitter.send(SseEmitter.event()
                                    .name("completed")
                                    .data(result));
                            break;
                        }
                    }

                    Thread.sleep(2000);
                    checkCount++;
                }

                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
    }

    /**
     * 批量添加发现的设备
     */
    @PostMapping("/batch-add")
    @Operation(summary = "批量添加发现的设备")
    public ResponseDTO<DeviceDiscoveryResultVO> batchAddDevices(@RequestBody List<DiscoveredDeviceVO> devices) {
        log.info("[设备发现] 批量添加设备: count={}", devices.size());
        return deviceDiscoveryService.batchAddDevices(devices);
    }

    /**
     * 导出发现结果
     */
    @GetMapping("/{scanId}/export")
    @Operation(summary = "导出发现结果")
    public ResponseDTO<Map<String, Object>> exportResult(@PathVariable String scanId) {
        log.info("[设备发现] 导出结果: scanId={}", scanId);
        return deviceDiscoveryService.exportDiscoveryResult(scanId);
    }
}
