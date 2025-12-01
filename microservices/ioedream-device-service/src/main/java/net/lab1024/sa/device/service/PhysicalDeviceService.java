package net.lab1024.sa.device.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.device.domain.form.PhysicalDeviceAddForm;
import net.lab1024.sa.device.domain.form.PhysicalDeviceQueryForm;
import net.lab1024.sa.device.domain.form.PhysicalDeviceUpdateForm;
import net.lab1024.sa.device.domain.vo.PhysicalDeviceDetailVO;
import net.lab1024.sa.device.domain.vo.PhysicalDeviceVO;

import java.util.List;
import java.util.Map;

/**
 * 物理设备服务接口
 *
 * 专注于物理设备的基础管理功能
 *
 * @author IOE-DREAM Team
 */
public interface PhysicalDeviceService {

    /**
     * 注册物理设备
     *
     * @param addForm 设备添加表单
     * @return 设备ID
     */
    Long registerPhysicalDevice(PhysicalDeviceAddForm addForm);

    /**
     * 分页查询物理设备
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<PhysicalDeviceVO> queryPhysicalDevicePage(PhysicalDeviceQueryForm queryForm);

    /**
     * 获取设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    PhysicalDeviceDetailVO getPhysicalDeviceDetail(Long deviceId);

    /**
     * 更新设备信息
     *
     * @param updateForm 更新表单
     */
    void updatePhysicalDevice(PhysicalDeviceUpdateForm updateForm);

    /**
     * 注销设备（软删除）
     *
     * @param deviceId 设备ID
     */
    void unregisterPhysicalDevice(Long deviceId);

    /**
     * 连接设备
     *
     * @param deviceId 设备ID
     */
    void connectDevice(Long deviceId);

    /**
     * 断开设备连接
     *
     * @param deviceId 设备ID
     */
    void disconnectDevice(Long deviceId);

    /**
     * 处理设备心跳
     *
     * @param deviceId    设备ID
     * @param heartbeatData 心跳数据
     * @return 处理结果
     */
    boolean handleDeviceHeartbeat(Long deviceId, Map<String, Object> heartbeatData);

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态信息
     */
    Map<String, Object> getDeviceStatus(Long deviceId);

    /**
     * 获取在线设备列表
     *
     * @param deviceType 设备类型（可选）
     * @return 在线设备列表
     */
    List<PhysicalDeviceVO> getOnlineDeviceList(String deviceType);

    /**
     * 获取离线设备列表
     *
     * @param deviceType 设备类型（可选）
     * @return 离线设备列表
     */
    List<PhysicalDeviceVO> getOfflineDeviceList(String deviceType);

    /**
     * 批量连接设备
     *
     * @param deviceIds 设备ID列表
     * @return 连接结果统计
     */
    Map<String, Integer> batchConnectDevices(List<Long> deviceIds);

    /**
     * 记录设备维护
     *
     * @param deviceId       设备ID
     * @param maintenanceData 维护数据
     */
    void recordMaintenance(Long deviceId, Map<String, Object> maintenanceData);

    /**
     * 获取维护历史
     *
     * @param deviceId 设备ID
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 维护历史列表
     */
    List<Map<String, Object>> getMaintenanceHistory(Long deviceId, Integer pageNum, Integer pageSize);
}