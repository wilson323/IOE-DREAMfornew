package net.lab1024.sa.common.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
// 已移除Manager层导入，Controller应只与Service层交互
import net.lab1024.sa.common.organization.service.AreaDeviceService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 区域设备关联管理控制器
 * 提供区域与设备的双向关联管理API接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/organization/area-device")
@RequiredArgsConstructor
@Tag(name = "区域设备关联管理", description = "区域设备关联管理相关接口")
@Validated
public class AreaDeviceController {

    private final AreaDeviceService areaDeviceService;

    @PostMapping("/add")
    @Operation(summary = "添加设备到区域", description = "将设备添加到指定区域，建立关联关系")
    public ResponseDTO<String> addDeviceToArea(@Valid @RequestBody AreaDeviceAddRequest request) {
        return ResponseDTO.ok(areaDeviceService.addDeviceToArea(
                request.getAreaId(),
                request.getDeviceId(),
                request.getDeviceCode(),
                request.getDeviceName(),
                request.getDeviceType(),
                request.getDeviceSubType(),
                request.getBusinessModule(),
                request.getPriority()
        ));
    }

    @DeleteMapping("/remove")
    @Operation(summary = "从区域移除设备", description = "将设备从指定区域中移除")
    public ResponseDTO<Boolean> removeDeviceFromArea(
            @Parameter(description = "区域ID", required = true)
            @RequestParam @NotNull Long areaId,
            @Parameter(description = "设备ID", required = true)
            @RequestParam @NotBlank String deviceId) {
        return ResponseDTO.ok(areaDeviceService.removeDeviceFromArea(areaId, deviceId));
    }

    @PutMapping("/move")
    @Operation(summary = "移动设备到新区域", description = "将设备从一个区域移动到另一个区域")
    public ResponseDTO<Boolean> moveDeviceToArea(
            @Parameter(description = "设备ID", required = true)
            @RequestParam @NotBlank String deviceId,
            @Parameter(description = "原区域ID", required = true)
            @RequestParam @NotNull Long oldAreaId,
            @Parameter(description = "新区域ID", required = true)
            @RequestParam @NotNull Long newAreaId) {
        return ResponseDTO.ok(areaDeviceService.moveDeviceToArea(deviceId, oldAreaId, newAreaId));
    }

    @PostMapping("/batch-add")
    @Operation(summary = "批量添加设备到区域", description = "批量将多个设备添加到指定区域")
    public ResponseDTO<Integer> batchAddDevicesToArea(@RequestBody @Valid AreaDeviceBatchAddRequest request) {
        return ResponseDTO.ok(areaDeviceService.batchAddDevicesToArea(request.getAreaId(), request.getDeviceRequests()));
    }

    @PutMapping("/attributes")
    @Operation(summary = "设置设备业务属性", description = "为指定区域中的设备设置业务属性")
    public ResponseDTO<Boolean> setDeviceBusinessAttributes(
            @Parameter(description = "设备ID", required = true)
            @RequestParam @NotBlank String deviceId,
            @Parameter(description = "区域ID", required = true)
            @RequestParam @NotNull Long areaId,
            @RequestBody Map<String, Object> attributes) {
        return ResponseDTO.ok(areaDeviceService.setDeviceBusinessAttributes(deviceId, areaId, attributes));
    }

    @GetMapping("/attributes")
    @Operation(summary = "获取设备业务属性", description = "获取指定区域中设备的业务属性")
    public ResponseDTO<Map<String, Object>> getDeviceBusinessAttributes(
            @Parameter(description = "设备ID", required = true)
            @RequestParam @NotBlank String deviceId,
            @Parameter(description = "区域ID", required = true)
            @RequestParam @NotNull Long areaId) {
        return ResponseDTO.ok(areaDeviceService.getDeviceBusinessAttributes(deviceId, areaId));
    }

    @PutMapping("/status/{relationId}")
    @Operation(summary = "更新设备关联状态", description = "更新指定设备关联的状态")
    public ResponseDTO<Boolean> updateDeviceRelationStatus(
            @Parameter(description = "关联ID", required = true)
            @PathVariable @NotBlank String relationId,
            @Parameter(description = "新状态", required = true)
            @RequestParam @NotNull Integer status) {
        return ResponseDTO.ok(areaDeviceService.updateDeviceRelationStatus(relationId, status));
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量更新区域设备状态", description = "批量更新指定区域中所有设备的状态")
    public ResponseDTO<Integer> batchUpdateAreaDeviceStatus(
            @Parameter(description = "区域ID", required = true)
            @RequestParam @NotNull Long areaId,
            @Parameter(description = "新状态", required = true)
            @RequestParam @NotNull Integer status) {
        return ResponseDTO.ok(areaDeviceService.batchUpdateAreaDeviceStatus(areaId, status));
    }

    @GetMapping("/check")
    @Operation(summary = "检查设备是否在区域中", description = "检查指定设备是否在指定区域中")
    public ResponseDTO<Boolean> isDeviceInArea(
            @Parameter(description = "区域ID", required = true)
            @RequestParam @NotNull Long areaId,
            @Parameter(description = "设备ID", required = true)
            @RequestParam @NotBlank String deviceId) {
        return ResponseDTO.ok(areaDeviceService.isDeviceInArea(areaId, deviceId));
    }

    @GetMapping("/area/{areaId}")
    @Operation(summary = "获取区域的所有设备", description = "获取指定区域中的所有设备关联")
    public ResponseDTO<List<AreaDeviceEntity>> getAreaDevices(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(areaDeviceService.getAreaDevices(areaId));
    }

    @GetMapping("/area/{areaId}/type/{deviceType}")
    @Operation(summary = "获取区域指定类型的设备", description = "获取指定区域中指定类型的所有设备")
    public ResponseDTO<List<AreaDeviceEntity>> getAreaDevicesByType(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId,
            @Parameter(description = "设备类型", required = true)
            @PathVariable @NotNull Integer deviceType) {
        return ResponseDTO.ok(areaDeviceService.getAreaDevicesByType(areaId, deviceType));
    }

    @GetMapping("/area/{areaId}/module/{businessModule}")
    @Operation(summary = "获取区域指定业务模块的设备", description = "获取指定区域中指定业务模块的所有设备")
    public ResponseDTO<List<AreaDeviceEntity>> getAreaDevicesByModule(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId,
            @Parameter(description = "业务模块", required = true)
            @PathVariable @NotBlank String businessModule) {
        return ResponseDTO.ok(areaDeviceService.getAreaDevicesByModule(areaId, businessModule));
    }

    @GetMapping("/area/{areaId}/primary")
    @Operation(summary = "获取区域的主设备", description = "获取指定区域中的所有主设备")
    public ResponseDTO<List<AreaDeviceEntity>> getAreaPrimaryDevices(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(areaDeviceService.getAreaPrimaryDevices(areaId));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "获取设备所属的所有区域", description = "获取指定设备所属的所有区域关联")
    public ResponseDTO<List<AreaDeviceEntity>> getDeviceAreas(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotBlank String deviceId) {
        return ResponseDTO.ok(areaDeviceService.getDeviceAreas(deviceId));
    }

    @GetMapping("/device/code/{deviceCode}")
    @Operation(summary = "根据设备编码获取所属区域", description = "根据设备编码查找设备所属的区域")
    public ResponseDTO<List<AreaDeviceEntity>> getDeviceAreasByCode(
            @Parameter(description = "设备编码", required = true)
            @PathVariable @NotBlank String deviceCode) {
        return ResponseDTO.ok(areaDeviceService.getDeviceAreasByCode(deviceCode));
    }

    @GetMapping("/statistics/{areaId}")
    @Operation(summary = "获取区域设备统计信息", description = "获取指定区域中设备的详细统计信息")
    public ResponseDTO<AreaDeviceStatisticsVO> getAreaDeviceStatistics(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(areaDeviceService.getAreaDeviceStatistics(areaId));
    }

    @GetMapping("/health/{areaId}")
    @Operation(summary = "获取区域设备健康状态统计", description = "获取指定区域中设备的健康状态分布")
    public ResponseDTO<AreaDeviceHealthStatistics> getAreaDeviceHealthStatistics(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(areaDeviceService.getAreaDeviceHealthStatistics(areaId));
    }

    @GetMapping("/distribution/{businessModule}")
    @Operation(summary = "获取业务模块设备分布", description = "获取指定业务模块在各区域的设备分布统计")
    public ResponseDTO<List<ModuleDeviceDistributionVO>> getModuleDeviceDistribution(
            @Parameter(description = "业务模块", required = true)
            @PathVariable @NotBlank String businessModule) {
        return ResponseDTO.ok(areaDeviceService.getModuleDeviceDistribution(businessModule));
    }

    @GetMapping("/distribution/all")
    @Operation(summary = "获取所有模块设备分布统计", description = "获取所有业务模块的设备分布统计")
    public ResponseDTO<Map<String, List<ModuleDeviceDistributionVO>>> getAllModuleDeviceDistribution() {
        return ResponseDTO.ok(areaDeviceService.getAllModuleDeviceDistribution());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户有权限访问的设备", description = "获取指定用户有权限访问的指定业务模块设备")
    public ResponseDTO<List<AreaDeviceEntity>> getUserAccessibleDevices(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId,
            @Parameter(description = "业务模块", required = true)
            @RequestParam @NotBlank String businessModule) {
        return ResponseDTO.ok(areaDeviceService.getUserAccessibleDevices(userId, businessModule));
    }

    @GetMapping("/online/{areaId}")
    @Operation(summary = "获取区域的在线设备数量", description = "获取指定区域中在线设备的数量")
    public ResponseDTO<Integer> getAreaOnlineDeviceCount(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(areaDeviceService.getAreaOnlineDeviceCount(areaId));
    }

    @PutMapping("/expire/{deviceId}")
    @Operation(summary = "设置设备为过期状态", description = "将指定设备的所有关联设置为过期状态")
    public ResponseDTO<Integer> expireDeviceRelations(
            @Parameter(description = "设备ID", required = true)
            @PathVariable @NotBlank String deviceId) {
        return ResponseDTO.ok(areaDeviceService.expireDeviceRelations(deviceId));
    }

    @PostMapping("/batch-move")
    @Operation(summary = "批量移动设备到新区域", description = "批量将多个设备移动到新的区域")
    public ResponseDTO<Integer> batchMoveDevices(@RequestBody @Valid List<DeviceMoveRequest> moveRequests) {
        return ResponseDTO.ok(areaDeviceService.batchMoveDevices(moveRequests));
    }

    // ================ 请求DTO ================

    /**
     * 区域设备添加请求
     */
    public static class AreaDeviceAddRequest {
        @NotNull(message = "区域ID不能为空")
        private Long areaId;

        @NotBlank(message = "设备ID不能为空")
        private String deviceId;

        @NotBlank(message = "设备编码不能为空")
        private String deviceCode;

        @NotBlank(message = "设备名称不能为空")
        private String deviceName;

        @NotNull(message = "设备类型不能为空")
        private Integer deviceType;

        private Integer deviceSubType;

        @NotBlank(message = "业务模块不能为空")
        private String businessModule;

        private Integer priority;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceCode() { return deviceCode; }
        public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public Integer getDeviceType() { return deviceType; }
        public void setDeviceType(Integer deviceType) { this.deviceType = deviceType; }
        public Integer getDeviceSubType() { return deviceSubType; }
        public void setDeviceSubType(Integer deviceSubType) { this.deviceSubType = deviceSubType; }
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
    }

    /**
     * 设备移动请求
     */
    public static class DeviceMoveRequest {
        @NotBlank(message = "设备ID不能为空")
        private String deviceId;

        @NotNull(message = "原区域ID不能为空")
        private Long oldAreaId;

        @NotNull(message = "新区域ID不能为空")
        private Long newAreaId;

        // getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public Long getOldAreaId() { return oldAreaId; }
        public void setOldAreaId(Long oldAreaId) { this.oldAreaId = oldAreaId; }
        public Long getNewAreaId() { return newAreaId; }
        public void setNewAreaId(Long newAreaId) { this.newAreaId = newAreaId; }
    }

    /**
     * 区域设备批量添加请求
     */
    public static class AreaDeviceBatchAddRequest {
        @NotNull(message = "区域ID不能为空")
        private Long areaId;

        @Valid
        private List<DeviceRequest> deviceRequests;

        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public List<DeviceRequest> getDeviceRequests() { return deviceRequests; }
        public void setDeviceRequests(List<DeviceRequest> deviceRequests) { this.deviceRequests = deviceRequests; }
    }

    /**
     * 设备请求
     */
    public static class DeviceRequest {
        @NotBlank(message = "设备ID不能为空")
        private String deviceId;

        @NotBlank(message = "设备编码不能为空")
        private String deviceCode;

        @NotBlank(message = "设备名称不能为空")
        private String deviceName;

        @NotNull(message = "设备类型不能为空")
        private Integer deviceType;

        private Integer deviceSubType;

        @NotBlank(message = "业务模块不能为空")
        private String businessModule;

        private Integer priority;

        // getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceCode() { return deviceCode; }
        public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public Integer getDeviceType() { return deviceType; }
        public void setDeviceType(Integer deviceType) { this.deviceType = deviceType; }
        public Integer getDeviceSubType() { return deviceSubType; }
        public void setDeviceSubType(Integer deviceSubType) { this.deviceSubType = deviceSubType; }
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
    }

    /**
     * 区域设备统计VO
     */
    public static class AreaDeviceStatisticsVO {
        private Long areaId;
        private Integer totalDevices;
        private Integer onlineDevices;
        private Integer offlineDevices;
        private Map<Integer, Integer> deviceTypeDistribution;
        private Map<String, Integer> moduleDistribution;

        // getters and setters
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public Integer getTotalDevices() { return totalDevices; }
        public void setTotalDevices(Integer totalDevices) { this.totalDevices = totalDevices; }
        public Integer getOnlineDevices() { return onlineDevices; }
        public void setOnlineDevices(Integer onlineDevices) { this.onlineDevices = onlineDevices; }
        public Integer getOfflineDevices() { return offlineDevices; }
        public void setOfflineDevices(Integer offlineDevices) { this.offlineDevices = offlineDevices; }
        public Map<Integer, Integer> getDeviceTypeDistribution() { return deviceTypeDistribution; }
        public void setDeviceTypeDistribution(Map<Integer, Integer> deviceTypeDistribution) { this.deviceTypeDistribution = deviceTypeDistribution; }
        public Map<String, Integer> getModuleDistribution() { return moduleDistribution; }
        public void setModuleDistribution(Map<String, Integer> moduleDistribution) { this.moduleDistribution = moduleDistribution; }
    }

    /**
     * 模块设备分布VO
     */
    public static class ModuleDeviceDistributionVO {
        private String businessModule;
        private String moduleName;
        private Long areaId;
        private String areaName;
        private Integer deviceCount;
        private Double percentage;

        // getters and setters
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        public String getModuleName() { return moduleName; }
        public void setModuleName(String moduleName) { this.moduleName = moduleName; }
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public Integer getDeviceCount() { return deviceCount; }
        public void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }
}
