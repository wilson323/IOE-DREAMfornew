package net.lab1024.sa.device.comm.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.device.comm.vendor.DeviceVendorSupportManager;

import java.util.List;
import java.util.Map;

/**
 * 厂商设备支持服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service层负责业务逻辑和事务管理
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VendorSupportService {

    /**
     * 获取所有支持的厂商信息
     *
     * @return 厂商信息列表
     */
    List<DeviceVendorSupportManager.VendorInfo> getAllSupportedVendors();

    /**
     * 根据厂商名称获取厂商信息
     *
     * @param vendorName 厂商名称
     * @return 厂商信息
     */
    DeviceVendorSupportManager.VendorInfo getVendorInfo(String vendorName);

    /**
     * 获取指定厂商的所有设备
     *
     * @param vendorName 厂商名称
     * @return 设备信息列表
     */
    List<DeviceVendorSupportManager.DeviceInfo> getVendorDevices(String vendorName);

    /**
     * 根据设备类型获取所有支持的设备
     *
     * @param deviceType 设备类型
     * @return 设备信息列表
     */
    List<DeviceVendorSupportManager.DeviceInfo> getDevicesByType(String deviceType);

    /**
     * 检查厂商是否支持
     *
     * @param vendorName 厂商名称
     * @return 是否支持
     */
    boolean isVendorSupported(String vendorName);

    /**
     * 检查设备型号是否支持
     *
     * @param deviceModel 设备型号
     * @return 是否支持
     */
    boolean isDeviceModelSupported(String deviceModel);

    /**
     * 获取厂商支持统计
     *
     * @return 支持统计信息
     */
    DeviceVendorSupportManager.VendorSupportStatistics getSupportStatistics();

    /**
     * 获取厂商兼容性报告
     *
     * @return 兼容性报告
     */
    DeviceVendorSupportManager.CompatibilityReport getCompatibilityReport();

    /**
     * 重新加载厂商支持
     *
     * @return 操作结果
     */
    boolean reloadVendorSupport();

    /**
     * 批量注册厂商设备
     *
     * @param vendorDevices 厂商设备映射
     * @return 注册结果
     */
    Map<String, Boolean> batchRegisterVendorDevices(Map<String, List<DeviceVendorSupportManager.DeviceInfo>> vendorDevices);

    /**
     * 移除厂商设备
     *
     * @param vendorName 厂商名称
     * @param deviceModel 设备型号
     * @return 操作结果
     */
    boolean removeVendorDevice(String vendorName, String deviceModel);
}
