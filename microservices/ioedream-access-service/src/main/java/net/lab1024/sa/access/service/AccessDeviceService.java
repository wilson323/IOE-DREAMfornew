package net.lab1024.sa.access.service;

import java.util.List;

import net.lab1024.sa.access.controller.AccessMobileController.MobileAreaItem;
import net.lab1024.sa.access.controller.AccessMobileController.MobileDeviceItem;
import net.lab1024.sa.access.controller.AccessMobileController.MobileRealTimeStatus;
import net.lab1024.sa.access.controller.AccessMobileController.MobileUserPermissions;
import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
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
}
