package net.lab1024.sa.consume.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeDeviceAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeDeviceQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeDeviceUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceVO;

import java.util.List;
import java.util.Map;

/**
 * 消费设备服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public interface ConsumeDeviceService {

    /**
     * 分页查询设备列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeDeviceVO> queryDevices(ConsumeDeviceQueryForm queryForm);

    /**
     * 获取设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    ConsumeDeviceVO getDeviceDetail(Long deviceId);

    /**
     * 创建设备
     *
     * @param addForm 设备信息
     * @return 设备ID
     */
    Long createDevice(ConsumeDeviceAddForm addForm);

    /**
     * 更新设备
     *
     * @param deviceId 设备ID
     * @param updateForm 更新信息
     */
    void updateDevice(Long deviceId, ConsumeDeviceUpdateForm updateForm);

    /**
     * 删除设备
     *
     * @param deviceId 设备ID
     */
    void deleteDevice(Long deviceId);

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    String getDeviceStatus(Long deviceId);

    /**
     * 更新设备状态
     *
     * @param deviceId 设备ID
     * @param status 设备状态
     */
    void updateDeviceStatus(Long deviceId, String status);

    /**
     * 获取在线设备列表
     *
     * @return 在线设备列表
     */
    List<ConsumeDeviceVO> getOnlineDevices();

    /**
     * 获取离线设备列表
     *
     * @return 离线设备列表
     */
    List<ConsumeDeviceVO> getOfflineDevices();

    /**
     * 重启设备
     *
     * @param deviceId 设备ID
     */
    void restartDevice(Long deviceId);

    /**
     * 同步设备配置
     *
     * @param deviceId 设备ID
     */
    void syncDeviceConfig(Long deviceId);

    /**
     * 获取设备统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getDeviceStatistics();

    /**
     * 批量操作设备
     *
     * @param deviceIds 设备ID列表
     * @param operation 操作类型
     */
    void batchOperateDevices(List<Long> deviceIds, String operation);

    /**
     * 获取设备健康状态
     *
     * @param deviceId 设备ID
     * @return 健康状态
     */
    String getDeviceHealthStatus(Long deviceId);

    /**
     * 检查设备连接
     *
     * @param deviceId 设备ID
     * @return 是否连接
     */
    Boolean checkDeviceConnection(Long deviceId);

    /**
     * 获取设备配置信息
     *
     * @param deviceId 设备ID
     * @return 配置信息
     */
    Map<String, Object> getDeviceConfig(Long deviceId);

    /**
     * 更新设备配置
     *
     * @param deviceId 设备ID
     * @param config 配置信息
     */
    void updateDeviceConfig(Long deviceId, Map<String, Object> config);
}