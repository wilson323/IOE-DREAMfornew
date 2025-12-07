package net.lab1024.sa.video.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.service.VideoDeviceService;

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
    @PreAuthorize("hasRole('VIDEO_MANAGER')")
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
        } catch (Exception e) {
            log.error("[视频设备] 分页查询设备失败", e);
            return ResponseDTO.error("QUERY_DEVICES_ERROR", "查询设备失败: " + e.getMessage());
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
    @PreAuthorize("hasRole('VIDEO_MANAGER')")
    public ResponseDTO<VideoDeviceVO> getDeviceDetail(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {
        log.info("[视频设备] 查询设备详情，deviceId={}", deviceId);
        try {
            VideoDeviceVO device = videoDeviceService.getDeviceDetail(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }
            return ResponseDTO.ok(device);
        } catch (Exception e) {
            log.error("[视频设备] 查询设备详情失败，deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_DEVICE_ERROR", "查询设备详情失败: " + e.getMessage());
        }
    }
}

