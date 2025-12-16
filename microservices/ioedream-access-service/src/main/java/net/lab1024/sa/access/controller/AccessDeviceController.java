package net.lab1024.sa.access.controller;

import io.micrometer.observation.annotation.Observed;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.access.service.AccessDeviceService;

/**
 * 门禁设备管理PC端控制器
 * <p>
 * 提供PC端门禁设备管理相关API
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
@RequestMapping("/api/v1/access/device")
@Tag(name = "门禁设备管理PC端", description = "设备查询、添加、更新、删除、状态管理等API")
@PermissionCheck(value = "ACCESS_DEVICE", description = "门禁设备管理模块权限")
public class AccessDeviceController {

    @Resource
    private AccessDeviceService accessDeviceService;

    /**
     * 分页查询设备
     * <p>
     * 支持多条件筛选：关键词（设备名称、设备编号）、区域ID、设备状态、启用状态等
     * 支持分页查询，默认每页20条记录
     * </p>
     *
     * @param pageNum 页码（从1开始，默认1）
     * @param pageSize 每页大小（默认20）
     * @param keyword 关键词（设备名称、设备编号，可选）
     * @param areaId 区域ID（可选）
     * @param deviceStatus 设备状态（可选：ONLINE-在线、OFFLINE-离线、FAULT-故障）
     * @param enabledFlag 启用状态（可选：1-启用、0-禁用）
     * @return 分页的设备列表
     * @apiNote 示例请求：
     * <pre>
     * GET /api/v1/access/device/query?pageNum=1&pageSize=20&keyword=门禁&areaId=4001&deviceStatus=ONLINE&enabledFlag=1
     * </pre>
     */
    @Observed(name = "accessDevice.queryDevices", contextualName = "access-device-query")
    @GetMapping("/query")
    @Operation(
        summary = "分页查询设备",
        description = "根据条件分页查询门禁设备，支持多条件筛选（关键词、区域ID、设备状态、启用状态等）。支持分页查询，默认每页20条记录。严格遵循RESTful规范：查询操作使用GET方法。",
        tags = {"门禁设备管理PC端"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PageResult.class)
        )
    )
    @PermissionCheck(value = "ACCESS_DEVICE_VIEW", description = "查询门禁设备")
    public ResponseDTO<PageResult<net.lab1024.sa.access.domain.vo.AccessDeviceVO>> queryDevices(
            @Parameter(description = "页码（从1开始）", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词（设备名称、设备编号）", example = "门禁")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "区域ID", example = "4001")
            @RequestParam(required = false) Long areaId,
            @Parameter(description = "设备状态（ONLINE/OFFLINE/FAULT）", example = "ONLINE")
            @RequestParam(required = false) String deviceStatus,
            @Parameter(description = "启用状态（1-启用、0-禁用）", example = "1")
            @RequestParam(required = false) Integer enabledFlag) {
        log.info("[门禁设备] 分页查询设备，pageNum={}, pageSize={}, keyword={}, areaId={}, deviceStatus={}, enabledFlag={}",
                pageNum, pageSize, keyword, areaId, deviceStatus, enabledFlag);
        try {
            // 构建查询表单
            net.lab1024.sa.access.domain.form.AccessDeviceQueryForm queryForm = new net.lab1024.sa.access.domain.form.AccessDeviceQueryForm();
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);
            queryForm.setKeyword(keyword);
            queryForm.setAreaId(areaId);
            queryForm.setDeviceStatus(deviceStatus);
            queryForm.setEnabledFlag(enabledFlag);

            // 调用Service层查询
            return accessDeviceService.queryDevices(queryForm);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 分页查询设备参数错误: pageNum={}, pageSize={}, error={}", pageNum, pageSize, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[门禁设备] 分页查询设备业务异常: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[门禁设备] 分页查询设备系统异常: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_DEVICES_SYSTEM_ERROR", "查询设备失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[门禁设备] 分页查询设备未知异常: pageNum={}, pageSize={}", pageNum, pageSize, e);
            return ResponseDTO.error("QUERY_DEVICES_ERROR", "查询设备失败: " + e.getMessage());
        }
    }

    /**
     * 查询设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @Observed(name = "accessDevice.getDeviceDetail", contextualName = "access-device-get-detail")
    @GetMapping("/{deviceId}")
    @Operation(summary = "查询设备详情", description = "根据设备ID查询设备详细信息")
    @PermissionCheck(value = "ACCESS_DEVICE_VIEW", description = "查询设备详情")
    public ResponseDTO<net.lab1024.sa.access.domain.vo.AccessDeviceVO> getDeviceDetail(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {
        log.info("[门禁设备] 查询设备详情，deviceId={}", deviceId);
        try {
            // 调用Service层查询
            return accessDeviceService.getDeviceDetail(deviceId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 查询设备详情参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[门禁设备] 查询设备详情业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[门禁设备] 查询设备详情系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_SYSTEM_ERROR", "查询设备详情失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[门禁设备] 查询设备详情未知异常: deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_DEVICE_ERROR", "查询设备详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加设备
     *
     * @param form 设备添加表单
     * @return 设备ID
     */
    @Observed(name = "accessDevice.addDevice", contextualName = "access-device-add")
    @PostMapping("/add")
    @Operation(summary = "添加设备", description = "添加新的门禁设备")
    @PermissionCheck(value = "ACCESS_DEVICE_ADD", description = "添加门禁设备")
    public ResponseDTO<Long> addDevice(@Valid @RequestBody net.lab1024.sa.access.domain.form.AccessDeviceAddForm form) {
        log.info("[门禁设备] 添加设备，deviceName={}, deviceCode={}", form.getDeviceName(), form.getDeviceCode());
        try {
            // 调用Service层添加
            return accessDeviceService.addDevice(form);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 添加设备参数错误: deviceName={}, deviceCode={}, error={}", form.getDeviceName(), form.getDeviceCode(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[门禁设备] 添加设备业务异常: deviceName={}, deviceCode={}, code={}, message={}", form.getDeviceName(), form.getDeviceCode(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[门禁设备] 添加设备系统异常: deviceName={}, deviceCode={}, code={}, message={}", form.getDeviceName(), form.getDeviceCode(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("ADD_DEVICE_SYSTEM_ERROR", "添加设备失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[门禁设备] 添加设备未知异常: deviceName={}, deviceCode={}", form.getDeviceName(), form.getDeviceCode(), e);
            return ResponseDTO.error("ADD_DEVICE_ERROR", "添加设备失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备
     *
     * @param form 设备更新表单
     * @return 是否成功
     */
    @Observed(name = "accessDevice.updateDevice", contextualName = "access-device-update")
    @PutMapping("/update")
    @Operation(summary = "更新设备", description = "更新门禁设备信息")
    @PermissionCheck(value = "ACCESS_DEVICE_UPDATE", description = "更新门禁设备")
    public ResponseDTO<Boolean> updateDevice(@Valid @RequestBody net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm form) {
        log.info("[门禁设备] 更新设备，deviceId={}", form.getDeviceId());
        try {
            // 调用Service层更新
            return accessDeviceService.updateDevice(form);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 更新设备参数错误: deviceId={}, error={}", form.getDeviceId(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[门禁设备] 更新设备业务异常: deviceId={}, code={}, message={}", form.getDeviceId(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[门禁设备] 更新设备系统异常: deviceId={}, code={}, message={}", form.getDeviceId(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_DEVICE_SYSTEM_ERROR", "更新设备失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[门禁设备] 更新设备未知异常: deviceId={}", form.getDeviceId(), e);
            return ResponseDTO.error("UPDATE_DEVICE_ERROR", "更新设备失败: " + e.getMessage());
        }
    }

    /**
     * 删除设备
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    @Observed(name = "accessDevice.deleteDevice", contextualName = "access-device-delete")
    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除设备", description = "删除门禁设备（软删除）")
    @PermissionCheck(value = "ACCESS_DEVICE_DELETE", description = "删除门禁设备")
    public ResponseDTO<Boolean> deleteDevice(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId) {
        log.info("[门禁设备] 删除设备，deviceId={}", deviceId);
        try {
            // 调用Service层删除
            return accessDeviceService.deleteDevice(deviceId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 删除设备参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[门禁设备] 删除设备业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[门禁设备] 删除设备系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("DELETE_DEVICE_SYSTEM_ERROR", "删除设备失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[门禁设备] 删除设备未知异常: deviceId={}", deviceId, e);
            return ResponseDTO.error("DELETE_DEVICE_ERROR", "删除设备失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param status 设备状态
     * @return 是否成功
     */
    @Observed(name = "accessDevice.updateDeviceStatus", contextualName = "access-device-update-status")
    @PutMapping("/{deviceId}/status")
    @Operation(summary = "更新设备状态", description = "更新门禁设备状态")
    @PermissionCheck(value = "ACCESS_DEVICE_UPDATE", description = "更新设备状态")
    public ResponseDTO<Boolean> updateDeviceStatus(
            @Parameter(description = "设备ID") @PathVariable @NotNull Long deviceId,
            @Parameter(description = "设备状态") @RequestParam @NotNull Integer status) {
        log.info("[门禁设备] 更新设备状态，deviceId={}, status={}", deviceId, status);
        try {
            // 调用Service层更新状态
            return accessDeviceService.updateDeviceStatus(deviceId, status);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 更新设备状态参数错误: deviceId={}, status={}, error={}", deviceId, status, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[门禁设备] 更新设备状态业务异常: deviceId={}, status={}, code={}, message={}", deviceId, status, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[门禁设备] 更新设备状态系统异常: deviceId={}, status={}, code={}, message={}", deviceId, status, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_DEVICE_STATUS_SYSTEM_ERROR", "更新设备状态失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[门禁设备] 更新设备状态未知异常: deviceId={}, status={}", deviceId, status, e);
            return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "更新设备状态失败: " + e.getMessage());
        }
    }
}


