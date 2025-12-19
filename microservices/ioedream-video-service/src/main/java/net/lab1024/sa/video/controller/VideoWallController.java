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
import net.lab1024.sa.video.domain.form.VideoDisplayTaskAddForm;
import net.lab1024.sa.video.domain.form.VideoWallAddForm;
import net.lab1024.sa.video.domain.form.VideoWallUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoDisplayTaskVO;
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
}
