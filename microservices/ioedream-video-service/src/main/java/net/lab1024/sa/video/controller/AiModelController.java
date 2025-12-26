package net.lab1024.sa.video.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.video.AiModelEntity;
import net.lab1024.sa.video.domain.form.AiModelQueryForm;
import net.lab1024.sa.video.domain.form.AiModelSyncForm;
import net.lab1024.sa.video.domain.form.AiModelUploadForm;
import net.lab1024.sa.video.domain.vo.AiModelVO;
import net.lab1024.sa.video.domain.vo.DeviceModelSyncProgressVO;
import net.lab1024.sa.video.service.AiModelService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * AI模型管理控制器
 * <p>
 * 提供AI模型管理的REST API接口：
 * 1. 模型上传和管理
 * 2. 模型发布和版本控制
 * 3. 设备同步管理
 * 4. 模型查询和删除
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/ai-model")
@Validated
@Tag(name = "AI模型管理", description = "AI智能模型管理相关API")
public class AiModelController {

    @Resource
    private AiModelService aiModelService;

    /**
     * 上传AI模型
     */
    @PostMapping("/upload")
    @Operation(summary = "上传AI模型", description = "上传AI模型文件并创建模型记录")
    public ResponseDTO<Long> uploadModel(
            @Parameter(description = "模型文件（最大500MB）", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "模型名称", required = true)
            @RequestParam("modelName") String modelName,

            @Parameter(description = "模型版本", required = true)
            @RequestParam("modelVersion") String modelVersion,

            @Parameter(description = "模型类型", required = true)
            @RequestParam("modelType") String modelType,

            @Parameter(description = "支持的事件类型（可选）")
            @RequestParam(value = "supportedEvents", required = false) String supportedEvents) {

        log.info("[AI模型API] 上传AI模型: fileName={}, modelName={}, modelVersion={}, modelType={}",
                file.getOriginalFilename(), modelName, modelVersion, modelType);

        try {
            // 构造上传表单
            AiModelUploadForm uploadForm = new AiModelUploadForm();
            uploadForm.setFile(file);
            uploadForm.setModelName(modelName);
            uploadForm.setModelVersion(modelVersion);
            uploadForm.setModelType(modelType);
            uploadForm.setSupportedEvents(supportedEvents);

            Long modelId = aiModelService.uploadModel(file, uploadForm);
            return ResponseDTO.ok(modelId);

        } catch (IllegalArgumentException e) {
            log.warn("[AI模型API] 上传失败: {}", e.getMessage());
            return ResponseDTO.error("INVALID_PARAM", e.getMessage());
        } catch (Exception e) {
            log.error("[AI模型API] 上传异常: {}", e.getMessage(), e);
            return ResponseDTO.error("UPLOAD_ERROR", "AI模型上传失败: " + e.getMessage());
        }
    }

    /**
     * 发布AI模型
     */
    @PostMapping("/{modelId}/publish")
    @Operation(summary = "发布AI模型", description = "将草稿状态的AI模型发布，使其可用于设备同步")
    public ResponseDTO<Void> publishModel(
            @Parameter(description = "模型ID", required = true)
            @PathVariable @NotNull Long modelId,

            @Parameter(description = "发布人ID（可选，默认为系统管理员）")
            @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId) {

        log.info("[AI模型API] 发布AI模型: modelId={}, userId={}", modelId, userId);

        try {
            aiModelService.publishModel(modelId, userId);
            return ResponseDTO.ok();

        } catch (IllegalArgumentException e) {
            log.warn("[AI模型API] 发布失败: {}", e.getMessage());
            return ResponseDTO.error("INVALID_PARAM", e.getMessage());
        } catch (Exception e) {
            log.error("[AI模型API] 发布异常: modelId={}, error={}", modelId, e.getMessage(), e);
            return ResponseDTO.error("PUBLISH_ERROR", "AI模型发布失败: " + e.getMessage());
        }
    }

    /**
     * 同步模型到设备
     */
    @PostMapping("/{modelId}/sync")
    @Operation(summary = "同步模型到设备", description = "将已发布的AI模型同步到指定的边缘设备")
    public ResponseDTO<Map<String, Object>> syncModelToDevices(
            @Parameter(description = "模型ID", required = true)
            @PathVariable @NotNull Long modelId,

            @Valid @RequestBody AiModelSyncForm syncForm) {

        log.info("[AI模型API] 同步模型到设备: modelId={}, deviceCount={}",
                modelId, syncForm.getDeviceIds().size());

        // 确保路径中的modelId与表单中的modelId一致
        syncForm.setModelId(modelId);

        try {
            String syncTaskId = aiModelService.syncModelToDevices(syncForm);

            Map<String, Object> result = new HashMap<>();
            result.put("syncTaskId", syncTaskId);
            result.put("modelId", modelId);
            result.put("deviceCount", syncForm.getDeviceIds().size());
            result.put("message", "模型同步任务已创建");

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("[AI模型API] 同步失败: {}", e.getMessage());
            return ResponseDTO.error("INVALID_PARAM", e.getMessage());
        } catch (Exception e) {
            log.error("[AI模型API] 同步异常: modelId={}, error={}", modelId, e.getMessage(), e);
            return ResponseDTO.error("SYNC_ERROR", "模型同步失败: " + e.getMessage());
        }
    }

    /**
     * 查询AI模型列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "查询AI模型列表", description = "根据条件分页查询AI模型列表")
    public ResponseDTO<IPage<AiModelEntity>> queryModelList(@Valid AiModelQueryForm queryForm) {
        log.info("[AI模型API] 查询AI模型列表: modelType={}, modelStatus={}, pageNum={}, pageSize={}",
                queryForm.getModelType(), queryForm.getModelStatus(),
                queryForm.getPageNum(), queryForm.getPageSize());

        try {
            IPage<AiModelEntity> pageResult = aiModelService.queryModelList(queryForm);
            return ResponseDTO.ok(pageResult);

        } catch (Exception e) {
            log.error("[AI模型API] 查询列表异常: {}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_ERROR", "查询模型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI模型详情
     */
    @GetMapping("/{modelId}")
    @Operation(summary = "获取AI模型详情", description = "根据模型ID获取AI模型的详细信息")
    public ResponseDTO<AiModelVO> getModelDetail(
            @Parameter(description = "模型ID", required = true)
            @PathVariable @NotNull Long modelId) {

        log.info("[AI模型API] 获取AI模型详情: modelId={}", modelId);

        try {
            AiModelVO modelDetail = aiModelService.getModelDetail(modelId);
            if (modelDetail == null) {
                return ResponseDTO.error("MODEL_NOT_FOUND", "AI模型不存在: " + modelId);
            }

            return ResponseDTO.ok(modelDetail);

        } catch (IllegalArgumentException e) {
            log.warn("[AI模型API] 查询失败: {}", e.getMessage());
            return ResponseDTO.error("INVALID_PARAM", e.getMessage());
        } catch (Exception e) {
            log.error("[AI模型API] 查询详情异常: modelId={}, error={}", modelId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_ERROR", "获取模型详情失败: " + e.getMessage());
        }
    }

    /**
     * 查询模型同步进度
     */
    @GetMapping("/{modelId}/sync-progress")
    @Operation(summary = "查询模型同步进度", description = "查询AI模型到设备的同步进度")
    public ResponseDTO<DeviceModelSyncProgressVO> querySyncProgress(
            @Parameter(description = "模型ID", required = true)
            @PathVariable @NotNull Long modelId) {

        log.info("[AI模型API] 查询模型同步进度: modelId={}", modelId);

        try {
            DeviceModelSyncProgressVO progress = aiModelService.querySyncProgress(modelId);
            return ResponseDTO.ok(progress);

        } catch (Exception e) {
            log.error("[AI模型API] 查询同步进度异常: modelId={}, error={}", modelId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_ERROR", "查询同步进度失败: " + e.getMessage());
        }
    }

    /**
     * 删除AI模型
     */
    @DeleteMapping("/{modelId}")
    @Operation(summary = "删除AI模型", description = "删除草稿状态的AI模型（已发布的模型不能删除）")
    public ResponseDTO<Void> deleteModel(
            @Parameter(description = "模型ID", required = true)
            @PathVariable @NotNull Long modelId) {

        log.info("[AI模型API] 删除AI模型: modelId={}", modelId);

        try {
            aiModelService.deleteModel(modelId);
            return ResponseDTO.ok();

        } catch (IllegalArgumentException e) {
            log.warn("[AI模型API] 删除失败: {}", e.getMessage());
            return ResponseDTO.error("INVALID_PARAM", e.getMessage());
        } catch (Exception e) {
            log.error("[AI模型API] 删除异常: modelId={}, error={}", modelId, e.getMessage(), e);
            return ResponseDTO.error("DELETE_ERROR", "AI模型删除失败: " + e.getMessage());
        }
    }
}
