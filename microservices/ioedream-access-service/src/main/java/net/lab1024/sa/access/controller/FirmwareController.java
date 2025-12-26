package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.FirmwareQueryForm;
import net.lab1024.sa.access.domain.form.FirmwareUploadForm;
import net.lab1024.sa.access.domain.vo.FirmwareDetailVO;
import net.lab1024.sa.access.domain.vo.FirmwareVO;
import net.lab1024.sa.access.service.FirmwareService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 固件管理Controller
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@Tag(name = "固件管理")
@RequestMapping("/api/v1/access/firmware")
public class FirmwareController {

    @Resource
    private FirmwareService firmwareService;

    // ==================== 固件上传 ====================

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传固件")
    public ResponseDTO<Long> uploadFirmware(
            @RequestParam("file") MultipartFile file,
            @RequestParam("firmwareName") String firmwareName,
            @RequestParam("firmwareVersion") String firmwareVersion,
            @RequestParam("deviceType") Integer deviceType,
            @RequestParam(value = "deviceModel", required = false) String deviceModel,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "minVersion", required = false) String minVersion,
            @RequestParam(value = "maxVersion", required = false) String maxVersion,
            @RequestParam(value = "isForce", defaultValue = "0") Integer isForce,
            @RequestParam(value = "releaseNotes", required = false) String releaseNotes,
            @RequestParam(value = "operatorId", required = false, defaultValue = "1") Long operatorId,
            @RequestParam(value = "operatorName", required = false, defaultValue = "系统") String operatorName
    ) {
        log.info("[固件管理] 上传固件: firmwareName={}, firmwareVersion={}, deviceType={}, operatorId={}",
                firmwareName, firmwareVersion, deviceType, operatorId);

        FirmwareUploadForm uploadForm = new FirmwareUploadForm();
        uploadForm.setFirmwareName(firmwareName);
        uploadForm.setFirmwareVersion(firmwareVersion);
        uploadForm.setDeviceType(deviceType);
        uploadForm.setDeviceModel(deviceModel);
        uploadForm.setBrand(brand);
        uploadForm.setMinVersion(minVersion);
        uploadForm.setMaxVersion(maxVersion);
        uploadForm.setIsForce(isForce);
        uploadForm.setReleaseNotes(releaseNotes);

        Long firmwareId = firmwareService.uploadFirmware(file, uploadForm, operatorId, operatorName);
        return ResponseDTO.ok(firmwareId);
    }

    // ==================== 固件查询 ====================

    @PostMapping("/page")
    @Operation(summary = "分页查询固件列表")
    public ResponseDTO<PageResult<FirmwareVO>> queryFirmwarePage(@Valid @RequestBody FirmwareQueryForm queryForm) {
        log.info("[固件管理] 分页查询固件列表: queryForm={}", queryForm);

        PageResult<FirmwareVO> pageResult = firmwareService.queryFirmwarePage(queryForm);
        return ResponseDTO.ok(pageResult);
    }

    @GetMapping("/{firmwareId}")
    @Operation(summary = "获取固件详情")
    public ResponseDTO<FirmwareDetailVO> getFirmwareDetail(@PathVariable Long firmwareId) {
        log.info("[固件管理] 获取固件详情: firmwareId={}", firmwareId);

        FirmwareDetailVO firmwareDetail = firmwareService.getFirmwareDetail(firmwareId);
        return ResponseDTO.ok(firmwareDetail);
    }

    @GetMapping("/available")
    @Operation(summary = "查询可用固件列表")
    public ResponseDTO<List<FirmwareVO>> queryAvailableFirmware(
            @RequestParam(value = "deviceType", required = false) Integer deviceType,
            @RequestParam(value = "deviceModel", required = false) String deviceModel,
            @RequestParam(value = "currentVersion", required = false) String currentVersion
    ) {
        log.info("[固件管理] 查询可用固件: deviceType={}, deviceModel={}, currentVersion={}",
                deviceType, deviceModel, currentVersion);

        List<FirmwareVO> firmwareList = firmwareService.queryAvailableFirmware(deviceType, deviceModel, currentVersion);
        return ResponseDTO.ok(firmwareList);
    }

    // ==================== 固件下载 ====================

    @GetMapping("/{firmwareId}/download")
    @Operation(summary = "下载固件")
    public ResponseDTO<FirmwareDetailVO> downloadFirmware(@PathVariable Long firmwareId) {
        log.info("[固件管理] 下载固件: firmwareId={}", firmwareId);

        FirmwareDetailVO firmwareDetail = firmwareService.downloadFirmware(firmwareId);
        return ResponseDTO.ok(firmwareDetail);
    }

    // ==================== 固件管理 ====================

    @PutMapping("/status")
    @Operation(summary = "更新固件状态")
    public ResponseDTO<Void> updateFirmwareStatus(
            @RequestParam("firmwareId") Long firmwareId,
            @RequestParam("firmwareStatus") Integer firmwareStatus
    ) {
        log.info("[固件管理] 更新固件状态: firmwareId={}, firmwareStatus={}", firmwareId, firmwareStatus);

        firmwareService.updateFirmwareStatus(firmwareId, firmwareStatus);
        return ResponseDTO.ok();
    }

    @PutMapping("/enabled")
    @Operation(summary = "更新固件启用状态")
    public ResponseDTO<Void> updateFirmwareEnabled(
            @RequestParam("firmwareId") Long firmwareId,
            @RequestParam("isEnabled") Integer isEnabled
    ) {
        log.info("[固件管理] 更新固件启用状态: firmwareId={}, isEnabled={}", firmwareId, isEnabled);

        firmwareService.updateFirmwareEnabled(firmwareId, isEnabled);
        return ResponseDTO.ok();
    }

    @DeleteMapping("/{firmwareId}")
    @Operation(summary = "删除固件")
    public ResponseDTO<Void> deleteFirmware(@PathVariable Long firmwareId) {
        log.info("[固件管理] 删除固件: firmwareId={}", firmwareId);

        firmwareService.deleteFirmware(firmwareId);
        return ResponseDTO.ok();
    }

    // ==================== MD5校验 ====================

    @GetMapping("/{firmwareId}/md5")
    @Operation(summary = "获取固件MD5")
    public ResponseDTO<String> getFirmwareMd5(@PathVariable Long firmwareId) {
        log.info("[固件管理] 获取固件MD5: firmwareId={}", firmwareId);

        String md5 = firmwareService.getFirmwareMd5(firmwareId);
        return ResponseDTO.ok(md5);
    }
}
