package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.OfflineRecordUploadForm;
import net.lab1024.sa.access.domain.form.OfflineSyncForm;
import net.lab1024.sa.access.domain.vo.OfflineRecordUploadResultVO;
import net.lab1024.sa.access.domain.vo.OfflineSyncDataVO;
import net.lab1024.sa.access.service.AccessUserPermissionService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;

/**
 * 离线数据同步控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Slf4j
@RestController
@Tag(name = "离线数据同步管理")
@RequestMapping("/api/v1/mobile/access/offline")
public class AccessOfflineSyncController {

    @Resource
    private AccessUserPermissionService accessUserPermissionService;

    @GetMapping("/sync-data")
    @Operation(summary = "获取离线同步数据", description = "下载最新的权限数据用于离线同步")
    public ResponseDTO<OfflineSyncDataVO> getSyncData(OfflineSyncForm form) {
        log.info("[离线同步] 获取同步数据: lastSyncTime={}, dataType={}",
                form.getLastSyncTime(), form.getDataType());

        try {
            // TODO: 从SecurityContext获取当前登录用户ID
            Long userId = 1L;

            OfflineSyncDataVO syncData = accessUserPermissionService.getOfflineSyncData(
                    userId,
                    form.getLastSyncTime(),
                    form.getDataType()
            );

            log.info("[离线同步] 同步数据获取成功: permissions={}",
                    syncData.getPermissions() != null ? syncData.getPermissions().size() : 0);

            return ResponseDTO.ok(syncData);
        } catch (Exception e) {
            log.error("[离线同步] 获取同步数据失败", e);
            return ResponseDTO.error("500", "获取同步数据失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload-records")
    @Operation(summary = "上传离线通行记录", description = "上传离线模式下产生的通行记录")
    public ResponseDTO<OfflineRecordUploadResultVO> uploadOfflineRecords(
            @Valid @RequestBody OfflineRecordUploadForm form) {
        log.info("[离线同步] 上传离线记录: records={}, deviceId={}",
                form.getRecords().size(), form.getDeviceId());

        try {
            // TODO: 从SecurityContext获取当前登录用户ID
            Long userId = 1L;

            OfflineRecordUploadResultVO result = accessUserPermissionService.uploadOfflineRecords(
                    userId,
                    form.getDeviceId(),
                    form.getRecords()
            );

            log.info("[离线同步] 离线记录上传完成: total={}, success={}, failed={}",
                    result.getTotal(), result.getSuccessCount(), result.getFailedCount());

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[离线同步] 上传离线记录失败", e);
            return ResponseDTO.error("500", "上传离线记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/sync-status")
    @Operation(summary = "获取同步状态", description = "获取离线数据同步状态信息")
    public ResponseDTO<SyncStatusVO> getSyncStatus() {
        log.info("[离线同步] 查询同步状态");

        try {
            // TODO: 从SecurityContext获取当前登录用户ID
            Long userId = 1L;

            SyncStatusVO status = accessUserPermissionService.getSyncStatus(userId);

            return ResponseDTO.ok(status);
        } catch (Exception e) {
            log.error("[离线同步] 查询同步状态失败", e);
            return ResponseDTO.error("500", "查询同步状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/sync-now")
    @Operation(summary = "立即同步", description = "立即执行离线数据同步")
    public ResponseDTO<OfflineSyncDataVO> syncNow(@RequestBody OfflineSyncForm form) {
        log.info("[离线同步] 立即同步: lastSyncTime={}, dataType={}",
                form.getLastSyncTime(), form.getDataType());

        try {
            // TODO: 从SecurityContext获取当前登录用户ID
            Long userId = 1L;

            OfflineSyncDataVO syncData = accessUserPermissionService.syncNow(
                    userId,
                    form.getLastSyncTime(),
                    form.getDataType()
            );

            log.info("[离线同步] 立即同步成功");

            return ResponseDTO.ok(syncData);
        } catch (Exception e) {
            log.error("[离线同步] 立即同步失败", e);
            return ResponseDTO.error("500", "立即同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步状态VO
     */
    @lombok.Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "同步状态VO")
    public static class SyncStatusVO {

        @io.swagger.v3.oas.annotations.media.Schema(description = "最后同步时间")
        private LocalDateTime lastSyncTime;

        @io.swagger.v3.oas.annotations.media.Schema(description = "同步状态：idle-空闲，syncing-同步中，error-错误")
        private String syncStatus;

        @io.swagger.v3.oas.annotations.media.Schema(description = "待上传记录数")
        private Integer pendingUploadCount;

        @io.swagger.v3.oas.annotations.media.Schema(description = "本地数据版本")
        private Long localDataVersion;

        @io.swagger.v3.oas.annotations.media.Schema(description = "服务器数据版本")
        private Long serverDataVersion;

        @io.swagger.v3.oas.annotations.media.Schema(description = "是否需要同步")
        private Boolean needSync;
    }
}
