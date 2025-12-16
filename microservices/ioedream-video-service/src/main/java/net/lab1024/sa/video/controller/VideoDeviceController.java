package net.lab1024.sa.video.controller;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoDeviceAddForm;
import net.lab1024.sa.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.video.domain.form.VideoDeviceUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.service.VideoDeviceService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;

import java.util.List;

/**
 * 视频设备管理PC端控制器
 * <p>
 * 提供PC端视频设备管理相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 设备CRUD操作
 * - 设备状态管理
 * - 设备查询和统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/device")
@Tag(name = "视频设备管理PC端", description = "设备查询、添加、更新、删除、状态管理等API")
@PermissionCheck(value = "VIDEO_DEVICE", description = "视频设备管理模块权限")
public class VideoDeviceController {

    @Resource
    private VideoDeviceService videoDeviceService;

    /**
     * 分页查询设备
     * <p>
     * 功能说明：
     * 1. 支持关键词搜索（设备名称、设备编号）
     * 2. 支持区域筛选
     * 3. 支持设备状态筛选（在线/离线/故障）
     * 4. 支持分页查询
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param keyword 关键词（可选）
     * @param areaId 区域ID（可选）
     * @param status 设备状态（可选）
     * @return 设备列表
     * @apiNote 示例请求：
     * <pre>
     * GET /api/v1/video/device/query?pageNum=1&pageSize=20&keyword=摄像头&areaId=4001&status=1
     * </pre>
     */
    @Observed(name = "video.device.queryDevices", contextualName = "video-device-query")
    @GetMapping("/query")
    @Operation(
            summary = "分页查询设备",
            description = "根据条件分页查询视频设备，支持关键词搜索、区域筛选、状态筛选。严格遵循RESTful规范：查询操作使用GET方法。",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PageResult.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "参数错误"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<PageResult<VideoDeviceVO>> queryDevices(
            @Parameter(description = "页码（从1开始）")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词（设备名称、设备编号）")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "区域ID（可选）")
            @RequestParam(required = false) String areaId,
            @Parameter(description = "设备状态（可选，1-在线，2-离线，3-故障）")
            @RequestParam(required = false) Integer status) {
        log.info("[视频设备] 分页查询设备，pageNum={}, pageSize={}, keyword={}, areaId={}, status={}",
                pageNum, pageSize, keyword, areaId, status);
        try {
            // 构建查询表单
            VideoDeviceQueryForm queryForm = new VideoDeviceQueryForm();
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);
            queryForm.setKeyword(keyword);
            queryForm.setAreaId(areaId);
            queryForm.setStatus(status);

            PageResult<VideoDeviceVO> result = videoDeviceService.queryDevices(queryForm);
            return ResponseDTO.ok(result);
        } catch (ParamException e) {
            log.warn("[视频设备] 分页查询设备参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频设备] 分页查询设备业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频设备] 分页查询设备系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频设备] 分页查询设备执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_DEVICES_ERROR", "查询设备失败");
        }
    }

    /**
     * 查询设备详情
     * <p>
     * 功能说明：
     * 1. 根据设备ID查询设备详细信息
     * 2. 验证设备类型是否为摄像头
     * 3. 返回设备状态、IP地址、端口等完整信息
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @Observed(name = "video.device.getDeviceDetail", contextualName = "video-device-get-detail")
    @GetMapping("/{deviceId}")
    @Operation(
            summary = "查询设备详情",
            description = "根据设备ID查询设备详细信息，包括设备状态、IP地址、端口等",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1001")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = VideoDeviceVO.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "设备不存在"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<VideoDeviceVO> getDeviceDetail(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {
        log.info("[视频设备] 查询设备详情，deviceId={}", deviceId);
        try {
            VideoDeviceVO device = videoDeviceService.getDeviceDetail(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }
            return ResponseDTO.ok(device);
        } catch (ParamException e) {
            log.warn("[视频设备] 查询设备详情参数错误，deviceId={}: {}", deviceId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频设备] 查询设备详情业务异常，deviceId={}: {}", deviceId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频设备] 查询设备详情系统异常，deviceId={}: {}", deviceId, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频设备] 查询设备详情执行异常，deviceId={}: {}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_ERROR", "查询设备详情失败");
        }
    }

    /**
     * 新增视频设备
     * <p>
     * 功能说明：
     * 1. 新增摄像头设备信息
     * 2. 验证设备编号唯一性
     * 3. 自动设置设备类型为CAMERA
     * 4. 支持设备扩展属性配置
     * </p>
     *
     * @param addForm 设备新增表单
     * @return 操作结果
     */
    @Observed(name = "video.device.addDevice", contextualName = "video-device-add")
    @PostMapping("/add")
    @Operation(
            summary = "新增视频设备",
            description = "新增摄像头设备，支持完整的设备信息录入和扩展属性配置",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "新增成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = VideoDeviceVO.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "参数错误"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<VideoDeviceVO> addDevice(@Valid @RequestBody VideoDeviceAddForm addForm) {
        log.info("[视频设备] 新增设备，deviceCode={}, deviceName={}", addForm.getDeviceCode(), addForm.getDeviceName());
        try {
            return videoDeviceService.addDevice(addForm);
        } catch (ParamException e) {
            log.warn("[视频设备] 新增设备参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频设备] 新增设备业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频设备] 新增设备系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频设备] 新增设备执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("ADD_DEVICE_ERROR", "新增设备失败");
        }
    }

    /**
     * 更新视频设备
     * <p>
     * 功能说明：
     * 1. 更新设备基本信息
     * 2. 支持部分字段更新
     * 3. 保持设备类型不变
     * 4. 更新扩展属性配置
     * </p>
     *
     * @param updateForm 设备更新表单
     * @return 操作结果
     */
    @Observed(name = "video.device.updateDevice", contextualName = "video-device-update")
    @PutMapping("/update")
    @Operation(
            summary = "更新视频设备",
            description = "更新设备基本信息和配置，支持部分字段更新",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "更新成功"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "参数错误"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "设备不存在"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Void> updateDevice(@Valid @RequestBody VideoDeviceUpdateForm updateForm) {
        log.info("[视频设备] 更新设备，deviceId={}", updateForm.getDeviceId());
        try {
            return videoDeviceService.updateDevice(updateForm);
        } catch (ParamException e) {
            log.warn("[视频设备] 更新设备参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频设备] 更新设备业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频设备] 更新设备系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频设备] 更新设备执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("UPDATE_DEVICE_ERROR", "更新设备失败");
        }
    }

    /**
     * 删除视频设备
     * <p>
     * 功能说明：
     * 1. 软删除设备记录
     * 2. 停止相关视频流
     * 3. 清理设备缓存
     * </p>
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @Observed(name = "video.device.deleteDevice", contextualName = "video-device-delete")
    @DeleteMapping("/{deviceId}")
    @Operation(
            summary = "删除视频设备",
            description = "软删除设备记录，同时停止相关视频流",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1001")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "删除成功"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "设备不存在"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Void> deleteDevice(@PathVariable @NotNull Long deviceId) {
        log.info("[视频设备] 删除设备，deviceId={}", deviceId);
        try {
            return videoDeviceService.deleteDevice(deviceId);
        } catch (ParamException e) {
            log.warn("[视频设备] 删除设备参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频设备] 删除设备业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频设备] 删除设备系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频设备] 删除设备执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("DELETE_DEVICE_ERROR", "删除设备失败");
        }
    }

    /**
     * 批量删除视频设备
     * <p>
     * 功能说明：
     * 1. 批量软删除设备记录
     * 2. 停止所有相关视频流
     * 3. 清理设备缓存
     * </p>
     *
     * @param deviceIds 设备ID列表
     * @return 操作结果
     */
    @Observed(name = "video.device.batchDeleteDevices", contextualName = "video-device-batch-delete")
    @DeleteMapping("/batch")
    @Operation(
            summary = "批量删除视频设备",
            description = "批量软删除设备记录，同时停止相关视频流",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "批量删除成功"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "参数错误"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Void> batchDeleteDevices(@RequestBody List<Long> deviceIds) {
        log.info("[视频设备] 批量删除设备，deviceIds={}", deviceIds);
        try {
            return videoDeviceService.batchDeleteDevices(deviceIds);
        } catch (ParamException e) {
            log.warn("[视频设备] 批量删除设备参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频设备] 批量删除设备业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频设备] 批量删除设备系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频设备] 批量删除设备执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("BATCH_DELETE_DEVICES_ERROR", "批量删除设备失败");
        }
    }

    /**
     * 启用/禁用设备
     * <p>
     * 功能说明：
     * 1. 切换设备启用状态
     * 2. 禁用时停止视频流
     * 3. 启用时恢复视频流
     * </p>
     *
     * @param deviceId 设备ID
     * @param enabledFlag 启用标志：0-禁用，1-启用
     * @return 操作结果
     */
    @Observed(name = "video.device.toggleDeviceStatus", contextualName = "video-device-toggle-status")
    @PutMapping("/{deviceId}/toggle")
    @Operation(
            summary = "启用/禁用设备",
            description = "切换设备启用状态，禁用时停止视频流，启用时恢复视频流",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1001"),
                    @Parameter(name = "enabledFlag", description = "启用标志：0-禁用，1-启用", required = true, example = "1")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "操作成功"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "设备不存在"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Void> toggleDeviceStatus(
            @PathVariable @NotNull Long deviceId,
            @RequestParam @NotNull Integer enabledFlag) {
        log.info("[视频设备] 切换设备状态，deviceId={}, enabledFlag={}", deviceId, enabledFlag);
        try {
            return videoDeviceService.toggleDeviceStatus(deviceId, enabledFlag);
        } catch (ParamException e) {
            log.warn("[视频设备] 切换设备状态参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频设备] 切换设备状态业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频设备] 切换设备状态系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频设备] 切换设备状态执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("TOGGLE_DEVICE_STATUS_ERROR", "切换设备状态失败");
        }
    }

    /**
     * 设备上线
     * <p>
     * 功能说明：
     * 1. 设备连接成功后调用
     * 2. 更新设备状态为在线
     * 3. 记录上线时间
     * </p>
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @Observed(name = "video.device.deviceOnline", contextualName = "video-device-online")
    @PutMapping("/{deviceId}/online")
    @Operation(
            summary = "设备上线",
            description = "设备连接成功后调用，更新设备状态为在线",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1001")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "操作成功"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "设备不存在"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Void> deviceOnline(@PathVariable @NotNull Long deviceId) {
        log.info("[视频设备] 设备上线，deviceId={}", deviceId);
        try {
            return videoDeviceService.deviceOnline(deviceId);
        } catch (Exception e) {
            log.error("[视频设备] 设备上线异常: deviceId={}", deviceId, e);
            return ResponseDTO.error("DEVICE_ONLINE_ERROR", "设备上线失败");
        }
    }

    /**
     * 设备离线
     * <p>
     * 功能说明：
     * 1. 设备断开连接时调用
     * 2. 更新设备状态为离线
     * 3. 记录离线时间
     * </p>
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @Observed(name = "video.device.deviceOffline", contextualName = "video-device-offline")
    @PutMapping("/{deviceId}/offline")
    @Operation(
            summary = "设备离线",
            description = "设备断开连接时调用，更新设备状态为离线",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1001")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "操作成功"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "设备不存在"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Void> deviceOffline(@PathVariable @NotNull Long deviceId) {
        log.info("[视频设备] 设备离线，deviceId={}", deviceId);
        try {
            return videoDeviceService.deviceOffline(deviceId);
        } catch (Exception e) {
            log.error("[视频设备] 设备离线异常: deviceId={}", deviceId, e);
            return ResponseDTO.error("DEVICE_OFFLINE_ERROR", "设备离线失败");
        }
    }

    /**
     * 检测设备连通性
     * <p>
     * 功能说明：
     * 1. 检测设备网络连通性
     * 2. 返回连通性检测结果
     * 3. 用于设备健康检查
     * </p>
     *
     * @param deviceId 设备ID
     * @return 连通性检测结果
     */
    @Observed(name = "video.device.checkConnectivity", contextualName = "video-device-check-connectivity")
    @GetMapping("/{deviceId}/connectivity")
    @Operation(
            summary = "检测设备连通性",
            description = "检测设备网络连通性，返回连通性检测结果",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true, example = "1001")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "检测完成",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Boolean.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "设备不存在"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Boolean> checkDeviceConnectivity(@PathVariable @NotNull Long deviceId) {
        log.info("[视频设备] 检测设备连通性，deviceId={}", deviceId);
        try {
            return videoDeviceService.checkDeviceConnectivity(deviceId);
        } catch (Exception e) {
            log.error("[视频设备] 检测设备连通性异常: deviceId={}", deviceId, e);
            return ResponseDTO.error("CHECK_CONNECTIVITY_ERROR", "检测设备连通性失败");
        }
    }

    /**
     * 根据区域ID查询设备列表
     * <p>
     * 功能说明：
     * 1. 查询指定区域的所有视频设备
     * 2. 返回设备基本信息
     * 3. 用于区域设备管理
     * </p>
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    @Observed(name = "video.device.getDevicesByAreaId", contextualName = "video-device-get-by-area")
    @GetMapping("/area/{areaId}")
    @Operation(
            summary = "根据区域ID查询设备列表",
            description = "查询指定区域的所有视频设备",
            parameters = {
                    @Parameter(name = "areaId", description = "区域ID", required = true, example = "4001")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = List.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<List<VideoDeviceVO>> getDevicesByAreaId(@PathVariable @NotNull Long areaId) {
        log.info("[视频设备] 根据区域查询设备，areaId={}", areaId);
        try {
            return videoDeviceService.getDevicesByAreaId(areaId);
        } catch (Exception e) {
            log.error("[视频设备] 根据区域查询设备异常: areaId={}", areaId, e);
            return ResponseDTO.error("GET_DEVICES_BY_AREA_ERROR", "根据区域查询设备失败");
        }
    }

    /**
     * 查询在线设备列表
     * <p>
     * 功能说明：
     * 1. 查询所有在线的视频设备
     * 2. 返回设备基本信息
     * 3. 用于实时监控展示
     * </p>
     *
     * @return 在线设备列表
     */
    @Observed(name = "video.device.getOnlineDevices", contextualName = "video-device-get-online")
    @GetMapping("/online")
    @Operation(
            summary = "查询在线设备列表",
            description = "查询所有在线的视频设备",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = List.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<List<VideoDeviceVO>> getOnlineDevices() {
        log.info("[视频设备] 查询在线设备");
        try {
            return videoDeviceService.getOnlineDevices();
        } catch (Exception e) {
            log.error("[视频设备] 查询在线设备异常", e);
            return ResponseDTO.error("GET_ONLINE_DEVICES_ERROR", "查询在线设备失败");
        }
    }

    /**
     * 查询离线设备列表
     * <p>
     * 功能说明：
     * 1. 查询所有离线的视频设备
     * 2. 返回设备基本信息
     * 3. 用于故障设备管理
     * </p>
     *
     * @return 离线设备列表
     */
    @Observed(name = "video.device.getOfflineDevices", contextualName = "video-device-get-offline")
    @GetMapping("/offline")
    @Operation(
            summary = "查询离线设备列表",
            description = "查询所有离线的视频设备",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = List.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<List<VideoDeviceVO>> getOfflineDevices() {
        log.info("[视频设备] 查询离线设备");
        try {
            return videoDeviceService.getOfflineDevices();
        } catch (Exception e) {
            log.error("[视频设备] 查询离线设备异常", e);
            return ResponseDTO.error("GET_OFFLINE_DEVICES_ERROR", "查询离线设备失败");
        }
    }

    /**
     * 查询支持PTZ的设备列表
     * <p>
     * 功能说明：
     * 1. 查询支持PTZ控制的设备
     * 2. 返回设备基本信息
     * 3. 用于PTZ控制功能
     * </p>
     *
     * @return PTZ设备列表
     */
    @Observed(name = "video.device.getPTZDevices", contextualName = "video-device-get-ptz")
    @GetMapping("/ptz")
    @Operation(
            summary = "查询支持PTZ的设备列表",
            description = "查询支持PTZ控制的设备",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = List.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<List<VideoDeviceVO>> getPTZDevices() {
        log.info("[视频设备] 查询PTZ设备");
        try {
            return videoDeviceService.getPTZDevices();
        } catch (Exception e) {
            log.error("[视频设备] 查询PTZ设备异常", e);
            return ResponseDTO.error("GET_PTZ_DEVICES_ERROR", "查询PTZ设备失败");
        }
    }

    /**
     * 查询支持AI的设备列表
     * <p>
     * 功能说明：
     * 1. 查询支持AI分析的设备
     * 2. 返回设备基本信息
     * 3. 用于AI分析功能
     * </p>
     *
     * @return AI设备列表
     */
    @Observed(name = "video.device.getAIDevices", contextualName = "video-device-get-ai")
    @GetMapping("/ai")
    @Operation(
            summary = "查询支持AI的设备列表",
            description = "查询支持AI分析的设备",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = List.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<List<VideoDeviceVO>> getAIDevices() {
        log.info("[视频设备] 查询AI设备");
        try {
            return videoDeviceService.getAIDevices();
        } catch (Exception e) {
            log.error("[视频设备] 查询AI设备异常", e);
            return ResponseDTO.error("GET_AI_DEVICES_ERROR", "查询AI设备失败");
        }
    }

    /**
     * 获取设备统计信息
     * <p>
     * 功能说明：
     * 1. 统计设备总数
     * 2. 统计在线/离线设备数量
     * 3. 统计支持PTZ/AI的设备数量
     * 4. 用于 dashboard 展示
     * </p>
     *
     * @return 统计信息
     */
    @Observed(name = "video.device.getDeviceStatistics", contextualName = "video-device-statistics")
    @GetMapping("/statistics")
    @Operation(
            summary = "获取设备统计信息",
            description = "获取设备总数、在线离线数量、PTZ/AI设备数量等统计信息",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json"
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "无权限"
                    )
            }
    )
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Object> getDeviceStatistics() {
        log.info("[视频设备] 获取设备统计信息");
        try {
            return videoDeviceService.getDeviceStatistics();
        } catch (Exception e) {
            log.error("[视频设备] 获取设备统计信息异常", e);
            return ResponseDTO.error("GET_DEVICE_STATISTICS_ERROR", "获取设备统计信息失败");
        }
    }
}

