package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.service.VideoStorageMonitorService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/video/storage/monitor")
@Tag(name = "视频存储监控")
@Slf4j
public class VideoStorageMonitorController {

    @Resource
    private VideoStorageMonitorService videoStorageMonitorService;

    @GetMapping("/status")
    @Operation(summary = "获取存储状态", description = "获取存储空间使用情况和告警级别")
    public ResponseDTO<Map<String, Object>> getStorageStatus() {

        log.info("[存储监控] 查询存储状态");

        Map<String, Object> status = videoStorageMonitorService.getStorageStatus();

        return ResponseDTO.ok(status);
    }

    @PostMapping("/check")
    @Operation(summary = "手动检查存储空间", description = "立即执行存储空间检查")
    public ResponseDTO<Void> checkStorageSpace() {

        log.info("[存储监控] 手动触发存储空间检查");

        videoStorageMonitorService.checkStorageSpace();

        return ResponseDTO.ok();
    }

    @GetMapping("/config")
    @Operation(summary = "获取监控配置", description = "获取告警阈值和配置信息")
    public ResponseDTO<Map<String, Object>> getMonitorConfig() {

        log.info("[存储监控] 查询监控配置");

        Map<String, Object> config = Map.of(
            "alertEnabled", videoStorageMonitorService.isAlertEnabled(),
            "storageThreshold", videoStorageMonitorService.getStorageThreshold()
        );

        return ResponseDTO.ok(config);
    }
}
