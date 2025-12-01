package net.lab1024.sa.admin.module.smart.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.annotation.SaCheckLogin;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceAddForm;
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceUpdateForm;
import net.lab1024.sa.admin.module.smart.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.admin.module.smart.video.service.VideoDeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 视频设备管理控制器
 * <p>
 * 提供视频设备的增删改查功能，包括：
 * </p>
 * <ul>
 * <li>设备注册与配置</li>
 * <li>设备状态监控</li>
 * <li>云台控制(PTZ)</li>
 * <li>设备分组管理</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/api/video/device")
@SaCheckLogin
@Tag(name = "视频设备管理", description = "视频设备的增删改查与控制接口")
public class VideoDeviceController {

    @Resource
    private VideoDeviceService videoDeviceService;

    /**
     * 新增视频设备
     *
     * @param addForm 设备新增表单
     * @return 操作结果
     */
    @Operation(summary = "新增视频设备")
    @PostMapping("/add")
    @SaCheckPermission("video:device:add")
    public ResponseDTO<String> add(@RequestBody @Valid VideoDeviceAddForm addForm) {
        try {
            log.info("新增视频设备: {}", addForm.getDeviceName());
            String result = videoDeviceService.add(addForm);
            log.info("新增视频设备成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("新增视频设备失败", e);
            return ResponseDTO.error("新增视频设备失败: " + e.getMessage());
        }
    }

    /**
     * 更新视频设备
     *
     * @param updateForm 设备更新表单
     * @return 操作结果
     */
    @Operation(summary = "更新视频设备")
    @PostMapping("/update")
    @SaCheckPermission("video:device:update")
    public ResponseDTO<String> update(@RequestBody @Valid VideoDeviceUpdateForm updateForm) {
        try {
            log.info("更新视频设备: {}", updateForm.getDeviceId());
            String result = videoDeviceService.update(updateForm);
            log.info("更新视频设备成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("更新视频设备失败", e);
            return ResponseDTO.error("更新视频设备失败: " + e.getMessage());
        }
    }

    /**
     * 删除视频设备
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @Operation(summary = "删除视频设备")
    @PostMapping("/delete")
    @SaCheckPermission("video:device:delete")
    public ResponseDTO<String> delete(@RequestParam @NotNull(message = "设备ID不能为空") Long deviceId) {
        try {
            log.info("删除视频设备: {}", deviceId);
            String result = videoDeviceService.delete(deviceId);
            log.info("删除视频设备成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("删除视频设备失败", e);
            return ResponseDTO.error("删除视频设备失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询视频设备
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @Operation(summary = "分页查询视频设备")
    @PostMapping("/page")
    @SaCheckPermission("video:device:page")
    public ResponseDTO<PageResult<VideoDeviceVO>> page(@RequestBody @Valid VideoDeviceQueryForm queryForm) {
        try {
            log.info("分页查询视频设备: {}", queryForm);
            PageResult<VideoDeviceVO> result = videoDeviceService.page(queryForm);
            log.info("分页查询视频设备成功, 总数: {}", result.getTotal());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询视频设备失败", e);
            return ResponseDTO.error("分页查询视频设备失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询视频设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @Operation(summary = "查询视频设备详情")
    @GetMapping("/detail")
    @SaCheckPermission("video:device:detail")
    public ResponseDTO<VideoDeviceVO> detail(@RequestParam @NotNull(message = "设备ID不能为空") Long deviceId) {
        try {
            log.info("查询视频设备详情: {}", deviceId);
            VideoDeviceVO result = videoDeviceService.detail(deviceId);
            log.info("查询视频设备详情成功: {}", result.getDeviceName());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("查询视频设备详情失败", e);
            return ResponseDTO.error("查询视频设备详情失败: " + e.getMessage());
        }
    }

    /**
     * 云台控制 - 上
     *
     * @param deviceId 设备ID
     * @param speed 速度
     * @return 操作结果
     */
    @Operation(summary = "云台控制-上")
    @PostMapping("/ptz/up")
    @SaCheckPermission("video:device:ptz")
    public ResponseDTO<String> ptzUp(
            @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @RequestParam(defaultValue = "5") Integer speed) {
        try {
            log.info("云台控制-上 deviceId={}, speed={}", deviceId, speed);
            String result = videoDeviceService.ptzControl(deviceId, "UP", speed);
            log.info("云台控制-上 成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("云台控制-上 失败", e);
            return ResponseDTO.error("云台控制-上 失败: " + e.getMessage());
        }
    }

    /**
     * 云台控制 - 下
     *
     * @param deviceId 设备ID
     * @param speed 速度
     * @return 操作结果
     */
    @Operation(summary = "云台控制-下")
    @PostMapping("/ptz/down")
    @SaCheckPermission("video:device:ptz")
    public ResponseDTO<String> ptzDown(
            @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @RequestParam(defaultValue = "5") Integer speed) {
        try {
            log.info("云台控制-下 deviceId={}, speed={}", deviceId, speed);
            String result = videoDeviceService.ptzControl(deviceId, "DOWN", speed);
            log.info("云台控制-下 成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("云台控制-下 失败", e);
            return ResponseDTO.error("云台控制-下 失败: " + e.getMessage());
        }
    }

    /**
     * 云台控制 - 左
     *
     * @param deviceId 设备ID
     * @param speed 速度
     * @return 操作结果
     */
    @Operation(summary = "云台控制-左")
    @PostMapping("/ptz/left")
    @SaCheckPermission("video:device:ptz")
    public ResponseDTO<String> ptzLeft(
            @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @RequestParam(defaultValue = "5") Integer speed) {
        try {
            log.info("云台控制-左 deviceId={}, speed={}", deviceId, speed);
            String result = videoDeviceService.ptzControl(deviceId, "LEFT", speed);
            log.info("云台控制-左 成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("云台控制-左 失败", e);
            return ResponseDTO.error("云台控制-左 失败: " + e.getMessage());
        }
    }

    /**
     * 云台控制 - 右
     *
     * @param deviceId 设备ID
     * @param speed 速度
     * @return 操作结果
     */
    @Operation(summary = "云台控制-右")
    @PostMapping("/ptz/right")
    @SaCheckPermission("video:device:ptz")
    public ResponseDTO<String> ptzRight(
            @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @RequestParam(defaultValue = "5") Integer speed) {
        try {
            log.info("云台控制-右 deviceId={}, speed={}", deviceId, speed);
            String result = videoDeviceService.ptzControl(deviceId, "RIGHT", speed);
            log.info("云台控制-右 成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("云台控制-右 失败", e);
            return ResponseDTO.error("云台控制-右 失败: " + e.getMessage());
        }
    }

    /**
     * 云台控制 - 变倍+
     *
     * @param deviceId 设备ID
     * @param speed 速度
     * @return 操作结果
     */
    @Operation(summary = "云台控制-变倍+")
    @PostMapping("/ptz/zoom-in")
    @SaCheckPermission("video:device:ptz")
    public ResponseDTO<String> ptzZoomIn(
            @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @RequestParam(defaultValue = "5") Integer speed) {
        try {
            log.info("云台控制-变倍+: deviceId={}, speed={}", deviceId, speed);
            String result = videoDeviceService.ptzControl(deviceId, "ZOOM_IN", speed);
            log.info("云台控制-变倍+ 成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("云台控制-变倍+ 失败", e);
            return ResponseDTO.error("云台控制-变倍+ 失败: " + e.getMessage());
        }
    }

    /**
     * 云台控制 - 变倍-
     *
     * @param deviceId 设备ID
     * @param speed 速度
     * @return 操作结果
     */
    @Operation(summary = "云台控制-变倍-")
    @PostMapping("/ptz/zoom-out")
    @SaCheckPermission("video:device:ptz")
    public ResponseDTO<String> ptzZoomOut(
            @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @RequestParam(defaultValue = "5") Integer speed) {
        try {
            log.info("云台控制-变倍-: deviceId={}, speed={}", deviceId, speed);
            String result = videoDeviceService.ptzControl(deviceId, "ZOOM_OUT", speed);
            log.info("云台控制-变倍- 成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("云台控制-变倍- 失败", e);
            return ResponseDTO.error("云台控制-变倍- 失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备实时状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    @Operation(summary = "获取设备实时状态")
    @GetMapping("/status")
    @SaCheckPermission("video:device:status")
    public ResponseDTO<String> getDeviceStatus(
            @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId) {
        try {
            log.info("获取设备实时状态: {}", deviceId);
            String result = videoDeviceService.getDeviceStatus(deviceId);
            log.info("获取设备实时状态成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取设备实时状态失败", e);
            return ResponseDTO.error("获取设备实时状态失败: " + e.getMessage());
        }
    }

    /**
     * 启动设备录像
     *
     * @param deviceId 设备ID
     * @return 录像ID
     */
    @Operation(summary = "启动设备录像")
    @PostMapping("/record/start")
    @SaCheckPermission("video:device:record")
    public ResponseDTO<Long> startRecording(
            @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId) {
        try {
            log.info("启动设备录像: {}", deviceId);
            Long recordId = videoDeviceService.startRecording(deviceId);
            log.info("启动设备录像成功: deviceId={}, recordId={}", deviceId, recordId);
            return ResponseDTO.ok(recordId, "录像启动成功");
        } catch (Exception e) {
            log.error("启动设备录像失败", e);
            return ResponseDTO.error("启动设备录像失败: " + e.getMessage());
        }
    }

    /**
     * 停止设备录像
     *
     * @param recordId 录像ID
     * @return 操作结果
     */
    @Operation(summary = "停止设备录像")
    @PostMapping("/record/stop")
    @SaCheckPermission("video:device:record")
    public ResponseDTO<String> stopRecording(
            @RequestParam @NotNull(message = "录像ID不能为空") Long recordId) {
        try {
            log.info("停止设备录像: {}", recordId);
            String result = videoDeviceService.stopRecording(recordId);
            log.info("停止设备录像成功: {}", result);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("停止设备录像失败", e);
            return ResponseDTO.error("停止设备录像失败: " + e.getMessage());
        }
    }
}