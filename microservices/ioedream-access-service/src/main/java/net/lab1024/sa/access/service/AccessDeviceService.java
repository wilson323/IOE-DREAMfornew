package net.lab1024.sa.access.service;

import java.util.List;

import net.lab1024.sa.access.controller.AccessMobileController.MobileAreaItem;
import net.lab1024.sa.access.controller.AccessMobileController.MobileDeviceItem;
import net.lab1024.sa.access.controller.AccessMobileController.MobileRealTimeStatus;
import net.lab1024.sa.access.controller.AccessMobileController.MobileUserPermissions;
import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.form.DeviceControlRequest;
import net.lab1024.sa.access.domain.form.AddDeviceRequest;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.access.domain.vo.MobileDeviceVO;
import net.lab1024.sa.access.domain.vo.DeviceControlResultVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 门禁设备服务接口
 * <p>
 * 提供门禁设备管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回ResponseDTO统一格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessDeviceService {

    /**
     * 获取附近设备
     *
     * @param userId 用户ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 半径（米）
     * @return 设备列表
     */
    ResponseDTO<List<MobileDeviceItem>> getNearbyDevices(Long userId, Double latitude, Double longitude, Integer radius);

    /**
     * 获取移动端用户权限
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    ResponseDTO<MobileUserPermissions> getMobileUserPermissions(Long userId);

    /**
     * 获取移动端实时状态
     *
     * @param deviceId 设备ID
     * @return 状态信息
     */
    ResponseDTO<MobileRealTimeStatus> getMobileRealTimeStatus(Long deviceId);

    /**
     * 分页查询设备
     *
     * @param queryForm 查询表单
     * @return 设备分页结果
     */
    ResponseDTO<PageResult<AccessDeviceVO>> queryDevices(AccessDeviceQueryForm queryForm);

    /**
     * 查询设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    ResponseDTO<AccessDeviceVO> getDeviceDetail(Long deviceId);

    /**
     * 添加设备
     *
     * @param addForm 添加表单
     * @return 设备ID
     */
    ResponseDTO<Long> addDevice(AccessDeviceAddForm addForm);

    /**
     * 更新设备
     *
     * @param updateForm 更新表单
     * @return 是否成功
     */
    ResponseDTO<Boolean> updateDevice(AccessDeviceUpdateForm updateForm);

    /**
     * 删除设备
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    ResponseDTO<Boolean> deleteDevice(Long deviceId);

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param status 设备状态
     * @return 是否成功
     */
    ResponseDTO<Boolean> updateDeviceStatus(Long deviceId, Integer status);

    /**
     * 获取移动端区域列表
     * <p>
     * 获取用户有权限访问的区域列表，包含区域详情（名称、类型、设备数量等）
     * </p>
     *
     * @param userId 用户ID（可选，不传则从Token获取）
     * @return 区域列表
     */
    ResponseDTO<List<MobileAreaItem>> getMobileAreas(Long userId);

    // ==================== 移动端设备管理功能 ====================

    /**
     * 获取移动端设备列表
     * <p>
     * 获取用户有权限管理的设备列表，支持按类型、状态、区域、关键词过滤
     * </p>
     *
     * @param userId 用户ID（可选，不传则从Token获取）
     * @param deviceType 设备类型（可选）
     * @param status 设备状态（可选）
     * @param areaId 所属区域（可选）
     * @param keyword 关键词搜索（可选）
     * @return 设备列表
     */
    ResponseDTO<List<MobileDeviceVO>> getMobileDeviceList(Long userId, Integer deviceType,
                                                        Integer status, Long areaId, String keyword);

    /**
     * 设备控制操作
     * <p>
     * 对指定设备执行控制操作，如重启、维护、校准等
     * </p>
     *
     * @param request 控制请求
     * @return 控制结果
     */
    ResponseDTO<DeviceControlResultVO> controlDevice(DeviceControlRequest request);

    /**
     * 添加设备
     * <p>
     * 移动端添加新设备，自动分配设备ID，记录操作日志
     * </p>
     *
     * @param request 添加设备请求
     * @return 设备ID
     */
    ResponseDTO<Long> addMobileDevice(AddDeviceRequest request);

    /**
     * 删除设备
     * <p>
     * 软删除设备，保留历史记录，验证权限
     * </p>
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    ResponseDTO<Boolean> deleteMobileDevice(Long deviceId);

    /**
     * 重启设备
     * <p>
     * 远程重启设备，支持软重启和硬重启
     * </p>
     *
     * @param deviceId 设备ID
     * @param restartType 重启类型（soft/hard）
     * @param reason 重启原因
     * @return 重启结果
     */
    ResponseDTO<DeviceControlResultVO> restartDevice(Long deviceId, String restartType, String reason);

    /**
     * 设备维护模式
     * <p>
     * 设置设备维护模式，支持设置维护时长和原因
     * </p>
     *
     * @param deviceId 设备ID
     * @param maintenanceDuration 维护时长（小时）
     * @param reason 维护原因
     * @return 操作结果
     */
    ResponseDTO<DeviceControlResultVO> setMaintenanceMode(Long deviceId, Integer maintenanceDuration, String reason);

    /**
     * 校准设备
     * <p>
     * 校准设备传感器或识别模块，支持多种校准类型
     * </p>
     *
     * @param deviceId 设备ID
     * @param calibrationType 校准类型
     * @param calibrationPrecision 校准精度
     * @return 校准结果
     */
    ResponseDTO<DeviceControlResultVO> calibrateDevice(Long deviceId, String calibrationType, String calibrationPrecision);

    /**
     * 获取移动端设备详情
     * <p>
     * 获取设备详细信息，包括运行状态、统计数据、维护记录等
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    ResponseDTO<MobileDeviceVO> getMobileDeviceDetail(Long deviceId);
}

