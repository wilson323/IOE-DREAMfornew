package net.lab1024.sa.device.comm.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.service.VendorSupportService;
import net.lab1024.sa.device.comm.vendor.DeviceVendorSupportManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 厂商设备支持服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VendorSupportServiceImpl implements VendorSupportService {

    @Resource
    private DeviceVendorSupportManager deviceVendorSupportManager;

    @Override
    public List<DeviceVendorSupportManager.VendorInfo> getAllSupportedVendors() {
        log.info("[厂商支持服务] 查询所有支持的厂商信息");
        return deviceVendorSupportManager.getAllSupportedVendors();
    }

    @Override
    public DeviceVendorSupportManager.VendorInfo getVendorInfo(String vendorName) {
        log.info("[厂商支持服务] 查询厂商信息: vendorName={}", vendorName);
        return deviceVendorSupportManager.getVendorInfo(vendorName);
    }

    @Override
    public List<DeviceVendorSupportManager.DeviceInfo> getVendorDevices(String vendorName) {
        log.info("[厂商支持服务] 查询厂商设备: vendorName={}", vendorName);
        return deviceVendorSupportManager.getVendorDevices(vendorName);
    }

    @Override
    public List<DeviceVendorSupportManager.DeviceInfo> getDevicesByType(String deviceType) {
        log.info("[厂商支持服务] 根据设备类型查询: deviceType={}", deviceType);
        return deviceVendorSupportManager.getDevicesByType(deviceType);
    }

    @Override
    public boolean isVendorSupported(String vendorName) {
        log.debug("[厂商支持服务] 检查厂商是否支持: vendorName={}", vendorName);
        return deviceVendorSupportManager.isVendorSupported(vendorName);
    }

    @Override
    public boolean isDeviceModelSupported(String deviceModel) {
        log.debug("[厂商支持服务] 检查设备型号是否支持: deviceModel={}", deviceModel);
        return deviceVendorSupportManager.isDeviceModelSupported(deviceModel);
    }

    @Override
    public DeviceVendorSupportManager.VendorSupportStatistics getSupportStatistics() {
        log.info("[厂商支持服务] 获取厂商支持统计");
        return deviceVendorSupportManager.getSupportStatistics();
    }

    @Override
    public DeviceVendorSupportManager.CompatibilityReport getCompatibilityReport() {
        log.info("[厂商支持服务] 获取厂商兼容性报告");
        return deviceVendorSupportManager.getCompatibilityReport();
    }

    @Override
    public boolean reloadVendorSupport() {
        log.info("[厂商支持服务] 重新加载厂商支持");
        return deviceVendorSupportManager.reloadVendorSupport();
    }

    @Override
    public Map<String, Boolean> batchRegisterVendorDevices(Map<String, List<DeviceVendorSupportManager.DeviceInfo>> vendorDevices) {
        log.info("[厂商支持服务] 批量注册厂商设备: 厂商数={}", vendorDevices.size());
        return deviceVendorSupportManager.batchRegisterVendorDevices(vendorDevices);
    }

    @Override
    public boolean removeVendorDevice(String vendorName, String deviceModel) {
        log.info("[厂商支持服务] 移除厂商设备: vendorName={}, deviceModel={}", vendorName, deviceModel);
        return deviceVendorSupportManager.removeVendorDevice(vendorName, deviceModel);
    }
}
