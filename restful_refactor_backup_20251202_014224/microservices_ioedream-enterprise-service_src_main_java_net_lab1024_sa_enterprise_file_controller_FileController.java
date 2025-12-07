package net.lab1024.sa.enterprise.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.enterprise.file.entity.FileEntity;
import net.lab1024.sa.enterprise.file.service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件管理控制器
 *
 * @author 老王
 * @since 2025-11-30
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "文件管理", description = "文件上传下载相关接口")
@Validated
public class FileController {

    private final FileService fileService;

    @Operation(summary = "单文件上传")
    @PostMapping("/upload")
    public ResponseDTO<FileEntity> uploadFile(
            @Parameter(description = "文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务模块") @RequestParam(value = "businessModule", required = false) String businessModule,
            @Parameter(description = "业务ID") @RequestParam(value = "businessId", required = false) String businessId,
            @Parameter(description = "文件描述") @RequestParam(value = "fileDescription", required = false) String fileDescription,
            HttpServletRequest request) {

        Long userId = SmartRequestUtil.getUserId(request);
        log.info("用户{}上传文件: {}", userId, file.getOriginalFilename());

        FileEntity fileEntity = fileService.uploadFile(file, businessModule, businessId, fileDescription);
        return ResponseDTO.ok(fileEntity);
    }

    @Operation(summary = "批量文件上传")
    @PostMapping("/upload/batch")
    public ResponseDTO<List<FileEntity>> uploadFiles(
            @Parameter(description = "文件列表", required = true) @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "业务模块") @RequestParam(value = "businessModule", required = false) String businessModule,
            @Parameter(description = "业务ID") @RequestParam(value = "businessId", required = false) String businessId,
            HttpServletRequest request) {

        Long userId = SmartRequestUtil.getUserId(request);
        log.info("用户{}批量上传{}个文件", userId, files.size());

        List<FileEntity> fileEntities = fileService.uploadFiles(files, businessModule, businessId);
        return ResponseDTO.ok(fileEntities);
    }

    @Operation(summary = "文件下载")
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件ID", required = true) @PathVariable @NotNull Long fileId,
            HttpServletRequest request) {

        Long userId = SmartRequestUtil.getUserId(request);
        log.info("用户{}下载文件: {}", userId, fileId);

        FileEntity fileEntity = fileService.getFileInfo(fileId);
        if (fileEntity == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileContent = fileService.downloadFile(fileId, userId);
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileEntity.getOriginalFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "获取文件信息")
    @GetMapping("/info/{fileId}")
    public ResponseDTO<FileEntity> getFileInfo(
            @Parameter(description = "文件ID", required = true) @PathVariable @NotNull Long fileId) {

        FileEntity fileEntity = fileService.getFileInfo(fileId);
        if (fileEntity == null) {
            return ResponseDTO.error("文件不存在");
        }

        return ResponseDTO.ok(fileEntity);
    }

    @Operation(summary = "根据MD5获取文件信息")
    @GetMapping("/info/md5/{fileMd5}")
    public ResponseDTO<FileEntity> getFileInfoByMd5(
            @Parameter(description = "文件MD5", required = true) @PathVariable @NotNull String fileMd5) {

        FileEntity fileEntity = fileService.getFileInfoByMd5(fileMd5);
        if (fileEntity == null) {
            return ResponseDTO.error("文件不存在");
        }

        return ResponseDTO.ok(fileEntity);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    public ResponseDTO<String> deleteFile(
            @Parameter(description = "文件ID", required = true) @PathVariable @NotNull Long fileId,
            HttpServletRequest request) {

        Long userId = SmartRequestUtil.getUserId(request);
        log.info("用户{}删除文件: {}", userId, fileId);

        boolean result = fileService.deleteFile(fileId, userId);
        if (result) {
            return ResponseDTO.ok("文件删除成功");
        } else {
            return ResponseDTO.error("文件删除失败");
        }
    }

    @Operation(summary = "批量删除文件")
    @DeleteMapping("/batch")
    public ResponseDTO<String> batchDeleteFiles(
            @Parameter(description = "文件ID列表", required = true) @RequestBody @NotNull List<Long> fileIds,
            HttpServletRequest request) {

        Long userId = SmartRequestUtil.getUserId(request);
        log.info("用户{}批量删除{}个文件", userId, fileIds.size());

        boolean result = fileService.batchDeleteFiles(fileIds, userId);
        if (result) {
            return ResponseDTO.ok("批量删除成功");
        } else {
            return ResponseDTO.error("批量删除失败");
        }
    }

    @Operation(summary = "获取文件列表")
    @GetMapping("/list")
    public ResponseDTO<List<FileEntity>> getFileList(
            @Parameter(description = "文件名") @RequestParam(value = "fileName", required = false) String fileName,
            @Parameter(description = "文件类型") @RequestParam(value = "fileType", required = false) String fileType,
            @Parameter(description = "业务模块") @RequestParam(value = "businessModule", required = false) String businessModule,
            @Parameter(description = "上传用户ID") @RequestParam(value = "uploadUserId", required = false) Long uploadUserId,
            @Parameter(description = "页码") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {

        List<FileEntity> fileList = fileService.getFileList(fileName, fileType, businessModule, uploadUserId, pageNum, pageSize);
        return ResponseDTO.ok(fileList);
    }

    @Operation(summary = "获取我的文件列表")
    @GetMapping("/my/list")
    public ResponseDTO<List<FileEntity>> getMyFileList(
            @Parameter(description = "文件名") @RequestParam(value = "fileName", required = false) String fileName,
            @Parameter(description = "文件类型") @RequestParam(value = "fileType", required = false) String fileType,
            @Parameter(description = "页码") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {

        Long userId = SmartRequestUtil.getUserId(request);
        List<FileEntity> fileList = fileService.getMyFileList(userId, fileName, fileType, pageNum, pageSize);
        return ResponseDTO.ok(fileList);
    }

    @Operation(summary = "根据业务模块获取文件列表")
    @GetMapping("/business/list")
    public ResponseDTO<List<FileEntity>> getBusinessFileList(
            @Parameter(description = "业务模块", required = true) @RequestParam @NotNull String businessModule,
            @Parameter(description = "业务ID") @RequestParam(value = "businessId", required = false) String businessId) {

        List<FileEntity> fileList = fileService.getBusinessFileList(businessModule, businessId);
        return ResponseDTO.ok(fileList);
    }

    @Operation(summary = "更新文件信息")
    @PutMapping("/info")
    public ResponseDTO<String> updateFileInfo(
            @Parameter(description = "文件信息", required = true) @RequestBody @Validated FileEntity fileEntity) {

        boolean result = fileService.updateFileInfo(fileEntity);
        if (result) {
            return ResponseDTO.ok("文件信息更新成功");
        } else {
            return ResponseDTO.error("文件信息更新失败");
        }
    }

    @Operation(summary = "生成文件访问URL")
    @GetMapping("/url/{fileId}")
    public ResponseDTO<String> generateFileUrl(
            @Parameter(description = "文件ID", required = true) @PathVariable @NotNull Long fileId,
            @Parameter(description = "过期小时数") @RequestParam(value = "expireHours", defaultValue = "24") Integer expireHours,
            HttpServletRequest request) {

        Long userId = SmartRequestUtil.getUserId(request);
        String fileUrl = fileService.generateFileUrl(fileId, userId, expireHours);
        return ResponseDTO.ok(fileUrl);
    }

    @Operation(summary = "生成缩略图")
    @PostMapping("/thumbnail/{fileId}")
    public ResponseDTO<String> generateThumbnail(
            @Parameter(description = "文件ID", required = true) @PathVariable @NotNull Long fileId) {

        String thumbnailUrl = fileService.generateThumbnail(fileId);
        return ResponseDTO.ok(thumbnailUrl);
    }

    @Operation(summary = "清理过期文件")
    @DeleteMapping("/clean/expired")
    public ResponseDTO<String> cleanExpiredFiles() {
        int count = fileService.cleanExpiredFiles();
        return ResponseDTO.ok("清理完成，共清理" + count + "个过期文件");
    }

    @Operation(summary = "获取文件统计信息")
    @GetMapping("/statistics")
    public ResponseDTO<Object> getFileStatistics(
            @Parameter(description = "用户ID") @RequestParam(value = "userId", required = false) Long userId,
            @Parameter(description = "开始时间") @RequestParam(value = "startTime", required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(value = "endTime", required = false) LocalDateTime endTime) {

        Object statistics = fileService.getFileStatistics(userId, startTime, endTime);
        return ResponseDTO.ok(statistics);
    }

}