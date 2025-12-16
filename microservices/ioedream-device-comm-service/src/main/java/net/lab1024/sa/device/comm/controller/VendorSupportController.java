package net.lab1024.sa.device.comm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.common.util.RequestUtils;
import net.lab1024.sa.device.comm.vendor.DeviceVendorSupportManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 厂商设备支持控制器
 * <p>
 * 提供厂商设备支持的管理和查询接口：
 * 1. 查询所有支持的厂商信息
 * 2. 查询指定厂商的设备列表
 * 3. 查询指定类型的设备列表
 * 4. 检查厂商和设备型号支持
 * 5. 获取厂商支持统计
 * 6. 获取兼容性报告
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/vendor-support")
@Tag(name = "厂商设备支持", description = "厂商设备支持管理和查询接口")
public class VendorSupportController {

    @Resource
    private DeviceVendorSupportManager deviceVendorSupportManager;

    /**
     * 获取所有支持的厂商信息
     *
     * @return 厂商信息列表
     */
    @GetMapping("/vendors")
    @Operation(summary = "获取所有支持的厂商信息", description = "返回所有支持的厂商详细信息")
    public ResponseDTO<List<DeviceVendorSupportManager.VendorInfo>> getAllSupportedVendors() {
        try {
            log.info("[厂商支持管理] 查询所有支持的厂商信息, 操作人: {}", RequestUtils.getUserId());

            List<DeviceVendorSupportManager.VendorInfo> vendors = deviceVendorSupportManager.getAllSupportedVendors();

            log.info("[厂商支持管理] 查询成功，返回 {} 个厂商信息", vendors.size());
            return ResponseDTO.ok(vendors);

        } catch (Exception e) {
            log.error("[厂商支持管理] 查询所有支持的厂商信息失败", e);
            return ResponseDTO.error("QUERY_VENDORS_ERROR", "查询厂商信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据厂商名称获取厂商信息
     *
     * @param vendorName 厂商名称
     * @return 厂商信息
     */
    @GetMapping("/vendors/{vendorName}")
    @Operation(summary = "获取厂商信息", description = "根据厂商名称获取详细信息")
    public ResponseDTO<DeviceVendorSupportManager.VendorInfo> getVendorInfo(
            @Parameter(description = "厂商名称", required = true, example = "海康威视")
            @PathVariable String vendorName) {
        try {
            log.info("[厂商支持管理] 查询厂商信息: {}, 操作人: {}", vendorName, RequestUtils.getUserId());

            DeviceVendorSupportManager.VendorInfo vendorInfo = deviceVendorSupportManager.getVendorInfo(vendorName);

            if (vendorInfo == null) {
                log.warn("[厂商支持管理] 厂商不存在: {}", vendorName);
                return ResponseDTO.error("VENDOR_NOT_FOUND", "厂商不存在: " + vendorName);
            }

            log.info("[厂商支持管理] 查询厂商信息成功: {}", vendorName);
            return ResponseDTO.ok(vendorInfo);

        } catch (Exception e) {
            log.error("[厂商支持管理] 查询厂商信息失败: {}", vendorName, e);
            return ResponseDTO.error("QUERY_VENDOR_ERROR", "查询厂商信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定厂商的所有设备
     *
     * @param vendorName 厂商名称
     * @return 设备信息列表
     */
    @GetMapping("/vendors/{vendorName}/devices")
    @Operation(summary = "获取厂商设备列表", description = "获取指定厂商的所有设备信息")
    public ResponseDTO<List<DeviceVendorSupportManager.DeviceInfo>> getVendorDevices(
            @Parameter(description = "厂商名称", required = true, example = "海康威视")
            @PathVariable String vendorName) {
        try {
            log.info("[厂商支持管理] 查询厂商设备列表: {}, 操作人: {}", vendorName, RequestUtils.getUserId());

            List<DeviceVendorSupportManager.DeviceInfo> devices = deviceVendorSupportManager.getVendorDevices(vendorName);

            log.info("[厂商支持管理] 查询厂商设备列表成功: {}, 返回 {} 个设备", vendorName, devices.size());
            return ResponseDTO.ok(devices);

        } catch (Exception e) {
            log.error("[厂商支持管理] 查询厂商设备列表失败: {}", vendorName, e);
            return ResponseDTO.error("QUERY_VENDOR_DEVICES_ERROR", "查询厂商设备列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据设备类型获取所有支持的设备
     *
     * @param deviceType 设备类型
     * @return 设备信息列表
     */
    @GetMapping("/devices")
    @Operation(summary = "根据设备类型查询设备", description = "获取指定类型的所有设备信息")
    public ResponseDTO<List<DeviceVendorSupportManager.DeviceInfo>> getDevicesByType(
            @Parameter(description = "设备类型", example = "视频监控")
            @RequestParam String deviceType) {
        try {
            log.info("[厂商支持管理] 查询设备类型: {}, 操作人: {}", deviceType, RequestUtils.getUserId());

            List<DeviceVendorSupportManager.DeviceInfo> devices = deviceVendorSupportManager.getDevicesByType(deviceType);

            log.info("[厂商支持管理] 查询设备类型成功: {}, 返回 {} 个设备", deviceType, devices.size());
            return ResponseDTO.ok(devices);

        } catch (Exception e) {
            log.error("[厂商支持管理] 查询设备类型失败: {}", deviceType, e);
            return ResponseDTO.error("QUERY_DEVICE_TYPE_ERROR", "查询设备类型失败: " + e.getMessage());
        }
    }

    /**
     * 检查厂商是否支持
     *
     * @param vendorName 厂商名称
     * @return 是否支持
     */
    @GetMapping("/vendors/{vendorName}/support")
    @Operation(summary = "检查厂商支持", description = "检查指定厂商是否被支持")
    public ResponseDTO<Boolean> checkVendorSupport(
            @Parameter(description = "厂商名称", required = true, example = "海康威视")
            @PathVariable String vendorName) {
        try {
            log.info("[厂商支持管理] 检查厂商支持: {}, 操作人: {}", vendorName, RequestUtils.getUserId());

            boolean supported = deviceVendorSupportManager.isVendorSupported(vendorName);

            log.info("[厂商支持管理] 检查厂商支持完成: {}, 结果: {}", vendorName, supported);
            return ResponseDTO.ok(supported);

        } catch (Exception e) {
            log.error("[厂商支持管理] 检查厂商支持失败: {}", vendorName, e);
            return ResponseDTO.error("CHECK_VENDOR_SUPPORT_ERROR", "检查厂商支持失败: " + e.getMessage());
        }
    }

    /**
     * 检查设备型号是否支持
     *
     * @param deviceModel 设备型号
     * @return 是否支持
     */
    @GetMapping("/devices/{deviceModel}/support")
    @Operation(summary = "检查设备型号支持", description = "检查指定设备型号是否被支持")
    public ResponseDTO<Boolean> checkDeviceModelSupport(
            @Parameter(description = "设备型号", required = true, example = "DS-2CD2032-I")
            @PathVariable String deviceModel) {
        try {
            log.info("[厂商支持管理] 检查设备型号支持: {}, 操作人: {}", deviceModel, RequestUtils.getUserId());

            boolean supported = deviceVendorSupportManager.isDeviceModelSupported(deviceModel);

            log.info("[厂商支持管理] 检查设备型号支持完成: {}, 结果: {}", deviceModel, supported);
            return ResponseDTO.ok(supported);

        } catch (Exception e) {
            log.error("[厂商支持管理] 检查设备型号支持失败: {}", deviceModel, e);
            return ResponseDTO.error("CHECK_DEVICE_SUPPORT_ERROR", "检查设备型号支持失败: " + e.getMessage());
        }
    }

    /**
     * 获取厂商支持统计
     *
     * @return 支持统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取厂商支持统计", description = "获取厂商设备支持的详细统计信息")
    public ResponseDTO<DeviceVendorSupportManager.VendorSupportStatistics> getSupportStatistics() {
        try {
            log.info("[厂商支持管理] 查询厂商支持统计, 操作人: {}", RequestUtils.getUserId());

            DeviceVendorSupportManager.VendorSupportStatistics statistics = deviceVendorSupportManager.getSupportStatistics();

            log.info("[厂商支持管理] 查询厂商支持统计成功");
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[厂商支持管理] 查询厂商支持统计失败", e);
            return ResponseDTO.error("QUERY_STATISTICS_ERROR", "查询厂商支持统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取厂商兼容性报告
     *
     * @return 兼容性报告
     */
    @GetMapping("/compatibility-report")
    @Operation(summary = "获取兼容性报告", description = "获取所有厂商设备的兼容性分析报告")
    public ResponseDTO<DeviceVendorSupportManager.CompatibilityReport> getCompatibilityReport() {
        try {
            log.info("[厂商支持管理] 查询兼容性报告, 操作人: {}", RequestUtils.getUserId());

            DeviceVendorSupportManager.CompatibilityReport report = deviceVendorSupportManager.getCompatibilityReport();

            log.info("[厂商支持管理] 查询兼容性报告成功, 总体评分: {}, 等级: {}",
                    report.getOverallCompatibilityScore(), report.getCompatibilityGrade());
            return ResponseDTO.ok(report);

        } catch (Exception e) {
            log.error("[厂商支持管理] 查询兼容性报告失败", e);
            return ResponseDTO.error("QUERY_COMPATIBILITY_REPORT_ERROR", "查询兼容性报告失败: " + e.getMessage());
        }
    }

    /**
     * 重新加载厂商支持
     *
     * @return 操作结果
     */
    @PostMapping("/reload")
    @Operation(summary = "重新加载厂商支持", description = "重新加载所有厂商设备支持信息")
    public ResponseDTO<Void> reloadVendorSupport() {
        try {
            log.info("[厂商支持管理] 重新加载厂商支持, 操作人: {}", RequestUtils.getUserId());

            boolean success = deviceVendorSupportManager.reloadVendorSupport();

            if (success) {
                log.info("[厂商支持管理] 重新加载厂商支持成功");
                return ResponseDTO.ok();
            } else {
                log.warn("[厂商支持管理] 重新加载厂商支持失败");
                return ResponseDTO.error("RELOAD_VENDOR_SUPPORT_ERROR", "重新加载厂商支持失败");
            }

        } catch (Exception e) {
            log.error("[厂商支持管理] 重新加载厂商支持失败", e);
            return ResponseDTO.error("RELOAD_VENDOR_SUPPORT_ERROR", "重新加载厂商支持失败: " + e.getMessage());
        }
    }

    /**
     * 批量注册厂商设备
     *
     * @param vendorDevices 厂商设备映射
     * @return 注册结果
     */
    @PostMapping("/batch-register")
    @Operation(summary = "批量注册厂商设备", description = "批量注册新厂商和设备信息")
    public ResponseDTO<Map<String, Boolean>> batchRegisterVendorDevices(
            @Valid @RequestBody Map<String, List<DeviceVendorSupportManager.DeviceInfo>> vendorDevices) {
        try {
            log.info("[厂商支持管理] 批量注册厂商设备, 厂商数: {}, 操作人: {}",
                    vendorDevices.size(), RequestUtils.getUserId());

            Map<String, Boolean> results = deviceVendorSupportManager.batchRegisterVendorDevices(vendorDevices);

            // 统计成功和失败的数量
            long successCount = results.values().stream().mapToLong(success -> success ? 1 : 0).sum();
            long failureCount = results.size() - successCount;

            log.info("[厂商支持管理] 批量注册厂商设备完成, 成功: {}, 失败: {}", successCount, failureCount);
            return ResponseDTO.ok(results);

        } catch (Exception e) {
            log.error("[厂商支持管理] 批量注册厂商设备失败", e);
            return ResponseDTO.error("BATCH_REGISTER_ERROR", "批量注册厂商设备失败: " + e.getMessage());
        }
    }

    /**
     * 移除厂商设备
     *
     * @param vendorName 厂商名称
     * @param deviceModel 设备型号
     * @return 操作结果
     */
    @DeleteMapping("/vendors/{vendorName}/devices/{deviceModel}")
    @Operation(summary = "移除厂商设备", description = "移除指定厂商的设备型号支持")
    public ResponseDTO<Void> removeVendorDevice(
            @Parameter(description = "厂商名称", required = true, example = "海康威视")
            @PathVariable String vendorName,
            @Parameter(description = "设备型号", required = true, example = "DS-2CD2032-I")
            @PathVariable String deviceModel) {
        try {
            log.info("[厂商支持管理] 移除厂商设备: {} - {}, 操作人: {}",
                    vendorName, deviceModel, RequestUtils.getUserId());

            boolean success = deviceVendorSupportManager.removeVendorDevice(vendorName, deviceModel);

            if (success) {
                log.info("[厂商支持管理] 移除厂商设备成功: {} - {}", vendorName, deviceModel);
                return ResponseDTO.ok();
            } else {
                log.warn("[厂商支持管理] 移除厂商设备失败: {} - {}", vendorName, deviceModel);
                return ResponseDTO.error("REMOVE_VENDOR_DEVICE_ERROR", "移除厂商设备失败");
            }

        } catch (Exception e) {
            log.error("[厂商支持管理] 移除厂商设备失败: {} - {}", vendorName, deviceModel, e);
            return ResponseDTO.error("REMOVE_VENDOR_DEVICE_ERROR", "移除厂商设备失败: " + e.getMessage());
        }
    }
}