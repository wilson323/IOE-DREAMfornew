package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * 门禁设备管理控制器
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 使用公共DeviceEntity（在common-business中）
 * - 只管理门禁设备（deviceType='ACCESS'）
 * </p>
 * <p>
 * 核心职责：
 * - 门禁设备的CRUD操作
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
@Tag(name = "门禁设备管理", description = "门禁设备管理接口，包括设备的增删改查和状态管理")
public class AccessDeviceController {

    @Resource
    private AccessDeviceService accessDeviceService;

    /**
     * 分页查询门禁设备列表
     * <p>
     * 支持多条件查询：关键字、区域ID、设备状态、启用状态
     * </p>
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询门禁设备列表", description = "支持多条件查询：关键字、区域ID、设备状态、启用状态")
    public ResponseDTO<PageResult<AccessDeviceVO>> queryDeviceList(
            @Valid @RequestBody AccessDeviceQueryForm queryForm) {
        log.info("[门禁设备] 分页查询设备列表: pageNum={}, pageSize={}", 
                queryForm.getPageNum(), queryForm.getPageSize());
        
        try {
            return accessDeviceService.queryDeviceList(queryForm);
        } catch (Exception e) {
            log.error("[门禁设备] 分页查询设备列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_DEVICE_LIST_ERROR", "查询设备列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询门禁设备详情
     * <p>
     * 根据设备ID查询设备详细信息，包括区域名称等关联信息
     * </p>
     *
     * @param deviceId 设备ID（String类型，与DeviceEntity保持一致）
     * @return 设备详情
     */
    @GetMapping("/{deviceId}")
    @Operation(summary = "查询门禁设备详情", description = "根据设备ID查询设备详细信息，包括区域名称等关联信息")
    public ResponseDTO<AccessDeviceVO> getDeviceDetail(
            @Parameter(description = "设备ID", required = true)
            @PathVariable String deviceId) {
        log.info("[门禁设备] 查询设备详情: deviceId={}", deviceId);
        
        try {
            return accessDeviceService.getDeviceDetail(deviceId);
        } catch (Exception e) {
            log.error("[门禁设备] 查询设备详情异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_DETAIL_ERROR", "查询设备详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加门禁设备
     * <p>
     * 添加新的门禁设备，自动设置deviceType='ACCESS'
     * </p>
     *
     * @param addForm 添加表单
     * @return 设备ID
     */
    @PostMapping
    @Operation(summary = "添加门禁设备", description = "添加新的门禁设备，自动设置deviceType='ACCESS'")
    public ResponseDTO<String> addDevice(@Valid @RequestBody AccessDeviceAddForm addForm) {
        log.info("[门禁设备] 添加设备: deviceName={}, deviceCode={}", 
                addForm.getDeviceName(), addForm.getDeviceCode());
        
        try {
            return accessDeviceService.addDevice(addForm);
        } catch (Exception e) {
            log.error("[门禁设备] 添加设备异常: deviceCode={}, error={}", 
                    addForm.getDeviceCode(), e.getMessage(), e);
            return ResponseDTO.error("ADD_DEVICE_ERROR", "添加设备失败: " + e.getMessage());
        }
    }

    /**
     * 更新门禁设备
     * <p>
     * 更新设备基本信息，不包括设备类型（固定为ACCESS）
     * </p>
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    @PutMapping
    @Operation(summary = "更新门禁设备", description = "更新设备基本信息，不包括设备类型（固定为ACCESS）")
    public ResponseDTO<Void> updateDevice(@Valid @RequestBody AccessDeviceUpdateForm updateForm) {
        log.info("[门禁设备] 更新设备: deviceId={}, deviceName={}", 
                updateForm.getDeviceId(), updateForm.getDeviceName());
        
        try {
            return accessDeviceService.updateDevice(updateForm);
        } catch (Exception e) {
            log.error("[门禁设备] 更新设备异常: deviceId={}, error={}", 
                    updateForm.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_DEVICE_ERROR", "更新设备失败: " + e.getMessage());
        }
    }

    /**
     * 删除门禁设备
     * <p>
     * 逻辑删除设备（设置deleted_flag=1）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除门禁设备", description = "逻辑删除设备（设置deleted_flag=1）")
    public ResponseDTO<Void> deleteDevice(
            @Parameter(description = "设备ID", required = true)
            @PathVariable String deviceId) {
        log.info("[门禁设备] 删除设备: deviceId={}", deviceId);
        
        try {
            return accessDeviceService.deleteDevice(deviceId);
        } catch (Exception e) {
            log.error("[门禁设备] 删除设备异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("DELETE_DEVICE_ERROR", "删除设备失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备状态
     * <p>
     * 更新设备的启用/禁用状态
     * </p>
     *
     * @param deviceId 设备ID
     * @param enabled 启用状态（0-禁用，1-启用）
     * @return 操作结果
     */
    @PutMapping("/{deviceId}/status")
    @Operation(summary = "更新设备状态", description = "更新设备的启用/禁用状态")
    public ResponseDTO<Void> updateDeviceStatus(
            @Parameter(description = "设备ID", required = true)
            @PathVariable String deviceId,
            @Parameter(description = "启用状态（0-禁用，1-启用）", required = true)
            @RequestParam Integer enabled) {
        log.info("[门禁设备] 更新设备状态: deviceId={}, enabled={}", deviceId, enabled);
        
        try {
            return accessDeviceService.updateDeviceStatus(deviceId, enabled);
        } catch (Exception e) {
            log.error("[门禁设备] 更新设备状态异常: deviceId={}, enabled={}, error={}", 
                    deviceId, enabled, e.getMessage(), e);
            return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "更新设备状态失败: " + e.getMessage());
        }
    }

    /**
     * 统计门禁设备数量
     * <p>
     * 统计系统中的门禁设备总数
     * </p>
     *
     * @return 设备总数
     */
    @GetMapping("/count")
    @Operation(summary = "统计门禁设备数量", description = "统计系统中的门禁设备总数")
    public ResponseDTO<Long> countDevices() {
        log.info("[门禁设备] 统计设备数量");
        
        try {
            return accessDeviceService.countDevices();
        } catch (Exception e) {
            log.error("[门禁设备] 统计设备数量异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("COUNT_DEVICES_ERROR", "统计设备数量失败: " + e.getMessage());
        }
    }

    /**
     * 统计在线门禁设备数量
     * <p>
     * 统计当前在线的门禁设备数量
     * </p>
     *
     * @return 在线设备数量
     */
    @GetMapping("/count/online")
    @Operation(summary = "统计在线门禁设备数量", description = "统计当前在线的门禁设备数量")
    public ResponseDTO<Long> countOnlineDevices() {
        log.info("[门禁设备] 统计在线设备数量");
        
        try {
            return accessDeviceService.countOnlineDevices();
        } catch (Exception e) {
            log.error("[门禁设备] 统计在线设备数量异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("COUNT_ONLINE_DEVICES_ERROR", "统计在线设备数量失败: " + e.getMessage());
        }
    }
}
