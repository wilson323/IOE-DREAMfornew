package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.DeviceImportQueryForm;
import net.lab1024.sa.access.domain.vo.DeviceImportBatchVO;
import net.lab1024.sa.access.domain.vo.DeviceImportErrorVO;
import net.lab1024.sa.access.domain.vo.DeviceImportResultVO;
import net.lab1024.sa.access.domain.vo.DeviceImportStatisticsVO;
import net.lab1024.sa.access.service.DeviceImportService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 设备批量导入Controller
 * <p>
 * 提供设备批量导入的REST API：
 * - 上传Excel文件
 * - 执行导入
 * - 查询导入记录
 * - 下载模板
 * - 导出错误记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/device-import")
@Tag(name = "设备批量导入")
public class DeviceImportController {

    @Resource
    private DeviceImportService deviceImportService;

    /**
     * 上传Excel文件并解析
     */
    @Operation(summary = "上传Excel文件并解析")
    @PostMapping("/upload")
    @PermissionCheck
    public ResponseDTO<Long> upload(
            @Parameter(description = "Excel文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "批次名称", required = true)
            @RequestParam("batchName") String batchName
    ) {
        log.info("[设备导入] 上传Excel文件: fileName={}, batchName={}", file.getOriginalFilename(), batchName);

        Long operatorId = SmartRequestUtil.getRequestUserId();
        String operatorName = SmartRequestUtil.getRequestUserName();

        Long batchId = deviceImportService.uploadAndParse(file, batchName, operatorId, operatorName);

        return ResponseDTO.ok(batchId);
    }

    /**
     * 执行导入操作
     */
    @Operation(summary = "执行导入操作")
    @PostMapping("/{batchId}/execute")
    @PermissionCheck
    public ResponseDTO<DeviceImportResultVO> executeImport(
            @Parameter(description = "批次ID", required = true)
            @PathVariable Long batchId
    ) {
        log.info("[设备导入] 执行导入: batchId={}", batchId);

        DeviceImportResultVO result = deviceImportService.executeImport(batchId);

        return ResponseDTO.ok(result);
    }

    /**
     * 分页查询导入批次
     */
    @Operation(summary = "分页查询导入批次")
    @GetMapping("/page")
    @PermissionCheck
    public ResponseDTO<PageResult<DeviceImportBatchVO>> queryPage(DeviceImportQueryForm queryForm) {
        log.debug("[设备导入] 分页查询导入批次: queryForm={}", queryForm);

        PageResult<DeviceImportBatchVO> pageResult = deviceImportService.queryImportBatches(queryForm);

        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询导入批次详情
     */
    @Operation(summary = "查询导入批次详情")
    @GetMapping("/{batchId}")
    @PermissionCheck
    public ResponseDTO<DeviceImportBatchVO> getDetail(
            @Parameter(description = "批次ID", required = true)
            @PathVariable Long batchId
    ) {
        log.debug("[设备导入] 查询批次详情: batchId={}", batchId);

        DeviceImportBatchVO batchVO = deviceImportService.getImportBatchDetail(batchId);

        if (batchVO == null) {
            return ResponseDTO.error("BATCH_NOT_FOUND", "导入批次不存在");
        }

        return ResponseDTO.ok(batchVO);
    }

    /**
     * 查询批次的错误列表
     */
    @Operation(summary = "查询批次的错误列表")
    @GetMapping("/{batchId}/errors")
    @PermissionCheck
    public ResponseDTO<List<DeviceImportErrorVO>> queryErrors(
            @Parameter(description = "批次ID", required = true)
            @PathVariable Long batchId
    ) {
        log.debug("[设备导入] 查询批次错误列表: batchId={}", batchId);

        List<DeviceImportErrorVO> errorList = deviceImportService.queryBatchErrors(batchId);

        return ResponseDTO.ok(errorList);
    }

    /**
     * 获取导入统计信息
     */
    @Operation(summary = "获取导入统计信息")
    @GetMapping("/statistics")
    @PermissionCheck
    public ResponseDTO<DeviceImportStatisticsVO> getStatistics() {
        log.debug("[设备导入] 查询导入统计信息");

        DeviceImportStatisticsVO statistics = deviceImportService.getImportStatistics();

        return ResponseDTO.ok(statistics);
    }

    /**
     * 删除导入批次
     */
    @Operation(summary = "删除导入批次")
    @DeleteMapping("/{batchId}")
    @PermissionCheck
    public ResponseDTO<Void> deleteBatch(
            @Parameter(description = "批次ID", required = true)
            @PathVariable Long batchId
    ) {
        log.info("[设备导入] 删除导入批次: batchId={}", batchId);

        Integer deleted = deviceImportService.deleteImportBatch(batchId);

        if (deleted <= 0) {
            return ResponseDTO.error("DELETE_FAILED", "删除失败");
        }

        return ResponseDTO.ok();
    }

    /**
     * 下载导入模板
     */
    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    @PermissionCheck
    public ResponseDTO<byte[]> downloadTemplate() {
        log.info("[设备导入] 下载导入模板");

        byte[] template = deviceImportService.downloadTemplate();

        return ResponseDTO.ok(template);
    }

    /**
     * 导出错误记录
     */
    @Operation(summary = "导出错误记录")
    @GetMapping("/{batchId}/export-errors")
    @PermissionCheck
    public ResponseDTO<byte[]> exportErrors(
            @Parameter(description = "批次ID", required = true)
            @PathVariable Long batchId
    ) {
        log.info("[设备导入] 导出错误记录: batchId={}", batchId);

        byte[] excelBytes = deviceImportService.exportErrors(batchId);

        return ResponseDTO.ok(excelBytes);
    }
}
