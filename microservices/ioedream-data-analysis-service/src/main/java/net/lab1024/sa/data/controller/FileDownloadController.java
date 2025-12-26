package net.lab1024.sa.data.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.data.dao.ExportTaskDao;
import net.lab1024.sa.data.domain.entity.ExportTaskEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件下载控制器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "文件下载管理", description = "文件下载、导出文件访问")
public class FileDownloadController {

    @Resource
    private ExportTaskDao exportTaskDao;

    @Value("${export.file.path:/tmp/exports}")
    private String exportFilePath;

    @Operation(summary = "下载导出文件")
    @GetMapping("/download/{taskId}")
    public void downloadExportFile(
            @Parameter(description = "导出任务ID") @PathVariable String taskId,
            HttpServletResponse response) throws IOException {

        log.info("[文件下载] 下载文件请求: taskId={}", taskId);

        // 查询导出任务
        ExportTaskEntity taskEntity = exportTaskDao.selectById(taskId);
        if (taskEntity == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("导出任务不存在");
            return;
        }

        // 检查任务状态
        if (!"completed".equals(taskEntity.getStatus())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("文件尚未生成完成，状态: " + taskEntity.getStatus());
            return;
        }

        // 检查文件是否存在
        String fileName = taskEntity.getFileName();
        if (fileName == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("文件不存在");
            return;
        }

        String filePath = exportFilePath + File.separator + fileName;
        File file = new File(filePath);

        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("文件不存在");
            log.warn("[文件下载] 文件不存在: filePath={}", filePath);
            return;
        }

        // 设置响应头
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLengthLong(file.length());

        // URL编码文件名（支持中文）
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + encodedFileName);

        // 写入文件流
        try (OutputStream outputStream = response.getOutputStream()) {
            Files.copy(file.toPath(), outputStream);
            outputStream.flush();
        }

        log.info("[文件下载] 文件下载成功: taskId={}, fileName={}, size={}KB",
                 taskId, fileName, file.length() / 1024);
    }

    @Operation(summary = "获取导出文件信息")
    @GetMapping("/info/{taskId}")
    public ResponseDTO<ExportFileInfo> getExportFileInfo(
            @Parameter(description = "导出任务ID") @PathVariable String taskId) {

        log.info("[文件下载] 查询文件信息: taskId={}", taskId);

        ExportTaskEntity taskEntity = exportTaskDao.selectById(taskId);
        if (taskEntity == null) {
            return ResponseDTO.error("FILE_NOT_FOUND", "导出任务不存在");
        }

        ExportFileInfo fileInfo = new ExportFileInfo();
        fileInfo.setTaskId(taskId);
        fileInfo.setFileName(taskEntity.getFileName());
        fileInfo.setFileSize(taskEntity.getFileSize());
        fileInfo.setStatus(taskEntity.getStatus());
        fileInfo.setCreateTime(taskEntity.getCreateTime());
        fileInfo.setCompleteTime(taskEntity.getCompleteTime());

        if (taskEntity.getFileUrl() != null) {
            fileInfo.setDownloadUrl("/api/v1/files/download/" + taskId);
        }

        return ResponseDTO.ok(fileInfo);
    }

    @Operation(summary = "预览文件（通过Spring Resource）")
    @GetMapping("/preview/{taskId}")
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "导出任务ID") @PathVariable String taskId) {

        log.info("[文件下载] 预览文件请求: taskId={}", taskId);

        // 查询导出任务
        ExportTaskEntity taskEntity = exportTaskDao.selectById(taskId);
        if (taskEntity == null) {
            return ResponseEntity.notFound().build();
        }

        // 检查任务状态
        if (!"completed".equals(taskEntity.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        // 检查文件是否存在
        String fileName = taskEntity.getFileName();
        String filePath = exportFilePath + File.separator + fileName;
        File file = new File(filePath);

        if (!file.exists()) {
            log.warn("[文件下载] 文件不存在: filePath={}", filePath);
            return ResponseEntity.notFound().build();
        }

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        // URL编码文件名
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        headers.setContentDispositionFormData("attachment", encodedFileName);

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(resource);
    }

    /**
     * 导出文件信息VO
     */
    public static class ExportFileInfo {
        private String taskId;
        private String fileName;
        private Long fileSize;
        private String status;
        private java.time.LocalDateTime createTime;
        private java.time.LocalDateTime completeTime;
        private String downloadUrl;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public java.time.LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(java.time.LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public java.time.LocalDateTime getCompleteTime() {
            return completeTime;
        }

        public void setCompleteTime(java.time.LocalDateTime completeTime) {
            this.completeTime = completeTime;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }
    }
}
