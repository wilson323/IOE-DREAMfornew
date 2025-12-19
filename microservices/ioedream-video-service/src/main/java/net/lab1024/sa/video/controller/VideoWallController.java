package net.lab1024.sa.video.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.video.domain.form.VideoDecoderAddForm;
import net.lab1024.sa.video.domain.form.VideoDecoderUpdateForm;
import net.lab1024.sa.video.domain.form.VideoDisplayTaskAddForm;
import net.lab1024.sa.video.domain.form.VideoWallAddForm;
import net.lab1024.sa.video.domain.form.VideoWallPresetAddForm;
import net.lab1024.sa.video.domain.form.VideoWallTourAddForm;
import net.lab1024.sa.video.domain.form.VideoWallUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoDecoderVO;
import net.lab1024.sa.video.domain.vo.VideoDisplayTaskVO;
import net.lab1024.sa.video.domain.vo.VideoWallPresetVO;
import net.lab1024.sa.video.domain.vo.VideoWallTourVO;
import net.lab1024.sa.video.domain.vo.VideoWallVO;
import net.lab1024.sa.video.service.VideoWallService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 解码上墙控制器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * - 严格遵循RESTful规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/wall")
@Tag(name = "解码上墙管理", description = "电视墙管理、上墙任务、预案、轮巡等API")
@PermissionCheck(value = "VIDEO_WALL", description = "解码上墙管理模块权限")
public class VideoWallController {

    @Resource
    private VideoWallService videoWallService;

    /**
     * 创建电视墙
     * <p>
     * 严格遵循RESTful规范：创建操作使用POST方法
     * </p>
     *
     * @param addForm 新增表单
     * @return 电视墙ID
     */
    @PostMapping
    @Operation(summary = "创建电视墙", description = "创建电视墙并自动初始化窗口布局")
    @Observed(name = "video.wall.create", contextualName = "video-wall-create")
    public ResponseDTO<Long> createWall(@Valid @RequestBody VideoWallAddForm addForm) {
        log.info("[解码上墙API] 创建电视墙: wallCode={}, wallName={}", addForm.getWallCode(), addForm.getWallName());
        return videoWallService.createWall(addForm);
    }

    /**
     * 更新电视墙
     * <p>
     * 严格遵循RESTful规范：更新操作使用PUT方法
     * </p>
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "更新电视墙", description = "更新电视墙配置信息")
    @Observed(name = "video.wall.update", contextualName = "video-wall-update")
    public ResponseDTO<Void> updateWall(@Valid @RequestBody VideoWallUpdateForm updateForm) {
        log.info("[解码上墙API] 更新电视墙: wallId={}", updateForm.getWallId());
        return videoWallService.updateWall(updateForm);
    }

    /**
     * 删除电视墙
     * <p>
     * 严格遵循RESTful规范：删除操作使用DELETE方法
     * </p>
     *
     * @param wallId 电视墙ID
     * @return 操作结果
     */
    @DeleteMapping("/{wallId}")
    @Operation(summary = "删除电视墙", description = "逻辑删除电视墙")
    @Observed(name = "video.wall.delete", contextualName = "video-wall-delete")
    public ResponseDTO<Void> deleteWall(
            @Parameter(description = "电视墙ID", required = true)
            @PathVariable @NotNull(message = "电视墙ID不能为空") Long wallId) {
        log.info("[解码上墙API] 删除电视墙: wallId={}", wallId);
        return videoWallService.deleteWall(wallId);
    }

    /**
     * 查询电视墙详情
     * <p>
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param wallId 电视墙ID
     * @return 电视墙详情
     */
    @GetMapping("/{wallId}")
    @Operation(summary = "查询电视墙详情", description = "根据ID查询电视墙详细信息")
    @Observed(name = "video.wall.get", contextualName = "video-wall-get")
    public ResponseDTO<VideoWallVO> getWallById(
            @Parameter(description = "电视墙ID", required = true)
            @PathVariable @NotNull(message = "电视墙ID不能为空") Long wallId) {
        log.info("[解码上墙API] 查询电视墙详情: wallId={}", wallId);
        return videoWallService.getWallById(wallId);
    }

    /**
     * 查询电视墙列表
     * <p>
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param regionId 区域ID（可选）
     * @return 电视墙列表
     */
    @GetMapping
    @Operation(summary = "查询电视墙列表", description = "查询电视墙列表，支持按区域筛选")
    @Observed(name = "video.wall.list", contextualName = "video-wall-list")
    public ResponseDTO<List<VideoWallVO>> getWallList(
            @Parameter(description = "区域ID", required = false)
            @RequestParam(required = false) Long regionId) {
        log.info("[解码上墙API] 查询电视墙列表: regionId={}", regionId);
        return videoWallService.getWallList(regionId);
    }

    /**
     * 创建上墙任务
     * <p>
     * 严格遵循RESTful规范：创建操作使用POST方法
     * </p>
     *
     * @param wallId 电视墙ID
     * @param addForm 新增表单
     * @return 任务ID
     */
    @PostMapping("/{wallId}/tasks")
    @Operation(summary = "创建上墙任务", description = "为指定电视墙创建上墙任务")
    @Observed(name = "video.wall.task.create", contextualName = "video-wall-task-create")
    public ResponseDTO<Long> createDisplayTask(
            @Parameter(description = "电视墙ID", required = true)
            @PathVariable @NotNull(message = "电视墙ID不能为空") Long wallId,
            @Valid @RequestBody VideoDisplayTaskAddForm addForm) {
        log.info("[解码上墙API] 创建上墙任务: wallId={}, windowId={}, deviceId={}",
                wallId, addForm.getWindowId(), addForm.getDeviceId());
        // 从路径参数设置wallId（确保一致性）
        addForm.setWallId(wallId);
        return videoWallService.createDisplayTask(addForm);
    }

    /**
     * 取消上墙任务
     * <p>
     * 严格遵循RESTful规范：删除操作使用DELETE方法
     * </p>
     *
     * @param wallId 电视墙ID
     * @param taskId 任务ID
     * @return 操作结果
     */
    @DeleteMapping("/{wallId}/tasks/{taskId}")
    @Operation(summary = "取消上墙任务", description = "取消指定的上墙任务")
    @Observed(name = "video.wall.task.cancel", contextualName = "video-wall-task-cancel")
    public ResponseDTO<Void> cancelDisplayTask(
            @Parameter(description = "电视墙ID", required = true)
            @PathVariable @NotNull(message = "电视墙ID不能为空") Long wallId,
            @Parameter(description = "任务ID", required = true)
            @PathVariable @NotNull(message = "任务ID不能为空") Long taskId) {
        log.info("[解码上墙API] 取消上墙任务: wallId={}, taskId={}", wallId, taskId);
        return videoWallService.cancelDisplayTask(taskId);
    }

    /**
     * 查询上墙任务列表
     * <p>
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param wallId 电视墙ID
     * @return 任务列表
     */
    @GetMapping("/{wallId}/tasks")
    @Operation(summary = "查询上墙任务列表", description = "查询指定电视墙的上墙任务列表")
    @Observed(name = "video.wall.task.list", contextualName = "video-wall-task-list")
    public ResponseDTO<List<VideoDisplayTaskVO>> getTaskList(
            @Parameter(description = "电视墙ID", required = true)
            @PathVariable @NotNull(message = "电视墙ID不能为空") Long wallId) {
        log.info("[解码上墙API] 查询上墙任务列表: wallId={}", wallId);
        return videoWallService.getTaskList(wallId);
    }

    /**
     * 创建预案
     * <p>
     * 严格遵循RESTful规范：创建操作使用POST方法
     * </p>
     *
     * @param wallId 电视墙ID
     * @param addForm 新增表单
     * @return 预案ID
     */
    @PostMapping("/{wallId}/presets")
    @Operation(summary = "创建预案", description = "为指定电视墙创建预案")
    @Observed(name = "video.wall.preset.create", contextualName = "video-wall-preset-create")
    public ResponseDTO<Long> createPreset(
            @Parameter(description = "电视墙ID", required = true)
            @PathVariable @NotNull(message = "电视墙ID不能为空") Long wallId,
            @Valid @RequestBody VideoWallPresetAddForm addForm) {
        log.info("[解码上墙API] 创建预案: wallId={}, presetName={}", wallId, addForm.getPresetName());
        addForm.setWallId(wallId);
        return videoWallService.createPreset(addForm);
    }

    /**
     * 删除预案
     * <p>
     * 严格遵循RESTful规范：删除操作使用DELETE方法
     * </p>
     *
     * @param presetId 预案ID
     * @return 操作结果
     */
    @DeleteMapping("/presets/{presetId}")
    @Operation(summary = "删除预案", description = "删除指定的预案")
    @Observed(name = "video.wall.preset.delete", contextualName = "video-wall-preset-delete")
    public ResponseDTO<Void> deletePreset(
            @Parameter(description = "预案ID", required = true)
            @PathVariable @NotNull(message = "预案ID不能为空") Long presetId) {
        log.info("[解码上墙API] 删除预案: presetId={}", presetId);
        return videoWallService.deletePreset(presetId);
    }

    /**
     * 查询预案列表
     * <p>
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param wallId 电视墙ID
     * @return 预案列表
     */
    @GetMapping("/{wallId}/presets")
    @Operation(summary = "查询预案列表", description = "查询指定电视墙的预案列表")
    @Observed(name = "video.wall.preset.list", contextualName = "video-wall-preset-list")
    public ResponseDTO<List<VideoWallPresetVO>> getPresetList(
            @Parameter(description = "电视墙ID", required = true)
            @PathVariable @NotNull(message = "电视墙ID不能为空") Long wallId) {
        log.info("[解码上墙API] 查询预案列表: wallId={}", wallId);
        return videoWallService.getPresetList(wallId);
    }

    /**
     * 调用预案
     * <p>
     * 严格遵循RESTful规范：操作使用POST方法
     * </p>
     *
     * @param presetId 预案ID
     * @return 操作结果
     */
    @PostMapping("/presets/{presetId}/apply")
    @Operation(summary = "调用预案", description = "应用指定的预案配置")
    @Observed(name = "video.wall.preset.apply", contextualName = "video-wall-preset-apply")
    public ResponseDTO<Void> applyPreset(
            @Parameter(description = "预案ID", required = true)
            @PathVariable @NotNull(message = "预案ID不能为空") Long presetId) {
        log.info("[解码上墙API] 调用预案: presetId={}", presetId);
        return videoWallService.applyPreset(presetId);
    }

    /**
     * 创建轮巡
     * <p>
     * 严格遵循RESTful规范：创建操作使用POST方法
     * </p>
     *
     * @param wallId 电视墙ID
     * @param addForm 新增表单
     * @return 轮巡ID
     */
    @PostMapping("/{wallId}/tours")
    @Operation(summary = "创建轮巡", description = "为指定电视墙创建轮巡")
    @Observed(name = "video.wall.tour.create", contextualName = "video-wall-tour-create")
    public ResponseDTO<Long> createTour(
            @Parameter(description = "电视墙ID", required = true)
            @PathVariable @NotNull(message = "电视墙ID不能为空") Long wallId,
            @Valid @RequestBody VideoWallTourAddForm addForm) {
        log.info("[解码上墙API] 创建轮巡: wallId={}, tourName={}", wallId, addForm.getTourName());
        addForm.setWallId(wallId);
        return videoWallService.createTour(addForm);
    }

    /**
     * 删除轮巡
     * <p>
     * 严格遵循RESTful规范：删除操作使用DELETE方法
     * </p>
     *
     * @param tourId 轮巡ID
     * @return 操作结果
     */
    @DeleteMapping("/tours/{tourId}")
    @Operation(summary = "删除轮巡", description = "删除指定的轮巡")
    @Observed(name = "video.wall.tour.delete", contextualName = "video-wall-tour-delete")
    public ResponseDTO<Void> deleteTour(
            @Parameter(description = "轮巡ID", required = true)
            @PathVariable @NotNull(message = "轮巡ID不能为空") Long tourId) {
        log.info("[解码上墙API] 删除轮巡: tourId={}", tourId);
        return videoWallService.deleteTour(tourId);
    }

    /**
     * 查询轮巡列表
     * <p>
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param wallId 电视墙ID
     * @return 轮巡列表
     */
    @GetMapping("/{wallId}/tours")
    @Operation(summary = "查询轮巡列表", description = "查询指定电视墙的轮巡列表")
    @Observed(name = "video.wall.tour.list", contextualName = "video-wall-tour-list")
    public ResponseDTO<List<VideoWallTourVO>> getTourList(
            @Parameter(description = "电视墙ID", required = true)
            @PathVariable @NotNull(message = "电视墙ID不能为空") Long wallId) {
        log.info("[解码上墙API] 查询轮巡列表: wallId={}", wallId);
        return videoWallService.getTourList(wallId);
    }

    /**
     * 启动轮巡
     * <p>
     * 严格遵循RESTful规范：操作使用POST方法
     * </p>
     *
     * @param tourId 轮巡ID
     * @return 操作结果
     */
    @PostMapping("/tours/{tourId}/start")
    @Operation(summary = "启动轮巡", description = "启动指定的轮巡任务")
    @Observed(name = "video.wall.tour.start", contextualName = "video-wall-tour-start")
    public ResponseDTO<Void> startTour(
            @Parameter(description = "轮巡ID", required = true)
            @PathVariable @NotNull(message = "轮巡ID不能为空") Long tourId) {
        log.info("[解码上墙API] 启动轮巡: tourId={}", tourId);
        return videoWallService.startTour(tourId);
    }

    /**
     * 停止轮巡
     * <p>
     * 严格遵循RESTful规范：操作使用POST方法
     * </p>
     *
     * @param tourId 轮巡ID
     * @return 操作结果
     */
    @PostMapping("/tours/{tourId}/stop")
    @Operation(summary = "停止轮巡", description = "停止指定的轮巡任务")
    @Observed(name = "video.wall.tour.stop", contextualName = "video-wall-tour-stop")
    public ResponseDTO<Void> stopTour(
            @Parameter(description = "轮巡ID", required = true)
            @PathVariable @NotNull(message = "轮巡ID不能为空") Long tourId) {
        log.info("[解码上墙API] 停止轮巡: tourId={}", tourId);
        return videoWallService.stopTour(tourId);
    }

    /**
     * 添加解码器
     * <p>
     * 严格遵循RESTful规范：创建操作使用POST方法
     * </p>
     *
     * @param addForm 新增表单
     * @return 解码器ID
     */
    @PostMapping("/decoders")
    @Operation(summary = "添加解码器", description = "添加新的解码器设备")
    @Observed(name = "video.wall.decoder.add", contextualName = "video-wall-decoder-add")
    public ResponseDTO<Long> addDecoder(@Valid @RequestBody VideoDecoderAddForm addForm) {
        log.info("[解码上墙API] 添加解码器: decoderCode={}, decoderName={}", addForm.getDecoderCode(), addForm.getDecoderName());
        return videoWallService.addDecoder(addForm);
    }

    /**
     * 更新解码器
     * <p>
     * 严格遵循RESTful规范：更新操作使用PUT方法
     * </p>
     *
     * @param decoderId 解码器ID
     * @param updateForm 更新表单
     * @return 操作结果
     */
    @PutMapping("/decoders/{decoderId}")
    @Operation(summary = "更新解码器", description = "更新解码器配置信息")
    @Observed(name = "video.wall.decoder.update", contextualName = "video-wall-decoder-update")
    public ResponseDTO<Void> updateDecoder(
            @Parameter(description = "解码器ID", required = true)
            @PathVariable @NotNull(message = "解码器ID不能为空") Long decoderId,
            @Valid @RequestBody VideoDecoderUpdateForm updateForm) {
        log.info("[解码上墙API] 更新解码器: decoderId={}", decoderId);
        updateForm.setDecoderId(decoderId);
        return videoWallService.updateDecoder(updateForm);
    }

    /**
     * 删除解码器
     * <p>
     * 严格遵循RESTful规范：删除操作使用DELETE方法
     * </p>
     *
     * @param decoderId 解码器ID
     * @return 操作结果
     */
    @DeleteMapping("/decoders/{decoderId}")
    @Operation(summary = "删除解码器", description = "逻辑删除解码器")
    @Observed(name = "video.wall.decoder.delete", contextualName = "video-wall-decoder-delete")
    public ResponseDTO<Void> deleteDecoder(
            @Parameter(description = "解码器ID", required = true)
            @PathVariable @NotNull(message = "解码器ID不能为空") Long decoderId) {
        log.info("[解码上墙API] 删除解码器: decoderId={}", decoderId);
        return videoWallService.deleteDecoder(decoderId);
    }

    /**
     * 查询解码器详情
     * <p>
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param decoderId 解码器ID
     * @return 解码器详情
     */
    @GetMapping("/decoders/{decoderId}")
    @Operation(summary = "查询解码器详情", description = "根据ID查询解码器详细信息")
    @Observed(name = "video.wall.decoder.get", contextualName = "video-wall-decoder-get")
    public ResponseDTO<VideoDecoderVO> getDecoderById(
            @Parameter(description = "解码器ID", required = true)
            @PathVariable @NotNull(message = "解码器ID不能为空") Long decoderId) {
        log.info("[解码上墙API] 查询解码器详情: decoderId={}", decoderId);
        return videoWallService.getDecoderById(decoderId);
    }

    /**
     * 查询解码器列表
     * <p>
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param areaId 区域ID（可选）
     * @return 解码器列表
     */
    @GetMapping("/decoders")
    @Operation(summary = "查询解码器列表", description = "查询解码器列表，支持按区域筛选")
    @Observed(name = "video.wall.decoder.list", contextualName = "video-wall-decoder-list")
    public ResponseDTO<List<VideoDecoderVO>> getDecoderList(
            @Parameter(description = "区域ID", required = false)
            @RequestParam(required = false) Long areaId) {
        log.info("[解码上墙API] 查询解码器列表: areaId={}", areaId);
        return videoWallService.getDecoderList(areaId);
    }
}
